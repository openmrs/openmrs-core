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

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Function;

import org.infinispan.Cache;
import org.infinispan.spring.embedded.provider.SpringEmbeddedCacheManager;
import org.openmrs.Privilege;
import org.openmrs.Role;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.Daemon;
import org.openmrs.api.db.UserDAO;
import org.openmrs.util.RoleConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

/**
 * Per-role cache of flattened privilege resolutions, so privilege and superuser checks need not
 * re-expand a user's role graph on every call. The cached value for a role is an immutable
 * {@link RolePrivileges} covering that role and its entire inherited closure.
 * <p>
 * On a miss the closure is computed from a freshly loaded copy of the role, never the
 * caller-supplied instance, which may be a stale role graph held by a long-lived
 * {@code UserContext}. The role is loaded in a daemon thread, because loading it through the
 * secured {@code UserService} requires {@code Get Roles}, the very kind of check being resolved.
 * The caller waits for the load, and concurrent misses for the same role share one. A role absent
 * from the database grants nothing.
 * <p>
 * If the load fails, the caller is interrupted while waiting for it, or the cache was evicted while
 * the caller waited, so that the load may have read the role from before a change, the role is
 * instead read once through the caller's own session, without caching it. That read sees committed
 * data and the caller's own changes, but a role the session has already loaded is served as the
 * session first loaded it. If that read fails too, the role grants nothing, so a privilege check
 * denies rather than throws.
 * <p>
 * The daemon only sees committed data. Evictions go through {@link CacheInvalidation}, so a load
 * only keeps its entry if nothing was evicted, on any node, while it ran, and a miss never waits on
 * a load that started before the latest eviction.
 * <p>
 * Within a transaction, the eviction is applied once, when the transaction completes. Until then
 * the transaction resolves roles through its own session, without the cache, so that it sees its
 * own changes, while other transactions keep reading the committed ones. Once a transaction that
 * changed a role or privilege has completed, privileges are therefore never served from before that
 * change, except on a node that missed the eviction while cut off from the cluster.
 * <p>
 * {@link org.openmrs.api.UserService} clears the cache when it saves or purges a role or privilege,
 * and {@link org.openmrs.api.db.hibernate.RolePrivilegeCacheInterceptor} clears it whenever
 * Hibernate flushes a change to one, which covers code that changes a loaded {@link Role} or
 * {@link Privilege} directly. Changes to the tables made outside Hibernate, for example with SQL,
 * are not detected, but the {@code role-privileges} cache template's lifespan limits how long they
 * can be served stale.
 *
 * @since 2.8.9
 */
@Component("rolePrivilegeCache")
public class RolePrivilegeCache implements ApplicationListener<ContextRefreshedEvent>, DisposableBean {

	private static final Logger log = LoggerFactory.getLogger(RolePrivilegeCache.class);

	public static final String CACHE_NAME = "rolePrivileges";

	/**
	 * Capability token issued by {@link Daemon}, letting this component launch a daemon thread to load
	 * roles with full trust.
	 */
	private static volatile Daemon.CallerKey daemonCallerKey;

	private final SpringEmbeddedCacheManager cacheManager;

	private final UserDAO dao;

	/**
	 * Runs a load on another thread, returning a future for the thread's task, or null if there is
	 * none. The task may fail without running the load, for example if a daemon cannot open a session.
	 */
	private final Function<Runnable, Future<?>> backgroundRunner;

	/** Loads a role by name on the background thread, seeing only committed data. */
	private final Function<String, Role> committedRoleLoader;

	/** Loads in progress, by normalized role name, so concurrent misses share a single load. */
	private final ConcurrentMap<String, Load> loads = new ConcurrentHashMap<>();

	private final CacheInvalidation invalidation;

	@Autowired
	public RolePrivilegeCache(@Qualifier("apiCacheManager") SpringEmbeddedCacheManager cacheManager, UserDAO dao) {
		// daemon threads skip authorization, so the secured UserService can load the role
		this(cacheManager, dao, task -> Daemon.runNewDaemonTask(task, daemonCallerKey()),
		        roleName -> Context.getUserService().getRole(roleName));
	}

