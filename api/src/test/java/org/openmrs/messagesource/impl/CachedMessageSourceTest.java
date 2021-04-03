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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Locale;

import org.junit.jupiter.api.Test;
import org.openmrs.messagesource.PresentationMessage;

/**
 * Tests {@link CachedMessageSource}.
 */
public class CachedMessageSourceTest {
	
	/**
	 * The PresentationMessageCollection should be able to contain messages in different locales.
	 * 
	 * @see CachedMessageSource#getLocales()
	 */
	@Test
	public void getLocales_shouldShouldBeAbleToContainMultipleLocales() {
		CachedMessageSource cachedMessages = new CachedMessageSource();
		cachedMessages.addPresentation(new PresentationMessage("uuid.not.unique", Locale.ENGLISH, "the uuid must be unique",
		        "a uuid needs to be unique"));
		cachedMessages.addPresentation(new PresentationMessage("patient.name.required", Locale.GERMAN,
		        "der patientenname ist verpflichtend", "der patientenname ist ein verpflichtendes feld"));
		cachedMessages.addPresentation(new PresentationMessage("patient.address.required", Locale.FRENCH,
		        "l'adresse du patient est obligatoire", "l'adresse du patient est obligatoire"));
		
		assertEquals(3, cachedMessages.getLocales().size());
	}
	
	/**
	 * The PresentationMessageCollection should return messages that are the same whether returned
	 * as Strings or when as part of full PresentationMessage object.
	 * 
	 * @see CachedMessageSource#getPresentation(String,Locale)
	 */
	@Test
	public void getPresentation_shouldMatchGetMessageWithPresentationMessage() {
		CachedMessageSource cachedMessages = new CachedMessageSource();
		
		PresentationMessage message = new PresentationMessage("uuid.not.unique", Locale.ENGLISH, "the uuid must be unique",
		        "a uuid needs to be unique");
		cachedMessages.addPresentation(message);
		
		String valueAsString = cachedMessages.getMessage(message.getCode(), null, message.getLocale());
		PresentationMessage valueAsPM = cachedMessages.getPresentation(message.getCode(), message.getLocale());
		
		assertEquals(valueAsString, valueAsPM.getMessage());
	}
	
}
