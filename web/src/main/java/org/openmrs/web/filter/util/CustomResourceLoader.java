/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web.filter.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.util.*;

import javax.servlet.http.HttpServletRequest;

import org.openmrs.util.LocaleUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

/**
 * This class is responsible for loading messages resources from file system
 */
public class CustomResourceLoader {
	
	private static final Logger log = LoggerFactory.getLogger(CustomResourceLoader.class);
	
	/** */
	public static final String PREFIX = "messages";
	public static final String CLASSPATH_MESSAGES_PROPERTIES = "classpath*:messages*.properties";
	public static final String CLASSPATH_OVERRIDE_MESSAGES_PROPERTIES = "classpath*:override-messages/%s_override.properties";
	
	/** the map that contains resource bundles for each locale */
	private Map<Locale, ResourceBundle> resources;
	
	/** the set of languages, which is currently supported */
	private Set<Locale> availablelocales;
	
	private static CustomResourceLoader instance = null;
	
	/**
	 * default constructor that initializes inner map of resources
	 */
	private CustomResourceLoader(HttpServletRequest httpRequest) {
		this.resources = new HashMap<>();
		this.availablelocales = new HashSet<>();
		this.resources = updateResources(CLASSPATH_MESSAGES_PROPERTIES,CLASSPATH_OVERRIDE_MESSAGES_PROPERTIES);
		this.availablelocales = this.resources.keySet();
	}

	/**
	 * sets values from property file into Provided maps. Acts as helper in testing 
	 */
	public Map<Locale, ResourceBundle> updateResources(String mainClassPath,String overrideClassPath){
		Map<Locale, ResourceBundle> resources = new HashMap<>();
		Set<Locale> availablelocales = new HashSet<>();
		try {
			PathMatchingResourcePatternResolver patternResolver = new PathMatchingResourcePatternResolver();
			Resource[] localResources = patternResolver.getResources(mainClassPath);
			for (Resource localeResource : localResources) {
				Locale locale = parseLocaleFrom(localeResource.getFilename(), PREFIX);
				InputStream defaultStream = localeResource.getInputStream();
				String actualOverrideClassPath = String.format(overrideClassPath,locale);
				List<InputStream> propertyStreams = new ArrayList<>();
				// add default property at beginning
				propertyStreams.add(defaultStream);
				Resource[] localOverrideResources = patternResolver.getResources(actualOverrideClassPath);
				for( Resource localOverrideResource: localOverrideResources){
					propertyStreams.add(localOverrideResource.getInputStream());
				}
				Enumeration<InputStream> enumeratedStreams = Collections.enumeration(propertyStreams);
				SequenceInputStream sequenceInputStream = new SequenceInputStream(enumeratedStreams);
				ResourceBundle rb = new PropertyResourceBundle(sequenceInputStream);
				resources.put(locale, rb);
				availablelocales.add(locale);
			}
		}
		catch (IOException ex) {
			log.error(ex.getMessage(), ex);
		}
		return resources;
	}
	
	/**
	 * Returns singleton instance of custom resource loader
	 *
	 * @param httpRequest <b>(optional)</b> the absolute path to directory, that contains resources to
	 *            be loaded. If this isn't specified then <code>${CONTEXT-ROOT}/WEB-INF/</code> will
	 *            be used
	 * @return the singleton instance of {@link CustomResourceLoader}
	 */
	public static CustomResourceLoader getInstance(HttpServletRequest httpRequest) {
		if (instance == null) {
			instance = new CustomResourceLoader(httpRequest);
		}
		return instance;
	}
	
	/**
	 * Utility method for deriving a locale from a filename.
	 *
	 * @param filename the name to parse
	 * @return Locale derived from the given string
	 */
	private Locale parseLocaleFrom(String filename, String basename) {
		Locale result;
		String tempFilename = filename;
		
		if (filename.startsWith(basename)) {
			tempFilename = filename.substring(basename.length());
		}
		
		String localespec = tempFilename.substring(0, tempFilename.indexOf('.'));
		
		if ("".equals(localespec)) {
			result = Locale.ENGLISH;
		} else {
			localespec = localespec.substring(1);
			result = LocaleUtility.fromSpecification(localespec);
		}
		return result;
	}
	
	/**
	 * @param locale the locale for which will be retrieved resource bundle
	 * @return resource bundle for specified locale
	 */
	public ResourceBundle getResourceBundle(Locale locale) {
		return resources.get(locale);
	}
	
	/**
	 * @return the map object, which contains locale as key and resources bundle for each locale as
	 *         value
	 */
	public Map<Locale, ResourceBundle> getResource() {
		return resources;
	}
	
	/**
	 * @return the set of locales which are currently supported by OpenMRS
	 */
	public Set<Locale> getAvailablelocales() {
		return availablelocales;
	}
}
