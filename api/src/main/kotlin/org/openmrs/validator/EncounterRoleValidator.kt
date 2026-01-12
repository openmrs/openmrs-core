/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.validator

import org.openmrs.EncounterRole
import org.openmrs.annotation.Handler
import org.openmrs.api.context.Context
import org.openmrs.util.OpenmrsUtil
import org.springframework.validation.Errors

/**
 * Validator for [org.openmrs.EncounterRole] class
 *
 * @since 1.9
 */
@Handler(supports = [EncounterRole::class], order = 50)
class EncounterRoleValidator : RequireNameValidator() {
    
    /**
     * Checks the form object for any inconsistencies/errors
     *
     * @see org.springframework.validation.Validator.validate
     * **Should** fail validation if name is null or empty or whitespace
     * **Should** fail validation if name is duplicate
     * **Should** pass validation if all required fields have proper values
     * **Should** pass validation if field lengths are correct
     * **Should** fail validation if field lengths are not correct
     */
    override fun validate(obj: Any, errors: Errors) {
        super.validate(obj, errors)
        val encounterRole = obj as EncounterRole
        
        if (!errors.hasErrors()) {
            val duplicate = Context.getEncounterService().getEncounterRoleByName(encounterRole.name?.trim())
            if (duplicate != null && duplicate.uuid != null &&
                !OpenmrsUtil.nullSafeEquals(encounterRole.uuid, duplicate.uuid)) {
                errors.rejectValue(
                    "name",
                    "encounterRole.duplicate.name",
                    "Specified Encounter Role name already exists, please specify another "
                )
            }
            ValidateUtil.validateFieldLengths(errors, obj.javaClass, "name", "description", "retireReason")
        }
    }
}
