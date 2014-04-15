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
import org.openmrs.ConceptMapType;
import org.openmrs.annotation.Handler;
import org.openmrs.api.context.Context;
import org.openmrs.util.OpenmrsUtil;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * Validates {@link ConceptMapType} objects.
 *
 * @since 1.9
 */
@Handler(supports = { ConceptMapType.class }, order = 50)
public class ConceptMapTypeValidator implements Validator {
	
	// Log for this class
	private static final Log log = LogFactory.getLog(ConceptMapTypeValidator.class);
	
	/**
	 * Determines if the command object being submitted is a valid type
	 *
	 * @see org.springframework.validation.Validator#supports(java.lang.Class)
	 */
	@SuppressWarnings("rawtypes")
	public boolean supports(Class c) {
		return ConceptMapType.class.isAssignableFrom(c);
	}
	
	/**
	 * Checks that a given concept map type object is valid.
	 *
	 * @see org.springframework.validation.Validator#validate(java.lang.Object,
	 *      org.springframework.validation.Errors)
	 * @should fail if the concept map type object is null
	 * @should fail if the name is null
	 * @should fail if the name is an empty string
	 * @should fail if the name is a white space character
	 * @should fail if the concept map type name is a duplicate
	 * @should pass if the name is unique amongst all concept map type names
	 */
	public void validate(Object obj, Errors errors) {
		
		if (obj == null || !(obj instanceof ConceptMapType)) {
			throw new IllegalArgumentException("The parameter obj should not be null and must be of type"
			        + ConceptMapType.class);
		}
		
		ConceptMapType conceptMapType = (ConceptMapType) obj;
		String name = conceptMapType.getName();
		if (!StringUtils.hasText(name)) {
			errors.rejectValue("name", "ConceptMapType.error.nameRequired",
			    "The name property is required for a concept map type");
			return;
		}
		
		name = name.trim();
		ConceptMapType duplicate = Context.getConceptService().getConceptMapTypeByName(name);
		if (duplicate != null) {
			if (!OpenmrsUtil.nullSafeEquals(duplicate.getUuid(), conceptMapType.getUuid())) {
				errors.rejectValue("name", "ConceptMapType.duplicate.name", "Duplicate concept map type name: " + name);
			}
		}
	}
}
