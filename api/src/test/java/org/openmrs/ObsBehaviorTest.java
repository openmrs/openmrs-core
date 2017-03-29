/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.Date;

import org.junit.Ignore;
import org.junit.Test;
import org.openmrs.api.EncounterService;
import org.openmrs.api.ObsService;
import org.openmrs.api.context.Context;
import org.openmrs.test.BaseContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Contains end to end tests for order entry operations i.g placing, discontinuing revising an order
 */
public class ObsBehaviorTest extends BaseContextSensitiveTest {
	
	protected static final String OBS_DATASET_XML = "org/openmrs/api/include/ObsBehaviorTest.xml";
	
	@Autowired
	private ObsService obsService;
	
	@Autowired
	private EncounterService encounterService;
	
	@Test
	public void shouldHaveAnObsLoadedFromTheDatabaseAsNotDirty() {
		assertFalse(obsService.getObs(7).isDirty());
	}
	
	@Test
	public void shouldHaveAllObsLoadedWithAnEncounterFromTheDatabaseNotMarkedAsDirty() {
		Encounter e = encounterService.getEncounter(3);
		Collection<Obs> allObs = e.getAllObs(true);
		assertFalse(allObs.isEmpty());
		allObs.forEach(o -> assertFalse(o.isDirty()));
	}
	
	@Test
	@Ignore
	public void shouldVoidAndReplaceOnlyEditedUnvoidedObsWhenTheyAreFlushedToTheDatabase() {
		executeDataSet(OBS_DATASET_XML);
		final String newValueText = "some new value that for sure is different";
		final Integer encounterId = 201;
		Encounter encounter = encounterService.getEncounter(encounterId);
		final int initialAllObsCount = encounter.getAllObs(true).size();
		obsService.getObservationsByPerson(encounter.getPatient()).size();
		Obs alreadyVoidedObs = obsService.getObs(101);
		Obs toBeVoidedObs = obsService.getObs(103);
		Obs unVoidedObsToUpdate = obsService.getObs(102);
		Obs unVoidedObs = obsService.getObs(104);
		
		//sanity checks
		assertTrue(encounter.getAllObs(true).contains(alreadyVoidedObs));
		assertTrue(encounter.getAllObs(true).contains(toBeVoidedObs));
		assertTrue(encounter.getAllObs(true).contains(unVoidedObsToUpdate));
		assertTrue(encounter.getAllObs(true).contains(unVoidedObs));
		assertNotEquals(newValueText, alreadyVoidedObs.getValueText());
		assertNotEquals(newValueText, toBeVoidedObs.getValueText());
		assertNotEquals(newValueText, unVoidedObsToUpdate.getValueText());
		assertNotEquals(newValueText, unVoidedObs.getValueText());
		
		assertTrue(alreadyVoidedObs.getVoided());
		assertNotNull(alreadyVoidedObs.getVoidedBy());
		assertNotNull(alreadyVoidedObs.getDateVoided());
		assertNotNull(alreadyVoidedObs.getVoidReason());
		
		assertFalse(toBeVoidedObs.getVoided());
		assertNull(toBeVoidedObs.getVoidedBy());
		assertNull(toBeVoidedObs.getDateVoided());
		assertNull(toBeVoidedObs.getVoidReason());
		
		assertFalse(unVoidedObsToUpdate.getVoided());
		assertNull(unVoidedObsToUpdate.getVoidedBy());
		assertNull(unVoidedObsToUpdate.getDateVoided());
		assertNull(unVoidedObsToUpdate.getVoidReason());
		
		assertFalse(unVoidedObs.getVoided());
		assertNull(unVoidedObs.getVoidedBy());
		assertNull(unVoidedObs.getDateVoided());
		assertNull(unVoidedObs.getVoidReason());
		
		alreadyVoidedObs.setValueText(newValueText);
		toBeVoidedObs.setValueText(newValueText);
		unVoidedObsToUpdate.setValueText(newValueText);
		toBeVoidedObs.setVoided(true);
		encounter.setEncounterDatetime(new Date());
		encounterService.saveEncounter(encounter);
		
		//Evict and reload the encounter so that the Obs collection is updated
		Context.evictFromSession(encounter);
		encounter = encounterService.getEncounter(encounterId);
		//1 new order should have been created to replaced the edited one
		assertEquals(initialAllObsCount + 1, encounter.getAllObs(true).size());
		//assertEquals(c, obsService.getObservationsByPerson(encounter.getPatient()).size());
		
		//the already voided obs should have stayed been updated
		assertEquals(newValueText, alreadyVoidedObs.getValueText());
		assertEquals(newValueText, toBeVoidedObs.getValueText());
		
		//the obs that we edited should have stayed unchanged
		assertNotEquals(newValueText, unVoidedObsToUpdate.getValueText());
		
		//the obs that was edited should have been voided
		assertTrue(unVoidedObsToUpdate.getVoided());
		assertNotNull(unVoidedObsToUpdate.getVoidedBy());
		assertNotNull(unVoidedObsToUpdate.getDateVoided());
		assertNotNull(unVoidedObsToUpdate.getVoidReason());
		
		//the unvoided obs that wasn't edited should have stayed the same
		assertFalse(unVoidedObs.getVoided());
		assertNull(unVoidedObs.getVoidedBy());
		assertNull(unVoidedObs.getDateVoided());
		assertNull(unVoidedObs.getVoidReason());
		
	}
}
