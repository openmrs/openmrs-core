/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.messagesource.impl;

import java.util.Collection;
import java.util.HashSet;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.messagesource.MessageSourceService;
import org.openmrs.messagesource.MutableMessageSource;
import org.openmrs.messagesource.PresentationMessage;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.NoSuchMessageException;
import org.springframework.transaction.annotation.Transactional;

/**
 * Extensible implementation of the MessageSourceService, which relies on injected implementations
 * of MutableMessageSource to actually provide the services. The sub-services are loaded by special
 * BeanPostProcessor handling, which looks for beans named For example:
 * ResourceBundleMessageSourceService can be specified in the applicationContext-service.xml file to
 * use the usual .properties files to provide messages.
 */
@Transactional
public class MessageSourceServiceImpl implements MessageSourceService {
	
	private Log log = LogFactory.getLog(getClass());
	
	private Set<MutableMessageSource> availableMessageSources = new HashSet<MutableMessageSource>();
	
	private MutableMessageSource activeMessageSource;
	
	/**
	 * @see org.openmrs.messagesource.MessageSourceService#getMessage(java.lang.String)
	 */
	@Transactional(readOnly = true)
	public String getMessage(String s) {
		return Context.getMessageSourceService().getMessage(s, null, Context.getLocale());
	}
	
	/**
	 * Gets the message source service which is currently providing services.
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
	public void setActiveMessageSource(MutableMessageSource activeMessageSource) {
		
		log.debug("Setting activeMessageSource: " + activeMessageSource);
		
		this.activeMessageSource = activeMessageSource;
		if (!availableMessageSources.contains(activeMessageSource)) {
			availableMessageSources.add(activeMessageSource);
		}
	}
	
	/**
	 * Gets the locales which are available from the current message source.
	 * 
	 * @see org.openmrs.messagesource.MessageSourceService#getLocales()
	 */
	public Collection<Locale> getLocales() {
		return activeMessageSource.getLocales();
	}
	
	/**
	 * Presumes to append the messages to a message.properties file which is already being monitored
	 * by the super ReloadableResourceBundleMessageSource. This is a blind, trusting hack.
	 * 
	 * @see org.openmrs.messagesource.MessageSourceService#publishProperties(Properties, String,
	 *      String, String, String)
	 * @deprecated use {@link #merge(MutableMessageSource, boolean)} instead
	 */
	public void publishProperties(Properties props, String locale, String namespace, String name, String version) {
		activeMessageSource.publishProperties(props, locale, namespace, name, version);
	}
	
	/**
	 * Returns all available messages.
	 * 
	 * @see org.openmrs.messagesource.MessageSourceService#getPresentations()
	 */
	public Collection<PresentationMessage> getPresentations() {
		return activeMessageSource.getPresentations();
	}
	
	/**
	 * @see org.springframework.context.MessageSource#getMessage(org.springframework.context.MessageSourceResolvable,
	 *      java.util.Locale)
	 */
	public String getMessage(MessageSourceResolvable resolvable, Locale locale) {
		return activeMessageSource.getMessage(resolvable, locale);
	}
	
	/**
	 * @see org.springframework.context.MessageSource#getMessage(java.lang.String,
	 *      java.lang.Object[], java.util.Locale)
	 */
	public String getMessage(String code, Object[] args, Locale locale) throws NoSuchMessageException {
		if (StringUtils.isBlank(code)) {
			return StringUtils.EMPTY;
		}
		
		return activeMessageSource.getMessage(code, args, code, locale);
	}
	
	/**
	 * @see org.springframework.context.MessageSource#getMessage(java.lang.String,
	 *      java.lang.Object[], java.lang.String, java.util.Locale)
	 */
	public String getMessage(String code, Object[] args, String defaultMessage, Locale locale) {
		if (StringUtils.isBlank(code) && StringUtils.isBlank(defaultMessage)) {
			return StringUtils.EMPTY;
		}
		
		return activeMessageSource.getMessage(code, args, defaultMessage, locale);
	}
	
	/**
	 * @see org.openmrs.messagesource.MutableMessageSource#addPresentation(org.openmrs.messagesource.PresentationMessage)
	 */
	public void addPresentation(PresentationMessage message) {
		activeMessageSource.addPresentation(message);
	}
	
	/**
	 * @see org.openmrs.messagesource.MutableMessageSource#addPresentation(org.openmrs.messagesource.PresentationMessage)
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
	public void setMessageSources(Set<MutableMessageSource> availableMessageSources) {
		this.availableMessageSources.addAll(availableMessageSources);
	}
	
	/**
	 * Merges messages from another message source into the active (current) message source.
	 * 
	 * @see org.openmrs.messagesource.MutableMessageSource#merge(MutableMessageSource, boolean)
	 */
	public void merge(MutableMessageSource fromSource, boolean overwrite) {
		activeMessageSource.merge(fromSource, overwrite);
	}
	
	/**
	 * @see org.openmrs.messagesource.MutableMessageSource#getPresentation(java.lang.String,
	 *      java.util.Locale)
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
