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

import org.openmrs.api.context.Context;

/**
 * Test hooks for the API caches that are invalidated through {@link CacheInvalidation}, for tests
 * that load data behind the API, for example with {@code executeDataSet}.
 */
public final class ApiCacheTestUtil {

	private ApiCacheTestUtil() {
	}

	/**
	 * Evicts every entry immediately, even within a transaction, after data has been loaded behind the
	 * API.
	 */
	public static void clearNow() {
		globalPropertyCache().clearNow();
		rolePrivilegeCache().clearNow();
	}

	/**
	 * Lets the current transaction read through the caches again after loading data behind the API, for
	 * example once that data has been committed.
	 */
	public static void forgetWritesInCurrentTransaction() {
		globalPropertyCache().forgetWritesInCurrentTransaction();
		rolePrivilegeCache().forgetWritesInCurrentTransaction();
	}

	/** Waits for background loads started by earlier misses to finish. */
	public static void awaitLoads() {
		globalPropertyCache().awaitFills();
		rolePrivilegeCache().awaitLoads();
	}

	private static GlobalPropertyCache globalPropertyCache() {
		return Context.getRegisteredComponent("globalPropertyCache", GlobalPropertyCache.class);
	}

	private static RolePrivilegeCache rolePrivilegeCache() {
		return Context.getRegisteredComponent("rolePrivilegeCache", RolePrivilegeCache.class);
	}
}
