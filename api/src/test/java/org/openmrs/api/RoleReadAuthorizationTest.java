/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.Role;
import org.openmrs.api.cache.RolePrivilegeCacheTestUtil;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.UsernamePasswordCredentials;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;
import org.openmrs.util.PrivilegeConstants;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pins the guard on the role read the REST layer exposes without a top-level privilege check:
 * {@code GET role/{uuid}} calls {@link UserService#getRoleByUuid(String)}, so its
 * {@code @PreAuthorize} is the only thing protecting the object. The last tests assert that the
 * framework's own implicit-role read does not leave the caller holding {@code Get Roles}.
 */
class RoleReadAuthorizationTest extends BaseContextSensitiveTest {

	/**
	 * Provides {@code test_user}, which has no roles of its own and is authorized only through the
	 * implicit {@code Authenticated} role.
	 */
	private static final String GLOBAL_PROPERTIES_DATASET = "org/openmrs/api/include/AdministrationServiceTest-globalproperties.xml";

	private static final String PROVIDER_ROLE_NAME = "Provider";

	private static final String PROVIDER_ROLE_UUID = "3480cb6d-c291-46c8-8d3a-96dc33d199fb";

	private static final String NO_SUCH_ROLE_UUID = "00000000-0000-0000-0000-000000000000";

	/**
	 * Inserted by {@link #GLOBAL_PROPERTIES_DATASET}, so it exists for the privilege-read tests.
	 */
	private static final String EXISTING_PRIVILEGE_UUID = "d979d066-15e6-467c-9d4b-cb575ef12345";

	@BeforeEach
	void setUpFixture() {
		executeDataSet(GLOBAL_PROPERTIES_DATASET);
		Context.clearSession();
		// recompute role closures from the dataset, not from an earlier cache
		RolePrivilegeCacheTestUtil.clear();
		Context.logout();
	}

	@AfterEach
	void clearState() {
		RolePrivilegeCacheTestUtil.clear();
		Context.logout();
	}

	@Test
	void anonymous_getRoleByUuid_shouldBeDenied() {
		APIAuthenticationException denial = assertThrows(APIAuthenticationException.class,
		    () -> Context.getUserService().getRoleByUuid(PROVIDER_ROLE_UUID));

		assertFalse(Context.isAuthenticated());
		assertEquals("Privileges required: Get Roles", denial.getMessage());
	}

	@Test
	void anonymous_getRoleByName_shouldBeDenied() {
		APIAuthenticationException denial = assertThrows(APIAuthenticationException.class,
		    () -> Context.getUserService().getRole(PROVIDER_ROLE_NAME));

		assertEquals("Privileges required: Get Roles", denial.getMessage());
	}

	@Test
	void anonymous_getAllRoles_shouldBeDenied() {
		APIAuthenticationException denial = assertThrows(APIAuthenticationException.class,
		    () -> Context.getUserService().getAllRoles());

		assertEquals("Privileges required: Manage Roles", denial.getMessage());
	}

	@Test
	void anonymous_getPrivilegeByUuid_shouldBeDenied() {
		APIAuthenticationException denial = assertThrows(APIAuthenticationException.class,
		    () -> Context.getUserService().getPrivilegeByUuid(EXISTING_PRIVILEGE_UUID));

		assertEquals("Privileges required: Get Privileges", denial.getMessage());
	}

	@Test
	void authenticatedWithoutGetRoles_getRoleByUuid_shouldBeDenied() {
		denyGetRolesToTheAuthenticatedRole();
		authenticateAsTestUser();

		assertTrue(Context.isAuthenticated());
		APIAuthenticationException denial = assertThrows(APIAuthenticationException.class,
		    () -> Context.getUserService().getRoleByUuid(PROVIDER_ROLE_UUID));

		assertEquals("Privileges required: Get Roles", denial.getMessage());
	}

	@Test
	void authenticatedWithoutGetRoles_getRoleByName_shouldBeDenied() {
		denyGetRolesToTheAuthenticatedRole();
		authenticateAsTestUser();

		APIAuthenticationException denial = assertThrows(APIAuthenticationException.class,
		    () -> Context.getUserService().getRole(PROVIDER_ROLE_NAME));

		assertEquals("Privileges required: Get Roles", denial.getMessage());
	}

	@Test
	void authenticatedWithGetRoles_getRoleByUuid_shouldBeAllowed() {
		authenticateAsTestUser();

		// test_user has no roles of its own; the grant comes from Authenticated, as in production
		assertTrue(Context.hasPrivilege(PrivilegeConstants.GET_ROLES));
		Role role = Context.getUserService().getRoleByUuid(PROVIDER_ROLE_UUID);

		assertNotNull(role);
		assertEquals(PROVIDER_ROLE_NAME, role.getRole());
	}

	@Test
	void authenticatedWithGetRoles_getRoleByName_shouldBeAllowed() {
		authenticateAsTestUser();

		assertNotNull(Context.getUserService().getRole(PROVIDER_ROLE_NAME));
	}

	@Test
	void superuser_getRoleByUuid_shouldBeAllowed() {
		authenticate();

		assertNotNull(Context.getUserService().getRoleByUuid(PROVIDER_ROLE_UUID));
	}

	/**
	 * One role is read with {@code Get Roles}, while listing needs {@code Manage Roles}. Documented so
	 * the asymmetry is not mistaken for a regression.
	 */
	@Test
	void authenticatedWithGetRoles_shouldReadOneRoleButNotListRoles() {
		authenticateAsTestUser();

		assertNotNull(Context.getUserService().getRole(PROVIDER_ROLE_NAME));
		APIAuthenticationException denial = assertThrows(APIAuthenticationException.class,
		    () -> Context.getUserService().getAllRoles());

		assertEquals("Privileges required: Manage Roles", denial.getMessage());
	}

	@Test
	void deniedRead_shouldNotLeaveAProxyPrivilegeBehind() {
		Context.logout();
		assertFalse(Context.hasProxyPrivileges());

		assertThrows(APIAuthenticationException.class, () -> Context.getUserService().getRoleByUuid(PROVIDER_ROLE_UUID));

		assertFalse(Context.hasProxyPrivileges(), "a denied read must not leave a proxy privilege behind");
		assertFalse(Context.hasPrivilege(PrivilegeConstants.GET_ROLES), "the caller must still not hold Get Roles");
		assertFalse(Context.hasProxyPrivileges(),
		    "the framework's own implicit role read must remove the proxy privilege it needs");
	}

	@Test
	void getRoles_shouldNotBeGrantedByAPrivilegeCheckOfItsOwn() {
		Context.logout();

		// the framework reads its implicit roles through the privilege-checked UserService on the
		// first check; that read must not count as the caller holding Get Roles
		assertFalse(Context.hasPrivilege(PrivilegeConstants.GET_ROLES));
		assertFalse(Context.hasPrivilege(PrivilegeConstants.GET_ROLES), "nor on the second, cached check");
		assertFalse(Context.hasProxyPrivileges());
	}

	@Test
	void repeatedDenials_shouldNotBecomeAllowances() {
		Context.logout();

		for (int i = 0; i < 3; i++) {
			assertThrows(APIAuthenticationException.class, () -> Context.getUserService().getRoleByUuid(PROVIDER_ROLE_UUID));
		}

		assertFalse(Context.hasPrivilege(PrivilegeConstants.GET_ROLES));
		assertFalse(Context.hasProxyPrivileges());
	}

	@Test
	void unknownRoleUuid_shouldBeDeniedIdenticallyToAnExistingOne() {
		Context.logout();

		APIAuthenticationException existing = assertThrows(APIAuthenticationException.class,
		    () -> Context.getUserService().getRoleByUuid(PROVIDER_ROLE_UUID));
		APIAuthenticationException unknown = assertThrows(APIAuthenticationException.class,
		    () -> Context.getUserService().getRoleByUuid(NO_SUCH_ROLE_UUID));

		assertEquals(existing.getClass(), unknown.getClass());
		assertEquals(existing.getMessage(), unknown.getMessage(), "a denial must not reveal whether the uuid names a role");
	}

	@Test
	void privilegeCheck_shouldTerminateAndStillSeeConfiguredGrants() {
		Context.logout();

		// reading the implicit roles is privilege-checked; without a proxy scoped to that read, this
		// check would recurse
		assertFalse(Context.hasPrivilege("A Privilege Nobody Holds"));

		// the implicit roles are still readable and still grant what the database says
		authenticateAsTestUser();
		assertTrue(Context.hasPrivilege(PrivilegeConstants.GET_ROLES));
	}

	private void authenticateAsTestUser() {
		Context.authenticate(new UsernamePasswordCredentials("test_user", "test"));
	}

	/**
	 * Seeds {@code Authenticated} without {@code Get Roles} (not persisted), modelling an install that
	 * does not grant role reads to every authenticated user.
	 */
	private void denyGetRolesToTheAuthenticatedRole() {
		RolePrivilegeCacheTestUtil.seed("Authenticated");
	}
}
