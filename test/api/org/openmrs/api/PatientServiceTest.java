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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openmrs.GlobalProperty;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.Person;
import org.openmrs.PersonAddress;
import org.openmrs.PersonAttribute;
import org.openmrs.PersonName;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.api.impl.PatientServiceImpl;
import org.openmrs.patient.IdentifierValidator;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.test.SkipBaseSetup;
import org.openmrs.test.TestUtil;
import org.openmrs.test.Verifies;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.util.OpenmrsUtil;

/**
 * This class tests methods in the PatientService class TODO Add methods to test all methods in
 * PatientService class
 */
public class PatientServiceTest extends BaseContextSensitiveTest {
	
	protected static final String CREATE_PATIENT_XML = "org/openmrs/api/include/PatientServiceTest-createPatient.xml";
	
	protected static final String CREATE_PATIENT_VALID_IDENT_XML = "org/openmrs/api/include/PatientServiceTest-createPatientValidIdent.xml";
	
	protected static final String JOHN_PATIENTS_XML = "org/openmrs/api/include/PatientServiceTest-lotsOfJohns.xml";
	
	protected static final String USERS_WHO_ARE_PATIENTS_XML = "org/openmrs/api/include/PatientServiceTest-usersWhoArePatients.xml";
	
	protected static final String FIND_PATIENTS_XML = "org/openmrs/api/include/PatientServiceTest-findPatients.xml";
	
	protected static PatientService patientService = null;
	
	protected static AdministrationService adminService = null;
	
	protected static LocationService locationService = null;
	
	/**
	 * Run this before each unit test in this class. The "@Before" method in
	 * {@link BaseContextSensitiveTest} is run right before this method.
	 * 
	 * @throws Exception
	 */
	@Before
	public void runBeforeAllTests() throws Exception {
		if (patientService == null) {
			patientService = Context.getPatientService();
			adminService = Context.getAdministrationService();
			locationService = Context.getLocationService();
		}
	}
	
	/**
	 * @see {@link PatientService#getAllIdentifierValidators()}
	 */
	@Test
	@Verifies(value = "should return all registered identifier validators", method = "getAllIdentifierValidators()")
	public void getAllIdentifierValidators_shouldReturnAllRegisteredIdentifierValidators() throws Exception {
		Collection<IdentifierValidator> expectedValidators = new HashSet<IdentifierValidator>();
		expectedValidators.add(patientService.getIdentifierValidator("org.openmrs.patient.impl.LuhnIdentifierValidator"));
		expectedValidators
		        .add(patientService.getIdentifierValidator("org.openmrs.patient.impl.VerhoeffIdentifierValidator"));
		Assert.assertEquals(2, patientService.getAllIdentifierValidators().size());
		TestUtil.assertCollectionContentsEquals(expectedValidators, patientService.getAllIdentifierValidators());
	}
	
	/**
	 * Tests creation of a patient and then subsequent fetching of that patient by internal id TODO:
	 * Split this into multiple tests, then un-ignore this
	 * 
	 * @throws Exception
	 */
	@Test
	@SkipBaseSetup
	@Ignore
	public void shouldGetPatient() throws Exception {
		initializeInMemoryDatabase();
		executeDataSet(CREATE_PATIENT_XML);
		authenticate();
		
		List<Patient> patientList = patientService.getPatients(null, "???", null, false);
		assertNotNull("an empty list should be returned instead of a null object", patientList);
		assertTrue("There shouldn't be any patients with this weird identifier", patientList.size() == 0);
		
		// make sure there is no identifier regex defined
		GlobalProperty prop = new GlobalProperty("patient.identifierRegex", "");
		Context.getAdministrationService().saveGlobalProperty(prop);
		patientList = patientService.getPatients(null, "1234", null, false);
		assertTrue("There should be at least one patient found with this identifier", patientList.size() > 0);
		
		// try the same search with a regex defined
		prop.setPropertyValue("^0*@SEARCH@([A-Z]+-[0-9])?$");
		Context.getAdministrationService().saveGlobalProperty(prop);
		patientList = patientService.getPatients(null, "1234", null, false);
		assertTrue("There should be at least one patient found with this identifier", patientList.size() > 0);
		
		// get a patient by id
		Patient patient = patientService.getPatient(-1);
		assertNull("There should be no patients with a patient_id of negative 1", patient);
		
		patient = patientService.getPatient(2);
		assertNotNull("There should be a patient with patient_id of 2", patient);
		
		patient.setGender("F");
		patientService.savePatient(patient);
		Patient patient2 = patientService.getPatient(patient.getPatientId());
		assertTrue("The updated patient and the orig patient should still be equal", patient.equals(patient2));
		
		assertTrue("The gender should be new", patient2.getGender().equals("F"));
	}
	
