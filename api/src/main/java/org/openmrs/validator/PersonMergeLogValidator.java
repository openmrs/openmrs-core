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
import org.openmrs.annotation.Handler;
import org.openmrs.person.PersonMergeLog;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

/**
 * Validates the {@link PersonMergeLog} class.
 * 
 * @since 1.5
 */
@Handler(supports = { PersonMergeLog.class }, order = 50)
public class PersonMergeLogValidator implements Validator {
	
	/** Log for this class and subclasses */
	protected final Log log = LogFactory.getLog(getClass());
	
	/**
	 * Determines if the command object being submitted is a valid type
	 * 
	 * @see org.springframework.validation.Validator#supports(java.lang.Class)
	 */
	@SuppressWarnings("unchecked")
	public boolean supports(Class c) {
		return PersonMergeLog.class.isAssignableFrom(c);
	}
	
	/**
	 * Checks the form object for any inconsistencies/errors
	 * 
	 * @see org.springframework.validation.Validator#validate(java.lang.Object,
	 *      org.springframework.validation.Errors)
	 * @should fail validation if personMergeLogData is null
	 * @should fail validation if winner is null 
	 * @should fail validation if loser is null 
	 * @should pass validation if all fields are correct
	 * @should pass validation if field lengths are correct
	 * @should fail validation if field lengths are not correct
	 */
	public void validate(Object obj, Errors errors) {
		
		PersonMergeLog personMergeLog = (PersonMergeLog) obj;
		
		if (personMergeLog == null) {
			errors.rejectValue("persnMergeLog", "error.general");
		} else {
			ValidationUtils.rejectIfEmpty(errors, "personMergeLogData", "error.null");
			ValidationUtils.rejectIfEmpty(errors, "winner", "error.null");
			ValidationUtils.rejectIfEmpty(errors, "loser", "error.null");
			ValidateUtil.validateFieldLengths(errors, obj.getClass(), "voidReason");
		}
	}
	
}
