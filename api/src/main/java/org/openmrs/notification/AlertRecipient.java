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

import java.util.Date;

import org.hibernate.envers.Audited;
import org.openmrs.BaseOpenmrsObject;
import org.openmrs.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;


/**
 * This class is essentially a wrapper for the user object. The alert is assigned to each recipient.
 * A recipient then has either "read" the alert or has not.
 * 
 * @see org.openmrs.notification.Alert
 */
@Entity
@Table(name = "notification_alert_recipient")
@Audited
public class AlertRecipient extends BaseOpenmrsObject {
	
	private static final long serialVersionUID = -507111109155L;
	
	@JoinColumn(name = "alert_id")
	@Id
	@ManyToOne
	private Alert alert;

	@JoinColumn(name = "user_id", updatable = false, insertable = false)
	@Id
	@ManyToOne
	private User recipient;
	
	@Column(name = "alert_read", length = 1)
	private Boolean alertRead = false;
	
	@Column(name = "date_changed", length = 19, updatable = false, insertable = false)
	private Date dateChanged;
	
	// necessary for hql queries
	@Column(name = "user_id", nullable = false, updatable = false, insertable = false)
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
	@Override
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
	
	/**
	 * @since 1.5
	 * @see org.openmrs.OpenmrsObject#getId()
	 */
	@Override
	public Integer getId() {
		throw new UnsupportedOperationException();
	}
	
	/**
	 * @since 1.5
	 * @see org.openmrs.OpenmrsObject#setId(java.lang.Integer)
	 */
	@Override
	public void setId(Integer id) {
		throw new UnsupportedOperationException();
	}
	
}
