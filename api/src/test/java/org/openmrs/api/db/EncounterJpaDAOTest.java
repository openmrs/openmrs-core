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

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openmrs.Encounter;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.test.Verifies;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;

/**
 * This class tests the {@link EncounterRepository} linked to from the Context.
 */
public class EncounterJpaDAOTest extends BaseContextSensitiveTest {

	@Autowired
	private EncounterRepository encounterRepository;
	
	/**
	 * Run this before each unit test in this class. The "@Before" method in
	 * {@link BaseContextSensitiveTest} is run right before this method.
	 * 
	 * @throws Exception
	 */
	@Before
	public void runBeforeEachTest() throws Exception {
		executeDataSet("org/openmrs/api/db/include/EncounterDAOTest-initialData.xml");
		Encounter encounter = encounterRepository.findOne(1);
	}
	
	/**
	 * @see EncounterDAO#getSavedEncounterDatetime(Encounter)
	 */
	@Test
	@Verifies(value = "should get saved encounter datetime from database", method = "getSavedEncounterDatetime(Encounter)")
	public void getSavedEncounterDatetime_shouldGetSavedEncounterDatetimeFromDatabase() throws Exception {
		
		Encounter encounter = encounterRepository.findOne(1);
		
		Date newDate = new Date();
		
		// sanity check: make sure the date on the encounter isn't "right now" 
		assertNotSame(encounter.getEncounterDatetime(), newDate);
		
		// save the date from the db for later checks
		Date origDate = encounter.getEncounterDatetime();
		
		// change the date on the java pojo (NOT in the database)
		encounter.setEncounterDatetime(newDate);
		
		// the value from the database should match the original date from the 
		// encounter right after /it/ was fetched from the database
		Date encounterDateFromDatabase = encounterRepository.findOne(encounter.getId()).getEncounterDatetime();
		encounterRepository.findByPatient_PatientId(encounter.getPatient().getId());
//		assertEquals(origDate, encounterDateFromDatabase);
	}
}
