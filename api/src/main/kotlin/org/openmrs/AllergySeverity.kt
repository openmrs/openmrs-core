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
 * Represents the severity level of an allergic reaction.
 */
enum class AllergySeverity {
    /** Severity is unknown or not assessed */
    UNKNOWN,
    /** Non-allergic intolerance (e.g., lactose intolerance) */
    INTOLERANCE,
    /** Mild allergic reaction */
    MILD,
    /** Moderate allergic reaction */
    MODERATE,
    /** Severe allergic reaction (potentially life-threatening) */
    SEVERE;

    companion object {
        /**
         * Safely parses a string to an AllergySeverity, returning null if not found.
         *
         * @param value the string value to parse
         * @return the matching AllergySeverity or null
         */
        @JvmStatic
        fun fromString(value: String?): AllergySeverity? =
            value?.uppercase()?.let { name ->
                entries.firstOrNull { it.name == name }
            }
    }
}
