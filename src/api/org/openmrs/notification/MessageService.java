package org.openmrs.notification;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.openmrs.Role;
import org.openmrs.User;
import org.openmrs.synchronization.engine.SyncRecord;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface MessageService { 
	
	// Set dependencies for message services
	// TODO Should these be required or do we let the implementations constructor dictate the dependencies?
	public void setMessageSender(MessageSender sender);
	public MessageSender getMessageSender();
	public void setMessagePreparator(MessagePreparator preparator);
	public MessagePreparator getMessagePreparator();
	
	
	// Send messages
	public void sendMessage(Message message) throws MessageException;
	public void sendMessage(Message message, String roleName) throws MessageException;	
	public void sendMessage(Message message, Integer userId) throws MessageException;
	public void sendMessage(Message message, User user) throws MessageException;	
	public void sendMessage(Message message, Role role) throws MessageException;	
	public void sendMessage(Message message, Collection<User> users) throws MessageException;
	public void sendMessage(String recipients, String sender, String subject, String message) throws MessageException;

	// Prepare message methods
	public Message createMessage(String subject, String message) throws MessageException;
	public Message createMessage(String sender, String subject, String message) throws MessageException;
	public Message createMessage(String recipients, String sender, String subject, String message) throws MessageException;
	public Message prepareMessage(String templateName, Map data) throws MessageException;
	public Message prepareMessage(Template template) throws MessageException;

	// Template methods
	public List getAllTemplates() throws MessageException;
	public Template getTemplate(Integer id) throws MessageException;
	public List getTemplatesByName(String name) throws MessageException;

}