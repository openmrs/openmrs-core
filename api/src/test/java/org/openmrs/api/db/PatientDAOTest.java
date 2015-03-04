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
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.test.Verifies;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PatientDAOTest extends BaseContextSensitiveTest {
	
	private PatientDAO dao = null;
	
	PatientService pService = null;
	
	/**
	 * Run this before each unit test in this class. The "@Before" method in
	 * {@link BaseContextSensitiveTest} is run right before this method.
	 * 
	 * @throws Exception
	 */
	@Before
	public void runBeforeEachTest() throws Exception {
		
		if (dao == null)
			// fetch the dao from the spring application context
			// this bean name matches the name in /metadata/spring/applicationContext-service.xml
			dao = (PatientDAO) applicationContext.getBean("patientDAO");
		if (pService == null)
			pService = Context.getPatientService();
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
		PatientIdentifier patientIdentifier = new PatientIdentifier("*567", pService.getPatientIdentifierType(2), Context
		        .getLocationService().getLocation(1));
		patient2.addIdentifier(patientIdentifier);
		pService.savePatient(patient2);
		
		//add closely matching identifier to a different patient
		Patient patient6 = pService.getPatient(6);
		PatientIdentifier patientIdentifier6 = new PatientIdentifier("4567", pService.getPatientIdentifierType(2), Context
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
		PatientIdentifier patientIdentifier = new PatientIdentifier("%567", pService.getPatientIdentifierType(2), Context
		        .getLocationService().getLocation(1));
		patient2.addIdentifier(patientIdentifier);
		pService.savePatient(patient2);
		
		//add closely matching identifier to a different patient
		Patient patient6 = pService.getPatient(6);
		PatientIdentifier patientIdentifier6 = new PatientIdentifier("4567", pService.getPatientIdentifierType(2), Context
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
		
		Patient patient2 = pService.getPatient(2);
		PatientIdentifier patientIdentifier = new PatientIdentifier("_567", pService.getPatientIdentifierType(2), Context
		        .getLocationService().getLocation(1));
		patient2.addIdentifier(patientIdentifier);
		pService.savePatient(patient2);
		
		//add closely matching identifier to a different patient
		Patient patient6 = pService.getPatient(6);
		PatientIdentifier patientIdentifier6 = new PatientIdentifier("4567", pService.getPatientIdentifierType(2), Context
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
		Assert.assertEquals("patientIdentifierTypes list should have 2 elements", 2, patientIdentifierTypes.size());
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
		Assert.assertEquals("patientIdentifierTypes list should have 3 elements", 3, patientIdentifierTypes.size());
	}
	
	@Test
	public void getPatientIdentifiers_shouldLimitByResultsByLocation() throws Exception {
		Location location = Context.getLocationService().getLocation(3); // there is only one identifier in the test database for location 3
		List<PatientIdentifier> patientIdentifiers = dao.getPatientIdentifiers(null, new ArrayList<PatientIdentifierType>(),
		    Collections.singletonList(location), new ArrayList<Patient>(), null);
		Assert.assertEquals(1, patientIdentifiers.size());
		Assert.assertEquals("12345K", patientIdentifiers.get(0).getIdentifier());
	}
	
}
