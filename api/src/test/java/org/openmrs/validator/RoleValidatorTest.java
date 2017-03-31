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
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;

/**
 * Tests methods on the {@link RoleValidator} class.
 */
public class RoleValidatorTest extends BaseContextSensitiveTest {
	
	/**
	 * @see RoleValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfRoleIsNullOrEmptyOrWhitespace() {
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
	 * @see RoleValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldPassValidationIfDescriptionIsNullOrEmptyOrWhitespace() {
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
	 * @see RoleValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfRoleHasLeadingOrTrailingSpace() {
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
	 * @see RoleValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldPassValidationIfAllRequiredFieldsHaveProperValues() {
		Role role = new Role();
		role.setRole("Bowling race car driver");
		role.setDescription("You don't bowl or race fast cars");
		
		Errors errors = new BindException(role, "type");
		new RoleValidator().validate(role, errors);
		
		Assert.assertFalse(errors.hasErrors());
	}
	
	/**
	 * @see RoleValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldPassValidationIfFieldLengthsAreCorrect() {
		Role role = new Role();
		role.setRole("Bowling race car driver");
		role.setDescription("description");
		
		Errors errors = new BindException(role, "type");
		new RoleValidator().validate(role, errors);
		
		Assert.assertFalse(errors.hasErrors());
	}
	
	/**
	 * @see RoleValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfFieldLengthsAreNotCorrect() {
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
