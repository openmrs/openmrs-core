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

import java.util.Date;

import org.openmrs.Relationship;
import org.openmrs.annotation.Handler;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * Validator for the Relationship class
 * @since 1.11.0
 */

@Handler(supports = { Relationship.class }, order = 50)
public class RelationshipValidator implements Validator {
	
	/**
	 * Determines if the command object being submitted is a valid type
	 *
	 * @see org.springframework.validation.Validator#supports(java.lang.Class)
	 */
	@Override
	public boolean supports(Class<?> c) {
		return Relationship.class.isAssignableFrom(c);
	}
	
	/**
	 * Checks that a given Relationship object is valid.
	 *
	 * @see org.springframework.validation.Validator#validate(java.lang.Object,
	 *      org.springframework.validation.Errors)
	 *
	 * <strong>Should</strong> fail if end date is prior to the start date
	 * <strong>Should</strong> fail if start date is a future date
	 * <strong>Should</strong> pass validation if field lengths are correct
	 * <strong>Should</strong> fail validation if field lengths are not correct
	 * @param target Relationship object to be validate
	 * @param errors Error object to hold any errors encounter in the test
	 *
	 *
	 **/
	@Override
	public void validate(Object target, Errors errors) {
		Relationship relationship = (Relationship) target;
		
		if (relationship != null) {
			Date startDate = relationship.getStartDate();
			Date endDate = relationship.getEndDate();
			if (startDate != null && endDate != null && startDate.after(endDate)) {
				errors.reject("Relationship.InvalidEndDate.error");
			}
			ValidateUtil.validateFieldLengths(errors, target.getClass(), "voidReason");
			if (startDate != null) {
				Date currentDate = new Date();
				if (startDate.after(currentDate)) {
					errors.reject("error.date.future");
				}
			}
		}
		
	}
	
}
