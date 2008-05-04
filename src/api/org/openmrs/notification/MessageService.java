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
	
	
	// Send message methods
	public void send(Message message) throws MessageException;
	public void send(Message message, String roleName) throws MessageException;	
	public void send(Message message, Integer userId) throws MessageException;
	public void send(Message message, User user) throws MessageException;	
	public void send(Message message, Role role) throws MessageException;	
	public void send(Message message, Collection<User> users) throws MessageException;

	// 
	public void send(String recipients, String sender, String subject, String message) throws MessageException;

	// Prepare message methods
	public Message create(String subject, String message) throws MessageException;
	public Message create(String sender, String subject, String message) throws MessageException;
	public Message create(String recipients, String sender, String subject, String message) throws MessageException;
	public Message prepare(String templateName, Map data) throws MessageException;
	public Message prepare(Template template) throws MessageException;

	// Template methods
	public List getAllTemplates() throws MessageException;
	public Template getTemplate(Integer id) throws MessageException;
	public List getTemplatesByName(String name) throws MessageException;

}