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

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.test.BaseContextSensitiveTest;

public class SentMessageServiceTest extends BaseContextSensitiveTest {

	private SentMessageService sentMessageService;
	private final String SENT_MESSAGES_IN_DB = "org/openmrs/notification/include/SentMessageServiceTest.xml";
	
	@Before
	public void setup() throws Exception {
		executeDataSet(SENT_MESSAGES_IN_DB);
		sentMessageService = Context.getSentMessageService();
	}
	
	@Test
	public void saveSentMessage_shouldReturnTheSavedMessage() {
		Message m = new Message();
		m.setId(17);
		m.setSubject("for test");
		m.setContent("content for testing");
		User sender = new User();
		sender.setId(27);
		
		SentMessage sm = sentMessageService.saveSentMessage(new SentMessage(m, sender, null));
		
		Assert.assertNotNull(sm);
		Assert.assertEquals(m.getId(), sm.getMessageId());
		Assert.assertEquals(sender.getUserId(), sm.getSender().getUserId());
	}
	
	@Test
	public void getSentMessage_shouldGetMessageById() {
		SentMessage sm = sentMessageService.getSentMessage(11);
		
		Assert.assertNotNull(sm);
		Assert.assertEquals(11, (int)sm.getMessageId());
		Assert.assertEquals(21, (int)sm.getSender().getUserId());
		Assert.assertEquals(31, (int)sm.getReceiver().getUserId());
	}
	
	@Test
	public void getSentMessages_shouldGetAllMessagesFromUser() {
		User user = new User();
		user.setUserId(21);
		
		List<SentMessage> usersMessages = sentMessageService.getSentMessages(user);
		
		Assert.assertNotNull(usersMessages);
		Assert.assertEquals(3, usersMessages.size());
	}

	@Test
	public void getAllSentMessages_shouldGetAllSentMessagesFromTheDB() {
		List<SentMessage> sentMessages = sentMessageService.getAllSentMessages();
		
		Assert.assertNotNull(sentMessages);
		Assert.assertEquals(6, sentMessages.size());
	}
}
