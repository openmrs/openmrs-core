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
package org.openmrs.test;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.EncounterType;

/**
 * This class tests the all of the {@link EncounterType} non-trivial object methods.
 * 
 * @see EncounterType
 */
public class EncounterTypeTest {
	
	/**
	 * Make sure the EncounterType(Integer) constructor sets the encounterTypeId
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldSetEncounterTypeIdFromConstructor() throws Exception {
		EncounterType encounterType = new EncounterType(123);
		Assert.assertEquals(123, encounterType.getEncounterTypeId().intValue());
	}
	
	/**
	 * Makes sure that two different encounterType objects that have the same 
	 * encounterType id are considered equal 
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldHaveEqualEncounterTypeObjectsByEncounterTypeId() throws Exception {
		EncounterType encounterType1 = new EncounterType(1);
		// another encounterType with the same encounterType id
		EncounterType encounterType2 = new EncounterType(1);
		
		Assert.assertTrue(encounterType1.equals(encounterType2));
	}
	
	/**
	 * Makes sure that two different encounterType objects that have different
	 * encounterType ids are considered unequal
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldNotHaveEqualEncounterTypeObjectsByEncounterTypeId() throws Exception {
		EncounterType encounterType1 = new EncounterType(1);
		// another encounterType with a different encounterType id
		EncounterType encounterType2 = new EncounterType(2);
		
		Assert.assertFalse(encounterType1.equals(encounterType2));
	}
	
	/**
	 * Makes sure that two different encounterType objects that have the same 
	 * encounterType id are considered equal (checks for NPEs)
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldHaveEqualEncounterTypeObjectsWithNoEncounterTypeId() throws Exception {
		// an encounterType object with no encounterType id
		EncounterType encounterType = new EncounterType();
		
		Assert.assertTrue(encounterType.equals(encounterType));
	}
	
	/**
	 * Makes sure that two different encounterType objects are unequal when
	 * one of them doesn't have an encounterType id defined (checks for NPEs)
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldNotHaveEqualEncounterTypeObjectsWhenOneHasNullEncounterTypeId() throws Exception {
		EncounterType encounterTypeWithId = new EncounterType(1);
		// another encounterType that doesn't have an encounterType id
		EncounterType encounterTypeWithoutId = new EncounterType();
		
		Assert.assertFalse(encounterTypeWithId.equals(encounterTypeWithoutId));
		
		// now test the reverse
		Assert.assertFalse(encounterTypeWithoutId.equals(encounterTypeWithId));
		
		EncounterType anotherEncounterTypeWithoutId = new EncounterType();
		// now test with both not having an id
		Assert.assertFalse(encounterTypeWithoutId.equals(anotherEncounterTypeWithoutId));
	}
	
	/**
	 * Make sure we can call {@link EncounterType#hashCode()} with all null
	 * attributes on encounterType and still get a hashcode
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldGetHashCodeWithNullAttributes() throws Exception {
		new EncounterType().hashCode();
	}
		
}
