package org.openmrs.notification;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.openmrs.Role;
import org.openmrs.User;

public interface MessageService { 
	
	// Set depenedencies for message services
	// TODO Should these be required or do we let the implementations constructor dictate the dependencies?
	public void setMessageSender(MessageSender sender);
	public void setMessagePreparator(MessagePreparator preparator);
	
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