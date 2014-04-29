/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
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
	@SuppressWarnings("unchecked")
	public boolean supports(Class c) {
		return Relationship.class.isAssignableFrom(c);
	}
	
	/**
	 * Checks that a given Relationship object is valid.
	 *
	 * @see org.springframework.validation.Validator#validate(java.lang.Object,
	 *      org.springframework.validation.Errors)
	 *
	 * @should fail if end date is prior to the start date
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
			if (startDate != null && endDate != null) {
				if (startDate.after(endDate)) {
					errors.reject("Relationship.InvalidEndDate.error");
				}
			}
		}
		
	}
	
}
