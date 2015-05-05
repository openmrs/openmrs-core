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
import org.openmrs.VisitAttributeType;
import org.openmrs.customdatatype.datatype.RegexValidatedTextDatatype;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.test.Verifies;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;

public class VisitAttributeTypeValidatorTest extends BaseContextSensitiveTest {
	
	/**
	 * @see {@link VisitAttributeTypeValidator#validate(Object, org.springframework.validation.Errors)}
	 */
	@Test
	@Verifies(value = "should pass validation if field lengths are correct", method = "validate(Object,Errors)")
	public void validate_shouldPassValidationIfFieldLengthsAreCorrect() throws Exception {
		VisitAttributeType visitAttributeType = new VisitAttributeType();
		visitAttributeType.setName("name");
		visitAttributeType.setMinOccurs(1);
		visitAttributeType.setDatatypeConfig("[a-z]+");
		visitAttributeType.setDatatypeClassname(RegexValidatedTextDatatype.class.getName());
		visitAttributeType.setDescription("some text");
		visitAttributeType.setRetireReason("some text");
		
		Errors errors = new BindException(visitAttributeType, "visitAttributeType");
		new VisitAttributeTypeValidator().validate(visitAttributeType, errors);
		
		Assert.assertFalse(errors.hasErrors());
	}
	
	/**
	 * @see {@link VisitAttributeTypeValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should fail validation if field lengths are not correct", method = "validate(Object,Errors)")
	public void validate_shouldFailValidationIfFieldLengthsAreNotCorrect() throws Exception {
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
		
		Assert.assertTrue(errors.hasFieldErrors("name"));
		Assert.assertTrue(errors.hasFieldErrors("description"));
		Assert.assertTrue(errors.hasFieldErrors("datatypeClassname"));
		Assert.assertTrue(errors.hasFieldErrors("preferredHandlerClassname"));
		Assert.assertTrue(errors.hasFieldErrors("retireReason"));
	}
}
