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
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.Person;
import org.openmrs.PersonAddress;
import org.openmrs.PersonAttribute;
import org.openmrs.PersonAttributeType;
import org.openmrs.PersonName;
import org.openmrs.Relationship;
import org.openmrs.RelationshipType;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.test.Verifies;
import org.openmrs.util.OpenmrsConstants;

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
	 * @see {@link PersonService#getRelationshipsByPerson(Person)}
	 */
	@Test
	@Verifies(value = "should only get unvoided relationships", method = "getRelationshipsByPerson(Person)")
	public void getRelationshipsByPerson_shouldOnlyGetUnvoidedRelationships() throws Exception {
		executeDataSet(CREATE_PATIENT_XML);
		executeDataSet(CREATE_RELATIONSHIP_XML);
		
		// TODO use xml imported in BaseContextSensitiveTest#baseSetupWithStandardDataAndAuthentication()
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
		patientIdentifier.setLocation(new Location(1));
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
		
		//test loading relationship types real quick.
		List<RelationshipType> rTmp = personService.getAllRelationshipTypes();
		assertNotNull(rTmp);
		RelationshipType rTypeTmp = personService.getRelationshipTypeByName("Doctor/Patient");
		assertNotNull(rTypeTmp);
		rTypeTmp = personService.getRelationshipTypeByName("booya");
		assertNull(rTypeTmp);
		
		// Void all relationships.
		List<Relationship> allRels = personService.getAllRelationships();
		for (Relationship r : allRels) {
			personService.voidRelationship(r, "Because of a JUnit test.");
		}
		
		// TODO this is the actual test.  Cut this method down to just this
		
		// Get unvoided relationships after voiding all of them.
		List<Relationship> updatedARels = personService.getRelationshipsByPerson(p);
		List<Relationship> updatedBRels = personService.getRelationshipsByPerson(patient);
		
		// Neither Patient#2 or Patient#3 should have any relationships now.
		assertEquals(0, updatedARels.size());
		assertEquals(updatedARels, updatedBRels);
	}
	
	/**
	 * This test should get the first/last name out of a string into a PersonName object.
	 * 
	 * @see {@link PersonService#parsePersonName(String)}
	 */
	@Test
	@Verifies(value = "should parse two person name with comma", method = "parsePersonName(String)")
	public void parsePersonName_shouldParseTwoPersonNameWithComma() throws Exception {
		PersonName pname = Context.getPersonService().parsePersonName("Doe, John");
		assertEquals("Doe", pname.getFamilyName());
		assertEquals("John", pname.getGivenName());
		
		// try without a space
		pname = Context.getPersonService().parsePersonName("Doe,John");
		assertEquals("Doe", pname.getFamilyName());
		assertEquals("John", pname.getGivenName());
	}
	
	/**
	 * @see {@link PersonService#parsePersonName(String)}
	 */
	@Test
	@Verifies(value = "should parse two person name without comma", method = "parsePersonName(String)")
	public void parsePersonName_shouldParseTwoPersonNameWithoutComma() throws Exception {
		PersonName pname2 = Context.getPersonService().parsePersonName("John Doe");
		assertEquals("Doe", pname2.getFamilyName());
		assertEquals("John", pname2.getGivenName());
	}
	
	/**
	 * @see {@link PersonService#savePersonAttributeType(PersonAttributeType)}
	 */
	@Test
	@Verifies(value = "should set the date created and creator on new", method = "savePersonAttributeType(PersonAttributeType)")
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
	 * @see {@link PersonService#savePersonAttributeType(PersonAttributeType)}
	 */
	@Test
	@Verifies(value = "should set the date changed and changed by on update", method = "savePersonAttributeType(PersonAttributeType)")
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
	 * @see {@link PersonService#savePersonAttributeType(PersonAttributeType)}
	 */
	@Test
	@Verifies(value = "should update any global property which reference this type", method = "savePersonAttributeType(PersonAttributeType)")
	public void savePersonAttributeType_shouldUpdateAnyGlobalPropertyWhichReferenceThisType() throws Exception {
		executeDataSet("org/openmrs/api/include/PersonServiceTest-updatePersonAttributeType.xml");
		PersonService service = Context.getPersonService();
		AdministrationService as = Context.getAdministrationService();
		
		// get the type and change its name
		PersonAttributeType pat = service.getPersonAttributeType(1);
		assertEquals("Race", pat.getName());
		
		String patientHeader = as.getGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_PATIENT_HEADER_ATTRIBUTES);
		assertEquals("Race,Birthpalce", patientHeader);
		String patientListing = as.getGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_PATIENT_LISTING_ATTRIBUTES);
		assertEquals("Race,Birthpalce", patientListing);
		String patientViewing = as.getGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_PATIENT_VIEWING_ATTRIBUTES);
		assertEquals("Birthpalce", patientViewing);
		
		pat.setName("Race Updated");
		pat = service.savePersonAttributeType(pat);
		assertEquals("Race Updated", pat.getName());
		
		patientHeader = as.getGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_PATIENT_HEADER_ATTRIBUTES);
		assertEquals("Race Updated,Birthpalce", patientHeader);
		patientListing = as.getGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_PATIENT_LISTING_ATTRIBUTES);
		assertEquals("Race Updated,Birthpalce", patientListing);
		patientViewing = as.getGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_PATIENT_VIEWING_ATTRIBUTES);
		assertEquals("Birthpalce", patientViewing);
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
		Assert.assertEquals(6, matches.size());
		Assert.assertTrue(matches.contains(new Person(1000)));
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
	
	/**
	 * @see {@link PersonService#getAllPersonAttributeTypes()}
	 * 
	 */
	@Test
	@Verifies(value = "should return all person attribute types including retired", method = "getAllPersonAttributeTypes()")
	public void getAllPersonAttributeTypes_shouldReturnAllPersonAttributeTypesIncludingRetired() throws Exception {
		executeDataSet("org/openmrs/api/include/PersonServiceTest-createRetiredPersonAttributeType.xml");
		
		List<PersonAttributeType> attributeTypes = Context.getPersonService().getAllPersonAttributeTypes();
		Assert.assertTrue("At least one element, otherwise no checking for retired will take place",
		    attributeTypes.size() > 0);
		
		boolean foundRetired = false;
		for (PersonAttributeType personAttributeType : attributeTypes) {
			if (personAttributeType.isRetired()) {
				foundRetired = true;
				break;
			}
		}
		
		Assert.assertTrue("There should be at least one retired person attribute type found in the list", foundRetired);
	}
	
	/**
	 * @see {@link PersonService#getAllPersonAttributeTypes(null)}
	 * 
	 */
	@Test
	@Verifies(value = "should return all person attribute types excluding retired when include retired is false", method = "getAllPersonAttributeTypes(null)")
	public void getAllPersonAttributeTypes_shouldReturnAllPersonAttributeTypesExcludingRetiredWhenIncludeRetiredIsFalse()
	        throws Exception {
		executeDataSet("org/openmrs/api/include/PersonServiceTest-createRetiredPersonAttributeType.xml");
		
		List<PersonAttributeType> attributeTypes = Context.getPersonService().getAllPersonAttributeTypes(false);
		Assert.assertTrue("At least one element, otherwise no checking for retired will take place",
		    attributeTypes.size() > 0);
		
		boolean foundRetired = false;
		for (PersonAttributeType personAttributeType : attributeTypes) {
			if (personAttributeType.isRetired()) {
				foundRetired = true;
				break;
			}
		}
		
		Assert.assertFalse("There should be no retired person attribute type found in the list", foundRetired);
	}
	
	/**
	 * @see {@link PersonService#getAllPersonAttributeTypes(null)}
	 * 
	 */
	@Test
	@Verifies(value = "should return all person attribute types including retired when include retired is true", method = "getAllPersonAttributeTypes(null)")
	public void getAllPersonAttributeTypes_shouldReturnAllPersonAttributeTypesIncludingRetiredWhenIncludeRetiredIsTrue()
	        throws Exception {
		executeDataSet("org/openmrs/api/include/PersonServiceTest-createRetiredPersonAttributeType.xml");
		
		//TODO: is this the correct way? or should we loop to find a retired type and then perform the following?
		List<PersonAttributeType> attributeTypes = Context.getPersonService().getAllPersonAttributeTypes(true);
		Assert.assertTrue("At least one element, otherwise no checking for retired will take place",
		    attributeTypes.size() > 0);
		
		boolean foundRetired = false;
		for (PersonAttributeType personAttributeType : attributeTypes) {
			if (personAttributeType.isRetired()) {
				foundRetired = true;
				break;
			}
		}
		
		Assert.assertTrue("There should be at least one retired person attribute type found in the list", foundRetired);
	}
	
	/**
	 * @see {@link PersonService#getAllRelationships()}
	 * 
	 */
	@Test
	@Verifies(value = "should return all unvoided relationships", method = "getAllRelationships()")
	public void getAllRelationships_shouldReturnAllUnvoidedRelationships() throws Exception {
		executeDataSet("org/openmrs/api/include/PersonServiceTest-createRetiredRelationship.xml");
		
		List<Relationship> relationships = Context.getPersonService().getAllRelationships();
		Assert
		        .assertTrue("At least one element, otherwise no checking for voided will take place",
		            relationships.size() > 0);
		
		boolean foundVoided = false;
		for (Relationship relationship : relationships) {
			if (relationship.isVoided()) {
				foundVoided = true;
				break;
			}
		}
		
		Assert.assertFalse("There should be no voided relationship here", foundVoided);
	}
	
	/**
	 * @see {@link PersonService#getAllRelationships(null)}
	 * 
	 */
	@Test
	@Verifies(value = "should return all relationship including voided when include voided equals true", method = "getAllRelationships(null)")
	public void getAllRelationships_shouldReturnAllRelationshipIncludingVoidedWhenIncludeVoidedEqualsTrue() throws Exception {
		executeDataSet("org/openmrs/api/include/PersonServiceTest-createRetiredRelationship.xml");
		
		List<Relationship> relationships = Context.getPersonService().getAllRelationships(true);
		Assert
		        .assertTrue("At least one element, otherwise no checking for voided will take place",
		            relationships.size() > 0);
		
		boolean foundVoided = false;
		for (Relationship relationship : relationships) {
			if (relationship.isVoided()) {
				foundVoided = true;
				break;
			}
		}
		
		Assert.assertTrue("There should be voided relationship here", foundVoided);
	}
	
	/**
	 * @see {@link PersonService#getAllRelationships(null)}
	 * 
	 */
	@Test
	@Verifies(value = "should return all relationship excluding voided when include voided equals false", method = "getAllRelationships(null)")
	public void getAllRelationships_shouldReturnAllRelationshipExcludingVoidedWhenIncludeVoidedEqualsFalse()
	        throws Exception {
		executeDataSet("org/openmrs/api/include/PersonServiceTest-createRetiredRelationship.xml");
		
		List<Relationship> relationships = Context.getPersonService().getAllRelationships(false);
		Assert
		        .assertTrue("At least one element, otherwise no checking for voided will take place",
		            relationships.size() > 0);
		
		boolean foundVoided = false;
		for (Relationship relationship : relationships) {
			if (relationship.isVoided()) {
				foundVoided = true;
				break;
			}
		}
		
		Assert.assertFalse("There should be no voided relationship here", foundVoided);
	}
	
	/**
	 * @see {@link PersonService#getAllRelationshipTypes()}
	 * 
	 */
	@Test
	@Verifies(value = "should return all relationship types", method = "getAllRelationshipTypes()")
	public void getAllRelationshipTypes_shouldReturnAllRelationshipTypes() throws Exception {
		executeDataSet("org/openmrs/api/include/PersonServiceTest-createRetiredRelationship.xml");
		
		List<RelationshipType> relationshipTypes = Context.getPersonService().getAllRelationshipTypes();
		Assert.assertTrue("Number of relationship type are 6", relationshipTypes.size() == 6);
	}
	
	/**
	 * @see {@link PersonService#getPerson(Integer)}
	 * 
	 */
	@Test
	@Verifies(value = "should return null when no person has the given id", method = "getPerson(Integer)")
	public void getPerson_shouldReturnNullWhenNoPersonHasTheGivenId() throws Exception {
		Person person = Context.getPersonService().getPerson(10000);
		Assert.assertNull(person);
	}
	
	/**
	 * @see {@link PersonService#getPersonAttribute(Integer)}
	 * 
	 */
	@Test
	@Verifies(value = "should return null when given id does not exist", method = "getPersonAttribute(Integer)")
	public void getPersonAttribute_shouldReturnNullWhenGivenIdDoesNotExist() throws Exception {
		PersonAttribute personAttribute = Context.getPersonService().getPersonAttribute(10000);
		Assert.assertNull(personAttribute);
	}
	
	/**
	 * @see {@link PersonService#getPersonAttribute(Integer)}
	 * 
	 */
	@Test
	@Verifies(value = "should return person attribute when given id does exist", method = "getPersonAttribute(Integer)")
	public void getPersonAttribute_shouldReturnPersonAttributeWhenGivenIdDoesExist() throws Exception {
		PersonAttribute personAttribute = Context.getPersonService().getPersonAttribute(17);
		Assert.assertNotNull(personAttribute);
		Assert.assertTrue("Expecting the return is of a person attribute", personAttribute.getClass().equals(
		    PersonAttribute.class));
	}
	
	/**
	 * @see {@link PersonService#getPersonAttributeType(Integer)}
	 * 
	 */
	@Test
	@Verifies(value = "should return null when no person attribute with the given id exist", method = "getPersonAttributeType(Integer)")
	public void getPersonAttributeType_shouldReturnNullWhenNoPersonAttributeWithTheGivenIdExist() throws Exception {
		PersonAttributeType attributeType = Context.getPersonService().getPersonAttributeType(10000);
		Assert.assertNull(attributeType);
	}
	
	/**
	 * @see {@link PersonService#getPersonAttributeTypeByName(String)}
	 * 
	 */
	@Test
	@Verifies(value = "should return person attribute type when name matches given type name", method = "getPersonAttributeTypeByName(String)")
	public void getPersonAttributeTypeByName_shouldReturnPersonAttributeTypeWhenNameMatchesGivenTypeName() throws Exception {
		PersonAttributeType attributeType = Context.getPersonService().getPersonAttributeTypeByName("Birthplace");
		Assert.assertNotNull(attributeType);
	}
	
	/**
	 * @see {@link PersonService#getPersonAttributeTypeByName(String)}
	 * 
	 */
	@Test
	@Verifies(value = "should return null when no person attribute type match given type name", method = "getPersonAttributeTypeByName(String)")
	public void getPersonAttributeTypeByName_shouldReturnNullWhenNoPersonAttributeTypeMatchGivenTypeName() throws Exception {
		PersonAttributeType attributeType = Context.getPersonService().getPersonAttributeTypeByName("Credit Card");
		Assert.assertNull(attributeType);
	}
	
	/**
	 * @see {@link PersonService#getPersonAttributeTypes(String,String,Integer,Boolean)}
	 * 
	 */
	@Test
	@Verifies(value = "should return person attribute types matching given parameters", method = "getPersonAttributeTypes(String,String,Integer,Boolean)")
	public void getPersonAttributeTypes_shouldReturnPersonAttributeTypesMatchingGivenParameters() throws Exception {
		executeDataSet("org/openmrs/api/include/PersonServiceTest-createRetiredPersonAttributeType.xml");
		
		List<PersonAttributeType> attributeTypes = Context.getPersonService().getPersonAttributeTypes(
		    "A nonexistent attr type name", null, null, null);
		Assert.assertNotNull(attributeTypes);
		Assert.assertTrue("Number of matched attribute type is 0", attributeTypes.isEmpty());
		
		attributeTypes = Context.getPersonService().getPersonAttributeTypes(null, "org.openmrs.Concept", null, null);
		Assert.assertNotNull(attributeTypes);
		Assert.assertTrue("Number of matched attribute type is 1", attributeTypes.size() == 1);
		
		attributeTypes = Context.getPersonService().getPersonAttributeTypes(null, null, null, false);
		Assert.assertNotNull(attributeTypes);
		Assert.assertTrue("Number of matched attribute type is 6", attributeTypes.size() == 6);
	}
	
	/**
	 * @see {@link PersonService#getPersonAttributeTypes(String,String,Integer,Boolean)}
	 * 
	 */
	@Test
	@Verifies(value = "should return empty list when no person attribute types match given parameters", method = "getPersonAttributeTypes(String,String,Integer,Boolean)")
	public void getPersonAttributeTypes_shouldReturnEmptyListWhenNoPersonAttributeTypesMatchGivenParameters()
	        throws Exception {
		executeDataSet("org/openmrs/api/include/PersonServiceTest-createRetiredPersonAttributeType.xml");
		
		List<PersonAttributeType> attributeTypes = Context.getPersonService().getPersonAttributeTypes(
		    "A non-existent attr type name", "java.lang.String", null, false);
		Assert.assertNotNull(attributeTypes);
		Assert.assertTrue("Should return empty list", attributeTypes.isEmpty());
	}
	
	/**
	 * @see {@link PersonService#getRelationship(Integer)}
	 * 
	 */
	@Test
	@Verifies(value = "should return relationship with given id", method = "getRelationship(Integer)")
	public void getRelationship_shouldReturnRelationshipWithGivenId() throws Exception {
		Relationship relationship = Context.getPersonService().getRelationship(1);
		Assert.assertNotNull(relationship);
	}
	
	/**
	 * @see {@link PersonService#getRelationship(Integer)}
	 * 
	 */
	@Test
	@Verifies(value = "should return null when relationship with given id does not exist", method = "getRelationship(Integer)")
	public void getRelationship_shouldReturnNullWhenRelationshipWithGivenIdDoesNotExist() throws Exception {
		Relationship relationship = Context.getPersonService().getRelationship(10000);
		Assert.assertNull(relationship);
	}
	
	/**
	 * @see {@link PersonService#getRelationshipMap(RelationshipType)}
	 * 
	 */
	@Test
	@Verifies(value = "should return empty map when no relationship has the matching relationship type", method = "getRelationshipMap(RelationshipType)")
	public void getRelationshipMap_shouldReturnEmptyMapWhenNoRelationshipHasTheMatchingRelationshipType() throws Exception {
		executeDataSet("org/openmrs/api/include/PersonServiceTest-createRetiredRelationship.xml");
		
		PersonService personService = Context.getPersonService();
		
		RelationshipType relationshipType = personService.getRelationshipType(15);
		Map<Person, List<Person>> relationshipMap = personService.getRelationshipMap(relationshipType);
		Assert.assertNotNull(relationshipMap);
		Assert.assertTrue("There should be no element in the map", relationshipMap.isEmpty());
	}
	
	/**
	 * @see {@link PersonService#getRelationships(Person,Person,RelationshipType)}
	 * 
	 */
	@Test
	@Verifies(value = "should fetch relationships matching the given from person", method = "getRelationships(Person,Person,RelationshipType)")
	public void getRelationships_shouldFetchRelationshipsMatchingTheGivenFromPerson() throws Exception {
		PersonService personService = Context.getPersonService();
		
		Person firstPerson = personService.getPerson(502);
		List<Relationship> relationships = personService.getRelationships(firstPerson, null, null);
		Assert.assertNotNull(relationships);
		Assert.assertTrue("There should be relationship found given the from person", relationships.size() > 0);
	}
	
	/**
	 * @see {@link PersonService#getRelationships(Person,Person,RelationshipType)}
	 * 
	 */
	@Test
	@Verifies(value = "should fetch relationships matching the given to person", method = "getRelationships(Person,Person,RelationshipType)")
	public void getRelationships_shouldFetchRelationshipsMatchingTheGivenToPerson() throws Exception {
		PersonService personService = Context.getPersonService();
		
		Person secondPerson = personService.getPerson(7);
		List<Relationship> relationships = personService.getRelationships(null, secondPerson, null);
		Assert.assertNotNull(relationships);
		Assert.assertTrue("There should be relationship found given the to person", relationships.size() > 0);
	}
	
	/**
	 * @see {@link PersonService#getRelationships(Person,Person,RelationshipType)}
	 * 
	 */
	@Test
	@Verifies(value = "should fetch relationships matching the given rel type", method = "getRelationships(Person,Person,RelationshipType)")
	public void getRelationships_shouldFetchRelationshipsMatchingTheGivenRelType() throws Exception {
		PersonService personService = Context.getPersonService();
		
		RelationshipType relationshipType = personService.getRelationshipType(1);
		List<Relationship> relationships = personService.getRelationships(null, null, relationshipType);
		Assert.assertNotNull(relationships);
		Assert.assertTrue("There should be relationship found given the relationship type", relationships.size() > 0);
	}
	
	/**
	 * @see {@link PersonService#getRelationships(Person,Person,RelationshipType)}
	 * 
	 */
	@Test
	@Verifies(value = "should return empty list when no relationship matching given parameters exist", method = "getRelationships(Person,Person,RelationshipType)")
	public void getRelationships_shouldReturnEmptyListWhenNoRelationshipMatchingGivenParametersExist() throws Exception {
		PersonService personService = Context.getPersonService();
		
		Person firstPerson = personService.getPerson(501);
		Person secondPerson = personService.getPerson(2);
		RelationshipType relationshipType = personService.getRelationshipType(1);
		List<Relationship> relationships = personService.getRelationships(firstPerson, secondPerson, relationshipType);
		Assert.assertNotNull(relationships);
		Assert.assertTrue("There should be no relationship found given the from person", relationships.isEmpty());
	}
	
	/**
	 * @see {@link PersonService#getRelationshipsByPerson(Person)}
	 * 
	 */
	@Test
	@Verifies(value = "should fetch relationships associated with the given person", method = "getRelationshipsByPerson(Person)")
	public void getRelationshipsByPerson_shouldFetchRelationshipsAssociatedWithTheGivenPerson() throws Exception {
		PersonService personService = Context.getPersonService();
		
		Person person = personService.getPerson(2);
		List<Relationship> relationships = personService.getRelationshipsByPerson(person);
		Assert.assertNotNull(relationships);
		Assert.assertTrue("There should be relationship found given the person", relationships.size() > 0);
	}
	
	/**
	 * @see {@link PersonService#getRelationshipsByPerson(Person)}
	 * 
	 */
	@Test
	@Verifies(value = "should fetch unvoided relationships only", method = "getRelationshipsByPerson(Person)")
	public void getRelationshipsByPerson_shouldFetchUnvoidedRelationshipsOnly() throws Exception {
		executeDataSet("org/openmrs/api/include/PersonServiceTest-createRetiredRelationship.xml");
		
		PersonService personService = Context.getPersonService();
		
		Person person = personService.getPerson(6);
		List<Relationship> relationships = personService.getRelationshipsByPerson(person);
		Assert.assertNotNull(relationships);
		Assert.assertTrue("There should be no relationship found given the person", relationships.isEmpty());
		
	}
	
	/**
	 * @see {@link PersonService#getRelationshipType(Integer)}
	 * 
	 */
	@Test
	@Verifies(value = "should return relationship type with the given relationship type id", method = "getRelationshipType(Integer)")
	public void getRelationshipType_shouldReturnRelationshipTypeWithTheGivenRelationshipTypeId() throws Exception {
		RelationshipType relationshipType = Context.getPersonService().getRelationshipType(1);
		Assert.assertNotNull(relationshipType);
		Assert.assertTrue("Expecting the return is of a relationship type", relationshipType.getClass().equals(
		    RelationshipType.class));
	}
	
	/**
	 * @see {@link PersonService#getRelationshipType(Integer)}
	 * 
	 */
	@Test
	@Verifies(value = "should return null when no relationship type matches given relationship type id", method = "getRelationshipType(Integer)")
	public void getRelationshipType_shouldReturnNullWhenNoRelationshipTypeMatchesGivenRelationshipTypeId() throws Exception {
		RelationshipType relationshipType = Context.getPersonService().getRelationshipType(10000);
		Assert.assertNull(relationshipType);
	}
	
	/**
	 * @see {@link PersonService#getRelationshipTypeByName(String)}
	 * 
	 */
	@Test
	@Verifies(value = "should return null when no relationship type match the given name", method = "getRelationshipTypeByName(String)")
	public void getRelationshipTypeByName_shouldReturnNullWhenNoRelationshipTypeMatchTheGivenName() throws Exception {
		RelationshipType relationshipType = Context.getPersonService().getRelationshipTypeByName("Supervisor");
		Assert.assertNull(relationshipType);
	}
	
	/**
	 * @see {@link PersonService#getRelationshipTypes(String)}
	 * 
	 */
	@Test
	@Verifies(value = "should return empty list when no relationship type match the search string", method = "getRelationshipTypes(String)")
	public void getRelationshipTypes_shouldReturnEmptyListWhenNoRelationshipTypeMatchTheSearchString() throws Exception {
		List<RelationshipType> relationshipTypes = Context.getPersonService().getRelationshipTypes("Doctor");
		Assert.assertNotNull(relationshipTypes);
		Assert.assertTrue("There should be no relationship type for the given name", relationshipTypes.isEmpty());
	}
	
	/**
	 * @see {@link PersonService#getRelationshipTypes(String,Boolean)}
	 * TODO Needs to test "preferred" 
	 */
	@Test
	@Verifies(value = "should return list of preferred relationship type matching given name", method = "getRelationshipTypes(String,Boolean)")
	public void getRelationshipTypes_shouldReturnListOfPreferredRelationshipTypeMatchingGivenName() throws Exception {
		executeDataSet("org/openmrs/api/include/PersonServiceTest-createRetiredRelationship.xml");
		List<RelationshipType> relationshipTypes = Context.getPersonService().getRelationshipTypes("Sibling/Sibling", true);
		Assert.assertNotNull(relationshipTypes);
		Assert.assertTrue("There should be relationship type for the given name", relationshipTypes.size() > 0);
	}
	
	/**
	 * @see {@link PersonService#getRelationshipTypes(String,Boolean)}
	 * 
	 */
	@Test
	@Verifies(value = "should return empty list when no preferred relationship type match the given name", method = "getRelationshipTypes(String,Boolean)")
	public void getRelationshipTypes_shouldReturnEmptyListWhenNoPreferredRelationshipTypeMatchTheGivenName()
	        throws Exception {
		List<RelationshipType> relationshipTypes = Context.getPersonService().getRelationshipTypes("Doctor/Patient", true);
		Assert.assertNotNull(relationshipTypes);
		Assert.assertTrue("There should be no relationship type for the given name", relationshipTypes.isEmpty());
	}
	
	/**
	 * @see {@link PersonService#purgePerson(Person)}
	 * 
	 */
	@Test
	@Verifies(value = "should delete person from the database", method = "purgePerson(Person)")
	public void purgePerson_shouldDeletePersonFromTheDatabase() throws Exception {
		PersonService personService = Context.getPersonService();
		
		Person person = personService.getPerson(8);
		personService.purgePerson(person);
		
		Person deletedPerson = personService.getPerson(8);
		Assert.assertNull(deletedPerson);
	}
	
	/**
	 * @see {@link PersonService#purgePersonAttributeType(PersonAttributeType)}
	 * 
	 */
	@Test
	@Verifies(value = "should delete person attribute type from database", method = "purgePersonAttributeType(PersonAttributeType)")
	public void purgePersonAttributeType_shouldDeletePersonAttributeTypeFromDatabase() throws Exception {
		PersonService personService = Context.getPersonService();
		
		PersonAttributeType personAttributeType = personService.getPersonAttributeType(1);
		personService.purgePersonAttributeType(personAttributeType);
		
		PersonAttributeType deletedPersonAttributeType = personService.getPersonAttributeType(1);
		Assert.assertNull(deletedPersonAttributeType);
	}
	
	/**
	 * @see {@link PersonService#purgeRelationship(Relationship)}
	 * 
	 */
	@Test
	@Verifies(value = "should delete relationship from the database", method = "purgeRelationship(Relationship)")
	public void purgeRelationship_shouldDeleteRelationshipFromTheDatabase() throws Exception {
		PersonService personService = Context.getPersonService();
		
		Relationship relationship = personService.getRelationship(1);
		personService.purgeRelationship(relationship);
		
		Relationship deletedRelationship = personService.getRelationship(1);
		Assert.assertNull(deletedRelationship);
	}
	
	/**
	 * @see {@link PersonService#purgeRelationshipType(RelationshipType)}
	 * 
	 */
	@Test
	@Verifies(value = "should delete relationship type from the database", method = "purgeRelationshipType(RelationshipType)")
	public void purgeRelationshipType_shouldDeleteRelationshipTypeFromTheDatabase() throws Exception {
		PersonService personService = Context.getPersonService();
		
		RelationshipType relationshipType = personService.getRelationshipType(1);
		personService.purgeRelationshipType(relationshipType);
		
		RelationshipType deletedRelationshipType = personService.getRelationshipType(1);
		Assert.assertNull(deletedRelationshipType);
	}
	
	/**
	 * @see {@link PersonService#savePerson(Person)}
	 * 
	 */
	@Test
	@Verifies(value = "should create new object when person id is null", method = "savePerson(Person)")
	public void savePerson_shouldCreateNewObjectWhenPersonIdIsNull() throws Exception {
		User user = Context.getAuthenticatedUser();
		Person person = new Person();
		person.setPersonCreator(user);
		person.setPersonDateCreated(new Date());
		person.setPersonChangedBy(user);
		person.setPersonDateChanged(new Date());
		person.setGender("F");
		Assert.assertNull(person.getId());
		Person personSaved = Context.getPersonService().savePerson(person);
		Assert.assertNotNull(personSaved.getId());
	}
	
	/**
	 * @see {@link PersonService#savePerson(Person)}
	 * 
	 */
	@Test
	@Verifies(value = "should update existing object when person id is not null", method = "savePerson(Person)")
	public void savePerson_shouldUpdateExistingObjectWhenPersonIdIsNotNull() throws Exception {
		Person personSaved = Context.getPersonService().getPerson(1);
		Assert.assertNotNull(personSaved.getId());
		personSaved.setGender("M");
		Person personUpdated = Context.getPersonService().savePerson(personSaved);
		Assert.assertEquals("M", personUpdated.getGender());
	}
	
	/**
	 * @see {@link PersonService#saveRelationship(Relationship)}
	 * 
	 */
	@Test
	@Verifies(value = "should create new object when relationship id is null", method = "saveRelationship(Relationship)")
	public void saveRelationship_shouldCreateNewObjectWhenRelationshipIdIsNull() throws Exception {
		PersonService personService = Context.getPersonService();
		
		Relationship relationship = new Relationship();
		relationship.setPersonA(personService.getPerson(1));
		relationship.setPersonB(personService.getPerson(2));
		relationship.setRelationshipType(personService.getRelationshipType(1));
		Assert.assertNull(relationship.getRelationshipId());
		Relationship savedRelationship = personService.saveRelationship(relationship);
		Assert.assertNotNull(savedRelationship.getRelationshipId());
	}
	
	/**
	 * @see {@link PersonService#saveRelationship(Relationship)}
	 * 
	 */
	@Test
	@Verifies(value = "should update existing object when relationship id is not null", method = "saveRelationship(Relationship)")
	public void saveRelationship_shouldUpdateExistingObjectWhenRelationshipIdIsNotNull() throws Exception {
		PersonService personService = Context.getPersonService();
		
		Relationship savedRelationship = personService.getRelationship(1);
		Assert.assertNotNull(savedRelationship.getRelationshipId());
		
		savedRelationship.setRelationshipType(personService.getRelationshipType(2));
		Relationship updatedRelationship = personService.saveRelationship(savedRelationship);
		Assert.assertEquals(personService.getRelationshipType(2), updatedRelationship.getRelationshipType());
	}
	
	/**
	 * @see {@link PersonService#saveRelationshipType(RelationshipType)}
	 * 
	 */
	@Test
	@Verifies(value = "should create new object when relationship type id is null", method = "saveRelationshipType(RelationshipType)")
	public void saveRelationshipType_shouldCreateNewObjectWhenRelationshipTypeIdIsNull() throws Exception {
		RelationshipType relationshipType = new RelationshipType();
		relationshipType.setDescription("Test relationship");
		relationshipType.setaIsToB("Sister");
		relationshipType.setbIsToA("Brother");
		Assert.assertNull(relationshipType.getRelationshipTypeId());
		RelationshipType savedRelationshipType = personService.saveRelationshipType(relationshipType);
		Assert.assertNotNull(savedRelationshipType.getRelationshipTypeId());
	}
	
	/**
	 * @see {@link PersonService#saveRelationshipType(RelationshipType)}
	 * 
	 */
	@Test
	@Verifies(value = "should update existing object when relationship type id is not null", method = "saveRelationshipType(RelationshipType)")
	public void saveRelationshipType_shouldUpdateExistingObjectWhenRelationshipTypeIdIsNotNull() throws Exception {
		RelationshipType savedRelationshipType = Context.getPersonService().getRelationshipType(1);
		Assert.assertNotNull(savedRelationshipType.getRelationshipTypeId());
		
		savedRelationshipType.setPreferred(true);
		RelationshipType updatedRelationshipType = personService.saveRelationshipType(savedRelationshipType);
		Assert.assertEquals(true, updatedRelationshipType.isPreferred());
	}
	
	/**
	 * @see {@link PersonService#unvoidPerson(Person)}
	 * TODO NullPointerException during RequiredDataAdvice.before()
	 * TODO Should we be able to unvoid an already not voided record?  This test assumes yes. 
	 * 
	 */
	@Test
	@Ignore
	@Verifies(value = "should unvoid the given person", method = "unvoidPerson(Person)")
	public void unvoidPerson_shouldUnvoidTheGivenPerson() throws Exception {
		executeDataSet("org/openmrs/api/include/PersonServiceTest-createPersonPurgeVoidTest.xml");
		Person person = Context.getPersonService().getPerson(1002);
		
		//Assert.assertTrue(person.isVoided());
		//Assert.assertNotNull(person.getDateVoided());
		
		Person unvoidedPerson = Context.getPersonService().unvoidPerson(person);
		
		Assert.assertFalse(unvoidedPerson.isVoided());
		Assert.assertNull(unvoidedPerson.getVoidedBy());
		Assert.assertNull(unvoidedPerson.getPersonVoidReason());
		Assert.assertNull(unvoidedPerson.getPersonDateVoided());
	}
	
	/**
	 * @see {@link PersonService#unvoidRelationship(Relationship)}
	 * 
	 */
	@Test
	@Verifies(value = "should unvoid voided relationship", method = "unvoidRelationship(Relationship)")
	public void unvoidRelationship_shouldUnvoidVoidedRelationship() throws Exception {
		Relationship relationship = Context.getPersonService().getRelationship(1);
		Relationship voidedRelationship = Context.getPersonService().voidRelationship(relationship,
		    "Test Voiding Relationship");
		
		Assert.assertTrue(voidedRelationship.isVoided());
		Assert.assertNotNull(voidedRelationship.getVoidedBy());
		Assert.assertNotNull(voidedRelationship.getVoidReason());
		Assert.assertNotNull(voidedRelationship.getDateVoided());
		
		Relationship unvoidedRelationship = Context.getPersonService().unvoidRelationship(voidedRelationship);
		
		Assert.assertFalse(unvoidedRelationship.isVoided());
		Assert.assertNull(unvoidedRelationship.getVoidedBy());
		Assert.assertNull(unvoidedRelationship.getVoidReason());
		Assert.assertNull(unvoidedRelationship.getDateVoided());
	}
	
	/**
	 * @see {@link PersonService#voidPerson(Person,String)}
	 * 
	 */
	@Test
	@Ignore
	// TODO Fix NullPointerException that occurs in RequiredDataAdvice
	@Verifies(value = "should return voided person with given reason", method = "voidPerson(Person,String)")
	public void voidPerson_shouldReturnVoidedPersonWithGivenReason() throws Exception {
		executeDataSet("org/openmrs/api/include/PersonServiceTest-createPersonPurgeVoidTest.xml");
		Person person = Context.getPersonService().getPerson(1001);
		Person voidedPerson = Context.getPersonService().voidPerson(person, "Test Voiding Person");
		
		Assert.assertTrue(voidedPerson.isVoided());
		Assert.assertNotNull(voidedPerson.getVoidedBy());
		Assert.assertNotNull(voidedPerson.getPersonVoidReason());
		Assert.assertNotNull(voidedPerson.getPersonDateVoided());
		Assert.assertEquals(voidedPerson.getPersonVoidReason(), "Test Voiding Reason");
	}
	
	/**
	 * @see {@link PersonService#voidRelationship(Relationship,String)}
	 * 
	 */
	@Test
	@Verifies(value = "should void relationship with the given reason", method = "voidRelationship(Relationship,String)")
	public void voidRelationship_shouldVoidRelationshipWithTheGivenReason() throws Exception {
		Relationship relationship = Context.getPersonService().getRelationship(1);
		Relationship voidedRelationship = Context.getPersonService().voidRelationship(relationship,
		    "Test Voiding Relationship");
		
		Assert.assertTrue(voidedRelationship.isVoided());
		Assert.assertNotNull(voidedRelationship.getVoidedBy());
		Assert.assertNotNull(voidedRelationship.getVoidReason());
		Assert.assertNotNull(voidedRelationship.getDateVoided());
		Assert.assertEquals(voidedRelationship.getVoidReason(), "Test Voiding Relationship");
	}
	
	/**
	 * @see {@link PersonService#getPersonAddressByUuid(String)}
	 * 
	 */
	@Test
	@Verifies(value = "should find object given valid uuid", method = "getPersonAddressByUuid(String)")
	public void getPersonAddressByUuid_shouldFindObjectGivenValidUuid() throws Exception {
		String uuid = "3350d0b5-821c-4e5e-ad1d-a9bce331e118";
		PersonAddress personAddress = Context.getPersonService().getPersonAddressByUuid(uuid);
		Assert.assertEquals(2, (int) personAddress.getPersonAddressId());
	}
	
	/**
	 * @see {@link PersonService#getPersonAddressByUuid(String)}
	 * 
	 */
	@Test
	@Verifies(value = "should return null if no object found with given uuid", method = "getPersonAddressByUuid(String)")
	public void getPersonAddressByUuid_shouldReturnNullIfNoObjectFoundWithGivenUuid() throws Exception {
		Assert.assertNull(Context.getPersonService().getPersonAddressByUuid("some invalid uuid"));
	}
	
	/**
	 * @see {@link PersonService#getPersonAttributeByUuid(String)}
	 * 
	 */
	@Test
	@Verifies(value = "should find object given valid uuid", method = "getPersonAttributeByUuid(String)")
	public void getPersonAttributeByUuid_shouldFindObjectGivenValidUuid() throws Exception {
		String uuid = "0768f3da-b692-44b7-a33f-abf2c450474e";
		PersonAttribute person = Context.getPersonService().getPersonAttributeByUuid(uuid);
		Assert.assertEquals(1, (int) person.getPersonAttributeId());
	}
	
	/**
	 * @see {@link PersonService#getPersonAttributeByUuid(String)}
	 * 
	 */
	@Test
	@Verifies(value = "should return null if no object found with given uuid", method = "getPersonAttributeByUuid(String)")
	public void getPersonAttributeByUuid_shouldReturnNullIfNoObjectFoundWithGivenUuid() throws Exception {
		Assert.assertNull(Context.getPersonService().getPersonAttributeByUuid("some invalid uuid"));
	}
	
	/**
	 * @see {@link PersonService#getPersonAttributeTypeByUuid(String)}
	 * 
	 */
	@Test
	@Verifies(value = "should find object given valid uuid", method = "getPersonAttributeTypeByUuid(String)")
	public void getPersonAttributeTypeByUuid_shouldFindObjectGivenValidUuid() throws Exception {
		String uuid = "b3b6d540-a32e-44c7-91b3-292d97667518";
		PersonAttributeType personAttributeType = Context.getPersonService().getPersonAttributeTypeByUuid(uuid);
		Assert.assertEquals(1, (int) personAttributeType.getPersonAttributeTypeId());
	}
	
	/**
	 * @see {@link PersonService#getPersonAttributeTypeByUuid(String)}
	 * 
	 */
	@Test
	@Verifies(value = "should return null if no object found with given uuid", method = "getPersonAttributeTypeByUuid(String)")
	public void getPersonAttributeTypeByUuid_shouldReturnNullIfNoObjectFoundWithGivenUuid() throws Exception {
		Assert.assertNull(Context.getPersonService().getPersonAttributeTypeByUuid("some invalid uuid"));
	}
	
	/**
	 * @see {@link PersonService#getPersonByUuid(String)}
	 * 
	 */
	@Test
	@Verifies(value = "should find object given valid uuid", method = "getPersonByUuid(String)")
	public void getPersonByUuid_shouldFindObjectGivenValidUuid() throws Exception {
		String uuid = "ba1b19c2-3ed6-4f63-b8c0-f762dc8d7562";
		Person person = Context.getPersonService().getPersonByUuid(uuid);
		Assert.assertEquals(1, (int) person.getPersonId());
	}
	
	/**
	 * @see {@link PersonService#getPersonByUuid(String)}
	 * 
	 */
	@Test
	@Verifies(value = "should return null if no object found with given uuid", method = "getPersonByUuid(String)")
	public void getPersonByUuid_shouldReturnNullIfNoObjectFoundWithGivenUuid() throws Exception {
		Assert.assertNull(Context.getPersonService().getPersonByUuid("some invalid uuid"));
	}
	
	/**
	 * @see {@link PersonService#getPersonNameByUuid(String)}
	 * 
	 */
	@Test
	@Verifies(value = "should find object given valid uuid", method = "getPersonNameByUuid(String)")
	public void getPersonNameByUuid_shouldFindObjectGivenValidUuid() throws Exception {
		String uuid = "399e3a7b-6482-487d-94ce-c07bb3ca3cc7";
		PersonName personName = Context.getPersonService().getPersonNameByUuid(uuid);
		Assert.assertEquals(2, (int) personName.getPersonNameId());
	}
	
	/**
	 * @see {@link PersonService#getPersonNameByUuid(String)}
	 * 
	 */
	@Test
	@Verifies(value = "should return null if no object found with given uuid", method = "getPersonNameByUuid(String)")
	public void getPersonNameByUuid_shouldReturnNullIfNoObjectFoundWithGivenUuid() throws Exception {
		Assert.assertNull(Context.getPersonService().getPersonNameByUuid("some invalid uuid"));
	}
	
	/**
	 * @see {@link PersonService#getRelationshipByUuid(String)}
	 * 
	 */
	@Test
	@Verifies(value = "should find object given valid uuid", method = "getRelationshipByUuid(String)")
	public void getRelationshipByUuid_shouldFindObjectGivenValidUuid() throws Exception {
		String uuid = "c18717dd-5d78-4a0e-84fc-ee62c5f0676a";
		Relationship relationship = Context.getPersonService().getRelationshipByUuid(uuid);
		Assert.assertEquals(1, (int) relationship.getRelationshipId());
	}
	
	/**
	 * @see {@link PersonService#getRelationshipByUuid(String)}
	 * 
	 */
	@Test
	@Verifies(value = "should return null if no object found with given uuid", method = "getRelationshipByUuid(String)")
	public void getRelationshipByUuid_shouldReturnNullIfNoObjectFoundWithGivenUuid() throws Exception {
		Assert.assertNull(Context.getPersonService().getRelationshipByUuid("some invalid uuid"));
	}
	
	/**
	 * @see {@link PersonService#getRelationshipTypeByUuid(String)}
	 * 
	 */
	@Test
	@Verifies(value = "should find object given valid uuid", method = "getRelationshipTypeByUuid(String)")
	public void getRelationshipTypeByUuid_shouldFindObjectGivenValidUuid() throws Exception {
		String uuid = "6d9002ea-a96b-4889-af78-82d48c57a110";
		RelationshipType relationshipType = Context.getPersonService().getRelationshipTypeByUuid(uuid);
		Assert.assertEquals(1, (int) relationshipType.getRelationshipTypeId());
	}
	
	/**
	 * @see {@link PersonService#getRelationshipTypeByUuid(String)}
	 * 
	 */
	@Test
	@Verifies(value = "should return null if no object found with given uuid", method = "getRelationshipTypeByUuid(String)")
	public void getRelationshipTypeByUuid_shouldReturnNullIfNoObjectFoundWithGivenUuid() throws Exception {
		Assert.assertNull(Context.getPersonService().getRelationshipTypeByUuid("some invalid uuid"));
	}
	
	/**
	 * @see PersonService#parsePersonName(String)
	 * @verifies not fail when ending with whitespace
	 */
	@Test
	public void parsePersonName_shouldNotFailWhenEndingWithWhitespace() throws Exception {
		PersonName pname = Context.getPersonService().parsePersonName("John ");
		assertEquals("John", pname.getGivenName());
	}
	
	/**
	 * @see PersonService#parsePersonName(String)
	 * @verifies not fail when ending with a comma
	 */
	@Test
	public void parsePersonName_shouldNotFailWhenEndingWithAComma() throws Exception {
		PersonName pname = Context.getPersonService().parsePersonName("John,");
		assertEquals("John", pname.getGivenName());
		
	}
	
	/**
	 * @see PersonService#parsePersonName(String)
	 * @verifies parse four person name
	 */
	@Test
	public void parsePersonName_shouldParseFourPersonName() throws Exception {
		PersonName pname = Context.getPersonService().parsePersonName("John David Alex Smith");
		assertEquals("John", pname.getGivenName());
		assertEquals("David", pname.getMiddleName());
		assertEquals("Alex", pname.getFamilyName());
		assertEquals("Smith", pname.getFamilyName2());
	}
	
	/**
	 * @see {@link PersonService#voidPersonName(org.openmrs.PersonName, String)}
	 */
	@Test
	@Verifies(value = "should void personName with the given reason", method = "voidPersonName(PersonName)")
	public void voidPersonName_shouldVoidPersonNameWithTheGivenReason() throws Exception {
		executeDataSet("org/openmrs/api/include/PersionServiceTest-voidUnvoidPersonName.xml");
		PersonName personName = Context.getPersonService().getPersonNameByUuid("5e6571cc-c7f2-41de-b289-f55f8fe79c6f");
		
		Assert.assertFalse(personName.isVoided());
		
		PersonName voidedPersonName = Context.getPersonService().voidPersonName(personName, "Test Voiding PersonName");
		
		Assert.assertTrue(voidedPersonName.isVoided());
		Assert.assertNotNull(voidedPersonName.getVoidedBy());
		Assert.assertNotNull(voidedPersonName.getDateVoided());
		Assert.assertEquals(voidedPersonName.getVoidReason(), "Test Voiding PersonName");
	}
	
	/**
	 * @see {@link PersonService#unvoidPersonName(org.openmrs.PersonName)}
	 */
	@Test
	@Verifies(value = "should unvoid voided personName", method = "unvoidPersonName(PersonName)")
	public void unvoidPersonName_shouldUnvoidVoidedPersonName() throws Exception {
		executeDataSet("org/openmrs/api/include/PersionServiceTest-voidUnvoidPersonName.xml");
		PersonName voidedPersonName = Context.getPersonService().getPersonNameByUuid("a6ghgh7e-1384-493a-a55b-d325924acd94");
		
		Assert.assertTrue(voidedPersonName.isVoided());
		
		PersonName unvoidedPersonName = Context.getPersonService().unvoidPersonName(voidedPersonName);
		
		Assert.assertFalse(unvoidedPersonName.isVoided());
		Assert.assertNull(unvoidedPersonName.getVoidedBy());
		Assert.assertNull(unvoidedPersonName.getDateVoided());
		Assert.assertNull(unvoidedPersonName.getVoidReason());
		
	}
	
	/**
	 * @throws APIException
	 * @see {@link PersonService#savePersonName(org.openmrs.PersonName)}
	 */
	@Test(expected = APIException.class)
	@Verifies(value = "should fail if you try to void the last non voided name", method = "savePersonName(PersonName)")
	public void savePersonName_shouldFailIfYouTryToVoidTheLastNonVoidedName() throws Exception {
		executeDataSet("org/openmrs/api/include/PersionServiceTest-voidUnvoidPersonName.xml");
		PersonName personName = Context.getPersonService().getPersonNameByUuid("39ghgh7b-6482-487d-94ce-c07bb3ca3cc1");
		Assert.assertFalse(personName.isVoided());
		Context.getPersonService().voidPersonName(personName, "Test Voiding PersonName");
	}
}
