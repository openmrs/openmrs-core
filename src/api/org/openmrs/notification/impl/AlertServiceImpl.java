package org.openmrs.notification.impl;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.User;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.notification.Alert;
import org.openmrs.notification.AlertRecipient;
import org.openmrs.notification.AlertService;
import org.openmrs.notification.db.AlertDAO;

/**
 * Service calls for the Alerting notification system
 * 
 */
public class AlertServiceImpl implements Serializable, AlertService {

	private static final long serialVersionUID = 564561231321112365L;

	private Log log = LogFactory.getLog(this.getClass());
	
	private AlertDAO dao;
	
	public AlertServiceImpl() { }

	private AlertDAO getAlertDAO() {
		return dao;
	}
	
	public void setAlertDAO(AlertDAO dao) {
		this.dao = dao;
	}

	/**
	 * Creates a new alert record
	 * 
	 * @param alert
	 *            to be created
	 * @throws APIException
	 */
	public void createAlert(Alert alert) throws Exception {
		log.debug("Create a alert " + alert);
		
		if (alert.getCreator() == null)
			alert.setCreator(Context.getAuthenticatedUser());
		if (alert.getDateCreated() == null)
			alert.setDateCreated(new Date());
		
		alert.setChangedBy(Context.getAuthenticatedUser());
		alert.setDateChanged(new Date());
		
		getAlertDAO().createAlert(alert);
	}
	
	/**
	 * Convenience method for creating an alert
	 * @param text
	 * @param User assigned to this alert
	 * @throws Exception
	 */
	public void createAlert(String text, User user) throws Exception {
		List<User> users = new Vector<User>();
		users.add(user);
		createAlert(text, users);
	}
	
	/**
	 * Convenience method for creating an alert
	 * @param text
	 * @param Collection<User> users assigned to this alert
	 * @throws Exception
	 */
	public void createAlert(String text, Collection<User> users) throws Exception {
		Alert alert = new Alert();
		alert.setText(text);
		for (User user : users) 
			alert.addRecipient(user);
		
		// Make sure all recipients are assigned to this alert
		if (alert.getRecipients() != null) {
			for (AlertRecipient recipient : alert.getRecipients()) {
				if (!alert.equals(recipient.getAlert()))
					recipient.setAlert(alert);
			}
		}
		createAlert(alert);
	}
		
	/**
	 * Get alert by internal identifier
	 * 
	 * @param alertId
	 *            internal alert identifier
	 * @return alert with given internal identifier
	 * @throws APIException
	 */
	public Alert getAlert(Integer alertId) throws Exception {
		log.debug("Get alert " + alertId);
		return getAlertDAO().getAlert(alertId);
	}

	/**
	 * Update alert
	 * 
	 * @param alert
	 *            to be updated
	 * @throws APIException
	 */
	public void updateAlert(Alert alert) throws Exception {
		log.debug("Update alert " + alert);
		
		if (alert.getCreator() == null)
			alert.setCreator(Context.getAuthenticatedUser());
		if (alert.getDateCreated() == null)
			alert.setDateCreated(new Date());
		
		alert.setChangedBy(Context.getAuthenticatedUser());
		alert.setDateChanged(new Date());
		
		// Make sure all recipients are assigned to this alert
		if (alert.getRecipients() != null) {
			for (AlertRecipient recipient : alert.getRecipients()) {
				if (!alert.equals(recipient.getAlert()))
					recipient.setAlert(alert);
			}
		}
		
		getAlertDAO().updateAlert(alert);
	}
	
	/**
	 * Mark the given alert as read by the authenticated user
	 * 
	 * @param alert
	 * @throws Exception
	 */
	public void markAlertRead(Alert alert) throws Exception {
		log.debug("Marking alert as read " + alert);
		User authUser = Context.getAuthenticatedUser();
		if (authUser != null) {
			AlertRecipient ar = alert.getRecipient(authUser);
			ar.setAlertRead(true);
			if (alert.isSatisfiedByAny())
				alert.setAlertRead(true);
			getAlertDAO().updateAlert(alert);
		}
			
	}

	/**
	 * Find all alerts for a user whether or not the alert has
	 * been read or expired
	 * @param User
	 * @return all alerts attributed to the user (with expired and read included)
	 * @throws Exception
	 */
	public List<Alert> getAllAlerts(User user) throws Exception {
		log.debug("Getting all alerts for user " + user);
		return getAlerts(user, true, true);
	}
	
	/**
	 * Find all alerts for a user that have not expired
	 * @param User
	 * @return alerts that are unread _or_ read that have not expired
	 * @throws Exception
	 */
	public List<Alert> getAllActiveAlerts(User user) throws Exception {
		log.debug("Getting all active alerts for user " + user);
		return getAlerts(user, true, false);
	}
	
	/**
	 * Find the alerts that are not read and have not expired for a user
	 * This will probably be the most commonly called method
	 * 
	 * @param user
	 * @return alerts that are unread and unexpired
	 * @throws Exception
	 */
	public List<Alert> getAlerts(User user) throws Exception {
		log.debug("Getting unread alerts for user " + user);
		return getAlerts(user, false, false);
	}
	
	/**
	 * Find alerts for the currently authenticated user.  If no user is 
	 *  authenticated, search on "new User()" (for "Anonymous" role 
	 *  possibilities)
	 * @return roles associated with Context.getAuthenticatedUser()
	 * @throws Exception
	 */
	public List<Alert> getAlerts() throws Exception {
		log.debug("Getting alerts for the authenticated user");
		// the default user is not null because we may need to find alerts 
		//  for the "Anonymous" role
		User user = new User();
		
		if (Context.isAuthenticated())
			user = Context.getAuthenticatedUser();
		
		return getAlerts(user);
	}
	
	/**
	 * 
	 * @param user to restrict to 
	 * @param includeRead
	 * @param includeExpired
	 * @return alerts for this user with these options
	 * @throws Exception
	 */
	public List<Alert> getAlerts(User user, boolean includeRead, boolean includeExpired) throws Exception {
		log.debug("Getting alerts for user " + user + " read? " + includeRead + " expired? " + includeExpired);
		return getAlertDAO().getAlerts(user, includeRead, includeExpired);
	}
	
	/**
	 * Get all unexpired alerts for all users
	 * @return list of unexpired alerts
	 * @throws Exception
	 */
	public List<Alert> getAllAlerts() throws Exception {
		log.debug("Getting alerts for all users");
		return getAllAlerts(false);
	}

	/**
	 * Get alerts for all users while obeying includeExpired
	 * @param includeExpired
	 * @return list of alerts
	 * @throws Exception
	 */
	public List<Alert> getAllAlerts(boolean includeExpired) throws Exception {
		log.debug("Getting alerts for all users");
		return getAlertDAO().getAllAlerts(includeExpired);
	}
}
