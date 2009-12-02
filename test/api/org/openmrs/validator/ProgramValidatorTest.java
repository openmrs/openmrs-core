package org.openmrs.validator;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.Program;
import org.openmrs.api.context.Context;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.test.Verifies;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;

/**
 * Tests methods on the {@link ProgramValidator} class.
 */
public class ProgramValidatorTest extends BaseContextSensitiveTest {
	
	/**
	 * @see {@link ProgramValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should fail validation if name is null or empty or whitespace", method = "validate(Object,Errors)")
	public void validate_shouldFailValidationIfNameIsNullOrEmptyOrWhitespace() throws Exception {
		Program prog = new Program();
		prog.setName(null);
		prog.setConcept(Context.getConceptService().getConcept(3));
		
		Errors errors = new BindException(prog, "prog");
		new ProgramValidator().validate(prog, errors);
		Assert.assertTrue(errors.hasFieldErrors("name"));
		
		prog.setName("");
		errors = new BindException(prog, "prog");
		new ProgramValidator().validate(prog, errors);
		Assert.assertTrue(errors.hasFieldErrors("name"));
		
		prog.setName(" ");
		errors = new BindException(prog, "prog");
		new ProgramValidator().validate(prog, errors);
		Assert.assertTrue(errors.hasFieldErrors("name"));
	}
	
	/**
	 * @see {@link ProgramValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should fail validation if program name already in use", method = "validate(Object,Errors)")
	public void validate_shouldFailValidationIfProgramNameAlreadyInUse() throws Exception {
		Program prog = new Program();
		prog.setName("MDR program");
		prog.setConcept(Context.getConceptService().getConcept(3));
		
		Errors errors = new BindException(prog, "prog");
		new ProgramValidator().validate(prog, errors);
		Assert.assertTrue(errors.hasFieldErrors("name"));
	}
	
	/**
	 * @see {@link ProgramValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should fail validation if concept is null or empty or whitespace", method = "validate(Object,Errors)")
	public void validate_shouldFailValidationIfConceptIsNullOrEmptyOrWhitespace() throws Exception {
		Program prog = new Program();
		prog.setName("Hypochondriasis program");
		prog.setConcept(null);
		
		Errors errors = new BindException(prog, "prog");
		new ProgramValidator().validate(prog, errors);
		Assert.assertTrue(errors.hasFieldErrors("concept"));
	}
	
	/**
	 * @see {@link ProgramValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should pass validation if all required fields have proper values", method = "validate(Object,Errors)")
	public void validate_shouldPassValidationIfAllRequiredFieldsHaveProperValues() throws Exception {
		Program prog = new Program();
		prog.setName("Hypochondriasis program");
		prog.setConcept(Context.getConceptService().getConcept(3));
		
		Errors errors = new BindException(prog, "prog");
		new ProgramValidator().validate(prog, errors);
		
		Assert.assertFalse(errors.hasErrors());
	}
}
