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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;

/**
 * This class should test all methods on the person object.<br>
 * <br>
 * This class does not touch the database, so it does not need to extend the normal openmrs BaseTest
 */
public class PersonTest extends BaseContextSensitiveTest {
	
	/**
	 * Test the add/removeAddresses method in the person object
	 *
	 */
	@Test
	public void shouldAddRemoveAddress() {
		
		Person p = new Person();
		
		assertNotNull(p.getAddresses());
		
		PersonAddress pa1 = new PersonAddress();
		
		pa1.setAddress1("firsttest");
		pa1.setAddress2("firsttest2");
		pa1.setDateCreated(new Date());
		pa1.setVoided(false);
		p.addAddress(pa1);
		
		// make sure the address is added.
		assertThat(p.getAddresses(), hasSize(1));
		
		// adding the same address should not increment the size
		p.addAddress(pa1);
		assertThat(p.getAddresses(), hasSize(1));
		
		PersonAddress pa2 = new PersonAddress();
		pa2.setAddress1("secondtest");
		pa2.setAddress2("secondtest2");
		pa2.setVoided(false);
		
		p.addAddress(pa2);
		
		// make sure the address is added
		assertThat(p.getAddresses(), hasSize(2));
		
		PersonAddress pa3 = new PersonAddress();
		pa3.setAddress1(pa1.getAddress1());
		pa3.setAddress2(pa1.getAddress2());
		pa3.setDateCreated(pa1.getDateCreated());
		pa3.setVoided(false);
		
		p.addAddress(pa3);
		// make sure the address is NOT added
		assertThat(p.getAddresses(), hasSize(2));
		
		pa3.setVoided(true);
		p.addAddress(pa3);
		// make sure the address IS added
		assertThat(p.getAddresses(), hasSize(3));
		
		p.removeAddress(pa3);
		assertThat(p.getAddresses(), hasSize(2));
		
		pa3.setDateCreated(new Date(pa1.getDateCreated().getTime() + 1));
		p.addAddress(pa3);
		// make sure the address IS added
		assertThat(p.getAddresses(), hasSize(3));
		
		// test removing all of the addresses
		p.removeAddress(pa3);
		assertThat(p.getAddresses(), hasSize(2));
		p.removeAddress(pa2);
		assertThat(p.getAddresses(), hasSize(1));
		p.removeAddress(pa2);
		assertThat(p.getAddresses(), hasSize(1));
		p.removeAddress(pa1);
		assertThat(p.getAddresses(), hasSize(0));
	}
	
