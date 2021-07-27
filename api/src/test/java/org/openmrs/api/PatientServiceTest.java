/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.openmrs.api.context.Context.getUserService;
import static org.openmrs.test.TestUtil.assertCollectionContentsEquals;
import static org.openmrs.util.AddressMatcher.containsAddress;
import static org.openmrs.util.NameMatcher.containsFullName;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.GlobalProperty;
import org.openmrs.Location;
import org.openmrs.Obs;
import org.openmrs.Order;
import org.openmrs.OrderType;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.PatientIdentifierType.UniquenessBehavior;
import org.openmrs.PatientProgram;
import org.openmrs.Person;
import org.openmrs.PersonAddress;
import org.openmrs.PersonAttribute;
import org.openmrs.PersonName;
import org.openmrs.Relationship;
import org.openmrs.RelationshipType;
import org.openmrs.User;
import org.openmrs.Visit;
import org.openmrs.api.context.Context;
import org.openmrs.api.impl.PatientServiceImpl;
import org.openmrs.api.impl.PatientServiceImplTest;
import org.openmrs.comparator.PatientIdentifierTypeDefaultComparator;
import org.openmrs.patient.IdentifierValidator;
import org.openmrs.patient.impl.LuhnIdentifierValidator;
import org.openmrs.person.PersonMergeLog;
import org.openmrs.person.PersonMergeLogData;
import org.openmrs.serialization.SerializationException;
import org.openmrs.test.SkipBaseSetup;
import org.openmrs.test.TestUtil;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.util.OpenmrsUtil;

/**
 * This class tests methods in the PatientService class TODO Add methods to test all methods in
 * PatientService class
 * Before adding a test, check if you can better test it with @see org.openmrs.api.{@link PatientServiceImplTest}, which
 * does not use the context, but mocks dependencies.
 */
public class PatientServiceTest extends BaseContextSensitiveTest {
	
	// Datasets
	protected static final String CREATE_PATIENT_XML = "org/openmrs/api/include/PatientServiceTest-createPatient.xml";
	
	protected static final String CREATE_PATIENT_VALID_IDENT_XML = "org/openmrs/api/include/PatientServiceTest-createPatientValidIdent.xml";
	
	protected static final String JOHN_PATIENTS_XML = "org/openmrs/api/include/PatientServiceTest-lotsOfJohns.xml";
	
	protected static final String USERS_WHO_ARE_PATIENTS_XML = "org/openmrs/api/include/PatientServiceTest-usersWhoArePatients.xml";
	
	protected static final String USER_WHO_IS_NOT_PATIENT_XML = "org/openmrs/api/include/PatientServiceTest-userNotAPatient.xml";
	
	protected static final String FIND_PATIENTS_XML = "org/openmrs/api/include/PatientServiceTest-findPatients.xml";
	
	protected static final String FIND_PATIENTS_ACCENTS_XML = "org/openmrs/api/include/PatientServiceTest-findPatientsAccents.xml";
	
	private static final String PATIENT_RELATIONSHIPS_XML = "org/openmrs/api/include/PersonServiceTest-createRelationship.xml";
	
	private static final String ENCOUNTERS_FOR_VISITS_XML = "org/openmrs/api/include/PersonServiceTest-encountersForVisits.xml";
	
	private static final String PATIENT_MERGE_XML = "org/openmrs/api/include/PatientServiceTest-mergePatients.xml";

	private static final String PATIENT_MERGE_OBS_WITH_GROUP_MEMBER = "org/openmrs/api/include/PatientServiceTest-mergePatientWithExistingObsHavingGroupMember.xml";
	
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
	@BeforeEach
	public void runBeforeAllTests() throws Exception {
		patientService = Context.getPatientService();
		personService = Context.getPersonService();
		adminService = Context.getAdministrationService();
		locationService = Context.getLocationService();

		updateSearchIndex();
	}
	
	private void voidOrders(Collection<Patient> patientsWithOrders) {
		OrderService os = Context.getOrderService();
		for (Patient p : patientsWithOrders) {
			List<Order> orders = os.getAllOrdersByPatient(p);
			for (Order o : orders) {
				o.setVoided(true);
			}
		}
	}

	private void voidOrdersForType(Collection<Patient> patients, OrderType ot) {
		patients.forEach(patient -> Context.getOrderService().getAllOrdersByPatient(patient).forEach(order -> {
			if(order.getOrderType().equals(ot)){
				order.setVoided(true);
			}
		}));
	}

	private boolean hasActiveOrderOfType(Patient patient, String orderTypeName) {
		OrderType drugOrder = Context.getOrderService().getOrderTypeByName(orderTypeName);
		List<Order> preferredPatientOrders = Context.getOrderService().getAllOrdersByPatient(patient).stream()
				.filter(Order::isActive)
				.filter(order -> Objects.equals(drugOrder, order.getOrderType()))
				.collect(Collectors.toList());
		return !preferredPatientOrders.isEmpty();
	}

	/**
	 * @see PatientService#getAllIdentifierValidators()
	 */
	@Test
	public void getAllIdentifierValidators_shouldReturnAllRegisteredIdentifierValidators() throws Exception {
		Collection<IdentifierValidator> expectedValidators = new HashSet<>();
		expectedValidators.add(patientService.getIdentifierValidator("org.openmrs.patient.impl.LuhnIdentifierValidator"));
		expectedValidators
		        .add(patientService.getIdentifierValidator("org.openmrs.patient.impl.VerhoeffIdentifierValidator"));
		assertEquals(2, patientService.getAllIdentifierValidators().size());
		assertCollectionContentsEquals(expectedValidators, patientService.getAllIdentifierValidators());
	}
	
	/**
	 * @see PatientService#getIdentifierValidator(String)
	 */
	@Test
	public void getAllIdentifierValidators_shouldTreatEmptyStringsLikeANullEntry() throws Exception {
		assertEquals(null, patientService.getIdentifierValidator(""));
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
		// patient.removeAddress(pAddress);
		
		patient.setBirthdate(new Date());
		patient.setBirthdateEstimated(true);
		patient.setDeathDate(new Date());
		patient.setCauseOfDeath(new Concept());
		patient.setGender("male");
		patient.setDeathdateEstimated(true);
		
		return patient;
	}
	
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
		// patient.removeAddress(pAddress);
		
		patient.setBirthdateEstimated(true);
		patient.setBirthdate(new Date());
		patient.setBirthdateEstimated(true);
		patient.setDeathDate(new Date());
		patient.setCauseOfDeath(new Concept());
		patient.setGender("male");
		
		List<PatientIdentifierType> patientIdTypes = patientService.getAllPatientIdentifierTypes();
		assertNotNull(patientIdTypes);
		PatientIdentifier patientIdentifier = new PatientIdentifier();
		patientIdentifier.setIdentifier("123-0");
		patientIdentifier.setIdentifierType(patientIdTypes.get(0));
		patientIdentifier.setLocation(new Location(1));
		patientIdentifier.setPreferred(true);
		
		Set<PatientIdentifier> patientIdentifiers = new TreeSet<>();
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
		PatientIdentifier ident1 = new PatientIdentifier("123-1", pit, locationService.getLocation(1));
		PatientIdentifier ident2 = new PatientIdentifier("123", pit, locationService.getLocation(1));
		PatientIdentifier ident3 = new PatientIdentifier("123-0", pit, locationService.getLocation(1));
		PatientIdentifier ident4 = new PatientIdentifier("123-A", pit, locationService.getLocation(1));
		
		try {
			ident1.setPreferred(true);
			patient.addIdentifier(ident1);
			patientService.savePatient(patient);
			fail("Patient creation should have failed with identifier " + ident1.getIdentifier());
		}
		catch (InvalidCheckDigitException ex) {}
		catch (APIException e) {
			if (!(e.getMessage() != null && e.getMessage().contains(
			    "failed to validate with reason: "
			            + Context.getMessageSourceService().getMessage("PatientIdentifier.error.checkDigitWithParameter",
			                new Object[] { ident1.getIdentifier() }, null)))) {
				fail("Patient creation should have failed with identifier " + ident1.getIdentifier());
			}
		}
		
		patient.removeIdentifier(ident1);
		
		try {
			ident2.setPreferred(true);
			patient.addIdentifier(ident2);
			patientService.savePatient(patient);
			fail("Patient creation should have failed with identifier " + ident2.getIdentifier());
		}
		catch (InvalidCheckDigitException ex) {}
		catch (APIException e) {
			if (!(e.getMessage() != null && e.getMessage().contains(
			    "failed to validate with reason: "
			            + Context.getMessageSourceService().getMessage("PatientIdentifier.error.unallowedIdentifier",
			                new Object[] { ident2.getIdentifier(), new LuhnIdentifierValidator().getName() }, null)))) {
				fail("Patient creation should have failed with identifier " + ident2.getIdentifier());
			}
		}
		
		patient.removeIdentifier(ident2);
		
		try {
			ident3.setPreferred(true);
			patient.addIdentifier(ident3);
			patientService.savePatient(patient);
			patientService.purgePatient(patient);
			patient.removeIdentifier(ident3);
			ident4.setPreferred(true);
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
		updateSearchIndex();
		
		// get the first patient
		Collection<Patient> johnPatients = patientService.getPatients("John", null, null, false);
		assertNotNull(johnPatients, "There should be a patient named 'John'");
		assertFalse(johnPatients.isEmpty(), "There should be a patient named 'John'");
		
		Patient firstJohnPatient = johnPatients.iterator().next();
		
		// get a list of patients with this identifier, make sure the john
		// patient is actually there
		String identifier = firstJohnPatient.getPatientIdentifier().getIdentifier();
		assertNotNull("Uh oh, the patient doesn't have an identifier", identifier);
		List<Patient> patients = patientService.getPatients(identifier, null, null, false);
		assertTrue(patients.contains(firstJohnPatient), "Odd. The firstJohnPatient isn't in the list of patients for this identifier");
		
	}
	
	// /**
	// * This method should be uncommented when you want to examine the actual
	// hibernate
	// * sql calls being made. The calls that should be limiting the number of
	// returned
	// * patients should show a "top" or "limit" in the sql -- this proves
	// hibernate's
	// * use of a native sql limit as opposed to a java-only limit.
	// *
	// * Note: if enabled, this test will be considerably slower
	// *
	// * @see org.openmrs.test.BaseContextSensitiveTest#getRuntimeProperties()
	// */
	//* @Override
	//* public Properties getRuntimeProperties() {
	//* 		Properties props = super.getRuntimeProperties();
	//* 		props.setProperty("hibernate.show_sql", "true");
	//*
	//* 		return props;
	//* }
	
