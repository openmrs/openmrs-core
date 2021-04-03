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

import org.openmrs.EncounterType;
import org.openmrs.annotation.Handler;
import org.openmrs.api.context.Context;
import org.openmrs.util.OpenmrsUtil;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

/**
 * Validates attributes on the {@link EncounterType} object.
 * 
 * @since 1.5
 */
@Handler(supports = { EncounterType.class }, order = 50)
public class EncounterTypeValidator implements Validator {
	
	/**
	 * Determines if the command object being submitted is a valid type
	 * 
	 * @see org.springframework.validation.Validator#supports(java.lang.Class)
	 */
	@Override
	public boolean supports(Class<?> c) {
		return c.equals(EncounterType.class);
	}
	
	/**
	 * Checks the form object for any inconsistencies/errors
	 * 
	 * @see org.springframework.validation.Validator#validate(java.lang.Object,
	 *      org.springframework.validation.Errors)
	 * <strong>Should</strong> fail validation if name is null or empty or whitespace
	 * <strong>Should</strong> fail validation if name is duplicate
	 * <strong>Should</strong> pass validation if description is null or empty or whitespace
	 * <strong>Should</strong> pass validation for an existing EncounterType
	 * <strong>Should</strong> pass validation if all required fields have proper values
	 * <strong>Should</strong> pass validation if field lengths are correct
	 * <strong>Should</strong> fail validation if field lengths are not correct
	 */
	@Override
	public void validate(Object obj, Errors errors) {
		EncounterType encounterType = (EncounterType) obj;
		if (encounterType == null) {
			errors.rejectValue("encounterType", "error.general");
		} else {
			ValidationUtils.rejectIfEmptyOrWhitespace(errors, "name", "error.name");
			
			if (!errors.hasErrors()) {
				EncounterType duplicate = Context.getEncounterService().getEncounterType(encounterType.getName().trim());
				if (duplicate != null && !OpenmrsUtil.nullSafeEquals(encounterType.getUuid(), duplicate.getUuid())
				        && !duplicate.getRetired()) {
					errors.rejectValue("name", "EncounterType.error.duplicateEncounterTypeNameSpecified",
					    "Specified Encounter Type name already exists, please specify another ");
				}
			}
			ValidateUtil.validateFieldLengths(errors, obj.getClass(), "name", "description", "retireReason");
		}
	}
}
