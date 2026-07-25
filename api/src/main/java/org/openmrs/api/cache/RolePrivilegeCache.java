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
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.openmrs.Privilege;
import org.openmrs.Role;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.Daemon;
import org.openmrs.util.RoleConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

/**
 * Per-role cache of flattened privilege resolutions, so privilege and superuser checks need not
 * re-expand a user's role graph on every call. The cached value for a role is an immutable
 * {@link RolePrivileges} covering that role and its entire inherited closure.
 * <p>
 * On a miss the closure is computed from a freshly loaded copy of the role, not the caller-supplied
 * instance: a long-lived {@code UserContext} can hold a stale, detached role graph that, cached by
 * role name, would serve out-of-date privileges to other sessions.
 * <p>
 * The fresh role is loaded and flattened inside a daemon thread, because loading a role through the
 * secured {@code UserService} itself requires {@code Get Roles} — the very check being resolved.
 * Daemon threads skip authorization (see {@link org.openmrs.aop.AuthorizationAdvice}), breaking
 * that cycle without the security bypass of a direct DAO read and without emitting spurious
 * {@code PrivilegeListener} notifications. Concurrent misses for the same role coalesce onto a
 * single {@link Future}; the caller blocks on it, so resolution stays synchronous.
 * <p>
 * The component reaches into the {@code rolePrivileges} cache directly rather than exposing a
 * {@code @Cacheable} service method, so a privilege check does not re-enter the service AOP stack
 * (which performs its own privilege checks). The cache lives on {@code apiCacheManager}, so it
 * participates in clustering when {@code cache.type=cluster} (as an invalidation cache: each node
 * computes its own entries and eviction broadcasts an invalidation) and is cleared by
 * {@code @CacheEvict} on role/privilege mutations and on context refresh.
 * <p>
 * Eviction is not atomic with an in-flight refresh: a daemon load can read a role, a concurrent
 * {@code saveRole} evict, and the daemon's write then land after the eviction, briefly caching a
 * pre-save closure. The window is bounded by the {@code role-privileges} cache template's expiry.
 * <p>
 * That template carries a {@code lifespan} TTL as a safety net against role changes made outside
 * the API (eviction only fires on API mutations); it is configured declaratively in {@code
 * infinispan-api.xml}/{@code infinispan-api-local.xml} rather than per entry here.
 *
 * @since 3.0.0, 2.9.0, 2.8.9
 */
@Component("rolePrivilegeCache")
public class RolePrivilegeCache implements ApplicationListener<ContextRefreshedEvent> {

	private static final Logger log = LoggerFactory.getLogger(RolePrivilegeCache.class);

	public static final String CACHE_NAME = "rolePrivileges";

	/**
	 * Capability token issued by {@link Daemon}, letting this component launch a daemon thread to load
	 * roles with full trust.
	 */
	private static volatile Daemon.CallerKey daemonCallerKey;

	private final CacheManager cacheManager;

	/**
	 * In-flight refreshes keyed by normalized role name, so concurrent misses for the same role share a
	 * single daemon computation instead of each launching their own.
	 */
	private final ConcurrentMap<String, Future<RolePrivileges>> inFlight = new ConcurrentHashMap<>();