	/**
	 * Check that the patient list is kept under the max for getPatientsByName
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldGetPatientsByNameShouldLimitSize() throws Exception {
		executeDataSet(JOHN_PATIENTS_XML);
		updateSearchIndex();
		
		Collection<Patient> patients = patientService.getPatients("John", null, null, false);
		
		assertTrue(patients.size() == 1000, "The patient list size should be restricted to under the max (1000). its " + patients.size());
		
		/*
		 * Temporary code to create lots of johns file
		 * 
		 * File file = new File("test/api/" + JOHN_PATIENTS_XML); PrintWriter
		 * writer = new PrintWriter(file);
		 * 
		 * int x = 3; while (x < 1010) { String line =
		 * "<person person_id=\"2\" dead=\"false\" creator=\"1\" date_created=\"1999-01-01 00:00:00.0\" voided=\"false\" gender=\"M\" />"
		 * ; writer.println(line.replaceAll("2",
		 * Integer.valueOf(x).toString()));
		 * 
		 * line =
		 * "<person_name person_id=\"2\" person_name_id=\"2\" preferred=\"1\" creator=\"1\" date_created=\"1999-01-01 00:00:00.0\" voided=\"false\" given_name=\"John2\" middle_name=\" \" family_name=\"Patient\" />"
		 * ; writer.println(line.replaceAll("2",
		 * Integer.valueOf(x).toString()));
		 * 
		 * line =
		 * "<patient patient_id=\"2\" creator=\"1\" date_created=\"1999-03-01 00:00:00.0\" voided=\"false\" />"
		 * ; writer.println(line.replaceAll("2",
		 * Integer.valueOf(x).toString()));
		 * 
		 * line =
		 * "<patient_identifier patient_id=\"2\" creator=\"1\" date_created=\"1999-03-01 00:00:00.0\" identifier=\"2\" identifier_type=\"1\" preferred=\"1\" voided=\"false\" location_id=\"1\" />"
		 * ; writer.println(line.replaceAll("2",
		 * Integer.valueOf(x).toString()));
		 * 
		 * x = x + 1; }
		 * 
		 * writer.close();
		 */
	}
	
	/**
	 * Test the PatientService.getPatients(String, String, List) method with both an identifier and
	 * an identifiertype
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldGetPatientsByIdegntifierAndIdentifierType() throws Exception {
		initializeInMemoryDatabase();
		executeDataSet(FIND_PATIENTS_XML);
		authenticate();
		updateSearchIndex();
		
		List<PatientIdentifierType> types = new ArrayList<>();
		types.add(new PatientIdentifierType(1));
		// make sure we get back only one patient
		List<Patient> patients = patientService.getPatients("4567", null, types, false);
		assertEquals(1, patients.size());
		
		// make sure error cases are found & catched
		patients = patientService.getPatients("4567", null, null, false);
		assertThat(patients, is(empty()));
		patients = patientService.getPatients("4567", null, null, false, -2, -2);
		assertThat(patients, is(empty()));		
		
		// make sure we get back only one patient
		patients = patientService.getPatients("1234", null, null, false);
		assertEquals(1, patients.size());
		
		// make sure we can search a padded identifier
		patients = patientService.getPatients("00000001234", null, null, false);
		assertEquals(1, patients.size());
		patients = patientService.getPatients("123", null, types, false);
		assertEquals(1, patients.size());
		patients = patientService.getPatients("123", null, types, true);
		assertEquals(0, patients.size());
		patients = patientService.getPatients("123", null, types, false);
		assertEquals(1, patients.size());		
		
		// change to use another identifier type
		// THESE TWO TESTS CURRENTLY FAIL
		types = new ArrayList<>();
		types.add(new PatientIdentifierType(2));
		patients = patientService.getPatients("1234", null, types, false);
		assertEquals(0, patients.size());
		
		patients = patientService.getPatients(null, "1234", types, false);
		assertEquals(0, patients.size());
	}
	
	@Test
	public void shouldGetPatientsByIdentifierAndMoreThanOneIdentifierTypes() throws Exception {
		initializeInMemoryDatabase();
		executeDataSet(FIND_PATIENTS_XML);
		authenticate();
		updateSearchIndex();
		
		List<PatientIdentifierType> types = new ArrayList<>();
		types.add(new PatientIdentifierType(1));
		types.add(new PatientIdentifierType(2));
		List<Patient> patients = patientService.getPatients("4567", null, types, false);
		assertEquals(1, patients.size());
	}
	
	/**
	 * @see PatientService#purgePatientIdentifierType(PatientIdentifierType)
	 */
	@Test
	public void purgePatientIdentifierType_shouldDeleteTypeFromDatabase() throws Exception {
		PatientIdentifierType patientIdentifierType = new PatientIdentifierType();
		
		patientIdentifierType.setName("testing");
		patientIdentifierType.setDescription("desc");
		patientIdentifierType.setRequired(false);
		
		patientService.savePatientIdentifierType(patientIdentifierType);
		
		PatientIdentifierType type = patientService.getPatientIdentifierType(patientIdentifierType.getId());
		
		patientService.purgePatientIdentifierType(type);
		assertNull(patientService.getPatientIdentifierType(patientIdentifierType.getId()));
	}
	
	/**
	 * @see PatientService#savePatientIdentifierType(PatientIdentifierType)
	 */
	@Test
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
	 * @see PatientService#savePatientIdentifierType(PatientIdentifierType)
	 */
	@Test
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
		// the creator of the next. We need to make sure hibernate isn't
		// caching and returning different person objects when it shouldn't be
		Patient patient2 = patientService.getPatient(2);
		assertTrue(patient2.getClass().equals(Patient.class), "When getting a patient, it should be of the class patient, not: " + patient2.getClass());
		
		Patient patient3 = patientService.getPatient(3);
		assertTrue(patient3.getClass().equals(Patient.class), "When getting a patient, it should be of the class patient, not: " + patient3.getClass());
		
		User user2 = Context.getUserService().getUser(2);
		assertTrue(User.class.isAssignableFrom(user2.getClass()), "When getting a user, it should be of the class user, not: " + user2.getClass());
		
	}
	
	/**
	 * @see PatientService#getPatients(String)
	 */
	@Test
	public void getPatients_shouldForceSearchStringToBeGreaterThanMinsearchcharactersGlobalProperty() throws Exception {
		// make sure we can get patients with the default of 3
		Context.getAdministrationService().setGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_PATIENT_SEARCH_MATCH_MODE, OpenmrsConstants.GLOBAL_PROPERTY_PATIENT_SEARCH_MATCH_START);
		
		assertEquals(1, Context.getPatientService().getPatients("Colle").size());
		
		Context.clearSession();
		Context.getAdministrationService().saveGlobalProperty(
		    new GlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_MIN_SEARCH_CHARACTERS, "4"));
		
		assertEquals(0, Context.getPatientService().getPatients("Col").size());
	}
	
	/**
	 * @see PatientService#getPatients(String)
	 */
	@Test
	public void getPatients_shouldAllowSearchStringToBeOneAccordingToMinsearchcharactersGlobalProperty() throws Exception {
		initializeInMemoryDatabase();
		executeDataSet(FIND_PATIENTS_XML);
		updateSearchIndex();
		
		// make sure the default of "2" kicks in and blocks any results
		assertEquals(0, Context.getPatientService().getPatients("J").size());
		
		Context.clearSession();
		Context.getAdministrationService().saveGlobalProperty(
		    new GlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_MIN_SEARCH_CHARACTERS, "1"));
		
		// there is a patient will middle name "F", so this should generate a hit.
		assertEquals(1, Context.getPatientService().getPatients("F").size());
	}
	
	/**
	 * @see PatientService#getPatient(Integer) Does this test duplicate
	 *      getPatient_shouldReturnNullObjectIfPatientIdDoesntExist()?
	 */
	@Test
	public void getPatient_shouldReturnNullObjectIfPatientIdDoesntExist() throws Exception {
		assertNull(Context.getPatientService().getPatient(1234512093));
	}
	
	/**
	 * @see PatientServiceImpl#mergePatients(Patient,Patient)
	 */
	@Test
	public void mergePatients_shouldNotMergePatientWithItself() throws Exception {
		assertThrows(APIException.class, () -> Context.getPatientService().mergePatients(new Patient(2), new Patient(2)));
	}
	
	/**
	 * @see PatientService#mergePatients(Patient,Patient)
	 */
	@Test
	public void mergePatients_shouldChangeUserRecordsOfNonPreferredPersonToPreferredPerson() throws Exception {
		executeDataSet(USERS_WHO_ARE_PATIENTS_XML);
		Patient notPreferred = patientService.getPatient(2);
		voidOrders(Collections.singleton(notPreferred));
		Context.getPatientService().mergePatients(patientService.getPatient(6), notPreferred);
		User user = Context.getUserService().getUser(2);
		assertEquals(6, user.getPerson().getId().intValue());
	}
	
	/**
	 * @see PatientService#mergePatients(Patient,Patient)
	 */
	@Test
	public void mergePatients_shouldMergeVisitsFromNonPreferredToPreferredPatient() throws Exception {
		executeDataSet(ENCOUNTERS_FOR_VISITS_XML);
		VisitService visitService = Context.getVisitService();
		
		Patient notPreferred = patientService.getPatient(2);
		voidOrders(Collections.singleton(notPreferred));
		Patient preferred = patientService.getPatient(6);
		
		// patient 2 (not preferred) has 3 unvoided visits (id = 1, 2, 3) and 1 voided visit (id = 6)
		Visit visit1 = visitService.getVisit(1);
		Visit visit2 = visitService.getVisit(2);
		Visit visit3 = visitService.getVisit(3);
		Visit visit6 = visitService.getVisit(6);
		// patient 6 (preferred) has 2 unvoided visits (id = 4, 5) and no voided visits
		Visit visit4 = visitService.getVisit(4);
		Visit visit5 = visitService.getVisit(5);
		
		List<String> encounterUuidsThatShouldBeMoved = new ArrayList<>();
		encounterUuidsThatShouldBeMoved.add(Context.getEncounterService().getEncounter(6).getUuid());
		for (Visit v : Arrays.asList(visit1, visit2, visit3)) {
			for (Encounter e : v.getEncounters()) {
				encounterUuidsThatShouldBeMoved.add(e.getUuid());
			}
		}
		List<Obs> originalUnvoidedObs = Context.getObsService().getObservationsByPerson(notPreferred);
		
		PersonMergeLog mergeLog = mergeAndRetrieveAudit(preferred, notPreferred);
		
		Patient merged = patientService.getPatient(preferred.getId());
		List<Visit> mergedVisits = visitService.getVisitsByPatient(merged, true, true);
		
		assertThat(mergedVisits.size(), is(6));
		// in order to keep this test passing when (someday?) we copy visits instead of moving them, use matchers here:
		assertThat(mergedVisits, containsInAnyOrder(matchingVisit(visit1), matchingVisit(visit2), matchingVisit(visit3),
		    matchingVisit(visit4), matchingVisit(visit5), matchingVisit(visit6)));

		// be sure nothing slipped through without being assigned to the right patient (probably not necessary)
		for (Visit v : mergedVisits) {
			for (Encounter e : v.getEncounters()) {
				assertThat(e.getPatient(), is(v.getPatient()));
				for (Obs o : e.getAllObs(true)) {
					if (!originalUnvoidedObs.contains(o)) {
						assertThat(o.getPerson().getId(), is(v.getPatient().getId()));
					}
				}
			}
		}
		
		// now check that moving visits and their contained encounters was audited correctly
		PersonMergeLogData mergeLogData = mergeLog.getPersonMergeLogData();
		assertThat(mergeLogData.getMovedVisits().size(), is(4));
		assertThat(mergeLogData.getMovedVisits(), containsInAnyOrder(visit1.getUuid(), visit2.getUuid(), visit3.getUuid(),
		    visit6.getUuid()));
		
		assertThat(mergeLogData.getMovedEncounters().size(), is(encounterUuidsThatShouldBeMoved.size()));
		assertThat(mergeLogData.getMovedEncounters(), containsInAnyOrder(encounterUuidsThatShouldBeMoved.toArray()));
	}
	
	private Matcher<Visit> matchingVisit(final Visit expected) {
		return new Matcher<Visit>() {
			
			@Override
			public boolean matches(Object argument) {
				Visit visit = (Visit) argument;
				return OpenmrsUtil.nullSafeEquals(visit.getLocation(), expected.getLocation())
				        && OpenmrsUtil.nullSafeEquals(visit.getVisitType(), expected.getVisitType())
				        && OpenmrsUtil.nullSafeEquals(visit.getIndication(), expected.getIndication())
				        && OpenmrsUtil.nullSafeEquals(visit.getStartDatetime(), expected.getStartDatetime())
				        && OpenmrsUtil.nullSafeEquals(visit.getStopDatetime(), expected.getStopDatetime())
				        && (visit.getEncounters().size() == expected.getEncounters().size());
			}

			@Override
			public void describeTo(Description description) {}

			@Override
			public void describeMismatch(Object actual, Description mismatchDescription) {}

			@Override
			public void _dont_implement_Matcher___instead_extend_BaseMatcher_() {}
		};
	}
	
	/**
	 * @see PatientService#mergePatients(Patient,Patient)
	 */
	@Test
	public void mergePatients_shouldVoidNonPreferredPersonObject() throws Exception {
		Patient notPreferred = patientService.getPatient(2);
		voidOrders(Collections.singleton(notPreferred));
		Context.getPatientService().mergePatients(patientService.getPatient(6), notPreferred);
		assertTrue(Context.getPersonService().getPerson(2).getVoided());
	}
	
	/**
	 * @see PatientService#savePatient(Patient)
	 */
	@Test
	public void savePatient_shouldCreateNewPatientFromExistingPersonPlusUserObject() throws Exception {
		// sanity check, make sure there isn't a 501 patient already
		Patient oldPatient = patientService.getPatient(501);
		assertNull(oldPatient);
		
		// fetch Bruno from the database
		Person existingPerson = Context.getPersonService().getPerson(501);
		Context.clearSession();
		Patient patient = new Patient(existingPerson);
		PatientIdentifier patientIdentifier = new PatientIdentifier("some identifier", new PatientIdentifierType(2),
		        new Location(1));
		patientIdentifier.setPreferred(true);
		patient.addIdentifier(patientIdentifier);
		
		patientService.savePatient(patient);
		
		assertEquals(501, patient.getPatientId().intValue());
		// make sure a new row with a patient id WAS created
		assertNotNull(patientService.getPatient(501));
		// make sure a new row with a new person id WASN'T created
		assertNull(patientService.getPatient(503));
	}
	
	/**
	 * @see PatientService#getPatients(String, String, List, boolean) 
	 */
	@Test
	public void getPatients_shouldSearchFamilyName2WithName() throws Exception {
		executeDataSet("org/openmrs/api/include/PersonServiceTest-extranames.xml");
		updateSearchIndex();
		
		List<Patient> patients = patientService.getPatients("Johnson", null, null, false);
		assertEquals(3, patients.size());
		assertTrue(TestUtil.containsId(patients, 2));
		assertTrue(TestUtil.containsId(patients, 4));
		assertTrue(TestUtil.containsId(patients, 5));
	}
	
	/**
	 * Regression test for ticket #1375: org.hibernate.NonUniqueObjectException caused by
	 * PatientIdentifierValidator Manually construct a patient with a correctly-matching patientId
	 * and patient identifier with validator. Calling PatientService.savePatient on that patient
	 * leads to a call to PatientIdentifierValidator.validateIdentifier which used to load the
	 * Patient for that identifier into the hibernate session, leading to a NonUniqueObjectException
	 * when the calling saveOrUpdate on the manually constructed Patient.
	 * 
	 * @see PatientService#savePatient(Patient)
	 */
	@Test
	public void savePatient_shouldNotThrowANonUniqueObjectExceptionWhenCalledWithAHandConstructedPatientRegression1375() {
		Patient patient = new Patient();
		patient.setGender("M");
		patient.setPatientId(2);
		patient.addName(new PersonName("This", "Isa", "Test"));
		PatientIdentifier patientIdentifier = new PatientIdentifier("101-6", new PatientIdentifierType(1), new Location(1));
		patientIdentifier.setPreferred(true);
		patient.addIdentifier(patientIdentifier);
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
	 * @see PatientService#savePatient(Patient)
	 */
	@Test
	public void savePatient_shouldNotThrowANonUniqueObjectExceptionWhenCalledWithAHandConstructedPatient() throws Exception {
		Patient patient = new Patient();
		patient.setGender("M");
		patient.setPatientId(2);
		// patient.setCreator(new User(1));
		// patient.setDateCreated date_created="2005-09-22 00:00:00.0"
		// changed_by="1" date_changed="2008-08-18 12:29:59.0"
		patient.addName(new PersonName("This", "Isa", "Test"));
		PatientIdentifier patientIdentifier = new PatientIdentifier("101-6", new PatientIdentifierType(1), new Location(1));
		patientIdentifier.setPreferred(true);
		patient.addIdentifier(patientIdentifier);
		Context.getPatientService().savePatient(patient);
	}
	
	/**
	 * @see PatientService#isIdentifierInUseByAnotherPatient(PatientIdentifier)
	 */
	@Test
	public void isIdentifierInUseByAnotherPatient_shouldIgnoreVoidedPatientIdentifiers() throws Exception {
		PatientIdentifierType pit = patientService.getPatientIdentifierType(2);
		PatientIdentifier patientIdentifier = new PatientIdentifier("ABC123", pit, null);
		assertFalse(patientService.isIdentifierInUseByAnotherPatient(patientIdentifier));
	}
	
	/**
	 * Regression test for http://dev.openmrs.org/ticket/790
	 * 
	 * @see PatientService#isIdentifierInUseByAnotherPatient(PatientIdentifier)
	 */
	@Test
	public void isIdentifierInUseByAnotherPatient_shouldIgnoreVoidedPatients() throws Exception {
		{
			// patient 999 should be voided and have a non-voided identifier of
			// XYZ
			Patient p = patientService.getPatient(999);
			assertNotNull(p);
			assertTrue(p.getVoided());
			boolean found = false;
			for (PatientIdentifier id : p.getIdentifiers()) {
				if (id.getIdentifier().equals("XYZ") && id.getIdentifierType().getId() == 2) {
					found = true;
					break;
				}
			}
			assertTrue(found);
		}
		PatientIdentifierType pit = patientService.getPatientIdentifierType(2);
		PatientIdentifier patientIdentifier = new PatientIdentifier("XYZ", pit, null);
		assertFalse(patientService.isIdentifierInUseByAnotherPatient(patientIdentifier));
	}
	
	/**
	 * @see PatientService#isIdentifierInUseByAnotherPatient(PatientIdentifier)
	 */
	@Test
	public void isIdentifierInUseByAnotherPatient_shouldReturnFalseWhenPatientIdentifierContainsAPatientAndNoOtherPatientHasThisId()
	    throws Exception {
		PatientIdentifierType pit = patientService.getPatientIdentifierType(1);
		PatientIdentifier patientIdentifier = new PatientIdentifier("Nobody could possibly have this identifier", pit, null);
		patientIdentifier.setPatient(patientService.getPatient(2));
		assertFalse(patientService.isIdentifierInUseByAnotherPatient(patientIdentifier));
	}
	
	/**
	 * @see PatientService#isIdentifierInUseByAnotherPatient(PatientIdentifier)
	 */
	@Test
	public void isIdentifierInUseByAnotherPatient_shouldReturnFalseWhenPatientIdentifierDoesNotContainAPatientAndNoPatientHasThisId()
	    throws Exception {
		PatientIdentifierType pit = patientService.getPatientIdentifierType(1);
		PatientIdentifier patientIdentifier = new PatientIdentifier("Nobody could possibly have this identifier", pit, null);
		assertFalse(patientService.isIdentifierInUseByAnotherPatient(patientIdentifier));
	}
	
	/**
	 * @see PatientService#isIdentifierInUseByAnotherPatient(PatientIdentifier)
	 */
	@Test
	public void isIdentifierInUseByAnotherPatient_shouldReturnTrueWhenPatientIdentifierContainsAPatientAndAnotherPatientHasThisId()
	    throws Exception {
		PatientIdentifierType pit = patientService.getPatientIdentifierType(1);
		PatientIdentifier patientIdentifier = new PatientIdentifier("7TU-8", pit, null);
		patientIdentifier.setPatient(patientService.getPatient(2));
		assertTrue(patientService.isIdentifierInUseByAnotherPatient(patientIdentifier));
	}
	
	/**
	 * @see PatientService#isIdentifierInUseByAnotherPatient(PatientIdentifier)
	 */
	@Test
	public void isIdentifierInUseByAnotherPatient_shouldReturnTrueWhenPatientIdentifierDoesNotContainAPatientAndAPatientHasThisId()
	    throws Exception {
		PatientIdentifierType pit = patientService.getPatientIdentifierType(1);
		PatientIdentifier patientIdentifier = new PatientIdentifier("7TU-8", pit, null);
		assertTrue(patientService.isIdentifierInUseByAnotherPatient(patientIdentifier));
	}

	/**
	 * @see PatientService#checkPatientIdentifiers(Patient)
	 */
	@Test
	public void checkPatientIdentifiers_shouldIgnoreVoidedPatientIdentifier() throws Exception {

		Patient patient = new Patient();
		PatientIdentifier patientIdentifier = new PatientIdentifier();
		patientIdentifier.setIdentifierType(Context.getPatientService().getAllPatientIdentifierTypes(false).get(0));
		patientIdentifier.setLocation(new Location(1));
		patientIdentifier.setVoided(true);
		patientIdentifier.setVoidedBy(Context.getAuthenticatedUser());
		patientIdentifier.setVoidReason("Testing whether voided identifiers are ignored");
		patient.addIdentifier(patientIdentifier);

		// add a non-voided identifier so that the initial
		// "at least one nonvoided identifier" check passes
		patientIdentifier = new PatientIdentifier();
		patientIdentifier.setIdentifier("a non empty string");
		patientIdentifier.setIdentifierType(Context.getPatientService().getAllPatientIdentifierTypes(false).get(0));
		patientIdentifier.setLocation(new Location(1));
		patientIdentifier.setVoided(false);
		patientIdentifier.setVoidedBy(Context.getAuthenticatedUser());
		patientIdentifier.setVoidReason("Testing whether voided identifiers are ignored");
		patient.addIdentifier(patientIdentifier);

		// If the identifier is ignored, it won't throw a
		// BlankIdentifierException as it should
		Context.getPatientService().checkPatientIdentifiers(patient);

	}
	
	/**
	 * @see PatientService#checkPatientIdentifiers(Patient)
	 */
	@Test
	public void checkPatientIdentifiers_shouldRemoveIdentifierAndThrowErrorWhenPatientHasBlankPatientIdentifier()
	    throws Exception {
		
		Patient patient = new Patient();
		PatientIdentifier patientIdentifier = new PatientIdentifier();
		patientIdentifier.setIdentifierType(Context.getPatientService().getAllPatientIdentifierTypes(false).get(0));
		patient.addIdentifier(patientIdentifier);
		
		// Should throw blank identifier exception
		assertThrows(BlankIdentifierException.class, () -> Context.getPatientService().checkPatientIdentifiers(patient));
		
	}

	/**
	 * @see PatientService#checkPatientIdentifiers(Patient)
	 */
	@Test
	public void checkPatientIdentifiers_shouldThrowErrorGivenPatientIdentifierIsInvalid()
			throws Exception {

		// given
		Patient patient = new Patient();
		PatientIdentifier nonBlankIdentifierWithoutLocation = new PatientIdentifier();
		nonBlankIdentifierWithoutLocation.setVoided(false);
		nonBlankIdentifierWithoutLocation.setLocation(null);
		nonBlankIdentifierWithoutLocation.setIdentifier("an identifier");
		nonBlankIdentifierWithoutLocation.setIdentifierType(new PatientIdentifierType(21345));

		nonBlankIdentifierWithoutLocation.setIdentifierType(Context.getPatientService().getAllPatientIdentifierTypes(false).get(0));
		patient.addIdentifier(nonBlankIdentifierWithoutLocation);

		assertEquals(1, patient.getIdentifiers().size());

		try {
		// when
			Context.getPatientService().checkPatientIdentifiers(patient);

		// then
			fail("should throw PatientIdentifierException");
		} catch (BlankIdentifierException e) {
			fail("should not throw BlankIdentifierException");
		} catch (PatientIdentifierException e) {
			assertEquals(1, patient.getIdentifiers().size());
		}

	}

	/**
	 * @see PatientService#checkPatientIdentifiers(Patient)
	 */
	@Test
	public void checkPatientIdentifiers_shouldRemovePatientIdentifierGivenItIsBlank()
			throws Exception {

		// given
		Patient patient = new Patient();
		PatientIdentifier blankPatientIdentifier = new PatientIdentifier();
		blankPatientIdentifier.setIdentifier("");
		blankPatientIdentifier.setIdentifierType(new PatientIdentifierType(21345));

		blankPatientIdentifier.setIdentifierType(Context.getPatientService().getAllPatientIdentifierTypes(false).get(0));
		patient.addIdentifier(blankPatientIdentifier);

		assertEquals(1, patient.getIdentifiers().size());
		try {
		// when
			Context.getPatientService().checkPatientIdentifiers(patient);

		// then
			fail("should throw BlankIdentifierException");
		} catch (BlankIdentifierException e) {
			assertEquals(0, patient.getIdentifiers().size());
		}

	}
	
	/**
	 * @see PatientService#checkPatientIdentifiers(Patient)
	 */
	@Test
	public void checkPatientIdentifiers_shouldThrowErrorWhenPatientHasNullPatientIdentifiers() throws Exception {
		Patient patient = new Patient();
		patient.setIdentifiers(null);
		assertThrows(InsufficientIdentifiersException.class, () -> Context.getPatientService().checkPatientIdentifiers(patient));
	}
	
	/**
	 * Cannot distinguish between null and empty patient identifiers because you cannot set the
	 * patient identifiers directly. There's only a method to add and remove patient identifiers.
	 * 
	 * @see PatientService#checkPatientIdentifiers(Patient)
	 */
	@Test
	public void checkPatientIdentifiers_shouldThrowErrorWhenPatientHasEmptyPatientIdentifiers() throws Exception {
		Patient patient = new Patient();
		patient.setIdentifiers(new HashSet<>());
		assertThrows(InsufficientIdentifiersException.class, () -> Context.getPatientService().checkPatientIdentifiers(patient));
	}
	
	/**
	 * @see PatientService#checkPatientIdentifiers(Patient)
	 */
	@Test
	public void checkPatientIdentifiers_shouldThrowErrorWhenPatientHasIdenticalIdentifiers() throws Exception {
		
		PatientIdentifierType patientIdentifierType = Context.getPatientService().getAllPatientIdentifierTypes(false).get(0);
		
		Patient patient = new Patient();
		// Identifier #1
		
		PatientIdentifier patientIdentifier1 = new PatientIdentifier();
		patientIdentifier1.setIdentifier("123456789");
		patientIdentifier1.setLocation( new Location(2) );
		patientIdentifier1.setIdentifierType(patientIdentifierType);
		patient.addIdentifier(patientIdentifier1);
		
		// Identifier #2
		PatientIdentifier patientIdentifier2 = new PatientIdentifier();
		patientIdentifier2.setIdentifier("123456789");
		patientIdentifier2.setIdentifierType(patientIdentifierType);
		patientIdentifier2.setLocation( new Location(2) );
		patient.addIdentifier(patientIdentifier2);
		assertThrows(DuplicateIdentifierException.class, () -> patientService.checkPatientIdentifiers(patient));
		
	}

	/**
	 * @see PatientService#getAllIdentifierValidators()
	 */
	@Test
	public void getAllIdentifierValidators_shouldReturnAllRegisteredPatientIdentifierValidators() throws Exception {
		
		Collection<IdentifierValidator> expectedValidators = new HashSet<>();
		expectedValidators.add(patientService.getIdentifierValidator("org.openmrs.patient.impl.LuhnIdentifierValidator"));
		expectedValidators
		        .add(patientService.getIdentifierValidator("org.openmrs.patient.impl.VerhoeffIdentifierValidator"));
		
		Collection<IdentifierValidator> actualValidators = patientService.getAllIdentifierValidators();
		assertNotNull(actualValidators);
		assertEquals(2, actualValidators.size());
		assertCollectionContentsEquals(expectedValidators, actualValidators);
		
	}
	
	/**
	 * @see PatientService#getAllPatientIdentifierTypes()
	 */
	@Test
	public void getAllPatientIdentifierTypes_shouldFetchAllNonRetiredPatientIdentifierTypes() throws Exception {
		Collection<PatientIdentifierType> types = Context.getPatientService().getAllPatientIdentifierTypes();
		assertNotNull(types, "Should not return null");
		
		for (PatientIdentifierType type : types) {
			if (type.getRetired()) {
				fail("Should not return retired patient identifier types");
			}
		}
		assertEquals(3, types.size(), "Should be exactly three patient identifier types in the dataset");
		
	}
	
	/**
	 * @see PatientService#getAllPatientIdentifierTypes(boolean) 
	 */
	@Test
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
		assertTrue(atLeastOneRetired, "There should be at least one retired patient identifier type");
		assertEquals(4, types.size(), "Should be exactly four patient identifier types");
	}
	
	/**
	 * @see PatientService#getAllPatientIdentifierTypes(boolean)
	 */
	@Test
	public void getAllPatientIdentifierTypes_shouldFetchPatientIdentifierTypesExcludingRetiredWhenIncludeRetiredIsFalse()
	    throws Exception {
		
		Collection<PatientIdentifierType> types = Context.getPatientService().getAllPatientIdentifierTypes(false);
		
		for (PatientIdentifierType type : types) {
			if (type.getRetired()) {
				fail("Should not return retired patient identifier types");
			}
		}
		assertEquals(3, types.size(), "Should be exactly three patient identifier types in the dataset");
		
	}
	
	/**
	 * @see PatientService#getIdentifierValidator(String)
	 */
	@Test
	public void getIdentifierValidator_shouldReturnPatientIdentifierValidatorGivenClassName() throws Exception {
		IdentifierValidator identifierValidator = Context.getPatientService().getIdentifierValidator(
		    "org.openmrs.patient.impl.LuhnIdentifierValidator");
		assertNotNull(identifierValidator);
		assertEquals("Luhn CheckDigit Validator", identifierValidator.getName());
		
		identifierValidator = Context.getPatientService().getIdentifierValidator(
		    "org.openmrs.patient.impl.VerhoeffIdentifierValidator");
		assertNotNull(identifierValidator);
		assertEquals("Verhoeff Check Digit Validator.", identifierValidator.getName());
	}
	
	/**
	 * @see PatientService#getPatient(Integer)
	 */
	@Test
	public void getPatient_shouldFetchPatientWithGivenPatientId() throws Exception {
		Patient patient = Context.getPatientService().getPatient(2);
		assertNotNull(patient);
		assertTrue(patient.getClass().isAssignableFrom(Patient.class));
	}
	
	/**
	 * @see PatientService#getPatient(Integer)
	 */
	@Test
	public void getPatient_shouldReturnNullWhenPatientWithGivenPatientIdDoesNotExist() throws Exception {
		Patient patient = Context.getPatientService().getPatient(10000);
		assertNull(patient);
	}
	
	/**
	 * @see PatientService#getPatientByExample(Patient)
	 */
	@Test
	public void getPatientByExample_shouldFetchPatientMatchingPatientIdOfGivenPatient() throws Exception {
		Patient examplePatient = Context.getPatientService().getPatient(6);
		examplePatient.setId(2);
		
		Patient patient = Context.getPatientService().getPatientByExample(examplePatient);
		assertNotNull(patient);
		assertTrue(patient.getClass().isAssignableFrom(Patient.class));
		assertEquals(Integer.valueOf(2), patient.getPatientId());
	}
	
	/**
	 * @see PatientService#getPatientByExample(Patient)
	 */
	@Test
	public void getPatientByExample_shouldNotFetchPatientMatchingAnyOtherPatientInformation() throws Exception {
		Patient examplePatient = Context.getPatientService().getPatient(6);
		// TODO Test this - it shouldn't matter what the identifier is
		examplePatient.setId(null);
		
		Patient patient = Context.getPatientService().getPatientByExample(examplePatient);
		assertNull(patient);
	}
	
	/**
	 * @see PatientService#getPatientByExample(Patient)
	 */
	@Test
	public void getPatientByExample_shouldReturnNullWhenNoPatientMatchesGivenPatientToMatch() throws Exception {
		Patient examplePatient = Context.getPatientService().getPatient(6);
		examplePatient.setId(3);
		
		clearHibernateCache();
		
		Patient patient = Context.getPatientService().getPatientByExample(examplePatient);
		assertNull(patient);
	}
	
	/**
	 * @see PatientService#getPatientIdentifierType(Integer)
	 */
	@Test
	public void getPatientIdentifierType_shouldFetchPatientIdentifierWithGivenPatientIdentifierTypeId() throws Exception {
		PatientIdentifierType identifierType = Context.getPatientService().getPatientIdentifierType(1);
		assertNotNull(identifierType);
		assertTrue(identifierType.getClass().isAssignableFrom(PatientIdentifierType.class));
	}
	
	/**
	 * @see PatientService#getPatientIdentifierType(Integer)
	 */
	@Test
	public void getPatientIdentifierType_shouldReturnNullWhenPatientIdentifierIdentifierDoesNotExist() throws Exception {
		PatientIdentifierType identifierType = Context.getPatientService().getPatientIdentifierType(10000);
		assertNull(identifierType);
	}
	
	/**
	 * @see PatientService#getPatientIdentifierTypeByName(String)
	 */
	@Test
	public void getPatientIdentifierTypeByName_shouldFetchPatientIdentifierTypeThatExactlyMatchesGivenName()
	    throws Exception {
		
		String identifierTypeName = "OpenMRS Identification Number";
		PatientIdentifierType identifierType = Context.getPatientService()
		        .getPatientIdentifierTypeByName(identifierTypeName);
		assertNotNull(identifierType);
		assertEquals(identifierType.getName(), identifierTypeName);
		assertTrue(identifierType.getClass().isAssignableFrom(PatientIdentifierType.class));
	}
	
	/**
	 * @see PatientService#getPatientIdentifierTypeByName(String)
	 */
	@Test
	public void getPatientIdentifierTypeByName_shouldNotReturnPatientIdentifierTypeThatPartiallyMatchesGivenName()
	    throws Exception {
		PatientIdentifierType identifierType = Context.getPatientService().getPatientIdentifierTypeByName("OpenMRS");
		assertNull(identifierType);
	}
	
	/**
	 * @see PatientService#getPatientIdentifierTypeByName(String)
	 */
	@Test
	public void getPatientIdentifierTypeByName_shouldReturnNullWhenPatientIdentifierTypeWithGivenNameDoesNotExist()
	    throws Exception {
		PatientIdentifierType identifierType = Context.getPatientService().getPatientIdentifierTypeByName(
		    "Invalid Identifier Example");
		assertNull(identifierType);
	}
	
	/**
	 * @see PatientService#getPatientIdentifierTypeByUuid(String)
	 */
	@Test
	public void getPatientIdentifierTypeByUuid_shouldFetchPatientIdentifierTypeWithGivenUuid() throws Exception {
		PatientIdentifierType identifierType = Context.getPatientService().getPatientIdentifierTypeByUuid(
		    "1a339fe9-38bc-4ab3-b180-320988c0b968");
		assertNotNull(identifierType);
		assertTrue(identifierType.getClass().isAssignableFrom(PatientIdentifierType.class));
	}
	
	/**
	 * @see PatientService#getPatientIdentifierTypeByUuid(String)
	 */
	@Test
	public void getPatientIdentifierTypeByUuid_shouldReturnNullWhenPatientIdentifierTypeWithGivenUuidDoesNotExist()
	    throws Exception {
		PatientIdentifierType identifierType = Context.getPatientService().getPatientIdentifierTypeByUuid(
		    "thisuuiddoesnotexist");
		assertNull(identifierType);
	}
	
	/**
	 * @see PatientService#getPatientIdentifierTypes(String,String,Boolean,Boolean)
	 */
	@Test
	public void getPatientIdentifierTypes_shouldFetchPatientIdentifierTypesThatMatchGivenNameWithGivenFormat()
	    throws Exception {
		executeDataSet("org/openmrs/api/include/PatientServiceTest-createPatientIdentifierType.xml");
		List<PatientIdentifierType> patientIdentifierTypes = Context.getPatientService().getPatientIdentifierTypes(
		    "Test OpenMRS Identification Number", "java.lang.Integer", null, null);
		
		assertFalse(patientIdentifierTypes.isEmpty());
		
		for (PatientIdentifierType patientIdentifierType : patientIdentifierTypes) {
			assertEquals("Test OpenMRS Identification Number", patientIdentifierType.getName());
			assertEquals("java.lang.Integer", patientIdentifierType.getFormat());
		}
	}
	
	/**
	 * @see PatientService#getPatientIdentifierTypes(String,String,Boolean,Boolean)
	 */
	@Test
	public void getPatientIdentifierTypes_shouldFetchRequiredPatientIdentifierTypesWhenGivenRequiredIsTrue()
	    throws Exception {
		executeDataSet("org/openmrs/api/include/PatientServiceTest-createPatientIdentifierType.xml");
		List<PatientIdentifierType> patientIdentifierTypes = Context.getPatientService().getPatientIdentifierTypes(null,
		    null, true, null);
		
		assertTrue(!patientIdentifierTypes.isEmpty());
		assertEquals(1, patientIdentifierTypes.size());
		for (PatientIdentifierType patientIdentifierType : patientIdentifierTypes) {
			assertTrue(patientIdentifierType.getRequired());
		}
	}
	
	/**
	 * @see PatientService#getPatientIdentifierTypes(String,String,Boolean,Boolean)
	 */
	@Test
	public void getPatientIdentifierTypes_shouldFetchNonRequiredPatientIdentifierTypesWhenGivenRequiredIsFalse()
	    throws Exception {
		executeDataSet("org/openmrs/api/include/PatientServiceTest-createPatientIdentifierType.xml");
		List<PatientIdentifierType> patientIdentifierTypes = Context.getPatientService().getPatientIdentifierTypes(null,
		    null, false, null);
		
		assertTrue(!patientIdentifierTypes.isEmpty());
		
		for (PatientIdentifierType patientIdentifierType : patientIdentifierTypes) {
			assertFalse(patientIdentifierType.getRequired());
		}
	}
	
	/**
	 * @see PatientService#getPatientIdentifierTypes(String,String,Boolean,Boolean)
	 */
	@Test
	public void getPatientIdentifierTypes_shouldFetchAnyPatientIdentifierTypesWhenGivenRequiredIsNull() throws Exception {
		executeDataSet("org/openmrs/api/include/PatientServiceTest-createPatientIdentifierType.xml");
		List<PatientIdentifierType> patientIdentifierTypes = Context.getPatientService().getPatientIdentifierTypes(null,
		    null, null, null);
		
		assertTrue(!patientIdentifierTypes.isEmpty());
		
		assertEquals(5, patientIdentifierTypes.size());
	}
	
	/**
	 * @see PatientService#getPatientIdentifierTypes(String,String,Boolean,Boolean)
	 */
	@Test
	public void getPatientIdentifierTypes_shouldFetchAnyPatientIdentifierTypesWhenGivenHasCheckDigitIsNull()
	    throws Exception {
		executeDataSet("org/openmrs/api/include/PatientServiceTest-createPatientIdentifierType.xml");
		List<PatientIdentifierType> patientIdentifierTypes = Context.getPatientService().getPatientIdentifierTypes(null,
		    null, null, null);
		
		assertTrue(!patientIdentifierTypes.isEmpty());
		
		assertEquals(5, patientIdentifierTypes.size());
	}
	
	/**
	 * @see PatientService#savePatientIdentifierType(PatientIdentifierType)
	 */
	@Test
	public void savePatientIdentifierType_shouldCreateNewPatientIdentifierType() throws Exception {
		PatientIdentifierType identifierType = new PatientIdentifierType();
		
		identifierType.setName("test");
		identifierType.setDescription("test description");
		identifierType.setRequired(false);
		
		assertNull(identifierType.getPatientIdentifierTypeId());
		
		patientService.savePatientIdentifierType(identifierType);
		
		PatientIdentifierType savedIdentifierType = patientService.getPatientIdentifierType(identifierType
		        .getPatientIdentifierTypeId());
		assertNotNull(savedIdentifierType);
		
	}
	
	/**
	 * @see PatientService#savePatientIdentifierType(PatientIdentifierType)
	 */
	@Test
	public void savePatientIdentifierType_shouldUpdateExistingPatientIdentifierType() throws Exception {
		
		PatientIdentifierType identifierType = Context.getPatientService().getAllPatientIdentifierTypes().get(0);
		
		assertNotNull(identifierType);
		assertNotNull(identifierType.getPatientIdentifierTypeId());
		assertEquals(2, identifierType.getPatientIdentifierTypeId().intValue());
		assertNotSame("test", identifierType.getName());
		
		// Change existing patient identifier
		identifierType.setName("test");
		identifierType.setDescription("test description");
		identifierType.setRequired(false);
		
		patientService.savePatientIdentifierType(identifierType);
		
		PatientIdentifierType savedIdentifierType = patientService.getPatientIdentifierType(2);
		
		assertNotNull(savedIdentifierType);
		assertEquals("test", identifierType.getName());
		assertTrue(savedIdentifierType.equals(identifierType));
		
	}
	
	/**
	 * @see PatientService#unretirePatientIdentifierType(PatientIdentifierType)
	 */
	@Test
	public void unretirePatientIdentifierType_shouldUnretirePatientIdentifierType() throws Exception {
		PatientIdentifierType identifierType = Context.getPatientService().getPatientIdentifierType(4);
		assertTrue(identifierType.getRetired());
		assertNotNull(identifierType.getRetiredBy());
		assertNotNull(identifierType.getRetireReason());
		assertNotNull(identifierType.getDateRetired());
		
		PatientIdentifierType unretiredIdentifierType = Context.getPatientService().unretirePatientIdentifierType(
		    identifierType);
		assertFalse(unretiredIdentifierType.getRetired());
		assertNull(unretiredIdentifierType.getRetiredBy());
		assertNull(unretiredIdentifierType.getRetireReason());
		assertNull(unretiredIdentifierType.getDateRetired());
	}
	
	/**
	 * @see PatientService#unretirePatientIdentifierType(PatientIdentifierType)
	 */
	@Test
	public void unretirePatientIdentifierType_shouldReturnUnretiredPatientIdentifierType() throws Exception {
		PatientIdentifierType identifierType = Context.getPatientService().getPatientIdentifierType(4);
		assertTrue(identifierType.getRetired());
		assertNotNull(identifierType.getRetiredBy());
		assertNotNull(identifierType.getRetireReason());
		assertNotNull(identifierType.getDateRetired());
		
		PatientIdentifierType unretiredIdentifierType = Context.getPatientService().unretirePatientIdentifierType(
		    identifierType);
		assertFalse(unretiredIdentifierType.getRetired());
		assertNull(unretiredIdentifierType.getRetiredBy());
		assertNull(unretiredIdentifierType.getRetireReason());
		assertNull(unretiredIdentifierType.getDateRetired());
		
	}
	
	/**
	 * @see PatientService#unvoidPatient(Patient)
	 */
	@Test
	public void unvoidPatient_shouldUnvoidGivenPatient() throws Exception {
		Patient patient = Context.getPatientService().getPatient(2);
		
		Patient voidedPatient = Context.getPatientService().voidPatient(patient, "Void for testing");
		assertTrue(voidedPatient.getVoided());
		assertNotNull(voidedPatient.getVoidedBy());
		assertNotNull(voidedPatient.getVoidReason());
		assertNotNull(voidedPatient.getDateVoided());
		
		Patient unvoidedPatient = Context.getPatientService().unvoidPatient(voidedPatient);
		assertFalse(unvoidedPatient.getVoided());
		assertNull(unvoidedPatient.getVoidedBy());
		assertNull(unvoidedPatient.getVoidReason());
		assertNull(unvoidedPatient.getDateVoided());
	}
	
	/**
	 * @see PatientService#unvoidPatient(Patient)
	 */
	@Test
	public void unvoidPatient_shouldReturnUnvoidedPatient() throws Exception {
		Patient patient = Context.getPatientService().getPatient(2);
		
		Patient voidedPatient = Context.getPatientService().voidPatient(patient, "Void for testing");
		assertTrue(voidedPatient.getVoided());
		assertNotNull(voidedPatient.getVoidedBy());
		assertNotNull(voidedPatient.getVoidReason());
		assertNotNull(voidedPatient.getDateVoided());
		
		Patient unvoidedPatient = Context.getPatientService().unvoidPatient(voidedPatient);
		assertFalse(unvoidedPatient.getVoided());
		assertNull(unvoidedPatient.getVoidedBy());
		assertNull(unvoidedPatient.getVoidReason());
		assertNull(unvoidedPatient.getDateVoided());
	}
	
	/**
	 * @see PatientService#voidPatient(Patient,String)
	 */
	@Test
	public void voidPatient_shouldVoidGivenPatientWithGivenReason() throws Exception {
		Patient patient = Context.getPatientService().getPatient(2);
		Patient voidedPatient = Context.getPatientService().voidPatient(patient, "Void for testing");
		
		assertTrue(voidedPatient.getVoided());
		assertEquals("Void for testing", voidedPatient.getVoidReason());
		assertFalse(Context.getPatientService().getAllPatients(false).contains(patient));
	}
	
	/**
	 * @see PatientService#voidPatient(Patient,String)
	 */
	@Test
	public void voidPatient_shouldVoidAllPatientIdentifiersAssociatedWithGivenPatient() throws Exception {
		Patient patient = Context.getPatientService().getPatient(2);
		Patient voidedPatient = Context.getPatientService().voidPatient(patient, "Void for testing");
		for (PatientIdentifier patientIdentifier : voidedPatient.getIdentifiers()) {
			assertTrue(patientIdentifier.getVoided());
			assertNotNull(patientIdentifier.getVoidedBy());
			assertNotNull(patientIdentifier.getVoidReason());
			assertNotNull(patientIdentifier.getDateVoided());
		}
		
	}
	
	/**
	 * @see PatientService#voidPatient(Patient,String)
	 */
	@Test
	public void voidPatient_shouldReturnVoidedPatientWithGivenReason() throws Exception {
		Patient patient = Context.getPatientService().getPatient(2);
		Patient voidedPatient = Context.getPatientService().voidPatient(patient, "Void for testing");
		
		assertTrue(voidedPatient.getVoided());
		assertNotNull(voidedPatient.getVoidedBy());
		assertNotNull(voidedPatient.getVoidReason());
		assertNotNull(voidedPatient.getDateVoided());
		assertEquals("Void for testing", voidedPatient.getVoidReason());
	}
	
	/**
	 * @see PatientService#voidPatient(Patient,String)
	 */
	@Test
	@Disabled
	// TODO fix: NullPointerException in RequiredDataAdvice
	public void voidPatient_shouldReturnNullWhenPatientIsNull() throws Exception {
		PatientService patientService = Context.getPatientService();
		Patient voidedPatient = patientService.voidPatient(null, "No null patient should be voided");
		assertNull(voidedPatient);
	}
	
	/**
	 * @see PatientService#getPatientByUuid(String)
	 */
	@Test
	public void getPatientByUuid_shouldFindObjectGivenValidUuid() throws Exception {
		String uuid = "da7f524f-27ce-4bb2-86d6-6d1d05312bd5";
		Patient patient = Context.getPatientService().getPatientByUuid(uuid);
		assertEquals(2, (int) patient.getPatientId());
	}
	
	/**
	 * @see PatientService#getPatientByUuid(String)
	 */
	@Test
	public void getPatientByUuid_shouldReturnNullIfNoObjectFoundWithGivenUuid() throws Exception {
		assertNull(Context.getPatientService().getPatientByUuid("some invalid uuid"));
	}
	
	/**
	 * @see PatientService#getPatientIdentifierByUuid(String)
	 */
	@Test
	public void getPatientIdentifierByUuid_shouldFindObjectGivenValidUuid() throws Exception {
		String uuid = "ff41928c-3bca-48d9-a4dc-9198f6b2873b";
		PatientIdentifier patientIdentifier = Context.getPatientService().getPatientIdentifierByUuid(uuid);
		assertEquals(1, (int) patientIdentifier.getPatientIdentifierId());
	}
	
	/**
	 * @see PatientService#getPatientIdentifierByUuid(String)
	 */
	@Test
	public void getPatientIdentifierByUuid_shouldReturnNullIfNoObjectFoundWithGivenUuid() throws Exception {
		assertNull(Context.getPatientService().getPatientIdentifierByUuid("some invalid uuid"));
	}
	
	/**
	 * @see PatientService#getPatientIdentifierTypeByUuid(String)
	 */
	@Test
	public void getPatientIdentifierTypeByUuid_shouldFindObjectGivenValidUuid() throws Exception {
		String uuid = "1a339fe9-38bc-4ab3-b180-320988c0b968";
		PatientIdentifierType patientIdentifierType = Context.getPatientService().getPatientIdentifierTypeByUuid(uuid);
		assertEquals(1, (int) patientIdentifierType.getPatientIdentifierTypeId());
	}
	
	/**
	 * @see PatientService#getPatientIdentifierTypeByUuid(String)
	 */
	@Test
	public void getPatientIdentifierTypeByUuid_shouldReturnNullIfNoObjectFoundWithGivenUuid() throws Exception {
		assertNull(Context.getPatientService().getPatientIdentifierTypeByUuid("some invalid uuid"));
	}
	
	/**
	 * @see PatientService#mergePatients(Patient,Patient)
	 */
	@Test
	public void mergePatients_shouldCopyNonvoidedAddressesToPreferredPatient() throws Exception {
		Patient preferred = patientService.getPatient(7);
		Patient notPreferred = patientService.getPatient(8);
		
		patientService.mergePatients(preferred, notPreferred);
		
		// make sure one of their addresses has the city of "Jabali"
		boolean found = false;
		for (PersonAddress pa : preferred.getAddresses()) {
			if (pa.getCityVillage().equals("Jabali")) {
				found = true;
			}
		}
		
		assertTrue(found, "odd, user 7 didn't get user 8's address");
	}
	
	/**
	 * @see PatientService#mergePatients(Patient,Patient)
	 */
	@Test
	public void mergePatients_shouldOnlyMarkAddressesOfPreferredPatientAsPreferred() throws Exception {
		
		Patient preferred = patientService.getPatient(7);
		Patient notPreferred = patientService.getPatient(8);
		
		// since the test data has no preferred addresses, we need to mark addresses to preferred to set up the test
		preferred.getPersonAddress().setPreferred(true);
		notPreferred.getPersonAddress().setPreferred(true);
		
		patientService.savePatient(preferred);
		patientService.savePatient(notPreferred);
		
		patientService.mergePatients(preferred, notPreferred);
		
		assertThat(preferred.getAddresses().size(), is(2));
		
		// make sure only the address from the preferred patient is marked as preferred
		for (PersonAddress pa : preferred.getAddresses()) {
			if (pa.getCityVillage().equals("Jabali")) {
				assertFalse(pa.getPreferred());
			}
		}
		
	}
	
	/**
	 * @see PatientService#mergePatients(Patient, Patient)
	 */
	@Test
	public void mergePatients_shouldCopyNonvoidedIdentifiersToPreferredPatient() throws Exception {
		Patient preferred = patientService.getPatient(7);
		Patient notPreferred = patientService.getPatient(8);
		
		PatientIdentifier nonvoidedPI = null;
		PatientIdentifier voidedPI = null;
		
		for (PatientIdentifier patientIdentifier : notPreferred.getIdentifiers()) {
			if (patientIdentifier.getIdentifier().equals("7TU-8")) {
				nonvoidedPI = patientIdentifier;
			}
			if (patientIdentifier.getIdentifier().equals("ABC123")) {
				voidedPI = patientIdentifier;
			}
		}
		
		patientService.mergePatients(preferred, notPreferred);
		
		assertNotNull(nonvoidedPI);
		assertTrue(contains(new ArrayList<>(preferred.getIdentifiers()), nonvoidedPI.getIdentifier()));
		assertNotNull(voidedPI);
		assertFalse(contains(new ArrayList<>(preferred.getIdentifiers()), voidedPI.getIdentifier()));
	}
	
	public static boolean contains(List<PatientIdentifier> list, String identifier) {
		for (PatientIdentifier patientIdentifier : list) {
			if (patientIdentifier.getIdentifier().equals(identifier)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * @see PatientService#mergePatients(Patient,Patient)
	 */
	@Test
	public void mergePatients_shouldCopyNonvoidedNamesToPreferredPatient() throws Exception {
		Patient preferred = patientService.getPatient(7);
		Patient notPreferred = patientService.getPatient(8);
		
		patientService.mergePatients(preferred, notPreferred);
		
		// make sure one of their addresses has the first name of "Anet"
		boolean found = false;
		for (PersonName pn : preferred.getNames()) {
			if (pn.getGivenName().equals("Anet")) {
				found = true;
			}
		}
		
		assertTrue(found, "odd, user 7 didn't get user 8's names");
	}
	
	/**
	 * @see PatientService#getPatientByUuid(String)
	 */
	@Test
	public void getPatientByUuid_shouldFetchPatientWithGivenUuid() throws Exception {
		String uuid = "da7f524f-27ce-4bb2-86d6-6d1d05312bd5";
		Patient patient = Context.getPatientService().getPatientByUuid(uuid);
		assertEquals(2, (int) patient.getPatientId());
	}
	
	/**
	 * @see PatientService#getPatientByUuid(String)
	 */
	@Test
	public void getPatientByUuid_shouldReturnNullIfPatientNotFoundWithGivenUuid() throws Exception {
		assertNull(Context.getPatientService().getPatientByUuid("some invalid uuid"));
	}
	
	/**
	 * @see PatientService#getPatientIdentifierByUuid(String)
	 */
	@Test
	public void getPatientIdentifierByUuid_shouldFetchPatientIdentifierWithGivenUuid() throws Exception {
		String uuid = "ff41928c-3bca-48d9-a4dc-9198f6b2873b";
		PatientIdentifier patientIdentifier = Context.getPatientService().getPatientIdentifierByUuid(uuid);
		assertEquals(1, (int) patientIdentifier.getPatientIdentifierId());
	}
	
	/**
	 * @see PatientService#getPatientIdentifierByUuid(String)
	 */
	@Test
	public void getPatientIdentifierByUuid_shouldReturnNullIfPatientIdentifierNotFoundWithGivenUuid() throws Exception {
		assertNull(Context.getPatientService().getPatientIdentifierByUuid("some invalid uuid"));
	}
	
	/**
	 * @see PatientService#mergePatients(Patient,Patient)
	 */
	@Test
	public void mergePatients_shouldNotCopyOverRelationshipsThatAreOnlyBetweenThePreferredAndNotpreferredPatient()
	    throws Exception {
		executeDataSet(PATIENT_RELATIONSHIPS_XML);
		
		Patient preferred = patientService.getPatient(999);
		Patient notPreferred = patientService.getPatient(2);
		voidOrders(Collections.singleton(notPreferred));
		
		patientService.mergePatients(preferred, notPreferred);
	}
	
	/**
	 * @see PatientService#getPatientIdentifiers(String,List,List,List,Boolean)
	 */
	@Test
	public void getPatientIdentifiers_shouldReturnOnlyNonVoidedPatientsAndPatientIdentifiers() throws Exception {
		// sanity check. make sure there is at least one voided patient
		Patient patient = patientService.getPatient(999);
		assertTrue(patient.getVoided(), "This patient should be voided");
		
		// now fetch all identifiers
		List<PatientIdentifier> patientIdentifiers = patientService.getPatientIdentifiers(null, null, null, null, null);
		for (PatientIdentifier patientIdentifier : patientIdentifiers) {
			assertFalse(patientIdentifier.getVoided(), "No voided identifiers should be returned");
			assertFalse(patientIdentifier.getPatient().getVoided(), "No identifiers of voided patients should be returned");
		}
	}
	
	/**
	 * @see PatientService#getPatients(String, String, java.util.List, boolean)
	 */
	@Test
	public void getPatients_shouldSupportSimpleRegex() throws Exception {
		Context.getAdministrationService().saveGlobalProperty(
		    new GlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_PATIENT_IDENTIFIER_REGEX, "^0*@SEARCH@([A-Z]+-[0-9])?$"));
		PatientIdentifier identifier = new PatientIdentifier("1234-4", new PatientIdentifierType(1), new Location(1));
		identifier.setCreator(new User(1));
		identifier.setDateCreated(new Date());
		Patient patient = Context.getPatientService().getPatient(2);
		patient.addIdentifier(identifier);
		Context.getPatientService().savePatient(patient);

		updateSearchIndex();

		assertEquals(1, Context.getPatientService().getPatients("1234-4").size());
	}
	
	@Test
	public void getPatients_shouldReturnEmptyListWhenNoMatchIsFound() throws Exception {
		initializeInMemoryDatabase();
		executeDataSet(CREATE_PATIENT_XML);
		authenticate();
		
		List<Patient> patientList = patientService.getPatients(null, "???", null, false);
		assertNotNull(patientList, "an empty list should be returned instead of a null object");
		assertEquals(0, patientList.size());
	}
	
	@Test
	public void savePatient_shouldUpdateAnExistingPatient() throws Exception {
		
		Patient patient = patientService.getPatient(2);
		// just some sanity checks
		assertNotNull(patient, "There should be a patient with patient_id of 2");
		assertTrue(patient.getGender().equals("M"), "The patient should be listed as male");
		
		patient.setGender("F");
		patientService.savePatient(patient);
		Patient patient2 = patientService.getPatient(patient.getPatientId());
		assertTrue(patient.equals(patient2), "The updated patient and the orig patient should still be equal");
		
		assertTrue(patient2.getGender().equals("F"), "The gender should be new");
	}
	
	@Test
	public void savePatient_shouldFailWhenPatientDoesNotHaveAnyPatientIdentifiers() throws Exception {
		Patient patient = new Patient();
		// a sanity check first
		assertThat(patient.getIdentifiers(), is(empty()));
		try {
			patientService.savePatient(patient);
			fail("should fail when patient does not have any patient identifiers");
		}
		catch (Exception e) {}
	}
	
	@Test
	public void getAllPatients_shouldFetchAllNonVoidedPatients() throws Exception {
		List<Patient> allPatients = patientService.getAllPatients();
		// there are 1 voided and 4 nonvoided patients in
		// standardTestDataset.xml
		assertEquals(4, allPatients.size());
	}
	
	@Test
	public void getAllPatients_shouldFetchNonVoidedPatientsWhenGivenIncludeVoidedIsFalse() throws Exception {
		List<Patient> allPatients = patientService.getAllPatients(false);
		// there are 1 voided and 4 nonvoided patients in
		// standardTestDataset.xml
		assertEquals(4, allPatients.size());
	}
	
	@Test
	public void getAllPatients_shouldFetchVoidedPatientsWhenGivenIncludeVoidedIsTrue() throws Exception {
		List<Patient> allPatients = patientService.getAllPatients(true);
		// there are 1 voided and 4 nonvoided patients in
		// standardTestDataset.xml
		assertEquals(6, allPatients.size());
	}
	
	@SkipBaseSetup
	@Test
	public void getPatients_shouldFetchAllPatientsThatPartiallyMatchGivenName() throws Exception {
		initializeInMemoryDatabase();
		executeDataSet(FIND_PATIENTS_XML);
		authenticate();
		updateSearchIndex();
		
		List<Patient> patients = patientService.getPatients("Jea", null, null, false);
		// patients with patientId of 4, 5, & 6 contain "Jea" at the start of a
		// first name
		assertTrue(patients.contains(patientService.getPatient(4)), "getPatients failed to find patient whose first name included partial match");
		assertTrue(patients.contains(patientService.getPatient(5)), "getPatients failed to find patient whose family name included partial match");
		assertTrue(patients.contains(patientService.getPatient(6)), "getPatients failed to find patient whose family name included partial match");
		// patients with patientId of 2 and 3 do not contain "Jea" in their name
		assertFalse(patients.contains(patientService.getPatient(2)), "getPatients failed to exclude patient whose first name did not include the partial string");
		assertFalse(patients.contains(patientService.getPatient(3)), "getPatients failed to exclude patient whose first name did not include the partial string");
		
		// Try it with a string that is part of a last name and in the middle of
		// a first name
		patients = patientService.getPatients("Claud", null, null, false);
		// patients with patientId of 4, 5, & 6 contain "Claud" in a first or
		// second name
		assertTrue(patients.contains(patientService.getPatient(5)), "getPatients failed to find patient whose family name included partial match");
		assertTrue(patients.contains(patientService.getPatient(6)), "getPatients failed to find patient whose family name included partial match");
		// patients with patientId of 2 and 3 do not contain "Claud" in their
		// name
		assertFalse(patients.contains(patientService.getPatient(2)), "getPatients failed to exclude patient whose name did not include the partial string");
		assertFalse(patients.contains(patientService.getPatient(3)), "getPatients failed to exclude patient whose name did not include the partial string");
	}
	
	@SkipBaseSetup
	@Test
	public void getPatients_shouldIgnoreAccentsWhenMatchingName() throws Exception {
		initializeInMemoryDatabase();
		executeDataSet(FIND_PATIENTS_ACCENTS_XML);
		authenticate();
		updateSearchIndex();
		
		List<Patient> patients = patientService.getPatients("Jose", null, null, false);
		assertTrue(patients.contains(patientService.getPatient(202)), "search term without accent did not find patient first name with accent");
		assertTrue(patients.contains(patientService.getPatient(205)), "search term without accent did not find patient middle name with accent");
		
		patients = patientService.getPatients("Úrsula", null, null, false);
		assertTrue(patients.contains(patientService.getPatient(203)), "search term with accent did not find matching name");
		assertTrue(patients.contains(patientService.getPatient(204)), "search term with accent did not find matching name");

		patients = patientService.getPatients("Ho Xuan Huong", null, null, false);
		assertTrue(patients.contains(patientService.getPatient(208)), "three-term search did not find Vietnamese-accented name");
		
		patients = patientService.getPatients("Hưong", null, null, false);
		assertTrue(patients.contains(patientService.getPatient(208)), "did not find a name with a partially accented search term");

		patients = patientService.getPatients("Ὀδυσσεύς", null, null, false);
		assertTrue(patients.contains(patientService.getPatient(207)), "did not find a Greek name that should have matched exactly");
		
		// This is more to document behavior than to assert that this should necessarily behave this way
		patients = patientService.getPatients("Amarantá", null, null, false);
		assertFalse(patients.contains(patientService.getPatient(204)), "unexpectedly found a patient using a search term with an extraneous accent");

		// This is more to document behavior than to assert that this should necessarily behave this way
		patients = patientService.getPatients("Οδυσσευς", null, null, false);
		assertFalse(patients.contains(patientService.getPatient(207)), "unexpectedly found an accented Greek name by searching for its un-accented equivalent");

		// This is more to document behavior than to assert that this should necessarily behave this way
		patients = patientService.getPatients("Dost", null, null, false);
		assertFalse(patients.contains(patientService.getPatient(209)), "unexpectedly found a Russian name with an ASCII search");
	}

	@SkipBaseSetup
	@Test
	public void purgePatient_shouldDeletePatientFromDatabase() throws Exception {
		initializeInMemoryDatabase();
		executeDataSet(FIND_PATIENTS_XML);
		authenticate();
		
		// verify patient with ID 2 exists in database
		Patient patientToPurge = patientService.getPatient(2);
		assertNotNull(patientToPurge);
		
		// purge the patient
		patientService.purgePatient(patientToPurge);
		// if the patient doesn't exist in the database, getPatient should
		// return null now
		assertNull(patientService.getPatient(2));
	}
	
	@SkipBaseSetup
	@Test
	public void getPatients_shouldNotReturnVoidedPatients() throws Exception {
		initializeInMemoryDatabase();
		executeDataSet(FIND_PATIENTS_XML);
		authenticate();
		
		// verify patient is voided
		assertTrue(patientService.getPatient(3).getVoided());
		// ask for list of patients with this name, expect none back because
		// patient is voided
		List<Patient> patients = patientService.getPatients("I am voided", null, null, false);
		assertEquals(patients.size(), 0);
	}
	
	/**
	 * @see PatientService#getPatients(String, String, java.util.List, boolean)
	 */
	@Test
	public void getPatients_shouldSupportPatternUsingLastDigitAsCheckDigit() throws Exception {
		Context.getAdministrationService().saveGlobalProperty(
		    new GlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_PATIENT_IDENTIFIER_SEARCH_PATTERN,
		            "@SEARCH@,0@SEARCH@,@SEARCH-1@-@CHECKDIGIT@,0@SEARCH-1@-@CHECKDIGIT@"));
		// "^(0*@SEARCH-1@-@CHECKDIGIT@)$"));
		PatientIdentifier identifier = new PatientIdentifier("1234-4", new PatientIdentifierType(1), new Location(1));
		identifier.setCreator(new User(1));
		identifier.setDateCreated(new Date());
		Patient patient = Context.getPatientService().getPatient(2);
		patient.addIdentifier(identifier);
		Context.getPatientService().savePatient(patient);

		updateSearchIndex();

		assertEquals(1, Context.getPatientService().getPatients("12344").size());
		assertEquals(1, Context.getPatientService().getPatients("1234-4").size());
	}
	
	/**
	 * @see PatientService#getPatientIdentifier(Integer patientId)
	 */
	@Test
	public void getPatientIdentifier_shouldReturnThePatientsIdentifier() throws Exception {
		
		assertEquals("101-6", patientService.getPatientIdentifier(2).getIdentifier());
		assertEquals(1, patientService.getPatientIdentifier(2).getIdentifierType().getPatientIdentifierTypeId()
		        .intValue());
	}
	
	/**
	 * @see PatientService#getPatientIdentifier(Integer patientId)
	 */
	
	@Test
	public void voidPatientIdentifier_shouldVoidGivenPatientIdentifierWithGivenReason() throws Exception {
		Patient patient = patientService.getPatientIdentifier(3).getPatient();
		int oldActiveIdentifierSize = patient.getActiveIdentifiers().size();
		PatientIdentifier patientIdentifierToVoid = patientService.getPatientIdentifier(3);
		
		PatientIdentifier voidedIdentifier = patientService.voidPatientIdentifier(patientIdentifierToVoid, "Testing");
		// was the void reason set
		assertEquals("Testing", voidedIdentifier.getVoidReason());
		// patient's active identifiers must have reduced by 1 if the identifier
		// was successfully voided
		assertEquals(oldActiveIdentifierSize - 1, patient.getActiveIdentifiers().size());
	}
	
	@Test
	public void savePatientIdentifier_shouldCreateNewPatientIndentifier() throws Exception {
		PatientIdentifier patientIdentifier = new PatientIdentifier("677-56-6666", new PatientIdentifierType(4),
		        new Location(1));
		Patient associatedPatient = patientService.getPatient(2);
		patientIdentifier.setPatient(associatedPatient);
		PatientIdentifier createdPatientIdentifier = patientService.savePatientIdentifier(patientIdentifier);
		assertNotNull(createdPatientIdentifier);
		assertNotNull(createdPatientIdentifier.getPatientIdentifierId());
	}
	
	@Test
	public void savePatientIdentifier_shouldUpdateAnExistingPatientIdentifier() throws Exception {
		PatientIdentifier patientIdentifier = patientService.getPatientIdentifier(7);
		patientIdentifier.setIdentifier("NEW-ID");
		PatientIdentifier updatedPatientIdentifier = patientService.savePatientIdentifier(patientIdentifier);
		assertNotNull(updatedPatientIdentifier);
		assertEquals("NEW-ID", updatedPatientIdentifier.getIdentifier());
	}
	
	@Test
	public void purgePatientIdentifier_shouldDeletePatientIdentifierFromDatabase() throws Exception {
		PatientIdentifier patientIdentifier = patientService.getPatientIdentifier(7);
		patientService.purgePatientIdentifier(patientIdentifier);
		assertNull(patientService.getPatientIdentifier(7));
		
	}
	
	@Test
	public void savePatientIdentifier_shouldThrowAnAPIExceptionWhenOneOfTheRequiredFieldsIsNull() throws Exception {
		PatientIdentifier patientIdentifier = patientService.getPatientIdentifier(7);
		patientIdentifier.setIdentifier(null);
		assertThrows(APIException.class, () -> patientService.savePatientIdentifier(patientIdentifier));
		
	}
	
	@Test
	public void savePatientIdentifier_shouldThrowAnAPIExceptionIfThePatientIdentifierStringIsAWhiteSpace() throws Exception {
		PatientIdentifier patientIdentifier = patientService.getPatientIdentifier(7);
		patientIdentifier.setIdentifier(" ");
		assertThrows(APIException.class, () -> patientService.savePatientIdentifier(patientIdentifier));
	}
	
	@Test
	public void savePatientIdentifier_shouldThrowAnAPIExceptionIfThePatientIdentifierStringIsAnEmptyString()
	    throws Exception {
		PatientIdentifier patientIdentifier = patientService.getPatientIdentifier(7);
		patientIdentifier.setIdentifier("");
		assertThrows(APIException.class, () -> patientService.savePatientIdentifier(patientIdentifier));
	}
	
	/**
	 * @see PatientService#savePatientIdentifier(PatientIdentifier)
	 */
	@Test
	public void savePatientIdentifier_shouldAllowLocationToBeNullWhenLocationBehaviourIsNotUsed() {
		PatientIdentifier patientIdentifier = patientService.getPatientIdentifier(7);
		patientIdentifier.setLocation(null);
		patientIdentifier.getIdentifierType().setLocationBehavior(PatientIdentifierType.LocationBehavior.NOT_USED);
		patientService.savePatientIdentifier(patientIdentifier);
	}
	
	/**
	 * @see PatientService#savePatientIdentifier(PatientIdentifier)
	 */
	@Test
	public void savePatientIdentifier_shouldAllowLocationToBeNullWhenLocationBehaviourIsRequired() {
		PatientIdentifier patientIdentifier = patientService.getPatientIdentifier(9);
		patientIdentifier.setLocation(null);
		patientIdentifier.getIdentifierType().setLocationBehavior(PatientIdentifierType.LocationBehavior.REQUIRED);
		assertThrows(ValidationException.class, () -> patientService.savePatientIdentifier(patientIdentifier));
	}
	
	@Test
	public void voidPatientIdentifier_shouldThrowAnAPIExceptionIfTheReasonIsNull() throws Exception {
		PatientIdentifier patientIdentifierToVoid = patientService.getPatientIdentifier(3);
		assertThrows(APIException.class, () -> patientService.voidPatientIdentifier(patientIdentifierToVoid, null));
	}
	
	@Test
	public void voidPatientIdentifier_shouldThrowAnAPIExceptionIfTheReasonIsAnEmptyString() throws Exception {
		PatientIdentifier patientIdentifierToVoid = patientService.getPatientIdentifier(3);
		assertThrows(APIException.class, () -> patientService.voidPatientIdentifier(patientIdentifierToVoid, ""));
	}
	
	@Test
	public void voidPatientIdentifier_shouldThrowAnAPIExceptionIfTheReasonIsAWhiteSpaceCharacter() throws Exception {
		PatientIdentifier patientIdentifierToVoid = patientService.getPatientIdentifier(3);
		assertThrows(APIException.class, () -> patientService.voidPatientIdentifier(patientIdentifierToVoid, " "));
	}
	
	@Test
	public void mergePatients_shouldMergeAllNonPreferredPatientsInTheTheNotPreferredListToPreferredPatient()
	    throws Exception {
		Patient preferred = patientService.getPatient(6);
		List<Patient> notPreferred = new ArrayList<>();
		notPreferred.add(patientService.getPatient(7));
		notPreferred.add(patientService.getPatient(8));
		voidOrders(notPreferred);
		patientService.mergePatients(preferred, notPreferred);
		assertFalse(patientService.getPatient(6).getVoided());
		assertTrue(patientService.getPatient(7).getVoided());
		assertTrue(patientService.getPatient(8).getVoided());
	}
	
	/**
	 * @see PatientService#mergePatients(Patient,Patient)
	 */
	@Test
	public void mergePatients_shouldNotCreateDuplicateRelationships() throws Exception {
		executeDataSet(PATIENT_RELATIONSHIPS_XML);
		
		Patient preferred = patientService.getPatient(999);
		Patient notPreferred = patientService.getPatient(2);
		voidOrders(Collections.singleton(notPreferred));
		
		// expected relationships before merge:
		// * 2->1 (type 2)
		// * 999->2 (type 5)
		// * 999->1 (type 2)
		// * 7->999 (type 4)
		// * 502->2 (type 1)
		// * 7->2 (type 1)
		patientService.mergePatients(preferred, notPreferred);
		
		// expected relationships after merge:
		// * 999->1 (type 2)
		// * 7->999 (type 4)
		// * 502->999 (type 1)
		// * 7->999 (type 1)
		
		// check for a relationship that should not be duplicated: 2->1 and
		// 999->1
		List<Relationship> rels = personService.getRelationships(preferred, new Person(1), new RelationshipType(2));
		assertEquals(1, rels.size(), "duplicate relationships were not removed");
	}
	
	/**
	 * @see PatientService#mergePatients(Patient,Patient)
	 */
	@Test
	public void mergePatients_shouldVoidAllRelationshipsForNonPreferredPatient() throws Exception {
		executeDataSet(PATIENT_RELATIONSHIPS_XML);
		
		Patient preferred = patientService.getPatient(999);
		Patient notPreferred = patientService.getPatient(2);
		voidOrders(Collections.singleton(notPreferred));
		
		patientService.mergePatients(preferred, notPreferred);
		
		List<Relationship> rels = personService.getRelationshipsByPerson(notPreferred);
		assertTrue(rels.isEmpty(), "there should not be any relationships for non preferred");
	}
	
	/**
	 * @see PatientService#mergePatients(Patient,Patient)
	 */
	@Test
	public void mergePatients_shouldAuditCreatedAddresses() throws Exception {
		
		//retrieve preferred patient
		Patient preferred = patientService.getPatient(999);
		
		//retrieve notPreferredPatient and save it with a new address
		Patient notPreferred = patientService.getPatient(2);
		voidOrders(Collections.singleton(notPreferred));
		PersonAddress address = new PersonAddress();
		address.setAddress1("another address123");
		address.setAddress2("another address234");
		address.setCityVillage("another city");
		address.setCountry("another country");
		notPreferred.addAddress(address);
		patientService.savePatient(notPreferred);
		
		//merge the two patients and retrieve the audit object
		PersonMergeLog audit = mergeAndRetrieveAudit(preferred, notPreferred);
		
		//find the UUID of the address that was added by the merge
		String addedAddressUuid = null;
		preferred = patientService.getPatient(999);
		for (PersonAddress a : preferred.getAddresses()) {
			if (a.getAddress1().equals(address.getAddress1())) {
				addedAddressUuid = a.getUuid();
			}
		}
		assertNotNull("expected new address was not found in the preferred patient after the merge", addedAddressUuid);
		assertTrue(isValueInList(addedAddressUuid, audit.getPersonMergeLogData().getCreatedAddresses()), "person address creation not audited");
	}
	
	/**
	 * @see PatientService#mergePatients(Patient,Patient)
	 */
	@Test
	public void mergePatients_shouldAuditCreatedAttributes() throws Exception {
		//retrieve preferred patient
		Patient preferred = patientService.getPatient(999);
		
		//retrieve notPreferredPatient and save it with a new attribute
		Patient notPreferred = patientService.getPatient(2);
		voidOrders(Collections.singleton(notPreferred));
		PersonAttribute attribute = new PersonAttribute(2);
		attribute.setValue("5089");
		attribute.setAttributeType(personService.getPersonAttributeType(1));
		notPreferred.addAttribute(attribute);
		patientService.savePatient(notPreferred);
		
		//merge the two patients and retrieve the audit object
		PersonMergeLog audit = mergeAndRetrieveAudit(preferred, notPreferred);
		
		//find the UUID of the attribute that was added by the merge
		String addedAttributeUuid = null;
		preferred = patientService.getPatient(999);
		for (PersonAttribute a : preferred.getAttributes()) {
			if (a.getValue().equals(attribute.getValue())) {
				addedAttributeUuid = a.getUuid();
			}
		}
		assertNotNull("expected new attribute was not found in the preferred patient after the merge",
			addedAttributeUuid);
		assertTrue(isValueInList(addedAttributeUuid, audit.getPersonMergeLogData().getCreatedAttributes()), "person attribute creation not audited");
	}
	
	/**
	 * @see PatientService#mergePatients(Patient,Patient)
	 */
	@Test
	public void mergePatients_shouldAuditCreatedIdentifiers() throws Exception {
		//retrieve preferred patient
		Patient preferred = patientService.getPatient(999);
		
		//retrieve notPreferredPatient and save it with a new identifier
		Patient notPreferred = patientService.getPatient(2);
		voidOrders(Collections.singleton(notPreferred));
		PatientIdentifier patientIdentifier = new PatientIdentifier();
		patientIdentifier.setIdentifier("123-0");
		patientIdentifier.setIdentifierType(patientService.getPatientIdentifierType(5));
		patientIdentifier.setLocation(new Location(1));
		notPreferred.addIdentifier(patientIdentifier);
		patientService.savePatient(notPreferred);
		
		//merge the two patients and retrieve the audit object
		PersonMergeLog audit = mergeAndRetrieveAudit(preferred, notPreferred);
		
		//find the UUID of the identifier that was added by the merge
		String addedIdentifierUuid = null;
		preferred = patientService.getPatient(999);
		for (PatientIdentifier id : preferred.getIdentifiers()) {
			if (id.getIdentifier().equals(patientIdentifier.getIdentifier())) {
				addedIdentifierUuid = id.getUuid();
			}
		}
		assertNotNull("expected new identifier was not found in the preferred patient after the merge",
			addedIdentifierUuid);
		assertTrue(isValueInList(addedIdentifierUuid, audit.getPersonMergeLogData().getCreatedIdentifiers()), "person identifier creation not audited");
	}
	
	/**
	 * @see PatientService#mergePatients(Patient,Patient)
	 */
	@Test
	public void mergePatients_shouldAuditCreatedNames() throws Exception {
		//retrieve preferred patient
		Patient preferred = patientService.getPatient(999);
		
		//retrieve notPreferredPatient and save it with an added name
		Patient notPreferred = patientService.getPatient(2);
		voidOrders(Collections.singleton(notPreferred));
		PersonName name = new PersonName("first1234", "middle", "last1234");
		notPreferred.addName(name);
		patientService.savePatient(notPreferred);
		
		//merge the two patients and retrieve the audit object
		PersonMergeLog audit = mergeAndRetrieveAudit(preferred, notPreferred);
		
		//find the UUID of the name that was added by the merge
		String addedNameUuid = null;
		preferred = patientService.getPatient(999);
		for (PersonName n : preferred.getNames()) {
			if (n.getFullName().equals(name.getFullName())) {
				addedNameUuid = n.getUuid();
			}
		}
		assertNotNull(addedNameUuid, "expected new name was not found in the preferred patient after the merge");
		assertTrue(isValueInList(addedNameUuid, audit.getPersonMergeLogData().getCreatedNames()), "person name creation not audited");
	}
	
	/**
	 * @see PatientService#mergePatients(Patient,Patient)
	 */
	@Test
	public void mergePatients_shouldAuditCreatedPatientPrograms() throws Exception {
		//retrieve preferred  and notPreferredPatient patient
		Patient preferred = patientService.getPatient(999);
		Patient notPreferred = patientService.getPatient(2);
		voidOrders(Collections.singleton(notPreferred));
		
		//retrieve program for notProferred patient
		PatientProgram program = Context.getProgramWorkflowService()
		        .getPatientPrograms(notPreferred, null, null, null, null, null, false).get(0);
		
		//merge the two patients and retrieve the audit object
		PersonMergeLog audit = mergeAndRetrieveAudit(preferred, notPreferred);
		
		//find the UUID of the program to which the preferred patient was enrolled as a result of the merge
		String enrolledProgramUuid = null;
		List<PatientProgram> programs = Context.getProgramWorkflowService().getPatientPrograms(preferred, null, null, null,
		    null, null, false);
		for (PatientProgram p : programs) {
			if (p.getDateCreated().equals(program.getDateCreated())) {
				enrolledProgramUuid = p.getUuid();
			}
		}
		assertNotNull("expected enrolled program was not found for the preferred patient after the merge",
			enrolledProgramUuid);
		assertTrue(isValueInList(enrolledProgramUuid, audit.getPersonMergeLogData().getCreatedPrograms()), "program enrollment not audited");
	}
	
	/**
	 * @see PatientService#mergePatients(Patient,Patient)
	 */
	@Test
	public void mergePatients_shouldAuditCreatedRelationships() throws Exception {
		//create relationships and retrieve preferred  and notPreferredPatient patient
		executeDataSet(PATIENT_RELATIONSHIPS_XML);
		Patient preferred = patientService.getPatient(7);
		Patient notPreferred = patientService.getPatient(2);
		voidOrders(Collections.singleton(notPreferred));
		
		//merge the two patients and retrieve the audit object
		PersonMergeLog audit = mergeAndRetrieveAudit(preferred, notPreferred);
		
		//find the UUID of the created relationship as a result of the merge
		//note: since patient 2 is related to patient 1. patient 7 should now be related to patient 1
		String createdRelationshipUuid = null;
		List<Relationship> relationships = personService.getRelationshipsByPerson(preferred);
		for (Relationship r : relationships) {
			if (r.getPersonB().getId().equals(1) || r.getPersonA().getId().equals(1)) {
				createdRelationshipUuid = r.getUuid();
			}
		}
		assertNotNull(createdRelationshipUuid, "expected relationship was not found for the preferred patient after the merge");
		assertTrue(isValueInList(createdRelationshipUuid, audit.getPersonMergeLogData().getCreatedRelationships()), "relationship creation not audited");
	}
	
	/**
	 * @see PatientService#mergePatients(Patient,Patient)
	 */
	@Test
	public void mergePatients_shouldAuditVoidedRelationships() throws Exception {
		//create relationships and retrieve preferred and notPreferredPatient patient
		executeDataSet(PATIENT_RELATIONSHIPS_XML);
		Patient preferred = patientService.getPatient(999);
		Patient notPreferred = patientService.getPatient(2);
		voidOrders(Collections.singleton(notPreferred));
		
		//merge the two patients and retrieve the audit object
		PersonMergeLog audit = mergeAndRetrieveAudit(preferred, notPreferred);
		
		assertTrue(isValueInList(personService.getRelationship(4).getUuid(), audit.getPersonMergeLogData().getVoidedRelationships()), "relationship voiding not audited");
	}
	
	/**
	 * @see PatientService#mergePatients(Patient,Patient)
	 */
	@Test
	public void mergePatients_shouldAuditMovedEncounters() throws Exception {
		//retrieve patients
		Patient preferred = patientService.getPatient(999);
		Patient notPreferred = patientService.getPatient(7);
		voidOrders(Collections.singleton(notPreferred));
		
		//merge the two patients and retrieve the audit object
		PersonMergeLog audit = mergeAndRetrieveAudit(preferred, notPreferred);
		
		assertTrue(isValueInList(Context.getEncounterService().getEncounter(3).getUuid(), audit.getPersonMergeLogData().getMovedEncounters()), "encounter creation not audited");
	}
	
	/**
	 * @see PatientService#mergePatients(Patient,Patient)
	 */
	@Test
	public void mergePatients_shouldAuditMovedIndependentObservations() throws Exception {
		//retrieve patients
		Patient preferred = patientService.getPatient(999);
		Patient notPreferred = patientService.getPatient(7);
		voidOrders(Collections.singleton(notPreferred));
		
		//get an observation for notPreferred and make it independent from any encounter
		Obs obs = Context.getObsService().getObs(7);
		obs.setEncounter(null);
		obs.setComment("this observation is for testing the merge");
		Context.getObsService().saveObs(obs, "Reason cannot be blank");
		
		//merge the two patients and retrieve the audit object
		PersonMergeLog audit = mergeAndRetrieveAudit(preferred, notPreferred);
		String uuid = null;
		List<Obs> observations = Context.getObsService().getObservationsByPerson(preferred);
		for (Obs o : observations) {
			if (obs.getComment().equals(o.getComment())) {
				uuid = o.getUuid();
			}
		}
		assertTrue(isValueInList(uuid, audit.getPersonMergeLogData().getMovedIndependentObservations()), "moving of independent observation was not audited");
	}
	
	/**
	 * @see PatientService#mergePatients(Patient,Patient)
	 */
	@Test
	public void mergePatients_shouldAuditMovedUsers() throws Exception {
		//retrieve patients
		Patient preferred = patientService.getPatient(999);
		Patient notPreferred = patientService.getPatient(7);
		voidOrders(Collections.singleton(notPreferred));
		
		User user = Context.getUserService().getUser(501);
		user.setPerson(notPreferred);
		Context.getUserService().saveUser(user);
		
		//merge the two patients and retrieve the audit object
		PersonMergeLog audit = mergeAndRetrieveAudit(preferred, notPreferred);
		
		assertTrue(isValueInList(Context.getUserService().getUser(501).getUuid(), audit.getPersonMergeLogData().getMovedUsers()), "user association change not audited");
	}
	
	/**
	 * @see PatientService#mergePatients(Patient,Patient)
	 */
	@Test
	public void mergePatients_shouldAuditPriorCauseOfDeath() throws Exception {
		//retrieve preferred patient and set a cause of death
		Patient preferred = patientService.getPatient(999);
		preferred.setCauseOfDeath(Context.getConceptService().getConcept(3));
		preferred.setDeathDate(new Date());
		preferred.setDead(true);
		preferred.addName(new PersonName("givenName", "middleName", "familyName"));
		patientService.savePatient(preferred);
		//merge with not preferred
		Patient notPreferred = patientService.getPatient(7);
		voidOrders(Collections.singleton(notPreferred));
		PersonMergeLog audit = mergeAndRetrieveAudit(preferred, notPreferred);
		assertEquals(Context.getConceptService().getConcept(3).getUuid(), audit.getPersonMergeLogData().getPriorCauseOfDeath(), "prior cause of death was not audited");
	}
	
	/**
	 * @see PatientService#mergePatients(Patient,Patient)
	 */
	@Test
	public void mergePatients_shouldAuditPriorDateOfBirth() throws Exception {
		//retrieve preferred patient and set a date of birth
		GregorianCalendar cDate = new GregorianCalendar();
		cDate.setTime(new Date());
		//milliseconds are not serialized into the database. they will be ignored in the test
		cDate.set(Calendar.MILLISECOND, 0);
		Patient preferred = patientService.getPatient(999);
		preferred.setBirthdate(cDate.getTime());
		preferred.addName(new PersonName("givenName", "middleName", "familyName"));
		patientService.savePatient(preferred);
		Patient notPreferred = patientService.getPatient(7);
		voidOrders(Collections.singleton(notPreferred));
		PersonMergeLog audit = mergeAndRetrieveAudit(preferred, notPreferred);
		assertEquals(cDate.getTime(), audit.getPersonMergeLogData().getPriorDateOfBirth(), "prior date of birth was not audited");
	}
	
	/**
	 * @see PatientService#mergePatients(Patient,Patient)
	 */
	@Test
	public void mergePatients_shouldAuditPriorDateOfBirthEstimated() throws Exception {
		//retrieve preferred patient and set a date of birth
		GregorianCalendar cDate = new GregorianCalendar();
		cDate.setTime(new Date());
		Patient preferred = patientService.getPatient(999);
		preferred.setBirthdate(cDate.getTime());
		preferred.setBirthdateEstimated(true);
		preferred.addName(new PersonName("givenName", "middleName", "familyName"));
		patientService.savePatient(preferred);
		Patient notPreferred = patientService.getPatient(7);
		voidOrders(Collections.singleton(notPreferred));
		PersonMergeLog audit = mergeAndRetrieveAudit(preferred, notPreferred);
		assertTrue(audit.getPersonMergeLogData().isPriorDateOfBirthEstimated(), "prior estimated date of birth was not audited");
	}
	
	/**
	 * @see PatientService#mergePatients(Patient,Patient)
	 */
	@Test
	public void mergePatients_shouldAuditPriorDateOfDeath() throws Exception {
		//retrieve preferred patient and set a date of birth
		GregorianCalendar cDate = new GregorianCalendar();
		cDate.setTime(new Date());
		//milliseconds are not serialized into the database. they will be ignored in the test
		cDate.set(Calendar.MILLISECOND, 0);
		Patient preferred = patientService.getPatient(999);
		preferred.setDeathDate(cDate.getTime());
		preferred.setDead(true);
		preferred.setCauseOfDeath(Context.getConceptService().getConcept(3));
		preferred.addName(new PersonName("givenName", "middleName", "familyName"));
		patientService.savePatient(preferred);
		Patient notPreferred = patientService.getPatient(7);
		voidOrders(Collections.singleton(notPreferred));
		PersonMergeLog audit = mergeAndRetrieveAudit(preferred, notPreferred);
		assertEquals(cDate.getTime(), audit.getPersonMergeLogData().getPriorDateOfDeath(), "prior date of death was not audited");

	}
	
	/**
	 * @see PatientService#mergePatients(Patient,Patient)
	 */
	@Test
	public void mergePatients_shouldAuditPriorDateOfDeathEstimated() throws Exception {
		//retrieve preferred patient and set a date of death
		GregorianCalendar cDate = new GregorianCalendar();
		cDate.setTime(new Date());
		Patient preferred = patientService.getPatient(999);
		preferred.setDeathDate(cDate.getTime());
		preferred.setDeathdateEstimated(true);
		preferred.setCauseOfDeath(Context.getConceptService().getConcept(3));
		preferred.addName(new PersonName("givenName", "middleName", "familyName"));
		patientService.savePatient(preferred);
		Patient notPreferred = patientService.getPatient(7);
		voidOrders(Collections.singleton(notPreferred));
		PersonMergeLog audit = mergeAndRetrieveAudit(preferred, notPreferred);
		assertTrue(audit.getPersonMergeLogData().getPriorDateOfDeathEstimated(), "prior estimated date of death was not audited");
	}
	
	/**
	 * @see PatientService#mergePatients(Patient,Patient)
	 */
	@Test
	public void mergePatients_shouldAuditPriorGender() throws Exception {
		//retrieve preferred patient and set gender
		Patient preferred = patientService.getPatient(999);
		preferred.setGender("M");
		preferred.addName(new PersonName("givenName", "middleName", "familyName"));
		patientService.savePatient(preferred);
		//merge with not preferred
		Patient notPreferred = patientService.getPatient(7);
		voidOrders(Collections.singleton(notPreferred));
		PersonMergeLog audit = mergeAndRetrieveAudit(preferred, notPreferred);
		assertEquals("M", audit.getPersonMergeLogData().getPriorGender(), "prior gender was not audited");
	}
	
	/**
	 * @see PatientService#mergePatients(Patient,Patient)
	 */
	@Test
	public void mergePatients_shouldNotCopyOverDuplicatePatientIdentifiers() throws Exception {
		List<Location> locations = Context.getLocationService().getAllLocations();
		assertTrue(CollectionUtils.isNotEmpty(locations));
		// check if we have patient identifiers already
		PatientIdentifierType patientIdentifierType = Context.getPatientService().getPatientIdentifierType(5);
		assertNotNull(patientIdentifierType);
		//retrieve preferred patient and set gender
		Patient preferred = patientService.getPatient(999);
		// create new identifier for the preferred patient
		PatientIdentifier preferredIdentifier = new PatientIdentifier();
		preferredIdentifier.setIdentifier("9999-4");
		preferredIdentifier.setIdentifierType(patientIdentifierType);
		preferredIdentifier.setLocation(locations.get(0));
		preferred.addIdentifier(preferredIdentifier);
		preferred.addName(new PersonName("givenName", "middleName", "familyName"));
		patientService.savePatient(preferred);
		//merge with not preferred
		Patient notPreferred = patientService.getPatient(7);
		voidOrders(Collections.singleton(notPreferred));
		// create identifier with the same values for the non preferred patient
		PatientIdentifier nonPreferredIdentifier = new PatientIdentifier();
		nonPreferredIdentifier.setIdentifier("9999-4");
		nonPreferredIdentifier.setIdentifierType(patientIdentifierType);
		nonPreferredIdentifier.setLocation(locations.get(0));
		notPreferred.addIdentifier(nonPreferredIdentifier);
		patientService.savePatient(notPreferred);
		PersonMergeLog audit = mergeAndRetrieveAudit(preferred, notPreferred);
		// should not copy the duplicate identifier to the winner
		assertEquals(notPreferred.getIdentifiers().size() - 1, audit.getPersonMergeLogData().getCreatedIdentifiers()
		        .size());
	}
	
	private PersonMergeLog mergeAndRetrieveAudit(Patient preferred, Patient notPreferred) throws SerializationException {
		patientService.mergePatients(preferred, notPreferred);
		List<PersonMergeLog> result = personService.getAllPersonMergeLogs(true);
		assertTrue(result.size() > 0, "person merge was not audited");
		return result.get(0);
	}
	
	private boolean isValueInList(String value, List<String> list) {
		return (list != null && list.contains(value));
	}
	
	/**
	 * @see PatientService#mergePatients(Patient,Patient)
	 */
	@Test
	public void mergePatients_shouldNotVoidRelationshipsForSameTypeAndSideWithDifferentRelatives() throws Exception {
		executeDataSet(PATIENT_RELATIONSHIPS_XML);
		
		Patient preferred = patientService.getPatient(999);
		Patient notPreferred = patientService.getPatient(2);
		voidOrders(Collections.singleton(notPreferred));
		
		// expected relationships before merge:
		// * 2->1 (type 2)
		// * 999->2 (type 5)
		// * 999->1 (type 2)
		// * 7->999 (type 4)
		// * 502->2 (type 1)
		// * 7->999 (type 1)
		patientService.mergePatients(preferred, notPreferred);
		
		// expected relationships after merge:
		// * 999->1 (type 2)
		// * 7->999 (type 4)
		// * 502->999 (type 1)
		// * 7->999 (type 1)
		
		// check for relationships that should not be removed: 7->999 (type 4)
		// and 7->999 (type 1)
		List<Relationship> rels = personService.getRelationships(new Person(7), preferred, new RelationshipType(4));
		assertEquals(1, rels.size(), "7->999 (type 4) was removed");
		
		rels = personService.getRelationships(new Person(7), preferred, new RelationshipType(1));
		assertEquals(1, rels.size(), "7->999 (type 1) was removed");
	}
	
	@Test
	public void savePatient_shouldUpdateTheDateChangedAndChangedByOnUpdateOfThePersonAddress() throws Exception {
		
		Patient patient = patientService.getPatient(2);
		PersonAddress address = patient.getAddresses().iterator().next();
		address.setAddress1("Modified Address");
		
		patientService.savePatient(patient);
		
		Context.evictFromSession(patient);
		patient = patientService.getPatient(2);
		
		PersonAddress personAddress = patient.getAddresses().iterator().next();
		assertNotNull(personAddress.getDateChanged());
		assertNotNull(personAddress.getChangedBy());
	}
	
	/**
	 * @see PatientService#getCountOfPatients(String)
	 */
	@Test
	public void getCountOfPatients_shouldReturnTheRightCountWhenAPatientHasMultipleMatchingPersonNames() throws Exception {
		// TODO H2 cannot execute the generated SQL because it requires all
		// fetched columns to be included in the group by clause
		Patient patient = patientService.getPatient(2);
		// sanity check
		assertTrue(patient.getPersonName().getGivenName().startsWith("Horati"));
		// add a name that will match the search phrase
		patient.addName(new PersonName("Horatio", "Test", "name"));
		Context.getPatientService().savePatient(patient);
		assertEquals(1, Context.getPatientService().getCountOfPatients("Hor").intValue());
	}
	
	@Test
	public void getPatient_shouldCreatePatientFromPerson() throws Exception {
		executeDataSet(USER_WHO_IS_NOT_PATIENT_XML);
		Patient patient = patientService.getPatientOrPromotePerson(202);
		assertNotNull(patient);
		assertEquals(202, patient.getId().intValue());
	}
	
	@Test
	public void getPatient_shouldReturnNullWhenPersonDoesNotExist() throws Exception {
		executeDataSet(USER_WHO_IS_NOT_PATIENT_XML);
		Patient patient = patientService.getPatientOrPromotePerson(-1);
		assertNull(patient);
	}
	
	/**
	 * @see PatientService#voidPatient(Patient,String)
	 */
	@Test
	public void voidPatient_shouldVoidPerson() throws Exception {
		//given
		Patient patient = patientService.getPatient(2);
		
		//when
		patientService.voidPatient(patient, "reason");
		
		//then
		assertTrue(patient.getPersonVoided());
	}
	
	/**
	 * @see PatientService#voidPatient(Patient,String)
	 */
	@Test
	public void voidPatient_shouldRetireUsers() throws Exception {
		//given
		Patient patient = patientService.getPatient(2);
		User user = new User(patient);
		Context.getUserService().createUser(user, "Admin123");
		assertFalse(Context.getUserService().getUsersByPerson(patient, false).isEmpty());
		
		//when
		patientService.voidPatient(patient, "reason");
		
		//then
		assertThat(getUserService().getUsersByPerson(patient, false), is(empty()));
	}
	
	/**
	 * @see PatientService#unvoidPatient(Patient)
	 */
	@Test
	public void unvoidPatient_shouldUnvoidPerson() throws Exception {
		//given
		Patient patient = patientService.getPatient(2);
		patientService.voidPatient(patient, "reason");
		assertTrue(patient.getPersonVoided());
		
		//when
		patientService.unvoidPatient(patient);
		
		//then
		assertFalse(patient.getPersonVoided());
	}
	
	@Test
	public void unvoidPatient_shouldReturnNullWhenPatientIsNull() throws Exception {
		
		assertNull(patientService.unvoidPatient(null));
	}
	
	/**
	 * @see PatientService#unvoidPatient(Patient)
	 */
	@Test
	public void unvoidPatient_shouldNotUnretireUsers() throws Exception {
		//given
		Patient patient = patientService.getPatient(2);
		User user = new User(patient);
		Context.getUserService().createUser(user, "Admin123");
		patientService.voidPatient(patient, "reason");
		
		//when
		patientService.unvoidPatient(patient);
		
		//then
		assertThat(getUserService().getUsersByPerson(patient, false), is(empty()));
	}
	
	/**
	 * @see PatientService#getPatients(String,String,List,boolean)
	 */
	@Test
	public void getPatients_shouldReturnEmptyListIfNameAndIdentifierIsEmpty() throws Exception {
		//given
		
		//when
		List<Patient> patients = patientService.getPatients("", "", null, false);
		
		//then
		assertThat(patients, is(empty()));
	}
	
	/**
	 * @see PatientService#isIdentifierInUseByAnotherPatient(PatientIdentifier)
	 */
	@Test
	public void isIdentifierInUseByAnotherPatient_shouldReturnTrueIfInUseAndIdTypeUniquenessIsNull() throws Exception {
		PatientIdentifier duplicateId = patientService.getPatientIdentifier(1);
		assertNotNull(duplicateId.getLocation());
		
		PatientIdentifierType idType = duplicateId.getIdentifierType();
		assertNull(idType.getUniquenessBehavior());
		
		PatientIdentifier pi = new PatientIdentifier(duplicateId.getIdentifier(), idType, duplicateId.getLocation());
		assertTrue(patientService.isIdentifierInUseByAnotherPatient(pi));
	}
	
	/**
	 * @see PatientService#isIdentifierInUseByAnotherPatient(PatientIdentifier)
	 */
	@Test
	public void isIdentifierInUseByAnotherPatient_shouldReturnTrueIfInUseAndIdTypeUniquenessIsSetToUnique() throws Exception {
		PatientIdentifier duplicateId = patientService.getPatientIdentifier(1);
		assertNotNull(duplicateId.getLocation());
		
		PatientIdentifierType idType = duplicateId.getIdentifierType();
		idType.setUniquenessBehavior(UniquenessBehavior.UNIQUE);
		patientService.savePatientIdentifierType(idType);
		
		PatientIdentifier pi = new PatientIdentifier(duplicateId.getIdentifier(), idType, duplicateId.getLocation());
		assertTrue(patientService.isIdentifierInUseByAnotherPatient(pi));
	}
	
	/**
	 * @see PatientService#isIdentifierInUseByAnotherPatient(PatientIdentifier)
	 */
	@Test
	public void isIdentifierInUseByAnotherPatient_shouldReturnTrueIfInUseForALocationAndIdTypeUniquenessIsSetToLocation()
	    throws Exception {
		PatientIdentifier duplicateId = patientService.getPatientIdentifier(1);
		assertNotNull(duplicateId.getLocation());
		
		PatientIdentifierType idType = duplicateId.getIdentifierType();
		idType.setUniquenessBehavior(UniquenessBehavior.LOCATION);
		patientService.savePatientIdentifierType(idType);
		
		PatientIdentifier pi = new PatientIdentifier(duplicateId.getIdentifier(), idType, duplicateId.getLocation());
		assertTrue(patientService.isIdentifierInUseByAnotherPatient(pi));
	}
	
	/**
	 * @see PatientService#isIdentifierInUseByAnotherPatient(PatientIdentifier)
	 */
	@Test
	public void isIdentifierInUseByAnotherPatient_shouldReturnFalseIfInUseForAnotherLocationAndIdUniquenessIsSetToLocation()
	    throws Exception {
		PatientIdentifier duplicateId = patientService.getPatientIdentifier(1);
		assertNotNull(duplicateId.getLocation());
		
		PatientIdentifierType idType = duplicateId.getIdentifierType();
		idType.setUniquenessBehavior(UniquenessBehavior.LOCATION);
		patientService.savePatientIdentifierType(idType);
		
		Location idLocation = locationService.getLocation(2);
		assertNotSame(idLocation, duplicateId.getLocation());//sanity check
		PatientIdentifier pi = new PatientIdentifier(duplicateId.getIdentifier(), idType, idLocation);
		assertFalse(patientService.isIdentifierInUseByAnotherPatient(pi));
	}
	
	/**
	 * @see PatientService#getAllPatientIdentifierTypes(boolean)
	 */
	@Test
	public void getAllPatientIdentifierTypes_shouldOrderAsDefaultComparator() throws Exception {
		List<PatientIdentifierType> list = patientService.getAllPatientIdentifierTypes();
		List<PatientIdentifierType> sortedList = new ArrayList<>(list);
		sortedList.sort(new PatientIdentifierTypeDefaultComparator());
		assertEquals(sortedList, list);
	}
	
	/**
	 * @see PatientService#getPatientIdentifierTypes(String,String,Boolean,Boolean)
	 */
	@Test
	public void getPatientIdentifierTypes_shouldOrderAsDefaultComparator() throws Exception {
		List<PatientIdentifierType> list = patientService.getPatientIdentifierTypes(null, null, false, null);
		List<PatientIdentifierType> sortedList = new ArrayList<>(list);
		sortedList.sort(new PatientIdentifierTypeDefaultComparator());
		assertEquals(sortedList, list);
	}
	
	@Test
	public void mergePatients_shouldMaintainSimilarButDifferentNames() throws Exception {
		executeDataSet(PATIENT_MERGE_XML);
		Patient preferredPatient = patientService.getPatient(10000);
		Patient nonPreferredPatient = patientService.getPatient(10001);
		
		patientService.mergePatients(preferredPatient, nonPreferredPatient);
		Set<PersonName> names = preferredPatient.getNames();
		
		if ((PersonName.getFormat()).equals(OpenmrsConstants.PERSON_NAME_FORMAT_LONG)) {
			assertThat(names, containsFullName("President John Fitzgerald Kennedy Esq."));
		} else {
			assertThat(names, containsFullName("John Fitzgerald Kennedy"));
		}
		
	}
	
	@Test
	public void mergePatients_shouldMaintainSimilarButDifferentAddresses() throws Exception {
		executeDataSet(PATIENT_MERGE_XML);
		Patient preferredPatient = patientService.getPatient(10000);
		Patient nonPreferredPatient = patientService.getPatient(10001);
		
		patientService.mergePatients(preferredPatient, nonPreferredPatient);
		Set<PersonAddress> addresses = preferredPatient.getAddresses();
		
		assertThat(
		    addresses,
		    containsAddress("a1:Apartment ABC, a2:123 fake st, cv:Faketown, sp:null, c:null, cd:null, nc:null, pc:1234, lat:null, long:null"));
		assertThat(
		    addresses,
		    containsAddress("a1:Apartment ABC, a2:123 fake st, cv:Faketown, sp:Fakeland, c:null, cd:null, nc:null, pc:null, lat:null, long:null"));
		
	}
	
	@Test
	public void mergePatients_shouldMergePatientNames() throws Exception {
		executeDataSet(PATIENT_MERGE_XML);
		Patient preferredPatient = patientService.getPatient(10001);
		Patient nonPreferredPatient = patientService.getPatient(10000);
		
		patientService.mergePatients(preferredPatient, nonPreferredPatient);
		assertThat(preferredPatient.getAddresses().size(), equalTo(2));
		
	}
	
	/**
	 * @see PatientService#savePatient(Patient)
	 */
	@Test
	public void savePatient_shouldSetThePreferredNameAddressAndIdentifierIfNoneIsSpecified() throws Exception {
		Patient patient = new Patient();
		patient.setGender("M");
		PatientIdentifier identifier = new PatientIdentifier("QWERTY", patientService.getPatientIdentifierType(2),
		        locationService.getLocation(1));
		patient.addIdentifier(identifier);
		PersonName name = new PersonName("givenName", "middleName", "familyName");
		patient.addName(name);
		PersonAddress address = new PersonAddress();
		address.setAddress1("some address");
		patient.addAddress(address);
		
		Context.getPatientService().savePatient(patient);
		assertTrue(identifier.getPreferred());
		assertTrue(name.getPreferred());
		assertTrue(address.getPreferred());
	}
	
	/**
	 * @see PatientService#savePatient(Patient)
	 */
	@Test
	public void savePatient_shouldNotSetThePreferredNameAddressAndIdentifierIfTheyAlreadyExist() throws Exception {
		Patient patient = new Patient();
		patient.setGender("M");
		PatientIdentifier identifier = new PatientIdentifier("QWERTY", patientService.getPatientIdentifierType(5),
		        locationService.getLocation(1));
		PatientIdentifier preferredIdentifier = new PatientIdentifier("QWERTY2", patientService.getPatientIdentifierType(2),
		        locationService.getLocation(1));
		preferredIdentifier.setPreferred(true);
		patient.addIdentifier(identifier);
		patient.addIdentifier(preferredIdentifier);
		
		PersonName name = new PersonName("givenName", "middleName", "familyName");
		PersonName preferredName = new PersonName("givenName", "middleName", "familyName");
		preferredName.setPreferred(true);
		patient.addName(name);
		patient.addName(preferredName);
		
		PersonAddress address = new PersonAddress();
		address.setAddress1("some address");
		PersonAddress preferredAddress = new PersonAddress();
		preferredAddress.setAddress1("another address");
		preferredAddress.setPreferred(true);
		patient.addAddress(address);
		patient.addAddress(preferredAddress);
		
		patientService.savePatient(patient);
		assertTrue(preferredIdentifier.getPreferred());
		assertTrue(preferredName.getPreferred());
		assertTrue(preferredAddress.getPreferred());
		assertFalse(identifier.getPreferred());
		assertFalse(name.getPreferred());
		assertFalse(address.getPreferred());
	}
	
	/**
	 * @see PatientService#savePatient(Patient)
	 */
	@Test
	public void savePatient_shouldNotSetAVoidedNameOrAddressOrIdentifierAsPreferred() throws Exception {
		Patient patient = new Patient();
		patient.setGender("M");
		PatientIdentifier identifier = new PatientIdentifier("QWERTY", patientService.getPatientIdentifierType(2),
		        locationService.getLocation(1));
		PatientIdentifier preferredIdentifier = new PatientIdentifier("QWERTY2", patientService.getPatientIdentifierType(2),
		        locationService.getLocation(1));
		preferredIdentifier.setPreferred(true);
		preferredIdentifier.setVoided(true);
		patient.addIdentifier(identifier);
		patient.addIdentifier(preferredIdentifier);
		
		PersonName name = new PersonName("givenName", "middleName", "familyName");
		PersonName preferredName = new PersonName("givenName", "middleName", "familyName");
		preferredName.setPreferred(true);
		preferredName.setVoided(true);
		patient.addName(name);
		patient.addName(preferredName);
		
		PersonAddress address = new PersonAddress();
		address.setAddress1("some address");
		PersonAddress preferredAddress = new PersonAddress();
		preferredAddress.setAddress1("another address");
		preferredAddress.setPreferred(true);
		preferredAddress.setVoided(true);
		patient.addAddress(address);
		patient.addAddress(preferredAddress);
		
		patientService.savePatient(patient);
		assertFalse(preferredIdentifier.getPreferred());
		assertFalse(preferredName.getPreferred());
		assertFalse(preferredAddress.getPreferred());
		assertTrue(identifier.getPreferred());
		assertTrue(name.getPreferred());
		assertTrue(address.getPreferred());
	}
	
	/**
	 * https://tickets.openmrs.org/browse/TRUNK-3728
	 * 
	 * @see PatientService#savePatient(Patient)
	 */
	@Test
	public void savePatient_shouldNotThrowNonUniqueObjectExceptionWhenCalledWithPersonPromotedToPatient() throws Exception {
		Person person = personService.getPerson(1);
		Patient patient = patientService.getPatientOrPromotePerson(person.getPersonId());
		PatientIdentifier patientIdentifier = new PatientIdentifier("some identifier", new PatientIdentifierType(2),
		        new Location(1));
		patientIdentifier.setPreferred(true);
		patient.addIdentifier(patientIdentifier);
		
		patientService.savePatient(patient);
	}
	
	/**
	 * @see PatientService#getPatients(String,Integer,Integer)
	 */
	@Test
	public void getPatients_shouldFindAPatientsWithAMatchingIdentifierWithNoDigits() throws Exception {
		final String identifier = "XYZ";
		Patient patient = patientService.getPatient(2);
		assertEquals(0, patientService.getPatients(identifier, null, null).size());
		PatientIdentifier pId = new PatientIdentifier(identifier, patientService.getPatientIdentifierType(5),
		        locationService.getLocation(1));
		patient.addIdentifier(pId);
		patientService.savePatient(patient);

		updateSearchIndex();

		assertEquals(1, patientService.getPatients(identifier).size());
	}
	
	/**
	 * @see PatientService#getCountOfPatients(String)
	 */
	@Test
	public void getCountOfPatients_shouldReturnTheRightCountOfPatientsWithAMatchingIdentifierWithNoDigits() throws Exception {
		final String identifier = "XYZ";
		Patient patient = patientService.getPatient(2);
		assertEquals(0, patientService.getCountOfPatients(identifier).intValue());
		PatientIdentifier pId = new PatientIdentifier(identifier, patientService.getPatientIdentifierType(5),
		        locationService.getLocation(1));
		patient.addIdentifier(pId);
		patientService.savePatient(patient);

		updateSearchIndex();
		
		assertEquals(1, patientService.getCountOfPatients(identifier).intValue());
	}
	
	/**
	 *           an APIException when a null argument is passed
	 */
	@Test
	public void savePatientIdentifier_shouldThrowAnAPIExceptionWhenANullArgumentIsPassed() throws Exception {
		assertThrows(APIException.class, () -> patientService.savePatientIdentifier(null));
	}
	
	/**
	 * Creates a new Global Property to lock patient identifier types by setting its value
	 * 
	 * @param propertyValue value for patient identifier types locked GP
	 */
	public void createPatientIdentifierTypeLockedGPAndSetValue(String propertyValue) {
		GlobalProperty gp = new GlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_PATIENT_IDENTIFIER_TYPES_LOCKED);
		gp.setPropertyValue(propertyValue);
		Context.getAdministrationService().saveGlobalProperty(gp);
	}
	
	@Test
	public void savePatientIdentifierType_shouldThrowErrorWhenTryingToSaveAPatientIdentifierTypeWhilePatientIdentifierTypesAreLocked()
	    throws Exception {
		PatientService ps = Context.getPatientService();
		createPatientIdentifierTypeLockedGPAndSetValue("true");
		PatientIdentifierType pit = ps.getPatientIdentifierType(1);
		pit.setDescription("test");
		assertThrows(PatientIdentifierTypeLockedException.class, () -> ps.savePatientIdentifierType(pit));
	}
	
	@Test
	public void retirePatientIdentifierType_shouldThrowErrorWhenTryingToRetireAPatientIdentifierTypeWhilePatientIdentifierTypesAreLocked()
	    throws Exception {
		PatientService ps = Context.getPatientService();
		createPatientIdentifierTypeLockedGPAndSetValue("true");
		PatientIdentifierType pit = ps.getPatientIdentifierType(1);
		assertThrows(PatientIdentifierTypeLockedException.class, () -> ps.retirePatientIdentifierType(pit, "Retire test"));
	}
	
	@Test
	public void retirePatientIdentifierType_shouldThrowAPIExceptionWhenNullReasonIsPassed() throws Exception {
		PatientService ps = Context.getPatientService();
		PatientIdentifierType pit = ps.getPatientIdentifierType(1);
		assertThrows(APIException.class, () -> ps.retirePatientIdentifierType(pit, null));
	}
	
	@Test
	public void retirePatientIdentifierType_shouldRetireAndSetReasonAndRetiredByAndDate() {
		PatientService ps = Context.getPatientService();
		PatientIdentifierType pit = ps.getPatientIdentifierType(1);
		String reason = "moved away";
		PatientIdentifierType result = ps.retirePatientIdentifierType(pit, reason);

		assertTrue(result.getRetired());
		assertEquals(result.getRetireReason(), reason);
		assertEquals(result.getRetiredBy(), Context.getAuthenticatedUser());
		Date today = new Date();
		Date dateRetired = result.getDateRetired();
		assertEquals(dateRetired.getDay(), today.getDay());
		assertEquals(dateRetired.getMonth(), today.getMonth());
		assertEquals(dateRetired.getYear(), today.getYear());
	}
	
	@Test
	public void unretirePatientIdentifierType_shouldThrowErrorWhenTryingToUnretireAPatientIdentifierTypeWhilePatientIdentifierTypesAreLocked()
	    throws Exception {
		PatientService ps = Context.getPatientService();
		createPatientIdentifierTypeLockedGPAndSetValue("true");
		PatientIdentifierType pit = ps.getPatientIdentifierType(1);
		assertThrows(PatientIdentifierTypeLockedException.class, () -> ps.unretirePatientIdentifierType(pit));
	}
	
	@Test
	public void purgePatientIdentifierType_shouldThrowErrorWhenTryingToDeleteAPatientIdentifierTypeWhilePatientIdentifierTypesAreLocked()
	    throws Exception {
		PatientService ps = Context.getPatientService();
		createPatientIdentifierTypeLockedGPAndSetValue("true");
		PatientIdentifierType pit = ps.getPatientIdentifierType(1);
		assertThrows(PatientIdentifierTypeLockedException.class, () -> ps.purgePatientIdentifierType(pit));
	}
	
	@Test
	public void mergePatients_shouldFailIfMultiplePatientsHaveActiveOrderOfSameType() throws Exception {
		String message = Context.getMessageSourceService().getMessage("Patient.merge.cannotHaveSameTypeActiveOrders",
				new Object[] { "2", "7", "Drug order" }, Context.getLocale());
		Patient preferredPatient = patientService.getPatient(2);
		Patient notPreferredPatient = patientService.getPatient(7);
		
		assertTrue(hasActiveOrderOfType(preferredPatient, "Drug order"), "Test pre-request: No Active Drug order in " + preferredPatient);
		assertTrue(hasActiveOrderOfType(preferredPatient, "Drug order"), "Test pre-request: No Active Drug order in " + notPreferredPatient);
		APIException exception = assertThrows(APIException.class, () -> patientService.mergePatients(preferredPatient, notPreferredPatient));
		assertThat(exception.getMessage(), is(message));
	}

	/**
	 * @see PatientService#mergePatients(org.openmrs.Patient, org.openmrs.Patient)
	 */
	@Test
	public void mergePatients_shouldNotFailIfOnePatientHasActiveOrder() throws Exception {
		Patient preferredPatient = patientService.getPatient(2);
		Patient notPreferredPatient = patientService.getPatient(7);
		voidOrders(Collections.singleton(notPreferredPatient));
		
		assertTrue(hasActiveOrderOfType(preferredPatient, "Drug order"), "Test pre-request: No Active Drug order in " + preferredPatient);
		assertFalse(hasActiveOrderOfType(notPreferredPatient, "Drug order"), "Test pre-request: At least one Active Drug order in " + notPreferredPatient);
		patientService.mergePatients(preferredPatient, notPreferredPatient);
	}

	/**
	 * @see PatientService#mergePatients(org.openmrs.Patient, org.openmrs.Patient)
	 */
	@Test
	public void mergePatients_shouldNotFailIfMultiplePatientsHaveActiveOrderOfDifferentTypes() throws Exception {
		Patient preferredPatient = patientService.getPatient(2);
		Patient notPreferredPatient = patientService.getPatient(7);
		OrderType DrugOrder = Context.getOrderService().getOrderTypeByName("Drug order");
		voidOrdersForType(Collections.singleton(preferredPatient), DrugOrder);
		
		assertFalse(hasActiveOrderOfType(preferredPatient, "Drug order"), "Test pre-request: No Active Drug order in " + preferredPatient);
		assertTrue(hasActiveOrderOfType(preferredPatient, "Test order"), "Test pre-request: At least one Active Test order in " + preferredPatient);
		
		assertTrue(hasActiveOrderOfType(notPreferredPatient, "Drug order"), "Test pre-request: At least one Active Drug order in " + notPreferredPatient);
		assertFalse(hasActiveOrderOfType(notPreferredPatient, "Test order"), "Test pre-request: No Active Test order in " + notPreferredPatient);
		patientService.mergePatients(preferredPatient, notPreferredPatient);
	}


	/**
	 * @see PatientService#mergePatients(org.openmrs.Patient, org.openmrs.Patient)
	 */
	@Test
	public void mergePatients_shouldMoveAllObsWithSameHierarchy() throws Exception {
		executeDataSet(PATIENT_MERGE_OBS_WITH_GROUP_MEMBER);

		Patient notPreffered = patientService.getPatient(11);
		Patient preffered = patientService.getPatient(21);

		EncounterService encounterService = Context.getEncounterService();

		assertEquals(57, encounterService.getEncountersByPatient(notPreffered).get(0).getId().intValue());
		assertEquals(3, encounterService.getEncounter(57).getAllObs(false).size());
		assertEquals(4, encounterService.getEncounter(57).getAllObs(true).size());
		assertEquals(1, encounterService.getEncounter(57).getObsAtTopLevel(false).size());
		assertEquals(1, encounterService.getEncounter(57).getObsAtTopLevel(true).size());

		patientService.mergePatients(preffered, notPreffered);

		assertEquals(3, encounterService.getEncounter(57).getAllObs(false).size());
		assertEquals(8, encounterService.getEncounter(57).getAllObs(true).size());
		assertEquals(1, encounterService.getEncounter(57).getObsAtTopLevel(false).size());
		assertEquals(2, encounterService.getEncounter(57).getObsAtTopLevel(true).size());
	}

}
