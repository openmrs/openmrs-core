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

import org.openmrs.Drug;
import org.openmrs.annotation.Handler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
	
	// Logger for this class
	private static final Logger log = LoggerFactory.getLogger(ConceptDrugValidator.class);
	
	/**
	 * Determines if the command object being submitted is a valid type
	 *
	 * @see org.springframework.validation.Validator#supports(java.lang.Class)
	 * <strong>Should</strong> support Drug class
	 * <strong>Should</strong> reject classes not extending Drug
	 */
	@Override
	public boolean supports(Class<?> c) {
		return Drug.class.isAssignableFrom(c);
	}
	
	/**
	 * Checks that a given <code>Drug</code> object is valid.
	 *
	 * @param obj the Object to validate
	 * @param errors holds the validation errors
	 * @throws IllegalArgumentException Runtime Exception if the supplied argument is
	 *         null or not of type <code>Drug</code>
	 * @see org.springframework.validation.Validator#validate(java.lang.Object,
	 *      org.springframework.validation.Errors)
	 * <strong>Should</strong> fail if a concept is not specified
	 */
	@Override
	public void validate(Object obj, Errors errors) throws IllegalArgumentException {
		if (obj == null || !(obj instanceof Drug)) {
			throw new IllegalArgumentException("The parameter obj should not be null and must be of type" + Drug.class);
		}
		log.debug("request to validate drug having concept: " + ((Drug) obj).getConcept());
		ValidationUtils.rejectIfEmpty(errors, "concept", "ConceptDrug.error.conceptRequired");
	}
	
}
