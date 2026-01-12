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

import org.springframework.validation.Errors
import org.springframework.validation.ValidationUtils
import org.springframework.validation.Validator
import java.beans.PropertyDescriptor

/**
 * Validates objects and requires that "name" be filled in. Used by other validators so that they
 * don't have to check the name every time.
 *
 * @since 1.5
 */
open class RequireNameValidator : Validator {
    
    /**
     * @see org.springframework.validation.Validator.supports
     */
    override fun supports(c: Class<*>): Boolean {
        return try {
            val pd = PropertyDescriptor("name", c)
            pd.readMethod != null && pd.writeMethod != null
        } catch (ex: Exception) {
            false
        }
    }
    
    /**
     * @see org.springframework.validation.Validator.validate
     * **Should** fail validation if name is null or empty or whitespace
     * **Should** pass validation if name has proper value
     */
    override fun validate(o: Any, errors: Errors) {
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "name", "error.name")
    }
}
