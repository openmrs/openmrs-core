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
import org.openmrs.ConceptClass;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;

/**
 * Tests methods on the {@link ConceptClassValidator} class.
 */
public class ConceptClassValidatorTest extends BaseContextSensitiveTest {
	
	/**
	 * @see ConceptClassValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfUserIsNullOrEmptyOrWhitespace() {
		ConceptClass cc = new ConceptClass();
		cc.setName(null);
		cc.setDescription("some text");
		
		Errors errors = new BindException(cc, "cc");
		new ConceptClassValidator().validate(cc, errors);
		assertTrue(errors.hasFieldErrors("name"));
		
		cc.setName("");
		errors = new BindException(cc, "cc");
		new ConceptClassValidator().validate(cc, errors);
		assertTrue(errors.hasFieldErrors("name"));
		
		cc.setName(" ");
		errors = new BindException(cc, "cc");
		new ConceptClassValidator().validate(cc, errors);
		assertTrue(errors.hasFieldErrors("name"));
	}
	
	@Test
	public void validate_shouldPassValidationIfDescriptionIsNullOrEmptyOrWhitespace() {
		ConceptClass cc = new ConceptClass();
		cc.setName("name");
		cc.setDescription(null);
		
		Errors errors = new BindException(cc, "cc");
		new ConceptClassValidator().validate(cc, errors);
		assertFalse(errors.hasFieldErrors("description"));
		
		cc.setDescription("");
		errors = new BindException(cc, "cc");
		new ConceptClassValidator().validate(cc, errors);
		assertFalse(errors.hasFieldErrors("description"));
		
		cc.setDescription(" ");
		errors = new BindException(cc, "cc");
		new ConceptClassValidator().validate(cc, errors);
		assertFalse(errors.hasFieldErrors("description"));
		
	}
	
	/**
	 * @see ConceptClassValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldPassValidationIfAllRequiredFieldsHaveProperValues() {
		ConceptClass cc = new ConceptClass();
		cc.setName("name");
		cc.setDescription("some text");
		
		Errors errors = new BindException(cc, "cc");
		new ConceptClassValidator().validate(cc, errors);
		
		assertFalse(errors.hasErrors());
	}
	
	/**
	 * @see ConceptClassValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfConceptClassNameAlreadyExist() {
		ConceptClass cc = new ConceptClass();
		cc.setName("Test");
		cc.setDescription("some text");
		
		Errors errors = new BindException(cc, "cc");
		new ConceptClassValidator().validate(cc, errors);
		
		assertTrue(errors.hasErrors());
	}
	
	/**
	 * @see ConceptClassValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldPassValidationIfFieldLengthsAreCorrect() {
		ConceptClass cc = new ConceptClass();
		cc.setName("name");
		cc.setDescription("some text");
		cc.setRetireReason("some text");
		
		Errors errors = new BindException(cc, "cc");
		new ConceptClassValidator().validate(cc, errors);
		
		assertFalse(errors.hasErrors());
	}
	
	/**
	 * @see ConceptClassValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfFieldLengthsAreNotCorrect() {
		ConceptClass cc = new ConceptClass();
		cc
		        .setName("too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text");
		cc
		        .setDescription("too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text");
		cc
		        .setRetireReason("too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text");
		
		Errors errors = new BindException(cc, "cc");
		new ConceptClassValidator().validate(cc, errors);
		
		assertTrue(errors.hasFieldErrors("name"));
		assertTrue(errors.hasFieldErrors("description"));
		assertTrue(errors.hasFieldErrors("retireReason"));
	}
}
