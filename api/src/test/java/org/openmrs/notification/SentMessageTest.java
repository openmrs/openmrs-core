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

import java.util.Date;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.openmrs.User;

public class SentMessageTest {
	
	@Test
	public void SentMessage_shouldFillInAllParameters() {
		Message message = new Message();
		message.setId(1);
		message.setSubject("hello world");
		message.setContent("hello world content");
		message.setContentType("plain text");
		message.setSentDate(new Date());
		
		User sender = new User();
		User receiver = new User();
		sender.setUserId(2);
		receiver.setUserId(3);
		
		SentMessage sm = new SentMessage(message, sender, receiver);
		
		Assert.assertEquals(message.getId(), sm.getMessageId());
		Assert.assertEquals(Integer.valueOf(2), sm.getSender().getUserId());
		Assert.assertEquals(Integer.valueOf(3), sm.getReceiver().getUserId());
		Assert.assertEquals(message.getSubject(), sm.getSubject());
		Assert.assertEquals(message.getContent(), sm.getContent());
		Assert.assertEquals(message.getContentType(), sm.getContentType());
		Assert.assertEquals(message.getSentDate(), sm.getSentDate());
	}
}