	/**
	 * Test the add/removeNames method in the person object
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldAddRemoveName() {
		
		Person p = new Person();
		
		assertNotNull(p.getNames());
		
		PersonName pa1 = new PersonName();
		
		pa1.setGivenName("firsttest");
		pa1.setFamilyName("firsttest2");
		pa1.setDateCreated(new Date());
		pa1.setVoided(false);
		p.addName(pa1);
		
		// make sure the name is added.
		assertThat(p.getNames(), hasSize(1));
		
		// adding the same name should not increment the size
		p.addName(pa1);
		assertThat(p.getNames(), hasSize(1));
		
		PersonName pa2 = new PersonName();
		pa2.setGivenName("secondtest");
		pa2.setFamilyName("secondtest2");
		pa2.setVoided(false);
		
		p.addName(pa2);
		
		// make sure the name is added
		assertThat(p.getNames(), hasSize(2));
		
		PersonName pa3 = new PersonName();
		pa3.setGivenName(pa1.getGivenName());
		pa3.setFamilyName(pa1.getFamilyName());
		pa3.setDateCreated(pa1.getDateCreated());
		pa3.setVoided(false);
		
		p.addName(pa3);
		// make sure the name is NOT added because its the same as pa1
		assertThat(p.getNames(), hasSize(2));
		
		PersonName pa4 = new PersonName();
		pa4.setGivenName(pa1.getGivenName() + "string to change the .equals method");
		pa4.setFamilyName(pa1.getFamilyName());
		pa4.setDateCreated(pa1.getDateCreated());
		pa4.setVoided(false);
		pa4.setVoided(true);
		p.addName(pa4);
		// make sure a voided name IS added
		assertThat(p.getNames(), hasSize(3));
		
		p.removeName(pa3);
		assertThat(p.getNames(), hasSize(2));
		
		pa3.setDateCreated(new Date(pa1.getDateCreated().getTime() + 1));
		p.addName(pa3);
		// make sure the name IS added
		assertThat(p.getNames(), hasSize(3));
		
		// test removing all of the names
		p.removeName(pa4);
		assertThat(p.getNames(), hasSize(2));
		p.removeName(pa3); // pa3 was never added, but is the same as pa1
		assertThat(p.getNames(), hasSize(1));
		p.removeName(pa2);
		assertThat(p.getNames(), hasSize(0));
		p.removeName(pa2);
		assertThat(p.getNames(), hasSize(0));
		p.removeName(pa1);
		assertThat(p.getNames(), hasSize(0));
	}
	
	/**
	 * Test the add/removeAttributes method in the person object
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldAddRemoveAttribute() {
		
		Person p = new Person();
		
		assertNotNull(p.getAttributes());
		
		PersonAttribute pa1 = new PersonAttribute();
		
		pa1.setValue("firsttest");
		pa1.setAttributeType(new PersonAttributeType(1));
		pa1.setDateCreated(new Date());
		pa1.setVoided(false);
		p.addAttribute(pa1);
		
		// make sure the attribute is added.
		assertThat(p.getAttributes(), hasSize(1));
		
		// adding the same attribute should not increment the size
		p.addAttribute(pa1);
		assertThat(p.getAttributes(), hasSize(1));
		
		PersonAttribute pa2 = new PersonAttribute();
		pa2.setValue("secondtest");
		pa2.setAttributeType(new PersonAttributeType(2));
		pa2.setVoided(false);
		
		p.addAttribute(pa2);
		
		// make sure the attribute is added
		assertThat(p.getAttributes(), hasSize(2));
		
		PersonAttribute pa3 = new PersonAttribute();
		pa3.setValue(pa1.getValue());
		pa3.setAttributeType(pa1.getAttributeType());
		pa3.setDateCreated(pa1.getDateCreated());
		pa3.setVoided(false);
		
		p.addAttribute(pa3);
		// make sure the attribute is NOT added
		assertThat(p.getAttributes(), hasSize(2));
		
		// (we must change the value here as well, because logic says that there
		// is no
		// point in adding an attribute that has the same value/type...even if
		// the void
		// status is different)
		pa3.setValue(pa1.getValue() + "addition to make sure the value is different");
		pa3.setVoided(true);
		p.addAttribute(pa3);
		// make sure the attribute IS added
		assertThat(p.getAttributes(), hasSize(3));
		
		p.removeAttribute(pa3);
		assertThat(p.getAttributes(), hasSize(2));
		
		pa3.setDateCreated(new Date(pa1.getDateCreated().getTime() + 1));
		p.addAttribute(pa3);
		// make sure the attribute IS added
		assertThat(p.getAttributes(), hasSize(3));
		
		// test removing all of the attributes
		p.removeAttribute(pa3);
		assertThat(p.getAttributes(), hasSize(2));
		p.removeAttribute(pa2);
		assertThat(p.getAttributes(), hasSize(1));
		p.removeAttribute(pa2);
		assertThat(p.getAttributes(), hasSize(1));
		p.removeAttribute(pa1);
		assertThat(p.getAttributes(), hasSize(0));
	}
	
	/**
	 * Test that setting a person's age correctly sets their birth date and records that this is
	 * inexact
	 * 
	 * @throws ParseException
	 * @throws Exception
	 */
	@Test
	public void shouldSetInexactBirthdateFromAge() throws ParseException {
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		Person p = new Person();
		
		// Test that default values are correct
		assertNull(p.getAge());
		assertFalse(p.getBirthdateEstimated());
		
		// Test standard case and ensure estimated field is set to true
		p.setBirthdateFromAge(10, df.parse("2008-05-20"));
		assertEquals(p.getBirthdate(), df.parse("1998-01-01"));
		assertTrue(p.getBirthdateEstimated());
		
		// Test boundary cases
		p.setBirthdateFromAge(52, df.parse("2002-01-01"));
		assertEquals(p.getBirthdate(), df.parse("1950-01-01"));
		p.setBirthdateFromAge(35, df.parse("2004-12-31"));
		assertEquals(p.getBirthdate(), df.parse("1969-01-01"));
		p.setBirthdateFromAge(0, df.parse("2008-05-20"));
		assertEquals(p.getBirthdate(), df.parse("2008-01-01"));
	}
	
