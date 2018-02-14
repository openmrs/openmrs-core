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
import org.junit.Test;
import org.openmrs.CodedOrFreeText;
import org.openmrs.ConditionVerificationStatus;
import org.openmrs.Diagnosis;
import org.openmrs.Encounter;
import org.openmrs.test.BaseContextSensitiveTest;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;

/**
 * Contains tests methods for the {@link DiagnosisValidator}
 */
public class DiagnosisValidatorTest extends BaseContextSensitiveTest {

	private Diagnosis diagnosis;
	private Errors errors;

	@Before
	public void setUp() {
		diagnosis = new Diagnosis();
		errors = new BindException(diagnosis, "diagnosis");
	}
	
	/**
	 * @see DiagnosisValidator#validate(Object, Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfEncounterIsNull(){
		diagnosis.setEncounter(null);
		new DiagnosisValidator().validate(diagnosis, errors);
		Assert.assertTrue(errors.hasFieldErrors("encounter"));
	}
	@Test
	public void validate_shouldFailValidationIfDiagnosisIsNull(){
		diagnosis.setDiagnosis(null);
		
		new DiagnosisValidator().validate(diagnosis, errors);
		Assert.assertTrue(errors.hasFieldErrors("diagnosis"));
	}

	@Test
	public void validate_shouldFailValidationIfCertaintyIsNull(){
		diagnosis.setCertainty(null);

		new DiagnosisValidator().validate(diagnosis, errors);
		Assert.assertTrue(errors.hasFieldErrors("certainty"));
	}

	@Test
	public void validate_shouldFailValidationIfRankIsNull(){
		diagnosis.setRank(null);

		new DiagnosisValidator().validate(diagnosis, errors);
		Assert.assertTrue(errors.hasFieldErrors("rank"));
	}
	
	@Test
	public void validate_shouldFailValidationIfRankIsNonPositive(){
		diagnosis.setRank(-1);
		
		new DiagnosisValidator().validate(diagnosis, errors);
		Assert.assertTrue(errors.hasFieldErrors("rank"));
	}
	
	@Test
	public void validate_shouldPassValidationIfAllRequiredFieldsAreSupplied(){
		diagnosis.setEncounter(new Encounter());
		diagnosis.setDiagnosis(new CodedOrFreeText());
		diagnosis.setCertainty(ConditionVerificationStatus.CONFIRMED);
		diagnosis.setRank(1);

		Assert.assertFalse(errors.hasErrors());
	}
}
