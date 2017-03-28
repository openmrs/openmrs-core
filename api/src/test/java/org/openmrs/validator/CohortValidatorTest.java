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
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.openmrs.Cohort;
import org.openmrs.CohortMembership;
import org.openmrs.Patient;
import org.openmrs.test.BaseContextSensitiveTest;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;

/**
 * Contains test methods for the {@link CohortValidator}
 */
public class CohortValidatorTest extends BaseContextSensitiveTest {
	
	private static final String nullOrIncompatibleObjErrorMessage = "The parameter obj should not be null and must be of type"
	        + Cohort.class;
	
	@Rule
	public ExpectedException expectedException = ExpectedException.none();
	
	private CohortValidator validator;
	
	private Cohort cohort;

	private Patient patient;
	
	private CohortMembership cohortMembership;
	
	private Errors errors;
	
	@Before
	public void setUp() {
		validator = new CohortValidator();
		
		cohort = new Cohort(2);
		patient = new Patient(7);
		cohortMembership = new CohortMembership(patient);
		cohort.addMembership(cohortMembership);
		
		errors = new BindException(cohort, "cohort");
	}
	
	@Test
	public void shouldFailIfGivenNull() {
		
		expectedException.expect(IllegalArgumentException.class);
		expectedException.expectMessage(nullOrIncompatibleObjErrorMessage);
		validator.validate(null, errors);
	}
	
	@Test
	public void shouldFailIfGivenInstanceOfOtherClassThanCohort() {
		
		expectedException.expect(IllegalArgumentException.class);
		expectedException.expectMessage(nullOrIncompatibleObjErrorMessage);
		validator.validate(new Patient(), errors);
	}
	
	/**
	 * @see CohortValidator#validate(Object, Errors)
	 */
	@Test
	public void validate_shouldFailIfPatientIsVoided() {
		
		patient.setVoided(true);
		
		validator.validate(cohort, errors);
		
		Assert.assertTrue(errors.hasFieldErrors("members"));
		String eMessage = "Patient " + patient.getPatientId() + " is voided, cannot add voided members to a cohort";
		Assert.assertTrue(errors.getFieldErrors().stream().anyMatch(e -> e.getDefaultMessage().equals(eMessage)));
	}

	@Test
	public void validate_shouldPassIfPatientIsNonVoided() {
		
		validator.validate(cohort, errors);
		
		Assert.assertFalse(errors.hasErrors());
		Assert.assertFalse(errors.hasFieldErrors("members"));
	}

	@Test
	public void validate_shouldPassIfMembershipisVoided() {
		
		cohortMembership.setVoided(true);
		
		validator.validate(cohort, errors);
		
		Assert.assertFalse(errors.hasErrors());
		Assert.assertFalse(errors.hasFieldErrors("members"));
	}

	@Test
	public void validate_shouldPassIfPatientAndMembershipAreVoided() {
		
		patient.setVoided(true);
		cohortMembership.setVoided(true);
		
		validator.validate(cohort, errors);
		
		Assert.assertFalse(errors.hasErrors());
		Assert.assertFalse(errors.hasFieldErrors("members"));
	}
}
