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

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import org.openmrs.Privilege;
import org.openmrs.Role;
import org.openmrs.util.RoleConstants;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

/**
 * Caches the fully-flattened {@link RolePrivileges} for each role, keyed by role name.
 * <p>
 * This is a plain Spring {@link Component}, not a service method, so reading it during a privilege
 * check does <b>not</b> re-enter the {@code @Authorized}/AOP service proxy (no overhead, and no
 * risk of a privilege check triggering another privilege check). It holds a direct handle to the
 * {@code rolePrivileges} cache declared in {@code cache-api.yaml}.
 * <p>
 * On a cache miss the role's entire inheritance chain is walked once by {@link #flatten(Role)},
 * collecting every privilege (lowercased) and whether the chain grants superuser, and the result is
 * stored so later checks are a single lookup with no recursion.
 *
 * @since 2.8.0
 */
@Component("rolePrivilegeCache")
public class RolePrivilegeCache {

	private final Cache cache;

	public RolePrivilegeCache(@Qualifier("apiCacheManager") CacheManager cacheManager) {
		this.cache = cacheManager.getCache("rolePrivileges");
	}

	/**
	 * Returns the flattened privileges for the given role, computing and caching them on the first call
	 * and returning the stored copy afterwards.
	 *
	 * @param role the role to resolve; must have a non-null name
	 * @return the role's flattened privileges (never null)
	 */
	public RolePrivileges getPrivileges(Role role) {
		// key by role NAME (a stable, unique String); compute via flatten() on a cache miss
		return cache.get(role.getRole(), () -> flatten(role));
	}

	/**
	 * Walks the given role plus everything it inherits, collecting all privilege names (lowercased) and
	 * noticing superuser status in a single pass. A {@code visited} set of role names guards against
	 * inheritance cycles so the walk always terminates.
	 */
	private RolePrivileges flatten(Role role) {
		Set<String> privileges = new HashSet<>();
		boolean grantsSuperuser = false;
		Set<String> visited = new HashSet<>();
		Deque<Role> stack = new ArrayDeque<>();
		stack.push(role);
		while (!stack.isEmpty()) {
			Role current = stack.pop();
			if (current == null || !visited.add(current.getRole())) {
				// null role, or one we have already processed -> skip (prevents infinite loops)
				continue;
			}
			if (RoleConstants.SUPERUSER.equals(current.getRole())) {
				grantsSuperuser = true;
			}
			if (current.getPrivileges() != null) {
				for (Privilege privilege : current.getPrivileges()) {
					privileges.add(privilege.getPrivilege().toLowerCase(Locale.ENGLISH));
				}
			}
			for (Role inherited : current.getInheritedRoles()) {
				stack.push(inherited);
			}
		}
		return new RolePrivileges(Collections.unmodifiableSet(privileges), grantsSuperuser);
	}
}
