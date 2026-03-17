/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.util;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

import org.hibernate.boot.Metadata;
import org.hibernate.engine.jdbc.connections.spi.ConnectionProvider;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RevisionEntity;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.tool.schema.TargetType;
import org.hibernate.tool.schema.spi.ExceptionHandler;
import org.hibernate.tool.schema.spi.ExecutionOptions;
import org.hibernate.tool.schema.spi.SchemaManagementTool;
import org.hibernate.tool.schema.spi.SchemaMigrator;
import org.hibernate.tool.schema.spi.ScriptTargetOutput;
import org.hibernate.tool.schema.spi.TargetDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Initializes Hibernate Envers audit tables when auditing is enabled. This class is responsible for
 * conditionally creating audit tables only when hibernate.integration.envers.enabled=true.
 */
public class EnversAuditTableInitializer {

	private static final Logger log = LoggerFactory.getLogger(EnversAuditTableInitializer.class);

	private static final Pattern SAFE_SQL_IDENTIFIER = Pattern.compile("[a-zA-Z_]\\w*");

	private EnversAuditTableInitializer() {

	}

	/**
	 * Checks if Envers is enabled and creates/updates audit tables as needed. This will Create or
	 * Update audit tables if they don't exist - Update existing audit tables if the schema has changed.
	 * After schema updates, backfills pre-existing data so Envers can resolve references to entities
	 * that existed before auditing was enabled.
	 *
	 * @param metadata Hibernate metadata containing entity mappings
	 * @param hibernateProperties properties containing Envers configuration
	 * @param serviceRegistry Hibernate service registry
	 */
	public static void initialize(Metadata metadata, Properties hibernateProperties, ServiceRegistry serviceRegistry) {

		if (!isEnversEnabled(hibernateProperties)) {
			log.debug("Hibernate Envers is not enabled. Skipping audit table initialization.");
			return;
		}

		updateAuditTables(metadata, hibernateProperties, serviceRegistry);
		backfillAuditTables(metadata, hibernateProperties, serviceRegistry);
	}

	/**
	 * Checks if Hibernate Envers is enabled in the configuration.
	 *
	 * @param properties Hibernate properties
	 * @return true if Envers is enabled, false otherwise
	 */
	private static boolean isEnversEnabled(Properties properties) {
		String enversEnabled = properties.getProperty("hibernate.integration.envers.enabled");
		return "true".equalsIgnoreCase(enversEnabled);
	}

	/**
	 * Creates or updates audit tables using Hibernate's {@link SchemaMigrator}. This method filters to
	 * only process audit tables.
	 *
	 * @param metadata Hibernate metadata containing entity mappings (includes Envers audit entities)
	 * @param hibernateProperties Hibernate configuration properties
	 * @param serviceRegistry Hibernate service registry
	 */
	private static void updateAuditTables(Metadata metadata, Properties hibernateProperties,
	        ServiceRegistry serviceRegistry) {
		String auditTablePrefix = hibernateProperties.getProperty("org.hibernate.envers.audit_table_prefix", "");
		String auditTableSuffix = hibernateProperties.getProperty("org.hibernate.envers.audit_table_suffix", "_audit");

		Map<String, Object> settings = (Map) hibernateProperties;
		AtomicBoolean hasErrors = new AtomicBoolean(false);
		ExecutionOptions executionOptions = getExecutionOptions(settings, hasErrors);
		SchemaMigrator schemaMigrator = serviceRegistry.getService(SchemaManagementTool.class).getSchemaMigrator(settings);

		TargetDescriptor targetDescriptor = getTargetDescriptor();

		schemaMigrator.doMigration(metadata, executionOptions, contributed -> {
			String tableName = contributed.getExportIdentifier();
			if (tableName == null) {
				return false;
			}

			String lowerTableName = tableName.toLowerCase();

			if (lowerTableName.contains("revision") || lowerTableName.equals("revinfo")) {
				return true;
			}

			String lowerPrefix = auditTablePrefix.toLowerCase();
			String lowerSuffix = auditTableSuffix.toLowerCase();

			boolean hasPrefix = lowerPrefix.isEmpty() || lowerTableName.startsWith(lowerPrefix);
			boolean hasSuffix = lowerSuffix.isEmpty() || lowerTableName.endsWith(lowerSuffix);

			return hasPrefix && hasSuffix;
		}, targetDescriptor);

		if (hasErrors.get()) {
			log.warn("Envers audit table migration completed with errors.");
		} else {
			log.info("Successfully created/updated Envers audit tables using Hibernate SchemaManagementTool.");
		}
	}

