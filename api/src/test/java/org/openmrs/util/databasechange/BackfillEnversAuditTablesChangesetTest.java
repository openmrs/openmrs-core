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
import java.util.Arrays;
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

	private static final String H2_URL = "jdbc:h2:mem:enversbackfilltest;DB_CLOSE_DELAY=-1";

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
	void isAuditTableEmpty_shouldReturnFalseWhenTableDoesNotExist() {
		// Table doesn't exist — should not throw, should return false (skip backfill safely)
		assertFalse(BackfillEnversAuditTablesChangeset.isAuditTableEmpty(connection, "nonexistent_audit"));
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
			stmt.execute("CREATE TABLE patient (patient_id INT PRIMARY KEY, name VARCHAR(100))");
			stmt.execute("CREATE TABLE patient_audit (REV INT, REVTYPE TINYINT, patient_id INT, name VARCHAR(100))");
		}
		connection.commit();

		List<String> columns = BackfillEnversAuditTablesChangeset.getAuditTableDataColumns(connection, "PATIENT_AUDIT",
		    "PATIENT");

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

		List<String> columns = Arrays.asList("PATIENT_ID", "NAME");
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

		BackfillEnversAuditTablesChangeset.backfillTable(connection, "patient", "patient_audit", Arrays.asList("PATIENT_ID"), 42);
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

	@Test
	void isEnversAuditTable_shouldReturnTrueForTableWithRevAndRevtype() throws Exception {
		try (Statement stmt = connection.createStatement()) {
			stmt.execute("CREATE TABLE patient_aud (REV INT, REVTYPE TINYINT, patient_id INT)");
		}
		connection.commit();

		BackfillEnversAuditTablesChangeset changeset = new BackfillEnversAuditTablesChangeset();
		assertTrue(changeset.isEnversAuditTable(connection, "patient_aud"),
		    "Table with REV and REVTYPE should be identified as an Envers audit table");
	}

	@Test
	void isEnversAuditTable_shouldReturnFalseForRegularTable() throws Exception {
		try (Statement stmt = connection.createStatement()) {
			stmt.execute("CREATE TABLE patient (patient_id INT PRIMARY KEY, name VARCHAR(100))");
		}
		connection.commit();

		BackfillEnversAuditTablesChangeset changeset = new BackfillEnversAuditTablesChangeset();
		assertFalse(changeset.isEnversAuditTable(connection, "patient"),
		    "Regular table without REV/REVTYPE should not be identified as audit table");
	}

	@Test
	void discoverAuditPairs_shouldMatchAuditTableToSourceTable() throws Exception {
		try (Statement stmt = connection.createStatement()) {
			stmt.execute("CREATE TABLE patient (patient_id INT PRIMARY KEY, name VARCHAR(100))");
			stmt.execute("CREATE TABLE patient_aud (REV INT, REVTYPE TINYINT, patient_id INT)");
		}
		connection.commit();

		BackfillEnversAuditTablesChangeset changeset = new BackfillEnversAuditTablesChangeset();
		List<String[]> pairs = changeset.discoverAuditPairs(connection);

		assertEquals(1, pairs.size(), "Should discover exactly one audit pair");
		assertEquals("patient", pairs.get(0)[0].toLowerCase(), "Source table should be patient");
		assertEquals("patient_aud", pairs.get(0)[1].toLowerCase(), "Audit table should be patient_aud");
	}

	@Test
	void discoverAuditPairs_shouldUseLongestPrefixMatch() throws Exception {
		try (Statement stmt = connection.createStatement()) {
			stmt.execute("CREATE TABLE patient (patient_id INT PRIMARY KEY)");
			stmt.execute("CREATE TABLE patient_identifier (id INT PRIMARY KEY)");
			stmt.execute("CREATE TABLE patient_identifier_aud (REV INT, REVTYPE TINYINT, id INT)");
		}
		connection.commit();

		BackfillEnversAuditTablesChangeset changeset = new BackfillEnversAuditTablesChangeset();
		List<String[]> pairs = changeset.discoverAuditPairs(connection);

		assertEquals(1, pairs.size(), "Should discover exactly one audit pair");
		assertEquals("patient_identifier", pairs.get(0)[0].toLowerCase(),
		    "Longest prefix match should pick patient_identifier over patient");
	}

	@Test
	void endToEnd_shouldBackfillAuditTablesWithCustomSuffix() throws Exception {
		// Simulate maintainer's setup: tables use _aud suffix, not _audit
		try (Statement stmt = connection.createStatement()) {
			stmt.execute("CREATE TABLE " + REVISION_TABLE + " (id INT NOT NULL PRIMARY KEY, timestamp BIGINT NOT NULL)");

			stmt.execute("CREATE TABLE patient (patient_id INT PRIMARY KEY, name VARCHAR(100))");
			stmt.execute("INSERT INTO patient VALUES (1, 'Alice'), (2, 'Bob')");

			stmt.execute("CREATE TABLE encounter (encounter_id INT PRIMARY KEY, type VARCHAR(50))");
			stmt.execute("INSERT INTO encounter VALUES (10, 'VISIT')");

			// Audit tables with _aud suffix (empty) — no suffix config needed
			stmt.execute("CREATE TABLE patient_aud (REV INT, REVTYPE TINYINT, patient_id INT, name VARCHAR(100))");
			stmt.execute("CREATE TABLE encounter_aud (REV INT, REVTYPE TINYINT, encounter_id INT, type VARCHAR(50))");
		}
		connection.commit();

		// Run full backfill flow using the same logic as execute()
		BackfillEnversAuditTablesChangeset changeset = new BackfillEnversAuditTablesChangeset();
		List<String[]> pairs = changeset.discoverAuditPairs(connection);
		assertEquals(2, pairs.size(), "Should discover patient_aud and encounter_aud");

		Integer revId = null;
		for (int pass = 0; pass < 2; pass++) {
			for (String[] pair : pairs) {
				String sourceTable = pair[0];
				String auditTable = pair[1];
				if (!BackfillEnversAuditTablesChangeset.isAuditTableEmpty(connection, auditTable)
				        || BackfillEnversAuditTablesChangeset.isTableEmpty(connection, sourceTable)) {
					continue;
				}
				if (revId == null) {
					revId = BackfillEnversAuditTablesChangeset.createBackfillRevision(connection, REVISION_TABLE);
				}
				List<String> columns = BackfillEnversAuditTablesChangeset.getAuditTableDataColumns(connection, auditTable,
				    sourceTable);
				if (!columns.isEmpty()) {
					BackfillEnversAuditTablesChangeset.backfillTable(connection, sourceTable, auditTable, columns, revId);
				}
			}
		}
		connection.commit();

		// Verify audit tables are populated
		try (Statement stmt = connection.createStatement()) {
			try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM patient_aud")) {
				rs.next();
				assertEquals(2, rs.getInt(1), "patient_aud should have 2 backfilled rows");
			}
			try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM encounter_aud")) {
				rs.next();
				assertEquals(1, rs.getInt(1), "encounter_aud should have 1 backfilled row");
			}
		}
	}

	private void createRevisionTable() throws Exception {
		try (Statement stmt = connection.createStatement()) {
			stmt.execute("CREATE TABLE " + REVISION_TABLE + " (id INT NOT NULL PRIMARY KEY, timestamp BIGINT NOT NULL)");
		}
	}
}
