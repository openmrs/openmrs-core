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
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.openmrs.hl7.HL7Source;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;

/**
 * Tests methods on the {@link HL7SourceValidator} class.
 */
public class HL7SourceValidatorTest extends BaseContextSensitiveTest {
	
	/**
	 * @see HL7SourceValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldPassValidationIfFieldLengthsAreCorrect() {
		HL7Source hl7Source = new HL7Source();
		hl7Source.setName("name");
		
		Errors errors = new BindException(hl7Source, "hl7Source");
		new HL7SourceValidator().validate(hl7Source, errors);
		assertFalse(errors.hasErrors());
	}
	
	/**
	 * @see HL7SourceValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfFieldLengthsAreNotCorrect() {
		HL7Source hl7Source = new HL7Source();
		hl7Source
		        .setName("too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text");
		
		Errors errors = new BindException(hl7Source, "hl7Source");
		new HL7SourceValidator().validate(hl7Source, errors);
		assertTrue(errors.hasFieldErrors("name"));
	}
}
