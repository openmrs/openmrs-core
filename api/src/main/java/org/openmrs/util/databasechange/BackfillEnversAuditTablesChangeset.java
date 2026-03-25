/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.util.databasechange;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import liquibase.change.custom.CustomTaskChange;
import liquibase.database.Database;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.CustomChangeException;
import liquibase.exception.SetupException;
import liquibase.exception.ValidationErrors;
import liquibase.resource.ResourceAccessor;

/**
 * Liquibase {@link CustomTaskChange} that backfills pre-existing rows into Envers audit tables.
 * <p>
 * When Envers auditing is enabled after data already exists, audit tables are empty and Envers
 * cannot resolve references to those pre-existing entities, causing "Unable to read" errors in the
 * audit UI. This changeset inserts all existing rows from each source table into the corresponding
 * {@code *_audit} table with {@code REVTYPE=0} (ADD) under a single backfill revision entry.
 * <p>
 * Because this is a Liquibase changeset it is tracked in {@code databasechangelog} and runs exactly
 * once per database, never on subsequent startups.
 */
public class BackfillEnversAuditTablesChangeset implements CustomTaskChange {

	private static final Logger log = LoggerFactory.getLogger(BackfillEnversAuditTablesChangeset.class);

	private static final Pattern SAFE_SQL_IDENTIFIER = Pattern.compile("[a-zA-Z_]\\w*");

	private String auditSuffix = "_audit";

	public void setAuditSuffix(String auditSuffix) {
		this.auditSuffix = auditSuffix;
	}

	@Override
	public void execute(Database database) throws CustomChangeException {
		try {
			Connection connection = ((JdbcConnection) database.getConnection()).getUnderlyingConnection();

			String revisionTableName = findRevisionEntityTable(connection);
			Integer revId = null;

			DatabaseMetaData metaData = connection.getMetaData();
			try (ResultSet tables = metaData.getTables(null, null, "%", new String[] { "TABLE" })) {
				while (tables.next()) {
					String tableName = tables.getString("TABLE_NAME");
					if (!tableName.endsWith(auditSuffix)) {
						continue;
					}
					String sourceTable = tableName.substring(0, tableName.length() - auditSuffix.length());
					if (doesTableExist(connection, sourceTable)) {
						revId = tryBackfillEntity(connection, sourceTable, tableName, revisionTableName, revId);
					}
				}
			}

			if (revId != null) {
				log.info("Audit table backfill completed successfully with initial revision ID {}", revId);
			} else {
				log.debug("No audit tables needed backfilling.");
			}
		} catch (Exception e) {
			throw new CustomChangeException("Failed to backfill Envers audit tables", e);
		}
	}

	private Integer tryBackfillEntity(Connection connection, String sourceTable, String auditTable, String revisionTableName,
	        Integer revId) {
		try {
			if (!isAuditTableEmpty(connection, auditTable) || isTableEmpty(connection, sourceTable)) {
				return revId;
			}
			if (revId == null) {
				revId = createBackfillRevision(connection, revisionTableName);
			}
			List<String> columns = getAuditTableDataColumns(connection, auditTable);
			if (!columns.isEmpty()) {
				backfillTable(connection, sourceTable, auditTable, columns, revId);
			}
		} catch (SQLException e) {
			log.warn("Failed to backfill audit table {}: {}", auditTable, e.getMessage());
		}
		return revId;
	}

