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
import java.util.HashSet;
import java.util.List;
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
 * <p>
 * Also caches the flattened set of every currently registered {@link Privilege} name (see
 * {@link #getAllPrivilegeNames()}) and every registered {@link org.openmrs.Role} name (see
 * {@link #getAllRoleNames()}), each under its own reserved key in the same cache region so both are
 * invalidated by the same {@code @CacheEvict} on {@code savePrivilege}/{@code purgePrivilege}/
 * {@code saveRole}/{@code purgeRole} that already clears per-role entries, without a dedicated
 * cache region. This backs {@code OpenmrsAuthenticationToken} giving a superuser or {@code Daemon}
 * thread every registered privilege and role as {@code GrantedAuthority}s (both satisfy any
 * privilege or role name, which cannot otherwise be represented as a static, enumerable authority
 * set - see {@link #isRegisteredPrivilege(String)}), and {@link #warnIfUnregistered(String)}, which
 * flags a privilege check naming a string with no corresponding row - almost always a forgotten
 * registration or a typo, since {@code Context.hasPrivilege(String)} enforces no such requirement
 * itself. There is no role equivalent of {@code warnIfUnregistered}: unlike a privilege name, a
 * role name checked via {@code hasRole(...)} is always compared against actual
 * {@link org.openmrs.Role} membership, so there is no "checked but never registered" case to
 * diagnose.
 *
 * @since 3.0.0, 2.9.0, 2.8.9
 */
@Component("rolePrivilegeCache")
public class RolePrivilegeCache implements ApplicationListener<ContextRefreshedEvent> {

	private static final Logger log = LoggerFactory.getLogger(RolePrivilegeCache.class);

	public static final String CACHE_NAME = "rolePrivileges";

	/**
	 * Cache/in-flight key for the flattened set of every registered privilege name. Chosen to be
	 * exceedingly unlikely to collide with an admin-supplied role name, which is the only other kind of
	 * key stored in this cache region.
	 */
	private static final String ALL_PRIVILEGES_KEY = "\u0000ALL_PRIVILEGES\u0000";

	/**
	 * Cache/in-flight key for the flattened set of every registered role name, alongside
	 * {@link #ALL_PRIVILEGES_KEY} in the same reserved-key scheme.
	 */
	private static final String ALL_ROLES_KEY = "\u0000ALL_ROLES\u0000";

	private static final RolePrivileges EMPTY_NAMES = new RolePrivileges(Collections.emptySet(), false);

	/**
	 * Capability token issued by {@link Daemon}, letting this component launch a daemon thread to load
	 * roles with full trust.
	 */
	private static volatile Daemon.CallerKey daemonCallerKey;

	private final CacheManager cacheManager;

	/**
	 * In-flight refreshes keyed by normalized role name (or {@link #ALL_PRIVILEGES_KEY}/
	 * {@link #ALL_ROLES_KEY}), so concurrent misses for the same key share a single daemon computation
	 * instead of each launching their own.
	 */
	private final ConcurrentMap<String, Future<RolePrivileges>> inFlight = new ConcurrentHashMap<>();

	/**
	 * Privilege names (case-normalized) that {@link #warnIfUnregistered(String)} has already logged
	 * about, so a repeatedly-checked unregistered privilege logs once rather than on every check.
	 */
	private final Set<String> loggedUnregisteredPrivileges = ConcurrentHashMap.newKeySet();

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
	 * Returns the names of every currently registered {@link Privilege}, in their original casing (see
	 * {@link RolePrivileges} for why), computing and caching the result on a miss. Never returns
	 * {@code null}.
	 *
	 * @return the registered privilege names
	 * @since 3.0.0
	 */
	public Set<String> getAllPrivilegeNames() {
		return getAllPrivilegesClosure().getPrivilegeNames();
	}

	/**
	 * @param privilege the privilege name to test (compared case-insensitively)
	 * @return true if a {@link Privilege} with this name is registered in the system
	 * @since 3.0.0
	 */
	public boolean isRegisteredPrivilege(String privilege) {
		return privilege != null && getAllPrivilegesClosure().containsPrivilege(privilege);
	}

	/**
	 * Returns the names of every currently registered {@link org.openmrs.Role}, in their original
	 * casing (see {@link RolePrivileges} for why), computing and caching the result on a miss. Never
	 * returns {@code null}.
	 *
	 * @return the registered role names
	 * @since 3.0.0
	 */
	public Set<String> getAllRoleNames() {
		return getAllRolesClosure().getPrivilegeNames();
	}

	private RolePrivileges getAllPrivilegesClosure() {
		Cache cache = getCache();
		if (cache != null) {
			RolePrivileges cached = cache.get(ALL_PRIVILEGES_KEY, RolePrivileges.class);
			if (cached != null) {
				return cached;
			}
		}

		return refreshAllPrivileges(cache);
	}

	private RolePrivileges getAllRolesClosure() {
		Cache cache = getCache();
		if (cache != null) {
			RolePrivileges cached = cache.get(ALL_ROLES_KEY, RolePrivileges.class);
			if (cached != null) {
				return cached;
			}
		}

		return refreshAllRoles(cache);
	}

	/**
	 * Logs, at error level and once per distinct privilege name for the lifetime of this component,
	 * that a privilege check named a string with no corresponding {@link Privilege} row. Purely
	 * diagnostic: it never affects the outcome of the check that triggered it. The empty string is
	 * exempt, since it is the implicit "authenticated" privilege every logged-in user holds (see
	 * {@code UserContext#resolvePrivilege(String)}) and is never itself registered as a
	 * {@link Privilege}.
	 *
	 * @param privilege the privilege name that was checked
	 * @since 3.0.0
	 */
	public void warnIfUnregistered(String privilege) {
		if (privilege == null || privilege.isEmpty() || isRegisteredPrivilege(privilege)) {
			return;
		}

		if (loggedUnregisteredPrivileges.add(RolePrivileges.normalize(privilege))) {
			log.error(
			    "Checked privilege '{}' is not registered as a Privilege in the system - "
			            + "register it (Manage Privileges, or a startup registration) so it can be assigned to a role",
			    privilege);
		}
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
	 * Loads and caches the flattened set of every registered {@link Privilege} name in a daemon thread,
	 * mirroring {@link #refresh(String, Role, Cache)} for the per-role case: loading them through the
	 * secured {@code UserService} itself requires {@code Manage Privileges}, so a daemon thread is
	 * needed to break that cycle for a caller who does not (yet) hold it.
	 * <p>
	 * Failure of any kind - scheduling, interruption, or the daemon task itself - fails open to an
	 * empty, uncached result rather than propagating: this data only ever adds convenience
	 * ({@code getAuthorities()}'s superuser authorities, or the diagnostic in
	 * {@link #warnIfUnregistered(String)}), never a security decision, so degrading it during a
	 * transient failure is preferable to letting it break the privilege check that triggered it.
	 *
	 * @param cache the target cache, or <code>null</code> if unavailable
	 * @return the flattened set of registered privilege names, or an empty one on failure
	 */
	private RolePrivileges refreshAllPrivileges(Cache cache) {
		Future<RolePrivileges> future;
		try {
			future = inFlight.computeIfAbsent(ALL_PRIVILEGES_KEY,
			    k -> Daemon.runNewDaemonTask((Callable<RolePrivileges>) () -> {
				    try {
					    return loadAndCacheAllPrivileges(cache);
				    } finally {
					    inFlight.remove(k);
				    }
			    }, daemonCallerKey()));
		} catch (RuntimeException e) {
			log.warn("Could not schedule a daemon refresh of the registered privilege names; treating the set as "
			        + "empty for this call",
			    e);
			return EMPTY_NAMES;
		}

		try {
			return future.get();
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			log.debug(
			    "Interrupted while refreshing the registered privilege names; treating the set as empty for " + "this call",
			    e);
			return EMPTY_NAMES;
		} catch (ExecutionException e) {
			log.warn("Refreshing the registered privilege names failed; treating the set as empty for this call",
			    e.getCause());
			return EMPTY_NAMES;
		}
	}

	/**
	 * Runs inside a daemon thread: loads every {@link Privilege} through the {@code UserService}
	 * (authorization skipped for daemon threads), collects their names, and caches the result.
	 *
	 * @param cache the target cache, or <code>null</code> if unavailable
	 * @return the flattened set of registered privilege names
	 */
	private RolePrivileges loadAndCacheAllPrivileges(Cache cache) {
		List<Privilege> privileges = Context.getUserService().getAllPrivileges();
		Set<String> names = new HashSet<>();
		for (Privilege privilege : privileges) {
			if (privilege != null && privilege.getPrivilege() != null) {
				names.add(privilege.getPrivilege());
			}
		}

		RolePrivileges computed = new RolePrivileges(names, false);
		if (cache != null) {
			cache.put(ALL_PRIVILEGES_KEY, computed);
		}
		return computed;
	}

	/**
	 * Loads and caches the flattened set of every registered {@link org.openmrs.Role} name in a daemon
	 * thread, mirroring {@link #refreshAllPrivileges(Cache)} for the same reason: loading them through
	 * the secured {@code UserService} itself requires {@code Manage Roles}, so a daemon thread is
	 * needed to break that cycle for a caller who does not (yet) hold it.
	 * <p>
	 * Failure of any kind fails open to an empty, uncached result, same as
	 * {@link #refreshAllPrivileges(Cache)} and for the same reason: this data only ever adds
	 * convenience ({@code getAuthorities()}'s superuser/Daemon authorities), never a security decision.
	 *
	 * @param cache the target cache, or <code>null</code> if unavailable
	 * @return the flattened set of registered role names, or an empty one on failure
	 */
	private RolePrivileges refreshAllRoles(Cache cache) {
		Future<RolePrivileges> future;
		try {
			future = inFlight.computeIfAbsent(ALL_ROLES_KEY, k -> Daemon.runNewDaemonTask((Callable<RolePrivileges>) () -> {
				try {
					return loadAndCacheAllRoles(cache);
				} finally {
					inFlight.remove(k);
				}
			}, daemonCallerKey()));
		} catch (RuntimeException e) {
			log.warn("Could not schedule a daemon refresh of the registered role names; treating the set as empty "
			        + "for this call",
			    e);
			return EMPTY_NAMES;
		}

		try {
			return future.get();
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			log.debug("Interrupted while refreshing the registered role names; treating the set as empty for this call", e);
			return EMPTY_NAMES;
		} catch (ExecutionException e) {
			log.warn("Refreshing the registered role names failed; treating the set as empty for this call", e.getCause());
			return EMPTY_NAMES;
		}
	}

	/**
	 * Runs inside a daemon thread: loads every {@link org.openmrs.Role} through the {@code UserService}
	 * (authorization skipped for daemon threads), collects their names, and caches the result.
	 *
	 * @param cache the target cache, or <code>null</code> if unavailable
	 * @return the flattened set of registered role names
	 */
	private RolePrivileges loadAndCacheAllRoles(Cache cache) {
		List<Role> roles = Context.getUserService().getAllRoles();
		Set<String> names = new HashSet<>();
		for (Role role : roles) {
			if (role != null && role.getRole() != null) {
				names.add(role.getRole());
			}
		}

		RolePrivileges computed = new RolePrivileges(names, false);
		if (cache != null) {
			cache.put(ALL_ROLES_KEY, computed);
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
