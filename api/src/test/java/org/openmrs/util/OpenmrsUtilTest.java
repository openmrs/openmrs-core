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

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Properties;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Collection;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.openmrs.GlobalProperty;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.User;
import org.openmrs.api.InvalidCharactersPasswordException;
import org.openmrs.api.ShortPasswordException;
import org.openmrs.api.WeakPasswordException;
import org.openmrs.api.context.Context;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.test.TestUtil;

/**
 * Tests the methods in {@link OpenmrsUtil} TODO: finish adding tests for all methods
 */
public class OpenmrsUtilTest extends BaseContextSensitiveTest {
	
	private static GlobalProperty luhnGP = new GlobalProperty(
	        OpenmrsConstants.GLOBAL_PROPERTY_DEFAULT_PATIENT_IDENTIFIER_VALIDATOR,
	        OpenmrsConstants.LUHN_IDENTIFIER_VALIDATOR);
	
	/**
	 * @throws Exception
	 * @see org.springframework.test.AbstractTransactionalSpringContextTests#onSetUpInTransaction()
	 */
	@Before
	public void runBeforeEachTest() throws Exception {
		initializeInMemoryDatabase();
		authenticate();
		
		Context.getAdministrationService().saveGlobalProperty(luhnGP);
	}
	
	/**
	 * test the collection contains method
	 * 
	 * @see OpenmrsUtil#collectionContains(Collection,Object)
	 */
	@Test
	public void collectionContains_shouldUseEqualsMethodForComparisonInsteadOfCompareToGivenListCollection()
	{
		
		ArrayList<PatientIdentifier> identifiers = new ArrayList<>();
		
		PatientIdentifier pi = new PatientIdentifier();
		pi.setIdentifier("123");
		pi.setIdentifierType(new PatientIdentifierType(1));
		pi.setDateCreated(new Date());
		pi.setCreator(new User(1));
		
		identifiers.add(pi);
		
		// sanity check
		identifiers.add(pi);
		assertFalse("Lists should accept more than one object", identifiers.size() == 1);
		
		pi.setDateCreated(null);
		pi.setCreator(null);
		
		assertTrue("Just because the date is null, doesn't make it not in the list anymore", OpenmrsUtil.collectionContains(
		    identifiers, pi));
	}
	
	/**
	 * test the collection contains method
	 * 
	 * @see OpenmrsUtil#collectionContains(Collection,Object)
	 */
	@Test
	public void collectionContains_shouldUseEqualsMethodForComparisonInsteadOfCompareToGivenSortedSetCollection()
	{
		
		SortedSet<PatientIdentifier> identifiers = new TreeSet<>();
		
		PatientIdentifier pi = new PatientIdentifier();
		pi.setIdentifier("123");
		pi.setIdentifierType(new PatientIdentifierType(1));
		pi.setDateCreated(new Date());
		pi.setCreator(new User(1));
		
		identifiers.add(pi);
		
		// sanity check
		identifiers.add(pi);
		assertTrue("There should still be only 1 identifier in the patient object now", identifiers.size() == 1);
		
		pi.setDateCreated(null);
		pi.setCreator(null);
		
		assertTrue("Just because the date is null, doesn't make it not in the list anymore", OpenmrsUtil.collectionContains(
		    identifiers, pi));
	}
	
	/**
	 * When given a null parameter, the {@link OpenmrsUtil#url2file(java.net.URL)} method should
	 * quietly fail by returning null
	 * 
	 * @see OpenmrsUtil#url2file(URL)
	 */
	@Test
	public void url2file_shouldReturnNullGivenNullParameter() {
		assertNull(OpenmrsUtil.url2file(null));
	}

	/**
	 * @see OpenmrsUtil#validatePassword(String,String,String)
	 */
	@Test(expected = InvalidCharactersPasswordException.class)
	public void validatePassword_shouldFailWithDigitOnlyPasswordByDefault() {
		OpenmrsUtil.validatePassword("admin", "12345678", "1-8");
	}
	
	/**
	 * @see OpenmrsUtil#validatePassword(String,String,String)
	 */
	@Test(expected = InvalidCharactersPasswordException.class)
	public void validatePassword_shouldFailWithDigitOnlyPasswordIfNotAllowed() {
		TestUtil.saveGlobalProperty(OpenmrsConstants.GP_PASSWORD_REQUIRES_NON_DIGIT, "true");
		OpenmrsUtil.validatePassword("admin", "12345678", "1-8");
	}
	
	/**
	 * @see OpenmrsUtil#validatePassword(String,String,String)
	 */
	@Test
	public void validatePassword_shouldPassWithDigitOnlyPasswordIfAllowed() {
		TestUtil.saveGlobalProperty(OpenmrsConstants.GP_PASSWORD_REQUIRES_NON_DIGIT, "false");
		TestUtil.saveGlobalProperty(OpenmrsConstants.GP_PASSWORD_REQUIRES_UPPER_AND_LOWER_CASE, "false");
		OpenmrsUtil.validatePassword("admin", "12345678", "1-8");
	}
	
