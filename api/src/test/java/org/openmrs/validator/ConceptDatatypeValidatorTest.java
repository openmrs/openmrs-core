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
import org.openmrs.ConceptDatatype;
import org.openmrs.test.BaseContextSensitiveTest;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;

/**
 * Tests methods on the {@link ConceptDatatypeValidator} class.
 */
public class ConceptDatatypeValidatorTest extends BaseContextSensitiveTest {
	
	/**
	 * @see ConceptDatatypeValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfNameIsNullOrEmptyOrWhitespace() {
		ConceptDatatype cd = new ConceptDatatype();
		cd.setName(null);
		cd.setDescription("some text");
		
		Errors errors = new BindException(cd, "cd");
		new ConceptDatatypeValidator().validate(cd, errors);
		Assert.assertTrue(errors.hasFieldErrors("name"));
		
		cd.setName("");
		errors = new BindException(cd, "cd");
		new ConceptDatatypeValidator().validate(cd, errors);
		Assert.assertTrue(errors.hasFieldErrors("name"));
		
		cd.setName(" ");
		errors = new BindException(cd, "cd");
		new ConceptDatatypeValidator().validate(cd, errors);
		Assert.assertTrue(errors.hasFieldErrors("name"));
	}
	
	/**
	 * @see ConceptDatatypeValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldPassValidationIfDescriptionIsNullOrEmptyOrWhitespace() {
		ConceptDatatype cd = new ConceptDatatype();
		cd.setName("name");
		cd.setDescription(null);
		
		Errors errors = new BindException(cd, "cd");
		new ConceptDatatypeValidator().validate(cd, errors);
		Assert.assertFalse(errors.hasFieldErrors("description"));
		
		cd.setDescription("");
		errors = new BindException(cd, "cd");
		new ConceptDatatypeValidator().validate(cd, errors);
		Assert.assertFalse(errors.hasFieldErrors("description"));
		
		cd.setDescription(" ");
		errors = new BindException(cd, "cd");
		new ConceptDatatypeValidator().validate(cd, errors);
		Assert.assertFalse(errors.hasFieldErrors("description"));
	}
	
	/**
	 * @see ConceptDatatypeValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldPassValidationIfAllRequiredFieldsHaveProperValues() {
		ConceptDatatype cd = new ConceptDatatype();
		cd.setName("name");
		cd.setDescription("some text");
		
		Errors errors = new BindException(cd, "cd");
		new ConceptDatatypeValidator().validate(cd, errors);
		
		Assert.assertFalse(errors.hasErrors());
	}
	
	/**
	 * @see ConceptDatatypeValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldPassValidationIfFieldLengthsAreCorrect() {
		ConceptDatatype cd = new ConceptDatatype();
		cd.setName("name");
		cd.setDescription("some text");
		cd.setHl7Abbreviation("hl7");
		cd.setRetireReason("retireReason");
		
		Errors errors = new BindException(cd, "cd");
		new ConceptDatatypeValidator().validate(cd, errors);
		
		Assert.assertFalse(errors.hasErrors());
	}
	
	/**
	 * @see ConceptDatatypeValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfFieldLengthsAreNotCorrect() {
		ConceptDatatype cd = new ConceptDatatype();
		cd
		        .setName("too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text");
		cd
		        .setDescription("too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text");
		cd.setHl7Abbreviation("hl7Abbreviation");
		cd
		        .setRetireReason("too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text");
		
		Errors errors = new BindException(cd, "cd");
		new ConceptDatatypeValidator().validate(cd, errors);
		
		Assert.assertTrue(errors.hasFieldErrors("name"));
		Assert.assertTrue(errors.hasFieldErrors("description"));
		Assert.assertTrue(errors.hasFieldErrors("hl7Abbreviation"));
		Assert.assertTrue(errors.hasFieldErrors("retireReason"));
	}
}
