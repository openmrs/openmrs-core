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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.VisitType;
import org.openmrs.annotation.Handler;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

/**
 * Validates attributes on the {@link VisitType} object.
 * 
 * @since 1.9
 */
@Handler(supports = { VisitType.class }, order = 50)
public class VisitTypeValidator implements Validator {
	
	/** Log for this class and subclasses */
	protected final Log log = LogFactory.getLog(getClass());
	
	/**
	 * Determines if the command object being submitted is a valid type
	 * 
	 * @see org.springframework.validation.Validator#supports(java.lang.Class)
	 */
	@SuppressWarnings("unchecked")
	public boolean supports(Class c) {
		return c.equals(VisitType.class);
	}
	
	/**
	 * Checks the form object for any inconsistencies/errors
	 * 
	 * @see org.springframework.validation.Validator#validate(java.lang.Object,
	 *      org.springframework.validation.Errors)
	 * @should fail validation if name is null or empty or whitespace
	 * @should fail validation if description is null or empty or whitespace
	 * @should pass validation if all required fields have proper values
	 * @should pass validation if field lengths are correct
	 * @should fail validation if field lengths are not correct
	 */
	public void validate(Object obj, Errors errors) {
		VisitType visitType = (VisitType) obj;
		if (visitType == null) {
			errors.rejectValue("visitType", "error.general");
		} else {
			ValidationUtils.rejectIfEmptyOrWhitespace(errors, "name", "error.name");
			ValidateUtil.validateFieldLengths(errors, obj.getClass(), "name", "description", "retireReason");
		}
	}
}
