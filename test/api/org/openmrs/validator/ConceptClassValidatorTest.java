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
	@Verifies(value = "should fail validation if unlocalized name is null or empty or whitespace", method = "validate(Object,Errors)")
	public void validate_shouldFailValidationIfUnlocalizedNameIsNullOrEmptyOrWhitespace() throws Exception {
		ConceptClass cc = new ConceptClass();
		cc.setName(null);
		cc.setDescription("some text");
		
		Errors errors = new BindException(cc, "cc");
		new ConceptClassValidator().validate(cc, errors);
		Assert.assertTrue(errors.hasFieldErrors("localizedName.unlocalizedValue"));
		
		cc.setName("");
		errors = new BindException(cc, "cc");
		new ConceptClassValidator().validate(cc, errors);
		Assert.assertTrue(errors.hasFieldErrors("localizedName.unlocalizedValue"));
		
		cc.setName(" ");
		errors = new BindException(cc, "cc");
		new ConceptClassValidator().validate(cc, errors);
		Assert.assertTrue(errors.hasFieldErrors("localizedName.unlocalizedValue"));
	}
	
	/**
	 * @see {@link ConceptClassValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should fail validation if unlocalized description is null or empty or whitespace", method = "validate(Object,Errors)")
	public void validate_shouldFailValidationIfUnlocalizedDescriptionIsNullOrEmptyOrWhitespace() throws Exception {
		ConceptClass cc = new ConceptClass();
		cc.setName("name");
		cc.setDescription(null);
		
		Errors errors = new BindException(cc, "cc");
		new ConceptClassValidator().validate(cc, errors);
		Assert.assertTrue(errors.hasFieldErrors("localizedDescription.unlocalizedValue"));
		
		cc.setDescription("");
		errors = new BindException(cc, "cc");
		new ConceptClassValidator().validate(cc, errors);
		Assert.assertTrue(errors.hasFieldErrors("localizedDescription.unlocalizedValue"));
		
		cc.setDescription(" ");
		errors = new BindException(cc, "cc");
		new ConceptClassValidator().validate(cc, errors);
		Assert.assertTrue(errors.hasFieldErrors("localizedDescription.unlocalizedValue"));
		
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
