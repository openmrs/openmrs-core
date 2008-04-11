/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.scheduler.tasks;

//import java.net.HttpURLConnection;
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

/**
 *  Simple implementation to check if we have a connection to the internet.
 */
public class CheckInternetConnectivityTask extends AbstractTask { 

	/**
	 * Logger 
	 */
	private static Log log = LogFactory.getLog(CheckInternetConnectivityTask.class);
	
	
	/**
	 * @see org.openmrs.scheduler.tasks.AbstractTask#execute()
	 */
	public void execute() { 
		
		// TODO url should be provided as a property to taskconfig
		String url = "http://www.google.com:80/index.html";
		try {
			URLConnection connection = new URL( url ).openConnection();
			connection.connect();
		} catch ( IOException ioe ) {
			try { 
				if (Context.isAuthenticated() == false)
					authenticate();
				String text = "At " + new Date() + " there was an error reported connecting to the internet address " + url + ": " + ioe;
				// TODO role should be provided as a property to taskconfig
				Role role = Context.getUserService().getRole("System Developer");
				Collection<User> users = Context.getUserService().getUsersByRole(role);
				Context.getAlertService().createAlert(text, users);
			} catch (Exception e) { 
				// Uh oh, just log it.
				log.error(e);
			}
		}
	}
	
}
