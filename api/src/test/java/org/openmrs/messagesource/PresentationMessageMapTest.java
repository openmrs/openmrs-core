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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests {@link PresentationMessageMap}.
 */
public class PresentationMessageMapTest {
	
	private static final PresentationMessage MESSAGE_EN = new PresentationMessage("uuid.not.unique", Locale.ENGLISH,
	        "the uuid must be unique", "a uuid needs to be unique");
	
	private static final PresentationMessage MESSAGE_DE = new PresentationMessage("patient.name.required", Locale.GERMAN,
	        "der patientenname ist verpflichtend", "der patientenname ist ein verpflichtendes feld");
	
	private static final String EXPECTED_MESSAGE_KEY = "right_locale";
	
	private PresentationMessageMap presentationMessages;
	
	@BeforeEach
	public void setUp() {
		presentationMessages = new PresentationMessageMap(Locale.ENGLISH);
	}
	
	/**
	 * PresentationMessageMap should not add PresentationMessages which are not from the same locale
	 * set for the PresentationMessageMap.
	 * 
	 * @see PresentationMessageMap#put(String,PresentationMessage)
	 */
	@Test
	public void put_shouldShouldIgnoreNonMatchingLocaleMessages() {
		
		presentationMessages.put(EXPECTED_MESSAGE_KEY, MESSAGE_EN);
		presentationMessages.put("wrong_locale", MESSAGE_DE);
		
		assertEquals(1, presentationMessages.size());
		assertTrue(presentationMessages.containsKey(EXPECTED_MESSAGE_KEY));
	}
	
	/**
	 * PresentationMessageMap should only add PresentationMessages which are from the same locale,
	 * even when adding from a batch.
	 * 
	 * @see PresentationMessageMap#putAll(Map<PresentationMessage>)
	 */
	@Test
	public void putAll_shouldFilterOutNonMatchingLocaleMessagesFromBatchAdd() {
		Map<String, PresentationMessage> messageMap = new HashMap<>();
		messageMap.put(EXPECTED_MESSAGE_KEY, MESSAGE_EN);
		messageMap.put("wrong_locale", MESSAGE_DE);
		
		presentationMessages.putAll(messageMap);
		
		assertEquals(1, presentationMessages.size());
		assertTrue(presentationMessages.containsKey(EXPECTED_MESSAGE_KEY));
	}
	
}
