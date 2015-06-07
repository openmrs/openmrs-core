/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.scheduler.tasks;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collection;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Role;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.notification.Alert;

/**
 * Simple implementation to check if we have a connection to the internet.
 */
public class CheckInternetConnectivityTask extends AbstractTask {
	
	/**
	 * Logger
	 */
	private Log log = LogFactory.getLog(getClass());
	
	/**
	 * @see org.openmrs.scheduler.tasks.AbstractTask#execute()
	 */
	public void execute() {
		
		// TODO url should be provided as a property to taskconfig
		String url = "http://www.google.com:80/index.html";
		try {
			URLConnection connection = new URL(url).openConnection();
			connection.connect();
		}
		catch (IOException ioe) {
			try {
				if (!Context.isAuthenticated()) {
					authenticate();
				}
				String text = "At " + new Date() + " there was an error reported connecting to the internet address " + url
				        + ": " + ioe;
				// TODO role should be provided as a property to taskconfig
				Role role = Context.getUserService().getRole("System Developer");
				Collection<User> users = Context.getUserService().getUsersByRole(role);
				Context.getAlertService().saveAlert(new Alert(text, users));
			}
			catch (Exception e) {
				// Uh oh, just log it.
				log.error(e);
			}
		}
	}
}
