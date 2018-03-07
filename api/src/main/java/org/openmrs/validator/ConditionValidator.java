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

import org.openmrs.Condition;
import org.openmrs.annotation.Handler;
import org.openmrs.api.APIException;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

/**
 * * Validates {@link Condition} objects
 * This class ensures that the condition object is valid and properly structured
 * 
 * @since 2.2
 */
@Handler(supports = {Condition.class}, order = 50)
public class ConditionValidator implements Validator {

	@Override
	public boolean supports(Class<?> aClass) {
		return ConditionValidator.class.isAssignableFrom(aClass);
	}

	/**
	 * @see org.springframework.validation.Validator#validate(java.lang.Object,
	 *      org.springframework.validation.Errors)
	 *      
	 * @should fail validation if condition object is null
	 * @should fail validation if condition object is not an instance of the Condition class
	 */
	@Override
	public void validate(Object object, Errors errors) {
		if(object == null){
			throw new IllegalArgumentException("The object parameter should not be null");
		}
		if(!(object instanceof Condition)){
			throw  new IllegalArgumentException("The object parameter should be of type " + Condition.class);
		}
		Condition condition = (Condition) object;
		if(condition.getCondition() == null){
			errors.rejectValue("condition", "Condition.conditionShouldNotBeNull", "The condition is required");
		}
		if(condition.getClinicalStatus() == null){
			errors.rejectValue("clinicalStatus", "Condition.clinicalStatusShouldNotBeNull", "The clinical status is required");
		}
	}
}
