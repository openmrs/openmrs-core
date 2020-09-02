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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;


import org.junit.jupiter.api.Test;
import org.openmrs.util.OpenmrsConstants;

/**
 * This class should test all methods on the PersonName object This class does not touch the
 * database, so it does not need to extend the normal openmrs BaseTest
 */
public class PersonNameTest {
	
	/**
	 * @see PersonName#newInstance(PersonName)
	 */
	@Test
	public void newInstance_shouldCopyEveryPropertyOfGivenPersonName() {
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
		
		assertEquals(personNameId, copy.getPersonNameId());
		assertEquals(preferred, copy.getPreferred());
		assertEquals(prefix, copy.getPrefix());
		assertEquals(person, copy.getPerson());
		assertEquals(givenName, copy.getGivenName());
		assertEquals(middleName, copy.getMiddleName());
		assertEquals(familyNamePrefix, copy.getFamilyNamePrefix());
		assertEquals(familyName, copy.getFamilyName());
		assertEquals(familyName2, copy.getFamilyName2());
		assertEquals(familyNameSuffix, copy.getFamilyNameSuffix());
		assertEquals(degree, copy.getDegree());
		assertEquals(voided, copy.getVoided());
		assertEquals(voidedBy, copy.getVoidedBy());
		assertEquals(voidReason, copy.getVoidReason());
	}

