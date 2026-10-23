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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.infinispan.Cache;
import org.infinispan.context.Flag;
import org.infinispan.notifications.Listener;
import org.infinispan.notifications.cachelistener.annotation.CacheEntryCreated;
import org.infinispan.notifications.cachelistener.event.CacheEntryCreatedEvent;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.Privilege;
import org.openmrs.Role;
import org.openmrs.api.db.UserDAO;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests {@link RolePrivilegeCache} on a two-node cluster running within this JVM.
 */
public class RolePrivilegeCacheClusterTest {

	private static ExternalReadSpringCacheManager[] nodes;

	private ExecutorService loadExecutor;

	private Role clerk;

	private RolePrivilegeCache cache1;

	private RolePrivilegeCache cache2;

	@BeforeAll
	public static void startCluster() throws Exception {
		nodes = InJvmCacheCluster.start("role-privilege-cache-test", RolePrivilegeCache.CACHE_NAME, "role-privileges");
	}

	@AfterAll
	public static void stopCluster() {
		InJvmCacheCluster.stop(nodes);
	}

	@BeforeEach
	public void setUp() {
		clerk = new Role("Clerk");
		clerk.addPrivilege(new Privilege("View Patients"));

		loadExecutor = Executors.newCachedThreadPool();
		cache1 = new RolePrivilegeCache(nodes[0], mock(UserDAO.class), loadExecutor::submit, name -> clerk);
		cache2 = new RolePrivilegeCache(nodes[1], mock(UserDAO.class), loadExecutor::submit, name -> clerk);
	}

	@AfterEach
	public void tearDown() {
		loadExecutor.shutdownNow();
		nativeCache(0).clear();
	}

	@Test
	public void getRolePrivileges_shouldNotRemoveTheEntryCachedOnAnotherNode() {
		load(cache1);
		load(cache2);

		assertTrue(nativeCache(0).containsKey("clerk"));
		assertTrue(nativeCache(1).containsKey("clerk"));
	}

	@Test
	public void clear_shouldRemoveEveryEntryOnEveryNode() {
		load(cache1);
		load(cache2);

		cache1.clear();

		assertFalse(nativeCache(0).containsKey("clerk"));
		assertFalse(nativeCache(1).containsKey("clerk"));
	}

	@Test
	public void getRolePrivileges_shouldNotCacheOrUseALoadThatRacesAnEvictionOnAnotherNode() {
		Role revoked = new Role("Clerk");
		revoked.addPrivilege(new Privilege("Revoked Privilege"));
		UserDAO dao = mock(UserDAO.class);
		when(dao.getRole("Clerk")).thenReturn(clerk);
		RolePrivilegeCache racing = new RolePrivilegeCache(nodes[1], dao, loadExecutor::submit, name -> {
			cache1.clear();
			return revoked;
		});

		RolePrivileges resolved = racing.getRolePrivileges(clerk);
		racing.awaitLoads();

		assertTrue(resolved.containsPrivilege("View Patients"));
		assertFalse(resolved.containsPrivilege("Revoked Privilege"));
		assertFalse(nativeCache(1).containsKey("clerk"));
	}

	@Test
	public void getRolePrivileges_shouldNotRemoveAnotherNodesEntryWhenDiscardingALoad() {
		load(cache1);
		// expire node2's token just after its load writes the entry, so the load must discard it
		ExpireTokenOnWrite expireToken = new ExpireTokenOnWrite(nativeCache(1));
		nativeCache(1).addListener(expireToken);
		try {
			load(cache2);
		} finally {
			nativeCache(1).removeListener(expireToken);
		}

		assertTrue(expireToken.fired);
		assertFalse(nativeCache(1).containsKey("clerk"));
		assertTrue(nativeCache(0).containsKey("clerk"));
	}

	@Test
	public void getRolePrivileges_shouldNotInvalidateAnotherNodesTokenWhenCreatingOne() {
		RolePrivilegeCache creatingOnNode2 = cache2;
		RolePrivilegeCache loadingOnNode1 = new RolePrivilegeCache(nodes[0], mock(UserDAO.class), loadExecutor::submit,
		        name -> {
			        // node2 has no token yet, so this creates one while node1's load is running
			        creatingOnNode2.getRolePrivileges(new Role("Other"));
			        return clerk;
		        });

		load(loadingOnNode1);

		assertTrue(nativeCache(1).containsKey(CacheInvalidation.GENERATION));
		assertTrue(nativeCache(0).containsKey("clerk"));
	}

	private void load(RolePrivilegeCache cache) {
		cache.getRolePrivileges(clerk);
		cache.awaitLoads();
	}

	@SuppressWarnings("unchecked")
	private static Cache<Object, Object> nativeCache(int node) {
		return (Cache<Object, Object>) nodes[node].getCache(RolePrivilegeCache.CACHE_NAME).getNativeCache();
	}

	/**
	 * Removes a node's generation token on that node alone, synchronously, whenever an entry is
	 * written.
	 */
	@Listener(sync = true)
	public static class ExpireTokenOnWrite {

		private final Cache<Object, Object> cache;

		private volatile boolean fired;

		ExpireTokenOnWrite(Cache<Object, Object> cache) {
			this.cache = cache;
		}

		@CacheEntryCreated
		public void entryCreated(CacheEntryCreatedEvent<Object, Object> event) {
			if (!event.isPre() && "clerk".equals(event.getKey())) {
				fired = true;
				cache.getAdvancedCache().withFlags(Flag.CACHE_MODE_LOCAL).remove(CacheInvalidation.GENERATION);
			}
		}
	}
}
