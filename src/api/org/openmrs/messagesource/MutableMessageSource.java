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

import java.util.Collection;
import java.util.Locale;
import java.util.Properties;

import org.springframework.context.HierarchicalMessageSource;
import org.springframework.context.MessageSource;
import org.springframework.transaction.annotation.Transactional;

/**
 * Extended MessageSource interface, which provides more information
 * about the available messages and can be changed.
 *  
 */
@Transactional
public interface MutableMessageSource extends MessageSource, HierarchicalMessageSource {
	

	/**
	 * Gets the locales for which messages are available from this source.
	 * 
	 * @return available message locales
	 */
	public Collection<Locale> getLocales();

	/**
	 * Makes a collection of properties available as messages.
	 * 
     * @param props key/value properties for the messages
     * @param locale locale in which the messages are expressed
     * @param namespace namespace within which the properties are valid ("" for generic, "module" for modules, etc)
     * @param name unique name for the properties within the namespace
     * @param version version of the properties
     * @deprecated use {@linkplain #merge(MutableMessageSource, boolean)}
     */
    public void publishProperties(Properties props, String locale, String namespace, 
    		String name, String version);
    
    /**
     * Gets all of the available messages, packaged as PresentationMessages.
     * 
     * @return collection of presentation messages
     */
    public Collection<PresentationMessage> getPresentations();
    
    /**
     * Gets alll the available messages in a particular locale, packaged as PresentationMessages.
     * 
     * @param locale locale for which to get the messages
     * @return collection of PresentationMessages in the locale
     */
    public Collection<PresentationMessage> getPresentationsInLocale(Locale locale);
	
    /**
     * Adds a presentation message to the source. This operation should 
     * overwrite any existing message which conflicts (has the same code 
     * and locale).  
     * 
     * @param message message to add to the source
     */
    public void addPresentation(PresentationMessage message);
    
    /**
     * Gets the PresentationMessage for a particular locale.
     * 
     * @param key textual key for the message
     * @param forLocale locale for which to get the message
     * @return corresponding PresentationMessage, or null if not available
     */
    public PresentationMessage getPresentation(String key, Locale forLocale);
    
    /**
     * Removes a presentation message from the source. 
     * 
     * @param message the message to remove
     */
    public void removePresentation(PresentationMessage message);
    
    /**
     * Merge messages from another source into this source.
     * 
     * @param fromSource message source from which messages should be merge
     * @param whether to overwrite existing messages
     */
    public void merge(MutableMessageSource fromSource, boolean overwrite);

}
