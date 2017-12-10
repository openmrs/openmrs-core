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

import org.openmrs.ConceptSource;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

/**
 * Validates attributes on the {@link org.openmrs.ConceptSource} object.
 *
 */
public class ConceptSourceValidator implements Validator {
	
	/**
	 * Determines if the command object being submitted is a valid type
	 *
	 * @see org.springframework.validation.Validator#supports(java.lang.Class)
	 */
	@Override
	public boolean supports(Class<?> c) {
		return c.equals(ConceptSource.class);
	}
	
	/**
	 * Checks the form object for any inconsistencies/errors
	 * 
	 * 	 * @see org.springframework.validation.Validator#validate(java.lang.Object,
	 *      org.springframework.validation.Errors)
	 * @should fail validation if name is null or empty or whitespace
	 * @should fail validation if description is null or empty or whitespace
	 * @should pass validation if HL7 Code is null or empty or whitespace
	 * @should pass validation if all required fields have proper values
	 * @should pass validation if field lengths are correct
	 * @should fail validation if field lengths are not correct
	 */
	@Override
	public void validate(Object obj, Errors errors) throws IllegalArgumentException {
		if (obj == null || !(obj instanceof ConceptSource)) {
			throw new IllegalArgumentException("The parameter obj should not be null and must be of type "
			        + ConceptSource.class);
		} else {
			ValidationUtils.rejectIfEmptyOrWhitespace(errors, "name", "error.name");
			ValidationUtils.rejectIfEmptyOrWhitespace(errors, "description", "error.null");
			ValidateUtil.validateFieldLengths(errors, obj.getClass(), "name", "hl7Code", "uniqueId", "description",
			    "retireReason");
		}
		
	}
}
