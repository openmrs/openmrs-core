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
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

import org.hibernate.SessionFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.PersonName;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.hibernate.HibernatePatientDAO;
import org.openmrs.api.db.hibernate.HibernatePersonDAO;
import org.openmrs.api.db.hibernate.PersonAttributeHelper;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.test.Verifies;
import org.openmrs.util.GlobalPropertiesTestHelper;
import org.openmrs.util.OpenmrsConstants;

public class PatientDAOTest extends BaseContextSensitiveTest {
	
	private final static Log log = LogFactory.getLog(PatientDAOTest.class);
	
	private final static String PEOPLE_FROM_THE_SHIRE_XML = "org/openmrs/api/db/hibernate/include/HibernatePersonDAOTest-people.xml";
	
	private final static String PATIENTS_FROM_THE_SHIRE_XML = "org/openmrs/api/db/hibernate/include/HibernatePatientDAOTest-patients.xml";
	
	private PatientDAO dao = null;
	
	private PatientService pService = null;
	
	private GlobalPropertiesTestHelper globalPropertiesTestHelper;
	
	private PersonAttributeHelper personAttributeHelper;
	
	/**
	 * Run this before each unit test in this class. The "@Before" method in
	 * {@link BaseContextSensitiveTest} is run right before this method.
	 * 
	 * @throws Exception
	 */
	@Before
	public void runBeforeEachTest() throws Exception {
		
		executeDataSet(PEOPLE_FROM_THE_SHIRE_XML);
		executeDataSet(PATIENTS_FROM_THE_SHIRE_XML);
		
		if (dao == null)
			// fetch the dao from the spring application context
			// this bean name matches the name in /metadata/spring/applicationContext-service.xml
			dao = (PatientDAO) applicationContext.getBean("patientDAO");
		if (pService == null)
			pService = Context.getPatientService();
		
		SessionFactory sessionFactory = (SessionFactory) applicationContext.getBean("sessionFactory");
		personAttributeHelper = new PersonAttributeHelper(sessionFactory);
		globalPropertiesTestHelper = new GlobalPropertiesTestHelper(Context.getAdministrationService());
		
	}
	
	private void logPatientList(List<Patient> patients) {
		for (Patient patient : patients) {
			logPatient(patient);
		}
	}
	
	private void logPatient(Patient patient) {
		StringBuilder builder = new StringBuilder();
		
		builder.append("class=").append(patient.getClass().getCanonicalName()).append(", person=")
		        .append(patient.toString()).append(", person.names=").append(patient.getNames().toString()).append(
		            ", person.attributes=").append(patient.getAttributes().toString());
		
		log.debug(builder.toString());
	}
	
	/**
	 * @see {@link PatientDAO#getPatients(String,String,List<QPatientIdentifierType;>,null)}
	 */
	@Test
	@Verifies(value = "should escape an asterix character in identifier phrase", method = "getPatients(String,String,List<QPatientIdentifierType;>,null)")
	public void getPatients_shouldEscapeAnAsterixCharacterInIdentifierPhrase() throws Exception {
		//Note that all tests for wildcard should be pass in 2s due to the behaviour of wildcards,
		//that is we test for the size and actual patient object returned
		Patient patient2 = pService.getPatient(2);
		PatientIdentifier patientIdentifier = new PatientIdentifier("*567", pService.getPatientIdentifierType(5), Context
		        .getLocationService().getLocation(1));
		patient2.addIdentifier(patientIdentifier);
		pService.savePatient(patient2);
		
		//add closely matching identifier to a different patient
		Patient patient6 = pService.getPatient(6);
		PatientIdentifier patientIdentifier6 = new PatientIdentifier("4567", pService.getPatientIdentifierType(5), Context
		        .getLocationService().getLocation(1));
		patientIdentifier6.setPreferred(true);
		patient6.addIdentifier(patientIdentifier6);
		pService.savePatient(patient6);
		
		List<PatientIdentifierType> identifierTypes = Collections.emptyList();
		//we expect only one matching patient
		int actualSize = dao.getPatients(null, "*567", identifierTypes, false, 0, null, false).size();
		Assert.assertEquals(1, actualSize);
		
		//if actually the search returned the matching patient
		Patient actualPatient = dao.getPatients(null, "*567", identifierTypes, false, 0, null, false).get(0);
		
		Assert.assertEquals(patient2, actualPatient);
	}
	
	/**
	 * @see {@link PatientDAO#getPatients(String,String,List<QPatientIdentifierType;>,null)}
	 */
	@Test
	@Verifies(value = "should escape percentage character in identifier phrase", method = "getPatients(String,String,List<QPatientIdentifierType;>,null)")
	public void getPatients_shouldEscapePercentageCharacterInIdentifierPhrase() throws Exception {
		
		Patient patient2 = pService.getPatient(2);
		PatientIdentifier patientIdentifier = new PatientIdentifier("%567", pService.getPatientIdentifierType(5), Context
		        .getLocationService().getLocation(1));
		patient2.addIdentifier(patientIdentifier);
		pService.savePatient(patient2);
		
		//add closely matching identifier to a different patient
		Patient patient6 = pService.getPatient(6);
		PatientIdentifier patientIdentifier6 = new PatientIdentifier("4567", pService.getPatientIdentifierType(5), Context
		        .getLocationService().getLocation(1));
		patientIdentifier6.setPreferred(true);
		patient6.addIdentifier(patientIdentifier6);
		pService.savePatient(patient6);
		
		List<PatientIdentifierType> identifierTypes = Collections.emptyList();
		//we expect only one matching patient
		int actualSize = dao.getPatients(null, "%567", identifierTypes, false, 0, null, false).size();
		Assert.assertEquals(1, actualSize);
		
		//if actually the search returned the matching patient
		Patient actualPatient = dao.getPatients(null, "%567", identifierTypes, false, 0, null, false).get(0);
		
		Assert.assertEquals(patient2, actualPatient);
	}
	
	/**
	 * @see {@link PatientDAO#getPatients(String,String,List<QPatientIdentifierType;>,null)}
	 */
	@Test
	@Verifies(value = "should escape underscore character in identifier phrase", method = "getPatients(String,String,List<QPatientIdentifierType;>,null)")
	public void getPatients_shouldEscapeUnderscoreCharacterInIdentifierPhrase() throws Exception {
		deleteAllData();
		baseSetupWithStandardDataAndAuthentication();
		Patient patient2 = pService.getPatient(2);
		PatientIdentifier patientIdentifier = new PatientIdentifier("_567", pService.getPatientIdentifierType(5), Context
		        .getLocationService().getLocation(1));
		patient2.addIdentifier(patientIdentifier);
		pService.savePatient(patient2);
		
		//add closely matching identifier to a different patient
		Patient patient6 = pService.getPatient(6);
		PatientIdentifier patientIdentifier6 = new PatientIdentifier("4567", pService.getPatientIdentifierType(5), Context
		        .getLocationService().getLocation(1));
		patientIdentifier6.setPreferred(true);
		patient6.addIdentifier(patientIdentifier6);
		pService.savePatient(patient6);
		
		List<PatientIdentifierType> identifierTypes = Collections.emptyList();
		//we expect only one matching patient
		int actualSize = dao.getPatients(null, "_567", identifierTypes, false, 0, null, false).size();
		Assert.assertEquals(1, actualSize);
		
		//if actually the search returned the matching patient
		Patient actualPatient = dao.getPatients(null, "_567", identifierTypes, false, 0, null, false).get(0);
		
		Assert.assertEquals(patient2, actualPatient);
	}
	
	/**
	 * @see {@link PatientDAO#getPatients(String,String,List<QPatientIdentifierType;>,null)}
	 */
	@Test
	@Verifies(value = "should escape percentage character in name phrase", method = "getPatients(String,String,List<QPatientIdentifierType;>,null)")
	public void getPatients_shouldEscapePercentageCharacterInNamePhrase() throws Exception {
		
		Patient patient2 = pService.getPatient(2);
		PersonName name = new PersonName("%cats", "and", "dogs");
		patient2.addName(name);
		pService.savePatient(patient2);
		
		//add a new closely matching identifier to another patient
		Patient patient6 = pService.getPatient(6);
		PersonName name6 = new PersonName("acats", "and", "dogs");
		patient6.addName(name6);
		patient6.getPatientIdentifier().setPreferred(true);
		pService.savePatient(patient6);
		
		List<PatientIdentifierType> identifierTypes = Collections.emptyList();
		//we expect only one matching patient
		int actualSize = dao.getPatients("%ca", null, identifierTypes, false, 0, null, false).size();
		Assert.assertEquals(1, actualSize);
		
		Patient actualPatient = dao.getPatients("%ca", null, identifierTypes, false, 0, null, false).get(0);
		//if actually the search returned the matching patient
		Assert.assertEquals(patient2, actualPatient);
	}
	
	/**
	 * @see {@link PatientDAO#getPatients(String,String,List<QPatientIdentifierType;>,null)}
	 */
	@Test
	@Verifies(value = "should escape underscore character in name phrase", method = "getPatients(String,String,List<QPatientIdentifierType;>,null)")
	public void getPatients_shouldEscapeUnderscoreCharacterInNamePhrase() throws Exception {
		
		Patient patient2 = pService.getPatient(2);
		PersonName name = new PersonName("_cats", "and", "dogs");
		patient2.addName(name);
		pService.savePatient(patient2);
		
		//add a new closely matching name to another patient
		Patient patient6 = pService.getPatient(6);
		PersonName name6 = new PersonName("acats", "and", "dogs");
		patient6.addName(name6);
		patient6.getPatientIdentifier().setPreferred(true);
		pService.savePatient(patient6);
		
		List<PatientIdentifierType> identifierTypes = Collections.emptyList();
		//we expect only one matching patient
		int actualSize = dao.getPatients("_ca", null, identifierTypes, false, 0, null, false).size();
		Assert.assertEquals(1, actualSize);
		
		//if actually the search returned the matching patient
		Patient actualPatient = dao.getPatients("_ca", null, identifierTypes, false, 0, null, false).get(0);
		Assert.assertEquals(patient2, actualPatient);
		
	}
	
