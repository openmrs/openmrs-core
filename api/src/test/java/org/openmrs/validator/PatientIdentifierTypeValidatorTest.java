package org.openmrs.validator;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.PatientIdentifierType;
import org.openmrs.test.Verifies;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;

/**
 * Tests methods on the {@link PatientIdentifierTypeValidator} class.
 */
public class PatientIdentifierTypeValidatorTest {
	
	/**
	 * @see {@link PatientIdentifierTypeValidator#validate(Object,Errors)}
	 * 
	 */
	@Test
	@Verifies(value = "should fail validation if name is null or empty or whitespace", method = "validate(Object,Errors)")
	public void validate_shouldFailValidationIfNameIsNullOrEmptyOrWhitespace() throws Exception {
		PatientIdentifierType type = new PatientIdentifierType();
		type.setName(null);
		type.setDescription("some text");
		
		Errors errors = new BindException(type, "type");
		new PatientIdentifierTypeValidator().validate(type, errors);
		Assert.assertTrue(errors.hasFieldErrors("name"));
		
		type.setName("");
		errors = new BindException(type, "type");
		new PatientIdentifierTypeValidator().validate(type, errors);
		Assert.assertTrue(errors.hasFieldErrors("name"));
		
		type.setName(" ");
		errors = new BindException(type, "type");
		new PatientIdentifierTypeValidator().validate(type, errors);
		Assert.assertTrue(errors.hasFieldErrors("name"));
	}
	
	/**
	 * @see {@link PatientIdentifierTypeValidator#validate(Object,Errors)}
	 * 
	 */
	@Test
	@Verifies(value = "should fail validation if description is null or empty or whitespace", method = "validate(Object,Errors)")
	public void validate_shouldFailValidationIfDescriptionIsNullOrEmptyOrWhitespace() throws Exception {
		PatientIdentifierType type = new PatientIdentifierType();
		type.setName("name");
		type.setDescription(null);
		
		Errors errors = new BindException(type, "type");
		new PatientIdentifierTypeValidator().validate(type, errors);
		Assert.assertTrue(errors.hasFieldErrors("description"));
		
		type.setDescription("");
		errors = new BindException(type, "type");
		new PatientIdentifierTypeValidator().validate(type, errors);
		Assert.assertTrue(errors.hasFieldErrors("description"));
		
		type.setDescription(" ");
		errors = new BindException(type, "type");
		new PatientIdentifierTypeValidator().validate(type, errors);
		Assert.assertTrue(errors.hasFieldErrors("description"));
	}
	
	/**
	 * @see {@link PatientIdentifierTypeValidator#validate(Object,Errors)}
	 * 
	 */
	@Test
	@Verifies(value = "should pass validation if all required fields have proper values", method = "validate(Object,Errors)")
	public void validate_shouldPassValidationIfAllRequiredFieldsHaveProperValues() throws Exception {
		PatientIdentifierType type = new PatientIdentifierType();
		type.setName("restraining");
		type.setDescription(":(");
		
		Errors errors = new BindException(type, "type");
		new PatientIdentifierTypeValidator().validate(type, errors);
		
		Assert.assertFalse(errors.hasErrors());
	}
}
