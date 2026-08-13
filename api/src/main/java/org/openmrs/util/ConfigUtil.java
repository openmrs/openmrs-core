/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.StringUtils;
import org.openmrs.GlobalProperty;
import org.openmrs.api.GlobalPropertyListener;
import org.openmrs.api.context.Context;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

/**
 * A utility class for working with configuration properties
 */
public class ConfigUtil implements GlobalPropertyListener, ApplicationListener<ContextRefreshedEvent> {

	/**
	 * Cache of global property key/value pairs to enable lookups that do not require accessing the
	 * service each time. Uses a concurrent map so that concurrent reads and updates from the
	 * {@link GlobalPropertyListener} callbacks cannot corrupt it. Missing properties are deliberately
	 * not cached so that a property created after the first read is picked up rather than being pinned
	 * to null.
	 */
	private static final Map<String, String> globalPropertyCache = new ConcurrentHashMap<>();

	/**
	 * Gets the value of the given OpenMRS global property
	 */
	public static String getGlobalProperty(String propertyName) {
		String cachedValue = globalPropertyCache.get(propertyName);
		if (cachedValue != null) {
			return cachedValue;
		}
		String value = Context.getAdministrationService().getGlobalProperty(propertyName);
		if (value != null) {
			globalPropertyCache.putIfAbsent(propertyName, value);
		}
		return value;
	}

	/**
	 * Clears the cache of global property values. Invoked on context refresh via
	 * {@link #onApplicationEvent} so that cached values cannot outlive the context that populated them.
	 */
	public static void clearGlobalPropertyCache() {
		globalPropertyCache.clear();
	}

	/**
	 * Returns the value of the given OpenMRS runtime property
	 */
	public static String getRuntimeProperty(String propertyName) {
		return Context.getRuntimeProperties().getProperty(propertyName);
	}

	/**
	 * Returns true if a runtime property with the given name has been defined, even if the value is
	 * empty
	 */
	public static boolean hasRuntimeProperty(String propertyName) {
		return Context.getRuntimeProperties().containsKey(propertyName);
	}

	/**
	 * Returns the value of the given OpenMRS system property
	 */
	public static String getSystemProperty(String propertyName) {
		return System.getProperty(propertyName);
	}

	/**
	 * Returns true if a system property with the given name has been defined, even if the value is
	 * empty
	 */
	public static boolean hasSystemProperty(String propertyName) {
		return System.getProperties().containsKey(propertyName);
	}

	/**
	 * Returns the value of the given configuration property. This will check the OpenMRS global
	 * properties, OpenMRS runtime properties, and any defined system properties. In the event that a
	 * property is defined in multiple places, the order of precedence is system properties, then
	 * runtime properties, then global properties
	 */
	public static String getProperty(String propertyName) {
		if (hasSystemProperty(propertyName)) {
			return getSystemProperty(propertyName);
		}
		if (hasRuntimeProperty(propertyName)) {
			return getRuntimeProperty(propertyName);
		}
		return getGlobalProperty(propertyName);
	}

	/**
	 * Returns the value of the given configuration property. This will check the OpenMRS global
	 * properties, OpenMRS runtime properties, and any defined system properties. In the event that a
	 * property is defined in multiple places, the order of precedence is system properties, then
	 * runtime properties, then global properties If the value found is null, empty, or only whitespace,
	 * then the default value is returned
	 */
	public static String getProperty(String propertyName, String defaultValue) {
		String value = getProperty(propertyName);
		if (StringUtils.isBlank(value)) {
			value = defaultValue;
		}
		return value;
	}

	/**
	 * Operates as above but returns the Boolean value of the property
	 *
	 * @param propertyName
	 * @param defaultValue
	 * @return
	 */
	public static boolean getProperty(String propertyName, boolean defaultValue) {
		String value = getProperty(propertyName);
		if (StringUtils.isBlank(value)) {
			return defaultValue;
		}
		return Boolean.parseBoolean(value);
	}

	@Override
	public void globalPropertyChanged(GlobalProperty newValue) {
		if (newValue.getPropertyValue() == null) {
			globalPropertyCache.remove(newValue.getProperty());
		} else {
			globalPropertyCache.put(newValue.getProperty(), newValue.getPropertyValue());
		}
	}

	@Override
	public void globalPropertyDeleted(String propertyName) {
		globalPropertyCache.remove(propertyName);
	}

	@Override
	public boolean supportsPropertyName(String propertyName) {
		return true;
	}

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		clearGlobalPropertyCache();
	}
}
