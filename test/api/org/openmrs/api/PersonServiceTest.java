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
package org.openmrs.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.Person;
import org.openmrs.PersonAddress;
import org.openmrs.PersonAttributeType;
import org.openmrs.PersonName;
import org.openmrs.Relationship;
import org.openmrs.RelationshipType;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.test.Verifies;

/**
 * This class tests methods in the PersonService class. TODO: Test all methods in the PersonService
 * class.
 */
public class PersonServiceTest extends BaseContextSensitiveTest {
	
	protected static final String CREATE_PATIENT_XML = "org/openmrs/api/include/PatientServiceTest-createPatient.xml";
	
	protected static final String CREATE_RELATIONSHIP_XML = "org/openmrs/api/include/PersonServiceTest-createRelationship.xml";
	
	protected PatientService ps = null;
	
	protected AdministrationService adminService = null;
	
	protected PersonService personService = null;
	
	@Before
	public void onSetUpInTransaction() throws Exception {
		if (ps == null) {
			ps = Context.getPatientService();
			adminService = Context.getAdministrationService();
			personService = Context.getPersonService();
		}
	}
	
	/**
	 * Tests a voided relationship between personA and Person B to see if it is still listed when
	 * retrieving unvoided relationships for personA and if it is still listed when retrieving
	 * unvoided relationships for personB.
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldGetUnvoidedRelationships() throws Exception {
		executeDataSet(CREATE_PATIENT_XML);
		executeDataSet(CREATE_RELATIONSHIP_XML);
		
		// Create Patient#3.
		Patient patient = new Patient();
		PersonName pName = new PersonName();
		pName.setGivenName("Tom");
		pName.setMiddleName("E.");
		pName.setFamilyName("Patient");
		patient.addName(pName);
		PersonAddress pAddress = new PersonAddress();
		pAddress.setAddress1("123 My street");
		pAddress.setAddress2("Apt 402");
		pAddress.setCityVillage("Anywhere city");
		pAddress.setCountry("Some Country");
		Set<PersonAddress> pAddressList = patient.getAddresses();
		pAddressList.add(pAddress);
		patient.setAddresses(pAddressList);
		patient.addAddress(pAddress);
		patient.setDeathDate(new Date());
		patient.setBirthdate(new Date());
		patient.setBirthdateEstimated(true);
		patient.setGender("male");
		List<PatientIdentifierType> patientIdTypes = ps.getAllPatientIdentifierTypes();
		assertNotNull(patientIdTypes);
		PatientIdentifier patientIdentifier = new PatientIdentifier();
		patientIdentifier.setIdentifier("123-0");
		patientIdentifier.setIdentifierType(patientIdTypes.get(0));
		Set<PatientIdentifier> patientIdentifiers = new TreeSet<PatientIdentifier>();
		patientIdentifiers.add(patientIdentifier);
		patient.setIdentifiers(patientIdentifiers);
		ps.savePatient(patient);
		
		// Create a sibling relationship between Patient#2 and Patient#3
		Relationship sibling = new Relationship();
		sibling.setPersonA(ps.getPatient(2));
		sibling.setPersonB(patient);
		sibling.setRelationshipType(personService.getRelationshipType(4));
		// relationship.setCreator(Context.getUserService().getUser(1));
		personService.saveRelationship(sibling);
		
		// Make Patient#3 the Doctor of Patient#2.
		Relationship doctor = new Relationship();
		doctor.setPersonB(ps.getPatient(2));
		doctor.setPersonA(patient);
		doctor.setRelationshipType(personService.getRelationshipType(3));
		personService.saveRelationship(doctor);
		
		// Get unvoided relationships before voiding any.
		Person p = personService.getPerson(2);
		List<Relationship> aRels = personService.getRelationshipsByPerson(p);
		List<Relationship> bRels = personService.getRelationshipsByPerson(patient);
		
		//test loading relationship types real quick.
		List<RelationshipType> rTmp = personService.getAllRelationshipTypes();
		assertNotNull(rTmp);
		RelationshipType rTypeTmp = personService.getRelationshipTypeByName("Doctor/Patient");
		assertNotNull(rTypeTmp);
		rTypeTmp = personService.getRelationshipTypeByName("booya");
		assertNull(rTypeTmp);
		
		// Uncomment for console output.
		//System.out.println("Relationships before voiding all:");
		//System.out.println(aRels);
		//System.out.println(bRels);
		
		// Void all relationships.
		List<Relationship> allRels = personService.getAllRelationships();
		for (Relationship r : allRels) {
			personService.voidRelationship(r, "Because of a JUnit test.");
		}
		
		// Get unvoided relationships after voiding all of them.
		List<Relationship> updatedARels = personService.getRelationshipsByPerson(p);
		List<Relationship> updatedBRels = personService.getRelationshipsByPerson(patient);
		// Uncomment for console output
		//System.out.println("Relationships after voiding all:");
		//System.out.println(updatedARels);
		//System.out.println(updatedBRels);
		
		// Neither Patient#2 or Patient#3 should have any relationships now.
		assertEquals(updatedARels, updatedBRels);
	}
	
	/**
	 * This test should get the first/last name out of a string into a PersonName object.
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldParseTwoPersonNameWithAndWithoutComma() throws Exception {
		PersonService service = Context.getPersonService();
		
		PersonName pname = service.parsePersonName("Doe, John");
		assertEquals("Doe", pname.getFamilyName());
		assertEquals("John", pname.getGivenName());
		
		PersonName pname2 = service.parsePersonName("John Doe");
		assertEquals("Doe", pname2.getFamilyName());
		assertEquals("John", pname2.getGivenName());
	}
	
	/**
	 * @verifies savePersonAttributeType test = set the date created and creator on new
	 * @throws Exception
	 */
	@Test
	public void savePersonAttributeType_shouldSetTheDateCreatedAndCreatorOnNew() throws Exception {
		PersonService service = Context.getPersonService();
		
		PersonAttributeType pat = new PersonAttributeType();
		pat.setName("attr type name");
		pat.setDescription("attr type desc");
		
		service.savePersonAttributeType(pat);
		
		assertEquals(new User(1), pat.getCreator());
		assertNotNull(pat.getDateCreated());
	}
	
