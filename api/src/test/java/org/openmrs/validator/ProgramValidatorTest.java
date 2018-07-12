/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.validator;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.Program;
import org.openmrs.api.context.Context;
import org.openmrs.test.BaseContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * Tests methods on the {@link ProgramValidator} class.
 */
public class ProgramValidatorTest extends BaseContextSensitiveTest {
	
	@Autowired
	protected Validator programValidator;
	
	/**
	 * @see ProgramValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfNameIsNullOrEmptyOrWhitespace() {
		Program prog = new Program();
		prog.initializeProgWithConcept(Context.getConceptService().getConcept(3));
		prog.setName(null);
		
		Errors errors = new BindException(prog, "prog");
		programValidator.validate(prog, errors);
		Assert.assertTrue(errors.hasFieldErrors("name"));
		
		prog.setName("");
		errors = new BindException(prog, "prog");
		programValidator.validate(prog, errors);
		Assert.assertTrue(errors.hasFieldErrors("name"));
		
		prog.setName(" ");
		errors = new BindException(prog, "prog");
		programValidator.validate(prog, errors);
		Assert.assertTrue(errors.hasFieldErrors("name"));
	}
	
	/**
	 * @see ProgramValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfProgramNameAlreadyInUse() {
		Program prog = new Program();
		prog.initializeProgWithConcept(Context.getConceptService().getConcept(3));
		prog.setName("MDR-TB PROGRAM");
		Errors errors = new BindException(prog, "prog");
		programValidator.validate(prog, errors);
		Assert.assertTrue(errors.hasFieldErrors("name"));
	}
	
	/**
	 * @see ProgramValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldPassValidationIfDescriptionIsNullOrEmptyOrWhitespace() {
		
		Program prog = new Program();
		prog.initializeProgWithConcept(Context.getConceptService().getConcept(3));
		prog.setDescription(null);
		
		Errors errors = new BindException(prog, "prog");
		programValidator.validate(prog, errors);
		Assert.assertFalse(errors.hasFieldErrors("description"));
		
		prog.setDescription("");
		errors = new BindException(prog, "prog");
		programValidator.validate(prog, errors);
		Assert.assertFalse(errors.hasFieldErrors("description"));
		
		prog.setDescription(" ");
		errors = new BindException(prog, "prog");
		programValidator.validate(prog, errors);
		Assert.assertFalse(errors.hasFieldErrors("description"));
	}
	
	/**
	 * @see ProgramValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldPassValidationIfAllRequiredFieldsHaveProperValues() {
		Program prog = new Program();
		prog.setName("Hypochondriasis program");
		prog.setDescription("This is Hypochondriasis program");
		
		Errors errors = new BindException(prog, "prog");
		programValidator.validate(prog, errors);
		
		Assert.assertFalse(errors.hasErrors());
	}
	
	/**
	 * @see ProgramValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldPassValidationAndSaveEditedProgram() {
		Program program = Context.getProgramWorkflowService().getProgram(3);
		program.setDescription("Edited description");
		
		Errors errors = new BindException(program, "program");
		programValidator.validate(program, errors);
		
		Assert.assertFalse(errors.hasErrors());
		
		Context.getProgramWorkflowService().saveProgram(program);
		program = Context.getProgramWorkflowService().getProgram(3);
		
		Assert.assertTrue(program.getDescription().equals("Edited description"));
	}
	
	/**
	 * @see ProgramValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldPassValidationIfFieldLengthsAreCorrect() {
		Program prog = new Program();
		prog.setName("Hypochondriasis program");
		
		Errors errors = new BindException(prog, "prog");
		programValidator.validate(prog, errors);
		
		Assert.assertFalse(errors.hasErrors());
	}
	
	/**
	 * @see ProgramValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfFieldLengthsAreNotCorrect() {
		Program prog = new Program();
		prog.setName("too long text too long text too long text too long text too long text too long text too long text too long text");
		
		Errors errors = new BindException(prog, "prog");
		programValidator.validate(prog, errors);
		
		Assert.assertTrue(errors.hasFieldErrors("name"));
	}
}
