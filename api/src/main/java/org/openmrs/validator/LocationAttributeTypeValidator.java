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
	
}
