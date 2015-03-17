/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs;

import java.util.HashSet;
import java.util.Set;
import junit.framework.Assert;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.openmrs.api.context.Context;
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
		assertEquals("Incorrect number of privileges", 1, role.getPrivileges().size());
		
		// adding the same privilege should not be allowed
		role.addPrivilege(priv1);
		assertEquals("Incorrect number of privileges", 1, role.getPrivileges().size());
		
		// adding a different privilege with the same name should not be allowed
		Privilege priv2 = new Privilege(priv1.getPrivilege());
		role.addPrivilege(priv2);
		assertEquals("Incorrect number of privileges", 1, role.getPrivileges().size());
		
		Privilege priv3 = new Privilege("priv3");
		
		// removing a fake privilege shouldn't do anything
		role.removePrivilege(priv3);
		assertEquals("Incorrect number of privileges", 1, role.getPrivileges().size());
		
		// removing the first privilege
		role.removePrivilege(priv1);
		assertEquals("Incorrect number of privileges", 0, role.getPrivileges().size());
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
	 * @see {@link Role#getAllParentRoles()}
	 */
	@Test
	@Verifies(value = "should only return parent roles", method = "getAllParentRoles()")
	public void getAllParentRoles_shouldOnlyReturnParentRoles() throws Exception {
		Role grandparent = new Role("Grandparent");
		Role aunt = new Role("Aunt");
		Role uncle = new Role("Uncle");
		Role parent = new Role("Parent");
		Role child1 = new Role("Child 1");
		Role child2 = new Role("Child 2");
		Role niece = new Role("Niece");
		
		Set<Role> inheritedRoles = new HashSet<Role>();
		
		// grandparent -> aunt, uncle, parent
		inheritedRoles.add(grandparent);
		parent.setInheritedRoles(new HashSet(inheritedRoles));
		aunt.setInheritedRoles(new HashSet(inheritedRoles));
		uncle.setInheritedRoles(new HashSet(inheritedRoles));
		
		// aunt, uncle -> niece
		inheritedRoles.clear();
		inheritedRoles.add(uncle);
		inheritedRoles.add(aunt);
		niece.setInheritedRoles(new HashSet(inheritedRoles));
		
		// parent -> child1, child2
		inheritedRoles.clear();
		inheritedRoles.add(parent);
		child1.setInheritedRoles(new HashSet(inheritedRoles));
		child2.setInheritedRoles(new HashSet(inheritedRoles));
		
		// ensure only inherited roles are found
		Assert.assertEquals(3, niece.getAllParentRoles().size());
		Assert.assertEquals(2, child1.getAllParentRoles().size());
		Assert.assertEquals(2, child2.getAllParentRoles().size());
		Assert.assertEquals(1, parent.getAllParentRoles().size());
		Assert.assertEquals(1, aunt.getAllParentRoles().size());
		Assert.assertEquals(1, uncle.getAllParentRoles().size());
		Assert.assertEquals(0, grandparent.getAllParentRoles().size());
	}
	
	/**
	 * @see {@link Role#getAllChildRoles()}
	 */
	@Test
	@Verifies(value = "should only return child roles", method = "getAllChildRoles()")
	public void getAllChildRoles_shouldOnlyReturnChildRoles() throws Exception {
		Role grandparent = new Role("Grandparent");
		Role aunt = new Role("Aunt");
		Role uncle = new Role("Uncle");
		Role parent = new Role("Parent");
		Role child1 = new Role("Child 1");
		Role child2 = new Role("Child 2");
		Role niece = new Role("Niece");
		
		Set<Role> childRoles = new HashSet<Role>();
		
		// grandparent -> aunt, uncle, parent
		childRoles.add(aunt);
		childRoles.add(uncle);
		childRoles.add(parent);
		grandparent.setChildRoles(new HashSet(childRoles));
		
		// aunt, uncle -> niece
		childRoles.clear();
		childRoles.add(niece);
		aunt.setChildRoles(new HashSet(childRoles));
		uncle.setChildRoles(new HashSet(childRoles));
		
		// parent -> child1, child2
		childRoles.clear();
		childRoles.add(child1);
		childRoles.add(child2);
		parent.setChildRoles(new HashSet(childRoles));
		
		// ensure only child roles are found
		Assert.assertEquals(0, niece.getAllChildRoles().size());
		Assert.assertEquals(0, child1.getAllChildRoles().size());
		Assert.assertEquals(0, child2.getAllChildRoles().size());
		Assert.assertEquals(2, parent.getAllChildRoles().size());
		Assert.assertEquals(1, aunt.getAllChildRoles().size());
		Assert.assertEquals(1, uncle.getAllChildRoles().size());
		Assert.assertEquals(6, grandparent.getAllChildRoles().size());
	}
	
}