	/**
	 * @see OpenmrsUtil#validatePassword(String,String,String)
	 */
	@Test(expected = InvalidCharactersPasswordException.class)
	public void validatePassword_shouldFailWithCharOnlyPasswordByDefault() {
		OpenmrsUtil.validatePassword("admin", "testonly", "1-8");
	}
	
	/**
	 * @see OpenmrsUtil#validatePassword(String,String,String)
	 */
	@Test(expected = InvalidCharactersPasswordException.class)
	public void validatePassword_shouldFailWithCharOnlyPasswordIfNotAllowed() {
		TestUtil.saveGlobalProperty(OpenmrsConstants.GP_PASSWORD_REQUIRES_DIGIT, "true");
		OpenmrsUtil.validatePassword("admin", "testonly", "1-8");
	}
	
	/**
	 * @see OpenmrsUtil#validatePassword(String,String,String)
	 */
	@Test
	public void validatePassword_shouldPassWithCharOnlyPasswordIfAllowed() {
		TestUtil.saveGlobalProperty(OpenmrsConstants.GP_PASSWORD_REQUIRES_DIGIT, "false");
		TestUtil.saveGlobalProperty(OpenmrsConstants.GP_PASSWORD_REQUIRES_UPPER_AND_LOWER_CASE, "false");
		OpenmrsUtil.validatePassword("admin", "testonly", "1-8");
	}
	
	/**
	 * @see OpenmrsUtil#validatePassword(String,String,String)
	 */
	@Test(expected = InvalidCharactersPasswordException.class)
	public void validatePassword_shouldFailWithoutUpperAndLowerCasePasswordByDefault() {
		OpenmrsUtil.validatePassword("admin", "test0nl1", "1-8");
	}
	
	/**
	 * @see OpenmrsUtil#validatePassword(String,String,String)
	 */
	@Test(expected = InvalidCharactersPasswordException.class)
	public void validatePassword_shouldFailWithoutUpperAndLowerCasePasswordIfNotAllowed() {
		TestUtil.saveGlobalProperty(OpenmrsConstants.GP_PASSWORD_REQUIRES_UPPER_AND_LOWER_CASE, "true");
		OpenmrsUtil.validatePassword("admin", "test0nl1", "1-8");
	}
	
	/**
	 * @see OpenmrsUtil#validatePassword(String,String,String)
	 */
	@Test
	public void validatePassword_shouldPassWithoutUpperAndLowerCasePasswordIfAllowed() {
		TestUtil.saveGlobalProperty(OpenmrsConstants.GP_PASSWORD_REQUIRES_UPPER_AND_LOWER_CASE, "false");
		OpenmrsUtil.validatePassword("admin", "test0nl1", "1-8");
	}
	
	/**
	 * @see OpenmrsUtil#validatePassword(String,String,String)
	 */
	@Test(expected = WeakPasswordException.class)
	public void validatePassword_shouldFailWithPasswordEqualsToUserNameByDefault() {
		OpenmrsUtil.validatePassword("Admin1234", "Admin1234", "1-8");
	}
	
	/**
	 * @see OpenmrsUtil#validatePassword(String,String,String)
	 */
	@Test(expected = WeakPasswordException.class)
	public void validatePassword_shouldFailWithPasswordEqualsToUserNameIfNotAllowed() {
		TestUtil.saveGlobalProperty(OpenmrsConstants.GP_PASSWORD_CANNOT_MATCH_USERNAME_OR_SYSTEMID, "true");
		OpenmrsUtil.validatePassword("Admin1234", "Admin1234", "1-8");
	}
	
	/**
	 * @see OpenmrsUtil#validatePassword(String,String,String)
	 */
	@Test
	public void validatePassword_shouldPassWithPasswordEqualsToUserNameIfAllowed() {
		TestUtil.saveGlobalProperty(OpenmrsConstants.GP_PASSWORD_CANNOT_MATCH_USERNAME_OR_SYSTEMID, "false");
		OpenmrsUtil.validatePassword("Admin1234", "Admin1234", "1-8");
	}
	
	/**
	 * @see OpenmrsUtil#validatePassword(String,String,String)
	 */
	@Test(expected = WeakPasswordException.class)
	public void validatePassword_shouldFailWithPasswordEqualsToSystemIdByDefault() {
		OpenmrsUtil.validatePassword("admin", "Admin1234", "Admin1234");
	}
	
	/**
	 * @see OpenmrsUtil#validatePassword(String,String,String)
	 */
	@Test(expected = WeakPasswordException.class)
	public void validatePassword_shouldFailWithPasswordEqualsToSystemIdIfNotAllowed() {
		TestUtil.saveGlobalProperty(OpenmrsConstants.GP_PASSWORD_CANNOT_MATCH_USERNAME_OR_SYSTEMID, "true");
		OpenmrsUtil.validatePassword("admin", "Admin1234", "Admin1234");
	}
	
