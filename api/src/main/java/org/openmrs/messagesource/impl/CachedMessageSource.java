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

import java.text.MessageFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ArrayList;

import org.openmrs.messagesource.MutableMessageSource;
import org.openmrs.messagesource.PresentationMessage;
import org.openmrs.messagesource.PresentationMessageMap;
import org.springframework.context.support.AbstractMessageSource;

/**
 * A MutableMessageSource backed by a localized map of PresentationMessageCollections, providing
 * in-memory storage of PresentationMessages. Useful for temporary storage, as a cache for other
 * sources, and for testing.
 */
public class CachedMessageSource extends AbstractMessageSource implements MutableMessageSource {
	
	Map<Locale, PresentationMessageMap> localizedMap = new HashMap<>();
	
	/* (non-Javadoc)
	 * @see org.openmrs.messagesource.MutableMessageSource#addPresentation(org.openmrs.api.PresentationMessage)
	 */
	@Override
	public void addPresentation(PresentationMessage message) {
		PresentationMessageMap codeMessageMap = localizedMap
				.computeIfAbsent(message.getLocale(), k -> new PresentationMessageMap(message.getLocale()));
		codeMessageMap.put(message.getCode(), message);
	}
	
	/**
	 * @see org.openmrs.messagesource.MutableMessageSource#getLocales()
	 * <strong>Should</strong> should be able to contain multiple locales
	 */
	@Override
	public Collection<Locale> getLocales() {
		return localizedMap.keySet();
	}
	
	/* (non-Javadoc)
	 * @see org.openmrs.messagesource.MutableMessageSource#getPresentations()
	 */
	@Override
	public Collection<PresentationMessage> getPresentations() {
		Collection<PresentationMessage> allMessages = new ArrayList<>();
		
		for (PresentationMessageMap codeMessageMap : localizedMap.values()) {
			allMessages.addAll(codeMessageMap.values());
		}
		
		return allMessages;
	}
	
	/* (non-Javadoc)
	 * @see org.openmrs.messagesource.MutableMessageSource#removePresentation(org.openmrs.api.PresentationMessage)
	 */
	@Override
	public void removePresentation(PresentationMessage message) {
		PresentationMessageMap codeMessageMap = localizedMap.get(message.getLocale());
		if ((codeMessageMap != null) && codeMessageMap.containsKey(message.getCode())) {
			codeMessageMap.remove(message.getCode());
		}
	}
	
	@Override
	public void merge(MutableMessageSource fromSource, boolean overwrite) {
		for (PresentationMessage message : fromSource.getPresentations()) {
			addPresentation(message);
		}
	}
	
	/**
	 * @see org.openmrs.messagesource.MutableMessageSource#getPresentation(java.lang.String,
	 *      java.util.Locale)
	 * <strong>Should</strong> match get message with presentation message
	 */
	@Override
	public PresentationMessage getPresentation(String key, Locale forLocale) {
		PresentationMessage foundPM = null;
		PresentationMessageMap codeMessageMap = localizedMap.get(forLocale);
		if ((codeMessageMap != null) && codeMessageMap.containsKey(key)) {
			foundPM = codeMessageMap.get(key);
		}
		return foundPM;
	}
	
	/**
	 * @see org.openmrs.messagesource.MutableMessageSource#getPresentationsInLocale(java.util.Locale)
	 */
	@Override
	public Collection<PresentationMessage> getPresentationsInLocale(Locale locale) {
		Collection<PresentationMessage> foundPresentations = null;
		PresentationMessageMap codeMessageMap = localizedMap.get(locale);
		if (codeMessageMap != null) {
			foundPresentations = codeMessageMap.values();
		}
		return foundPresentations;
	}
	
	/**
	 * @see org.springframework.context.support.AbstractMessageSource#resolveCode(java.lang.String,
	 *      java.util.Locale)
	 */
	@Override
	protected MessageFormat resolveCode(String code, Locale locale) {
		MessageFormat resolvedMessageFormatForCode = null;
		
		PresentationMessage pmForCode = getPresentation(code, locale);
		if (pmForCode != null) {
			resolvedMessageFormatForCode = new MessageFormat(pmForCode.getMessage());
		}
		return resolvedMessageFormatForCode;
	}
	
}
