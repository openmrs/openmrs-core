package org.openmrs.validator;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.EncounterRole;
import org.openmrs.EncounterType;
import org.openmrs.api.context.Context;
import org.openmrs.test.Verifies;
import org.openmrs.validator.EncounterRoleValidator;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;

/**
 * Tests methods on the {@link EncounterTypeValidator} class.
 */
public class EncounterTypeValidatorTest {
	
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
	
	/**
	 * @see {@link EncounterTypeValidator#validate(Object, Errors)}
	 */
	@Test
	@Verifies(value = "should pass editing encounter type name", method = "validate(Object,Errors)")
	public void validate_shouldPassEditingEncounterTypeName() throws Exception {
		
		EncounterType et = Context.getEncounterService().getEncounterType("Scheduled");
		Assert.assertNotNull(et);
		Errors errors = new BindException(et, "encounterType");
		new EncounterTypeValidator().validate(et, errors);
		Assert.assertFalse(errors.hasErrors());
		
	}
}