	/**
	 * @see OpenmrsUtil#validatePassword(String,String,String)
	 */
	@Test
	public void validatePassword_shouldPassWithPasswordEqualsToSystemIdIfAllowed() {
		TestUtil.saveGlobalProperty(OpenmrsConstants.GP_PASSWORD_CANNOT_MATCH_USERNAME_OR_SYSTEMID, "false");
		OpenmrsUtil.validatePassword("admin", "Admin1234", "Admin1234");
	}
	
	/**
	 * @see OpenmrsUtil#validatePassword(String,String,String)
	 */
	@Test(expected = ShortPasswordException.class)
	public void validatePassword_shouldFailWithShortPasswordByDefault() {
		OpenmrsUtil.validatePassword("admin", "1234567", "1-8");
	}
	
	/**
	 * @see OpenmrsUtil#validatePassword(String,String,String)
	 */
	@Test(expected = ShortPasswordException.class)
	public void validatePassword_shouldFailWithShortPasswordIfNotAllowed() {
		TestUtil.saveGlobalProperty(OpenmrsConstants.GP_PASSWORD_MINIMUM_LENGTH, "6");
		OpenmrsUtil.validatePassword("admin", "12345", "1-8");
	}
	
	/**
	 * @see OpenmrsUtil#validatePassword(String,String,String)
	 */
	@Test
	public void validatePassword_shouldPassWithShortPasswordIfAllowed() {
		TestUtil.saveGlobalProperty(OpenmrsConstants.GP_PASSWORD_MINIMUM_LENGTH, "0");
		OpenmrsUtil.validatePassword("admin", "H4t", "1-8");
	}
	
	/**
	 * @see OpenmrsUtil#validatePassword(String,String,String)
	 */
	@Test(expected = InvalidCharactersPasswordException.class)
	public void validatePassword_shouldFailWithPasswordNotMatchingConfiguredRegex() {
		TestUtil.saveGlobalProperty(OpenmrsConstants.GP_PASSWORD_CUSTOM_REGEX,
		    "[A-Z][a-z][0-9][0-9][a-z][A-Z][a-z][a-z][a-z][a-z]");
		OpenmrsUtil.validatePassword("admin", "he11oWorld", "1-8");
	}
	
	/**
	 * @see OpenmrsUtil#validatePassword(String,String,String)
	 */
	@Test
	public void validatePassword_shouldPassWithPasswordMatchingConfiguredRegex() {
		TestUtil.saveGlobalProperty(OpenmrsConstants.GP_PASSWORD_CUSTOM_REGEX,
		    "[A-Z][a-z][0-9][0-9][a-z][A-Z][a-z][a-z][a-z][a-z]");
		OpenmrsUtil.validatePassword("admin", "He11oWorld", "1-8");
	}
	
	/**
	 * @see OpenmrsUtil#validatePassword(String,String,String)
	 */
	@Test
	public void validatePassword_shouldAllowPasswordToContainNonAlphanumericCharacters() {
		OpenmrsUtil.validatePassword("admin", "Test1234?", "1-8");
	}
	
	/**
	 * @see OpenmrsUtil#validatePassword(String,String,String)
	 */
	@Test
	public void validatePassword_shouldAllowPasswordToContainWhiteSpaces() {
		OpenmrsUtil.validatePassword("admin", "Test *&^ 1234? ", "1-8");
	}
	
	/**
	 * @see OpenmrsUtil#getDateFormat(Locale)
	 */
	@Test
	public void getDateFormat_shouldReturnAPatternWithFourYCharactersInIt() {
		Assert.assertEquals("MM/dd/yyyy", OpenmrsUtil.getDateFormat(Locale.US).toLocalizedPattern());
		Assert.assertEquals("dd/MM/yyyy", OpenmrsUtil.getDateFormat(Locale.UK).toLocalizedPattern());
		Assert.assertEquals("tt.MM.uuuu", OpenmrsUtil.getDateFormat(Locale.GERMAN).toLocalizedPattern());
		Assert.assertEquals("dd-MM-yyyy", OpenmrsUtil.getDateFormat(new Locale("pt", "pt")).toLocalizedPattern());
	}
	
	/**
	 * @see OpenmrsUtil#containsUpperAndLowerCase(String)
	 */
	@Test
	public void containsUpperAndLowerCase_shouldReturnTrueIfStringContainsUpperAndLowerCase() {
		Assert.assertTrue(OpenmrsUtil.containsUpperAndLowerCase("Hello"));
		Assert.assertTrue(OpenmrsUtil.containsUpperAndLowerCase("methodName"));
		Assert.assertTrue(OpenmrsUtil.containsUpperAndLowerCase("the letter K"));
		Assert.assertTrue(OpenmrsUtil.containsUpperAndLowerCase("The number 10"));
	}
	
