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

import java.util.Date;

import org.junit.jupiter.api.Test;
import org.openmrs.Concept;
import org.openmrs.Obs;
import org.openmrs.ObsReferenceRange;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Tests the {@link ObsSaveHandler} class.
 */
public class ObsSaveHandlerTest extends BaseContextSensitiveTest {

	@Test
	public void handle_shouldSetReferenceRangeAndInterpretationForNewObs() {
		ObsSaveHandler handler = new ObsSaveHandler();

		Concept concept = Context.getConceptService().getConcept(5089);
		Patient patient = Context.getPatientService().getPatient(2);

		Obs obs = new Obs();
		obs.setConcept(concept);
		obs.setPerson(patient);
		obs.setObsDatetime(new Date());
		obs.setValueNumeric(120.0);

		handler.handle(obs, null, null, null);

		assertNotNull(obs.getReferenceRange());
		assertNotNull(obs.getInterpretation());
	}

	@Test
	public void handle_shouldNotOverwriteUserSuppliedReferenceRangeOnNewObs() {
		ObsSaveHandler handler = new ObsSaveHandler();

		Concept concept = Context.getConceptService().getConcept(5089);
		Patient patient = Context.getPatientService().getPatient(2);

		Obs obs = new Obs();
		obs.setConcept(concept);
		obs.setPerson(patient);
		obs.setObsDatetime(new Date());
		obs.setValueNumeric(120.0);

		ObsReferenceRange customRange = new ObsReferenceRange();
		customRange.setLowNormal(10.0);
		customRange.setHiNormal(50.0);
		obs.setReferenceRange(customRange);

		handler.handle(obs, null, null, null);

		assertSame(customRange, obs.getReferenceRange());
		assertEquals(10.0, obs.getReferenceRange().getLowNormal());
		assertEquals(50.0, obs.getReferenceRange().getHiNormal());
	}
}
