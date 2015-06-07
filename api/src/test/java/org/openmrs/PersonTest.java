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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.test.Verifies;

/**
 * This class should test all methods on the person object.<br/>
 * <br/>
 * This class does not touch the database, so it does not need to extend the normal openmrs BaseTest
 */
public class PersonTest extends BaseContextSensitiveTest {
	
	/**
	 * Test the add/removeAddresses method in the person object
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldAddRemoveAddress() throws Exception {
		
		Person p = new Person();
		
		assertNotNull(p.getAddresses());
		
		PersonAddress pa1 = new PersonAddress();
		
		pa1.setAddress1("firsttest");
		pa1.setAddress2("firsttest2");
		pa1.setDateCreated(new Date());
		pa1.setVoided(false);
		p.addAddress(pa1);
		
		// make sure the address is added.
		assertTrue("There should be 1 address in the person object but there is actually : " + p.getAddresses().size(), p
		        .getAddresses().size() == 1);
		
		// adding the same address should not increment the size
		p.addAddress(pa1);
		assertTrue("There should be 1 address in the person object but there is actually : " + p.getAddresses().size(), p
		        .getAddresses().size() == 1);
		
		PersonAddress pa2 = new PersonAddress();
		pa2.setAddress1("secondtest");
		pa2.setAddress2("secondtest2");
		pa2.setVoided(false);
		
		p.addAddress(pa2);
		
		// make sure the address is added
		assertTrue("There should be 2 addresses in the person object but there is actually : " + p.getAddresses().size(), p
		        .getAddresses().size() == 2);
		
		PersonAddress pa3 = new PersonAddress();
		pa3.setAddress1(pa1.getAddress1());
		pa3.setAddress2(pa1.getAddress2());
		pa3.setDateCreated(pa1.getDateCreated());
		pa3.setVoided(false);
		
		p.addAddress(pa3);
		// make sure the address is NOT added
		assertTrue("There should be 2 addresses in the person object but there is actually : " + p.getAddresses().size(), p
		        .getAddresses().size() == 2);
		
		pa3.setVoided(true);
		p.addAddress(pa3);
		// make sure the address IS added
		assertTrue("There should be 3 addresses in the person object but there is actually : " + p.getAddresses().size(), p
		        .getAddresses().size() == 3);
		
		p.removeAddress(pa3);
		assertTrue("There should be only 2 address in the person object now", p.getAddresses().size() == 2);
		
		pa3.setDateCreated(new Date(pa1.getDateCreated().getTime() + 1));
		p.addAddress(pa3);
		// make sure the address IS added
		assertTrue("There should be 3 addresses in the person object but there is actually : " + p.getAddresses().size(), p
		        .getAddresses().size() == 3);
		
		// test removing all of the addresses
		p.removeAddress(pa3);
		assertTrue("There should be only 2 address in the person object now", p.getAddresses().size() == 2);
		p.removeAddress(pa2);
		assertTrue("There should be only 1 address in the person object now", p.getAddresses().size() == 1);
		p.removeAddress(pa2);
		assertTrue("There should still be only 1 address in the person object now", p.getAddresses().size() == 1);
		p.removeAddress(pa1);
		assertTrue("There shouldn't be any addresses in the person object now", p.getAddresses().size() == 0);
	}
	
	/**
	 * Test the add/removeNames method in the person object
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldAddRemoveName() throws Exception {
		
		Person p = new Person();
		
		assertNotNull(p.getNames());
		
		PersonName pa1 = new PersonName();
		
		pa1.setGivenName("firsttest");
		pa1.setFamilyName("firsttest2");
		pa1.setDateCreated(new Date());
		pa1.setVoided(false);
		p.addName(pa1);
		
		// make sure the name is added.
		assertTrue("There should be 1 name in the person object but there is actually : " + p.getNames().size(), p
		        .getNames().size() == 1);
		
		// adding the same name should not increment the size
		p.addName(pa1);
		assertTrue("There should be 1 name in the person object but there is actually : " + p.getNames().size(), p
		        .getNames().size() == 1);
		
		PersonName pa2 = new PersonName();
		pa2.setGivenName("secondtest");
		pa2.setFamilyName("secondtest2");
		pa2.setVoided(false);
		
		p.addName(pa2);
		
		// make sure the name is added
		assertTrue("There should be 2 names in the person object but there is actually : " + p.getNames().size(), p
		        .getNames().size() == 2);
		
		PersonName pa3 = new PersonName();
		pa3.setGivenName(pa1.getGivenName());
		pa3.setFamilyName(pa1.getFamilyName());
		pa3.setDateCreated(pa1.getDateCreated());
		pa3.setVoided(false);
		
		p.addName(pa3);
		// make sure the name is NOT added because its the same as pa1
		assertTrue("There should be 2 names in the person object but there is actually : " + p.getNames().size(), p
		        .getNames().size() == 2);
		
		PersonName pa4 = new PersonName();
		pa4.setGivenName(pa1.getGivenName() + "string to change the .equals method");
		pa4.setFamilyName(pa1.getFamilyName());
		pa4.setDateCreated(pa1.getDateCreated());
		pa4.setVoided(false);
		pa4.setVoided(true);
		p.addName(pa4);
		// make sure a voided name IS added
		assertTrue("There should be 3 names in the person object but there is actually : " + p.getNames().size(), p
		        .getNames().size() == 3);
		
		p.removeName(pa3);
		assertTrue("There should be only 2 name in the person object now", p.getNames().size() == 2);
		
		pa3.setDateCreated(new Date(pa1.getDateCreated().getTime() + 1));
		p.addName(pa3);
		// make sure the name IS added
		assertTrue("There should be 3 names in the person object but there is actually : " + p.getNames().size(), p
		        .getNames().size() == 3);
		
		// test removing all of the names
		p.removeName(pa4);
		assertTrue("There should be only 2 names in the person object now", p.getNames().size() == 2);
		p.removeName(pa3); // pa3 was never added, but is the same as pa1
		assertTrue("There should be only 1 names in the person object now", p.getNames().size() == 1);
		p.removeName(pa2);
		assertTrue("There should be only no names in the person object now", p.getNames().size() == 0);
		p.removeName(pa2);
		assertTrue("There should still be only no names in the person object now", p.getNames().size() == 0);
		p.removeName(pa1);
		assertTrue("There shouldn't be any names in the person object now", p.getNames().size() == 0);
	}
	
	/**
	 * Test the add/removeAttributes method in the person object
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldAddRemoveAttribute() throws Exception {
		
		Person p = new Person();
		
		assertNotNull(p.getAttributes());
		
		PersonAttribute pa1 = new PersonAttribute();
		
		pa1.setValue("firsttest");
		pa1.setAttributeType(new PersonAttributeType(1));
		pa1.setDateCreated(new Date());
		pa1.setVoided(false);
		p.addAttribute(pa1);
		
		// make sure the attribute is added.
		assertTrue("There should be 1 attribute in the person object but there is actually : " + p.getAttributes().size(), p
		        .getAttributes().size() == 1);
		
		// adding the same attribute should not increment the size
		p.addAttribute(pa1);
		assertTrue("There should be 1 attribute in the person object but there is actually : " + p.getAttributes().size(), p
		        .getAttributes().size() == 1);
		
		PersonAttribute pa2 = new PersonAttribute();
		pa2.setValue("secondtest");
		pa2.setAttributeType(new PersonAttributeType(2));
		pa2.setVoided(false);
		
		p.addAttribute(pa2);
		
		// make sure the attribute is added
		assertTrue("There should be 2 attributes in the person object but there is actually : " + p.getAttributes().size(),
		    p.getAttributes().size() == 2);
		
		PersonAttribute pa3 = new PersonAttribute();
		pa3.setValue(pa1.getValue());
		pa3.setAttributeType(pa1.getAttributeType());
		pa3.setDateCreated(pa1.getDateCreated());
		pa3.setVoided(false);
		
		p.addAttribute(pa3);
		// make sure the attribute is NOT added
		assertTrue("There should be 2 attributes in the person object but there is actually : " + p.getAttributes().size(),
		    p.getAttributes().size() == 2);
		
		// (we must change the value here as well, because logic says that there
		// is no
		// point in adding an attribute that has the same value/type...even if
		// the void
		// status is different)
		pa3.setValue(pa1.getValue() + "addition to make sure the value is different");
		pa3.setVoided(true);
		p.addAttribute(pa3);
		// make sure the attribute IS added
		assertTrue("There should be 3 attributes in the person object but there is actually : " + p.getAttributes().size(),
		    p.getAttributes().size() == 3);
		
		p.removeAttribute(pa3);
		assertTrue("There should be only 2 attribute in the person object now", p.getAttributes().size() == 2);
		
		pa3.setDateCreated(new Date(pa1.getDateCreated().getTime() + 1));
		p.addAttribute(pa3);
		// make sure the attribute IS added
		assertTrue("There should be 3 attributes in the person object but there is actually : " + p.getAttributes().size(),
		    p.getAttributes().size() == 3);
		
		// test removing all of the attributes
		p.removeAttribute(pa3);
		assertTrue("There should be only 2 attribute in the person object now", p.getAttributes().size() == 2);
		p.removeAttribute(pa2);
		assertTrue("There should be only 1 attribute in the person object now", p.getAttributes().size() == 1);
		p.removeAttribute(pa2);
		assertTrue("There should still be only 1 attribute in the person object now", p.getAttributes().size() == 1);
		p.removeAttribute(pa1);
		assertTrue("There shouldn't be any attributes in the person object now", p.getAttributes().size() == 0);
	}
	
	/**
	 * Test that setting a person's age correctly sets their birth date and records that this is
	 * inexact
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldSetInexactBirthdateFromAge() throws Exception {
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		Person p = new Person();
		
		// Test that default values are correct
		assertNull(p.getAge());
		assertFalse(p.isBirthdateEstimated());
		
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
	 * @see {@link Person#getAge(Date)}
	 */
	@Test
	@Verifies(value = "should get age after birthday", method = "getAge(Date)")
	public void getAge_shouldGetAgeAfterBirthday() throws Exception {
		Calendar birthdate = Calendar.getInstance();
		birthdate.set(2006, Calendar.JUNE, 2);
		Calendar onDate = Calendar.getInstance();
		onDate.set(2008, Calendar.JUNE, 3);
		Person person = new Person();
		person.setBirthdate(birthdate.getTime());
		assertEquals(person.getAge(onDate.getTime()), 2, 0);
	}
	