	/**
	 * @verifies savePersonAttributeType test = set the date changed and changed by on update
	 * @throws Exception
	 */
	@Test
	public void savePersonAttributeType_shouldSetTheDateChangedAndChangedByOnUpdate() throws Exception {
		PersonService service = Context.getPersonService();
		
		// get the type and change something about it
		PersonAttributeType pat = service.getPersonAttributeType(2);
		pat.setName("attr type name");
		
		// save the type again
		service.savePersonAttributeType(pat);
		
		assertEquals(new User(1), pat.getChangedBy());
		assertNotNull(pat.getDateChanged());
	}
	
	/**
	 * @see {@link PersonService#getSimilarPeople(String,Integer,String)}
	 */
	@Test
	@Verifies(value = "should accept greater than three names", method = "getSimilarPeople(String,Integer,String)")
	public void getSimilarPeople_shouldAcceptGreaterThanThreeNames() throws Exception {
		executeDataSet("org/openmrs/api/include/PersonServiceTest-names.xml");
		Set<Person> matches = Context.getPersonService().getSimilarPeople("Darius Graham Jazayeri Junior", 1979, "M");
		Assert.assertEquals(2, matches.size());
		Assert.assertTrue(matches.contains(new Person(1006)));
		Assert.assertTrue(matches.contains(new Person(1007)));
	}
	
