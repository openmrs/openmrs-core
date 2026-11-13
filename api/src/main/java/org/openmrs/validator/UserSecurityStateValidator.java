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

import org.jspecify.annotations.NonNull;
import org.openmrs.UserSecurityState;
import org.openmrs.annotation.Handler;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

/**
 * Validates a {@link UserSecurityState} entry before it is stored.
 *
 * @since 3.0.0, 2.9.0
 */
@Handler(supports = { UserSecurityState.class }, order = 50)
public class UserSecurityStateValidator implements Validator {

	/**
	 * @see org.springframework.validation.Validator#supports(java.lang.Class)
	 */
	@Override
	public boolean supports(@NonNull Class<?> c) {
		return UserSecurityState.class.isAssignableFrom(c);
	}

	/**
	 * <strong>Should</strong> fail validation if user id is null <strong>Should</strong> fail
	 * validation if property is null <strong>Should</strong> fail validation if property is empty or
	 * whitespace <strong>Should</strong> fail validation if property is longer than 255 characters
	 * <strong>Should</strong> fail validation if value is null <strong>Should</strong> fail validation
	 * if value is empty or whitespace <strong>Should</strong> pass validation if all required fields
	 * have proper values
	 *
	 * @see org.springframework.validation.Validator#validate(java.lang.Object,
	 *      org.springframework.validation.Errors)
	 */
	@Override
	public void validate(@NonNull Object obj, @NonNull Errors errors) {
		UserSecurityState entry = (UserSecurityState) obj;
		if (entry.getUserId() == null) {
			errors.rejectValue("userId", "error.null");
		}
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "property", "error.null");
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "value", "error.null");
		ValidateUtil.validateFieldLengths(errors, UserSecurityState.class, "property");
	}
}
