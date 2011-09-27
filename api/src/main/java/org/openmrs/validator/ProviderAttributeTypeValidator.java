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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.ProviderAttributeType;
import org.openmrs.annotation.Handler;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

/**
 * Validates attributes on the {@link ProviderAttributeType} object.
 * 
 * @since 1.9
 */
@Handler(supports = { ProviderAttributeType.class }, order = 50)
public class ProviderAttributeTypeValidator implements Validator {
	
	/** Log for this class and subclasses */
	protected final Log log = LogFactory.getLog(getClass());
	
	/**
	 * Determines if the command object being submitted is a valid type
	 * 
	 * @see org.springframework.validation.Validator#supports(java.lang.Class)
	 */
	@SuppressWarnings("unchecked")
	public boolean supports(Class c) {
		if (log.isDebugEnabled())
			log.debug(this.getClass().getName() + ".supports: " + c.getName());
		return ProviderAttributeType.class.isAssignableFrom(c);
	}
	
	/**
	 * Checks the form object for any inconsistencies/errors
	 * 
	 * @see org.springframework.validation.Validator#validate(java.lang.Object,
	 *      org.springframework.validation.Errors)
	 * @should fail validation if name is null or empty or whitespace
	 * @should fail validation if description is null or empty or whitespace
	 * @should pass validation if all required fields have proper values
	 */
	public void validate(Object obj, Errors errors) {
		ProviderAttributeType providerAttributeType = (ProviderAttributeType) obj;
		if (providerAttributeType == null) {
			errors.reject("error.general");
		} else {
			ValidationUtils.rejectIfEmptyOrWhitespace(errors, "name", "error.name");
			ValidationUtils.rejectIfEmptyOrWhitespace(errors, "minOccurs", "error.null");
			
			Integer minOccurs = providerAttributeType.getMinOccurs();
			Integer maxOccurs = providerAttributeType.getMaxOccurs();
			
			if (minOccurs != null) {
				if (minOccurs < 0) {
					errors.rejectValue("minOccurs", "AttributeType.minOccursShouldNotBeLessThanZero");
				}
			}
			
			if (maxOccurs != null) {
				if (maxOccurs < 0) {
					errors.rejectValue("maxOccurs", "AttributeType.maxOccursShouldNotBeLessThanZero");
				} else if (maxOccurs < minOccurs) {
					errors.rejectValue("maxOccurs", "AttributeType.maxOccursShouldNotBeLessThanMinOccurs");
				}
			}
		}
	}
}
