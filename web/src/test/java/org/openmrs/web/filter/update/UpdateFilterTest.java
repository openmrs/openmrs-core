/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web.filter.update;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import javax.servlet.ServletException;
import java.sql.Connection;
import java.sql.SQLException;

import org.junit.jupiter.api.Test;
import org.openmrs.api.context.Context;
import org.openmrs.web.test.BaseWebContextSensitiveTest;

/**
 * Tests some of the methods on the {@link UpdateFilter}
 */
public class UpdateFilterTest extends BaseWebContextSensitiveTest {
	
	/**
	 * @throws ServletException
	 * @see UpdateFilter#authenticateAsSuperUser(String,String)
	 */
	@Test
	public void authenticateAsSuperUser_shouldReturnFalseIfGivenInvalidCredentials() throws ServletException {
		assertFalse(new UpdateFilter().authenticateAsSuperUser("a-bad-username", "a-bad-password"));
	}
	
	/**
	 * @throws ServletException
	 * @see UpdateFilter#authenticateAsSuperUser(String,String)
	 */
	@Test
	public void authenticateAsSuperUser_shouldReturnFalseIfGivenUserIsNotSuperuser() throws ServletException {
		// can switch to using "butch" in standardDataSet once we know bruno's password
		executeDataSet("org/openmrs/api/include/UserServiceTest.xml");
		Context.authenticate("userWithSha512Hash", "test"); // sanity check
		Context.logout();
		
		assertFalse(new UpdateFilter().authenticateAsSuperUser("userWithSha512Hash", "test"));
	}
	
	/**
	 * @throws ServletException
	 * @see UpdateFilter#authenticateAsSuperUser(String,String)
	 */
	@Test
	public void authenticateAsSuperUser_shouldReturnTrueIfGivenUserIsSuperuser() throws ServletException {
		assertTrue(new UpdateFilter().authenticateAsSuperUser("admin", "test"));
	}
	
	/**
	 * @throws SQLException
	 * @throws Exception
	 * @see UpdateFilter#isSuperUser(Connection,Integer)
	 */
	@Test
	public void isSuperUser_shouldReturnTrueIfGivenUserHasSuperuserRole() throws SQLException {
		assertTrue(new UpdateFilter().isSuperUser(getConnection(), 1));
	}
	
	/**
	 * @throws SQLException
	 * @throws Exception
	 * @see UpdateFilter#isSuperUser(Connection,Integer)
	 */
	@Test
	public void isSuperUser_shouldReturnFalseIfGivenUserDoesNotHaveTheSuperUserRole() throws SQLException {
		assertFalse(new UpdateFilter().isSuperUser(getConnection(), 502));
	}
	
}
