package org.openmrs.notification;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.openmrs.Role;
import org.openmrs.User;

/**
 * Alerts are the simplest form of communication. An Administrator (or script)
 * sets the user or role to attribute the alert to. Alerts are not intended to
 * be sent from user to user and a user cannot send a "reply alert"
 * 
 * @author Ben Wolfe
 * 
 */
public class Alert implements Serializable {

	private static final long serialVersionUID = -507111111109152L;

	private Integer alertId;

	private User user;

	private Role role;

	private String text;

	private Date dateToExpire;

	private User creator;

	private Date dateCreated;

	private User changedBy;

	private Date dateChanged;

	private Set<User> readByUsers;

	/**
	 * Default empty constructor
	 */
	public Alert() {
	}

	/**
	 * Initializes an alert with the given alert id
	 */
	public Alert(Integer alertId) {
		this.alertId = alertId;
	}

	/**
	 * @return Returns the alertId.
	 */
	public Integer getAlertId() {
		return alertId;
	}

	/**
	 * @param alertId
	 *            The alertId to set.
	 */
	public void setAlertId(Integer alertId) {
		this.alertId = alertId;
	}

	/**
	 * @return Returns the creator.
	 */
	public User getCreator() {
		return creator;
	}

	/**
	 * @param creator
	 *            The creator to set.
	 */
	public void setCreator(User creator) {
		this.creator = creator;
	}

	/**
	 * @return Returns the dateCreated.
	 */
	public Date getDateCreated() {
		return dateCreated;
	}

	/**
	 * @param dateCreated
	 *            The dateCreated to set.
	 */
	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}

	/**
	 * @return Returns the date this alert expires
	 */
	public Date getDateToExpire() {
		return dateToExpire;
	}

	/**
	 * @param dateToExpire
	 *            The date To Expire this alert
	 */
	public void setDateToExpire(Date dateToExpire) {
		this.dateToExpire = dateToExpire;
	}

	/**
	 * @return Returns the role.
	 */
	public Role getRole() {
		return role;
	}

	/**
	 * @param role
	 *            The role to set.
	 */
	public void setRole(Role role) {
		this.role = role;
	}

	/**
	 * @return Returns the text.
	 */
	public String getText() {
		return text;
	}

	/**
	 * @param text
	 *            The text to set.
	 */
	public void setText(String text) {
		this.text = text;
	}

	/**
	 * @return Returns the user.
	 */
	public User getUser() {
		return user;
	}

	/**
	 * @param user
	 *            The user to set.
	 */
	public void setUser(User user) {
		this.user = user;
	}
	
	/**
	 * @return Returns the changedBy.
	 */
	public User getChangedBy() {
		return changedBy;
	}

	/**
	 * @param changedBy The user that changed this alert
	 */
	public void setChangedBy(User changedBy) {
		this.changedBy = changedBy;
	}

	/**
	 * @return Returns the date this alert was changed
	 */
	public Date getDateChanged() {
		return dateChanged;
	}

	/**
	 * @param dateChanged The date this alert was changed
	 */
	public void setDateChanged(Date dateChanged) {
		this.dateChanged = dateChanged;
	}

	/**
	 * @return Returns the Users that have read this alert.
	 */
	public Set<User> getReadByUsers() {
		return readByUsers;
	}

	/**
	 * @param readByUsers
	 *            The Users that have read this alert
	 */
	public void setReadByUsers(Set<User> readByUsers) {
		this.readByUsers = readByUsers;
	}
	
	public void addReadByUser(User u) {
		if (this.readByUsers == null)
			this.readByUsers = new HashSet<User>();
		readByUsers.add(u);
	}
	
	//@override
	public String toString() {
		return "Alert: #" + alertId;
	}

}