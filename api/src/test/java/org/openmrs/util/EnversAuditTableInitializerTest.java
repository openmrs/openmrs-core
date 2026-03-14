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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import org.hibernate.boot.Metadata;
import org.hibernate.engine.jdbc.connections.spi.ConnectionProvider;
import org.hibernate.mapping.Column;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.mapping.SimpleValue;
import org.hibernate.mapping.Table;
import org.hibernate.service.ServiceRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for the audit-table backfill logic in {@link EnversAuditTableInitializer}.
 * <p>
 * These tests mock JDBC infrastructure (Connection, DatabaseMetaData, ResultSet, Statement) to
 * verify the SQL generation and control-flow of the backfill without requiring a real database.
 * </p>
 */
@ExtendWith(MockitoExtension.class)
public class EnversAuditTableInitializerTest {

	@Mock
	private ServiceRegistry serviceRegistry;

	@Mock
	private ConnectionProvider connectionProvider;

	@Mock
	private Connection connection;

	@Mock
	private DatabaseMetaData dbMeta;

	@Mock
	private Metadata metadata;

	private Properties hibernateProperties;

	@BeforeEach
	void setUp() throws SQLException {
		hibernateProperties = new Properties();
		hibernateProperties.setProperty("org.hibernate.envers.audit_table_prefix", "");
		hibernateProperties.setProperty("org.hibernate.envers.audit_table_suffix", "_audit");
		hibernateProperties.setProperty("org.hibernate.envers.revision_field_name", "REV");
		hibernateProperties.setProperty("org.hibernate.envers.revision_type_field_name", "REVTYPE");

		when(serviceRegistry.getService(ConnectionProvider.class)).thenReturn(connectionProvider);
		when(connectionProvider.getConnection()).thenReturn(connection);
		when(connection.getAutoCommit()).thenReturn(true);
		when(connection.getMetaData()).thenReturn(dbMeta);
	}

	// -------------------------------------------------------------------------
	// tableExists
	// -------------------------------------------------------------------------

	@Test
	void tableExists_returnsTrueWhenTableFoundExactCase() throws SQLException {
		ResultSet rs = singleRowResultSet();
		when(dbMeta.getTables(null, null, "person_audit", new String[] { "TABLE" })).thenReturn(rs);

		assertTrue(EnversAuditTableInitializer.tableExists(connection, "person_audit"));
	}

	@Test
	void tableExists_returnsTrueWhenTableFoundLowerCase() throws SQLException {
		ResultSet emptyRs = emptyResultSet();
		ResultSet hitRs = singleRowResultSet();
		when(dbMeta.getTables(null, null, "Person_Audit", new String[] { "TABLE" })).thenReturn(emptyRs);
		when(dbMeta.getTables(null, null, "person_audit", new String[] { "TABLE" })).thenReturn(hitRs);

		assertTrue(EnversAuditTableInitializer.tableExists(connection, "Person_Audit"));
	}

	@Test
	void tableExists_returnsFalseWhenTableMissing() throws SQLException {
		when(dbMeta.getTables(any(), any(), anyString(), any())).thenReturn(emptyResultSet());

		assertFalse(EnversAuditTableInitializer.tableExists(connection, "nonexistent_audit"));
	}

	// -------------------------------------------------------------------------
	// getIdentifierColumnName
	// -------------------------------------------------------------------------

	@Test
	void getIdentifierColumnName_returnsColumnNameForSimpleKey() {
		PersistentClass pc = mock(PersistentClass.class);
		SimpleValue identifier = mock(SimpleValue.class);
		Column column = mock(Column.class);

		when(pc.getIdentifier()).thenReturn(identifier);
		when(column.getName()).thenReturn("person_id");
		when(identifier.getSelectables()).thenReturn(Collections.singletonList(column));

		assertEquals("person_id", EnversAuditTableInitializer.getIdentifierColumnName(pc));
	}

	@Test
	void getIdentifierColumnName_returnsNullWhenIdentifierIsNotSimpleValue() {
		PersistentClass pc = mock(PersistentClass.class);
		when(pc.getIdentifier()).thenReturn(null);

		assertEquals(null, EnversAuditTableInitializer.getIdentifierColumnName(pc));
	}

	// -------------------------------------------------------------------------
	// getAuditSourceColumns
	// -------------------------------------------------------------------------

