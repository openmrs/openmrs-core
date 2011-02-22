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
import org.openmrs.Drug;
import org.openmrs.annotation.Handler;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

/**
 * Validates {@link Drug} objects.
 * 
 * @since 1.9
 */
@Handler(supports = { Drug.class }, order = 50)
public class ConceptDrugValidator implements Validator {
	
	// Log for this class
	private static final Log log = LogFactory.getLog(ConceptDrugValidator.class);
	
	/**
	 * Determines if the command object being submitted is a valid type
	 * 
	 * @see org.springframework.validation.Validator#supports(java.lang.Class)
	 * @should support Drug class
	 * @should reject classes not extending Drug
	 */
	@SuppressWarnings("rawtypes")
	public boolean supports(Class c) {
		return Drug.class.isAssignableFrom(c);
	}
	
	/**
	 * Checks that a given <code>Drug</code> object is valid.
	 * 
	 * @param obj the Object to validate
	 * @param errors holds the validation errors
	 * @throws <code>IllegalArgumentException</code> Runtime Exception if the supplied argument is
	 *         null or not of type <code>Drug</code>
	 * @see org.springframework.validation.Validator#validate(java.lang.Object,
	 *      org.springframework.validation.Errors)
	 * @should fail if a concept is not specified
	 */
	public void validate(Object obj, Errors errors) throws IllegalArgumentException {
		if (obj == null || !(obj instanceof Drug))
			throw new IllegalArgumentException("The parameter obj should not be null and must be of type" + Drug.class);
		log.debug("request to validate drug having concept: " + ((Drug) obj).getConcept());
		ValidationUtils.rejectIfEmpty(errors, "concept", "ConceptDrug.error.conceptRequired");
	}
	
}
