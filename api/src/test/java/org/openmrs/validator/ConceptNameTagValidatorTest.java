/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC. All Rights Reserved.
 */
package org.openmrs.validator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.Concept;
import org.openmrs.ConceptNameTag;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.test.Verifies;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;

/**
 * Tests methods on the {@link ConceptNameTagValidator} class.
 *
 * @since 1.10
 */
public class ConceptNameTagValidatorTest extends BaseContextSensitiveTest {
	
	@Test(expected = IllegalArgumentException.class)
	@Verifies(value = "fail validation if conceptNameTag is null", method = "validate(Object,Errors)")
	public void validate_shouldFailValidationIfConceptNameTagIsNull() throws Exception {
		Errors errors = new BindException(new ConceptNameTag(), "cnt");
		new ConceptNameTagValidator().validate(null, errors);
	}
	
	/**
	 * @see ConceptNameTagValidator#validate(Object,Errors)
	 */
	@Test
	@Verifies(value = "fail validation if tag is null or empty or whitespace", method = "validate(Object,Errors)")
	public void validate_shouldFailValidationIfTagIsNullOrEmptyOrWhitespace() throws Exception {
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
	@Verifies(value = "pass validation if tag does not exist and is not null or empty", method = "validate(Object,Errors)")
	public void validate_shouldPassValidationIfAllRequiredFieldsHaveProperValues() throws Exception {
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
	@Verifies(value = "should fail if the concept name tag is a duplicate", method = "validate(Object,Errors)")
	public void validate_shouldFailIfTheConceptNameTagIsADuplicate() throws Exception {
		String objectName = "duplicate concept name tag";
		
		ConceptNameTag existing = Context.getConceptService().getConceptNameTag(1);
		
		ConceptNameTag cnt = new ConceptNameTag();
		cnt.setTag(existing.getTag());
		
		Errors errors = new BindException(cnt, objectName);
		new ConceptNameTagValidator().validate(cnt, errors);
		Assert.assertTrue(errors.hasErrors());
		Assert.assertEquals(true, errors.hasFieldErrors("tag"));
	}
}
