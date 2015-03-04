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
import org.openmrs.ConceptStateConversion;
import org.openmrs.ProgramWorkflow;
import org.openmrs.api.context.Context;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.test.Verifies;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;

/**
 * Tests methods on the {@link StateConversionValidator} class.
 */
public class StateConversionValidatorTest extends BaseContextSensitiveTest {
	
	/**
	 * @see {@link StateConversionValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should fail validation if concept is null or empty or whitespace", method = "validate(Object,Errors)")
	public void validate_shouldFailValidationIfConceptIsNullOrEmptyOrWhitespace() throws Exception {
		ConceptStateConversion csc = new ConceptStateConversion();
		ProgramWorkflow workflow = Context.getProgramWorkflowService().getProgram(1).getAllWorkflows().iterator().next();
		csc.setProgramWorkflow(workflow);
		csc.setProgramWorkflowState(workflow.getState(1));
		
		Errors errors = new BindException(csc, "csc");
		new StateConversionValidator().validate(csc, errors);
		
		Assert.assertTrue(errors.hasFieldErrors("concept"));
	}
	
	/**
	 * @see {@link StateConversionValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should fail validation if programWorkflow is null or empty or whitespace", method = "validate(Object,Errors)")
	public void validate_shouldFailValidationIfProgramWorkflowIsNullOrEmptyOrWhitespace() throws Exception {
		ConceptStateConversion csc = new ConceptStateConversion();
		csc.setProgramWorkflow(null);
		
		ProgramWorkflow workflow = Context.getProgramWorkflowService().getProgram(1).getAllWorkflows().iterator().next();
		csc.setConcept(Context.getConceptService().getConcept(3));
		csc.setProgramWorkflowState(workflow.getState(1));
		
		Errors errors = new BindException(csc, "csc");
		new StateConversionValidator().validate(csc, errors);
		
		Assert.assertTrue(errors.hasFieldErrors("programWorkflow"));
	}
	
	/**
	 * @see {@link StateConversionValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should fail validation if programWorkflowState is null or empty or whitespace", method = "validate(Object,Errors)")
	public void validate_shouldFailValidationIfProgramWorkflowStateIsNullOrEmptyOrWhitespace() throws Exception {
		ConceptStateConversion csc = new ConceptStateConversion();
		
		ProgramWorkflow workflow = Context.getProgramWorkflowService().getProgram(1).getAllWorkflows().iterator().next();
		csc.setConcept(Context.getConceptService().getConcept(3));
		csc.setProgramWorkflow(workflow);
		csc.setProgramWorkflowState(null);
		
		Errors errors = new BindException(csc, "csc");
		new StateConversionValidator().validate(csc, errors);
		
		Assert.assertTrue(errors.hasFieldErrors("programWorkflowState"));
	}
	
	/**
	 * @see {@link StateConversionValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should pass validation if all required fields have proper values", method = "validate(Object,Errors)")
	public void validate_shouldPassValidationIfAllRequiredFieldsHaveProperValues() throws Exception {
		ConceptStateConversion csc = new ConceptStateConversion();
		ProgramWorkflow workflow = Context.getProgramWorkflowService().getProgram(1).getAllWorkflows().iterator().next();
		csc.setConcept(Context.getConceptService().getConcept(3));
		csc.setProgramWorkflow(workflow);
		csc.setProgramWorkflowState(workflow.getState(1));
		
		Errors errors = new BindException(csc, "csc");
		new StateConversionValidator().validate(csc, errors);
		
		Assert.assertFalse(errors.hasErrors());
	}
}
