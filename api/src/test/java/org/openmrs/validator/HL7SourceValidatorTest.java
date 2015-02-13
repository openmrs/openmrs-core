/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
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
import org.openmrs.hl7.HL7Source;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.test.Verifies;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;

/**
 * Tests methods on the {@link HL7SourceValidator} class.
 */
public class HL7SourceValidatorTest extends BaseContextSensitiveTest {
	
	/**
	 * @see HL7SourceValidator#validate(Object,Errors)
	 * @verifies pass validation if field lengths are correct
	 */
	@Test
	@Verifies(value = "should pass validation if field lengths are correct", method = "validate(java.lang.Object, org.springframework.validation.Errors)")
	public void validate_shouldPassValidationIfFieldLengthsAreCorrect() throws Exception {
		HL7Source hl7Source = new HL7Source();
		hl7Source.setName("name");
		
		Errors errors = new BindException(hl7Source, "hl7Source");
		new HL7SourceValidator().validate(hl7Source, errors);
		Assert.assertFalse(errors.hasErrors());
	}
	
	/**
	 * @see HL7SourceValidator#validate(Object,Errors)
	 * @verifies pass validation if field lengths are correct
	 */
	@Test
	@Verifies(value = "should fail validation if field lengths are not correct", method = "validate(java.lang.Object, org.springframework.validation.Errors)")
	public void validate_shouldFailValidationIfFieldLengthsAreNotCorrect() throws Exception {
		HL7Source hl7Source = new HL7Source();
		hl7Source
		        .setName("too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text");
		
		Errors errors = new BindException(hl7Source, "hl7Source");
		new HL7SourceValidator().validate(hl7Source, errors);
		Assert.assertTrue(errors.hasFieldErrors("name"));
	}
}