	/**
	 * @see {@link PatientDAO#getPatients(String,String,List<QPatientIdentifierType;>,null)}
	 */
	@Test
	@Verifies(value = "should escape an asterix character in name phrase", method = "getPatients(String,String,List<QPatientIdentifierType;>,null)")
	public void getPatients_shouldEscapeAnAsterixCharacterInNamePhrase() throws Exception {
		
		Patient patient2 = pService.getPatient(2);
		PersonName name = new PersonName("*cats", "and", "dogs");
		patient2.addName(name);
		pService.savePatient(patient2);
		
		//add a new closely matching name to another patient
		Patient patient6 = pService.getPatient(6);
		PersonName name6 = new PersonName("acats", "and", "dogs");
		patient6.addName(name6);
		patient6.getPatientIdentifier().setPreferred(true);
		pService.savePatient(patient6);
		
		List<PatientIdentifierType> identifierTypes = Collections.emptyList();
		//we expect only one matching patient
		int actualSize = dao.getPatients("*ca", null, identifierTypes, false, 0, null, false).size();
		Assert.assertEquals(1, actualSize);
		
		//if actually the search returned the matching patient
		Patient actualPatient = dao.getPatients("*ca", null, identifierTypes, false, 0, null, false).get(0);
		Assert.assertEquals(patient2, actualPatient);
	}
	
	/**
	 * @see PatientDAO#getAllPatientIdentifierTypes(boolean)
	 * @verifies not return null excluding retired
	 */
	@Test
	public void getAllPatientIdentifierTypes_shouldNotReturnNullExcludingRetired() throws Exception {
		Assert.assertNotNull(dao.getAllPatientIdentifierTypes(false));
	}
	
	/**
	 * @see PatientDAO#getAllPatientIdentifierTypes(boolean)
	 * @verifies not return retired
	 */
	@Test
	public void getAllPatientIdentifierTypes_shouldNotReturnRetired() throws Exception {
		List<PatientIdentifierType> patientIdentifierTypes = dao.getAllPatientIdentifierTypes(false);
		Assert.assertEquals("patientIdentifierTypes list should have 3 elements", 3, patientIdentifierTypes.size());
	}
	
	/**
	 * @see PatientDAO#getAllPatientIdentifierTypes(boolean)
	 * @verifies not return null including retired
	 */
	@Test
	public void getAllPatientIdentifierTypes_shouldNotReturnNullIncludingRetired() throws Exception {
		Assert.assertNotNull(dao.getAllPatientIdentifierTypes(true));
	}
	
	/**
	 * @see PatientDAO#getAllPatientIdentifierTypes(boolean)
	 * @verifies return all
	 */
	@Test
	public void getAllPatientIdentifierTypes_shouldReturnAll() throws Exception {
		List<PatientIdentifierType> patientIdentifierTypes = dao.getAllPatientIdentifierTypes(true);
		Assert.assertEquals("patientIdentifierTypes list should have 4 elements", 4, patientIdentifierTypes.size());
	}
	
	@Test
	public void getPatientIdentifiers_shouldLimitByResultsByLocation() throws Exception {
		Location location = Context.getLocationService().getLocation(3); // there is only one identifier in the test database for location 3
		List<PatientIdentifier> patientIdentifiers = dao.getPatientIdentifiers(null, new ArrayList<PatientIdentifierType>(),
		    Collections.singletonList(location), new ArrayList<Patient>(), null);
		Assert.assertEquals(1, patientIdentifiers.size());
		Assert.assertEquals("12345K", patientIdentifiers.get(0).getIdentifier());
	}
	
	/**
	 * @see PatientDAO#getPatientIdentifiers(String,List,List,List,Boolean)
	 * @verifies not get voided patient identifiers
	 */
	@Test
	public void getPatientIdentifiers_shouldNotGetVoidedPatientIdentifiers() throws Exception {
		
		List<PatientIdentifier> patientIdentifiers = dao.getPatientIdentifiers(null, new ArrayList<PatientIdentifierType>(),
		    new ArrayList<Location>(), new ArrayList<Patient>(), null);
		
		// standartTestDataset.xml contains 5 non-voided identifiers
		//
		// plus 1 non-voided identifier from HibernatePatientDAOTest-patients.xml
		
		Assert.assertEquals(6, patientIdentifiers.size());
		
		for (PatientIdentifier patientIdentifier : patientIdentifiers) {
			Assert.assertFalse(patientIdentifier.isVoided());
		}
	}
	
	/**
	 * @see PatientDAO#getPatientIdentifiers(String,List,List,List,Boolean)
	 * @verifies not fetch patient identifiers that partially matches given identifier
	 */
	@Test
	public void getPatientIdentifiers_shouldNotFetchPatientIdentifiersThatPartiallyMatchesGivenIdentifier() throws Exception {
		
		String identifier = "123"; // identifier [12345K] exist in test dataSet
		
		List<PatientIdentifier> patientIdentifiers = dao.getPatientIdentifiers(identifier,
		    new ArrayList<PatientIdentifierType>(), new ArrayList<Location>(), new ArrayList<Patient>(), null);
		
		Assert.assertTrue(patientIdentifiers.isEmpty());
	}
	
	/**
	 * @see PatientDAO#getPatientIdentifiers(String,List,List,List,Boolean)
	 * @verifies fetch patient identifiers that equals given identifier
	 */
	@Test
	public void getPatientIdentifiers_shouldFetchPatientIdentifiersThatEqualsGivenIdentifier() throws Exception {
		
		String identifier = "101";
		
		List<PatientIdentifier> patientIdentifiers = dao.getPatientIdentifiers(identifier,
		    new ArrayList<PatientIdentifierType>(), new ArrayList<Location>(), new ArrayList<Patient>(), null);
		
		Assert.assertEquals(1, patientIdentifiers.size());
		Assert.assertEquals(identifier, patientIdentifiers.get(0).getIdentifier());
	}
	
	/**
	 * @see PatientDAO#getPatientIdentifiers(String,List,List,List,Boolean)
	 * @verifies return all matching non voided patient identifiers if is preferred is set to false
	 */
	@Test
	public void getPatientIdentifiers_shouldReturnAllMatchingNonVoidedPatientIdentifiersIfIsPreferredIsSetToFalse()
	        throws Exception {
		
		List<PatientIdentifier> patientIdentifiers = dao.getPatientIdentifiers(null, new ArrayList<PatientIdentifierType>(),
		    new ArrayList<Location>(), new ArrayList<Patient>(), Boolean.FALSE);
		
		Assert.assertEquals(4, patientIdentifiers.size());
	}
	
	/**
	 * @see PatientDAO#getPatientIdentifiers(String,List,List,List,Boolean)
	 * @verifies return all matching non voided patient identifiers if is preferred is set to null
	 */
	@Test
	public void getPatientIdentifiers_shouldReturnAllMatchingNonVoidedPatientIdentifiersIfIsPreferredIsSetToNull()
	        throws Exception {
		
		List<PatientIdentifier> patientIdentifiers = dao.getPatientIdentifiers(null, new ArrayList<PatientIdentifierType>(),
		    new ArrayList<Location>(), new ArrayList<Patient>(), null);
		
		Assert.assertEquals(6, patientIdentifiers.size());
	}
	
	/**
	 * @see PatientDAO#getPatientIdentifiers(String,List,List,List,Boolean)
	 * @verifies return all matching non voided patient identifiers if is preferred is set to true
	 */
	@Test
	public void getPatientIdentifiers_shouldReturnAllMatchingNonVoidedPatientIdentifiersIfIsPreferredIsSetToTrue()
	        throws Exception {
		
		List<PatientIdentifier> patientIdentifiers = dao.getPatientIdentifiers(null, new ArrayList<PatientIdentifierType>(),
		    new ArrayList<Location>(), new ArrayList<Patient>(), Boolean.TRUE);
		
		Assert.assertEquals(2, patientIdentifiers.size());
	}
	
	/**
	 * @see PatientDAO#getPatientIdentifiers(String,List,List,List,Boolean)
	 * @verifies fetch all patient identifiers belong to given patient
	 */
	@Test
	public void getPatientIdentifiers_shouldFetchAllPatientIdentifiersBelongToGivenPatient() throws Exception {
		
		//There are two identifiers in the test database for patient with id 2
		Patient patientWithId2 = Context.getPatientService().getPatient(2);
		
		List<PatientIdentifier> patientIdentifiers = dao.getPatientIdentifiers(null, new ArrayList<PatientIdentifierType>(),
		    new ArrayList<Location>(), Collections.singletonList(patientWithId2), null);
		
		assertThat(patientIdentifiers, containsInAnyOrder(hasIdentifier("101"), hasIdentifier("101-6")));
	}
	
