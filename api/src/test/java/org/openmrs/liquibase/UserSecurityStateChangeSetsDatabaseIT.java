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
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.openmrs.util.DatabaseIT;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Covers the Liquibase changesets that create the user_security_state table and move the
 * loginAttempts and lockoutTimestamp user properties into it: a fresh install, an upgrade with
 * existing values, and an upgrade from a maintenance branch that already carries the same
 * changesets under another file name.
 */
public class UserSecurityStateChangeSetsDatabaseIT extends DatabaseIT {

	private static final String SNAPSHOT_VERSION = "2.9.x";

	private static final String UPDATE_TO_LATEST_3_0_X = "org/openmrs/liquibase/updates/liquibase-update-to-latest-3.0.x.xml";

	/**
	 * The same two changesets under a different file name, standing in for a patched maintenance branch
	 * that ships them in its own liquibase-update-to-latest file.
	 */
	private static final String PATCHED_MAINTENANCE_BRANCH_UPDATE = "org/openmrs/liquibase/include/patched-2.7.x-user-security-state.xml";

	private static final String CREATE_TABLE_CHANGE_SET_ID = "20261110-create-user-security-state";

	private static final String MIGRATE_CHANGE_SET_ID = "20261110-migrate-login-attempts-to-user-security-state";

	private static final int ADMIN_USER_ID = 1;

	private static final int DAEMON_USER_ID = 2;

	@Test
	public void shouldCreateTheTableOnAFreshInstall() throws Exception {
		updateDatabase(new ChangeLogVersionFinder().getSnapshotCombinations().get(SNAPSHOT_VERSION));

		updateDatabase(UPDATE_TO_LATEST_3_0_X);

		assertEquals(List.of("user_security_state_id", "uuid", "user_id", "property", "property_value", "creator",
		    "date_created", "changed_by", "date_changed"), columnsOfUserSecurityState());
	}

	/**
	 * The core data snapshot seeds the admin user with {@code loginAttempts} 0 and an empty
	 * {@code lockoutTimestamp}; a fresh install must not turn those into security state.
	 */
	@Test
	public void shouldNotMigrateEmptyOrZeroValuesOnAFreshInstall() throws Exception {
		updateDatabase(new ChangeLogVersionFinder().getSnapshotCombinations().get(SNAPSHOT_VERSION));

		updateDatabase(UPDATE_TO_LATEST_3_0_X);

		assertEquals(List.of(), userSecurityState());
	}

	@Test
	public void shouldMigrateLoginAttemptsAndLockoutTimestampUserProperties() throws Exception {
		updateDatabase(new ChangeLogVersionFinder().getSnapshotCombinations().get(SNAPSHOT_VERSION));
		setUserProperty(ADMIN_USER_ID, "loginAttempts", "3");
		setUserProperty(ADMIN_USER_ID, "lockoutTimestamp", "1700000000000");
		setUserProperty(DAEMON_USER_ID, "loginAttempts", "2");

		updateDatabase(UPDATE_TO_LATEST_3_0_X);

		assertEquals(List.of(ADMIN_USER_ID + ":core.lockoutTimestamp=1700000000000", ADMIN_USER_ID + ":core.loginAttempts=3",
		    DAEMON_USER_ID + ":core.loginAttempts=2"), userSecurityState());
	}

	@Test
	public void shouldRemoveTheMigratedUserProperties() throws Exception {
		updateDatabase(new ChangeLogVersionFinder().getSnapshotCombinations().get(SNAPSHOT_VERSION));
		setUserProperty(ADMIN_USER_ID, "loginAttempts", "3");
		setUserProperty(ADMIN_USER_ID, "lockoutTimestamp", "1700000000000");

		updateDatabase(UPDATE_TO_LATEST_3_0_X);

		assertEquals(List.of(), lockoutUserProperties());
	}

