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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
 * audit table with {@code REVTYPE=0} (ADD) under a single backfill revision entry.
 * <p>
 * Audit tables are detected by the presence of {@code REV} and {@code REVTYPE} columns — the
 * Envers metadata columns always present regardless of the configured audit table suffix or prefix.
 * This avoids any dependency on runtime properties that may not be loaded when Liquibase runs.
 * <p>
 * Because this is a Liquibase changeset it is tracked in {@code liquibasechangelog} and runs
 * exactly once per database, never on subsequent startups.
 */
public class BackfillEnversAuditTablesChangeset implements CustomTaskChange {

	private static final Logger log = LoggerFactory.getLogger(BackfillEnversAuditTablesChangeset.class);

	private static final Pattern SAFE_SQL_IDENTIFIER = Pattern.compile("[a-zA-Z_]\\w*");

	@Override
	public void execute(Database database) throws CustomChangeException {
		try {
			Connection connection = ((JdbcConnection) database.getConnection()).getUnderlyingConnection();

			String revisionTableName = findRevisionEntityTable(connection);

			// Discover (sourceTable, auditTable) pairs by detecting Envers audit tables via
			// REV + REVTYPE columns — independent of the configured audit suffix.
			List<String[]> auditPairs = discoverAuditPairs(connection);

			// Temporarily disable FK checks on MySQL/MariaDB so that joined-subclass audit
			// tables (patient_aud → person_aud, drug_order_aud → orders_aud) can be
			// backfilled regardless of iteration order.
			boolean mysqlCompatible = isMysqlCompatible(connection);
			if (mysqlCompatible) {
				try (Statement stmt = connection.createStatement()) {
					stmt.execute("SET FOREIGN_KEY_CHECKS=0");
				}
			}

			Integer revId = null;
			try {
				for (String[] pair : auditPairs) {
					revId = tryBackfillEntity(connection, pair[0], pair[1], revisionTableName, revId);
				}
			}
			finally {
				if (mysqlCompatible) {
					try (Statement stmt = connection.createStatement()) {
						stmt.execute("SET FOREIGN_KEY_CHECKS=1");
					}
				}
			}

			if (revId != null) {
				log.info("Audit table backfill completed successfully with initial revision ID {}", revId);
			} else {
				log.debug("No audit tables needed backfilling.");
			}
		}
		catch (Exception e) {
			throw new CustomChangeException("Failed to backfill Envers audit tables", e);
		}
	}

	private boolean isMysqlCompatible(Connection connection) {
		try {
			String productName = connection.getMetaData().getDatabaseProductName().toLowerCase();
			return productName.contains("mysql") || productName.contains("mariadb");
		}
		catch (SQLException e) {
			return false;
		}
	}

	/**
	 * Discovers (sourceTable, auditTable) pairs without relying on the configured audit suffix.
	 * Identifies Envers audit tables by the presence of {@code REV} and {@code REVTYPE} columns,
	 * then matches each audit table to its source table using longest-prefix matching.
	 *
	 * @param connection JDBC connection
	 * @return list of [sourceTable, auditTable] pairs
	 * @throws SQLException if metadata cannot be read
	 */
	List<String[]> discoverAuditPairs(Connection connection) throws SQLException {
		DatabaseMetaData metaData = connection.getMetaData();
		// Restrict to the current catalog so system tables from other databases
		// (mysql, information_schema, etc.) are not included in the search.
		String catalog = connection.getCatalog();

		List<String> allTables = new ArrayList<>();
		try (ResultSet tables = metaData.getTables(catalog, null, "%", new String[] { "TABLE" })) {
			while (tables.next()) {
				allTables.add(tables.getString("TABLE_NAME"));
			}
		}

		// Phase 1: Find definite Envers audit tables (have both REV and REVTYPE) and
		// potential subclass audit tables (have REV but not REVTYPE).
		// In Hibernate Envers with joined-table inheritance, subclass audit tables
		// (e.g. patient_aud, drug_order_aud) do NOT have their own REVTYPE column —
		// only the root-class audit table does. So we detect them separately.
		Set<String> definiteAuditSet = new HashSet<>();
		Set<String> revOnlySet = new HashSet<>();
		for (String tableName : allTables) {
			if (isEnversAuditTable(connection, tableName)) {
				definiteAuditSet.add(tableName);
			} else if (hasRevColumn(connection, tableName)) {
				revOnlySet.add(tableName);
			}
		}

		// Source tables: everything that is not a (potential) audit table
		Set<String> sourceTableNames = new HashSet<>();
		for (String tableName : allTables) {
			if (!definiteAuditSet.contains(tableName) && !revOnlySet.contains(tableName)) {
				sourceTableNames.add(tableName.toLowerCase());
			}
		}

		// For each definite audit table, find the longest-prefix matching source table
		List<String[]> pairs = new ArrayList<>();
		for (String auditTable : definiteAuditSet) {
			String lowerAudit = auditTable.toLowerCase();
			String bestMatch = null;
			for (String sourceTable : sourceTableNames) {
				if (lowerAudit.startsWith(sourceTable) && lowerAudit.length() > sourceTable.length()) {
					if (bestMatch == null || sourceTable.length() > bestMatch.length()) {
						bestMatch = sourceTable;
					}
				}
			}
			if (bestMatch != null) {
				for (String tableName : allTables) {
					if (tableName.equalsIgnoreCase(bestMatch)) {
						pairs.add(new String[] { tableName, auditTable });
						break;
					}
				}
			}
		}

		// Infer the audit suffix by majority vote from definite pairs to filter false matches.
		String inferredSuffix = null;
		if (!pairs.isEmpty()) {
			Map<String, Integer> suffixVotes = new HashMap<>();
			for (String[] pair : pairs) {
				String suffix = pair[1].toLowerCase().substring(pair[0].toLowerCase().length());
				suffixVotes.merge(suffix, 1, Integer::sum);
			}
			inferredSuffix = suffixVotes.entrySet().stream()
			        .max(Map.Entry.comparingByValue())
			        .get().getKey();
			log.warn("Inferred audit suffix '{}' from majority vote across {} pairs", inferredSuffix, pairs.size());
			final String finalSuffix = inferredSuffix;
			pairs.removeIf(pair -> !pair[1].toLowerCase().substring(pair[0].toLowerCase().length()).equals(finalSuffix));
		}

		// Phase 2: Add joined-subclass audit tables (have REV but not REVTYPE).
		// These are matched by checking their name ends with the inferred suffix
		// and a matching source table exists (e.g. patient_aud → patient).
		if (inferredSuffix != null) {
			for (String auditTable : revOnlySet) {
				String lowerAudit = auditTable.toLowerCase();
				if (lowerAudit.endsWith(inferredSuffix)) {
					String sourceTableLower = lowerAudit.substring(0, lowerAudit.length() - inferredSuffix.length());
					if (sourceTableNames.contains(sourceTableLower)) {
						for (String tableName : allTables) {
							if (tableName.equalsIgnoreCase(sourceTableLower)) {
								pairs.add(new String[] { tableName, auditTable });
								break;
							}
						}
					}
				}
			}
		}

		log.warn("Discovered {} audit table pairs to backfill: {}", pairs.size(),
		    pairs.stream().map(p -> p[0] + " -> " + p[1]).collect(java.util.stream.Collectors.joining(", ")));
		return pairs;
	}