	RolePrivilegeCache(SpringEmbeddedCacheManager cacheManager, UserDAO dao, Function<Runnable, Future<?>> backgroundRunner,
	    Function<String, Role> committedRoleLoader) {
		this.cacheManager = cacheManager;
		this.dao = dao;
		this.backgroundRunner = backgroundRunner;
		this.committedRoleLoader = committedRoleLoader;

		this.invalidation = new CacheInvalidation("the role privilege cache", this::getCache,
		        cacheManager.getNativeCacheManager());
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
	 * Returns the flattened privileges for the given role, loading and caching them on a miss. Never
	 * returns {@code null} and never throws; a role that cannot be loaded grants nothing.
	 *
	 * @param role the directly assigned role to resolve
	 * @return the flattened privilege closure for the role
	 */
	public RolePrivileges getRolePrivileges(Role role) {
		if (role == null || role.getRole() == null) {
			return new RolePrivileges(new HashSet<>(), false);
		}

		if (invalidation.isWrittenInCurrentTransaction(null)) {
			return resolveInCurrentSession(role.getRole(), null);
		}

		Cache<Object, Object> cache = getCache();
		String key = RolePrivileges.normalize(role.getRole());
		if (cache != null) {
			Object cached = cache.get(key);
			if (cached instanceof RolePrivileges) {
				return (RolePrivileges) cached;
			}
		}

		Load load = startOrJoinLoad(key, role.getRole(), cache);
		RolePrivileges loaded;
		try {
			loaded = await(key, load);
		} catch (APIException e) {
			// the daemon may have failed for want of a connection the caller already holds, or the caller
			// may have been interrupted
			log.warn("Could not load the privileges of role {}; reading it through the caller's session", key, e);
			return resolveInCurrentSession(role.getRole(), e);
		}

		if (cache != null && !CacheInvalidation.isCurrent(cache, load.generation)) {
			return resolveInCurrentSession(role.getRole(), null);
		}
		return loaded;
	}

	/**
	 * Evicts every role. Must be called whenever a role or privilege is saved or purged; closures span
	 * inherited roles, so a change to one role can affect any entry.
	 * <p>
	 * Within a transaction the cache is cleared once, when the transaction completes, whether it
	 * commits or rolls back, and until then the transaction resolves roles without the cache. Outside
	 * one it is cleared immediately.
	 */
	public void clear() {
		invalidation.invalidate(CacheInvalidation.ALL);
	}

	@Override
	public void destroy() {
		invalidation.close();
	}

	/**
	 * Clears the cache whenever the application context is refreshed, ensuring role graph changes
	 * applied outside the API before or during startup are not served stale.
	 */
	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		clear();
	}

	/**
	 * Flattens a role and its transitively inherited roles into an immutable {@link RolePrivileges},
	 * with a visited set guarding against inheritance cycles. A <code>null</code> role grants nothing.
	 * <p>
	 * Resolves against the passed-in instance with <em>no freshness guarantee</em>, so it must not
	 * drive a security decision on a possibly stale role.
	 *
	 * @param role the role to flatten
	 * @return the flattened privilege closure
	 */
	public static RolePrivileges computeRolePrivileges(Role role) {
		Set<String> privileges = new HashSet<>();
		Set<String> visited = new HashSet<>();
		boolean grantsSuperuser = collect(role, privileges, visited);
		return new RolePrivileges(privileges, grantsSuperuser);
	}

	/**
	 * Depth-first walk over a role and its inherited roles, collecting normalized privilege names.
	 *
	 * @param role the role currently being visited
	 * @param privileges accumulates normalized privilege names
	 * @param visited role names already visited, to break inheritance cycles
	 * @return true if this role or any role reachable from it confers superuser status
	 */
	private static boolean collect(Role role, Set<String> privileges, Set<String> visited) {
		if (role == null || role.getRole() == null || !visited.add(RolePrivileges.normalize(role.getRole()))) {
			return false;
		}

		// Superuser status can be inherited, so a superuser role anywhere in the closure grants it.
		boolean grantsSuperuser = RoleConstants.SUPERUSER.equalsIgnoreCase(role.getRole());

		if (role.getPrivileges() != null) {
			for (Privilege privilege : role.getPrivileges()) {
				if (privilege != null && privilege.getPrivilege() != null) {
					// RolePrivileges normalizes names on construction, so raw names are fine here.
					privileges.add(privilege.getPrivilege());
				}
			}
		}

		for (Role inherited : role.getInheritedRoles()) {
			grantsSuperuser |= collect(inherited, privileges, visited);
		}

		return grantsSuperuser;
	}

	/**
	 * Waits for the loads in progress to finish. A load writes or discards its cache entry before
	 * handing over its result, so this is only needed to wait for loads the caller is not itself
	 * waiting on, for example in tests.
	 */
	void awaitLoads() {
		loads.forEach((key, load) -> {
			try {
				await(key, load);
			} catch (APIException e) {
				// the load's failure is its caller's to handle
			}
		});
	}

