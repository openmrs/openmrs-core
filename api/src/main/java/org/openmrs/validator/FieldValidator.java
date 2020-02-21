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

import org.openmrs.Field;
import org.openmrs.annotation.Handler;
import org.openmrs.api.APIException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
	
	private static final Logger log = LoggerFactory.getLogger(FieldValidator.class);
	
	/**
	 * Returns whether or not this validator supports validating a given class.
	 *
	 * @param c The class to check for support.
	 * @see org.springframework.validation.Validator#supports(java.lang.Class)
	 */
	@Override
	public boolean supports(Class<?> c) {
		log.debug("{}.supports: {}", this.getClass().getName(), c.getName());
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
	 * <strong>Should</strong> fail if field name is null
	 * <strong>Should</strong> fail if field name is empty
	 * <strong>Should</strong> fail if field name is all whitespace
	 * <strong>Should</strong> fail if selectMultiple is null
	 * <strong>Should</strong> fail if retired is null
	 * <strong>Should</strong> pass if name is ok and fieldType, selectMultiple, and retired are non-null
	 * <strong>Should</strong> pass validation if field lengths are correct
	 * <strong>Should</strong> fail validation if field lengths are not correct
	 * should not fail if fieldType is null
	 */
	@Override
	public void validate(Object obj, Errors errors) throws APIException {
		log.debug("{}.validate...", this.getClass().getName());
		
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
		ValidateUtil.validateFieldLengths(errors, obj.getClass(), "name", "tableName", "attributeName", "retireReason");
	}
}
