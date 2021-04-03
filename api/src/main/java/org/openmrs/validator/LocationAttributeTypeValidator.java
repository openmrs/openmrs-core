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

import org.openmrs.LocationAttributeType;
import org.openmrs.annotation.Handler;
import org.openmrs.api.LocationService;
import org.openmrs.api.context.Context;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;

/**
 * Validates attributes on the {@link LocationAttributeType} object.
 *
 * @since 1.9
 */
@Handler(supports = { LocationAttributeType.class }, order = 50)
public class LocationAttributeTypeValidator extends BaseAttributeTypeValidator<LocationAttributeType> {
	
	/**
	 * @see org.springframework.validation.Validator#supports(java.lang.Class)
	 */
	@Override
	public boolean supports(Class<?> clazz) {
		return LocationAttributeType.class.isAssignableFrom(clazz);
	}
	
	/**
	 * @see org.springframework.validation.Validator#validate(java.lang.Object,
	 *      org.springframework.validation.Errors)
	 * <strong>Should</strong> fail validation if name is null
	 * <strong>Should</strong> fail validation if name already in use
	 * <strong>Should</strong> pass validation if the location attribute type description is null or empty or whitespace
	 * <strong>Should</strong> pass validation if all fields are correct
	 * <strong>Should</strong> pass validation if field lengths are correct
	 * <strong>Should</strong> fail validation if field lengths are not correct
	 */
	@Override
	public void validate(Object obj, Errors errors) {
		super.validate(obj, errors);
		LocationAttributeType locationObj = (LocationAttributeType) obj;
		LocationService ls = Context.getLocationService();
		if (locationObj.getName() != null && !locationObj.getName().isEmpty()) {
			LocationAttributeType loc = ls.getLocationAttributeTypeByName(locationObj.getName());
			if (loc != null && !loc.getUuid().equals(locationObj.getUuid())) {
				errors.rejectValue("name", "LocationAttributeType.error.nameAlreadyInUse");
			}
		} else {
			ValidationUtils.rejectIfEmptyOrWhitespace(errors, "name", "LocationAttributeType.error.nameEmpty");
		}
		ValidateUtil.validateFieldLengths(errors, obj.getClass(), "name", "description", "datatypeClassname",
		    "preferredHandlerClassname", "retireReason");
	}
	
}
