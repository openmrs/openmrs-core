/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs;

import net.sf.ehcache.config.CacheConfiguration;
import org.openmrs.util.CachePropertiesUtil;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.ehcache.EhCacheCacheManager;
import org.springframework.cache.ehcache.EhCacheManagerFactoryBean;
import org.springframework.cache.support.CompositeCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Configuration
@EnableCaching
public class CachingConfig extends CachingConfigurerSupport {

    @Bean
    public EhCacheCacheManager ehCacheCacheManager() {
        return new EhCacheCacheManager(ehCacheManagerFactoryBean().getObject());
    }

    @Bean
    public EhCacheManagerFactoryBean ehCacheManagerFactoryBean(){
        EhCacheManagerFactoryBean cacheManagerFactoryBean = new EhCacheManagerFactoryBean();
        cacheManagerFactoryBean.setConfigLocation(new ClassPathResource("ehcache-api.xml"));
        cacheManagerFactoryBean.setShared(true);

        return cacheManagerFactoryBean;
    }

    @Bean(destroyMethod = "shutdown", name = "apiEhCacheManager")
    public net.sf.ehcache.CacheManager apiEhCacheManager(){

        Map<String, CacheConfiguration> cacheConfig = ehCacheManagerFactoryBean().getObject().getConfiguration().getCacheConfigurations();
        net.sf.ehcache.config.Configuration config = new net.sf.ehcache.config.Configuration();

        List<CacheConfiguration> cacheConfigurations = CachePropertiesUtil.getCacheConfigurations();
        cacheConfigurations.stream()
                .filter(cc ->
                        cacheConfig.get(cc.getName()) == null)
                .forEach(config::addCache);

        return net.sf.ehcache.CacheManager.newInstance(config);
    }

    @Override
    @Bean(name = "apiCacheManager")
    public CacheManager cacheManager() {
        List<CacheManager> cacheManagers = new ArrayList<>();

        cacheManagers.add(ehCacheCacheManager());
        cacheManagers.add(new EhCacheCacheManager(apiEhCacheManager()));

        CompositeCacheManager cacheManager = new CompositeCacheManager();

        cacheManager.setCacheManagers(cacheManagers);
        cacheManager.setFallbackToNoOpCache(false);

        return cacheManager;
    }


}
