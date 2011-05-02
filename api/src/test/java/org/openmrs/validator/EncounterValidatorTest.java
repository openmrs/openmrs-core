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

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.Encounter;
import org.openmrs.Patient;
import org.openmrs.Visit;
import org.openmrs.test.Verifies;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;

/**
 * Contains methods for testing {@link EncounterValidator#validate(Object, Errors)}
 */
public class EncounterValidatorTest {
	
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
		Assert.assertTrue(errors.hasFieldErrors("visit"));
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
}
