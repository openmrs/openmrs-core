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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BooleanSupplier;
import java.util.function.Function;

import org.infinispan.AdvancedCache;
import org.infinispan.Cache;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.configuration.parsing.ConfigurationBuilderHolder;
import org.infinispan.configuration.parsing.ParserRegistry;
import org.infinispan.context.Flag;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.spring.common.provider.SpringCache;
import org.infinispan.spring.embedded.provider.SpringEmbeddedCacheManager;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.openmrs.Privilege;
import org.openmrs.Role;
import org.openmrs.api.db.DAOException;
import org.openmrs.api.db.UserDAO;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionSynchronizationUtils;
import org.springframework.transaction.support.TransactionTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests how {@link RolePrivilegeCache} loads, caches and evicts roles, using a local cache manager
 * built from {@code infinispan-api-local.xml}.
 */
public class RolePrivilegeCacheLoadTest {

	private static ExternalReadSpringCacheManager cacheManager;

	private ExecutorService executor;

	private UserDAO dao;

	@BeforeAll
	public static void startCacheManager() throws Exception {
		ConfigurationBuilderHolder holder = new ParserRegistry().parseFile("infinispan-api-local.xml");
		DefaultCacheManager nativeCacheManager = new DefaultCacheManager(holder, true);
		nativeCacheManager.defineConfiguration(RolePrivilegeCache.CACHE_NAME, new ConfigurationBuilder()
		        .read(nativeCacheManager.getCacheConfiguration("role-privileges")).template(false).build());
		cacheManager = new ExternalReadSpringCacheManager(nativeCacheManager, CacheConfig.EXTERNAL_READ_CACHES);
	}

	@AfterAll
	public static void stopCacheManager() {
		cacheManager.stop();
	}

	@BeforeEach
	public void setUp() {
		executor = Executors.newCachedThreadPool();
		dao = mock(UserDAO.class);
	}

	@AfterEach
	public void tearDown() {
		executor.shutdownNow();
		nativeCache().clear();
		if (TransactionSynchronizationManager.isSynchronizationActive()) {
			TransactionSynchronizationManager.clearSynchronization();
		}
	}

	@Test
	public void getRolePrivileges_shouldCacheTheLoadedClosure() {
		RolePrivilegeCache cache = newCache(name -> role("Clerk", "View Patients"));

		RolePrivileges resolved = cache.getRolePrivileges(new Role("Clerk"));
		cache.awaitLoads();

		assertTrue(resolved.containsPrivilege("View Patients"));
		assertEquals(resolved, nativeCache().get("clerk"));
	}

	@Test
	public void getRolePrivileges_shouldResolveFromTheLoadedRoleNotTheSuppliedOne() {
		RolePrivilegeCache cache = newCache(name -> role("Clerk", "View Patients"));

		RolePrivileges resolved = cache.getRolePrivileges(role("Clerk", "Phantom Privilege"));

		assertTrue(resolved.containsPrivilege("View Patients"));
		assertFalse(resolved.containsPrivilege("Phantom Privilege"));
	}

	@Test
	public void getRolePrivileges_shouldReadARoleEvictedDuringItsLoadThroughTheCallersSession() {
		// the load read the role before the change that caused the eviction, so it must not be used
		RolePrivilegeCache[] holder = new RolePrivilegeCache[1];
		holder[0] = newCache(name -> {
			holder[0].clear();
			return role("Clerk", "Revoked Privilege");
		});
		when(dao.getRole("Clerk")).thenReturn(role("Clerk", "View Patients"));

		RolePrivileges resolved = holder[0].getRolePrivileges(new Role("Clerk"));
		holder[0].awaitLoads();

		assertTrue(resolved.containsPrivilege("View Patients"));
		assertFalse(resolved.containsPrivilege("Revoked Privilege"));
		assertFalse(nativeCache().containsKey("clerk"));
	}

	@Test
	public void getRolePrivileges_shouldNotCacheALoadWhoseGenerationTokenDisappeared() {
		// the token can expire or be evicted for space; that must read as an eviction
		RolePrivilegeCache cache = newCache(name -> {
			nativeCache().remove(CacheInvalidation.GENERATION);
			return role("Clerk", "View Patients");
		});
		when(dao.getRole("Clerk")).thenReturn(role("Clerk", "View Patients"));

		RolePrivileges resolved = cache.getRolePrivileges(new Role("Clerk"));
		cache.awaitLoads();

		assertTrue(resolved.containsPrivilege("View Patients"));
		assertFalse(nativeCache().containsKey("clerk"));
	}

