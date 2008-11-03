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
package org.openmrs.messagesource.impl;

import java.util.Collection;
import java.util.HashSet;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.messagesource.MessageSourceService;
import org.openmrs.messagesource.MutableMessageSource;
import org.openmrs.messagesource.PresentationMessage;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.NoSuchMessageException;

/**
 * Extensible implementation of the MessageSourceService, which relies on
 * injected implementations of MutableMessageSource to actually provide the 
 * services. 
 * 
 * The sub-services are loaded by special BeanPostProcessor handling, which
 * looks for beans named 
 * 
 * For example: ResourceBundleMessageSourceService can be specified in
 * the applicationContext-service.xml file to use the usual .properties
 * files to provide messages. 
 * 
 */
public class MessageSourceServiceImpl implements MessageSourceService {
		
	private static Log log = LogFactory.getLog(MessageSourceServiceImpl.class);
	
	private Set<MutableMessageSource> availableMessageSources = new HashSet<MutableMessageSource>();
	
	private MutableMessageSource activeMessageSource;
		
	/**
	 * @see org.openmrs.messagesource.MessageSourceService#getMessage(java.lang.String)
	 */
	public String getMessage(String s) {
		return getMessage(s, null, Context.getLocale());
	}
	
	/**
	 * Gets the message source service which is currently providing
	 * services.
	 * 
     * @return the activeMessageSource
     */
    public MutableMessageSource getActiveMessageSource() {
    	return activeMessageSource;
    }

	/**
	 * Sets the message source service which will actually provide services. 
	 * 
     * @param activeMessageSource the activeMessageSourceService to set
     */
    public void setActiveMessageSource(
            MutableMessageSource activeMessageSource) {
    	
    	log.debug("Setting activeMessageSource: " + activeMessageSource);
    	
    	this.activeMessageSource = activeMessageSource;
    	if (!availableMessageSources.contains(activeMessageSource))
    	{
    		availableMessageSources.add(activeMessageSource);
    	}
    }

	/**
	 * Gets the locales which are available from the current message source. 
	 * 
	 * @see org.openmrs.message.MessageSourceService#getLocalesOfConceptNames()
	 */
	public Collection<Locale> getLocales() {
		return activeMessageSource.getLocales();
	}
	
	/**
	 * Presumes to append the messages to a message.properties file which is
	 * already being monitored by the super
	 * ReloadableResourceBundleMessageSource.
	 * 
	 * This is a blind, trusting hack.
	 * 
	 * @see org.openmrs.message.MessageSourceService#publishProperties(java.util.Properties,
	 *      java.lang.String, java.lang.String, java.lang.String)
	 * @deprecated use {@link #merge(MutableMessageSource, boolean)} instead
	 */
	public void publishProperties(Properties props, String locale,
	        String namespace, String name, String version) {
		activeMessageSource.publishProperties(props, locale, namespace, name, version);
	}

	/**
	 * Returns all available messages.
	 * 
     * @see org.openmrs.message.MessageSourceService#getPresentations()
     */
    public Collection<PresentationMessage> getPresentations() {
    	return activeMessageSource.getPresentations();
    }

	/**
     * @see org.springframework.context.MessageSource#getMessage(org.springframework.context.MessageSourceResolvable, java.util.Locale)
     */
    public String getMessage(MessageSourceResolvable arg0, Locale arg1) {
    	return activeMessageSource.getMessage(arg0, arg1);
    }

	/**
     * @see org.springframework.context.MessageSource#getMessage(java.lang.String, java.lang.Object[], java.util.Locale)
     */
    public String getMessage(String arg0, Object[] arg1, Locale arg2)
            throws NoSuchMessageException {
    	return activeMessageSource.getMessage(arg0, arg1, arg2);
    }

	/**
     * @see org.springframework.context.MessageSource#getMessage(java.lang.String, java.lang.Object[], java.lang.String, java.util.Locale)
     */
    public String getMessage(String arg0, Object[] arg1, String arg2,
            Locale arg3) {
    	return activeMessageSource.getMessage(arg0, arg1, arg2, arg3);
    }


	/**
     * @see org.openmrs.message.MutableMessageSource#addPresentation(org.openmrs.messagesource.PresentationMessage)
     */
    public void addPresentation(PresentationMessage message) {
    	activeMessageSource.addPresentation(message);
    }

	/**
     * @see org.openmrs.message.MutableMessageSource#addPresentation(org.openmrs.messagesource.PresentationMessage)
     */
    public void removePresentation(PresentationMessage message) {
    	activeMessageSource.removePresentation(message);
    }
    
	/**
     * @return the availableMessageSources
     */
    public Set<MutableMessageSource> getMessageSources() {
    	return availableMessageSources;
    }

	/**
     * @param availableMessageSources the availableMessageSources to set
     */
    public void setMessageSources(
            Set<MutableMessageSource> availableMessageSources) {
    	this.availableMessageSources.addAll(availableMessageSources);
    }

	/**
     * Auto generated method comment
     * 
     * @param beanObject
     */
    private void add(MutableMessageSource beanObject) {
    	availableMessageSources.add(beanObject);
    }

	/**
	 * Merges messages from another message source into the
	 * active (current) message source.
	 * 
     * @see org.openmrs.message.MutableMessageSource#merge(org.openmrs.message.MutableMessageSource)
     */
    public void merge(MutableMessageSource fromSource, boolean overwrite) {
    	activeMessageSource.merge(fromSource, overwrite);
    }

	/**
     * @see org.openmrs.messagesource.MutableMessageSource#getPresentation(java.lang.String, java.util.Locale)
     */
    public PresentationMessage getPresentation(String key, Locale forLocale) {
    	return activeMessageSource.getPresentation(key, forLocale);
    }

	/**
     * @see org.openmrs.messagesource.MutableMessageSource#getPresentationsInLocale(java.util.Locale)
     */
    public Collection<PresentationMessage> getPresentationsInLocale(Locale locale) {
    	return activeMessageSource.getPresentationsInLocale(locale);
    }

	/**
     * @see org.springframework.context.HierarchicalMessageSource#getParentMessageSource()
     */
    public MessageSource getParentMessageSource() {
    	return activeMessageSource.getParentMessageSource();
    }

	/**
     * @see org.springframework.context.HierarchicalMessageSource#setParentMessageSource(org.springframework.context.MessageSource)
     */
    public void setParentMessageSource(MessageSource parent) {
    	activeMessageSource.setParentMessageSource(parent);
    }

}
