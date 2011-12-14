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