	/**
	 * @see Person#getAge(Date)
	 */
	@Test
	public void getAge_shouldGetAgeAfterBirthday() {
		Calendar birthdate = Calendar.getInstance();
		birthdate.set(2006, Calendar.JUNE, 2);
		Calendar onDate = Calendar.getInstance();
		onDate.set(2008, Calendar.JUNE, 3);
		Person person = new Person();
		person.setBirthdate(birthdate.getTime());
		assertEquals(person.getAge(onDate.getTime()), 2, 0);
	}
	
	/**
	 * @see Person#getAge(Date)
	 */
	@Test
	public void getAge_shouldGetAgeBeforeBirthday() {
		Calendar birthdate = Calendar.getInstance();
		birthdate.set(2006, Calendar.JUNE, 2);
		Calendar onDate = Calendar.getInstance();
		onDate.set(2008, Calendar.JUNE, 1);
		Person person = new Person();
		person.setBirthdate(birthdate.getTime());
		assertEquals(person.getAge(onDate.getTime()), 1, 0);
	}
	
	/**
	 * @see Person#getAge(Date)
	 */
	@Test
	public void getAge_shouldGetAgeOnBirthdayWithMinutesDefined() {
		Calendar birthdate = Calendar.getInstance();
		birthdate.set(2006, Calendar.JUNE, 2, 9, 9, 9);
		Calendar onDate = Calendar.getInstance();
		onDate.set(2008, Calendar.JUNE, 2, 7, 7, 7);
		Person person = new Person();
		person.setBirthdate(birthdate.getTime());
		assertEquals(person.getAge(onDate.getTime()), 2, 0);
	}
	
	/**
	 * @see Person#getAge(Date)
	 */
	@Test
	public void getAge_shouldGetAgeOnBirthdayWithNoMinutesDefined() {
		Calendar birthdate = Calendar.getInstance();
		birthdate.set(2006, Calendar.JUNE, 2);
		Calendar onDate = Calendar.getInstance();
		onDate.set(2008, Calendar.JUNE, 2);
		Person person = new Person();
		person.setBirthdate(birthdate.getTime());
		assertEquals(person.getAge(onDate.getTime()), 2, 0);
	}
	
	/**
	 * @see Person#getAge()
	 */
	@Test
	public void getAge_shouldGetAgeAfterDeath() {
		Calendar birthdate = Calendar.getInstance();
		birthdate.set(1990, Calendar.JUNE, 2);
		Calendar deathDate = Calendar.getInstance();
		deathDate.set(2000, Calendar.JUNE, 3);
		Person person = new Person();
		person.setBirthdate(birthdate.getTime());
		person.setDead(true);
		person.setDeathDate(deathDate.getTime());
		assertEquals(10, person.getAge(), 0);
	}
	
	/**
	 * @see Person#getAge(Date)
	 */
	@Test
	public void getAge_shouldGetAgeWithGivenDateAfterDeath() {
		Calendar birthdate = Calendar.getInstance();
		birthdate.set(1990, Calendar.JUNE, 2);
		Calendar deathDate = Calendar.getInstance();
		deathDate.set(2000, Calendar.JUNE, 3);
		Calendar givenDate = Calendar.getInstance();
		givenDate.set(2010, Calendar.JUNE, 3);
		Person person = new Person();
		person.setBirthdate(birthdate.getTime());
		person.setDead(true);
		person.setDeathDate(deathDate.getTime());
		assertEquals(10, person.getAge(givenDate.getTime()), 0);
	}
	
	/**
	 * @see Person#getAge(Date)
	 */
	@Test
	public void getAge_shouldGetAgeWithGivenDateBeforeDeath() {
		Calendar birthdate = Calendar.getInstance();
		birthdate.set(1990, Calendar.JUNE, 2);
		Calendar deathDate = Calendar.getInstance();
		deathDate.set(2000, Calendar.JUNE, 3);
		Calendar givenDate = Calendar.getInstance();
		givenDate.set(1995, Calendar.JUNE, 3);
		Person person = new Person();
		person.setBirthdate(birthdate.getTime());
		person.setDead(true);
		person.setDeathDate(deathDate.getTime());
		assertEquals(5, person.getAge(givenDate.getTime()), 0);
	}
	
