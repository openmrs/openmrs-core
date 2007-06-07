package org.openmrs.notification;


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