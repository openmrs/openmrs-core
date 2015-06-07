/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
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
