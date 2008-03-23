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
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.ContextAuthenticationException;
import org.openmrs.notification.Alert;
import org.openmrs.notification.AlertRecipient;
import org.openmrs.notification.Message;
import org.openmrs.scheduler.Schedulable;
import org.openmrs.scheduler.TaskConfig;

/**
 *  Sample implementation of task that shows how to send emails to users/roles via message service.
 *
 */
public class AlertReminderTask implements Schedulable { 

	// Logger 
	private Log log = LogFactory.getLog( AlertReminderTask.class );
	
	// Instance of configuration information for task
	private TaskConfig taskConfig;

	/**
	 * Public constructor
	 *
	 */
	public AlertReminderTask() {
	}
	
	/**
	 *  Set the context.
	 *
	 *  @param  Context  context
	 */
	public void setContext( ) { }

	/**
	 * Initialize task.
	 * 
	 * @param config
	 */
	public void initialize(TaskConfig config) { 
		this.taskConfig = config;
	}	  
	/** 
	 * Send alert reminder email to user(s) associated with the alert.
	 */
	public void run() {
		try { 
			if (Context.isAuthenticated() == false)
				authenticate();
			
			// TODO Change to getAllAlerts(Boolean includeRead, Boolean includeExpired);
			Collection<Alert> alerts = Context.getAlertService().getAllAlerts(false);
			Collection<User> users = new HashSet<User>();
			Message message = Context.getMessageService().create("Alert Reminder", "You have unread alerts.");

			for (Alert alert : alerts) { 
				log.debug("Send email to alert recipient(s) ...");
				if (alert.isAlertRead() == false && alert.getRecipients() != null) {
					for (AlertRecipient recipient : alert.getRecipients()) {
						if (recipient.isAlertRead() == false && recipient.getRecipient() != null)
							users.add(recipient.getRecipient());
					}
				}
			}
			
			// Send a message to each person only once
			Context.getMessageService().send(message, users);
		} 
		catch (Exception e) { 
			log.error(e);
			e.printStackTrace();
		}
	
	}
	
	private void authenticate() {
		try {
			AdministrationService adminService = Context.getAdministrationService();
			Context.authenticate(adminService.getGlobalProperty("scheduler.username"),
				adminService.getGlobalProperty("scheduler.password"));
			
		} catch (ContextAuthenticationException e) {
			log.error("Error authenticating user", e);
		}
	}

}
