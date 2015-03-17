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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.messagesource.MutableMessageSource;
import org.openmrs.messagesource.PresentationMessage;
import org.openmrs.util.LocaleUtility;
import org.openmrs.util.OpenmrsUtil;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.core.io.Resource;

/**
 * ResourceBundleMessageSource extends ReloadableResourceBundleMessageSource to provide the
 * additional features of a MutableMessageSource.
 */
public class MutableResourceBundleMessageSource extends ReloadableResourceBundleMessageSource implements MutableMessageSource, ApplicationContextAware {
	
	private static final String PROPERTIES_FILE_COMMENT = "OpenMRS Application Messages";
	
	private Log log = LogFactory.getLog(getClass());
	
	private ApplicationContext applicationContext;
	
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
		Collection<Locale> foundLocales = new HashSet<Locale>();
		
		for (File propertiesFile : findPropertiesFiles()) {
			String filename = propertiesFile.getName();
			
			Locale parsedLocale = parseLocaleFrom(filename);
			
			foundLocales.add(parsedLocale);
			
		}
		
		if (foundLocales.size() == 0) {
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
		Locale parsedLocale = null;
		
		// trim off leading basename
		for (String basename : basenames) {
			File basefilename = new File(basename);
			basename = basefilename.getPath();
			
			int indexOfLastPart = basename.lastIndexOf(File.separatorChar) + 1;
			if (indexOfLastPart > 0) {
				basename = basename.substring(indexOfLastPart);
			}
			
			if (filename.startsWith(basename)) {
				filename = filename.substring(basename.length());
			}
		}
		
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
	 * Presumes to append the messages to a message.properties file which is already being monitored
	 * by the super ReloadableResourceBundleMessageSource. This is a blind, trusting hack.
	 *
	 * @see org.openmrs.messagesource.MutableMessageSource#publishProperties(java.util.Properties,
	 *      java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 * @deprecated use {@linkplain #merge(MutableMessageSource, boolean)}
	 */
	@Deprecated
	public void publishProperties(Properties props, String locale, String namespace, String name, String version) {
		
		String filePrefix = (namespace.length() > 0) ? (namespace + "_") : "";
		String propertiesPath = "/WEB-INF/" + filePrefix + "messages" + locale + ".properties";
		
		Resource propertiesResource = applicationContext.getResource(propertiesPath);
		try {
			File propertiesFile = propertiesResource.getFile();
			
			if (!propertiesFile.exists()) {
				propertiesFile.createNewFile();
			}
			// append the properties to the appropriate messages file
			OpenmrsUtil.storeProperties(props, propertiesFile, namespace + ": " + name + " v" + version);
			
		}
		catch (Exception ex) {
			log.error("Error creating new properties file");
		}
	}
	
	/**
	 * @see org.springframework.context.ApplicationContextAware#setApplicationContext(org.springframework.context.ApplicationContext)
	 */
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
		
	}
	
	/**
	 * Returns all available messages.
	 *
	 * @see org.openmrs.messagesource.MessageSourceService#getPresentations()
	 */
	public Collection<PresentationMessage> getPresentations() {
		Collection<PresentationMessage> presentations = new Vector<PresentationMessage>();
		
		for (File propertiesFile : findPropertiesFiles()) {
			Locale currentLocale = parseLocaleFrom(propertiesFile.getName());
			Properties props = new Properties();
			try {
				OpenmrsUtil.loadProperties(props, propertiesFile);
				for (Map.Entry<Object, Object> property : props.entrySet()) {
					presentations.add(new PresentationMessage(property.getKey().toString(), currentLocale, property
					        .getValue().toString(), ""));
				}
			}
			catch (Exception e) {
				// skip over errors in loading a single file
				log.error("Unable to load properties from file: " + propertiesFile.getAbsolutePath(), e);
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
	public void setBasenames(String[] basenames) {
		super.setBasenames(basenames);
		if (basenames == null) {
			this.basenames = new String[0];
		} else {
			this.basenames = Arrays.copyOf(basenames, basenames.length);
		}
	}
	
	/**
	 * @see org.openmrs.messagesource.MutableMessageSource#addPresentation(org.openmrs.messagesource.PresentationMessage)
	 */
	public void addPresentation(PresentationMessage message) {
		File propertyFile = findPropertiesFileFor(message.getCode());
		if (propertyFile != null) {
			Properties props = new Properties();
			try {
				OpenmrsUtil.loadProperties(props, propertyFile);
				props.setProperty(message.getCode(), message.getMessage());
				OpenmrsUtil.storeProperties(props, propertyFile, "OpenMRS Application Messages");
			}
			catch (Exception e) {
				log.error("Error generated", e);
			}
		}
	}
	
	/**
	 * @see org.openmrs.messagesource.MutableMessageSource#removePresentation(org.openmrs.messagesource.PresentationMessage)
	 */
	public void removePresentation(PresentationMessage message) {
		File propertyFile = findPropertiesFileFor(message.getCode());
		if (propertyFile != null) {
			Properties props = new Properties();
			try {
				OpenmrsUtil.loadProperties(props, propertyFile);
				props.remove(message.getCode());
				OpenmrsUtil.storeProperties(props, propertyFile, PROPERTIES_FILE_COMMENT);
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
	private File findPropertiesFileFor(String code) {
		Properties props = new Properties();
		File foundFile = null;
		
		for (File propertiesFile : findPropertiesFiles()) {
			props.clear();
			try {
				OpenmrsUtil.loadProperties(props, propertiesFile);
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
	 * @return collection of property file names
	 */
	private Collection<File> findPropertiesFiles() {
		Collection<File> propertiesFiles = new Vector<File>();
		
		try {
			for (String basename : basenames) {
				File basefilename = new File(basename);
				basename = basefilename.getPath();
				int nameIndex = basename.lastIndexOf(File.separatorChar) + 1;
				String basedir = (nameIndex > 0) ? basename.substring(0, nameIndex) : "";
				String namePrefix = basename.substring(nameIndex);
				Resource propertiesDir = applicationContext.getResource(basedir);
				boolean filesFound = false;
				if (propertiesDir.exists()) {
					for (File possibleFile : propertiesDir.getFile().listFiles()) {
						if (possibleFile.getName().startsWith(namePrefix) && possibleFile.getName().endsWith(".properties")) {
							propertiesFiles.add(possibleFile);
							filesFound = true;
						}
					}
				} else {
					if (log.isDebugEnabled()) {
						log.debug("Parent directory " + propertiesDir + " does not exist");
					}
				}
				
				if (log.isDebugEnabled() && !filesFound) {
					log.debug("No messages for basename " + basename);
				}
			}
		}
		catch (IOException e) {
			log.error("Error generated", e);
		}
		if (log.isWarnEnabled() && (propertiesFiles.size() == 0)) {
			log.warn("No properties files found.");
		}
		return propertiesFiles;
	}
	
	/**
	 * @see org.openmrs.messagesource.MutableMessageSource#merge(MutableMessageSource, boolean)
	 */
	public void merge(MutableMessageSource fromSource, boolean overwrite) {
		
		// collect all existing properties
		Collection<File> propertiesFiles = findPropertiesFiles();
		Map<Locale, List<File>> localeToFilesMap = new HashMap<Locale, List<File>>();
		Map<File, Properties> fileToPropertiesMap = new HashMap<File, Properties>();
		
		for (File propertiesFile : propertiesFiles) {
			Properties props = new Properties();
			Locale propsLocale = parseLocaleFrom(propertiesFile.getName());
			List<File> propList = localeToFilesMap.get(propsLocale);
			if (propList == null) {
				propList = new ArrayList<File>();
				localeToFilesMap.put(propsLocale, propList);
			}
			propList.add(propertiesFile);
			
			try {
				OpenmrsUtil.loadProperties(props, propertiesFile);
				fileToPropertiesMap.put(propertiesFile, props);
			}
			catch (Exception e) {
				// skip over errors in loading a single file
				log.error("Unable to load properties from file: " + propertiesFile.getAbsolutePath(), e);
			}
		}
		
		// merge in the new properties
		for (PresentationMessage message : fromSource.getPresentations()) {
			Locale messageLocale = message.getLocale();
			
			List<File> filelist = localeToFilesMap.get(messageLocale);
			if (filelist != null) {
				Properties propertyDestination = null;
				boolean propExists = false;
				for (File propertiesFile : filelist) {
					Properties possibleDestination = fileToPropertiesMap.get(propertiesFile);
					
					if (possibleDestination.containsKey(message.getCode())) {
						propertyDestination = possibleDestination;
						propExists = true;
						break;
					} else if (propertyDestination == null) {
						propertyDestination = possibleDestination;
					}
				}
				if ((propExists && overwrite) || !propExists) {
					propertyDestination.put(message.getCode(), message.getMessage());
				}
				
			} else {
				// no properties files for this locale, create one
				File newPropertiesFile = new File(basenames[0] + "_" + messageLocale.toString() + ".properties");
				Properties newProperties = new Properties();
				fileToPropertiesMap.put(newPropertiesFile, newProperties);
				newProperties.put(message.getCode(), message.getMessage());
				List<File> newFilelist = new ArrayList<File>();
				newFilelist.add(newPropertiesFile);
				localeToFilesMap.put(messageLocale, newFilelist);
			}
			
			message.getCode();
		}
	}
	
	/**
	 * @see org.openmrs.messagesource.MutableMessageSource#getPresentation(java.lang.String,
	 *      java.util.Locale)
	 */
	public PresentationMessage getPresentation(String key, Locale forLocale) {
		// TODO Auto-generated method stub
		return null;
	}
	
	/**
	 * @see org.openmrs.messagesource.MutableMessageSource#getPresentationsInLocale(java.util.Locale)
	 */
	public Collection<PresentationMessage> getPresentationsInLocale(Locale locale) {
		// TODO Auto-generated method stub
		return null;
	}
	
}
