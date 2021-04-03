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

import org.openmrs.Encounter;
import org.openmrs.annotation.Handler;
import org.openmrs.api.APIException;
import org.openmrs.notification.Alert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

	private static final Logger logger = LoggerFactory.getLogger(AlertValidator.class);

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
	 * <strong>Should</strong> fail validation if Alert Text is null or empty or whitespace
	 * <strong>Should</strong> pass validation if all required values are set
	 * <strong>Should</strong> pass validation if field lengths are correct
	 * <strong>Should</strong> fail validation if field lengths are not correct
	 */
	@Override
	public void validate(Object obj, Errors errors) throws APIException {
		logger.debug("{}.validate...", this.getClass().getName());

		if (obj == null || !(obj instanceof Alert)) {
			throw new IllegalArgumentException("error.general and must be of type " + Alert.class);
	}
		Alert alert = (Alert) obj;

			ValidationUtils.rejectIfEmptyOrWhitespace(errors, "text", "Alert.text.required");
			ValidateUtil.validateFieldLengths(errors, obj.getClass(), "text");

	}
}
