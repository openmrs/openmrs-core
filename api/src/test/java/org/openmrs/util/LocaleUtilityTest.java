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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;
import java.util.Locale;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.GlobalProperty;
import org.openmrs.api.context.Context;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.test.Verifies;

/**
 * Behavior-driven unit tests for {@link LocaleUtility} class
 */
public class LocaleUtilityTest extends BaseContextSensitiveTest {
	
	/**
	 * @see {@link LocaleUtility#areCompatible(Locale,Locale)}
	 */
	@Test
	@Verifies(value = "should confirm matching language as compatible", method = "areCompatible(Locale,Locale)")
	public void areCompatible_shouldConfirmMatchingLanguageAsCompatible() throws Exception {
		Locale lhs = Locale.ENGLISH;
		Locale rhs = Locale.ENGLISH;
		
		assertTrue(LocaleUtility.areCompatible(lhs, rhs));
	}
	
	/**
	 * @see {@link LocaleUtility#areCompatible(Locale,Locale)}
	 */
	@Test
	@Verifies(value = "should not confirm different language as compatible", method = "areCompatible(Locale,Locale)")
	public void areCompatible_shouldNotConfirmDifferentLanguageAsCompatible() throws Exception {
		Locale lhs = Locale.ENGLISH;
		Locale rhs = Locale.FRENCH;
		
		assertFalse(LocaleUtility.areCompatible(lhs, rhs));
	}
	
	/**
	 * @see {@link LocaleUtility#areCompatible(Locale,Locale)}
	 */
	@Test
	@Verifies(value = "should confirm matching country as compatible", method = "areCompatible(Locale,Locale)")
	public void areCompatible_shouldConfirmMatchingCountryAsCompatible() throws Exception {
		Locale lhs = Locale.US;
		Locale rhs = Locale.US;
		
		assertTrue(LocaleUtility.areCompatible(lhs, rhs));
	}
	
	/**
	 * @see {@link LocaleUtility#areCompatible(Locale,Locale)}
	 */
	@Test
	@Verifies(value = "should not confirm different country as compatible", method = "areCompatible(Locale,Locale)")
	public void areCompatible_shouldNotConfirmDifferentCountryAsCompatible() throws Exception {
		Locale lhs = Locale.US;
		Locale rhs = Locale.UK;
		
		assertFalse(LocaleUtility.areCompatible(lhs, rhs));
	}
	
	/**
	 * Two locales, where one has no country specified, but where the language matches, should be
	 * considered compatible.
	 * 
	 * @see {@link LocaleUtility#areCompatible(Locale,Locale)}
	 */
	@Test
	@Verifies(value = "should confirm same language missing country as compatible", method = "areCompatible(Locale,Locale)")
	public void areCompatible_shouldConfirmSameLanguageMissingCountryAsCompatible() throws Exception {
		Locale lhs = Locale.US;
		Locale rhs = Locale.ENGLISH;
		
		assertTrue(LocaleUtility.areCompatible(lhs, rhs));
	}
	
	/**
	 * Two locales, where one has no country specified, but where the language does not match,
	 * should not be considered compatible.
	 * 
	 * @see {@link LocaleUtility#areCompatible(Locale,Locale)}
	 */
	@Test
	@Verifies(value = "should confirm different language missing country as compatible", method = "areCompatible(Locale,Locale)")
	public void areCompatible_shouldConfirmDifferentLanguageMissingCountryAsCompatible() throws Exception {
		Locale lhs = Locale.US;
		Locale rhs = Locale.FRENCH;
		
		assertFalse(LocaleUtility.areCompatible(lhs, rhs));
	}
	
	/**
	 * @see {@link LocaleUtility#getDefaultLocale()}
	 */
	@Test
	@Verifies(value = "should not fail with bogus global property value", method = "getDefaultLocale()")
	public void getDefaultLocale_shouldNotFailWithBogusGlobalPropertyValue() throws Exception {
		Context.getAdministrationService().saveGlobalProperty(
		    new GlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_LOCALE_ALLOWED_LIST, "en_GB, asdfasdf"));
		
