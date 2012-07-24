package org.openmrs.validator;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.Privilege;
import org.openmrs.test.Verifies;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;

/**
 * Tests methods on the {@link PrivilegeValidator} class.
 */
public class PrivilegeValidatorTest {
	
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
	@Verifies(value = "should fail validation if description is null or empty or whitespace", method = "validate(Object,Errors)")
	public void validate_shouldFailValidationIfDescriptionIsNullOrEmptyOrWhitespace() throws Exception {
		Privilege priv = new Privilege();
		priv.setPrivilege("Wallhacking");
		priv.setDescription(null);
		
		Errors errors = new BindException(priv, "priv");
		new PrivilegeValidator().validate(priv, errors);
		Assert.assertTrue(errors.hasFieldErrors("description"));
		
		priv.setDescription("");
		errors = new BindException(priv, "priv");
		new PrivilegeValidator().validate(priv, errors);
		Assert.assertTrue(errors.hasFieldErrors("description"));
		
		priv.setDescription(" ");
		errors = new BindException(priv, "priv");
		new PrivilegeValidator().validate(priv, errors);
		Assert.assertTrue(errors.hasFieldErrors("description"));
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
	}
}
