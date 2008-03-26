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
package org.openmrs.test.notification;

import java.io.File;
import java.util.Date;

import org.openmrs.test.BaseContextSensitiveTest;

import org.openmrs.notification.Message;
import org.openmrs.notification.MessageException;
import org.openmrs.notification.MessageService;
import org.openmrs.api.context.Context;

/**
 * Tests the {@link DataExportReportObject} class
 * 
 * TODO clean up, finish, add methods to this test class
 */
public class MessageServiceTest extends BaseContextSensitiveTest {
	
	
	@Override
	protected void onSetUpInTransaction() throws Exception {
		initializeInMemoryDatabase();
		executeDataSet("org/openmrs/test/notification/include/MessageServiceTest-initial.xml");
		authenticate();
	}
	
	
	/**
	 * 
	 * @throws Exception
	 */
	public void testSendMessage() throws Exception { 
		MessageService messageService = Context.getMessageService();
		
		Message message = new Message();
		message.setSubject("Subject");
		message.setContent("Content");
		message.addRecipient("recipient@example.com");
		message.setSender("sender@example.com");
		message.setSentDate(new Date());
		
		try { 
			messageService.sendMessage(message);	
			
		} catch (MessageException e) {
			e.printStackTrace();
			fail("message not sent");
		}
		
	}
	

	

}