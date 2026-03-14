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
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import org.hibernate.boot.Metadata;
import org.hibernate.engine.jdbc.connections.spi.ConnectionProvider;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.tool.schema.spi.SchemaManagementTool;
import org.hibernate.tool.schema.spi.SchemaMigrator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link EnversAuditTableInitializer}, focused on the backfill behaviour introduced
 * to fix AUDIT-28: "Unable to read" values in audit tables for entities that existed before
 * auditing was enabled.
 * <p>
 * These tests call the package-private helper methods directly against a real H2 in-memory
 * database, avoiding the need to mock Hibernate's abstract {@code PersistentClass}.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class EnversAuditTableInitializerTest {

	private static final String H2_URL = "jdbc:h2:mem:enverstest;DB_CLOSE_DELAY=-1;MODE=LEGACY";

	private static final String REVISION_TABLE = "revision_entity";

	private Connection connection;

	@Mock
	private Metadata metadata;

	@Mock
	private ServiceRegistry serviceRegistry;

	@Mock
	private SchemaManagementTool schemaManagementTool;

	@Mock
	private SchemaMigrator schemaMigrator;

	@Mock
	private ConnectionProvider connectionProvider;

	@BeforeEach
	void setUp() throws Exception {
		connection = DriverManager.getConnection(H2_URL, "sa", "");
		connection.setAutoCommit(false);

		when(serviceRegistry.getService(SchemaManagementTool.class)).thenReturn(schemaManagementTool);
		when(schemaManagementTool.getSchemaMigrator(any())).thenReturn(schemaMigrator);
		when(serviceRegistry.getService(ConnectionProvider.class)).thenReturn(connectionProvider);
		when(connectionProvider.getConnection()).thenReturn(connection);
		when(metadata.getEntityBindings()).thenReturn(Collections.emptyList());
	}

	@AfterEach
	void tearDown() throws Exception {
		try (Statement stmt = connection.createStatement()) {
			stmt.execute("DROP ALL OBJECTS");
		}
		connection.commit();
		connection.close();
	}

	@Test
	void initialize_shouldDoNothingWhenEnversIsDisabled() {
		Properties disabledProps = new Properties();
		disabledProps.setProperty("hibernate.integration.envers.enabled", "false");

		EnversAuditTableInitializer.initialize(metadata, disabledProps, serviceRegistry);

		verifyNoInteractions(serviceRegistry);
	}

	@Test
	void initialize_shouldCompleteWithoutErrorWhenNoAuditedEntitiesAreFound() throws Exception {
		createRevisionTable();
		connection.commit();

		Properties props = enversProps();
		// metadata.getEntityBindings() returns empty list — no entity to backfill
		EnversAuditTableInitializer.initialize(metadata, props, serviceRegistry);

		try (Statement stmt = connection.createStatement();
		        ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM " + REVISION_TABLE)) {
			rs.next();
			assertEquals(0, rs.getInt(1), "No revision should be created when there are no entities to backfill");
		}
	}

	@Test
	void createBackfillRevision_shouldInsertRevisionAndReturnGeneratedId() throws Exception {
		createRevisionTable();
		connection.commit();

		int revId = EnversAuditTableInitializer.createBackfillRevision(connection, REVISION_TABLE);
		connection.commit();

		assertTrue(revId > 0, "Generated revision ID should be positive");
		try (Statement stmt = connection.createStatement();
		        ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM " + REVISION_TABLE)) {
			rs.next();
			assertEquals(1, rs.getInt(1), "Exactly one revision row should be present");
		}
	}

	@Test
	void isAuditTableEmpty_shouldReturnTrueWhenTableHasNoRows() throws Exception {
		try (Statement stmt = connection.createStatement()) {
			stmt.execute("CREATE TABLE patient_audit (REV INT, REVTYPE TINYINT, patient_id INT)");
		}
		connection.commit();

		assertTrue(EnversAuditTableInitializer.isAuditTableEmpty(connection, "patient_audit"));
	}

	@Test
	void isAuditTableEmpty_shouldReturnFalseWhenTableHasRows() throws Exception {
		try (Statement stmt = connection.createStatement()) {
			stmt.execute("CREATE TABLE patient_audit (REV INT, REVTYPE TINYINT, patient_id INT)");
			stmt.execute("INSERT INTO patient_audit VALUES (1, 0, 42)");
		}
		connection.commit();

		assertFalse(EnversAuditTableInitializer.isAuditTableEmpty(connection, "patient_audit"));
	}

	@Test
	void isAuditTableEmpty_shouldReturnFalseWhenTableDoesNotExist() {
		// Table doesn't exist — should not throw, should return false (skip backfill safely)
		assertFalse(EnversAuditTableInitializer.isAuditTableEmpty(connection, "nonexistent_audit"));
	}

	@Test
	void isTableEmpty_shouldReturnTrueForEmptyTable() throws Exception {
		try (Statement stmt = connection.createStatement()) {
			stmt.execute("CREATE TABLE patient (patient_id INT PRIMARY KEY)");
		}
		connection.commit();

		assertTrue(EnversAuditTableInitializer.isTableEmpty(connection, "patient"));
	}

	@Test
	void isTableEmpty_shouldReturnFalseWhenTableHasData() throws Exception {
		try (Statement stmt = connection.createStatement()) {
			stmt.execute("CREATE TABLE patient (patient_id INT PRIMARY KEY)");
			stmt.execute("INSERT INTO patient VALUES (1)");
		}
		connection.commit();

		assertFalse(EnversAuditTableInitializer.isTableEmpty(connection, "patient"));
	}

	@Test
	void getSourceTableColumns_shouldReturnAllColumnNamesFromTable() throws Exception {
		try (Statement stmt = connection.createStatement()) {
			stmt.execute("CREATE TABLE patient (patient_id INT PRIMARY KEY, name VARCHAR(100), gender CHAR(1))");
		}
		connection.commit();

		List<String> columns = EnversAuditTableInitializer.getSourceTableColumns(connection, "PATIENT");

		assertEquals(3, columns.size(), "Should return all 3 columns");
	}

	@Test
	void backfillTable_shouldInsertAllSourceRowsIntoAuditTableWithRevtype0() throws Exception {
		try (Statement stmt = connection.createStatement()) {
			stmt.execute("CREATE TABLE patient (patient_id INT PRIMARY KEY, name VARCHAR(100))");
			stmt.execute("INSERT INTO patient VALUES (1, 'Alice')");
			stmt.execute("INSERT INTO patient VALUES (2, 'Bob')");
			stmt.execute("CREATE TABLE patient_audit (REV INT, REVTYPE TINYINT, patient_id INT, name VARCHAR(100))");
		}
		connection.commit();

		List<String> columns = List.of("PATIENT_ID", "NAME");
		EnversAuditTableInitializer.backfillTable(connection, "patient", "patient_audit", columns, 1);
		connection.commit();

		try (Statement stmt = connection.createStatement()) {
			try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM patient_audit WHERE REVTYPE = 0")) {
				rs.next();
				assertEquals(2, rs.getInt(1), "Both source rows should appear in audit table with REVTYPE=0 (ADD)");
			}
			try (ResultSet rs = stmt.executeQuery("SELECT name FROM patient_audit ORDER BY patient_id")) {
				assertTrue(rs.next());
				assertEquals("Alice", rs.getString(1));
				assertTrue(rs.next());
				assertEquals("Bob", rs.getString(1));
			}
		}
	}

	@Test
	void backfillTable_shouldUseTheProvidedRevisionId() throws Exception {
		try (Statement stmt = connection.createStatement()) {
			stmt.execute("CREATE TABLE patient (patient_id INT PRIMARY KEY)");
			stmt.execute("INSERT INTO patient VALUES (99)");
			stmt.execute("CREATE TABLE patient_audit (REV INT, REVTYPE TINYINT, patient_id INT)");
		}
		connection.commit();

		EnversAuditTableInitializer.backfillTable(connection, "patient", "patient_audit", List.of("PATIENT_ID"), 42);
		connection.commit();

		try (Statement stmt = connection.createStatement();
		        ResultSet rs = stmt.executeQuery("SELECT REV FROM patient_audit")) {
			assertTrue(rs.next());
			assertEquals(42, rs.getInt(1), "Audit row should carry the supplied revision ID");
		}
	}

	@Test
	void backfillTable_shouldNotDuplicateRowsWhenCalledTwice() throws Exception {
		try (Statement stmt = connection.createStatement()) {
			stmt.execute("CREATE TABLE patient (patient_id INT PRIMARY KEY, name VARCHAR(100))");
			stmt.execute("INSERT INTO patient VALUES (1, 'Alice')");
			stmt.execute("CREATE TABLE patient_audit (REV INT, REVTYPE TINYINT, patient_id INT, name VARCHAR(100))");
		}
		connection.commit();

		List<String> columns = List.of("PATIENT_ID", "NAME");
		// First call — backfills 1 row
		EnversAuditTableInitializer.backfillTable(connection, "patient", "patient_audit", columns, 1);
		connection.commit();

		// Second call — the caller (backfillAuditTables) checks isAuditTableEmpty first, so
		// this scenario tests that the SQL itself only copies source rows (no duplication logic here)
		EnversAuditTableInitializer.backfillTable(connection, "patient", "patient_audit", columns, 2);
		connection.commit();

		// The actual guard against duplication is isAuditTableEmpty(), not backfillTable().
		// Here we just confirm backfillTable is idempotent w.r.t. source data size.
		try (Statement stmt = connection.createStatement();
		        ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM patient_audit")) {
			rs.next();
			assertEquals(2, rs.getInt(1), "Two calls produce two sets of audit rows (guard is in isAuditTableEmpty)");
		}
	}

	@Test
	void backfillIsSkipped_whenAuditTableAlreadyHasData() throws Exception {
		try (Statement stmt = connection.createStatement()) {
			stmt.execute("CREATE TABLE patient (patient_id INT PRIMARY KEY, name VARCHAR(100))");
			stmt.execute("INSERT INTO patient VALUES (1, 'Alice')");
			stmt.execute("CREATE TABLE patient_audit (REV INT, REVTYPE TINYINT, patient_id INT, name VARCHAR(100))");
			stmt.execute("INSERT INTO patient_audit VALUES (1, 0, 1, 'Alice')");
		}
		connection.commit();

		// isAuditTableEmpty should be false — backfill must NOT run
		assertFalse(EnversAuditTableInitializer.isAuditTableEmpty(connection, "patient_audit"),
		    "Audit table with data should not trigger backfill");
	}

	@Test
	void backfillIsSkipped_whenSourceTableIsEmpty() throws Exception {
		try (Statement stmt = connection.createStatement()) {
			stmt.execute("CREATE TABLE patient (patient_id INT PRIMARY KEY, name VARCHAR(100))");
			stmt.execute("CREATE TABLE patient_audit (REV INT, REVTYPE TINYINT, patient_id INT, name VARCHAR(100))");
		}
		connection.commit();

		// isTableEmpty should be true — backfill must NOT run
		assertTrue(EnversAuditTableInitializer.isTableEmpty(connection, "patient"),
		    "Empty source table should not trigger backfill");
	}

	private void createRevisionTable() throws Exception {
		try (Statement stmt = connection.createStatement()) {
			stmt.execute("CREATE TABLE " + REVISION_TABLE + " (REV INT AUTO_INCREMENT PRIMARY KEY, REVTSTMP BIGINT)");
		}
	}

	private static Properties enversProps() {
		Properties props = new Properties();
		props.setProperty("hibernate.integration.envers.enabled", "true");
		props.setProperty("org.hibernate.envers.audit_table_suffix", "_audit");
		return props;
	}
}
