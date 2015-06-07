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
import org.openmrs.attribute.Attribute;
import org.openmrs.customdatatype.CustomDatatypeUtil;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

/**
 * Common validator for all types of Attribute Types
 * @since 1.9
 */
@Handler(supports = { Attribute.class }, order = 50)
public class BaseAttributeValidator implements Validator {
	
	/**
	 * @see org.springframework.validation.Validator#supports(java.lang.Class)
	 */
	@Override
	public boolean supports(Class<?> clazz) {
		return Attribute.class.isAssignableFrom(clazz);
	}
	
	/**
	 * @see org.springframework.validation.Validator#validate(java.lang.Object, org.springframework.validation.Errors)
	 */
	@Override
	public void validate(Object target, Errors errors) {
		Attribute<?, ?> attribute = (Attribute<?, ?>) target;
		ValidationUtils.rejectIfEmpty(errors, "attributeType", "error.null");
		ValidationUtils.rejectIfEmpty(errors, "owner", "error.null");
		Object value = attribute.getValue();
		if (value == null) {
			errors.rejectValue("value", "error.null");
		} else if (!CustomDatatypeUtil.validate(attribute)) {
			errors.rejectValue("value", "error.invalid");
		}
		
	}
	
}
