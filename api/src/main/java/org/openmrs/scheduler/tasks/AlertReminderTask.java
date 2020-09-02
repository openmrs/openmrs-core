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

import java.util.Collection;
import java.util.HashSet;

import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.notification.Alert;
import org.openmrs.notification.AlertRecipient;
import org.openmrs.notification.Message;
import org.openmrs.notification.MessageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Sample implementation of task that shows how to send emails to users/roles via message service.
 */
public class AlertReminderTask extends AbstractTask {
	
	// Logger 
	private static final Logger log = LoggerFactory.getLogger(AlertReminderTask.class);
	
	/**
	 * Send alert reminder email to user(s) associated with the alert.
	 */
	@Override
	public void execute() {
		try {
			// Get all unread alerts
			// TODO Change to getAllAlerts(Boolean includeRead, Boolean includeExpired);
			Collection<Alert> alerts = Context.getAlertService().getAllAlerts(false);
			
			// Send alert notifications to users who have unread alerts
			sendAlertNotifications(alerts);
			
		}
		catch (Exception e) {
			log.error("Failed to send alert notifications", e);
		}
	}
	
	/**
	 * Send alerts
	 * 
	 * @param alerts the unread alerts
	 */
	private void sendAlertNotifications(Collection<Alert> alerts) {
		
		try {
			
			// Create a new message
			Message message = Context.getMessageService().createMessage("Alert Reminder", "You have unread alerts.");
			
			// Get all recipients
			Collection<User> users = getRecipients(alerts);
			
			// Send a message to each person only once
			Context.getMessageService().sendMessage(message, users);
			
		}
		catch (MessageException e) {
			log.error("Failed to send message", e);
		}
	}
	
	/**
	 * Get the recipients of all unread alerts.
	 * 
	 * @param alerts
	 * @return the users who have not read the alerts
	 */
	private Collection<User> getRecipients(Collection<Alert> alerts) {
		Collection<User> users = new HashSet<>();
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