	@Test
	public void getRolePrivileges_shouldNotJoinALoadThatStartedBeforeAnEviction() throws Exception {
		CountDownLatch firstLoadStarted = new CountDownLatch(1);
		CountDownLatch releaseFirstLoad = new CountDownLatch(1);
		AtomicInteger loadCount = new AtomicInteger();
		RolePrivilegeCache cache = newCache(name -> {
			if (loadCount.incrementAndGet() == 1) {
				firstLoadStarted.countDown();
				await(releaseFirstLoad);
				return role("Clerk", "Before Privilege");
			}
			return role("Clerk", "After Privilege");
		});
		// the first caller waited across the eviction, so it reads the role through its own session
		when(dao.getRole("Clerk")).thenReturn(role("Clerk", "After Privilege"));

		CompletableFuture<RolePrivileges> first = CompletableFuture
		        .supplyAsync(() -> cache.getRolePrivileges(new Role("Clerk")), executor);
		assertTrue(firstLoadStarted.await(10, TimeUnit.SECONDS));

		cache.clear();
		RolePrivileges second = cache.getRolePrivileges(new Role("Clerk"));

		releaseFirstLoad.countDown();
		assertTrue(first.get(10, TimeUnit.SECONDS).containsPrivilege("After Privilege"));
		cache.awaitLoads();

		assertEquals(2, loadCount.get());
		assertTrue(second.containsPrivilege("After Privilege"));
		assertEquals(second, nativeCache().get("clerk"));
	}

	@Test
	public void getRolePrivileges_shouldShareALoadBetweenConcurrentMisses() throws Exception {
		CountDownLatch releaseLoad = new CountDownLatch(1);
		AtomicInteger loadCount = new AtomicInteger();
		RolePrivilegeCache cache = newCache(name -> {
			loadCount.incrementAndGet();
			await(releaseLoad);
			return role("Clerk", "View Patients");
		});

		CompletableFuture<RolePrivileges> first = CompletableFuture
		        .supplyAsync(() -> cache.getRolePrivileges(new Role("Clerk")), executor);
		CompletableFuture<RolePrivileges> second = CompletableFuture
		        .supplyAsync(() -> cache.getRolePrivileges(new Role("Clerk")), executor);
		waitUntil(() -> loadCount.get() > 0);
		// give the second miss time to join the load; if it is late it finds the result cached instead
		Thread.sleep(100);
		releaseLoad.countDown();

		assertTrue(first.get(10, TimeUnit.SECONDS).containsPrivilege("View Patients"));
		assertTrue(second.get(10, TimeUnit.SECONDS).containsPrivilege("View Patients"));
		assertEquals(1, loadCount.get());
	}

	@Test
	public void getRolePrivileges_shouldReadTheRoleThroughTheCallersSessionWhenTheLoadFails() {
		RolePrivilegeCache cache = newCache(name -> {
			throw new DAOException("no connection for the daemon");
		});
		when(dao.getRole("Clerk")).thenReturn(role("Clerk", "View Patients"));

		RolePrivileges resolved = cache.getRolePrivileges(role("Clerk", "Stale Privilege"));
		cache.awaitLoads();

		assertTrue(resolved.containsPrivilege("View Patients"));
		assertFalse(resolved.containsPrivilege("Stale Privilege"));
		assertFalse(nativeCache().containsKey("clerk"));
	}

	@Test
	public void getRolePrivileges_shouldReadTheRoleThroughTheCallersSessionWhenALoadCannotStart() {
		RolePrivilegeCache cache = new RolePrivilegeCache(cacheManager, dao, task -> {
			throw new IllegalStateException("no threads");
		}, name -> role("Clerk", "Daemon Privilege"));
		when(dao.getRole("Clerk")).thenReturn(role("Clerk", "View Patients"));

		assertTrue(cache.getRolePrivileges(new Role("Clerk")).containsPrivilege("View Patients"));
	}

