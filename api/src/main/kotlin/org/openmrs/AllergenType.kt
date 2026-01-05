/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs

/**
 * Represents the type of allergen that causes an allergic reaction.
 */
enum class AllergenType {
    /** Drug/medication allergen */
    DRUG,
    /** Food allergen */
    FOOD,
    /** Environmental allergen (e.g., pollen, dust) */
    ENVIRONMENT,
    /** Other type of allergen */
    OTHER;

    companion object {
        /**
         * Safely parses a string to an AllergenType, returning null if not found.
         *
         * @param value the string value to parse
         * @return the matching AllergenType or null
         */
        @JvmStatic
        fun fromString(value: String?): AllergenType? =
            value?.uppercase()?.let { name ->
                entries.firstOrNull { it.name == name }
            }
    }
}
