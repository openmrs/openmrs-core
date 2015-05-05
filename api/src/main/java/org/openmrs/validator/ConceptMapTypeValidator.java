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
	 * @should pass validation if field lengths are correct
	 * @should fail validation if field lengths are not correct
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
		if (duplicate != null && !OpenmrsUtil.nullSafeEquals(duplicate.getUuid(), conceptMapType.getUuid())) {
			errors.rejectValue("name", "ConceptMapType.duplicate.name", "Duplicate concept map type name: " + name);
		}
		ValidateUtil.validateFieldLengths(errors, obj.getClass(), "name", "description", "retireReason");
	}
}
