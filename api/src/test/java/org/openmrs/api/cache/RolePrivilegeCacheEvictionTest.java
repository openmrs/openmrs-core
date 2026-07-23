/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api.cache;

import org.junit.jupiter.api.Test;
import org.openmrs.Privilege;
import org.openmrs.Role;
import org.openmrs.api.UserService;
import org.openmrs.api.context.Context;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Context-sensitive test proving that the {@code @CacheEvict} annotations on the role/privilege
 * write methods actually clear the {@code rolePrivileges} cache, so edits through the service take
 * effect immediately. Unlike {@link RolePrivilegeCacheTest} (which bypasses the service to prove
 * caching), this test goes through the real {@link UserService} so the service proxy fires the
 * eviction.
 */
public class RolePrivilegeCacheEvictionTest extends BaseContextSensitiveTest {

	@Test
	void saveRole_shouldEvictCacheSoPrivilegeChangesTakeEffectImmediately() {
		RolePrivilegeCache cache = Context.getRegisteredComponent("rolePrivilegeCache", RolePrivilegeCache.class);
		UserService userService = Context.getUserService();

		// create a role that has a single privilege, saved through the service
		Privilege privilegeA = userService.savePrivilege(new Privilege("Cache Test Privilege A", "test"));
		Role role = new Role("Cache Test Role", "test");
		role.addPrivilege(privilegeA);
		role = userService.saveRole(role);

		// populate the cache for this role
		RolePrivileges before = cache.getPrivileges(role);
		assertTrue(before.containsPrivilege("cache test privilege a"));
		assertFalse(before.containsPrivilege("cache test privilege b"));

		// add a second privilege and save -> @CacheEvict on saveRole must clear the cache
		Privilege privilegeB = userService.savePrivilege(new Privilege("Cache Test Privilege B", "test"));
		role.addPrivilege(privilegeB);
		userService.saveRole(role);

		// a fresh lookup must now reflect the new privilege; if eviction had NOT fired, this would
		// return the stale cached entry (without privilege B) and the assertion would fail
		RolePrivileges after = cache.getPrivileges(role);
		assertTrue(after.containsPrivilege("cache test privilege b"));
	}
}
