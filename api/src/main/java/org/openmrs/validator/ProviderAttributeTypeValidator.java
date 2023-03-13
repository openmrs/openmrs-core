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

import org.openmrs.ProviderAttributeType;
import org.openmrs.annotation.Handler;
import org.springframework.validation.Errors;

/**
 * Validates attributes on the {@link ProviderAttributeType} object.
 *
 * @since 1.9
 */
@Handler(supports = { ProviderAttributeType.class }, order = 50)
public class ProviderAttributeTypeValidator extends BaseAttributeTypeValidator<ProviderAttributeType> {
	
	/**
	 * Determines if the command object being submitted is a valid type
	 *
	 * @see org.springframework.validation.Validator#supports(java.lang.Class)
	 * <strong>Should</strong> pass validation if field lengths are correct
	 * <strong>Should</strong> fail validation if field lengths are not correct
	 */
	@Override
	public boolean supports(Class<?> c) {
		return ProviderAttributeType.class.isAssignableFrom(c);
	}
	
	@Override
	public void validate(Object obj, Errors errors) {
		if (obj != null) {
			super.validate(obj, errors);
			ValidateUtil.validateFieldLengths(errors, obj.getClass(), "name", "description", "datatypeClassname",
			    "preferredHandlerClassname", "retireReason");
		}
	}
}
