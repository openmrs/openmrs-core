/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api.db.hibernate;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

import org.hibernate.SessionFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Person;
import org.openmrs.api.context.Context;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.util.GlobalPropertiesTestHelper;
import org.openmrs.util.OpenmrsConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HibernatePersonDAOTest extends BaseContextSensitiveTest {
	
	private static final Logger log = LoggerFactory.getLogger(HibernatePersonDAOTest.class);
	
	private final static String PEOPLE_FROM_THE_SHIRE_XML = "org/openmrs/api/db/hibernate/include/HibernatePersonDAOTest-people.xml";
	
	private SessionFactory sessionFactory;
	
	private HibernatePersonDAO hibernatePersonDAO;
	
	private PersonAttributeHelper personAttributeHelper;
	
 	private GlobalPropertiesTestHelper globalPropertiesTestHelper;

	@Before
	public void getPersonDAO() {
		executeDataSet(PEOPLE_FROM_THE_SHIRE_XML);

		updateSearchIndex();
		
		hibernatePersonDAO = (HibernatePersonDAO) applicationContext.getBean("personDAO");
		sessionFactory = (SessionFactory) applicationContext.getBean("sessionFactory");
		
		personAttributeHelper = new PersonAttributeHelper(sessionFactory);
		globalPropertiesTestHelper = new GlobalPropertiesTestHelper(Context.getAdministrationService());
	}
	
	private void logPeople(List<Person> people) {
		for (Person person : people) {
			logPerson(person);
		}
	}
	
	private void logPerson(Person person) {
		String info = "class=" + person.getClass().getCanonicalName() + ", person=" + person.toString() +
				", person.names=" + person.getNames().toString() + ", person.attributes=" +
				person.getAttributes().toString();

		log.debug(info);
	}
	
	/**
	 * @see HibernatePersonDAO#getPeople(String, Boolean)
	 */
	@Test
	public void getPeople_shouldGetNoOneByNull() {
		List<Person> people = hibernatePersonDAO.getPeople(null, false);
		logPeople(people);
		
		Assert.assertEquals(0, people.size());
	}
	
	/**
	 * @see HibernatePersonDAO#getPeople(String, Boolean)
	 */
	@Test
	public void getPeople_shouldGetEveryOneExceptVoidedByEmptyString() {
		List<Person> people = hibernatePersonDAO.getPeople("", false);
		logPeople(people);
		
		// PEOPLE_FROM_THE_SHIRE_XML contains 7 people but more people are defined in the standard test data set
		Assert.assertTrue(people.size() >= 7);
		
		// assert that all 7 people from PEOPLE_FROM_THE_SHIRE_XML (who are neither dead nor voided) are retrieved
		assertPeopleContainPersonID(people, 42);
		assertPeopleContainPersonID(people, 43);
		assertPeopleContainPersonID(people, 44);
		assertPeopleContainPersonID(people, 45);
		assertPeopleContainPersonID(people, 46);
		assertPeopleContainPersonID(people, 47);
		assertPeopleContainPersonID(people, 48);
	}
	
	/**
	 * @see HibernatePersonDAO#getPeople(String, Boolean, Boolean)
	 */
	@Test
	public void getPeople_shouldGetEveryOneByEmptyStringIncludingVoided() {
		List<Person> people = hibernatePersonDAO.getPeople("", false, true);
		logPeople(people);
		assertPeopleContainPersonID(people, 42);
		assertPeopleContainPersonID(people, 43);
		assertPeopleContainPersonID(people, 44);
		assertPeopleContainPersonID(people, 45);
		assertPeopleContainPersonID(people, 46);
		assertPeopleContainPersonID(people, 47);
		assertPeopleContainPersonID(people, 48);
		assertPeopleContainPersonID(people, 50);
	}
	
	/**
	 * @see HibernatePersonDAO#getPeople(String, Boolean, Boolean)
	 */
	@Test
	public void getPeople_shouldNotGetVoided() {
		List<Person> people = hibernatePersonDAO.getPeople("", false, false);
		logPeople(people);
		for (Person p : people)
			Assert.assertFalse(p.getVoided());
	}
	
	private void assertPeopleContainPersonID(List<Person> people, Integer personID) {
		for (Person person : people) {
			if (person.getId() == personID) {
				return;
			}
		}
		Assert.fail("list of people does not contain person with ID = " + personID);
	}
	
	/**
	 * @see HibernatePersonDAO#getPeople(String, Boolean)
	 */
	@Test
	public void getPeople_shouldGetNoOneByNonexistingAttribute() {
		Assert.assertFalse(personAttributeHelper.personAttributeExists("Wizard"));
		
		List<Person> people = hibernatePersonDAO.getPeople("Wizard", false);
		logPeople(people);
		
		Assert.assertEquals(0, people.size());
	}
	
	/**
	 * @see HibernatePersonDAO#getPeople(String, Boolean)
	 */
	@Test
	public void getPeople_shouldGetNoOneByNonsearchableAttribute() {
		Assert.assertTrue(personAttributeHelper.nonSearchablePersonAttributeExists("Porridge with honey"));
		
		List<Person> people = hibernatePersonDAO.getPeople("Porridge honey", false);
		logPeople(people);
		
		Assert.assertEquals(0, people.size());
	}
	
	/**
	 * @see HibernatePersonDAO#getPeople(String, Boolean)
	 */
	@Test
	public void getPeople_shouldGetNoOneByVoidedAttribute() {
		Assert.assertTrue(personAttributeHelper.voidedPersonAttributeExists("Master thief"));
		
		List<Person> people = hibernatePersonDAO.getPeople("Master thief", false);
		logPeople(people);
		
		Assert.assertEquals(0, people.size());
	}
	
	/**
	 * @see HibernatePersonDAO#getPeople(String, Boolean)
	 */
	@Test
	public void getPeople_shouldGetOnePersonByAttribute() {
		globalPropertiesTestHelper.setGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_PERSON_ATTRIBUTE_SEARCH_MATCH_MODE,
		    OpenmrsConstants.GLOBAL_PROPERTY_PERSON_ATTRIBUTE_SEARCH_MATCH_ANYWHERE);
		Assert.assertTrue(personAttributeHelper.personAttributeExists("Story teller"));
		
		List<Person> people = hibernatePersonDAO.getPeople("Story Teller", false);
		logPeople(people);
		
		Assert.assertEquals(1, people.size());
		Assert.assertEquals("Bilbo Odilon", people.get(0).getGivenName());
	}
	
	/**
	 * @see HibernatePersonDAO#getPeople(String, Boolean)
	 */
	@Test
	public void getPeople_shouldGetOnePersonByRandomCaseAttribute() {
		globalPropertiesTestHelper.setGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_PERSON_ATTRIBUTE_SEARCH_MATCH_MODE,
		    OpenmrsConstants.GLOBAL_PROPERTY_PERSON_ATTRIBUTE_SEARCH_MATCH_ANYWHERE);
		Assert.assertTrue(personAttributeHelper.personAttributeExists("Story teller"));
		
		List<Person> people = hibernatePersonDAO.getPeople("sToRy TeLlEr", false);
		logPeople(people);
		
		Assert.assertEquals(1, people.size());
		Assert.assertEquals("Bilbo Odilon", people.get(0).getGivenName());
	}
	
	/**
	 * @see HibernatePersonDAO#getPeople(String, Boolean)
	 */
	@Test
	public void getPeople_shouldGetOnePersonBySearchingForAMixOfAttributeAndVoidedAttribute() {
		globalPropertiesTestHelper.setGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_PERSON_ATTRIBUTE_SEARCH_MATCH_MODE,
		    OpenmrsConstants.GLOBAL_PROPERTY_PERSON_ATTRIBUTE_SEARCH_MATCH_ANYWHERE);
		Assert.assertTrue(personAttributeHelper.personAttributeExists("Story teller"));
		Assert.assertFalse(personAttributeHelper.voidedPersonAttributeExists("Story teller"));
		Assert.assertTrue(personAttributeHelper.voidedPersonAttributeExists("Master thief"));
		
		List<Person> people = hibernatePersonDAO.getPeople("Story Thief", false);
		logPeople(people);
		
		Assert.assertEquals(1, people.size());
		Assert.assertEquals("Bilbo Odilon", people.get(0).getGivenName());
	}
	
	/**
	 * @see HibernatePersonDAO#getPeople(String, Boolean)
	 */
	@Test
	public void getPeople_shouldGetMultiplePeopleBySingleAttribute() {
		globalPropertiesTestHelper.setGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_PERSON_ATTRIBUTE_SEARCH_MATCH_MODE,
		    OpenmrsConstants.GLOBAL_PROPERTY_PERSON_ATTRIBUTE_SEARCH_MATCH_ANYWHERE);
		Assert.assertTrue(personAttributeHelper.personAttributeExists("Senior ring bearer"));
		List<Person> people = hibernatePersonDAO.getPeople("Senior ring bearer", false);
		logPeople(people);
		
		Assert.assertEquals(2, people.size());
		
		Assert.assertEquals("Baggins", people.get(0).getFamilyName());
		Assert.assertEquals("Baggins", people.get(1).getFamilyName());
		Assert.assertFalse(people.get(0).getGivenName().equalsIgnoreCase(people.get(1).getGivenName()));
	}
	
	/**
	 * @see HibernatePersonDAO#getPeople(String, Boolean)
	 */
	@Test
	public void getPeople_shouldGetMultiplePeopleByMultipleAttributes() {
		globalPropertiesTestHelper.setGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_PERSON_ATTRIBUTE_SEARCH_MATCH_MODE,
		    OpenmrsConstants.GLOBAL_PROPERTY_PERSON_ATTRIBUTE_SEARCH_MATCH_ANYWHERE);
		Assert.assertTrue(personAttributeHelper.personAttributeExists("Senior ring bearer"));
		Assert.assertTrue(personAttributeHelper.personAttributeExists("Story teller"));
		List<Person> people = hibernatePersonDAO.getPeople("Story Bearer", false);
		logPeople(people);
		
		Assert.assertEquals(2, people.size());
		
		Assert.assertEquals("Baggins", people.get(0).getFamilyName());
		Assert.assertEquals("Baggins", people.get(1).getFamilyName());
		Assert.assertFalse(people.get(0).getGivenName().equalsIgnoreCase(people.get(1).getGivenName()));
	}
	
	/**
	 * @see HibernatePersonDAO#getPeople(String, Boolean)
	 */
	@Test
	public void getPeople_shouldGetNoOneByNonexistingName() {
		List<Person> people = hibernatePersonDAO.getPeople("Gandalf", false);
		logPeople(people);
		
		Assert.assertEquals(0, people.size());
	}
	
	/**
	 * @see HibernatePersonDAO#getPeople(String, Boolean)
	 */
	@Test
	public void getPeople_shouldGetOnePersonByName() {
		List<Person> people = hibernatePersonDAO.getPeople("Bilbo", false);
		logPeople(people);
		
		Assert.assertEquals(1, people.size());
		Assert.assertEquals("Bilbo Odilon", people.get(0).getGivenName());
	}
	
	/**
	 * @see HibernatePersonDAO#getPeople(String, Boolean)
	 */
	@Test
	public void getPeople_shouldGetOnePersonByRandomCaseName() {
		List<Person> people = hibernatePersonDAO.getPeople("fRoDo", false);
		logPeople(people);
		
		Assert.assertEquals(1, people.size());
		Assert.assertEquals("Frodo Ansilon", people.get(0).getGivenName());
	}
	
	/**
	 * @see HibernatePersonDAO#getPeople(String, Boolean)
	 */
	@Test
	public void getPeople_shouldGetMultiplePeopleBySingleName() {
		List<Person> people = hibernatePersonDAO.getPeople("Baggins", false);
		logPeople(people);
		
		Assert.assertEquals(2, people.size());
		
		Assert.assertEquals("Baggins", people.get(0).getFamilyName());
		Assert.assertEquals("Baggins", people.get(1).getFamilyName());
		Assert.assertFalse(people.get(0).getGivenName().equalsIgnoreCase(people.get(1).getGivenName()));
	}
	
	/**
	 * @see HibernatePersonDAO#getPeople(String, Boolean)
	 */
	@Test
	public void getPeople_shouldGetMultiplePeopleByMultipleNames() {
		List<Person> people = hibernatePersonDAO.getPeople("Bilbo Frodo", false);
		logPeople(people);
		
		Assert.assertEquals(2, people.size());
		
		Assert.assertEquals("Baggins", people.get(0).getFamilyName());
		Assert.assertEquals("Baggins", people.get(1).getFamilyName());
		Assert.assertFalse(people.get(0).getGivenName().equalsIgnoreCase(people.get(1).getGivenName()));
	}

    /**
	 * @see HibernatePersonDAO#getPeople(String, Boolean)
	 */
	@Test
	public void getPeople_shouldGetNoOneByNonexistingNameAndNonexistingAttribute() {
		Assert.assertFalse(personAttributeHelper.personAttributeExists("Wizard"));
		
		List<Person> people = hibernatePersonDAO.getPeople("Gandalf Wizard", false);
		logPeople(people);
		
		Assert.assertEquals(0, people.size());
	}
	
	/**
	 * @see HibernatePersonDAO#getPeople(String, Boolean)
	 */
	@Test
	public void getPeople_shouldGetNoOneByNonexistingNameAndNonsearchableAttribute() {
		Assert.assertTrue(personAttributeHelper.nonSearchablePersonAttributeExists("Mushroom pie"));
		List<Person> people = hibernatePersonDAO.getPeople("Gandalf Mushroom pie", false);
		logPeople(people);
		
		Assert.assertEquals(0, people.size());
	}
	
	/**
	 * @see HibernatePersonDAO#getPeople(String, Boolean)
	 */
	@Test
	public void getPeople_shouldGetNoOneByNonexistingNameAndVoidedAttribute() {
		Assert.assertTrue(personAttributeHelper.voidedPersonAttributeExists("Master Thief"));
		List<Person> people = hibernatePersonDAO.getPeople("Gandalf Master Thief", false);
		logPeople(people);
		
		Assert.assertEquals(0, people.size());
	}
	
	/**
	 * @see HibernatePersonDAO#getPeople(String, Boolean)
	 */
	@Test
	public void getPeople_shouldGetOnePersonByNameAndAttribute() {
		Assert.assertTrue(personAttributeHelper.personAttributeExists("Story teller"));
		List<Person> people = hibernatePersonDAO.getPeople("Bilbo Story Teller", false);
		logPeople(people);
		
		Assert.assertEquals(1, people.size());
		Assert.assertEquals("Bilbo Odilon", people.get(0).getGivenName());
	}
	
	/**
	 * @see HibernatePersonDAO#getPeople(String, Boolean)
	 */
	@Test
	public void getPeople_shouldGetOnePersonByNameAndVoidedAttribute() {
		Assert.assertTrue(personAttributeHelper.voidedPersonAttributeExists("Master Thief"));
		List<Person> people = hibernatePersonDAO.getPeople("Frodo Master Thief", false);
		logPeople(people);
		
		Assert.assertEquals(1, people.size());
		Assert.assertEquals("Frodo Ansilon", people.get(0).getGivenName());
	}
	
	/**
	 * @see HibernatePersonDAO#getPeople(String, Boolean)
	 */
	@Test
	public void getPeople_shouldGetMultiplePeopleByNameAndAttribute() {
		List<Person> people = hibernatePersonDAO
		        .getPeople(
		            "Bilbo Baggins Story Teller Master Thief Porridge Honey Frodo Baggins Ring Bearer Mushroom Pie Gandalf Wizard Beer",
		            false);
		logPeople(people);
		
		Assert.assertEquals(2, people.size());
		Assert.assertEquals("Baggins", people.get(0).getFamilyName());
		Assert.assertEquals("Baggins", people.get(1).getFamilyName());
		Assert.assertFalse(people.get(0).getGivenName().equalsIgnoreCase(people.get(1).getGivenName()));
	}
	
	/**
	 * @see HibernatePersonDAO#getPeople(String, Boolean)
	 */
	@Test
	public void getPeople_shouldGetOnePersonByGivenName() {
		List<Person> people = hibernatePersonDAO.getPeople("bravo", false);
		logPeople(people);
		
		Assert.assertEquals(1, people.size());
		Assert.assertEquals("bravo", people.get(0).getGivenName());
	}
	
	/**
	 * @see HibernatePersonDAO#getPeople(String, Boolean)
	 */
	@Test
	public void getPeople_shouldGetMultiplePeopleByGivenName() {
		List<Person> people = hibernatePersonDAO.getPeople("alpha", false);
		logPeople(people);
		
		Assert.assertEquals(2, people.size());
		Assert.assertEquals("alpha", people.get(0).getGivenName());
		Assert.assertEquals("alpha", people.get(1).getGivenName());
		Assert.assertTrue(people.get(0).getMiddleName() != people.get(1).getMiddleName());
	}
	
	/**
	 * @see HibernatePersonDAO#getPeople(String, Boolean)
	 */
	@Test
	public void getPeople_shouldGetOnePersonByMiddleName() {
		List<Person> people = hibernatePersonDAO.getPeople("echo", false);
		logPeople(people);
		
		Assert.assertEquals(1, people.size());
		Assert.assertEquals("echo", people.get(0).getMiddleName());
	}
	
	/**
	 * @see HibernatePersonDAO#getPeople(String, Boolean)
	 */
	@Test
	public void getPeople_shouldGetMultiplePeopleByMiddleName() {
		List<Person> people = hibernatePersonDAO.getPeople("foxtrot", false);
		logPeople(people);
		
		Assert.assertEquals(2, people.size());
		Assert.assertEquals("foxtrot", people.get(0).getMiddleName());
		Assert.assertEquals("foxtrot", people.get(1).getMiddleName());
		Assert.assertTrue(people.get(0).getFamilyName() != people.get(1).getFamilyName());
	}
	
	/**
	 * @see HibernatePersonDAO#getPeople(String, Boolean)
	 */
	@Test
	public void getPeople_shouldGetOnePersonByFamilyName() {
		List<Person> people = hibernatePersonDAO.getPeople("lima", false);
		logPeople(people);
		
		Assert.assertEquals(1, people.size());
		Assert.assertEquals("lima", people.get(0).getFamilyName());
	}
	
	/**
	 * @see HibernatePersonDAO#getPeople(String, Boolean)
	 */
	@Test
	public void getPeople_shouldGetMultiplePeopleByFamilyName() {
		List<Person> people = hibernatePersonDAO.getPeople("kilo", false);
		logPeople(people);
		
		Assert.assertEquals(2, people.size());
		Assert.assertEquals("kilo", people.get(0).getFamilyName());
		Assert.assertEquals("kilo", people.get(1).getFamilyName());
		Assert.assertTrue(people.get(0).getGivenName() != people.get(1).getGivenName());
	}
	
	/**
	 * @see HibernatePersonDAO#getPeople(String, Boolean)
	 */
	@Test
	public void getPeople_shouldGetOnePersonByFamilyName2() {
		List<Person> people = hibernatePersonDAO.getPeople("mike", false);
		logPeople(people);
		
		Assert.assertEquals(1, people.size());
		Assert.assertEquals("alpha", people.get(0).getGivenName());
	}
	
	/**
	 * @see HibernatePersonDAO#getPeople(String, Boolean)
	 */
	@Test
	public void getPeople_shouldGetMultiplePeopleByFamilyName2() {
		List<Person> people = hibernatePersonDAO.getPeople("papa", false);
		logPeople(people);
		
		Assert.assertEquals(2, people.size());
		Assert.assertEquals("papa", people.get(0).getPersonName().getFamilyName2());
		Assert.assertEquals("papa", people.get(1).getPersonName().getFamilyName2());
		Assert.assertTrue(people.get(0).getFamilyName() != people.get(1).getFamilyName());
	}
	
	/**
	 * @see HibernatePersonDAO#getPeople(String, Boolean)
	 */
	@Test
	public void getPeople_shouldGetOnePersonByMultipleNameParts() {
		List<Person> people = hibernatePersonDAO.getPeople("echo india mike", false);
		logPeople(people);
		
		Assert.assertEquals(1, people.size());
		Assert.assertEquals("alpha", people.get(0).getGivenName());
	}
	
	/**
	 * @see HibernatePersonDAO#getPeople(String, Boolean)
	 */
	@Test
	public void getPeople_shouldGetMultiplePeopleByMultipleNameParts() {
		List<Person> people = hibernatePersonDAO.getPeople("bravo delta golf juliet mike ", false);
		logPeople(people);
		
		Assert.assertEquals(5, people.size());
	}
	
	/**
	 * @see HibernatePersonDAO#getPeople(String, Boolean)
	 */
	@Test
	public void getPeople_shouldGetNoOneByVoidedName() {
		List<Person> people = hibernatePersonDAO.getPeople("voided-delta", false);
		logPeople(people);
		
		Assert.assertEquals(0, people.size());
	}
	
	/**
	 * @see HibernatePersonDAO#getPeople(String, Boolean, Boolean)
	 */
	@Test
	public void getPeople_shouldGetVoidedByVoidedNameWhenVoidedIsTrue() {
		List<Person> people = hibernatePersonDAO.getPeople("voided-bravo", false, true);
		logPeople(people);
		
		Assert.assertEquals(1, people.size());
	}
	
	/**
	 * @see HibernatePersonDAO#getPeople(String, Boolean)
	 */
	@Test
	public void getPeople_shouldNotGetVoidedPerson() {
		List<Person> people = hibernatePersonDAO.getPeople("voided-bravo", false);
		logPeople(people);
		
		Assert.assertEquals(0, people.size());
	}
	
	/**
	 * @see HibernatePersonDAO#getPeople(String, Boolean)
	 */
	@Test
	public void getPeople_shouldNotGetDeadPerson() {
		List<Person> people = hibernatePersonDAO.getPeople("dead-charlie", false);
		logPeople(people);
		
		Assert.assertEquals(0, people.size());
	}
	
	/**
	 * @see HibernatePersonDAO#getPeople(String, Boolean)
	 */
	@Test
	public void getPeople_shouldGetSingleDeadPerson() {
		List<Person> people = hibernatePersonDAO.getPeople("dead-charlie", true);
		logPeople(people);
		
		Assert.assertEquals(1, people.size());
		Assert.assertEquals("dead-charlie", people.get(0).getGivenName());
	}
	
	/**
	 * @see HibernatePersonDAO#getPeople(String, Boolean)
	 */
	@Test
	public void getPeople_shouldGetMultipleDeadPeople() {
		List<Person> people = hibernatePersonDAO.getPeople("dead-papa", true);
		logPeople(people);
		
		Assert.assertEquals(2, people.size());
		Assert.assertEquals("dead-papa", people.get(0).getPersonName().getFamilyName2());
		Assert.assertEquals("dead-papa", people.get(1).getPersonName().getFamilyName2());
		Assert.assertTrue(people.get(0).getFamilyName() != people.get(1).getFamilyName());
	}
	
	/**
	 * @see HibernatePersonDAO#getPeople(String, Boolean)
	 */
	@Test
	public void getPeople_shouldObeyAttributeMatchMode() {
		// exact match mode
		long patientCount = hibernatePersonDAO.getPeople("337-4820", false).size();
		Assert.assertEquals(1, patientCount);
		
		patientCount = hibernatePersonDAO.getPeople("337", false).size();
		Assert.assertEquals(0, patientCount);
		
		globalPropertiesTestHelper.setGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_PERSON_ATTRIBUTE_SEARCH_MATCH_MODE,
		    OpenmrsConstants.GLOBAL_PROPERTY_PERSON_ATTRIBUTE_SEARCH_MATCH_ANYWHERE);
		
		patientCount = hibernatePersonDAO.getPeople("337", false).size();
		Assert.assertEquals(1, patientCount);
	}
	
	@Test
	public void savePerson_shouldSavePersonWithBirthDateTime() throws ParseException {
		Person person = new Person();
		person.setBirthtime(new SimpleDateFormat("HH:mm:ss").parse("15:23:56"));
		person.setBirthdate(new SimpleDateFormat("yyyy-MM-dd").parse("2012-05-29"));
		person.setDead(false);
		person.setVoided(false);
		person.setBirthdateEstimated(false);
		person.setId(345);
		hibernatePersonDAO.savePerson(person);

		Person savedPerson = hibernatePersonDAO.getPerson(345);
		Assert.assertEquals(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2012-05-29 15:23:56"), savedPerson.getBirthDateTime());
	}

}
