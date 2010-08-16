package org.openmrs.validator;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.OrderType;
import org.openmrs.test.Verifies;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;

/**
 * Tests methods on the {@link RequireNameValidator} class.
 */
public class RequireNameValidatorTest {
	
	/**
	 * @see {@link RequireNameValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should fail validation if name is null or empty or whitespace", method = "validate(Object,Errors)")
	public void validate_shouldFailValidationIfNameIsNullOrEmptyOrWhitespace() throws Exception {
		OrderType type = new OrderType();
		type.setName(null);
		type.setDescription(":(");
		
		Errors errors = new BindException(type, "type");
		new RequireNameValidator().validate(type, errors);
		Assert.assertTrue(errors.hasFieldErrors("name"));
		
		type.setName("");
		errors = new BindException(type, "type");
		new RequireNameValidator().validate(type, errors);
		Assert.assertTrue(errors.hasFieldErrors("name"));
		
		type.setName(" ");
		errors = new BindException(type, "type");
		new RequireNameValidator().validate(type, errors);
		Assert.assertTrue(errors.hasFieldErrors("name"));
	}
	
	/**
	 * @see {@link RequireNameValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should pass validation if name has proper value", method = "validate(Object,Errors)")
	public void validate_shouldPassValidationIfNameHasProperValue() throws Exception {
		OrderType type = new OrderType();
		type.setName("restraining");
		
		Errors errors = new BindException(type, "type");
		new RequireNameValidator().validate(type, errors);
		
		Assert.assertFalse(errors.hasErrors());
	}
}
