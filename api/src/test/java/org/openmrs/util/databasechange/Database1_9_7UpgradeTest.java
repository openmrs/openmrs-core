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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.util.DatabaseUtil;

/**
 * Tests database upgrade from OpenMRS 1.9.7
 */
public class Database1_9_7UpgradeTest {
	
	public final static String databasePath = "/org/openmrs/util/databasechange/openmrs-1.9.7.h2.db";
	
	private DatabaseUpgradeTestUtil upgradeTestUtil;
	
	@Before
	public void before() throws IOException, SQLException {
		upgradeTestUtil = new DatabaseUpgradeTestUtil(databasePath);
	}
	
	@After
	public void after() throws SQLException {
		upgradeTestUtil.close();
	}
	
	@Test
	public void shouldUpgradeFromClean1_9() throws IOException, SQLException {
		upgradeTestUtil.upgrade();
		
		//Test if the generated schema corresponds to Hibernate mappings
		upgradeTestUtil.buildSessionFactory();
	}
	
	@Test
	public void shouldAddTheNecessaryPrivilegesAndAssignThemToSpecificRoles() throws Exception {
		final String VIEW_ENCOUNTERS = "View Encounters";
		final String ADD_VISITS = "Add Visits";
		final String ADD_ENCOUNTERS = "Add Encounters";
		final String EDIT_ENCOUNTERS = "Edit Encounters";
		final String VIEW_VISITS = "View Visits";
		final String VIEW_PROVIDERS = "View Providers";
		final String PROVIDER_ROLE = "Provider";
		final String AUTHENTICATED_ROLE = "Authenticated";
		Connection connection = upgradeTestUtil.getConnection();
		//Assign some privileges to some roles for testing purposes
		DatabaseUtil.executeSQL(connection, "insert into role_privilege (role,privilege) values ('" + PROVIDER_ROLE + "', '"
		        + VIEW_ENCOUNTERS + "'), ('" + PROVIDER_ROLE + "', '" + EDIT_ENCOUNTERS + "'), ('" + AUTHENTICATED_ROLE
		        + "', '" + ADD_ENCOUNTERS + "')", false);
		connection.commit();
		assertTrue(roleHasPrivilege(PROVIDER_ROLE, VIEW_ENCOUNTERS));
		assertTrue(roleHasPrivilege(PROVIDER_ROLE, EDIT_ENCOUNTERS));
		assertFalse(roleHasPrivilege(PROVIDER_ROLE, VIEW_VISITS));
		assertFalse(roleHasPrivilege(PROVIDER_ROLE, VIEW_PROVIDERS));
		assertFalse(roleHasPrivilege(PROVIDER_ROLE, ADD_VISITS));
		assertTrue(roleHasPrivilege(AUTHENTICATED_ROLE, ADD_ENCOUNTERS));
		
		upgradeTestUtil.upgrade();
		assertTrue(roleHasPrivilege(PROVIDER_ROLE, VIEW_VISITS));
		assertTrue(roleHasPrivilege(PROVIDER_ROLE, VIEW_PROVIDERS));
		assertTrue(roleHasPrivilege(PROVIDER_ROLE, ADD_VISITS));
		assertTrue(roleHasPrivilege(AUTHENTICATED_ROLE, ADD_VISITS));
	}
	
	private boolean roleHasPrivilege(String role, String privilege) {
		final String query = "select * from role_privilege where role='" + role + "' and privilege ='" + privilege + "'";
		return DatabaseUtil.executeSQL(upgradeTestUtil.getConnection(), query, true).size() == 1;
	}
}
