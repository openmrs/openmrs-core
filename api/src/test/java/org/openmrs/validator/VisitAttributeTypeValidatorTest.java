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
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
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