	/**
	 * @see PatientDAO#getPatientIdentifiers(String,List,List,List,Boolean)
	 * @verifies fetch all patient identifiers belong to given patients
	 */
	@Test
	public void getPatientIdentifiers_shouldFetchAllPatientIdentifiersBelongToGivenPatients() throws Exception {
		
		//There is one identifier[id=12345K] in the test database for patient with id 6 
		Patient patientWithId6 = Context.getPatientService().getPatient(6);
		
		//There is one identifier[id=6TS-4] in the test database for patient with id 7 
		Patient patientWithId7 = Context.getPatientService().getPatient(7);
		
		List<Patient> patientsList = Arrays.asList(patientWithId6, patientWithId7);
		
		List<PatientIdentifier> patientIdentifiers = dao.getPatientIdentifiers(null, new ArrayList<PatientIdentifierType>(),
		    new ArrayList<Location>(), patientsList, null);
		
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
	 * @verifies return ordered
	 */
	@Test
	public void getAllPatientIdentifierTypes_shouldReturnOrdered() throws Exception {
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
	 * @verifies return non retired patient identifier types with given name
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
	 * @verifies return non retired patient identifier types with given format
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
	 * @verifies return non retired patient identifie types that are not required
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
	 * @verifies return non retired patient identifier types that are required
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
	 * @verifies return non retired patient identifier types that has checkDigit
	 */
	@Test
	public void getPatientIdentifierTypes_shouldReturnNonRetiredPatientIdentifierTypesThatHasCheckDigit() {
		PatientIdentifierType nonRetiredHasDigit = dao.getPatientIdentifierType(1);
		
		List<PatientIdentifierType> patientIdentifierTypes = dao.getPatientIdentifierTypes(null, null, null, true);
		
		Assert.assertEquals(patientIdentifierTypes.size(), 1);
		Assert.assertEquals(nonRetiredHasDigit, patientIdentifierTypes.get(0));
	}
	
	/**
	 * @see PatientDAO#getPatientIdentifierTypes(String, String, Boolean, Boolean)
	 * @verifies return non retired patient identifier types that has not CheckDigit
	 */
	@Test
	public void getPatientIdentifierTypes_shouldReturnNonRetiredPatientIdentifierTypesThatHasNotCheckDigit() {
		PatientIdentifierType nonRetiredHasNoDigit1 = dao.getPatientIdentifierType(2);
		PatientIdentifierType nonRetiredHasNoDigit2 = dao.getPatientIdentifierType(5);
		
		List<PatientIdentifierType> patientIdentifierTypes = dao.getPatientIdentifierTypes(null, null, null, false);
		
		Assert.assertEquals(patientIdentifierTypes.size(), 2);
		Assert.assertTrue(patientIdentifierTypes.contains(nonRetiredHasNoDigit1));
		Assert.assertTrue(patientIdentifierTypes.contains(nonRetiredHasNoDigit2));
	}
	
	/**
	 * @see PatientDAO#getPatientIdentifierTypes(String, String, Boolean, Boolean)
	 * @verifies return only non retired patient identifier types
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
	 * @verifies return non retired patient identifier types ordered by required first
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
	 * @verifies return non retired patient identifier types ordered by required and name
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
	 * @verifies return non retired patient identifier types ordered by required name and type id
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
	 * @verifies return non when searching on voided patients
	 */
	@Test
	public void getPatients_shouldNotMatchVoidedPatients() {
		List<PatientIdentifierType> identifierTypes = Collections.emptyList();
		List<Patient> patients = dao.getPatients("Hornblower3", null, identifierTypes, false, 0, 11, false);
		Assert.assertEquals(1, patients.size());
		
		Patient patient = patients.get(0);
		patient.setVoided(true);
		dao.savePatient(patient);
		
		patients = dao.getPatients("Hornblower3", null, identifierTypes, false, 0, 11, false);
		Assert.assertEquals(0, patients.size());
	}
	
	/**
	 * @see PatientDAO#getPatients(String, String, java.util.List, boolean, Integer,
	 *      Integer, boolean)
	 * @verifies return none when searching on voided patient name
	 */
	@Test
	public void getPatients_shouldNotMatchVoidedPatientNames() {
		List<PatientIdentifierType> identifierTypes = Collections.emptyList();
		List<Patient> patients = dao.getPatients("Oloo", null, identifierTypes, false, 0, 11, false);
		Assert.assertEquals(1, patients.size());
		
		Patient patient = patients.get(0);
		
		Set<PersonName> names = patient.getNames();
		
		for (PersonName name : names) {
			name.setVoided(true);
		}
		
		dao.savePatient(patient);
		patients = dao.getPatients("Oloo", null, identifierTypes, false, 0, 11, false);
		Assert.assertEquals(0, patients.size());
	}
	
	/**
	 * @verifies not match voided patients _ signature no 1
	 * @see HibernatePatientDAO#getPatients(String, String, java.util.List, boolean, Integer, Integer, boolean)
	 */
	@Test
	public void getPatients_shouldNotMatchVoidedPatients_SignatureNo1() throws Exception {
		List<PatientIdentifierType> identifierTypes = Collections.emptyList();
		List<Patient> patients = dao.getPatients("Hornblower3", null, identifierTypes, false, 0, 11, false);
		Assert.assertEquals(1, patients.size());
		
		Patient patient = patients.get(0);
		patient.setVoided(true);
		dao.savePatient(patient);
		
		patients = dao.getPatients("Hornblower3", null, identifierTypes, false, 0, 11, false);
		Assert.assertEquals(0, patients.size());
	}
	
	/**
	 * @verifies not match voided patient names _ signature no 1
	 * @see HibernatePatientDAO#getPatients(String, String, java.util.List, boolean, Integer, Integer, boolean)
	 */
	@Test
	public void getPatients_shouldNotMatchVoidedPatientNames_SignatureNo1() throws Exception {
		List<PatientIdentifierType> identifierTypes = Collections.emptyList();
		List<Patient> patients = dao.getPatients("Oloo", null, identifierTypes, false, 0, 11, false);
		Assert.assertEquals(1, patients.size());
		
		Patient patient = patients.get(0);
		
		Set<PersonName> names = patient.getNames();
		
		for (PersonName name : names) {
			name.setVoided(true);
		}
		
		dao.savePatient(patient);
		patients = dao.getPatients("Oloo", null, identifierTypes, false, 0, 11, false);
		Assert.assertEquals(0, patients.size());
	}
	
	/**
	 * @verifies get patient by given name _ signature no 1
	 * @see HibernatePatientDAO#getPatients(String, String, java.util.List, boolean, Integer, Integer, boolean)
	 */
	@Test
	public void getPatients_shouldGetPatientByGivenName_SignatureNo1() throws Exception {
		List<Patient> patients = dao.getPatients("Bilbo Odilon", null, null, false, 0, 11, false);
		
		Assert.assertEquals(1, patients.size());
		Assert.assertEquals("Bilbo Odilon", patients.get(0).getGivenName());
	}
	
	/**
	 * @verifies get patient by middle name _ signature no 1
	 * @see HibernatePatientDAO#getPatients(String, String, java.util.List, boolean, Integer, Integer, boolean)
	 */
	@Test
	public void getPatients_shouldGetPatientByMiddleName_SignatureNo1() throws Exception {
		List<Patient> patients = dao.getPatients("B.", null, null, false, 0, 11, false);
		
		Assert.assertEquals(1, patients.size());
		Assert.assertEquals("Bilbo Odilon", patients.get(0).getGivenName());
	}
	
	/**
	 * @verifies get patient by family name _ signature no 1
	 * @see HibernatePatientDAO#getPatients(String, String, java.util.List, boolean, Integer, Integer, boolean)
	 */
	@Test
	public void getPatients_shouldGetPatientByFamilyName_SignatureNo1() throws Exception {
		List<Patient> patients = dao.getPatients("Baggins", null, null, false, 0, 11, false);
		
		Assert.assertEquals(2, patients.size());
		Assert.assertEquals("Baggins", patients.get(0).getFamilyName());
		Assert.assertEquals("Baggins", patients.get(1).getFamilyName());
		Assert.assertFalse(patients.get(0).getGivenName().equalsIgnoreCase(patients.get(1).getGivenName()));
	}
	
	/**
	 * @verifies get patient by family2 name _ signature no 1
	 * @see HibernatePatientDAO#getPatients(String, String, java.util.List, boolean, Integer, Integer, boolean)
	 */
	@Test
	public void getPatients_shouldGetPatientByFamily2Name_SignatureNo1() throws Exception {
		List<Patient> patients = dao.getPatients("Senior", null, null, false, 0, 11, false);
		
		Assert.assertEquals(1, patients.size());
		Assert.assertEquals("Bilbo Odilon", patients.get(0).getGivenName());
	}
	
	/**
	 * @verifies get patient by whole name _ signature no 1
	 * @see HibernatePatientDAO#getPatients(String, String, java.util.List, boolean, Integer, Integer, boolean)
	 */
	@Test
	public void getPatients_shouldGetPatientByWholeName_SignatureNo1() throws Exception {
		List<Patient> patients = dao.getPatients("Bilbo Odilon B. Baggins Senior", null, null, false, 0, 11, false);
		
		Assert.assertEquals(1, patients.size());
		Assert.assertEquals("Bilbo Odilon", patients.get(0).getGivenName());
	}
	
	/**
	 * @verifies not get patient by non-existing single name _ signature no 1
	 * @see HibernatePatientDAO#getPatients(String, String, java.util.List, boolean, Integer, Integer, boolean)
	 */
	@Test
	public void getPatients_shouldNotGetPatientByNonexistingSingleName_SignatureNo1() throws Exception {
		List<Patient> patients = dao.getPatients("Peter", null, null, false, 0, 11, false);
		
		Assert.assertEquals(0, patients.size());
	}
	
	/**
	 * @verifies not get patient by non-existing name parts _ signature no 1
	 * @see HibernatePatientDAO#getPatients(String, String, java.util.List, boolean, Integer, Integer, boolean)
	 */
	@Test
	public void getPatients_shouldNotGetPatientByNonexistingNameParts_SignatureNo1() throws Exception {
		List<Patient> patients = dao.getPatients("Sam Gamdschie Eldest", null, null, false, 0, 11, false);
		
		Assert.assertEquals(0, patients.size());
	}
	
	/**
	 * @verifies not get patient by mix of existing and non-existing name parts _ signature no 1
	 * @see HibernatePatientDAO#getPatients(String, String, java.util.List, boolean, Integer, Integer, boolean)
	 */
	@Test
	public void getPatients_shouldNotGetPatientByMixOfExistingAndNonexistingNameParts_SignatureNo1() throws Exception {
		List<Patient> patients = dao.getPatients("Bilbo Odilon X. Baggins", null, null, false, 0, 11, false);
		
		Assert.assertEquals(0, patients.size());
	}
	
	/**
	 * @verifies not get patient by voided name _ signature no 1
	 * @see HibernatePatientDAO#getPatients(String, String, java.util.List, boolean, Integer, Integer, boolean)
	 */
	@Test
	public void getPatients_shouldNotGetPatientByVoidedName_SignatureNo1() throws Exception {
		List<Patient> patients = dao.getPatients("voided-delta", null, null, false, 0, 11, false);
		
		Assert.assertEquals(0, patients.size());
	}
	
	/**
	 * @verifies not get patients by empty name _ signature no 1
	 * @see HibernatePatientDAO#getPatients(String, String, java.util.List, boolean, Integer, Integer, boolean)
	 */
	@Test
	public void getPatients_shouldNotGetPatientsByEmptyName_SignatureNo1() throws Exception {
		List<Patient> patients = dao.getPatients("", null, null, false, 0, 11, false);
		
		Assert.assertEquals(0, patients.size());
	}
	
	/**
	 * @verifies not get patients by null name _ signature no 1
	 * @see HibernatePatientDAO#getPatients(String, String, java.util.List, boolean, Integer, Integer, boolean)
	 */
	@Test
	public void getPatients_shouldNotGetPatientsByNullName_SignatureNo1() throws Exception {
		List<Patient> patients = dao.getPatients(null, null, null, false, 0, 11, false);
		
		Assert.assertEquals(0, patients.size());
	}
	
	/**
	 * @verifies get patient by short given name _ signature no 1
	 * @see HibernatePatientDAO#getPatients(String, String, java.util.List, boolean, Integer, Integer, boolean)
	 */
	@Test
	public void getPatients_shouldGetPatientByShortGivenName_SignatureNo1() throws Exception {
		List<Patient> patients = dao.getPatients("al", null, null, false, 0, 11, false);
		
		Assert.assertEquals(2, patients.size());
		Assert.assertEquals("al", patients.get(0).getGivenName());
		Assert.assertEquals("al", patients.get(1).getGivenName());
		Assert.assertFalse(patients.get(0).getMiddleName().equalsIgnoreCase(patients.get(1).getMiddleName()));
	}
	
	/**
	 * @verifies get patient by short middle name _ signature no 1
	 * @see HibernatePatientDAO#getPatients(String, String, java.util.List, boolean, Integer, Integer, boolean)
	 */
	@Test
	public void getPatients_shouldGetPatientByShortMiddleName_SignatureNo1() throws Exception {
		List<Patient> patients = dao.getPatients("ec", null, null, false, 0, 11, false);
		
		Assert.assertEquals(1, patients.size());
		Assert.assertEquals("ec", patients.get(0).getMiddleName());
	}
	
	/**
	 * @verifies get patient by short family name _ signature no 1
	 * @see HibernatePatientDAO#getPatients(String, String, java.util.List, boolean, Integer, Integer, boolean)
	 */
	@Test
	public void getPatients_shouldGetPatientByShortFamilyName_SignatureNo1() throws Exception {
		List<Patient> patients = dao.getPatients("ki", null, null, false, 0, 11, false);
		
		Assert.assertEquals(2, patients.size());
		Assert.assertEquals("ki", patients.get(0).getFamilyName());
		Assert.assertEquals("ki", patients.get(1).getFamilyName());
		Assert.assertFalse(patients.get(0).getMiddleName().equalsIgnoreCase(patients.get(1).getMiddleName()));
	}
	
	/**
	 * @verifies get patient by short family2 name _ signature no 1
	 * @see HibernatePatientDAO#getPatients(String, String, java.util.List, boolean, Integer, Integer, boolean)
	 */
	@Test
	public void getPatients_shouldGetPatientByShortFamily2Name_SignatureNo1() throws Exception {
		List<Patient> patients = dao.getPatients("os", null, null, false, 0, 11, false);
		
		Assert.assertEquals(1, patients.size());
		Assert.assertEquals("br", patients.get(0).getGivenName());
	}
	
	/**
	 * @verifies get patient by whole name made up of short names _ signature no 1
	 * @see HibernatePatientDAO#getPatients(String, String, java.util.List, boolean, Integer, Integer, boolean)
	 */
	@Test
	public void getPatients_shouldGetPatientByWholeNameMadeUpOfShortNames_SignatureNo1() throws Exception {
		List<Patient> patients = dao.getPatients("br fo ki os", null, null, false, 0, 11, false);
		
		Assert.assertEquals(1, patients.size());
		Assert.assertEquals("fo", patients.get(0).getMiddleName());
	}
	
	/**
	 * @verifies get patients by multiple short name parts _ signature no 1
	 * @see HibernatePatientDAO#getPatients(String, String, java.util.List, boolean, Integer, Integer, boolean)
	 */
	@Test
	public void getPatients_shouldGetPatientsByMultipleShortNameParts_SignatureNo1() throws Exception {
		List<Patient> patients = dao.getPatients("al mi", null, null, false, 0, 11, false);
		
		Assert.assertEquals(1, patients.size());
		Assert.assertEquals("ec", patients.get(0).getMiddleName());
	}
	
	/**
	 * @verifies not get patient by non-existing single short name _ signature no 1
	 * @see HibernatePatientDAO#getPatients(String, String, java.util.List, boolean, Integer, Integer, boolean)
	 */
	@Test
	public void getPatients_shouldNotGetPatientByNonexistingSingleShortName_SignatureNo1() throws Exception {
		List<Patient> patients = dao.getPatients("xy", null, null, false, 0, 11, false);
		
		Assert.assertEquals(0, patients.size());
	}
	
	/**
	 * @verifies not get patient by non-existing short name parts _ signature no 1
	 * @see HibernatePatientDAO#getPatients(String, String, java.util.List, boolean, Integer, Integer, boolean)
	 */
	@Test
	public void getPatients_shouldNotGetPatientByNonexistingShortNameParts_SignatureNo1() throws Exception {
		List<Patient> patients = dao.getPatients("xy yz za", null, null, false, 0, 11, false);
		
		Assert.assertEquals(0, patients.size());
	}
	
	/**
	 * @verifies not get patient by mix of existing and non-existing short name parts _ signature no 1
	 * @see HibernatePatientDAO#getPatients(String, String, java.util.List, boolean, Integer, Integer, boolean)
	 */
	@Test
	public void getPatients_shouldNotGetPatientByMixOfExistingAndNonexistingShortNameParts_SignatureNo1() throws Exception {
		List<Patient> patients = dao.getPatients("xy yz al", null, null, false, 0, 11, false);
		
		Assert.assertEquals(0, patients.size());
	}
	
	/**
	 * @verifies not get patient by voided short name _ signature no 1
	 * @see HibernatePatientDAO#getPatients(String, String, java.util.List, boolean, Integer, Integer, boolean)
	 */
	@Test
	public void getPatients_shouldNotGetPatientByVoidedShortName_SignatureNo1() throws Exception {
		List<Patient> patients = dao.getPatients("vd", null, null, false, 0, 11, false);
		
		Assert.assertEquals(0, patients.size());
	}
	
	/**
	 * @verifies get patients with match mode start _ signature no 1
	 * @see HibernatePatientDAO#getPatients(String, String, java.util.List, boolean, Integer, Integer, boolean)
	 */
	@Test
	public void getPatients_shouldGetPatientsWithMatchModeStart_SignatureNo1() throws Exception {
		String oldPropertyValue = globalPropertiesTestHelper.setGlobalProperty(
		    OpenmrsConstants.GLOBAL_PROPERTY_PATIENT_SEARCH_MATCH_MODE,
		    OpenmrsConstants.GLOBAL_PROPERTY_PATIENT_SEARCH_MATCH_START);
		
		List<Patient> patients = dao.getPatients("Bagg", null, null, false, 0, 11, false);
		
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
	 * @verifies get patients with match mode anywhere _ signature no 1
	 * @see HibernatePatientDAO#getPatients(String, String, java.util.List, boolean, Integer, Integer, boolean)
	 */
	@Test
	public void getPatients_shouldGetPatientsWithMatchModeAnywhere_SignatureNo1() throws Exception {
		String oldPropertyValue = globalPropertiesTestHelper.setGlobalProperty(
		    OpenmrsConstants.GLOBAL_PROPERTY_PATIENT_SEARCH_MATCH_MODE,
		    OpenmrsConstants.GLOBAL_PROPERTY_PATIENT_SEARCH_MATCH_ANYWHERE);
		
		List<Patient> patients = dao.getPatients("aggins", null, null, false, 0, 11, false);
		
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
	 * @verifies not get patients with match mode start _ signature no 1
	 * @see HibernatePatientDAO#getPatients(String, String, java.util.List, boolean, Integer, Integer, boolean)
	 */
	@Test
	public void getPatients_shouldNotGetPatientsWithMatchModeStart_SignatureNo1() throws Exception {
		String oldPropertyValue = globalPropertiesTestHelper.setGlobalProperty(
		    OpenmrsConstants.GLOBAL_PROPERTY_PATIENT_SEARCH_MATCH_MODE,
		    OpenmrsConstants.GLOBAL_PROPERTY_PATIENT_SEARCH_MATCH_START);
		
		List<Patient> patients = dao.getPatients("xyz", null, null, false, 0, 11, false);
		
		if (oldPropertyValue != null) {
			globalPropertiesTestHelper.setGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_PATIENT_SEARCH_MATCH_MODE,
			    oldPropertyValue);
		} else {
			globalPropertiesTestHelper.purgeGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_PATIENT_SEARCH_MATCH_MODE);
		}
	}
	
	/**
	 * @verifies not get patients with match mode anywhere _ signature no 1
	 * @see HibernatePatientDAO#getPatients(String, String, java.util.List, boolean, Integer, Integer, boolean)
	 */
	@Test
	public void getPatients_shouldNotGetPatientsWithMatchModeAnywhere_SignatureNo1() throws Exception {
		String oldPropertyValue = globalPropertiesTestHelper.setGlobalProperty(
		    OpenmrsConstants.GLOBAL_PROPERTY_PATIENT_SEARCH_MATCH_MODE,
		    OpenmrsConstants.GLOBAL_PROPERTY_PATIENT_SEARCH_MATCH_ANYWHERE);
		
		List<Patient> patients = dao.getPatients("xyz", null, null, false, 0, 11, false);
		
		Assert.assertEquals(0, patients.size());
		
		if (oldPropertyValue != null) {
			globalPropertiesTestHelper.setGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_PATIENT_SEARCH_MATCH_MODE,
			    oldPropertyValue);
		} else {
			globalPropertiesTestHelper.purgeGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_PATIENT_SEARCH_MATCH_MODE);
		}
	}
	
