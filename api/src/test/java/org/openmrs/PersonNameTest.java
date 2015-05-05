/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs;

import java.util.Date;

import junit.framework.Assert;

import org.junit.Test;
import org.openmrs.test.Verifies;
import org.openmrs.util.OpenmrsConstants;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * This class should test all methods on the PersonName object This class does not touch the
 * database, so it does not need to extend the normal openmrs BaseTest
 */
public class PersonNameTest {
	
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
	@Verifies(value = "should return true if all fields other than ID, person and preferred are equal", method = "equalsContent(PersonName)")
	public void equalsContent_shouldReturnTrueIfAllFieldsOtherThanIdPersonAndPreferredAreEqual() throws Exception {
		PersonName pn = new PersonName(1);
		pn.setPrefix("Count");
		pn.setGivenName("Adam");
		pn.setMiddleName("Alex");
		pn.setFamilyNamePrefix("family prefix");
		pn.setFamilyName("Jones");
		pn.setFamilyName2("Howard");
		pn.setFamilyNameSuffix("Jr.");
		pn.setDegree("Dr.");
		pn.setPreferred(true);
		pn.setPerson(new Person(999));
		
		PersonName other = new PersonName(2);
		other.setPrefix("Count");
		other.setGivenName("Adam");
		other.setMiddleName("Alex");
		other.setFamilyNamePrefix("family prefix");
		other.setFamilyName("Jones");
		other.setFamilyName2("Howard");
		other.setFamilyNameSuffix("Jr.");
		other.setDegree("Dr.");
		other.setPreferred(false);
		other.setPerson(new Person(111));
		
		assertThat(pn.equalsContent(other), is(true));
	}
	
	/**
	 * @see {@link PersonName#equalsContent(PersonName)}
	 */
	@Test
	@Verifies(value = "should return false if suffixes are not equal", method = "equalsContent(PersonName)")
	public void equalsContent_shouldReturnFalseIfSuffixesAreNotEqual() throws Exception {
		PersonName nameWithSenior = new PersonName(1);
		PersonName nameWithJunior = new PersonName(2);
		
		nameWithSenior.setFamilyNameSuffix("Sr.");
		nameWithJunior.setFamilyNameSuffix("Jr.");
		
		assertThat(nameWithSenior.equalsContent(nameWithJunior), is(false));
	}
	
	/**
	 * @see {@link PersonName#equalsContent(PersonName)}
	 */
	@Test
	@Verifies(value = "should return false if family name prefixes are not equal", method = "equalsContent(PersonName)")
	public void equalsContent_shouldReturnFalseIfPrefixesAreNotEqual() throws Exception {
		PersonName nameWithVanDer = new PersonName(1);
		PersonName nameWithDe = new PersonName(2);
		
		nameWithVanDer.setFamilyNamePrefix("van der");
		nameWithDe.setFamilyNamePrefix("de");
		
		assertThat(nameWithVanDer.equalsContent(nameWithDe), is(false));
	}
	
	/**
	 * @see {@link PersonName#equalsContent(PersonName)}
	 */
	@Test
	@Verifies(value = "should return false if family name 2 is not equal", method = "equalsContent(PersonName)")
	public void equalsContent_shouldReturnFalseIfFamilyName2IsNotEqual() throws Exception {
		PersonName name1 = new PersonName(1);
		PersonName name2 = new PersonName(2);
		
		name1.setFamilyName2("van der");
		name2.setFamilyName2("de");
		
		assertThat(name1.equalsContent(name2), is(false));
	}
	
	/**
	 * @see {@link PersonName#equalsContent(PersonName)}
	 */
	@Test
	@Verifies(value = "should return false if prefix is not equal", method = "equalsContent(PersonName)")
	public void equalsContent_shouldReturnFalseIfPrefixIsNotEqual() throws Exception {
		PersonName name1 = new PersonName(1);
		PersonName name2 = new PersonName(2);
		
		name1.setPrefix("count");
		name2.setPrefix("baron");
		
		assertThat(name1.equalsContent(name2), is(false));
	}
	
	/**
	 * @see {@link PersonName#equalsContent(PersonName)}
	 */
	@Test
	@Verifies(value = "should return false if degrees are not equal", method = "equalsContent(PersonName)")
	public void equalsContent_shouldReturnFalseIfDegreesAreNotEqual() throws Exception {
		PersonName nameWithDoctor = new PersonName(1);
		PersonName nameWithProfessor = new PersonName(2);
		
		nameWithDoctor.setDegree("Dr.");
		nameWithProfessor.setFamilyNameSuffix("Prof.");
		
		assertThat(nameWithDoctor.equalsContent(nameWithProfessor), is(false));
	}
	