	/**
	 * @see Person#getAge(Date)
	 */
	@Test
	public void getAge_shouldGetAgeWithGivenDateBeforeBirth() {
		Calendar birthdate = Calendar.getInstance();
		birthdate.set(1990, Calendar.JUNE, 2);
		Calendar deathDate = Calendar.getInstance();
		deathDate.set(2000, Calendar.JUNE, 3);
		Calendar givenDate = Calendar.getInstance();
		givenDate.set(1985, Calendar.JUNE, 3);
		Person person = new Person();
		person.setBirthdate(birthdate.getTime());
		person.setDead(true);
		person.setDeathDate(deathDate.getTime());
		assertEquals(-5, person.getAge(givenDate.getTime()), 0);
	}
	
	/**
	 * @see Person#addAttribute(PersonAttribute)
	 */
	@Test
	public void addAttribute_shouldNotSaveAnAttributeWithABlankStringValue() {
		Person p = new Person();
		
		// make sure there are no initial attributes
		assertEquals(0, p.getAttributes().size(), "There should not be any attributes");
		
		PersonAttribute pa1 = new PersonAttribute();
		pa1.setValue("");
		pa1.setAttributeType(new PersonAttributeType(1));
		pa1.setVoided(false);
		p.addAttribute(pa1);
		
		// make sure the attribute was not added
		assertEquals(0, p.getAttributes().size(), "There should not be any attributes");
	}
	
	/**
	 * @see Person#addAttribute(PersonAttribute)
	 */
	@Test
	public void addAttribute_shouldNotSaveAnAttributeWithANullValue() {
		Person p = new Person();
		
		// make sure there are no initial attributes
		assertEquals(0, p.getAttributes().size(), "There should not be any attributes");
		
		PersonAttribute pa1 = new PersonAttribute();
		pa1.setValue(null);
		pa1.setAttributeType(new PersonAttributeType(1));
		pa1.setVoided(false);
		p.addAttribute(pa1);
		
		// make sure the attribute was not added
		assertEquals(0, p.getAttributes().size(), "There should not be any attributes");
	}
	
	/**
	 * @see Person#addAttribute(PersonAttribute)
	 */
	@Test
	public void addAttribute_shouldVoidOldAttributeWhenANullOrBlankStringValueIsAdded() {
		Person p = new Person();
		
		// make sure there are no initial attributes
		assertEquals(0, p.getAttributes().size(), "There should not be any attributes");
		
		PersonAttribute pa1 = new PersonAttribute();
		pa1.setValue("ack");
		PersonAttributeType attributeType = new PersonAttributeType(1);
		pa1.setAttributeType(attributeType);
		pa1.setVoided(false);
		pa1.setCreator(new User(1));
		p.addAttribute(pa1);
		
		// make sure the attribute was added
		assertEquals(1, p.getAttributes().size(), "The attribute was not added");
		
		// add another one
		PersonAttribute pa2 = new PersonAttribute();
		pa2.setValue(null);
		pa2.setAttributeType(attributeType);
		pa2.setVoided(false);
		p.addAttribute(pa2);
		
		// make sure the new attribute was not added and the old was not removed
		assertEquals(1, p.getAttributes().size(), "Something changed ...");
		
		// make sure the new attribute effectively voided the original
		assertTrue(p.getAttributes().iterator().next().getVoided(), "The original attribute is not voided");
		
	}
	
	/**
	 * @see Person#addAddress(PersonAddress)
	 */
	@Test
	public void addAddress_shouldNotAddAPersonAddressWithBlankFields() {
		Person p = new Person();
		PersonAddress pa1 = new PersonAddress();
		pa1.setAddress1("address1");
		p.addAddress(pa1);
		PersonAddress pa2 = new PersonAddress();
		pa2.setAddress1("");
		p.addAddress(pa2);
		
		assertEquals(1, p.getAddresses().size());
	}
	