	/**
	 * Backfills pre-existing data into newly created audit tables. When auditing is enabled after data
	 * already exists, audit tables are empty and Envers cannot resolve references to those pre-existing
	 * entities, causing "Unable to read" in the audit UI. This method inserts all existing rows from
	 * each source table into the corresponding audit table with REVTYPE=0 (ADD), but only when the
	 * audit table is empty (i.e. it was just created).
	 *
	 * @param metadata Hibernate metadata containing entity mappings
	 * @param hibernateProperties Hibernate configuration properties
	 * @param serviceRegistry Hibernate service registry
	 */
	private static void backfillAuditTables(Metadata metadata, Properties hibernateProperties,
	        ServiceRegistry serviceRegistry) {
		String auditTablePrefix = hibernateProperties.getProperty("org.hibernate.envers.audit_table_prefix", "");
		String auditTableSuffix = hibernateProperties.getProperty("org.hibernate.envers.audit_table_suffix", "_audit");

		ConnectionProvider connectionProvider = serviceRegistry.getService(ConnectionProvider.class);
		Connection connection = null;

		try {
			connection = connectionProvider.getConnection();
			boolean originalAutoCommit = connection.getAutoCommit();
			connection.setAutoCommit(false);

			String revisionTableName = getRevisionEntityTableName(metadata);
			Integer revId = null;

			for (PersistentClass persistentClass : metadata.getEntityBindings()) {
				Class<?> mappedClass = persistentClass.getMappedClass();
				if (mappedClass == null || !isAuditedClass(mappedClass)) {
					continue;
				}
				String sourceTable = persistentClass.getTable().getName();
				String auditTable = auditTablePrefix + sourceTable + auditTableSuffix;
				revId = tryBackfillEntity(connection, sourceTable, auditTable, revisionTableName, revId);
			}

			if (revId != null) {
				connection.commit();
				log.info("Audit table backfill completed successfully with initial revision ID {}", revId);
			} else {
				log.debug("No audit tables needed backfilling.");
			}

			connection.setAutoCommit(originalAutoCommit);
		} catch (SQLException e) {
			log.error("Failed to backfill audit tables", e);
			if (connection != null) {
				try {
					connection.rollback();
				} catch (SQLException ex) {
					log.error("Failed to rollback backfill transaction", ex);
				}
			}
		} finally {
			if (connection != null) {
				try {
					connectionProvider.closeConnection(connection);
				} catch (SQLException e) {
					log.error("Failed to close JDBC connection after audit backfill", e);
				}
			}
		}
	}

	/**
	 * Attempts to backfill a single entity's audit table. Skips if the audit table already has data or
	 * the source table is empty. Returns the (possibly newly created) revision ID.
	 */
	private static Integer tryBackfillEntity(Connection connection, String sourceTable, String auditTable,
	        String revisionTableName, Integer revId) {
		try {
			if (!isAuditTableEmpty(connection, auditTable) || isTableEmpty(connection, sourceTable)) {
				return revId;
			}
			if (revId == null) {
				revId = createBackfillRevision(connection, revisionTableName);
			}
			List<String> columns = getSourceTableColumns(connection, sourceTable);
			if (!columns.isEmpty()) {
				backfillTable(connection, sourceTable, auditTable, columns, revId);
			}
		} catch (SQLException e) {
			log.warn("Failed to backfill audit table {}: {}", auditTable, e.getMessage());
		}
		return revId;
	}

