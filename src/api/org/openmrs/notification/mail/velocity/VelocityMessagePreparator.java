package org.openmrs.notification.mail.velocity;

import java.io.StringWriter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.openmrs.notification.Message;
import org.openmrs.notification.MessageException;
import org.openmrs.notification.MessagePreparator;
import org.openmrs.notification.Template;

public class VelocityMessagePreparator implements MessagePreparator { 

	/**
	 * Logger
	 */
	private static final Log log = LogFactory.getLog(VelocityMessagePreparator.class);    
	
	/**
	 * Velocity template engine
	 */
	private VelocityEngine engine;
	
	/** 
	 * Public constructor 
	 * TODO: needs better error handling
	 * @throws MessageException
	 */
	public VelocityMessagePreparator() throws MessageException { 
		try { 
			engine = new VelocityEngine();
			engine.init();
		} catch (Exception e) { 
			log.error("Failed to create velocity engine " + e.getMessage(), e);
			throw new MessageException(e);
		}
	}
	
	
	// TODO: need better error handling
	public Message prepare(Template template) throws MessageException { 

		VelocityContext context = new VelocityContext( template.getData() );		
		StringWriter writer = new StringWriter();	
		
		try { 
			engine.evaluate(
				context, 
				writer, 
				"template",    				// I have no idea what this is used for
				template.getTemplate()
			);
		} catch (Exception e) { 
			// need better error handling
			log.error("Failed to prepare message using template " + e.getMessage(), e);
			throw new MessageException(e);
		}
		
		// Prepare the message
		Message message = new Message();		
		message.setSubject( template.getSubject() );
		message.setRecipients( template.getRecipients() );
		message.setSender( template.getSender() );
		message.setContent( writer.toString() );
		
		return message;
	}
}
	