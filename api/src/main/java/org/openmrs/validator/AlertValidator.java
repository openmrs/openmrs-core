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

import org.openmrs.annotation.Handler;
import org.openmrs.notification.Alert;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

/**
 * Validates attributes on the {@link Alert} object.
 *
 * @since 1.10
 */
@Handler(supports = { Alert.class }, order = 50)
public class AlertValidator implements Validator {

	/**
	 * Determines if the command object being submitted is a valid type
	 *
	 * @see org.springframework.validation.Validator#supports(java.lang.Class)
	 */
	@Override
	public boolean supports(Class<?> c) {
		return Alert.class.isAssignableFrom(c);
	}
	
	/**
	 * @see org.springframework.validation.Validator#validate(java.lang.Object, org.springframework.validation.Errors)
	 * @should fail validation if Alert Text is null or empty or whitespace
	 * @should pass validation if all required values are set
	 * @should pass validation if field lengths are correct
	 * @should fail validation if field lengths are not correct
	 */
	@Override
	public void validate(Object obj, Errors errors) {
		Alert alert = (Alert) obj;
		if (alert == null) {
			errors.rejectValue("alert", "error.general");
		} else {
			ValidationUtils.rejectIfEmptyOrWhitespace(errors, "text", "Alert.text.required");
			ValidateUtil.validateFieldLengths(errors, obj.getClass(), "text");
		}
	}
}
