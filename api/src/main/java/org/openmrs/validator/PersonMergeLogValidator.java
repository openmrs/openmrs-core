/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
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
	 */
	public void validate(Object obj, Errors errors) {
		
		PersonMergeLog personMergeLog = (PersonMergeLog) obj;
		
		if (personMergeLog == null) {
			errors.rejectValue("persnMergeLog", "error.general");
		} else {
			ValidationUtils.rejectIfEmpty(errors, "personMergeLogData", "error.null");
			ValidationUtils.rejectIfEmpty(errors, "winner", "error.null");
			ValidationUtils.rejectIfEmpty(errors, "loser", "error.null");
		}
	}
	
}
