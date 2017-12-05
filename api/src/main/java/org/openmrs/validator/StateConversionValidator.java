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

import org.openmrs.ConceptStateConversion;
import org.openmrs.annotation.Handler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

/**
 * Validates attributes on the {@link ConceptStateConversion} object.
 * 
 * @since 1.5
 */
@Handler(supports = { ConceptStateConversion.class }, order = 50)
public class StateConversionValidator implements Validator {
	
	private static final Logger log = LoggerFactory.getLogger(StateConversionValidator.class);
	
	/**
	 * Determines if the command object being submitted is a valid type
	 * 
	 * @see org.springframework.validation.Validator#supports(java.lang.Class)
	 */
	@Override
	public boolean supports(Class<?> c) {
		return c.equals(ConceptStateConversion.class);
	}
	
	/**
	 * Checks the form object for any inconsistencies/errors
	 * 
	 * @see org.springframework.validation.Validator#validate(java.lang.Object,
	 *      org.springframework.validation.Errors)
	 * @should fail validation if concept is null or empty or whitespace
	 * @should fail validation if programWorkflow is null or empty or whitespace
	 * @should fail validation if programWorkflowState is null or empty or whitespace
	 * @should pass validation if all required fields have proper values
	 */
	@Override
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
