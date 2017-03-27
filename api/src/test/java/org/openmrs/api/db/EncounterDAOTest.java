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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.Encounter;
import org.openmrs.api.EncounterService;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.hibernate.HibernateEncounterDAO;
import org.openmrs.test.BaseContextSensitiveTest;

/**
 * This class tests the {@link EncounterDAO} linked to from the Context. Currently that file is the
 * {@link HibernateEncounterDAO} This should only have to test methods that don't really have
 * equivalents at the {@link EncounterService} layer.
 */
public class EncounterDAOTest extends BaseContextSensitiveTest {
	
	private EncounterDAO dao = null;
	
	/**
	 * Run this before each unit test in this class. The "@Before" method in
	 * {@link BaseContextSensitiveTest} is run right before this method.
	 * 
	 * @throws Exception
	 */
	@Before
	public void runBeforeEachTest() {
		executeDataSet("org/openmrs/api/db/include/EncounterDAOTest-initialData.xml");
		
		if (dao == null)
			// fetch the dao from the spring application context
			// this bean name matches the name in /metadata/spring/applicationContext-service.xml
			dao = (EncounterDAO) applicationContext.getBean("encounterDAO");
		
	}
	
	/**
	 * @see EncounterDAO#getSavedEncounterDatetime(Encounter)
	 */
	@Test
	public void getSavedEncounterDatetime_shouldGetSavedEncounterDatetimeFromDatabase() {
		
		Encounter encounter = Context.getEncounterService().getEncounter(1);
		
		Date newDate = new Date();
		
		// sanity check: make sure the date on the encounter isn't "right now" 
		assertNotSame(encounter.getEncounterDatetime(), newDate);
		
		// save the date from the db for later checks
		Date origDate = encounter.getEncounterDatetime();
		
		// change the date on the java pojo (NOT in the database)
		encounter.setEncounterDatetime(newDate);
		
		// the value from the database should match the original date from the 
		// encounter right after /it/ was fetched from the database
		Date encounterDateFromDatabase = dao.getSavedEncounterDatetime(encounter);
		assertEquals(origDate, encounterDateFromDatabase);
	}
	
	/**
	 * @see EncounterDAO#getEncounters(query, patientId, start, length, includeVoided)
	 */
	@Test
	public void getEncounters_shouldWork_WithNameQuery() {
		List<Encounter> expectedEncountersForPatientOne = initializeExpectedEncounters();
		List<Encounter> encounters = dao.getEncounters("John Doe", null, null, null, true);
		assertEquals(expectedEncountersForPatientOne, encounters);
	}
	
	/**
	 * @see EncounterDAO#getEncounters(query, patientId, start, length, includeVoided)
	 */
	@Test
	public void getEncounters_shouldWork_WithIdentifierQuery() {
		List<Encounter> expectedEncountersForPatientOne = initializeExpectedEncounters();
		
		List<Encounter> encountersByNumericIdentifier = dao.getEncounters("1234", null, null, null, true);
		assertEquals(expectedEncountersForPatientOne, encountersByNumericIdentifier);
		
		List<Encounter> encountersByStringIdentifier = dao.getEncounters("abcd", null, null, null, true);
		assertEquals(expectedEncountersForPatientOne, encountersByStringIdentifier);
	}
	
	private List<Encounter> initializeExpectedEncounters() {
		Encounter encounterOne = Context.getEncounterService().getEncounter(1);
		Encounter encounterSix = Context.getEncounterService().getEncounter(6);
		List<Encounter> expectedEncountersForPatientOne = new ArrayList<>();
		expectedEncountersForPatientOne.add(encounterOne);
		expectedEncountersForPatientOne.add(encounterSix);
		return expectedEncountersForPatientOne;
	}
	
	/**
	 * @see EncounterDAO#getEncounters(query, patientId, start, length, includeVoided)
	 */
	@Test
	public void getEncounters_shouldNotWork_WithPartialIdentifier() {
		List<Encounter> encountersByPartialIdentifier = dao.getEncounters("123", null, null, null, true);
		assertEquals(0, encountersByPartialIdentifier.size());
	}
}
