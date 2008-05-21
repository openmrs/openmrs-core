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

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.PersonAddress;
import org.openmrs.PersonName;
import org.openmrs.User;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.EncounterService;
import org.openmrs.api.InvalidCheckDigitException;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.test.BaseContextSensitiveTest;

/**
 * This class tests methods in the PatientService class
 * 
 * TODO Add methods to test all methods in PatientService class
 */
public class PatientServiceTest extends BaseContextSensitiveTest {
	
	protected static final String CREATE_PATIENT_XML = "org/openmrs/test/api/include/PatientServiceTest-createPatient.xml";
	protected static final String CREATE_PATIENT_VALID_IDENT_XML = "org/openmrs/test/api/include/PatientServiceTest-createPatientValidIdent.xml";
	protected static final String JOHN_PATIENTS_XML = "org/openmrs/test/api/include/PatientServiceTest-lotsOfJohns.xml";
	protected static final String USERS_WHO_ARE_PATIENTS_XML = "org/openmrs/test/api/include/PatientServiceTest-usersWhoArePatients.xml";
	protected static final String FIND_PATIENTS_XML = "org/openmrs/test/api/include/PatientServiceTest-findPatients.xml";
	
	protected PatientService ps = null; 
	protected AdministrationService adminService = null;
	protected EncounterService encounterService = null;
	
	@Override
	protected void onSetUpInTransaction() throws Exception {
		initializeInMemoryDatabase();
		authenticate();
		
		if (ps == null) {
			ps = Context.getPatientService();
			adminService = Context.getAdministrationService();
			encounterService = Context.getEncounterService();
		}
	}

	/**
	 * Tests creation of a patient and then subsequent fetching of that
	 * patient by internal id
	 * 
	 * @throws Exception
	 */
	public void testGetPatient() throws Exception {
		
		executeDataSet(CREATE_PATIENT_XML);
		
		Set<Patient> patientList = ps.getPatientsByIdentifier("???", true);
		assertNotNull("an empty list should be returned instead of a null object", patientList);
		assertTrue("There shouldn't be any patients with this weird identifier", patientList.size() == 0);
		
		patientList = ps.getPatientsByIdentifier("1234", true);
		assertTrue("There should be at least one patient found with this identifier", patientList.size() > 0);
		
		// get a patient by id
		Patient patient = ps.getPatient(-1);
		assertNull("There should be no patients with a patient_id of negative 1", patient);
		
		patient = ps.getPatient(2);
		assertNotNull("There should be a patient with patient_id of 2", patient);
		
		
		patient.setGender("F");
		ps.updatePatient(patient);
		Patient patient2 = ps.getPatient(patient.getPatientId());
		assertTrue("The updated patient and the orig patient should still be equal", patient.equals(patient2));
		
		assertTrue("The gender should be new", patient2.getGender().equals("F"));	
	}
	
	private Patient createBasicPatient(){
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
		//patient.removeAddress(pAddress);
		
		patient.setDeathDate(new Date());
		//patient.setCauseOfDeath("air");
		patient.setBirthdate(new Date());
		patient.setBirthdateEstimated(true);
		patient.setGender("male");	
		
		return patient;
	}
	
	/**
	 * 
	 * Tests creating a patient 
	 * 
	 * @throws Exception
	 */
	public void testCreatePatient() throws Exception {
		executeDataSet(CREATE_PATIENT_XML);
		
		Patient patient = createBasicPatient();
		
		List<PatientIdentifierType> patientIdTypes = ps.getPatientIdentifierTypes();
		assertNotNull(patientIdTypes);
		PatientIdentifier patientIdentifier = new PatientIdentifier();
		patientIdentifier.setIdentifier("123-0");
		patientIdentifier.setIdentifierType(patientIdTypes.get(0));
		patientIdentifier.setLocation(encounterService.getLocations().get(0));
		
		Set<PatientIdentifier> patientIdentifiers = new TreeSet<PatientIdentifier>();
		patientIdentifiers.add(patientIdentifier);
		
		patient.setIdentifiers(patientIdentifiers);
		
		ps.createPatient(patient);
		Patient createdPatient = ps.getPatient(patient.getPatientId());
		assertNotNull(createdPatient);
		
		assertNotNull(createdPatient.getPatientId());
		
		Patient createdPatientById = ps.getPatient(createdPatient.getPatientId());
		assertNotNull(createdPatientById);
		
	}
	
