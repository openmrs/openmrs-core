/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.notification;

import java.util.List;

import org.openmrs.User;
import org.openmrs.annotation.Authorized;
import org.openmrs.api.APIException;
import org.openmrs.api.OpenmrsService;
import org.openmrs.notification.db.AlertDAO;
import org.openmrs.util.PrivilegeConstants;

/**
 * Contains methods pertaining to creating/deleting/voiding Alerts in the system Use:<br>
 * 
 * <pre>
 *   Alert alert = new Alert();
 *   alert.set___(___);
 *   ...etc
 *   Context.getAlertService().saveAlert(alert);
 * </pre>
 */
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
	 * <strong>Should</strong> save simple alert with one user
	 * <strong>Should</strong> save alerts by role
	 * <strong>Should</strong> assign uuid to alert
	 */
	@Authorized(PrivilegeConstants.MANAGE_ALERTS)
	public Alert saveAlert(Alert alert) throws APIException;
	
	/**
	 * Get alert by internal identifier. Callers may only read an alert addressed to themselves; an
	 * alert addressed to another user is returned only to a caller holding the
	 * {@link PrivilegeConstants#GET_ALERTS} privilege. Unlike the user-scoped reads such as
	 * {@link #getAlerts(User, boolean, boolean)} - which throw
	 * {@link org.openmrs.api.APIAuthenticationException} for another user's alerts - this id-based
	 * lookup instead returns <code>null</code> in that case, the same as for an unknown identifier, so
	 * it cannot be used to probe which alert ids exist.
	 *
	 * @param alertId internal alert identifier
	 * @return the alert with the given internal identifier, or <code>null</code> if no such alert
	 *         exists, or it is addressed to another user and the caller lacks the
	 *         {@link PrivilegeConstants#GET_ALERTS} privilege
	 * @throws APIException
	 */
	@Authorized
	public Alert getAlert(Integer alertId) throws APIException;
	
	/**
	 * Completely delete the given alert from the database
	 * 
	 * @param alert the Alert to purge/delete
	 * @throws APIException
	 */
	@Authorized(PrivilegeConstants.MANAGE_ALERTS)
	public void purgeAlert(Alert alert) throws APIException;
	
	/**
	 * Find all alerts for a user that have not expired. Callers may only read their own alerts; reading
	 * another user's alerts requires the {@link PrivilegeConstants#GET_ALERTS} privilege.
	 *
	 * @param user
	 * @return alerts that are unread _or_ read that have not expired
	 * @see #getAlerts(User, boolean, boolean)
	 * @throws APIException
	 * @throws org.openmrs.api.APIAuthenticationException if <code>user</code> is another user and the
	 *             caller lacks the {@link PrivilegeConstants#GET_ALERTS} privilege
	 */
	@Authorized
	public List<Alert> getAllActiveAlerts(User user) throws APIException;
	
	/**
	 * Find the alerts that are not read and have not expired for a user This will probably be the most
	 * commonly called method If null is passed in for <code>user</code>, find alerts for the currently
	 * authenticated user. If no user is authenticated, search on "new User()" (for "Anonymous" role
	 * alert possibilities). Callers may only read their own alerts; reading another user's alerts
	 * requires the {@link PrivilegeConstants#GET_ALERTS} privilege.
	 *
	 * @param user the user that is assigned to the returned alerts
	 * @return alerts that are unread and not expired
	 * @throws APIException
	 * @throws org.openmrs.api.APIAuthenticationException if <code>user</code> is another user and the
	 *             caller lacks the {@link PrivilegeConstants#GET_ALERTS} privilege
	 */
	@Authorized
	public List<Alert> getAlertsByUser(User user) throws APIException;
	
	/**
	 * Finds alerts for the given user with the given status. Callers may only read their own alerts;
	 * reading another user's alerts requires the {@link PrivilegeConstants#GET_ALERTS} privilege.
	 *
	 * @param user to restrict to
	 * @param includeRead
	 * @param includeExpired
	 * @return alerts for this user with these options
	 * @throws APIException
	 * @throws org.openmrs.api.APIAuthenticationException if <code>user</code> is another user and the
	 *             caller lacks the {@link PrivilegeConstants#GET_ALERTS} privilege
	 */
	@Authorized
	public List<Alert> getAlerts(User user, boolean includeRead, boolean includeExpired) throws APIException;
	
	/**
	 * Get all unexpired alerts for all users
	 * 
	 * @return list of unexpired alerts
	 * @throws APIException
	 */
	@Authorized(PrivilegeConstants.GET_ALERTS)
	public List<Alert> getAllAlerts() throws APIException;
	
	/**
	 * Get alerts for all users while obeying includeExpired
	 * 
	 * @param includeExpired
	 * @return list of alerts
	 * @throws APIException
	 */
	@Authorized(PrivilegeConstants.GET_ALERTS)
	public List<Alert> getAllAlerts(boolean includeExpired) throws APIException;
	
	/**
	 * Sends an alert to all superusers
	 * 
	 * @param messageCode The alert message code from messages.properties
	 * @param cause The exception that was thrown, method will work if cause is null
	 * @param messageArguments The arguments for the coded message
	 * <strong>Should</strong> add an alert with message of length equals Text Max Length
	 * <strong>Should</strong> add an alert with message text if cause is null
	 * <strong>Should</strong> add an alert to the database
	 */
	@Authorized(PrivilegeConstants.MANAGE_ALERTS)
	public void notifySuperUsers(String messageCode, Exception cause, Object... messageArguments);
}
