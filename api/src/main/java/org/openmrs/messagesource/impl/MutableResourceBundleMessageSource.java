/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.messagesource.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.openmrs.messagesource.MutableMessageSource;
import org.openmrs.messagesource.PresentationMessage;
import org.openmrs.module.Module;
import org.openmrs.module.ModuleClassLoader;
import org.openmrs.module.ModuleFactory;
import org.openmrs.util.LocaleUtility;
import org.openmrs.util.OpenmrsClassLoader;
import org.openmrs.util.OpenmrsUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

/**
 * ResourceBundleMessageSource extends ReloadableResourceBundleMessageSource to provide the
 * additional features of a MutableMessageSource.
 */
public class MutableResourceBundleMessageSource extends ReloadableResourceBundleMessageSource implements MutableMessageSource {

	private static final Logger log = LoggerFactory.getLogger(MutableResourceBundleMessageSource.class);
	
	/**
	 * Local reference to basenames used to search for properties files.
	 */
	private String[] basenames = new String[0];
	
	private int cacheMilliseconds = -1;
	
	private long lastCached = System.currentTimeMillis();
	
	/** Cached list of available locales. */
	private Collection<Locale> locales;
	
	/**
	 * @see org.openmrs.messagesource.MessageSourceService#getLocales()
	 */
	@Override
	public Collection<Locale> getLocales() {
		long now = System.currentTimeMillis();
		if (locales == null || cacheMilliseconds <= 0 || now - cacheMilliseconds > lastCached) {
			locales = findLocales();
			lastCached = now;
		}
		
		return locales;
	}
	
	@Override
	public void setCacheSeconds(int cacheSeconds) {
		this.cacheMilliseconds = cacheSeconds * 1000;
		super.setCacheSeconds(cacheSeconds);
	}
	
	/**
	 * This method looks at the current property files and deduces what locales are available from
	 * those
	 *
	 * @see #getLocales()
	 * @see #findPropertiesFiles()
	 */
	private Collection<Locale> findLocales() {
		Collection<Locale> foundLocales = new HashSet<>();
		
		for (Resource propertiesFile : findPropertiesFiles()) {
			String filename = propertiesFile.getFilename();
			
			Locale parsedLocale = parseLocaleFrom(filename);
			
			foundLocales.add(parsedLocale);
			
		}
		
		if (foundLocales.isEmpty()) {
			log.warn("no locales found.");
		}
		return foundLocales;
	}
	
	/**
	 * Utility method for deriving a locale from a filename, presumed to have an embedded locale
	 * specification near the end. For instance messages_it.properties if the filename is
	 * messages.properties, the Locale is presumed to be the default set for Java
	 *
	 * @param filename the name to parse
	 * @return Locale derived from the given string
	 */
	private Locale parseLocaleFrom(String filename) {
		Locale parsedLocale;
		
		// trim off leading basename
		filename = filename.substring("messages".length());
		
		// trim off extension
		String localespec = filename.substring(0, filename.indexOf('.'));
		
		if ("".equals(localespec)) {
			parsedLocale = Locale.getDefault();
		} else {
			localespec = localespec.substring(1); // trim off leading '_'
			parsedLocale = LocaleUtility.fromSpecification(localespec);
		}
		return parsedLocale;
	}
	
	/**
	 * Returns all available messages.
	 *
	 * @see org.openmrs.messagesource.MessageSourceService#getPresentations()
	 */
	@Override
	public Collection<PresentationMessage> getPresentations() {
		Collection<PresentationMessage> presentations = new ArrayList<>();
		
		for (Resource propertiesFile : findPropertiesFiles()) {
			Locale currentLocale = parseLocaleFrom(propertiesFile.getFilename());
			Properties props = new Properties();
			try {
				OpenmrsUtil.loadProperties(props, propertiesFile.getInputStream());
				for (Map.Entry<Object, Object> property : props.entrySet()) {
					presentations.add(new PresentationMessage(property.getKey().toString(), currentLocale, property
					        .getValue().toString(), ""));
				}
			}
			catch (Exception e) {
				// skip over errors in loading a single file
				log.error("Unable to load properties from file: " + propertiesFile.getFilename(), e);
			}
		}
		return presentations;
	}
	
