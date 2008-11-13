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
package org.openmrs.messagesource.impl;

import static org.junit.Assert.assertEquals;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openmrs.messagesource.MockPresentationMessage;
import org.openmrs.messagesource.PresentationMessage;

/**
 *
 */
public class CachedMessageSourceTest {

	/**
	 * Auto generated method comment
	 * 
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	/**
	 * Auto generated method comment
	 * 
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	/**
	 * The PresentationMessageCollection should be able to contain messages in different locales.
	 */
	@Test
	public void shouldContainMultipleLocales() {
		CachedMessageSource testPmc = new CachedMessageSource();
		testPmc.addPresentation(MockPresentationMessage.createMockPresentationMessage("en"));
		testPmc.addPresentation(MockPresentationMessage.createMockPresentationMessage("fr"));
		testPmc.addPresentation(MockPresentationMessage.createMockPresentationMessage("pt"));
		
		assertEquals(3, testPmc.getLocales().size());
	}


	/**
	 * The PresentationMessageCollection should return messages that are the same whether
	 * returned as Strings or when as part of full PresentationMessage object.
	 */
	@Test
	public void shouldMatchMessageWithPresentationMessage() {
		CachedMessageSource testPmc = new CachedMessageSource();
		
		MockPresentationMessage mockPM = MockPresentationMessage.createMockPresentationMessage();
		testPmc.addPresentation(mockPM);
		
		String valueAsString = testPmc.getMessage(mockPM.getCode(), null, mockPM.getLocale());
		PresentationMessage valueAsPM = testPmc.getPresentation(mockPM.getCode(), mockPM.getLocale());
		
		assertEquals(valueAsString, valueAsPM.getMessage());
	}
}