	/**
	 * @see {@link Person#getAge(Date)}
	 */
	@Test
	@Verifies(value = "should get age before birthday", method = "getAge(Date)")
	public void getAge_shouldGetAgeBeforeBirthday() throws Exception {
		Calendar birthdate = Calendar.getInstance();
		birthdate.set(2006, Calendar.JUNE, 2);
		Calendar onDate = Calendar.getInstance();
		onDate.set(2008, Calendar.JUNE, 1);
		Person person = new Person();
		person.setBirthdate(birthdate.getTime());
		assertEquals(person.getAge(onDate.getTime()), 1, 0);
	}
	
	/**
	 * @see {@link Person#getAge(Date)}
	 */
	@Test
	@Verifies(value = "should get age on birthday with minutes defined", method = "getAge(Date)")
	public void getAge_shouldGetAgeOnBirthdayWithMinutesDefined() throws Exception {
		Calendar birthdate = Calendar.getInstance();
		birthdate.set(2006, Calendar.JUNE, 2, 9, 9, 9);
		Calendar onDate = Calendar.getInstance();
		onDate.set(2008, Calendar.JUNE, 2, 7, 7, 7);
		Person person = new Person();
		person.setBirthdate(birthdate.getTime());
		assertEquals(person.getAge(onDate.getTime()), 2, 0);
	}
	
