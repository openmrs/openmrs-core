/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.notification.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.notification.Message;
import org.openmrs.notification.MessageException;
import org.openmrs.notification.MessageSender;
import org.openmrs.notification.MessageService;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public class MessageServiceImpl implements MessageService {
	
	private static final Log log = LogFactory.getLog(MessageServiceImpl.class);
	
	private MessageSender messageSender; // Delivers message
	
	/**
	 * Public constructor Required for use with spring's method injection. Be careful because this
	 * class requires a DAO Context in order to work properly. Please set the DAO context
	 */
	public MessageServiceImpl() {
	}

	/**
	 * Set the message sender.
	 *
	 * @param messageSender
	 */
	public void setMessageSender(MessageSender messageSender) {
		this.messageSender = messageSender;
	}
	
	public MessageSender getMessageSender() {
		return this.messageSender;
	}
	
	/**
	 * Send the message. All send calls go through this method.
	 *
	 * @param message the Message to be sent
	 * @see org.openmrs.notification.MessageService#sendMessage(org.openmrs.notification.Message)
	 */
	public void sendMessage(Message message) throws MessageException {
		try {
			messageSender.send(message);
		}
		catch (Exception e) {
			log.error("Message could not be sent due to " + e.getMessage(), e);
			throw new MessageException(e);
		}
	}
	
	/**
	 * @see org.openmrs.notification.MessageService#createMessage(java.lang.String,
	 *      java.lang.String, java.lang.String, java.lang.String, java.lang.String,
	 *      java.lang.String, java.lang.String)
	 */
	public Message createMessage(String recipients, String sender, String subject, String content, String attachment,
	        String attachmentContentType, String attachmentFileName) throws MessageException {
		Message message = new Message();
		message.setRecipients(recipients);
		message.setSender(sender);
		message.setContent(content);
		message.setSubject(subject);
		message.setAttachment(attachment);
		message.setAttachmentContentType(attachmentContentType);
		message.setAttachmentFileName(attachmentFileName);
		return message;
	}
	
}
