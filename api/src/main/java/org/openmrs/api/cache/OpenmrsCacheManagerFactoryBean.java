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

import java.util.List;
import java.util.Map;

import org.springframework.cache.ehcache.EhCacheManagerFactoryBean;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.config.CacheConfiguration;

/**
 * This class creates cache configurations from apiCacheConfig.properties files in the classpath. This file should be
 * created in modules resource directory only. To configure cache in openmrs-core go to ehcache-api.xml.
 * If the configuration already exists it won't be overridden.
 * Example content for apiCacheConfig.properties:
 * userSearchLocales.maxElementsInMemory=500
 * userSearchLocales.eternal=false
 * userSearchLocales.timeToIdleSeconds=300
 * userSearchLocales.timeToLiveSeconds=300
 * userSearchLocales.memoryStoreEvictionPolicy=LRU
 */
public class OpenmrsCacheManagerFactoryBean extends EhCacheManagerFactoryBean {

	@Override
	public CacheManager getObject() {
		CacheManager cacheManager = super.getObject();

		Map<String, CacheConfiguration> cacheConfig = cacheManager.getConfiguration().getCacheConfigurations();

		List<CacheConfiguration> cacheConfigurations = CachePropertiesUtil.getCacheConfigurations();
		cacheConfigurations.stream()
				.filter(cc ->
						cacheConfig.get(cc.getName()) == null)
				.forEach(cc ->
						cacheManager.addCache(new Cache(cc)));

		return cacheManager;
	}
}