	/**
	 * @see Person#getPersonAddress()
	 */
	@Test
	public void getPersonAddress_shouldGetNotvoidedPersonAddressIfPreferredAddressDoesNotExist() {
		
		// addresses
		PersonAddress voidedAddress = PersonAddressBuilder.newBuilder().withPreferred(false).withVoided(true).build();
		
		// addresses
		PersonAddress notVoidedAddress = PersonAddressBuilder.newBuilder().withPreferred(false).withVoided(false).build();

		Set<PersonAddress> personAddresses = new HashSet<>(Arrays.asList(voidedAddress, notVoidedAddress));
		
		checkGetPersonAddressResultForVoidedPerson(notVoidedAddress, personAddresses);
	}
	
	/**
	 * @see Person#getPersonAddress()
	 */
	@Test
	public void getPersonAddress_shouldGetPreferredAndNotvoidedPersonAddressIfExist() {
		
		// addresses
		PersonAddress voidedAddress = PersonAddressBuilder.newBuilder().withPreferred(false).withVoided(true).build();
		
		PersonAddress preferredNotVoidedAddress = PersonAddressBuilder.newBuilder().withPreferred(true).withVoided(false)
		        .build();

		HashSet<PersonAddress> personAddresses = new HashSet<>(Arrays.asList(voidedAddress,
		    preferredNotVoidedAddress));
		
		checkGetPersonAddressResultForVoidedPerson(preferredNotVoidedAddress, personAddresses);
		
	}
	
	/**
	 * @see Person#getPersonAddress()
	 */
	@Test
	public void getPersonAddress_shouldGetVoidedPersonAddressIfPersonIsVoidedAndNotvoidedAddressDoesNotExist() {
		
		// addresses
		PersonAddress voidedAddress1 = PersonAddressBuilder.newBuilder().withVoided(true).build();
		PersonAddress voidedAddress2 = PersonAddressBuilder.newBuilder().withVoided(true).build();
		
		Set<PersonAddress> personAddresses = new HashSet<>(Arrays.asList(voidedAddress1, voidedAddress2));
		
		Person person = new Person();
		person.setVoided(true);
		person.setAddresses(personAddresses);
		
		PersonAddress actualPersonAddress = person.getPersonAddress();
		
		assertTrue(actualPersonAddress.getVoided());
	}
	
	/**
	 * @see Person#getPersonName()
	 */
	@Test
	public void getPersonName_shouldGetNotvoidedPersonNameIfPreferredAddressDoesNotExist() {
		
		PersonName notVoidedName = PersonNameBuilder.newBuilder().withVoided(false).build();
		PersonName voidedName = PersonNameBuilder.newBuilder().withVoided(true).build();

		checkGetPersonNameResultForVoidedPerson(notVoidedName, new HashSet<>(Arrays.asList(notVoidedName,
				voidedName)));
	}
	
	/**
	 * @see Person#getPersonName()
	 */
	@Test
	public void getPersonName_shouldGetPreferredAndNotvoidedPersonNameIfExist() {
		
		PersonName preferredNotVoidedName = PersonNameBuilder.newBuilder().withPreferred(true).withVoided(false).build();
		PersonName notVoidedName = PersonNameBuilder.newBuilder().withVoided(false).build();
		PersonName voidedName = PersonNameBuilder.newBuilder().withVoided(true).build();

		checkGetPersonNameResultForVoidedPerson(preferredNotVoidedName, new HashSet<>(Arrays.asList(
		    preferredNotVoidedName, notVoidedName, voidedName)));
	}
	
	/**
	 * @see Person#getPersonName()
	 */
	@Test
	public void getPersonName_shouldGetVoidedPersonAddressIfPersonIsVoidedAndNotvoidedAddressDoesNotExist() {
		
		PersonName voidedName = PersonNameBuilder.newBuilder().withVoided(true).build();

		checkGetPersonNameResultForVoidedPerson(voidedName, new HashSet<>(Collections.singletonList(voidedName)));
		
	}
	
	/**
	 * @see Person#getPersonAddress()
	 */
	@Test
	public void getPersonAddress_shouldReturnNullIfPersonIsNotvoidedAndHaveVoidedAddress() {
		
		PersonAddress firstPersonAddress = PersonAddressBuilder.newBuilder().withVoided(true).build();
		PersonAddress secondPersonAddress = PersonAddressBuilder.newBuilder().withVoided(true).build();
		
		Person notVoidedPerson = new Person();
		notVoidedPerson.addAddress(firstPersonAddress);
		notVoidedPerson.addAddress(secondPersonAddress);
		
		assertNull(notVoidedPerson.getPersonAddress());
	}
	
