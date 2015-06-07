/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api.db.hibernate;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.Interceptor;
import org.openmrs.api.context.Context;
import org.openmrs.module.Module;
import org.openmrs.module.ModuleFactory;
import org.openmrs.util.OpenmrsUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate4.LocalSessionFactoryBean;

public class HibernateSessionFactoryBean extends LocalSessionFactoryBean {
	
	private static Log log = LogFactory.getLog(HibernateSessionFactoryBean.class);
	
	protected Set<String> mappingResources = new HashSet<String>();
	
	/**
	 * @since 1.9.2, 1.10
	 */
	protected Set<String> packagesToScan = new HashSet<String>();
	
	// @since 1.6.3, 1.7.2, 1.8.0, 1.9
	protected ChainingInterceptor chainingInterceptor = new ChainingInterceptor();
	
	// @since 1.6.3, 1.7.2, 1.8.0, 1.9
	// This will be sorted on keys before being used
	@Autowired(required = false)
	public Map<String, Interceptor> interceptors = new HashMap<String, Interceptor>();
	
	/**
	 * Collect the mapping resources for future use because the mappingResources object is defined
	 * as 'private' instead of 'protected'
	 */
	@Override
	public void setMappingResources(String... mappingResources) {
		for (String resource : mappingResources) {
			this.mappingResources.add(resource);
		}
		
		super.setMappingResources(this.mappingResources.toArray(new String[] {}));
	}
	
	/**
	 * Collect packages to scan that are set in core and for tests in modules.
	 * <p>
	 * It adds to the set instead of overwriting it with each call.
	 */
	@Override
	public void setPackagesToScan(String... packagesToScan) {
		this.packagesToScan.addAll(Arrays.asList(packagesToScan));
		
		super.setPackagesToScan(this.packagesToScan.toArray(new String[0]));
	}
	
	public Set<String> getModuleMappingResources() {
		for (Module mod : ModuleFactory.getStartedModules()) {
			for (String s : mod.getMappingFiles()) {
				mappingResources.add(s);
			}
		}
		return mappingResources;
	}
	
	/**
	 * Gets packages with mapped classes from all modules.
	 *
	 * @return the set of packages with mapped classes
	 * @since 1.9.2, 1.10
	 */
	public Set<String> getModulePackagesWithMappedClasses() {
		Set<String> packages = new HashSet<String>();
		for (Module module : ModuleFactory.getStartedModules()) {
			for (String pack : module.getPackagesWithMappedClasses()) {
				packages.add(pack);
			}
		}
		return packages;
	}
	
	/**
	 * Overridden to populate mappings from modules.
	 */
	@Override
	public void afterPropertiesSet() throws IOException {
		log.debug("Configuring hibernate sessionFactory properties");
		Properties config = getHibernateProperties();
		
		Properties moduleProperties = Context.getConfigProperties();
		
		// override or initialize config properties with module-provided ones
		for (Map.Entry<Object, Object> entry : moduleProperties.entrySet()) {
			Object key = entry.getKey();
			String prop = (String) key;
			String value = (String) entry.getValue();
			log.trace("Setting module property: " + prop + ":" + value);
			config.setProperty(prop, value);
			if (!prop.startsWith("hibernate")) {
				config.setProperty("hibernate." + prop, value);
			}
		}
		
		Properties properties = Context.getRuntimeProperties();
		
		// loop over runtime properties and override each in the configuration
		for (Map.Entry<Object, Object> entry : properties.entrySet()) {
			Object key = entry.getKey();
			String prop = (String) key;
			String value = (String) entry.getValue();
			log.trace("Setting property: " + prop + ":" + value);
			config.setProperty(prop, value);
			if (!prop.startsWith("hibernate")) {
				config.setProperty("hibernate." + prop, value);
			}
		}
		
		// load in the default hibernate properties
		try {
			InputStream propertyStream = getClass().getResourceAsStream("/hibernate.default.properties");
			Properties props = new Properties();
			
			OpenmrsUtil.loadProperties(props, propertyStream);
			propertyStream.close();
			
			// Only load in the default properties if they don't exist
			for (Entry<Object, Object> prop : props.entrySet()) {
				if (!config.containsKey(prop.getKey())) {
					config.put(prop.getKey(), prop.getValue());
				}
			}
			
		}
		catch (IOException e) {
			log.fatal("Unable to load default hibernate properties", e);
		}
		
		log.debug("Replacing variables in hibernate properties");
		final String applicationDataDirectory = OpenmrsUtil.getApplicationDataDirectory();
		for (Entry<Object, Object> entry : config.entrySet()) {
			String value = (String) entry.getValue();
			
			value = value.replace("%APPLICATION_DATA_DIRECTORY%", applicationDataDirectory);
			entry.setValue(value);
		}
		
		log.debug("Setting global Hibernate Session Interceptor for SessionFactory, Interceptor: " + chainingInterceptor);
		
		// make sure all autowired interceptors are put onto our chaining interceptor
		// sort on the keys so that the devs/modules have some sort of control over the order of the interceptors 
		List<String> keys = new ArrayList<String>(interceptors.keySet());
		Collections.sort(keys);
		for (String key : keys) {
			chainingInterceptor.addInterceptor(interceptors.get(key));
		}
		
		setEntityInterceptor(chainingInterceptor);
		
		//Adding each module's mapping file to the list of mapping resources
		setMappingResources(getModuleMappingResources().toArray(new String[0]));
		
		setPackagesToScan(getModulePackagesWithMappedClasses().toArray(new String[0]));
		
		super.afterPropertiesSet();
	}
	
	/**
	 * @see org.springframework.orm.hibernate3.LocalSessionFactoryBean#destroy()
	 */
	@Override
	public void destroy() throws HibernateException {
		try {
			super.destroy();
		}
		catch (IllegalStateException e) {
			// ignore errors sometimes thrown by the CacheManager trying to shut down twice
			// see net.sf.ehcache.CacheManager#removeShutdownHook()
		}
	}
	
}
