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

import jakarta.mail.NoSuchProviderException;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.openmrs.api.context.Context;
import org.openmrs.notification.Message;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mockStatic;

/**
 * Tests for {@link MailMessageSender}. These tests verify that the Jakarta Mail API resolves
 * correctly via the Angus Mail SPI provider and that MIME messages are constructed as expected
 * without requiring a running SMTP server.
 */
class MailMessageSenderTest {

	private MailMessageSender sender;

	private Session session;

	private MockedStatic<Context> contextMock;

	@BeforeEach
	void setUp() {
		Properties props = new Properties();
		props.setProperty("mail.smtp.host", "localhost");
		props.setProperty("mail.smtp.port", "25");
		props.setProperty("mail.transport.protocol", "smtp");
		session = Session.getInstance(props);
		sender = new MailMessageSender(session);

		Properties mailProperties = new Properties();
		mailProperties.setProperty("mail.default_content_type", "text/plain");
		mailProperties.setProperty("mail.from", "default@example.com");
		contextMock = mockStatic(Context.class);
		contextMock.when(Context::getMailProperties).thenReturn(mailProperties);
	}

	@AfterEach
	void tearDown() {
		contextMock.close();
	}

	@Test
	void getTransport_shouldResolveSmtpViaAngusSpi() throws NoSuchProviderException {
		Transport transport = session.getTransport("smtp");

		assertNotNull(transport, "SMTP transport should be resolved via Angus Mail SPI");
	}

	@Test
	void createMimeMessage_shouldCreateMimeMessageWithCorrectFields() throws Exception {
		Message message = new Message(1, "recipient@example.com", "sender@example.com", "Test Subject", "Test body");

		MimeMessage mimeMessage = sender.createMimeMessage(message);

		assertNotNull(mimeMessage);
		assertEquals("Test Subject", mimeMessage.getSubject());
		assertEquals("sender@example.com", mimeMessage.getFrom()[0].toString());
		assertEquals(1, mimeMessage.getAllRecipients().length);
		assertEquals("recipient@example.com", mimeMessage.getAllRecipients()[0].toString());
	}

	@Test
	void createMimeMessage_shouldHandleMultipleRecipients() throws Exception {
		Message message = new Message(1, "a@example.com,b@example.com", "sender@example.com", "Subject", "Body");

		MimeMessage mimeMessage = sender.createMimeMessage(message);

		assertEquals(2, mimeMessage.getAllRecipients().length);
	}

	@Test
	void createMimeMessage_shouldDefaultContentTypeToTextPlain() throws Exception {
		Message message = new Message(1, "recipient@example.com", "sender@example.com", "Subject", "Body");

		MimeMessage mimeMessage = sender.createMimeMessage(message);

		assertTrue(mimeMessage.getContentType().startsWith("text/plain"));
	}

	@Test
	void createMimeMessage_shouldCreateMultipartForAttachment() throws Exception {
		Message message = new Message(1, "recipient@example.com", "sender@example.com", "Subject", "Body",
		        "attachment content", "text/plain", "file.txt");

		MimeMessage mimeMessage = sender.createMimeMessage(message);

		assertTrue(mimeMessage.getContent() instanceof MimeMultipart);
		MimeMultipart multipart = (MimeMultipart) mimeMessage.getContent();
		assertEquals(2, multipart.getCount());
	}

	@Test
	void createMimeMessage_shouldUseDefaultSenderFromMailProperties() throws Exception {
		Message message = new Message(1, "recipient@example.com", null, "Subject", "Body");

		MimeMessage mimeMessage = sender.createMimeMessage(message);

		assertEquals("default@example.com", mimeMessage.getFrom()[0].toString());
	}

}
