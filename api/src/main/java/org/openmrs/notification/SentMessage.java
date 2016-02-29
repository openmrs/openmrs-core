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

public class SentMessage  implements  Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private Integer sentMessageId;
	private Integer senderId;
	private Integer recieverId;
	private String message;
	private Date dateSent;
	private String messageType;
	
	public SentMessage() {}
	
	public SentMessage(Message message, Integer senderId, Integer recieverId) {
		this.sentMessageId = message.getId();
		this.message = message.getSubject() + ", " + message.getContent();
		this.dateSent = message.getSentDate();
		this.messageType = message.getContentType();
		this.senderId = senderId;
		this.recieverId = recieverId;
	}
	
	public Integer getSenderId() {
		return senderId;
	}
	
	public void setSenderId(Integer senderId) {
		this.senderId = senderId;
	}
	
	public Integer getRecieverId() {
		return recieverId;
	}
	
	public void setRecieverId(Integer recieverId) {
		this.recieverId = recieverId;
	}
	
	public String getMessage() {
		return message;
	}
	
	public void setMessage(String message) {
		this.message = message;
	}
	
	public Date getDateSent() {
		return dateSent;
	}
	
	public void setDateSent(Date dateSent) {
		this.dateSent = dateSent;
	}
	
	public String getMessageType() {
		return messageType;
	}
	
	public void setMessageType(String messageType) {
		this.messageType = messageType;
	}
	
	public Integer getSentMessageId() {
		return sentMessageId;
	}
	
	public void setSentMessageId(Integer sentMessageId) {
		this.sentMessageId = sentMessageId;
	}
}