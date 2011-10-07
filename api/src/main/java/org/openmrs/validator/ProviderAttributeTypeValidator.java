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

import org.openmrs.ProviderAttributeType;
import org.openmrs.annotation.Handler;

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
	 */
	public boolean supports(Class<?> c) {
		return ProviderAttributeType.class.isAssignableFrom(c);
	}
	
}
