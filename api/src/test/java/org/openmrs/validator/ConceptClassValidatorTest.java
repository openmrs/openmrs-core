package org.openmrs.validator;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.ConceptClass;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.test.Verifies;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;

/**
 * Tests methods on the {@link ConceptClassValidator} class.
 */
public class ConceptClassValidatorTest extends BaseContextSensitiveTest {
	
	/**
	 * @see {@link ConceptClassValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should fail validation if user is null or empty or whitespace", method = "validate(Object,Errors)")
	public void validate_shouldFailValidationIfUserIsNullOrEmptyOrWhitespace() throws Exception {
		ConceptClass cc = new ConceptClass();
		cc.setName(null);
		cc.setDescription("some text");
		
		Errors errors = new BindException(cc, "cc");
		new ConceptClassValidator().validate(cc, errors);
		Assert.assertTrue(errors.hasFieldErrors("name"));
		
		cc.setName("");
		errors = new BindException(cc, "cc");
		new ConceptClassValidator().validate(cc, errors);
		Assert.assertTrue(errors.hasFieldErrors("name"));
		
		cc.setName(" ");
		errors = new BindException(cc, "cc");
		new ConceptClassValidator().validate(cc, errors);
		Assert.assertTrue(errors.hasFieldErrors("name"));
	}
	
	@Test
	@Verifies(value = "should pass validation if description is null or empty or whitespace", method = "validate(Object,Errors)")
	public void validate_shouldPassValidationIfDescriptionIsNullOrEmptyOrWhitespace() throws Exception {
		ConceptClass cc = new ConceptClass();
		cc.setName("name");
		cc.setDescription(null);
		
		Errors errors = new BindException(cc, "cc");
		new ConceptClassValidator().validate(cc, errors);
		Assert.assertFalse(errors.hasFieldErrors("description"));
		
		cc.setDescription("");
		errors = new BindException(cc, "cc");
		new ConceptClassValidator().validate(cc, errors);
		Assert.assertFalse(errors.hasFieldErrors("description"));
		
		cc.setDescription(" ");
		errors = new BindException(cc, "cc");
		new ConceptClassValidator().validate(cc, errors);
		Assert.assertFalse(errors.hasFieldErrors("description"));
		
	}
	
	/**
	 * @see {@link ConceptClassValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should pass validation if all required fields have proper values", method = "validate(Object,Errors)")
	public void validate_shouldPassValidationIfAllRequiredFieldsHaveProperValues() throws Exception {
		ConceptClass cc = new ConceptClass();
		cc.setName("name");
		cc.setDescription("some text");
		
		Errors errors = new BindException(cc, "cc");
		new ConceptClassValidator().validate(cc, errors);
		
		Assert.assertFalse(errors.hasErrors());
	}
	
	/**
	 * @see {@link ConceptClassValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should fail validation if concept class name is already exist", method = "validate(Object,Errors)")
	public void validate_shouldFailValidationIfConceptClassNameAlreadyExist() throws Exception {
		ConceptClass cc = new ConceptClass();
		cc.setName("Test");
		cc.setDescription("some text");
		
		Errors errors = new BindException(cc, "cc");
		new ConceptClassValidator().validate(cc, errors);
		
		Assert.assertTrue(errors.hasErrors());
	}
}