	/**
	 * Returns true if the table is an Envers audit table, detected by the presence of both
	 * {@code REV} and {@code REVTYPE} columns.
	 */
	boolean isEnversAuditTable(Connection connection, String tableName) {
		String safeTableName;
		try {
			safeTableName = requireSafeIdentifier(tableName);
		}
		catch (IllegalArgumentException e) {
			return false;
		}
		try (Statement stmt = connection.createStatement()) {
			stmt.execute("SELECT REV, REVTYPE FROM " + safeTableName + " WHERE 1=0");
			return true;
		}
		catch (Exception e) {
			return false;
		}
	}

	/**
	 * Returns true if the table has a {@code REV} column but not {@code REVTYPE}.
	 * Used to detect joined-subclass Envers audit tables which only carry the REV FK.
	 */
	boolean hasRevColumn(Connection connection, String tableName) {
		String safeTableName;
		try {
			safeTableName = requireSafeIdentifier(tableName);
		}
		catch (IllegalArgumentException e) {
			return false;
		}
		try (Statement stmt = connection.createStatement()) {
			stmt.execute("SELECT REV FROM " + safeTableName + " WHERE 1=0");
			return true;
		}
		catch (Exception e) {
			return false;
		}
	}

	private Integer tryBackfillEntity(Connection connection, String sourceTable, String auditTable, String revisionTableName,
	        Integer revId) {
		try {
			if (!isAuditTableEmpty(connection, auditTable)) {
				log.warn("Skipping {} - audit table already has data", auditTable);
				return revId;
			}
			if (isTableEmpty(connection, sourceTable)) {
				log.warn("Skipping {} - source table {} is empty", auditTable, sourceTable);
				return revId;
			}
			if (revId == null) {
				revId = createBackfillRevision(connection, revisionTableName);
			}
			List<String> columns = getAuditTableDataColumns(connection, auditTable, sourceTable);
			if (columns.isEmpty()) {
				log.warn("Skipping {} - no common columns found between {} and {}", auditTable, sourceTable, auditTable);
				return revId;
			}
			backfillTable(connection, sourceTable, auditTable, columns, revId);
		}
		catch (SQLException e) {
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
		        ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM " + requireSafeIdentifier(tableName))) {
			return rs.next() && rs.getLong(1) == 0;
		}
		catch (SQLException e) {
			log.debug("Audit table {} not accessible, skipping backfill: {}", tableName, e.getMessage());
			return false;
		}
	}

	/**
	 * Returns true if the given source table has no rows.
	 */
	static boolean isTableEmpty(Connection connection, String tableName) throws SQLException {
		try (Statement stmt = connection.createStatement();
		        ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM " + requireSafeIdentifier(tableName))) {
			return rs.next() && rs.getLong(1) == 0;
		}
	}

	/**
	 * Returns the column names that exist in both the audit table and the source table, excluding the
	 * Envers metadata columns REV and REVTYPE. Using the intersection avoids INSERT failures caused by
	 * extra columns in the audit table that have no counterpart in the source table (e.g. columns
	 * added by Envers for joined-subclass inheritance tracking).
	 */
	static List<String> getAuditTableDataColumns(Connection connection, String auditTable, String sourceTable)
	        throws SQLException {
		DatabaseMetaData metaData = connection.getMetaData();

		Set<String> sourceColumns = new HashSet<>();
		try (ResultSet rs = metaData.getColumns(null, null, sourceTable, null)) {
			while (rs.next()) {
				sourceColumns.add(rs.getString("COLUMN_NAME").toLowerCase());
			}
		}

		List<String> columns = new ArrayList<>();
		try (ResultSet rs = metaData.getColumns(null, null, auditTable, null)) {
			while (rs.next()) {
				String colName = rs.getString("COLUMN_NAME");
				if (!colName.equalsIgnoreCase("REV") && !colName.equalsIgnoreCase("REVTYPE")
				        && sourceColumns.contains(colName.toLowerCase())) {
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
			log.warn("Backfilled {} rows from {} into {}", rows, sourceTable, auditTable);
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
