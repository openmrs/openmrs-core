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
import org.openmrs.Privilege;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.test.Verifies;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;

/**
 * Tests methods on the {@link PrivilegeValidator} class.
 */
public class PrivilegeValidatorTest extends BaseContextSensitiveTest {
	
	/**
	 * @see {@link PrivilegeValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should fail validation if privilege is null or empty or whitespace", method = "validate(Object,Errors)")
	public void validate_shouldFailValidationIfPrivilegeIsNullOrEmptyOrWhitespace() throws Exception {
		Privilege priv = new Privilege();
		priv.setPrivilege(null);
		priv.setDescription("some text");
		
		Errors errors = new BindException(priv, "priv");
		new PrivilegeValidator().validate(priv, errors);
		Assert.assertTrue(errors.hasFieldErrors("privilege"));
		
		priv.setPrivilege("");
		errors = new BindException(priv, "priv");
		new PrivilegeValidator().validate(priv, errors);
		Assert.assertTrue(errors.hasFieldErrors("privilege"));
		
		priv.setPrivilege(" ");
		errors = new BindException(priv, "priv");
		new PrivilegeValidator().validate(priv, errors);
		Assert.assertTrue(errors.hasFieldErrors("privilege"));
	}
	
	/**
	 * @see {@link PrivilegeValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should pass validation if description is null or empty or whitespace", method = "validate(Object,Errors)")
	public void validate_shouldPassValidationIfDescriptionIsNullOrEmptyOrWhitespace() throws Exception {
		Privilege priv = new Privilege();
		priv.setPrivilege("Wallhacking");
		priv.setDescription(null);
		
		Errors errors = new BindException(priv, "priv");
		new PrivilegeValidator().validate(priv, errors);
		Assert.assertFalse(errors.hasFieldErrors("description"));
		
		priv.setDescription("");
		errors = new BindException(priv, "priv");
		new PrivilegeValidator().validate(priv, errors);
		Assert.assertFalse(errors.hasFieldErrors("description"));
		
		priv.setDescription(" ");
		errors = new BindException(priv, "priv");
		new PrivilegeValidator().validate(priv, errors);
		Assert.assertFalse(errors.hasFieldErrors("description"));
	}
	
	/**
	 * @see {@link PrivilegeValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should pass validation if all required fields have proper values", method = "validate(Object,Errors)")
	public void validate_shouldPassValidationIfAllRequiredFieldsHaveProperValues() throws Exception {
		Privilege priv = new Privilege();
		priv.setPrivilege("Wallhacking");
		priv.setDescription("idspispopd");
		
		Errors errors = new BindException(priv, "priv");
		new PrivilegeValidator().validate(priv, errors);
		
		Assert.assertFalse(errors.hasErrors());
		Assert.assertNotNull(priv.getName());
		Assert.assertEquals(priv.getPrivilege(), "Wallhacking");
	}
	
	/**
	 * @see {@link PrivilegeValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should pass validation if field lengths are correct", method = "validate(Object,Errors)")
	public void validate_shouldPassValidationIfFieldLengthsAreCorrect() throws Exception {
		Privilege priv = new Privilege();
		priv.setPrivilege("Wallhacking");
		priv.setDescription("description");
		
		Errors errors = new BindException(priv, "priv");
		new PrivilegeValidator().validate(priv, errors);
		
		Assert.assertFalse(errors.hasErrors());
	}
	
	/**
	 * @see {@link PrivilegeValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should fail validation if field lengths are not correct", method = "validate(Object,Errors)")
	public void validate_shouldFailValidationIfFieldLengthsAreNotCorrect() throws Exception {
		Privilege priv = new Privilege();
		priv
		        .setPrivilege("too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text");
		priv
		        .setDescription("too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text");
		
		Errors errors = new BindException(priv, "priv");
		new PrivilegeValidator().validate(priv, errors);
		
		Assert.assertTrue(errors.hasFieldErrors("privilege"));
		Assert.assertTrue(errors.hasFieldErrors("description"));
	}
}
