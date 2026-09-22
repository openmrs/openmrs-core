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

import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.Privilege;
import org.openmrs.Role;
import org.openmrs.api.UserService;
import org.openmrs.api.context.Context;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integration tests verifying that {@link RolePrivilegeCache} populates the shared
 * {@code apiCacheManager} cache and that it is evicted on role and privilege mutations.
 */
public class RolePrivilegeCacheIntegrationTest extends BaseContextSensitiveTest {

	@Autowired
	private RolePrivilegeCache rolePrivilegeCache;

	@Autowired
	private UserService userService;

	private CacheManager cacheManager;

	@BeforeEach
	public void setup() {
		cacheManager = Context.getRegisteredComponent("apiCacheManager", CacheManager.class);
		cache().clear();
	}

	private Cache cache() {
		return cacheManager.getCache(RolePrivilegeCache.CACHE_NAME);
	}

	private RolePrivileges cachedEntry(Role role) {
		return cache().get(RolePrivileges.normalize(role.getRole()), RolePrivileges.class);
	}

	@Test
	public void cache_shouldBeConfiguredWithALifespanTtl() {
		// The TTL safety net now comes from the dedicated role-privileges cache template rather than a
		// per-entry put, so assert the backing Infinispan cache actually carries a lifespan; a default
		// (-1) would mean the template was not wired and the safety net silently lost.
		org.infinispan.Cache<?, ?> nativeCache = (org.infinispan.Cache<?, ?>) cache().getNativeCache();

		assertEquals(3_600_000L, nativeCache.getCacheConfiguration().expiration().lifespan());
	}

	@Test
	public void getRolePrivileges_shouldPopulateCacheOnMiss() {
		// "Provider" is a committed role in the standard dataset, so the daemon thread's own session can
		// load it; a freshly saved role would not be visible across the daemon's separate transaction.
		Role role = userService.getRole("Provider");
		assertNull(cachedEntry(role), "cache should start empty for this role");

		RolePrivileges result = rolePrivilegeCache.getRolePrivileges(role);

		assertNotNull(result);
		assertNotNull(cachedEntry(role), "cache should be populated after lookup");
		assertEquals(result, cachedEntry(role), "the cached entry should match the returned value");
	}

	@Test
	public void getRolePrivileges_shouldServeSubsequentLookupsFromCache() {
		// Pre-seed the cache directly, then verify the lookup returns that value rather than recomputing.
		// The role name is not in the database, so a miss would fail-closed to an empty closure and drop
		// the seeded privilege; this also keeps the test independent of daemon-thread visibility.
		RolePrivileges seeded = new RolePrivileges(Collections.singleton("Cache Hit Privilege"), false);
		cache().put(RolePrivileges.normalize("Cache Hit Role"), seeded);

		RolePrivileges result = rolePrivilegeCache.getRolePrivileges(new Role("Cache Hit Role"));

		assertEquals(seeded, result, "lookup should return the value already in the cache");
		assertTrue(result.containsPrivilege("Cache Hit Privilege"));
	}

	@Test
	public void getRolePrivileges_shouldGrantNothingForARoleThatNoLongerExists() {
		// A detached role still held by a session after it was purged: the instance carries privileges but
		// the role is gone from the database. Fail-closed resolution must grant nothing and must not write
		// the stale privileges into the shared cache.
		Role purged = new Role("Nonexistent Role");
		purged.addPrivilege(new Privilege("Ghost Privilege"));

		RolePrivileges resolved = rolePrivilegeCache.getRolePrivileges(purged);

		assertFalse(resolved.containsPrivilege("Ghost Privilege"), "a role absent from the database must grant nothing");
		assertFalse(resolved.grantsSuperuser());
		RolePrivileges cached = cachedEntry(purged);
		assertNotNull(cached, "the empty closure should be cached to avoid repeated daemon loads for a missing role");
		assertFalse(cached.containsPrivilege("Ghost Privilege"),
		    "stale caller-supplied privileges must not be written to the shared cache");
	}

	@Test
	public void getRolePrivileges_shouldResolveFromAFreshRoleNotACallerSuppliedStaleInstance() {
		// "Provider" is a committed role with no privileges, so the daemon session sees it. A stale copy
		// carrying a phantom privilege must not grant it: the closure comes from the freshly loaded role,
		// not the caller-supplied instance.
		Role stale = new Role("Provider");
		stale.addPrivilege(new Privilege("Phantom Privilege"));

		cache().clear();
		RolePrivileges resolved = rolePrivilegeCache.getRolePrivileges(stale);

		assertFalse(resolved.containsPrivilege("Phantom Privilege"),
		    "caller-supplied privileges must be ignored in favor of the freshly loaded role");
	}

	@Test
	public void saveRole_shouldEvictTheCache() {
		primeCache();

		Role role = new Role("Evicting Save Role", "role saved to trigger eviction");
		userService.saveRole(role);

		assertCacheCleared();
	}

	@Test
	public void purgeRole_shouldEvictTheCache() {
		Role role = new Role("Purgeable Role", "role that will be purged");
		userService.saveRole(role);

		primeCache();

		userService.purgeRole(role);

		assertCacheCleared();
	}

	@Test
	public void savePrivilege_shouldEvictTheCache() {
		primeCache();

		Privilege privilege = new Privilege("Evicting Save Privilege", "privilege saved to trigger eviction");
		userService.savePrivilege(privilege);

		assertCacheCleared();
	}

	@Test
	public void purgePrivilege_shouldEvictTheCache() {
		Privilege privilege = new Privilege("Purgeable Privilege", "privilege that will be purged");
		userService.savePrivilege(privilege);

		primeCache();

		userService.purgePrivilege(privilege);

		assertCacheCleared();
	}

	private void primeCache() {
		Role role = new Role("Primed Role");
		role.addPrivilege(new Privilege("Primed Privilege"));
		rolePrivilegeCache.getRolePrivileges(role);
		assertNotNull(cachedEntry(role), "cache should be primed");
	}

	private void assertCacheCleared() {
		Role primed = new Role("Primed Role");
		assertNull(cachedEntry(primed), "cache should be cleared after mutation");
	}
}
