package org.openmrs.validator;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.RelationshipType;
import org.openmrs.test.Verifies;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;

/**
 * Tests methods on the {@link RelationshipTypeValidator} class.
 */
public class RelationshipTypeValidatorTest {
	
	/**
	 * @see {@link RelationshipTypeValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should fail validation if name is null or empty or whitespace", method = "validate(Object,Errors)")
	public void validate_shouldFailValidationIfNameIsNullOrEmptyOrWhitespace() throws Exception {
		RelationshipType type = new RelationshipType();
		type.setName(null);
		type.setDescription("ooooook");
		
		Errors errors = new BindException(type, "type");
		new RelationshipTypeValidator().validate(type, errors);
		Assert.assertTrue(errors.hasFieldErrors("name"));
		
		type.setName("");
		errors = new BindException(type, "type");
		new RelationshipTypeValidator().validate(type, errors);
		Assert.assertTrue(errors.hasFieldErrors("name"));
		
		type.setName(" ");
		errors = new BindException(type, "type");
		new RelationshipTypeValidator().validate(type, errors);
		Assert.assertTrue(errors.hasFieldErrors("name"));
	}
	
	/**
	 * @see {@link RelationshipTypeValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should pass validation if description is null or empty or whitespace", method = "validate(Object,Errors)")
	public void validate_shouldPassValidationIfDescriptionIsNullOrEmptyOrWhitespace() throws Exception {
		RelationshipType type = new RelationshipType();
		type.setName("CLOSE");
		type.setDescription(null);
		
		Errors errors = new BindException(type, "type");
		new RelationshipTypeValidator().validate(type, errors);
		Assert.assertFalse(errors.hasFieldErrors("description"));
		
		type.setDescription("");
		errors = new BindException(type, "type");
		new RelationshipTypeValidator().validate(type, errors);
		Assert.assertFalse(errors.hasFieldErrors("description"));
		
		type.setDescription(" ");
		errors = new BindException(type, "type");
		new RelationshipTypeValidator().validate(type, errors);
		Assert.assertFalse(errors.hasFieldErrors("description"));
	}
	
	/**
	 * @see {@link RelationshipTypeValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should pass validation if all required fields have proper values", method = "validate(Object,Errors)")
	public void validate_shouldPassValidationIfAllRequiredFieldsHaveProperValues() throws Exception {
		RelationshipType type = new RelationshipType();
		type.setName("CLOSE");
		type.setDescription("ooooook");
		
		Errors errors = new BindException(type, "type");
		new RelationshipTypeValidator().validate(type, errors);
		
		Assert.assertFalse(errors.hasErrors());
	}
}
