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
import org.openmrs.Location;
import org.openmrs.Privilege;
import org.openmrs.Role;
import org.openmrs.api.UserService;
import org.openmrs.api.context.Context;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;
import org.openmrs.util.RoleConstants;
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
 * {@code apiCacheManager} cache, that it resolves a role from a freshly loaded copy rather than a
 * caller-supplied stale one, and that it is evicted on {@link Role} and {@link Privilege}
 * mutations.
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
	public void saveRole_shouldStopTheTransactionReadingTheCache() {
		primeCache();

		Role role = new Role("Evicting Save Role", "role saved to trigger eviction");
		userService.saveRole(role);

		assertPrimedEntryNotServed();
	}

	@Test
	public void purgeRole_shouldStopTheTransactionReadingTheCache() {
		Role role = new Role("Purgeable Role", "role that will be purged");
		userService.saveRole(role);

		primeCache();

		userService.purgeRole(role);

		assertPrimedEntryNotServed();
	}

	@Test
	public void savePrivilege_shouldStopTheTransactionReadingTheCache() {
		primeCache();

		Privilege privilege = new Privilege("Evicting Save Privilege", "privilege saved to trigger eviction");
		userService.savePrivilege(privilege);

		assertPrimedEntryNotServed();
	}

	@Test
	public void purgePrivilege_shouldStopTheTransactionReadingTheCache() {
		Privilege privilege = new Privilege("Purgeable Privilege", "privilege that will be purged");
		userService.savePrivilege(privilege);

		primeCache();

		userService.purgePrivilege(privilege);

		assertPrimedEntryNotServed();
	}

	@Test
	public void getRolePrivileges_shouldSeeRoleChangesMadeEarlierInTheSameTransaction() {
		// The daemon load cannot see this transaction's uncommitted role, so resolving it through the cache
		// would grant nothing and cache that for every session.
		Privilege privilege = userService.savePrivilege(new Privilege("Uncommitted Privilege", "an uncommitted privilege"));
		Role role = new Role("Uncommitted Role", "role saved in the current transaction");
		role.addPrivilege(privilege);
		userService.saveRole(role);

		RolePrivileges resolved = rolePrivilegeCache.getRolePrivileges(role);

		assertTrue(resolved.containsPrivilege("Uncommitted Privilege"));
		assertNull(cachedEntry(role), "a role changed in the current transaction must not be cached");
	}

	@Test
	public void flushingAChangeToALoadedRole_shouldStopTheTransactionReadingTheCache() {
		Role provider = userService.getRole("Provider");
		primeCache();

		provider.setDescription("changed without saving through the service");
		Context.flushSession();

		assertPrimedEntryNotServed();
	}

	@Test
	public void flushingAChangeToALoadedRolesInheritedRoles_shouldStopTheTransactionReadingTheCache() {
		// flushed as an update of the role's collection, not of the role
		Role provider = userService.getRole("Provider");
		Role authenticated = userService.getRole(RoleConstants.AUTHENTICATED);
		primeCache();

		provider.getInheritedRoles().add(authenticated);
		Context.flushSession();

		assertPrimedEntryNotServed();
	}

	@Test
	public void flushingAnUnrelatedChange_shouldNotAffectTheCache() {
		Location location = Context.getLocationService().getLocation(1);
		primeCache();

		location.setDescription("changed");
		Context.flushSession();

		assertTrue(rolePrivilegeCache.getRolePrivileges(new Role("Primed Role")).containsPrivilege("Primed Privilege"));
	}

	private void primeCache() {
		// Seeded directly: after a role or privilege change in this transaction, lookups bypass the cache.
		Role role = new Role("Primed Role");
		cache().put(RolePrivileges.normalize(role.getRole()),
		    new RolePrivileges(Collections.singleton("Primed Privilege"), false));
		assertNotNull(cachedEntry(role), "cache should be primed");
	}

	/**
	 * The cache is only evicted when the test's transaction completes, so this checks what the
	 * transaction itself sees: "Primed Role" is not in the database, so resolving it without the cache
	 * grants nothing.
	 */
	private void assertPrimedEntryNotServed() {
		assertFalse(rolePrivilegeCache.getRolePrivileges(new Role("Primed Role")).containsPrivilege("Primed Privilege"),
		    "the transaction should resolve roles without the cache after a change");
	}

}
