/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api.handler;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Calendar;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.Encounter;
import org.openmrs.Visit;
import org.openmrs.api.context.Context;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;

/**
 * Tests methods in the {@link ExistingVisitAssignmentHandler}
 */
public class ExistingVisitAssignmentHandlerTest extends BaseContextSensitiveTest {
	
	protected static final String ENC_INITIAL_DATA_XML = "org/openmrs/api/include/EncounterServiceTest-initialData.xml";
	
	/**
	 * This method is run before all of the tests in this class because it has the @Before
	 * annotation on it. This will add the contents of {@link #ENC_INITIAL_DATA_XML} to the current
	 * database
	 * 
	 * @see BaseContextSensitiveTest#runBeforeAllUnitTests()
	 * @throws Exception
	 */
	@BeforeEach
	public void runBeforeEachTest() {
		executeDataSet(ENC_INITIAL_DATA_XML);
	}
	
	/**
	 * @see ExistingVisitAssignmentHandler#beforeCreateEncounter(Encounter)
	 */
	@Test
	public void beforeCreateEncounter_shouldAssignExistingVisitIfMatchFound() {
		Encounter encounter = Context.getEncounterService().getEncounter(1);
		assertNull(encounter.getVisit());
		
		new ExistingVisitAssignmentHandler().beforeCreateEncounter(encounter);
		
		assertNotNull(encounter.getVisit());
		assertNotNull(encounter.getVisit().getVisitId());
	}
	
	/**
	 * @see ExistingVisitAssignmentHandler#beforeCreateEncounter(Encounter)
	 */
	@Test
	public void beforeCreateEncounter_shouldNotAssignVisitIfNoMatchFound() {
		Encounter encounter = Context.getEncounterService().getEncounter(1);
		assertNull(encounter.getVisit());
		
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(encounter.getEncounterDatetime());
		calendar.set(Calendar.YEAR, 1900);
		
		encounter.setEncounterDatetime(calendar.getTime());
		
		new ExistingVisitAssignmentHandler().beforeCreateEncounter(encounter);
		
		assertNull(encounter.getVisit());
	}
	
	/**
	 * @see ExistingVisitAssignmentHandler#beforeCreateEncounter(Encounter)
	 */
	@Test
	public void beforeCreateEncounter_shouldNotAssignVisitWhichStoppedBeforeEncounterDate() {
		Encounter encounter = Context.getEncounterService().getEncounter(1);
		assertNull(encounter.getVisit());
		
		//set the visit stop date to that before the encounter date
		Visit visit = Context.getVisitService().getVisit(1);
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(visit.getStartDatetime());
		calendar.set(Calendar.YEAR, 2004);
		visit.setStopDatetime(calendar.getTime());
		Context.getVisitService().saveVisit(visit);
		
		new ExistingVisitAssignmentHandler().beforeCreateEncounter(encounter);
		
		assertNull(encounter.getVisit());
	}
}
