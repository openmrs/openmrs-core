/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web;

import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;

import org.openmrs.util.ConfigUtil;
import org.openmrs.util.OpenmrsConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Applies the configured session timeout to each new session. The value is loaded once at startup,
 * after modules start, and kept statically so it survives Spring context refreshes.
 *
 * @see OpenmrsConstants#SESSION_TIMEOUT_PROPERTY
 * @since 2.8.11
 */
@Component
public class SessionTimeoutHttpSessionListener implements HttpSessionListener {

	private static final Logger log = LoggerFactory.getLogger(SessionTimeoutHttpSessionListener.class);

	private static volatile Integer sessionTimeoutInMinutes = null;

	@Override
	public void sessionCreated(HttpSessionEvent se) {
		Integer timeout = sessionTimeoutInMinutes;
		if (timeout != null) {
			se.getSession().setMaxInactiveInterval(timeout * 60);
		}
	}

	/**
	 * Reads the session timeout from the system, runtime, or global properties. Never throws, so it
	 * cannot fail startup.
	 */
	public static void loadSessionTimeout() {
		try {
			setSessionTimeout(ConfigUtil.getProperty(OpenmrsConstants.SESSION_TIMEOUT_PROPERTY));
		} catch (Exception e) {
			log.warn("Unable to load the {} property", OpenmrsConstants.SESSION_TIMEOUT_PROPERTY, e);
		}
	}

	/**
	 * Sets the timeout, in minutes, applied to each new session. Blank or invalid values leave the
	 * servlet container's default in place.
	 *
	 * @param value the timeout in minutes, may be null
	 */
	static void setSessionTimeout(String value) {
		if (!StringUtils.hasText(value)) {
			sessionTimeoutInMinutes = null;
			return;
		}
		int minutes;
		try {
			minutes = Integer.parseInt(value.trim());
		} catch (NumberFormatException e) {
			minutes = -1;
		}
		if (minutes <= 0 || minutes > Integer.MAX_VALUE / 60) {
			sessionTimeoutInMinutes = null;
			log.warn("Ignoring invalid {} property value '{}'; it must be a positive number of minutes",
			    OpenmrsConstants.SESSION_TIMEOUT_PROPERTY, value);
			return;
		}
		sessionTimeoutInMinutes = minutes;
		log.info("Using a session timeout of {} minute(s) from the {} property", minutes,
		    OpenmrsConstants.SESSION_TIMEOUT_PROPERTY);
	}
}
