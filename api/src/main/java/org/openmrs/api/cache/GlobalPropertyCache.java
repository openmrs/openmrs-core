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

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Semaphore;
import java.util.function.Consumer;

import org.infinispan.Cache;
import org.infinispan.context.Flag;
import org.infinispan.notifications.Listener;
import org.infinispan.notifications.cachemanagerlistener.annotation.Merged;
import org.infinispan.notifications.cachemanagerlistener.event.MergeEvent;
import org.infinispan.spring.embedded.provider.SpringEmbeddedCacheManager;
import org.openmrs.GlobalProperty;
import org.openmrs.api.context.Daemon;
import org.openmrs.api.db.AdministrationDAO;
import org.openmrs.util.OpenmrsConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionManager;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Caches the global property lookups made through
 * {@link org.openmrs.api.AdministrationService#getGlobalProperty(String)}, including lookups of
 * properties that do not exist. Values are immutable {@link Entry} snapshots rather than
 * {@link GlobalProperty} entities, which are mutable and lazily load their privileges.
 * <p>
 * Entries are keyed by the lower-cased name, so a write evicts one key whatever spelling it was
 * read with, and each entry is only served for the exact name it was loaded for, since
 * {@link AdministrationDAO#getGlobalPropertyObject(String)} may treat other spellings differently.
 * A property stored under a name that does not lower-case to the key it was read with is not
 * cached, and an insert clears the whole cache, since it can change what a lookup of an equivalent
 * spelling returns.
 * <p>
 * The caller never fills the cache. On a miss, the property is read through the caller's own
 * session, which sees the caller's uncommitted changes, and a fill is started in the background.
 * The fill reads the property in a new transaction on a daemon thread, so it only sees committed
 * data and never touches the caller's transaction. Every eviction replaces a generation token kept
 * in the cache itself, which the fill reads before loading and checks after writing, so an entry is
 * only kept if nothing was evicted in between. In a cluster, replacing the token invalidates it on
 * every node. A token that expires or is evicted for space also reads as an eviction, which only
 * costs an uncached load. If an eviction cannot reach every node, this node clears its own cache
 * instead, and a node clears its cache when a network partition that cut it off heals.
 * <p>
 * Within a transaction, evictions are recorded rather than applied, and applied once when the
 * transaction completes. Until then the transaction reads the properties it has written, or every
 * property after a {@link #clear()}, without the cache, so it always reads its own writes, while
 * other transactions keep reading the committed values. After a write through the API commits, the
 * property is therefore never served from before that write.
 * <p>
 * {@link org.openmrs.api.AdministrationService} evicts a property as soon as it is written, and
 * {@link org.openmrs.api.db.hibernate.GlobalPropertyCacheInterceptor} evicts any property Hibernate
 * flushes, which covers code that changes a loaded {@link GlobalProperty} directly. Such a change
 * is only visible to cache hits once it has been flushed. Changes to the {@code global_property}
 * table made outside Hibernate, for example with SQL, are not detected, but the
 * {@code global-properties} cache template's lifespan limits how long they can be served stale.
 *
 * @since 2.8.10
 */
@Component("globalPropertyCache")
public class GlobalPropertyCache implements ApplicationListener<ContextRefreshedEvent>, DisposableBean {

	private static final Logger log = LoggerFactory.getLogger(GlobalPropertyCache.class);

	public static final String CACHE_NAME = "globalProperties";

	/**
	 * Key of the generation token, which every eviction replaces. The NUL character keeps it from
	 * colliding with a lower-cased property name.
	 */
	static final String GENERATION = "\0generation";

	/**
	 * Recorded in the current transaction's written set by {@link #clear()}, meaning every property.
	 */
	private static final String ALL_PROPERTIES = "\0all";

	static final int MAX_CONCURRENT_FILLS = 4;

	/** Capability token issued by {@link Daemon}, letting fills run on daemon threads. */
	private static volatile Daemon.CallerKey daemonCallerKey;

	private final SpringEmbeddedCacheManager cacheManager;

	private final AdministrationDAO dao;

	private final TransactionTemplate readOnlyTransaction;

	/** Runs a fill on another thread without waiting for it. */
	private final Consumer<Runnable> backgroundRunner;

	/** Fills in progress, by property name, so concurrent misses start a single fill. */
	private final ConcurrentMap<String, CompletableFuture<Void>> fills = new ConcurrentHashMap<>();

	/**
	 * Limits how many fills run at once, since each holds a database connection that callers may be
	 * waiting for. A miss that finds none free skips its fill; a later miss starts one.
	 */
	private final Semaphore fillPermits = new Semaphore(MAX_CONCURRENT_FILLS);

	private final PartitionMergeListener mergeListener = new PartitionMergeListener(this);

	@Autowired
	public GlobalPropertyCache(@Qualifier("apiCacheManager") SpringEmbeddedCacheManager cacheManager, AdministrationDAO dao,
	    @Qualifier("transactionManager") TransactionManager transactionManager) {
		this(cacheManager, dao, (PlatformTransactionManager) transactionManager,
		        task -> Daemon.runNewDaemonTask(task, daemonCallerKey()));
	}

	GlobalPropertyCache(SpringEmbeddedCacheManager cacheManager, AdministrationDAO dao,
	    PlatformTransactionManager transactionManager, Consumer<Runnable> backgroundRunner) {
		this.cacheManager = cacheManager;
		this.dao = dao;
		this.backgroundRunner = backgroundRunner;

		this.readOnlyTransaction = new TransactionTemplate(transactionManager);
		this.readOnlyTransaction.setReadOnly(true);

		cacheManager.getNativeCacheManager().addListener(mergeListener);
	}

	@Override
	public void destroy() {
		cacheManager.getNativeCacheManager().removeListener(mergeListener);
	}

	/**
	 * Receives the {@link Daemon} caller key. Called only by {@link Daemon} during its initialization.
	 *
	 * @param callerKey the caller key issued by {@link Daemon}
	 */
	public static void setDaemonCallerKey(Daemon.CallerKey callerKey) {
		if (callerKey != null && daemonCallerKey == null) {
			daemonCallerKey = callerKey;
		}
	}

	private static Daemon.CallerKey daemonCallerKey() {
		if (daemonCallerKey == null) {
			// Guarantee Daemon has initialized and therefore handed us the key, regardless of the order in
			// which the two classes were first loaded.
			Daemon.ensureInitialized();
		}
		return daemonCallerKey;
	}

	/**
	 * Returns a snapshot of the named property. On a miss it is read through the caller's session and a
	 * fill of the cache is started in the background. Never returns null; a property that does not
	 * exist is returned as {@link Entry#ABSENT}.
	 * <p>
	 * The snapshot does not record whether the current user may view the property, so callers must
	 * check {@link Entry#getViewPrivilege()} on every call.
	 *
	 * @param propertyName the name of the property, not null
	 * @return a snapshot of the property
	 */
	public Entry get(String propertyName) {
		Cache<Object, Object> cache = getCache();
		if (cache == null || propertyName == null || isWrittenInCurrentTransaction(propertyName)) {
			return Entry.of(dao.getGlobalPropertyObject(propertyName));
		}

		Object cached = cache.get(key(propertyName));
		if (cached instanceof CachedEntry && ((CachedEntry) cached).isFor(propertyName)) {
			return ((CachedEntry) cached).entry;
		}

		Entry loaded = Entry.of(dao.getGlobalPropertyObject(propertyName));
		// a fill cannot replace an entry cached for another spelling of the name, and loading may have
		// flushed a write of the property
		if (!(cached instanceof CachedEntry) && !isWrittenInCurrentTransaction(propertyName)) {
			startFill(propertyName);
		}
		return loaded;
	}

	/**
	 * Returns the cached snapshot of the named property without loading it, or null if it is not cached
	 * or the current transaction has written it. Only for tests that must observe the cache.
	 *
	 * @param propertyName the name of the property
	 * @return a snapshot of the property, or null if it is not cached
	 */
	Entry getIfCached(String propertyName) {
		Cache<Object, Object> cache = getCache();
		if (cache == null || propertyName == null || isWrittenInCurrentTransaction(propertyName)) {
			return null;
		}

		Object cached = cache.get(key(propertyName));
		return cached instanceof CachedEntry && ((CachedEntry) cached).isFor(propertyName) ? ((CachedEntry) cached).entry
		        : null;
	}

	/**
	 * Evicts the named property, for example because it has been saved or purged.
	 * <p>
	 * Within a transaction the property is evicted when the transaction completes, whether it commits
	 * or rolls back, and until then the transaction reads the property without the cache. Outside one
	 * it is evicted immediately.
	 *
	 * @param propertyName the name of the property, not null
	 */
	public void evict(String propertyName) {
		if (OpenmrsConstants.GP_CASE_SENSITIVE_DATABASE_STRING_COMPARISON.equalsIgnoreCase(propertyName)) {
			// changes what the DAO returns for every spelling of every name
			clear();
		} else {
			invalidate(key(propertyName));
		}
	}

	/**
	 * Evicts every property. As with {@link #evict(String)}, within a transaction this happens when the
	 * transaction completes, and until then the transaction reads every property without the cache.
	 */
	public void clear() {
		invalidate(ALL_PROPERTIES);
	}

	/**
	 * Clears the cache whenever the application context is refreshed, which happens whenever modules
	 * are started or stopped.
	 */
	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		clear();
	}

	/**
	 * Waits for the fills in progress to finish. Fills only affect what later calls find in the cache,
	 * so this is only needed where a caller must observe a fill, for example in tests.
	 */
	void awaitFills() {
		fills.values().forEach(CompletableFuture::join);
	}

	/**
	 * Evicts every property immediately, even within a transaction, which still reads every property
	 * without the cache until it completes. Only for tests that load data behind the API.
	 */
	void clearNow() {
		Cache<Object, Object> cache = getCache();
		if (cache == null) {
			return;
		}

		evictOrClearLocally(cache, Collections.singleton(ALL_PROPERTIES));
		if (TransactionSynchronizationManager.isSynchronizationActive()) {
			getWrittenInCurrentTransaction(cache).add(ALL_PROPERTIES);
		}
	}

	/**
	 * Forgets which properties the current transaction has written, so that it reads through the cache
	 * again and does not evict them when it completes. Only for tests that load data behind the API but
	 * still need to observe cache hits.
	 */
	void forgetWritesInCurrentTransaction() {
		if (TransactionSynchronizationManager.hasResource(this)) {
			((Set<?>) TransactionSynchronizationManager.getResource(this)).clear();
		}
	}

	/**
	 * Starts a fill unless one is already in progress for the property or {@link #MAX_CONCURRENT_FILLS}
	 * are running. The in-progress marker is published before the fill starts and only that marker is
	 * removed when it finishes, so the fill may run on any thread, including this one.
	 */
	private void startFill(String propertyName) {
		if (!fillPermits.tryAcquire()) {
			return;
		}

		CompletableFuture<Void> marker = new CompletableFuture<>();
		if (fills.putIfAbsent(propertyName, marker) != null) {
			fillPermits.release();
			return;
		}

		try {
			backgroundRunner.accept(() -> {
				try {
					fill(propertyName);
				} catch (RuntimeException e) {
					log.warn("Could not fill the global property cache with {}", propertyName, e);
				} finally {
					finish(propertyName, marker);
				}
			});
		} catch (RuntimeException e) {
			log.warn("Could not start a fill of the global property cache with {}", propertyName, e);
			finish(propertyName, marker);
		}
	}

	private void finish(String propertyName, CompletableFuture<Void> marker) {
		fills.remove(propertyName, marker);
		fillPermits.release();
		marker.complete(null);
	}

	/**
	 * Runs on a background thread: reads the property in a new read-only transaction and caches it
	 * unless an eviction happens in the meantime, or it is stored under a name that does not lower-case
	 * to the same key, whose writes would evict a different key.
	 */
	private void fill(String propertyName) {
		Cache<Object, Object> cache = getCache();
		if (cache == null) {
			return;
		}

		Object loadedAt = currentGeneration(cache);
		readOnlyTransaction.executeWithoutResult(status -> {
			GlobalProperty property = dao.getGlobalPropertyObject(propertyName);
			if (property != null) {
				String storedName = dao.getStoredGlobalPropertyName(propertyName);
				if (storedName == null || !key(storedName).equals(key(propertyName))) {
					return;
				}
			}
			putIfCurrent(cache, key(propertyName), new CachedEntry(propertyName, Entry.of(property)), loadedAt);
		});
	}

	/**
	 * Returns this node's generation token, first creating one if there is none. The token is created
	 * on this node only, since writing it cluster-wide would invalidate the other nodes' tokens.
	 */
	private static Object currentGeneration(Cache<Object, Object> cache) {
		Object generation = cache.get(GENERATION);
		if (generation != null) {
			return generation;
		}

		String created = UUID.randomUUID().toString();
		Object existing = cache.getAdvancedCache().withFlags(Flag.CACHE_MODE_LOCAL).putIfAbsent(GENERATION, created);
		return existing != null ? existing : created;
	}

	/**
	 * Caches <code>value</code> unless an eviction has happened since it was loaded. An eviction
	 * replaces the generation token before removing entries, so if one runs while the value is being
	 * put, the second check either sees it and removes the value, or the eviction removes it. The
	 * removal is local, since the value was only ever written on this node.
	 */
	private static void putIfCurrent(Cache<Object, Object> cache, String key, CachedEntry value, Object loadedAt) {
		if (!loadedAt.equals(cache.get(GENERATION))) {
			return;
		}

		cache.putForExternalRead(key, value);
		if (!loadedAt.equals(cache.get(GENERATION))) {
			cache.getAdvancedCache().withFlags(Flag.CACHE_MODE_LOCAL).remove(key);
		}
	}

	private static String key(String propertyName) {
		return propertyName.toLowerCase(Locale.ROOT);
	}

	/**
	 * Evicts <code>key</code> when the current transaction completes, or immediately outside one. Keys
	 * recorded by one transaction are evicted together.
	 */
	private void invalidate(String key) {
		Cache<Object, Object> cache = getCache();
		if (cache == null) {
			return;
		}

		if (TransactionSynchronizationManager.isSynchronizationActive()) {
			getWrittenInCurrentTransaction(cache).add(key);
		} else {
			evictOrClearLocally(cache, Collections.singleton(key));
		}
	}

	/**
	 * Replaces the generation token and then removes the keys, or every entry if they include
	 * {@link #ALL_PROPERTIES}. The token must be replaced first: a clear does not lock every key, so a
	 * fill could otherwise write behind it and still find its token. Replacing the token with a plain
	 * put invalidates it on every other node before the removals are sent. The removals are sent
	 * together, so evicting several keys costs about one round trip to the other nodes.
	 */
	/**
	 * Evicts the keys on every node or, if that fails, for example because the cluster is partitioned,
	 * clears this node's cache instead, so that at least this node does not serve values from before
	 * the write. Nodes the eviction did not reach may serve them until the lifespan expires, or, if
	 * they were cut off by a partition, until it heals.
	 */
	private static void evictOrClearLocally(Cache<Object, Object> cache, Set<String> keys) {
		try {
			evictNow(cache, keys);
		} catch (RuntimeException e) {
			log.error("Could not evict {} from the global property cache on every node, so clearing it on this node only",
			    keys.contains(ALL_PROPERTIES) ? "every property" : keys, e);
			clearLocally(cache);
		}
	}

	/**
	 * Clears this node's entries, including its generation token, so that fills in progress on this
	 * node are discarded too.
	 */
	private static void clearLocally(Cache<Object, Object> cache) {
		cache.getAdvancedCache().withFlags(Flag.CACHE_MODE_LOCAL).clear();
	}

	private static void evictNow(Cache<Object, Object> cache, Set<String> keys) {
		cache.put(GENERATION, UUID.randomUUID().toString());
		if (keys.contains(ALL_PROPERTIES)) {
			cache.clear();
		} else {
			CompletableFuture.allOf(keys.stream().map(cache::removeAsync).toArray(CompletableFuture[]::new)).join();
		}
	}

	private boolean isWrittenInCurrentTransaction(String propertyName) {
		if (!TransactionSynchronizationManager.hasResource(this)) {
			return false;
		}

		Set<?> written = (Set<?>) TransactionSynchronizationManager.getResource(this);
		return written.contains(ALL_PROPERTIES) || written.contains(key(propertyName));
	}

	/**
	 * Returns the keys the current transaction has written, first arranging for them to be evicted when
	 * it completes. The set is unbound while the transaction is suspended, so a transaction started in
	 * the meantime records and evicts its own writes.
	 */
	@SuppressWarnings("unchecked")
	private Set<String> getWrittenInCurrentTransaction(Cache<Object, Object> cache) {
		if (TransactionSynchronizationManager.hasResource(this)) {
			return (Set<String>) TransactionSynchronizationManager.getResource(this);
		}

		Set<String> written = new HashSet<>();
		TransactionSynchronizationManager.bindResource(this, written);
		TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {

			@Override
			public void suspend() {
				TransactionSynchronizationManager.unbindResource(GlobalPropertyCache.this);
			}

			@Override
			public void resume() {
				TransactionSynchronizationManager.bindResource(GlobalPropertyCache.this, written);
			}

			@Override
			public void afterCompletion(int status) {
				try {
					if (!written.isEmpty()) {
						evictOrClearLocally(cache, written);
					}
				} finally {
					TransactionSynchronizationManager.unbindResourceIfPossible(GlobalPropertyCache.this);
				}
			}
		});
		return written;
	}

	@SuppressWarnings("unchecked")
	private Cache<Object, Object> getCache() {
		org.springframework.cache.Cache cache = cacheManager.getCache(CACHE_NAME);
		return cache == null ? null : (Cache<Object, Object>) cache.getNativeCache();
	}

	/**
	 * Clears this node's cache when a network partition heals, since while it was cut off it missed the
	 * evictions of writes made on the other side. Public only because Infinispan requires listeners to
	 * be.
	 */
	@Listener
	public static final class PartitionMergeListener {

		private final GlobalPropertyCache owner;

		PartitionMergeListener(GlobalPropertyCache owner) {
			this.owner = owner;
		}

		@Merged
		public void merged(MergeEvent event) {
			Cache<Object, Object> cache = owner.getCache();
			if (cache != null) {
				clearLocally(cache);
			}
		}
	}

	/** A cached {@link Entry} and the exact name it was loaded for. */
	static final class CachedEntry implements Serializable {

		private static final long serialVersionUID = 1L;

		private final String propertyName;

		private final Entry entry;

		CachedEntry(String propertyName, Entry entry) {
			this.propertyName = propertyName;
			this.entry = entry;
		}

		boolean isFor(String propertyName) {
			return this.propertyName.equals(propertyName);
		}

		Entry getEntry() {
			return entry;
		}
	}

	/**
	 * An immutable snapshot of a global property's value and view privilege, or {@link #ABSENT} if the
	 * property does not exist.
	 */
	public static final class Entry implements Serializable {

		private static final long serialVersionUID = 1L;

		public static final Entry ABSENT = new Entry(false, null, null);

		private final boolean present;

		private final String value;

		private final String viewPrivilege;

		private Entry(boolean present, String value, String viewPrivilege) {
			this.present = present;
			this.value = value;
			this.viewPrivilege = viewPrivilege;
		}

		/**
		 * @param globalProperty the property to snapshot, or null if it does not exist
		 * @return a snapshot of <code>globalProperty</code>, or {@link #ABSENT} if it is null
		 */
		public static Entry of(GlobalProperty globalProperty) {
			if (globalProperty == null) {
				return ABSENT;
			}

			String viewPrivilege = globalProperty.getViewPrivilege() == null ? null
			        : globalProperty.getViewPrivilege().getPrivilege();
			return new Entry(true, globalProperty.getPropertyValue(), viewPrivilege);
		}

		/**
		 * @return true if the property exists
		 */
		public boolean isPresent() {
			return present;
		}

		/**
		 * @return the property's value, or null if it has none or does not exist
		 */
		public String getValue() {
			return value;
		}

		/**
		 * @return the name of the privilege needed to view the property, or null if none is needed
		 */
		public String getViewPrivilege() {
			return viewPrivilege;
		}
	}
}