	/**
	 * @see OpenmrsUtil#containsUpperAndLowerCase(String)
	 */
	@Test
	public void containsUpperAndLowerCase_shouldReturnFalseIfStringDoesNotContainLowerCaseCharacters() {
		Assert.assertFalse(OpenmrsUtil.containsUpperAndLowerCase("HELLO"));
		Assert.assertFalse(OpenmrsUtil.containsUpperAndLowerCase("THE NUMBER 10?"));
		Assert.assertFalse(OpenmrsUtil.containsUpperAndLowerCase(""));
		Assert.assertFalse(OpenmrsUtil.containsUpperAndLowerCase(null));
	}
	
	/**
	 * @see OpenmrsUtil#containsUpperAndLowerCase(String)
	 */
	@Test
	public void containsUpperAndLowerCase_shouldReturnFalseIfStringDoesNotContainUpperCaseCharacters() {
		Assert.assertFalse(OpenmrsUtil.containsUpperAndLowerCase("hello"));
		Assert.assertFalse(OpenmrsUtil.containsUpperAndLowerCase("the number 10?"));
		Assert.assertFalse(OpenmrsUtil.containsUpperAndLowerCase(""));
		Assert.assertFalse(OpenmrsUtil.containsUpperAndLowerCase(null));
	}
	
	/**
	 * @see OpenmrsUtil#containsOnlyDigits(String)
	 */
	@Test
	public void containsOnlyDigits_shouldReturnTrueIfStringContainsOnlyDigits() {
		Assert.assertTrue(OpenmrsUtil.containsOnlyDigits("1234567890"));
	}
	
	/**
	 * @see OpenmrsUtil#containsOnlyDigits(String)
	 */
	@Test
	public void containsOnlyDigits_shouldReturnFalseIfStringContainsAnyNonDigits() {
		Assert.assertFalse(OpenmrsUtil.containsOnlyDigits("1.23"));
		Assert.assertFalse(OpenmrsUtil.containsOnlyDigits("123A"));
		Assert.assertFalse(OpenmrsUtil.containsOnlyDigits("12 3"));
		Assert.assertFalse(OpenmrsUtil.containsOnlyDigits(""));
		Assert.assertFalse(OpenmrsUtil.containsOnlyDigits(null));
	}
	
	/**
	 * @see OpenmrsUtil#containsDigit(String)
	 */
	@Test
	public void containsDigit_shouldReturnTrueIfStringContainsAnyDigits() {
		Assert.assertTrue(OpenmrsUtil.containsDigit("There is 1 digit here."));
	}
	
	/**
	 * @see OpenmrsUtil#containsDigit(String)
	 */
	@Test
	public void containsDigit_shouldReturnFalseIfStringContainsNoDigits() {
		Assert.assertFalse(OpenmrsUtil.containsDigit("ABC .$!@#$%^&*()-+=/?><.,~`|[]"));
		Assert.assertFalse(OpenmrsUtil.containsDigit(""));
		Assert.assertFalse(OpenmrsUtil.containsDigit(null));
	}
	
	/**
	 * The validate password method should be in a separate jvm here so that the Context and
	 * services are not available to the validatePassword (similar to how its used in the
	 * initialization wizard), but that is not possible to set up on a test-by-test basis, so we
	 * settle by making the user context not available.
	 * 
	 * @see OpenmrsUtil#validatePassword(String,String,String)
	 */
	@Test
	public void validatePassword_shouldStillWorkWithoutAnOpenSession() {
		Context.closeSession();
		OpenmrsUtil.validatePassword("admin", "1234Password", "systemId");
	}
	
	/**
	 * @see OpenmrsUtil#getDateFormat(Locale)
	 */
	@Test
	public void getDateFormat_shouldNotAllowTheReturnedSimpleDateFormatToBeModified() {
		// start with a locale that is not currently cached by getDateFormat()
		Locale locale = new Locale("hk");
		Assert.assertTrue("default locale is potentially already cached", !Context.getLocale().equals(locale));
		
		// get the initially built dateformat from getDateFormat()
		SimpleDateFormat sdf = OpenmrsUtil.getDateFormat(locale);
		Assert.assertNotSame("initial dateFormatCache entry is modifiable", OpenmrsUtil.getDateFormat(locale), sdf);
		
		// verify changing the pattern on our variable does not affect the cache
		sdf.applyPattern("yyyymmdd");
		Assert.assertTrue("initial dateFormatCache pattern is modifiable", !OpenmrsUtil.getDateFormat(locale).toPattern()
		        .equals(sdf.toPattern()));
		
		// the dateformat cache now contains the format for this locale; checking
		// a second time will guarantee we are looking at cached data and not the
		// initially built dateformat
		sdf = OpenmrsUtil.getDateFormat(locale);
		Assert.assertNotSame("cached dateFormatCache entry is modifiable", OpenmrsUtil.getDateFormat(locale), sdf);
		
		// verify changing the pattern on our variable does not affect the cache
		sdf.applyPattern("yyyymmdd");
		Assert.assertTrue("cached dateFormatCache pattern is modifiable", !OpenmrsUtil.getDateFormat(locale).toPattern()
		        .equals(sdf.toPattern()));
	}
	
