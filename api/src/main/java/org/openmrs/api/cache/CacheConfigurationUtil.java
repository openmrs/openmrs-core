package org.openmrs.api.cache;

import net.sf.ehcache.config.CacheConfiguration;
import org.apache.commons.beanutils.BeanUtils;
import org.springframework.core.io.Resource;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Properties;

public class CacheConfigurationUtil {

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

	public static void addCacheConfigsFormResourceToList(Resource resource, List<CacheConfiguration> openmrsCacheConfigurationList) {
		Properties cacheProperties = CachePropertiesUtil.getPropertiesFromResource(resource);
		CachePropertiesUtil.getAllCacheNames(cacheProperties.keySet())
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

}
