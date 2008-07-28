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
package org.openmrs.test.util;

import java.util.ArrayList;
import java.util.Date;
import java.util.SortedSet;
import java.util.TreeSet;

import org.openmrs.GlobalProperty;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.util.OpenmrsUtil;

/**
 * Tests the methods in {@link OpenmrsUtil}
 * 
 * TODO: finish adding tests for all methods
 */
public class OpenmrsUtilTest extends BaseContextSensitiveTest {
	
	private static GlobalProperty luhnGP = new GlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_DEFAULT_PATIENT_IDENTIFIER_VALIDATOR, OpenmrsConstants.LUHN_IDENTIFIER_VALIDATOR);
	
	/**
	 * @see org.springframework.test.AbstractTransactionalSpringContextTests#onSetUpInTransaction()
	 */
	@Override
	protected void onSetUpInTransaction() throws Exception {
		initializeInMemoryDatabase();
		authenticate();
		
		Context.getAdministrationService().saveGlobalProperty(luhnGP);
	}
	
	/**
	 * Test the check digit method
	 * 
	 * @throws Exception
	 */
	public void testShouldGetCheckDigit() throws Exception {
		
		System.out.println("In testGetCheckDigit()");
		
		String[] ids = {"9", "99", "999", "123MT", "asdf"};
		int[] cds = {1, 2, 3, 2, 8} ;
		
		for (int i = 0; i< ids.length; i++) {
			System.out.println(ids[i]);
			assertEquals(OpenmrsUtil.getCheckDigit(ids[i]), cds[i]);
		}
		
	}
	
	/**
	 * Test check digit validation methods
	 * 
	 * @throws Exception
	 */
	public void testShouldIsValidCheckDigit() throws Exception {
		
		System.out.println("In testIsValidCheckDigit()");
		
		String[] ids2 = {"9-1", "99-2", "999-3", "123MT-2", "asdf-8", "12abd-7"};
		String[] ids2Char = {"9-b", "99-c", "999-d", "123MT-c", "asdf-i", "12abd-h"};
		for (int i = 0; i< ids2.length; i++) {
			System.out.println(ids2[i]);
			assertTrue(OpenmrsUtil.isValidCheckDigit(ids2[i]));
			assertTrue(OpenmrsUtil.isValidCheckDigit(ids2Char[i]));
		}
		
		String[] ids3 = {"asdf-7", "9-2", "9-4"};
		for (int i = 0; i< ids3.length; i++) {
			System.out.println(ids3[i]);
			assertFalse(OpenmrsUtil.isValidCheckDigit(ids3[i]));
		}
		
		String[] ids4 = {"#@!", "234-3-3", "-3", "2134"};
		for (int i = 0; i< ids4.length; i++) {
			try {
				System.out.println(ids4[i]);
				OpenmrsUtil.isValidCheckDigit(ids4[i]);
				fail("An exception was not thrown for invalid identifier: " + ids4[i]);
			}
			catch (Exception e) {}
		}
	}
	
	/**
	 * test the collection contains method
	 * 
	 * @throws Exception
	 */
	public void testShouldCollectionContainsWithList() throws Exception {
		
		ArrayList<PatientIdentifier> identifiers = new ArrayList<PatientIdentifier>();
		
		PatientIdentifier pi = new PatientIdentifier();
		pi.setIdentifier("123");
		pi.setIdentifierType(new PatientIdentifierType(1));
		pi.setDateCreated(new Date());
		pi.setCreator(new User(1));
		
		identifiers.add(pi);
		assertTrue("There should be 1 identifier in the patient object now", identifiers.size() == 1);
		
		identifiers.add(pi);
		assertFalse("Lists should accept more than one object", identifiers.size() == 1);
		
		pi.setDateCreated(null);
		pi.setCreator(null);
		
		assertTrue("Just because the date is null, doesn't make it not in the list anymore", OpenmrsUtil.collectionContains(identifiers, pi));
	}
	
	/**
	 * test the collection contains method
	 * 
	 * @throws Exception
	 */
	public void testShouldCollectionContainsWithSortedSet() throws Exception {
		
		SortedSet<PatientIdentifier> identifiers = new TreeSet<PatientIdentifier>();
		
		PatientIdentifier pi = new PatientIdentifier();
		pi.setIdentifier("123");
		pi.setIdentifierType(new PatientIdentifierType(1));
		pi.setDateCreated(new Date());
		pi.setCreator(new User(1));
		
		identifiers.add(pi);
		assertTrue("There should be 1 identifier in the patient object now", identifiers.size() == 1);
		
		identifiers.add(pi);
		assertTrue("There should still be only 1 identifier in the patient object now", identifiers.size() == 1);
		
		pi.setDateCreated(null);
		pi.setCreator(null);
		
		assertTrue("Just because the date is null, doesn't make it not in the list anymore", OpenmrsUtil.collectionContains(identifiers, pi));
	}
	
}
