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
import java.util.concurrent.TimeUnit;

import org.infinispan.Cache;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.configuration.parsing.ConfigurationBuilderHolder;
import org.infinispan.configuration.parsing.ParserRegistry;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.notifications.Listener;
import org.infinispan.notifications.cachelistener.annotation.CacheEntryCreated;
import org.infinispan.notifications.cachelistener.event.CacheEntryCreatedEvent;
import org.infinispan.remoting.transport.jgroups.JGroupsTransport;
import org.infinispan.spring.embedded.provider.SpringEmbeddedCacheManager;
import org.jgroups.JChannel;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.GlobalProperty;
import org.openmrs.api.db.AdministrationDAO;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionSynchronizationUtils;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests {@link GlobalPropertyCache} on a two-node cluster, built from the cluster configuration in
 * {@code infinispan-api.xml} and running within this JVM.
 */
public class GlobalPropertyCacheClusterTest {

	private static SpringEmbeddedCacheManager node1;

	private static SpringEmbeddedCacheManager node2;

	private AdministrationDAO dao1;

	private AdministrationDAO dao2;

	private ExecutorService fillExecutor;

	private GlobalPropertyCache cache1;

	private GlobalPropertyCache cache2;

	@BeforeAll
	public static void startCluster() throws Exception {
		node1 = startNode("node1");
		node2 = startNode("node2");

		long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(30);
		while (node1.getNativeCacheManager().getMembers().size() < 2) {
			if (System.nanoTime() > deadline) {
				throw new IllegalStateException("The two cache managers did not form a cluster");
			}
			Thread.sleep(50);
		}
	}

	@AfterAll
	public static void stopCluster() {
		if (node2 != null) {
			node2.stop();
		}
		if (node1 != null) {
			node1.stop();
		}
	}

	@BeforeEach
	public void setUp() {
		dao1 = mockDao();
		dao2 = mockDao();
		fillExecutor = Executors.newCachedThreadPool();
		cache1 = new GlobalPropertyCache(node1, dao1, mock(PlatformTransactionManager.class), fillExecutor::execute);
		cache2 = new GlobalPropertyCache(node2, dao2, mock(PlatformTransactionManager.class), fillExecutor::execute);
	}

	@AfterEach
	public void tearDown() {
		cache1.destroy();
		cache2.destroy();
		fillExecutor.shutdownNow();
		nativeCache(node1).clear();
	}

	@Test
	public void get_shouldNotRemoveTheEntryCachedOnAnotherNode() {
		givenProperty("some.property", "value");

		fill(cache1, "some.property");
		fill(cache2, "some.property");

		assertNotNull(cache1.getIfCached("some.property"));
		assertNotNull(cache2.getIfCached("some.property"));
	}

	@Test
	public void evict_shouldRemoveTheEntryOnEveryNode() {
		givenProperty("some.property", "value");
		fill(cache1, "some.property");
		fill(cache2, "some.property");

		cache1.evict("some.property");

		assertFalse(nativeCache(node1).containsKey("some.property"));
		assertFalse(nativeCache(node2).containsKey("some.property"));
	}

	@Test
	public void evict_shouldRemoveTheEntryOnANodeThatDidNotCacheIt() {
		givenProperty("some.property", "value");
		fill(cache2, "some.property");

		cache1.evict("SOME.Property");

		assertFalse(nativeCache(node2).containsKey("some.property"));
	}

	@Test
	public void evict_shouldRemoveEveryKeyATransactionWroteOnEveryNode() {
		givenProperty("some.property", "value");
		givenProperty("other.property", "value");
		fill(cache2, "some.property");
		fill(cache2, "other.property");

		TransactionSynchronizationManager.initSynchronization();
		try {
			cache1.evict("some.property");
			cache1.evict("other.property");
			TransactionSynchronizationUtils.invokeAfterCompletion(TransactionSynchronizationManager.getSynchronizations(),
			    TransactionSynchronization.STATUS_COMMITTED);
		} finally {
			TransactionSynchronizationManager.clearSynchronization();
		}

		assertFalse(nativeCache(node2).containsKey("some.property"));
		assertFalse(nativeCache(node2).containsKey("other.property"));
	}

	@Test
	public void evict_shouldReplaceTheGenerationTokenOnEveryNode() {
		givenProperty("some.property", "value");
		fill(cache2, "some.property");
		Object token = nativeCache(node2).get(GlobalPropertyCache.GENERATION);

		cache1.evict("other.property");

		assertNotEquals(token, nativeCache(node2).get(GlobalPropertyCache.GENERATION));
	}

