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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import org.hibernate.boot.Metadata;
import org.hibernate.engine.jdbc.connections.spi.ConnectionProvider;
import org.hibernate.mapping.Column;
import org.hibernate.mapping.KeyValue;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.mapping.Selectable;
import org.hibernate.mapping.SimpleValue;
import org.hibernate.mapping.Table;
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
 * <p>
 * It also backfills pre-existing data into newly created audit tables. Without backfilling, records
 * that existed before auditing was enabled cannot be resolved by Envers, causing "Unable to read"
 * errors when viewing audit history for entities that reference those pre-existing records.
 * </p>
 */
public class EnversAuditTableInitializer {

	private static final Logger log = LoggerFactory.getLogger(EnversAuditTableInitializer.class);

	/**
	 * Name of the revision entity table, as declared in {@code OpenmrsRevisionEntity}.
	 */
	static final String REVISION_ENTITY_TABLE = "revision_entity";

	private EnversAuditTableInitializer() {

	}

	/**
	 * Checks if Envers is enabled and creates/updates audit tables as needed, then backfills
	 * pre-existing data into those tables so that Envers can resolve all entity references.
	 *
	 * @param metadata Hibernate metadata containing entity mappings
	 * @param hibernateProperties properties containing Envers configuration
	 * @param serviceRegistry Hibernate service registry
	 */
	public static void initialize(Metadata metadata, Properties hibernateProperties,
								  ServiceRegistry serviceRegistry) {

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
	 * Creates or updates audit tables using Hibernate's {@link SchemaMigrator}. This method filters
	 * to only process audit tables.
	 *
	 * @param metadata Hibernate metadata containing entity mappings (includes Envers audit
	 *            entities)
	 * @param hibernateProperties Hibernate configuration properties
	 * @param serviceRegistry Hibernate service registry
     */
	private static void updateAuditTables(Metadata metadata, Properties hibernateProperties,
	        ServiceRegistry serviceRegistry) {
		String auditTablePrefix = hibernateProperties.getProperty("org.hibernate.envers.audit_table_prefix", "");
		String auditTableSuffix = hibernateProperties.getProperty("org.hibernate.envers.audit_table_suffix", "_audit");

		@SuppressWarnings("unchecked")
		Map<String, Object> settings = (Map<String, Object>) (Map<?, ?>) hibernateProperties;
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
	 * Backfills pre-existing (pre-audit) data into audit tables. This is necessary because when
	 * auditing is first enabled, existing records are not present in the audit tables. As a result,
	 * any new audit entries that reference those pre-existing records (e.g. an Encounter referencing
	 * an EncounterType that existed before auditing) cannot be resolved by Envers, producing
	 * "Unable to read" in the audit UI.
	 * <p>
	 * For each source entity table that has a corresponding audit table, this method inserts a
	 * snapshot (REVTYPE=0, meaning ADD) of every existing row that does not yet have any audit
	 * record. All such rows are grouped under a single synthetic backfill revision so they can be
	 * distinguished from real user-driven revisions.
	 * </p>
	 * <p>
	 * This operation is idempotent: re-running it will not create duplicate audit rows because the
	 * INSERT uses a NOT EXISTS guard on the primary-key column.
	 * </p>
	 *
	 * @param metadata Hibernate metadata containing entity mappings
	 * @param hibernateProperties Hibernate configuration properties
	 * @param serviceRegistry Hibernate service registry
	 */
	static void backfillAuditTables(Metadata metadata, Properties hibernateProperties,
	        ServiceRegistry serviceRegistry) {
		String auditTablePrefix = hibernateProperties.getProperty("org.hibernate.envers.audit_table_prefix", "");
		String auditTableSuffix = hibernateProperties.getProperty("org.hibernate.envers.audit_table_suffix", "_audit");
		String revFieldName = hibernateProperties.getProperty("org.hibernate.envers.revision_field_name", "REV");
		String revTypeFieldName = hibernateProperties.getProperty("org.hibernate.envers.revision_type_field_name", "REVTYPE");

		ConnectionProvider connectionProvider = serviceRegistry.getService(ConnectionProvider.class);
		Set<String> processedTables = new HashSet<>();

		try (Connection connection = connectionProvider.getConnection()) {
			boolean autoCommit = connection.getAutoCommit();
			connection.setAutoCommit(false);

			try {
				// Collect which entity tables actually need backfilling
				List<TableBackfillInfo> tablesToBackfill = new ArrayList<>();

				for (PersistentClass persistentClass : metadata.getEntityBindings()) {
					Table sourceTable = persistentClass.getTable();
					String sourceTableName = sourceTable.getName();

					// Avoid processing the same physical table twice (e.g. joined-table inheritance)
					if (!processedTables.add(sourceTableName.toLowerCase())) {
						continue;
					}

					// Skip tables that are themselves audit tables
					if (sourceTableName.toLowerCase().endsWith(auditTableSuffix.toLowerCase())) {
						continue;
					}

					String auditTableName = auditTablePrefix + sourceTableName + auditTableSuffix;

					if (!tableExists(connection, auditTableName)) {
						continue;
					}

					String idColumnName = getIdentifierColumnName(persistentClass);
					if (idColumnName == null) {
						log.debug("Could not determine identifier column for table {}, skipping backfill", sourceTableName);
						continue;
					}

					List<String> auditColumns = getAuditSourceColumns(connection, auditTableName, revFieldName, revTypeFieldName);
					if (auditColumns.isEmpty()) {
						continue;
					}

					if (hasUnauditedRecords(connection, sourceTableName, auditTableName, idColumnName)) {
						tablesToBackfill.add(new TableBackfillInfo(sourceTableName, auditTableName, idColumnName, auditColumns));
					}
				}

				if (!tablesToBackfill.isEmpty()) {
					long backfillRevId = createBackfillRevision(connection);

					for (TableBackfillInfo info : tablesToBackfill) {
						int count = backfillTable(connection, info.sourceTableName, info.auditTableName,
						    info.auditColumns, info.idColumnName, backfillRevId, revFieldName, revTypeFieldName);
						if (count > 0) {
							log.info("Backfilled {} records into audit table {}", count, info.auditTableName);
						}
					}
				} else {
					log.debug("No pre-existing records require audit table backfill.");
				}

				connection.commit();
				log.info("Audit table backfill step completed.");

			} catch (Exception e) {
				try {
					connection.rollback();
				} catch (SQLException rollbackEx) {
					log.error("Failed to rollback after backfill error", rollbackEx);
				}
				log.error("Audit table backfill failed, transaction rolled back", e);
			} finally {
				try {
					connection.setAutoCommit(autoCommit);
				} catch (SQLException ex) {
					log.warn("Could not restore autoCommit state after backfill", ex);
				}
			}

		} catch (SQLException e) {
			log.error("Failed to obtain JDBC connection for audit table backfill", e);
		}
	}

	/**
	 * Returns true if the given source table contains at least one row that has no corresponding
	 * entry in the audit table (i.e. the record has never been audited).
	 */
	static boolean hasUnauditedRecords(Connection connection, String sourceTableName,
	        String auditTableName, String idColumnName) throws SQLException {
		String sql = "SELECT COUNT(*) FROM " + sourceTableName + " s WHERE NOT EXISTS ("
		        + "SELECT 1 FROM " + auditTableName + " a WHERE a." + idColumnName + " = s." + idColumnName + ")";
		try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
			return rs.next() && rs.getLong(1) > 0;
		}
	}

	/**
	 * Inserts a synthetic "backfill" revision into the {@code revision_entity} table and returns
	 * its generated ID. The {@code changed_by} field is left NULL to indicate this is a system
	 * backfill rather than a real user action.
	 */
	static long createBackfillRevision(Connection connection) throws SQLException {
		long now = System.currentTimeMillis();
		String insertSql = "INSERT INTO " + REVISION_ENTITY_TABLE + " (REVTSTMP) VALUES (?)";
		try (PreparedStatement ps = connection.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
			ps.setLong(1, now);
			ps.executeUpdate();
			try (ResultSet rs = ps.getGeneratedKeys()) {
				if (rs.next()) {
					return rs.getLong(1);
				}
			}
		}
		throw new SQLException("Failed to create backfill revision — no generated key returned");
	}

	/**
	 * Inserts rows from {@code sourceTableName} into {@code auditTableName} for every source row
	 * that does not yet have an audit entry. All inserted rows are stamped with the given revision
	 * ID and REVTYPE 0 (ADD).
	 *
	 * @return the number of rows inserted
	 */
	static int backfillTable(Connection connection, String sourceTableName, String auditTableName,
	        List<String> auditColumns, String idColumnName, long revId, String revFieldName,
	        String revTypeFieldName) throws SQLException {

		String sourceColumnList = auditColumns.stream().map(c -> "s." + c).collect(Collectors.joining(", "));
		String targetColumnList = String.join(", ", auditColumns);

		String sql = "INSERT INTO " + auditTableName
		        + " (" + targetColumnList + ", " + revFieldName + ", " + revTypeFieldName + ") "
		        + "SELECT " + sourceColumnList + ", " + revId + ", 0 "
		        + "FROM " + sourceTableName + " s "
		        + "WHERE NOT EXISTS ("
		        + "  SELECT 1 FROM " + auditTableName + " a WHERE a." + idColumnName + " = s." + idColumnName
		        + ")";

		try (Statement stmt = connection.createStatement()) {
			return stmt.executeUpdate(sql);
		}
	}

	/**
	 * Returns the names of columns in the given audit table that should be copied from the source
	 * table (i.e. all columns except {@code REV}, {@code REVTYPE}, {@code REVEND}, and
	 * {@code REVEND_TSTMP}).
	 */
	static List<String> getAuditSourceColumns(Connection connection, String auditTableName,
	        String revFieldName, String revTypeFieldName) throws SQLException {
		List<String> columns = new ArrayList<>();
		String resolvedName = resolveTableName(connection, auditTableName);
		if (resolvedName == null) {
			return columns;
		}
		DatabaseMetaData dbMeta = connection.getMetaData();
		try (ResultSet rs = dbMeta.getColumns(null, null, resolvedName, null)) {
			while (rs.next()) {
				String columnName = rs.getString("COLUMN_NAME");
				if (!columnName.equalsIgnoreCase(revFieldName)
				        && !columnName.equalsIgnoreCase(revTypeFieldName)
				        && !columnName.equalsIgnoreCase("REVEND")
				        && !columnName.equalsIgnoreCase("REVEND_TSTMP")) {
					columns.add(columnName);
				}
			}
		}
		return columns;
	}

	/**
	 * Returns the name of the primary-key column for the given {@link PersistentClass}, or
	 * {@code null} if it cannot be determined (e.g. composite keys).
	 */
	static String getIdentifierColumnName(PersistentClass persistentClass) {
		try {
			KeyValue identifier = persistentClass.getIdentifier();
			if (identifier instanceof SimpleValue) {
				Iterator<Selectable> it = ((SimpleValue) identifier).getSelectables().iterator();
				if (it.hasNext()) {
					Selectable selectable = it.next();
					if (selectable instanceof Column) {
						return ((Column) selectable).getName();
					}
				}
			}
		} catch (Exception e) {
			log.debug("Could not determine identifier column for {}: {}", persistentClass.getEntityName(),
			    e.getMessage());
		}
		return null;
	}

	/**
	 * Returns true if a table with the given name exists in the database, checking case variants to
	 * handle both case-sensitive (H2) and case-insensitive (MySQL) databases.
	 */
	static boolean tableExists(Connection connection, String tableName) throws SQLException {
		return resolveTableName(connection, tableName) != null;
	}

	/**
	 * Returns the actual table name as stored in the database metadata (accounting for case
	 * differences), or {@code null} if the table does not exist.
	 */
	private static String resolveTableName(Connection connection, String tableName) throws SQLException {
		DatabaseMetaData dbMeta = connection.getMetaData();
		for (String candidate : new String[] { tableName, tableName.toLowerCase(), tableName.toUpperCase() }) {
			try (ResultSet rs = dbMeta.getTables(null, null, candidate, new String[] { "TABLE" })) {
				if (rs.next()) {
					return candidate;
				}
			}
		}
		return null;
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

	/**
	 * Holds the information needed to backfill a single source/audit table pair.
	 */
	private static class TableBackfillInfo {

		final String sourceTableName;

		final String auditTableName;

		final String idColumnName;

		final List<String> auditColumns;

		TableBackfillInfo(String sourceTableName, String auditTableName, String idColumnName,
		        List<String> auditColumns) {
			this.sourceTableName = sourceTableName;
			this.auditTableName = auditTableName;
			this.idColumnName = idColumnName;
			this.auditColumns = auditColumns;
		}
	}
}
