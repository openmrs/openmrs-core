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


import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.beanutils.BeanUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import net.sf.ehcache.config.CacheConfiguration;

public class CachePropertiesUtil {

    private CachePropertiesUtil() {
    }

    /**
     * This method looks for all apiCacheConfig.properties file located in cacheConfig folder in classpath
     * @return list of CacheConfiguration objects
     */
    public static List<CacheConfiguration> getCacheConfigurations(){
        List<CacheConfiguration> openmrsCacheConfigurationList = new ArrayList<>();
        Resource[] resourceFromClassPath = getResourceFromClassPath();
        Arrays.stream(resourceFromClassPath)
                .forEach(r -> addCacheConfigsFormResourceToList(r, openmrsCacheConfigurationList));

        return openmrsCacheConfigurationList;
    }

    private static CacheConfiguration createCacheConfiguration(OpenmrsCacheConfiguration openmrsCacheConfiguration) {
        CacheConfiguration cacheConfiguration = new CacheConfiguration();
        openmrsCacheConfiguration.getAllKeys()
                .forEach(key -> {
                    try {
                        BeanUtils.setProperty(cacheConfiguration, key, openmrsCacheConfiguration.getProperty(key));
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        throw new IllegalStateException(e);
                    }
                });
        return cacheConfiguration;
    }

    private static void addCacheConfigsFormResourceToList(Resource resource, List<CacheConfiguration> openmrsCacheConfigurationList) {
        Properties cacheProperties = getPropertiesFromResource(resource);
        getAllCacheNames(cacheProperties.keySet())
                .forEach(cacheName -> {
                    OpenmrsCacheConfiguration openmrsCacheConfiguration = new OpenmrsCacheConfiguration();
                    openmrsCacheConfiguration.addProperty("name", cacheName);
                    cacheProperties.keySet()
                            .stream()
                            .filter(key -> key.toString().startsWith(cacheName))
                            .forEach(key -> {
                                String s = key.toString();
                                openmrsCacheConfiguration.addProperty(
                                        s.replace(cacheName+".", ""),
                                        cacheProperties.getProperty(key.toString()));
                            });
                    openmrsCacheConfigurationList.add(createCacheConfiguration(openmrsCacheConfiguration));
                });
    }

    private static Set<String> getAllCacheNames(Set<Object> keys) {
        Set<String> cacheNames = new HashSet<>();
        keys.forEach(cacheName -> {
            String s = cacheName.toString();
            cacheNames.add(s.substring(0, s.indexOf(".")));
        });
        return cacheNames;
    }

    private static Properties getPropertiesFromResource(Resource resource) {
        Properties properties = new Properties();
        try (InputStream inputStream = resource.getInputStream()){
            properties.load(inputStream);
            return properties;
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private static Resource[] getResourceFromClassPath() {
        ResourcePatternResolver patternResolver = new PathMatchingResourcePatternResolver();
        try {
            return patternResolver.getResources("classpath*:apiCacheConfig.properties");
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