	/**
	 * Creates a backfill revision entry in the revision entity table. Dynamically discovers the primary
	 * key and timestamp column names from JDBC metadata to avoid hardcoding Hibernate-version-specific
	 * names.
	 *
	 * @param connection JDBC connection
	 * @param revisionTableName name of the revision entity table
	 * @return the generated revision ID
	 * @throws SQLException if the revision entry cannot be created
	 */
	static int createBackfillRevision(Connection connection, String revisionTableName) throws SQLException {
		String pkColumn = getRevisionPrimaryKeyColumn(connection, revisionTableName);
		String timestampColumn = getRevisionTimestampColumn(connection, revisionTableName);
		int nextId;
		try (Statement stmt = connection.createStatement();
		        ResultSet rs = stmt.executeQuery("SELECT COALESCE(MAX(" + requireSafeIdentifier(pkColumn) + "), 0) + 1 FROM "
		                + requireSafeIdentifier(revisionTableName))) {
			nextId = rs.next() ? rs.getInt(1) : 1;
		}
		String sql = "INSERT INTO " + requireSafeIdentifier(revisionTableName) + " (" + requireSafeIdentifier(pkColumn)
		        + ", " + requireSafeIdentifier(timestampColumn) + ") VALUES (?, ?)";
		try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
			pstmt.setInt(1, nextId);
			pstmt.setLong(2, System.currentTimeMillis());
			pstmt.executeUpdate();
			return nextId;
		}
	}

	/**
	 * Discovers the primary key column name of the revision entity table via JDBC metadata.
	 *
	 * @param connection JDBC connection
	 * @param revisionTableName name of the revision entity table
	 * @return the primary key column name, falling back to "id" if not found
	 * @throws SQLException if metadata cannot be read
	 */
	static String getRevisionPrimaryKeyColumn(Connection connection, String revisionTableName) throws SQLException {
		DatabaseMetaData metaData = connection.getMetaData();
		for (String name : new String[] { revisionTableName, revisionTableName.toUpperCase() }) {
			try (ResultSet rs = metaData.getPrimaryKeys(null, null, name)) {
				if (rs.next()) {
					return rs.getString("COLUMN_NAME");
				}
			}
		}
		return "id";
	}

	/**
	 * Discovers the timestamp column name in the revision entity table by finding the first BIGINT
	 * column that is not the primary key. This avoids hardcoding Hibernate-version-specific names like
	 * "REVTSTMP" which may differ across Hibernate versions.
	 *
	 * @param connection JDBC connection
	 * @param revisionTableName name of the revision entity table
	 * @return the timestamp column name, falling back to "REVTSTMP" if not found
	 * @throws SQLException if metadata cannot be read
	 */
	static String getRevisionTimestampColumn(Connection connection, String revisionTableName) throws SQLException {
		DatabaseMetaData metaData = connection.getMetaData();
		String pkColumn = null;
		for (String name : new String[] { revisionTableName, revisionTableName.toUpperCase() }) {
			try (ResultSet pkRs = metaData.getPrimaryKeys(null, null, name)) {
				if (pkRs.next()) {
					pkColumn = pkRs.getString("COLUMN_NAME");
					break;
				}
			}
		}
		for (String name : new String[] { revisionTableName, revisionTableName.toUpperCase() }) {
			try (ResultSet colRs = metaData.getColumns(null, null, name, null)) {
				while (colRs.next()) {
					String colName = colRs.getString("COLUMN_NAME");
					int dataType = colRs.getInt("DATA_TYPE");
					if (dataType == java.sql.Types.BIGINT && !colName.equalsIgnoreCase(pkColumn)) {
						return colName;
					}
				}
			}
		}
		return "REVTSTMP";
	}

	/**
	 * Validates that a SQL identifier (table or column name) contains only safe characters, preventing
	 * SQL injection when identifiers must be concatenated into queries.
	 *
	 * @param identifier the SQL identifier to validate
	 * @return the identifier unchanged if safe
	 * @throws IllegalArgumentException if the identifier contains unsafe characters
	 */
	static String requireSafeIdentifier(String identifier) {
		if (identifier == null || !SAFE_SQL_IDENTIFIER.matcher(identifier).matches()) {
			throw new IllegalArgumentException("Unsafe SQL identifier rejected: " + identifier);
		}
		return identifier;
	}

	/**
	 * Returns true if the given audit table exists but contains no rows.
	 */
	static boolean isAuditTableEmpty(Connection connection, String tableName) {
		try (Statement stmt = connection.createStatement();
		        ResultSet rs = stmt.executeQuery("SELECT 1 FROM " + requireSafeIdentifier(tableName) + " LIMIT 1")) {
			return !rs.next();
		} catch (SQLException e) {
			log.debug("Audit table {} not accessible, skipping backfill: {}", tableName, e.getMessage());
			return false;
		}
	}

	/**
	 * Returns true if the given source table has no rows.
	 */
	static boolean isTableEmpty(Connection connection, String tableName) throws SQLException {
		try (Statement stmt = connection.createStatement();
		        ResultSet rs = stmt.executeQuery("SELECT 1 FROM " + requireSafeIdentifier(tableName) + " LIMIT 1")) {
			return !rs.next();
		}
	}

	/**
	 * Returns the data column names from the given audit table, excluding the Envers metadata columns
	 * REV and REVTYPE. These are the columns that correspond to the audited entity fields and must
	 * exist in the source table.
	 */
	static List<String> getAuditTableDataColumns(Connection connection, String auditTable) throws SQLException {
		List<String> columns = new ArrayList<>();
		DatabaseMetaData metaData = connection.getMetaData();
		try (ResultSet rs = metaData.getColumns(null, null, auditTable, null)) {
			while (rs.next()) {
				String colName = rs.getString("COLUMN_NAME");
				if (!colName.equalsIgnoreCase("REV") && !colName.equalsIgnoreCase("REVTYPE")) {
					columns.add(colName);
				}
			}
		}
		return columns;
	}

	/**
	 * Inserts all rows from the source table into the audit table with REVTYPE=0 (ADD).
	 */
	static void backfillTable(Connection connection, String sourceTable, String auditTable, List<String> columns, int revId)
	        throws SQLException {
		requireSafeIdentifier(sourceTable);
		requireSafeIdentifier(auditTable);
		columns.forEach(BackfillEnversAuditTablesChangeset::requireSafeIdentifier);
		String columnList = String.join(", ", columns);
		String sql = "INSERT INTO " + auditTable + " (REV, REVTYPE, " + columnList + ") SELECT " + revId + ", 0, "
		        + columnList + " FROM " + sourceTable;
		try (Statement stmt = connection.createStatement()) {
			int rows = stmt.executeUpdate(sql);
			log.info("Backfilled {} rows from {} into {}", rows, sourceTable, auditTable);
		}
	}

	private String findRevisionEntityTable(Connection connection) throws SQLException {
		for (String name : new String[] { "revision_entity", "REVINFO" }) {
			if (doesTableExist(connection, name)) {
				return name;
			}
		}
		return "revision_entity";
	}

	private boolean doesTableExist(Connection connection, String tableName) throws SQLException {
		DatabaseMetaData metaData = connection.getMetaData();
		try (ResultSet rs = metaData.getTables(null, null, tableName, new String[] { "TABLE" })) {
			return rs.next();
		}
	}

	@Override
	public String getConfirmationMessage() {
		return "Successfully backfilled pre-existing rows into Envers audit tables";
	}

	@Override
	public void setUp() throws SetupException {
	}

	@Override
	public void setFileOpener(ResourceAccessor resourceAccessor) {
	}

	@Override
	public ValidationErrors validate(Database database) {
		return null;
	}
}
