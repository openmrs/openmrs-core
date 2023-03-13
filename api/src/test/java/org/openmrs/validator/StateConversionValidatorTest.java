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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.openmrs.ConceptStateConversion;
import org.openmrs.ProgramWorkflow;
import org.openmrs.api.context.Context;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;

/**
 * Tests methods on the {@link StateConversionValidator} class.
 */
public class StateConversionValidatorTest extends BaseContextSensitiveTest {
	
	/**
	 * @see StateConversionValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfConceptIsNullOrEmptyOrWhitespace() {
		ConceptStateConversion csc = new ConceptStateConversion();
		ProgramWorkflow workflow = Context.getProgramWorkflowService().getProgram(1).getAllWorkflows().iterator().next();
		csc.setProgramWorkflow(workflow);
		csc.setProgramWorkflowState(workflow.getState(1));
		
		Errors errors = new BindException(csc, "csc");
		new StateConversionValidator().validate(csc, errors);
		
		assertTrue(errors.hasFieldErrors("concept"));
	}
	
	/**
	 * @see StateConversionValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfProgramWorkflowIsNullOrEmptyOrWhitespace() {
		ConceptStateConversion csc = new ConceptStateConversion();
		csc.setProgramWorkflow(null);
		
		ProgramWorkflow workflow = Context.getProgramWorkflowService().getProgram(1).getAllWorkflows().iterator().next();
		csc.setConcept(Context.getConceptService().getConcept(3));
		csc.setProgramWorkflowState(workflow.getState(1));
		
		Errors errors = new BindException(csc, "csc");
		new StateConversionValidator().validate(csc, errors);
		
		assertTrue(errors.hasFieldErrors("programWorkflow"));
	}
	
	/**
	 * @see StateConversionValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfProgramWorkflowStateIsNullOrEmptyOrWhitespace() {
		ConceptStateConversion csc = new ConceptStateConversion();
		
		ProgramWorkflow workflow = Context.getProgramWorkflowService().getProgram(1).getAllWorkflows().iterator().next();
		csc.setConcept(Context.getConceptService().getConcept(3));
		csc.setProgramWorkflow(workflow);
		csc.setProgramWorkflowState(null);
		
		Errors errors = new BindException(csc, "csc");
		new StateConversionValidator().validate(csc, errors);
		
		assertTrue(errors.hasFieldErrors("programWorkflowState"));
	}
	
	/**
	 * @see StateConversionValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldPassValidationIfAllRequiredFieldsHaveProperValues() {
		ConceptStateConversion csc = new ConceptStateConversion();
		ProgramWorkflow workflow = Context.getProgramWorkflowService().getProgram(1).getAllWorkflows().iterator().next();
		csc.setConcept(Context.getConceptService().getConcept(3));
		csc.setProgramWorkflow(workflow);
		csc.setProgramWorkflowState(workflow.getState(1));
		
		Errors errors = new BindException(csc, "csc");
		new StateConversionValidator().validate(csc, errors);
		
		assertFalse(errors.hasErrors());
	}
}
