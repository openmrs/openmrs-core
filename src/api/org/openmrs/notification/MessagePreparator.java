package org.openmrs.notification;

import java.util.Map;
import org.openmrs.notification.Message;
import org.openmrs.notification.Template;

/**
 * Interface that defines the message preparator's functionality.
 * 
 * @author Justin Miranda
 */
public interface MessagePreparator {
	
	/**
	 * Prepare a message using a template.
	 * 
	 * @param template
	 * @return
	 */
	public Message prepare(Template template) throws MessageException;

} 