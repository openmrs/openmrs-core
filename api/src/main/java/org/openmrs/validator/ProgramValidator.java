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

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Program;
import org.openmrs.annotation.Handler;
import org.openmrs.api.context.Context;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

/**
 * Validates attributes on the {@link Program} object.
 * 
 * @since 1.5
 */
@Handler(supports = { Program.class }, order = 50)
public class ProgramValidator implements Validator {
	
	protected final Log log = LogFactory.getLog(getClass());
	
	/**
	 * Determines if the command object being submitted is a valid type
	 * 
	 * @see org.springframework.validation.Validator#supports(java.lang.Class)
	 */
	public boolean supports(Class<?> c) {
		return c.equals(Program.class);
	}
	
	/**
	 * Checks the form object for any inconsistencies/errors
	 * 
	 * @see org.springframework.validation.Validator#validate(java.lang.Object,
	 *      org.springframework.validation.Errors)
	 * @should fail validation if name is null or empty or whitespace
	 * @should fail validation if description is null or empty or whitespace
	 * @should fail validation if program name already in use
	 * @should fail validation if concept is null or empty or whitespace
	 * @should pass validation if all required fields have proper values
	 */
	public void validate(Object obj, Errors errors) {
		Program p = (Program) obj;
		if (p == null) {
			errors.rejectValue("program", "error.general");
		} else {
			ValidationUtils.rejectIfEmptyOrWhitespace(errors, "name", "error.name");
			ValidationUtils.rejectIfEmptyOrWhitespace(errors, "description", "error.description.required");
			List<Program> programs = Context.getProgramWorkflowService().getAllPrograms(false);
			for (Program program : programs) {
				if (program.getName().equals(p.getName()) && !program.getProgramId().equals(p.getProgramId())) {
					errors.rejectValue("name", "general.error.nameAlreadyInUse");
					break;
				} else {
					Context.evictFromSession(program);
				}
			}
			
			ValidationUtils.rejectIfEmptyOrWhitespace(errors, "concept", "error.concept");
		}
	}
}
