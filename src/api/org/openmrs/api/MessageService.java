package org.openmrs.api;

import java.util.List;
import java.util.Map;

import org.openmrs.api.db.DAOContext;
import org.openmrs.domain.Message;
import org.openmrs.domain.Template;
import org.openmrs.notification.MessagePreparator;
import org.openmrs.notification.MessageSender;


public interface MessageService { 

	// Methods used for dependency injection
	//public void setDAOContext(DAOContext daoContext);
	//public void setMessagePreparator(MessagePreparator messagePreparator);
	//public void setMessageSender(MessageSender messageSender);
	
	// Send message methods
	public void send(Message message) throws Exception;
	public void send(String recipients, String sender, String subject, String message) throws Exception;
	//public void send(String templateName, Map data) throws Exception;

	// Prepare message methods
	public Message prepare(String templateName, Map data) throws Exception;
	public Message prepare(Template template) throws Exception;

	// Template methods
	public List getAllTemplates( ) throws Exception;
	public Template getTemplate( Integer id ) throws Exception;
	public List getTemplatesByName( String name ) throws Exception;

}