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

import org.openmrs.EncounterRole;
import org.openmrs.annotation.Handler;
import org.openmrs.api.context.Context;
import org.openmrs.util.OpenmrsUtil;
import org.springframework.validation.Errors;

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
	 * <strong>Should</strong> fail validation if name is null or empty or whitespace
	 * <strong>Should</strong> fail validation if name is duplicate
	 * <strong>Should</strong> pass validation if all required fields have proper values
	 * <strong>Should</strong> pass validation if field lengths are correct
	 * <strong>Should</strong> fail validation if field lengths are not correct
	 */
	@Override
	public void validate(Object obj, Errors errors) {
		super.validate(obj, errors);
		EncounterRole encounterRole = (EncounterRole) obj;
		if (!errors.hasErrors()) {
			EncounterRole duplicate = Context.getEncounterService().getEncounterRoleByName(encounterRole.getName().trim());
			if (duplicate != null && duplicate.getUuid() != null
			        && !OpenmrsUtil.nullSafeEquals(encounterRole.getUuid(), duplicate.getUuid())) {
				errors.rejectValue("name", "encounterRole.duplicate.name",
				    "Specified Encounter Role name already exists, please specify another ");
			}
			ValidateUtil.validateFieldLengths(errors, obj.getClass(), "name", "description", "retireReason");
		}
	}
}
