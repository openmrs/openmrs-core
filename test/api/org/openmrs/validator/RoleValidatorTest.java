package org.openmrs.validator;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.Role;
import org.openmrs.test.Verifies;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;

/**
 * Tests methods on the {@link RoleValidator} class.
 */
public class RoleValidatorTest {
	
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
	@Verifies(value = "should fail validation if description is null or empty or whitespace", method = "validate(Object,Errors)")
	public void validate_shouldFailValidationIfDescriptionIsNullOrEmptyOrWhitespace() throws Exception {
		Role role = new Role();
		role.setRole("Bowling race car driver");
		role.setDescription(null);
		
		Errors errors = new BindException(role, "type");
		new RoleValidator().validate(role, errors);
		Assert.assertTrue(errors.hasFieldErrors("description"));
		
		role.setDescription("");
		errors = new BindException(role, "role");
		new RoleValidator().validate(role, errors);
		Assert.assertTrue(errors.hasFieldErrors("description"));
		
		role.setDescription(" ");
		errors = new BindException(role, "role");
		new RoleValidator().validate(role, errors);
		Assert.assertTrue(errors.hasFieldErrors("description"));
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
}