	/**
	 * Convenience method to have a Patient object with all required values filled in
	 * 
	 * @return a mock Patient object that can be saved
	 */
	private Patient createBasicPatient() {
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
		
		List<PatientIdentifierType> patientIdTypes = patientService.getAllPatientIdentifierTypes();
		assertNotNull(patientIdTypes);
		PatientIdentifier patientIdentifier = new PatientIdentifier();
		patientIdentifier.setIdentifier("123-0");
		patientIdentifier.setIdentifierType(patientIdTypes.get(0));
		patientIdentifier.setLocation(new Location(1));
		
		Set<PatientIdentifier> patientIdentifiers = new TreeSet<PatientIdentifier>();
		patientIdentifiers.add(patientIdentifier);
		
		patient.setIdentifiers(patientIdentifiers);
		
		patientService.savePatient(patient);
		Patient createdPatient = patientService.getPatient(patient.getPatientId());
		assertNotNull(createdPatient);
		
		assertNotNull(createdPatient.getPatientId());
		
		Patient createdPatientById = patientService.getPatient(createdPatient.getPatientId());
		assertNotNull(createdPatientById);
		
	}
	
	/**
	 * Tests creating patients with identifiers that are or are not validated.
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldCreatePatientWithValidatedIdentifier() throws Exception {
		executeDataSet(CREATE_PATIENT_VALID_IDENT_XML);
		Patient patient = createBasicPatient();
		Patient patient2 = createBasicPatient();
		
		PatientIdentifierType pit = patientService.getPatientIdentifierType(1);
		PatientIdentifier ident1 = new PatientIdentifier("123-1", pit, locationService.getLocation(0));
		PatientIdentifier ident2 = new PatientIdentifier("123", pit, locationService.getLocation(0));
		PatientIdentifier ident3 = new PatientIdentifier("123-0", pit, locationService.getLocation(0));
		PatientIdentifier ident4 = new PatientIdentifier("123-A", pit, locationService.getLocation(0));
		
		try {
			patient.addIdentifier(ident1);
			patientService.savePatient(patient);
			fail("Patient creation should have failed with identifier " + ident1.getIdentifier());
		}
		catch (InvalidCheckDigitException ex) {}
		
		patient.removeIdentifier(ident1);
		
		try {
			patient.addIdentifier(ident2);
			patientService.savePatient(patient);
			fail("Patient creation should have failed with identifier " + ident2.getIdentifier());
		}
		catch (InvalidCheckDigitException ex) {}
		
		patient.removeIdentifier(ident2);
		
		try {
			patient.addIdentifier(ident3);
			patientService.savePatient(patient);
			patientService.purgePatient(patient);
			patient.removeIdentifier(ident3);
			patient2.addIdentifier(ident4);
			patientService.savePatient(patient2);
		}
		catch (InvalidCheckDigitException ex) {
			fail("Patient creation should have worked with identifiers " + ident3.getIdentifier() + " and "
			        + ident4.getIdentifier());
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
		Collection<Patient> johnPatients = patientService.getPatients("John", null, null, false);
		assertNotNull("There should be a patient named 'John'", johnPatients);
		assertFalse("There should be a patient named 'John'", johnPatients.isEmpty());
		
		Patient firstJohnPatient = johnPatients.iterator().next();
		
		// get a list of patients with this identifier, make sure the john patient is actually there
		String identifier = firstJohnPatient.getPatientIdentifier().getIdentifier();
		assertNotNull("Uh oh, the patient doesn't have an identifier", identifier);
		List<Patient> patients = patientService.getPatients(null, identifier, null, false);
		assertTrue("Odd. The firstJohnPatient isn't in the list of patients for this identifier", patients
		        .contains(firstJohnPatient));
		
	}
	
	//	/**
	//	 * This method should be uncommented when you want to examine the actual hibernate
	//	 * sql calls being made.  The calls that should be limiting the number of returned
	//	 * patients should show a "top" or "limit" in the sql -- this proves hibernate's
	//	 * use of a native sql limit as opposed to a java-only limit.  
	//	 * 
	//	 * Note: if enabled, this test will be considerably slower
	//     * 
	//     * @see org.openmrs.test.BaseContextSensitiveTest#getRuntimeProperties()
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
		
		Collection<Patient> patients = patientService.getPatients("John", null, null, false);
		
		assertTrue("The patient list size should be restricted to under the max (1000). its " + patients.size(), patients
		        .size() == 1000);
		
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
		Collection<Patient> pset = patientService.getPatients("Jea", null, null, false);
		boolean claudioFound = false;
		boolean jeanClaudeFound1 = false;
		boolean jeanClaudeFound2 = false;
		boolean jeannetteClaudentFound = false;
		for (Patient patient : pset) {
			if (patient.getFamilyName().equals("Claudio"))
				claudioFound = true;
			if (patient.getGivenName().equals("Jean Claude"))
				jeanClaudeFound1 = true;
			if (patient.getGivenName().equals("Jean"))
				jeanClaudeFound2 = true;
			if (patient.getGivenName().equals("Jeannette"))
				jeannetteClaudentFound = true;
		}
		assertFalse(claudioFound);
		assertTrue(jeanClaudeFound1);
		assertTrue(jeanClaudeFound2);
		assertTrue(jeannetteClaudentFound);
		
		//Test that "Jean Claude" finds given_name="Jean Claude" and given_name="Jean", family_name="Claude"
		//and given_name="Jeannette" family_name="Claudent" but not
		//given_name="John" family_name="Claudio"
		pset = patientService.getPatients("Jean Claude", null, null, false);
		claudioFound = false;
		jeanClaudeFound1 = false;
		jeanClaudeFound2 = false;
		jeannetteClaudentFound = false;
		for (Patient patient : pset) {
			if (patient.getFamilyName().equals("Claudio"))
				claudioFound = true;
			if (patient.getGivenName().equals("Jean Claude"))
				jeanClaudeFound1 = true;
			if (patient.getGivenName().equals("Jean"))
				jeanClaudeFound2 = true;
			if (patient.getGivenName().equals("Jeannette"))
				jeannetteClaudentFound = true;
		}
		assertFalse(claudioFound);
		assertTrue(jeanClaudeFound1);
		assertTrue(jeanClaudeFound2);
		assertTrue(jeannetteClaudentFound);
		
		pset = patientService.getPatients("I am voided", null, null, false);
		assertEquals(pset.size(), 0);
		
	}
	
	/**
	 * Test the PatientService.getPatients(String, String, List) method with both an identifier and
	 * an identifiertype
	 * 
	 * @throws Exception
	 */
	@Test
	@SkipBaseSetup
	public void shouldGetPatientsByIdentifierAndIdentifierType() throws Exception {
		initializeInMemoryDatabase();
		executeDataSet(FIND_PATIENTS_XML);
		authenticate();
		
		List<PatientIdentifierType> types = new Vector<PatientIdentifierType>();
		types.add(new PatientIdentifierType(1));
		
		// make sure we get back only one patient
		List<Patient> patients = patientService.getPatients(null, "1234", types, false);
		assertEquals(1, patients.size());
		
		// make sure we get back only one patient
		patients = patientService.getPatients(null, "1234", null, false);
		assertEquals(1, patients.size());
		
		// make sure we get back only patient #2 and patient #5
		patients = patientService.getPatients(null, null, types, false);
		assertEquals(2, patients.size());
		
		// make sure we can search a padded identifier
		patients = patientService.getPatients(null, "00000001234", null, false);
		assertEquals(1, patients.size());
	}
	
