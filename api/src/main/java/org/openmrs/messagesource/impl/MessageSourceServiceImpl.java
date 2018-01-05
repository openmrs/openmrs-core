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
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.openmrs.api.context.Context;
import org.openmrs.messagesource.MessageSourceService;
import org.openmrs.messagesource.MutableMessageSource;
import org.openmrs.messagesource.PresentationMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.NoSuchMessageException;

/**
 * Extensible implementation of the MessageSourceService, which relies on injected implementations
 * of MutableMessageSource to actually provide the services. The sub-services are loaded by special
 * BeanPostProcessor handling, which looks for beans named For example:
 * ResourceBundleMessageSourceService can be specified in the applicationContext-service.xml file to
 * use the usual .properties files to provide messages.
 */
public class MessageSourceServiceImpl implements MessageSourceService {
	
	private static final Logger log = LoggerFactory.getLogger(MessageSourceServiceImpl.class);
	
	private Set<MutableMessageSource> availableMessageSources = new HashSet<>();
	
	private MutableMessageSource activeMessageSource;
	
	/**
	 * @see org.openmrs.messagesource.MessageSourceService#getMessage(java.lang.String)
	 */
	@Override
	public String getMessage(String s) {
		return Context.getMessageSourceService().getMessage(s, null, Context.getLocale());
	}
	
	/**
	 * Gets the message source service which is currently providing services.
	 * 
	 * @return the activeMessageSource
	 */
	@Override
	public MutableMessageSource getActiveMessageSource() {
		return activeMessageSource;
	}
	
	/**
	 * Sets the message source service which will actually provide services.
	 * 
	 * @param activeMessageSource the activeMessageSourceService to set
	 */
	@Override
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
	@Override
	public Collection<Locale> getLocales() {
		return activeMessageSource.getLocales();
	}
	
	/**
	 * Returns all available messages.
	 * 
	 * @see org.openmrs.messagesource.MessageSourceService#getPresentations()
	 */
	@Override
	public Collection<PresentationMessage> getPresentations() {
		return activeMessageSource.getPresentations();
	}
	
	/**
	 * @see org.springframework.context.MessageSource#getMessage(org.springframework.context.MessageSourceResolvable,
	 *      java.util.Locale)
	 */
	@Override
	public String getMessage(MessageSourceResolvable resolvable, Locale locale) {
		if((resolvable.getCodes()[0]).equals((activeMessageSource.getMessage(resolvable, locale)))){
			return (resolvable.getCodes()[(resolvable.getCodes().length) - 1]);
		}
		else{
			return activeMessageSource.getMessage(resolvable, locale);
		}
	}
	
	/**
	 * @see org.springframework.context.MessageSource#getMessage(java.lang.String,
	 *      java.lang.Object[], java.util.Locale)
	 */
	@Override
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
	@Override
	public String getMessage(String code, Object[] args, String defaultMessage, Locale locale) {
		if (StringUtils.isBlank(code) && StringUtils.isBlank(defaultMessage)) {
			return StringUtils.EMPTY;
		}
		
		return activeMessageSource.getMessage(code, args, defaultMessage, locale);
	}
	
	/**
	 * @see org.openmrs.messagesource.MutableMessageSource#addPresentation(org.openmrs.messagesource.PresentationMessage)
	 */
	@Override
	public void addPresentation(PresentationMessage message) {
		activeMessageSource.addPresentation(message);
	}
	
	/**
	 * @see org.openmrs.messagesource.MutableMessageSource#addPresentation(org.openmrs.messagesource.PresentationMessage)
	 */
	@Override
	public void removePresentation(PresentationMessage message) {
		activeMessageSource.removePresentation(message);
	}
	
	/**
	 * @return the availableMessageSources
	 */
	@Override
	public Set<MutableMessageSource> getMessageSources() {
		return availableMessageSources;
	}
	
	/**
	 * @param availableMessageSources the availableMessageSources to set
	 */
	@Override
	public void setMessageSources(Set<MutableMessageSource> availableMessageSources) {
		this.availableMessageSources.addAll(availableMessageSources);
	}
	
	/**
	 * Merges messages from another message source into the active (current) message source.
	 * 
	 * @see org.openmrs.messagesource.MutableMessageSource#merge(MutableMessageSource, boolean)
	 */
	@Override
	public void merge(MutableMessageSource fromSource, boolean overwrite) {
		activeMessageSource.merge(fromSource, overwrite);
	}
	
	/**
	 * @see org.openmrs.messagesource.MutableMessageSource#getPresentation(java.lang.String,
	 *      java.util.Locale)
	 */
	@Override
	public PresentationMessage getPresentation(String key, Locale forLocale) {
		return activeMessageSource.getPresentation(key, forLocale);
	}
	
	/**
	 * @see org.openmrs.messagesource.MutableMessageSource#getPresentationsInLocale(java.util.Locale)
	 */
	@Override
	public Collection<PresentationMessage> getPresentationsInLocale(Locale locale) {
		return activeMessageSource.getPresentationsInLocale(locale);
	}
	
	/**
	 * @see org.springframework.context.HierarchicalMessageSource#getParentMessageSource()
	 */
	@Override
	public MessageSource getParentMessageSource() {
		return activeMessageSource.getParentMessageSource();
	}
	
	/**
	 * @see org.springframework.context.HierarchicalMessageSource#setParentMessageSource(org.springframework.context.MessageSource)
	 */
	@Override
	public void setParentMessageSource(MessageSource parent) {
		activeMessageSource.setParentMessageSource(parent);
	}
	
}
