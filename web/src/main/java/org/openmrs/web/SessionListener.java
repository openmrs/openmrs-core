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

import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.openmrs.web.user.CurrentUsers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles events of session life cycle. <br>
 * <br>
 * This is set by the web.xml class
 * @since 2.6.0
 */
public class SessionListener implements HttpSessionListener {
	
	private static final Logger log = LoggerFactory.getLogger(SessionListener.class);
	
	/**
	 * @see HttpSessionListener#sessionCreated(HttpSessionEvent)
	 */
	public void sessionCreated(HttpSessionEvent httpSessionEvent) {
	}
	
	/**
	 * Called whenever a session times out or a user logs out (and so the session is closed)
	 *
	 * @see HttpSessionListener#sessionDestroyed(HttpSessionEvent)
	 */
	public void sessionDestroyed(HttpSessionEvent httpSessionEvent) {
		CurrentUsers.removeSessionFromList(httpSessionEvent.getSession());
	}
}
