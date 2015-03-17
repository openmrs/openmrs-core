/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.notification.web.dwr;

import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.directwebremoting.WebContextFactory;
import org.openmrs.api.context.Context;
import org.openmrs.notification.MessageService;
import org.openmrs.notification.NotificationConstants;

public class DWRMessageService {
	
	protected final Log log = LogFactory.getLog(getClass());
	
	public boolean sendFeedback(String sender, String subject, String content) {
		
		HttpServletRequest request = WebContextFactory.get().getHttpServletRequest();
		
		if (!Context.isAuthenticated()) {
			try {
				MessageService messageService = Context.getMessageService();
				
				String recipients = NotificationConstants.FEEDBACK_EMAIL_ADDRESS;
				if (StringUtils.isEmpty(subject)) {
					subject = NotificationConstants.FEEDBACK_EMAIL_SUBJECT;
				}
				
				String referer = request.getPathTranslated();
				String userName = "an Anonymous User";
				if (Context.isAuthenticated()) {
					userName = Context.getAuthenticatedUser().getPersonName().getFullName();
				}
				
				content += "\n\n This email sent from: " + referer + " by: " + userName;
				
				messageService.sendMessage(recipients, sender, subject, content);
				
				return true;
				
			}
			catch (Exception e) {
				log.error("Error sending feedback", e);
			}
		}
		
		return false;
	}
	
	public Vector<Object> sendMessage(String recipients, String sender, String subject, String content) {
		
		// List to return
		// Object type gives ability to return error strings
		Vector<Object> objectList = new Vector<Object>();
		
		HttpServletRequest request = WebContextFactory.get().getHttpServletRequest();
		
		if (!Context.isAuthenticated()) {
			objectList.add("Your session has expired.");
			objectList.add("Please <a href='" + request.getContextPath() + "/logout'>log in</a> again.");
		} else {
			try {
				MessageService messageService = Context.getMessageService();
				messageService.sendMessage(recipients, sender, subject, content);
				objectList.add("Message has been sent successfully.");
			}
			catch (Exception e) {
				log.error(e);
				objectList.add("Error while attempting to send message: " + e.getMessage());
			}
		}
		return objectList;
	}
	
}