	/**
	 * @see {@link Person#getAge(Date)}
	 */
	@Test
	@Verifies(value = "should get age on birthday with no minutes defined", method = "getAge(Date)")
	public void getAge_shouldGetAgeOnBirthdayWithNoMinutesDefined() throws Exception {
		Calendar birthdate = Calendar.getInstance();
		birthdate.set(2006, Calendar.JUNE, 2);
		Calendar onDate = Calendar.getInstance();
		onDate.set(2008, Calendar.JUNE, 2);
		Person person = new Person();
		person.setBirthdate(birthdate.getTime());
		assertEquals(person.getAge(onDate.getTime()), 2, 0);
	}
	
	/**
	 * @see {@link Person#getAge()}
	 */
	@Test
	@Verifies(value = "should get age after death", method = "getAge()")
	public void getAge_shouldGetAgeAfterDeath() throws Exception {
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
	 * @see {@link Person#getAge(Date)}
	 */
	@Test
	@Verifies(value = "should get age with given date after death", method = "getAge(Date)")
	public void getAge_shouldGetAgeWithGivenDateAfterDeath() throws Exception {
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
	 * @see {@link Person#getAge(Date)}
	 */
	@Test
	@Verifies(value = "should get age with given date before death", method = "getAge(Date)")
	public void getAge_shouldGetAgeWithGivenDateBeforeDeath() throws Exception {
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
	 * @see {@link Person#getAge(Date)}
	 */
	@Test
	@Verifies(value = "should get age with given date before birth", method = "getAge(Date)")
	public void getAge_shouldGetAgeWithGivenDateBeforeBirth() throws Exception {
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
	 * @see {@link Person#addAttribute(PersonAttribute)}
	 */
	@Test
	@Verifies(value = "should not save an attribute with a blank string value", method = "addAttribute(PersonAttribute)")
	public void addAttribute_shouldNotSaveAnAttributeWithABlankStringValue() throws Exception {
		Person p = new Person();
		
		// make sure there are no initial attributes
		Assert.assertEquals("There should not be any attributes", 0, p.getAttributes().size());
		
		PersonAttribute pa1 = new PersonAttribute();
		pa1.setValue("");
		pa1.setAttributeType(new PersonAttributeType(1));
		pa1.setVoided(false);
		p.addAttribute(pa1);
		
		// make sure the attribute was not added
		Assert.assertEquals("There should not be any attributes", 0, p.getAttributes().size());
	}
	
	/**
	 * @see {@link Person#addAttribute(PersonAttribute)}
	 */
	@Test
	@Verifies(value = "should not save an attribute with a null value", method = "addAttribute(PersonAttribute)")
	public void addAttribute_shouldNotSaveAnAttributeWithANullValue() throws Exception {
		Person p = new Person();
		
		// make sure there are no initial attributes
		Assert.assertEquals("There should not be any attributes", 0, p.getAttributes().size());
		
		PersonAttribute pa1 = new PersonAttribute();
		pa1.setValue(null);
		pa1.setAttributeType(new PersonAttributeType(1));
		pa1.setVoided(false);
		p.addAttribute(pa1);
		
		// make sure the attribute was not added
		Assert.assertEquals("There should not be any attributes", 0, p.getAttributes().size());
	}
	
	/**
	 * @see {@link Person#addAttribute(PersonAttribute)}
	 */
	@Test
	@Verifies(value = "should void old attribute when a null or blank string value is added", method = "addAttribute(PersonAttribute)")
	public void addAttribute_shouldVoidOldAttributeWhenANullOrBlankStringValueIsAdded() throws Exception {
		Person p = new Person();
		
		// make sure there are no initial attributes
		Assert.assertEquals("There should not be any attributes", 0, p.getAttributes().size());
		
		PersonAttribute pa1 = new PersonAttribute();
		pa1.setValue("ack");
		PersonAttributeType attributeType = new PersonAttributeType(1);
		pa1.setAttributeType(attributeType);
		pa1.setVoided(false);
		pa1.setCreator(new User(1));
		p.addAttribute(pa1);
		
		// make sure the attribute was added
		Assert.assertEquals("The attribute was not added", 1, p.getAttributes().size());
		
		// add another one
		PersonAttribute pa2 = new PersonAttribute();
		pa2.setValue(null);
		pa2.setAttributeType(attributeType);
		pa2.setVoided(false);
		p.addAttribute(pa2);
		
		// make sure the new attribute was not added and the old was not removed
		Assert.assertEquals("Something changed ...", 1, p.getAttributes().size());
		
		// make sure the new attribute effectively voided the original
		Assert.assertTrue("The original attribute is not voided", ((PersonAttribute) p.getAttributes().toArray()[0])
		        .isVoided());
		
	}
	
	/**
	 * @see {@link Person#addAddress(PersonAddress)}
	 */
	@Test
	@Verifies(value = "should not add a person address with blank fields", method = "addAddress(PersonAddress)")
	public void addAddress_shouldNotAddAPersonAddressWithBlankFields() throws Exception {
		Person p = new Person();
		PersonAddress pa1 = new PersonAddress();
		pa1.setAddress1("address1");
		p.addAddress(pa1);
		PersonAddress pa2 = new PersonAddress();
		pa2.setAddress1("");
		p.addAddress(pa2);
		
		Assert.assertEquals(1, p.getAddresses().size());
	}
	
	/**
	 * @see Person#getPersonAddress()
	 * @verifies get not-voided person address if preferred address does not exist
	 */
	@Test
	public void getPersonAddress_shouldGetNotvoidedPersonAddressIfPreferredAddressDoesNotExist() throws Exception {
		
		// addresses
		PersonAddress voidedAddress = PersonAddressBuilder.newBuilder().withPreferred(false).withVoided(true).build();
		
		// addresses
		PersonAddress notVoidedAddress = PersonAddressBuilder.newBuilder().withPreferred(false).withVoided(false).build();
		
		PersonAddress expectedPersonAddress = notVoidedAddress;
		Set<PersonAddress> personAddresses = new HashSet<PersonAddress>(Arrays.asList(voidedAddress, notVoidedAddress));
		
		checkGetPersonAddressResultForVoidedPerson(expectedPersonAddress, personAddresses);
	}
	
	/**
	 * @see Person#getPersonAddress()
	 * @verifies get preferred and not-voided person address if exist
	 */
	@Test
	public void getPersonAddress_shouldGetPreferredAndNotvoidedPersonAddressIfExist() throws Exception {
		
		// addresses
		PersonAddress voidedAddress = PersonAddressBuilder.newBuilder().withPreferred(false).withVoided(true).build();
		
		PersonAddress preferredNotVoidedAddress = PersonAddressBuilder.newBuilder().withPreferred(true).withVoided(false)
		        .build();
		
		PersonAddress expectedPersonAddress = preferredNotVoidedAddress;
		HashSet<PersonAddress> personAddresses = new HashSet<PersonAddress>(Arrays.asList(voidedAddress,
		    preferredNotVoidedAddress));
		
		checkGetPersonAddressResultForVoidedPerson(expectedPersonAddress, personAddresses);
		
	}
	
	/**
	 * @see Person#getPersonAddress()
	 * @verifies get voided person address if person is voided and not-voided address does not exist
	 */
	@Test
	public void getPersonAddress_shouldGetVoidedPersonAddressIfPersonIsVoidedAndNotvoidedAddressDoesNotExist()
	        throws Exception {
		
		// addresses
		PersonAddress voidedAddress1 = PersonAddressBuilder.newBuilder().withVoided(true).build();
		PersonAddress voidedAddress2 = PersonAddressBuilder.newBuilder().withVoided(true).build();
		
		Set<PersonAddress> personAddresses = new HashSet<PersonAddress>(Arrays.asList(voidedAddress1, voidedAddress2));
		
		Person person = new Person();
		person.setVoided(true);
		person.setAddresses(personAddresses);
		
		PersonAddress actualPersonAddress = person.getPersonAddress();
		
		assertTrue(actualPersonAddress.isVoided());
	}
	
	/**
	 * @see Person#getPersonName()
	 * @verifies get not-voided person name if preferred address does not exist
	 */
	@Test
	public void getPersonName_shouldGetNotvoidedPersonNameIfPreferredAddressDoesNotExist() throws Exception {
		
		PersonName notVoidedName = PersonNameBuilder.newBuilder().withVoided(false).build();
		PersonName voidedName = PersonNameBuilder.newBuilder().withVoided(true).build();
		
		PersonName expectedPersonName = notVoidedName;
		
		checkGetPersonNameResultForVoidedPerson(expectedPersonName, new HashSet<PersonName>(Arrays.asList(notVoidedName,
		    voidedName)));
	}
	
	/**
	 * @see Person#getPersonName()
	 * @verifies get preferred and not-voided person name if exist
	 */
	@Test
	public void getPersonName_shouldGetPreferredAndNotvoidedPersonNameIfExist() throws Exception {
		
		PersonName preferredNotVoidedName = PersonNameBuilder.newBuilder().withPreferred(true).withVoided(false).build();
		PersonName notVoidedName = PersonNameBuilder.newBuilder().withVoided(false).build();
		PersonName voidedName = PersonNameBuilder.newBuilder().withVoided(true).build();
		
		PersonName expectedPersonName = preferredNotVoidedName;
		
		checkGetPersonNameResultForVoidedPerson(expectedPersonName, new HashSet<PersonName>(Arrays.asList(
		    preferredNotVoidedName, notVoidedName, voidedName)));
	}
	
	/**
	 * @see Person#getPersonName()
	 * @verifies get voided person address if person is voided and not-voided address does not exist
	 */
	@Test
	public void getPersonName_shouldGetVoidedPersonAddressIfPersonIsVoidedAndNotvoidedAddressDoesNotExist() throws Exception {
		
		PersonName voidedName = PersonNameBuilder.newBuilder().withVoided(true).build();
		
		PersonName expectedPersonName = voidedName;
		
		checkGetPersonNameResultForVoidedPerson(expectedPersonName, new HashSet<PersonName>(Arrays.asList(voidedName)));
		
	}
	
	/**
	 * @see Person#getPersonAddress()
	 * @verifies return null if person is not-voided and have voided address
	 */
	@Test
	public void getPersonAddress_shouldReturnNullIfPersonIsNotvoidedAndHaveVoidedAddress() throws Exception {
		
		PersonAddress firstPersonAddress = PersonAddressBuilder.newBuilder().withVoided(true).build();
		PersonAddress secondPersonAddress = PersonAddressBuilder.newBuilder().withVoided(true).build();
		
		Person notVoidedPerson = new Person();
		notVoidedPerson.addAddress(firstPersonAddress);
		notVoidedPerson.addAddress(secondPersonAddress);
		
		Assert.assertNull(notVoidedPerson.getPersonAddress());
	}
	
	/**
	 * @see Person#getPersonName()
	 * @verifies return null if person is not-voided and have voided names
	 */
	@Test
	public void getPersonName_shouldReturnNullIfPersonIsNotvoidedAndHaveVoidedNames() throws Exception {
		
		PersonName firstVoidedName = PersonNameBuilder.newBuilder().withVoided(true).build();
		PersonName secondVoidedName = PersonNameBuilder.newBuilder().withVoided(true).build();
		
		Person notVoidedPerson = new Person();
		notVoidedPerson.addName(firstVoidedName);
		notVoidedPerson.addName(secondVoidedName);
		
		Assert.assertNull(notVoidedPerson.getPersonName());
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