	/**
	 * @see {@link PersonName#equalsContent(PersonName)}
	 */
	@Test
	@Verifies(value = "should return true if only difference in content fields is between null and empty string", method = "equalsContent(PersonName)")
	public void equalsContent_shouldReturnTrueIfOnlyInContentFieldsDifferenceIsBetweenNullAndEmptyString() throws Exception {
		PersonName pn = new PersonName(1);
		pn.setPrefix("");
		pn.setGivenName("");
		pn.setMiddleName("");
		pn.setFamilyNamePrefix("");
		pn.setFamilyName("");
		pn.setFamilyName2("");
		pn.setFamilyNameSuffix("");
		pn.setDegree("");
		pn.setPreferred(true);
		pn.setPerson(new Person(999));
		
		PersonName other = new PersonName(2);
		other.setPrefix(null);
		other.setGivenName(null);
		other.setMiddleName(null);
		other.setFamilyNamePrefix(null);
		other.setFamilyName(null);
		other.setFamilyName2(null);
		other.setFamilyNameSuffix(null);
		other.setDegree(null);
		other.setPreferred(false);
		other.setPerson(new Person(111));
		
		assertThat(pn.equalsContent(other), is(true));
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
	
	/**
	 * @see PersonName#getFullName()
	 * @verifies not put spaces around an empty middle name
	 */
	@Test
	public void getFullName_shouldNotPutSpacesAroundAnEmptyMiddleName() throws Exception {
		PersonName pn = new PersonName();
		pn.setGivenName("Bob");
		pn.setMiddleName("");
		pn.setFamilyName("Jones");
		Assert.assertEquals("Bob Jones", pn.getFullName());
	}
	
	/**
	 * @see {@link PersonName#getFullName()}
	 */
	@Test
	@Verifies(value = "should Not Return Long If Person Name Format Is Short", method = "getFullName()")
	public void getFullName_shouldNotReturnLongIfPersonNameFormatIsShort() throws Exception {
		PersonName pn = new PersonName();
		PersonName.setFormat(OpenmrsConstants.PERSON_NAME_FORMAT_LONG);
		pn.setPrefix("Sr.");
		pn.setGivenName("Taylor");
		pn.setMiddleName("Bob");
		pn.setFamilyNamePrefix("Wilson");
		pn.setFamilyName("Mark");
		pn.setFamilyName2("Jones");
		pn.setFamilyNameSuffix("jr.");
		pn.setDegree("3");
		PersonName.setFormat(OpenmrsConstants.PERSON_NAME_FORMAT_SHORT);
		Assert.assertEquals(pn.getFullName(), "Sr. Taylor Bob Mark");
	}
	
	@Test
	@Verifies(value = "should Not Return Short If Person Name Format Is Long", method = "getFullName()")
	public void getFullName_shouldNotReturnShortIfPersonNameFormatIsLong() throws Exception {
		PersonName pn = new PersonName();
		PersonName.setFormat(OpenmrsConstants.PERSON_NAME_FORMAT_LONG);
		pn.setPrefix("Sr.");
		pn.setGivenName("Taylor");
		pn.setMiddleName("Bob");
		pn.setFamilyNamePrefix("Wilson");
		pn.setFamilyName("Mark");
		pn.setFamilyName2("Jones");
		pn.setFamilyNameSuffix("jr.");
		pn.setDegree("3");
		Assert.assertEquals(pn.getFullName(), "Sr. Taylor Bob Wilson Mark Jones jr. 3");
	}
	
	@Test
	@Verifies(value = "should Return Short If Person Name Format Is null", method = "getFullName()")
	public void getFullName_shouldReturnShortIfPersonNameFormatIsNull() throws Exception {
		PersonName pn = new PersonName();
		PersonName.setFormat(OpenmrsConstants.PERSON_NAME_FORMAT_LONG);
		pn.setPrefix("Sr.");
		pn.setGivenName("Taylor");
		pn.setMiddleName("Bob");
		pn.setFamilyNamePrefix("Wilson");
		pn.setFamilyName("Mark");
		pn.setFamilyName2("Jones");
		pn.setFamilyNameSuffix("jr.");
		pn.setDegree("3");
		PersonName.setFormat("");
		Assert.assertEquals(pn.getFullName(), "Sr. Taylor Bob Mark");
	}
	
}
