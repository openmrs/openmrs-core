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

import java.util.Locale;

import org.openmrs.GlobalProperty;
import org.openmrs.api.context.Context;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

/**
 * Lets context-sensitive tests observe and arrange the {@link GlobalPropertyCache}. Test
 * transactions never commit and fills only read committed data, so tests that need a particular
 * cached value seed it directly.
 */
public final class GlobalPropertyCacheTestUtil {

	private GlobalPropertyCacheTestUtil() {
	}

	/**
	 * Caches <code>property</code> as a fill would. Assumes lower-cased keys, which is the case unless
	 * the test sets the case-sensitivity global property to false.
	 */
	public static void seed(GlobalProperty property) {
		Cache cache = getCache();
		cache.put(GlobalPropertyCache.KEY_MODE, Boolean.TRUE);
		cache.put(property.getProperty().toLowerCase(Locale.ROOT), GlobalPropertyCache.Entry.of(property));
	}

	/**
	 * @return true if an entry, including one for an unset property, is cached under the lower-cased
	 *         name
	 */
	public static boolean isCached(String propertyName) {
		return getCache().get(propertyName.toLowerCase(Locale.ROOT)) != null;
	}

	/** Waits for fills started by earlier misses to finish. */
	public static void awaitFills() {
		getGlobalPropertyCache().awaitFills();
	}

	/**
	 * Lets the current transaction read through the cache again after loading data behind the API, for
	 * example with {@code executeDataSet}.
	 */
	public static void forgetWritesInCurrentTransaction() {
		getGlobalPropertyCache().forgetWritesInCurrentTransaction();
	}

	private static GlobalPropertyCache getGlobalPropertyCache() {
		return Context.getRegisteredComponent("globalPropertyCache", GlobalPropertyCache.class);
	}

	private static Cache getCache() {
		return Context.getRegisteredComponent("apiCacheManager", CacheManager.class)
		        .getCache(GlobalPropertyCache.CACHE_NAME);
	}
}