	/**
	 * @see {@link PersonService#getSimilarPeople(String,Integer,String)}
	 */
	@Test
	@Verifies(value = "should match single search to any name part", method = "getSimilarPeople(String,Integer,String)")
	public void getSimilarPeople_shouldMatchSingleSearchToAnyNamePart() throws Exception {
		executeDataSet("org/openmrs/api/include/PersonServiceTest-names.xml");
		Set<Person> matches = Context.getPersonService().getSimilarPeople("Darius", 1979, "M");
		Assert.assertEquals(9, matches.size());
		Assert.assertTrue(matches.contains(new Person(1000)));
		Assert.assertTrue(matches.contains(new Person(1001)));
		Assert.assertTrue(matches.contains(new Person(1002)));
		Assert.assertTrue(matches.contains(new Person(1003)));
		Assert.assertTrue(matches.contains(new Person(1004)));
		Assert.assertTrue(matches.contains(new Person(1005)));
		Assert.assertTrue(matches.contains(new Person(1006)));
		Assert.assertTrue(matches.contains(new Person(1007)));
		Assert.assertTrue(matches.contains(new Person(1008)));
	}
	
	/**
	 * @see {@link PersonService#getSimilarPeople(String,Integer,String)}
	 */
	@Test
	@Verifies(value = "should match two word search to any name part", method = "getSimilarPeople(String,Integer,String)")
	public void getSimilarPeople_shouldMatchTwoWordSearchToAnyNamePart() throws Exception {
		executeDataSet("org/openmrs/api/include/PersonServiceTest-names.xml");
		Set<Person> matches = Context.getPersonService().getSimilarPeople("Darius Graham", 1979, "M");
		Assert.assertEquals(8, matches.size());
		Assert.assertTrue(matches.contains(new Person(1000)));
		Assert.assertTrue(matches.contains(new Person(1001)));
		Assert.assertTrue(matches.contains(new Person(1002)));
		Assert.assertTrue(matches.contains(new Person(1003)));
		Assert.assertTrue(matches.contains(new Person(1004)));
		Assert.assertTrue(matches.contains(new Person(1005)));
		Assert.assertTrue(matches.contains(new Person(1006)));
		Assert.assertTrue(matches.contains(new Person(1007)));
	}
	
	/**
	 * @see {@link PersonService#getSimilarPeople(String,Integer,String)}
	 */
	@Test
	@Verifies(value = "should match three word search to any name part", method = "getSimilarPeople(String,Integer,String)")
	public void getSimilarPeople_shouldMatchThreeWordSearchToAnyNamePart() throws Exception {
		executeDataSet("org/openmrs/api/include/PersonServiceTest-names.xml");
		Set<Person> matches = Context.getPersonService().getSimilarPeople("Darius Graham Jazayeri", 1979, "M");
		Assert.assertEquals(3, matches.size());
		Assert.assertTrue(matches.contains(new Person(1003)));
		Assert.assertTrue(matches.contains(new Person(1006)));
		Assert.assertTrue(matches.contains(new Person(1007)));
	}
	
	/**
	 * @see {@link PersonService#getPeople(String,Boolean)}
	 */
	@Test
	@Verifies(value = "should match search to familyName2", method = "getPeople(String,Boolean)")
	public void getPeople_shouldMatchSearchToFamilyName2() throws Exception {
		executeDataSet("org/openmrs/api/include/PersonServiceTest-extranames.xml");
		
		List<Person> people = Context.getPersonService().getPeople("Johnson", false);
		Assert.assertEquals(3, people.size());
		Assert.assertTrue(people.contains(new Patient(2)));
		Assert.assertTrue(people.contains(new Patient(4)));
		Assert.assertTrue(people.contains(new Patient(5)));
	}
	
	/**
	 * @see {@link PersonService#getSimilarPeople(String,Integer,String)}
	 */
	@Test
	@Verifies(value = "should match search to familyName2", method = "getSimilarPeople(String,Integer,String)")
	public void getSimilarPeople_shouldMatchSearchToFamilyName2() throws Exception {
		executeDataSet("org/openmrs/api/include/PersonServiceTest-extranames.xml");
		
		Set<Person> people = Context.getPersonService().getSimilarPeople("Johnson", null, "M");
		Assert.assertEquals(2, people.size());
		Assert.assertTrue(people.contains(new Patient(2)));
		Assert.assertTrue(people.contains(new Patient(4)));
	}
	
}
