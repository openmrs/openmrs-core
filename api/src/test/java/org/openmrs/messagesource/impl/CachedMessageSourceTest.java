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

import static org.junit.Assert.assertEquals;

import java.util.Locale;

import org.junit.Test;
import org.openmrs.messagesource.MockPresentationMessage;
import org.openmrs.messagesource.PresentationMessage;
import org.openmrs.test.Verifies;

/**
 *
 */
public class CachedMessageSourceTest {
	
	/**
	 * The PresentationMessageCollection should be able to contain messages in different locales.
	 * 
	 * @see {@link CachedMessageSource#getLocales()}
	 */
	@Test
	@Verifies(value = "should should be able to contain multiple locales", method = "getLocales()")
	public void getLocales_shouldShouldBeAbleToContainMultipleLocales() throws Exception {
		CachedMessageSource testPmc = new CachedMessageSource();
		testPmc.addPresentation(MockPresentationMessage.createMockPresentationMessage("en"));
		testPmc.addPresentation(MockPresentationMessage.createMockPresentationMessage("fr"));
		testPmc.addPresentation(MockPresentationMessage.createMockPresentationMessage("pt"));
		
		assertEquals(3, testPmc.getLocales().size());
	}
	
	/**
	 * The PresentationMessageCollection should return messages that are the same whether returned
	 * as Strings or when as part of full PresentationMessage object.
	 * 
	 * @see {@link CachedMessageSource#getPresentation(String,Locale)}
	 */
	@Test
	@Verifies(value = "should match get message with presentation message", method = "getPresentation(String,Locale)")
	public void getPresentation_shouldMatchGetMessageWithPresentationMessage() throws Exception {
		CachedMessageSource testPmc = new CachedMessageSource();
		
		MockPresentationMessage mockPM = MockPresentationMessage.createMockPresentationMessage();
		testPmc.addPresentation(mockPM);
		
		String valueAsString = testPmc.getMessage(mockPM.getCode(), null, mockPM.getLocale());
		PresentationMessage valueAsPM = testPmc.getPresentation(mockPM.getCode(), mockPM.getLocale());
		
		assertEquals(valueAsString, valueAsPM.getMessage());
	}
	
}
