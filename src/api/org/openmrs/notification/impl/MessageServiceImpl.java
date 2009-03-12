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
package org.openmrs.notification.impl;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Role;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.TemplateDAO;
import org.openmrs.notification.Message;
import org.openmrs.notification.MessageException;
import org.openmrs.notification.MessagePreparator;
import org.openmrs.notification.MessageSender;
import org.openmrs.notification.MessageService;
import org.openmrs.notification.Template;
import org.openmrs.util.OpenmrsConstants;

public class MessageServiceImpl implements MessageService {
	
	private static final Log log = LogFactory.getLog(MessageServiceImpl.class);
	
	private TemplateDAO templateDAO;
	
	private MessageSender messageSender; // Delivers message 
	
	private MessagePreparator messagePreparator; // Prepares message for delivery 
	
	public void setTemplateDAO(TemplateDAO dao) {
		this.templateDAO = dao;
	}
	
	/**
	 * Public constructor Required for use with spring's method injection. Be careful because this
	 * class requires a DAO Context in order to work properly. Please set the DAO context
	 */
	public MessageServiceImpl() {
	}
	
	/**
	 * Set the message preparator.
	 * 
	 * @param messagePreparator
	 */
	public void setMessagePreparator(MessagePreparator messagePreparator) {
		this.messagePreparator = messagePreparator;
	}
	
	public MessagePreparator getMessagePreparator() {
		return this.messagePreparator;
	}
	
	/**
	 * Set the message sender.
	 * 
	 * @param messageSender
	 */
	public void setMessageSender(MessageSender messageSender) {
		this.messageSender = messageSender;
	}
	
	public MessageSender getMessageSender() {
		return this.messageSender;
	}
	
	/**
	 * Send the message. All send calls go through this method.
	 * 
	 * @param message the Message to be sent
	 * @see org.openmrs.notification.MessageService#sendMessage(org.openmrs.notification.Message)
	 */
	public void sendMessage(Message message) throws MessageException {
		try {
			messageSender.send(message);
		}
		catch (Exception e) {
			log.error("Message could not be sent due to " + e.getMessage(), e);
			throw new MessageException(e);
		}
	}
	
	/**
	 * Create a message object with the given parts.
	 * 
	 * @param recipients the recipients of the message
	 * @param sender the send of the message
	 * @param subject the subject of the message
	 * @param content the content or body of the message
	 */
	public Message createMessage(String recipients, String sender, String subject, String content) throws MessageException {
		return createMessage(recipients, sender, subject, content, null, null, null);
	}
	
	/**
	 * Create a message object with the given parts.
	 * 
	 * @param sender the send of the message
	 * @param subject the subject of the message
	 * @param content the content or body of the message
	 */
	public Message createMessage(String sender, String subject, String content) throws MessageException {
		return createMessage(null, sender, subject, content);
	}
	
	/**
	 * Create a message object with the given parts.
	 * 
	 * @param sender the send of the message
	 * @param subject the subject of the message
	 * @param content the content or body of the message
	 */
	public Message createMessage(String subject, String content) throws MessageException {
		return createMessage(null, null, subject, content);
	}
	
	/**
	 * @see org.openmrs.notification.MessageService#createMessage(java.lang.String,
	 *      java.lang.String, java.lang.String, java.lang.String, java.lang.String,
	 *      java.lang.String, java.lang.String)
	 */
	public Message createMessage(String recipients, String sender, String subject, String content, String attachment,
	                             String attachmentContentType, String attachmentFileName) throws MessageException {
		Message message = new Message();
		message.setRecipients(recipients);
		message.setSender(sender);
		message.setContent(content);
		message.setSubject(subject);
		message.setAttachment(attachment);
		message.setAttachmentContentType(attachmentContentType);
		message.setAttachmentFileName(attachmentFileName);
		return message;
	}
	
