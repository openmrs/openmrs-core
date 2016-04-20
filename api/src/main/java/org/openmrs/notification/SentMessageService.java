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

import org.openmrs.User;
import org.openmrs.api.APIException;
import org.openmrs.notification.db.SentMessageDAO;

public interface SentMessageService {
	
	/**
	 * Used by Spring to set the specific/chosen database access implementation
	 * 
	 * @param dao The dao implementation to use
	 */
	public void setSentMessageDAO(SentMessageDAO dao);
		
	/**
	*  save a sent message to the database
	*  
	*  @param sentMessage
	*/
	public SentMessage saveSentMessage(SentMessage sentMessage) throws APIException;
		
	/**
	 *  get a saved sent message by id
	 *  
	 *  @param messageId the id of message to get
	 */
	public SentMessage getSentMessage(Integer messageId) throws APIException;
		
	/**
	 * get all messages sent by a particular user
	 * 
	 * @param user
	 */
	public List<SentMessage> getSentMessages(User user) throws APIException;
	
	/**
	 * delete message from db
	 * 
	 * @param sentMessage to delete
	 */
	public void purgeSentMessage(SentMessage sentMessage) throws APIException;
	
	/**
	 * get all sent messages from the db
	 */
	public List<SentMessage> getAllSentMessages() throws APIException; 

	public MessageReceiver saveSentMessageReceiver(MessageReceiver messageReceiver) throws APIException;
}
