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

import java.util.function.Supplier;

import org.openmrs.GlobalProperty;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

/**
 * Caches global property lookups for
 * {@link org.openmrs.api.AdministrationService#getGlobalProperty(String)}.
 * <p>
 * This lives in its own bean rather than on the service because Spring's cache advice is applied by
 * a proxy: a call the service made to itself would bypass the proxy and therefore the cache.
 * Keeping it here also avoids having to widen the public
 * {@link org.openmrs.api.AdministrationService} interface purely to make caching work.
 * <p>
 * <b>Keys are lowercased.</b> Global property lookups are case-insensitive, so <code>"Foo"</code>
 * and <code>"foo"</code> must resolve to the same entry. Without normalisation a save of
 * <code>"foo"</code> would evict only <code>"foo"</code> and leave a stale <code>"Foo"</code> entry
 * behind.
 * <p>
 * Global properties written straight to the database, bypassing
 * {@link org.openmrs.api.AdministrationService}, are not supported by this cache. Such writes
 * already bypass the {@link org.openmrs.api.GlobalPropertyListener} based caches today.
 *
 * @see CachedGlobalProperty
 * @since 3.0.0
 */
@Component("globalPropertyCache")
public class GlobalPropertyCache {

	public static final String CACHE_NAME = "globalProperty";

	/**
	 * The cache key is the lowercased property name. Referenced positionally as <code>#p0</code> rather
	 * than by name, because parameter names are only visible to SpEL when the code is compiled with
	 * <code>-parameters</code>, which this build does not do.
	 */
	private static final String KEY = "#p0.toLowerCase()";

	/**
	 * Returns a cached snapshot of the named global property, loading it through <code>loader</code> on
	 * a miss.
	 * <p>
	 * Properties that do not exist are cached as {@link CachedGlobalProperty#ABSENT} so that repeated
	 * lookups of an unset property do not hit the database either. This method never returns null.
	 *
	 * @param propertyName the property to look up, not null
	 * @param loader loads the property from the database on a cache miss
	 * @return a snapshot of the property, or {@link CachedGlobalProperty#ABSENT} if it does not exist
	 */
	@Cacheable(value = CACHE_NAME, key = KEY)
	public CachedGlobalProperty get(String propertyName, Supplier<GlobalProperty> loader) {
		GlobalProperty globalProperty = loader.get();

		return globalProperty == null ? CachedGlobalProperty.ABSENT : CachedGlobalProperty.of(globalProperty);
	}

	/**
	 * Drops the cached snapshot of the named property. Must be called whenever a global property is
	 * saved, updated or purged through the API.
	 *
	 * @param propertyName the property to evict, not null
	 */
	@CacheEvict(value = CACHE_NAME, key = KEY)
	public void evict(String propertyName) {
		// the annotation does the work
	}

	/**
	 * Drops every cached snapshot. Used when properties may have changed wholesale behind the API, for
	 * instance when a module inserts global properties through Liquibase at startup.
	 */
	@CacheEvict(value = CACHE_NAME, allEntries = true)
	public void evictAll() {
		// the annotation does the work
	}
}