	/**
	 * @verifies get patient by identifier _ signature no 1
	 * @see HibernatePatientDAO#getPatients(String, String, java.util.List, boolean, Integer, Integer, boolean)
	 */
	@Test
	public void getPatients_shouldGetPatientByIdentifier_SignatureNo1() throws Exception {
		List<Patient> patients = dao.getPatients(null, "42-42-42", null, false, 0, 11, false);
		
		Assert.assertEquals(1, patients.size());
		Assert.assertEquals("Bilbo Odilon", patients.get(0).getGivenName());
	}
	
	/**
	 * @verifies not get patient by non-existing identifier _ signature no 1
	 * @see HibernatePatientDAO#getPatients(String, String, java.util.List, boolean, Integer, Integer, boolean)
	 */
	@Test
	public void getPatients_shouldNotGetPatientByNonexistingIdentifier_SignatureNo1() throws Exception {
		List<Patient> patients = dao.getPatients(null, "xy-xy-xy", null, false, 0, 11, false);
		
		Assert.assertEquals(0, patients.size());
	}
	
	/**
	 * @verifies not get patient by voided identifier _ signature no 1
	 * @see HibernatePatientDAO#getPatients(String, String, java.util.List, boolean, Integer, Integer, boolean)
	 */
	@Test
	public void getPatients_shouldNotGetPatientByVoidedIdentifier_SignatureNo1() throws Exception {
		List<Patient> patients = dao.getPatients(null, "voided-42", null, false, 0, 11, false);
		
		Assert.assertEquals(0, patients.size());
	}
	
