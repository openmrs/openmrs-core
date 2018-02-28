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
import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.User;
import org.openmrs.test.BaseContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;

public class SentMessageServiceTest extends BaseContextSensitiveTest {

	private static final String TEST_DATASET = "org/openmrs/notification/include/SentMessageServiceTest.xml";

	private static final Integer EXISTING_SENT_MESSAGE_ID = 1001;

	private static final Integer EXISTING_USER_ID = 3017;

	@Autowired
	private SentMessageService sentMessageService;

	@Before
	public void setUp() {
		executeDataSet(TEST_DATASET);
	}

	/**
	 *@see SentMessageService#saveSentMessage(SentMessage)
	 */
	@Test
	public void saveSentMessage_shouldSaveNewSentMessage() throws Exception {
		User sender = new User();
		sender.setId(EXISTING_USER_ID);

		SentMessage sentMessage = new SentMessage();
		sentMessage.setSender(sender);
		sentMessage.setSubject("Subject");
		sentMessage.setContent("Content text");

		SentMessage saved = sentMessageService.saveSentMessage(sentMessage);
		SentMessage newlySaved = sentMessageService.getSentMessage(saved.getId());

		assertNotNull(saved);
		assertNotNull(newlySaved);
		assertEquals(saved.getSubject(), newlySaved.getSubject());
		assertEquals(saved.getContent(), newlySaved.getContent());
	}

	/**
	 *@see SentMessageService#saveSentMessage(SentMessage)
	 */
	@Test
	public void saveSentMessage_shouldUpdateExistingSentMessage() throws Exception{
		SentMessage sentMessage = sentMessageService.getSentMessage(EXISTING_SENT_MESSAGE_ID);
		assertNotNull(sentMessage);

		final String NEW_SUBJECT = "A new Subject";
		sentMessage.setSubject(NEW_SUBJECT);
		sentMessageService.saveSentMessage(sentMessage);

		SentMessage updatedSentMessage = sentMessageService.getSentMessage(EXISTING_SENT_MESSAGE_ID);

		assertNotNull(updatedSentMessage);
		assertEquals(sentMessage.getId(), updatedSentMessage.getId());
		assertEquals(updatedSentMessage.getSubject(), NEW_SUBJECT);
	}

	/**
	 *@see SentMessageService#getSentMessage(Integer)
	 */
	@Test
	public void getSentMessage_shouldGetSentMessageGivenMessageId() {
		SentMessage sentMessage = sentMessageService.getSentMessage(EXISTING_SENT_MESSAGE_ID);
		assertNotNull(sentMessage);
		assertEquals(sentMessage.getId(), EXISTING_SENT_MESSAGE_ID);
		assertEquals(sentMessage.getSubject(), "Subject 1");
	}

	/**
	 *@see SentMessageService#getSentMessages(User)
	 */
	@Test
	public void getSentMessages_shouldGetAllSentMessagesFromGivenSender() {
		User user = new User();
		user.setId(EXISTING_USER_ID);

		List<SentMessage> sentMessages = sentMessageService.getSentMessages(user);
		assertNotNull(sentMessages);
		assertEquals(sentMessages.size(), 3);
	}

	/**
	 *@see SentMessageServiceTest#getAllSentMessages_shouldGetAllSentMessages()
	 */
	@Test
	public void getAllSentMessages_shouldGetAllSentMessages() {
		List<SentMessage> sentMessages = sentMessageService.getAllSentMessages();
		assertNotNull(sentMessages);
		assertEquals(sentMessages.size(), 5);
	}
}
