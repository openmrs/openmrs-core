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

import java.io.Serializable;
import java.util.Date;

import org.openmrs.User;

/**
 * This class is essentially a wrapper for the user object. The alert is assigned to each recipient.
 * A recipient then has either "read" the alert or has not.
 * 
 * @see org.openmrs.notification.Alert
 */
public class AlertRecipient implements Serializable {
	
	private static final long serialVersionUID = -507111109155L;
	
	private Alert alert;
	
	private User recipient;
	
	private Boolean alertRead = false;
	
	private Date dateChanged;
	
	// necessary for hql queries
	private Integer recipientId;
	
	/** Default empty constructor */
	public AlertRecipient() {
	}
	
	/** Initializes a recipient with the given alert */
	public AlertRecipient(Alert a) {
		this.alert = a;
	}
	
	/** Initializes a recipient with the given alert and recipient/user */
	public AlertRecipient(Alert a, User recipient) {
		this.alert = a;
		setRecipient(recipient);
	}
	
	/**
	 * Initializes a recipient with the given user
	 * 
	 * @param read
	 * @param user
	 */
	public AlertRecipient(User user, Boolean read) {
		setRecipient(user);
		this.alertRead = read;
	}
	
	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		if (obj instanceof AlertRecipient) {
			AlertRecipient a = (AlertRecipient) obj;
			if (alert != null && a.getAlert() != null && recipient != null && a.getRecipient() != null)
				return (alert.equals(a.getAlert()) && recipient.equals(a.getRecipient()));
		}
		return obj == this;
	}
	
	/**
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		if (this.getAlert() == null)
			return super.hashCode();
		int hash = 8;
		hash = 31 * this.getAlert().hashCode() + hash;
		hash = 31 * this.getRecipient().hashCode() + hash;
		return hash;
	}
	
	/**
	 * @return Returns the alert.
	 */
	public Alert getAlert() {
		return alert;
	}
	
	/**
	 * @param alert The alert to set.
	 */
	public void setAlert(Alert alert) {
		this.alert = alert;
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
	
	// @override
	public String toString() {
		return "Alert: " + alert + ". Recipient: " + recipient;
	}
	
	/**
	 * @return Returns the read status
	 */
	public Boolean isAlertRead() {
		return alertRead;
	}
	
	/**
	 * @see #getAlertRead()
	 */
	public Boolean getAlertRead() {
		return isAlertRead();
	}
	
	/**
	 * @param alertRead The alertRead to set.
	 */
	public void setAlertRead(Boolean alertRead) {
		this.alertRead = alertRead;
	}
	
	/**
	 * @return Returns the recipient/user.
	 */
	public User getRecipient() {
		return recipient;
	}
	
	/**
	 * @param user The recipient/user to set.
	 */
	public void setRecipient(User user) {
		this.recipient = user;
		setRecipientId(user.getUserId());
	}
	
	/**
	 * @return Returns the recipientId.
	 */
	@SuppressWarnings("unused")
	private Integer getRecipientId() {
		return recipientId;
	}
	
	/**
	 * @param recipientId The recipientId to set.
	 */
	private void setRecipientId(Integer recipientId) {
		this.recipientId = recipientId;
	}
	
}
