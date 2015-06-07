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

import org.openmrs.LocationTag;
import org.openmrs.annotation.Handler;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

/**
 * Validates {@link LocationTag} objects
 * 
 * @since 1.7
 */
@Handler(supports = { LocationTag.class }, order = 50)
public class LocationTagValidator implements Validator {
	
	/**
	 * @see org.springframework.validation.Validator#supports(java.lang.Class)
	 */
	@Override
	public boolean supports(Class clazz) {
		return LocationTag.class.isAssignableFrom(clazz);
	}
	
	/**
	 * @see org.springframework.validation.Validator#validate(java.lang.Object,
	 *      org.springframework.validation.Errors)
	 * @should pass validation if field lengths are correct
	 * @should fail validation if field lengths are not correct
	 */
	@Override
	public void validate(Object target, Errors errors) {
		if (target != null) {
			ValidationUtils.rejectIfEmptyOrWhitespace(errors, "name", "LocationTag.error.name.required");
			ValidateUtil.validateFieldLengths(errors, target.getClass(), "name", "description", "retireReason");
		}
	}
	
}