	/**
	 * Tests creating patients with identifiers that are or are not validated.
	 * @throws Exception 
	 */
	public void testCreatePatientWithValidatedIdentifier() throws Exception{
		executeDataSet(CREATE_PATIENT_VALID_IDENT_XML);
		Patient patient = createBasicPatient();
		Patient patient2 = createBasicPatient();
		
		PatientIdentifierType pit = ps.getPatientIdentifierType(1);
		PatientIdentifier ident1 = new PatientIdentifier("123-1", pit, encounterService.getLocation(0));
		PatientIdentifier ident2 = new PatientIdentifier("123", pit, encounterService.getLocation(0));
		PatientIdentifier ident3 = new PatientIdentifier("123-0", pit, encounterService.getLocation(0));
		PatientIdentifier ident4 = new PatientIdentifier("123-A", pit, encounterService.getLocation(0));
		
		try{
			patient.addIdentifier(ident1);
			ps.createPatient(patient);
			fail("Patient creation should have failed with identifier " + ident1.getIdentifier()	);
		}catch(InvalidCheckDigitException ex){		}

		patient.removeIdentifier(ident1);
		
		try{
			patient.addIdentifier(ident2);
			ps.createPatient(patient);
			fail("Patient creation should have failed with identifier " + ident2.getIdentifier()	);
		}catch(InvalidCheckDigitException ex){		}
		
		patient.removeIdentifier(ident2);

		try{
			patient.addIdentifier(ident3);
			ps.createPatient(patient);
			ps.deletePatient(patient);
			patient.removeIdentifier(ident3);
			patient2.addIdentifier(ident4);
			ps.createPatient(patient2);
		}catch(InvalidCheckDigitException ex){
			fail("Patient creation should have worked with identifiers " + ident3.getIdentifier() + " and " + ident4.getIdentifier());
		}
	}
	
	/**
	 * Gets the first patient, then sees if it can get that patient by its identifier as well
	 * 
	 * @throws Exception
	 */
	public void testGetPatientsByIdentifier() throws Exception {
		
		executeDataSet(CREATE_PATIENT_XML);
		
		// get the first patient
		Collection<Patient> johnPatients = ps.getPatientsByName("John");
		assertNotNull("There should be a patient named 'John'", johnPatients);
		assertFalse("There should be a patient named 'John'", johnPatients.isEmpty());
		
		Patient firstJohnPatient = johnPatients.iterator().next();
		
		// get a list of patients with this identifier, make sure the john patient is actually there
		String identifier = firstJohnPatient.getPatientIdentifier().getIdentifier();
		assertNotNull("Uh oh, the patient doesn't have an identifier", identifier);
		Set<Patient> patients = ps.getPatientsByIdentifier(identifier, true);
		assertTrue("Odd. The firstJohnPatient isn't in the list of patients for this identifier", patients.contains(firstJohnPatient));
		
	}
	
//	/**
//	 * This method should be uncommented when you want to examine the actual hibernate
//	 * sql calls being made.  The calls that should be limiting the number of returned
//	 * patients should show a "top" or "limit" in the sql -- this proves hibernate's
//	 * use of a native sql limit as opposed to a java-only limit.  
//	 * 
//	 * Note: if enabled, this test will be considerably slower
//     * 
//     * @see org.openmrs.BaseContextSensitiveTest#getRuntimeProperties()
//     */
//    @Override
//    public Properties getRuntimeProperties() {
//	    Properties props = super.getRuntimeProperties();
//	    props.setProperty("hibernate.show_sql", "true");
//	    
//    	return props;
//    }

	/**
	 * Check that the patient list is kept under the max for getPatientsByName
	 * 
	 * @throws Exception
	 */
	public void testGetPatientsByNameShouldLimitSize() throws Exception {
		executeDataSet(JOHN_PATIENTS_XML);
		
		Collection<Patient> patients = ps.getPatientsByName("John");
		
		assertTrue("The patient list size should be restricted to under the max (1000). its " + patients.size(), patients.size() == 1000);
		
		/* Temporary code to create lots of johns file
		 * 
		File file = new File("test/api/" + JOHN_PATIENTS_XML);
		PrintWriter writer = new PrintWriter(file);

		int x = 3;
		while (x < 1010) {
			String line = "<person person_id=\"2\" dead=\"false\" creator=\"1\" date_created=\"1999-01-01 00:00:00.0\" voided=\"false\" gender=\"M\" />";
			writer.println(line.replaceAll("2", Integer.valueOf(x).toString()));

			line = "<person_name person_id=\"2\" person_name_id=\"2\" preferred=\"1\" creator=\"1\" date_created=\"1999-01-01 00:00:00.0\" voided=\"false\" given_name=\"John2\" middle_name=\" \" family_name=\"Patient\" />";
			writer.println(line.replaceAll("2", Integer.valueOf(x).toString()));
			
			line = "<patient patient_id=\"2\" creator=\"1\" date_created=\"1999-03-01 00:00:00.0\" voided=\"false\" />";
			writer.println(line.replaceAll("2", Integer.valueOf(x).toString()));
			
			line = "<patient_identifier patient_id=\"2\" creator=\"1\" date_created=\"1999-03-01 00:00:00.0\" identifier=\"2\" identifier_type=\"1\" preferred=\"1\" voided=\"false\" location_id=\"1\" />";
			writer.println(line.replaceAll("2", Integer.valueOf(x).toString()));

			x = x + 1;
		}

		writer.close();
		*/
	}
	
