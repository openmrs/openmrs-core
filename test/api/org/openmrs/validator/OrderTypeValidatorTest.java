package org.openmrs.validator;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.OrderType;
import org.openmrs.test.Verifies;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;

/**
 * Tests methods on the {@link OrderTypeValidator} class.
 */
public class OrderTypeValidatorTest {
	/**
	 * @see {@link OrderTypeValidator#validate(Object,Errors)}
	 * 
	 */
	@Test
	@Verifies(value = "should fail validation if unlocalized name is null or empty or whitespace", method = "validate(Object,Errors)")
	public void validate_shouldFailValidationIfUnlocalizedNameIsNullOrEmptyOrWhitespace()
			throws Exception {
		OrderType type = new OrderType();
		type.setName(null);
		type.setDescription(":(");
		
		Errors errors = new BindException(type, "type");
		new OrderTypeValidator().validate(type, errors);
		Assert.assertTrue(errors.hasFieldErrors("localizedName.unlocalizedValue"));
		
		type.setName("");
		errors = new BindException(type, "type");
		new OrderTypeValidator().validate(type, errors);
		Assert.assertTrue(errors.hasFieldErrors("localizedName.unlocalizedValue"));
		
		type.setName(" ");
		errors = new BindException(type, "type");
		new OrderTypeValidator().validate(type, errors);
		Assert.assertTrue(errors.hasFieldErrors("localizedName.unlocalizedValue"));
	}

	/**
	 * @see {@link OrderTypeValidator#validate(Object,Errors)}
	 * 
	 */
	@Test
	@Verifies(value = "should fail validation if unlocalized description is null or empty or whitespace", method = "validate(Object,Errors)")
	public void validate_shouldFailValidationIfUnlocalizedDescriptionIsNullOrEmptyOrWhitespace()
			throws Exception {
		OrderType type = new OrderType();
		type.setName("restraining");
		type.setDescription(null);
		
		Errors errors = new BindException(type, "type");
		new OrderTypeValidator().validate(type, errors);
		Assert.assertTrue(errors.hasFieldErrors("localizedDescription.unlocalizedValue"));
		
		type.setDescription("");
		errors = new BindException(type, "type");
		new OrderTypeValidator().validate(type, errors);
		Assert.assertTrue(errors.hasFieldErrors("localizedDescription.unlocalizedValue"));
		
		type.setDescription(" ");
		errors = new BindException(type, "type");
		new OrderTypeValidator().validate(type, errors);
		Assert.assertTrue(errors.hasFieldErrors("localizedDescription.unlocalizedValue"));
	}

	/**
	 * @see {@link OrderTypeValidator#validate(Object,Errors)}
	 * 
	 */
	@Test
	@Verifies(value = "should pass validation if all required fields have proper values", method = "validate(Object,Errors)")
	public void validate_shouldPassValidationIfAllRequiredFieldsHaveProperValues()
			throws Exception {
		OrderType type = new OrderType();
		type.setName("restraining");
		type.setDescription(":(");
		
		Errors errors = new BindException(type, "type");
		new OrderTypeValidator().validate(type, errors);
		
		Assert.assertFalse(errors.hasErrors());
	}
}