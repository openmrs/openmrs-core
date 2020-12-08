/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.notification.impl;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

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
import org.openmrs.util.RoleConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

/**
 * This class should not be instantiated by itself.
 *
 * @see org.openmrs.notification.AlertService
 */
@Transactional
public class AlertServiceImpl extends BaseOpenmrsService implements Serializable, AlertService {
	
	private static final long serialVersionUID = 564561231321112365L;
	
	private static final Logger log = LoggerFactory.getLogger(AlertServiceImpl.class);
	
	private AlertDAO dao;
	
	/**
	 * Default constructor
	 */
	public AlertServiceImpl() {
	}
	
	/**
	 * @see org.openmrs.notification.AlertService#setAlertDAO(org.openmrs.notification.db.AlertDAO)
	 */
	@Override
	public void setAlertDAO(AlertDAO dao) {
		this.dao = dao;
	}
	
	/**
	 * @see org.openmrs.notification.AlertService#saveAlert(org.openmrs.notification.Alert)
	 */
	@Override
	public Alert saveAlert(Alert alert) throws APIException {
		log.debug("Create a alert " + alert);
		
		if (alert.getCreator() == null) {
			alert.setCreator(Context.getAuthenticatedUser());
		}
		if (alert.getDateCreated() == null) {
			alert.setDateCreated(new Date());
		}
		
		if (alert.getAlertId() != null) {
			alert.setChangedBy(Context.getAuthenticatedUser());
			alert.setDateChanged(new Date());
		}
		
		// Make sure all recipients are assigned to this alert
		if (alert.getRecipients() != null) {
			for (AlertRecipient recipient : alert.getRecipients()) {
				if (!alert.equals(recipient.getAlert())) {
					recipient.setAlert(alert);
				}
			}
		}
		
		return dao.saveAlert(alert);
	}

	/**
	 * @see org.openmrs.notification.AlertService#getAlert(java.lang.Integer)
	 */
	@Override
	@Transactional(readOnly = true)
	public Alert getAlert(Integer alertId) throws APIException {
		return dao.getAlert(alertId);
	}
	
	/**
	 * @see org.openmrs.notification.AlertService#purgeAlert(org.openmrs.notification.Alert)
	 */
	@Override
	public void purgeAlert(Alert alert) throws APIException {
		dao.deleteAlert(alert);
	}
	
	/**
	 * @see org.openmrs.notification.AlertService#getAllActiveAlerts(org.openmrs.User)
	 */
	@Override
	@Transactional(readOnly = true)
	public List<Alert> getAllActiveAlerts(User user) throws APIException {
		log.debug("Getting all active alerts for user " + user);
		return Context.getAlertService().getAlerts(user, true, false);
	}
	
	/**
	 * @see org.openmrs.notification.AlertService#getAlertsByUser(org.openmrs.User)
	 */
	@Override
	@Transactional(readOnly = true)
	public List<Alert> getAlertsByUser(User user) throws APIException {
		log.debug("Getting unread alerts for user " + user);
		
		if (user == null) {
			if (Context.isAuthenticated()) {
				user = Context.getAuthenticatedUser();
			} else {
				user = new User();
			}
		}
		
		return Context.getAlertService().getAlerts(user, false, false);
	}

	/**
	 * @see org.openmrs.notification.AlertService#getAlerts(org.openmrs.User, boolean, boolean)
	 */
	@Override
	@Transactional(readOnly = true)
	public List<Alert> getAlerts(User user, boolean includeRead, boolean includeExpired) throws APIException {
		log.debug("Getting alerts for user " + user + " read? " + includeRead + " expired? " + includeExpired);
		return dao.getAlerts(user, includeRead, includeExpired);
	}
	
	/**
	 * @see org.openmrs.notification.AlertService#getAllAlerts()
	 */
	@Override
	@Transactional(readOnly = true)
	public List<Alert> getAllAlerts() throws APIException {
		log.debug("Getting alerts for all users");
		return Context.getAlertService().getAllAlerts(false);
	}
	
	/**
	 * @see org.openmrs.notification.AlertService#getAllAlerts(boolean)
	 */
	@Override
	@Transactional(readOnly = true)
	public List<Alert> getAllAlerts(boolean includeExpired) throws APIException {
		log.debug("Getting alerts for all users");
		return dao.getAllAlerts(includeExpired);
	}
	
	/**
	 * @see org.openmrs.notification.AlertService#notifySuperUsers(String, Exception, Object...)
	 */
	@Override
	public void notifySuperUsers(String messageCode, Exception cause, Object... messageArguments) {
		
		// Generate an internationalized error message with the beginning of the stack trace from cause added onto the end
		String message = Context.getMessageSourceService().getMessage(messageCode, messageArguments, Context.getLocale());
		
		if (cause != null) {
			StringBuilder stackTrace = new StringBuilder();
			// get the first two lines of the stack trace ( no more can fit in the alert text )
			
			for (StackTraceElement traceElement : cause.getStackTrace()) {
				stackTrace.append(traceElement);
				stackTrace.append("\n");
				if (stackTrace.length() >= Alert.TEXT_MAX_LENGTH) {
					break;
				}
			}
			
			message = message + ":" + stackTrace;
			
			//limit message to Alert.TEXT_MAX_LENGTH
			message = message.substring(0, Math.min(message.length(), Alert.TEXT_MAX_LENGTH));
		}
		
		//Send an alert to all administrators
		Alert alert = new Alert(message, Context.getUserService().getUsersByRole(new Role(RoleConstants.SUPERUSER)));
		
		// Set the alert so that if any administrator 'reads' it it will be marked as read for everyone who received it
		alert.setSatisfiedByAny(true);
		
		//If there is not user creator for the alert ( because it is being created at start-up )create a user
		if (alert.getCreator() == null) { 
			User daemonUser = Context.getUserService().getUserByUuid(Daemon.getDaemonUserUuid());
			alert.setCreator(daemonUser);
		} 
		
		// save the alert to send it to all administrators
		Context.getAlertService().saveAlert(alert);
	}
}