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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.commons.lang3.StringUtils;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.openmrs.ConceptSource;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;

/**
 *Tests methods on the {@link org.openmrs.validator.ConceptSourceValidator} class.
 */
public class ConceptSourceValidatorTest extends BaseContextSensitiveTest {
	
	@Test
	public void validate_shouldFailValidationIfNameIsNullOrEmptyOrWhitespace() {
		ConceptSource conceptSource = new ConceptSource();
		conceptSource.setName(null);
		conceptSource.setDescription("Some description");
		
		Errors errors = new BindException(conceptSource, "conceptSource");
		new ConceptSourceValidator().validate(conceptSource, errors);
		assertTrue(errors.hasFieldErrors("name"));
		assertThat(errors.getFieldErrors("name").get(0).getCode(), Matchers.is("error.name"));
		
		conceptSource.setName("");
		errors = new BindException(conceptSource, "conceptSource");
		new ConceptSourceValidator().validate(conceptSource, errors);
		assertTrue(errors.hasFieldErrors("name"));
		assertThat(errors.getFieldErrors("name").get(0).getCode(), Matchers.is("error.name"));
		
		conceptSource.setName("   ");
		errors = new BindException(conceptSource, "conceptSource");
		new ConceptSourceValidator().validate(conceptSource, errors);
		assertTrue(errors.hasFieldErrors("name"));
		assertThat(errors.getFieldErrors("name").get(0).getCode(), Matchers.is("error.name"));
	}

	/**
	 * @see ConceptSourceValidator#validate(Object, Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfDescriptionIsNullOrEmptyOrWhitespace() {

		ConceptSource conceptSource = new ConceptSource();
		conceptSource.setName("New name");

		conceptSource.setDescription(null);
		Errors errors = new BindException(conceptSource, "conceptSource");
		new ConceptSourceValidator().validate(conceptSource, errors);
		assertTrue(errors.hasFieldErrors("description"));
		assertThat(errors.getFieldErrors("description").get(0).getCode(), Matchers.is("error.null"));
		
		conceptSource.setDescription("");
		errors = new BindException(conceptSource, "conceptSource");
		new ConceptSourceValidator().validate(conceptSource, errors);
		assertTrue(errors.hasFieldErrors("description"));
		assertThat(errors.getFieldErrors("description").get(0).getCode(), Matchers.is("error.null"));
		
		conceptSource.setDescription("   ");
		errors = new BindException(conceptSource, "conceptSource");
		new ConceptSourceValidator().validate(conceptSource, errors);
		assertTrue(errors.hasFieldErrors("description"));
		assertThat(errors.getFieldErrors("description").get(0).getCode(), Matchers.is("error.null"));
	}
	
	@Test
	public void validate_shouldPassValidationIfHl7CodeIsNullOrEmptyOrWhitespace() {
		ConceptSource conceptSource = new ConceptSource();
		conceptSource.setName("New name");
		conceptSource.setDescription("Some description");
		conceptSource.setHl7Code(null);
		
		Errors errors = new BindException(conceptSource, "conceptSource");
		new ConceptSourceValidator().validate(conceptSource, errors);
		assertFalse(errors.hasFieldErrors("Hl7Code"));
		
		conceptSource.setHl7Code("");
		errors = new BindException(conceptSource, "conceptSource");
		new ConceptSourceValidator().validate(conceptSource, errors);
		assertFalse(errors.hasFieldErrors("Hl7Code"));
		
		conceptSource.setHl7Code("   ");
		errors = new BindException(conceptSource, "conceptSource");
		new ConceptSourceValidator().validate(conceptSource, errors);
		assertFalse(errors.hasFieldErrors("Hl7Code"));
	}
	
	@Test
	public void validate_shouldPassValidationIfAllRequiredFieldsHaveProperValues() {
		ConceptSource conceptSource = new ConceptSource();
		conceptSource.setName("New name");
		conceptSource.setDescription("Some description");
		
		Errors errors = new BindException(conceptSource, "conceptSource");
		new ConceptSourceValidator().validate(conceptSource, errors);
		assertFalse(errors.hasErrors());
	}
	
	@Test
	public void validate_shouldPassValidationIfFieldLengthsAreCorrect() {
		ConceptSource conceptSource = new ConceptSource();
		conceptSource.setName("New name");
		conceptSource.setDescription("Some description");
		conceptSource.setHl7Code("Hl7Code");
		conceptSource.setRetireReason("RetireReason");
		
		Errors errors = new BindException(conceptSource, "conceptSource");
		new ConceptSourceValidator().validate(conceptSource, errors);
		assertFalse(errors.hasErrors());
	}
	
	@Test
	public void validate_shouldFailValidationIfFieldLengthsAreNotCorrect() {
		ConceptSource conceptSource = new ConceptSource();
		conceptSource.setName(StringUtils.repeat("a", 51));
		conceptSource.setDescription(StringUtils.repeat("a", 1025));
		conceptSource.setHl7Code(StringUtils.repeat("a", 51));
		conceptSource.setUniqueId(StringUtils.repeat("a", 251));
		conceptSource.setRetireReason(StringUtils.repeat("a", 256));
		Errors errors = new BindException(conceptSource, "conceptSource");
		new ConceptSourceValidator().validate(conceptSource, errors);
		assertTrue(errors.hasFieldErrors("name"));
		assertThat(errors.getFieldErrors("name").get(0).getCode(), Matchers.is("error.exceededMaxLengthOfField"));
		
		assertTrue(errors.hasFieldErrors("description"));
		assertThat(errors.getFieldErrors("description").get(0).getCode(), Matchers.is("error.exceededMaxLengthOfField"));
		
		assertTrue(errors.hasFieldErrors("hl7Code"));
		assertThat(errors.getFieldErrors("hl7Code").get(0).getCode(), Matchers.is("error.exceededMaxLengthOfField"));
		
		assertTrue(errors.hasFieldErrors("uniqueId"));
		assertThat(errors.getFieldErrors("uniqueId").get(0).getCode(), Matchers.is("error.exceededMaxLengthOfField"));
		
		assertTrue(errors.hasFieldErrors("retireReason"));
		assertThat(errors.getFieldErrors("retireReason").get(0).getCode(), Matchers.is("error.exceededMaxLengthOfField"));
	}
}