	/**
	 * Send a message using the given parameters. This is a convenience method so that the client
	 * does not need to create its own Message object.
	 */
	public void sendMessage(String recipients, String sender, String subject, String content) throws MessageException {
		Message message = createMessage(recipients, sender, subject, content);
		sendMessage(message);
	}
	
	/**
	 * Send a message to a user that is identified by the given identifier.
	 * 
	 * @param message message to be sent
	 * @param userId identifier of user (recipient)
	 */
	public void sendMessage(Message message, Integer recipientId) throws MessageException {
		log.debug("Sending message to user with user id " + recipientId);
		User user = Context.getUserService().getUser(recipientId);
		message.addRecipient(user.getUserProperty(OpenmrsConstants.USER_PROPERTY_NOTIFICATION_ADDRESS));
		// message.setFormat( user( OpenmrsConstants.USER_PROPERTY_NOTIFICATION_FORMAT ) );
		sendMessage(message);
	}
	
	/**
	 * Send message to a single user.
	 * 
	 * @param message the message to be sent
	 * @param recipient the recipient of the message
	 */
	public void sendMessage(Message message, User user) throws MessageException {
		log.debug("Sending message to user " + user);
		String address = user.getUserProperty(OpenmrsConstants.USER_PROPERTY_NOTIFICATION_ADDRESS);
		if (address != null)
			message.addRecipient(address);
		// message.setFormat( user.getProperty( OpenmrsConstants.USER_PROPERTY_NOTIFICATION_FORMAT ) );
		sendMessage(message);
	}
	
	/**
	 * Send message to a collection of recipients.
	 */
	public void sendMessage(Message message, Collection<User> users) throws MessageException {
		log.debug("Sending message to users " + users);
		for (User user : users) {
			String address = user.getUserProperty(OpenmrsConstants.USER_PROPERTY_NOTIFICATION_ADDRESS);
			if (address != null)
				message.addRecipient(address);
		}
		sendMessage(message);
	}
	
	/**
	 * Send a message to a group of users identified by their role.
	 */
	public void sendMessage(Message message, String roleName) throws MessageException {
		log.debug("Sending message to role with name " + roleName);
		Role role = Context.getUserService().getRole(roleName);
		sendMessage(message, role);
	}
	
	/**
	 * Sends a message to a group of users identifier by thir role.
	 */
	public void sendMessage(Message message, Role role) throws MessageException {
		log.debug("Sending message to role " + role);
		log.debug("User Service : " + Context.getUserService());
		
		List<Role> roles = new Vector<Role>();
		roles.add(role);
		
		Collection<User> users = Context.getUserService().getUsers(null, roles, false);
		
		log.debug("Sending message " + message + " to " + users);
		sendMessage(message, users);
	}
	
	/**
	 * Prepare a message given the template. The template should be populated with all necessary
	 * data including the variable name-value pairs
	 * 
	 * @param the given Template
	 * @return the prepared Message
	 */
	public Message prepareMessage(Template template) throws MessageException {
		return messagePreparator.prepare(template);
	}
	
	/**
	 * Prepare a message based on a template and data used for variable subsitution within template.
	 * 
	 * @param templateName name of the template to be used
	 * @param data data mapping used for variable substitution within template
	 * @return the prepared Message
	 */
	@SuppressWarnings("unchecked")
	public Message prepareMessage(String templateName, Map data) throws MessageException {
		try {
			Template template = (Template) getTemplatesByName(templateName).get(0);
			template.setData(data);
			return prepareMessage(template);
		}
		catch (Exception e) {
			throw new MessageException("Could not prepare message with template " + templateName, e);
		}
	}
	
	/**
	 * Get all templates in the database.
	 * 
	 * @return list of Templates
	 */
	@SuppressWarnings("unchecked")
	public List getAllTemplates() throws MessageException {
		return templateDAO.getTemplates();
	}
	
	/**
	 * Get template by identifier.
	 * 
	 * @param id template identifier
	 * @return Template
	 */
	public Template getTemplate(Integer id) throws MessageException {
		return templateDAO.getTemplate(id);
	}
	
