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
import org.openmrs.Form;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

/**
 * This checks a Form object to make sure that it passes all API requirements. E.g. it must have a
 * name and version, if it is retired it must have metadata about that, etc.
 */
public class FormValidator implements Validator {
	
	/** Log for this class and subclasses */
	protected final Log log = LogFactory.getLog(getClass());
	
	/**
	 * Determines if the command object being submitted is a valid type
	 * 
	 * @see org.springframework.validation.Validator#supports(java.lang.Class)
	 */
	@SuppressWarnings("unchecked")
	public boolean supports(Class c) {
		return c.equals(Form.class);
	}
	
	/**
	 * Checks the form object for any inconsistencies/errors
	 * 
	 * @see org.springframework.validation.Validator#validate(java.lang.Object,
	 *      org.springframework.validation.Errors)
	 */
	public void validate(Object obj, Errors errors) {
		Form form = (Form) obj;
		if (form == null) {
			errors.rejectValue("form", "error.general");
		} else {
			ValidationUtils.rejectIfEmptyOrWhitespace(errors, "name", "error.name");
			
			ValidationUtils.rejectIfEmptyOrWhitespace(errors, "version", "error.null");
			
			if (form.getVersion() != null && !form.getVersion().matches("^\\d.*$"))
				errors.rejectValue("version", "Form.version.invalid");
			
			if (form.isRetired()) {
				if (form.getRetiredBy() == null || form.getRetiredReason() == null)
					errors.rejectValue("retiredBy", "error.retired.requireMetadata");
			}
		}
	}
	
}
