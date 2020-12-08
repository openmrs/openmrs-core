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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Test;
import org.openmrs.Concept;
import org.openmrs.Drug;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifierType;
import org.openmrs.api.ValidationException;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;

/**
 * Tests methods on the {@link ValidateUtil} class.
 */
public class ValidateUtilTest extends BaseContextSensitiveTest {
	
	/**
	 * @see ValidateUtil#validate(Object)
	 */
	@Test
	public void validate_shouldThrowValidationExceptionIfErrorsOccurDuringValidation() {
		Location loc = new Location();
		assertThrows(ValidationException.class , () -> ValidateUtil.validate(loc));
	}
	
	@Test
	public void validate_shouldThrowAPIExceptionIfErrorsOccurDuringValidation() {
		Location loc = new Location();
		
		try {
			ValidateUtil.validate(loc);
		}
		catch (ValidationException validationException) {
			assertNotNull(validationException.getErrors());
			assertTrue(validationException.getErrors().hasErrors());
		}
		
	}

	/**
	 * @see ValidateUtil#validate(Object)
	 */
	@Test
	public void validate_shouldReturnImmediatelyIfValidationIsDisabled() {
		Boolean prevVal = ValidateUtil.getDisableValidation();
		ValidateUtil.setDisableValidation(true);

		try {
			ValidateUtil.validate(new Patient());
		} catch (Exception e) {
			ValidateUtil.setDisableValidation(prevVal);
			e.printStackTrace();
			fail("An unexpected exception occurred");
		}

		ValidateUtil.setDisableValidation(prevVal);
	}
	
	/**
	 * @see ValidateUtil#validateFieldLengths(org.springframework.validation.Errors, Class, String...)
	 */
	@Test
	public void validateFieldLength_shouldRejectValueWhenNameIsToLong() {
		PatientIdentifierType patientIdentifierType = new PatientIdentifierType();
		patientIdentifierType.setName("asdfghjkl asdfghjkl asdfghjkl asdfghjkl asdfghjkl xx");
		
		BindException errors = new BindException(patientIdentifierType, "patientIdentifierType");
		ValidateUtil.validateFieldLengths(errors, PatientIdentifierType.class, "name");
		assertTrue(errors.hasFieldErrors("name"));
	}
	
	/**
	 * @see ValidateUtil#validateFieldLengths(org.springframework.validation.Errors, Class, String...)
	 */
	@Test
	public void validateFieldLength_shouldNotRejectValueWhenNameIsEqualMax() {
		PatientIdentifierType patientIdentifierType = new PatientIdentifierType();
		patientIdentifierType.setName("asdfghjkl asdfghjkl asdfghjkl asdfghjkl asdfghjkl ");
		
		BindException errors = new BindException(patientIdentifierType, "patientIdentifierType");
		ValidateUtil.validateFieldLengths(errors, PatientIdentifierType.class, "name");
		assertFalse(errors.hasFieldErrors("name"));
	}

	/**
	 * @see ValidateUtil#validateFieldLengths(org.springframework.validation.Errors, Class, String...)
	 */
	@Test
	public void validateFieldLength_shouldReturnImmediatelyIfValidationIsDisabledAndHaveNoErrors() {
		Boolean prevVal = ValidateUtil.getDisableValidation();
		ValidateUtil.setDisableValidation(true);

		PatientIdentifierType patientIdentifierType = new PatientIdentifierType();
		patientIdentifierType.setName("asdfghjkl asdfghjkl asdfghjkl asdfghjkl asdfghjkl +1");

		BindException errors = new BindException(patientIdentifierType, "patientIdentifierType");
		ValidateUtil.validateFieldLengths(errors, PatientIdentifierType.class, "name");
		assertFalse(errors.hasFieldErrors("name"));

		ValidateUtil.setDisableValidation(prevVal);
	}
	
	/**
	 * @see ValidateUtil#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldPopulateErrorsIfObjectInvalid() {
		Location loc = new Location();
		Errors errors = new BindException(loc, "");
		ValidateUtil.validate(loc, errors);
		
		assertTrue(errors.hasErrors());
	}

	/**
	 * @see ValidateUtil#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldReturnImmediatelyIfValidationIsDisabledAndHaveNoErrors() {
		Boolean prevVal = ValidateUtil.getDisableValidation();
		ValidateUtil.setDisableValidation(true);

		try {
			Patient patient = new Patient();
			Errors errors = new BindException(patient, "patient");
			ValidateUtil.validate(patient, errors);
			assertFalse(errors.hasErrors());
		} catch (Exception e) {
			ValidateUtil.setDisableValidation(prevVal);
			e.printStackTrace();
			fail("An unexpected exception occurred");
		}

		ValidateUtil.setDisableValidation(prevVal);
	}

	/**
	 * @see ValidateUtil#validate(Object)
	 */
	@Test
	public void validate_shouldReturnThrowExceptionAlongWithAppropriateMessageIfTheObjectIsInvalid() {
		Drug drug = new Drug();
		Concept concept = new Concept();
		drug.setName("Sucedáneo de leche humana de término de kcal 509-528/100g, lípidos 25.80-28.90/100g, proteínas 9.50-12.0/100g, hidrato de carbono 55.20-57.90/100g, polvo, envase de lata con 400 a 454 g y medida de 4.30 a 4.50 g. - envase con 400 a 454 g - - envase con 400 a 454 g");
		drug.setConcept(concept);
		
		ValidationException exception = assertThrows(ValidationException.class, () -> ValidateUtil.validate(drug));
		assertTrue(exception.getMessage().contains("failed to validate with reason: name: This value exceeds the maximum length of 255 permitted for this field."));
	}
}
