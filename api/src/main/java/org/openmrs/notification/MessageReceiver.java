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

import java.io.Serializable;
import java.util.Date;

import org.openmrs.BaseOpenmrsObject;
import org.openmrs.User;

/**
 * This stores all receivers of a particular message inside the db
 * 
 * @see org.openmrs.notification.SentMessage
 */
public class MessageReceiver extends BaseOpenmrsObject implements Serializable {

	private static final long serialVersionUID = 1L;
	private int id;
	private User receiver;
	private SentMessage sentMessage;
	private Date sentDate;
	
	public MessageReceiver() {}
	
	public MessageReceiver(SentMessage sentMessage, User receiver) {
		setSentMessage(sentMessage);
		setReceiver(receiver);
		setSentDate(sentMessage.getSentDate());
	}
	
	@Override
	public Integer getId() {
		return id;
	}
	
	@Override
	public void setId(Integer id) {
		this.id = id;
	}

	public User getReceiver() {
		return this.receiver;
	}

	public void setReceiver(User receiver) {
		this.receiver = receiver;
	}

	public SentMessage getSentMessage() {
		return this.sentMessage;
	}

	public void setSentMessage(SentMessage sentMessage) {
		this.sentMessage = sentMessage;
	}

	public Date getSentDate() {
		return this.sentDate;
	}

	public void setSentDate(Date dateSent) {
		this.sentDate = dateSent;
	}
}
