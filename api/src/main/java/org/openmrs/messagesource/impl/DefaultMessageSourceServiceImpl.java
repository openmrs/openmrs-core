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

import java.io.InputStream;
import java.util.Collection;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;

import org.openmrs.messagesource.MessageSourceService;
import org.openmrs.messagesource.MutableMessageSource;
import org.openmrs.messagesource.PresentationMessage;
import org.openmrs.util.OpenmrsClassLoader;
import org.openmrs.util.OpenmrsUtil;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.NoSuchMessageException;

/**
 * Loads messages from the default message properties file before spring starts up
 */
public class DefaultMessageSourceServiceImpl implements MessageSourceService {

	private Properties props = new Properties();
	
	/**
	 * Private class to hold the one instance. This is an alternative to
	 * storing the instance object on {@link DefaultMessageSourceServiceImpl} itself so that garbage collection
	 * can happen correctly.
	 */
	private static class DefaultMessageSourceServiceImplHolder {

		private DefaultMessageSourceServiceImplHolder() {
		}

		private static DefaultMessageSourceServiceImpl INSTANCE = null;
	}
	
	/**
	 * Get the static/singular instance
	 *
	 * @return DefaultMessageSourceServiceImpl
	 */
	public static DefaultMessageSourceServiceImpl getInstance() {
		if (DefaultMessageSourceServiceImplHolder.INSTANCE == null) {
			DefaultMessageSourceServiceImplHolder.INSTANCE = new DefaultMessageSourceServiceImpl();
		}
		
		return DefaultMessageSourceServiceImplHolder.INSTANCE;
	}
	
	private DefaultMessageSourceServiceImpl() {
		InputStream stream = OpenmrsClassLoader.getInstance().getResourceAsStream("messages.properties");
		if (stream != null) {
			OpenmrsUtil.loadProperties(props, stream);
		}
	}
	
	@Override
	public String getMessage(String code, Object[] args, String defaultMessage, Locale locale) {
		return getMessage(code);
	}

	@Override
	public String getMessage(String code, Object[] args, Locale locale) throws NoSuchMessageException {
		return getMessage(code);
	}
	
	@Override
	public String getMessage(String s) {
		return (String)props.get(s);
	}
	
	@Override
	public Collection<Locale> getLocales() {
		return null;
	}

	@Override
	public Collection<PresentationMessage> getPresentations() {
		return null;
	}

	@Override
	public Collection<PresentationMessage> getPresentationsInLocale(Locale locale) {
		return null;
	}

	@Override
	public void addPresentation(PresentationMessage message) {
		
	}

	@Override
	public PresentationMessage getPresentation(String key, Locale forLocale) {
		return null;
	}

	@Override
	public void removePresentation(PresentationMessage message) {
		
	}

	@Override
	public void merge(MutableMessageSource fromSource, boolean overwrite) {
		
	}

	@Override
	public String getMessage(MessageSourceResolvable resolvable, Locale locale) throws NoSuchMessageException {
		return null;
	}

	@Override
	public void setParentMessageSource(MessageSource parent) {
		
	}

	@Override
	public MessageSource getParentMessageSource() {
		return null;
	}

	@Override
	public MutableMessageSource getActiveMessageSource() {
		return null;
	}

	@Override
	public void setActiveMessageSource(MutableMessageSource activeMessageSource) {
		
	}

	@Override
	public Set<MutableMessageSource> getMessageSources() {
		return null;
	}

	@Override
	public void setMessageSources(Set<MutableMessageSource> availableMessageSources) {

	}
}
