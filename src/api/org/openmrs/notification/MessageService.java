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
package org.openmrs.notification;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.openmrs.Role;
import org.openmrs.User;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface MessageService {
	
	// Set dependencies for message services
	// TODO Should these be required or do we let the implementations constructor dictate the dependencies?
	public void setMessageSender(MessageSender sender);
	
	public MessageSender getMessageSender();
	
	public void setMessagePreparator(MessagePreparator preparator);
	
	public MessagePreparator getMessagePreparator();
	
	/* Send Message Methods */

	//sends message
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
