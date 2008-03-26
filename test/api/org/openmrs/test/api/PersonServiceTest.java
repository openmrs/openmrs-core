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
package org.openmrs.test.api;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.Person;
import org.openmrs.PersonAddress;
import org.openmrs.PersonName;
import org.openmrs.Relationship;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.PatientService;
import org.openmrs.api.PersonService;
import org.openmrs.api.context.Context;
import org.openmrs.test.BaseContextSensitiveTest;

/**
 * This class tests methods in the PersonService class.
 * 
 * TODO: Test all methods in the PersonService class.
 */
public class PersonServiceTest extends BaseContextSensitiveTest {

	protected static final String CREATE_PATIENT_XML = "org/openmrs/test/api/include/PatientServiceTest-createPatient.xml";
	protected static final String CREATE_RELATIONSHIP_XML = "org/openmrs/test/api/include/PersonServiceTest-createRelationship.xml";

	protected PatientService ps = null;
	protected AdministrationService adminService = null;
	protected PersonService personService = null;

	@Override
	protected void onSetUpInTransaction() throws Exception {
		initializeInMemoryDatabase();
		authenticate();

		if (ps == null) {
			ps = Context.getPatientService();
			adminService = Context.getAdministrationService();
			personService = Context.getPersonService();
		}
	}

	/**
	 * Tests a voided relationship between personA and Person B to see if it is
	 * still listed when retrieving unvoided relationships for personA and if it is
	 * still listed when retrieving unvoided relationships for personB.
	 * 
	 * @throws Exception
	 */
	public void testGetUnvoidedRelationships() throws Exception {
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
		List<PatientIdentifierType> patientIdTypes = ps.getPatientIdentifierTypes();
		assertNotNull(patientIdTypes);
		PatientIdentifier patientIdentifier = new PatientIdentifier();
		patientIdentifier.setIdentifier("123-0");
		patientIdentifier.setIdentifierType(patientIdTypes.get(0));
		Set<PatientIdentifier> patientIdentifiers = new TreeSet<PatientIdentifier>();
		patientIdentifiers.add(patientIdentifier);
		patient.setIdentifiers(patientIdentifiers);
		ps.createPatient(patient);
		Patient createdPatient = ps.getPatient(patient.getPatientId());

		// Create a sibling relationship between Patient#2 and Patient#3
		Relationship sibling = new Relationship();
		sibling.setPersonA(ps.getPatient(2));
		sibling.setPersonB(patient);
		sibling.setRelationshipType(personService.getRelationshipType(4));
		// relationship.setCreator(Context.getUserService().getUser(1));
		personService.createRelationship(sibling);

		// Make Patient#3 the Doctor of Patient#2.
		Relationship doctor = new Relationship();
		doctor.setPersonB(ps.getPatient(2));
		doctor.setPersonA(patient);
		doctor.setRelationshipType(personService.getRelationshipType(3));
		personService.createRelationship(doctor);

		// Get unvoided relationships before voiding any.
		Person p = personService.getPerson(2);
		List<Relationship> aRels = personService.getRelationships(p, false);
		List<Relationship> bRels = personService.getRelationships(patient,
		                                                          false);
		// Uncomment for console output.
		//System.out.println("Relationships before voiding all:");
		//System.out.println(aRels);
		//System.out.println(bRels);

		// Void all relationships.
		List<Relationship> allRels = personService.getRelationships();
		for (Relationship r : allRels) {
			personService.voidRelationship(r, "Because of a JUnit test.");
		}

		// Get unvoided relationships after voiding all of them.
		List<Relationship> updatedARels = personService.getRelationships(p,
		                                                                 false);
		List<Relationship> updatedBRels = personService.getRelationships(patient,
		                                                                 false);
		// Uncomment for console output
		//System.out.println("Relationships after voiding all:");
		//System.out.println(updatedARels);
		//System.out.println(updatedBRels);

		// Neither Patient#2 or Patient#3 should have any relationships now.
		assertEquals(updatedARels, updatedBRels);
	}

}
