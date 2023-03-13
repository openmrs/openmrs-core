/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.messagesource;

import java.util.Collection;
import java.util.Locale;

import org.springframework.context.HierarchicalMessageSource;
import org.springframework.context.MessageSource;

/**
 * Extended MessageSource interface, which provides more information about the available messages
 * and can be changed.
 */
public interface MutableMessageSource extends MessageSource, HierarchicalMessageSource {
	
	/**
	 * Gets the locales for which messages are available from this source.
	 * 
	 * @return available message locales
	 */
	public Collection<Locale> getLocales();
		
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
	 * Adds a presentation message to the source. This operation should overwrite any existing
	 * message which conflicts (has the same code and locale).
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
	 * @param overwrite whether to overwrite existing messages
	 */
	public void merge(MutableMessageSource fromSource, boolean overwrite);
	
}
