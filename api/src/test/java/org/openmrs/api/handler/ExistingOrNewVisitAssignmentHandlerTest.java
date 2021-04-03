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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Calendar;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.Encounter;
import org.openmrs.GlobalProperty;
import org.openmrs.VisitType;
import org.openmrs.api.context.Context;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;
import org.openmrs.util.OpenmrsConstants;

/**
 * Tests methods in the {@link ExistingOrNewVisitAssignmentHandler}
 */
public class ExistingOrNewVisitAssignmentHandlerTest extends BaseContextSensitiveTest {
	
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
		
		new ExistingOrNewVisitAssignmentHandler().beforeCreateEncounter(encounter);
		
		assertNotNull(encounter.getVisit());
	}
	
	/**
	 * @see ExistingVisitAssignmentHandler#beforeCreateEncounter(Encounter)
	 */
	@Test
	public void beforeCreateEncounter_shouldAssignNewVisitIfNoMatchFound() {
		Encounter encounter = Context.getEncounterService().getEncounter(1);
		assertNull(encounter.getVisit());
		
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(encounter.getEncounterDatetime());
		calendar.set(Calendar.YEAR, 1900);
		
		encounter.setEncounterDatetime(calendar.getTime());
		
		new ExistingOrNewVisitAssignmentHandler().beforeCreateEncounter(encounter);
		
		assertNotNull(encounter.getVisit());
	}
	
	/**
	 * @see ExistingVisitAssignmentHandler#beforeCreateEncounter(Encounter)
	 */
	@Test
	public void beforeCreateEncounter_shouldAssignFirstVisitTypeIfMappingGlobalPropertyIsNotSet() {
		VisitType visitType = Context.getVisitService().getAllVisitTypes().get(0);
		
		Encounter encounter = Context.getEncounterService().getEncounter(1);
		assertNull(encounter.getVisit());
		
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(encounter.getEncounterDatetime());
		calendar.set(Calendar.YEAR, 1900);
		
		encounter.setEncounterDatetime(calendar.getTime());
		
		new ExistingOrNewVisitAssignmentHandler().beforeCreateEncounter(encounter);
		
		assertNotNull(encounter.getVisit());
		assertEquals(visitType, encounter.getVisit().getVisitType());
	}
	
	/**
	 * @see ExistingVisitAssignmentHandler#beforeCreateEncounter(Encounter)
	 */
	@Test
	public void beforeCreateEncounter_shouldAssignMappingGlobalPropertyVisitType() {
		Encounter encounter = Context.getEncounterService().getEncounter(1);
		assertNull(encounter.getVisit());
		
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(encounter.getEncounterDatetime());
		calendar.set(Calendar.YEAR, 1900);
		
		encounter.setEncounterDatetime(calendar.getTime());
		
		GlobalProperty gp = new GlobalProperty(OpenmrsConstants.GP_ENCOUNTER_TYPE_TO_VISIT_TYPE_MAPPING,
		        "3:4, 5:2, 1:2, 2:2");
		Context.getAdministrationService().saveGlobalProperty(gp);
		
		new ExistingOrNewVisitAssignmentHandler().beforeCreateEncounter(encounter);
		
		assertNotNull(encounter.getVisit());
		
		//should be set according to: 1:2 encounterTypeId:visitTypeId
		assertEquals(1, encounter.getEncounterType().getEncounterTypeId().intValue());
		assertEquals(Context.getVisitService().getVisitType(2), encounter.getVisit().getVisitType());
	}
	
	/**
	 * @see ExistingOrNewVisitAssignmentHandler#beforeCreateEncounter(Encounter)
	 */
	@Test
	public void beforeCreateEncounter_shouldResolveEncounterAndVisitTypeUuidsAsGlobalPropertyValues() {
		final String encounterTypeUuid = "759799ab-c9a5-435e-b671-77773ada74e4";
		final String visitTypeUuid = "c0c579b0-8e59-401d-8a4a-976a0b183519";
		Encounter encounter = Context.getEncounterService().getEncounter(1);
		assertNull(encounter.getVisit());
		assertEquals(encounterTypeUuid, encounter.getEncounterType().getUuid());
		
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(encounter.getEncounterDatetime());
		calendar.set(Calendar.YEAR, 1900);
		
		encounter.setEncounterDatetime(calendar.getTime());
		
		GlobalProperty gp = new GlobalProperty(OpenmrsConstants.GP_ENCOUNTER_TYPE_TO_VISIT_TYPE_MAPPING, encounterTypeUuid
		        + ":" + visitTypeUuid);
		Context.getAdministrationService().saveGlobalProperty(gp);
		
		new ExistingOrNewVisitAssignmentHandler().beforeCreateEncounter(encounter);
		
		assertNotNull(encounter.getVisit());
		
		//should be set according toencounterTypeUuid:visitTypeUuid
		assertEquals(1, encounter.getEncounterType().getEncounterTypeId().intValue());
		assertEquals(Context.getVisitService().getVisitTypeByUuid(visitTypeUuid), encounter.getVisit()
		                .getVisitType());
	}
}