	@Test
	public void openmrsDateFormat_shouldParseValidDate() throws ParseException {
		SimpleDateFormat sdf = OpenmrsUtil.getDateFormat(new Locale("en", "GB"));
		sdf.parse("20/12/2001");
		
		sdf = OpenmrsUtil.getDateFormat(new Locale("en", "US"));
		sdf.parse("12/20/2001");
	}
	
	@Test
	public void openmrsDateFormat_shouldNotAllowDatesWithInvalidDaysOrMonths() {
		
		try {
			SimpleDateFormat sdf = OpenmrsUtil.getDateFormat(new Locale("en", "GB"));
			sdf.parse("1/13/2001");
			Assert.fail("Date with invalid month should throw exception.");
		}
		catch (ParseException e) {}
		
		try {
			SimpleDateFormat sdf = OpenmrsUtil.getDateFormat(new Locale("en", "GB"));
			sdf.parse("32/1/2001");
			Assert.fail("Date with invalid day should throw exception.");
		}
		catch (ParseException e) {}
		
		try {
			SimpleDateFormat sdf = OpenmrsUtil.getDateFormat(new Locale("en", "US"));
			sdf.parse("13/1/2001");
			Assert.fail("Date with invalid month should throw exception.");
		}
		catch (ParseException e) {}
		
		try {
			SimpleDateFormat sdf = OpenmrsUtil.getDateFormat(new Locale("en", "US"));
			sdf.parse("1/32/2001");
			Assert.fail("Date with invalid day should throw exception.");
		}
		catch (ParseException e) {}
	}
	
	@Test
	public void openmrsDateFormat_shouldAllowSingleDigitDatesAndMonths() throws ParseException {
		
		SimpleDateFormat sdf = OpenmrsUtil.getDateFormat(new Locale("en"));
		sdf.parse("1/1/2001");
		
	}
	
	@Test
	public void openmrsDateFormat_shouldNotAllowTwoDigitYears() {
		
		try {
			SimpleDateFormat sdf = OpenmrsUtil.getDateFormat(new Locale("en"));
			sdf.parse("01/01/01");
			Assert.fail("Date with two-digit year should throw exception.");
		}
		catch (ParseException e) {}
		
	}
	