	@Test
	public void clear_shouldRemoveEveryEntryOnEveryNode() {
		givenProperty("some.property", "value");
		fill(cache2, "some.property");
		fill(cache2, "missing.property");

		cache1.clear();

		assertTrue(nativeCache(node2).isEmpty());
	}

	@Test
	public void get_shouldNotCacheAValueLoadedWhileAnotherNodeEvictsIt() {
		GlobalProperty property = new GlobalProperty("some.property", "old");
		// the first read is the caller's; the second is node2's fill, which races node1's eviction
		when(dao2.getGlobalPropertyObject("some.property")).thenReturn(property).thenAnswer(invocation -> {
			cache1.evict("some.property");
			return property;
		});
		when(dao2.getStoredGlobalPropertyName("some.property")).thenReturn("some.property");

		fill(cache2, "some.property");

		assertFalse(nativeCache(node2).containsKey("some.property"));
	}

	@Test
	public void get_shouldNotCacheAnAbsenceLoadedWhileAnotherNodeEvictsIt() {
		when(dao2.getGlobalPropertyObject("some.property")).thenReturn(null).thenAnswer(invocation -> {
			cache1.evict("some.property");
			return null;
		});

		fill(cache2, "some.property");

		assertFalse(nativeCache(node2).containsKey("some.property"));
	}

	@Test
	public void get_shouldOnlyDiscardAFillOnTheNodeThatMadeIt() {
		givenProperty("some.property", "value");
		fill(cache2, "some.property");

		// node1's fill writes its entry and then finds that an unrelated eviction replaced the token
		EvictOnCreate listener = new EvictOnCreate("some.property", () -> cache1.evict("other.property"));
		nativeCache(node1).addListener(listener);
		try {
			fill(cache1, "some.property");
		} finally {
			nativeCache(node1).removeListener(listener);
		}

		assertTrue(listener.fired);
		assertFalse(nativeCache(node1).containsKey("some.property"));
		assertTrue(nativeCache(node2).containsKey("some.property"));
	}

	private static void fill(GlobalPropertyCache cache, String propertyName) {
		cache.get(propertyName);
		cache.awaitFills();
	}

	private static SpringEmbeddedCacheManager startNode(String name) throws Exception {
		ConfigurationBuilderHolder holder = new ParserRegistry().parseFile("infinispan-api.xml");
		holder.getGlobalConfigurationBuilder().transport().clusterName("global-property-cache-test").nodeName(name)
		        .transport(new JGroupsTransport(new JChannel("org/openmrs/api/cache/jgroups-in-jvm.xml")));

		DefaultCacheManager cacheManager = new DefaultCacheManager(holder, true);
		cacheManager.defineConfiguration(GlobalPropertyCache.CACHE_NAME, new ConfigurationBuilder()
		        .read(cacheManager.getCacheConfiguration("global-properties")).template(false).build());
		return new SpringEmbeddedCacheManager(cacheManager);
	}

	/** Runs an action on the writing thread once the given key has been written on this node. */
	@Listener(sync = true)
	public static class EvictOnCreate {

		private final String key;

		private final Runnable action;

		private volatile boolean fired;

		EvictOnCreate(String key, Runnable action) {
			this.key = key;
			this.action = action;
		}

		@CacheEntryCreated
		public void created(CacheEntryCreatedEvent<Object, Object> event) {
			if (!event.isPre() && key.equals(event.getKey()) && !fired) {
				fired = true;
				action.run();
			}
		}
	}

	@SuppressWarnings("unchecked")
	private static Cache<Object, Object> nativeCache(SpringEmbeddedCacheManager node) {
		return (Cache<Object, Object>) node.getCache(GlobalPropertyCache.CACHE_NAME).getNativeCache();
	}

	private static AdministrationDAO mockDao() {
		return mock(AdministrationDAO.class);
	}

	private void givenProperty(String name, String value) {
		GlobalProperty property = new GlobalProperty(name, value);
		for (AdministrationDAO dao : new AdministrationDAO[] { dao1, dao2 }) {
			when(dao.getGlobalPropertyObject(name)).thenReturn(property);
			when(dao.getStoredGlobalPropertyName(name)).thenReturn(name);
		}
	}
}
