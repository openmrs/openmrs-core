package org.openmrs.notification;

import java.util.Collection;
import java.util.List;

import org.openmrs.User;
import org.openmrs.api.APIException;
import org.openmrs.notification.db.AlertDAO;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface AlertService {

	public void setAlertDAO(AlertDAO dao);

	/**
	 * Creates a new alert record
	 * 
	 * @param alert
	 *            to be created
	 * @throws APIException
	 */
	public void createAlert(Alert alert) throws Exception;

	/**
	 * Convenience method for creating an alert
	 * @param text
	 * @param User assigned to this alert
	 * @throws Exception
	 */
	public void createAlert(String text, User user) throws Exception;

	/**
	 * Convenience method for creating an alert
	 * @param text
	 * @param Collection<User> users assigned to this alert
	 * @throws Exception
	 */
	public void createAlert(String text, Collection<User> users)
			throws Exception;

	/**
	 * Get alert by internal identifier
	 * 
	 * @param alertId
	 *            internal alert identifier
	 * @return alert with given internal identifier
	 * @throws APIException
	 */
	@Transactional(readOnly=true)
	public Alert getAlert(Integer alertId) throws Exception;

	/**
	 * Update alert
	 * 
	 * @param alert
	 *            to be updated
	 * @throws APIException
	 */
	public void updateAlert(Alert alert) throws Exception;

	/**
	 * Mark the given alert as read by the authenticated user
	 * 
	 * @param alert
	 * @throws Exception
	 */
	public void markAlertRead(Alert alert) throws Exception;

	/**
	 * Find all alerts for a user whether or not the alert has
	 * been read or expired
	 * @param User
	 * @return all alerts attributed to the user (with expired and read included)
	 * @throws Exception
	 */
	@Transactional(readOnly=true)
	public List<Alert> getAllAlerts(User user) throws Exception;

	/**
	 * Find all alerts for a user that have not expired
	 * @param User
	 * @return alerts that are unread _or_ read that have not expired
	 * @throws Exception
	 */
	@Transactional(readOnly=true)
	public List<Alert> getAllActiveAlerts(User user) throws Exception;

	/**
	 * Find the alerts that are not read and have not expired for a user
	 * This will probably be the most commonly called method
	 * 
	 * @param user
	 * @return alerts that are unread and unexpired
	 * @throws Exception
	 */
	@Transactional(readOnly=true)
	public List<Alert> getAlerts(User user) throws Exception;

	/**
	 * Find alerts for the currently authenticated user.  If no user is 
	 *  authenticated, search on "new User()" (for "Anonymous" role 
	 *  possibilities)
	 * @return roles associated with Context.getAuthenticatedUser()
	 * @throws Exception
	 */
	@Transactional(readOnly=true)
	public List<Alert> getAlerts() throws Exception;

	/**
	 * 
	 * @param user to restrict to 
	 * @param includeRead
	 * @param includeExpired
	 * @return alerts for this user with these options
	 * @throws Exception
	 */
	@Transactional(readOnly=true)
	public List<Alert> getAlerts(User user, boolean includeRead,
			boolean includeExpired) throws Exception;

	/**
	 * Get all unexpired alerts for all users
	 * @return list of unexpired alerts
	 * @throws Exception
	 */
	@Transactional(readOnly=true)
	public List<Alert> getAllAlerts() throws Exception;

	/**
	 * Get alerts for all users while obeying includeExpired
	 * @param includeExpired
	 * @return list of alerts
	 * @throws Exception
	 */
	@Transactional(readOnly=true)
	public List<Alert> getAllAlerts(boolean includeExpired) throws Exception;

}