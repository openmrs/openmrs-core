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
