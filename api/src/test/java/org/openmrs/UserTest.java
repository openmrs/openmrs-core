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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.util.RoleConstants;

import java.util.Collection;

public class UserTest {
	
	private User user;
	
	private final String MATERNITY_NURSE_UPPERCASE = "Maternity Nurse";
	
	private final String MATERNITY_NURSE_LOWERCASE = "maternity nurse";
	
	private final String ROLE_WHICH_DOES_NOT_EXIT = "Role Which Does Not Exist";

	private final String AUTHENTICATED_USER_PRIVILEGES = "";
	@Before
	public void setUp() {
		user = new User();
		user.addRole(new Role("Some Role", "This is a test role"));
	}
	
	@Test
	public void hasRole_shouldHaveRole() {
		assertTrue(user.hasRole("Some Role"));
	}
	
	@Test
	public void hasRole_shouldNotHaveRole() {
		assertFalse(user.hasRole("Not A Role"));
	}
	
	@Test
	public void hasRole_shouldHaveAnyRoleWhenSuperUser() {
		user.addRole(new Role(RoleConstants.SUPERUSER));
		assertTrue(user.hasRole("Not A Role"));
	}
	
	@Test
	public void hasRole_shouldNotHaveAnyRoleWhenSuperWhenIgnoreSuperUserFlagIsTrue() {
		user.addRole(new Role(RoleConstants.SUPERUSER));
		assertFalse(user.hasRole("Not A Role", true));
	}
	
	@Test
	public void isSuperUser_shouldBeSuperUser() {
		user.addRole(new Role(RoleConstants.SUPERUSER));
		assertTrue(user.isSuperUser());
	}
	
	@Test
	public void isSuperUser_shouldNotBeSuperUser() {
		assertFalse(user.isSuperUser());
	}
	
	/**
	 * @see User#containsRole(String)
	 */
	@Test
	public void containsRole_shouldBeCaseInsensitive() {
		user.addRole(new Role(MATERNITY_NURSE_UPPERCASE));
		assertTrue(user.containsRole(MATERNITY_NURSE_UPPERCASE));
		assertTrue(user.containsRole(MATERNITY_NURSE_LOWERCASE));
	}
	
	/**
	 * @see User#containsRole(String)
	 */
	@Test
	public void containsRole_shouldReturnTrueIfTheUserHasTheGivenRole() {
		user.addRole(new Role(MATERNITY_NURSE_UPPERCASE));
		assertTrue(user.containsRole(MATERNITY_NURSE_UPPERCASE));
	}
	
	/**
	 * @see User#containsRole(String)
	 */
	@Test
	public void containsRole_shouldReturnFalseIfTheUserDoesNotHaveTheGivenRole() {
		assertFalse(user.containsRole(ROLE_WHICH_DOES_NOT_EXIT));
	}

	@Test
	public void hasPrivilege_shouldReturnTrueForAuthenticatedUser(){
		user.addRole(new Role(RoleConstants.AUTHENTICATED));
		assertTrue(user.hasPrivilege(AUTHENTICATED_USER_PRIVILEGES));
	}

	@Test
	public void hasPrivilege_shouldReturnTrueForSuperUser(){
		user.addRole(new Role(RoleConstants.SUPERUSER));
		assertTrue(user.hasPrivilege("Any privilege"));
	}

	@Test
	public void hasPrivilege_shouldReturnTrueGivenPrivilegeFoundInAnyRole(){
		Role authenticatedRole = new Role(RoleConstants.AUTHENTICATED);
		authenticatedRole.addPrivilege(new Privilege("priv1"));
		user.addRole(authenticatedRole);

		assertTrue(user.hasPrivilege("priv1"));
	}

	@Test
	public void hasPrivilege_shouldReturnFalseIfGivenPrivilegeNotFoundInAnyRole(){
		Role authenticatedRole = new Role(RoleConstants.AUTHENTICATED);
		authenticatedRole.addPrivilege(new Privilege("priv1"));
		user.addRole(authenticatedRole);

		Role providerRole = new Role(RoleConstants.PROVIDER);
		providerRole.addPrivilege(new Privilege("priv2"));
		user.addRole(providerRole);

		assertFalse(user.hasPrivilege("Unknown privilege"));
	}

	@Test
	public void getPrivileges_shouldContainPreviouslyAddedPrivilege(){
		Role authenticatedRole = new Role(RoleConstants.AUTHENTICATED);
		Privilege privilege = new Privilege("priv1");
		authenticatedRole.addPrivilege(privilege);
		user.addRole(authenticatedRole);

		Collection<Privilege> privilegeCollection = user.getPrivileges();
		assertTrue(privilegeCollection.contains(privilege));
	}

}
