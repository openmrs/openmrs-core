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
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

import org.infinispan.Cache;
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
 * data and never touches the caller's transaction. Evictions go through {@link CacheInvalidation},
 * so a fill only keeps its entry if nothing was evicted, on any node, while it ran.
 * <p>
 * Within a transaction, evictions are applied once, when the transaction completes. Until then the
 * transaction reads the properties it has written, or every property after a {@link #clear()},
 * without the cache, so it always reads its own writes, while other transactions keep reading the
 * committed values. After a write through the API commits, the property is therefore never served
 * from before that write.
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

	static final int MAX_CONCURRENT_FILLS = 4;

	/** Capability token issued by {@link Daemon}, letting fills run on daemon threads. */
	private static volatile Daemon.CallerKey daemonCallerKey;

	private final SpringEmbeddedCacheManager cacheManager;

	private final AdministrationDAO dao;

	private final TransactionTemplate readOnlyTransaction;

	/**
	 * Runs a fill on another thread without waiting for it, returning a future for the thread's task,
	 * or null if there is none. The task may fail without running the fill, for example if a daemon
	 * cannot open a session.
	 */
	private final Function<Runnable, Future<?>> backgroundRunner;

	/** Fills in progress, by property name, so concurrent misses start a single fill. */
	private final ConcurrentMap<String, Fill> fills = new ConcurrentHashMap<>();

	/**
	 * Limits how many fills run at once, since each holds a database connection that callers may be
	 * waiting for. A miss that finds none free skips its fill; a later miss starts one. Each miss first
	 * reclaims the permits of fills whose task ended without running them.
	 */
	private final Semaphore fillPermits = new Semaphore(MAX_CONCURRENT_FILLS);

	private final CacheInvalidation invalidation;

	@Autowired
	public GlobalPropertyCache(@Qualifier("apiCacheManager") SpringEmbeddedCacheManager cacheManager, AdministrationDAO dao,
	    @Qualifier("transactionManager") TransactionManager transactionManager) {
		this(cacheManager, dao, (PlatformTransactionManager) transactionManager,
		        task -> Daemon.runNewDaemonTask(task, daemonCallerKey()));
	}

	GlobalPropertyCache(SpringEmbeddedCacheManager cacheManager, AdministrationDAO dao,
	    PlatformTransactionManager transactionManager, Function<Runnable, Future<?>> backgroundRunner) {
		this.cacheManager = cacheManager;
		this.dao = dao;
		this.backgroundRunner = backgroundRunner;

		this.readOnlyTransaction = new TransactionTemplate(transactionManager);
		this.readOnlyTransaction.setReadOnly(true);

		this.invalidation = new CacheInvalidation("the global property cache", this::getCache,
		        cacheManager.getNativeCacheManager());
	}

	@Override
	public void destroy() {
		invalidation.close();
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
		if (cache == null || propertyName == null || invalidation.isWrittenInCurrentTransaction(key(propertyName))) {
			return Entry.of(dao.getGlobalPropertyObject(propertyName));
		}

		Object cached = cache.get(key(propertyName));
		if (cached instanceof CachedEntry && ((CachedEntry) cached).isFor(propertyName)) {
			return ((CachedEntry) cached).entry;
		}

		Entry loaded = Entry.of(dao.getGlobalPropertyObject(propertyName));
		// a fill cannot replace an entry cached for another spelling of the name, and loading may have
		// flushed a write of the property
		if (!(cached instanceof CachedEntry) && !invalidation.isWrittenInCurrentTransaction(key(propertyName))) {
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
		if (cache == null || propertyName == null || invalidation.isWrittenInCurrentTransaction(key(propertyName))) {
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
			invalidation.invalidate(key(propertyName));
		}
	}

	/**
	 * Evicts every property. As with {@link #evict(String)}, within a transaction this happens when the
	 * transaction completes, and until then the transaction reads every property without the cache.
	 */
	public void clear() {
		invalidation.invalidate(CacheInvalidation.ALL);
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
		fills.forEach((propertyName, fill) -> {
			Future<?> task = fill.task.join();
			if (task != null) {
				try {
					task.get();
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					return;
				} catch (ExecutionException e) {
					finish(propertyName, fill);
				}
			}
			fill.done.join();
		});
	}

	/**
	 * Evicts every property immediately, even within a transaction, which still reads every property
	 * without the cache until it completes. Only for tests that load data behind the API.
	 */
	void clearNow() {
		invalidation.clearNow();
	}

	/**
	 * Forgets which properties the current transaction has written, so that it reads through the cache
	 * again and does not evict them when it completes. Only for tests that load data behind the API but
	 * still need to observe cache hits.
	 */
	void forgetWritesInCurrentTransaction() {
		invalidation.forgetWritesInCurrentTransaction();
	}

	/**
	 * Starts a fill unless one is already in progress for the property or {@link #MAX_CONCURRENT_FILLS}
	 * are running. The in-progress marker is published before the fill starts and only that marker is
	 * removed when it finishes, so the fill may run on any thread, including this one.
	 */
	private void startFill(String propertyName) {
		reclaimFillsThatNeverRan();
		if (!fillPermits.tryAcquire()) {
			return;
		}

		Fill marker = new Fill();
		if (fills.putIfAbsent(propertyName, marker) != null) {
			fillPermits.release();
			return;
		}

		Future<?> task = null;
		try {
			task = backgroundRunner.apply(() -> {
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
		} finally {
			marker.task.complete(task);
		}
	}

	/**
	 * Finishes the fills whose task has ended, which only leaves a fill unfinished if the task ended
	 * without running it. There are at most {@link #MAX_CONCURRENT_FILLS}.
	 */
	private void reclaimFillsThatNeverRan() {
		fills.forEach((propertyName, fill) -> {
			Future<?> task = fill.task.getNow(null);
			if (task != null && task.isDone()) {
				finish(propertyName, fill);
			}
		});
	}

	/** Releases a fill's marker and permit. Only the first call for a fill has any effect. */
	private void finish(String propertyName, Fill fill) {
		if (fill.finished.compareAndSet(false, true)) {
			fills.remove(propertyName, fill);
			fillPermits.release();
			fill.done.complete(null);
		}
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

		Object loadedAt = CacheInvalidation.currentGeneration(cache);
		readOnlyTransaction.executeWithoutResult(status -> {
			GlobalProperty property = dao.getGlobalPropertyObject(propertyName);
			if (property != null) {
				String storedName = dao.getStoredGlobalPropertyName(propertyName);
				if (storedName == null || !key(storedName).equals(key(propertyName))) {
					return;
				}
			}
			CacheInvalidation.putIfCurrent(cache, key(propertyName), new CachedEntry(propertyName, Entry.of(property)),
			    loadedAt);
		});
	}

	private static String key(String propertyName) {
		return propertyName.toLowerCase(Locale.ROOT);
	}

	@SuppressWarnings("unchecked")
	private Cache<Object, Object> getCache() {
		org.springframework.cache.Cache cache = cacheManager.getCache(CACHE_NAME);
		return cache == null ? null : (Cache<Object, Object>) cache.getNativeCache();
	}

	/** A fill in progress. */
	private static final class Fill {

		private final AtomicBoolean finished = new AtomicBoolean();

		/** Completed once the fill has finished. */
		private final CompletableFuture<Void> done = new CompletableFuture<>();

		/** The thread's task running the fill, completed as soon as it has been started. */
		private final CompletableFuture<Future<?>> task = new CompletableFuture<>();
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
