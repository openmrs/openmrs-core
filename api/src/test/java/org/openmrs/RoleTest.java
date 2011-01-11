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
package org.openmrs;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.openmrs.test.Verifies;
import org.openmrs.util.RoleConstants;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;

/**
 * This class tests all methods that are not getter or setters in the Role java object TODO: finish
 * this test class for Role
 * 
 * @see Role
 */
public class RoleTest {
	
	/**
	 * Test the adding and removing of privileges to a role
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldAddRemovePrivilege() throws Exception {
		Role role = new Role();
		
		// test the null parameter cases
		role.addPrivilege(null);
		role.removePrivilege(null);
		
		Privilege priv1 = new Privilege("priv1");
		role.addPrivilege(priv1);
		assertTrue("The role should have 1 privilege but instead has " + role.getPrivileges().size(), role.getPrivileges()
		        .size() == 1);
		
		// adding the same privilege should not be allowed
		role.addPrivilege(priv1);
		assertTrue("The role should have 1 privilege but instead has " + role.getPrivileges().size(), role.getPrivileges()
		        .size() == 1);
		
		// adding a different privilege with the same name should not be allowed
		Privilege priv2 = new Privilege(priv1.getPrivilege());
		role.addPrivilege(priv2);
		assertTrue("The role should have 1 privilege but instead has " + role.getPrivileges().size(), role.getPrivileges()
		        .size() == 1);
		
		Privilege priv3 = new Privilege("priv3");
		
		// removing a fake privilege shouldn't do anything
		role.removePrivilege(priv3);
		assertTrue("The role should have 1 privilege but instead has " + role.getPrivileges().size(), role.getPrivileges()
		        .size() == 1);
		
		// removing the first privilege
		role.removePrivilege(priv1);
		assertTrue("The role should have 0 privileges but instead has " + role.getPrivileges().size(), role.getPrivileges()
		        .size() == 0);
	}
	
	/**
	 * Simple test to check the hasPrivilege method
	 * 
	 * @see {@link Role#hasPrivilege(String)}
	 */
	@Test
	@Verifies(value = "should not fail given null parameter", method = "hasPrivilege(String)")
	public void hasPrivilege_shouldNotFailGivenNullParameter() throws Exception {
		Role role = new Role();
		
		// test the null case
		role.hasPrivilege(null);
	}
	
	/**
	 * @see {@link Role#hasPrivilege(String)}
	 */
	@Test
	@Verifies(value = "should return true if found", method = "hasPrivilege(String)")
	public void hasPrivilege_shouldReturnTrueIfFound() throws Exception {
		Role role = new Role();
		
		// very basic privilege adding and checking
		Privilege p1 = new Privilege("priv1");
		role.addPrivilege(p1);
		assertTrue("This roles should have the privilege", role.hasPrivilege("priv1"));
	}
	
	/**
	 * @see {@link Role#hasPrivilege(String)}
	 */
	@Test
	@Verifies(value = "should return false if not found", method = "hasPrivilege(String)")
	public void hasPrivilege_shouldReturnFalseIfNotFound() throws Exception {
		Role role = new Role();
		assertFalse("This roles should not have the privilege", role.hasPrivilege("some other privilege name"));
	}
	
	/**
	 * @see {@link Role#hasPrivilege(String)}
	 */
	@Test
	@Verifies(value = "should return true for any privilegeName if super user", method = "hasPrivilege(String)")
	public void hasPrivilege_shouldReturnTrueForAnyPrivilegeNameIfSuperUser() throws Exception {
		// check super user "super" status
		Role role = new Role(RoleConstants.SUPERUSER);
		
		assertTrue("Super users are super special and should have all privileges", role
		        .hasPrivilege("Some weird privilege name that shouldn't be there"));
		assertNotNull(role.getName());
		assertEquals(role.getName(), RoleConstants.SUPERUSER);
	}
	
	/**
	 * Tests how roles inherit other roles
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldInheritingRoles() throws Exception {
		
		// TODO finish test method
		
		// test the isInheriting method
		
		// test the getting of parent roles
		
		// test the recursing over parent roles
		
		// test the possible infinite loop condition of recursing over parent roles
	}
	
}
