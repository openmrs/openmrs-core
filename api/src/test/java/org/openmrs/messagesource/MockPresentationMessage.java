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
		List<String> possibleLocales = Arrays.asList(new String[] { "en", "fr", "pt", "en_US", "en_UK" });
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
	public static MockPresentationMessage createMockPresentationMessage(String localeSpec) {
		return createMockPresentationMessage(new Locale(localeSpec));
	}
	
	/**
	 * Auto generated method comment
	 * 
	 * @param string
	 * @return
	 */
	public static MockPresentationMessage createMockPresentationMessage(Locale forLocale) {
		MockPresentationMessage mockedMessage = createMockPresentationMessage();
		mockedMessage.setLocale(forLocale);
		return mockedMessage;
	}
}
