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

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.junit.Test;
import org.openmrs.test.Verifies;

/**
 *
 */
public class PresentationMessageMapTest {
	
	/**
	 * PresentationMessageMap should not add PresentationMessages which are not from the same locale
	 * set for the PresentationMessageMap.
	 * 
	 * @see {@link PresentationMessageMap#put(String,PresentationMessage)}
	 */
	@Test
	@Verifies(value = "should should ignore non matching locale messages", method = "put(String,PresentationMessage)")
	public void put_shouldShouldIgnoreNonMatchingLocaleMessages() throws Exception {
		PresentationMessageMap testPmm = new PresentationMessageMap(Locale.ENGLISH);
		testPmm.put("right_locale", MockPresentationMessage.createMockPresentationMessage("en"));
		testPmm.put("wrong_locale", MockPresentationMessage.createMockPresentationMessage(Locale.GERMAN));
		
		assertEquals(1, testPmm.size());
	}
	
	/**
	 * PresentationMessageMap should only add PresentationMessages which are from the same locale,
	 * even when adding from a batch.
	 * 
	 * @see {@link PresentationMessageMap#putAll(Map<PresentationMessage>)}
	 */
	@Test
	@Verifies(value = "should filter out non matching locale messages from batch add", method = "putAll(Map<PresentationMessage>)")
	public void putAll_shouldFilterOutNonMatchingLocaleMessagesFromBatchAdd() throws Exception {
		Map<String, PresentationMessage> mockMessageMap = new HashMap<String, PresentationMessage>();
		mockMessageMap.put("right_locale", MockPresentationMessage.createMockPresentationMessage("en"));
		mockMessageMap.put("wrong_locale", MockPresentationMessage.createMockPresentationMessage(Locale.GERMAN));
		
		PresentationMessageMap testPmm = new PresentationMessageMap(Locale.ENGLISH);
		testPmm.putAll(mockMessageMap);
		
		assertEquals(1, testPmm.size());
	}
	
}
