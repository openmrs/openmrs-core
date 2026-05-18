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
import org.openmrs.api.AdministrationService;
import org.openmrs.api.ServiceNotFoundException;
import org.openmrs.api.context.Context;
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
		AdministrationService adminService = null;

		try {
			adminService = Context.getAdministrationService();
		} catch (ServiceNotFoundException e) {
			// if AdministrationService is not available, we'll assume we're starting up and everything is ok
			if (!AdministrationService.class.isAssignableFrom(e.getServiceClass())) {
				throw e;
			}
		}

		switch (key) {
			case "applicationDirectory":
				final String applicationDirectory = OpenmrsUtil.getApplicationDataDirectory();
				return applicationDirectory == null || applicationDirectory.isEmpty() ? null : applicationDirectory;
			case "logLocation":
				final String logLocation = getGlobalProperty(adminService, OpenmrsConstants.GP_LOG_LOCATION);
				return logLocation == null ? null
				        : logLocation.endsWith("/") ? logLocation.substring(0, logLocation.length() - 1) : logLocation;
			case "logLayout":
				return getGlobalProperty(adminService, OpenmrsConstants.GP_LOG_LAYOUT);
			default:
				throw new IllegalArgumentException(key
				        + " is not a supported property. We support openmrs:applicationDirectory, openmrs:logLocation, and openmrs:logLayout");
		}
	}

	private String getGlobalProperty(AdministrationService adminService, String globalPropertyName) {
		if (adminService == null) {
			return getPropertyDefault(globalPropertyName);
		}

		String value;
		try {
			Context.addProxyPrivilege(PrivilegeConstants.GET_GLOBAL_PROPERTIES);
			value = adminService.getGlobalProperty(globalPropertyName);
		} finally {
			Context.removeProxyPrivilege(PrivilegeConstants.GET_GLOBAL_PROPERTIES);
		}

		if (value == null) {
			return getPropertyDefault(globalPropertyName);
		} else {
			value = value.trim();
		}

		if (value.isEmpty()) {
			return getPropertyDefault(globalPropertyName);
		}

		return value;
	}

	private static String getPropertyDefault(String propertyName) {
		switch (propertyName) {
			case OpenmrsConstants.GP_LOG_LOCATION:
				return "";
			case OpenmrsConstants.GP_LOG_LAYOUT:
				return "%p - %C{1}.%M(%L) |%d{ISO8601}| %m%n";
			default:
				return null;
		}
	}
}