	@Test
	public void shouldKeepTheMigratedValuesWhenUpgradingFromAPatchedMaintenanceBranch() throws Exception {
		updateDatabase(new ChangeLogVersionFinder().getSnapshotCombinations().get(SNAPSHOT_VERSION));
		setUserProperty(ADMIN_USER_ID, "loginAttempts", "3");
		updateDatabase(PATCHED_MAINTENANCE_BRANCH_UPDATE);

		updateDatabase(UPDATE_TO_LATEST_3_0_X);

		assertEquals(List.of(ADMIN_USER_ID + ":core.loginAttempts=3"), userSecurityState());
	}

	@Test
	public void shouldMarkTheChangeSetsAsRanWhenUpgradingFromAPatchedMaintenanceBranch() throws Exception {
		updateDatabase(new ChangeLogVersionFinder().getSnapshotCombinations().get(SNAPSHOT_VERSION));
		setUserProperty(ADMIN_USER_ID, "loginAttempts", "3");
		updateDatabase(PATCHED_MAINTENANCE_BRANCH_UPDATE);

		updateDatabase(UPDATE_TO_LATEST_3_0_X);

		assertEquals(List.of(CREATE_TABLE_CHANGE_SET_ID + ":MARK_RAN", MIGRATE_CHANGE_SET_ID + ":MARK_RAN"),
		    executionTypesInFile(UPDATE_TO_LATEST_3_0_X));
	}

	private void setUserProperty(int userId, String property, String value) throws SQLException {
		try (Connection connection = getConnection();
		        PreparedStatement delete = connection
		                .prepareStatement("delete from user_property where user_id = ? and property = ?");
		        PreparedStatement insert = connection.prepareStatement(
		            "insert into user_property (user_id, property, property_value) values (?, ?, ?)")) {
			delete.setInt(1, userId);
			delete.setString(2, property);
			delete.executeUpdate();
			insert.setInt(1, userId);
			insert.setString(2, property);
			insert.setString(3, value);
			insert.executeUpdate();
			connection.commit();
		}
	}

	private List<String> columnsOfUserSecurityState() throws SQLException {
		List<String> columns = new ArrayList<>();
		try (Connection connection = getConnection()) {
			DatabaseMetaData metaData = connection.getMetaData();
			try (ResultSet resultSet = metaData.getColumns(null, null, "%", "%")) {
				while (resultSet.next()) {
					if ("user_security_state".equalsIgnoreCase(resultSet.getString("TABLE_NAME"))) {
						columns.add(resultSet.getString("COLUMN_NAME").toLowerCase());
					}
				}
			}
		}
		return columns;
	}

	private List<String> userSecurityState() throws SQLException {
		return select("select user_id, property, property_value from user_security_state order by user_id, property");
	}

	private List<String> lockoutUserProperties() throws SQLException {
		return select("select user_id, property, property_value from user_property "
		        + "where property in ('loginAttempts', 'lockoutTimestamp') order by user_id, property");
	}

	private List<String> executionTypesInFile(String filename) throws SQLException {
		List<String> executionTypes = new ArrayList<>();
		try (Connection connection = getConnection();
		        PreparedStatement statement = connection.prepareStatement(
		            "select id, exectype from liquibasechangelog where filename = ? and id in (?, ?) order by id")) {
			statement.setString(1, filename);
			statement.setString(2, CREATE_TABLE_CHANGE_SET_ID);
			statement.setString(3, MIGRATE_CHANGE_SET_ID);
			try (ResultSet resultSet = statement.executeQuery()) {
				while (resultSet.next()) {
					executionTypes.add(resultSet.getString(1) + ":" + resultSet.getString(2));
				}
			}
		}
		return executionTypes;
	}

	private List<String> select(String sql) throws SQLException {
		List<String> rows = new ArrayList<>();
		try (Connection connection = getConnection();
		        PreparedStatement statement = connection.prepareStatement(sql);
		        ResultSet resultSet = statement.executeQuery()) {
			while (resultSet.next()) {
				rows.add(resultSet.getInt(1) + ":" + resultSet.getString(2) + "=" + resultSet.getString(3));
			}
		}
		return rows;
	}
}
