package org.openmrs.validator;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.ConceptClass;
import org.openmrs.test.Verifies;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;

/**
 * Tests methods on the {@link ConceptClassValidator} class.
 */
public class ConceptClassValidatorTest {
	
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
	
	/**
	 * @see {@link ConceptClassValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should fail validation if description is null or empty or whitespace", method = "validate(Object,Errors)")
	public void validate_shouldFailValidationIfDescriptionIsNullOrEmptyOrWhitespace() throws Exception {
		ConceptClass cc = new ConceptClass();
		cc.setName("name");
		cc.setDescription(null);
		
		Errors errors = new BindException(cc, "cc");
		new ConceptClassValidator().validate(cc, errors);
		Assert.assertTrue(errors.hasFieldErrors("description"));
		
		cc.setDescription("");
		errors = new BindException(cc, "cc");
		new ConceptClassValidator().validate(cc, errors);
		Assert.assertTrue(errors.hasFieldErrors("description"));
		
		cc.setDescription(" ");
		errors = new BindException(cc, "cc");
		new ConceptClassValidator().validate(cc, errors);
		Assert.assertTrue(errors.hasFieldErrors("description"));
		
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
}
