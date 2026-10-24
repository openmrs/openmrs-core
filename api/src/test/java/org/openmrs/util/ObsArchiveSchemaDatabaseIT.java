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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Set;
import java.util.TreeSet;

import org.junit.jupiter.api.Test;
import org.openmrs.api.impl.ObsArchiveHelper;
import org.openmrs.liquibase.ChangeLogVersionFinder;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ObsArchiveSchemaDatabaseIT extends DatabaseIT {

	@Test
	public void shouldCarryEveryColumnOfTheirSourceTable() throws Exception {
		/*
		 * Drop all database objects before running the test as previously run tests may have left tables behind.
		 */
		this.dropAllDatabaseObjects();

		ChangeLogVersionFinder finder = new ChangeLogVersionFinder();
		updateDatabase(finder.getChangeLogCombinations().get(finder.getLatestSnapshotVersion().orElseThrow()));
		assertMirrors("OBS", "OBS_ARCHIVE", "CHANGED_BY", "DATE_CHANGED", "ARCHIVED_BY", "DATE_ARCHIVED");
		assertMirrors("OBS_REFERENCE_RANGE", "OBS_REFERENCE_RANGE_ARCHIVE", "ARCHIVED_BY", "DATE_ARCHIVED");
	}

	private void assertMirrors(String source, String archive, String... archiveOnlyColumns) throws Exception {
		Set<String> expected = columnsOf(source);
		expected.addAll(Set.of(archiveOnlyColumns));
		assertEquals(expected, columnsOf(archive),
		    archive + " must carry every column of " + source + " plus " + Set.of(archiveOnlyColumns));

		if ("OBS".equals(source)) {
			Set<String> manualColumns = new TreeSet<>();
			for (String col : ObsArchiveHelper.RESTORE_OBS_COLUMNS.split(",")) {
				manualColumns.add(col.trim().toUpperCase());
			}
			assertEquals(columnsOf(source), manualColumns,
			    "RESTORE_OBS_COLUMNS must match exactly the columns of OBS table");
		}
	}

	private Set<String> columnsOf(String table) throws Exception {
		Set<String> columns = new TreeSet<>();
		try (Connection connection = getConnection();
		        PreparedStatement statement = connection.prepareStatement(
		            "SELECT column_name FROM information_schema.columns WHERE UPPER(table_name) = ?")) {
			statement.setString(1, table);
			try (ResultSet resultSet = statement.executeQuery()) {
				while (resultSet.next()) {
					columns.add(resultSet.getString(1).toUpperCase());
				}
			}
		}
		assertEquals(false, columns.isEmpty(), "table " + table + " was not created");
		return columns;
	}
}
