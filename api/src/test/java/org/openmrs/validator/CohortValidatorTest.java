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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.Cohort;
import org.openmrs.CohortMembership;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;

/**
 * Contains test methods for the {@link CohortValidator}
 */
public class CohortValidatorTest extends BaseContextSensitiveTest {
	
	private static final String nullOrIncompatibleObjErrorMessage = "The parameter obj should not be null and must be of type"
	        + Cohort.class;
	
	
	private CohortValidator validator;
	
	private Cohort cohort;

	private Patient patient;
	
	private CohortMembership cohortMembership;

	protected static final String COHORT_XML = "org/openmrs/api/include/CohortServiceTest-cohort.xml";

	private Errors errors;
	
	@BeforeEach
	public void setUp() {
		validator = new CohortValidator();

		executeDataSet(COHORT_XML);
		cohort = Context.getCohortService().getCohort(2);
		patient = Context.getPatientService().getPatient(7);
		cohortMembership = new CohortMembership(patient.getPatientId());
		cohort.addMembership(cohortMembership);
		
		errors = new BindException(cohort, "cohort");
	}
	
	@Test
	public void shouldFailIfGivenNull() { 
		
		IllegalArgumentException exception =assertThrows(IllegalArgumentException.class, () -> validator.validate(null, errors));
		assertThat(exception.getMessage(), is(nullOrIncompatibleObjErrorMessage));
	}
	
	@Test
	public void shouldFailIfGivenInstanceOfOtherClassThanCohort() {
		
		IllegalArgumentException exception =assertThrows(IllegalArgumentException.class, () -> validator.validate(new Patient(), errors));
		assertThat(exception.getMessage(), is(nullOrIncompatibleObjErrorMessage));
	}
	
	/**
	 * @see CohortValidator#validate(Object, Errors)
	 */
	@Test
	public void validate_shouldFailIfPatientIsVoided() {
		
		patient.setVoided(true);

		validator.validate(cohort, errors);
		
		assertTrue(errors.hasFieldErrors("memberships"));
		String eMessage = "Patient " + patient.getPatientId() + " is voided, cannot add voided members to a cohort";
		assertEquals(eMessage, errors.getFieldError("memberships").getDefaultMessage());
	}

	@Test
	public void validate_shouldPassIfPatientIsNonVoided() {
		
		validator.validate(cohort, errors);
		
		assertFalse(errors.hasErrors());
		assertFalse(errors.hasFieldErrors("memberships"));
	}

	@Test
	public void validate_shouldPassIfMembershipisVoided() {
		
		cohortMembership.setVoided(true);

		validator.validate(cohort, errors);
		
		assertFalse(errors.hasErrors());
		assertFalse(errors.hasFieldErrors("memberships"));
	}

	@Test
	public void validate_shouldPassIfPatientAndMembershipAreVoided() {
		
		patient.setVoided(true);
		cohortMembership.setVoided(true);

		validator.validate(cohort, errors);
		
		assertFalse(errors.hasErrors());
		assertFalse(errors.hasFieldErrors("memberships"));
	}

	@Test
	public void validate_shouldPassIfMembershipStartDateIsAfterEndDate() throws Exception {
		Cohort cohort = new Cohort(2);
		CohortMembership membership = new CohortMembership(patient.getPatientId());
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date startDate = dateFormat.parse("2016-11-01 00:00:00");
		Date endDate = dateFormat.parse("2015-01-01 00:00:00");
		membership.setStartDate(startDate);
		membership.setEndDate(endDate);
		Errors errors = new BindException(cohort, "cohort");
		new CohortValidator().validate(cohort, errors);
		assertFalse(errors.hasFieldErrors("memberships"));
	}
}
