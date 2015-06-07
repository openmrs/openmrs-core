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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.openmrs.test.Verifies;

/**
 * Unit testing for the Message class
 */
public class MessageTest {
	
	/**
	 * Convenience method to create a reusable/retestable message object with an attachment.
	 * 
	 * @return a testable {@link Message} object
	 */
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
	
	/**
	 * Convenience method to create a reusable/retestable message object with only the basic
	 * to/form/subject/content filled in.
	 * 
	 * @return a testable {@link Message} object
	 */
	private Message createTestMessage2() {
		int id = 1;
		String recipients = "recipient1@example.com";
		String sender = "foo@bar.com";
		String subject = "moo";
		String content = "message";
		
		return new Message(id, recipients, sender, subject, content);
	}
	
	/**
	 * @see {@link Message#Message(Integer,String,String,String,String,String,String,String)}
	 */
	@Test
	@Verifies(value = "should fill in all parameters", method = "Message(Integer,String,String,String,String,String,String,String)")
	public void Message_shouldFillInAllParameters() throws Exception {
		int id = 1;
		String recipients = "recipient1@example.com,recipient2@example.com";
		String sender = "foo@bar.com";
		String subject = "tres important";
		String content = "message";
		String attachment = "attachment";
		String attachmentContentType = "text/plain";
		String attachmentFileName = "moo.txt";
		
		Message toTest = new Message(id, recipients, sender, subject, content, attachment, attachmentContentType,
		        attachmentFileName);
		assertEquals((int) toTest.getId(), 1);
		assertEquals(recipients, toTest.getRecipients());
		assertEquals(sender, toTest.getSender());
		assertEquals(subject, toTest.getSubject());
		assertEquals(content, toTest.getContent());
	}
	
	/**
	 * @see {@link Message#setRecipients(String)}
	 */
	@Test
	@Verifies(value = "should set multiple recipients", method = "setRecipients(String)")
	public void setRecipients_shouldSetMultipleRecipients() throws Exception {
		Message testMessage = createTestMessage1();
		
		String recipients = "recipient1@example.com,recipient2@example.com";
		
		testMessage.setRecipients(recipients);
		
		assertEquals(testMessage.getRecipients(), recipients);
	}
	
	/**
	 * @see {@link Message#addRecipient(String)}
	 */
	@Test
	@Verifies(value = "should add new recipient", method = "addRecipient(String)")
	public void addRecipient_shouldAddNewRecipient() throws Exception {
		Message testMessage = createTestMessage1();
		
		String oldRecipients = testMessage.getRecipients();
		String newRecipient = "bob@example.com";
		
		testMessage.addRecipient(newRecipient);
		
		assertEquals(testMessage.getRecipients(), oldRecipients + "," + newRecipient);
	}
	
	/**
	 * @see {@link Message#hasAttachment()}
	 */
	@Test
	@Verifies(value = "should r return true if this message has an attachment", method = "hasAttachment()")
	public void hasAttachment_shouldRReturnTrueIfThisMessageHasAnAttachment() throws Exception {
		Message testMessage1 = createTestMessage1();
		Message testMessage2 = createTestMessage2();
		
		assertTrue(testMessage1.hasAttachment());
		assertFalse(testMessage2.hasAttachment());
	}
	
}
