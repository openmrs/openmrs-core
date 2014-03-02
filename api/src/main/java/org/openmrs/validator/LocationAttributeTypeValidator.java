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
	 * @should fail validation if name is null
	 * @should fail validation if name already in use
	 * @should pass validation if the location attribute type description is null or empty or whitespace
	 * @should pass validation if all fields are correct
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
	}
	
}
