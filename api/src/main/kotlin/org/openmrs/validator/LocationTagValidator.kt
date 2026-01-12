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

import org.openmrs.LocationTag
import org.openmrs.annotation.Handler
import org.springframework.validation.Errors
import org.springframework.validation.ValidationUtils
import org.springframework.validation.Validator

/**
 * Validates [LocationTag] objects
 *
 * @since 1.7
 */
@Handler(supports = [LocationTag::class], order = 50)
class LocationTagValidator : Validator {
    
    /**
     * @see org.springframework.validation.Validator.supports
     */
    override fun supports(c: Class<*>): Boolean {
        return LocationTag::class.java.isAssignableFrom(c)
    }
    
    /**
     * @see org.springframework.validation.Validator.validate
     * **Should** pass validation if field lengths are correct
     * **Should** fail validation if field lengths are not correct
     */
    override fun validate(target: Any, errors: Errors) {
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "name", "LocationTag.error.name.required")
        ValidateUtil.validateFieldLengths(errors, target.javaClass, "name", "description", "retireReason")
    }
}