	/**
	 * @see OpenmrsUtil#shortenedStackTrace(String)
	 */
	@Test
	public void shortenedStackTrace_shouldRemoveSpringframeworkAndReflectionRelatedLines() {
		String test = "ca.uhn.hl7v2.HL7Exception: Error while processing HL7 message: ORU_R01\n"
		        + "\tat org.openmrs.hl7.impl.HL7ServiceImpl.processHL7Message(HL7ServiceImpl.java:752)\n"
		        + "\tat sun.reflect.GeneratedMethodAccessor262.invoke(Unknown Source)\n"
		        + "\tat sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:25)\n"
		        + "\tat java.lang.reflect.Method.invoke(Method.java:597)\n"
		        + "\tat org.springframework.aop.support.AopUtils.invokeJoinpointUsingReflection(AopUtils.java:307)\n"
		        + "\tat org.springframework.aop.framework.ReflectiveMethodInvocation.invokeJoinpoint(ReflectiveMethodInvocation.java:182)\n"
		        + "\tat org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:149)\n"
		        + "\tat org.springframework.transaction.interceptor.TransactionInterceptor.invoke(TransactionInterceptor.java:106)\n"
		        + "\tat org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:171)\n"
		        + "\tat org.springframework.aop.framework.JdkDynamicAopProxy.invoke(JdkDynamicAopProxy.java:204)\n"
		        + "\tat $Proxy106.processHL7Message(Unknown Source)\n"
		        + "\tat sun.reflect.GeneratedMethodAccessor262.invoke(Unknown Source)\n"
		        + "\tat sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:25)\n"
		        + "\tat java.lang.reflect.Method.invoke(Method.java:597)\n"
		        + "\tat org.springframework.aop.support.AopUtils.invokeJoinpointUsingReflection(AopUtils.java:307)\n"
		        + "\tat org.springframework.aop.framework.ReflectiveMethodInvocation.invokeJoinpoint(ReflectiveMethodInvocation.java:182)\n"
		        + "\tat org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:149)\n"
		        + "\tat org.springframework.transaction.interceptor.TransactionInterceptor.invoke(TransactionInterceptor.java:106)\n"
		        + "\tat org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:171)\n"
		        + "\tat org.openmrs.aop.LoggingAdvice.invoke(LoggingAdvice.java:107)\n"
		        + "\tat org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:171)\n"
		        + "\tat org.springframework.aop.framework.adapter.MethodBeforeAdviceInterceptor.invoke(MethodBeforeAdviceInterceptor.java:50)\n"
		        + "\tat org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:171)\n"
		        + "\tat org.springframework.aop.framework.adapter.MethodBeforeAdviceInterceptor.invoke(MethodBeforeAdviceInterceptor.java:50)\n"
		        + "\tat org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:171)\n"
		        + "\tat org.springframework.aop.framework.JdkDynamicAopProxy.invoke(JdkDynamicAopProxy.java:204)\n"
		        + "\tat $Proxy107.processHL7Message(Unknown Source)\n"
		        + "\tat sun.reflect.GeneratedMethodAccessor262.invoke(Unknown Source)\n"
		        + "\tat sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:25)\n"
		        + "\tat java.lang.reflect.Method.invoke(Method.java:597)\n"
		        + "\tat org.springframework.aop.support.AopUtils.invokeJoinpointUsingReflection(AopUtils.java:307)\n"
		        + "\tat org.springframework.aop.framework.ReflectiveMethodInvocation.invokeJoinpoint(ReflectiveMethodInvocation.java:182)\n"
		        + "\tat org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:149)\n"
		        + "\tat org.springframework.transaction.interceptor.TransactionInterceptor.invoke(TransactionInterceptor.java:106)\n"
		        + "\tat org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:171)\n"
		        + "\tat org.springframework.aop.framework.JdkDynamicAopProxy.invoke(JdkDynamicAopProxy.java:204)\n"
		        + "\tat $Proxy107.processHL7Message(Unknown Source)\n"
		        + "\tat sun.reflect.GeneratedMethodAccessor262.invoke(Unknown Source)\n"
		        + "\tat sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:25)\n"
		        + "\tat java.lang.reflect.Method.invoke(Method.java:597)\n"
		        + "\tat org.springframework.aop.support.AopUtils.invokeJoinpointUsingReflection(AopUtils.java:307)\n"
		        + "\tat org.springframework.aop.framework.ReflectiveMethodInvocation.invokeJoinpoint(ReflectiveMethodInvocation.java:182)\n"
		        + "\tat org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:149)\n"
		        + "\tat org.springframework.aop.framework.adapter.AfterReturningAdviceInterceptor.invoke(AfterReturningAdviceInterceptor.java:50)\n"
		        + "\tat org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:171)\n"
		        + "\tat org.springframework.aop.framework.JdkDynamicAopProxy.invoke(JdkDynamicAopProxy.java:204)\n"
		        + "\tat $Proxy138.processHL7Message(Unknown Source)\n"
		        + "\tat org.openmrs.hl7.impl.HL7ServiceImpl.processHL7InQueue(HL7ServiceImpl.java:657)\n"
		        + "\tat sun.reflect.GeneratedMethodAccessor260.invoke(Unknown Source)\n"
		        + "\tat sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:25)\n"
		        + "\tat java.lang.reflect.Method.invoke(Method.java:597)\n"
		        + "\tat org.springframework.aop.support.AopUtils.invokeJoinpointUsingReflection(AopUtils.java:307)\n"
		        + "\tat org.springframework.aop.framework.ReflectiveMethodInvocation.invokeJoinpoint(ReflectiveMethodInvocation.java:182)\n"
		        + "\tat org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:149)\n"
		        + "\tat org.springframework.transaction.interceptor.TransactionInterceptor.invoke(TransactionInterceptor.java:106)\n"
		        + "\tat org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:171)\n"
		        + "\tat org.springframework.aop.framework.JdkDynamicAopProxy.invoke(JdkDynamicAopProxy.java:204)\n"
		        + "\tat $Proxy106.processHL7InQueue(Unknown Source)\n"
		        + "\tat sun.reflect.GeneratedMethodAccessor260.invoke(Unknown Source)\n"
		        + "\tat sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:25)\n"
		        + "\tat java.lang.reflect.Method.invoke(Method.java:597)\n"
		        + "\tat org.springframework.aop.support.AopUtils.invokeJoinpointUsingReflection(AopUtils.java:307)\n"
		        + "\tat org.springframework.aop.framework.ReflectiveMethodInvocation.invokeJoinpoint(ReflectiveMethodInvocation.java:182)\n"
		        + "\tat org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:149)\n"
		        + "\tat org.springframework.transaction.interceptor.TransactionInterceptor.invoke(TransactionInterceptor.java:106)\n"
		        + "\tat org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:171)\n"
		        + "\tat org.openmrs.aop.LoggingAdvice.invoke(LoggingAdvice.java:107)\n"
		        + "\tat org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:171)\n"
		        + "\tat org.springframework.aop.framework.adapter.MethodBeforeAdviceInterceptor.invoke(MethodBeforeAdviceInterceptor.java:50)\n"
		        + "\tat $Proxy138.processHL7InQueue(Unknown Source)\n"
		        + "\tat org.openmrs.hl7.HL7InQueueProcessor.processHL7InQueue(HL7InQueueProcessor.java:61)\n"
		        + "\tat org.openmrs.hl7.HL7InQueueProcessor.processNextHL7InQueue(HL7InQueueProcessor.java:91)\n"
		        + "\tat org.openmrs.hl7.HL7InQueueProcessor.processHL7InQueue(HL7InQueueProcessor.java:110)\n"
		        + "\tat org.openmrs.scheduler.tasks.ProcessHL7InQueueTask.execute(ProcessHL7InQueueTask.java:57)\n"
		        + "\tat org.openmrs.scheduler.tasks.TaskThreadedInitializationWrapper.execute(TaskThreadedInitializationWrapper.java:72)\n"
		        + "\tat org.openmrs.scheduler.timer.TimerSchedulerTask.run(TimerSchedulerTask.java:48)\n"
		        + "\tat java.util.TimerThread.mainLoop(Timer.java:512)\n"
		        + "\tat java.util.TimerThread.run(Timer.java:462) "
		        + "Caused by: ca.uhn.hl7v2.app.ApplicationException: ca.uhn.hl7v2.HL7Exception: Could not resolve patient by identifier\n"
		        + "\tat org.openmrs.hl7.handler.ORUR01Handler.processMessage(ORUR01Handler.java:132)\n"
		        + "\tat ca.uhn.hl7v2.app.MessageTypeRouter.processMessage(MessageTypeRouter.java:52)\n"
		        + "\tat org.openmrs.hl7.impl.HL7ServiceImpl.processHL7Message(HL7ServiceImpl.java:749) ... 101 more "
		        + "Caused by: ca.uhn.hl7v2.HL7Exception: Could not resolve patient by identifier\n"
		        + "\tat org.openmrs.hl7.handler.ORUR01Handler.getPatientByIdentifier(ORUR01Handler.java:998)\n"
		        + "\tat org.openmrs.hl7.handler.ORUR01Handler.processORU_R01(ORUR01Handler.java:184)\n"
		        + "\tat org.openmrs.hl7.handler.ORUR01Handler.processMessage(ORUR01Handler.java:124) ... 103 more";
		String expected = "ca.uhn.hl7v2.HL7Exception: Error while processing HL7 message: ORU_R01\n"
		        + "\tat org.openmrs.hl7.impl.HL7ServiceImpl.processHL7Message(HL7ServiceImpl.java:752)\n"
		        + "\tat [ignored] ...\n"
		        + "\tat $Proxy106.processHL7Message(Unknown Source)\n"
		        + "\tat [ignored] ...\n"
		        + "\tat org.openmrs.aop.LoggingAdvice.invoke(LoggingAdvice.java:107)\n"
		        + "\tat [ignored] ...\n"
		        + "\tat $Proxy107.processHL7Message(Unknown Source)\n"
		        + "\tat [ignored] ...\n"
		        + "\tat $Proxy107.processHL7Message(Unknown Source)\n"
		        + "\tat [ignored] ...\n"
		        + "\tat $Proxy138.processHL7Message(Unknown Source)\n"
		        + "\tat org.openmrs.hl7.impl.HL7ServiceImpl.processHL7InQueue(HL7ServiceImpl.java:657)\n"
		        + "\tat [ignored] ...\n"
		        + "\tat $Proxy106.processHL7InQueue(Unknown Source)\n"
		        + "\tat [ignored] ...\n"
		        + "\tat org.openmrs.aop.LoggingAdvice.invoke(LoggingAdvice.java:107)\n"
		        + "\tat [ignored] ...\n"
		        + "\tat $Proxy138.processHL7InQueue(Unknown Source)\n"
		        + "\tat org.openmrs.hl7.HL7InQueueProcessor.processHL7InQueue(HL7InQueueProcessor.java:61)\n"
		        + "\tat org.openmrs.hl7.HL7InQueueProcessor.processNextHL7InQueue(HL7InQueueProcessor.java:91)\n"
		        + "\tat org.openmrs.hl7.HL7InQueueProcessor.processHL7InQueue(HL7InQueueProcessor.java:110)\n"
		        + "\tat org.openmrs.scheduler.tasks.ProcessHL7InQueueTask.execute(ProcessHL7InQueueTask.java:57)\n"
		        + "\tat org.openmrs.scheduler.tasks.TaskThreadedInitializationWrapper.execute(TaskThreadedInitializationWrapper.java:72)\n"
		        + "\tat org.openmrs.scheduler.timer.TimerSchedulerTask.run(TimerSchedulerTask.java:48)\n"
		        + "\tat java.util.TimerThread.mainLoop(Timer.java:512)\n"
		        + "\tat java.util.TimerThread.run(Timer.java:462) "
		        + "Caused by: ca.uhn.hl7v2.app.ApplicationException: ca.uhn.hl7v2.HL7Exception: Could not resolve patient by identifier\n"
		        + "\tat org.openmrs.hl7.handler.ORUR01Handler.processMessage(ORUR01Handler.java:132)\n"
		        + "\tat ca.uhn.hl7v2.app.MessageTypeRouter.processMessage(MessageTypeRouter.java:52)\n"
		        + "\tat org.openmrs.hl7.impl.HL7ServiceImpl.processHL7Message(HL7ServiceImpl.java:749) ... 101 more "
		        + "Caused by: ca.uhn.hl7v2.HL7Exception: Could not resolve patient by identifier\n"
		        + "\tat org.openmrs.hl7.handler.ORUR01Handler.getPatientByIdentifier(ORUR01Handler.java:998)\n"
		        + "\tat org.openmrs.hl7.handler.ORUR01Handler.processORU_R01(ORUR01Handler.java:184)\n"
		        + "\tat org.openmrs.hl7.handler.ORUR01Handler.processMessage(ORUR01Handler.java:124) ... 103 more";
		Assert.assertEquals("stack trace was not shortened properly", expected, OpenmrsUtil.shortenedStackTrace(test));
	}
	
