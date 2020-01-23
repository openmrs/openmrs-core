/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api.db;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;
import org.hibernate.SessionFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.Person;
import org.openmrs.PersonAttribute;
import org.openmrs.PersonAttributeType;
import org.openmrs.PersonName;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.LocationService;
import org.openmrs.api.PatientService;
import org.openmrs.api.PersonService;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.hibernate.HibernatePatientDAO;
import org.openmrs.api.db.hibernate.HibernatePersonDAO;
import org.openmrs.api.db.hibernate.PersonAttributeHelper;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.util.GlobalPropertiesTestHelper;
import org.openmrs.util.OpenmrsConstants;
import org.springframework.beans.factory.annotation.Autowired;

public class PatientDAOTest extends BaseContextSensitiveTest {
	
	private final static String PEOPLE_FROM_THE_SHIRE_XML = "org/openmrs/api/db/hibernate/include/HibernatePersonDAOTest-people.xml";
	
	private final static String PATIENTS_FROM_THE_SHIRE_XML = "org/openmrs/api/db/hibernate/include/HibernatePatientDAOTest-patients.xml";

	@Autowired
	private SessionFactory sessionFactory;

	@Autowired
	private PatientDAO dao;

	@Autowired
	private PatientService patientService;

	@Autowired
	private PersonService personService;

	@Autowired
	private LocationService locationService;

	@Autowired
	private AdministrationService adminService;

	private GlobalPropertiesTestHelper globalPropertiesTestHelper;
	
	private PersonAttributeHelper personAttributeHelper;
	