	/**
	 * Override to obtain a local reference to the basenames.
	 *
	 * @see org.springframework.context.support.ReloadableResourceBundleMessageSource#setBasename(java.lang.String)
	 */
	@Override
	public void setBasename(String basename) {
		super.setBasename(basename);
		this.basenames = new String[] { basename };
	}
	
	/**
	 * Override to obtain a local reference to the basenames.
	 *
	 * @see org.springframework.context.support.ReloadableResourceBundleMessageSource#setBasenames(java.lang.String[])
	 */
	@Override
	public void setBasenames(String... basenames) {
		if (basenames == null) {
			this.basenames = new String[0];
		} else {
			this.basenames = Arrays.copyOf(basenames, basenames.length);
		}
		
		//add module file urls to basenames used for locating message properties files
		Collection<Module> modules = ModuleFactory.getStartedModules();
		if (!modules.isEmpty()) {
			String[] names =  new String[this.basenames.length + modules.size()];
			System.arraycopy(this.basenames, 0, names, 0, this.basenames.length);
			int index = this.basenames.length;
			for (Module module : modules) {
				names[index] = "jar:file:" + module.getFile().getAbsolutePath() + "!/messages";
				index++;
			}
			
			basenames = names;
		}
		
		super.setBasenames(basenames);
	}
	
	/**
	 * @see org.openmrs.messagesource.MutableMessageSource#addPresentation(org.openmrs.messagesource.PresentationMessage)
	 */
	@Override
	public void addPresentation(PresentationMessage message) {
		Resource propertyFile = findPropertiesFileFor(message.getCode());
		if (propertyFile != null) {
			Properties props = new Properties();
			try {
				OpenmrsUtil.loadProperties(props, propertyFile.getInputStream());
				props.setProperty(message.getCode(), message.getMessage());
				
				//TODO properties files are now in api jar files which cannot be modified. TRUNK-4097
				//We should therefore remove this method implementation or stop claiming that we are a mutable resource
				//OpenmrsUtil.storeProperties(props, propertyFile.getInputStream(), "OpenMRS Application Messages");
			}
			catch (Exception e) {
				log.error("Error generated", e);
			}
		}
	}
	
	/**
	 * @see org.openmrs.messagesource.MutableMessageSource#removePresentation(org.openmrs.messagesource.PresentationMessage)
	 */
	@Override
	public void removePresentation(PresentationMessage message) {
		Resource propertyFile = findPropertiesFileFor(message.getCode());
		if (propertyFile != null) {
			Properties props = new Properties();
			try {
				OpenmrsUtil.loadProperties(props, propertyFile.getInputStream());
				props.remove(message.getCode());
				
				//TODO properties files are now in api jar files which cannot be modified. TRUNK-4097
				//We should therefore remove this method implementation or stop claiming that we are a mutable resource
				//OpenmrsUtil.storeProperties(props, propertyFile, PROPERTIES_FILE_COMMENT);
			}
			catch (Exception e) {
				log.error("Error generated", e);
			}
		}
	}
	
	/**
	 * Convenience method to scan the available properties files, looking for the one that has a
	 * definition for the given code.
	 *
	 * @param code
	 * @return the file which defines the code, or null if not found
	 */
	private Resource findPropertiesFileFor(String code) {
		Properties props = new Properties();
		Resource foundFile = null;
		
		for (Resource propertiesFile : findPropertiesFiles()) {
			props.clear();
			try {
				OpenmrsUtil.loadProperties(props, propertiesFile.getInputStream());
			}
			catch (Exception e) {
				log.error("Error generated", e);
			}
			if (props.containsKey(code)) {
				foundFile = propertiesFile;
				break;
			}
		}
		return foundFile;
	}
	
