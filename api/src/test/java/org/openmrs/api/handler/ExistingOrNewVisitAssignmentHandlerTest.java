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

import java.util.Calendar;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Encounter;
import org.openmrs.GlobalProperty;
import org.openmrs.VisitType;
import org.openmrs.api.context.Context;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.test.Verifies;
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
	@Before
	public void runBeforeEachTest() throws Exception {
		executeDataSet(ENC_INITIAL_DATA_XML);
	}
	
	/**
	 * @see {@link ExistingVisitAssignmentHandler#beforeCreateEncounter(Encounter)}
	 */
	@Test
	@Verifies(value = "should assign existing visit if match found", method = "beforeCreateEncounter(Encounter)")
	public void beforeCreateEncounter_shouldAssignExistingVisitIfMatchFound() throws Exception {
		Encounter encounter = Context.getEncounterService().getEncounter(1);
		Assert.assertNull(encounter.getVisit());
		
		new ExistingOrNewVisitAssignmentHandler().beforeCreateEncounter(encounter);
		
		Assert.assertNotNull(encounter.getVisit());
	}
	
	/**
	 * @see {@link ExistingVisitAssignmentHandler#beforeCreateEncounter(Encounter)}
	 */
	@Test
	@Verifies(value = "should assign new visit if no match found", method = "beforeCreateEncounter(Encounter)")
	public void beforeCreateEncounter_shouldAssignNewVisitIfNoMatchFound() throws Exception {
		Encounter encounter = Context.getEncounterService().getEncounter(1);
		Assert.assertNull(encounter.getVisit());
		
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(encounter.getEncounterDatetime());
		calendar.set(Calendar.YEAR, 1900);
		
		encounter.setEncounterDatetime(calendar.getTime());
		
		new ExistingOrNewVisitAssignmentHandler().beforeCreateEncounter(encounter);
		
		Assert.assertNotNull(encounter.getVisit());
	}
	
	/**
	 * @see {@link ExistingVisitAssignmentHandler#beforeCreateEncounter(Encounter)}
	 */
	@Test
	@Verifies(value = "should assign first visit type if mapping global property is not set", method = "beforeCreateEncounter(Encounter)")
	public void beforeCreateEncounter_shouldAssignFirstVisitTypeIfMappingGlobalPropertyIsNotSet() throws Exception {
		VisitType visitType = Context.getVisitService().getAllVisitTypes().get(0);
		
		Encounter encounter = Context.getEncounterService().getEncounter(1);
		Assert.assertNull(encounter.getVisit());
		
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(encounter.getEncounterDatetime());
		calendar.set(Calendar.YEAR, 1900);
		
		encounter.setEncounterDatetime(calendar.getTime());
		
		new ExistingOrNewVisitAssignmentHandler().beforeCreateEncounter(encounter);
		
		Assert.assertNotNull(encounter.getVisit());
		Assert.assertEquals(visitType, encounter.getVisit().getVisitType());
	}
	
	/**
	 * @see {@link ExistingVisitAssignmentHandler#beforeCreateEncounter(Encounter)}
	 */
	@Test
	@Verifies(value = "should assign mapping global property visit type", method = "beforeCreateEncounter(Encounter)")
	public void beforeCreateEncounter_shouldAssignMappingGlobalPropertyVisitType() throws Exception {
		Encounter encounter = Context.getEncounterService().getEncounter(1);
		Assert.assertNull(encounter.getVisit());
		
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(encounter.getEncounterDatetime());
		calendar.set(Calendar.YEAR, 1900);
		
		encounter.setEncounterDatetime(calendar.getTime());
		
		GlobalProperty gp = new GlobalProperty(OpenmrsConstants.GP_ENCOUNTER_TYPE_TO_VISIT_TYPE_MAPPING,
		        "3:4, 5:2, 1:2, 2:2");
		Context.getAdministrationService().saveGlobalProperty(gp);
		
		new ExistingOrNewVisitAssignmentHandler().beforeCreateEncounter(encounter);
		
		Assert.assertNotNull(encounter.getVisit());
		
		//should be set according to: 1:2 encounterTypeId:visitTypeId
		Assert.assertEquals(1, encounter.getEncounterType().getEncounterTypeId().intValue());
		Assert.assertEquals(Context.getVisitService().getVisitType(2), encounter.getVisit().getVisitType());
	}
	
	/**
	 * @see {@link ExistingOrNewVisitAssignmentHandler#beforeCreateEncounter(Encounter)}
	 */
	@Test
	@Verifies(value = "should resolve encounter and visit type uuids as global property values", method = "beforeCreateEncounter(Encounter)")
	public void beforeCreateEncounter_shouldResolveEncounterAndVisitTypeUuidsAsGlobalPropertyValues() throws Exception {
		final String encounterTypeUuid = "759799ab-c9a5-435e-b671-77773ada74e4";
		final String visitTypeUuid = "c0c579b0-8e59-401d-8a4a-976a0b183519";
		Encounter encounter = Context.getEncounterService().getEncounter(1);
		Assert.assertNull(encounter.getVisit());
		Assert.assertEquals(encounterTypeUuid, encounter.getEncounterType().getUuid());
		
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(encounter.getEncounterDatetime());
		calendar.set(Calendar.YEAR, 1900);
		
		encounter.setEncounterDatetime(calendar.getTime());
		
		GlobalProperty gp = new GlobalProperty(OpenmrsConstants.GP_ENCOUNTER_TYPE_TO_VISIT_TYPE_MAPPING, encounterTypeUuid
		        + ":" + visitTypeUuid);
		Context.getAdministrationService().saveGlobalProperty(gp);
		
		new ExistingOrNewVisitAssignmentHandler().beforeCreateEncounter(encounter);
		
		Assert.assertNotNull(encounter.getVisit());
		
		//should be set according toencounterTypeUuid:visitTypeUuid
		Assert.assertEquals(1, encounter.getEncounterType().getEncounterTypeId().intValue());
		Assert
		        .assertEquals(Context.getVisitService().getVisitTypeByUuid(visitTypeUuid), encounter.getVisit()
		                .getVisitType());
	}
}
