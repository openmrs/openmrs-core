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

import java.util.Collection;
import java.util.HashSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.notification.Alert;
import org.openmrs.notification.AlertRecipient;
import org.openmrs.notification.Message;
import org.openmrs.notification.MessageException;

/**
 *  Sample implementation of task that shows how to send emails to users/roles via message service.
 *
 */
public class AlertReminderTask extends AbstractTask { 

	// Logger 
	private Log log = LogFactory.getLog( AlertReminderTask.class );
	
	
	/** 
	 * Send alert reminder email to user(s) associated with the alert.
	 */
	public void execute() {
		try { 
			// Authenticate
			if (!Context.isAuthenticated()) { 
				authenticate();
			}
			
			// Get all unread alerts
			// TODO Change to getAllAlerts(Boolean includeRead, Boolean includeExpired);
			Collection<Alert> alerts = 
				Context.getAlertService().getAllAlerts(false);
			
			// Send alert notifications to users who have unread alerts
			sendAlertNotifications(alerts);
						
			
		} 
		catch (Exception e) { 
			log.error(e);
		}
	}
	
	
	/**
	 * Send alerts 
	 * 
	 * @param alerts  the unread alerts
	 * @param users		the users who have not read the alerts
	 */
	private void sendAlertNotifications(Collection<Alert> alerts) { 

		try { 
			
			// Create a new message
			Message message = 
				Context.getMessageService().create("Alert Reminder", "You have unread alerts.");

			// Get all recipients
			Collection<User> users = 
				getRecipients(alerts);
			
			// Send a message to each person only once
			Context.getMessageService().send(message, users);
			
		} catch (MessageException e) { 
			log.error(e);
		}
	}
	
	/**
	 * Get the recipients of all unread alerts.
	 * 	  
	 * @param alerts
	 * @return
	 */
	private Collection<User> getRecipients(Collection<Alert> alerts) { 
		Collection<User> users = new HashSet<User>();
		for (Alert alert : alerts) { 
			log.debug("Send email to alert recipient(s) ...");
			if (!alert.isAlertRead() && alert.getRecipients() != null) {
				for (AlertRecipient recipient : alert.getRecipients()) {
					if (!recipient.isAlertRead() && recipient.getRecipient() != null) { 							
						users.add(recipient.getRecipient());
					}
				}
			}
		}
		return users;
	}
	
	
	

}
