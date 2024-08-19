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
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.openmrs.VisitAttributeType;
import org.openmrs.customdatatype.datatype.RegexValidatedTextDatatype;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;

public class VisitAttributeTypeValidatorTest extends BaseContextSensitiveTest {
	
	/**
	 * @see VisitAttributeTypeValidator#validate(Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldPassValidationIfFieldLengthsAreCorrect() {
		VisitAttributeType visitAttributeType = new VisitAttributeType();
		visitAttributeType.setName("name");
		visitAttributeType.setMinOccurs(1);
		visitAttributeType.setDatatypeConfig("[a-z]+");
		visitAttributeType.setDatatypeClassname(RegexValidatedTextDatatype.class.getName());
		visitAttributeType.setDescription("some text");
		visitAttributeType.setRetireReason("some text");
		
		Errors errors = new BindException(visitAttributeType, "visitAttributeType");
		new VisitAttributeTypeValidator().validate(visitAttributeType, errors);
		
		assertFalse(errors.hasErrors());
	}
	
	/**
	 * @see VisitAttributeTypeValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfFieldLengthsAreNotCorrect() {
		VisitAttributeType visitAttributeType = new VisitAttributeType();
		visitAttributeType
		        .setName("too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text");
		visitAttributeType.setMinOccurs(1);
		visitAttributeType.setDatatypeConfig("[a-z]+");
		visitAttributeType
		        .setDatatypeClassname("too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text");
		visitAttributeType.setDescription(new String(new char[66000]));
		visitAttributeType
		        .setPreferredHandlerClassname("too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text");
		visitAttributeType
		        .setRetireReason("too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text");
		
		Errors errors = new BindException(visitAttributeType, "visitAttributeType");
		new VisitAttributeTypeValidator().validate(visitAttributeType, errors);
		
		assertTrue(errors.hasFieldErrors("name"));
		assertThat(errors.getFieldErrors("name").get(0).getCode(), is("error.exceededMaxLengthOfField"));

		assertTrue(errors.hasFieldErrors("datatypeClassname"));
		assertThat(errors.getFieldErrors("datatypeClassname").get(0).getCode(), is("error.exceededMaxLengthOfField"));

		assertTrue(errors.hasFieldErrors("description"));
		assertThat(errors.getFieldErrors("description").get(0).getCode(), is("error.exceededMaxLengthOfField"));

		assertTrue(errors.hasFieldErrors("preferredHandlerClassname"));
		assertThat(errors.getFieldErrors("preferredHandlerClassname").get(0).getCode(), is("error.exceededMaxLengthOfField"));

		assertTrue(errors.hasFieldErrors("retireReason"));
		assertThat(errors.getFieldErrors("retireReason").get(0).getCode(), is("error.exceededMaxLengthOfField"));
	}
}