	/**
	 * Evicts every role immediately, even within a transaction, which still resolves roles without the
	 * cache until it completes. Only for tests that load data behind the API.
	 */
	void clearNow() {
		invalidation.clearNow();
	}

	/**
	 * Lets the current transaction read through the cache again and not evict it when it completes.
	 * Only for tests that load data behind the API but still need to observe cache hits.
	 */
	void forgetWritesInCurrentTransaction() {
		invalidation.forgetWritesInCurrentTransaction();
	}

	/**
	 * Returns the load in progress for the role if it started after the latest eviction, and otherwise
	 * starts a new one. A load that started before the latest eviction may have read the role before
	 * the change that caused it, so it is replaced rather than joined.
	 */
	private Load startOrJoinLoad(String key, String roleName, Cache<Object, Object> cache) {
		Load fresh = new Load(cache == null ? null : CacheInvalidation.currentGeneration(cache));
		Load current = loads.compute(key,
		    (k, existing) -> existing != null && Objects.equals(existing.generation, fresh.generation) ? existing : fresh);
		if (current != fresh) {
			return current;
		}

		Future<?> task = null;
		try {
			task = backgroundRunner.apply(() -> {
				try {
					fresh.result.complete(load(key, roleName, cache, fresh.generation));
				} catch (Throwable e) {
					fresh.result.completeExceptionally(e);
				} finally {
					loads.remove(key, fresh);
				}
			});
		} catch (RuntimeException e) {
			fresh.result.completeExceptionally(e);
			loads.remove(key, fresh);
		} finally {
			fresh.task.complete(task);
		}
		return fresh;
	}

	/**
	 * Runs on the background thread: loads a fresh copy of the role, flattens it, and caches it unless
	 * the generation token has changed since <code>generation</code> was read. A role absent from the
	 * database grants nothing.
	 */
	private RolePrivileges load(String key, String roleName, Cache<Object, Object> cache, Object generation) {
		RolePrivileges loaded = computeRolePrivileges(committedRoleLoader.apply(roleName));
		if (cache != null) {
			CacheInvalidation.putIfCurrent(cache, key, loaded, generation);
		}
		return loaded;
	}

	/**
	 * Reads the role through the caller's own session and flattens it, without caching it. If the read
	 * fails, the role grants nothing.
	 *
	 * @param loadFailure why the load failed, if it did, which is recorded on any failure of this read
	 */
	private RolePrivileges resolveInCurrentSession(String roleName, Exception loadFailure) {
		try {
			return computeRolePrivileges(dao.getRole(roleName));
		} catch (RuntimeException e) {
			if (loadFailure != null) {
				e.addSuppressed(loadFailure);
			}
			log.error("Could not load the privileges of role {}; it grants nothing", roleName, e);
			return new RolePrivileges(new HashSet<>(), false);
		}
	}

	/**
	 * Waits for the load. The thread's task is waited for first, since it can end without running the
	 * load, which would then never complete.
	 */
	private RolePrivileges await(String key, Load load) {
		try {
			Future<?> task = load.task.join();
			if (task != null) {
				try {
					task.get();
				} catch (ExecutionException e) {
					load.result.completeExceptionally(e.getCause());
					loads.remove(key, load);
				}
			}
			return load.result.get();
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new APIException("Interrupted while loading the privileges of role " + key, e);
		} catch (ExecutionException e) {
			Throwable cause = e.getCause();
			if (cause instanceof APIException) {
				throw (APIException) cause;
			}
			if (cause instanceof Error) {
				throw (Error) cause;
			}
			throw new APIException("Could not load the privileges of role " + key, cause);
		}
	}

	@SuppressWarnings("unchecked")
	private Cache<Object, Object> getCache() {
		org.springframework.cache.Cache cache = cacheManager.getCache(CACHE_NAME);
		return cache == null ? null : (Cache<Object, Object>) cache.getNativeCache();
	}

	/** A load in progress, with the generation token read before it started. */
	private static final class Load {

		private final Object generation;

		private final CompletableFuture<RolePrivileges> result = new CompletableFuture<>();

		/** The thread's task running the load, completed as soon as it has been started. */
		private final CompletableFuture<Future<?>> task = new CompletableFuture<>();

		private Load(Object generation) {
			this.generation = generation;
		}
	}
}
