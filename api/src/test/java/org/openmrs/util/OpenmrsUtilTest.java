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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
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
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.Concept;
import org.openmrs.GlobalProperty;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.User;
import org.openmrs.api.ConceptService;
import org.openmrs.api.InvalidCharactersPasswordException;
import org.openmrs.api.ShortPasswordException;
import org.openmrs.api.WeakPasswordException;
import org.openmrs.api.context.Context;
import org.openmrs.logging.MemoryAppender;
import org.openmrs.logging.OpenmrsLoggingUtil;
import org.openmrs.test.TestUtil;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;

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
	@BeforeEach
	public void runBeforeEachTest() throws Exception {
		if (useInMemoryDatabase()) {
			initializeInMemoryDatabase();
			authenticate();
		}
		
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
		assertFalse(identifiers.size() == 1, "Lists should accept more than one object");
		
		pi.setDateCreated(null);
		pi.setCreator(null);
		
		assertTrue(OpenmrsUtil.collectionContains(identifiers, pi), "Just because the date is null, doesn't make it not in the list anymore");
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
		assertTrue(identifiers.size() == 1, "There should still be only 1 identifier in the patient object now");
		
		pi.setDateCreated(null);
		pi.setCreator(null);
		
		assertTrue(OpenmrsUtil.collectionContains(identifiers, pi), "Just because the date is null, doesn't make it not in the list anymore");
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
	@Test
	public void validatePassword_shouldFailWithDigitOnlyPasswordByDefault() {
		assertThrows(InvalidCharactersPasswordException.class, () -> OpenmrsUtil.validatePassword("admin", "12345678", "1-8"));
	}
	
	/**
	 * @see OpenmrsUtil#validatePassword(String,String,String)
	 */
	@Test
	public void validatePassword_shouldFailWithDigitOnlyPasswordIfNotAllowed() {
		TestUtil.saveGlobalProperty(OpenmrsConstants.GP_PASSWORD_REQUIRES_NON_DIGIT, "true");
		assertThrows(InvalidCharactersPasswordException.class, () -> OpenmrsUtil.validatePassword("admin", "12345678", "1-8"));
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
	@Test
	public void validatePassword_shouldFailWithCharOnlyPasswordByDefault() {
		assertThrows(InvalidCharactersPasswordException.class, () -> OpenmrsUtil.validatePassword("admin", "testonly", "1-8"));
	}
	
	/**
	 * @see OpenmrsUtil#validatePassword(String,String,String)
	 */
	@Test
	public void validatePassword_shouldFailWithCharOnlyPasswordIfNotAllowed() {
		TestUtil.saveGlobalProperty(OpenmrsConstants.GP_PASSWORD_REQUIRES_DIGIT, "true");
		assertThrows(InvalidCharactersPasswordException.class, () -> OpenmrsUtil.validatePassword("admin", "testonly", "1-8"));
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
	@Test
	public void validatePassword_shouldFailWithoutUpperAndLowerCasePasswordByDefault() {
		assertThrows(InvalidCharactersPasswordException.class, () -> OpenmrsUtil.validatePassword("admin", "test0nl1", "1-8"));
	}
	
	/**
	 * @see OpenmrsUtil#validatePassword(String,String,String)
	 */
	@Test
	public void validatePassword_shouldFailWithoutUpperAndLowerCasePasswordIfNotAllowed() {
		TestUtil.saveGlobalProperty(OpenmrsConstants.GP_PASSWORD_REQUIRES_UPPER_AND_LOWER_CASE, "true");
		assertThrows(InvalidCharactersPasswordException.class, () -> OpenmrsUtil.validatePassword("admin", "test0nl1", "1-8"));
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
	@Test
	public void validatePassword_shouldFailWithPasswordEqualsToUserNameByDefault() {
		assertThrows(WeakPasswordException.class, () -> OpenmrsUtil.validatePassword("Admin1234", "Admin1234", "1-8"));
	}
	
	/**
	 * @see OpenmrsUtil#validatePassword(String,String,String)
	 */
	@Test
	public void validatePassword_shouldFailWithPasswordEqualsToUserNameIfNotAllowed() {
		TestUtil.saveGlobalProperty(OpenmrsConstants.GP_PASSWORD_CANNOT_MATCH_USERNAME_OR_SYSTEMID, "true");
		assertThrows(WeakPasswordException.class, () -> OpenmrsUtil.validatePassword("Admin1234", "Admin1234", "1-8"));
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
	@Test
	public void validatePassword_shouldFailWithPasswordEqualsToSystemIdByDefault() {
		assertThrows(WeakPasswordException.class, () -> OpenmrsUtil.validatePassword("admin", "Admin1234", "Admin1234"));
	}
	
	/**
	 * @see OpenmrsUtil#validatePassword(String,String,String)
	 */
	@Test
	public void validatePassword_shouldFailWithPasswordEqualsToSystemIdIfNotAllowed() {
		TestUtil.saveGlobalProperty(OpenmrsConstants.GP_PASSWORD_CANNOT_MATCH_USERNAME_OR_SYSTEMID, "true");
		assertThrows(WeakPasswordException.class, () -> OpenmrsUtil.validatePassword("admin", "Admin1234", "Admin1234"));
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
	@Test
	public void validatePassword_shouldFailWithShortPasswordByDefault() {
		assertThrows(ShortPasswordException.class, () -> OpenmrsUtil.validatePassword("admin", "1234567", "1-8"));
	}
	
	/**
	 * @see OpenmrsUtil#validatePassword(String,String,String)
	 */
	@Test
	public void validatePassword_shouldFailWithShortPasswordIfNotAllowed() {
		TestUtil.saveGlobalProperty(OpenmrsConstants.GP_PASSWORD_MINIMUM_LENGTH, "6");
		assertThrows(ShortPasswordException.class, () ->  OpenmrsUtil.validatePassword("admin", "12345", "1-8"));
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
	@Test
	public void validatePassword_shouldFailWithPasswordNotMatchingConfiguredRegex() {
		TestUtil.saveGlobalProperty(OpenmrsConstants.GP_PASSWORD_CUSTOM_REGEX,
		    "[A-Z][a-z][0-9][0-9][a-z][A-Z][a-z][a-z][a-z][a-z]");
		assertThrows(InvalidCharactersPasswordException.class, () ->  OpenmrsUtil.validatePassword("admin", "he11oWorld", "1-8"));
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
		assertEquals("MM/dd/yyyy", OpenmrsUtil.getDateFormat(Locale.US).toLocalizedPattern());
		assertEquals("dd/MM/yyyy", OpenmrsUtil.getDateFormat(Locale.UK).toLocalizedPattern());
		assertEquals("tt.MM.uuuu", OpenmrsUtil.getDateFormat(Locale.GERMAN).toLocalizedPattern());
		assertEquals("dd-MM-yyyy", OpenmrsUtil.getDateFormat(new Locale("pt", "pt")).toLocalizedPattern());
	}
	
	/**
	 * @see OpenmrsUtil#containsUpperAndLowerCase(String)
	 */
	@Test
	public void containsUpperAndLowerCase_shouldReturnTrueIfStringContainsUpperAndLowerCase() {
		assertTrue(OpenmrsUtil.containsUpperAndLowerCase("Hello"));
		assertTrue(OpenmrsUtil.containsUpperAndLowerCase("methodName"));
		assertTrue(OpenmrsUtil.containsUpperAndLowerCase("the letter K"));
		assertTrue(OpenmrsUtil.containsUpperAndLowerCase("The number 10"));
	}
	
	/**
	 * @see OpenmrsUtil#containsUpperAndLowerCase(String)
	 */
	@Test
	public void containsUpperAndLowerCase_shouldReturnFalseIfStringDoesNotContainLowerCaseCharacters() {
		assertFalse(OpenmrsUtil.containsUpperAndLowerCase("HELLO"));
		assertFalse(OpenmrsUtil.containsUpperAndLowerCase("THE NUMBER 10?"));
		assertFalse(OpenmrsUtil.containsUpperAndLowerCase(""));
		assertFalse(OpenmrsUtil.containsUpperAndLowerCase(null));
	}
	
	/**
	 * @see OpenmrsUtil#containsUpperAndLowerCase(String)
	 */
	@Test
	public void containsUpperAndLowerCase_shouldReturnFalseIfStringDoesNotContainUpperCaseCharacters() {
		assertFalse(OpenmrsUtil.containsUpperAndLowerCase("hello"));
		assertFalse(OpenmrsUtil.containsUpperAndLowerCase("the number 10?"));
		assertFalse(OpenmrsUtil.containsUpperAndLowerCase(""));
		assertFalse(OpenmrsUtil.containsUpperAndLowerCase(null));
	}
	
	/**
	 * @see OpenmrsUtil#containsOnlyDigits(String)
	 */
	@Test
	public void containsOnlyDigits_shouldReturnTrueIfStringContainsOnlyDigits() {
		assertTrue(OpenmrsUtil.containsOnlyDigits("1234567890"));
	}
	
	/**
	 * @see OpenmrsUtil#containsOnlyDigits(String)
	 */
	@Test
	public void containsOnlyDigits_shouldReturnFalseIfStringContainsAnyNonDigits() {
		assertFalse(OpenmrsUtil.containsOnlyDigits("1.23"));
		assertFalse(OpenmrsUtil.containsOnlyDigits("123A"));
		assertFalse(OpenmrsUtil.containsOnlyDigits("12 3"));
		assertFalse(OpenmrsUtil.containsOnlyDigits(""));
		assertFalse(OpenmrsUtil.containsOnlyDigits(null));
	}
	
	/**
	 * @see OpenmrsUtil#containsDigit(String)
	 */
	@Test
	public void containsDigit_shouldReturnTrueIfStringContainsAnyDigits() {
		assertTrue(OpenmrsUtil.containsDigit("There is 1 digit here."));
	}
	
	/**
	 * @see OpenmrsUtil#containsDigit(String)
	 */
	@Test
	public void containsDigit_shouldReturnFalseIfStringContainsNoDigits() {
		assertFalse(OpenmrsUtil.containsDigit("ABC .$!@#$%^&*()-+=/?><.,~`|[]"));
		assertFalse(OpenmrsUtil.containsDigit(""));
		assertFalse(OpenmrsUtil.containsDigit(null));
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
		assertTrue(!Context.getLocale().equals(locale), "default locale is potentially already cached");
		
		// get the initially built dateformat from getDateFormat()
		SimpleDateFormat sdf = OpenmrsUtil.getDateFormat(locale);
		assertNotSame(OpenmrsUtil.getDateFormat(locale), sdf, "initial dateFormatCache entry is modifiable");
		
		// verify changing the pattern on our variable does not affect the cache
		sdf.applyPattern("yyyymmdd");
		assertTrue(!OpenmrsUtil.getDateFormat(locale).toPattern().equals(sdf.toPattern()), "initial dateFormatCache pattern is modifiable");
		
		// the dateformat cache now contains the format for this locale; checking
		// a second time will guarantee we are looking at cached data and not the
		// initially built dateformat
		sdf = OpenmrsUtil.getDateFormat(locale);
		assertNotSame(OpenmrsUtil.getDateFormat(locale), sdf, "cached dateFormatCache entry is modifiable");
		
		// verify changing the pattern on our variable does not affect the cache
		sdf.applyPattern("yyyymmdd");
		assertTrue(!OpenmrsUtil.getDateFormat(locale).toPattern().equals(sdf.toPattern()), "cached dateFormatCache pattern is modifiable");
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
			fail("Date with invalid month should throw exception.");
		}
		catch (ParseException e) {}
		
		try {
			SimpleDateFormat sdf = OpenmrsUtil.getDateFormat(new Locale("en", "GB"));
			sdf.parse("32/1/2001");
			fail("Date with invalid day should throw exception.");
		}
		catch (ParseException e) {}
		
		try {
			SimpleDateFormat sdf = OpenmrsUtil.getDateFormat(new Locale("en", "US"));
			sdf.parse("13/1/2001");
			fail("Date with invalid month should throw exception.");
		}
		catch (ParseException e) {}
		
		try {
			SimpleDateFormat sdf = OpenmrsUtil.getDateFormat(new Locale("en", "US"));
			sdf.parse("1/32/2001");
			fail("Date with invalid day should throw exception.");
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
			fail("Date with two-digit year should throw exception.");
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
		assertEquals(expected, OpenmrsUtil.shortenedStackTrace(test), "stack trace was not shortened properly");
	}
	
	/**
	 * @see OpenmrsUtil#shortenedStackTrace(String)
	 */
	@Test
	public void shortenedStackTrace_shouldReturnNullIfStackTraceIsNull() {
		assertNull(OpenmrsUtil.shortenedStackTrace(null), "null value was not returned with null parameter");
	}
	
	/**
	 * @see OpenmrsUtil#nullSafeEqualsIgnoreCase(String,String)
	 */
	@Test
	public void nullSafeEqualsIgnoreCase_shouldBeCaseInsensitive() {
		assertTrue(OpenmrsUtil.nullSafeEqualsIgnoreCase("equal", "Equal"));
	}
	
	/**
	 * @see OpenmrsUtil#nullSafeEqualsIgnoreCase(String,String)
	 */
	@Test
	public void nullSafeEqualsIgnoreCase_shouldReturnFalseIfOnlyOneOfTheStringsIsNull() {
		assertFalse(OpenmrsUtil.nullSafeEqualsIgnoreCase(null, ""));
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

	/**
	 * @see OpenmrsUtil#applyLogLevels()
	 */
	@Test
	public void applyLogLevels_shouldUpdateLogLevels() {
		Logger logger = LogManager.getLogger(OpenmrsConstants.LOG_CLASS_DEFAULT + ".test");
		Level previousLevel = logger.getLevel();
		Context.getAdministrationService().setGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_LOG_LEVEL,
			OpenmrsConstants.LOG_CLASS_DEFAULT + ".test:" + OpenmrsConstants.LOG_LEVEL_DEBUG);

		OpenmrsUtil.applyLogLevels();

		try {
			assertEquals(logger.getLevel(), Level.DEBUG);
			assertNotEquals(previousLevel, logger.getLevel());
		}
		finally {
			// undo the logging level
			LoggerContext context = ((org.apache.logging.log4j.core.Logger) logger).getContext();
			LoggerConfig config = context.getConfiguration().getLoggerConfig(OpenmrsConstants.LOG_CLASS_DEFAULT + ".test");
			config.setLevel(previousLevel);
			context.updateLoggers();
		}
	}

	/**
	 * @see OpenmrsUtil#applyLogLevels()
	 */
	@Test
	public void applyLogLevels_shouldUpdateDefaultLoggerIfNoneSpecified() {
		Logger logger = LogManager.getLogger(OpenmrsConstants.LOG_CLASS_DEFAULT);
		Level previousLevel = logger.getLevel();
		Context.getAdministrationService()
			.setGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_LOG_LEVEL, OpenmrsConstants.LOG_LEVEL_DEBUG);

		OpenmrsUtil.applyLogLevels();

		try {
			assertEquals(Level.DEBUG, logger.getLevel());
			assertNotEquals(previousLevel, logger.getLevel());
		}
		finally {
			// undo the logging level
			LoggerContext context = ((org.apache.logging.log4j.core.Logger) logger).getContext();
			LoggerConfig config = context.getConfiguration().getLoggerConfig(OpenmrsConstants.LOG_CLASS_DEFAULT);
			config.setLevel(previousLevel);
			context.updateLoggers();
		}
	}

	/**
	 * @see OpenmrsUtil#applyLogLevel(String, String)
	 */
	@Test
	public void applyLogLevels_shouldApplyTraceLevel() {
		Logger logger = LogManager.getLogger(OpenmrsConstants.LOG_CLASS_DEFAULT);
		Level previousLevel = logger.getLevel();

		OpenmrsUtil.applyLogLevel(OpenmrsConstants.LOG_CLASS_DEFAULT, OpenmrsConstants.LOG_LEVEL_TRACE);

		try {
			assertEquals(Level.TRACE, logger.getLevel());
		}
		finally {
			// undo the logging level
			LoggerContext context = ((org.apache.logging.log4j.core.Logger) logger).getContext();
			LoggerConfig config = context.getConfiguration().getLoggerConfig(OpenmrsConstants.LOG_CLASS_DEFAULT);
			config.setLevel(previousLevel);
			context.updateLoggers();
		}
	}

	/**
	 * @see OpenmrsUtil#applyLogLevel(String, String)
	 */
	@Test
	public void applyLogLevels_shouldApplyDebugLevel() {
		Logger logger = LogManager.getLogger(OpenmrsConstants.LOG_CLASS_DEFAULT);
		Level previousLevel = logger.getLevel();

		OpenmrsUtil.applyLogLevel(OpenmrsConstants.LOG_CLASS_DEFAULT, OpenmrsConstants.LOG_LEVEL_DEBUG);

		try {
			assertEquals(Level.DEBUG, logger.getLevel());
		}
		finally {
			// undo the logging level
			LoggerContext context = ((org.apache.logging.log4j.core.Logger) logger).getContext();
			LoggerConfig config = context.getConfiguration().getLoggerConfig(OpenmrsConstants.LOG_CLASS_DEFAULT);
			config.setLevel(previousLevel);
			context.updateLoggers();
		}
	}

	/**
	 * @see OpenmrsUtil#applyLogLevel(String, String)
	 */
	@Test
	public void applyLogLevels_shouldApplyInfoLevel() {
		Logger logger = LogManager.getLogger(OpenmrsConstants.LOG_CLASS_DEFAULT);
		Level previousLevel = logger.getLevel();

		OpenmrsUtil.applyLogLevel(OpenmrsConstants.LOG_CLASS_DEFAULT, OpenmrsConstants.LOG_LEVEL_INFO);

		try {
			assertEquals(Level.INFO, logger.getLevel());
		}
		finally {
			// undo the logging level
			LoggerContext context = ((org.apache.logging.log4j.core.Logger) logger).getContext();
			LoggerConfig config = context.getConfiguration().getLoggerConfig(OpenmrsConstants.LOG_CLASS_DEFAULT);
			config.setLevel(previousLevel);
			context.updateLoggers();
		}
	}

	/**
	 * @see OpenmrsUtil#applyLogLevel(String, String)
	 */
	@Test
	public void applyLogLevels_shouldApplyWarnLevel() {
		Logger logger = LogManager.getLogger(OpenmrsConstants.LOG_CLASS_DEFAULT);
		Level previousLevel = logger.getLevel();

		OpenmrsUtil.applyLogLevel(OpenmrsConstants.LOG_CLASS_DEFAULT, OpenmrsConstants.LOG_LEVEL_WARN);

		try {
			assertEquals(Level.WARN, logger.getLevel());
		}
		finally {
			// undo the logging level
			LoggerContext context = ((org.apache.logging.log4j.core.Logger) logger).getContext();
			LoggerConfig config = context.getConfiguration().getLoggerConfig(OpenmrsConstants.LOG_CLASS_DEFAULT);
			config.setLevel(previousLevel);
			context.updateLoggers();
		}
	}

	/**
	 * @see OpenmrsUtil#applyLogLevel(String, String)
	 */
	@Test
	public void applyLogLevels_shouldApplyErrorLevel() {
		Logger logger = LogManager.getLogger(OpenmrsConstants.LOG_CLASS_DEFAULT);
		Level previousLevel = logger.getLevel();

		OpenmrsUtil.applyLogLevel(OpenmrsConstants.LOG_CLASS_DEFAULT, OpenmrsConstants.LOG_LEVEL_ERROR);

		try {
			assertEquals(Level.ERROR, logger.getLevel());
		}
		finally {
			// undo the logging level
			LoggerContext context = ((org.apache.logging.log4j.core.Logger) logger).getContext();
			LoggerConfig config = context.getConfiguration().getLoggerConfig(OpenmrsConstants.LOG_CLASS_DEFAULT);
			config.setLevel(previousLevel);
			context.updateLoggers();
		}
	}

	/**
	 * @see OpenmrsUtil#applyLogLevel(String, String)
	 */
	@Test
	public void applyLogLevels_shouldApplyFatalLevel() {
		Logger logger = LogManager.getLogger(OpenmrsConstants.LOG_CLASS_DEFAULT);
		Level previousLevel = logger.getLevel();

		OpenmrsUtil.applyLogLevel(OpenmrsConstants.LOG_CLASS_DEFAULT, OpenmrsConstants.LOG_LEVEL_FATAL);

		try {
			assertEquals(Level.FATAL, logger.getLevel());
		}
		finally {
			// undo the logging level
			LoggerContext context = ((org.apache.logging.log4j.core.Logger) logger).getContext();
			LoggerConfig config = context.getConfiguration().getLoggerConfig(OpenmrsConstants.LOG_CLASS_DEFAULT);
			config.setLevel(previousLevel);
			context.updateLoggers();
		}
	}

	/**
	 * @see OpenmrsUtil#applyLogLevel(String, String)
	 */
	@Test
	public void applyLogLevels_shouldDefaultToDefaultLoggerName() {
		Logger logger = LogManager.getLogger(OpenmrsConstants.LOG_CLASS_DEFAULT);
		Level previousLevel = logger.getLevel();

		OpenmrsUtil.applyLogLevel("", OpenmrsConstants.LOG_LEVEL_DEBUG);

		try {
			assertEquals(Level.DEBUG, logger.getLevel());
		}
		finally {
			// undo the logging level
			LoggerContext context = ((org.apache.logging.log4j.core.Logger) logger).getContext();
			LoggerConfig config = context.getConfiguration().getLoggerConfig(OpenmrsConstants.LOG_CLASS_DEFAULT);
			config.setLevel(previousLevel);
			context.updateLoggers();
		}
	}

	/**
	 * @see OpenmrsUtil#applyLogLevel(String, String)
	 */
	@Test
	public void applyLogLevels_shouldWarnWhenCalledWithInvalidLevel() {
		org.openmrs.logging.MemoryAppender memoryAppender = MemoryAppender.newBuilder()
			.setLayout(PatternLayout.newBuilder().withPattern("%m").build()).build();
		
		memoryAppender.start();

		org.apache.logging.log4j.core.Logger logger = (org.apache.logging.log4j.core.Logger) LogManager
			.getLogger(OpenmrsLoggingUtil.class);
		
		Level previousLevel = logger.getLevel();
		logger.setAdditive(false);

		LoggerContext context = logger.getContext();
		LoggerConfig config = logger.get();
		config.setLevel(Level.WARN);
		context.updateLoggers();
		
		logger.addAppender(memoryAppender);
		
		try {
			OpenmrsUtil.applyLogLevel(OpenmrsConstants.LOG_CLASS_DEFAULT, "INVALID STRING");
			
			assertNotNull(memoryAppender.getLogLines());
			assertTrue(memoryAppender.getLogLines().size() > 0);
		} finally {
			logger.removeAppender(memoryAppender);
			
			config.setLevel(previousLevel);
			context.updateLoggers();
		}
	}

	/**
	 * @see OpenmrsUtil#conceptListHelper(String)
	 */
	@Test
	public void conceptListHelper_shouldNotReturnDuplicateConcepts() {
		String descriptor = "name:YES | name:NO | name:YES";
		List<Concept> ret = OpenmrsUtil.conceptListHelper(descriptor);
		assertEquals(2, ret.size());
		descriptor = "set:30 | set:29";
		ret = OpenmrsUtil.conceptListHelper(descriptor);
		assertEquals(1, ret.size());
	}
}