	/**
	 * @see OpenmrsUtil#shortenedStackTrace(String)
	 */
	@Test
	public void shortenedStackTrace_shouldReturnNullIfStackTraceIsNull() {
		Assert.assertNull("null value was not returned with null parameter", OpenmrsUtil.shortenedStackTrace(null));
	}
	
	/**
	 * @see OpenmrsUtil#nullSafeEqualsIgnoreCase(String,String)
	 */
	@Test
	public void nullSafeEqualsIgnoreCase_shouldBeCaseInsensitive() {
		Assert.assertTrue(OpenmrsUtil.nullSafeEqualsIgnoreCase("equal", "Equal"));
	}
	
	/**
	 * @see OpenmrsUtil#nullSafeEqualsIgnoreCase(String,String)
	 */
	@Test
	public void nullSafeEqualsIgnoreCase_shouldReturnFalseIfOnlyOneOfTheStringsIsNull() {
		Assert.assertFalse(OpenmrsUtil.nullSafeEqualsIgnoreCase(null, ""));
	}
	
	@Test
	public void storeProperties_shouldEscapeSlashes() throws IOException {
		Charset utf8 = StandardCharsets.UTF_8;
		String expectedProperty = "blacklistRegex";
		String expectedValue = "[^\\p{InBasicLatin}\\p{InLatin1Supplement}]";
		Properties properties = new Properties();
		properties.setProperty(expectedProperty, expectedValue);

		ByteArrayOutputStream actual = new ByteArrayOutputStream();
		ByteArrayOutputStream expected = new ByteArrayOutputStream();
		
		OpenmrsUtil.storeProperties(properties, actual, null);
		
		// Java's underlying implementation correctly writes:
		// blacklistRegex=[^\\p{InBasicLatin}\\p{InLatin1Supplement}]
		// This method didn't exist in Java 5, which is why we wrote a utility method in the first place, so we should
		// just get rid of our own implementation, and use the underlying java one.
		properties.store(new OutputStreamWriter(expected, utf8), null);
		
		assertThat(actual.toByteArray(), is(expected.toByteArray()));
	}