	/**
	 * @see {@link PatientService#purgePatientIdentifierType(PatientIdentifierType)}
	 */
	@Test
	@Verifies(value = "should delete type from database", method = "purgePatientIdentifierType(PatientIdentifierType)")
	public void purgePatientIdentifierType_shouldDeleteTypeFromDatabase() throws Exception {
		PatientIdentifierType type = patientService.getPatientIdentifierType(1);
		
		patientService.purgePatientIdentifierType(type);
		assertNull(patientService.getPatientIdentifierType(1));
	}
	
	/**
	 * @see {@link PatientService#savePatientIdentifierType(PatientIdentifierType)}
	 */
	@Test
	@Verifies(value = "should create new type", method = "savePatientIdentifierType(PatientIdentifierType)")
	public void savePatientIdentifierType_shouldCreateNewType() throws Exception {
		PatientIdentifierType patientIdentifierType = new PatientIdentifierType();
		
		patientIdentifierType.setName("testing");
		patientIdentifierType.setDescription("desc");
		patientIdentifierType.setRequired(false);
		
		patientService.savePatientIdentifierType(patientIdentifierType);
		
		PatientIdentifierType newPatientIdentifierType = patientService.getPatientIdentifierType(patientIdentifierType
		        .getPatientIdentifierTypeId());
		assertNotNull(newPatientIdentifierType);
	}
	
