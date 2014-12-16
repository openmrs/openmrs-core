/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.validator;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.Encounter;
import org.openmrs.Patient;
import org.openmrs.Visit;
import org.openmrs.api.context.Context;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.test.Verifies;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;

/**
 * Contains methods for testing {@link EncounterValidator#validate(Object, Errors)}
 */
public class EncounterValidatorTest extends BaseContextSensitiveTest {
	
	/**
	 * @see {@link EncounterValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should fail if the patients for the visit and the encounter dont match", method = "validate(Object,Errors)")
	public void validate_shouldFailIfThePatientsForTheVisitAndTheEncounterDontMatch() throws Exception {
		Encounter encounter = new Encounter();
		encounter.setPatient(new Patient(2));
		Visit visit = new Visit();
		visit.setPatient(new Patient(3));
		encounter.setVisit(visit);
		Errors errors = new BindException(encounter, "encounter");
		new EncounterValidator().validate(encounter, errors);
		Assert.assertEquals("Encounter.visit.patients.dontMatch", errors.getFieldError("visit").getCode());
	}
	
	/**
	 * @see {@link EncounterValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should fail if the visit has no patient", method = "validate(Object,Errors)")
	public void validate_shouldFailIfTheVisitHasNoPatient() {
		Encounter encounter = new Encounter();
		encounter.setPatient(new Patient(2));
		Visit visit = new Visit();
		encounter.setVisit(visit);
		Errors errors = new BindException(encounter, "encounter");
		new EncounterValidator().validate(encounter, errors);
		Assert.assertEquals("Encounter.visit.patients.dontMatch", errors.getFieldError("visit").getCode());
	}
	
	/**
	 * @see {@link EncounterValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should fail if patient is not set", method = "validate(Object,Errors)")
	public void validate_shouldFailIfPatientIsNotSet() throws Exception {
		Encounter encounter = new Encounter();
		Errors errors = new BindException(encounter, "encounter");
		new EncounterValidator().validate(encounter, errors);
		Assert.assertTrue(errors.hasFieldErrors("patient"));
	}
	
	/**
	 * @see {@link EncounterValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should fail if encounter dateTime is before visit startDateTime", method = "validate(Object,Errors)")
	public void validate_shouldFailIfEncounterDateTimeIsBeforeVisitStartDateTime() throws Exception {
		Visit visit = Context.getVisitService().getVisit(1);
		
		Encounter encounter = Context.getEncounterService().getEncounter(3);
		visit.setPatient(encounter.getPatient());
		encounter.setVisit(visit);
		
		//Set encounter dateTime to before the visit startDateTime.
		Date date = new Date(visit.getStartDatetime().getTime() - 1);
		encounter.setEncounterDatetime(date);
		
		Errors errors = new BindException(encounter, "encounter");
		new EncounterValidator().validate(encounter, errors);
		Assert.assertEquals(true, errors.hasFieldErrors("encounterDatetime"));
	}
	
	/**
	 * @see {@link EncounterValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should fail if encounter dateTime is after visit stopDateTime", method = "validate(Object,Errors)")
	public void validate_shouldFailIfEncounterDateTimeIsAfterVisitStopDateTime() throws Exception {
		Visit visit = Context.getVisitService().getVisit(1);
		
		Encounter encounter = Context.getEncounterService().getEncounter(3);
		visit.setPatient(encounter.getPatient());
		encounter.setVisit(visit);
		
		//Set encounter dateTime to after the visit stopDateTime.
		visit.setStopDatetime(new Date());
		Date date = new Date(visit.getStopDatetime().getTime() + 1);
		encounter.setEncounterDatetime(date);
		
		Errors errors = new BindException(encounter, "encounter");
		new EncounterValidator().validate(encounter, errors);
		Assert.assertEquals(true, errors.hasFieldErrors("encounterDatetime"));
	}
	
	/**
	 * @see {@link EncounterValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "fail if encounter dateTime is after current dateTime", method = "validate(Object,Errors)")
	public void validate_shouldFailIfEncounterDateTimeIsAfterCurrentDateTime() throws Exception {
		
		Encounter encounter = Context.getEncounterService().getEncounter(3);
		
		//Set encounter dateTime after the current dateTime.
		Calendar calendar = new GregorianCalendar();
		calendar.add(Calendar.DAY_OF_YEAR, 1);
		Date tomorrowDate = calendar.getTime();
		encounter.setEncounterDatetime(tomorrowDate);
		
		Errors errors = new BindException(encounter, "encounter");
		new EncounterValidator().validate(encounter, errors);
		Assert.assertEquals(true, errors.hasFieldErrors("encounterDatetime"));
	}
	
	/**
	 * @verifies fail if encounter dateTime is not set
	 * @see EncounterValidator#validate(Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldFailIfEncounterDateTimeIsNotSet() throws Exception {
		
		Encounter encounter = new Encounter();
		
		Errors errors = new BindException(encounter, "encounter");
		new EncounterValidator().validate(encounter, errors);
		Assert.assertTrue(errors.hasFieldErrors("encounterDatetime"));
	}
	
	/**
	 * @verifies fail if encounter encounterType is not set
	 * @see EncounterValidator#validate(Object, org.springframework.validation.Errors)
	 */
	@Test
	@Verifies(value = "should fail if encounterType is not set", method = "validate(Object,Errors)")
	public void validate_shouldFailIfEncounterTypeIsNotSet() throws Exception {
		Encounter encounter = new Encounter();
		Errors errors = new BindException(encounter, "encounter");
		new EncounterValidator().validate(encounter, errors);
		Assert.assertTrue(errors.hasFieldErrors("encounterType"));
	}
}