		Context.getAdministrationService().saveGlobalProperty(
		    new GlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_DEFAULT_LOCALE, "asdfasdf"));
		
		// check for nonnullness
		Assert.assertNotNull(LocaleUtility.getDefaultLocale());
		
		Context.getAdministrationService().saveGlobalProperty(
		    new GlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_DEFAULT_LOCALE, ""));
	}
	
	/**
	 * @see {@link LocaleUtility#getDefaultLocale()}
	 */
	@Test
	@Verifies(value = "should not fail with empty global property value", method = "getDefaultLocale()")
	public void getDefaultLocale_shouldNotFailWithEmptyGlobalPropertyValue() throws Exception {
		Context.getAdministrationService().saveGlobalProperty(
		    new GlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_DEFAULT_LOCALE, ""));
		
		// check for nonnullness
		Assert.assertNotNull(LocaleUtility.getDefaultLocale());
	}
	
	/**
	 * @see {@link LocaleUtility#getDefaultLocale()}
	 */
	@Test
	@Verifies(value = "should not return null if global property does not exist", method = "getDefaultLocale()")
	public void getDefaultLocale_shouldNotReturnNullIfGlobalPropertyDoesNotExist() throws Exception {
		// sanity check
		Assert.assertNull(Context.getAdministrationService().getGlobalProperty(
		    OpenmrsConstants.GLOBAL_PROPERTY_DEFAULT_LOCALE));
		
		// check for nonnullness
		Assert.assertNotNull(LocaleUtility.getDefaultLocale());
	}
	
	/**
	 * @see {@link LocaleUtility#getDefaultLocale()}
	 */
	@Test
	@Verifies(value = "should return locale object for global property", method = "getDefaultLocale()")
	public void getDefaultLocale_shouldReturnLocaleObjectForGlobalProperty() throws Exception {
		Context.getAdministrationService().saveGlobalProperty(
		    new GlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_LOCALE_ALLOWED_LIST, "en_GB, ja"));
		
		GlobalProperty gp = Context.getAdministrationService().saveGlobalProperty(
		    new GlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_DEFAULT_LOCALE, "ja"));
		
		Assert.assertEquals(Locale.JAPANESE, LocaleUtility.getDefaultLocale());
	}
	
	/**
	 * @see {@link LocaleUtility#fromSpecification(String)}
	 */
	@Test
	@Verifies(value = "should get locale from two character language code", method = "fromSpecification(String)")
	public void fromSpecification_shouldGetLocaleFromTwoCharacterLanguageCode() throws Exception {
		Assert.assertEquals(Locale.ENGLISH, LocaleUtility.fromSpecification("en"));
	}
	
	/**
	 * @see {@link LocaleUtility#fromSpecification(String)}
	 */
	@Test
	@Verifies(value = "should get locale from language code and country code", method = "fromSpecification(String)")
	public void fromSpecification_shouldGetLocaleFromLanguageCodeAndCountryCode() throws Exception {
		Assert.assertEquals(Locale.UK, LocaleUtility.fromSpecification("en_GB"));
	}
	
	/**
	 * @see {@link LocaleUtility#fromSpecification(String)}
	 */
	@Test
	@Verifies(value = "should get locale from language code country code and variant", method = "fromSpecification(String)")
	public void fromSpecification_shouldGetLocaleFromLanguageCodeCountryCodeAndVariant() throws Exception {
		Locale locale = LocaleUtility.fromSpecification("en_US_Traditional_WIN");
		Assert.assertEquals(Locale.US.getLanguage(), locale.getLanguage());
		Assert.assertEquals(Locale.US.getCountry(), locale.getCountry());
		Assert.assertEquals("Traditional,WIN", locale.getDisplayVariant());
	}
	
	/**
	 * @see {@link LocaleUtility#getLocalesInOrder()}
	 */
	@Test
	@Verifies(value = "should always have english included in the returned collection", method = "getLocalesInOrder()")
	public void getLocalesInOrder_shouldAlwaysHaveEnglishIncludedInTheReturnedCollection() throws Exception {
		Set<Locale> localesInOrder = LocaleUtility.getLocalesInOrder();
		Assert.assertEquals(true, localesInOrder.contains(Locale.ENGLISH));
	}
	
	/**
	 * @see {@link LocaleUtility#getLocalesInOrder()}
	 */
	@Test
	@Verifies(value = "should always have default locale default value included in the returned collection", method = "getLocalesInOrder()")
	public void getLocalesInOrder_shouldAlwaysHaveDefaultLocaleDefaultValueIncludedInTheReturnedCollection()
	        throws Exception {
		Set<Locale> localesInOrder = LocaleUtility.getLocalesInOrder();
		Assert.assertEquals(true, localesInOrder.contains(LocaleUtility
		        .fromSpecification(OpenmrsConstants.GLOBAL_PROPERTY_DEFAULT_LOCALE_DEFAULT_VALUE)));
	}
	
	/**
	 * @see {@link LocaleUtility#getLocalesInOrder()}
	 */
	@Test
	@Verifies(value = "should have default locale as the first element if user has no preferred locale", method = "getLocalesInOrder()")
	public void getLocalesInOrder_shouldHaveDefaultLocaleAsTheFirstElementIfUserHasNoPreferredLocale() throws Exception {
		// make sure the user doesn't have a locale
		Context.setLocale(null);
		
		Set<Locale> localesInOrder = LocaleUtility.getLocalesInOrder();
		Assert.assertEquals(LocaleUtility.getDefaultLocale(), localesInOrder.iterator().next());
	}
	
	/**
	 * @see {@link LocaleUtility#getLocalesInOrder()}
	 */
	@Test
	@Verifies(value = "should have default locale as the second element if user has a preferred locale", method = "getLocalesInOrder()")
	public void getLocalesInOrder_shouldHaveDefaultLocaleAsTheSecondElementIfUserHasAPreferredLocale() throws Exception {
		Locale lu_UG = new Locale("lu", "UG");
		Context.setLocale(lu_UG);
		Set<Locale> localesInOrder = LocaleUtility.getLocalesInOrder();
		Iterator<Locale> it = localesInOrder.iterator();
		Assert.assertEquals(lu_UG, it.next());
		Assert.assertEquals(LocaleUtility.getDefaultLocale(), it.next());
	}
	
	/**
	 * @see {@link LocaleUtility#getLocalesInOrder()}
	 */
	@Test
	@Verifies(value = "should return a set of locales with a predictable order", method = "getLocalesInOrder()")
	public void getLocalesInOrder_shouldReturnASetOfLocalesWithAPredictableOrder() throws Exception {
		GlobalProperty gp = new GlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_LOCALE_ALLOWED_LIST,
		        "lu, sw_KE, en_US, en_GB", "Test Allowed list of locales");
		Context.getAdministrationService().saveGlobalProperty(gp);
		Locale lu_UG = new Locale("lu", "UG");
		Context.setLocale(lu_UG);
		Set<Locale> localesInOrder = LocaleUtility.getLocalesInOrder();
		Iterator<Locale> it = localesInOrder.iterator();
		Assert.assertEquals(new Locale("lu", "UG"), it.next());
		Assert.assertEquals(LocaleUtility.getDefaultLocale(), it.next());
		Assert.assertEquals(new Locale("lu"), it.next());
		Assert.assertEquals(new Locale("sw", "KE"), it.next());
		Assert.assertEquals(new Locale("en", "US"), it.next());
		Assert.assertEquals(new Locale("en"), it.next());
	}
	
	/**
	 * @see {@link LocaleUtility#getLocalesInOrder()}
	 */
	@Test
	@Verifies(value = "should return a set of locales with no duplicates", method = "getLocalesInOrder()")
	public void getLocalesInOrder_shouldReturnASetOfLocalesWithNoDuplicates() throws Exception {
		GlobalProperty gp = new GlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_LOCALE_ALLOWED_LIST,
		        "lu_UG, lu, sw_KE, en_US, en, en, en_GB, sw_KE", "Test Allowed list of locales");
		Context.getAdministrationService().saveGlobalProperty(gp);
		GlobalProperty defaultLocale = new GlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_DEFAULT_LOCALE, "lu",
		        "Test Allowed list of locales");
		Context.getAdministrationService().saveGlobalProperty(defaultLocale);
		Locale lu_UG = new Locale("lu", "UG");
		Context.setLocale(lu_UG);
		//note that unique list of locales should be lu_UG, lu, sw_KE, en_US, en
		Assert.assertEquals(6, LocaleUtility.getLocalesInOrder().size());
		
		Context.getAdministrationService().saveGlobalProperty(
		    new GlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_DEFAULT_LOCALE, ""));
	}
	
	/**
	 * This test doesn't really test anything, and it should ALWAYS be the last method in this
	 * class. <br/>
	 * <br/>
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
	 * @verifies not cache locale when session is not open
	 */
	@Test
	public void getDefaultLocale_shouldNotCacheLocaleWhenSessionIsNotOpen() throws Exception {
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
		Assert.assertEquals(LocaleUtility.fromSpecification(OpenmrsConstants.GLOBAL_PROPERTY_DEFAULT_LOCALE_DEFAULT_VALUE),
		    LocaleUtility.getDefaultLocale());
		
		// open a session
		Context.openSession();
		authenticate();
		
		// verify that the default locale is the GP default locale
		Assert.assertEquals(Locale.JAPANESE, LocaleUtility.getDefaultLocale());
		
		// clear GP default locale
		gp.setPropertyValue("");
		Context.getAdministrationService().saveGlobalProperty(gp);
	}
}