	/**
	 * Searches the filesystem for message properties files. ABKTODO: consider caching this, rather
	 * than searching every time
	 *
	 * @return an array of property file names
	 */
	private Resource[] findPropertiesFiles() {
		Set<Resource> resourceSet = new HashSet<>();
		try {
			String pattern = "classpath*:messages*.properties";
			ResourcePatternResolver resourceResolver = new PathMatchingResourcePatternResolver(OpenmrsClassLoader.getInstance());
			Resource[] propertiesFiles = resourceResolver.getResources(pattern);
			Collections.addAll(resourceSet, propertiesFiles);
			
			for (ModuleClassLoader moduleClassLoader : ModuleFactory.getModuleClassLoaders()) {
				resourceResolver = new PathMatchingResourcePatternResolver(moduleClassLoader);
				propertiesFiles = resourceResolver.getResources(pattern);
				Collections.addAll(resourceSet, propertiesFiles);
			}
		}
		catch (IOException e) {
			log.error("Error generated", e);
		}
		if (log.isWarnEnabled() && (resourceSet.isEmpty())) {
			log.warn("No properties files found.");
		}
		return resourceSet.toArray(new Resource[resourceSet.size()]);
	}
	
	/**
	 * @see org.openmrs.messagesource.MutableMessageSource#merge(MutableMessageSource, boolean)
	 */
	@Override
	public void merge(MutableMessageSource fromSource, boolean overwrite) {
		
		// collect all existing properties
		Resource[] propertiesFiles = findPropertiesFiles();
		Map<Locale, List<Resource>> localeToFilesMap = new HashMap<>();
		Map<Resource, Properties> fileToPropertiesMap = new HashMap<>();
		
		for (Resource propertiesFile : propertiesFiles) {
			Properties props = new Properties();
			Locale propsLocale = parseLocaleFrom(propertiesFile.getFilename());
			List<Resource> propList = localeToFilesMap.computeIfAbsent(propsLocale, k -> new ArrayList<>());
			propList.add(propertiesFile);
			
			try {
				OpenmrsUtil.loadProperties(props, propertiesFile.getInputStream());
				fileToPropertiesMap.put(propertiesFile, props);
			}
			catch (Exception e) {
				// skip over errors in loading a single file
				log.error("Unable to load properties from file: " + propertiesFile.getFilename(), e);
			}
		}
		
		// merge in the new properties
		for (PresentationMessage message : fromSource.getPresentations()) {
			Locale messageLocale = message.getLocale();
			
			List<Resource> filelist = localeToFilesMap.get(messageLocale);
			if (filelist != null) {
				Properties propertyDestination = null;
				boolean propExists = false;
				for (Resource propertiesFile : filelist) {
					Properties possibleDestination = fileToPropertiesMap.get(propertiesFile);
					
					if (possibleDestination.containsKey(message.getCode())) {
						propertyDestination = possibleDestination;
						propExists = true;
						break;
					} else if (propertyDestination == null) {
						propertyDestination = possibleDestination;
					}
				}
				if (!propExists || overwrite) {
					propertyDestination.put(message.getCode(), message.getMessage());
				}
				
			}
			
			message.getCode();
		}
	}
	
	/**
	 * @see org.openmrs.messagesource.MutableMessageSource#getPresentation(java.lang.String,
	 *      java.util.Locale)
	 */
	@Override
	public PresentationMessage getPresentation(String key, Locale forLocale) {
		// TODO Auto-generated method stub
		return null;
	}
	
	/**
	 * @see org.openmrs.messagesource.MutableMessageSource#getPresentationsInLocale(java.util.Locale)
	 */
	@Override
	public Collection<PresentationMessage> getPresentationsInLocale(Locale locale) {
		// TODO Auto-generated method stub
		return null;
	}
	
}
