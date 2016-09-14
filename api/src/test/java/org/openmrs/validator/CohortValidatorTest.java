/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.validator;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.Cohort;
import org.openmrs.CohortMembership;
import org.openmrs.Patient;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.test.Verifies;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;

/**
 * Contains test methods for the {@link CohortValidator}
 */
public class CohortValidatorTest extends BaseContextSensitiveTest {

	/**
	 * @see CohortValidator#validate(Object, Errors)
	 */
	@Test
	@Verifies(value = "should fail if the patient is voided", method = "validate(Object, Errors)")
	public void validate_shouldFailIfPatientIsVoided() throws Exception {
		Cohort cohort = new Cohort(2);
		Patient patient = new Patient(7);
		patient.setVoided(true);
		cohort.addMembership(new CohortMembership(patient));
		Errors errors = new BindException(cohort, "cohort");
		new CohortValidator().validate(cohort, errors);
		Assert.assertTrue(errors.hasFieldErrors("members"));
		String eMessage = "Patient " + patient.getPatientId() + " is voided, cannot add voided members to a cohort";
		Assert.assertTrue(errors.getFieldErrors().stream().anyMatch(e -> e.getDefaultMessage().equals(eMessage)));
	}

	@Test
	@Verifies(value = "should pass if patient is not voided", method = "validate(Object, Errors)")
	public void validate_shouldPassIfPatientIsNonVoided() throws Exception {
		Cohort cohort = new Cohort(2);
		cohort.addMembership(new CohortMembership(new Patient(7)));
		Errors errors = new BindException(cohort, "cohort");
		new CohortValidator().validate(cohort, errors);
		Assert.assertFalse(errors.hasErrors());
		Assert.assertFalse(errors.hasFieldErrors("members"));
	}

	@Test
	@Verifies(value = "should pass if membership is voided", method = "validate(Object, Errors)")
	public void validate_shouldPassIfMembershipisVoided() throws Exception {
		Cohort cohort = new Cohort(2);
		CohortMembership cohortMembership = new CohortMembership(new Patient(7));
		cohortMembership.setVoided(true);
		cohort.addMembership(cohortMembership);
		Errors errors = new BindException(cohort, "cohort");
		new CohortValidator().validate(cohort, errors);
		Assert.assertFalse(errors.hasErrors());
		Assert.assertFalse(errors.hasFieldErrors("members"));
	}

	@Test
	@Verifies(value = "should pass if patient and membership are voided", method = "validate(Object, Errors)")
	public void validate_shouldPassIfPatientAndMembershipAreVoided() throws Exception {
		Cohort cohort = new Cohort(2);
		Patient patient = new Patient(7);
		patient.setVoided(true);
		CohortMembership cohortMembership = new CohortMembership(patient);
		cohortMembership.setVoided(true);
		Errors errors = new BindException(cohort, "cohort");
		new CohortValidator().validate(cohort, errors);
		Assert.assertFalse(errors.hasErrors());
		Assert.assertFalse(errors.hasFieldErrors("members"));
	}
}
