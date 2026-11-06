/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.logging;

import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.lookup.AbstractLookup;
import org.apache.logging.log4j.core.lookup.StrLookup;
import org.apache.logging.log4j.status.StatusLogger;
import org.openmrs.api.context.Context;
import org.openmrs.util.ConfigUtil;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.util.OpenmrsUtil;
import org.openmrs.util.PrivilegeConstants;

/**
 * This class exposes a subset of OpenMRS properties to the log4j context configuration. This is
 * intended to allow the logger to make use of certain OpenMRS properties.
 * <p/>
 * To use these properties in your logger configuration, reference them like
 * <tt>${openmrs:&lt;property&gt;}</tt>, e.g. <tt>${openmrs:applicationDirectory}</tt>.
 * <p/>
 * Supported properties:
 * <dl>
 * <dt>applicationDirectory</dt>
 * <dd>The OpenMRS application directory as a string</dd>
 * <dt>logLocation</dt>
 * <dd>The current value for the <tt>log.location</tt> setting</dd>
 * <dt>logLayout</dt>
 * <dd>The current value for the <tt>log.layout</tt> setting</dd>
 * </dl>
 * <p/>
 * Care should be taken in exposing information through this class to ensure that no secrets are
 * leaked or properties that expose potentially unsafe operations based on user input.
 */
@Plugin(name = OpenmrsPropertyLookup.NAME, category = StrLookup.CATEGORY)
@SuppressWarnings("unused")
public class OpenmrsPropertyLookup extends AbstractLookup {

	public static final String NAME = "openmrs";

	@Override
	public String lookup(LogEvent event, String key) {
		switch (key) {
			case "applicationDirectory":
				final String applicationDirectory = OpenmrsUtil.getApplicationDataDirectory();
				return applicationDirectory == null || applicationDirectory.isEmpty() ? null : applicationDirectory;
			case "logLocation":
				final String logLocation = getProperty(OpenmrsConstants.GP_LOG_LOCATION);
				return logLocation == null ? null
				        : logLocation.endsWith("/") ? logLocation.substring(0, logLocation.length() - 1) : logLocation;
			case "logLayout":
				return getProperty(OpenmrsConstants.GP_LOG_LAYOUT);
			default:
				StatusLogger.getLogger().error(
				    "{} is not a supported property. We support openmrs:applicationDirectory, openmrs:logLocation, and openmrs:logLayout",
				    key);
				return null;
		}
	}

	private String getProperty(String propertyName) {
		String value = ConfigUtil.getSystemProperty(propertyName);
		if (value == null) {
			value = ConfigUtil.getRuntimeProperty(propertyName);
		}

		if (value == null && Context.isSessionOpen()) {
			Context.addProxyPrivilege(PrivilegeConstants.GET_GLOBAL_PROPERTIES);
			try {
				value = ConfigUtil.getGlobalProperty(propertyName);
			} catch (RuntimeException e) {
				// Resolving a global property requires the service layer, which may be unavailable while
				// the logging system is being configured - in particular before, or re-entrantly during,
				// ServiceContext initialization. Logging configuration must be resilient, so swallow the
				// failure (e.g. ServiceNotFoundException, or a re-entrant initialization error) and fall
				// back to the default below.
				StatusLogger.getLogger().warn("Could not read the \"{}\" global property; falling back to the default",
				    propertyName, e);
			} finally {
				Context.removeProxyPrivilege(PrivilegeConstants.GET_GLOBAL_PROPERTIES);
			}
		}

		if (value == null || value.trim().isEmpty()) {
			return getPropertyDefault(propertyName);
		}

		return value.trim();
	}

	private static String getPropertyDefault(String propertyName) {
		if (OpenmrsConstants.GP_LOG_LAYOUT.equals(propertyName)) {
			return OpenmrsConstants.DEFAULT_LOG_LAYOUT_PATTERN;
		}
		return null;
	}
}
