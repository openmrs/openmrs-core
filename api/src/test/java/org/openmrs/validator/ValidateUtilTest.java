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

import org.junit.Test;
import org.openmrs.Location;
import org.openmrs.PatientIdentifierType;
import org.openmrs.api.ValidationException;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.test.Verifies;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests methods on the {@link ValidateUtil} class.
 */
public class ValidateUtilTest extends BaseContextSensitiveTest {
	
	/**
	 * @see {@link ValidateUtil#validate(Object)}
	 */
	@Test(expected = ValidationException.class)
	@Verifies(value = "should throw ValidationException if errors occur during validation", method = "validate(Object)")
	public void validate_shouldThrowValidationExceptionIfErrorsOccurDuringValidation() throws Exception {
		Location loc = new Location();
		ValidateUtil.validate(loc);
	}
	
	@Test
	@Verifies(value = "should return Spring errors in ValidationException", method = "validate(Object)")
	public void validate_shouldThrowAPIExceptionIfErrorsOccurDuringValidation() throws Exception {
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
	 * @see {@link ValidateUtil#validateFieldLengths(org.springframework.validation.Errors, Class, String...)}
	 */
	@Test
	@Verifies(value = "fail validation if name field length is too long", method = "validateFieldLengths(org.springframework.validation.Errors, Class, String...)")
	public void validateFieldLength_shouldRejectValueWhenNameIsToLong() {
		PatientIdentifierType patientIdentifierType = new PatientIdentifierType();
		patientIdentifierType.setName("asdfghjkl asdfghjkl asdfghjkl asdfghjkl asdfghjkl xx");
		
		BindException errors = new BindException(patientIdentifierType, "patientIdentifierType");
		ValidateUtil.validateFieldLengths(errors, PatientIdentifierType.class, "name");
		assertTrue(errors.hasFieldErrors("name"));
	}
	
	/**
	 * @see {@link ValidateUtil#validateFieldLengths(org.springframework.validation.Errors, Class, String...)}
	 */
	@Test
	@Verifies(value = "pass validation if name field length is equal to maximum length", method = "validateFieldLengths(org.springframework.validation.Errors, Class, String...)")
	public void validateFieldLength_shouldNotRejectValueWhenNameIsEqualMax() {
		PatientIdentifierType patientIdentifierType = new PatientIdentifierType();
		patientIdentifierType.setName("asdfghjkl asdfghjkl asdfghjkl asdfghjkl asdfghjkl ");
		
		BindException errors = new BindException(patientIdentifierType, "patientIdentifierType");
		ValidateUtil.validateFieldLengths(errors, PatientIdentifierType.class, "name");
		assertFalse(errors.hasFieldErrors("name"));
	}
	
	/**
	 * @see ValidateUtil#validate(Object,Errors)
	 * @verifies populate errors if object invalid
	 */
	@Test
	public void validate_shouldPopulateErrorsIfObjectInvalid() throws Exception {
		Location loc = new Location();
		Errors errors = new BindException(loc, "");
		ValidateUtil.validate(loc, errors);
		
		assertTrue(errors.hasErrors());
	}
}
