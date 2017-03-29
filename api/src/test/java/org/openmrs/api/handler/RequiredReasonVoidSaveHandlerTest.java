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

import org.junit.Test;
import org.openmrs.Encounter;
import org.openmrs.Patient;
import org.openmrs.Person;
import org.openmrs.User;
import org.openmrs.Voidable;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.test.BaseContextSensitiveTest;

/**
 * Tests for the {@link RequireVoidReasonSaveHandler} class.
 */
public class RequiredReasonVoidSaveHandlerTest extends BaseContextSensitiveTest {
	
	/**
	 * @see RequireVoidReasonSaveHandler#handle(Voidable,User,Date,String)
	 */
	@Test(expected = APIException.class)
	public void handle_shouldThrowAPIExceptionIfPatientVoidReasonIsNull() {
		Patient p = Context.getPatientService().getPatient(2);
		p.setVoided(true);
		p.setVoidReason(null);
		Context.getPatientService().savePatient(p);
	}
	
	/**
	 * @see RequireVoidReasonSaveHandler#handle(Voidable,User,Date,String)
	 */
	@Test(expected = APIException.class)
	public void handle_shouldThrowAPIExceptionIfEncounterVoidReasonIsEmpty() {
		Encounter e = Context.getEncounterService().getEncounter(3);
		e.setVoided(true);
		e.setVoidReason("");
		Context.getEncounterService().saveEncounter(e);
	}
	
	/**
	 * @see RequireVoidReasonSaveHandler#handle(Voidable,User,Date,String)
	 */
	@Test(expected = APIException.class)
	public void handle_shouldThrowAPIExceptionIfObsVoidReasonIsBlank() {
		Encounter e = Context.getEncounterService().getEncounter(3);
		e.setVoided(true);
		e.setVoidReason("  ");
		Context.getEncounterService().saveEncounter(e);
	}
	
	/**
	 * @see RequireVoidReasonSaveHandler#handle(Voidable,User,Date,String)
	 */
	@Test
	public void handle_shouldNotThrowExceptionIfVoidReasonIsNotBlank() {
		Encounter e = Context.getEncounterService().getEncounter(3);
		e.setVoided(true);
		e.setVoidReason("Some Reason");
		Context.getEncounterService().saveEncounter(e);
	}
	
	/**
	 * @see RequireVoidReasonSaveHandler#handle(Voidable,User,Date,String)
	 */
	@Test
	public void handle_shouldNotThrowExceptionIfVoidReasonIsNullForUnsupportedTypes() {
		Person p = Context.getPersonService().getPerson(1);
		p.setVoided(true);
		p.setVoidReason(null);
		p.setVoidReason("voidReason");
		Context.getPersonService().savePerson(p);
	}
}
