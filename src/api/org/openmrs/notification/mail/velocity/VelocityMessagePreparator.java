package org.openmrs.notification.mail.velocity;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.VelocityContext;
import java.io.StringWriter;
import java.util.Map;
import org.openmrs.domain.Template;
import org.openmrs.notification.MessagePreparator;
import org.openmrs.domain.Message;

public class VelocityMessagePreparator implements MessagePreparator { 

	
	private static final Log log = LogFactory.getLog(VelocityMessagePreparator.class);    
	
	private VelocityEngine engine;
	
	// TODO: need better error handling
	public VelocityMessagePreparator() throws Exception { 
		engine = new VelocityEngine();
		engine.init();
	}

	
	// TODO: need better error handling
	public Message prepare(Template template) { 

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
			e.printStackTrace();

		}
		
		// Set the subject, recipients, sender, and content from the template object
		Message message = new Message();		
		message.setSubject( template.getSubject() );
		message.setRecipients( template.getRecipients() );
		message.setSender( template.getSender() );
		message.setContent( writer.toString() );
		
		return message;
	}
}
	