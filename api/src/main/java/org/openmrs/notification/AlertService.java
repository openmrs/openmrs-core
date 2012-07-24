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
package org.openmrs.notification;

import java.util.Collection;
import java.util.List;

import org.openmrs.User;
import org.openmrs.annotation.Authorized;
import org.openmrs.api.APIException;
import org.openmrs.api.OpenmrsService;
import org.openmrs.notification.db.AlertDAO;
import org.openmrs.util.PrivilegeConstants;
import org.springframework.transaction.annotation.Transactional;

/**
 * Contains methods pertaining to creating/deleting/voiding Alerts in the system Use:<br/>
 * 
 * <pre>
 *   Alert alert = new Alert();
 *   alert.set___(___);
 *   ...etc
 *   Context.getAlertService().saveAlert(alert);
 * </pre>
 */
@Transactional
public interface AlertService extends OpenmrsService {
	
	/**
	 * Used by Spring to set the specific/chosen database access implementation
	 * 
	 * @param dao The dao implementation to use
	 */
	public void setAlertDAO(AlertDAO dao);
	
	/**
	 * Save the given <code>alert</code> in the database
	 * 
	 * @param alert the Alert object to save
	 * @return The saved alert object
	 * @throws APIException
	 * @should save simple alert with one user
	 * @should save alerts by role
	 * @should assign uuid to alert
	 */
	@Authorized(PrivilegeConstants.MANAGE_ALERTS)
	public Alert saveAlert(Alert alert) throws APIException;
	
	/**
	 * @deprecated use {@link #saveAlert(Alert)}
	 */
	@Deprecated
	public void createAlert(Alert alert) throws APIException;
	
	/**
	 * Use AlertService.saveAlert(new Alert(text, user))
	 * 
	 * @deprecated use {@link #saveAlert(Alert)}
	 */
	@Deprecated
	public void createAlert(String text, User user) throws APIException;
	
	/**
	 * Use AlertService.saveAlert(new Alert(text, users))
	 * 
	 * @deprecated use {@link #saveAlert(Alert)}
	 */
	@Deprecated
	public void createAlert(String text, Collection<User> users) throws APIException;
	
	/**
	 * Get alert by internal identifier
	 * 
	 * @param alertId internal alert identifier
	 * @return alert with given internal identifier
	 * @throws APIException
	 */
	@Transactional(readOnly = true)
	public Alert getAlert(Integer alertId) throws APIException;
	
	/**
	 * @deprecated use {@link #saveAlert(Alert)}
	 */
	@Deprecated
	public void updateAlert(Alert alert) throws APIException;
	
	/**
	 * Completely delete the given alert from the database
	 * 
	 * @param alert the Alert to purge/delete
	 * @throws APIException
	 */
	@Authorized(PrivilegeConstants.MANAGE_ALERTS)
	public void purgeAlert(Alert alert) throws APIException;
	
	/**
	 * Use AlertService.saveAlert(alert.markAlertRead())
	 * 
	 * @deprecated use {@link #saveAlert(Alert)}
	 */
	@Deprecated
	public void markAlertRead(Alert alert) throws APIException;
	
	/**
	 * @deprecated use #getAlerts(User, boolean, boolean)
	 */
	@Deprecated
	@Transactional(readOnly = true)
	public List<Alert> getAllAlerts(User user) throws APIException;
	
	/**
	 * Find all alerts for a user that have not expired
	 * 
	 * @param user
	 * @return alerts that are unread _or_ read that have not expired
	 * @see #getAlerts(User, boolean, boolean)
	 * @throws APIException
	 */
	@Transactional(readOnly = true)
	public List<Alert> getAllActiveAlerts(User user) throws APIException;
	
	/**
	 * @deprecated use {@link #getAlertsByUser(User)}
	 */
	@Deprecated
	@Transactional(readOnly = true)
	public List<Alert> getAlerts(User user) throws APIException;
	
	/**
	 * Find the alerts that are not read and have not expired for a user This will probably be the
	 * most commonly called method If null is passed in for <code>user</code>, find alerts for the
	 * currently authenticated user. If no user is authenticated, search on "new
	 * User()" (for "Anonymous" role alert possibilities)
	 * 
	 * @param user the user that is assigned to the returned alerts
	 * @return alerts that are unread and not expired
	 * @throws APIException
	 */
	@Transactional(readOnly = true)
	public List<Alert> getAlertsByUser(User user) throws APIException;
	
	/**
	 * @deprecated use {@link #getAlertsByUser(User)} and pass "null" as the parameter for
	 *             <code>user</code>
	 */
	@Deprecated
	@Transactional(readOnly = true)
	public List<Alert> getAlerts() throws APIException;
	
	/**
	 * Finds alerts for the given user with the given status
	 * 
	 * @param user to restrict to
	 * @param includeRead
	 * @param includeExpired
	 * @return alerts for this user with these options
	 * @throws APIException
	 */
	@Transactional(readOnly = true)
	public List<Alert> getAlerts(User user, boolean includeRead, boolean includeExpired) throws APIException;
	
	/**
	 * Get all unexpired alerts for all users
	 * 
	 * @return list of unexpired alerts
	 * @throws APIException
	 */
	@Transactional(readOnly = true)
	public List<Alert> getAllAlerts() throws APIException;
	
	/**
	 * Get alerts for all users while obeying includeExpired
	 * 
	 * @param includeExpired
	 * @return list of alerts
	 * @throws APIException
	 */
	@Transactional(readOnly = true)
	public List<Alert> getAllAlerts(boolean includeExpired) throws APIException;
	
	/**
	 * Sends an alert to all superusers
	 * 
	 * @param messageCode The alert message code from messages.properties
	 * @param cause The exception that was thrown, method will work if cause is null
	 * @param messageArguments The arguments for the coded message
	 * @should add an alert to the database
	 */
	@Authorized(PrivilegeConstants.MANAGE_ALERTS)
	public void notifySuperUsers(String messageCode, Exception cause, Object... messageArguments);
}
