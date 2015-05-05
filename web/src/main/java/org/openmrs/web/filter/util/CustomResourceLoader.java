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

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.util.LocaleUtility;

/**
 * This class is responsible for loading messages resources from file system
 */
public class CustomResourceLoader {
	
	private static final Log log = LogFactory.getLog(CustomResourceLoader.class);
	
	/** */
	public static final String PREFIX = "messages";
	
	/** */
	public static final String SUFFIX = ".properties";
	
	/** the map that contains resource bundles for each locale */
	private Map<Locale, ResourceBundle> resources = null;
	
	/** the set of languages, which is currently supported */
	private Set<Locale> availablelocales = null;
	
	private static CustomResourceLoader instance = null;
	
	/**
	 * default constructor that initializes inner map of resources
	 */
	private CustomResourceLoader(HttpServletRequest httpRequest) {
		this.resources = new HashMap<Locale, ResourceBundle>();
		this.availablelocales = new HashSet<Locale>();
		String basePath = null;
		if (httpRequest != null) {
			basePath = httpRequest.getSession().getServletContext().getRealPath("/WEB-INF");
		}
		File basedir = null;
		if (StringUtils.isBlank(basePath)) {
			basedir = new File(getClass().getClassLoader().getResource("").getPath()).getParentFile();
		} else {
			basedir = new File(basePath);
		}
		loadResources(basedir.getAbsolutePath());
	}
	
	/**
	 * Returns singleton instance of custom resource loader
	 *
	 * @param basedir <b>(optional)</b> the absolute path to directory, that contains resources to
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
	 * This method is intended to load resource bundle from the file system by specified messages
	 * properties file path and for specified locale
	 *
	 * @param path location of the resource on the file system
	 * @param basename the name prefix for resource file
	 * @param locale the location parameter
	 * @return resource bundle object if success, otherwise null. Error message is passed out
	 *         through <code>errorMsg</code> property
	 */
	protected ResourceBundle getFileSystemResource(String path, String basename, Locale locale) {
		File resourceFile = new File(path);
		try {
			FileInputStream fileInputStream = new FileInputStream(resourceFile);
			return new PropertyResourceBundle(new InputStreamReader(fileInputStream, "UTF-8"));
		}
		catch (Exception e) {
			log.warn("Unable to load bundle by path " + path + ", because of ", e);
		}
		return null;
	}
	
	/**
	 * Searches under the base directory on the file system for possible message properties files
	 * and loads them. <br />
	 * <br />
	 * It iterates over each file, nested to the base directory, and decides if this file is a
	 * messages properties. Then, if file is suitable, it parses the locale from its name. And
	 * finally, it loads resource bundle for that file and associates it with locale, derived from
	 * the file's name.
	 *
	 * @param basedir the absolute path of base directory to search files (e.g. $CONTEXT_ROOT +
	 *            /WEB_INF/)
	 */
	private void loadResources(String basedir) {
		File propertiesDir = new File(basedir);
		for (File possibleFile : propertiesDir.listFiles()) {
			if (possibleFile.getName().startsWith(PREFIX) && possibleFile.getName().endsWith(SUFFIX)) {
				Locale locale = parseLocaleFrom(possibleFile.getName(), PREFIX);
				ResourceBundle rb = getFileSystemResource(possibleFile.getAbsolutePath(), PREFIX, locale);
				if (rb != null) {
					getResource().put(locale, rb);
					getAvailablelocales().add(locale);
				}
			}
		}
		if (log.isWarnEnabled() && (getResource().size() == 0)) {
			log.warn("No properties files found.");
		}
	}
	
	/**
	 * Utility method for deriving a locale from a filename.
	 *
	 * @param filename the name to parse
	 * @return Locale derived from the given string
	 */
	private Locale parseLocaleFrom(String filename, String basename) {
		Locale result = null;
		
		if (filename.startsWith(basename)) {
			filename = filename.substring(basename.length());
		}
		
		String localespec = filename.substring(0, filename.indexOf('.'));
		
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
