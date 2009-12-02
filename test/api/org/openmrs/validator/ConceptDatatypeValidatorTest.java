package org.openmrs.validator;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.ConceptDatatype;
import org.openmrs.test.Verifies;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;

/**
 * Tests methods on the {@link ConceptDatatypeValidator} class.
 */
public class ConceptDatatypeValidatorTest {
	
	/**
	 * @see {@link ConceptDatatypeValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should fail validation if name is null or empty or whitespace", method = "validate(Object,Errors)")
	public void validate_shouldFailValidationIfNameIsNullOrEmptyOrWhitespace() throws Exception {
		ConceptDatatype cd = new ConceptDatatype();
		cd.setName(null);
		cd.setDescription("some text");
		
		Errors errors = new BindException(cd, "cd");
		new ConceptDatatypeValidator().validate(cd, errors);
		Assert.assertTrue(errors.hasFieldErrors("name"));
		
		cd.setName("");
		errors = new BindException(cd, "cd");
		new ConceptDatatypeValidator().validate(cd, errors);
		Assert.assertTrue(errors.hasFieldErrors("name"));
		
		cd.setName(" ");
		errors = new BindException(cd, "cd");
		new ConceptDatatypeValidator().validate(cd, errors);
		Assert.assertTrue(errors.hasFieldErrors("name"));
	}
	
	/**
	 * @see {@link ConceptDatatypeValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should fail validation if description is null or empty or whitespace", method = "validate(Object,Errors)")
	public void validate_shouldFailValidationIfDescriptionIsNullOrEmptyOrWhitespace() throws Exception {
		ConceptDatatype cd = new ConceptDatatype();
		cd.setName("name");
		cd.setDescription(null);
		
		Errors errors = new BindException(cd, "cd");
		new ConceptDatatypeValidator().validate(cd, errors);
		Assert.assertTrue(errors.hasFieldErrors("description"));
		
		cd.setDescription("");
		errors = new BindException(cd, "cd");
		new ConceptDatatypeValidator().validate(cd, errors);
		Assert.assertTrue(errors.hasFieldErrors("description"));
		
		cd.setDescription(" ");
		errors = new BindException(cd, "cd");
		new ConceptDatatypeValidator().validate(cd, errors);
		Assert.assertTrue(errors.hasFieldErrors("description"));
	}
	
	/**
	 * @see {@link ConceptDatatypeValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should pass validation if all required fields have proper values", method = "validate(Object,Errors)")
	public void validate_shouldPassValidationIfAllRequiredFieldsHaveProperValues() throws Exception {
		ConceptDatatype cd = new ConceptDatatype();
		cd.setName("name");
		cd.setDescription("some text");
		
		Errors errors = new BindException(cd, "cd");
		new ConceptDatatypeValidator().validate(cd, errors);
		
		Assert.assertFalse(errors.hasErrors());
	}
}