	@Test
	public void getRolePrivileges_shouldReadTheRoleThroughTheCallersSessionWhenTheLoadsTaskEndsWithoutRunningIt() {
		Function<String, Role> loader = mockLoader();
		RolePrivilegeCache cache = new RolePrivilegeCache(cacheManager, dao,
		        task -> CompletableFuture.failedFuture(new IllegalStateException("no session")), loader);
		when(dao.getRole("Clerk")).thenReturn(role("Clerk", "View Patients"));

		assertTrue(cache.getRolePrivileges(new Role("Clerk")).containsPrivilege("View Patients"));
		assertTrue(cache.getRolePrivileges(new Role("Clerk")).containsPrivilege("View Patients"));
		cache.awaitLoads();

		verify(loader, never()).apply("Clerk");
		assertFalse(nativeCache().containsKey("clerk"));
	}

	@Test
	public void getRolePrivileges_shouldGrantNothingWhenTheCallersSessionCannotReadTheRoleEither() {
		RolePrivilegeCache cache = newCache(name -> {
			throw new DAOException("no connection for the daemon");
		});
		when(dao.getRole("Clerk")).thenThrow(new IllegalStateException("no session"));

		RolePrivileges resolved = cache.getRolePrivileges(role("Clerk", "Stale"));
		cache.awaitLoads();

		assertFalse(resolved.containsPrivilege("Stale"));
		assertFalse(resolved.grantsSuperuser());
		assertFalse(nativeCache().containsKey("clerk"));
	}

	@Test
	public void getRolePrivileges_shouldReadTheRoleThroughTheCallersSessionWhenInterrupted() {
		CountDownLatch releaseLoad = new CountDownLatch(1);
		RolePrivilegeCache cache = newCache(name -> {
			await(releaseLoad);
			return role("Clerk", "Daemon Privilege");
		});
		when(dao.getRole("Clerk")).thenReturn(role("Clerk", "View Patients"));

		Thread.currentThread().interrupt();
		try {
			assertTrue(cache.getRolePrivileges(new Role("Clerk")).containsPrivilege("View Patients"));
			assertTrue(Thread.currentThread().isInterrupted());
		} finally {
			Thread.interrupted();
			releaseLoad.countDown();
		}
	}

	@Test
	public void getRolePrivileges_shouldGrantNothingForARoleThatIsNotInTheDatabase() {
		RolePrivilegeCache cache = newCache(name -> null);

		RolePrivileges resolved = cache.getRolePrivileges(role("Clerk", "Ghost Privilege"));

		assertFalse(resolved.containsPrivilege("Ghost Privilege"));
		assertFalse(resolved.grantsSuperuser());
	}

	@Test
	public void getRolePrivileges_shouldResolveThroughTheCallersSessionAfterAClearInTheSameTransaction() {
		Function<String, Role> loader = mockLoader();
		RolePrivilegeCache cache = newCache(loader);
		when(dao.getRole("Clerk")).thenReturn(role("Clerk", "Uncommitted Privilege"));

		TransactionSynchronizationManager.initSynchronization();
		cache.clear();
		nativeCache().put("clerk", new RolePrivileges(Collections.singleton("Stale Privilege"), false));
		RolePrivileges resolved = cache.getRolePrivileges(new Role("Clerk"));

		assertTrue(resolved.containsPrivilege("Uncommitted Privilege"));
		verify(loader, never()).apply("Clerk");
	}

	@Test
	public void clear_shouldEvictWhenANestedNewTransactionCompletes() {
		RolePrivilegeCache cache = newCache(name -> role("Clerk", "View Patients"));
		when(dao.getRole("Clerk")).thenReturn(role("Clerk", "Uncommitted Privilege"));
		TransactionTemplate outer = new TransactionTemplate(new NoOpTransactionManager());
		TransactionTemplate inner = new TransactionTemplate(new NoOpTransactionManager());
		inner.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

		outer.executeWithoutResult(outerStatus -> {
			cache.clear();
			inner.executeWithoutResult(innerStatus -> {
				cache.clear();
				// a load that read the role before the inner transaction's change committed
				nativeCache().put("clerk", new RolePrivileges(Collections.singleton("Stale Privilege"), false));
			});

			assertFalse(nativeCache().containsKey("clerk"));
			// the outer transaction still bypasses the cache once the inner one has completed
			assertTrue(cache.getRolePrivileges(new Role("Clerk")).containsPrivilege("Uncommitted Privilege"));
		});

		assertFalse(TransactionSynchronizationManager.hasResource(cache));
	}

