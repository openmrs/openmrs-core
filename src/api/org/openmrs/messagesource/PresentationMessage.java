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
package org.openmrs.messagesource;

import java.util.Locale;

/**
 * A PresentationMessage is a textual message presented to a user along with qualifying
 * information like the code, locale and a description.
 *
 * ABKTODO: This is really part of the presentation layer, and should be part of a separate 
 * model package rather than included with the domain model. Possibly move all this to
 * its own package? Or into org.openmrs.api.context?
 *  
 */
public class PresentationMessage {

	private String code;
	private Locale locale;
	private String message;
	private String description;

	/**
	 * Create a new, fully specified PresentationMessage.
	 * 
	 * @param code the look-up key for the message
	 * @param locale locale within which the message is expressed
	 * @param message text of the message
	 * @param description description of the meaning or intended use of the message
	 */
	public PresentationMessage(String code, Locale locale, String message, String description) {
		this.code = code;
		this.locale = locale;
		this.message = message;
		this.description = description;
	}
	
	/**
	 * Returns the look-up key for the message. This is the programmatic
	 * reference for the message, typically used when the message is 
	 * retrieved from a {@link org.springframework.context.MessageSource}.
	 * 
     * @return the code
     */
    public String getCode() {
    	return code;
    }
    
	/**
	 * Sets the look-up key for the message. 
	 * 
     * @param code the code to set
     */
    public void setCode(String code) {
    	this.code = code;
    }
    
	/**
	 * Returns the locale within which the message is expressed.
	 * 
     * @return the locale
     */
    public Locale getLocale() {
    	return locale;
    }
    
	/**
	 * Sets the locale of the message.
	 * 
     * @param locale the locale to set
     */
    public void setLocale(Locale locale) {
    	this.locale = locale;
    }
    
	/**
	 * Returns the text of the message. This is what should be
	 * presented to the user. 
	 * 
     * @return the message
     */
    public String getMessage() {
    	return message;
    }
	/**
     * @param message the message to set
     */
    public void setMessage(String message) {
    	this.message = message;
    }
	/**
	 * Gets the description, which should indicate the meaning or 
	 * intended use of the message.
	 * 
     * @return the description
     */
    public String getDescription() {
    	return description;
    }
	/**
     * @param description the description to set
     */
    public void setDescription(String description) {
    	this.description = description;
    }
	
}