	/**
	 * @see {@link PatientService#savePatientIdentifierType(PatientIdentifierType)}
	 */
	@Test
	@Verifies(value = "should update existing type", method = "savePatientIdentifierType(PatientIdentifierType)")
	public void savePatientIdentifierType_shouldUpdateExistingType() throws Exception {
		
		PatientIdentifierType type = patientService.getPatientIdentifierType(1);
		type.setName("SOME NEW NAME");
		patientService.savePatientIdentifierType(type);
		
		PatientIdentifierType newerPatientIdentifierType = patientService.getPatientIdentifierType(1);
		assertEquals("SOME NEW NAME", newerPatientIdentifierType.getName());
	}
	
	/**
	 * Make sure the api can handle having a User object that is also a patient and was previously
	 * loaded via hibernate
	 * 
	 * @throws Exception
	 */
	// ignoring this test until we refactor person/patient/user
	@Ignore
	@Test
	public void shouldAllowGettingPatientsThatWereCreatedByUsersWhoArePatients() throws Exception {
		executeDataSet(USERS_WHO_ARE_PATIENTS_XML);
		
		// we must fetch this person first, because this person is
		// the creator of the next.  We need to make sure hibernate isn't
		// caching and returning different person objects when it shouldn't be
		Patient patient2 = patientService.getPatient(2);
		assertTrue("When getting a patient, it should be of the class patient, not: " + patient2.getClass(), patient2
		        .getClass().equals(Patient.class));
		
		Patient patient3 = patientService.getPatient(3);
		assertTrue("When getting a patient, it should be of the class patient, not: " + patient3.getClass(), patient3
		        .getClass().equals(Patient.class));
		
		User user2 = Context.getUserService().getUser(2);
		assertTrue("When getting a user, it should be of the class user, not: " + user2.getClass(), User.class
		        .isAssignableFrom(user2.getClass()));
		
	}
	
	/**
	 * @see {@link PatientService#getPatients(String)}
	 */
	@Test
	@Verifies(value = "should force search string to be greater than minsearchcharacters global property", method = "getPatients(String)")
	public void getPatients_shouldForceSearchStringToBeGreaterThanMinsearchcharactersGlobalProperty() throws Exception {
		// make sure we can get patients with the default of 3 
		assertEquals(1, Context.getPatientService().getPatients("Colle").size());
		
		Context.clearSession();
		Context.getAdministrationService().saveGlobalProperty(
		    new GlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_MIN_SEARCH_CHARACTERS, "4"));
		
