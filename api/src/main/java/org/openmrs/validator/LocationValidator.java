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

import org.openmrs.Location;
import org.openmrs.annotation.Handler;
import org.openmrs.api.context.Context;
import org.openmrs.util.OpenmrsUtil;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

/**
 * Validates the Location object.
 * 
 * @since 1.5
 **/
@Handler(supports = { Location.class }, order = 50)
public class LocationValidator extends BaseCustomizableValidator implements Validator {
	
	/**
	 * Determines if the command object being submitted is a valid type
	 * 
	 * @see org.springframework.validation.Validator#supports(java.lang.Class)
	 */
	@Override
	public boolean supports(Class<?> c) {
		return c.equals(Location.class);
	}
	
	/**
	 * Checks the form object for any inconsistencies/errors
	 * 
	 * @see org.springframework.validation.Validator#validate(java.lang.Object,
	 *      org.springframework.validation.Errors)
	 * <strong>Should</strong> fail validation if name is null or empty
	 * <strong>Should</strong> fail validation if retired and retireReason is null or empty
	 * <strong>Should</strong> set retired to false if retireReason is null or empty
	 * <strong>Should</strong> pass validation if all fields are correct
	 * <strong>Should</strong> pass validation if retired location is given retired reason
	 * <strong>Should</strong> fail validation if parent location creates a loop
	 * <strong>Should</strong> fail validation if name is exist in non retired locations
	 * <strong>Should</strong> pass validation if field lengths are correct
	 * <strong>Should</strong> fail validation if field lengths are not correct
	 */
	@Override
	public void validate(Object obj, Errors errors) {
		Location location = (Location) obj;
		if (location == null) {
			errors.rejectValue("location", "error.general");
		} else {
			ValidationUtils.rejectIfEmptyOrWhitespace(errors, "name", "error.name");
			
			if (location.getRetired() && !StringUtils.hasLength(location.getRetireReason())) {
				location.setRetired(false); // so that the jsp page displays
				// properly again
				errors.rejectValue("retireReason", "error.null");
			}
			
			Location exist = Context.getLocationService().getLocation(location.getName());
			if (exist != null && !exist.getRetired() && !OpenmrsUtil.nullSafeEquals(location.getUuid(), exist.getUuid())) {
				errors.rejectValue("name", "location.duplicate.name");
			}
			
			// Traverse all the way up (down?) to the root and check if it
			// equals the root.
			Location root = location;
			while (root.getParentLocation() != null) {
				root = root.getParentLocation();
				if (root.equals(location)) { // Have gone in a circle
					errors.rejectValue("parentLocation", "Location.parentLocation.error");
					break;
				}
			}
			ValidateUtil.validateFieldLengths(errors, obj.getClass(), "name", "description", "address1", "address2",
			    "cityVillage", "stateProvince", "country", "postalCode", "latitude", "longitude", "countyDistrict",
			    "address3", "address4", "address5", "address6", "address7", "address8", "address9", "address10",
			    "address11", "address12", "address13", "address14", "address15", "retireReason");
			super.validateAttributes(location, errors, Context.getLocationService().getAllLocationAttributeTypes());
		}
		
	}
	
}
