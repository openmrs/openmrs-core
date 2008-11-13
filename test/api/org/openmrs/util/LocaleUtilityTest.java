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
package org.openmrs.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Locale;

import org.junit.Test;


/**
 * Behavior-driven unit tests for LocaleUtility.
 * 
 */
public class LocaleUtilityTest {

	@Test
	public void shouldConfirmMatchingLanguageAsCompatible() {
		Locale lhs = Locale.ENGLISH;
		Locale rhs = Locale.ENGLISH;
		
		assertTrue(LocaleUtility.areCompatible(lhs, rhs));
	}
	
	@Test
	public void shouldNotConfirmDifferentLanguageAsCompatible() {
		Locale lhs = Locale.ENGLISH;
		Locale rhs = Locale.FRENCH;
		
		assertFalse(LocaleUtility.areCompatible(lhs, rhs));		
	}
	
	@Test
	public void shouldConfirmMatchingCountrysAsCompatible() {
		Locale lhs = Locale.US;
		Locale rhs = Locale.US;
		
		assertTrue(LocaleUtility.areCompatible(lhs, rhs));
	}
	
	@Test
	public void shouldNotConfirmDifferentCountryAsCompatible() {
		Locale lhs = Locale.US;
		Locale rhs = Locale.UK;
		
		assertFalse(LocaleUtility.areCompatible(lhs, rhs));		
	}
	
	/**
	 * Two locales, where one has no country specified, but where
	 * the language matches, should be considered compatible.
	 */
	@Test
	public void shouldConfirmSameLanguageMissingCountryAsCompatible() {
		Locale lhs = Locale.US;
		Locale rhs = Locale.ENGLISH;
		
		assertTrue(LocaleUtility.areCompatible(lhs, rhs));
	}
	

	/**
	 * Two locales, where one has no country specified, but where
	 * the language does not match, should not be considered compatible.
	 */
	@Test
	public void shouldConfirmDifferentLanguageMissingCountryAsCompatible() {
		Locale lhs = Locale.US;
		Locale rhs = Locale.FRENCH;
		
		assertFalse(LocaleUtility.areCompatible(lhs, rhs));
	}
	
}