	@Test
	void getAuditSourceColumns_excludesRevAndRevtypeColumns() throws SQLException {
		ResultSet tableRs = singleRowResultSet();
		when(dbMeta.getTables(null, null, "person_audit", new String[] { "TABLE" })).thenReturn(tableRs);

		ResultSet colRs = columnResultSet("person_id", "gender", "REV", "REVTYPE");
		when(dbMeta.getColumns(null, null, "person_audit", null)).thenReturn(colRs);

		List<String> cols = EnversAuditTableInitializer.getAuditSourceColumns(connection, "person_audit", "REV",
		    "REVTYPE");

		assertEquals(Arrays.asList("person_id", "gender"), cols);
	}

	@Test
	void getAuditSourceColumns_excludesRevendColumns() throws SQLException {
		ResultSet tableRs = singleRowResultSet();
		when(dbMeta.getTables(null, null, "person_audit", new String[] { "TABLE" })).thenReturn(tableRs);

		ResultSet colRs = columnResultSet("person_id", "REV", "REVTYPE", "REVEND", "REVEND_TSTMP");
		when(dbMeta.getColumns(null, null, "person_audit", null)).thenReturn(colRs);

		List<String> cols = EnversAuditTableInitializer.getAuditSourceColumns(connection, "person_audit", "REV",
		    "REVTYPE");

		assertEquals(Collections.singletonList("person_id"), cols);
	}

	@Test
	void getAuditSourceColumns_returnsEmptyListWhenAuditTableDoesNotExist() throws SQLException {
		when(dbMeta.getTables(any(), any(), anyString(), any())).thenReturn(emptyResultSet());

		List<String> cols = EnversAuditTableInitializer.getAuditSourceColumns(connection, "missing_audit", "REV",
		    "REVTYPE");

		assertTrue(cols.isEmpty());
	}

	// -------------------------------------------------------------------------
	// hasUnauditedRecords
	// -------------------------------------------------------------------------

	@Test
	void hasUnauditedRecords_returnsTrueWhenCountIsNonZero() throws SQLException {
		Statement stmt = mock(Statement.class);
		ResultSet rs = mock(ResultSet.class);
		when(connection.createStatement()).thenReturn(stmt);
		when(stmt.executeQuery(anyString())).thenReturn(rs);
		when(rs.next()).thenReturn(true);
		when(rs.getLong(1)).thenReturn(5L);

		assertTrue(EnversAuditTableInitializer.hasUnauditedRecords(connection, "person", "person_audit", "person_id"));
	}

	@Test
	void hasUnauditedRecords_returnsFalseWhenCountIsZero() throws SQLException {
		Statement stmt = mock(Statement.class);
		ResultSet rs = mock(ResultSet.class);
		when(connection.createStatement()).thenReturn(stmt);
		when(stmt.executeQuery(anyString())).thenReturn(rs);
		when(rs.next()).thenReturn(true);
		when(rs.getLong(1)).thenReturn(0L);

		assertFalse(
		    EnversAuditTableInitializer.hasUnauditedRecords(connection, "person", "person_audit", "person_id"));
	}

	// -------------------------------------------------------------------------
	// createBackfillRevision
	// -------------------------------------------------------------------------