	/**
	 * @see Person#getPersonName()
	 */
	@Test
	public void getPersonName_shouldReturnNullIfPersonIsNotvoidedAndHaveVoidedNames() {
		
		PersonName firstVoidedName = PersonNameBuilder.newBuilder().withVoided(true).build();
		PersonName secondVoidedName = PersonNameBuilder.newBuilder().withVoided(true).build();
		
		Person notVoidedPerson = new Person();
		notVoidedPerson.addName(firstVoidedName);
		notVoidedPerson.addName(secondVoidedName);
		
		assertNull(notVoidedPerson.getPersonName());
	}

	/**
	 * @throws ParseException
	 * @see Person#getBirthDateTime()
	 */
	@Test
	public void getBirthDateTime_shouldReturnBirthDateTimeAlongWithBirthdate() throws ParseException {
		Person person = new Person();

		person.setBirthdate(new SimpleDateFormat("yyyy-MM-dd").parse("2012-01-01"));
		person.setBirthtime(new SimpleDateFormat("HH:mm:ss").parse("11:11:11"));

		assertEquals(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2012-01-01 11:11:11"), person.getBirthDateTime());
	}

	/**
	 * @see Person#getBirthDateTime()
	 */
	@Test
	public void getBirthDateTime_shouldReturnNullIfBirthdateIsNull() {
		Person person = new Person();

		person.setBirthdate(null);
		assertNull(person.getBirthDateTime());
	}

	/**
	 * @throws ParseException
	 * @see Person#getBirthDateTime()
	 */
	@Test
	public void getBirthDateTime_shouldReturnNullIfBirthtimeIsNull() throws ParseException {
		Person person = new Person();

		person.setBirthdate(new SimpleDateFormat("yyyy-MM-dd").parse("2012-01-01"));
		person.setBirthtime(null);
		assertNull(person.getBirthDateTime());
	}

	/**
	 * @see Person#getAttribute(String)
	 */
	@Test
	public void getAttribute_shouldPersonAttributeBasedOnAttributeName() {
		Person person = personHelper(false, 1, 2, 3, "name1", "name2", "name3", "value1", "value2", "value3");
		assertEquals("name3", person.getAttribute("name3").getAttributeType().getName());
	}

	/**
	 * @see Person#getAttribute(String)
	 */
	@Test
	public void getAttribute_shouldReturnNullIfAttributeNameIsVoided() {
		Person person = personHelper(true, 1, 2, 3, "name1", "name2", "name3", "value1", "value2", "value3");
		assertNull(person.getAttribute("name3"));
	}

	/**
	 * @see Person#getAttribute(PersonAttributeType)
	 */
	@Test
	public void getAttribute_shouldReturnNullWhenExistingPersonAttributeTypeIsVoided() {
		Person person = personHelper(true, 1, 2, 3, "name1", "name2", "name3", "value1", "value2", "value3");
	 	PersonAttributeType type = new PersonAttributeType(3);
	 	type.setName("name3");
		assertNull(person.getAttribute(type));
	}

	/**
	 * @see Person#getAttribute(Integer)
	 */
	@Test
	public void getAttribute_shouldreturnPersonAttributeBasedOnAttributeTypeId() {
		Person person = personHelper(false, 1, 2, 3, "name1", "name2", "name3", "value1", "value2", "value3");
		assertEquals(new Integer(3), person.getAttribute(3).getAttributeType().getId());
	}

	/**
	 * @see Person#getAttribute(Integer)
	 */
	@Test
	public void getAttribute_shouldReturnNullWhenExistingPersonAttributeWithMatchingAttributeTypeIdIsVoided() {
		Person person = personHelper(true, 1, 2, 3, "name1", "name2", "name3", "value1", "value2", "value3");
		assertNull(person.getAttribute(3));
	}

	/**
	 * @see Person#getAttributes(String)
	 */
	@Test
	public void getAttributes_shouldReturnAllPersonAttributesWithMatchingAttributeTypeNames() {
		Person person = personHelper(false, 1, 2, 3, "name1", "name1", "name3", "value1", "value2", "value3");
		assertEquals(2, person.getAttributes("name1").size());
	}

	/**
	 * @see Person#getAttributes(Integer)
	 */
	@Test
	public void getAttributes_shouldReturnListOfPersonAttributesBasedOnAttributeTypeId() {
		Person person = personHelper(false, 1, 1, 3, "name1", "name2", "name3", "value1", "value2", "value3");
		assertEquals(2, person.getAttributes(1).size());
	}

	/**
	 * @see Person#getAttributes(Integer)
	 */
	@Test
	public void getAttributes_shouldReturnEmptyListWhenMatchingPersonAttributeByIdIsVoided() {
		Person person = personHelper(true, 1, 1, 3, "name1", "name2", "name3", "value1", "value2", "value3");
		assertEquals(0, person.getAttributes(1).size());
	}

	private Person personHelper(boolean isVoid, int attributeType1, int attributeType2, int attributeType3, String attributeName1, String attributeName2, String attributeName3, String attributeValue1, String attributeValue2, String attributeValue3) {
		Person person = new Person();

	 	PersonAttributeType type1 = new PersonAttributeType(attributeType1);
	 	PersonAttributeType type2 = new PersonAttributeType(attributeType2);
	 	PersonAttributeType type3 = new PersonAttributeType(attributeType3);
	    
	 	type1.setName(attributeName1);
	 	type2.setName(attributeName2);
	 	type3.setName(attributeName3);
	 	PersonAttribute personAttribute1 = new PersonAttribute(type1, attributeValue1);
	 	PersonAttribute personAttribute2 = new PersonAttribute(type2, attributeValue2);
	 	PersonAttribute personAttribute3 = new PersonAttribute(type3, attributeValue3);
	    
		personAttribute1.setVoided(isVoid);
		personAttribute2.setVoided(isVoid);
		personAttribute3.setVoided(isVoid);

		person.addAttribute(personAttribute1);
		person.addAttribute(personAttribute2);
		person.addAttribute(personAttribute3);

		return person;
	}

	private void checkGetPersonAddressResultForVoidedPerson(PersonAddress expectedPersonAddress,
	        Set<PersonAddress> personAddresses) {
		
		Person person = new Person();
		person.setAddresses(personAddresses);
		person.setVoided(true);
		
		PersonAddress actualPersonAddress = person.getPersonAddress();
		
		assertEquals(expectedPersonAddress, actualPersonAddress);
	}
	
	private void checkGetPersonNameResultForVoidedPerson(PersonName expectedPersonAddress, Set<PersonName> personAddresses) {
		
		Person person = new Person();
		person.setVoided(true);
		
		for (PersonName personName : personAddresses) {
			person.addName(personName);
		}
		
		PersonName actualPersonName = person.getPersonName();
		
		assertEquals(expectedPersonAddress, actualPersonName);
	}

	@Test
	public void shouldSetDeadToTrueIfSetDeathdate() {
		Person p = new Person();
		Date deathDate = new Date();
		p.setDeathDate(deathDate);
		assertTrue(p.getDead(), "Person must be dead(setDead(true)) inorder have a deathDate set for him");
	}
	
	// helper class
	private static class PersonNameBuilder {
		
		private PersonName personName;
		
		private PersonNameBuilder() {
			personName = new PersonName();
		}
		
		public static PersonNameBuilder newBuilder() {
			return new PersonNameBuilder();
		}
		
		public PersonNameBuilder withVoided(boolean voided) {
			personName.setVoided(voided);
			return this;
		}
		
		public PersonNameBuilder withPreferred(boolean preferred) {
			personName.setPreferred(preferred);
			return this;
		}
		
		public PersonName build() {
			return personName;
		}
	}
	
	// helper class
	private static class PersonAddressBuilder {
		
		private PersonAddress personAddress;
		
		private PersonAddressBuilder() {
			personAddress = new PersonAddress();
		}
		
		public static PersonAddressBuilder newBuilder() {
			return new PersonAddressBuilder();
		}
		
		public PersonAddressBuilder withVoided(boolean voided) {
			personAddress.setVoided(voided);
			return this;
		}
		
		public PersonAddressBuilder withPreferred(boolean preferred) {
			personAddress.setPreferred(preferred);
			return this;
		}
		
		public PersonAddress build() {
			return personAddress;
		}
	}
}
