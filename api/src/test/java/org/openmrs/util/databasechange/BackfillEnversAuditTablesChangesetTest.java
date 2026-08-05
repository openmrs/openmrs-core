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
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link BackfillEnversAuditTablesChangeset}, focused on the backfill behaviour
 * introduced to fix AUDIT-28: "Unable to read" values in audit tables for entities that existed
 * before auditing was enabled.
 * <p>
 * These tests call the package-private helper methods directly against a real H2 in-memory
 * database.
 */
class BackfillEnversAuditTablesChangesetTest {

	private static final String H2_URL = "jdbc:h2:mem:enversbackfilltest;DB_CLOSE_DELAY=-1;MODE=LEGACY";

	private static final String REVISION_TABLE = "revision_entity";

	private Connection connection;

	@BeforeEach
	void setUp() throws Exception {
		connection = DriverManager.getConnection(H2_URL, "sa", "");
		connection.setAutoCommit(false);
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
	void createBackfillRevision_shouldInsertRevisionAndReturnGeneratedId() throws Exception {
		createRevisionTable();
		connection.commit();

		int revId = BackfillEnversAuditTablesChangeset.createBackfillRevision(connection, REVISION_TABLE);
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

		assertTrue(BackfillEnversAuditTablesChangeset.isAuditTableEmpty(connection, "patient_audit"));
	}

	@Test
	void isAuditTableEmpty_shouldReturnFalseWhenTableHasRows() throws Exception {
		try (Statement stmt = connection.createStatement()) {
			stmt.execute("CREATE TABLE patient_audit (REV INT, REVTYPE TINYINT, patient_id INT)");
			stmt.execute("INSERT INTO patient_audit VALUES (1, 0, 42)");
		}
		connection.commit();

		assertFalse(BackfillEnversAuditTablesChangeset.isAuditTableEmpty(connection, "patient_audit"));
	}

	@Test
	void isAuditTableEmpty_shouldThrowWhenTableDoesNotExist() {
		// Table doesn't exist — should now throw SQLException so the migration fails fast
		// rather than silently skipping and leaving the database in an inconsistent state
		org.junit.jupiter.api.Assertions.assertThrows(java.sql.SQLException.class,
		    () -> BackfillEnversAuditTablesChangeset.isAuditTableEmpty(connection, "nonexistent_audit"));
	}

	@Test
	void isTableEmpty_shouldReturnTrueForEmptyTable() throws Exception {
		try (Statement stmt = connection.createStatement()) {
			stmt.execute("CREATE TABLE patient (patient_id INT PRIMARY KEY)");
		}
		connection.commit();

		assertTrue(BackfillEnversAuditTablesChangeset.isTableEmpty(connection, "patient"));
	}

	@Test
	void isTableEmpty_shouldReturnFalseWhenTableHasData() throws Exception {
		try (Statement stmt = connection.createStatement()) {
			stmt.execute("CREATE TABLE patient (patient_id INT PRIMARY KEY)");
			stmt.execute("INSERT INTO patient VALUES (1)");
		}
		connection.commit();

		assertFalse(BackfillEnversAuditTablesChangeset.isTableEmpty(connection, "patient"));
	}

	@Test
	void getAuditTableDataColumns_shouldReturnColumnsExcludingRevAndRevtype() throws Exception {
		try (Statement stmt = connection.createStatement()) {
			stmt.execute("CREATE TABLE patient_audit (REV INT, REVTYPE TINYINT, patient_id INT, name VARCHAR(100))");
		}
		connection.commit();

		List<String> columns = BackfillEnversAuditTablesChangeset.getAuditTableDataColumns(connection, "PATIENT_AUDIT");

		assertEquals(2, columns.size(), "Should return only non-Envers columns");
		assertFalse(columns.stream().anyMatch(c -> c.equalsIgnoreCase("REV")), "REV should be excluded");
		assertFalse(columns.stream().anyMatch(c -> c.equalsIgnoreCase("REVTYPE")), "REVTYPE should be excluded");
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
		BackfillEnversAuditTablesChangeset.backfillTable(connection, "patient", "patient_audit", columns, 1);
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

		BackfillEnversAuditTablesChangeset.backfillTable(connection, "patient", "patient_audit", List.of("PATIENT_ID"), 42);
		connection.commit();

		try (Statement stmt = connection.createStatement();
		        ResultSet rs = stmt.executeQuery("SELECT REV FROM patient_audit")) {
			assertTrue(rs.next());
			assertEquals(42, rs.getInt(1), "Audit row should carry the supplied revision ID");
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

		assertFalse(BackfillEnversAuditTablesChangeset.isAuditTableEmpty(connection, "patient_audit"),
		    "Audit table with data should not trigger backfill");
	}

	@Test
	void backfillIsSkipped_whenSourceTableIsEmpty() throws Exception {
		try (Statement stmt = connection.createStatement()) {
			stmt.execute("CREATE TABLE patient (patient_id INT PRIMARY KEY, name VARCHAR(100))");
			stmt.execute("CREATE TABLE patient_audit (REV INT, REVTYPE TINYINT, patient_id INT, name VARCHAR(100))");
		}
		connection.commit();

		assertTrue(BackfillEnversAuditTablesChangeset.isTableEmpty(connection, "patient"),
		    "Empty source table should not trigger backfill");
	}

	private void createRevisionTable() throws Exception {
		try (Statement stmt = connection.createStatement()) {
			stmt.execute("CREATE TABLE " + REVISION_TABLE + " (id INT NOT NULL PRIMARY KEY, timestamp BIGINT NOT NULL)");
		}
	}
}
