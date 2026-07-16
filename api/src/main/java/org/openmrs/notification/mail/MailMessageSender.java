/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.notification.mail;

import java.util.Properties;

import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;

import org.openmrs.api.context.Context;
import org.openmrs.notification.Message;
import org.openmrs.notification.MessageException;
import org.openmrs.notification.MessageSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

public class MailMessageSender implements MessageSender {

	private static final Logger log = LoggerFactory.getLogger(MailMessageSender.class);

	/**
	 * Jakarta Mail session
	 */
	private Session session;

	/**
	 * Default public constructor.
	 */
	public MailMessageSender() {
	}

	/**
	 * Public constructor.
	 *
	 * @param session
	 */
	public MailMessageSender(Session session) {
		this.session = session;
	}

	/**
	 * Set Jakarta Mail session.
	 *
	 * @param session
	 */
	public void setMailSession(Session session) {
		this.session = session;
	}

	/**
	 * Send the message.
	 *
	 * @param message the message to be sent
	 */
	@Override
	public void send(Message message) throws MessageException {
		try {
			MimeMessage mimeMessage = createMimeMessage(message);
			Transport.send(mimeMessage);
		} catch (Exception e) {
			log.error("failed to send message", e);

			// catch mail-specific exception and re-throw it as app-specific exception
			throw new MessageException(e);
		}
	}

	/**
	 * Converts the message object to a mime message in order to prepare it to be sent.
	 *
	 * @param message
	 * @return MimeMessage
	 */
	public MimeMessage createMimeMessage(Message message) throws Exception {

		if (message.getRecipients() == null) {
			throw new MessageException("Message must contain at least one recipient");
		}

		Properties mailProperties = Context.getMailProperties();

		// set the content-type to the default if it isn't defined in Message
		if (!StringUtils.hasText(message.getContentType())) {
			String contentType = mailProperties.getProperty("mail.default_content_type", "text/plain");
			message.setContentType(contentType);
		}

		MimeMessage mimeMessage = new MimeMessage(session);

		String sender = message.getSender();
		if (!StringUtils.hasText(sender)) {
			sender = mailProperties.getProperty("mail.from");
		}
		if (StringUtils.hasText(sender)) {
			InternetAddress senderAddress = new InternetAddress(sender);
			mimeMessage.setFrom(senderAddress);
			mimeMessage.setSender(senderAddress);
		}

		mimeMessage.setRecipients(jakarta.mail.Message.RecipientType.TO,
		    InternetAddress.parse(message.getRecipients(), false));
		mimeMessage.setSubject(message.getSubject());

		if (!message.hasAttachment()) {
			mimeMessage.setContent(message.getContent(), message.getContentType());
		} else {
			mimeMessage.setContent(createMultipart(message));
		}

		return mimeMessage;
	}

	/**
	 * Creates a MimeMultipart, so that we can have an attachment.
	 *
	 * @param message
	 * @return
	 */
	private MimeMultipart createMultipart(Message message) throws Exception {
		MimeMultipart toReturn = new MimeMultipart();

		MimeBodyPart textContent = new MimeBodyPart();
		textContent.setContent(message.getContent(), message.getContentType());

		MimeBodyPart attachment = new MimeBodyPart();
		attachment.setContent(message.getAttachment(), message.getAttachmentContentType());
		attachment.setFileName(message.getAttachmentFileName());

		toReturn.addBodyPart(textContent);
		toReturn.addBodyPart(attachment);

		return toReturn;
	}

}
