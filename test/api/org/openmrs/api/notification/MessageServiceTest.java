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
package org.openmrs.api.notification;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.api.context.Context;
import org.openmrs.notification.Message;
import org.openmrs.notification.MessageException;
import org.openmrs.notification.MessageService;
import org.openmrs.test.BaseContextSensitiveTest;

/**
 * Unit tests for the MessageService.
 */
public class MessageServiceTest extends BaseContextSensitiveTest {
	
	private static final String NO_SMTP_SERVER_ERROR = "Could not connect to SMTP host:";
	
	MessageService ms = null;
	
	/**
	 * Run this before each unit test in this class. The "@Before" method in
	 * {@link BaseContextSensitiveTest} is run right before this method.
	 * 
	 * @throws Exception
	 */
	@Before
	public void runBeforeEachTest() throws Exception {
		executeDataSet("org/openmrs/api/notification/include/MessageServiceTest-initial.xml");
		
		ms = Context.getMessageService();
	}
	
	@Test
	public void shouldCreateMessage() throws Exception {
		String recipients = "foo@bar.com,marco@polo.com";
		String sender = "me@mydomain.com";
		String subject = "foo";
		String message = "content";
		String attachment = "inga";
		String attachmentContentType = "text/plain";
		String attachmentFileName = "inga.txt";
		
		Message msg1 = ms.createMessage(recipients, sender, subject, message);
		Message msg2 = ms.createMessage(subject, message);
		Message msg3 = ms.createMessage(sender, subject, message);
		Message msg4 = ms.createMessage(recipients, sender, subject, message, attachment, attachmentContentType,
		    attachmentFileName);
		
		assertEquals(recipients, msg1.getRecipients());
		assertEquals(recipients, msg4.getRecipients());
		
		assertEquals(sender, msg1.getSender());
		assertEquals(sender, msg3.getSender());
		assertEquals(sender, msg4.getSender());
		
		assertEquals(subject, msg1.getSubject());
		assertEquals(subject, msg2.getSubject());
		assertEquals(subject, msg3.getSubject());
		assertEquals(subject, msg4.getSubject());
		
		assertEquals(message, msg1.getContent());
		assertEquals(message, msg2.getContent());
		assertEquals(message, msg3.getContent());
		assertEquals(message, msg4.getContent());
		
		assertEquals(attachment, msg4.getAttachment());
		assertEquals(attachmentContentType, msg4.getAttachmentContentType());
		assertEquals(attachmentFileName, msg4.getAttachmentFileName());
	}
	
	@Test
	public void shouldSendMessage() throws Exception {
		Message tryToSend1 = ms.createMessage("recipient@example.com", "sender@example.com", "subject", "content");
		try {
			ms.sendMessage(tryToSend1);
		}
		catch (MessageException e) {
			//So that this test doesn't fail just because the user isn't running an SMTP server.
			if (!e.getMessage().contains(NO_SMTP_SERVER_ERROR)) {
				e.printStackTrace();
				fail();
			}
		}
		
		Message tryToSend2 = ms.createMessage("recipient@example.com,recipient2@example.com", "openmrs.emailer@gmail.com",
		    "subject", "content", "moo", "text/plain", "moo.txt");
		try {
			ms.sendMessage(tryToSend2);
		}
		catch (MessageException e) {
			//So that this test doesn't fail just because the user isn't running an SMTP server.
			if (!e.getMessage().contains(NO_SMTP_SERVER_ERROR)) {
				e.printStackTrace();
				fail();
			}
		}
	}
	
}
