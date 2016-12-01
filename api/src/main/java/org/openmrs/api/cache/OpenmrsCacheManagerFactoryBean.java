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

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.config.Configuration;
import org.springframework.cache.ehcache.EhCacheManagerFactoryBean;

import java.util.List;
import java.util.Map;

public class OpenmrsCacheManagerFactoryBean extends EhCacheManagerFactoryBean {

	@Override
	public CacheManager getObject() {
		CacheManager cacheManager = super.getObject();

		Map<String, CacheConfiguration> cacheConfig = cacheManager.getConfiguration().getCacheConfigurations();
		Configuration config = new Configuration();

		List<CacheConfiguration> cacheConfigurations = CachePropertiesUtil.getCacheConfigurations();
		cacheConfigurations.stream()
				.filter(cc ->
						cacheConfig.get(cc.getName()) == null)
				.forEach(config::addCache);

		return cacheManager;
	}
}