	@Autowired
	public RolePrivilegeCache(@Qualifier("apiCacheManager") CacheManager cacheManager) {
		this.cacheManager = cacheManager;
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
	 * Returns the flattened privileges for the given role, computing and caching the result on a miss.
	 * Never returns {@code null}.
	 *
	 * @param role the directly assigned role to resolve
	 * @return the flattened privilege closure for the role
	 */
	public RolePrivileges getRolePrivileges(Role role) {
		if (role == null || role.getRole() == null) {
			return new RolePrivileges(new HashSet<>(), false);
		}

		Cache cache = getCache();
		String key = RolePrivileges.normalize(role.getRole());
		if (cache != null) {
			RolePrivileges cached = cache.get(key, RolePrivileges.class);
			if (cached != null) {
				return cached;
			}
		}

		return refresh(key, role, cache);
	}

	/**
	 * Loads and flattens a current copy of the role in a daemon thread, caches it, and returns it
	 * synchronously; concurrent misses for the same role coalesce onto one {@link Future}.
	 * <p>
	 * If the refresh cannot be scheduled, fails (for example a transient database error), or is
	 * interrupted, it falls back to flattening the caller-supplied instance <em>without caching it</em>
	 * — a deliberate fail-open, since denying every privilege during a database hiccup would brick the
	 * application, and not caching keeps a transient failure from poisoning other sessions. A role that
	 * loads but is absent from the database instead fails closed (see {@link #loadAndCache}).
	 *
	 * @param key the normalized role name used as the cache and in-flight key
	 * @param role the role supplied by the caller (used for its name, and as a fail-open fallback)
	 * @param cache the target cache, or <code>null</code> if unavailable
	 * @return the flattened privilege closure
	 */
	private RolePrivileges refresh(String key, Role role, Cache cache) {
		Future<RolePrivileges> future;
		try {
			future = inFlight.computeIfAbsent(key, k -> Daemon.runNewDaemonTask((Callable<RolePrivileges>) () -> {
				try {
					return loadAndCache(k, role, cache);
				} finally {
					// Clear on the daemon thread when the load finishes, not on the waiter's path: an
					// interrupted waiter does not cancel this task, so removing there could drop a
					// still-running refresh and let a concurrent miss schedule a duplicate.
					inFlight.remove(k);
				}
			}, daemonCallerKey()));
		} catch (RuntimeException e) {
			log.warn("Could not schedule a daemon refresh for role '{}'; resolving against the supplied instance", key, e);
			return computeRolePrivileges(role);
		}

		try {
			return future.get();
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			log.debug("Interrupted while refreshing role '{}'; resolving against the supplied instance", key, e);
			return computeRolePrivileges(role);
		} catch (ExecutionException e) {
			log.warn("Daemon refresh for role '{}' failed; resolving against the supplied instance", key, e.getCause());
			return computeRolePrivileges(role);
		}
	}

	/**
	 * Runs inside a daemon thread: loads a fresh copy of the role through the {@code UserService}
	 * (authorization skipped for daemon threads), flattens it, and caches it.
	 * <p>
	 * A role absent from the database grants nothing. The caller-supplied instance is deliberately not
	 * used as a fallback: it may be stale or purged, and caching it by name would serve out-of-date
	 * privileges to every session holding that role name. Failing closed keeps a purge or out-of-API
	 * deletion from being silently over-granted.
	 *
	 * @param key the normalized role name
	 * @param role the caller-supplied role, used for its name
	 * @param cache the target cache, or <code>null</code> if unavailable
	 * @return the flattened closure, or an empty closure if the role no longer exists
	 */
	private RolePrivileges loadAndCache(String key, Role role, Cache cache) {
		Role fresh = Context.getUserService().getRole(role.getRole());
		RolePrivileges computed = (fresh != null) ? computeRolePrivileges(fresh)
		        : new RolePrivileges(new HashSet<>(), false);
		if (cache != null) {
			// The lifespan TTL comes from the cache template (see the class-level note), so a plain put
			// carries it; no per-entry expiry handling is needed here.
			cache.put(key, computed);
		}
		return computed;
	}

	/**
	 * Flattens a role and its transitively inherited roles into an immutable {@link RolePrivileges},
	 * with a visited set guarding against inheritance cycles.
	 * <p>
	 * Resolves against the passed-in instance with <em>no freshness guarantee</em>, so it must not
	 * drive a security decision on a possibly stale role; the cache uses it only on a freshly loaded
	 * role or as a fail-open fallback.
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
	 * Clears the entire cache. Invoked on context refresh via {@link #onApplicationEvent}.
	 */
	public void clear() {
		Cache cache = getCache();
		if (cache != null) {
			cache.clear();
		}
	}

	private Cache getCache() {
		return cacheManager == null ? null : cacheManager.getCache(CACHE_NAME);
	}

	/**
	 * Clears the cache whenever the application context is refreshed, ensuring role graph changes
	 * applied outside the API before or during startup are not served stale.
	 */
	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		clear();
	}
}