	/**
	 * Get templates by name.
	 * 
	 * @param name the name of the template
	 * @return list of Templates
	 */
	@SuppressWarnings("unchecked")
	public List getTemplatesByName(String name) throws MessageException {
		return templateDAO.getTemplatesByName(name);
	}
	
	/**
	 * @see org.openmrs.notification.MessageService#create(java.lang.String, java.lang.String)
	 * @deprecated
	 */
	public Message create(String subject, String message) throws MessageException {
		return createMessage(subject, message);
	}
	
	/**
	 * @see org.openmrs.notification.MessageService#create(java.lang.String, java.lang.String,
	 *      java.lang.String)
	 * @deprecated
	 */
	public Message create(String sender, String subject, String message) throws MessageException {
		return createMessage(sender, subject, message);
	}
	
	/**
	 * @see org.openmrs.notification.MessageService#create(java.lang.String, java.lang.String,
	 *      java.lang.String, java.lang.String)
	 * @deprecated
	 */
	public Message create(String recipients, String sender, String subject, String message) throws MessageException {
		return createMessage(recipients, sender, subject, message);
	}
	
	/**
	 * @see org.openmrs.notification.MessageService#prepare(java.lang.String, java.util.Map)
	 * @deprecated
	 */
	@SuppressWarnings("unchecked")
	public Message prepare(String templateName, Map data) throws MessageException {
		return prepareMessage(templateName, data);
	}
	
	/**
	 * @see org.openmrs.notification.MessageService#prepare(org.openmrs.notification.Template)
	 * @deprecated
	 */
	public Message prepare(Template template) throws MessageException {
		return prepareMessage(template);
	}
	
	/**
	 * @see org.openmrs.notification.MessageService#send(org.openmrs.notification.Message)
	 * @deprecated use {@link #sendMessage(Message)}
	 */
	public void send(Message message) throws MessageException {
		sendMessage(message);
	}
	
	/**
	 * @see org.openmrs.notification.MessageService#send(org.openmrs.notification.Message,
	 *      java.lang.String)
	 * @deprecated use {@link #sendMessage(Message, String)}
	 */
	public void send(Message message, String roleName) throws MessageException {
		sendMessage(message, roleName);
	}
	
	/**
	 * @see org.openmrs.notification.MessageService#send(org.openmrs.notification.Message,
	 *      java.lang.Integer)
	 * @deprecated use {@link #sendMessage(Message, Integer)}
	 */
	public void send(Message message, Integer userId) throws MessageException {
		sendMessage(message, userId);
	}
	
	/**
	 * @see org.openmrs.notification.MessageService#send(org.openmrs.notification.Message,
	 *      org.openmrs.User)
	 * @deprecated use {@link #sendMessage(Message, User)}
	 */
	public void send(Message message, User user) throws MessageException {
		sendMessage(message, user);
	}
	
	/**
	 * @see org.openmrs.notification.MessageService#send(org.openmrs.notification.Message,
	 *      org.openmrs.Role)
	 * @deprecated {@link #send(Message, Role)}
	 */
	public void send(Message message, Role role) throws MessageException {
		sendMessage(message, role);
	}
	
	/**
	 * @see org.openmrs.notification.MessageService#send(org.openmrs.notification.Message,
	 *      java.util.Collection)
	 * @deprecated {@link #send(Message, Collection)}
	 */
	public void send(Message message, Collection<User> users) throws MessageException {
		sendMessage(message, users);
	}
	
	/**
	 * @see org.openmrs.notification.MessageService#send(java.lang.String, java.lang.String,
	 *      java.lang.String, java.lang.String)
	 * @deprecated use {@link #send(String, String, String, String)}
	 */
	public void send(String recipients, String sender, String subject, String message) throws MessageException {
		sendMessage(recipients, sender, subject, message);
	}
	
}