	@Test
	public void clear_shouldRegisterOneCompletionEvictionPerTransaction() {
		RolePrivilegeCache cache = newCache(name -> role("Clerk", "View Patients"));
		TransactionSynchronizationManager.initSynchronization();

		cache.clear();
		cache.clear();
		cache.clear();

		assertEquals(1, TransactionSynchronizationManager.getSynchronizations().size());
	}

	@Test
	public void clear_shouldLeaveTheCacheForOtherTransactionsUntilTheTransactionCompletes() {
		RolePrivilegeCache cache = newCache(name -> role("Clerk", "View Patients"));
		RolePrivileges committed = new RolePrivileges(Collections.singleton("View Patients"), false);
		nativeCache().put("clerk", committed);
		TransactionSynchronizationManager.initSynchronization();

		cache.clear();

		assertEquals(committed, nativeCache().get("clerk"));
		complete(TransactionSynchronization.STATUS_COMMITTED);
		assertFalse(nativeCache().containsKey("clerk"));
	}

	@Test
	public void clear_shouldClearThisNodeOnlyWhenTheEvictionCannotReachEveryNode() {
		Cache<Object, Object> failing = mockNativeCache();
		AdvancedCache<Object, Object> local = mockAdvancedCache();
		when(failing.put(eq(CacheInvalidation.GENERATION), any())).thenThrow(new IllegalStateException("partitioned"));
		when(failing.getAdvancedCache()).thenReturn(local);
		when(local.withFlags(Flag.CACHE_MODE_LOCAL)).thenReturn(local);
		RolePrivilegeCache cache = new RolePrivilegeCache(cacheManagerFor(failing), dao, executor::submit,
		        name -> role("Clerk", "View Patients"));

		cache.clear();

		verify(local).clear();
	}

	@Test
	public void clear_shouldClearThisNodeOnlyWhenTheEvictionAtCompletionCannotReachEveryNode() {
		Cache<Object, Object> failing = mockNativeCache();
		AdvancedCache<Object, Object> local = mockAdvancedCache();
		when(failing.put(eq(CacheInvalidation.GENERATION), any())).thenThrow(new IllegalStateException("partitioned"));
		when(failing.getAdvancedCache()).thenReturn(local);
		when(local.withFlags(Flag.CACHE_MODE_LOCAL)).thenReturn(local);
		RolePrivilegeCache cache = new RolePrivilegeCache(cacheManagerFor(failing), dao, executor::submit,
		        name -> role("Clerk", "View Patients"));
		TransactionSynchronizationManager.initSynchronization();
		cache.clear();

		complete(TransactionSynchronization.STATUS_COMMITTED);

		verify(local).clear();
		assertFalse(TransactionSynchronizationManager.hasResource(cache));
	}

	@Test
	public void destroy_shouldStopListeningForPartitionMerges() {
		SpringEmbeddedCacheManager springCacheManager = cacheManagerFor(mockNativeCache());
		RolePrivilegeCache cache = new RolePrivilegeCache(springCacheManager, dao, executor::submit,
		        name -> role("Clerk", "View Patients"));
		ArgumentCaptor<Object> listener = ArgumentCaptor.forClass(Object.class);
		verify(springCacheManager.getNativeCacheManager()).addListener(listener.capture());

		cache.destroy();

		assertTrue(listener.getValue() instanceof CacheInvalidation.PartitionMergeListener);
		verify(springCacheManager.getNativeCacheManager()).removeListener(listener.getValue());
	}

	@Test
	public void clear_shouldEvictWhenTheTransactionCommits() {
		assertEvictsOnCompletion(TransactionSynchronization.STATUS_COMMITTED);
	}

	@Test
	public void clear_shouldEvictWhenTheTransactionRollsBack() {
		assertEvictsOnCompletion(TransactionSynchronization.STATUS_ROLLED_BACK);
	}

	@Test
	public void clear_shouldStopBypassingTheCacheWhenTheTransactionCompletes() {
		RolePrivilegeCache cache = newCache(name -> role("Clerk", "View Patients"));
		TransactionSynchronizationManager.initSynchronization();
		cache.clear();

		complete(TransactionSynchronization.STATUS_COMMITTED);
		cache.getRolePrivileges(new Role("Clerk"));
		cache.awaitLoads();

		assertTrue(nativeCache().containsKey("clerk"));
		verify(dao, never()).getRole("Clerk");
	}

