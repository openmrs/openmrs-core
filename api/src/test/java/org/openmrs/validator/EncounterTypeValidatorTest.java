package org.openmrs.validator;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.EncounterType;
import org.openmrs.api.context.Context;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.test.Verifies;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;

/**
 * Tests methods on the {@link EncounterTypeValidator} class.
 */
public class EncounterTypeValidatorTest extends BaseContextSensitiveTest {
	
	/**
	 * @see {@link EncounterTypeValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should fail validation if name is null or empty or whitespace", method = "validate(Object,Errors)")
	public void validate_shouldFailValidationIfNameIsNullOrEmptyOrWhitespace() throws Exception {
		EncounterType type = new EncounterType();
		type.setName(null);
		type.setDescription("Aaaaah");
		
		Errors errors = new BindException(type, "type");
		new EncounterTypeValidator().validate(type, errors);
		Assert.assertTrue(errors.hasFieldErrors("name"));
		
		type.setName("");
		errors = new BindException(type, "type");
		new EncounterTypeValidator().validate(type, errors);
		Assert.assertTrue(errors.hasFieldErrors("name"));
		
		type.setName(" ");
		errors = new BindException(type, "type");
		new EncounterTypeValidator().validate(type, errors);
		Assert.assertTrue(errors.hasFieldErrors("name"));
	}
	
	/**
	 * @see {@link EncounterTypeValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should pass validation if description is null or empty or whitespace", method = "validate(Object,Errors)")
	public void validate_shouldPassValidationIfDescriptionIsNullOrEmptyOrWhitespace() throws Exception {
		EncounterType type = new EncounterType();
		type.setName("CLOSE");
		type.setDescription(null);
		
		Errors errors = new BindException(type, "type");
		new EncounterTypeValidator().validate(type, errors);
		Assert.assertFalse(errors.hasFieldErrors("description"));
		
		type.setDescription("");
		errors = new BindException(type, "type");
		new EncounterTypeValidator().validate(type, errors);
		Assert.assertFalse(errors.hasFieldErrors("description"));
		
		type.setDescription(" ");
		errors = new BindException(type, "type");
		new EncounterTypeValidator().validate(type, errors);
		Assert.assertFalse(errors.hasFieldErrors("description"));
	}
	
	/**
	 * @see {@link EncounterTypeValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should pass validation if all required fields have proper values", method = "validate(Object,Errors)")
	public void validate_shouldPassValidationIfAllRequiredFieldsHaveProperValues() throws Exception {
		EncounterType type = new EncounterType();
		type.setName("CLOSE");
		type.setDescription("Aaaaah");
		
		Errors errors = new BindException(type, "type");
		new EncounterTypeValidator().validate(type, errors);
		
		Assert.assertFalse(errors.hasErrors());
	}
	
	/**
	 * @see {@link EncounterTypeValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should pass validation for an existing EncounterType", method = "validate(Object,Errors)")
	public void validate_shouldPassValidationWhenEditingAnExistingEncounterType() throws Exception {
		EncounterType type = Context.getEncounterService().getEncounterType("Scheduled");
		Assert.assertNotNull(type);
		
		Errors errors = new BindException(type, "encounterType");
		new EncounterTypeValidator().validate(type, errors);
		
		Assert.assertFalse(errors.hasErrors());
	}
	
	/**
	 * @see {@link EncounterTypeValidator#validate(Object, Errors)}
	 */
	@Test
	@Verifies(value = "should fail if encounter type name is duplicate", method = "validate(Object,Errors)")
	public void validate_shouldFailIfEncounterTypeNameIsDuplicate() throws Exception {
		
		Assert.assertNotNull(Context.getEncounterService().getEncounterType("Scheduled"));
		
		EncounterType newEncounterType = new EncounterType();
		newEncounterType.setName("Scheduled");
		Errors errors = new BindException(newEncounterType, "encounterType");
		new EncounterTypeValidator().validate(newEncounterType, errors);
		Assert.assertTrue(errors.hasFieldErrors("name"));
		
	}
	
}
