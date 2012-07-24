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
package org.openmrs.notification.impl;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Role;
import org.openmrs.User;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.Daemon;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.notification.Alert;
import org.openmrs.notification.AlertRecipient;
import org.openmrs.notification.AlertService;
import org.openmrs.notification.db.AlertDAO;
import org.openmrs.util.OpenmrsConstants;

/**
 * This class should not be instantiated by itself.
 * 
 * @see org.openmrs.notification.AlertService
 */
public class AlertServiceImpl extends BaseOpenmrsService implements Serializable, AlertService {
	
	private static final long serialVersionUID = 564561231321112365L;
	
	private Log log = LogFactory.getLog(this.getClass());
	
	private AlertDAO dao;
	
	/**
	 * Default constructor
	 */
	public AlertServiceImpl() {
	}
	
	/**
	 * @see org.openmrs.notification.AlertService#setAlertDAO(org.openmrs.notification.db.AlertDAO)
	 */
	public void setAlertDAO(AlertDAO dao) {
		this.dao = dao;
	}
	
	/**
	 * @see org.openmrs.notification.AlertService#createAlert(org.openmrs.notification.Alert)
	 * @deprecated
	 */
	@Deprecated
	public void createAlert(Alert alert) throws APIException {
		Context.getAlertService().saveAlert(alert);
	}
	
	/**
	 * @see org.openmrs.notification.AlertService#saveAlert(org.openmrs.notification.Alert)
	 */
	public Alert saveAlert(Alert alert) throws APIException {
		log.debug("Create a alert " + alert);
		
		if (alert.getCreator() == null)
			alert.setCreator(Context.getAuthenticatedUser());
		if (alert.getDateCreated() == null)
			alert.setDateCreated(new Date());
		
		if (alert.getAlertId() != null) {
			alert.setChangedBy(Context.getAuthenticatedUser());
			alert.setDateChanged(new Date());
		}
		
		// Make sure all recipients are assigned to this alert
		if (alert.getRecipients() != null) {
			for (AlertRecipient recipient : alert.getRecipients()) {
				if (!alert.equals(recipient.getAlert()))
					recipient.setAlert(alert);
			}
		}
		
		return dao.saveAlert(alert);
	}
	
	/**
	 * @see org.openmrs.notification.AlertService#createAlert(java.lang.String, org.openmrs.User)
	 * @deprecated
	 */
	@Deprecated
	public void createAlert(String text, User user) throws APIException {
		Context.getAlertService().saveAlert(new Alert(text, user));
	}
	
	/**
	 * @see org.openmrs.notification.AlertService#createAlert(java.lang.String,
	 *      java.util.Collection)
	 * @deprecated
	 */
	@Deprecated
	public void createAlert(String text, Collection<User> users) throws APIException {
		Context.getAlertService().saveAlert(new Alert(text, users));
	}
	
	/**
	 * @see org.openmrs.notification.AlertService#getAlert(java.lang.Integer)
	 */
	public Alert getAlert(Integer alertId) throws APIException {
		return dao.getAlert(alertId);
	}
	
	/**
	 * @see org.openmrs.notification.AlertService#updateAlert(org.openmrs.notification.Alert)
	 * @deprecated
	 */
	@Deprecated
	public void updateAlert(Alert alert) throws APIException {
		Context.getAlertService().saveAlert(alert);
	}
	
	/**
	 * @see org.openmrs.notification.AlertService#purgeAlert(org.openmrs.notification.Alert)
	 */
	public void purgeAlert(Alert alert) throws APIException {
		dao.deleteAlert(alert);
	}
	
	/**
	 * @see org.openmrs.notification.AlertService#markAlertRead(org.openmrs.notification.Alert)
	 * @deprecated
	 */
	@Deprecated
	public void markAlertRead(Alert alert) throws APIException {
		Context.getAlertService().saveAlert(alert.markAlertRead());
	}
	
