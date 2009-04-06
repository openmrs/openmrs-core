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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Date;
import java.util.SortedSet;
import java.util.TreeSet;

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
import org.openmrs.test.Verifies;

/**
 * Tests the methods in {@link OpenmrsUtil} TODO: finish adding tests for all methods
 */
public class OpenmrsUtilTest extends BaseContextSensitiveTest {
	
	private static GlobalProperty luhnGP = new GlobalProperty(
	        OpenmrsConstants.GLOBAL_PROPERTY_DEFAULT_PATIENT_IDENTIFIER_VALIDATOR,
	        OpenmrsConstants.LUHN_IDENTIFIER_VALIDATOR);
	
	/**
	 * @see org.springframework.test.AbstractTransactionalSpringContextTests#onSetUpInTransaction()
	 */
	@Before
	public void runBeforeEachTest() throws Exception {
		initializeInMemoryDatabase();
		authenticate();
		
		Context.getAdministrationService().saveGlobalProperty(luhnGP);
	}
	
	/**
	 * Test the check digit method
	 * 
	 * @see {@link OpenmrsUtil#getCheckDigit(String)}
	 */
	@Test
	@Verifies(value = "should get valid check digits", method = "getCheckDigit(String)")
	public void getCheckDigit_shouldGetValidCheckDigits() throws Exception {
		
		String[] ids = { "9", "99", "999", "123MT", "asdf" };
		int[] cds = { 1, 2, 3, 2, 8 };
		
		for (int i = 0; i < ids.length; i++) {
			assertEquals(OpenmrsUtil.getCheckDigit(ids[i]), cds[i]);
		}
		
	}
	
	/**
	 * Test check digit validation methods
	 * 
	 * @see {@link OpenmrsUtil#isValidCheckDigit(String)}
	 */
	@Test
	@Verifies(value = "should validate correct check digits", method = "isValidCheckDigit(String)")
	public void isValidCheckDigit_shouldValidateCorrectCheckDigits() throws Exception {
		
		String[] ids2 = { "9-1", "99-2", "999-3", "123MT-2", "asdf-8", "12abd-7" };
		String[] ids2Char = { "9-b", "99-c", "999-d", "123MT-c", "asdf-i", "12abd-h" };
		for (int i = 0; i < ids2.length; i++) {
			assertTrue(OpenmrsUtil.isValidCheckDigit(ids2[i]));
			assertTrue(OpenmrsUtil.isValidCheckDigit(ids2Char[i]));
		}
	}
	
	/**
	 * @see {@link OpenmrsUtil#isValidCheckDigit(String)}
	 */
	@Test
	@Verifies(value = "should not validate invalid check digits", method = "isValidCheckDigit(String)")
	public void isValidCheckDigit_shouldNotValidateInvalidCheckDigits() throws Exception {
		String[] ids3 = { "asdf-7", "9-2", "9-4" };
		for (int i = 0; i < ids3.length; i++) {
			assertFalse(OpenmrsUtil.isValidCheckDigit(ids3[i]));
		}
	}
	
	/**
	 * @see {@link OpenmrsUtil#isValidCheckDigit(String)}
	 */
	@Test
	@Verifies(value = "should throw error if given an invalid character in id", method = "isValidCheckDigit(String)")
	public void isValidCheckDigit_shouldThrowErrorIfGivenAnInvalidCharacterInId() throws Exception {
		String[] ids4 = { "#@!", "234-3-3", "-3", "2134" };
		for (int i = 0; i < ids4.length; i++) {
			try {
				OpenmrsUtil.isValidCheckDigit(ids4[i]);
				fail("An exception was not thrown for invalid identifier: " + ids4[i]);
			}
			catch (Exception e) {}
		}
	}
	
	/**
	 * test the collection contains method
	 * 
	 * @see {@link OpenmrsUtil#collectionContains(Collection<*>,Object)}
	 */
	@Test
	@Verifies(value = "should use equals method for comparison instead of compareTo given List collection", method = "collectionContains(Collection<*>,Object)")
	public void collectionContains_shouldUseEqualsMethodForComparisonInsteadOfCompareToGivenListCollection()
	                                                                                                        throws Exception {
		
		ArrayList<PatientIdentifier> identifiers = new ArrayList<PatientIdentifier>();
		
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
	 * @see {@link OpenmrsUtil#collectionContains(Collection<*>,Object)}
	 */
	@Test
	@Verifies(value = "should use equals method for comparison instead of compareTo given SortedSet collection", method = "collectionContains(Collection<*>,Object)")
	public void collectionContains_shouldUseEqualsMethodForComparisonInsteadOfCompareToGivenSortedSetCollection()
	                                                                                                             throws Exception {
		
		SortedSet<PatientIdentifier> identifiers = new TreeSet<PatientIdentifier>();
		
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
	 * @see {@link OpenmrsUtil#url2file(URL)}
	 */
	@Test
	@Verifies(value = "should return null given null parameter", method = "url2file(URL)")
	public void url2file_shouldReturnNullGivenNullParameter() throws Exception {
		assertNull(OpenmrsUtil.url2file(null));
	}
	
	/**
	 * @see {@link OpenmrsUtil#validatePassword(String,String,String)}
	 */
	@Test(expected = InvalidCharactersPasswordException.class)
	@Verifies(value = "should fail with digit only password", method = "validatePassword(String,String,String)")
	public void validatePassword_shouldFailWithDigitOnlyPassword() throws Exception {
		OpenmrsUtil.validatePassword("admin", "12345678", "1-8");
	}
	
	/**
	 * @see {@link OpenmrsUtil#validatePassword(String,String,String)}
	 */
	@Test(expected = InvalidCharactersPasswordException.class)
	@Verifies(value = "should fail with char only password", method = "validatePassword(String,String,String)")
	public void validatePassword_shouldFailWithCharOnlyPassword() throws Exception {
		OpenmrsUtil.validatePassword("admin", "testonly", "1-8");
	}
	
	/**
	 * @see {@link OpenmrsUtil#validatePassword(String,String,String)}
	 */
	@Test(expected = InvalidCharactersPasswordException.class)
	@Verifies(value = "should fail without upper case char password", method = "validatePassword(String,String,String)")
	public void validatePassword_shouldFailWithoutUpperCaseCharPassword() throws Exception {
		OpenmrsUtil.validatePassword("admin", "test0nl1", "1-8");
	}
	
	/**
	 * @see {@link OpenmrsUtil#validatePassword(String,String,String)}
	 */
	@Test(expected = WeakPasswordException.class)
	@Verifies(value = "should fail with password equals to user name", method = "validatePassword(String,String,String)")
	public void validatePassword_shouldFailWithPasswordEqualsToUserName() throws Exception {
		OpenmrsUtil.validatePassword("Admin1234", "Admin1234", "1-8");
	}
	
	/**
	 * @see {@link OpenmrsUtil#validatePassword(String,String,String)}
	 */
	@Test(expected = WeakPasswordException.class)
	@Verifies(value = "should fail with password equals to system id", method = "validatePassword(String,String,String)")
	public void validatePassword_shouldFailWithPasswordEqualsToSystemId() throws Exception {
		OpenmrsUtil.validatePassword("admin", "Admin1234", "Admin1234");
	}
	
	/**
	 * @see {@link OpenmrsUtil#validatePassword(String,String,String)}
	 */
	@Test(expected = ShortPasswordException.class)
	@Verifies(value = "should fail with short password", method = "validatePassword(String,String,String)")
	public void validatePassword_shouldFailWithShortPassword() throws Exception {
		OpenmrsUtil.validatePassword("admin", "1234567", "1-8");
	}
	
}
