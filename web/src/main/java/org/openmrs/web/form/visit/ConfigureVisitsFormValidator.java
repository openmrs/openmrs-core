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
package org.openmrs.web.form.visit;

import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

/**
 * Validates {@link ConfigureVisitsForm}
 */
public class ConfigureVisitsFormValidator implements Validator {
	
	@Override
	public boolean supports(Class<?> clazz) {
		return ConfigureVisitsForm.class.isAssignableFrom(clazz);
	}
	
	@Override
	public void validate(Object target, Errors errors) {
		ConfigureVisitsForm form = (ConfigureVisitsForm) target;
		
		if (form.isEnableVisits()) {
			ValidationUtils.rejectIfEmptyOrWhitespace(errors, "visitEncounterHandler",
			    "Encounter.error.visits.handler.empty");
		}
	}
	
}
