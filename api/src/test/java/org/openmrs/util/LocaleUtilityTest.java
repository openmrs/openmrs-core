/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.util;

import static java.util.Locale.ENGLISH;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.openmrs.util.LocaleUtility.fromSpecification;
import static org.openmrs.util.OpenmrsConstants.GLOBAL_PROPERTY_DEFAULT_LOCALE_DEFAULT_VALUE;

import java.util.Iterator;
import java.util.Locale;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.openmrs.GlobalProperty;
import org.openmrs.api.context.Context;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;

/**
 * Behavior-driven unit tests for {@link LocaleUtility} class
 */
public class LocaleUtilityTest extends BaseContextSensitiveTest {
	
	/**
	 * @see LocaleUtility#areCompatible(Locale,Locale)
	 */
	@Test
	public void areCompatible_shouldConfirmMatchingLanguageAsCompatible() {
		Locale lhs = Locale.ENGLISH;
		Locale rhs = Locale.ENGLISH;
		
		assertTrue(LocaleUtility.areCompatible(lhs, rhs));
	}
	
	/**
	 * @see LocaleUtility#areCompatible(Locale,Locale)
	 */
	@Test
	public void areCompatible_shouldNotConfirmDifferentLanguageAsCompatible() {
		Locale lhs = Locale.ENGLISH;
		Locale rhs = Locale.FRENCH;
		
		assertFalse(LocaleUtility.areCompatible(lhs, rhs));
	}
	
	/**
	 * @see LocaleUtility#areCompatible(Locale,Locale)
	 */
	@Test
	public void areCompatible_shouldConfirmMatchingCountryAsCompatible() {
		Locale lhs = Locale.US;
		Locale rhs = Locale.US;
		
		assertTrue(LocaleUtility.areCompatible(lhs, rhs));
	}
	
	/**
	 * @see LocaleUtility#areCompatible(Locale,Locale)
	 */
	@Test
	public void areCompatible_shouldNotConfirmDifferentCountryAsCompatible() {
		Locale lhs = Locale.US;
		Locale rhs = Locale.UK;
		
		assertFalse(LocaleUtility.areCompatible(lhs, rhs));
	}
	
	/**
	 * Two locales, where one has no country specified, but where the language matches, should be
	 * considered compatible.
	 * 
	 * @see LocaleUtility#areCompatible(Locale,Locale)
	 */
	@Test
	public void areCompatible_shouldConfirmSameLanguageMissingCountryAsCompatible() {
		Locale lhs = Locale.US;
		Locale rhs = Locale.ENGLISH;
		
		assertTrue(LocaleUtility.areCompatible(lhs, rhs));
	}
	
	/**
	 * Two locales, where one has no country specified, but where the language does not match,
	 * should not be considered compatible.
	 * 
	 * @see LocaleUtility#areCompatible(Locale,Locale)
	 */
	@Test
	public void areCompatible_shouldConfirmDifferentLanguageMissingCountryAsCompatible() {
		Locale lhs = Locale.US;
		Locale rhs = Locale.FRENCH;
		
		assertFalse(LocaleUtility.areCompatible(lhs, rhs));
	}
	
	/**
	 * @see LocaleUtility#getDefaultLocale()
	 */
	@Test
	public void getDefaultLocale_shouldNotFailWithBogusGlobalPropertyValue() {
		Context.getAdministrationService().saveGlobalProperty(
		    new GlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_LOCALE_ALLOWED_LIST, "en_GB, asdfasdf"));
		
