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

import junit.framework.Assert;

import org.junit.Test;
import org.openmrs.test.Verifies;
import org.openmrs.util.OpenmrsConstants;

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
	
	/**
	 * @see PersonName#newInstance(PersonName)
	 */
	@Test
	@Verifies(value = "should copy every property of given personName", method = "newInstance(PersonName)")
	public void newInstance_shouldCopyEveryPropertyOfGivenPersonName() throws Exception {
		Integer personNameId = 333;
		boolean preferred = true;
		String prefix = "prefix";
		Person person = new Person(1);
		String givenName = "given";
		String middleName = "middle";
		String familyNamePrefix = "familyNamePrefix";
		String familyName = "familyName";
		String familyName2 = "familyName2";
		String familyNameSuffix = "familyNameSuffix";
		String degree = "degree";
		boolean voided = true;
		User voidedBy = new User(1);
		Date dateVoided = new Date();
		String voidReason = "voidReason";
		
		PersonName pn = new PersonName(personNameId);
		pn.setPreferred(preferred);
		pn.setPrefix(prefix);
		pn.setPerson(person);
		pn.setGivenName(givenName);
		pn.setMiddleName(middleName);
		pn.setFamilyNamePrefix(familyNamePrefix);
		pn.setFamilyName(familyName);
		pn.setFamilyName2(familyName2);
		pn.setFamilyNameSuffix(familyNameSuffix);
		pn.setDegree(degree);
		pn.setVoided(voided);
		pn.setVoidedBy(voidedBy);
		pn.setVoidReason(voidReason);
		
		PersonName copy = PersonName.newInstance(pn);
		
		Assert.assertEquals(personNameId, copy.getPersonNameId());
		Assert.assertEquals(preferred, copy.getPreferred().booleanValue());
		Assert.assertEquals(prefix, copy.getPrefix());
		Assert.assertEquals(person, copy.getPerson());
		Assert.assertEquals(givenName, copy.getGivenName());
		Assert.assertEquals(middleName, copy.getMiddleName());
		Assert.assertEquals(familyNamePrefix, copy.getFamilyNamePrefix());
		Assert.assertEquals(familyName, copy.getFamilyName());
		Assert.assertEquals(familyName2, copy.getFamilyName2());
		Assert.assertEquals(familyNameSuffix, copy.getFamilyNameSuffix());
		Assert.assertEquals(degree, copy.getDegree());
		Assert.assertEquals(voided, copy.getVoided().booleanValue());
		Assert.assertEquals(voidedBy, copy.getVoidedBy());
		Assert.assertEquals(voidReason, copy.getVoidReason());
	}
	
	/**
	 * @see PersonName#compareTo(PersonName)
	 */
	@Test
	@Verifies(value = "should return negative if other name is voided", method = "compareTo(PersonName)")
	public void compareTo_shouldReturnNegativeIfOtherNameIsVoided() throws Exception {
		PersonName pn = new PersonName();
		pn.setVoided(false);
		PersonName other = new PersonName();
		other.setVoided(true);
		
		Assert.assertTrue(pn.compareTo(other) < 0);
	}
	
	/**
	 * @see PersonName#compareTo(PersonName)
	 */
	@Test
	@Verifies(value = "should return negative if this name is preferred", method = "compareTo(PersonName)")
	public void compareTo_shouldReturnNegativeIfThisNameIsPreferred() throws Exception {
		PersonName pn = new PersonName();
		pn.setPreferred(true);
		PersonName other = new PersonName();
		other.setPreferred(false);
		
		Assert.assertTrue(pn.compareTo(other) < 0);
	}
	
	/**
	 * @see PersonName#compareTo(PersonName)
	 */
	@Test
	@Verifies(value = "should return negative if other familyName is greater", method = "compareTo(PersonName)")
	public void compareTo_shouldReturnNegativeIfOtherFamilyNameIsGreater() throws Exception {
		PersonName pn = new PersonName();
		pn.setFamilyName("Jones");
		PersonName other = new PersonName();
		other.setFamilyName("Smith");
		
		Assert.assertTrue(pn.compareTo(other) < 0);
	}
	
	/**
	 * @see PersonName#compareTo(PersonName)
	 */
	@Test
	@Verifies(value = "should return negative if other familyName2 is greater", method = "compareTo(PersonName)")
	public void compareTo_shouldReturnNegativeIfOtherFamilyName2IsGreater() throws Exception {
		PersonName pn = new PersonName();
		pn.setFamilyName2("Jones");
		PersonName other = new PersonName();
		other.setFamilyName2("Smith");
		
		Assert.assertTrue(pn.compareTo(other) < 0);
	}
	
	/**
	 * @see PersonName#compareTo(PersonName)
	 */
	@Test
	@Verifies(value = "should return negative if other givenName is greater", method = "compareTo(PersonName)")
	public void compareTo_shouldReturnNegativeIfOtherGivenNameIsGreater() throws Exception {
		PersonName pn = new PersonName();
		pn.setGivenName("Adam");
		PersonName other = new PersonName();
		other.setGivenName("Bob");
		
		Assert.assertTrue(pn.compareTo(other) < 0);
	}
	
	/**
	 * @see PersonName#compareTo(PersonName)
	 */
	@Test
	@Verifies(value = "should return negative if other middleName is greater", method = "compareTo(PersonName)")
	public void compareTo_shouldReturnNegativeIfOtherMiddleNameIsGreater() throws Exception {
		PersonName pn = new PersonName();
		pn.setMiddleName("Alex");
		PersonName other = new PersonName();
		other.setMiddleName("Brian");
		
		Assert.assertTrue(pn.compareTo(other) < 0);
	}
	
	/**
	 * @see PersonName#compareTo(PersonName)
	 */
	@Test
	@Verifies(value = "should return negative if other familynamePrefix is greater", method = "compareTo(PersonName)")
	public void compareTo_shouldReturnNegativeIfOtherFamilynamePrefixIsGreater() throws Exception {
		PersonName pn = new PersonName();
		pn.setFamilyNamePrefix("Madam");
		PersonName other = new PersonName();
		other.setFamilyNamePrefix("Sir");
		
		Assert.assertTrue(pn.compareTo(other) < 0);
	}
	
	/**
	 * @see PersonName#compareTo(PersonName)
	 */
	@Test
	@Verifies(value = "should return negative if other familyNameSuffix is greater", method = "compareTo(PersonName)")
	public void compareTo_shouldReturnNegativeIfOtherFamilyNameSuffixIsGreater() throws Exception {
		PersonName pn = new PersonName();
		pn.setFamilyNameSuffix("Jr");
		PersonName other = new PersonName();
		other.setFamilyNameSuffix("Sr");
		
		Assert.assertTrue(pn.compareTo(other) < 0);
	}
	
	/**
	 * @see PersonName#compareTo(PersonName)
	 */
	@Test
	@Verifies(value = "should return negative if other dateCreated is greater", method = "compareTo(PersonName)")
	public void compareTo_shouldReturnNegativeIfOtherDateCreatedIsGreater() throws Exception {
		PersonName pn = new PersonName();
		pn.setDateCreated(new Date());
		PersonName other = new PersonName();
		other.setDateCreated(new Date(pn.getDateCreated().getTime() + 1000));
		
		Assert.assertTrue(pn.compareTo(other) < 0);
	}
	
	/**
	 * @see {@link PersonName#equalsContent(PersonName)}
	 */
	@Test
	@Verifies(value = "should return true if given middle and family name are equal", method = "equalsContent(PersonName)")
	public void equalsContent_shouldReturnTrueIfGivenMiddleAndFamilyNameAreEqual() throws Exception {
		PersonName pn = new PersonName(1); // a different person name id than below
		pn.setGivenName("Adam");
		pn.setMiddleName("Alex");
		pn.setFamilyName("Jones");
		PersonName other = new PersonName(2); // a different person name id than above
		other.setGivenName("Adam");
		other.setMiddleName("Alex");
		other.setFamilyName("Jones");
		
		Assert.assertTrue(pn.equalsContent(other));
	}
	
	/**
	 * @see {@link PersonName#getFamilyName()}
	 */
	@Test
	@Verifies(value = "should return obscured name if obscure_patients is set to true", method = "getFamilyName()")
	public void getFamilyName_shouldReturnObscuredNameIfObscure_patientsIsSetToTrue() throws Exception {
		OpenmrsConstants.OBSCURE_PATIENTS = true;
		
		OpenmrsConstants.OBSCURE_PATIENTS_FAMILY_NAME = "family name";
		Assert.assertEquals("family name", new PersonName().getFamilyName());
		
		OpenmrsConstants.OBSCURE_PATIENTS = false; // cleanup 
	}
	
	/**
	 * @see {@link PersonName#getFamilyName2()}
	 */
	@Test
	@Verifies(value = "should return null if obscure_patients is set to true", method = "getFamilyName2()")
	public void getFamilyName2_shouldReturnNullIfObscure_patientsIsSetToTrue() throws Exception {
		OpenmrsConstants.OBSCURE_PATIENTS = true;
		
		PersonName pn = new PersonName();
		pn.setFamilyName2("a non-null name");
		Assert.assertNull(pn.getFamilyName2());
		
		OpenmrsConstants.OBSCURE_PATIENTS = false; // cleanup 
	}
	
	/**
	 * @see {@link PersonName#getFamilyNamePrefix()}
	 */
	@Test
	@Verifies(value = "should return null if obscure_patients is set to true", method = "getFamilyNamePrefix()")
	public void getFamilyNamePrefix_shouldReturnNullIfObscure_patientsIsSetToTrue() throws Exception {
		OpenmrsConstants.OBSCURE_PATIENTS = true;
		
		PersonName pn = new PersonName();
		pn.setFamilyNamePrefix("a non-null name");
		Assert.assertNull(pn.getFamilyNamePrefix());
		
		OpenmrsConstants.OBSCURE_PATIENTS = false; // cleanup 
	}
	
	/**
	 * @see {@link PersonName#getFamilyNameSuffix()}
	 */
	@Test
	@Verifies(value = "should return null if obscure_patients is set to true", method = "getFamilyNameSuffix()")
	public void getFamilyNameSuffix_shouldReturnNullIfObscure_patientsIsSetToTrue() throws Exception {
		OpenmrsConstants.OBSCURE_PATIENTS = true;
		
		PersonName pn = new PersonName();
		pn.setFamilyNameSuffix("a non-null name");
		Assert.assertNull(pn.getFamilyNameSuffix());
		
		OpenmrsConstants.OBSCURE_PATIENTS = false; // cleanup 
	}
	
	/**
	 * @see {@link PersonName#getGivenName()}
	 */
	@Test
	@Verifies(value = "should return obscured name if obscure_patients is set to true", method = "getGivenName()")
	public void getGivenName_shouldReturnObscuredNameIfObscure_patientsIsSetToTrue() throws Exception {
		OpenmrsConstants.OBSCURE_PATIENTS = true;
		
		OpenmrsConstants.OBSCURE_PATIENTS_GIVEN_NAME = "given name";
		Assert.assertEquals("given name", new PersonName().getGivenName());
		
		OpenmrsConstants.OBSCURE_PATIENTS = false; // cleanup 
	}
	
	/**
	 * @see {@link PersonName#getMiddleName()}
	 */
	@Test
	@Verifies(value = "should return obscured name if obscure_patients is set to true", method = "getMiddleName()")
	public void getMiddleName_shouldReturnObscuredNameIfObscure_patientsIsSetToTrue() throws Exception {
		OpenmrsConstants.OBSCURE_PATIENTS = true;
		
		OpenmrsConstants.OBSCURE_PATIENTS_MIDDLE_NAME = "middle name";
		Assert.assertEquals("middle name", new PersonName().getMiddleName());
		
		OpenmrsConstants.OBSCURE_PATIENTS = false; // cleanup 
	}
	
	/**
	 * @see {@link PersonName#getPrefix()}
	 */
	@Test
	@Verifies(value = "should return null if obscure_patients is set to true", method = "getPrefix()")
	public void getPrefix_shouldReturnNullIfObscure_patientsIsSetToTrue() throws Exception {
		OpenmrsConstants.OBSCURE_PATIENTS = true;
		
		PersonName pn = new PersonName();
		pn.setPrefix("a non-null name");
		Assert.assertNull(pn.getPrefix());
		
		OpenmrsConstants.OBSCURE_PATIENTS = false; // cleanup 
	}
	
}
