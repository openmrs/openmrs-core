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
import java.util.function.Consumer;
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
 * The caller waits for the load, and concurrent misses for the same role share one. If the load
 * fails, the failure propagates to the caller; a role absent from the database grants nothing.
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
 * @since 3.0.0, 2.9.0, 2.8.9
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

	/** Runs a load on another thread. */
	private final Consumer<Runnable> backgroundRunner;

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

	RolePrivilegeCache(SpringEmbeddedCacheManager cacheManager, UserDAO dao, Consumer<Runnable> backgroundRunner,
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
	 * returns {@code null}.
	 *
	 * @param role the directly assigned role to resolve
	 * @return the flattened privilege closure for the role
	 * @throws APIException if the role could not be loaded
	 */
	public RolePrivileges getRolePrivileges(Role role) {
		if (role == null || role.getRole() == null) {
			return new RolePrivileges(new HashSet<>(), false);
		}

		if (invalidation.isWrittenInCurrentTransaction(null)) {
			return computeRolePrivileges(dao.getRole(role.getRole()));
		}

		Cache<Object, Object> cache = getCache();
		String key = RolePrivileges.normalize(role.getRole());
		if (cache != null) {
			Object cached = cache.get(key);
			if (cached instanceof RolePrivileges) {
				return (RolePrivileges) cached;
			}
		}

		return await(key, startOrJoinLoad(key, role.getRole(), cache));
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
		loads.values().forEach(load -> load.result.handle((result, e) -> null).join());
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

		try {
			backgroundRunner.accept(() -> {
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

	private static RolePrivileges await(String key, Load load) {
		try {
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

		private Load(Object generation) {
			this.generation = generation;
		}
	}
}
