package org.openmrs.validator;


import org.junit.Assert;
import org.junit.Test;
import org.openmrs.RelationshipType;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;

public class RelationshipTypeValidatorTest {
	/**
	 * @see RelationshipTypeValidator#validate(Object,Errors)
	 * @verifies fail validation if aIsToB(or A is To B) is null or empty or whitespace
	 */
	@Test
	public void validate_shouldFailValidationIfaIsToBIsNullOrEmptyOrWhitespace()
			throws Exception {
		RelationshipType type = new RelationshipType();
		type.setaIsToB(null);
		type.setDescription("A is To B");
		
		Errors errors = new BindException(type, "type");
		new RelationshipTypeValidator().validate(type, errors);
		Assert.assertTrue(errors.hasFieldErrors("aIsToB"));
		
		type.setaIsToB("");
		errors = new BindException(type, "type");
		new RelationshipTypeValidator().validate(type, errors);
		Assert.assertTrue(errors.hasFieldErrors("aIsToB"));
		
		type.setaIsToB(" ");
		errors = new BindException(type, "type");
		new RelationshipTypeValidator().validate(type, errors);
		Assert.assertTrue(errors.hasFieldErrors("aIsToB"));
		
	}

	/**
	 * @see RelationshipTypeValidator#validate(Object,Errors)
	 * @verifies fail validation if bIsToA(or B is To A) is null or empty or whitespace
	 */
	@Test
	public void validate_shouldFailValidationIfbIsToAIsNullOrEmptyOrWhitespace()
			throws Exception {
		RelationshipType type = new RelationshipType();
		type.setbIsToA(null);
		type.setDescription("B is To A");
		
		Errors errors = new BindException(type, "type");
		new RelationshipTypeValidator().validate(type, errors);
		Assert.assertTrue(errors.hasFieldErrors("bIsToA"));
		
		type.setbIsToA("");
		errors = new BindException(type, "type");
		new RelationshipTypeValidator().validate(type, errors);
		Assert.assertTrue(errors.hasFieldErrors("bIsToA"));
		
		type.setbIsToA(" ");
		errors = new BindException(type, "type");
		new RelationshipTypeValidator().validate(type, errors);
		Assert.assertTrue(errors.hasFieldErrors("bIsToA"));
		
	}

	/**
	 * @see RelationshipTypeValidator#validate(Object,Errors)
	 * @verifies fail validation if description is null or empty or whitespace
	 */
	@Test
	public void validate_shouldFailValidationIfDescriptionIsNullOrEmptyOrWhitespace()
			throws Exception {
		RelationshipType type = new RelationshipType();
		type.setName("name");
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
	 * @see RelationshipTypeValidator#validate(Object,Errors)
	 * @verifies pass validation if all required fields are set
	 */
	@Test
	public void validate_shouldPassValidationIfAllRequiredFieldsAreSet()
			throws Exception {
		RelationshipType type = new RelationshipType();
		type.setaIsToB("A is To B");
		type.setbIsToA("B is To A");
		type.setDescription("name");

		
		Errors errors = new BindException(type, "type");
		new RelationshipTypeValidator().validate(type, errors);
		
		Assert.assertFalse(errors.hasErrors());

	}
}