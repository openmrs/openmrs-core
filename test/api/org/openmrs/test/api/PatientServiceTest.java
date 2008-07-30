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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.GlobalProperty;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.PersonAddress;
import org.openmrs.PersonName;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.InvalidCheckDigitException;
import org.openmrs.api.LocationService;
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
	protected LocationService locationService = null;
	
	
	@Before
	public void runBeforeEachTest() throws Exception {
		initializeInMemoryDatabase();
		authenticate();
		
		if (ps == null) {
			ps = Context.getPatientService();
			adminService = Context.getAdministrationService();
			locationService = Context.getLocationService();
		}
	}

	/**
	 * Tests creation of a patient and then subsequent fetching of that
	 * patient by internal id
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldGetPatient() throws Exception {
		
		executeDataSet(CREATE_PATIENT_XML);
		
		List<Patient> patientList = ps.getPatients(null, "???", null);
		assertNotNull("an empty list should be returned instead of a null object", patientList);
		assertTrue("There shouldn't be any patients with this weird identifier", patientList.size() == 0);
		
		// make sure there is no identifier regex defined
		GlobalProperty prop = new GlobalProperty("patient.identifierRegex", "");
		Context.getAdministrationService().saveGlobalProperty(prop);
		patientList = ps.getPatients(null, "1234", null);
		assertTrue("There should be at least one patient found with this identifier", patientList.size() > 0);
		
		// try the same search with a regex defined
		prop.setPropertyValue("^0*@SEARCH@([A-Z]+-[0-9])?$");
		Context.getAdministrationService().saveGlobalProperty(prop);
		patientList = ps.getPatients(null, "1234", null);
		assertTrue("There should be at least one patient found with this identifier", patientList.size() > 0);
		
		// get a patient by id
		Patient patient = ps.getPatient(-1);
		assertNull("There should be no patients with a patient_id of negative 1", patient);
		
		patient = ps.getPatient(2);
		assertNotNull("There should be a patient with patient_id of 2", patient);
		
		
		patient.setGender("F");
		ps.savePatient(patient);
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
	@Test
	public void shouldCreatePatient() throws Exception {
		executeDataSet(CREATE_PATIENT_XML);
		
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
	@Test
	public void shouldCreatePatientWithValidatedIdentifier() throws Exception{
		executeDataSet(CREATE_PATIENT_VALID_IDENT_XML);
		Patient patient = createBasicPatient();
		Patient patient2 = createBasicPatient();
		
		PatientIdentifierType pit = ps.getPatientIdentifierType(1);
		PatientIdentifier ident1 = new PatientIdentifier("123-1", pit, locationService.getLocation(0));
		PatientIdentifier ident2 = new PatientIdentifier("123", pit, locationService.getLocation(0));
		PatientIdentifier ident3 = new PatientIdentifier("123-0", pit, locationService.getLocation(0));
		PatientIdentifier ident4 = new PatientIdentifier("123-A", pit, locationService.getLocation(0));
		
		try{
			patient.addIdentifier(ident1);
			ps.savePatient(patient);
			fail("Patient creation should have failed with identifier " + ident1.getIdentifier()	);
		}catch(InvalidCheckDigitException ex){		}

		patient.removeIdentifier(ident1);
		
		try{
			patient.addIdentifier(ident2);
			ps.savePatient(patient);
			fail("Patient creation should have failed with identifier " + ident2.getIdentifier()	);
		}catch(InvalidCheckDigitException ex){		}
		
		patient.removeIdentifier(ident2);

		try{
			patient.addIdentifier(ident3);
			ps.savePatient(patient);
			ps.purgePatient(patient);
			patient.removeIdentifier(ident3);
			patient2.addIdentifier(ident4);
			ps.savePatient(patient2);
		}catch(InvalidCheckDigitException ex){
			fail("Patient creation should have worked with identifiers " + ident3.getIdentifier() + " and " + ident4.getIdentifier());
		}
	}
	
	/**
	 * Gets the first patient, then sees if it can get that patient by its identifier as well
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldGetPatientsByIdentifier() throws Exception {
		
		executeDataSet(CREATE_PATIENT_XML);
		
		// get the first patient
		Collection<Patient> johnPatients = ps.getPatients("John", null, null);
		assertNotNull("There should be a patient named 'John'", johnPatients);
		assertFalse("There should be a patient named 'John'", johnPatients.isEmpty());
		
		Patient firstJohnPatient = johnPatients.iterator().next();
		
		// get a list of patients with this identifier, make sure the john patient is actually there
		String identifier = firstJohnPatient.getPatientIdentifier().getIdentifier();
		assertNotNull("Uh oh, the patient doesn't have an identifier", identifier);
		List<Patient> patients = ps.getPatients(null, identifier, null);
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
	@Test
	public void shouldGetPatientsByNameShouldLimitSize() throws Exception {
		executeDataSet(JOHN_PATIENTS_XML);
		
		Collection<Patient> patients = ps.getPatients("John", null, null);
		
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
	 * 
	 * Tests the findPatients method.
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldFindPatients() throws Exception {
		executeDataSet(FIND_PATIENTS_XML);
		
		//Test that "Jea" finds given_name="Jean Claude" and given_name="Jean", family_name="Claude"
		//and given_name="Jeannette" family_name="Claudent"
		//but not given_name="John" family_name="Claudio"
		Collection<Patient> pset = ps.getPatients("Jea", null, null);
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
		pset = ps.getPatients("Jean Claude", null, null);
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
				
		pset = ps.getPatients("I am voided", null, null);
		assertEquals(pset.size(), 0);
		
	}
	
	/**
	 * Test the PatientService.getPatients(String, String, List) method with both an identifier and
	 * an identifiertype
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldGetPatientsByIdentifierAndIdentifierType() throws Exception {
		executeDataSet(FIND_PATIENTS_XML);
		
		List<PatientIdentifierType> types = new Vector<PatientIdentifierType>();
		types.add(new PatientIdentifierType(1));
		
		// make sure we get back only one patient
		List<Patient> patients = Context.getPatientService().getPatients(null, "1234", types);
		assertEquals(1, patients.size());
		
		// make sure we get back only one patient
		patients = Context.getPatientService().getPatients(null, "1234", null);
		assertEquals(1, patients.size());
		
		// make sure we get back only patient #2 and patient #5
		patients = Context.getPatientService().getPatients(null, null, types);
		assertEquals(2, patients.size());
		
	}
	
}
