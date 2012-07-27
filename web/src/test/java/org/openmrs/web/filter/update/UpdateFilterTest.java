/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.web.filter.update;

import java.sql.Connection;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.api.context.Context;
import org.openmrs.test.Verifies;
import org.openmrs.web.test.BaseWebContextSensitiveTest;

/**
 * Tests some of the methods on the {@link UpdateFilter}
 */
public class UpdateFilterTest extends BaseWebContextSensitiveTest {
	
	/**
	 * @see {@link UpdateFilter#authenticateAsSuperUser(String,String)}
	 */
	@Test
	@Verifies(value = "should return false if given invalid credentials", method = "authenticateAsSuperUser(String,String)")
	public void authenticateAsSuperUser_shouldReturnFalseIfGivenInvalidCredentials() throws Exception {
		Assert.assertFalse(new UpdateFilter().authenticateAsSuperUser("a-bad-username", "a-bad-password"));
	}
	
	/**
	 * @see {@link UpdateFilter#authenticateAsSuperUser(String,String)}
	 */
	@Test
	@Verifies(value = "should return false if given user is not superuser", method = "authenticateAsSuperUser(String,String)")
	public void authenticateAsSuperUser_shouldReturnFalseIfGivenUserIsNotSuperuser() throws Exception {
		// can switch to using "butch" in standardDataSet once we know bruno's password
		executeDataSet("org/openmrs/api/include/UserServiceTest.xml");
		Context.authenticate("userWithSha512Hash", "test"); // sanity check
		Context.logout();
		
		Assert.assertFalse(new UpdateFilter().authenticateAsSuperUser("userWithSha512Hash", "test"));
	}
	
	/**
	 * @see {@link UpdateFilter#authenticateAsSuperUser(String,String)}
	 */
	@Test
	@Verifies(value = "should return true if given user is superuser", method = "authenticateAsSuperUser(String,String)")
	public void authenticateAsSuperUser_shouldReturnTrueIfGivenUserIsSuperuser() throws Exception {
		Assert.assertTrue(new UpdateFilter().authenticateAsSuperUser("admin", "test"));
	}
	
	/**
	 * @see {@link UpdateFilter#isSuperUser(Connection,Integer)}
	 */
	@Test
	@Verifies(value = "should return true if given user has superuser role", method = "isSuperUser(Connection,Integer)")
	public void isSuperUser_shouldReturnTrueIfGivenUserHasSuperuserRole() throws Exception {
		Assert.assertTrue(new UpdateFilter().isSuperUser(getConnection(), 1));
	}
	
	/**
	 * @see {@link UpdateFilter#isSuperUser(Connection,Integer)}
	 */
	@Test
	@Verifies(value = "should return false if given user does not have the super user role", method = "isSuperUser(Connection,Integer)")
	public void isSuperUser_shouldReturnFalseIfGivenUserDoesNotHaveTheSuperUserRole() throws Exception {
		Assert.assertFalse(new UpdateFilter().isSuperUser(getConnection(), 502));
	}
}
