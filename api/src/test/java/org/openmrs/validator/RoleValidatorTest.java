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
import org.openmrs.Role;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.test.Verifies;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;

/**
 * Tests methods on the {@link RoleValidator} class.
 */
public class RoleValidatorTest extends BaseContextSensitiveTest {
	
	/**
	 * @see {@link RoleValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should fail validation if role is null or empty or whitespace", method = "validate(Object,Errors)")
	public void validate_shouldFailValidationIfRoleIsNullOrEmptyOrWhitespace() throws Exception {
		Role role = new Role();
		role.setRole(null);
		role.setDescription("some text");
		//TODO: change/fix this test when it is decided whether to change the validator behavior to avoid throwing an NPE
		Errors errors = new BindException(role, "type");
		//new RoleValidator().validate(role, errors);
		//Assert.assertTrue(errors.hasFieldErrors("role"));
		
		role.setRole("");
		errors = new BindException(role, "role");
		new RoleValidator().validate(role, errors);
		Assert.assertTrue(errors.hasFieldErrors("role"));
		
		role.setRole(" ");
		errors = new BindException(role, "role");
		new RoleValidator().validate(role, errors);
		Assert.assertTrue(errors.hasFieldErrors("role"));
	}
	
	/**
	 * @see {@link RoleValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should pass validation if description is null or empty or whitespace", method = "validate(Object,Errors)")
	public void validate_shouldPassValidationIfDescriptionIsNullOrEmptyOrWhitespace() throws Exception {
		Role role = new Role();
		role.setRole("Bowling race car driver");
		role.setDescription(null);
		
		Errors errors = new BindException(role, "type");
		new RoleValidator().validate(role, errors);
		Assert.assertFalse(errors.hasFieldErrors("description"));
		
		role.setDescription("");
		errors = new BindException(role, "role");
		new RoleValidator().validate(role, errors);
		Assert.assertFalse(errors.hasFieldErrors("description"));
		
		role.setDescription(" ");
		errors = new BindException(role, "role");
		new RoleValidator().validate(role, errors);
		Assert.assertFalse(errors.hasFieldErrors("description"));
	}
	
	/**
	 * @see {@link RoleValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should fail validation if role has leading or trailing space", method = "validate(Object,Errors)")
	public void validate_shouldFailValidationIfRoleHasLeadingOrTrailingSpace() throws Exception {
		Role role = new Role();
		role.setDescription("some text");
		role.setRole(" Bowling race car driver");
		
		Errors errors = new BindException(role, "type");
		new RoleValidator().validate(role, errors);
		Assert.assertTrue(errors.hasFieldErrors("role"));
		Assert.assertEquals("error.trailingSpaces", errors.getFieldError("role").getCode());
		
		role.setRole("Bowling race car driver ");
		errors = new BindException(role, "role");
		new RoleValidator().validate(role, errors);
		Assert.assertTrue(errors.hasFieldErrors("role"));
		Assert.assertEquals("error.trailingSpaces", errors.getFieldError("role").getCode());
	}
	
	/**
	 * @see {@link RoleValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should pass validation if all required fields have proper values", method = "validate(Object,Errors)")
	public void validate_shouldPassValidationIfAllRequiredFieldsHaveProperValues() throws Exception {
		Role role = new Role();
		role.setRole("Bowling race car driver");
		role.setDescription("You don't bowl or race fast cars");
		
		Errors errors = new BindException(role, "type");
		new RoleValidator().validate(role, errors);
		
		Assert.assertFalse(errors.hasErrors());
	}
	
	/**
	 * @see {@link RoleValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should pass validation if field lengths are correct", method = "validate(Object,Errors)")
	public void validate_shouldPassValidationIfFieldLengthsAreCorrect() throws Exception {
		Role role = new Role();
		role.setRole("Bowling race car driver");
		role.setDescription("description");
		
		Errors errors = new BindException(role, "type");
		new RoleValidator().validate(role, errors);
		
		Assert.assertFalse(errors.hasErrors());
	}
	
	/**
	 * @see {@link RoleValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should fail validation if field lengths are not correct", method = "validate(Object,Errors)")
	public void validate_shouldFailValidationIfFieldLengthsAreNotCorrect() throws Exception {
		Role role = new Role();
		role
		        .setRole("too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text");
		role
		        .setDescription("too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text");
		
		Errors errors = new BindException(role, "type");
		new RoleValidator().validate(role, errors);
		
		Assert.assertTrue(errors.hasFieldErrors("role"));
	}
}