		assertEquals(0, Context.getPatientService().getPatients("Col").size());
	}
	
	/**
	 * @see {@link PatientService#getPatients(String)}
	 */
	@Test
	@Verifies(value = "should allow search string to be one according to minsearchcharacters global property", method = "getPatients(String)")
	public void getPatients_shouldAllowSearchStringToBeOneAccordingToMinsearchcharactersGlobalProperty() throws Exception {
		// make sure the default of "3" kicks in and blocks any results
		assertEquals(0, Context.getPatientService().getPatients("Co").size());
		
		Context.clearSession();
		Context.getAdministrationService().saveGlobalProperty(
		    new GlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_MIN_SEARCH_CHARACTERS, "1"));
		
		assertEquals(1, Context.getPatientService().getPatients("Co").size());
	}
	
	/**
	 * @see {@link PatientService#getPatient(Integer)}
	 */
	@Test
	@Verifies(value = "should return null object if patient id doesnt exist", method = "getPatient(Integer)")
	public void getPatient_shouldReturnNullObjectIfPatientIdDoesntExist() throws Exception {
		Assert.assertNull(Context.getPatientService().getPatient(1234512093));
	}
	
	/**
	 * @see {@link PatientServiceImpl#mergePatients(Patient,Patient)}
	 */
	@Test(expected = APIException.class)
	@Verifies(value = "should not merge the same patient to itself", method = "mergePatients(Patient,Patient)")
	public void mergePatients_shouldNotMergeTheSamePatientToItself() throws Exception {
		Context.getPatientService().mergePatients(new Patient(2), new Patient(2));
	}
	
	/**
	 * @see {@link PatientService#savePatient(Patient)}
	 */
	@Test
	@Verifies(value = "should create new patient from existing person plus user object", method = "savePatient(Patient)")
	public void savePatient_shouldCreateNewPatientFromExistingPersonPlusUserObject() throws Exception {
		// sanity check, make sure there isn't a 501 patient already
		Assert.assertNull(patientService.getPatient(501));
		
		Person existingPerson = Context.getPersonService().getPerson(501); // fetch Bruno from the database
		Context.clearSession();
		Patient patient = new Patient(existingPerson);
		patient.addIdentifier(new PatientIdentifier("some identifier", new PatientIdentifierType(2), new Location(1)));
		
		patientService.savePatient(patient);
		
		Assert.assertEquals(501, patient.getPatientId().intValue());
		Assert.assertNotNull(patientService.getPatient(501)); // make sure a new row with a patient id WAS created
		Assert.assertNull(patientService.getPatient(503)); // make sure a new row with a new person id WASN'T created
	}
	
	/**
	 * @see {@link PatientService#getPatients(String,String,List<QPatientIdentifierType;>,null)}
	 */
	@Test
	@Verifies(value = "should search familyName2 with name", method = "getPatients(String,String,List<QPatientIdentifierType;>,null)")
	public void getPatients_shouldSearchFamilyName2WithName() throws Exception {
		executeDataSet("org/openmrs/api/include/PersonServiceTest-extranames.xml");
		
		List<Patient> patients = patientService.getPatients("Johnson", null, null, false);
		Assert.assertEquals(3, patients.size());
		Assert.assertTrue(patients.contains(new Patient(2)));
		Assert.assertTrue(patients.contains(new Patient(4)));
		Assert.assertTrue(patients.contains(new Patient(5)));
	}
	
	/**
	 * Regression test for ticket #1375: org.hibernate.NonUniqueObjectException caused by
	 * PatientIdentifierValidator Manually construct a patient with a correctly-matching patientId
	 * and patient identifier with validator. Calling PatientService.savePatient on that patient
	 * leads to a call to PatientIdentifierValidator.validateIdentifier which used to load the
	 * Patient for that identifier into the hibernate session, leading to a NonUniqueObjectException
	 * when the calling saveOrUpdate on the manually constructed Patient.
	 * 
	 * @see {@link PatientService#savePatient(Patient)}
	 */
	@Test
	@Verifies(value = "should not throw a NonUniqueObjectException when called with a hand constructed patient regression 1375", method = "savePatient(Patient)")
	public void savePatient_shouldNotThrowANonUniqueObjectExceptionWhenCalledWithAHandConstructedPatientRegression1375() {
		Patient patient = new Patient();
		patient.setGender("M");
		patient.setPatientId(2);
		patient.addName(new PersonName("This", "Isa", "Test"));
		patient.addIdentifier(new PatientIdentifier("101-6", new PatientIdentifierType(1), new Location(1)));
		patientService.savePatient(patient);
	}
	
	/**
	 * This test verifies that {@link PersonName}s are fetched correctly from the hibernate cache.
	 * (Or really, not fetched from the cache but instead are mapped with lazy=false. For some
	 * reason Hibernate isn't able to find objects in the cache if a parent object was the one that
	 * loaded them)
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldFetchNamesForPersonsThatWereFirstFetchedAsPatients() throws Exception {
		Person person = Context.getPersonService().getPerson(2);
		Patient patient = Context.getPatientService().getPatient(2);
		
		patient.getNames().size();
		person.getNames().size();
	}
	
	/**
	 * This test verifies that {@link PersonAddress}es are fetched correctly from the hibernate
	 * cache. (Or really, not fetched from the cache but instead are mapped with lazy=false. For
	 * some reason Hibernate isn't able to find objects in the cache if a parent object was the one
	 * that loaded them)
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldFetchAddressesForPersonsThatWereFirstFetchedAsPatients() throws Exception {
		Person person = Context.getPersonService().getPerson(2);
		Patient patient = Context.getPatientService().getPatient(2);
		
		patient.getAddresses().size();
		person.getAddresses().size();
	}
	
	/**
	 * This test verifies that {@link PersonAttribute}s are fetched correctly from the hibernate
	 * cache. (Or really, not fetched from the cache but instead are mapped with lazy=false. For
	 * some reason Hibernate isn't able to find objects in the cache if a parent object was the one
	 * that loaded them)
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldFetchPersonAttributesForPersonsThatWereFirstFetchedAsPatients() throws Exception {
		Person person = Context.getPersonService().getPerson(2);
		Patient patient = Context.getPatientService().getPatient(2);
		
		patient.getAttributes().size();
		person.getAttributes().size();
	}
	
	/**
	 * Regression test for http://dev.openmrs.org/ticket/1375
	 * 
	 * @see {@link PatientService#savePatient(Patient)}
	 */
	@Test
	@Verifies(value = "should not throw a NonUniqueObjectException when called with a hand constructed patient", method = "savePatient(Patient)")
	public void savePatient_shouldNotThrowANonUniqueObjectExceptionWhenCalledWithAHandConstructedPatient() throws Exception {
		Patient patient = new Patient();
		patient.setGender("M");
		patient.setPatientId(2);
		//patient.setCreator(new User(1));
		//patient.setDateCreated date_created="2005-09-22 00:00:00.0" changed_by="1" date_changed="2008-08-18 12:29:59.0"
		patient.addName(new PersonName("This", "Isa", "Test"));
		patient.addIdentifier(new PatientIdentifier("101-6", new PatientIdentifierType(1), new Location(1)));
		Context.getPatientService().savePatient(patient);
	}
	
	/**
	 * @see {@link PatientService#isIdentifierInUseByAnotherPatient(PatientIdentifier)}
	 */
	@Test
	@Verifies(value = "should ignore voided patientIdentifiers", method = "isIdentifierInUseByAnotherPatient(PatientIdentifier)")
	public void isIdentifierInUseByAnotherPatient_shouldIgnoreVoidedPatientIdentifiers() throws Exception {
		PatientIdentifierType pit = patientService.getPatientIdentifierType(2);
		PatientIdentifier patientIdentifier = new PatientIdentifier("ABC123", pit, null);
		Assert.assertFalse(patientService.isIdentifierInUseByAnotherPatient(patientIdentifier));
	}
	
	/**
	 * Regression test for http://dev.openmrs.org/ticket/790
	 * 
	 * @see {@link PatientService#isIdentifierInUseByAnotherPatient(PatientIdentifier)}
	 */
	@Test
	@Verifies(value = "should ignore voided patients", method = "isIdentifierInUseByAnotherPatient(PatientIdentifier)")
	public void isIdentifierInUseByAnotherPatient_shouldIgnoreVoidedPatients() throws Exception {
		{ // patient 999 should be voided and have a non-voided identifier of XYZ
			Patient p = patientService.getPatient(999);
			Assert.assertNotNull(p);
			Assert.assertTrue(p.isVoided());
			System.out.println(p.getVoidReason());
			boolean found = false;
			for (PatientIdentifier id : p.getIdentifiers()) {
				if (id.getIdentifier().equals("XYZ") && id.getIdentifierType().getId() == 2) {
					found = true;
					break;
				}
			}
			Assert.assertTrue(found);
		}
		PatientIdentifierType pit = patientService.getPatientIdentifierType(2);
		PatientIdentifier patientIdentifier = new PatientIdentifier("XYZ", pit, null);
		Assert.assertFalse(patientService.isIdentifierInUseByAnotherPatient(patientIdentifier));
	}
	
	/**
	 * @see {@link PatientService#isIdentifierInUseByAnotherPatient(PatientIdentifier)}
	 */
	@Test
	@Verifies(value = "should return false when patientIdentifier contains a patient and no other patient has this id", method = "isIdentifierInUseByAnotherPatient(PatientIdentifier)")
	public void isIdentifierInUseByAnotherPatient_shouldReturnFalseWhenPatientIdentifierContainsAPatientAndNoOtherPatientHasThisId()
	                                                                                                                                throws Exception {
		PatientIdentifierType pit = patientService.getPatientIdentifierType(1);
		PatientIdentifier patientIdentifier = new PatientIdentifier("Nobody could possibly have this identifier", pit, null);
		patientIdentifier.setPatient(patientService.getPatient(2));
		Assert.assertFalse(patientService.isIdentifierInUseByAnotherPatient(patientIdentifier));
	}
	
	/**
	 * @see {@link PatientService#isIdentifierInUseByAnotherPatient(PatientIdentifier)}
	 */
	@Test
	@Verifies(value = "should return false when patientIdentifier does not contain a patient and no patient has this id", method = "isIdentifierInUseByAnotherPatient(PatientIdentifier)")
	public void isIdentifierInUseByAnotherPatient_shouldReturnFalseWhenPatientIdentifierDoesNotContainAPatientAndNoPatientHasThisId()
	                                                                                                                                 throws Exception {
		PatientIdentifierType pit = patientService.getPatientIdentifierType(1);
		PatientIdentifier patientIdentifier = new PatientIdentifier("Nobody could possibly have this identifier", pit, null);
		Assert.assertFalse(patientService.isIdentifierInUseByAnotherPatient(patientIdentifier));
	}
	
	/**
	 * @see {@link PatientService#isIdentifierInUseByAnotherPatient(PatientIdentifier)}
	 */
	@Test
	@Verifies(value = "should return true when patientIdentifier contains a patient and another patient has this id", method = "isIdentifierInUseByAnotherPatient(PatientIdentifier)")
	public void isIdentifierInUseByAnotherPatient_shouldReturnTrueWhenPatientIdentifierContainsAPatientAndAnotherPatientHasThisId()
	                                                                                                                               throws Exception {
		PatientIdentifierType pit = patientService.getPatientIdentifierType(1);
		PatientIdentifier patientIdentifier = new PatientIdentifier("7TU-8", pit, null);
		patientIdentifier.setPatient(patientService.getPatient(2));
		Assert.assertTrue(patientService.isIdentifierInUseByAnotherPatient(patientIdentifier));
	}
	
	/**
	 * @see {@link PatientService#isIdentifierInUseByAnotherPatient(PatientIdentifier)}
	 */
	@Test
	@Verifies(value = "should return true when patientIdentifier does not contain a patient and a patient has this id", method = "isIdentifierInUseByAnotherPatient(PatientIdentifier)")
	public void isIdentifierInUseByAnotherPatient_shouldReturnTrueWhenPatientIdentifierDoesNotContainAPatientAndAPatientHasThisId()
	                                                                                                                               throws Exception {
		PatientIdentifierType pit = patientService.getPatientIdentifierType(1);
		PatientIdentifier patientIdentifier = new PatientIdentifier("7TU-8", pit, null);
		Assert.assertTrue(patientService.isIdentifierInUseByAnotherPatient(patientIdentifier));
	}
	
	/**
	 * @see PatientService#mergePatients(Patient,Patient)
	 */
	@Test
	@Verifies(value = "should copy nonvoided addresses to preferred patient", method = "mergePatients(Patient,Patient)")
	public void mergePatients_shouldCopyNonvoidedAddressesToPreferredPatient() throws Exception {
		Patient preferred = patientService.getPatient(7);
		Patient notPreferred = patientService.getPatient(8);
		
		patientService.mergePatients(preferred, notPreferred);
		
		// make sure one of their addresses has the city of "Jabali"
		boolean found = false;
		for (PersonAddress pa : preferred.getAddresses()) {
			if (pa.getCityVillage().equals("Jabali"))
				found = true;
		}
		
		Assert.assertTrue("odd, user 7 didn't get user 8's address", found);
	}
	
	/**
	 * @see PatientService#mergePatients(Patient,Patient)
	 */
	@Test
	@Verifies(value = "should copy nonvoided identifiers to preferred patient", method = "mergePatients(Patient,Patient)")
	public void mergePatients_shouldCopyNonvoidedIdentifiersToPreferredPatient() throws Exception {
		Patient preferred = patientService.getPatient(7);
		Patient notPreferred = patientService.getPatient(8);
		
		patientService.mergePatients(preferred, notPreferred);
		
		PatientIdentifier nonvoidedPI = new PatientIdentifier("7TU-8", new PatientIdentifierType(1), new Location(1));
		nonvoidedPI.setPatient(preferred);
		PatientIdentifier voidedPI = new PatientIdentifier("ABC123", new PatientIdentifierType(2), new Location(1));
		voidedPI.setPatient(preferred);
		
		Assert.assertTrue(OpenmrsUtil.collectionContains(preferred.getIdentifiers(), nonvoidedPI));
		Assert.assertFalse("The voided identifier: " + voidedPI + " should not have been moved over because it was voided",
		    OpenmrsUtil.collectionContains(preferred.getIdentifiers(), voidedPI));
	}
	
	/**
	 * @see PatientService#mergePatients(Patient,Patient)
	 */
	@Test
	@Verifies(value = "should copy nonvoided names to preferred patient", method = "mergePatients(Patient,Patient)")
	public void mergePatients_shouldCopyNonvoidedNamesToPreferredPatient() throws Exception {
		Patient preferred = patientService.getPatient(7);
		Patient notPreferred = patientService.getPatient(8);
		
		patientService.mergePatients(preferred, notPreferred);
		
		// make sure one of their addresses has the first name of "Anet"
		boolean found = false;
		for (PersonName pn : preferred.getNames()) {
			if (pn.getGivenName().equals("Anet"))
				found = true;
		}
		
		Assert.assertTrue("odd, user 7 didn't get user 8's names", found);
	}
	
	/**
	 * @see {@link PatientService#getPatientByUuid(String)}
	 */
	@Test
	@Verifies(value = "should find object given valid uuid", method = "getPatientByUuid(String)")
	public void getPatientByUuid_shouldFindObjectGivenValidUuid() throws Exception {
		String uuid = "da7f524f-27ce-4bb2-86d6-6d1d05312bd5";
		Patient patient = Context.getPatientService().getPatientByUuid(uuid);
		Assert.assertEquals(2, (int) patient.getPatientId());
	}
	
	/**
	 * @see {@link PatientService#getPatientByUuid(String)}
	 */
	@Test
	@Verifies(value = "should return null if no object found with given uuid", method = "getPatientByUuid(String)")
	public void getPatientByUuid_shouldReturnNullIfNoObjectFoundWithGivenUuid() throws Exception {
		Assert.assertNull(Context.getPatientService().getPatientByUuid("some invalid uuid"));
	}
	
	/**
	 * @see {@link PatientService#getPatientIdentifierByUuid(String)}
	 */
	@Test
	@Verifies(value = "should find object given valid uuid", method = "getPatientIdentifierByUuid(String)")
	public void getPatientIdentifierByUuid_shouldFindObjectGivenValidUuid() throws Exception {
		String uuid = "ff41928c-3bca-48d9-a4dc-9198f6b2873b";
		PatientIdentifier patientIdentifier = Context.getPatientService().getPatientIdentifierByUuid(uuid);
		Assert.assertEquals(1, (int) patientIdentifier.getPatientIdentifierId());
	}
	
	/**
	 * @see {@link PatientService#getPatientIdentifierByUuid(String)}
	 */
	@Test
	@Verifies(value = "should return null if no object found with given uuid", method = "getPatientIdentifierByUuid(String)")
	public void getPatientIdentifierByUuid_shouldReturnNullIfNoObjectFoundWithGivenUuid() throws Exception {
		Assert.assertNull(Context.getPatientService().getPatientIdentifierByUuid("some invalid uuid"));
	}
	
	/**
	 * @see {@link PatientService#getPatientIdentifierTypeByUuid(String)}
	 */
	@Test
	@Verifies(value = "should find object given valid uuid", method = "getPatientIdentifierTypeByUuid(String)")
	public void getPatientIdentifierTypeByUuid_shouldFindObjectGivenValidUuid() throws Exception {
		String uuid = "1a339fe9-38bc-4ab3-b180-320988c0b968";
		PatientIdentifierType patientIdentifierType = Context.getPatientService().getPatientIdentifierTypeByUuid(uuid);
		Assert.assertEquals(1, (int) patientIdentifierType.getPatientIdentifierTypeId());
	}
	
	/**
	 * @see {@link PatientService#getPatientIdentifierTypeByUuid(String)}
	 */
	@Test
	@Verifies(value = "should return null if no object found with given uuid", method = "getPatientIdentifierTypeByUuid(String)")
	public void getPatientIdentifierTypeByUuid_shouldReturnNullIfNoObjectFoundWithGivenUuid() throws Exception {
		Assert.assertNull(Context.getPatientService().getPatientIdentifierTypeByUuid("some invalid uuid"));
	}
	
	/**
	 * @see {@link PatientService#mergePatients(Patient,Patient)}
	 */
	@Test
	@Verifies(value = "should not copy over relationships that are only between the preferred and notpreferred patient", method = "mergePatients(Patient,Patient)")
	public void mergePatients_shouldNotCopyOverRelationshipsThatAreOnlyBetweenThePreferredAndNotpreferredPatient()
	                                                                                                              throws Exception {
		executeDataSet("org/openmrs/api/include/PersonServiceTest-createRelationship.xml");
		
		Patient preferred = patientService.getPatient(999);
		Patient notPreferred = patientService.getPatient(2);
		
		patientService.mergePatients(preferred, notPreferred);
	}
	
}