	/**
	 * @verifies not get patient by empty identifier _ signature no 1
	 * @see HibernatePatientDAO#getPatients(String, String, java.util.List, boolean, Integer, Integer, boolean)
	 */
	@Test
	public void getPatients_shouldNotGetPatientByEmptyIdentifier_SignatureNo1() throws Exception {
		List<Patient> patients = dao.getPatients(null, "", null, false, 0, 11, false);
		
		Assert.assertEquals(0, patients.size());
	}
	
	/**
	 * @verifies not get patient by null identifier _ signature no 1
	 * @see HibernatePatientDAO#getPatients(String, String, java.util.List, boolean, Integer, Integer, boolean)
	 */
	@Test
	public void getPatients_shouldNotGetPatientByNullIdentifier_SignatureNo1() throws Exception {
		List<Patient> patients = dao.getPatients(null, null, null, false, 0, 11, false);
		Assert.assertEquals(0, patients.size());
	}
	
	/**
	 * @verifies get patient by searching on names or identifiers and using name value as identifier parameter _ signature no 1
	 * @see HibernatePatientDAO#getPatients(String, String, java.util.List, boolean, Integer, Integer, boolean)
	 */
	@Test
	public void getPatients_shouldGetPatientBySearchingOnNamesOrIdentifiersAndUsingNameValueAsIdentifierParameter_SignatureNo1()
	        throws Exception {
		List<Patient> patients = dao.getPatients(null, "Bilbo Odilon", null, false, 0, 11, true);
		
		Assert.assertEquals(1, patients.size());
		Assert.assertEquals("Bilbo Odilon", patients.get(0).getGivenName());
	}
	
	/**
	 * @verifies get patient by searching on names or identifiers and using identifier value as name parameter _ signature no 1
	 * @see HibernatePatientDAO#getPatients(String, String, java.util.List, boolean, Integer, Integer, boolean)
	 */
	@Test
	public void getPatients_shouldGetPatientBySearchingOnNamesOrIdentifiersAndUsingIdentifierValueAsNameParameter_SignatureNo1()
	        throws Exception {
		List<Patient> patients = dao.getPatients("42-42-42", null, null, false, 0, 11, true);
		
		Assert.assertEquals(1, patients.size());
		Assert.assertEquals("Bilbo Odilon", patients.get(0).getGivenName());
	}
	
	/**
	 * @verifies get one patient by multiple name parts _ signature no 1
	 * @see HibernatePatientDAO#getPatients(String, String, java.util.List, boolean, Integer, Integer, boolean)
	 */
	@Test
	public void getPatients_shouldGetOnePatientByMultipleNameParts_SignatureNo1() throws Exception {
		List<Patient> patients = dao.getPatients("Bilbo Odilon B.", null, null, false, 0, 11, false);
		
		Assert.assertEquals(1, patients.size());
		Assert.assertEquals("Bilbo Odilon", patients.get(0).getGivenName());
	}
	
	/**
	 * @verifies not get patients by empty query _ signature no 2
	 * @see HibernatePatientDAO#getPatients(String, Integer, Integer)
	 */
	@Test
	public void getPatients_shouldNotGetPatientsByEmptyQuery_SignatureNo2() throws Exception {
		List<Patient> patients = dao.getPatients("", 0, 11);
		Assert.assertEquals(0, patients.size());
	}
	
	/**
	 * @verifies not get patients by null query _ signature no 2
	 * @see HibernatePatientDAO#getPatients(String, Integer, Integer)
	 */
	@Test
	public void getPatients_shouldNotGetPatientsByNullQuery_SignatureNo2() throws Exception {
		List<Patient> patients = dao.getPatients("", 0, 11);
		Assert.assertEquals(0, patients.size());
	}
	
	/**
	 * @verifies get patient by given name _ signature no 2
	 * @see HibernatePatientDAO#getPatients(String, Integer, Integer)
	 */
	@Test
	public void getPatients_shouldGetPatientByGivenName_SignatureNo2() throws Exception {
		List<Patient> patients = dao.getPatients("Bilbo Odilon", 0, 11);
		
		Assert.assertEquals(1, patients.size());
		Assert.assertEquals("Bilbo Odilon", patients.get(0).getGivenName());
	}
	
	/**
	 * @verifies get patient by middle name _ signature no 2
	 * @see HibernatePatientDAO#getPatients(String, Integer, Integer)
	 */
	@Test
	public void getPatients_shouldGetPatientByMiddleName_SignatureNo2() throws Exception {
		List<Patient> patients = dao.getPatients("B.", 0, 11);
		
		Assert.assertEquals(1, patients.size());
		Assert.assertEquals("Bilbo Odilon", patients.get(0).getGivenName());
	}
	
	/**
	 * @verifies get patient by family name _ signature no 2
	 * @see HibernatePatientDAO#getPatients(String, Integer, Integer)
	 */
	@Test
	public void getPatients_shouldGetPatientByFamilyName_SignatureNo2() throws Exception {
		List<Patient> patients = dao.getPatients("Baggins", 0, 11);
		
		Assert.assertEquals(2, patients.size());
		Assert.assertEquals("Baggins", patients.get(0).getFamilyName());
		Assert.assertEquals("Baggins", patients.get(1).getFamilyName());
		Assert.assertFalse(patients.get(0).getGivenName().equalsIgnoreCase(patients.get(1).getGivenName()));
	}
	
	/**
	 * @verifies get patient by family2 name _ signature no 2
	 * @see HibernatePatientDAO#getPatients(String, Integer, Integer)
	 */
	@Test
	public void getPatients_shouldGetPatientByFamily2Name_SignatureNo2() throws Exception {
		List<Patient> patients = dao.getPatients("Junior", 0, 11);
		
		Assert.assertEquals(1, patients.size());
		Assert.assertEquals("Frodo Ansilon", patients.get(0).getGivenName());
	}
	
	/**
	 * @verifies get patient by whole name _ signature no 2
	 * @see HibernatePatientDAO#getPatients(String, Integer, Integer)
	 */
	@Test
	public void getPatients_shouldGetPatientByWholeName_SignatureNo2() throws Exception {
		List<Patient> patients = dao.getPatients("Bilbo Odilon B. Baggins Senior", 0, 11);
		
		Assert.assertEquals(1, patients.size());
		Assert.assertEquals("Bilbo Odilon", patients.get(0).getGivenName());
	}
	
	/**
	 * @verifies not get patient by non-existing single name _ signature no 2
	 * @see HibernatePatientDAO#getPatients(String, Integer, Integer)
	 */
	@Test
	public void getPatients_shouldNotGetPatientByNonexistingSingleName_SignatureNo2() throws Exception {
		List<Patient> patients = dao.getPatients("Peter", 0, 11);
		Assert.assertEquals(0, patients.size());
	}
	
	/**
	 * @verifies not get patient by non-existing name parts _ signature no 2
	 * @see HibernatePatientDAO#getPatients(String, Integer, Integer)
	 */
	@Test
	public void getPatients_shouldNotGetPatientByNonexistingNameParts_SignatureNo2() throws Exception {
		List<Patient> patients = dao.getPatients("Sam Gamdschie Eldest", 0, 11);
		Assert.assertEquals(0, patients.size());
	}
	
	/**
	 * @verifies not get patient by mix of existing and non-existing name parts _ signature no 2
	 * @see HibernatePatientDAO#getPatients(String, Integer, Integer)
	 */
	@Test
	public void getPatients_shouldNotGetPatientByMixOfExistingAndNonexistingNameParts_SignatureNo2() throws Exception {
		List<Patient> patients = dao.getPatients("Bilbo Odilon X. Baggins", 0, 11);
		Assert.assertEquals(0, patients.size());
	}
	
