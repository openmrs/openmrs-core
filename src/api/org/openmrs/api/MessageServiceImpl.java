package org.openmrs.api;

import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.domain.Message;
import org.openmrs.domain.Template;
import org.openmrs.notification.MessageSender;
import org.openmrs.notification.MessagePreparator;

import org.openmrs.api.context.Context;
import org.openmrs.api.db.DAOContext;
import java.util.Date;

public class MessageServiceImpl implements MessageService { 
	
	
	private static final Log log = LogFactory.getLog( MessageServiceImpl.class );
	
	private Context context;
	private MessageSender messageSender;
	private MessagePreparator messagePreparator;
	
	
	/**
	 *  Public constructor
	 */
	public MessageServiceImpl() { }	
	
	/**
	 *  Set the context.
	 *  
	 *  @param context
	 */
	public void setContext(Context context) { 
		this.context = context;
	}
		
	/**
	 *  Set the message preparator.
	 *  
	 *  @param messagePreparator
	 */
	public void setMessagePreparator(MessagePreparator messagePreparator) { 
		this.messagePreparator = messagePreparator;
	}

	/**
	 *  Set the message sender.
	 *  
	 *  @param messageSender
	 */
	public void setMessageSender(MessageSender messageSender) { 
		this.messageSender = messageSender;
	}

	
	/**
	 *  Send the message.
	 *  
	 *  @param  message  the Message to be sent
	 */
	public void send(Message message) throws Exception {
		messageSender.send( message );
	}

	/**
	 *  Send a message using the given parameters.  This is a convenience method so that the client
	 *  does not need to create its own Message object. 
	 */
	public void send(String recipients, String sender, String subject, String content) throws Exception { 
		
		// Build comma delimited list of recipients 
		// TODO: Add logic to prevent extra comma at the end
		/*
		StringBuffer buffer = new StringBuffer();
		for ( String recipient : recipients ) { buffer.append( recipient ).append(","); }*/

		Message message = new Message();
		message.setSender( sender );
		message.setRecipients( recipients );
		message.setContent( content );
		message.setSubject( subject );
		message.setSentDate( new Date() );
		
		messageSender.send( message );
	}
	
	/**
	 *	Prepare a message given the template.  The template should be populated with 
	 *  all necessary data including the variable name-value pairs
	 *  
	 *  @param the given Template
	 *  @return the prepared Message
	 */
	public Message prepare(Template template) throws Exception { 
		return messagePreparator.prepare( template );
	}
	
	
	/**
	 *  Prepare a message based on a template and data used for variable subsitution within template.
	 *  
	 *  @param	templateName	name of the template to be used
	 *  @param	data	data mapping used for variable substitution within template
	 *  @return	the prepared Message
	 */
	public Message prepare(String templateName, Map data) throws Exception { 
		Template template = new Template();
		List templates = getTemplatesByName( templateName );
		if ( templates != null && !templates.isEmpty() ) { 
			template = (Template) templates.get(0);
		} else { 
			throw new Exception("template " + templateName + " not found");
		}
		template.setData( data );
		return prepare( template );
	}
	

	/**
	 *  Get all templates in the database.
	 *  
	 *  @return  list of Templates
	 */
	public List getAllTemplates( ) throws Exception { 
		return context.getDaoContext().getTemplateDAO().getTemplates();
	}
	
	/**
	 *  Get template by identifier.
	 *  
	 *  @param	id	template identifier
	 *  @return	Template 	
	 */
	public Template getTemplate( Integer id ) throws Exception { 
		return context.getDaoContext().getTemplateDAO().getTemplate( id );
	}
	
	/**
	 *  Get templates by name.
	 *  
	 *  @param  name	the name of the template
	 *  @return  list of Templates
	 */
	public List getTemplatesByName( String name ) throws Exception { 
		return context.getDaoContext().getTemplateDAO().getTemplatesByName( name );
	}

}