	/**
	 * Run this before each unit test in this class. The "@Before" method in
	 * {@link BaseContextSensitiveTest} is run right before this method.
	 * 
	 * @throws Exception
	 */
	@Before
	public void runBeforeEachTest() {
		executeDataSet(PEOPLE_FROM_THE_SHIRE_XML);
		executeDataSet(PATIENTS_FROM_THE_SHIRE_XML);

		updateSearchIndex();

		personAttributeHelper = new PersonAttributeHelper(sessionFactory);
		globalPropertiesTestHelper = new GlobalPropertiesTestHelper(adminService);

		globalPropertiesTestHelper.setGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_PATIENT_SEARCH_MATCH_MODE,
				OpenmrsConstants.GLOBAL_PROPERTY_PATIENT_SEARCH_MATCH_START);
		globalPropertiesTestHelper.setGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_PERSON_ATTRIBUTE_SEARCH_MATCH_MODE,
				OpenmrsConstants.GLOBAL_PROPERTY_PERSON_ATTRIBUTE_SEARCH_MATCH_EXACT);
	}
	
	/**
	 * @see PatientDAO#getPatients(String,String,List<QPatientIdentifierType;>,null)
	 */
	@Test
	public void getPatients_shouldEscapeAnAsterixCharacterInIdentifierPhrase() {
		//Note that all tests for wildcard should be pass in 2s due to the behaviour of wildcards,
		//that is we test for the size and actual patient object returned
		Patient patient2 = patientService.getPatient(2);
		PatientIdentifier patientIdentifier = new PatientIdentifier("*567", patientService.getPatientIdentifierType(5), Context
		        .getLocationService().getLocation(1));
		patient2.addIdentifier(patientIdentifier);
		patientService.savePatient(patient2);
		
		//add closely matching identifier to a different patient
		Patient patient6 = patientService.getPatient(6);
		PatientIdentifier patientIdentifier6 = new PatientIdentifier("4567", patientService.getPatientIdentifierType(5), Context
		        .getLocationService().getLocation(1));
		patientIdentifier6.setPreferred(true);
		patient6.addIdentifier(patientIdentifier6);
		patientService.savePatient(patient6);

		updateSearchIndex();

		//we expect only one matching patient
		int actualSize = dao.getPatients("*567", 0, null).size();
		Assert.assertEquals(1, actualSize);
		
		//if actually the search returned the matching patient
		Patient actualPatient = dao.getPatients("*567", 0, null).get(0);
		
		Assert.assertEquals(patient2, actualPatient);
	}
	
	/**
	 * @see PatientDAO#getPatients(String,String,List<QPatientIdentifierType;>,null)
	 */
	@Test
	public void getPatients_shouldEscapePercentageCharacterInIdentifierPhrase() {
		
		Patient patient2 = patientService.getPatient(2);
		PatientIdentifier patientIdentifier = new PatientIdentifier("%567", patientService.getPatientIdentifierType(5), Context
		        .getLocationService().getLocation(1));
		patient2.addIdentifier(patientIdentifier);
		patientService.savePatient(patient2);
		
		//add closely matching identifier to a different patient
		Patient patient6 = patientService.getPatient(6);
		PatientIdentifier patientIdentifier6 = new PatientIdentifier("4567", patientService.getPatientIdentifierType(5), Context
		        .getLocationService().getLocation(1));
		patientIdentifier6.setPreferred(true);
		patient6.addIdentifier(patientIdentifier6);
		patientService.savePatient(patient6);

		updateSearchIndex();
		
		//we expect only one matching patient
		int actualSize = dao.getPatients("%567", 0, null).size();
		Assert.assertEquals(1, actualSize);
		
		//if actually the search returned the matching patient
		Patient actualPatient = dao.getPatients("%567", 0, null).get(0);
		
		Assert.assertEquals(patient2, actualPatient);
	}
	
	/**
	 * @throws SQLException
	 * @see PatientDAO#getPatients(String,String,List<QPatientIdentifierType;>,null)
	 */
	@Test
	public void getPatients_shouldEscapeUnderscoreCharacterInIdentifierPhrase() throws SQLException {
		deleteAllData();
		baseSetupWithStandardDataAndAuthentication();
		Patient patient2 = patientService.getPatient(2);
		PatientIdentifier patientIdentifier = new PatientIdentifier("_567", patientService.getPatientIdentifierType(5), Context
		        .getLocationService().getLocation(1));
		patient2.addIdentifier(patientIdentifier);
		patientService.savePatient(patient2);
		
		//add closely matching identifier to a different patient
		Patient patient6 = patientService.getPatient(6);
		PatientIdentifier patientIdentifier6 = new PatientIdentifier("4567", patientService.getPatientIdentifierType(5), Context
		        .getLocationService().getLocation(1));
		patientIdentifier6.setPreferred(true);
		patient6.addIdentifier(patientIdentifier6);
		patientService.savePatient(patient6);

		updateSearchIndex();
		
		//we expect only one matching patient
		int actualSize = dao.getPatients("_567", 0, null).size();
		Assert.assertEquals(1, actualSize);
		
		//if actually the search returned the matching patient
		Patient actualPatient = dao.getPatients("_567", 0, null).get(0);
		
		Assert.assertEquals(patient2, actualPatient);
	}
	
	/**
	 * @see PatientDAO#getPatients(String,String,List<QPatientIdentifierType;>,null)
	 */
	@Test
	public void getPatients_shouldEscapePercentageCharacterInNamePhrase() {
		
		Patient patient2 = patientService.getPatient(2);
		PersonName name = new PersonName("%cats", "and", "dogs");
		patient2.addName(name);
		patientService.savePatient(patient2);
		
		//add a new closely matching identifier to another patient
		Patient patient6 = patientService.getPatient(6);
		PersonName name6 = new PersonName("acats", "and", "dogs");
		patient6.addName(name6);
		patient6.getPatientIdentifier().setPreferred(true);
		patientService.savePatient(patient6);

		updateSearchIndex();
		
		//we expect only one matching patient
		int actualSize = dao.getPatients("%ca", 0, null).size();
		Assert.assertEquals(1, actualSize);
		
		Patient actualPatient = dao.getPatients("%ca", 0, null).get(0);
		//if actually the search returned the matching patient
		Assert.assertEquals(patient2, actualPatient);
	}
	
	/**
	 * @see PatientDAO#getPatients(String,String,List<QPatientIdentifierType;>,null)
	 */
	@Test
	public void getPatients_shouldEscapeUnderscoreCharacterInNamePhrase() {
		
		Patient patient2 = patientService.getPatient(2);
		PersonName name = new PersonName("_cats", "and", "dogs");
		patient2.addName(name);
		patientService.savePatient(patient2);
		
		//add a new closely matching name to another patient
		Patient patient6 = patientService.getPatient(6);
		PersonName name6 = new PersonName("acats", "and", "dogs");
		patient6.addName(name6);
		patient6.getPatientIdentifier().setPreferred(true);
		patientService.savePatient(patient6);

		updateSearchIndex();
		
		//we expect only one matching patient
		int actualSize = dao.getPatients("_ca", 0, null).size();
		Assert.assertEquals(1, actualSize);
		
		//if actually the search returned the matching patient
		Patient actualPatient = dao.getPatients("_ca", 0, null).get(0);
		Assert.assertEquals(patient2, actualPatient);
		
	}
	
	/**
	 * @see PatientDAO#getPatients(String,String,List<QPatientIdentifierType;>,null)
	 */
	@Test
	public void getPatients_shouldEscapeAnAsterixCharacterInNamePhrase() {
		
		Patient patient2 = patientService.getPatient(2);
		PersonName name = new PersonName("*cats", "and", "dogs");
		patient2.addName(name);
		patientService.savePatient(patient2);
		
		//add a new closely matching name to another patient
		Patient patient6 = patientService.getPatient(6);
		PersonName name6 = new PersonName("acats", "and", "dogs");
		patient6.addName(name6);
		patient6.getPatientIdentifier().setPreferred(true);
		patientService.savePatient(patient6);

		updateSearchIndex();
		
		//we expect only one matching patient
		int actualSize = dao.getPatients("*ca", 0, null).size();
		Assert.assertEquals(1, actualSize);
		
		//if actually the search returned the matching patient
		Patient actualPatient = dao.getPatients("*ca", 0, null).get(0);
		Assert.assertEquals(patient2, actualPatient);
	}
	
	/**
	 * @see PatientDAO#getAllPatientIdentifierTypes(boolean)
	 */
	@Test
	public void getAllPatientIdentifierTypes_shouldNotReturnNullExcludingRetired() {
		Assert.assertNotNull(dao.getAllPatientIdentifierTypes(false));
	}
	
	/**
	 * @see PatientDAO#getAllPatientIdentifierTypes(boolean)
	 */
	@Test
	public void getAllPatientIdentifierTypes_shouldNotReturnRetired() {
		List<PatientIdentifierType> patientIdentifierTypes = dao.getAllPatientIdentifierTypes(false);
		Assert.assertEquals("patientIdentifierTypes list should have 3 elements", 3, patientIdentifierTypes.size());
	}
	
	/**
	 * @see PatientDAO#getAllPatientIdentifierTypes(boolean)
	 */
	@Test
	public void getAllPatientIdentifierTypes_shouldNotReturnNullIncludingRetired() {
		Assert.assertNotNull(dao.getAllPatientIdentifierTypes(true));
	}
	
	/**
	 * @see PatientDAO#getAllPatientIdentifierTypes(boolean)
	 */
	@Test
	public void getAllPatientIdentifierTypes_shouldReturnAll() {
		List<PatientIdentifierType> patientIdentifierTypes = dao.getAllPatientIdentifierTypes(true);
		Assert.assertEquals("patientIdentifierTypes list should have 4 elements", 4, patientIdentifierTypes.size());
	}
	
	@Test
	public void getPatientIdentifiers_shouldLimitByResultsByLocation() {
		Location location = Context.getLocationService().getLocation(3); // there is only one identifier in the test database for location 3
		List<PatientIdentifier> patientIdentifiers = dao.getPatientIdentifiers(null, new ArrayList<>(),
		    Collections.singletonList(location), new ArrayList<>(), null);
		Assert.assertEquals(1, patientIdentifiers.size());
		Assert.assertEquals("12345K", patientIdentifiers.get(0).getIdentifier());
	}
	
	/**
	 * @see PatientDAO#getPatientIdentifiers(String,List,List,List,Boolean)
	 */
	@Test
	public void getPatientIdentifiers_shouldNotGetVoidedPatientIdentifiers() {
		
		List<PatientIdentifier> patientIdentifiers = dao.getPatientIdentifiers(null, new ArrayList<>(),
				new ArrayList<>(), new ArrayList<>(), null);
		
		// standartTestDataset.xml contains 5 non-voided identifiers
		//
		// plus 1 non-voided identifier from HibernatePatientDAOTest-patients.xml
		
		Assert.assertEquals(8, patientIdentifiers.size());
		
		for (PatientIdentifier patientIdentifier : patientIdentifiers) {
			Assert.assertFalse(patientIdentifier.getVoided());
		}
	}
	
	/**
	 * @see PatientDAO#getPatientIdentifiers(String,List,List,List,Boolean)
	 */
	@Test
	public void getPatientIdentifiers_shouldNotFetchPatientIdentifiersThatPartiallyMatchesGivenIdentifier() {
		
		String identifier = "123"; // identifier [12345K] exist in test dataSet
		
		List<PatientIdentifier> patientIdentifiers = dao.getPatientIdentifiers(identifier,
				new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), null);
		
		Assert.assertTrue(patientIdentifiers.isEmpty());
	}
	
	/**
	 * @see PatientDAO#getPatientIdentifiers(String,List,List,List,Boolean)
	 */
	@Test
	public void getPatientIdentifiers_shouldFetchPatientIdentifiersThatEqualsGivenIdentifier() {
		
		String identifier = "101";
		
		List<PatientIdentifier> patientIdentifiers = dao.getPatientIdentifiers(identifier,
				new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), null);
		
		Assert.assertEquals(1, patientIdentifiers.size());
		Assert.assertEquals(identifier, patientIdentifiers.get(0).getIdentifier());
	}
	
	/**
	 * @see PatientDAO#getPatientIdentifiers(String,List,List,List,Boolean)
	 */
	@Test
	public void getPatientIdentifiers_shouldReturnAllMatchingNonVoidedPatientIdentifiersIfIsPreferredIsSetToFalse()
	{
		
		List<PatientIdentifier> patientIdentifiers = dao.getPatientIdentifiers(null, new ArrayList<>(),
				new ArrayList<>(), new ArrayList<>(), Boolean.FALSE);
		
		Assert.assertEquals(6, patientIdentifiers.size());
	}
	
	/**
	 * @see PatientDAO#getPatientIdentifiers(String,List,List,List,Boolean)
	 */
	@Test
	public void getPatientIdentifiers_shouldReturnAllMatchingNonVoidedPatientIdentifiersIfIsPreferredIsSetToNull()
	{
		
		List<PatientIdentifier> patientIdentifiers = dao.getPatientIdentifiers(null, new ArrayList<>(),
				new ArrayList<>(), new ArrayList<>(), null);
		
		Assert.assertEquals(8, patientIdentifiers.size());
	}
	
	/**
	 * @see PatientDAO#getPatientIdentifiers(String,List,List,List,Boolean)
	 */
	@Test
	public void getPatientIdentifiers_shouldReturnAllMatchingNonVoidedPatientIdentifiersIfIsPreferredIsSetToTrue()
	{
		
		List<PatientIdentifier> patientIdentifiers = dao.getPatientIdentifiers(null, new ArrayList<>(),
				new ArrayList<>(), new ArrayList<>(), Boolean.TRUE);
		
		Assert.assertEquals(2, patientIdentifiers.size());
	}
	
	/**
	 * @see PatientDAO#getPatientIdentifiers(String,List,List,List,Boolean)
	 */
	@Test
	public void getPatientIdentifiers_shouldFetchAllPatientIdentifiersBelongToGivenPatient() {
		
		//There are two identifiers in the test database for patient with id 2
		Patient patientWithId2 = Context.getPatientService().getPatient(2);
		
		List<PatientIdentifier> patientIdentifiers = dao.getPatientIdentifiers(null, new ArrayList<>(),
				new ArrayList<>(), Collections.singletonList(patientWithId2), null);
		
		assertThat(patientIdentifiers, containsInAnyOrder(hasIdentifier("101"), hasIdentifier("101-6")));
	}
	
	/**
	 * @see PatientDAO#getPatientIdentifiers(String,List,List,List,Boolean)
	 */
	@Test
	public void getPatientIdentifiers_shouldFetchAllPatientIdentifiersBelongToGivenPatients() {
		
		//There is one identifier[id=12345K] in the test database for patient with id 6 
		Patient patientWithId6 = Context.getPatientService().getPatient(6);
		
		//There is one identifier[id=6TS-4] in the test database for patient with id 7 
		Patient patientWithId7 = Context.getPatientService().getPatient(7);
		
		List<Patient> patientsList = Arrays.asList(patientWithId6, patientWithId7);
		
		List<PatientIdentifier> patientIdentifiers = dao.getPatientIdentifiers(null, new ArrayList<>(),
				new ArrayList<>(), patientsList, null);
		
		assertThat(patientIdentifiers, containsInAnyOrder(hasIdentifier("12345K"), hasIdentifier("6TS-4")));
	}
	
	/**
	 * Matcher for PatientIdentifier class.
	 * 
	 * @param identifier
	 * @return getIdentifier value matcher.
	 */
	private Matcher<PatientIdentifier> hasIdentifier(final String identifier) {
		
		return new FeatureMatcher<PatientIdentifier, String>(
		                                                     is(identifier), "identifier", "identifier") {
			
			@Override
			protected String featureValueOf(PatientIdentifier actual) {
				return actual.getIdentifier();
			}
			
		};
	}
	
	/**
	 * @see PatientDAO#getAllPatientIdentifierTypes(boolean)
	 */
	@Test
	public void getAllPatientIdentifierTypes_shouldReturnOrdered() {
		//given
		PatientIdentifierType patientIdentifierType1 = dao.getPatientIdentifierType(1); //non retired, non required
		
		PatientIdentifierType patientIdentifierType2 = dao.getPatientIdentifierType(2); //non retired, required
		patientIdentifierType2.setRequired(true);
		dao.savePatientIdentifierType(patientIdentifierType2);
		
		PatientIdentifierType patientIdentifierType4 = dao.getPatientIdentifierType(4); //retired
		
		PatientIdentifierType patientIdentifierType5 = dao.getPatientIdentifierType(5); //non retired, non required
		
		//when
		List<PatientIdentifierType> all = dao.getAllPatientIdentifierTypes(true);
		
		//then
		Assert.assertArrayEquals(new Object[] { patientIdentifierType2, patientIdentifierType1, patientIdentifierType5,
		        patientIdentifierType4 }, all.toArray());
	}
	
	/**
	 * @see PatientDAO#getPatientIdentifierTypes(String, String, Boolean, Boolean)
	 */
	@Test
	public void getPatientIdentifierTypes_shouldReturnNonRetiredPatientIdentifierTypesWithGivenName() {
		PatientIdentifierType oldIdNumberNonRetired = dao.getPatientIdentifierType(2);
		
		List<PatientIdentifierType> patientIdentifierTypes = dao.getPatientIdentifierTypes("Old Identification Number",
		    null, null, null);
		
		Assert.assertEquals(patientIdentifierTypes.size(), 1);
		Assert.assertEquals(oldIdNumberNonRetired, patientIdentifierTypes.get(0));
	}
	
	/**
	 * @see PatientDAO#getPatientIdentifierTypes(String, String, Boolean, Boolean)
	 */
	@Test
	public void getPatientIdentifierTypes_shouldReturnNonRetiredPatientIdentifierTypesWithGivenFormat() {
		PatientIdentifierType formatOneNonRetired = dao.getPatientIdentifierType(1);
		formatOneNonRetired.setFormat("1");
		dao.savePatientIdentifierType(formatOneNonRetired);
		
		List<PatientIdentifierType> patientIdentifierTypes = dao.getPatientIdentifierTypes(null, "1", null, null);
		
		Assert.assertEquals(patientIdentifierTypes.size(), 1);
		Assert.assertEquals(formatOneNonRetired, patientIdentifierTypes.get(0));
	}
	
	/**
	 * @see PatientDAO#getPatientIdentifierTypes(String, String, Boolean, Boolean)
	 */
	@Test
	public void getPatientIdentifierTypes_shouldReturnNonRetiredPatientIdentifierTypesThatAreNotRequired() {
		PatientIdentifierType nonRetiredNonRequired1 = dao.getPatientIdentifierType(1);
		PatientIdentifierType nonRetiredNonRequired2 = dao.getPatientIdentifierType(2);
		PatientIdentifierType nonRetiredNonRequired3 = dao.getPatientIdentifierType(5);
		
		List<PatientIdentifierType> patientIdentifierTypes = dao.getPatientIdentifierTypes(null, null, false, null);
		
		Assert.assertEquals(patientIdentifierTypes.size(), 3);
		Assert.assertTrue(patientIdentifierTypes.contains(nonRetiredNonRequired1));
		Assert.assertTrue(patientIdentifierTypes.contains(nonRetiredNonRequired2));
		Assert.assertTrue(patientIdentifierTypes.contains(nonRetiredNonRequired3));
	}
	
	/**
	 * @see PatientDAO#getPatientIdentifierTypes(String, String, Boolean, Boolean)
	 */
	@Test
	public void getPatientIdentifierTypes_shouldReturnNonRetiredPatientIdentifierTypesThatAreRequired() {
		PatientIdentifierType nonRetiredRequired = dao.getPatientIdentifierType(4);
		nonRetiredRequired.setRetired(false);
		nonRetiredRequired.setRequired(true);
		dao.savePatientIdentifierType(nonRetiredRequired);
		
		List<PatientIdentifierType> patientIdentifierTypes = dao.getPatientIdentifierTypes(null, null, true, null);
		
		Assert.assertEquals(patientIdentifierTypes.size(), 1);
		Assert.assertEquals(nonRetiredRequired, patientIdentifierTypes.get(0));
	}
	
	/**
	 * @see PatientDAO#getPatientIdentifierTypes(String, String, Boolean, Boolean)
	 */
	@Test
	public void getPatientIdentifierTypes_shouldReturnOnlyNonRetiredPatientIdentifierTypes() {
		PatientIdentifierType nonRetiredType1 = dao.getPatientIdentifierType(1);
		Assert.assertEquals(nonRetiredType1.getRetired(), false);
		
		PatientIdentifierType nonRetiredType2 = dao.getPatientIdentifierType(2);
		Assert.assertEquals(nonRetiredType2.getRetired(), false);
		
		PatientIdentifierType nonRetiredType3 = dao.getPatientIdentifierType(5);
		Assert.assertEquals(nonRetiredType3.getRetired(), false);
		
		PatientIdentifierType retiredType = dao.getPatientIdentifierType(4);
		Assert.assertEquals(retiredType.getRetired(), true);
		
		List<PatientIdentifierType> patientIdentifierTypes = dao.getPatientIdentifierTypes(null, null, null, null);
		
		Assert.assertEquals(patientIdentifierTypes.size(), 3);
		Assert.assertTrue(patientIdentifierTypes.contains(nonRetiredType1));
		Assert.assertTrue(patientIdentifierTypes.contains(nonRetiredType2));
		Assert.assertTrue(patientIdentifierTypes.contains(nonRetiredType3));
	}
	
	/**
	 * @see PatientDAO#getPatientIdentifierTypes(String, String, Boolean, Boolean)
	 */
	@Test
	public void getPatientIdentifierTypes_shouldReturnNonRetiredPatientIdentifierTypes_OrderedByRequiredFirst() {
		PatientIdentifierType nonRetiredNonRequiredType1 = dao.getPatientIdentifierType(1);
		PatientIdentifierType nonRetiredNonRequiredType2 = dao.getPatientIdentifierType(5);
		PatientIdentifierType nonRetiredRequiredType = dao.getPatientIdentifierType(2);
		nonRetiredRequiredType.setRequired(true);
		dao.savePatientIdentifierType(nonRetiredRequiredType);
		
		List<PatientIdentifierType> patientIdentifierTypes = dao.getPatientIdentifierTypes(null, null, null, null);
		
		Assert.assertArrayEquals(new Object[] { nonRetiredRequiredType, nonRetiredNonRequiredType1,
		        nonRetiredNonRequiredType2 }, patientIdentifierTypes.toArray());
	}
	
	/**
	 * @see PatientDAO#getPatientIdentifierTypes(String, String, Boolean, Boolean)
	 */
	@Test
	public void getPatientIdentifierTypes_shouldReturnNonRetiredPatientIdentifierTypes_OrderedByRequiredAndName() {
		PatientIdentifierType openMRSIdNumber = dao.getPatientIdentifierType(1);
		
		PatientIdentifierType oldIdNumber = dao.getPatientIdentifierType(2);
		oldIdNumber.setRequired(true);
		dao.savePatientIdentifierType(oldIdNumber);
		
		PatientIdentifierType nationalIdNo = dao.getPatientIdentifierType(5);
		oldIdNumber.setRequired(true);
		dao.savePatientIdentifierType(nationalIdNo);
		
		PatientIdentifierType socialSecNumber = dao.getPatientIdentifierType(4);
		socialSecNumber.setName("ASecurityNumber");
		socialSecNumber.setRequired(true);
		socialSecNumber.setRetired(false);
		dao.savePatientIdentifierType(socialSecNumber);
		
		List<PatientIdentifierType> patientIdentifierTypes = dao.getPatientIdentifierTypes(null, null, null, null);
		
		Assert.assertArrayEquals(new Object[] { socialSecNumber, oldIdNumber, openMRSIdNumber, nationalIdNo },
		    patientIdentifierTypes.toArray());
	}
	
	/**
	 * @see PatientDAO#getPatientIdentifierTypes(String, String, Boolean, Boolean)
	 */
	@Test
	public void getPatientIdentifierTypes_shouldReturnNonRetiredPatientIdentifierTypes_OrderedByRequiredNameAndTypeId() {
		PatientIdentifierType openMRSIdNumber = dao.getPatientIdentifierType(1);
		openMRSIdNumber.setName("IdNumber");
		openMRSIdNumber.setRequired(true);
		dao.savePatientIdentifierType(openMRSIdNumber);
		
		PatientIdentifierType oldIdNumber = dao.getPatientIdentifierType(2);
		oldIdNumber.setName("IdNumber");
		oldIdNumber.setRequired(true);
		dao.savePatientIdentifierType(oldIdNumber);
		
		PatientIdentifierType socialSecNumber = dao.getPatientIdentifierType(4);
		socialSecNumber.setRequired(true);
		socialSecNumber.setRetired(false);
		dao.savePatientIdentifierType(socialSecNumber);
		
		PatientIdentifierType nationalIdNo = dao.getPatientIdentifierType(5);
		oldIdNumber.setName("IdNumber");
		oldIdNumber.setRequired(true);
		dao.savePatientIdentifierType(nationalIdNo);
		
		List<PatientIdentifierType> patientIdentifierTypes = dao.getPatientIdentifierTypes(null, null, null, null);
		
		Assert.assertArrayEquals(new Object[] { openMRSIdNumber, oldIdNumber, socialSecNumber, nationalIdNo },
		    patientIdentifierTypes.toArray());
	}
	
	/**
	 * @see PatientDAO#getPatients(String, String, java.util.List, boolean, Integer,
	 *      Integer, boolean)
	 */
	@Test
	public void getPatients_shouldNotMatchVoidedPatients() {
		List<Patient> patients = dao.getPatients("Hornblower3", 0, 11);
		Assert.assertEquals(1, patients.size());
		
		Patient patient = patients.get(0);
		patient.setVoided(true);
		dao.savePatient(patient);

		updateSearchIndex();

		patients = dao.getPatients("Hornblower3", 0, 11);
		Assert.assertEquals(0, patients.size());
	}
	
	/**
	 * @see PatientDAO#getPatients(String, String, java.util.List, boolean, Integer,
	 *      Integer, boolean)
	 */
	@Test
	public void getPatients_shouldNotMatchVoidedPatientNames() {
		List<Patient> patients = dao.getPatients("Oloo", 0, 11);
		Assert.assertEquals(1, patients.size());
		
		Patient patient = patients.get(0);
		
		Set<PersonName> names = patient.getNames();
		
		for (PersonName name : names) {
			name.setVoided(true);
		}

		updateSearchIndex();

		dao.savePatient(patient);
		patients = dao.getPatients("Oloo", 0, 11);
		Assert.assertEquals(0, patients.size());
	}
	
	/**
	 * @see HibernatePatientDAO#getPatients(String, String, java.util.List, boolean, Integer, Integer, boolean)
	 */
	@Test
	public void getPatients_shouldNotMatchVoidedPatients_SignatureNo1() {
		List<Patient> patients = dao.getPatients("Hornblower3", 0, 11);
		Assert.assertEquals(1, patients.size());
		
		Patient patient = patients.get(0);
		patient.setVoided(true);
		dao.savePatient(patient);

		updateSearchIndex();

		patients = dao.getPatients("Hornblower3", 0, 11);
		Assert.assertEquals(0, patients.size());
	}
	
	/**
	 * @see HibernatePatientDAO#getPatients(String, String, java.util.List, boolean, Integer, Integer, boolean)
	 */
	@Test
	public void getPatients_shouldNotMatchVoidedPatientNames_SignatureNo1() {
		List<Patient> patients = dao.getPatients("Oloo", 0, 11);
		Assert.assertEquals(1, patients.size());
		
		Patient patient = patients.get(0);

		Set<PersonName> names = patient.getNames();
		
		for (PersonName name : names) {
			name.setVoided(true);
		}
		
		dao.savePatient(patient);

		updateSearchIndex();

		patients = dao.getPatients("Oloo", 0, 11);
		Assert.assertEquals(0, patients.size());
	}
	
	/**
	 * @see HibernatePatientDAO#getPatients(String, String, java.util.List, boolean, Integer, Integer, boolean)
	 */
	@Test
	public void getPatients_shouldGetPatientByGivenName_SignatureNo1() {
		List<Patient> patients = dao.getPatients("Bilbo Odilon", 0, 11);
		
		Assert.assertEquals(1, patients.size());
		Assert.assertEquals("Bilbo Odilon", patients.get(0).getGivenName());
	}
	
	/**
	 * @see HibernatePatientDAO#getPatients(String, String, java.util.List, boolean, Integer, Integer, boolean)
	 */
	@Test
	public void getPatients_shouldGetPatientByMiddleName_SignatureNo1() {
		List<Patient> patients = dao.getPatients("B.", 0, 11);
		
		Assert.assertEquals(1, patients.size());
		Assert.assertEquals("Bilbo Odilon", patients.get(0).getGivenName());
	}
	
	/**
	 * @see HibernatePatientDAO#getPatients(String, String, java.util.List, boolean, Integer, Integer, boolean)
	 */
	@Test
	public void getPatients_shouldGetPatientByFamilyName_SignatureNo1() {
		List<Patient> patients = dao.getPatients("Baggins", 0, 11);
		
		Assert.assertEquals(2, patients.size());
		Assert.assertEquals("Baggins", patients.get(0).getFamilyName());
		Assert.assertEquals("Baggins", patients.get(1).getFamilyName());
		Assert.assertFalse(patients.get(0).getGivenName().equalsIgnoreCase(patients.get(1).getGivenName()));
	}
	
	/**
	 * @see HibernatePatientDAO#getPatients(String, String, java.util.List, boolean, Integer, Integer, boolean)
	 */
	@Test
	public void getPatients_shouldGetPatientByFamily2Name_SignatureNo1() {
		List<Patient> patients = dao.getPatients("Senior", 0, 11);
		
		Assert.assertEquals(2, patients.size());
		Assert.assertEquals("Bilbo Odilon", patients.get(0).getGivenName());
	}
	
	/**
	 * @see HibernatePatientDAO#getPatients(String, String, java.util.List, boolean, Integer, Integer, boolean)
	 */
	@Test
	public void getPatients_shouldGetPatientByWholeName_SignatureNo1() {
		List<Patient> patients = dao.getPatients("Bilbo Odilon B. Baggins Senior", 0, 11);
		
		Assert.assertEquals(1, patients.size());
		Assert.assertEquals("Bilbo Odilon", patients.get(0).getGivenName());
	}
	
	/**
	 * @see HibernatePatientDAO#getPatients(String, String, java.util.List, boolean, Integer, Integer, boolean)
	 */
	@Test
	public void getPatients_shouldNotGetPatientByNonexistingSingleName_SignatureNo1() {
		List<Patient> patients = dao.getPatients("Peter", 0, 11);
		
		Assert.assertEquals(0, patients.size());
	}
	
	/**
	 * @see HibernatePatientDAO#getPatients(String, String, java.util.List, boolean, Integer, Integer, boolean)
	 */
	@Test
	public void getPatients_shouldNotGetPatientByNonexistingNameParts_SignatureNo1() {
		List<Patient> patients = dao.getPatients("Sam Gamdschie Eldest", 0, 11);
		
		Assert.assertEquals(0, patients.size());
	}
	
	/**
	 * @see HibernatePatientDAO#getPatients(String, String, java.util.List, boolean, Integer, Integer, boolean)
	 */
	@Test
	public void getPatients_shouldNotGetPatientByMixOfExistingAndNonexistingNameParts_SignatureNo1() {
		List<Patient> patients = dao.getPatients("Bilbo Odilon X. Baggins", 0, 11);
		
		Assert.assertEquals(0, patients.size());
	}
	
	/**
	 * @see HibernatePatientDAO#getPatients(String, String, java.util.List, boolean, Integer, Integer, boolean)
	 */
	@Test
	public void getPatients_shouldNotGetPatientByVoidedName_SignatureNo1() {
		List<Patient> patients = dao.getPatients("voided-delta", 0, 11);
		
		Assert.assertEquals(0, patients.size());
	}
	
	/**
	 * @see HibernatePatientDAO#getPatients(String, String, java.util.List, boolean, Integer, Integer, boolean)
	 */
	@Test
	public void getPatients_shouldNotGetPatientsByEmptyName_SignatureNo1() {
		List<Patient> patients = dao.getPatients("", 0, 11);
		
		Assert.assertEquals(0, patients.size());
	}
	
	/**
	 * @see HibernatePatientDAO#getPatients(String, String, java.util.List, boolean, Integer, Integer, boolean)
	 */
	@Test
	public void getPatients_shouldNotGetPatientsByNullName_SignatureNo1() {
		List<Patient> patients = dao.getPatients(null, 0, 11);
		
		Assert.assertEquals(0, patients.size());
	}
	
	/**
	 * @see HibernatePatientDAO#getPatients(String, String, java.util.List, boolean, Integer, Integer, boolean)
	 */
	@Test
	public void getPatients_shouldGetPatientByShortGivenName_SignatureNo1() {
		List<Patient> patients = dao.getPatients("al", 0, 11);
		
		Assert.assertEquals(3, patients.size());
		Assert.assertEquals("al", patients.get(0).getGivenName());
		Assert.assertEquals("al", patients.get(1).getGivenName());
		Assert.assertFalse(patients.get(0).getMiddleName().equalsIgnoreCase(patients.get(1).getMiddleName()));
	}
	
	/**
	 * @see HibernatePatientDAO#getPatients(String, String, java.util.List, boolean, Integer, Integer, boolean)
	 */
	@Test
	public void getPatients_shouldGetPatientByShortMiddleName_SignatureNo1() {
		List<Patient> patients = dao.getPatients("ec", 0, 11);
		
		Assert.assertEquals(1, patients.size());
		Assert.assertEquals("ec", patients.get(0).getMiddleName());
	}
	
	/**
	 * @see HibernatePatientDAO#getPatients(String, String, java.util.List, boolean, Integer, Integer, boolean)
	 */
	@Test
	public void getPatients_shouldGetPatientByShortFamilyName_SignatureNo1() {
		List<Patient> patients = dao.getPatients("ki", 0, 11);
		
		Assert.assertEquals(2, patients.size());
		Assert.assertEquals("ki", patients.get(0).getFamilyName());
		Assert.assertEquals("ki", patients.get(1).getFamilyName());
		Assert.assertFalse(patients.get(0).getMiddleName().equalsIgnoreCase(patients.get(1).getMiddleName()));
	}
	
	/**
	 * @see HibernatePatientDAO#getPatients(String, String, java.util.List, boolean, Integer, Integer, boolean)
	 */
	@Test
	public void getPatients_shouldGetPatientByShortFamily2Name_SignatureNo1() {
		List<Patient> patients = dao.getPatients("os", 0, 11);
		
		Assert.assertEquals(1, patients.size());
		Assert.assertEquals("br", patients.get(0).getGivenName());
	}
	
	/**
	 * @see HibernatePatientDAO#getPatients(String, String, java.util.List, boolean, Integer, Integer, boolean)
	 */
	@Test
	public void getPatients_shouldGetPatientByWholeNameMadeUpOfShortNames_SignatureNo1() {
		List<Patient> patients = dao.getPatients("br fo ki os", 0, 11);
		
		Assert.assertEquals(1, patients.size());
		Assert.assertEquals("fo", patients.get(0).getMiddleName());
	}
	
	/**
	 * @see HibernatePatientDAO#getPatients(String, String, java.util.List, boolean, Integer, Integer, boolean)
	 */
	@Test
	public void getPatients_shouldGetPatientsByMultipleShortNameParts_SignatureNo1() {
		List<Patient> patients = dao.getPatients("al mi", 0, 11);
		
		Assert.assertEquals(1, patients.size());
		Assert.assertEquals("ec", patients.get(0).getMiddleName());
	}
	
	/**
	 * @see HibernatePatientDAO#getPatients(String, String, java.util.List, boolean, Integer, Integer, boolean)
	 */
	@Test
	public void getPatients_shouldNotGetPatientByNonexistingSingleShortName_SignatureNo1() {
		List<Patient> patients = dao.getPatients("xy", 0, 11);
		
		Assert.assertEquals(0, patients.size());
	}
	
	/**
	 * @see HibernatePatientDAO#getPatients(String, String, java.util.List, boolean, Integer, Integer, boolean)
	 */
	@Test
	public void getPatients_shouldNotGetPatientByNonexistingShortNameParts_SignatureNo1() {
		List<Patient> patients = dao.getPatients("xy yz za", 0, 11);
		
		Assert.assertEquals(0, patients.size());
	}
	
	/**
	 * @see HibernatePatientDAO#getPatients(String, String, java.util.List, boolean, Integer, Integer, boolean)
	 */
	@Test
	public void getPatients_shouldNotGetPatientByMixOfExistingAndNonexistingShortNameParts_SignatureNo1() {
		List<Patient> patients = dao.getPatients("xy yz al", 0, 11);
		
		Assert.assertEquals(0, patients.size());
	}
	
	/**
	 * @see HibernatePatientDAO#getPatients(String, String, java.util.List, boolean, Integer, Integer, boolean)
	 */
	@Test
	public void getPatients_shouldNotGetPatientByVoidedShortName_SignatureNo1() {
		List<Patient> patients = dao.getPatients("vd", 0, 11);
		
		Assert.assertEquals(0, patients.size());
	}
	
	/**
	 * @see HibernatePatientDAO#getPatients(String, String, java.util.List, boolean, Integer, Integer, boolean)
	 */
	@Test
	public void getPatients_shouldGetPatientsWithMatchModeStart_SignatureNo1() {
		String oldPropertyValue = globalPropertiesTestHelper.setGlobalProperty(
		    OpenmrsConstants.GLOBAL_PROPERTY_PATIENT_SEARCH_MATCH_MODE,
		    OpenmrsConstants.GLOBAL_PROPERTY_PATIENT_SEARCH_MATCH_START);
		
		List<Patient> patients = dao.getPatients("Bagg", 0, 11);
		
		Assert.assertEquals(2, patients.size());
		Assert.assertEquals("Baggins", patients.get(0).getFamilyName());
		Assert.assertEquals("Baggins", patients.get(1).getFamilyName());
		Assert.assertFalse(patients.get(0).getGivenName().equalsIgnoreCase(patients.get(1).getGivenName()));
		
		if (oldPropertyValue != null) {
			globalPropertiesTestHelper.setGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_PATIENT_SEARCH_MATCH_MODE,
			    oldPropertyValue);
		} else {
			globalPropertiesTestHelper.purgeGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_PATIENT_SEARCH_MATCH_MODE);
		}
	}
	
	/**
	 * @see HibernatePatientDAO#getPatients(String, String, java.util.List, boolean, Integer, Integer, boolean)
	 */
	@Test
	public void getPatients_shouldGetPatientsWithMatchModeAnywhere_SignatureNo1() {
		String oldPropertyValue = globalPropertiesTestHelper.setGlobalProperty(
		    OpenmrsConstants.GLOBAL_PROPERTY_PATIENT_SEARCH_MATCH_MODE,
		    OpenmrsConstants.GLOBAL_PROPERTY_PATIENT_SEARCH_MATCH_ANYWHERE);
		
		List<Patient> patients = dao.getPatients("aggins", 0, 11);
		
		Assert.assertEquals(2, patients.size());
		Assert.assertEquals("Baggins", patients.get(0).getFamilyName());
		Assert.assertEquals("Baggins", patients.get(1).getFamilyName());
		Assert.assertFalse(patients.get(0).getGivenName().equalsIgnoreCase(patients.get(1).getGivenName()));
		
		if (oldPropertyValue != null) {
			globalPropertiesTestHelper.setGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_PATIENT_SEARCH_MATCH_MODE,
			    oldPropertyValue);
		} else {
			globalPropertiesTestHelper.purgeGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_PATIENT_SEARCH_MATCH_MODE);
		}
	}
	
	/**
	 * @see HibernatePatientDAO#getPatients(String, String, java.util.List, boolean, Integer, Integer, boolean)
	 */
	@Test
	public void getPatients_shouldNotGetPatientsWithMatchModeStart_SignatureNo1() {
		String oldPropertyValue = globalPropertiesTestHelper.setGlobalProperty(
		    OpenmrsConstants.GLOBAL_PROPERTY_PATIENT_SEARCH_MATCH_MODE,
		    OpenmrsConstants.GLOBAL_PROPERTY_PATIENT_SEARCH_MATCH_START);
		
		dao.getPatients("xyz", 0, 11);
		
		if (oldPropertyValue != null) {
			globalPropertiesTestHelper.setGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_PATIENT_SEARCH_MATCH_MODE,
			    oldPropertyValue);
		} else {
			globalPropertiesTestHelper.purgeGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_PATIENT_SEARCH_MATCH_MODE);
		}
	}
	
	/**
	 * @see HibernatePatientDAO#getPatients(String, String, java.util.List, boolean, Integer, Integer, boolean)
	 */
	@Test
	public void getPatients_shouldNotGetPatientsWithMatchModeAnywhere_SignatureNo1() {
		String oldPropertyValue = globalPropertiesTestHelper.setGlobalProperty(
		    OpenmrsConstants.GLOBAL_PROPERTY_PATIENT_SEARCH_MATCH_MODE,
		    OpenmrsConstants.GLOBAL_PROPERTY_PATIENT_SEARCH_MATCH_ANYWHERE);
		
		List<Patient> patients = dao.getPatients("xyz", 0, 11);
		
		Assert.assertEquals(0, patients.size());
		
		if (oldPropertyValue != null) {
			globalPropertiesTestHelper.setGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_PATIENT_SEARCH_MATCH_MODE,
			    oldPropertyValue);
		} else {
			globalPropertiesTestHelper.purgeGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_PATIENT_SEARCH_MATCH_MODE);
		}
	}
	
	/**
	 * @see HibernatePatientDAO#getPatients(String, String, java.util.List, boolean, Integer, Integer, boolean)
	 */
	@Test
	public void getPatients_shouldGetPatientByIdentifier_SignatureNo1() {
		List<Patient> patients = dao.getPatients("42-42-42", 0, 11);
		
		Assert.assertEquals(1, patients.size());
		Assert.assertEquals("Bilbo Odilon", patients.get(0).getGivenName());
	}
	
	/**
	 * @see HibernatePatientDAO#getPatients(String, String, java.util.List, boolean, Integer, Integer, boolean)
	 */
	@Test
	public void getPatients_shouldNotGetPatientByNonexistingIdentifier_SignatureNo1() {
		List<Patient> patients = dao.getPatients(null, 0, 11);
		
		Assert.assertEquals(0, patients.size());
	}
	
	/**
	 * @see HibernatePatientDAO#getPatients(String, String, java.util.List, boolean, Integer, Integer, boolean)
	 */
	@Test
	public void getPatients_shouldNotGetPatientByVoidedIdentifier_SignatureNo1() {
		List<Patient> patients = dao.getPatients(null, 0, 11);
		
		Assert.assertEquals(0, patients.size());
	}
	
	/**
	 * @see HibernatePatientDAO#getPatients(String, String, java.util.List, boolean, Integer, Integer, boolean)
	 */
	@Test
	public void getPatients_shouldNotGetPatientByEmptyIdentifier_SignatureNo1() {
		List<Patient> patients = dao.getPatients(null, 0, 11);
		
		Assert.assertEquals(0, patients.size());
	}
	
	/**
	 * @see HibernatePatientDAO#getPatients(String, String, java.util.List, boolean, Integer, Integer, boolean)
	 */
	@Test
	public void getPatients_shouldNotGetPatientByNullIdentifier_SignatureNo1() {
		List<Patient> patients = dao.getPatients(null, 0, 11);
		Assert.assertEquals(0, patients.size());
	}
	
	/**
	 * @see HibernatePatientDAO#getPatients(String, String, java.util.List, boolean, Integer, Integer, boolean)
	 */
	@Test
	public void getPatients_shouldGetPatientBySearchingOnNamesOrIdentifiersAndUsingNameValueAsIdentifierParameter_SignatureNo1()
	{
		List<Patient> patients = dao.getPatients("Bilbo Odilon", 0, 11);
		
		Assert.assertEquals(1, patients.size());
		Assert.assertEquals("Bilbo Odilon", patients.get(0).getGivenName());
	}
	
	/**
	 * @see HibernatePatientDAO#getPatients(String, String, java.util.List, boolean, Integer, Integer, boolean)
	 */
	@Test
	public void getPatients_shouldGetPatientBySearchingOnNamesOrIdentifiersAndUsingIdentifierValueAsNameParameter_SignatureNo1()
	{
		List<Patient> patients = dao.getPatients("42-42-42", 0, 11);
		
		Assert.assertEquals(1, patients.size());
		Assert.assertEquals("Bilbo Odilon", patients.get(0).getGivenName());
	}
	
	/**
	 * @see HibernatePatientDAO#getPatients(String, String, java.util.List, boolean, Integer, Integer, boolean)
	 */
	@Test
	public void getPatients_shouldGetOnePatientByMultipleNameParts_SignatureNo1() {
		List<Patient> patients = dao.getPatients("Bilbo Odilon B.", 0, 11);
		
		Assert.assertEquals(1, patients.size());
		Assert.assertEquals("Bilbo Odilon", patients.get(0).getGivenName());
	}
	
	/**
	 * @see HibernatePatientDAO#getPatients(String, Integer, Integer)
	 */
	@Test
	public void getPatients_shouldNotGetPatientsByEmptyQuery_SignatureNo2() {
		List<Patient> patients = dao.getPatients("", 0, 11);
		Assert.assertEquals(0, patients.size());
	}
	
	/**
	 * @see HibernatePatientDAO#getPatients(String, Integer, Integer)
	 */
	@Test
	public void getPatients_shouldNotGetPatientsByNullQuery_SignatureNo2() {
		List<Patient> patients = dao.getPatients("", 0, 11);
		Assert.assertEquals(0, patients.size());
	}
	
	/**
	 * @see HibernatePatientDAO#getPatients(String, Integer, Integer)
	 */
	@Test
	public void getPatients_shouldGetPatientByGivenName_SignatureNo2() {
		List<Patient> patients = dao.getPatients("Bilbo Odilon", 0, 11);
		
		Assert.assertEquals(1, patients.size());
		Assert.assertEquals("Bilbo Odilon", patients.get(0).getGivenName());
	}
	
	/**
	 * @see HibernatePatientDAO#getPatients(String, Integer, Integer)
	 */
	@Test
	public void getPatients_shouldGetPatientByMiddleName_SignatureNo2() {
		List<Patient> patients = dao.getPatients("B.", 0, 11);
		
		Assert.assertEquals(1, patients.size());
		Assert.assertEquals("Bilbo Odilon", patients.get(0).getGivenName());
	}
	
	/**
	 * @see HibernatePatientDAO#getPatients(String, Integer, Integer)
	 */
	@Test
	public void getPatients_shouldGetPatientByFamilyName_SignatureNo2() {
		List<Patient> patients = dao.getPatients("Baggins", 0, 11);
		
		Assert.assertEquals(2, patients.size());
		Assert.assertEquals("Baggins", patients.get(0).getFamilyName());
		Assert.assertEquals("Baggins", patients.get(1).getFamilyName());
		Assert.assertFalse(patients.get(0).getGivenName().equalsIgnoreCase(patients.get(1).getGivenName()));
	}
	
	/**
	 * @see HibernatePatientDAO#getPatients(String, Integer, Integer)
	 */
	@Test
	public void getPatients_shouldGetPatientByFamily2Name_SignatureNo2() {
		List<Patient> patients = dao.getPatients("Junior", 0, 11);
		
		Assert.assertEquals(1, patients.size());
		Assert.assertEquals("Frodo Ansilon", patients.get(0).getGivenName());
	}
	
	/**
	 * @see HibernatePatientDAO#getPatients(String, Integer, Integer)
	 */
	@Test
	public void getPatients_shouldGetPatientByWholeName_SignatureNo2() {
		List<Patient> patients = dao.getPatients("Bilbo Odilon B. Baggins Senior", 0, 11);
		
		Assert.assertEquals(1, patients.size());
		Assert.assertEquals("Bilbo Odilon", patients.get(0).getGivenName());
	}
	
	/**
	 * @see HibernatePatientDAO#getPatients(String, Integer, Integer)
	 */
	@Test
	public void getPatients_shouldNotGetPatientByNonexistingSingleName_SignatureNo2() {
		List<Patient> patients = dao.getPatients("Peter", 0, 11);
		Assert.assertEquals(0, patients.size());
	}
	
	/**
	 * @see HibernatePatientDAO#getPatients(String, Integer, Integer)
	 */
	@Test
	public void getPatients_shouldNotGetPatientByNonexistingNameParts_SignatureNo2() {
		List<Patient> patients = dao.getPatients("Sam Gamdschie Eldest", 0, 11);
		Assert.assertEquals(0, patients.size());
	}
	
	/**
	 * @see HibernatePatientDAO#getPatients(String, Integer, Integer)
	 */
	@Test
	public void getPatients_shouldNotGetPatientByMixOfExistingAndNonexistingNameParts_SignatureNo2() {
		List<Patient> patients = dao.getPatients("Bilbo Odilon X. Baggins", 0, 11);
		Assert.assertEquals(0, patients.size());
	}
	
	/**
	 * @see HibernatePatientDAO#getPatients(String, Integer, Integer)
	 */
	@Test
	public void getPatients_shouldNotGetPatientByVoidedName_SignatureNo2() {
		List<Patient> patients = dao.getPatients("voided-delta", 0, 11);
		Assert.assertEquals(0, patients.size());
	}
	
	/**
	 * @see HibernatePatientDAO#getPatients(String, Integer, Integer)
	 */
	@Test
	public void getPatients_shouldGetPatientByShortGivenName_SignatureNo2() {
		List<Patient> patients = dao.getPatients("al", 0, 11);
		
		Assert.assertEquals(3, patients.size());
		Assert.assertEquals("al", patients.get(0).getGivenName());
		Assert.assertEquals("al", patients.get(1).getGivenName());
		Assert.assertFalse(patients.get(0).getMiddleName().equalsIgnoreCase(patients.get(1).getMiddleName()));
	}
	
	/**
	 * @see HibernatePatientDAO#getPatients(String, Integer, Integer)
	 */
	@Test
	public void getPatients_shouldGetPatientByShortMiddleName_SignatureNo2() {
		List<Patient> patients = dao.getPatients("ec", 0, 11);
		
		Assert.assertEquals(1, patients.size());
		Assert.assertEquals("ec", patients.get(0).getMiddleName());
	}
	
	/**
	 * @see HibernatePatientDAO#getPatients(String, Integer, Integer)
	 */
	@Test
	public void getPatients_shouldGetPatientByShortFamilyName_SignatureNo2() {
		List<Patient> patients = dao.getPatients("ki", 0, 11);
		
		Assert.assertEquals(2, patients.size());
		Assert.assertEquals("ki", patients.get(0).getFamilyName());
		Assert.assertEquals("ki", patients.get(1).getFamilyName());
		Assert.assertFalse(patients.get(0).getMiddleName().equalsIgnoreCase(patients.get(1).getMiddleName()));
	}
	
	/**
	 * @see HibernatePatientDAO#getPatients(String, Integer, Integer)
	 */
	@Test
	public void getPatients_shouldGetPatientByShortFamily2Name_SignatureNo2() {
		List<Patient> patients = dao.getPatients("os", 0, 11);
		
		Assert.assertEquals(1, patients.size());
		Assert.assertEquals("br", patients.get(0).getGivenName());
	}
	
	/**
	 * @see HibernatePatientDAO#getPatients(String, Integer, Integer)
	 */
	@Test
	public void getPatients_shouldGetPatientByWholeNameMadeUpOfShortNames_SignatureNo2() {
		List<Patient> patients = dao.getPatients("br fo ki os", 0, 11);
		
		Assert.assertEquals(1, patients.size());
		Assert.assertEquals("fo", patients.get(0).getMiddleName());
	}
	
	/**
	 * @see HibernatePatientDAO#getPatients(String, Integer, Integer)
	 */
	@Test
	public void getPatients_shouldGetPatientsByMultipleShortNameParts_SignatureNo2() {
		List<Patient> patients = dao.getPatients("al mi", 0, 11);
		
		Assert.assertEquals(1, patients.size());
		Assert.assertEquals("ec", patients.get(0).getMiddleName());
	}
	
	/**
	 * @see HibernatePatientDAO#getPatients(String, Integer, Integer)
	 */
	@Test
	public void getPatients_shouldNotGetPatientByNonexistingSingleShortName_SignatureNo2() {
		List<Patient> patients = dao.getPatients("xy", 0, 11);
		
		Assert.assertEquals(0, patients.size());
	}
	
	/**
	 * @see HibernatePatientDAO#getPatients(String, Integer, Integer)
	 */
	@Test
	public void getPatients_shouldNotGetPatientByNonexistingShortNameParts_SignatureNo2() {
		List<Patient> patients = dao.getPatients("xy yz za", 0, 11);
		
		Assert.assertEquals(0, patients.size());
	}
	
	/**
	 * @see HibernatePatientDAO#getPatients(String, Integer, Integer)
	 */
	@Test
	public void getPatients_shouldNotGetPatientByMixOfExistingAndNonexistingShortNameParts_SignatureNo2() {
		List<Patient> patients = dao.getPatients("xy yz al", 0, 11);
		
		Assert.assertEquals(0, patients.size());
	}
	
	/**
	 * @see HibernatePatientDAO#getPatients(String, Integer, Integer)
	 */
	@Test
	public void getPatients_shouldNotGetPatientByVoidedShortName_SignatureNo2() {
		List<Patient> patients = dao.getPatients("vd", 0, 11);
		Assert.assertEquals(0, patients.size());
	}
	
	/**
	 * @see HibernatePatientDAO#getPatients(String, Integer, Integer)
	 */
	@Test
	public void getPatients_shouldGetPatientByIdentifier_SignatureNo2() {
		List<Patient> patients = dao.getPatients("42-42-42", 0, 11);
		Assert.assertEquals(1, patients.size());
		Assert.assertEquals("Bilbo Odilon", patients.get(0).getGivenName());
	}
	
	/**
	 * @see HibernatePatientDAO#getPatients(String, Integer, Integer)
	 */
	@Test
	public void getPatients_shouldNotGetPatientByNonexistingIdentifier_SignatureNo2() {
		List<Patient> patients = dao.getPatients("xy-xy-xy", 0, 11);
		Assert.assertEquals(0, patients.size());
	}
	
	/**
	 * @see HibernatePatientDAO#getPatients(String, Integer, Integer)
	 */
	@Test
	public void getPatients_shouldNotGetPatientByVoidedIdentifier_SignatureNo2() {
		List<Patient> patients = dao.getPatients("voided-42", 0, 11);
		Assert.assertEquals(0, patients.size());
	}
	
	/**
	 * @see HibernatePatientDAO#getPatients(String, Integer, Integer)
	 */
	@Test
	public void getPatients_shouldGetNoPatientByNonexistingAttribute_SignatureNo2() {
		Assert.assertFalse(personAttributeHelper.personAttributeExists("Wizard"));
		
		List<Patient> patients = dao.getPatients("Wizard", 0, 11);
		Assert.assertEquals(0, patients.size());
	}
	
	/**
	 * @see HibernatePatientDAO#getPatients(String, Integer, Integer)
	 */
	@Test
	public void getPatients_shouldGetNoPatientByNonsearchableAttribute_SignatureNo2() {
		Assert.assertTrue(personAttributeHelper.nonSearchablePersonAttributeExists("Mushroom pie"));
		List<Patient> patients = dao.getPatients("Mushroom pie", 0, 11);
		Assert.assertEquals(0, patients.size());
	}
	
	/**
	 * @see HibernatePatientDAO#getPatients(String, Integer, Integer)
	 */
	@Test
	public void getPatients_shouldGetNoPatientByVoidedAttribute_SignatureNo2() {
		Assert.assertTrue(personAttributeHelper.voidedPersonAttributeExists("Master thief"));
		List<Patient> patients = dao.getPatients("Master thief", 0, 11);
		Assert.assertEquals(0, patients.size());
	}
	
	/**
	 * @see HibernatePatientDAO#getPatients(String, Integer, Integer)
	 */
	@Test
	public void getPatients_shouldGetOnePatientByAttribute_SignatureNo2() {
		globalPropertiesTestHelper.setGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_PERSON_ATTRIBUTE_SEARCH_MATCH_MODE,
		    OpenmrsConstants.GLOBAL_PROPERTY_PERSON_ATTRIBUTE_SEARCH_MATCH_ANYWHERE);
		Assert.assertTrue(personAttributeHelper.personAttributeExists("Story teller"));
		List<Patient> patients = dao.getPatients("Story teller", 0, 11);
		Assert.assertEquals(1, patients.size());
	}
	
	/**
	 * @see HibernatePatientDAO#getPatients(String, Integer, Integer)
	 */
	@Test
	public void getPatients_shouldGetOnePatientByRandomCaseAttribute_SignatureNo2() {
		globalPropertiesTestHelper.setGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_PERSON_ATTRIBUTE_SEARCH_MATCH_MODE,
		    OpenmrsConstants.GLOBAL_PROPERTY_PERSON_ATTRIBUTE_SEARCH_MATCH_ANYWHERE);
		Assert.assertTrue(personAttributeHelper.personAttributeExists("Story teller"));
		List<Patient> patients = dao.getPatients("STORY teller", 0, 11);
		Assert.assertEquals(1, patients.size());
	}
	
	/**
	 * @see HibernatePatientDAO#getPatients(String, Integer, Integer)
	 */
	@Test
	public void getPatients_shouldNotGetPatientsBySearchingForNonvoidedAndVoidedAttribute_SignatureNo2() {
		Assert.assertTrue(personAttributeHelper.personAttributeExists("Story teller"));
		Assert.assertFalse(personAttributeHelper.voidedPersonAttributeExists("Story teller"));
		Assert.assertTrue(personAttributeHelper.voidedPersonAttributeExists("Master thief"));
		List<Patient> patients = dao.getPatients("Story Thief", 0, 11);
		Assert.assertEquals(0, patients.size());
	}
	
	/**
	 * @see HibernatePatientDAO#getPatients(String, Integer, Integer)
	 */
	@Test
	public void getPatients_shouldGetMultiplePatientsBySingleAttribute_SignatureNo2() {
		globalPropertiesTestHelper.setGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_PERSON_ATTRIBUTE_SEARCH_MATCH_MODE,
		    OpenmrsConstants.GLOBAL_PROPERTY_PERSON_ATTRIBUTE_SEARCH_MATCH_ANYWHERE);
		Assert.assertTrue(personAttributeHelper.personAttributeExists("Senior ring bearer"));
		List<Patient> patients = dao.getPatients("Senior ring bearer", 0, 11);
		
		Assert.assertEquals(2, patients.size());
		Assert.assertEquals("Baggins", patients.get(0).getFamilyName());
		Assert.assertEquals("Baggins", patients.get(1).getFamilyName());
		Assert.assertFalse(patients.get(0).getGivenName().equalsIgnoreCase(patients.get(1).getGivenName()));
	}
	
	/**
	 * @see HibernatePatientDAO#getPatients(String, Integer, Integer)
	 */
	@Test
	public void getPatients_shouldNotGetPatientsByMultipleAttributes_SignatureNo2() {
		Assert.assertTrue(personAttributeHelper.searchablePersonAttributeExists("Story teller"));
		Assert.assertTrue(personAttributeHelper.nonVoidedPersonAttributeExists("Story teller"));
		
		Assert.assertTrue(personAttributeHelper.searchablePersonAttributeExists("Senior ring bearer"));
		Assert.assertTrue(personAttributeHelper.nonVoidedPersonAttributeExists("Senior ring bearer"));
		
		List<Patient> patients = dao.getPatients("Story bearer", 0, 11);
		Assert.assertEquals(0, patients.size());
	}
	
	/**
	 * @see HibernatePatientDAO#getPatients(String, Integer, Integer)
	 */
	@Test
	public void getPatients_shouldFindElevenOutOfElevenPatients_SignatureNo2() {
		List<Patient> firstFourPatients = dao.getPatients("Saruman", 0, 11);
		Assert.assertEquals(11, firstFourPatients.size());
	}
	
	/**
	 * @see HibernatePatientDAO#getPatients(String, Integer, Integer)
	 */
	@Test
	public void getPatients_shouldFindTheFirstFourOutOfElevenPatients_SignatureNo2() {
		List<Patient> firstFourPatients = dao.getPatients("Saruman", 0, 4);
		Assert.assertEquals(4, firstFourPatients.size());
	}
	
	/**
	 * @see HibernatePatientDAO#getPatients(String, Integer, Integer)
	 */
	@Test
	public void getPatients_shouldFindTheNextFourOutOfElevenPatients_SignatureNo2() {
		List<Patient> firstFourPatients = dao.getPatients("Saruman", 0, 4);
		Assert.assertEquals(4, firstFourPatients.size());
		
		List<Patient> nextFourPatients = dao.getPatients("Saruman", 4, 4);
		Assert.assertEquals(4, nextFourPatients.size());
		
		for (Patient patient : nextFourPatients) {
			Assert.assertFalse(firstFourPatients.contains(patient));
		}
	}
	
	/**
	 * @see HibernatePatientDAO#getPatients(String, Integer, Integer)
	 */
	@Test
	public void getPatients_shouldFindTheRemainingThreeOutOfElevenPatients_SignatureNo2() {
		List<Patient> firstFourPatients = dao.getPatients("Saruman", 0, 4);
		Assert.assertEquals(4, firstFourPatients.size());
		
		List<Patient> nextFourPatients = dao.getPatients("Saruman", 4, 4);
		Assert.assertEquals(4, nextFourPatients.size());
		
		List<Patient> lastThreePatients = dao.getPatients("Saruman", 8, 4);
		Assert.assertEquals(3, lastThreePatients.size());
		
		for (Patient patient : lastThreePatients) {
			Assert.assertFalse(firstFourPatients.contains(patient));
			Assert.assertFalse(nextFourPatients.contains(patient));
		}
	}
	
	/**
	 * @see HibernatePatientDAO#getPatients(String, Integer, Integer)
	 */
	@Test
	public void getPatients_shouldFindPatientsWithNullAsStart_SignatureNo2() {
		List<Patient> patients = dao.getPatients("Saruman", null, 11);
		Assert.assertEquals(11, patients.size());
	}
	
	/**
	 * @see HibernatePatientDAO#getPatients(String, Integer, Integer)
	 */
	@Test
	public void getPatients_shouldFindPatientsWithNegativeStart_SignatureNo2() {
		List<Patient> patients = dao.getPatients("Saruman", -7, 11);
		Assert.assertEquals(11, patients.size());
	}
	
	/**
	 * @see HibernatePatientDAO#getPatients(String, Integer, Integer)
	 */
	@Test
	public void getPatients_shouldFindPatientsWithNullAsLength_SignatureNo2() {
		List<Patient> patients = dao.getPatients("Saruman", 0, null);
		Assert.assertEquals(11, patients.size());
	}
	
	/**
	 * @see HibernatePatientDAO#getPatients(String, Integer, Integer)
	 */
	@Test
	public void getPatients_shouldNotGetPatientsByZeroLength_SignatureNo2() {
		List<Patient> patients = dao.getPatients("Saruman", 0, 0);
		Assert.assertEquals(0, patients.size());
	}
	
	/**
	 * @see HibernatePatientDAO#getPatients(String, Integer, Integer)
	 */
	@Test
	public void getPatients_shouldNotGetPatientsByNegativeLength_SignatureNo2() {
		List<Patient> patients = dao.getPatients("Saruman", 0, -7);
		Assert.assertEquals(0, patients.size());
	}
	
	/**
	 * @see HibernatePatientDAO#getPatients(String, Integer, Integer)
	 */
	@Test
	public void getPatients_shouldFindPatientsWithExcessiveLength_SignatureNo2() {
		List<Patient> patients = dao.getPatients("Saruman", 0, HibernatePersonDAO.getMaximumSearchResults() + 42);
		Assert.assertEquals(11, patients.size());
	}
	
	/**
	 * @see HibernatePatientDAO#getPatients(String, Integer, Integer)
	 */
	@Test
	public void getPatients_shouldNotGetExcessPatients() {
		List<Patient> patients = dao.getPatients("alpha", 0, 1);
		Assert.assertEquals(1,patients.size());
	}
	
	/**
	 * @see HibernatePatientDAO#getPatients(String, Integer, Integer)
	 */
	@Test
	public void getPatients_shouldReturnPatientsByStartAndLengthPassed() {
		List<Patient> patients = dao.getPatients("alpha", 1, 1);
		Assert.assertEquals(1,patients.size());
	}
	
	/**
	 * @see HibernatePatientDAO#getPatients(String, Integer, Integer)
	 */
	@Test
	public void getPatients_shouldNotGetExcessPatientsOnIdentifierAndNameMatch() {
		Patient patient = patientService.getPatient(2);
		PatientIdentifier patientIdentifier = new PatientIdentifier("alpha", patientService.getPatientIdentifierType(5), Context
				.getLocationService().getLocation(1));
		patient.addIdentifier(patientIdentifier);
		patientService.savePatient(patient);
		updateSearchIndex();
		
		List<Patient> patients = dao.getPatients("alpha", 0 , null);
		Assert.assertEquals(patient, patients.get(0));
		Assert.assertEquals(3,patients.size());
		
		List<Patient> first_patient = dao.getPatients("alpha", 0, 1);
		Assert.assertEquals(patient, first_patient.get(0));
		Assert.assertEquals(1,first_patient.size());
		
		List<Patient> two_patients_only = dao.getPatients("alpha", 0, 2);
		Assert.assertEquals(2, two_patients_only.size());
		
		List<Patient> second_patient = dao.getPatients("alpha", 1, 1);
		Assert.assertEquals(1, second_patient.size());
	}
	
	/**
	 * @see HibernatePatientDAO#getPatients(String, Integer, Integer)
	 */
	@Test
	public void getPatients_shouldNotGetExcessPatientsOnIdentifierAndAttributeMatch()  {
		globalPropertiesTestHelper.setGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_PERSON_ATTRIBUTE_SEARCH_MATCH_MODE,
				OpenmrsConstants.GLOBAL_PROPERTY_PERSON_ATTRIBUTE_SEARCH_MATCH_ANYWHERE);
		Patient patient = patientService.getPatient(2);
		PatientIdentifier patientIdentifier = new PatientIdentifier("Senior", patientService.getPatientIdentifierType(5), Context
				.getLocationService().getLocation(1));
		patient.addIdentifier(patientIdentifier);
		patientService.savePatient(patient);
		updateSearchIndex();
		
		List<Patient> patients = dao.getPatients("Senior", 0 , null);
		Assert.assertEquals(patient, patients.get(0));
		Assert.assertEquals(3,patients.size());
		
		List<Patient> first_patient = dao.getPatients("Senior", 0, 1);
		Assert.assertEquals(patient, first_patient.get(0));
		Assert.assertEquals(1,first_patient.size());
		
		List<Patient> two_patients_only = dao.getPatients("Senior", 0, 2);
		Assert.assertEquals(2, two_patients_only.size());
		
		List<Patient> second_patient = dao.getPatients("Senior", 1, 1);
		Assert.assertEquals(1, second_patient.size());
	}
	
	/**
	 * @see HibernatePatientDAO#getPatients(String, Integer, Integer)
	 */
	@Test
	public void getPatients_shouldReturnDistinctPatientList_SignatureNo2() {
		Assert.assertTrue(personAttributeHelper.searchablePersonAttributeExists("Cook"));
		
		List<Patient> patientsByName = dao.getPatients("Adalgrim Took Cook", 0, 11);
		Assert.assertEquals(1, patientsByName.size());
		
		List<Patient> patientsByNameOrAttribute = dao.getPatients("Cook", 0, 11);
		Assert.assertEquals(1, patientsByNameOrAttribute.size());
	}
	
	/**
	 * @see HibernatePatientDAO#getPatients(String, Integer, Integer)
	 */
	@Test
	public void getPatients_shouldNotMatchVoidedPatients_SignatureNo2() {
		List<Patient> patients = dao.getPatients("Meriadoc", 0, 11);
		Assert.assertEquals(0, patients.size());
	}
	
	/**
	 * @see HibernatePatientDAO#getPatients(String, Integer, Integer)
	 */
	@Test
	public void getPatients_shouldGetPatientsWithMatchModeStart_SignatureNo2() {
		String oldPropertyValue = globalPropertiesTestHelper.setGlobalProperty(
		    OpenmrsConstants.GLOBAL_PROPERTY_PATIENT_SEARCH_MATCH_MODE,
		    OpenmrsConstants.GLOBAL_PROPERTY_PATIENT_SEARCH_MATCH_START);
		
		List<Patient> patients = dao.getPatients("Bagg", 0, 11);
		
		Assert.assertEquals(2, patients.size());
		Assert.assertEquals("Baggins", patients.get(0).getFamilyName());
		Assert.assertEquals("Baggins", patients.get(1).getFamilyName());
		Assert.assertFalse(patients.get(0).getGivenName().equalsIgnoreCase(patients.get(1).getGivenName()));
		
		if (oldPropertyValue != null) {
			globalPropertiesTestHelper.setGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_PATIENT_SEARCH_MATCH_MODE,
			    oldPropertyValue);
		} else {
			globalPropertiesTestHelper.purgeGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_PATIENT_SEARCH_MATCH_MODE);
		}
	}
	
	/**
	 * @see HibernatePatientDAO#getPatients(String, Integer, Integer)
	 */
	@Test
	public void getPatients_shouldGetPatientsWithMatchModeAnywhere_SignatureNo2() {
		String oldPropertyValue = globalPropertiesTestHelper.setGlobalProperty(
		    OpenmrsConstants.GLOBAL_PROPERTY_PATIENT_SEARCH_MATCH_MODE,
		    OpenmrsConstants.GLOBAL_PROPERTY_PATIENT_SEARCH_MATCH_ANYWHERE);
		
		List<Patient> patients = dao.getPatients("aggins", 0, 11);
		
		Assert.assertEquals(2, patients.size());
		Assert.assertEquals("Baggins", patients.get(0).getFamilyName());
		Assert.assertEquals("Baggins", patients.get(1).getFamilyName());
		Assert.assertFalse(patients.get(0).getGivenName().equalsIgnoreCase(patients.get(1).getGivenName()));
		
		if (oldPropertyValue != null) {
			globalPropertiesTestHelper.setGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_PATIENT_SEARCH_MATCH_MODE,
			    oldPropertyValue);
		} else {
			globalPropertiesTestHelper.purgeGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_PATIENT_SEARCH_MATCH_MODE);
		}
	}
	
	/**
	 * @see HibernatePatientDAO#getPatients(String, Integer, Integer)
	 */
	@Test
	public void getPatients_shouldNotGetPatientsWithMatchModeStart_SignatureNo2() {
		String oldPropertyValue = globalPropertiesTestHelper.setGlobalProperty(
		    OpenmrsConstants.GLOBAL_PROPERTY_PATIENT_SEARCH_MATCH_MODE,
		    OpenmrsConstants.GLOBAL_PROPERTY_PATIENT_SEARCH_MATCH_START);
		
		dao.getPatients("xyz", 0, 11);
		
		if (oldPropertyValue != null) {
			globalPropertiesTestHelper.setGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_PATIENT_SEARCH_MATCH_MODE,
			    oldPropertyValue);
		} else {
			globalPropertiesTestHelper.purgeGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_PATIENT_SEARCH_MATCH_MODE);
		}
	}
	
	/**
	 * @see HibernatePatientDAO#getPatients(String, Integer, Integer)
	 */
	@Test
	public void getPatients_shouldNotGetPatientsWithMatchModeAnywhere_SignatureNo2() {
		String oldPropertyValue = globalPropertiesTestHelper.setGlobalProperty(
		    OpenmrsConstants.GLOBAL_PROPERTY_PATIENT_SEARCH_MATCH_MODE,
		    OpenmrsConstants.GLOBAL_PROPERTY_PATIENT_SEARCH_MATCH_ANYWHERE);
		
		List<Patient> patients = dao.getPatients("xyz", 0, 11);
		
		Assert.assertEquals(0, patients.size());
		
		if (oldPropertyValue != null) {
			globalPropertiesTestHelper.setGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_PATIENT_SEARCH_MATCH_MODE,
			    oldPropertyValue);
		} else {
			globalPropertiesTestHelper.purgeGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_PATIENT_SEARCH_MATCH_MODE);
		}
	}
	
	/**
	 * @see HibernatePatientDAO#getCountOfPatients(String, String, java.util.List, boolean, boolean)
	 */
	@Test
	public void getCountOfPatients_shouldCountZeroPatientsWhenNameAndIdentifierAndListOfIdentifierTypesAreEmpty_SignatureNo1()
	{
		long patientCount = dao.getCountOfPatients("");
		Assert.assertEquals(0, patientCount);
	}
	
	/**
	 * @see HibernatePatientDAO#getCountOfPatients(String, String, java.util.List, boolean, boolean)
	 */
	@Test
	public void getCountOfPatients_shouldCountZeroPatientsWhenNameAndIdentifierAndListOfIdentifierTypesAreNull_SignatureNo1()
	{
		long patientCount = dao.getCountOfPatients(null);
		Assert.assertEquals(0, patientCount);
	}
	
	/**
	 * @see HibernatePatientDAO#getCountOfPatients(String, String, java.util.List, boolean, boolean)
	 */
	@Test
	public void getCountOfPatients_shouldCountZeroPatientsForNonmatchingQuery_SignatureNo1() {
		long patientCount = dao.getCountOfPatients("a random query value");
		Assert.assertEquals(0, patientCount);
	}
	
	/**
	 * @see HibernatePatientDAO#getCountOfPatients(String, String, java.util.List, boolean, boolean)
	 */
	@Test
	public void getCountOfPatients_shouldNotCountVoidedPatients_SignatureNo1() {
		long patientCount = dao.getCountOfPatients("Meriadoc Brandybuck");
		Assert.assertEquals(0, patientCount);
	}
	
	/**
	 * @see HibernatePatientDAO#getCountOfPatients(String, String, java.util.List, boolean, boolean)
	 */
	@Test
	public void getCountOfPatients_shouldCountSinglePatient_SignatureNo1() {
		long patientCount = dao.getCountOfPatients("Bilbo");
		Assert.assertEquals(1, patientCount);
	}
	
	/**
	 * @see HibernatePatientDAO#getCountOfPatients(String, String, java.util.List, boolean, boolean)
	 */
	@Test
	public void getCountOfPatients_shouldCountMultiplePatients_SignatureNo1() {
		long patientCount = dao.getCountOfPatients("Saruman");
		Assert.assertEquals(11, patientCount);
	}
	
	/**
	 * @see HibernatePatientDAO#getCountOfPatients(String, String, java.util.List, boolean, boolean)
	 */
	@Test
	public void getCountOfPatients_shouldCountPatientsByName_SignatureNo1() {
		long patientCount = dao.getCountOfPatients("Bilbo");
		Assert.assertEquals(1, patientCount);
	}
	
	/**
	 * @see HibernatePatientDAO#getCountOfPatients(String, String, java.util.List, boolean, boolean)
	 */
	@Test
	public void getCountOfPatients_shouldCountPatientsByIdentifier_SignatureNo1() {
		long patientCount = dao.getCountOfPatients("42-42-42");
		Assert.assertEquals(1, patientCount);
	}
	
	/**
	 * @see HibernatePatientDAO#getCountOfPatients(String)
	 */
	@Test
	public void getCountOfPatients_shouldCountZeroPatientsWhenQueryIsEmpty_SignatureNo2() {
		long patientCount = dao.getCountOfPatients("");
		Assert.assertEquals(0, patientCount);
	}
	
	/**
	 * @see HibernatePatientDAO#getCountOfPatients(String)
	 */
	@Test
	public void getCountOfPatients_shouldCountZeroPatientsWhenQueryIsNull_SignatureNo2() {
		long patientCount = dao.getCountOfPatients(null);
		Assert.assertEquals(0, patientCount);
	}
	
	/**
	 * @see HibernatePatientDAO#getCountOfPatients(String)
	 */
	@Test
	public void getCountOfPatients_shouldCountZeroPatientsForNonmatchingQuery_SignatureNo2() {
		long patientCount = dao.getCountOfPatients("a random query value");
		Assert.assertEquals(0, patientCount);
	}
	
	/**
	 * @see HibernatePatientDAO#getCountOfPatients(String)
	 */
	@Test
	public void getCountOfPatients_shouldNotCountVoidedPatients_SignatureNo2() {
		long patientCount = dao.getCountOfPatients("Meriadoc Brandybuck");
		Assert.assertEquals(0, patientCount);
	}
	
	/**
	 * @see HibernatePatientDAO#getCountOfPatients(String)
	 */
	@Test
	public void getCountOfPatients_shouldCountSinglePatient_SignatureNo2() {
		long patientCount = dao.getCountOfPatients("Bilbo");
		Assert.assertEquals(1, patientCount);
	}
	
	/**
	 * @see HibernatePatientDAO#getCountOfPatients(String)
	 */
	@Test
	public void getCountOfPatients_shouldCountMultiplePatients_SignatureNo2() {
		long patientCount = dao.getCountOfPatients("Saruman");
		Assert.assertEquals(11, patientCount);
	}
	
	/**
	 * @see HibernatePatientDAO#getCountOfPatients(String)
	 */
	@Test
	public void getCountOfPatients_shouldCountPatientsByName_SignatureNo2() {
		long patientCount = dao.getCountOfPatients("Saruman");
		Assert.assertEquals(11, patientCount);
	}
	
	/**
	 * @see HibernatePatientDAO#getCountOfPatients(String)
	 */
	@Test
	public void getCountOfPatients_shouldCountPatientsByIdentifier_SignatureNo2() {
		long patientCount = dao.getCountOfPatients("42-42-42");
		Assert.assertEquals(1, patientCount);
	}
	
	/**
	 * @see HibernatePatientDAO#getCountOfPatients(String)
	 */
	@Test
	public void getCountOfPatients_shouldCountPatientsBySearchableAttribute_SignatureNo2() {
		globalPropertiesTestHelper.setGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_PERSON_ATTRIBUTE_SEARCH_MATCH_MODE,
		    OpenmrsConstants.GLOBAL_PROPERTY_PERSON_ATTRIBUTE_SEARCH_MATCH_ANYWHERE);
		long patientCount = dao.getCountOfPatients("Story teller");
		Assert.assertEquals(1, patientCount);
	}
	
	/**
	 * @see HibernatePatientDAO#getPatients(String, Integer, Integer)
	 */
	@Test
	public void getPatients_shouldReturnExactMatchFirst() {
		List<Patient> patients = dao.getPatients("Ben", 0, 11);
		
		Assert.assertEquals(4, patients.size());
		Assert.assertEquals("Ben", patients.get(0).getGivenName());
		Assert.assertEquals("Alan", patients.get(1).getGivenName());
		Assert.assertEquals("Benedict", patients.get(2).getGivenName());
		Assert.assertEquals("Adam", patients.get(3).getGivenName());
		
		patients = dao.getPatients("Ben Frank", 0, 11);
		
		Assert.assertEquals(4, patients.size());
		Assert.assertEquals("Ben", patients.get(0).getGivenName());
		Assert.assertEquals("Alan", patients.get(1).getGivenName());
		Assert.assertEquals("Benedict", patients.get(2).getGivenName());
		Assert.assertEquals("Adam", patients.get(3).getGivenName());
	}
	
	/**
	 * @see HibernatePatientDAO#getCountOfPatients(String)
	 */
	@Test
	public void getCountOfPatients_shouldObeyAttributeMatchMode() {
		// exact match mode
		long patientCount = dao.getCountOfPatients("Cook");
		Assert.assertEquals(1, patientCount);
		
		patientCount = dao.getCountOfPatients("ook");
		Assert.assertEquals(0, patientCount);
		
		globalPropertiesTestHelper.setGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_PERSON_ATTRIBUTE_SEARCH_MATCH_MODE,
		    OpenmrsConstants.GLOBAL_PROPERTY_PERSON_ATTRIBUTE_SEARCH_MATCH_ANYWHERE);
		
		patientCount = dao.getCountOfPatients("ook");
		Assert.assertEquals(1, patientCount);
	}
	
	/**
	 * @see PatientDAO#getPatients(String, boolean, Integer, Integer)
	 */
	@Test
	public void getPatients_shouldGetVoidedPersonWhenVoidedTrueIsPassed() {
		List<Patient> patients = dao.getPatients("voided-bravo", true, 0, 11);
		Assert.assertEquals(1, patients.size());
	}
	
	/**
	 * @see PatientDAO#getPatients(String, boolean, Integer, Integer)
	 */
	@Test
	public void getPatients_shouldGetNoVoidedPersonWhenVoidedFalseIsPassed() {
		List<Patient> patients = dao.getPatients("voided-bravo", false, 0, 11);
		Assert.assertEquals(0, patients.size());
	}
	/**
	 * @see HibernatePatientDAO#getDuplicatePatientsByAttributes(List)
	 */
	@Test
	public void getDuplicatePatients_shouldGetDuplicatesWithBirthDate() {
		List<String> attributes = new ArrayList<>();
		attributes.add("birthdate");
		List<Patient> patients = dao.getDuplicatePatientsByAttributes(attributes);
		Assert.assertEquals(31, patients.size());
	}
	/**
	 * @see HibernatePatientDAO#getDuplicatePatientsByAttributes(List)
	 */
	@Test
	public void getDuplicatePatients_shouldGetDuplicatesWithGenderBirthDate() {
		List<String> attributes = new ArrayList<>();
		attributes.add("gender");
		attributes.add("birthdate");
		List<Patient> patients = dao.getDuplicatePatientsByAttributes(attributes);
		Assert.assertEquals(31, patients.size());
	}
	/**
	 * @see HibernatePatientDAO#getDuplicatePatientsByAttributes(List)
	 */
	@Test
	public void getDuplicatePatients_shouldGetDuplicatesWithGenderBirthDateGivenName() {
		List<String> attributes = new ArrayList<>();
		attributes.add("gender");
		attributes.add("birthdate");
		attributes.add("givenName");
		List<Patient> patients = dao.getDuplicatePatientsByAttributes(attributes);
		Assert.assertEquals(22, patients.size());
	}

	/**
	 * @see HibernatePatientDAO#getDuplicatePatientsByAttributes(List)
	 */
	@Test
	public void getDuplicatePatients_shouldGetDuplicatesWithGenderBirthDateGivenNameFamilyName() {
		List<String> attributes = new ArrayList<>();
		attributes.add("gender");
		attributes.add("birthdate");
		attributes.add("givenName");
		attributes.add("familyName");
		List<Patient> patients = dao.getDuplicatePatientsByAttributes(attributes);
		Assert.assertEquals(10, patients.size());
	}
	/**
	 * @see HibernatePatientDAO#getDuplicatePatientsByAttributes(List)
	 */
	@Test
	public void getDuplicatePatients_shouldGetDuplicatesWithGenderBirthDateGivenNameFamilyNameIdentifier() {
		List<String> attributes = new ArrayList<>();
		attributes.add("gender");
		attributes.add("birthdate");
		attributes.add("givenName");
		attributes.add("familyName");
		attributes.add("identifier");
		List<Patient> patients = dao.getDuplicatePatientsByAttributes(attributes);
		Assert.assertEquals(2, patients.size());
	}
	/**
	 * @see HibernatePatientDAO#getDuplicatePatientsByAttributes(List)
	 */
	@Test
	public void getDuplicatePatients_shouldGetZeroDuplicatesWithInvalidAttribute() {
		List<String> attributes = new ArrayList<>();
		attributes.add("abcDef");
		List<Patient> patients = dao.getDuplicatePatientsByAttributes(attributes);
		Assert.assertEquals(0, patients.size());
	}
	/**
	 * is passed along with birthdate
	 * @see HibernatePatientDAO#getDuplicatePatientsByAttributes(List)
	 */
	@Test
	public void getDuplicatePatients_shouldGetDuplicatesWithBirthDateInvalidAttribute() {
		List<String> attributes = new ArrayList<>();
		attributes.add("abcDef");
		attributes.add("birthdate");
		List<Patient> patients = dao.getDuplicatePatientsByAttributes(attributes);
		Assert.assertEquals(31, patients.size());
	}

	@Test
	public void getPatients_shouldFindOnlySearchablePersonAttributes() {
		PersonAttributeType attributeType = personService.getPersonAttributeTypeByName("Birthplace");
		attributeType.setSearchable(false);
		personService.savePersonAttributeType(attributeType);

		List<Patient> patients = patientService.getPatients("London");
		assertThat(patients, is(empty()));

		attributeType = personService.getPersonAttributeTypeByName("Birthplace");
		attributeType.setSearchable(true);
		personService.savePersonAttributeType(attributeType);

		patients = patientService.getPatients("London");
		Patient patient = patientService.getPatient(2);
		assertThat(patients, contains(patient));
	}

	@Test
	public void getPatients_shouldReturnOnlyPatients() {
		Person person = personService.getPerson(501);
		assertThat(person.getIsPatient(), is(false));

		List<Patient> patients = patientService.getPatients(person.getGivenName());
		assertThat(patients, is(empty()));
	}

	@Test
	public void getPatients_shouldFindIdentifierIgnoringCase() {
		Patient patient = patientService.getPatient(2);
		PatientIdentifier patientIdentifier = new PatientIdentifier("AS_567", patientService.getPatientIdentifierType(5),
				locationService.getLocation(1));
		patient.addIdentifier(patientIdentifier);
		patientService.savePatient(patient);

		updateSearchIndex();

		List<Patient> patients = patientService.getPatients("as_567");
		assertThat(patients, contains(patient));
	}
	
	@Test
	public void getPatients_shouldGetPatientByIdentifierStartMatch() {
		String oldPropertyValue = globalPropertiesTestHelper.setGlobalProperty(
				OpenmrsConstants.GLOBAL_PROPERTY_PATIENT_IDENTIFIER_SEARCH_MATCH_MODE,
				OpenmrsConstants.GLOBAL_PROPERTY_PATIENT_SEARCH_MATCH_START);
		
		List<Patient> patients = dao.getPatients("42-42", false, 0, null);
		Assert.assertEquals(1, patients.size());
		Assert.assertEquals("42-42-42",patients.get(0).getPatientIdentifier().toString());
		if (oldPropertyValue != null) {
			globalPropertiesTestHelper.setGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_PATIENT_IDENTIFIER_SEARCH_MATCH_MODE,
					oldPropertyValue);
		} else {
			globalPropertiesTestHelper.purgeGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_PATIENT_IDENTIFIER_SEARCH_MATCH_MODE);
		}
		
	}
	
	@Test
	public void getPatients_shouldNotGetPatientByWrongIdentifierStartMatchPhrase() {
		String oldPropertyValue = globalPropertiesTestHelper.setGlobalProperty(
				OpenmrsConstants.GLOBAL_PROPERTY_PATIENT_IDENTIFIER_SEARCH_MATCH_MODE,
				OpenmrsConstants.GLOBAL_PROPERTY_PATIENT_SEARCH_MATCH_START);
		
		List<Patient> patients = dao.getPatients("42-47", false, 0, null);
		Assert.assertEquals(0, patients.size());
		
		if (oldPropertyValue != null) {
			globalPropertiesTestHelper.setGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_PATIENT_IDENTIFIER_SEARCH_MATCH_MODE,
					oldPropertyValue);
		} else {
			globalPropertiesTestHelper.purgeGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_PATIENT_IDENTIFIER_SEARCH_MATCH_MODE);
		}
	}
	
	@Test
	public void getPatients_shouldGetVoidedPatientsWithIdentifierStartMatch() {
		String oldPropertyValue = globalPropertiesTestHelper.setGlobalProperty(
				OpenmrsConstants.GLOBAL_PROPERTY_PATIENT_IDENTIFIER_SEARCH_MATCH_MODE,
				OpenmrsConstants.GLOBAL_PROPERTY_PATIENT_SEARCH_MATCH_START);
		
		List<Patient> patients = dao.getPatients("voided", true, 0, 11);
		Assert.assertEquals(3, patients.size());
		Assert.assertEquals(42, (int) patients.get(0).getPersonId());
		if (oldPropertyValue != null) {
			globalPropertiesTestHelper.setGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_PATIENT_IDENTIFIER_SEARCH_MATCH_MODE,
					oldPropertyValue);
		} else {
			globalPropertiesTestHelper.purgeGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_PATIENT_IDENTIFIER_SEARCH_MATCH_MODE);
		}
	}
	
	@Test
	public void getPatients_shouldGetNewPatientByIdentifierStartMatch() {
		String oldPropertyValue = globalPropertiesTestHelper.setGlobalProperty(
				OpenmrsConstants.GLOBAL_PROPERTY_PATIENT_IDENTIFIER_SEARCH_MATCH_MODE,
				OpenmrsConstants.GLOBAL_PROPERTY_PATIENT_SEARCH_MATCH_START);
		
		Patient patient = patientService.getPatient(2);
		PatientIdentifier patientIdentifier = new PatientIdentifier("OM292", patientService.getPatientIdentifierType(5), Context
				.getLocationService().getLocation(1));
		patient.addIdentifier(patientIdentifier);
		patientService.savePatient(patient);
		updateSearchIndex();
		//Check for partial identifier match
		List<Patient> patients = dao.getPatients("OM", false, 0, null);
		Assert.assertEquals(1, patients.size());
		Assert.assertEquals("OM292", patients.get(0).getPatientIdentifier(5).toString());
		
		if (oldPropertyValue != null) {
			globalPropertiesTestHelper.setGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_PATIENT_IDENTIFIER_SEARCH_MATCH_MODE,
					oldPropertyValue);
		} else {
			globalPropertiesTestHelper.purgeGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_PATIENT_IDENTIFIER_SEARCH_MATCH_MODE);
		}
	}
	
	@Test
	public void getPatients_shouldNotGetNewPatientByWrongIdentifierStartMatch() {
		String oldPropertyValue = globalPropertiesTestHelper.setGlobalProperty(
				OpenmrsConstants.GLOBAL_PROPERTY_PATIENT_IDENTIFIER_SEARCH_MATCH_MODE,
				OpenmrsConstants.GLOBAL_PROPERTY_PATIENT_SEARCH_MATCH_START);
		Patient patient = patientService.getPatient(2);
		PatientIdentifier patientIdentifier = new PatientIdentifier("OM292", patientService.getPatientIdentifierType(5), Context
				.getLocationService().getLocation(1));
		patient.addIdentifier(patientIdentifier);
		patientService.savePatient(patient);
		updateSearchIndex();
		
		List<Patient> patients = dao.getPatients("OM78", false, 0, null);
		Assert.assertEquals(0, patients.size());
		if (oldPropertyValue != null) {
			globalPropertiesTestHelper.setGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_PATIENT_IDENTIFIER_SEARCH_MATCH_MODE,
					oldPropertyValue);
		} else {
			globalPropertiesTestHelper.purgeGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_PATIENT_IDENTIFIER_SEARCH_MATCH_MODE);
		}
	}
	
	@Test
	public void getPatients_shouldGetCloseIdentifiersWithCorrectStartPhrase(){
		String oldPropertyValue = globalPropertiesTestHelper.setGlobalProperty(
				OpenmrsConstants.GLOBAL_PROPERTY_PATIENT_IDENTIFIER_SEARCH_MATCH_MODE,
				OpenmrsConstants.GLOBAL_PROPERTY_PATIENT_SEARCH_MATCH_START);
		Patient patient = patientService.getPatient(2);
		PatientIdentifier patientIdentifier = new PatientIdentifier("BAH409", patientService.getPatientIdentifierType(5), Context
				.getLocationService().getLocation(1));
		patient.addIdentifier(patientIdentifier);
		patientService.savePatient(patient);
		
		//add closely matching identifier to a different patient
		Patient patient2 = patientService.getPatient(6);
		PatientIdentifier patientIdentifier6 = new PatientIdentifier("BAH509", patientService.getPatientIdentifierType(5), Context
				.getLocationService().getLocation(1));
		patientIdentifier6.setPreferred(true);
		patient2.addIdentifier(patientIdentifier6);
		patientService.savePatient(patient2);
		
		updateSearchIndex();
		
		//Check for partial identifier match
		List<Patient> patients = dao.getPatients("BAH", false, 0, null);
		Assert.assertEquals(2,patients.size());
		if (oldPropertyValue != null) {
			globalPropertiesTestHelper.setGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_PATIENT_IDENTIFIER_SEARCH_MATCH_MODE,
					oldPropertyValue);
		} else {
			globalPropertiesTestHelper.purgeGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_PATIENT_IDENTIFIER_SEARCH_MATCH_MODE);
		}
	}
	
	@Test
	public void getPatients_shouldNotGetCloseIdentifiersWithWrongStartPhrase(){
		String oldPropertyValue = globalPropertiesTestHelper.setGlobalProperty(
				OpenmrsConstants.GLOBAL_PROPERTY_PATIENT_IDENTIFIER_SEARCH_MATCH_MODE,
				OpenmrsConstants.GLOBAL_PROPERTY_PATIENT_SEARCH_MATCH_START);
		Patient patient = patientService.getPatient(2);
		PatientIdentifier patientIdentifier = new PatientIdentifier("BAH409", patientService.getPatientIdentifierType(5), Context
				.getLocationService().getLocation(1));
		patient.addIdentifier(patientIdentifier);
		patientService.savePatient(patient);
		
		//add closely matching identifier to a different patient
		Patient patient2 = patientService.getPatient(6);
		PatientIdentifier patientIdentifier6 = new PatientIdentifier("BAH509", patientService.getPatientIdentifierType(5), Context
				.getLocationService().getLocation(1));
		patientIdentifier6.setPreferred(true);
		patient2.addIdentifier(patientIdentifier6);
		patientService.savePatient(patient2);
		
		updateSearchIndex();
		
		//Check for partial identifier match
		List<Patient> patients = dao.getPatients("BAH5", false, 0, null);
		Assert.assertEquals(1,patients.size());
		Assert.assertEquals("BAH509", patients.get(0).getPatientIdentifier(5).toString());
		if (oldPropertyValue != null) {
			globalPropertiesTestHelper.setGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_PATIENT_IDENTIFIER_SEARCH_MATCH_MODE,
					oldPropertyValue);
		} else {
			globalPropertiesTestHelper.purgeGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_PATIENT_IDENTIFIER_SEARCH_MATCH_MODE);
		}
	}
	
	@Test
	public void getPatients_shouldGetPatientByIdentifierAnywhereMatch() {
		String oldPropertyValue = globalPropertiesTestHelper.setGlobalProperty(
				OpenmrsConstants.GLOBAL_PROPERTY_PATIENT_IDENTIFIER_SEARCH_MATCH_MODE,
				OpenmrsConstants.GLOBAL_PROPERTY_PATIENT_SEARCH_MATCH_ANYWHERE);
		Patient patient = patientService.getPatient(2);
		PatientIdentifier patientIdentifier = new PatientIdentifier("OM292", patientService.getPatientIdentifierType(5), Context
				.getLocationService().getLocation(1));
		patient.addIdentifier(patientIdentifier);
		patientService.savePatient(patient);
		updateSearchIndex();
		List<Patient> patients = dao.getPatients("292", false, 0, null);
		Assert.assertEquals(1, patients.size());
		Assert.assertEquals("OM292", patients.get(0).getPatientIdentifier(5).toString());
		Patient actualPatient = patients.get(0);
		Assert.assertEquals(patient, actualPatient);
		
		if (oldPropertyValue != null) {
			globalPropertiesTestHelper.setGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_PATIENT_IDENTIFIER_SEARCH_MATCH_MODE,
					oldPropertyValue);
		} else {
			globalPropertiesTestHelper.purgeGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_PATIENT_IDENTIFIER_SEARCH_MATCH_MODE);
		}
	}
	
	@Test
	@Ignore("Designated for manual runs")
	public void getPatients_shouldFindPatientsEfficiently() throws IOException, URISyntaxException {
		URL givenNamesIn = getClass().getResource("/org/openmrs/api/db/givenNames.csv");
		List<String> givenNames = FileUtils.readLines(new File(givenNamesIn.toURI()));
		URL familyNamesIn = getClass().getResource("/org/openmrs/api/db/familyNames.csv");
		List<String> familyNames = FileUtils.readLines(new File(familyNamesIn.toURI()));
		List<String> attributes = Arrays.asList("London", "Berlin", "Warsaw", "Paris", "Zurich", "Singapore");

		PatientIdentifierType idType = patientService.getPatientIdentifierTypeByName("Old Identification Number");

		PersonAttributeType attributeType = personService.getPersonAttributeTypeByName("Birthplace");
		attributeType.setSearchable(true);
		Context.getPersonService().savePersonAttributeType(attributeType);

		Location location = locationService.getLocation(1);
		Random random = new Random(100); //set the seed to have repeatable results
		List<String> generatedPatients = new ArrayList<>();
		for (int i = 0; i < 20000; i++) {
			int given = random.nextInt(givenNames.size());
			int family = random.nextInt(familyNames.size());
			int attribute = random.nextInt(attributes.size());

			generatedPatients.add((i + 1000) + " " + givenNames.get(given) + " " + familyNames.get(family) + " " + attributes.get(attribute));

			PersonName personName = new PersonName(givenNames.get(given), null, familyNames.get(family));
			Patient patient = new Patient();
			patient.setGender("m");
			patient.addIdentifier(new PatientIdentifier("" + (i + 1000), idType, location));
			patient.addName(personName);
			PersonAttribute personAttribute = new PersonAttribute();
			personAttribute.setAttributeType(attributeType);
			personAttribute.setValue(attributes.get(attribute));
			patient.addAttribute(personAttribute);
			patientService.savePatient(patient);

			if (i % 100 == 0) {
				System.out.println("Created " + i + " patients!");
				Context.flushSession();
				Context.clearSession();
			}
		}

		File file = File.createTempFile("generated-patients-", ".csv");
		FileUtils.writeLines(file, generatedPatients);
		System.out.println("Dumped generated patients to " + file.getAbsolutePath());

		long time = System.currentTimeMillis();
		updateSearchIndex();
		time = System.currentTimeMillis() - time;
		System.out.println("Indexing took " + time + " ms.");

		patientService.getPatients("Aaaaa"); //get Lucene up to speed...

		time = System.currentTimeMillis();
		List<Patient> results = patientService.getPatients("Al");
		time = System.currentTimeMillis() - time;
		System.out.println("Starts with search for 'Al' name returned " + results.size() + " in " + time + " ms");

		time = System.currentTimeMillis();
		results = patientService.getPatients("Al", 0, 15);
		time = System.currentTimeMillis() - time;
		System.out.println("Starts with search for 'Al' name limited to 15 results returned in " + time + " ms");

		time = System.currentTimeMillis();
		results = patientService.getPatients("Al Dem");
		time = System.currentTimeMillis() - time;
		System.out.println("Starts with search for 'Al Dem' name returned " + results.size() + " in " + time + " ms");

		time = System.currentTimeMillis();
		results = patientService.getPatients("Al Dem", 0, 15);
		time = System.currentTimeMillis() - time;
		System.out.println("Starts with search for 'Al Dem' name limited to 15 results returned in " + time + " ms");

		time = System.currentTimeMillis();
		results = patientService.getPatients("Jack");
		time = System.currentTimeMillis() - time;
		System.out.println("Starts with search for 'Jack' name returned " + results.size() + " in " + time + " ms");

		time = System.currentTimeMillis();
		results = patientService.getPatients("Jack", 0, 15);
		time = System.currentTimeMillis() - time;
		System.out.println("Starts with search for 'Jack' name limited to 15 results returned in " + time + " ms");

		time = System.currentTimeMillis();
		results = patientService.getPatients("Jack Sehgal");
		time = System.currentTimeMillis() - time;
		System.out.println("Starts with search for 'Jack Sehgal' name returned " + results.size() + " in " + time + " ms");

		time = System.currentTimeMillis();
		results = patientService.getPatients("Jack Sehgal", 0, 15);
		time = System.currentTimeMillis() - time;
		System.out.println("Starts with search for 'Jack Sehgal' name limited to 15 results returned in " + time + " ms");

		Context.getAdministrationService().setGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_PATIENT_SEARCH_MATCH_MODE,
				OpenmrsConstants.GLOBAL_PROPERTY_PATIENT_SEARCH_MATCH_ANYWHERE);

		time = System.currentTimeMillis();
		results = patientService.getPatients("aso");
		time = System.currentTimeMillis() - time;
		System.out.println("Anywhere search for 'aso' name returned " + results.size() + " in " + time + " ms");

		time = System.currentTimeMillis();
		results = patientService.getPatients("aso", 0, 15);
		time = System.currentTimeMillis() - time;
		System.out.println("Anywhere search for 'aso' name limited to 15 results returned in " + time + " ms");

		time = System.currentTimeMillis();
		results = patientService.getPatients("aso os");
		time = System.currentTimeMillis() - time;
		System.out.println("Anywhere search for 'aso os' name returned " + results.size() + " in " + time + " ms");

		time = System.currentTimeMillis();
		results = patientService.getPatients("aso os", 0, 15);
		time = System.currentTimeMillis() - time;
		System.out.println("Anywhere search for 'aso os' limited to 15 results returned in " + time + " ms");

		time = System.currentTimeMillis();
		results = patientService.getPatients("9243");
		time = System.currentTimeMillis() - time;
		System.out.println("Exact search for '9243' identifier returned " + results.size() + " in " + time + " ms");

		time = System.currentTimeMillis();
		results = patientService.getPatients("London");
		time = System.currentTimeMillis() - time;
		System.out.println("Exact search for 'London' attribute returned " + results.size() + " in " + time + " ms");

		time = System.currentTimeMillis();
		results = patientService.getPatients("London", 0, 15);
		time = System.currentTimeMillis() - time;
		System.out.println("Exact search for 'London' attribute limited to 15 results returned in " + time + " ms");

		Context.getAdministrationService().setGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_PERSON_ATTRIBUTE_SEARCH_MATCH_MODE,
				OpenmrsConstants.GLOBAL_PROPERTY_PERSON_ATTRIBUTE_SEARCH_MATCH_ANYWHERE);

		time = System.currentTimeMillis();
		results = patientService.getPatients("uric");
		time = System.currentTimeMillis() - time;
		System.out.println("Anywhere search for 'uric' attribute returned " + results.size() + " in " + time + " ms");

		time = System.currentTimeMillis();
		results = patientService.getPatients("uric", 0, 15);
		time = System.currentTimeMillis() - time;
		System.out.println("Anywhere search for 'uric' attribute limited to 15 results returned in " + time + " ms");
	}
}
