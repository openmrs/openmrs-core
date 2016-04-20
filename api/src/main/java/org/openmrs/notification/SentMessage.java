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
 * This is used by the System administrator to view all sent messages stored in the 
 * database. For normal messaging functions use the Message api
 * 
 * @see org.openmrs.notification.Message
 * @see org.openmrs.notification.MessageService
 */
public class SentMessage extends BaseOpenmrsObject implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Integer messageId;
	
	private User sender;
	
	private String subject;
	
	private String content;
	
	private String contentType;
	
	private Date sentDate;
	
	/**
	 * default constructor
	 */
	public SentMessage() {
	}
	
	/**
	 *  creates a SentMessage object with sender 
	 *  
	 *  @param message been sent
	 *  @param sender the sender of this message
	 */
	public SentMessage(Message message, User sender) {
		this(message.getId(), sender, message.getSubject(), message.getContent(), message.getContentType(), message
		        .getSentDate());
	}
	
	/**
	 * constructor to create a SentMessage object from all fields
	 * 
	 * @param messageId the id of the message to save
	 * @param sender of the message
	 * @param subject subject of this message
	 * @param content message content
	 * @param contentType content type of message 
	 * @param sentDate date this message was sent
	 */
	public SentMessage(Integer messageId, User sender, String subject, String content, String contentType, Date sentDate) {
		setMessageId(messageId);
		setSender(sender);
		setSubject(subject);
		setContent(content);
		setContentType(contentType);
		setSentDate(sentDate);
	}
	
	@Override
	public Integer getId() {
		return getMessageId();
	}

	@Override
	public void setId(Integer id) {
		setMessageId(id);
	}
	
	/**
	 *  @return the integer identifier for this message
	 */
	public Integer getMessageId() {
		return this.messageId;
	}
	
	/**
	 * sets an identifier for this message
	 * 
	 * @param messageId 
	 */
	public void setMessageId(Integer messageId) {
		this.messageId = messageId;
	}
	
	/**
	 * @return user object for  the sender of this message
	 */
	public User getSender() {
		return this.sender;
	}
	
	/**
	 *  sets the id of the sender of this message
	 *  
	 *  @param senderId specifies the id of the message sender
	 */
	public void setSender(User sender) {
		this.sender = sender;
	}
	
	/**
	 * @return the subject of this message
	 */
	public String getSubject() {
		return this.subject;
	}
	
	/**
	 *  sets the subject of the sent message
	 *  
	 *  @param subject String representation of message subject
	 */
	public void setSubject(String subject) {
		this.subject = subject;
	}
	
	/**
	 * @return the content of the sent message
	 */
	public String getContent() {
		return this.content;
	}
	
	/**
	 * set content of message
	 * 
	 * @param content String representation of this message
	 */
	public void setContent(String content) {
		this.content = content;
	}
	
	/**
	 * @return content type of the sent message
	 */
	public String getContentType() {
		return this.contentType;
	}
	
	/**
	 * setter method for contentType
	 * 
	 * @param contentType content type of sent message
	 */
	public void setContentType(String contentType) {
		this.contentType = contentType;
	}
	
	/**
	 *  @return the date this message was sent
	 */
	public Date getSentDate() {
		return this.sentDate;
	}
	
	/**
	 *  setter for sentDate
	 *  
	 *  @param sentDate date message was sent
	 */
	public void setSentDate(Date sentDate) {
		this.sentDate = sentDate;
	}
}