	/**
	 * @verifies not get patient by voided name _ signature no 2
	 * @see HibernatePatientDAO#getPatients(String, Integer, Integer)
	 */
	@Test
	public void getPatients_shouldNotGetPatientByVoidedName_SignatureNo2() throws Exception {
		List<Patient> patients = dao.getPatients("voided-delta", 0, 11);
		Assert.assertEquals(0, patients.size());
	}
	
	/**
	 * @verifies get patient by short given name _ signature no 2
	 * @see HibernatePatientDAO#getPatients(String, Integer, Integer)
	 */
	@Test
	public void getPatients_shouldGetPatientByShortGivenName_SignatureNo2() throws Exception {
		List<Patient> patients = dao.getPatients("al", 0, 11);
		
		Assert.assertEquals(2, patients.size());
		Assert.assertEquals("al", patients.get(0).getGivenName());
		Assert.assertEquals("al", patients.get(1).getGivenName());
		Assert.assertFalse(patients.get(0).getMiddleName().equalsIgnoreCase(patients.get(1).getMiddleName()));
	}
	
	/**
	 * @verifies get patient by short middle name _ signature no 2
	 * @see HibernatePatientDAO#getPatients(String, Integer, Integer)
	 */
	@Test
	public void getPatients_shouldGetPatientByShortMiddleName_SignatureNo2() throws Exception {
		List<Patient> patients = dao.getPatients("ec", 0, 11);
		
		Assert.assertEquals(1, patients.size());
		Assert.assertEquals("ec", patients.get(0).getMiddleName());
	}
	
	/**
	 * @verifies get patient by short family name _ signature no 2
	 * @see HibernatePatientDAO#getPatients(String, Integer, Integer)
	 */
	@Test
	public void getPatients_shouldGetPatientByShortFamilyName_SignatureNo2() throws Exception {
		List<Patient> patients = dao.getPatients("ki", 0, 11);
		
		Assert.assertEquals(2, patients.size());
		Assert.assertEquals("ki", patients.get(0).getFamilyName());
		Assert.assertEquals("ki", patients.get(1).getFamilyName());
		Assert.assertFalse(patients.get(0).getMiddleName().equalsIgnoreCase(patients.get(1).getMiddleName()));
	}
	
	/**
	 * @verifies get patient by short family2 name _ signature no 2
	 * @see HibernatePatientDAO#getPatients(String, Integer, Integer)
	 */
	@Test
	public void getPatients_shouldGetPatientByShortFamily2Name_SignatureNo2() throws Exception {
		List<Patient> patients = dao.getPatients("os", 0, 11);
		
		Assert.assertEquals(1, patients.size());
		Assert.assertEquals("br", patients.get(0).getGivenName());
	}
	
	/**
	 * @verifies get patient by whole name made up of short names _ signature no 2
	 * @see HibernatePatientDAO#getPatients(String, Integer, Integer)
	 */
	@Test
	public void getPatients_shouldGetPatientByWholeNameMadeUpOfShortNames_SignatureNo2() throws Exception {
		List<Patient> patients = dao.getPatients("br fo ki os", 0, 11);
		
		Assert.assertEquals(1, patients.size());
		Assert.assertEquals("fo", patients.get(0).getMiddleName());
	}
	
	/**
	 * @verifies get patients by multiple short name parts _ signature no 2
	 * @see HibernatePatientDAO#getPatients(String, Integer, Integer)
	 */
	@Test
	public void getPatients_shouldGetPatientsByMultipleShortNameParts_SignatureNo2() throws Exception {
		List<Patient> patients = dao.getPatients("al mi", 0, 11);
		
		Assert.assertEquals(1, patients.size());
		Assert.assertEquals("ec", patients.get(0).getMiddleName());
	}
	
	/**
	 * @verifies not get patient by non-existing single short name _ signature no 2
	 * @see HibernatePatientDAO#getPatients(String, Integer, Integer)
	 */
	@Test
	public void getPatients_shouldNotGetPatientByNonexistingSingleShortName_SignatureNo2() throws Exception {
		List<Patient> patients = dao.getPatients("xy", 0, 11);
		
		Assert.assertEquals(0, patients.size());
	}
	
	/**
	 * @verifies not get patient by non-existing short name parts _ signature no 2
	 * @see HibernatePatientDAO#getPatients(String, Integer, Integer)
	 */
	@Test
	public void getPatients_shouldNotGetPatientByNonexistingShortNameParts_SignatureNo2() throws Exception {
		List<Patient> patients = dao.getPatients("xy yz za", 0, 11);
		
		Assert.assertEquals(0, patients.size());
	}
	
	/**
	 * @verifies not get patient by mix of existing and non-existing short name parts _ signature no 2
	 * @see HibernatePatientDAO#getPatients(String, Integer, Integer)
	 */
	@Test
	public void getPatients_shouldNotGetPatientByMixOfExistingAndNonexistingShortNameParts_SignatureNo2() throws Exception {
		List<Patient> patients = dao.getPatients("xy yz al", 0, 11);
		
		Assert.assertEquals(0, patients.size());
	}
	
	/**
	 * @verifies not get patient by voided short name _ signature no 2
	 * @see HibernatePatientDAO#getPatients(String, Integer, Integer)
	 */
	@Test
	public void getPatients_shouldNotGetPatientByVoidedShortName_SignatureNo2() throws Exception {
		List<Patient> patients = dao.getPatients("vd", 0, 11);
		Assert.assertEquals(0, patients.size());
	}
	
	/**
	 * @verifies get patient by identifier _ signature no 2
	 * @see HibernatePatientDAO#getPatients(String, Integer, Integer)
	 */
	@Test
	public void getPatients_shouldGetPatientByIdentifier_SignatureNo2() throws Exception {
		List<Patient> patients = dao.getPatients("42-42-42", 0, 11);
		Assert.assertEquals(1, patients.size());
		Assert.assertEquals("Bilbo Odilon", patients.get(0).getGivenName());
	}
	
	/**
	 * @verifies not get patient by non-existing identifier _ signature no 2
	 * @see HibernatePatientDAO#getPatients(String, Integer, Integer)
	 */
	@Test
	public void getPatients_shouldNotGetPatientByNonexistingIdentifier_SignatureNo2() throws Exception {
		List<Patient> patients = dao.getPatients("xy-xy-xy", 0, 11);
		Assert.assertEquals(0, patients.size());
	}
	
	/**
	 * @verifies not get patient by voided identifier _ signature no 2
	 * @see HibernatePatientDAO#getPatients(String, Integer, Integer)
	 */
	@Test
	public void getPatients_shouldNotGetPatientByVoidedIdentifier_SignatureNo2() throws Exception {
		List<Patient> patients = dao.getPatients("voided-42", 0, 11);
		Assert.assertEquals(0, patients.size());
	}
	
	/**
	 * @verifies get no patient by non-existing attribute _ signature no 2
	 * @see HibernatePatientDAO#getPatients(String, Integer, Integer)
	 */
	@Test
	public void getPatients_shouldGetNoPatientByNonexistingAttribute_SignatureNo2() throws Exception {
		Assert.assertFalse(personAttributeHelper.personAttributeExists("Wizard"));
		
		List<Patient> patients = dao.getPatients("Wizard", 0, 11);
		Assert.assertEquals(0, patients.size());
	}
	
	/**
	 * @verifies get no patient by non-searchable attribute _ signature no 2
	 * @see HibernatePatientDAO#getPatients(String, Integer, Integer)
	 */
	@Test
	public void getPatients_shouldGetNoPatientByNonsearchableAttribute_SignatureNo2() throws Exception {
		Assert.assertTrue(personAttributeHelper.nonSearchablePersonAttributeExists("Mushroom pie"));
		List<Patient> patients = dao.getPatients("Mushroom pie", 0, 11);
		Assert.assertEquals(0, patients.size());
	}
	
	/**
	 * @verifies get no patient by voided attribute _ signature no 2
	 * @see HibernatePatientDAO#getPatients(String, Integer, Integer)
	 */
	@Test
	public void getPatients_shouldGetNoPatientByVoidedAttribute_SignatureNo2() throws Exception {
		Assert.assertTrue(personAttributeHelper.voidedPersonAttributeExists("Master thief"));
		List<Patient> patients = dao.getPatients("Master thief", 0, 11);
		Assert.assertEquals(0, patients.size());
	}
	
