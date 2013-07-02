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

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.ConceptNameTag;
import org.openmrs.api.impl.ConceptServiceImpl;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;

/**
 * Tests methods on the {@link ConceptNameTagValidator} class.
 * 
 * @since 1.10
 */
public class ConceptNameTagValidatorTest {
	
	/**
	 * @see ConceptNameTagValidator#validate(Object,Errors)
	 * @verifies fail validation if tag is null or empty or whitespace
	 */
	@Test
	public void validate_ShouldFailValidationIfTagIsNullOrEmptyOrWhitespace() throws Exception {
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
	 * @verifies fail validation if description is null or empty or whitespace
	 */
	@Test
	public void validate_ShouldFailValidationIfDescriptionIsNullOrEmptyOrWhitespace() throws Exception {
		ConceptNameTag cnt = new ConceptNameTag();
		
		Errors errors = new BindException(cnt, "cnt");
		new ConceptNameTagValidator().validate(cnt, errors);
		Assert.assertTrue(errors.hasFieldErrors("description"));
		
		cnt.setDescription("");
		errors = new BindException(cnt, "cnt");
		new ConceptNameTagValidator().validate(cnt, errors);
		Assert.assertTrue(errors.hasFieldErrors("description"));
		
		cnt.setDescription(" ");
		errors = new BindException(cnt, "cnt");
		new ConceptNameTagValidator().validate(cnt, errors);
		Assert.assertTrue(errors.hasFieldErrors("description"));
	}
	
	/**
	 * @see ConceptNameTagValidator#validate(Object,Errors)
	 * @verifies pass validation if all required fields have proper values
	 */
	@Test
	public void validate_ShouldPassValidationIfAllRequiredFieldsHaveProperValues() throws Exception {
		ConceptNameTag cnt = new ConceptNameTag();
		
		cnt.setTag("tag");
		cnt.setDescription("tag");
		
		Errors errors = new BindException(cnt, "cnt");
		new ConceptNameTagValidator().validate(cnt, errors);
		Assert.assertFalse(errors.hasErrors());
	}
	
	/**
	 * @see ConceptNameTagValidator#validate(Object,Errors)
	 * @verifies confirm that a faulty conceptNameTag will not be saved
	 */
	@Test
	public void validate_ShouldConfirmThatAFaultyConceptNameTagWillNotBeSaved() throws Exception {
		ConceptNameTag cnt = new ConceptNameTag();
		ConceptServiceImpl impl = new ConceptServiceImpl();
		Assert.assertTrue("falty ConceptNameTag",impl.saveConceptNameTag(cnt).equals(null));
	}
}