	private void assertEvictsOnCompletion(int status) {
		RolePrivilegeCache cache = newCache(name -> role("Clerk", "View Patients"));
		TransactionSynchronizationManager.initSynchronization();
		cache.clear();
		// a load that read the role before the transaction's change committed
		nativeCache().put("clerk", new RolePrivileges(Collections.singleton("Stale Privilege"), false));

		complete(status);

		assertFalse(nativeCache().containsKey("clerk"));
	}

	private static void complete(int status) {
		TransactionSynchronizationUtils.invokeAfterCompletion(TransactionSynchronizationManager.getSynchronizations(),
		    status);
		TransactionSynchronizationManager.clearSynchronization();
	}

	@SuppressWarnings("unchecked")
	private static Cache<Object, Object> mockNativeCache() {
		return mock(Cache.class);
	}

	@SuppressWarnings("unchecked")
	private static AdvancedCache<Object, Object> mockAdvancedCache() {
		return mock(AdvancedCache.class);
	}

	/** A cache manager whose role privilege cache is backed by <code>nativeCache</code>. */
	private static SpringEmbeddedCacheManager cacheManagerFor(Cache<Object, Object> nativeCache) {
		SpringEmbeddedCacheManager springCacheManager = mock(SpringEmbeddedCacheManager.class);
		when(springCacheManager.getNativeCacheManager()).thenReturn(mock(EmbeddedCacheManager.class));
		SpringCache springCache = mock(SpringCache.class);
		doReturn(nativeCache).when(springCache).getNativeCache();
		when(springCacheManager.getCache(RolePrivilegeCache.CACHE_NAME)).thenReturn(springCache);
		return springCacheManager;
	}

	private RolePrivilegeCache newCache(Function<String, Role> loader) {
		return new RolePrivilegeCache(cacheManager, dao, executor::submit, loader);
	}

	@SuppressWarnings("unchecked")
	private static Function<String, Role> mockLoader() {
		return mock(Function.class);
	}

	@SuppressWarnings("unchecked")
	private static Cache<Object, Object> nativeCache() {
		return (Cache<Object, Object>) cacheManager.getCache(RolePrivilegeCache.CACHE_NAME).getNativeCache();
	}

	private static Role role(String name, String privilege) {
		Role role = new Role(name);
		role.addPrivilege(new Privilege(privilege));
		return role;
	}

	private static void await(CountDownLatch latch) {
		try {
			if (!latch.await(10, TimeUnit.SECONDS)) {
				throw new IllegalStateException("Timed out waiting for the test to release the load");
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException(e);
		}
	}

	private static void waitUntil(BooleanSupplier condition) throws InterruptedException {
		long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
		while (!condition.getAsBoolean()) {
			if (System.nanoTime() > deadline) {
				throw new IllegalStateException("Timed out waiting for the condition");
			}
			Thread.sleep(10);
		}
	}

	/**
	 * A transaction manager that does nothing but track whether a transaction is active on this thread,
	 * so that Spring runs its real synchronization, suspension and resumption around a nested
	 * transaction.
	 */
	private static final class NoOpTransactionManager extends AbstractPlatformTransactionManager {

		private static final ThreadLocal<Object> ACTIVE = new ThreadLocal<>();

		@Override
		protected Object doGetTransaction() {
			return new Object[] { ACTIVE.get() };
		}

		@Override
		protected boolean isExistingTransaction(Object transaction) {
			return ((Object[]) transaction)[0] != null;
		}

		@Override
		protected void doBegin(Object transaction, TransactionDefinition definition) {
			ACTIVE.set(transaction);
		}

		@Override
		protected Object doSuspend(Object transaction) {
			Object suspended = ACTIVE.get();
			ACTIVE.remove();
			return suspended;
		}

		@Override
		protected void doResume(Object transaction, Object suspendedResources) {
			ACTIVE.set(suspendedResources);
		}

		@Override
		protected void doCommit(DefaultTransactionStatus status) {
		}

		@Override
		protected void doRollback(DefaultTransactionStatus status) {
		}

		@Override
		protected void doCleanupAfterCompletion(Object transaction) {
			ACTIVE.remove();
		}
	}
}