	/**
	 * Creates a backfill revision entry in the revision entity table. Dynamically discovers the
	 * timestamp column name from JDBC metadata to avoid hardcoding Hibernate-version-specific names.
	 *
	 * @param connection JDBC connection
	 * @param revisionTableName name of the revision entity table
	 * @return the generated revision ID
	 * @throws SQLException if the revision entry cannot be created
	 */
	static int createBackfillRevision(Connection connection, String revisionTableName) throws SQLException {
		String timestampColumn = getRevisionTimestampColumn(connection, revisionTableName);
		String sql = "INSERT INTO " + requireSafeIdentifier(revisionTableName) + " ("
		        + requireSafeIdentifier(timestampColumn) + ") VALUES (?)";
		try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
			pstmt.setLong(1, System.currentTimeMillis());
			pstmt.executeUpdate();
			try (ResultSet rs = pstmt.getGeneratedKeys()) {
				if (rs.next()) {
					return rs.getInt(1);
				}
			}
		}
		throw new SQLException("Failed to create backfill revision entry in " + revisionTableName);
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
		try (ResultSet pkRs = metaData.getPrimaryKeys(null, null, revisionTableName)) {
			if (pkRs.next()) {
				pkColumn = pkRs.getString("COLUMN_NAME");
			}
		}
		try (ResultSet colRs = metaData.getColumns(null, null, revisionTableName, null)) {
			while (colRs.next()) {
				String colName = colRs.getString("COLUMN_NAME");
				int dataType = colRs.getInt("DATA_TYPE");
				if (dataType == java.sql.Types.BIGINT && !colName.equalsIgnoreCase(pkColumn)) {
					return colName;
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
	private static String requireSafeIdentifier(String identifier) {
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
		        ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM " + requireSafeIdentifier(tableName))) {
			return rs.next() && rs.getLong(1) == 0;
		}
	}

	/**
	 * Returns all column names from the given source table using JDBC metadata.
	 */
	static List<String> getSourceTableColumns(Connection connection, String tableName) throws SQLException {
		List<String> columns = new ArrayList<>();
		DatabaseMetaData metaData = connection.getMetaData();
		try (ResultSet rs = metaData.getColumns(null, null, tableName, null)) {
			while (rs.next()) {
				columns.add(rs.getString("COLUMN_NAME"));
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
		columns.forEach(EnversAuditTableInitializer::requireSafeIdentifier);
		String columnList = String.join(", ", columns);
		String sql = "INSERT INTO " + auditTable + " (REV, REVTYPE, " + columnList + ") " + "SELECT " + revId + ", 0, "
		        + columnList + " FROM " + sourceTable;
		try (Statement stmt = connection.createStatement()) {
			int rows = stmt.executeUpdate(sql);
			log.info("Backfilled {} rows from {} into {}", rows, sourceTable, auditTable);
		}
	}

	/**
	 * Resolves the revision entity table name dynamically from Hibernate metadata by finding the entity
	 * annotated with {@link RevisionEntity}. Falls back to "revision_entity" if not found.
	 */
	private static String getRevisionEntityTableName(Metadata metadata) {
		for (PersistentClass persistentClass : metadata.getEntityBindings()) {
			Class<?> mappedClass = persistentClass.getMappedClass();
			if (mappedClass != null && mappedClass.isAnnotationPresent(RevisionEntity.class)) {
				return persistentClass.getTable().getName();
			}
		}
		return "revision_entity";
	}

	/**
	 * Returns true if the given class or any of its superclasses is annotated with {@link Audited}.
	 */
	private static boolean isAuditedClass(Class<?> clazz) {
		Class<?> current = clazz;
		while (current != null && current != Object.class) {
			if (current.isAnnotationPresent(Audited.class)) {
				return true;
			}
			current = current.getSuperclass();
		}
		return false;
	}

	private static TargetDescriptor getTargetDescriptor() {
		return new TargetDescriptor() {

			@Override
			public EnumSet<TargetType> getTargetTypes() {
				return EnumSet.of(TargetType.DATABASE);
			}

			@Override
			public ScriptTargetOutput getScriptTargetOutput() {
				return null;
			}
		};
	}

	private static ExecutionOptions getExecutionOptions(Map<String, Object> settings, AtomicBoolean hasErrors) {
		return new ExecutionOptions() {

			@Override
			public Map<String, Object> getConfigurationValues() {
				return settings;
			}

			@Override
			public boolean shouldManageNamespaces() {
				return false;
			}

			@Override
			public ExceptionHandler getExceptionHandler() {
				return throwable -> {
					hasErrors.set(true);
					log.warn("Schema migration encountered an issue: {}", throwable.getMessage());
				};
			}
		};
	}
}