	@Test
	void createBackfillRevision_returnsGeneratedKey() throws SQLException {
		PreparedStatement ps = mock(PreparedStatement.class);
		ResultSet keyRs = mock(ResultSet.class);
		when(connection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS))).thenReturn(ps);
		when(ps.getGeneratedKeys()).thenReturn(keyRs);
		when(keyRs.next()).thenReturn(true);
		when(keyRs.getLong(1)).thenReturn(42L);

		assertEquals(42L, EnversAuditTableInitializer.createBackfillRevision(connection));
	}

	@Test
	void createBackfillRevision_throwsSQLExceptionWhenNoKeyGenerated() throws SQLException {
		PreparedStatement ps = mock(PreparedStatement.class);
		ResultSet keyRs = mock(ResultSet.class);
		when(connection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS))).thenReturn(ps);
		when(ps.getGeneratedKeys()).thenReturn(keyRs);
		when(keyRs.next()).thenReturn(false);

		assertThrows(SQLException.class, () -> EnversAuditTableInitializer.createBackfillRevision(connection));
	}

	// -------------------------------------------------------------------------
	// backfillTable
	// -------------------------------------------------------------------------

	@Test
	void backfillTable_executesInsertNotExistsSQL() throws SQLException {
		Statement stmt = mock(Statement.class);
		when(connection.createStatement()).thenReturn(stmt);
		when(stmt.executeUpdate(anyString())).thenReturn(3);

		int count = EnversAuditTableInitializer.backfillTable(connection, "person", "person_audit",
		    Arrays.asList("person_id", "gender"), "person_id", 7L, "REV", "REVTYPE");

		assertEquals(3, count);

		// Verify the SQL contains the required NOT EXISTS guard
		org.mockito.ArgumentCaptor<String> sqlCaptor = org.mockito.ArgumentCaptor.forClass(String.class);
		verify(stmt).executeUpdate(sqlCaptor.capture());
		String sql = sqlCaptor.getValue();
		assertTrue(sql.contains("INSERT INTO person_audit"), "SQL should target audit table");
		assertTrue(sql.contains("FROM person s"), "SQL should select from source table");
		assertTrue(sql.contains("NOT EXISTS"), "SQL should use NOT EXISTS guard");
		assertTrue(sql.contains("REV"), "SQL should include REV column");
		assertTrue(sql.contains("REVTYPE"), "SQL should include REVTYPE column");
	}

	// -------------------------------------------------------------------------
	// backfillAuditTables (integration of helpers)
	// -------------------------------------------------------------------------

	@Test
	void backfillAuditTables_skipsEntitiesWithNoMatchingAuditTable() throws SQLException {
		PersistentClass pc = mockPersistentClass("person", "person_id");
		when(metadata.getEntityBindings()).thenReturn(Collections.singletonList(pc));

		// audit table does not exist
		when(dbMeta.getTables(any(), any(), anyString(), any())).thenReturn(emptyResultSet());

		EnversAuditTableInitializer.backfillAuditTables(metadata, hibernateProperties, serviceRegistry);

		// No statement should be executed at all
		verify(connection, never()).createStatement();
		verify(connection).commit();
	}

	@Test
	void backfillAuditTables_skipsAuditTablesInEntityBindings() throws SQLException {
		// Simulate an entity whose table name already ends with _audit (Envers shadow entity)
		PersistentClass pc = mockPersistentClass("person_audit", "person_id");
		when(metadata.getEntityBindings()).thenReturn(Collections.singletonList(pc));

		EnversAuditTableInitializer.backfillAuditTables(metadata, hibernateProperties, serviceRegistry);

		verify(connection, never()).createStatement();
		verify(connection).commit();
	}

	// -------------------------------------------------------------------------
	// Helpers
	// -------------------------------------------------------------------------

	private static ResultSet emptyResultSet() throws SQLException {
		ResultSet rs = mock(ResultSet.class);
		when(rs.next()).thenReturn(false);
		return rs;
	}

	private static ResultSet singleRowResultSet() throws SQLException {
		ResultSet rs = mock(ResultSet.class);
		when(rs.next()).thenReturn(true, false);
		return rs;
	}

	/**
	 * Builds a ResultSet that mimics {@link DatabaseMetaData#getColumns} for the given column names.
	 */
	private static ResultSet columnResultSet(String... columnNames) throws SQLException {
		ResultSet rs = mock(ResultSet.class);
		Boolean[] nexts = new Boolean[columnNames.length + 1];
		for (int i = 0; i < columnNames.length; i++) {
			nexts[i] = true;
		}
		nexts[columnNames.length] = false;

		when(rs.next()).thenReturn(nexts[0],
		    Arrays.copyOfRange(nexts, 1, nexts.length));

		// Return each column name in sequence
		if (columnNames.length == 1) {
			when(rs.getString("COLUMN_NAME")).thenReturn(columnNames[0]);
		} else {
			String first = columnNames[0];
			String[] rest = Arrays.copyOfRange(columnNames, 1, columnNames.length);
			when(rs.getString("COLUMN_NAME")).thenReturn(first, rest);
		}
		return rs;
	}

	private static PersistentClass mockPersistentClass(String tableName, String idColumnName) {
		PersistentClass pc = mock(PersistentClass.class);
		Table table = mock(Table.class);
		when(pc.getTable()).thenReturn(table);
		when(table.getName()).thenReturn(tableName);

		SimpleValue identifier = mock(SimpleValue.class);
		Column column = mock(Column.class);
		when(pc.getIdentifier()).thenReturn(identifier);
		when(column.getName()).thenReturn(idColumnName);
		when(identifier.getSelectables()).thenReturn(Collections.singletonList(column));

		return pc;
	}
}
