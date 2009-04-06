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
package org.openmrs;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Date;

import org.junit.Test;
import org.openmrs.test.Verifies;

/**
 * This class should test all methods on the PersonName object This class does not touch the
 * database, so it does not need to extend the normal openmrs BaseTest
 */
public class PersonNameTest {
	
	/**
	 * @see {@link PersonName#equals(Object)}
	 */
	@Test
	@Verifies(value = "should not fail if either has a null person property", method = "equals(Object)")
	public void equals_shouldNotFailIfEitherHasANullPersonProperty() throws Exception {
		
		PersonName pn1 = new PersonName();
		pn1.setGivenName("firsttest");
		pn1.setFamilyName("firsttest2");
		pn1.setDateCreated(new Date());
		pn1.setVoided(false);
		
		PersonName pn2 = new PersonName();
		pn2.setGivenName("secondtest");
		pn2.setFamilyName("secondtest2");
		pn1.setDateCreated(new Date());
		pn2.setVoided(false);
		
		// this should not fail if the "person" object on PersonName is null
		assertFalse("The names should not be equal", pn1.equals(pn2));
		assertFalse("The names should not be equal", pn2.equals(pn1));
	}
	
	/**
	 * @see {@link PersonName#equals(Object)}
	 */
	@Test
	@Verifies(value = "should return false if this has a missing person property", method = "equals(Object)")
	public void equals_shouldReturnFalseIfThisHasAMissingPersonProperty() throws Exception {
		PersonName pn1 = new PersonName();
		pn1.setGivenName("firsttest");
		pn1.setFamilyName("firsttest2");
		pn1.setDateCreated(new Date());
		pn1.setVoided(false);
		
		PersonName pn2 = new PersonName();
		pn2.setGivenName("secondtest");
		pn2.setFamilyName("secondtest2");
		pn1.setDateCreated(new Date());
		pn2.setVoided(false);
		
		// if only one has a non-null person object
		Person person = new Person();
		pn2.setPerson(person);
		
		// test in both directions
		assertFalse("The names should not be equal", pn1.equals(pn2));
	}
	
	/**
	 * @see {@link PersonName#equals(Object)}
	 */
	@Test
	@Verifies(value = "should return false if obj has a missing person property", method = "equals(Object)")
	public void equals_shouldReturnFalseIfObjHasAMissingPersonProperty() throws Exception {
		PersonName pn1 = new PersonName();
		pn1.setGivenName("firsttest");
		pn1.setFamilyName("firsttest2");
		pn1.setDateCreated(new Date());
		pn1.setVoided(false);
		
		PersonName pn2 = new PersonName();
		pn2.setGivenName("secondtest");
		pn2.setFamilyName("secondtest2");
		pn1.setDateCreated(new Date());
		pn2.setVoided(false);
		
		// if only one has a non-null person object
		Person person = new Person();
		pn2.setPerson(person);
		assertFalse("The names should not be equal", pn2.equals(pn1));
	}
	
	/**
	 * @see {@link PersonName#equals(Object)}
	 */
	@Test
	@Verifies(value = "should return true if properties are equal and have null person", method = "equals(Object)")
	public void equals_shouldReturnTrueIfPropertiesAreEqualAndHaveNullPerson() throws Exception {
		PersonName pn1 = new PersonName();
		pn1.setGivenName("firsttest");
		pn1.setFamilyName("firsttest2");
		pn1.setDateCreated(new Date());
		pn1.setVoided(false);
		
		// test with objects supposedly equal now
		PersonName pn3 = new PersonName();
		pn3.setGivenName("firsttest");
		pn3.setFamilyName("firsttest2");
		pn3.setDateCreated(new Date());
		pn3.setVoided(false);
		
		assertTrue("The names should be equal", pn1.equals(pn3));
		assertTrue("The names should be equal", pn3.equals(pn1));
		
	}
	
}