	/**
	 * @verifies get one patient by attribute _ signature no 2
	 * @see HibernatePatientDAO#getPatients(String, Integer, Integer)
	 */
	@Test
	public void getPatients_shouldGetOnePatientByAttribute_SignatureNo2() throws Exception {
		globalPropertiesTestHelper.setGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_PERSON_ATTRIBUTE_SEARCH_MATCH_MODE,
		    OpenmrsConstants.GLOBAL_PROPERTY_PERSON_ATTRIBUTE_SEARCH_MATCH_ANYWHERE);
		Assert.assertTrue(personAttributeHelper.personAttributeExists("Story teller"));
		List<Patient> patients = dao.getPatients("Story teller", 0, 11);
		Assert.assertEquals(1, patients.size());
	}
	
	/**
	 * @verifies get one patient by random case attribute _ signature no 2
	 * @see HibernatePatientDAO#getPatients(String, Integer, Integer)
	 */
	@Test
	public void getPatients_shouldGetOnePatientByRandomCaseAttribute_SignatureNo2() throws Exception {
		globalPropertiesTestHelper.setGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_PERSON_ATTRIBUTE_SEARCH_MATCH_MODE,
		    OpenmrsConstants.GLOBAL_PROPERTY_PERSON_ATTRIBUTE_SEARCH_MATCH_ANYWHERE);
		Assert.assertTrue(personAttributeHelper.personAttributeExists("Story teller"));
		List<Patient> patients = dao.getPatients("STORY teller", 0, 11);
		Assert.assertEquals(1, patients.size());
	}
	
	/**
	 * @verifies not get patients by searching for non-voided and voided attribute _ signature no 2
	 * @see HibernatePatientDAO#getPatients(String, Integer, Integer)
	 */
	@Test
	public void getPatients_shouldNotGetPatientsBySearchingForNonvoidedAndVoidedAttribute_SignatureNo2() throws Exception {
		Assert.assertTrue(personAttributeHelper.personAttributeExists("Story teller"));
		Assert.assertFalse(personAttributeHelper.voidedPersonAttributeExists("Story teller"));
		Assert.assertTrue(personAttributeHelper.voidedPersonAttributeExists("Master thief"));
		List<Patient> patients = dao.getPatients("Story Thief", 0, 11);
		Assert.assertEquals(0, patients.size());
	}
	
	/**
	 * @verifies get multiple patients by single attribute _ signature no 2
	 * @see HibernatePatientDAO#getPatients(String, Integer, Integer)
	 */
	@Test
	public void getPatients_shouldGetMultiplePatientsBySingleAttribute_SignatureNo2() throws Exception {
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
	 * @verifies not get patients by multiple attributes _ signature no 2
	 * @see HibernatePatientDAO#getPatients(String, Integer, Integer)
	 */
	@Test
	public void getPatients_shouldNotGetPatientsByMultipleAttributes_SignatureNo2() throws Exception {
		Assert.assertTrue(personAttributeHelper.searchablePersonAttributeExists("Story teller"));
		Assert.assertTrue(personAttributeHelper.nonVoidedPersonAttributeExists("Story teller"));
		
		Assert.assertTrue(personAttributeHelper.searchablePersonAttributeExists("Senior ring bearer"));
		Assert.assertTrue(personAttributeHelper.nonVoidedPersonAttributeExists("Senior ring bearer"));
		
		List<Patient> patients = dao.getPatients("Story bearer", 0, 11);
		Assert.assertEquals(0, patients.size());
	}
	
	/**
	 * @verifies find eleven out of eleven patients _ signature no 2
	 * @see HibernatePatientDAO#getPatients(String, Integer, Integer)
	 */
	@Test
	public void getPatients_shouldFindElevenOutOfElevenPatients_SignatureNo2() throws Exception {
		List<Patient> firstFourPatients = dao.getPatients("Saruman", 0, 11);
		Assert.assertEquals(11, firstFourPatients.size());
	}
	
	/**
	 * @verifies find the first four out of eleven patients _ signature no 2
	 * @see HibernatePatientDAO#getPatients(String, Integer, Integer)
	 */
	@Test
	public void getPatients_shouldFindTheFirstFourOutOfElevenPatients_SignatureNo2() throws Exception {
		List<Patient> firstFourPatients = dao.getPatients("Saruman", 0, 4);
		Assert.assertEquals(4, firstFourPatients.size());
	}
	
	/**
	 * @verifies find the next four out of eleven patients _ signature no 2
	 * @see HibernatePatientDAO#getPatients(String, Integer, Integer)
	 */
	@Test
	public void getPatients_shouldFindTheNextFourOutOfElevenPatients_SignatureNo2() throws Exception {
		List<Patient> firstFourPatients = dao.getPatients("Saruman", 0, 4);
		Assert.assertEquals(4, firstFourPatients.size());
		
		List<Patient> nextFourPatients = dao.getPatients("Saruman", 4, 4);
		Assert.assertEquals(4, nextFourPatients.size());
		
		for (Patient patient : nextFourPatients) {
			Assert.assertFalse(firstFourPatients.contains(patient));
		}
	}
	
	/**
	 * @verifies find the remaining three out of eleven patients _ signature no 2
	 * @see HibernatePatientDAO#getPatients(String, Integer, Integer)
	 */
	@Test
	public void getPatients_shouldFindTheRemainingThreeOutOfElevenPatients_SignatureNo2() throws Exception {
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
	 * @verifies find patients with null as start _ signature no 2
	 * @see HibernatePatientDAO#getPatients(String, Integer, Integer)
	 */
	@Test
	public void getPatients_shouldFindPatientsWithNullAsStart_SignatureNo2() throws Exception {
		List<Patient> patients = dao.getPatients("Saruman", null, 11);
		Assert.assertEquals(11, patients.size());
	}
	
	/**
	 * @verifies find patients with negative start _ signature no 2
	 * @see HibernatePatientDAO#getPatients(String, Integer, Integer)
	 */
	@Test
	public void getPatients_shouldFindPatientsWithNegativeStart_SignatureNo2() throws Exception {
		List<Patient> patients = dao.getPatients("Saruman", -7, 11);
		Assert.assertEquals(11, patients.size());
	}
	
	/**
	 * @verifies find patients with null as length _ signature no 2
	 * @see HibernatePatientDAO#getPatients(String, Integer, Integer)
	 */
	@Test
	public void getPatients_shouldFindPatientsWithNullAsLength_SignatureNo2() throws Exception {
		List<Patient> patients = dao.getPatients("Saruman", 0, null);
		Assert.assertEquals(11, patients.size());
	}
	
	/**
	 * @verifies not get patients by zero length _ signature no 2
	 * @see HibernatePatientDAO#getPatients(String, Integer, Integer)
	 */
	@Test
	public void getPatients_shouldNotGetPatientsByZeroLength_SignatureNo2() throws Exception {
		List<Patient> patients = dao.getPatients("Saruman", 0, 0);
		Assert.assertEquals(0, patients.size());
	}
	
	/**
	 * @verifies not get patients by negative length _ signature no 2
	 * @see HibernatePatientDAO#getPatients(String, Integer, Integer)
	 */
	@Test
	public void getPatients_shouldNotGetPatientsByNegativeLength_SignatureNo2() throws Exception {
		List<Patient> patients = dao.getPatients("Saruman", 0, -7);
		Assert.assertEquals(0, patients.size());
	}
	
	/**
	 * @verifies find patients with excessive length _ signature no 2
	 * @see HibernatePatientDAO#getPatients(String, Integer, Integer)
	 */
	@Test
	public void getPatients_shouldFindPatientsWithExcessiveLength_SignatureNo2() throws Exception {
		List<Patient> patients = dao.getPatients("Saruman", 0, HibernatePersonDAO.getMaximumSearchResults() + 42);
		Assert.assertEquals(11, patients.size());
	}
	
	/**
	 * @verifies return distinct patient list _ signature no 2
	 * @see HibernatePatientDAO#getPatients(String, Integer, Integer)
	 */
	@Test
	public void getPatients_shouldReturnDistinctPatientList_SignatureNo2() throws Exception {
		Assert.assertTrue(personAttributeHelper.searchablePersonAttributeExists("Cook"));
		
		List<Patient> patientsByName = dao.getPatients("Adalgrim Took Cook", 0, 11);
		Assert.assertEquals(1, patientsByName.size());
		
		List<Patient> patientsByNameOrAttribute = dao.getPatients("Cook", 0, 11);
		Assert.assertEquals(1, patientsByNameOrAttribute.size());
	}
	
	/**
	 * @verifies not match voided patients _ signature no 2
	 * @see HibernatePatientDAO#getPatients(String, Integer, Integer)
	 */
	@Test
	public void getPatients_shouldNotMatchVoidedPatients_SignatureNo2() throws Exception {
		List<Patient> patients = dao.getPatients("Meriadoc", 0, 11);
		Assert.assertEquals(0, patients.size());
	}
	
	/**
	 * @verifies get patients with match mode start _ signature no 2
	 * @see HibernatePatientDAO#getPatients(String, Integer, Integer)
	 */
	@Test
	public void getPatients_shouldGetPatientsWithMatchModeStart_SignatureNo2() throws Exception {
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
	 * @verifies get patients with match mode anywhere _ signature no 2
	 * @see HibernatePatientDAO#getPatients(String, Integer, Integer)
	 */
	@Test
	public void getPatients_shouldGetPatientsWithMatchModeAnywhere_SignatureNo2() throws Exception {
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
	 * @verifies not get patients with match mode start _ signature no 2
	 * @see HibernatePatientDAO#getPatients(String, Integer, Integer)
	 */
	@Test
	public void getPatients_shouldNotGetPatientsWithMatchModeStart_SignatureNo2() throws Exception {
		String oldPropertyValue = globalPropertiesTestHelper.setGlobalProperty(
		    OpenmrsConstants.GLOBAL_PROPERTY_PATIENT_SEARCH_MATCH_MODE,
		    OpenmrsConstants.GLOBAL_PROPERTY_PATIENT_SEARCH_MATCH_START);
		
		List<Patient> patients = dao.getPatients("xyz", 0, 11);
		
		if (oldPropertyValue != null) {
			globalPropertiesTestHelper.setGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_PATIENT_SEARCH_MATCH_MODE,
			    oldPropertyValue);
		} else {
			globalPropertiesTestHelper.purgeGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_PATIENT_SEARCH_MATCH_MODE);
		}
	}
	
	/**
	 * @verifies not get patients with match mode anywhere _ signature no 2
	 * @see HibernatePatientDAO#getPatients(String, Integer, Integer)
	 */
	@Test
	public void getPatients_shouldNotGetPatientsWithMatchModeAnywhere_SignatureNo2() throws Exception {
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
	 * @verifies count zero patients when name and identifier and list of identifier types are empty _ signature no 1
	 * @see HibernatePatientDAO#getCountOfPatients(String, String, java.util.List, boolean, boolean)
	 */
	@Test
	public void getCountOfPatients_shouldCountZeroPatientsWhenNameAndIdentifierAndListOfIdentifierTypesAreEmpty_SignatureNo1()
	        throws Exception {
		List<PatientIdentifierType> identifierTypes = Collections.emptyList();
		long patientCount = dao.getCountOfPatients("", "", identifierTypes, false, false);
		Assert.assertEquals(0, patientCount);
	}
	
	/**
	 * @verifies count zero patients when name and identifier and list of identifier types are null _ signature no 1
	 * @see HibernatePatientDAO#getCountOfPatients(String, String, java.util.List, boolean, boolean)
	 */
	@Test
	public void getCountOfPatients_shouldCountZeroPatientsWhenNameAndIdentifierAndListOfIdentifierTypesAreNull_SignatureNo1()
	        throws Exception {
		List<PatientIdentifierType> identifierTypes = Collections.emptyList();
		long patientCount = dao.getCountOfPatients(null, null, null, false, false);
		Assert.assertEquals(0, patientCount);
	}
	
	/**
	 * @verifies count zero patients for non-matching query _ signature no 1
	 * @see HibernatePatientDAO#getCountOfPatients(String, String, java.util.List, boolean, boolean)
	 */
	@Test
	public void getCountOfPatients_shouldCountZeroPatientsForNonmatchingQuery_SignatureNo1() throws Exception {
		List<PatientIdentifierType> identifierTypes = Collections.emptyList();
		long patientCount = dao.getCountOfPatients("a random query value", null, identifierTypes, false, true);
		Assert.assertEquals(0, patientCount);
	}
	
	/**
	 * @verifies not count voided patients _ signature no 1
	 * @see HibernatePatientDAO#getCountOfPatients(String, String, java.util.List, boolean, boolean)
	 */
	@Test
	public void getCountOfPatients_shouldNotCountVoidedPatients_SignatureNo1() throws Exception {
		List<PatientIdentifierType> identifierTypes = Collections.emptyList();
		long patientCount = dao.getCountOfPatients("Meriadoc Brandybuck", null, identifierTypes, false, false);
		Assert.assertEquals(0, patientCount);
	}
	
	/**
	 * @verifies count single patient _ signature no 1
	 * @see HibernatePatientDAO#getCountOfPatients(String, String, java.util.List, boolean, boolean)
	 */
	@Test
	public void getCountOfPatients_shouldCountSinglePatient_SignatureNo1() throws Exception {
		List<PatientIdentifierType> identifierTypes = Collections.emptyList();
		long patientCount = dao.getCountOfPatients("Bilbo", null, identifierTypes, false, false);
		Assert.assertEquals(1, patientCount);
	}
	
	/**
	 * @verifies count multiple patients _ signature no 1
	 * @see HibernatePatientDAO#getCountOfPatients(String, String, java.util.List, boolean, boolean)
	 */
	@Test
	public void getCountOfPatients_shouldCountMultiplePatients_SignatureNo1() throws Exception {
		List<PatientIdentifierType> identifierTypes = Collections.emptyList();
		long patientCount = dao.getCountOfPatients("Saruman", null, identifierTypes, false, false);
		Assert.assertEquals(11, patientCount);
	}
	
	/**
	 * @verifies count patients by name _ signature no 1
	 * @see HibernatePatientDAO#getCountOfPatients(String, String, java.util.List, boolean, boolean)
	 */
	@Test
	public void getCountOfPatients_shouldCountPatientsByName_SignatureNo1() throws Exception {
		List<PatientIdentifierType> identifierTypes = Collections.emptyList();
		long patientCount = dao.getCountOfPatients("Bilbo", null, identifierTypes, false, false);
		Assert.assertEquals(1, patientCount);
	}
	
	/**
	 * @verifies count patients by identifier _ signature no 1
	 * @see HibernatePatientDAO#getCountOfPatients(String, String, java.util.List, boolean, boolean)
	 */
	@Test
	public void getCountOfPatients_shouldCountPatientsByIdentifier_SignatureNo1() throws Exception {
		List<PatientIdentifierType> identifierTypes = Collections.emptyList();
		long patientCount = dao.getCountOfPatients(null, "42-42-42", identifierTypes, false, false);
		Assert.assertEquals(1, patientCount);
	}
	
	/**
	 * @verifies count zero patients when query is empty _ signature no 2
	 * @see HibernatePatientDAO#getCountOfPatients(String)
	 */
	@Test
	public void getCountOfPatients_shouldCountZeroPatientsWhenQueryIsEmpty_SignatureNo2() throws Exception {
		long patientCount = dao.getCountOfPatients("");
		Assert.assertEquals(0, patientCount);
	}
	
	/**
	 * @verifies count zero patients when query is null _ signature no 2
	 * @see HibernatePatientDAO#getCountOfPatients(String)
	 */
	@Test
	public void getCountOfPatients_shouldCountZeroPatientsWhenQueryIsNull_SignatureNo2() throws Exception {
		long patientCount = dao.getCountOfPatients(null);
		Assert.assertEquals(0, patientCount);
	}
	
	/**
	 * @verifies count zero patients for non-matching query _ signature no 2
	 * @see HibernatePatientDAO#getCountOfPatients(String)
	 */
	@Test
	public void getCountOfPatients_shouldCountZeroPatientsForNonmatchingQuery_SignatureNo2() throws Exception {
		long patientCount = dao.getCountOfPatients("a random query value");
		Assert.assertEquals(0, patientCount);
	}
	
	/**
	 * @verifies not count voided patients _ signature no 2
	 * @see HibernatePatientDAO#getCountOfPatients(String)
	 */
	@Test
	public void getCountOfPatients_shouldNotCountVoidedPatients_SignatureNo2() throws Exception {
		long patientCount = dao.getCountOfPatients("Meriadoc Brandybuck");
		Assert.assertEquals(0, patientCount);
	}
	
	/**
	 * @verifies count single patient _ signature no 2
	 * @see HibernatePatientDAO#getCountOfPatients(String)
	 */
	@Test
	public void getCountOfPatients_shouldCountSinglePatient_SignatureNo2() throws Exception {
		long patientCount = dao.getCountOfPatients("Bilbo");
		Assert.assertEquals(1, patientCount);
	}
	
	/**
	 * @verifies count multiple patients _ signature no 2
	 * @see HibernatePatientDAO#getCountOfPatients(String)
	 */
	@Test
	public void getCountOfPatients_shouldCountMultiplePatients_SignatureNo2() throws Exception {
		long patientCount = dao.getCountOfPatients("Saruman");
		Assert.assertEquals(11, patientCount);
	}
	
	/**
	 * @verifies count patients by name _ signature no 2
	 * @see HibernatePatientDAO#getCountOfPatients(String)
	 */
	@Test
	public void getCountOfPatients_shouldCountPatientsByName_SignatureNo2() throws Exception {
		long patientCount = dao.getCountOfPatients("Saruman");
		Assert.assertEquals(11, patientCount);
	}
	
	/**
	 * @verifies count patients by identifier _ signature no 2
	 * @see HibernatePatientDAO#getCountOfPatients(String)
	 */
	@Test
	public void getCountOfPatients_shouldCountPatientsByIdentifier_SignatureNo2() throws Exception {
		long patientCount = dao.getCountOfPatients("42-42-42");
		Assert.assertEquals(1, patientCount);
	}
	
	/**
	 * @verifies count patients by searchable attribute _ signature no 2
	 * @see HibernatePatientDAO#getCountOfPatients(String)
	 */
	@Test
	public void getCountOfPatients_shouldCountPatientsBySearchableAttribute_SignatureNo2() throws Exception {
		globalPropertiesTestHelper.setGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_PERSON_ATTRIBUTE_SEARCH_MATCH_MODE,
		    OpenmrsConstants.GLOBAL_PROPERTY_PERSON_ATTRIBUTE_SEARCH_MATCH_ANYWHERE);
		long patientCount = dao.getCountOfPatients("Story teller");
		Assert.assertEquals(1, patientCount);
	}
	
	/**
	 * @verifies return exact match first
	 * @see HibernatePatientDAO#getPatients(String, Integer, Integer)
	 */
	@Test
	public void getPatients_shouldReturnExactMatchFirst() throws Exception {
		List<Patient> patients = dao.getPatients("Ben", 0, 11);
		
		Assert.assertEquals(4, patients.size());
		Assert.assertEquals("Alan", patients.get(0).getGivenName());
		Assert.assertEquals("Ben", patients.get(1).getGivenName());
		Assert.assertEquals("Adam", patients.get(2).getGivenName());
		Assert.assertEquals("Benedict", patients.get(3).getGivenName());
		
		patients = dao.getPatients("Ben Frank", 0, 11);
		
		Assert.assertEquals(4, patients.size());
		Assert.assertEquals("Ben", patients.get(0).getGivenName());
		Assert.assertEquals("Alan", patients.get(1).getGivenName());
		Assert.assertEquals("Benedict", patients.get(2).getGivenName());
		Assert.assertEquals("Adam", patients.get(3).getGivenName());
	}
	
	/**
	 * @verifies obey attribute match mode
	 * @see HibernatePatientDAO#getCountOfPatients(String)
	 */
	@Test
	public void getCountOfPatients_shouldObeyAttributeMatchMode() throws Exception {
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
	 * @verifies get voided person when voided=true is passed
	 * @see PatientDAO#getPatients(String, boolean, Integer, Integer)
	 */
	@Test
	public void getPatients_shouldGetVoidedPersonWhenVoidedTrueIsPassed() throws Exception {
		List<Patient> patients = dao.getPatients("voided-bravo", true, 0, 11);
		Assert.assertEquals(1, patients.size());
	}
	
	/**
	 * @verifies get no voided person when voided=false is passed
	 * @see PatientDAO#getPatients(String, boolean, Integer, Integer)
	 */
	@Test
	public void getPatients_shouldGetNoVoidedPersonWhenVoidedFalseIsPassed() throws Exception {
		List<Patient> patients = dao.getPatients("voided-bravo", false, 0, 11);
		Assert.assertEquals(0, patients.size());
	}
}
