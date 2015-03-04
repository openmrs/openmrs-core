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
	public void setMessageSender(MessageSender sender);
	
	public MessageSender getMessageSender();
	
	public void setMessagePreparator(MessagePreparator preparator);
	
	public MessagePreparator getMessagePreparator();
	
	/* Send Message Methods */

	/**
	 * TODO Auto generated method comment
	 * 
	 * @param message
	 * @throws MessageException
	 * @should send message
	 */
	public void sendMessage(Message message) throws MessageException;
	
	//sends message to everyone of a certain role
	public void sendMessage(Message message, String roleName) throws MessageException;
	
	//sends message to user with the given id
	public void sendMessage(Message message, Integer userId) throws MessageException;
	
	//sends message to user
	public void sendMessage(Message message, User user) throws MessageException;
	
	//sends message to all users with a given role
	public void sendMessage(Message message, Role role) throws MessageException;
	
	//sends message to a collection of users
	public void sendMessage(Message message, Collection<User> users) throws MessageException;
	
	public void sendMessage(String recipients, String sender, String subject, String message) throws MessageException;
	
	// Prepare message methods
	public Message createMessage(String subject, String message) throws MessageException;
	
	public Message createMessage(String sender, String subject, String message) throws MessageException;
	
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
	public Message createMessage(String recipients, String sender, String subject, String message) throws MessageException;
	
	public Message createMessage(String recipients, String sender, String subject, String message, String attachment,
	        String attachmentContentType, String attachmentFileName) throws MessageException;
	
	@SuppressWarnings("unchecked")
	public Message prepareMessage(String templateName, Map data) throws MessageException;
	
	public Message prepareMessage(Template template) throws MessageException;
	
	// Template methods
	@SuppressWarnings("unchecked")
	public List getAllTemplates() throws MessageException;
	
	public Template getTemplate(Integer id) throws MessageException;
	
	@SuppressWarnings("unchecked")
	public List getTemplatesByName(String name) throws MessageException;
	
	/* Begin Deprecated methods */

	// Old send message methods
	@Deprecated
	public void send(Message message) throws MessageException;
	
	@Deprecated
	public void send(Message message, String roleName) throws MessageException;
	
	@Deprecated
	public void send(Message message, Integer userId) throws MessageException;
	
	@Deprecated
	public void send(Message message, User user) throws MessageException;
	
	@Deprecated
	public void send(Message message, Role role) throws MessageException;
	
	@Deprecated
	public void send(Message message, Collection<User> users) throws MessageException;
	
	@Deprecated
	public void send(String recipients, String sender, String subject, String message) throws MessageException;
	
	// Old prepare message methods
	@Deprecated
	public Message create(String subject, String message) throws MessageException;
	
	@Deprecated
	public Message create(String sender, String subject, String message) throws MessageException;
	
	@Deprecated
	public Message create(String recipients, String sender, String subject, String message) throws MessageException;
	
	@Deprecated
	@SuppressWarnings("unchecked")
	public Message prepare(String templateName, Map data) throws MessageException;
	
	@Deprecated
	public Message prepare(Template template) throws MessageException;
	
}
