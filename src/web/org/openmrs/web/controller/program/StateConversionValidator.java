package org.openmrs.web.controller.program;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.ConceptStateConversion;
import org.openmrs.Program;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

public class StateConversionValidator implements Validator {

	protected final Log log = LogFactory.getLog(getClass());
	
	/**
	 * 
	 * Determines if the command object being submitted is a valid type
	 * 
	 * @see org.springframework.validation.Validator#supports(java.lang.Class)
	 */
	public boolean supports(Class c) {
		return c.equals(ConceptStateConversion.class);
	}

	/**
	 * 
	 * Checks the form object for any inconsistencies/errors
	 * 
	 * @see org.springframework.validation.Validator#validate(java.lang.Object, org.springframework.validation.Errors)
	 */
	public void validate(Object obj, Errors errors) {
		ConceptStateConversion c = (ConceptStateConversion) obj;
		if (c == null) {
			log.debug("Rejecting because c is null");
			errors.rejectValue("conceptStateConversion", "error.general");
		} else {
			ValidationUtils.rejectIfEmptyOrWhitespace(errors, "concept", "error.concept");
			ValidationUtils.rejectIfEmptyOrWhitespace(errors, "programWorkflow", "error.programWorkflow");
			ValidationUtils.rejectIfEmptyOrWhitespace(errors, "programWorkflowState", "error.programWorkflowState");
		}
	}

}