	/**
	 * @see org.openmrs.notification.AlertService#getAllAlerts(org.openmrs.User)
	 * @deprecated
	 */
	@Deprecated
	public List<Alert> getAllAlerts(User user) throws APIException {
		log.debug("Getting all alerts for user " + user);
		return getAlerts(user, true, true);
	}
	
	/**
	 * @see org.openmrs.notification.AlertService#getAllActiveAlerts(org.openmrs.User)
	 */
	public List<Alert> getAllActiveAlerts(User user) throws APIException {
		log.debug("Getting all active alerts for user " + user);
		return getAlerts(user, true, false);
	}
	
	/**
	 * @see org.openmrs.notification.AlertService#getAlerts(org.openmrs.User)
	 * @deprecated
	 */
	@Deprecated
	public List<Alert> getAlerts(User user) throws APIException {
		log.debug("Getting unread alerts for user " + user);
		return getAlertsByUser(user);
	}
	
	/**
	 * @see org.openmrs.notification.AlertService#getAlertsByUser(org.openmrs.User)
	 */
	public List<Alert> getAlertsByUser(User user) throws APIException {
		log.debug("Getting unread alerts for user " + user);
		
		if (user == null) {
			if (Context.isAuthenticated())
				user = Context.getAuthenticatedUser();
			else
				user = new User();
		}
		
		return getAlerts(user, false, false);
	}
	
	/**
	 * @see org.openmrs.notification.AlertService#getAlerts()
	 * @deprecated
	 */
	@Deprecated
	public List<Alert> getAlerts() throws APIException {
		return getAlertsByUser(null);
	}
	
	/**
	 * @see org.openmrs.notification.AlertService#getAlerts(org.openmrs.User, boolean, boolean)
	 */
	public List<Alert> getAlerts(User user, boolean includeRead, boolean includeExpired) throws APIException {
		log.debug("Getting alerts for user " + user + " read? " + includeRead + " expired? " + includeExpired);
		return dao.getAlerts(user, includeRead, includeExpired);
	}
	
	/**
	 * @see org.openmrs.notification.AlertService#getAllAlerts()
	 */
	public List<Alert> getAllAlerts() throws APIException {
		log.debug("Getting alerts for all users");
		return getAllAlerts(false);
	}
	
	/**
	 * @see org.openmrs.notification.AlertService#getAllAlerts(boolean)
	 */
	public List<Alert> getAllAlerts(boolean includeExpired) throws APIException {
		log.debug("Getting alerts for all users");
		return dao.getAllAlerts(includeExpired);
	}
	
	/**
	 * @see org.openmrs.notification.AlertService#notifySuperUsers(java.lang.String,java.lang.Exception,java.lang.String[])
	 */
	public void notifySuperUsers(String messageCode, Exception cause, Object... messageArguments) {
		
		// Generate an internationalized error message with the beginning of the stack trace from cause added onto the end
		String message = Context.getMessageSourceService().getMessage(messageCode, messageArguments, Context.getLocale());
		
		if (cause != null) {
			StringBuffer stackTrace = new StringBuffer();
			// get the first two lines of the stack trace ( no more can fit in the alert text )
			
			for (StackTraceElement traceElement : cause.getStackTrace()) {
				stackTrace.append(traceElement);
				stackTrace.append("\n");
				if (stackTrace.length() >= 512) {
					break;
				}
			}
			
			message = message + ": " + stackTrace.substring(0, Alert.TEXT_MAX_LENGTH - message.length() - 2);
		}
		
		//Send an alert to all administrators
		Alert alert = new Alert(message, Context.getUserService().getUsersByRole(new Role(OpenmrsConstants.SUPERUSER_ROLE)));
		
		// Set the alert so that if any administrator 'reads' it it will be marked as read for everyone who received it
		alert.setSatisfiedByAny(true);
		
		//If there is not user creator for the alert ( because it is being created at start-up )create a user
		//TODO switch this to use the daemon user when ticket TRUNK-120 is complete
		if (alert.getCreator() == null) {
			alert.setCreator(new User(1));
		}
		
		// save the alert to send it to all administrators
		Context.getAlertService().saveAlert(alert);
		
	}
}