	/**
	 * @see PersonName#equalsContent(PersonName)
	 */
	@Test
	public void equalsContent_shouldReturnTrueIfAllFieldsOtherThanIdPersonAndPreferredAreEqual() {
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
	 * @see PersonName#equalsContent(PersonName)
	 */
	@Test
	public void equalsContent_shouldReturnFalseIfSuffixesAreNotEqual() {
		PersonName nameWithSenior = new PersonName(1);
		PersonName nameWithJunior = new PersonName(2);
		
		nameWithSenior.setFamilyNameSuffix("Sr.");
		nameWithJunior.setFamilyNameSuffix("Jr.");
		
		assertThat(nameWithSenior.equalsContent(nameWithJunior), is(false));
	}
	
	/**
	 * @see PersonName#equalsContent(PersonName)
	 */
	@Test
	public void equalsContent_shouldReturnFalseIfPrefixesAreNotEqual() {
		PersonName nameWithVanDer = new PersonName(1);
		PersonName nameWithDe = new PersonName(2);
		
		nameWithVanDer.setFamilyNamePrefix("van der");
		nameWithDe.setFamilyNamePrefix("de");
		
		assertThat(nameWithVanDer.equalsContent(nameWithDe), is(false));
	}
	
	/**
	 * @see PersonName#equalsContent(PersonName)
	 */
	@Test
	public void equalsContent_shouldReturnFalseIfFamilyName2IsNotEqual() {
		PersonName name1 = new PersonName(1);
		PersonName name2 = new PersonName(2);
		
		name1.setFamilyName2("van der");
		name2.setFamilyName2("de");
		
		assertThat(name1.equalsContent(name2), is(false));
	}
	
	/**
	 * @see PersonName#equalsContent(PersonName)
	 */
	@Test
	public void equalsContent_shouldReturnFalseIfPrefixIsNotEqual() {
		PersonName name1 = new PersonName(1);
		PersonName name2 = new PersonName(2);
		
		name1.setPrefix("count");
		name2.setPrefix("baron");
		
		assertThat(name1.equalsContent(name2), is(false));
	}
	
	/**
	 * @see PersonName#equalsContent(PersonName)
	 */
	@Test
	public void equalsContent_shouldReturnFalseIfDegreesAreNotEqual() {
		PersonName nameWithDoctor = new PersonName(1);
		PersonName nameWithProfessor = new PersonName(2);
		
		nameWithDoctor.setDegree("Dr.");
		nameWithProfessor.setFamilyNameSuffix("Prof.");
		
		assertThat(nameWithDoctor.equalsContent(nameWithProfessor), is(false));
	}
	
	/**
	 * @see PersonName#equalsContent(PersonName)
	 */
	@Test
	public void equalsContent_shouldReturnTrueIfOnlyInContentFieldsDifferenceIsBetweenNullAndEmptyString() {
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
	 * @see PersonName#getFamilyName()
	 */
	@Test
	public void getFamilyName_shouldReturnObscuredNameIfObscure_patientsIsSetToTrue() {
		OpenmrsConstants.OBSCURE_PATIENTS = true;
		
		OpenmrsConstants.OBSCURE_PATIENTS_FAMILY_NAME = "family name";
		assertEquals("family name", new PersonName().getFamilyName());
		
		OpenmrsConstants.OBSCURE_PATIENTS = false; // cleanup 
	}
	
	/**
	 * @see PersonName#getFamilyName2()
	 */
	@Test
	public void getFamilyName2_shouldReturnNullIfObscure_patientsIsSetToTrue() {
		OpenmrsConstants.OBSCURE_PATIENTS = true;
		
		PersonName pn = new PersonName();
		pn.setFamilyName2("a non-null name");
		assertNull(pn.getFamilyName2());
		
		OpenmrsConstants.OBSCURE_PATIENTS = false; // cleanup 
	}
	
	/**
	 * @see PersonName#getFamilyNamePrefix()
	 */
	@Test
	public void getFamilyNamePrefix_shouldReturnNullIfObscure_patientsIsSetToTrue() {
		OpenmrsConstants.OBSCURE_PATIENTS = true;
		
		PersonName pn = new PersonName();
		pn.setFamilyNamePrefix("a non-null name");
		assertNull(pn.getFamilyNamePrefix());
		
		OpenmrsConstants.OBSCURE_PATIENTS = false; // cleanup 
	}
	
	/**
	 * @see PersonName#getFamilyNameSuffix()
	 */
	@Test
	public void getFamilyNameSuffix_shouldReturnNullIfObscure_patientsIsSetToTrue() {
		OpenmrsConstants.OBSCURE_PATIENTS = true;
		
		PersonName pn = new PersonName();
		pn.setFamilyNameSuffix("a non-null name");
		assertNull(pn.getFamilyNameSuffix());
		
		OpenmrsConstants.OBSCURE_PATIENTS = false; // cleanup 
	}
	
	/**
	 * @see PersonName#getGivenName()
	 */
	@Test
	public void getGivenName_shouldReturnObscuredNameIfObscure_patientsIsSetToTrue() {
		OpenmrsConstants.OBSCURE_PATIENTS = true;
		
		OpenmrsConstants.OBSCURE_PATIENTS_GIVEN_NAME = "given name";
		assertEquals("given name", new PersonName().getGivenName());
		
		OpenmrsConstants.OBSCURE_PATIENTS = false; // cleanup 
	}
	
	/**
	 * @see PersonName#getMiddleName()
	 */
	@Test
	public void getMiddleName_shouldReturnObscuredNameIfObscure_patientsIsSetToTrue() {
		OpenmrsConstants.OBSCURE_PATIENTS = true;
		
		OpenmrsConstants.OBSCURE_PATIENTS_MIDDLE_NAME = "middle name";
		assertEquals("middle name", new PersonName().getMiddleName());
		
		OpenmrsConstants.OBSCURE_PATIENTS = false; // cleanup 
	}
	
	/**
	 * @see PersonName#getPrefix()
	 */
	@Test
	public void getPrefix_shouldReturnNullIfObscure_patientsIsSetToTrue() {
		OpenmrsConstants.OBSCURE_PATIENTS = true;
		
		PersonName pn = new PersonName();
		pn.setPrefix("a non-null name");
		assertNull(pn.getPrefix());
		
		OpenmrsConstants.OBSCURE_PATIENTS = false; // cleanup 
	}
	
	/**
	 * @see PersonName#getFullName()
	 */
	@Test
	public void getFullName_shouldNotPutSpacesAroundAnEmptyMiddleName() {
		PersonName pn = new PersonName();
		pn.setGivenName("Bob");
		pn.setMiddleName("");
		pn.setFamilyName("Jones");
		assertEquals("Bob Jones", pn.getFullName());
	}
	
	/**
	 * @see PersonName#getFullName()
	 */
	@Test
	public void getFullName_shouldNotReturnLongIfPersonNameFormatIsShort() {
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
		assertEquals(pn.getFullName(), "Sr. Taylor Bob Mark");
	}
	
	@Test
	public void getFullName_shouldNotReturnShortIfPersonNameFormatIsLong() {
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
		assertEquals(pn.getFullName(), "Sr. Taylor Bob Wilson Mark Jones jr. 3");
	}
	
	@Test
	public void getFullName_shouldReturnShortIfPersonNameFormatIsNull() {
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
		assertEquals(pn.getFullName(), "Sr. Taylor Bob Mark");
	}
	
}
