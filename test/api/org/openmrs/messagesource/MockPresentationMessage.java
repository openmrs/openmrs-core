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

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.databene.benerator.Generator;
import org.databene.benerator.factory.GeneratorFactory;

/**
 *
 */
public class MockPresentationMessage extends PresentationMessage {
	
	static String CODE_PATTERN = "[a-z]{4,12}";
	static Generator<String> codeGenerator;
	
	static Generator<String> localeGenerator;
	
	static String MESSAGE_PATTERN = "[a-z]*";
	static Generator<String> messageGenerator;

	static String DESCRIPTION_PATTERN = "[a-z]*";
	static Generator<String> descriptionGenerator;
	
	static {
		// create the generators used by the factory
		codeGenerator = GeneratorFactory.getUniqueRegexStringGenerator(CODE_PATTERN, 4, 12, null);
		List<String> possibleLocales = Arrays.asList(new String[] {"en", "fr", "pt", "en_US", "en_UK"});
		localeGenerator = GeneratorFactory.getSampleGenerator(possibleLocales);
		messageGenerator = GeneratorFactory.getUniqueRegexStringGenerator(MESSAGE_PATTERN, 2, 20, null);
		descriptionGenerator = GeneratorFactory.getUniqueRegexStringGenerator(DESCRIPTION_PATTERN, 2, 20, null);
	}
	
	public MockPresentationMessage(String code, Locale locale, String message, String description) {
		super(code, locale, message, description);
	}
	
	/**
	 * Factory method to produce random mocked-up PresentationMessages.
	 * 
	 * @return
	 */
	public static MockPresentationMessage createMockPresentationMessage() {
		
		String code = "mock." + codeGenerator.generate();
		Locale locale = new Locale(localeGenerator.generate());
		String message = messageGenerator.generate();
		String description = "described mockingly as " + descriptionGenerator.generate();
		
		return new MockPresentationMessage(code, locale, message, description);
	}

	/**
     * Auto generated method comment
     * 
     * @param string
     * @return
     */
    public static MockPresentationMessage createMockPresentationMessage(
            String localeSpec) {
    	return createMockPresentationMessage(new Locale(localeSpec));
    }

	/**
     * Auto generated method comment
     * 
     * @param string
     * @return
     */
    public static MockPresentationMessage createMockPresentationMessage(
            Locale forLocale) {
    	MockPresentationMessage mockedMessage = createMockPresentationMessage();
    	mockedMessage.setLocale(forLocale);
    	return mockedMessage;
    }
}
