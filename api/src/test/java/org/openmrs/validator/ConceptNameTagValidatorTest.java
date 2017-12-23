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
import org.openmrs.ConceptNameTag;
import org.openmrs.api.context.Context;
import org.openmrs.test.BaseContextSensitiveTest;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;

/**
 * Tests methods on the {@link ConceptNameTagValidator} class.
 *
 * @since 1.10
 */
public class ConceptNameTagValidatorTest extends BaseContextSensitiveTest {
	
	@Test(expected = IllegalArgumentException.class)
	public void validate_shouldFailValidationIfConceptNameTagIsNull() {
		Errors errors = new BindException(new ConceptNameTag(), "cnt");
		new ConceptNameTagValidator().validate(null, errors);
	}
	
	/**
	 * @see ConceptNameTagValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfTagIsNullOrEmptyOrWhitespace() {
		ConceptNameTag cnt = new ConceptNameTag();
		
		Errors errors = new BindException(cnt, "cnt");
		new ConceptNameTagValidator().validate(cnt, errors);
		Assert.assertTrue(errors.hasFieldErrors("tag"));
		
		cnt.setTag("");
		errors = new BindException(cnt, "cnt");
		new ConceptNameTagValidator().validate(cnt, errors);
		Assert.assertTrue(errors.hasFieldErrors("tag"));
		
		cnt.setTag(" ");
		errors = new BindException(cnt, "cnt");
		new ConceptNameTagValidator().validate(cnt, errors);
		Assert.assertTrue(errors.hasFieldErrors("tag"));
	}
	
	/**
	 * @see ConceptNameTagValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldPassValidationIfAllRequiredFieldsHaveProperValues() {
		ConceptNameTag cnt = new ConceptNameTag();
		
		cnt.setTag("tag");
		
		Errors errors = new BindException(cnt, "cnt");
		new ConceptNameTagValidator().validate(cnt, errors);
		Assert.assertFalse(errors.hasErrors());
	}
	
	/**
	 * @see ConceptNameTagValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldFailIfTheConceptNameTagIsADuplicate() {
		String objectName = "duplicate concept name tag";
		
		ConceptNameTag existing = Context.getConceptService().getConceptNameTag(1);
		
		ConceptNameTag cnt = new ConceptNameTag();
		cnt.setTag(existing.getTag());
		
		Errors errors = new BindException(cnt, objectName);
		new ConceptNameTagValidator().validate(cnt, errors);
		Assert.assertTrue(errors.hasErrors());
		Assert.assertEquals(true, errors.hasFieldErrors("tag"));
	}
	
	/**
	 * @see ConceptNameTagValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldPassValidationIfFieldLengthsAreCorrect() {
		ConceptNameTag cnt = new ConceptNameTag();
		
		cnt.setTag("tag");
		cnt.setVoidReason("VoidReason");
		
		Errors errors = new BindException(cnt, "cnt");
		new ConceptNameTagValidator().validate(cnt, errors);
		Assert.assertFalse(errors.hasErrors());
	}
	
	/**
	 * @see ConceptNameTagValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfFieldLengthsAreNotCorrect() {
		ConceptNameTag cnt = new ConceptNameTag();
		
		cnt
		        .setTag("too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text");
		cnt
		        .setVoidReason("too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text");
		
		Errors errors = new BindException(cnt, "cnt");
		new ConceptNameTagValidator().validate(cnt, errors);
		Assert.assertEquals(true, errors.hasFieldErrors("tag"));
		Assert.assertEquals(true, errors.hasFieldErrors("voidReason"));
	}
	
	@Test
	public void validate_shouldNotFailIfTheConceptNameTagIsTheSame() {
		String objectName = "duplicate concept name tag";

		ConceptNameTag cnt = Context.getConceptService().getConceptNameTag(1);

		Errors errors = new BindException(cnt, objectName);
		new ConceptNameTagValidator().validate(cnt, errors);
		Assert.assertFalse(errors.hasErrors());
		Assert.assertEquals(false, errors.hasFieldErrors("tag"));
	}
}