	/**
	 * Make sure the api can handle having a User object that is also a 
	 * patient and was previously loaded via hibernate 
	 * 
	 * @throws Exception
	 */
	public void testAllowUsersWhoArePatients() throws Exception {
		executeDataSet(USERS_WHO_ARE_PATIENTS_XML);
		
		// we must fetch this person first, because this person is
		// the creator of the next.  We ned to make sure hibernate isn't
		// caching and returning different person objects when it shouldn't be
		Patient patient2 = ps.getPatient(2);
		assertTrue("When getting a patient, it should be of the class patient, not: " + patient2.getClass(), patient2.getClass().equals(Patient.class));
		
		Patient patient3 = ps.getPatient(3);
		assertTrue("When getting a patient, it should be of the class patient, not: " + patient3.getClass(), patient3.getClass().equals(Patient.class));
		
		User user2 = Context.getUserService().getUser(2);
		assertTrue("When getting a user, it should be of the class user, not: " + user2.getClass(), user2.getClass().equals(User.class));
		
	}
	
	/**
	 * 
	 * Tests the findPatients method.
	 * 
	 * @throws Exception
	 */
	public void testFindPatients() throws Exception {
		executeDataSet(FIND_PATIENTS_XML);
		
		//Test that "Jea" finds given_name="Jean Claude" and given_name="Jean", family_name="Claude"
		//and given_name="Jeannette" family_name="Claudent"
		//but not given_name="John" family_name="Claudio"
		Collection<Patient> pset = ps.findPatients("Jea", false);
		boolean claudioFound = false;
		boolean jeanClaudeFound1 = false;
		boolean jeanClaudeFound2 = false;
		boolean jeannetteClaudentFound = false;
		for(Patient patient : pset){
			if(patient.getFamilyName().equals("Claudio"))
				claudioFound = true;
			if(patient.getGivenName().equals("Jean Claude"))
				jeanClaudeFound1 = true;
			if(patient.getGivenName().equals("Jean"))
				jeanClaudeFound2 = true;
			if(patient.getGivenName().equals("Jeannette"))
				jeannetteClaudentFound = true;
		}
		assertFalse(claudioFound);
		assertTrue(jeanClaudeFound1);
		assertTrue(jeanClaudeFound2);
		assertTrue(jeannetteClaudentFound);
		
		//Test that "Jean Claude" finds given_name="Jean Claude" and given_name="Jean", family_name="Claude"
		//and given_name="Jeannette" family_name="Claudent" but not
		//given_name="John" family_name="Claudio"
		pset = ps.findPatients("Jean Claude", false);
		claudioFound = false;
		jeanClaudeFound1 = false;
		jeanClaudeFound2 = false;
		jeannetteClaudentFound = false;
		for(Patient patient : pset){
			if(patient.getFamilyName().equals("Claudio"))
				claudioFound = true;
			if(patient.getGivenName().equals("Jean Claude"))
				jeanClaudeFound1 = true;
			if(patient.getGivenName().equals("Jean"))
				jeanClaudeFound2 = true;
			if(patient.getGivenName().equals("Jeannette"))
				jeannetteClaudentFound = true;
		}
		assertFalse(claudioFound);
		assertTrue(jeanClaudeFound1);
		assertTrue(jeanClaudeFound2);
		assertTrue(jeannetteClaudentFound);
		
		//Test the "include voided" option.
		pset = ps.findPatients("I am voided", true);
		assertEquals(pset.iterator().next().getFamilyName(), "voided");
		
		pset = ps.findPatients("I am voided", false);
		assertEquals(pset.size(), 0);
		
	}
}
