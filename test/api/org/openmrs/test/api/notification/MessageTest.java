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
package org.openmrs.test.api.notification;

import junit.framework.TestCase;

import org.openmrs.notification.Message;

/**
 * Unit testing for the Message class
 */
public class MessageTest extends TestCase {

	private Message createTestMessage1() {
		int id = 1;
		String recipients = "recipient1@example.com,recipient2@example.com";
		String sender = "foo@bar.com";
		String subject = "tres important";
		String content = "message";
		String attachment = "attachment";
		String attachmentContentType = "text/plain";
		String attachmentFileName = "moo.txt";
		
		return new Message(id, recipients, sender, subject, content, attachment, attachmentContentType, attachmentFileName);
	}
	
	private Message createTestMessage2() {
		int id = 1;
		String recipients = "recipient1@example.com";
		String sender = "foo@bar.com";
		String subject = "moo";
		String content = "message";
		
		return new Message(id, recipients, sender, subject, content);
	}
	
	public void testShouldConstructor() throws Exception {
		int id = 1;
		String recipients = "recipient1@example.com,recipient2@example.com";
		String sender = "foo@bar.com";
		String subject = "tres important";
		String content = "message";
		String attachment = "attachment";
		String attachmentContentType = "text/plain";
		String attachmentFileName = "moo.txt";
		
		Message toTest = new Message(id, recipients, sender, subject, content, attachment, attachmentContentType, attachmentFileName);
		assertEquals((int)toTest.getId(), 1);
		assertEquals(recipients, toTest.getRecipients());
		assertEquals(sender, toTest.getSender());
		assertEquals(subject, toTest.getSubject());
		assertEquals(content, toTest.getContent());
	}
	
	public void testShouldSetRecipients() throws Exception {
		Message testMessage = createTestMessage1();
		
		String recipients = "recipient1@example.com,recipient2@example.com";
		
		testMessage.setRecipients(recipients);
		
		assertEquals(testMessage.getRecipients(), recipients);
	}
	
	public void testShouldAddRecipient() throws Exception {
		Message testMessage = createTestMessage1();
		
		String oldRecipients = testMessage.getRecipients();
		String newRecipient = "bob@example.com";
		
		testMessage.addRecipient(newRecipient);
		
		assertEquals(testMessage.getRecipients(), oldRecipients +","+ newRecipient);
	}
	
	public void testShouldHasAttachment() throws Exception {
		Message testMessage1 = createTestMessage1();
		Message testMessage2 = createTestMessage2();
		
		assertTrue(testMessage1.hasAttachment());
		assertFalse(testMessage2.hasAttachment());
	}
}
