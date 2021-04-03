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

public class Message implements Serializable {
	
	/**
	 *
	 */
	private static final long serialVersionUID = -5392076713513109152L;
	
	private Integer id;
	
	private StringBuilder recipients = new StringBuilder();
	
	private String sender;
	
	private String subject;
	
	private String content;
	
	private String contentType;
	
	private Date sentDate;
	
	private String attachment;
	
	private String attachmentContentType;
	
	private String attachmentFileName;
	
	public Message() {
	}
	
	public Message(Integer id, String recipients, String sender, String subject, String content) {
		this.id = id;
		this.recipients.append(recipients);
		this.sender = sender;
		this.subject = subject;
		this.content = content;
	}
	
	public Message(Integer id, String recipients, String sender, String subject, String content, String contentType) {
		this.id = id;
		this.recipients.append(recipients);
		this.sender = sender;
		this.subject = subject;
		this.content = content;
		this.contentType = contentType;
	}
	
	/**
	 * @param id
	 * @param recipients
	 * @param sender
	 * @param subject
	 * @param content
	 * @param attachment
	 * @param attachmentContentType
	 * @param attachmentFileName
	 * <strong>Should</strong> fill in all parameters
	 */
	public Message(Integer id, String recipients, String sender, String subject, String content, String attachment,
	    String attachmentContentType, String attachmentFileName) {
		this(id, recipients, sender, subject, content);
		this.attachment = attachment;
		this.attachmentContentType = attachmentContentType;
		this.attachmentFileName = attachmentFileName;
	}
	
	public Message(Integer id, String recipients, String sender, String subject, String content, String contentType,
	    String attachment, String attachmentContentType, String attachmentFileName) {
		this(id, recipients, sender, subject, content, contentType);
		this.attachment = attachment;
		this.attachmentContentType = attachmentContentType;
		this.attachmentFileName = attachmentFileName;
	}
	
	public void setId(Integer id) {
		this.id = id;
	}
	
	public Integer getId() {
		return this.id;
	}
	
	/**
	 * Auto generated method comment
	 *
	 * @param recipients
	 * <strong>Should</strong> set multiple recipients
	 */
	public void setRecipients(String recipients) {
		if (recipients != null) {
			this.recipients = new StringBuilder(recipients);
		}
	}
	
	public String getRecipients() {
		return this.recipients.toString();
	}
	
	/**
	 * Add a new receiver of this message. Will append to current list of recipients by inserting a
	 * comma. If no recipients exist, this method has no effect (TODO is this the correct
	 * behavior??!).
	 *
	 * @param recipient a new address to assign
	 * <strong>Should</strong> add new recipient
	 */
	public void addRecipient(String recipient) {
		if (recipient != null) {
			this.recipients.append(",").append(recipient);
		}
	}
	
	public void setSender(String sender) {
		this.sender = sender;
	}
	
	public String getSender() {
		return this.sender;
	}
	
	public void setSubject(String subject) {
		this.subject = subject;
	}
	
	public String getSubject() {
		return this.subject;
	}
	
	public void setSentDate(Date sentDate) {
		this.sentDate = sentDate;
	}
	
	public Date getSentDate() {
		return this.sentDate;
	}
	
	public void setContent(String content) {
		this.content = content;
	}
	
	public String getContent() {
		return this.content;
	}
	
	public void setContentType(String contentType) {
		this.contentType = contentType;
	}
	
	public String getContentType() {
		return contentType;
	}
	
	public String getAttachment() {
		return attachment;
	}
	
	public void setAttachment(String attachment) {
		this.attachment = attachment;
	}
	
	public String getAttachmentContentType() {
		return attachmentContentType;
	}
	
	public void setAttachmentContentType(String attachmentContentType) {
		this.attachmentContentType = attachmentContentType;
	}
	
	public String getAttachmentFileName() {
		return attachmentFileName;
	}
	
	public void setAttachmentFileName(String attachmentFileName) {
		this.attachmentFileName = attachmentFileName;
	}
	
	/**
	 * @return true if this message has an attachment
	 * <strong>Should</strong> return true if this message has an attachment
	 */
	public boolean hasAttachment() {
		return attachment != null;
	}
	
}
