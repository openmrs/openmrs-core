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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;

import java.net.URL;
import java.util.Collection;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.interceptor.CacheInterceptor;

public class CacheConfigTest extends BaseContextSensitiveTest {

	@Autowired
	CacheManager cacheManager;
	
	@Autowired
	CacheConfig cacheConfig;
	
	@Autowired
	NonServiceTestBean nonServiceTestBean;
	
	@Autowired
	ServiceTestBean serviceTestBean;

	@Test
	public void shouldContainSpecificCacheConfigurations(){
		String[] expectedCaches = {"conceptDatatype", "subscription", "userSearchLocales", "conceptIdsByMapping", 
			"testCache", "serializerWhiteListTypes"};
		Collection<String> actualCaches = cacheManager.getCacheNames();
		assertThat(actualCaches, containsInAnyOrder(expectedCaches));
	}
	
    @Test
    public void shouldReturnCacheConfigurations(){
        List<URL> cacheConfigurations = cacheConfig.getCacheConfigurations();
        assertThat(cacheConfigurations.size(), is(2));
    }
	
	@Test
	public void shouldCacheNonServiceMethods() {
		String cachedValue = nonServiceTestBean.getCachedUUID();
		assertThat(nonServiceTestBean.getCachedUUID(), is(cachedValue));
	}

	@Test
	public void shouldCacheServiceMethods() {
		String cachedValue = serviceTestBean.getCachedUUID();
		assertThat(serviceTestBean.getCachedUUID(), is(cachedValue));
	}
}