	/**
	 * @throws IOException
	 * @see OpenmrsUtil#copyFile(InputStream, OutputStream)
	 */
	@Test
	public void copyFile_shouldNotCopyTheOutputstreamWhenOutputstreamIsNull() throws IOException {
		String exampleInputStreamString = "ExampleInputStream";
		ByteArrayInputStream input = new ByteArrayInputStream(exampleInputStreamString.getBytes());

		OutputStream output = null;

		OpenmrsUtil.copyFile(input, output);

		assertNull(output);
		assertNotNull(input);
	}


	/**
	 * @throws IOException
	 * @see OpenmrsUtil#copyFile(InputStream, OutputStream)
	 */
	@Test
	public void copyFile_shouldNotCopyTheOutputstreamIfInputstreamIsNull() throws IOException {
		InputStream input = null;

		ByteArrayOutputStream output = spy(new ByteArrayOutputStream());
		OpenmrsUtil.copyFile(input, output);

		assertNull(input);
		assertNotNull(output);
		verify(output, times(1)).close();
	}

	/**
	 * @throws IOException
	 * @see OpenmrsUtil#copyFile(InputStream, OutputStream)
	 */
	@Test
	public void copyFile_shouldCopyInputstreamToOutputstreamAndCloseTheOutputstream() throws IOException {

		String exampleInputStreamString = "ExampleInputStream";
		ByteArrayInputStream expectedByteArrayInputStream = new ByteArrayInputStream(exampleInputStreamString.getBytes());

		ByteArrayOutputStream output = spy(new ByteArrayOutputStream());
		OpenmrsUtil.copyFile(expectedByteArrayInputStream, output);

		expectedByteArrayInputStream.reset();
		ByteArrayInputStream byteArrayInputStreamFromOutputStream = new ByteArrayInputStream(output.toByteArray());

		assertTrue(IOUtils.contentEquals(expectedByteArrayInputStream, byteArrayInputStreamFromOutputStream));
		verify(output, times(1)).close();
	}
	
}
