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

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.openmrs.Role;
import org.openmrs.User;

public interface MessageService {
	
	// Set dependencies for message services
	// TODO Should these be required or do we let the implementations constructor dictate the dependencies?
	void setMessageSender(MessageSender sender);
	
	MessageSender getMessageSender();
	
	void setMessagePreparator(MessagePreparator preparator);
	
	MessagePreparator getMessagePreparator();
	
	/* Send Message Methods */

	/**
	 * TODO Auto generated method comment
	 * 
	 * @param message
	 * @throws MessageException
	 * @should send message
	 */
	void sendMessage(Message message) throws MessageException;
	
	//sends message to everyone of a certain role
	void sendMessage(Message message, String roleName) throws MessageException;
	
	//sends message to user with the given id
	void sendMessage(Message message, Integer userId) throws MessageException;
	
	//sends message to user
	void sendMessage(Message message, User user) throws MessageException;
	
	//sends message to all users with a given role
	void sendMessage(Message message, Role role) throws MessageException;
	
	//sends message to a collection of users
	void sendMessage(Message message, Collection<User> users) throws MessageException;
	
	void sendMessage(String recipients, String sender, String subject, String message) throws MessageException;
	
	// Prepare message methods
	Message createMessage(String subject, String message) throws MessageException;
	
	Message createMessage(String sender, String subject, String message) throws MessageException;
	
	/**
	 * TODO Auto generated method comment
	 * 
	 * @param recipients
	 * @param sender
	 * @param subject
	 * @param message
	 * @return Message the message that was created
	 * @throws MessageException
	 * @should create message
	 */
	Message createMessage(String recipients, String sender, String subject, String message) throws MessageException;
	
	Message createMessage(String recipients, String sender, String subject, String message, String attachment,
			String attachmentContentType, String attachmentFileName) throws MessageException;
	
	Message prepareMessage(String templateName, Map data) throws MessageException;
	
	Message prepareMessage(Template template) throws MessageException;
	
	// Template methods
	List getAllTemplates() throws MessageException;
	
	Template getTemplate(Integer id) throws MessageException;
	
	List getTemplatesByName(String name) throws MessageException;
}
