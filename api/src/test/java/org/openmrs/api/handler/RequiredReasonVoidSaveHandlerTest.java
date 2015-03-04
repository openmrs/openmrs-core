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
import org.openmrs.test.Verifies;

/**
 * Tests for the {@link RequireVoidReasonSaveHandler} class.
 */
public class RequiredReasonVoidSaveHandlerTest extends BaseContextSensitiveTest {
	
	/**
	 * @see {@link RequireVoidReasonSaveHandler#handle(Voidable,User,Date,String)}
	 */
	@Test(expected = APIException.class)
	@Verifies(value = "should throw APIException if Patient voidReason is null", method = "handle(Voidable,User,Date,String)")
	public void handle_shouldThrowAPIExceptionIfPatientVoidReasonIsNull() throws Exception {
		Patient p = Context.getPatientService().getPatient(2);
		p.setVoided(true);
		p.setVoidReason(null);
		Context.getPatientService().savePatient(p);
	}
	
	/**
	 * @see {@link RequireVoidReasonSaveHandler#handle(Voidable,User,Date,String)}
	 */
	@Test(expected = APIException.class)
	@Verifies(value = "should throw APIException if Encounter voidReason is empty", method = "handle(Voidable,User,Date,String)")
	public void handle_shouldThrowAPIExceptionIfEncounterVoidReasonIsEmpty() throws Exception {
		Encounter e = Context.getEncounterService().getEncounter(3);
		e.setVoided(true);
		e.setVoidReason("");
		Context.getEncounterService().saveEncounter(e);
	}
	
	/**
	 * @see {@link RequireVoidReasonSaveHandler#handle(Voidable,User,Date,String)}
	 */
	@Test(expected = APIException.class)
	@Verifies(value = "should throw APIException if Encounter voidReason is blank", method = "handle(Voidable,User,Date,String)")
	public void handle_shouldThrowAPIExceptionIfObsVoidReasonIsBlank() throws Exception {
		Encounter e = Context.getEncounterService().getEncounter(3);
		e.setVoided(true);
		e.setVoidReason("  ");
		Context.getEncounterService().saveEncounter(e);
	}
	
	/**
	 * @see {@link RequireVoidReasonSaveHandler#handle(Voidable,User,Date,String)}
	 */
	@Test
	@Verifies(value = "should not throw Exception if voidReason is not blank", method = "handle(Voidable,User,Date,String)")
	public void handle_shouldNotThrowExceptionIfVoidReasonIsNotBlank() throws Exception {
		Encounter e = Context.getEncounterService().getEncounter(3);
		e.setVoided(true);
		e.setVoidReason("Some Reason");
		Context.getEncounterService().saveEncounter(e);
	}
	
	/**
	 * @see {@link RequireVoidReasonSaveHandler#handle(Voidable,User,Date,String)}
	 */
	@Test
	@Verifies(value = "not throw Exception if voidReason is null for unsupported types", method = "handle(Voidable,User,Date,String)")
	public void handle_shouldNotThrowExceptionIfVoidReasonIsNullForUnsupportedTypes() throws Exception {
		Person p = Context.getPersonService().getPerson(1);
		p.setVoided(true);
		p.setVoidReason(null);
		p.setVoidReason("voidReason");
		Context.getPersonService().savePerson(p);
	}
}