		Context.getAdministrationService().saveGlobalProperty(
		    new GlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_DEFAULT_LOCALE, "asdfasdf"));
		
		// check for nonnullness
		assertNotNull(LocaleUtility.getDefaultLocale());
		
		Context.getAdministrationService().saveGlobalProperty(
		    new GlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_DEFAULT_LOCALE, ""));
	}
	
	/**
	 * @see LocaleUtility#getDefaultLocale()
	 */
	@Test
	public void getDefaultLocale_shouldNotFailWithEmptyGlobalPropertyValue() {
		Context.getAdministrationService().saveGlobalProperty(
		    new GlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_DEFAULT_LOCALE, ""));
		
		// check for nonnullness
		assertNotNull(LocaleUtility.getDefaultLocale());
	}
	
	/**
	 * @see LocaleUtility#getDefaultLocale()
	 */
	@Test
	public void getDefaultLocale_shouldNotReturnNullIfGlobalPropertyDoesNotExist() {
		// sanity check
		assertNull(Context.getAdministrationService().getGlobalProperty(
		    OpenmrsConstants.GLOBAL_PROPERTY_DEFAULT_LOCALE));
		
		// check for nonnullness
		assertNotNull(LocaleUtility.getDefaultLocale());
	}
	
	/**
	 * @see LocaleUtility#getDefaultLocale()
	 */
	@Test
	public void getDefaultLocale_shouldReturnLocaleObjectForGlobalProperty() {
		Context.getAdministrationService().saveGlobalProperty(
		    new GlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_LOCALE_ALLOWED_LIST, "en_GB, ja"));
		
		Context.getAdministrationService()
		        .saveGlobalProperty(
		    new GlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_DEFAULT_LOCALE, "ja"));
		
		assertEquals(Locale.JAPANESE, LocaleUtility.getDefaultLocale());
	}
	
	/**
	 * @see LocaleUtility#fromSpecification(String)
	 */
	@Test
	public void fromSpecification_shouldGetLocaleFromTwoCharacterLanguageCode() {
		assertEquals(Locale.ENGLISH, LocaleUtility.fromSpecification("en"));
	}
	
	/**
	 * @see LocaleUtility#fromSpecification(String)
	 */
	@Test
	public void fromSpecification_shouldGetLocaleFromLanguageCodeAndCountryCode() {
		assertEquals(Locale.UK, LocaleUtility.fromSpecification("en_GB"));
	}
	
	/**
	 * @see LocaleUtility#fromSpecification(String)
	 */
	@Test
	public void fromSpecification_shouldGetLocaleFromLanguageCodeCountryCodeAndVariant() {
		Locale locale = LocaleUtility.fromSpecification("en_US_Traditional_WIN");
		assertEquals(Locale.US.getLanguage(), locale.getLanguage());
		assertEquals(Locale.US.getCountry(), locale.getCountry());
		assertEquals("Traditional,WIN", locale.getDisplayVariant());
	}
	
	/**
	 * @see LocaleUtility#getLocalesInOrder()
	 */
	@Test
	public void getLocalesInOrder_shouldAlwaysHaveEnglishIncludedInTheReturnedCollection() {
		Set<Locale> localesInOrder = LocaleUtility.getLocalesInOrder();
		assertTrue(localesInOrder.contains(ENGLISH));
	}
	
	/**
	 * @see LocaleUtility#getLocalesInOrder()
	 */
	@Test
	public void getLocalesInOrder_shouldAlwaysHaveDefaultLocaleDefaultValueIncludedInTheReturnedCollection()
	{
		Set<Locale> localesInOrder = LocaleUtility.getLocalesInOrder();
		assertTrue(localesInOrder.contains(fromSpecification(GLOBAL_PROPERTY_DEFAULT_LOCALE_DEFAULT_VALUE)));
	}
	
	/**
	 * @see LocaleUtility#getLocalesInOrder()
	 */
	@Test
	public void getLocalesInOrder_shouldHaveDefaultLocaleAsTheFirstElementIfUserHasNoPreferredLocale() {
		// make sure the user doesn't have a locale
		Context.setLocale(null);
		
		Set<Locale> localesInOrder = LocaleUtility.getLocalesInOrder();
		assertEquals(LocaleUtility.getDefaultLocale(), localesInOrder.iterator().next());
	}
	
	/**
	 * @see LocaleUtility#getLocalesInOrder()
	 */
	@Test
	public void getLocalesInOrder_shouldHaveDefaultLocaleAsTheSecondElementIfUserHasAPreferredLocale() {
		Locale lu_UG = new Locale("lu", "UG");
		Context.setLocale(lu_UG);
		Set<Locale> localesInOrder = LocaleUtility.getLocalesInOrder();
		Iterator<Locale> it = localesInOrder.iterator();
		assertEquals(lu_UG, it.next());
		assertEquals(LocaleUtility.getDefaultLocale(), it.next());
	}
	
	/**
	 * @see LocaleUtility#getLocalesInOrder()
	 */
	@Test
	public void getLocalesInOrder_shouldReturnASetOfLocalesWithAPredictableOrder() {
		GlobalProperty gp = new GlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_LOCALE_ALLOWED_LIST,
		        "lu, sw_KE, en_US, en_GB", "Test Allowed list of locales");
		Context.getAdministrationService().saveGlobalProperty(gp);
		Locale lu_UG = new Locale("lu", "UG");
		Context.setLocale(lu_UG);
		Set<Locale> localesInOrder = LocaleUtility.getLocalesInOrder();
		Iterator<Locale> it = localesInOrder.iterator();
		assertEquals(new Locale("lu", "UG"), it.next());
		assertEquals(LocaleUtility.getDefaultLocale(), it.next());
		assertEquals(new Locale("lu"), it.next());
		assertEquals(new Locale("sw", "KE"), it.next());
		assertEquals(new Locale("en", "US"), it.next());
		assertEquals(new Locale("en"), it.next());
	}
	
	/**
	 * @see LocaleUtility#getLocalesInOrder()
	 */
	@Test
	public void getLocalesInOrder_shouldReturnASetOfLocalesWithNoDuplicates() {
		GlobalProperty gp = new GlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_LOCALE_ALLOWED_LIST,
		        "lu_UG, lu, sw_KE, en_US, en, en, en_GB, sw_KE", "Test Allowed list of locales");
		Context.getAdministrationService().saveGlobalProperty(gp);
		GlobalProperty defaultLocale = new GlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_DEFAULT_LOCALE, "lu",
		        "Test Allowed list of locales");
		Context.getAdministrationService().saveGlobalProperty(defaultLocale);
		Locale lu_UG = new Locale("lu", "UG");
		Context.setLocale(lu_UG);
		//note that unique list of locales should be lu_UG, lu, sw_KE, en_US, en
		assertEquals(6, LocaleUtility.getLocalesInOrder().size());
		
		Context.getAdministrationService().saveGlobalProperty(
		    new GlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_DEFAULT_LOCALE, ""));
	}
	
	/**
	 * This test doesn't really test anything, and it should ALWAYS be the last method in this
	 * class. <br>
	 * <br>
	 * This method just resets the current user's locale so that when things are run in batches all
	 * tests still work.
	 */
	@Test
	public void should_resetTheLocale() {
		// set user locale to nothing
		Context.setLocale(null);
		
		// clear out the caches
		GlobalProperty defaultLocale = new GlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_DEFAULT_LOCALE, "",
		        "blanking out default locale");
		Context.getAdministrationService().saveGlobalProperty(defaultLocale);
	}
	
	/**
	 * @see LocaleUtility#getDefaultLocale()
	 */
	@Test
	public void getDefaultLocale_shouldNotCacheLocaleWhenSessionIsNotOpen() {
		Context.getAdministrationService().saveGlobalProperty(
		    new GlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_LOCALE_ALLOWED_LIST, "en_GB, ja"));
		// set GP default locale to valid locale that is not the OpenmrsConstant default locale
		GlobalProperty gp = Context.getAdministrationService().saveGlobalProperty(
		    new GlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_DEFAULT_LOCALE, "ja"));
		
		// close session
		Context.closeSession();
		
		// This might fail if default locale is called before this test is run and so the static defaultLocale is cached
		//
		// verify that default locale is the OpenmrsConstant default locale
		assertEquals(LocaleUtility.fromSpecification(OpenmrsConstants.GLOBAL_PROPERTY_DEFAULT_LOCALE_DEFAULT_VALUE),
		    LocaleUtility.getDefaultLocale());
		
		// open a session
		Context.openSession();
		authenticate();
		
		// verify that the default locale is the GP default locale
		assertEquals(Locale.JAPANESE, LocaleUtility.getDefaultLocale());
		
		// clear GP default locale
		gp.setPropertyValue("");
		Context.getAdministrationService().saveGlobalProperty(gp);
	}
}
