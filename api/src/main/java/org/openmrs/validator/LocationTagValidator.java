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

import org.apache.commons.lang3.StringUtils;
import org.openmrs.LocationTag;
import org.openmrs.annotation.Handler;
import org.openmrs.api.context.Context;
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
	public boolean supports(Class<?> c) {
		return LocationTag.class.isAssignableFrom(c);
	}
	
	/**
	 * @see org.springframework.validation.Validator#validate(java.lang.Object,
	 *      org.springframework.validation.Errors)
	 * <strong>Should</strong> pass validation if field lengths are correct
	 * <strong>Should</strong> fail validation if field lengths are not correct
	 * <strong>Should</strong> fail validation if name is already in use
	 */
	@Override
	public void validate(Object target, Errors errors) {
		if (target != null) {
			LocationTag locationTag = (LocationTag) target;
			if (StringUtils.isNotBlank(locationTag.getName())) {
				LocationTag existingLocationTag = Context.getLocationService().getLocationTagByName(locationTag.getName());
				if (existingLocationTag != null && !existingLocationTag.getUuid().equals(locationTag.getUuid())) {
					errors.rejectValue("name", "general.error.nameAlreadyInUse");
					return;
				}
			}
			ValidationUtils.rejectIfEmptyOrWhitespace(errors, "name", "LocationTag.error.name.required");
			ValidateUtil.validateFieldLengths(errors, target.getClass(), "name", "description", "retireReason");
		}
	}
	
}
