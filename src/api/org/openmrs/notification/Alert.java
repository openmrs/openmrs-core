package org.openmrs.notification;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

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

	private String text;

	private Boolean satisfiedByAny = false;
	
	private Boolean alertRead = false;

	private Date dateToExpire;

	private User creator;

	private Date dateCreated;

	private User changedBy;

	private Date dateChanged;

	private Set<AlertRecipient> recipients;

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

	public boolean equals(Object obj) {
		if (obj instanceof Alert) {
			Alert a = (Alert)obj;
			if (alertId != null && a != null)
				return (alertId.equals(a.getAlertId()));
		}
		return false;
	}
	
	public int hashCode() {
		if (this.getAlertId() == null) return super.hashCode();
		int hash = 8;
		hash = 31 * this.getAlertId().hashCode() + hash;
		return hash;
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
	 * @see isSatisfiedByAny()
	 */
	public Boolean getSatisfiedByAny() {
		return isSatisfiedByAny();
	}

	/**
	 * @return Returns the satisfiedByAny.
	 */
	public Boolean isSatisfiedByAny() {
		return satisfiedByAny;
	}

	/**
	 * @param satisfiedByAny
	 *            The satisfiedByAny to set.
	 */
	public void setSatisfiedByAny(Boolean satisfiedByAny) {
		this.satisfiedByAny = satisfiedByAny;
	}
	
	/**
	 * @see isAlertRead()
	 */
	public Boolean getAlertRead() {
		return isAlertRead();
	}

	/**
	 * @return Returns the alertRead.
	 */
	public Boolean isAlertRead() {
		return alertRead;
	}

	/**
	 * @param alertRead
	 *            The alertRead to set.
	 */
	public void setAlertRead(Boolean alertRead) {
		this.alertRead = alertRead;
	}

	/**
	 * @return Returns the changedBy.
	 */
	public User getChangedBy() {
		return changedBy;
	}

	/**
	 * @param changedBy
	 *            The user that changed this alert
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
	 * @param dateChanged
	 *            The date this alert was changed
	 */
	public void setDateChanged(Date dateChanged) {
		this.dateChanged = dateChanged;
	}

	/**
	 * @return Returns the Recipients of this alert
	 */
	public Set<AlertRecipient> getRecipients() {
		return recipients;
	}

	/**
	 * @param recipients
	 *            The recipients of this alert
	 */
	public void setRecipients(Set<AlertRecipient> recipients) {
		this.recipients = recipients;
	}

	public void addRecipient(AlertRecipient r) {
		if (this.recipients == null)
			this.recipients = new HashSet<AlertRecipient>();
		r.setAlert(this);
		// duplicates are avoided by depending on the .equals and .hashcode 
		//  methods of Alert
		recipients.add(r);
	}

	public void addRecipient(User u) {
		addRecipient(new AlertRecipient(u, false));
	}
	
	public void removeRecipient(AlertRecipient r) {
		if (recipients != null) {
			if (recipients.contains(r))
				recipients.remove(r);
		}
	}
	
	public AlertRecipient getRecipient(User recipient) {
		for (AlertRecipient ar : recipients) {
			if (ar.getRecipient().equals(recipient))
				return ar;
		}
		return null;
	}

	// @override
	public String toString() {
		return "Alert: #" + alertId;
	}

}