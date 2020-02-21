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

import org.openmrs.ConceptDatatype;
import org.openmrs.annotation.Handler;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

/**
 * Validates attributes on the {@link ConceptDatatype} object.
 * 
 * @since 1.5
 */
@Handler(supports = { ConceptDatatype.class }, order = 50)
public class ConceptDatatypeValidator implements Validator {
	
	/**
	 * Determines if the command object being submitted is a valid type
	 * 
	 * @see org.springframework.validation.Validator#supports(java.lang.Class)
	 */
	@Override
	public boolean supports(Class<?> c) {
		return c.equals(ConceptDatatype.class);
	}
	
	/**
	 * Checks the form object for any inconsistencies/errors
	 * 
	 * @see org.springframework.validation.Validator#validate(java.lang.Object,
	 *      org.springframework.validation.Errors)
	 * <strong>Should</strong> pass validation if description is null or empty or whitespace	 *      
	 * <strong>Should</strong> fail validation if name is null or empty or whitespace
	 * <strong>Should</strong> pass validation if all required fields have proper values
	 * <strong>Should</strong> pass validation if field lengths are correct
	 * <strong>Should</strong> fail validation if field lengths are not correct
	 */
	@Override
	public void validate(Object obj, Errors errors) {
		ConceptDatatype cd = (ConceptDatatype) obj;
		if (cd == null) {
			errors.rejectValue("conceptDatatype", "error.general");
		} else {
			ValidationUtils.rejectIfEmptyOrWhitespace(errors, "name", "error.name");
			ValidateUtil.validateFieldLengths(errors, obj.getClass(), "name", "hl7Abbreviation", "description",
			    "retireReason");
		}
	}
	
}
