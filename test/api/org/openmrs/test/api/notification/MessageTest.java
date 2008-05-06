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
		String recipients = "vergil@gmail.com,cyarbrough@pih.org";
		String sender = "foo@bar.com";
		String subject = "tres important";
		String content = "message";
		
		return new Message(id, recipients, sender, subject, content);
	}
	
	public void testConstructor() throws Exception {
		int id = 1;
		String recipients = "vergil@gmail.com,cyarbrough@pih.org";
		String sender = "foo@bar.com";
		String subject = "tres important";
		String content = "message";
		
		Message toTest = new Message(id, recipients, sender, subject, content);
		assertEquals((int)toTest.getId(), 1);
		assertEquals(recipients, toTest.getRecipients());
		assertEquals(sender, toTest.getSender());
		assertEquals(subject, toTest.getSubject());
		assertEquals(content, toTest.getContent());
	}
	
	public void testSetRecipients() throws Exception {
		Message testMessage = createTestMessage1();
		
		String recipients = "vergil@gmail.com,cyarbrough@pih.org";
		
		testMessage.setRecipients(recipients);
		
		assertEquals(testMessage.getRecipients(), recipients);
	}
	
	public void testAddRecipient() throws Exception {
		Message testMessage = createTestMessage1();
		
		String oldRecipients = testMessage.getRecipients();
		String newRecipient = "bob@gmail.com";
		
		testMessage.addRecipient(newRecipient);
		
		assertEquals(testMessage.getRecipients(), oldRecipients +","+ newRecipient);
	}
}
