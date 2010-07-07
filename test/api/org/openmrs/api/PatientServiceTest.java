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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
import org.openmrs.Relationship;
import org.openmrs.RelationshipType;
import org.openmrs.User;
import org.openmrs.activelist.Allergy;
import org.openmrs.activelist.Problem;
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
	
	// Logger
	protected final Log log = LogFactory.getLog(getClass());
	
	// Datasets
	protected static final String CREATE_PATIENT_XML = "org/openmrs/api/include/PatientServiceTest-createPatient.xml";
	
	protected static final String CREATE_PATIENT_VALID_IDENT_XML = "org/openmrs/api/include/PatientServiceTest-createPatientValidIdent.xml";
	
	protected static final String JOHN_PATIENTS_XML = "org/openmrs/api/include/PatientServiceTest-lotsOfJohns.xml";
	
	protected static final String USERS_WHO_ARE_PATIENTS_XML = "org/openmrs/api/include/PatientServiceTest-usersWhoArePatients.xml";
	
	protected static final String FIND_PATIENTS_XML = "org/openmrs/api/include/PatientServiceTest-findPatients.xml";
	
	private static final String ACTIVE_LIST_INITIAL_XML = "org/openmrs/api/include/ActiveListTest.xml";

	private static final String PATIENT_RELATIONSHIPS_XML = "org/openmrs/api/include/PersonServiceTest-createRelationship.xml";

	// Services
	protected static PatientService patientService = null;
	
	protected static PersonService personService = null;

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
			personService = Context.getPersonService();
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
	 * @see {@link PatientService#getIdentifierValidator(String)}
	 */
	@Test
	@Verifies(value = "should treat empty strings like a null entry", method = "getIdentifierValidator()")
	public void getAllIdentifierValidators_shouldTreatEmptyStringsLikeANullEntry() throws Exception {
		Assert.assertEquals(null, patientService.getIdentifierValidator(""));
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
	@Verifies(value = "should not merge patient with itself", method = "mergePatients(Patient,Patient)")
	public void mergePatients_shouldNotMergePatientWithItself() {
		Context.getPatientService().mergePatients(new Patient(2), new Patient(2));
	}
	
	/**
	 * @see {@link PatientService#mergePatients(Patient,Patient)}
	 */
	@Test
	@Verifies(value = "should change user records of non preferred person to preferred person", method = "mergePatients(Patient,Patient)")
	public void mergePatients_shouldChangeUserRecordsOfNonPreferredPersonToPreferredPerson() throws Exception {
		executeDataSet(USERS_WHO_ARE_PATIENTS_XML);
		//TestUtil.printOutTableContents(getConnection(), "users", "person", "patient");
		Context.getPatientService().mergePatients(patientService.getPatient(6), patientService.getPatient(2));
		User user = Context.getUserService().getUser(2);
		Assert.assertEquals(new Person(6), user.getPerson());
	}
	
	/**
	 * @see {@link PatientService#mergePatients(Patient,Patient)}
	 */
	@Test
	@Verifies(value = "should void non preferred person object", method = "mergePatients(Patient,Patient)")
	public void mergePatients_shouldVoidNonPreferredPersonObject() throws Exception {
		Context.getPatientService().mergePatients(patientService.getPatient(6), patientService.getPatient(2));
		Assert.assertTrue(Context.getPersonService().getPerson(2).isVoided());
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
		{
			// patient 999 should be voided and have a non-voided identifier of XYZ
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
	 * @see {@link PatientService#checkPatientIdentifiers(Patient)}
	 */
	@Test
	@Verifies(value = "should ignore voided patient identifier", method = "checkPatientIdentifiers(Patient)")
	public void checkPatientIdentifiers_shouldIgnoreVoidedPatientIdentifier() throws Exception {
		
		Patient patient = new Patient();
		PatientIdentifier patientIdentifier = new PatientIdentifier();
		patientIdentifier.setIdentifierType(Context.getPatientService().getAllPatientIdentifierTypes(false).get(0));
		patientIdentifier.setVoided(true);
		patientIdentifier.setVoidedBy(Context.getAuthenticatedUser());
		patientIdentifier.setVoidReason("Testing whether voided identifiers are ignored");
		patient.addIdentifier(patientIdentifier);
		
		// add a non-voided identifier so that the initial "at least one nonvoided identifier" check passes
		patientIdentifier = new PatientIdentifier();
		patientIdentifier.setIdentifier("a non empty string");
		patientIdentifier.setIdentifierType(Context.getPatientService().getAllPatientIdentifierTypes(false).get(0));
		patientIdentifier.setVoided(false);
		patientIdentifier.setVoidedBy(Context.getAuthenticatedUser());
		patientIdentifier.setVoidReason("Testing whether voided identifiers are ignored");
		patient.addIdentifier(patientIdentifier);
		
		// If the identifier is ignored, it won't throw a BlankIdentifierException as it should
		Context.getPatientService().checkPatientIdentifiers(patient);
		
	}
	
	/**
	 * @see {@link PatientService#checkPatientIdentifiers(Patient)}
	 */
	@Test(expected = InsufficientIdentifiersException.class)
	@Verifies(value = "should require one non voided patient identifier", method = "checkPatientIdentifiers(Patient)")
	public void checkPatientIdentifiers_shouldRequireOneNonVoidedPatientIdentifier() throws Exception {
		
		Patient patient = new Patient();
		PatientIdentifier patientIdentifier = new PatientIdentifier();
		patientIdentifier.setIdentifierType(Context.getPatientService().getAllPatientIdentifierTypes(false).get(0));
		patientIdentifier.setVoided(true);
		patientIdentifier.setVoidedBy(Context.getAuthenticatedUser());
		patientIdentifier.setVoidReason("Testing whether voided identifiers are ignored");
		patient.addIdentifier(patientIdentifier);
		
		// this patient only has a voided identifier, so saving is not allowed
		Context.getPatientService().checkPatientIdentifiers(patient);
		
	}
	
	/**
	 * @see {@link PatientService#checkPatientIdentifiers(Patient)}
	 */
	@Test(expected = BlankIdentifierException.class)
	@Verifies(value = "should remove identifier and throw error when patient has blank patient identifier", method = "checkPatientIdentifiers(Patient)")
	public void checkPatientIdentifiers_shouldRemoveIdentifierAndThrowErrorWhenPatientHasBlankPatientIdentifier()
	                                                                                                             throws Exception {
		
		Patient patient = new Patient();
		PatientIdentifier patientIdentifier = new PatientIdentifier();
		patientIdentifier.setIdentifierType(Context.getPatientService().getAllPatientIdentifierTypes(false).get(0));
		patient.addIdentifier(patientIdentifier);
		
		// Should throw blank identifier exception
		Context.getPatientService().checkPatientIdentifiers(patient);
		
	}
	
	/**
	 * @see {@link PatientService#checkPatientIdentifiers(Patient)}
	 */
	@Test(expected = InsufficientIdentifiersException.class)
	@Verifies(value = "should throw error when patient has null patient identifiers", method = "checkPatientIdentifiers(Patient)")
	public void checkPatientIdentifiers_shouldThrowErrorWhenPatientHasNullPatientIdentifiers() throws Exception {
		Patient patient = new Patient();
		patient.setIdentifiers(null);
		Context.getPatientService().checkPatientIdentifiers(patient);
	}
	
	/**
	 * Cannot distinguish between null and empty patient identifiers because you cannot set the
	 * patient identifiers directly. There's only a method to add and remove patient identifiers.
	 * 
	 * @see {@link PatientService#checkPatientIdentifiers(Patient)}
	 */
	@Test(expected = InsufficientIdentifiersException.class)
	@Verifies(value = "should throw error when patient has empty patient identifiers", method = "checkPatientIdentifiers(Patient)")
	public void checkPatientIdentifiers_shouldThrowErrorWhenPatientHasEmptyPatientIdentifiers() throws Exception {
		Patient patient = new Patient();
		patient.setIdentifiers(new HashSet<PatientIdentifier>());
		Context.getPatientService().checkPatientIdentifiers(patient);
	}
	
	/**
	 * @see {@link PatientService#checkPatientIdentifiers(Patient)}
	 */
	@Test(expected = DuplicateIdentifierException.class)
	@Ignore
	// TODO fix: DuplicateIdentifierException not being thrown
	@Verifies(value = "should throw error when patient has identical identifiers", method = "checkPatientIdentifiers(Patient)")
	public void checkPatientIdentifiers_shouldThrowErrorWhenPatientHasIdenticalIdentifiers() throws Exception {
		
		PatientIdentifierType patientIdentifierType = Context.getPatientService().getAllPatientIdentifierTypes(false).get(0);
		
		Patient patient = new Patient();
		
		// Identifier #1
		PatientIdentifier patientIdentifier1 = new PatientIdentifier();
		patientIdentifier1.setIdentifier("123456789");
		patientIdentifier1.setDateCreated(new Date());
		patientIdentifier1.setIdentifierType(patientIdentifierType);
		patient.addIdentifier(patientIdentifier1);
		
		// Identifier #2
		PatientIdentifier patientIdentifier2 = new PatientIdentifier();
		patientIdentifier2.setIdentifier("123456789");
		patientIdentifier2.setIdentifierType(patientIdentifierType);
		patientIdentifier2.setDateCreated(new Date());
		patient.addIdentifier(patientIdentifier2);
		
		// Should throw blank identifier exception
		Context.getPatientService().checkPatientIdentifiers(patient);
		
	}
	
	/**
	 * @see {@link PatientService#checkPatientIdentifiers(Patient)}
	 */
	@Test
	@Verifies(value = "should throw error when patient does not have one or more required identifiers", method = "checkPatientIdentifiers(Patient)")
	public void checkPatientIdentifiers_shouldThrowErrorWhenPatientDoesNotHaveOneOrMoreRequiredIdentifiers()
	                                                                                                        throws Exception {
		
		PatientIdentifierType patientIdentifierType = Context.getPatientService().getAllPatientIdentifierTypes(false).get(0);
		
		log.info(patientIdentifierType.getRequired());
		
		// TODO Finish
		
	}
	
	/**
	 * @see {@link PatientService#getAllIdentifierValidators()}
	 */
	@Test
	@Verifies(value = "should return all registered patient identifier validators", method = "getAllIdentifierValidators()")
	public void getAllIdentifierValidators_shouldReturnAllRegisteredPatientIdentifierValidators() throws Exception {
		
		Collection<IdentifierValidator> expectedValidators = new HashSet<IdentifierValidator>();
		expectedValidators.add(patientService.getIdentifierValidator("org.openmrs.patient.impl.LuhnIdentifierValidator"));
		expectedValidators
		        .add(patientService.getIdentifierValidator("org.openmrs.patient.impl.VerhoeffIdentifierValidator"));
		
		Collection<IdentifierValidator> actualValidators = patientService.getAllIdentifierValidators();
		Assert.assertNotNull(actualValidators);
		Assert.assertEquals(2, actualValidators.size());
		TestUtil.assertCollectionContentsEquals(expectedValidators, actualValidators);
		
	}
	
	/**
	 * @see {@link PatientService#getAllPatientIdentifierTypes()}
	 */
	@Test
	@Verifies(value = "should fetch all non retired patient identifier types", method = "getAllPatientIdentifierTypes()")
	public void getAllPatientIdentifierTypes_shouldFetchAllNonRetiredPatientIdentifierTypes() throws Exception {
		Collection<PatientIdentifierType> types = Context.getPatientService().getAllPatientIdentifierTypes();
		Assert.assertNotNull("Should not return null", types);
		
		for (PatientIdentifierType type : types) {
			if (type.getRetired())
				Assert.fail("Should not return retired patient identifier types");
		}
		Assert.assertEquals("Should be exactly two patient identifier types in the dataset", 2, types.size());
		
	}
	
	/**
	 * @see {@link PatientService#getAllPatientIdentifierTypes(null)}
	 */
	@Test
	@Verifies(value = "should fetch patient identifier types including retired when include retired is true", method = "getAllPatientIdentifierTypes(null)")
	public void getAllPatientIdentifierTypes_shouldFetchPatientIdentifierTypesIncludingRetiredWhenIncludeRetiredIsTrue()
	                                                                                                                    throws Exception {
		
		Collection<PatientIdentifierType> types = Context.getPatientService().getAllPatientIdentifierTypes(true);
		
		boolean atLeastOneRetired = false;
		for (PatientIdentifierType type : types) {
			if (type.getRetired()) {
				atLeastOneRetired = true;
				break;
			}
		}
		Assert.assertTrue("There should be at least one retired patient identifier type", atLeastOneRetired);
		Assert.assertEquals("Should be exactly three patient identifier types", 3, types.size());
	}
	
	/**
	 * @see {@link PatientService#getAllPatientIdentifierTypes(null)}
	 */
	@Test
	@Verifies(value = "should fetch patient identifier types excluding retired when include retired is false", method = "getAllPatientIdentifierTypes(null)")
	public void getAllPatientIdentifierTypes_shouldFetchPatientIdentifierTypesExcludingRetiredWhenIncludeRetiredIsFalse()
	                                                                                                                     throws Exception {
		
		Collection<PatientIdentifierType> types = Context.getPatientService().getAllPatientIdentifierTypes(false);
		
		for (PatientIdentifierType type : types) {
			if (type.getRetired())
				Assert.fail("Should not return retired patient identifier types");
		}
		Assert.assertEquals("Should be exactly two patient identifier types in the dataset", 2, types.size());
		
	}
	
	/**
	 * @see {@link PatientService#getIdentifierValidator(String)}
	 */
	@Test
	@Verifies(value = "should return patient identifier validator given class name", method = "getIdentifierValidator(String)")
	public void getIdentifierValidator_shouldReturnPatientIdentifierValidatorGivenClassName() throws Exception {
		IdentifierValidator identifierValidator = Context.getPatientService().getIdentifierValidator(
		    "org.openmrs.patient.impl.LuhnIdentifierValidator");
		Assert.assertNotNull(identifierValidator);
		Assert.assertEquals("Luhn CheckDigit Validator", identifierValidator.getName());
		
		identifierValidator = Context.getPatientService().getIdentifierValidator(
		    "org.openmrs.patient.impl.VerhoeffIdentifierValidator");
		Assert.assertNotNull(identifierValidator);
		Assert.assertEquals("Verhoeff Check Digit Validator.", identifierValidator.getName());
	}
	
	/**
	 * @see {@link PatientService#getPatient(Integer)}
	 */
	@Test
	@Verifies(value = "should fetch patient with given patient id", method = "getPatient(Integer)")
	public void getPatient_shouldFetchPatientWithGivenPatientId() throws Exception {
		Patient patient = Context.getPatientService().getPatient(2);
		Assert.assertNotNull(patient);
		Assert.assertTrue(patient.getClass().isAssignableFrom(Patient.class));
	}
	
	/**
	 * @see {@link PatientService#getPatient(Integer)}
	 */
	@Test
	@Verifies(value = "should return null when patient with given patient id does not exist", method = "getPatient(Integer)")
	public void getPatient_shouldReturnNullWhenPatientWithGivenPatientIdDoesNotExist() throws Exception {
		Patient patient = Context.getPatientService().getPatient(10000);
		Assert.assertNull(patient);
	}
	
	/**
	 * @see {@link PatientService#getPatientByExample(Patient)}
	 */
	@Test
	@Verifies(value = "should fetch patient matching patient id of given patient", method = "getPatientByExample(Patient)")
	public void getPatientByExample_shouldFetchPatientMatchingPatientIdOfGivenPatient() throws Exception {
		Patient examplePatient = Context.getPatientService().getPatient(6);
		examplePatient.setId(2);
		
		Patient patient = Context.getPatientService().getPatientByExample(examplePatient);
		Assert.assertNotNull(patient);
		Assert.assertTrue(patient.getClass().isAssignableFrom(Patient.class));
		Assert.assertEquals(new Integer(2), patient.getPatientId());
	}
	
	/**
	 * @see {@link PatientService#getPatientByExample(Patient)}
	 */
	@Test
	@Verifies(value = "should not fetch patient matching any other patient information", method = "getPatientByExample(Patient)")
	public void getPatientByExample_shouldNotFetchPatientMatchingAnyOtherPatientInformation() throws Exception {
		Patient examplePatient = Context.getPatientService().getPatient(6);
		// TODO Test this - it shouldn't matter what the identifier is
		examplePatient.setId(null);
		
		Patient patient = Context.getPatientService().getPatientByExample(examplePatient);
		Assert.assertNull(patient);
	}
	
	/**
	 * @see {@link PatientService#getPatientByExample(Patient)}
	 */
	@Test
	@Verifies(value = "should return null when no patient matches given patient to match", method = "getPatientByExample(Patient)")
	public void getPatientByExample_shouldReturnNullWhenNoPatientMatchesGivenPatientToMatch() throws Exception {
		Patient examplePatient = Context.getPatientService().getPatient(6);
		examplePatient.setId(3);
		
		Patient patient = Context.getPatientService().getPatientByExample(examplePatient);
		Assert.assertNull(patient);
	}
	
	/**
	 * @see {@link PatientService#getPatientIdentifierType(Integer)}
	 */
	@Test
	@Verifies(value = "should fetch patient identifier with given patient identifier type id", method = "getPatientIdentifierType(Integer)")
	public void getPatientIdentifierType_shouldFetchPatientIdentifierWithGivenPatientIdentifierTypeId() throws Exception {
		PatientIdentifierType identifierType = Context.getPatientService().getPatientIdentifierType(1);
		Assert.assertNotNull(identifierType);
		Assert.assertTrue(identifierType.getClass().isAssignableFrom(PatientIdentifierType.class));
	}
	
	/**
	 * @see {@link PatientService#getPatientIdentifierType(Integer)}
	 */
	@Test
	@Verifies(value = "should return null when patient identifier identifier does not exist", method = "getPatientIdentifierType(Integer)")
	public void getPatientIdentifierType_shouldReturnNullWhenPatientIdentifierIdentifierDoesNotExist() throws Exception {
		PatientIdentifierType identifierType = Context.getPatientService().getPatientIdentifierType(10000);
		Assert.assertNull(identifierType);
	}
	
	/**
	 * @see {@link PatientService#getPatientIdentifierTypeByName(String)}
	 */
	@Test
	@Verifies(value = "should fetch patient identifier type that exactly matches given name", method = "getPatientIdentifierTypeByName(String)")
	public void getPatientIdentifierTypeByName_shouldFetchPatientIdentifierTypeThatExactlyMatchesGivenName()
	                                                                                                        throws Exception {
		
		String identifierTypeName = "OpenMRS Identification Number";
		PatientIdentifierType identifierType = Context.getPatientService()
		        .getPatientIdentifierTypeByName(identifierTypeName);
		Assert.assertNotNull(identifierType);
		Assert.assertEquals(identifierType.getName(), identifierTypeName);
		Assert.assertTrue(identifierType.getClass().isAssignableFrom(PatientIdentifierType.class));
	}
	
	/**
	 * @see {@link PatientService#getPatientIdentifierTypeByName(String)}
	 */
	@Test
	@Verifies(value = "should not return patient identifier type that partially matches given name", method = "getPatientIdentifierTypeByName(String)")
	public void getPatientIdentifierTypeByName_shouldNotReturnPatientIdentifierTypeThatPartiallyMatchesGivenName()
	                                                                                                              throws Exception {
		PatientIdentifierType identifierType = Context.getPatientService().getPatientIdentifierTypeByName("OpenMRS");
		Assert.assertNull(identifierType);
	}
	
	/**
	 * @see {@link PatientService#getPatientIdentifierTypeByName(String)}
	 */
	@Test
	@Verifies(value = "should return null when patient identifier type with given name does not exist", method = "getPatientIdentifierTypeByName(String)")
	public void getPatientIdentifierTypeByName_shouldReturnNullWhenPatientIdentifierTypeWithGivenNameDoesNotExist()
	                                                                                                               throws Exception {
		PatientIdentifierType identifierType = Context.getPatientService().getPatientIdentifierTypeByName(
		    "Invalid Identifier Example");
		Assert.assertNull(identifierType);
	}
	
	/**
	 * @see {@link PatientService#getPatientIdentifierTypeByUuid(String)}
	 */
	@Test
	@Verifies(value = "should fetch patient identifier type with given uuid", method = "getPatientIdentifierTypeByUuid(String)")
	public void getPatientIdentifierTypeByUuid_shouldFetchPatientIdentifierTypeWithGivenUuid() throws Exception {
		PatientIdentifierType identifierType = Context.getPatientService().getPatientIdentifierTypeByUuid(
		    "1a339fe9-38bc-4ab3-b180-320988c0b968");
		Assert.assertNotNull(identifierType);
		Assert.assertTrue(identifierType.getClass().isAssignableFrom(PatientIdentifierType.class));
	}
	
	/**
	 * @see {@link PatientService#getPatientIdentifierTypeByUuid(String)}
	 */
	@Test
	@Verifies(value = "should return null when patient identifier type with given uuid does not exist", method = "getPatientIdentifierTypeByUuid(String)")
	public void getPatientIdentifierTypeByUuid_shouldReturnNullWhenPatientIdentifierTypeWithGivenUuidDoesNotExist()
	                                                                                                               throws Exception {
		PatientIdentifierType identifierType = Context.getPatientService().getPatientIdentifierTypeByUuid(
		    "thisuuiddoesnotexist");
		Assert.assertNull(identifierType);
	}
	
	/**
	 * @see {@link PatientService#getPatientIdentifierTypes(String,String,Boolean,Boolean)}
	 */
	@Test
	@Verifies(value = "should fetch patient identifier types that match given name with given format", method = "getPatientIdentifierTypes(String,String,Boolean,Boolean)")
	public void getPatientIdentifierTypes_shouldFetchPatientIdentifierTypesThatMatchGivenNameWithGivenFormat()
	                                                                                                          throws Exception {
		executeDataSet("org/openmrs/api/include/PatientServiceTest-createPatientIdentifierType.xml");
		List<PatientIdentifierType> patientIdentifierTypes = Context.getPatientService().getPatientIdentifierTypes(
		    "Test OpenMRS Identification Number", "java.lang.Integer", null, null);
		
		Assert.assertEquals(false, patientIdentifierTypes.isEmpty());
		
		for (PatientIdentifierType patientIdentifierType : patientIdentifierTypes) {
			Assert.assertEquals("Test OpenMRS Identification Number", patientIdentifierType.getName());
			Assert.assertEquals("java.lang.Integer", patientIdentifierType.getFormat());
		}
	}
	
	/**
	 * @see {@link PatientService#getPatientIdentifierTypes(String,String,Boolean,Boolean)}
	 */
	@Test
	@Verifies(value = "should fetch required patient identifier types when given required is true", method = "getPatientIdentifierTypes(String,String,Boolean,Boolean)")
	public void getPatientIdentifierTypes_shouldFetchRequiredPatientIdentifierTypesWhenGivenRequiredIsTrue()
	                                                                                                        throws Exception {
		executeDataSet("org/openmrs/api/include/PatientServiceTest-createPatientIdentifierType.xml");
		List<PatientIdentifierType> patientIdentifierTypes = Context.getPatientService().getPatientIdentifierTypes(null,
		    null, true, null);
		
		Assert.assertTrue(!patientIdentifierTypes.isEmpty());
		Assert.assertEquals(1, patientIdentifierTypes.size());
		for (PatientIdentifierType patientIdentifierType : patientIdentifierTypes) {
			Assert.assertTrue(patientIdentifierType.getRequired());
		}
	}
	
	/**
	 * @see {@link PatientService#getPatientIdentifierTypes(String,String,Boolean,Boolean)}
	 */
	@Test
	@Verifies(value = "should fetch non required patient identifier types when given required is false", method = "getPatientIdentifierTypes(String,String,Boolean,Boolean)")
	public void getPatientIdentifierTypes_shouldFetchNonRequiredPatientIdentifierTypesWhenGivenRequiredIsFalse()
	                                                                                                            throws Exception {
		executeDataSet("org/openmrs/api/include/PatientServiceTest-createPatientIdentifierType.xml");
		List<PatientIdentifierType> patientIdentifierTypes = Context.getPatientService().getPatientIdentifierTypes(null,
		    null, false, null);
		
		Assert.assertTrue(!patientIdentifierTypes.isEmpty());
		
		for (PatientIdentifierType patientIdentifierType : patientIdentifierTypes) {
			Assert.assertFalse(patientIdentifierType.getRequired());
		}
	}
	
	/**
	 * @see {@link PatientService#getPatientIdentifierTypes(String,String,Boolean,Boolean)}
	 */
	@Test
	@Verifies(value = "should fetch any patient identifier types when given required is null", method = "getPatientIdentifierTypes(String,String,Boolean,Boolean)")
	public void getPatientIdentifierTypes_shouldFetchAnyPatientIdentifierTypesWhenGivenRequiredIsNull() throws Exception {
		executeDataSet("org/openmrs/api/include/PatientServiceTest-createPatientIdentifierType.xml");
		List<PatientIdentifierType> patientIdentifierTypes = Context.getPatientService().getPatientIdentifierTypes(null,
		    null, null, null);
		
		Assert.assertTrue(!patientIdentifierTypes.isEmpty());
		
		Assert.assertEquals(4, patientIdentifierTypes.size());
	}
	
	/**
	 * @see {@link PatientService#getPatientIdentifierTypes(String,String,Boolean,Boolean)}
	 */
	@Test
	@Verifies(value = "should fetch patient identifier types with check digit when given has check digit is true", method = "getPatientIdentifierTypes(String,String,Boolean,Boolean)")
	public void getPatientIdentifierTypes_shouldFetchPatientIdentifierTypesWithCheckDigitWhenGivenHasCheckDigitIsTrue()
	                                                                                                                   throws Exception {
		executeDataSet("org/openmrs/api/include/PatientServiceTest-createPatientIdentifierType.xml");
		List<PatientIdentifierType> patientIdentifierTypes = Context.getPatientService().getPatientIdentifierTypes(null,
		    null, null, true);
		
		Assert.assertTrue(!patientIdentifierTypes.isEmpty());
		
		for (PatientIdentifierType patientIdentifierType : patientIdentifierTypes) {
			Assert.assertTrue(patientIdentifierType.hasCheckDigit());
		}
	}
	
	/**
	 * @see {@link PatientService#getPatientIdentifierTypes(String,String,Boolean,Boolean)}
	 */
	@Test
	@Verifies(value = "should fetch patient identifier types without check digit when given has check digit is false", method = "getPatientIdentifierTypes(String,String,Boolean,Boolean)")
	public void getPatientIdentifierTypes_shouldFetchPatientIdentifierTypesWithoutCheckDigitWhenGivenHasCheckDigitIsFalse()
	                                                                                                                       throws Exception {
		executeDataSet("org/openmrs/api/include/PatientServiceTest-createPatientIdentifierType.xml");
		List<PatientIdentifierType> patientIdentifierTypes = Context.getPatientService().getPatientIdentifierTypes(null,
		    null, null, false);
		
		Assert.assertTrue(!patientIdentifierTypes.isEmpty());
		
		for (PatientIdentifierType patientIdentifierType : patientIdentifierTypes) {
			Assert.assertFalse(patientIdentifierType.hasCheckDigit());
		}
	}
	
	/**
	 * @see {@link PatientService#getPatientIdentifierTypes(String,String,Boolean,Boolean)}
	 */
	@Test
	@Verifies(value = "should fetch any patient identifier types when given has check digit is null", method = "getPatientIdentifierTypes(String,String,Boolean,Boolean)")
	public void getPatientIdentifierTypes_shouldFetchAnyPatientIdentifierTypesWhenGivenHasCheckDigitIsNull()
	                                                                                                        throws Exception {
		executeDataSet("org/openmrs/api/include/PatientServiceTest-createPatientIdentifierType.xml");
		List<PatientIdentifierType> patientIdentifierTypes = Context.getPatientService().getPatientIdentifierTypes(null,
		    null, null, null);
		
		Assert.assertTrue(!patientIdentifierTypes.isEmpty());
		
		Assert.assertEquals(4, patientIdentifierTypes.size());
	}
	
	/**
	 * @see {@link PatientService#savePatientIdentifierType(PatientIdentifierType)}
	 */
	@Test
	@Verifies(value = "should create new patient identifier type", method = "savePatientIdentifierType(PatientIdentifierType)")
	public void savePatientIdentifierType_shouldCreateNewPatientIdentifierType() throws Exception {
		PatientIdentifierType identifierType = new PatientIdentifierType();
		
		identifierType.setName("test");
		identifierType.setDescription("test description");
		identifierType.setRequired(false);
		
		Assert.assertNull(identifierType.getPatientIdentifierTypeId());
		
		patientService.savePatientIdentifierType(identifierType);
		
		PatientIdentifierType savedIdentifierType = patientService.getPatientIdentifierType(identifierType
		        .getPatientIdentifierTypeId());
		assertNotNull(savedIdentifierType);
		
	}
	
	/**
	 * @see {@link PatientService#savePatientIdentifierType(PatientIdentifierType)}
	 */
	@Test
	@Verifies(value = "should update existing patient identifier type", method = "savePatientIdentifierType(PatientIdentifierType)")
	public void savePatientIdentifierType_shouldUpdateExistingPatientIdentifierType() throws Exception {
		
		PatientIdentifierType identifierType = Context.getPatientService().getAllPatientIdentifierTypes().get(0);
		
		Assert.assertNotNull(identifierType);
		Assert.assertNotNull(identifierType.getPatientIdentifierTypeId());
		Assert.assertEquals(2, identifierType.getPatientIdentifierTypeId().intValue());
		Assert.assertNotSame("test", identifierType.getName());
		
		// Change existing patient identifier
		identifierType.setName("test");
		identifierType.setDescription("test description");
		identifierType.setRequired(false);
		
		patientService.savePatientIdentifierType(identifierType);
		
		PatientIdentifierType savedIdentifierType = patientService.getPatientIdentifierType(2);
		
		assertNotNull(savedIdentifierType);
		Assert.assertEquals("test", identifierType.getName());
		assertTrue(savedIdentifierType.equals(identifierType));
		
	}
	
	/**
	 * @see {@link PatientService#unretirePatientIdentifierType(PatientIdentifierType)}
	 */
	@Test
	@Verifies(value = "should untire patient identifier type", method = "unretirePatientIdentifierType(PatientIdentifierType)")
	public void unretirePatientIdentifierType_shouldUntirePatientIdentifierType() throws Exception {
		PatientIdentifierType identifierType = Context.getPatientService().getPatientIdentifierType(4);
		Assert.assertTrue(identifierType.isRetired());
		Assert.assertNotNull(identifierType.getRetiredBy());
		Assert.assertNotNull(identifierType.getRetireReason());
		Assert.assertNotNull(identifierType.getDateRetired());
		
		PatientIdentifierType unretiredIdentifierType = Context.getPatientService().unretirePatientIdentifierType(
		    identifierType);
		Assert.assertFalse(unretiredIdentifierType.isRetired());
		Assert.assertNull(unretiredIdentifierType.getRetiredBy());
		Assert.assertNull(unretiredIdentifierType.getRetireReason());
		Assert.assertNull(unretiredIdentifierType.getDateRetired());
	}
	
	/**
	 * @see {@link PatientService#unretirePatientIdentifierType(PatientIdentifierType)}
	 */
	@Test
	@Verifies(value = "should return unretired patient identifier type", method = "unretirePatientIdentifierType(PatientIdentifierType)")
	public void unretirePatientIdentifierType_shouldReturnUnretiredPatientIdentifierType() throws Exception {
		PatientIdentifierType identifierType = Context.getPatientService().getPatientIdentifierType(4);
		Assert.assertTrue(identifierType.isRetired());
		Assert.assertNotNull(identifierType.getRetiredBy());
		Assert.assertNotNull(identifierType.getRetireReason());
		Assert.assertNotNull(identifierType.getDateRetired());
		
		PatientIdentifierType unretiredIdentifierType = Context.getPatientService().unretirePatientIdentifierType(
		    identifierType);
		Assert.assertFalse(unretiredIdentifierType.isRetired());
		Assert.assertNull(unretiredIdentifierType.getRetiredBy());
		Assert.assertNull(unretiredIdentifierType.getRetireReason());
		Assert.assertNull(unretiredIdentifierType.getDateRetired());
		
	}
	
	/**
	 * @see {@link PatientService#unvoidPatient(Patient)}
	 */
	@Test
	@Verifies(value = "should unvoid given patient", method = "unvoidPatient(Patient)")
	public void unvoidPatient_shouldUnvoidGivenPatient() throws Exception {
		Patient patient = Context.getPatientService().getPatient(2);
		
		Patient voidedPatient = Context.getPatientService().voidPatient(patient, "Void for testing");
		Assert.assertTrue(voidedPatient.isVoided());
		Assert.assertNotNull(voidedPatient.getVoidedBy());
		Assert.assertNotNull(voidedPatient.getVoidReason());
		Assert.assertNotNull(voidedPatient.getDateVoided());
		
		Patient unvoidedPatient = Context.getPatientService().unvoidPatient(voidedPatient);
		Assert.assertFalse(unvoidedPatient.isVoided());
		Assert.assertNull(unvoidedPatient.getVoidedBy());
		Assert.assertNull(unvoidedPatient.getVoidReason());
		Assert.assertNull(unvoidedPatient.getDateVoided());
	}
	
	/**
	 * @see {@link PatientService#unvoidPatient(Patient)}
	 */
	@Test
	@Verifies(value = "should return unvoided patient", method = "unvoidPatient(Patient)")
	public void unvoidPatient_shouldReturnUnvoidedPatient() throws Exception {
		Patient patient = Context.getPatientService().getPatient(2);
		
		Patient voidedPatient = Context.getPatientService().voidPatient(patient, "Void for testing");
		Assert.assertTrue(voidedPatient.isVoided());
		Assert.assertNotNull(voidedPatient.getVoidedBy());
		Assert.assertNotNull(voidedPatient.getVoidReason());
		Assert.assertNotNull(voidedPatient.getDateVoided());
		
		Patient unvoidedPatient = Context.getPatientService().unvoidPatient(voidedPatient);
		Assert.assertFalse(unvoidedPatient.isVoided());
		Assert.assertNull(unvoidedPatient.getVoidedBy());
		Assert.assertNull(unvoidedPatient.getVoidReason());
		Assert.assertNull(unvoidedPatient.getDateVoided());
	}
	
	/**
	 * @see {@link PatientService#voidPatient(Patient,String)}
	 */
	@Test
	@Verifies(value = "should void given patient with given reason", method = "voidPatient(Patient,String)")
	public void voidPatient_shouldVoidGivenPatientWithGivenReason() throws Exception {
		Patient patient = Context.getPatientService().getPatient(2);
		Patient voidedPatient = Context.getPatientService().voidPatient(patient, "Void for testing");
		
		Assert.assertTrue(voidedPatient.isVoided());
		Assert.assertEquals("Void for testing", voidedPatient.getVoidReason());
	}
	
	/**
	 * @see {@link PatientService#voidPatient(Patient,String)}
	 */
	@Test
	@Verifies(value = "should void all patient identifiers associated with given patient", method = "voidPatient(Patient,String)")
	public void voidPatient_shouldVoidAllPatientIdentifiersAssociatedWithGivenPatient() throws Exception {
		Patient patient = Context.getPatientService().getPatient(2);
		Patient voidedPatient = Context.getPatientService().voidPatient(patient, "Void for testing");
		for (PatientIdentifier patientIdentifier : voidedPatient.getIdentifiers()) {
			Assert.assertTrue(patientIdentifier.isVoided());
			Assert.assertNotNull(patientIdentifier.getVoidedBy());
			Assert.assertNotNull(patientIdentifier.getVoidReason());
			Assert.assertNotNull(patientIdentifier.getDateVoided());
		}
		
	}
	
	/**
	 * @see {@link PatientService#voidPatient(Patient,String)}
	 */
	@Test
	@Verifies(value = "should return voided patient with given reason", method = "voidPatient(Patient,String)")
	public void voidPatient_shouldReturnVoidedPatientWithGivenReason() throws Exception {
		Patient patient = Context.getPatientService().getPatient(2);
		Patient voidedPatient = Context.getPatientService().voidPatient(patient, "Void for testing");
		
		Assert.assertTrue(voidedPatient.isVoided());
		Assert.assertNotNull(voidedPatient.getVoidedBy());
		Assert.assertNotNull(voidedPatient.getVoidReason());
		Assert.assertNotNull(voidedPatient.getDateVoided());
		Assert.assertEquals("Void for testing", voidedPatient.getVoidReason());
	}
	
	/**
	 * @see {@link PatientService#voidPatient(Patient,String)}
	 */
	@Test
	@Ignore
	// TODO fix: NullPointerException in RequiredDataAdvice
	@Verifies(value = "should return null when patient is null", method = "voidPatient(Patient,String)")
	public void voidPatient_shouldReturnNullWhenPatientIsNull() throws Exception {
		PatientService patientService = Context.getPatientService();
		Patient voidedPatient = patientService.voidPatient(null, "No null patient should be voided");
		Assert.assertNull(voidedPatient);
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
	@Verifies(value = "should fetch patient with given uuid", method = "getPatientByUuid(String)")
	public void getPatientByUuid_shouldFetchPateintWithGivenUuid() throws Exception {
		String uuid = "da7f524f-27ce-4bb2-86d6-6d1d05312bd5";
		Patient patient = Context.getPatientService().getPatientByUuid(uuid);
		Assert.assertEquals(2, (int) patient.getPatientId());
	}
	
	/**
	 * @see {@link PatientService#getPatientByUuid(String)}
	 */
	@Test
	@Verifies(value = "should return null if patient not found with given uuid ", method = "getPatientByUuid(String)")
	public void getPatientByUuid_shouldReturnNullIfPatientNotFoundWithUuid() throws Exception {
		Assert.assertNull(Context.getPatientService().getPatientByUuid("some invalid uuid"));
	}
	
	/**
	 * @see {@link PatientService#getPatientIdentifierByUuid(String)}
	 */
	@Test
	@Verifies(value = "should fetch patient identifier with given uuid", method = "getPatientIdentifierByUuid(String)")
	public void getPatientIdentifierByUuid_shouldFetchPatientIdentifierWithGivenUuid() throws Exception {
		String uuid = "ff41928c-3bca-48d9-a4dc-9198f6b2873b";
		PatientIdentifier patientIdentifier = Context.getPatientService().getPatientIdentifierByUuid(uuid);
		Assert.assertEquals(1, (int) patientIdentifier.getPatientIdentifierId());
	}
	
	/**
	 * @see {@link PatientService#getPatientIdentifierByUuid(String)}
	 */
	@Test
	@Verifies(value = "return null if patient identifier not found with given uuid", method = "getPatientIdentifierByUuid(String)")
	public void getPatientIdentifierByUuid_shouldReturnNullIfPatientIdentifierNotFoundWithGivenUuid() throws Exception {
		Assert.assertNull(Context.getPatientService().getPatientIdentifierByUuid("some invalid uuid"));
	}
	
	/**
	 * @see {@link PatientService#mergePatients(Patient,Patient)}
	 */
	@Test
	@Verifies(value = "should not copy over relationships that are only between the preferred and notpreferred patient", method = "mergePatients(Patient,Patient)")
	public void mergePatients_shouldNotCopyOverRelationshipsThatAreOnlyBetweenThePreferredAndNotpreferredPatient()
	                                                                                                              throws Exception {
		executeDataSet(PATIENT_RELATIONSHIPS_XML);
		
		Patient preferred = patientService.getPatient(999);
		Patient notPreferred = patientService.getPatient(2);
		
		patientService.mergePatients(preferred, notPreferred);
	}
	
	/**
	 * @see {@link PatientService#getPatientIdentifiers(String,List,List,List,Boolean)}
	 */
	@Test
	@Verifies(value = "should return only non voided patients and patient identifiers", method = "getPatientIdentifiers(String,List<QPatientIdentifierType;>,List<QLocation;>,List<QPatient;>,Boolean)")
	public void getPatientIdentifiers_shouldReturnOnlyNonVoidedPatientsAndPatientIdentifiers() throws Exception {
		// sanity check. make sure there is at least one voided patient
		Patient patient = patientService.getPatient(999);
		Assert.assertTrue("This patient should be voided", patient.isVoided());
		Assert.assertFalse("This test expects the patient to be voided BUT the identifier to be NONvoided",
		    ((PatientIdentifier) (patient.getIdentifiers().toArray()[0])).isVoided());
		
		// now fetch all identifiers
		List<PatientIdentifier> patientIdentifiers = patientService.getPatientIdentifiers(null, null, null, null, null);
		for (PatientIdentifier patientIdentifier : patientIdentifiers) {
			Assert.assertFalse("No voided identifiers should be returned", patientIdentifier.isVoided());
			Assert.assertFalse("No identifiers of voided patients should be returned", patientIdentifier.getPatient()
			        .isVoided());
		}
	}
	
	/**
	 * @see {@link PatientService#getPatients(String, String, java.util.List, boolean)}
	 */
	@Test
	@Verifies(value = "support simple regex", method = "getPatients(null,Identifier,null,false)")
	public void getPatients_shouldSupportSimpleRegex() throws Exception {
		Context.getAdministrationService().saveGlobalProperty(
		    new GlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_PATIENT_IDENTIFIER_REGEX, "^0*@SEARCH@([A-Z]+-[0-9])?$"));
		PatientIdentifier identifier = new PatientIdentifier("1234-4", new PatientIdentifierType(1), new Location(1));
		identifier.setCreator(new User(1));
		identifier.setDateCreated(new Date());
		Context.getPatientService().getPatient(2).addIdentifier(identifier);
		assertEquals(1, Context.getPatientService().getPatients("1234-4").size());
	}
	
	/**
	 * @see {@link PatientService#getPatients(String, String, java.util.List, boolean)}
	 */
	@Test
	@Verifies(value = "support pattern using last digit as check digit", method = "getPatients(null,Identifier,null,false)")
	public void getPatients_shouldSupportPatternUsingLastDigitAsCheckDigit() throws Exception {
		Context.getAdministrationService().saveGlobalProperty(
		    new GlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_PATIENT_IDENTIFIER_SEARCH_PATTERN,
		            "@SEARCH@,0@SEARCH@,@SEARCH-1@-@CHECKDIGIT@,0@SEARCH-1@-@CHECKDIGIT@"));
		//"^(0*@SEARCH-1@-@CHECKDIGIT@)$"));
		PatientIdentifier identifier = new PatientIdentifier("1234-4", new PatientIdentifierType(1), new Location(1));
		identifier.setCreator(new User(1));
		identifier.setDateCreated(new Date());
		Context.getPatientService().getPatient(2).addIdentifier(identifier);
		assertEquals(1, Context.getPatientService().getPatients("12344").size());
		assertEquals(1, Context.getPatientService().getPatients("1234-4").size());
	}
	
	/**
	 * @see {@link PatientService#getPatientIdentifier(Integer patientId)}
	 */
	@Test
	@Verifies(value = "should return the patient's identifier", method = "getPatientIdentifier(Integer patientIdentifierId)")
	public void getPatientIdentifier_shouldReturnThePatientsIdentifier() throws Exception {
		
		Assert.assertEquals("101-6", patientService.getPatientIdentifier(2).getIdentifier());
		Assert.assertEquals(1, patientService.getPatientIdentifier(2).getIdentifierType().getPatientIdentifierTypeId()
		        .intValue());
	}
	
	/**
	 * @see {@link PatientService#getPatientIdentifier(Integer patientId)}
	 */
	
	@Test
	@Verifies(value = "should void given patient identifier with given reason", method = "voidPatientIdentifier(PatientIdentifier, String)")
	public void voidPatientIdentifier_shouldVoidGivenPatientIdentifierWithGivenReason() throws Exception {
		Patient patient = patientService.getPatientIdentifier(3).getPatient();
		int oldActiveIdentifierSize = patient.getActiveIdentifiers().size();
		PatientIdentifier patientIdentifierToVoid = patientService.getPatientIdentifier(3);
		
		PatientIdentifier voidedIdentifier = patientService.voidPatientIdentifier(patientIdentifierToVoid, "Testing");
		//was the void reason set
		Assert.assertEquals("Testing", voidedIdentifier.getVoidReason());
		//patient's active identifiers must have reduced by 1 if the identifier was successfully voided
		Assert.assertEquals(oldActiveIdentifierSize - 1, patient.getActiveIdentifiers().size());
	}
	
	@Test
	@Verifies(value = "should create new patientIndentifier", method = "savePatientIdentifier(PatientIdentifier)")
	public void savePatientIdentifier_shouldCreateNewPatientIndentifier() throws Exception {
		PatientIdentifier patientIdentifier = new PatientIdentifier("677-56-6666", new PatientIdentifierType(4),
		        new Location(1));
		Patient associatedPatient = patientService.getPatient(2);
		patientIdentifier.setPatient(associatedPatient);
		PatientIdentifier createdPatientIdentifier = patientService.savePatientIdentifier(patientIdentifier);
		Assert.assertNotNull(createdPatientIdentifier);
		Assert.assertNotNull(createdPatientIdentifier.getPatientIdentifierId());
	}
	
	@Test
	@Verifies(value = "should update an existing patient identifier", method = "savePatientIdentifier(PatientIdentifier)")
	public void savePatientIdentifier_shouldUpdateAnExistingPatientIdentifier() throws Exception {
		PatientIdentifier patientIdentifier = patientService.getPatientIdentifier(7);
		patientIdentifier.setIdentifier("NEW-ID");
		PatientIdentifier updatedPatientIdentifier = patientService.savePatientIdentifier(patientIdentifier);
		Assert.assertNotNull(updatedPatientIdentifier);
		Assert.assertEquals("NEW-ID", updatedPatientIdentifier.getIdentifier());
	}
	
	@Test
	@Verifies(value = "should delete patient identifier from database", method = "purgePatientIdentifier(PatientIdentifier)")
	public void purgePatientIdentifier_shouldDeletePatientIdentifierFromDatabase() throws Exception {
		PatientIdentifier patientIdentifier = patientService.getPatientIdentifier(7);
		patientService.purgePatientIdentifier(patientIdentifier);
		Assert.assertNull(patientService.getPatientIdentifier(7));
		
	}
	
	/**
	 * @verifies {@link PatientService#savePatientIdentifier(PatientIdentifier)} test = should throw
	 *           an APIException when a null argument is passed
	 */
	@Test(expected = APIException.class)
	@Verifies(value = "should throw an APIException when a null argument is passed", method = "savePatientIdentifier(PatientIdentifier)")
	public void savePatientIdentifier_shouldThrowAnAPIExceptionWhenANullArgumentIsPassed() throws Exception {
		patientService.savePatientIdentifier(null);
	}
	
	/**
	 * @verifies {@link PatientService#savePatientIdentifier(PatientIdentifier)} test = should throw
	 *           an APIException when one of the required fields is null
	 */
	@Test(expected = APIException.class)
	@Verifies(value = "should throw an APIException when one of the required fields is null", method = "savePatientIdentifier(PatientIdentifier)")
	public void savePatientIdentifier_shouldThrowAnAPIExceptionWhenOneOfTheRequiredFieldsIsNull() throws Exception {
		PatientIdentifier patientIdentifier = patientService.getPatientIdentifier(7);
		patientIdentifier.setIdentifier(null);
		patientService.savePatientIdentifier(patientIdentifier);
		
	}
	
	/**
	 * @verifies {@link PatientService#savePatientIdentifier(PatientIdentifier)} test = should throw
	 *           an APIException if the patientIdentifier string is a white space
	 */
	@Test(expected = APIException.class)
	@Verifies(value = "should throw an APIException if the patientIdentifier string is a white space", method = "savePatientIdentifier(PatientIdentifier)")
	public void savePatientIdentifier_shouldThrowAnAPIExceptionIfThePatientIdentifierStringIsAWhiteSpace() throws Exception {
		PatientIdentifier patientIdentifier = patientService.getPatientIdentifier(7);
		patientIdentifier.setIdentifier(" ");
		patientService.savePatientIdentifier(patientIdentifier);
	}
	
	/**
	 * @verifies {@link PatientService#savePatientIdentifier(PatientIdentifier)} test = should throw
	 *           an APIException if the patientIdentifier string is an empty string
	 */
	@Test(expected = APIException.class)
	@Verifies(value = "should throw an APIException if the patientIdentifier string is an empty string", method = "savePatientIdentifier(PatientIdentifier)")
	public void savePatientIdentifier_shouldThrowAnAPIExceptionIfThePatientIdentifierStringIsAnEmptyString()
	                                                                                                        throws Exception {
		PatientIdentifier patientIdentifier = patientService.getPatientIdentifier(7);
		patientIdentifier.setIdentifier("");
		patientService.savePatientIdentifier(patientIdentifier);
	}
	
	/**
	 * @verifies {@link PatientService#voidPatientIdentifier(PatientIdentifier,String)} test =
	 *           should throw an APIException if the reason is null
	 */
	@Test(expected = APIException.class)
	@Verifies(value = "should throw an APIException if the reason is null", method = "voidPatientIdentifier(PatientIdentifier, String)")
	public void voidPatientIdentifier_shouldThrowAnAPIExceptionIfTheReasonIsNull() throws Exception {
		PatientIdentifier patientIdentifierToVoid = patientService.getPatientIdentifier(3);
		patientService.voidPatientIdentifier(patientIdentifierToVoid, null);
	}
	
	/**
	 * @verifies {@link PatientService#voidPatientIdentifier(PatientIdentifier,String)} test =
	 *           should throw an APIException if the reason is an empty string
	 */
	@Test(expected = APIException.class)
	@Verifies(value = "should throw an APIException if the reason is an empty string", method = "voidPatientIdentifier(PatientIdentifier, String)")
	public void voidPatientIdentifier_shouldThrowAnAPIExceptionIfTheReasonIsAnEmptyString() throws Exception {
		PatientIdentifier patientIdentifierToVoid = patientService.getPatientIdentifier(3);
		patientService.voidPatientIdentifier(patientIdentifierToVoid, "");
	}
	
	/**
	 * @verifies {@link PatientService#voidPatientIdentifier(PatientIdentifier,String)} test =
	 *           should throw an APIException if the reason is a white space character
	 */
	@Test(expected = APIException.class)
	@Verifies(value = "should throw an APIException if the reason is a white space character", method = "voidPatientIdentifier(PatientIdentifier, String)")
	public void voidPatientIdentifier_shouldThrowAnAPIExceptionIfTheReasonIsAWhiteSpaceCharacter() throws Exception {
		PatientIdentifier patientIdentifierToVoid = patientService.getPatientIdentifier(3);
		patientService.voidPatientIdentifier(patientIdentifierToVoid, " ");
	}
	
	/**
	 * @see {@link PatientService#getProblems(Patient)}
	 */
	@Test
	@Verifies(value = "return empty list if no problems exist for this Patient", method = "getProblems(Patient)")
	public void getProblems_shouldReturnEmptyListIfNoProblemsExistForThisPatient() throws Exception {
		executeDataSet(ACTIVE_LIST_INITIAL_XML);
		
		Patient p = patientService.getPatient(3);
		List<Problem> problems = patientService.getProblems(p);
		Assert.assertNotNull(problems);
		assertEqualsInt(0, problems.size());
	}
	
	/**
	 * @see {@link PatientService#getAllergies(Patient)}
	 */
	@Test
	@Verifies(value = "return empty list if no allergies exist for this Patient", method = "getAllergies(Patient)")
	public void getAllergies_shouldReturnEmptyListIfNoAllergiesExistForThisPatient() throws Exception {
		executeDataSet(ACTIVE_LIST_INITIAL_XML);
		
		Patient p = patientService.getPatient(3);
		List<Allergy> allergies = patientService.getAllergies(p);
		Assert.assertNotNull(allergies);
		assertEqualsInt(0, allergies.size());
	}
	
	/**
	 * @see {@link PatientService#getAllergy(Integer)}
	 */
	@Test
	@Verifies(value = "return an allergy by id", method = "getAllergy(Integer)")
	public void getAllergy_shouldReturnAnAllergyById() throws Exception {
		executeDataSet(ACTIVE_LIST_INITIAL_XML);
		
		Allergy allergy = patientService.getAllergy(1);
		Assert.assertNotNull(allergy);
		Assert.assertNotNull(allergy.getActiveListId());
		Assert.assertNotNull(allergy.getActiveListType());
		Assert.assertNotNull(allergy.getAllergen());
		Assert.assertNotNull(allergy.getStartDate());
	}
	
	/**
	 * @see {@link PatientService#saveProblem(Problem)}
	 */
	@Test
	@Verifies(value = "save the problem and set the weight for correct ordering", method = "saveProblem(ProblemListItem)")
	public void saveProblem_shouldSaveTheProblemAndSetTheWeightForCorrectOrdering() throws Exception {
		executeDataSet(ACTIVE_LIST_INITIAL_XML);
		
		Patient p = patientService.getPatient(2);
		
		List<Problem> problems = patientService.getProblems(p);
		assertEqualsInt(1, problems.size());

		Problem problem = new Problem();
		problem.setPerson(p);
		problem.setProblem(Context.getConceptService().getConcept(88));//Aspirin
		
		patientService.saveProblem(problem);
		
		problems = patientService.getProblems(p);
		Assert.assertNotNull(problems);
		assertEqualsInt(2, problems.size());
		
		problem = problems.get(1);
		assertEqualsInt(88, problem.getProblem().getConceptId());
		Assert.assertNotNull(problem.getPerson());
		Assert.assertNotNull(problem.getStartDate());
		Assert.assertEquals(Double.valueOf(2), problem.getSortWeight());
	}
	
	/**
	 * @see {@link PatientService#resolveProblem(Problem, String)}
	 */
	@Test
	@Verifies(value = "set the end date for the problem", method = "resolveProblem(ProblemListItem, String)")
	public void resolveProblem_shouldSetTheEndDateForTheProblem() throws Exception {
		executeDataSet(ACTIVE_LIST_INITIAL_XML);
		
		Patient p = patientService.getPatient(2);
		
		List<Problem> problems = patientService.getProblems(p);
		Assert.assertNotNull(problems);
		patientService.removeProblem(problems.get(0), "resolving by retiring");
		
		problems = patientService.getProblems(p);
		Assert.assertNotNull(problems);
		Assert.assertNotNull(problems.get(0).getEndDate());
	}
	
	/**
	 * @see {@link PatientService#saveAllergy(Problem)}
	 */
	@Test
	@Verifies(value = "save the allergy", method = "saveAllergy(AllergyListItem)")
	public void saveAllergy_shouldSaveTheAllergy() throws Exception {
		executeDataSet(ACTIVE_LIST_INITIAL_XML);
		
		Patient p = patientService.getPatient(2);
		Allergy allergen = new Allergy();
		allergen.setPerson(p);
		allergen.setAllergen(Context.getConceptService().getConcept(88));//Aspirin
		
		patientService.saveAllergy(allergen);
		
		List<Allergy> allergies = patientService.getAllergies(p);
		Assert.assertNotNull(allergies);
		assertEqualsInt(2, allergies.size());
		
		for (Allergy a : allergies) {
			if (a.getAllergen().getConceptId().equals(88)) {
				allergen = a;
				break;
			}
		}
		
		Assert.assertNotNull(allergen.getPerson());
		Assert.assertNotNull(allergen.getStartDate());
	}
	
	/**
	 * @see {@link PatientService#resolveAllergy(Problem, String)}
	 */
	@Test
	@Verifies(value = "set the end date for the allergy", method = "resolveAllergy(AllergyListItem, String)")
	public void resolveAllergy_shouldSetTheEndDateForTheAllergy() throws Exception {
		executeDataSet(ACTIVE_LIST_INITIAL_XML);
		
		Patient p = patientService.getPatient(2);
		
		List<Allergy> allergies = patientService.getAllergies(p);
		Assert.assertNotNull(allergies);
		patientService.removeAllergy(allergies.get(0), "resolving by retiring");
		
		allergies = patientService.getAllergies(p);
		Assert.assertNotNull(allergies);
		Assert.assertNotNull(allergies.get(0).getEndDate());
	}
	
	private void assertEqualsInt(int expected, Integer actual) throws Exception {
		Assert.assertEquals(Integer.valueOf(expected), actual);
	}

	/**
	 * @see {@link PatientService#mergePatients(Patient,Patient)}
	 */
	@Test
	@Verifies(value = "should not create duplicate relationships", method = "mergePatients(Patient,Patient)")
	public void mergePatients_shouldNotCreateDuplicateRelationships() throws Exception {
		executeDataSet(PATIENT_RELATIONSHIPS_XML);
		
		Patient preferred = patientService.getPatient(999);
		Patient notPreferred = patientService.getPatient(2);
		
		// expected relationships before merge:
		//  * 2->1 (type 2)
		//  * 999->2 (type 5)
		//  * 999->1 (type 2)
		//  * 7->999 (type 4)
		//  * 502->2 (type 1)
		//  * 7->2 (type 1)
		patientService.mergePatients(preferred, notPreferred);
		
		// expected relationships after merge:
		//  * 999->1 (type 2)
		//  * 7->999 (type 4)
		//  * 502->999 (type 1)
		//  * 7->999 (type 1)

		// check for a relationship that should not be duplicated: 2->1 and 999->1
		List<Relationship> rels = personService.getRelationships(preferred, new Person(1), new RelationshipType(2));
		assertEquals("duplicate relationships were not removed", 1, rels.size());
	}

	/**
	 * @see {@link PatientService#mergePatients(Patient,Patient)}
	 */
	@Test
	@Verifies(value = "should void all relationships for non preferred patient", method = "mergePatients(Patient,Patient)")
	public void mergePatients_shouldVoidAllRelationshipsForNonPreferredPatient() throws Exception {
		executeDataSet(PATIENT_RELATIONSHIPS_XML);
		
		Patient preferred = patientService.getPatient(999);
		Patient notPreferred = patientService.getPatient(2);
		
		patientService.mergePatients(preferred, notPreferred);
		
		List<Relationship> rels = personService.getRelationshipsByPerson(notPreferred);
		assertTrue("there should not be any relationships for non preferred", rels.isEmpty());
	}

	/**
	 * @see {@link PatientService#mergePatients(Patient,Patient)}
	 */
	@Test
	@Verifies(value = "should not void relationships for same type and side with different relatives", method = "mergePatients(Patient,Patient)")
	public void mergePatients_shouldNotVoidRelationshipsForSameTypeAndSideWithDifferentRelatives() throws Exception {
		executeDataSet(PATIENT_RELATIONSHIPS_XML);
		
		Patient preferred = patientService.getPatient(999);
		Patient notPreferred = patientService.getPatient(2);
		
		// expected relationships before merge:
		//  * 2->1 (type 2)
		//  * 999->2 (type 5)
		//  * 999->1 (type 2)
		//  * 7->999 (type 4)
		//  * 502->2 (type 1)
		//  * 7->999 (type 1)
		patientService.mergePatients(preferred, notPreferred);
		
		// expected relationships after merge:
		//  * 999->1 (type 2)
		//  * 7->999 (type 4)
		//  * 502->999 (type 1)
		//  * 7->999 (type 1)
		
		// check for relationships that should not be removed: 7->999 (type 4) and 7->999 (type 1)
		List<Relationship> rels = personService.getRelationships(new Person(7), preferred, new RelationshipType(4));
		assertEquals("7->999 (type 4) was removed", 1, rels.size());
		
		rels = personService.getRelationships(new Person(7), preferred, new RelationshipType(1));
		assertEquals("7->999 (type 1) was removed", 1, rels.size());
	}

}
