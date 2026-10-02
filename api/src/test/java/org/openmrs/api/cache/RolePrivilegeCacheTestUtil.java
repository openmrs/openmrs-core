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

import java.util.Arrays;
import java.util.HashSet;

import org.openmrs.api.context.Context;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

/**
 * Test helpers for the {@link RolePrivilegeCache}.
 */
public final class RolePrivilegeCacheTestUtil {

	private RolePrivilegeCacheTestUtil() {
	}

	/**
	 * Caches the privileges as the role's flattened closure, letting a test grant privileges to a role
	 * without persisting them. The current transaction reads through the role cache afterwards, even if
	 * it has loaded data behind the API, so that it sees the seeded entry.
	 */
	public static void seed(String roleName, String... privileges) {
		Context.getRegisteredComponent("rolePrivilegeCache", RolePrivilegeCache.class).forgetWritesInCurrentTransaction();
		String key = RolePrivileges.normalize(roleName);
		// the cache's put does not overwrite, so drop any entry an earlier privilege check cached
		getCache().evict(key);
		getCache().put(key, new RolePrivileges(new HashSet<>(Arrays.asList(privileges)), false));
	}

	/**
	 * @return true if an entry is cached for the role on this node, whether or not the current
	 *         transaction would read it
	 */
	public static boolean isCached(String roleName) {
		return getCache().get(RolePrivileges.normalize(roleName)) != null;
	}

	/** Removes every entry from this node's role cache immediately. */
	public static void clear() {
		getCache().clear();
	}

	private static Cache getCache() {
		return Context.getRegisteredComponent("apiCacheManager", CacheManager.class).getCache(RolePrivilegeCache.CACHE_NAME);
	}
}
