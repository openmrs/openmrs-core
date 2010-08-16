package org.openmrs.validator;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.FieldType;
import org.openmrs.test.Verifies;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;

/**
 * Tests methods on the {@link FieldTypeValidator} class.
 */
public class FieldTypeValidatorTest {
	/**
	 * @see {@link FieldTypeValidator#validate(Object,Errors)}
	 * 
	 */
	@Test
	@Verifies(value = "should fail validation if name is null or empty or whitespace", method = "validate(Object,Errors)")
	public void validate_shouldFailValidationIfNameIsNullOrEmptyOrWhitespace()
			throws Exception {
		FieldType type = new FieldType();
		type.setName(null);
		type.setDescription("Humba humba humba ...");
		
		Errors errors = new BindException(type, "type");
		new FieldTypeValidator().validate(type, errors);
		Assert.assertTrue(errors.hasFieldErrors("name"));
		
		type.setName("");
		errors = new BindException(type, "type");
		new FieldTypeValidator().validate(type, errors);
		Assert.assertTrue(errors.hasFieldErrors("name"));
		
		type.setName(" ");
		errors = new BindException(type, "type");
		new FieldTypeValidator().validate(type, errors);
		Assert.assertTrue(errors.hasFieldErrors("name"));
	}

	/**
	 * @see {@link FieldTypeValidator#validate(Object,Errors)}
	 * 
	 */
	@Test
	@Verifies(value = "should pass validation if all required fields have proper values", method = "validate(Object,Errors)")
	public void validate_shouldPassValidationIfAllRequiredFieldsHaveProperValues()
			throws Exception {
		FieldType type = new FieldType();
		type.setName("soccer");
		
		Errors errors = new BindException(type, "type");
		new FieldTypeValidator().validate(type, errors);
		
		Assert.assertFalse(errors.hasErrors());
	}
}