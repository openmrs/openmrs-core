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

import org.openmrs.EncounterRole;
import org.openmrs.annotation.Handler;
import org.openmrs.api.context.Context;
import org.openmrs.util.OpenmrsUtil;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;

/**
 * Validator for {@link org.openmrs.EncounterRole} class
 * 
 * @since 1.9
 */
@Handler(supports = { EncounterRole.class }, order = 50)
public class EncounterRoleValidator extends RequireNameValidator {
	
	/**
	 * Checks the form object for any inconsistencies/errors
	 * 
	 * @see org.springframework.validation.Validator#validate(java.lang.Object,
	 *      org.springframework.validation.Errors)
	 * @should fail validation if name is null or empty or whitespace
	 * @should fail validation if name is duplicate
	 * @should pass validation if all required fields have proper values
	 */
	public void validate(Object obj, Errors errors) {
		super.validate(obj, errors);
		EncounterRole encounterRole = (EncounterRole) obj;
		if (!errors.hasErrors()) {
			EncounterRole duplicate = Context.getEncounterService().getEncounterRoleByName(encounterRole.getName().trim());
			if (duplicate != null) {
				if (duplicate.getUuid() != null && !OpenmrsUtil.nullSafeEquals(encounterRole.getUuid(), duplicate.getUuid())) {
					errors.rejectValue("name", "encounterRole.duplicate.name",
					    "Specified Encounter Role name already exists, please specify another ");
				}
			}
		}
	}
}
