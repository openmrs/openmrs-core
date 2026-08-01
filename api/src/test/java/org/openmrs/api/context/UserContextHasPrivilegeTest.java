/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api.context;

import java.util.Collections;
import java.util.HashSet;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.Privilege;
import org.openmrs.Role;
import org.openmrs.User;
import org.openmrs.api.cache.RolePrivilegeCache;
import org.openmrs.api.cache.RolePrivileges;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;
import org.openmrs.util.RoleConstants;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Behavioral tests for {@link UserContext#hasPrivilege(String)}. Each test pre-populates the
 * {@code rolePrivileges} cache so the check resolves against a known {@link RolePrivileges} value
 * (a cache hit bypasses the daemon-backed fresh load), exercising the resolution wiring directly:
 * proxy handling lives in {@link UserContextTest}, the flattening algorithm in
 * {@code RolePrivilegeCacheTest}, and the daemon fresh load in
 * {@code RolePrivilegeCacheIntegrationTest}.
 */
public class UserContextHasPrivilegeTest extends BaseContextSensitiveTest {

	private static final String GRANTED = "Cache Test Granted Privilege";

	private CacheManager cacheManager;

	@BeforeEach
	public void setup() {
		cacheManager = Context.getRegisteredComponent("apiCacheManager", CacheManager.class);
		rolePrivilegeCache().clear();
	}

	@AfterEach
	public void clearCache() {
		rolePrivilegeCache().clear();
	}

	private Cache rolePrivilegeCache() {
		return cacheManager.getCache(RolePrivilegeCache.CACHE_NAME);
	}

	private void cacheRole(String roleName, RolePrivileges value) {
		rolePrivilegeCache().put(RolePrivileges.normalize(roleName), value);
	}

	private User userWithRole(String roleName) {
		User user = new User();
		user.addRole(new Role(roleName));
		return user;
	}

	/**
	 * Runs the given action with the supplied user (or <code>null</code> for an unauthenticated
	 * context) set on the current {@link UserContext}, restoring the previous user afterward.
	 */
	private void runAs(User user, Runnable action) throws IllegalAccessException {
		UserContext userContext = Context.getUserContext();
		User previous = userContext.getAuthenticatedUser();
		try {
			FieldUtils.getField(UserContext.class, "user", true).set(userContext, user);
			action.run();
		} finally {
			FieldUtils.getField(UserContext.class, "user", true).set(userContext, previous);
		}
	}

	@Test
	public void hasPrivilege_shouldAuthorizeWhenADirectlyAssignedRoleGrantsThePrivilege() throws Exception {
		cacheRole("Clerk", new RolePrivileges(Collections.singleton(GRANTED), false));

		runAs(userWithRole("Clerk"), () -> assertTrue(Context.hasPrivilege(GRANTED)));
	}

	@Test
	public void hasPrivilege_shouldMatchPrivilegesCaseInsensitively() throws Exception {
		cacheRole("Clerk", new RolePrivileges(Collections.singleton(GRANTED), false));

		runAs(userWithRole("Clerk"), () -> assertTrue(Context.hasPrivilege(GRANTED.toUpperCase())));
	}

	@Test
	public void hasPrivilege_shouldDenyWhenNoRoleGrantsThePrivilege() throws Exception {
		cacheRole("Clerk", new RolePrivileges(Collections.singleton("Some Unrelated Privilege"), false));

		runAs(userWithRole("Clerk"), () -> assertFalse(Context.hasPrivilege(GRANTED)));
	}

	@Test
	public void hasPrivilege_shouldAuthorizeAnyPrivilegeWhenARoleGrantsSuperuser() throws Exception {
		// grantsSuperuser is set by the flattening walk when the superuser role is the role itself or
		// anywhere in its inherited closure, so this also covers inherited superuser status.
		cacheRole("Delegated Admin", new RolePrivileges(Collections.emptySet(), true));

		runAs(userWithRole("Delegated Admin"), () -> assertTrue(Context.hasPrivilege("Any Arbitrary Privilege")));
	}

	@Test
	public void hasPrivilege_shouldAuthorizeTheEmptyPrivilegeForAnyAuthenticatedUser() throws Exception {
		runAs(userWithRole("Clerk"), () -> assertTrue(Context.hasPrivilege("")));
	}

	@Test
	public void hasPrivilege_shouldAuthorizeWhenTheAuthenticatedRoleGrantsThePrivilege() throws Exception {
		cacheRole(RoleConstants.AUTHENTICATED, new RolePrivileges(Collections.singleton(GRANTED), false));

		// The user's own role does not grant it; the authenticated role does.
		runAs(userWithRole("Clerk"), () -> assertTrue(Context.hasPrivilege(GRANTED)));
	}

	@Test
	public void hasPrivilege_shouldAuthorizeWhenTheAnonymousRoleGrantsThePrivilegeForUnauthenticatedUsers()
	        throws Exception {
		cacheRole(RoleConstants.ANONYMOUS, new RolePrivileges(Collections.singleton(GRANTED), false));

		runAs(null, () -> assertTrue(Context.hasPrivilege(GRANTED)));
	}

	@Test
	public void hasPrivilege_shouldHonorInheritedPrivilegesForTheAuthenticatedRole() throws Exception {
		// The pre-cache code resolved the authenticated and anonymous roles via Role.hasPrivilege, which
		// ignores inherited roles; routing them through the flattening cache deliberately honors inheritance.
		// Seed the authenticated role's entry as the daemon flatten would for a role that inherits GRANTED.
		Role inherited = new Role("Inherited Role");
		inherited.addPrivilege(new Privilege(GRANTED));
		Role authenticated = new Role(RoleConstants.AUTHENTICATED);
		authenticated.setInheritedRoles(new HashSet<>(Collections.singletonList(inherited)));
		cacheRole(RoleConstants.AUTHENTICATED, RolePrivilegeCache.computeRolePrivileges(authenticated));

		runAs(userWithRole("Clerk"), () -> assertTrue(Context.hasPrivilege(GRANTED)));
	}

	@Test
	public void hasPrivilege_shouldNotAuthorizeTheEmptyOrNullPrivilegeForUnauthenticatedUsers() throws Exception {
		// The empty-privilege shortcut applies only to authenticated users; an unauthenticated caller is
		// authorized solely by the anonymous role, which here grants nothing.
		cacheRole(RoleConstants.ANONYMOUS, new RolePrivileges(Collections.emptySet(), false));

		runAs(null, () -> {
			assertFalse(Context.hasPrivilege(""));
			assertFalse(Context.hasPrivilege(null));
		});
	}

	@Test
	public void hasPrivilege_shouldNotAuthorizeViaAProxyPrivilegeWhenProxiesAreExcluded() throws Exception {
		runAs(userWithRole("Clerk"), () -> {
			Context.addProxyPrivilege(GRANTED);
			try {
				// proxy-inclusive check is satisfied by the proxy privilege...
				assertTrue(Context.hasPrivilege(GRANTED));
				// ...but the proxy-excluding check is not, since no role grants it
				assertFalse(Context.hasPrivilege(GRANTED, false));
			} finally {
				Context.removeProxyPrivilege(GRANTED);
			}
		});
	}

	@Test
	public void hasPrivilege_shouldAuthorizeViaARoleEvenWhenProxiesAreExcluded() throws Exception {
		cacheRole("Clerk", new RolePrivileges(Collections.singleton(GRANTED), false));

		runAs(userWithRole("Clerk"), () -> assertTrue(Context.hasPrivilege(GRANTED, false)));
	}
}
