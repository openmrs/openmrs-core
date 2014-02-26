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
import org.openmrs.Field;
import org.openmrs.annotation.Handler;
import org.openmrs.api.APIException;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

/**
 * Validator for {@link Field} class
 *
 * @since 1.10
 */
@Handler(supports = { Field.class }, order = 50)
public class FieldValidator implements Validator {
	
	private static final Log log = LogFactory.getLog(FieldValidator.class);
	
	/**
	 * Returns whether or not this validator supports validating a given class.
	 *
	 * @param c The class to check for support.
	 * @see org.springframework.validation.Validator#supports(java.lang.Class)
	 */
	@SuppressWarnings("rawtypes")
	public boolean supports(Class c) {
		if (log.isDebugEnabled()) {
			log.debug(this.getClass().getName() + ".supports: " + c.getName());
		}
		return Field.class.isAssignableFrom(c);
	}
	
	/**
	 * Validates the given Field. 
	 * Ensures that the field name is present and valid
	 *
	 * @param obj The Field to validate.
	 * @param errors Errors
	 * @see org.springframework.validation.Validator#validate(java.lang.Object,
	 *      org.springframework.validation.Errors)
	 * @should fail if field name is null
	 * @should fail if field name is empty
	 * @should fail if field name is all whitespace
	 * @should fail if selectMultiple is null
	 * @should fail if retired is null
	 * @should pass if name is ok and fieldType, selectMultiple, and retired are non-null
	 * should not fail if fieldType is null
	 */
	public void validate(Object obj, Errors errors) throws APIException {
		if (log.isDebugEnabled()) {
			log.debug(this.getClass().getName() + ".validate...");
		}
		
		if (obj == null || !(obj instanceof Field)) {
			throw new IllegalArgumentException("The parameter obj should not be null and must be of type " + Field.class);
		}
		
		Field field = (Field) obj;
		
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "name", "error.null", "Field name is required");
		if (field.getSelectMultiple() == null) {
			errors.rejectValue("selectMultiple", "error.general");
		}
		if (field.getRetired() == null) {
			errors.rejectValue("retired", "error.general");
		}
	}
}
