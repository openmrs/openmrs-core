/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.liquibase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.util.DatabaseIT;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Every liquibase-update-to-latest file since 2.4.x carries its own copy of the PostgreSQL change
 * set that creates the UUID() alias. Liquibase tells the copies apart by file name, so upgrading
 * runs each copy the database has not seen yet, against a database where an earlier copy already
 * created the function.
 */
public class UuidFunctionChangeSetsDatabaseIT extends DatabaseIT {

	private static final String SNAPSHOT_VERSION = "2.8.x";

	private static final String UPDATE_TO_LATEST_2_9_X = "org/openmrs/liquibase/updates/liquibase-update-to-latest-2.9.x.xml";

	private static final String UPDATE_TO_LATEST_3_0_X = "org/openmrs/liquibase/updates/liquibase-update-to-latest-3.0.x.xml";

	private static final String LAST_3_0_X_CHANGE_SET_ID = "20261110-migrate-login-attempts-to-user-security-state";

	/**
	 * Containers.ensureDatabaseRunning() creates the latest schema before the first test, and
	 * dropAllDatabaseObjects() drops tables but not functions, so start from an empty database.
	 */
	@BeforeEach
	public void startFromAnEmptyDatabase() throws SQLException {
		dropAllDatabaseObjects();
		if ("postgres".equals(System.getProperty("database"))) {
			try (Connection connection = getConnection(); Statement statement = connection.createStatement()) {
				statement.execute("drop function if exists uuid()");
				connection.commit();
			}
		}
	}

	/**
	 * A 2.9.x install is the 2.8.x snapshot plus the 2.9.x update file; upgrading it to 3.0.x runs the
	 * 3.0.x update file on top.
	 */
	@Test
	public void shouldUpgradeA2_9_xInstallTo3_0_x() throws Exception {
		updateDatabase(new ChangeLogVersionFinder().getSnapshotCombinations().get(SNAPSHOT_VERSION));
		updateDatabase(UPDATE_TO_LATEST_2_9_X);

		updateDatabase(UPDATE_TO_LATEST_3_0_X);

		assertEquals(1, countChangeSetsInFile(UPDATE_TO_LATEST_3_0_X, LAST_3_0_X_CHANGE_SET_ID));
	}

	private int countChangeSetsInFile(String filename, String id) throws SQLException {
		try (Connection connection = getConnection();
		        PreparedStatement statement = connection
		                .prepareStatement("select count(*) from liquibasechangelog where filename = ? and id = ?")) {
			statement.setString(1, filename);
			statement.setString(2, id);
			try (ResultSet resultSet = statement.executeQuery()) {
				resultSet.next();
				return resultSet.getInt(1);
			}
		}
	}
}
