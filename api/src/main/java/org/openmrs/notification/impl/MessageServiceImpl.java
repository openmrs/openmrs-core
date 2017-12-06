/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.notification.impl;

import java.util.Collection;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public class MessageServiceImpl implements MessageService {
	
	private static final Logger log = LoggerFactory.getLogger(MessageServiceImpl.class);
	
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
	@Override
	public void setMessagePreparator(MessagePreparator messagePreparator) {
		this.messagePreparator = messagePreparator;
	}
	
	@Override
	public MessagePreparator getMessagePreparator() {
		return this.messagePreparator;
	}
	
	/**
	 * Set the message sender.
	 *
	 * @param messageSender
	 */
	@Override
	public void setMessageSender(MessageSender messageSender) {
		this.messageSender = messageSender;
	}
	
	@Override
	public MessageSender getMessageSender() {
		return this.messageSender;
	}
	
	/**
	 * Send the message. All send calls go through this method.
	 *
	 * @param message the Message to be sent
	 * @see org.openmrs.notification.MessageService#sendMessage(org.openmrs.notification.Message)
	 */
	@Override
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
	@Override
	public Message createMessage(String recipients, String sender, String subject, String content) throws MessageException {
		return Context.getMessageService().createMessage(recipients, sender, subject, content, null, null, null);
	}
	
	/**
	 * Create a message object with the given parts.
	 *
	 * @param sender the send of the message
	 * @param subject the subject of the message
	 * @param content the content or body of the message
	 */
	@Override
	public Message createMessage(String sender, String subject, String content) throws MessageException {
		return Context.getMessageService().createMessage(null, sender, subject, content);
	}
	
	/**
	 * Create a message object with the given parts.
	 *
	 * @param subject the subject of the message
	 * @param content the content or body of the message
	 */
	@Override
	public Message createMessage(String subject, String content) throws MessageException {
		return Context.getMessageService().createMessage(null, null, subject, content);
	}
	
	/**
	 * @see org.openmrs.notification.MessageService#createMessage(java.lang.String,
	 *      java.lang.String, java.lang.String, java.lang.String, java.lang.String,
	 *      java.lang.String, java.lang.String)
	 */
	@Override
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
	@Override
	public void sendMessage(String recipients, String sender, String subject, String content) throws MessageException {
		Message message = createMessage(recipients, sender, subject, content);
		Context.getMessageService().sendMessage(message);
	}
	
	/**
	 * Send a message to a user that is identified by the given identifier.
	 *
	 * @param message <code>Message</code> to be sent
	 * @param recipientId Integer identifier of user (recipient)
	 */
	@Override
	public void sendMessage(Message message, Integer recipientId) throws MessageException {
		log.debug("Sending message to user with user id " + recipientId);
		User user = Context.getUserService().getUser(recipientId);
		message.addRecipient(user.getUserProperty(OpenmrsConstants.USER_PROPERTY_NOTIFICATION_ADDRESS));
		Context.getMessageService().sendMessage(message);
	}
	
	/**
	 * Send message to a single user.
	 *
	 * @param message the <code>Message</code> to be sent
	 * @param user the recipient of the message
	 */
	@Override
	public void sendMessage(Message message, User user) throws MessageException {
		log.debug("Sending message to user " + user);
		String address = user.getUserProperty(OpenmrsConstants.USER_PROPERTY_NOTIFICATION_ADDRESS);
		if (address != null) {
			message.addRecipient(address);
		}
		Context.getMessageService().sendMessage(message);
	}
	
	/**
	 * Send message to a collection of recipients.
	 */
	@Override
	public void sendMessage(Message message, Collection<User> users) throws MessageException {
		log.debug("Sending message to users " + users);
		for (User user : users) {
			String address = user.getUserProperty(OpenmrsConstants.USER_PROPERTY_NOTIFICATION_ADDRESS);
			if (address != null) {
				message.addRecipient(address);
			}
		}
		Context.getMessageService().sendMessage(message);
	}
	
	/**
	 * Send a message to a group of users identified by their role.
	 */
	@Override
	public void sendMessage(Message message, String roleName) throws MessageException {
		log.debug("Sending message to role with name " + roleName);
		Role role = Context.getUserService().getRole(roleName);
		Context.getMessageService().sendMessage(message, role);
	}
	
	/**
         * Sends a message to a group of users identifier by their role.
	 */
	@Override
	public void sendMessage(Message message, Role role) throws MessageException {
		log.debug("Sending message to role " + role);
		log.debug("User Service : " + Context.getUserService());
		
		List<Role> roles = new ArrayList<>();
		roles.add(role);
		
		Collection<User> users = Context.getUserService().getUsers(null, roles, false);
		
		log.debug("Sending message " + message + " to " + users);
		Context.getMessageService().sendMessage(message, users);
	}
	
	/**
	 * Prepare a message given the template. The template should be populated with all necessary
	 * data including the variable name-value pairs
	 *
	 * @param template the given <code>Template</code>
	 * @return the prepared <code>Message</code>
	 */
	@Override
	public Message prepareMessage(Template template) throws MessageException {
		return messagePreparator.prepare(template);
	}
	
	/**
	 * Prepare a message based on a template and data used for variable substitution within template.
	 *
	 * @param templateName name of the template to be used
	 * @param data mapping used for variable substitution within template
	 * @return the prepared Message
	 */
	@Override
	@Transactional(readOnly = true)
	public Message prepareMessage(String templateName, Map data) throws MessageException {
		try {
			Template template = (Template) getTemplatesByName(templateName).get(0);
			template.setData(data);
			return Context.getMessageService().prepareMessage(template);
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
	@Override
	@Transactional(readOnly = true)
	public List getAllTemplates() throws MessageException {
		return templateDAO.getTemplates();
	}
	
	/**
	 * Get template by identifier.
	 *
	 * @param id template identifier
	 * @return Template
	 */
	@Override
	@Transactional(readOnly = true)
	public Template getTemplate(Integer id) throws MessageException {
		return templateDAO.getTemplate(id);
	}
	
	/**
	 * Get templates by name.
	 *
	 * @param name the name of the template
	 * @return list of Templates
	 */
	@Override
	@Transactional(readOnly = true)
	public List getTemplatesByName(String name) throws MessageException {
		return templateDAO.getTemplatesByName(name);
	}	
}
