/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api.db.hibernate

/**
 * Represents different modes for searching patients.
 */
enum class PatientSearchMode {
    /** Search patients by name only */
    PATIENT_SEARCH_BY_NAME,
    /** Search patients by identifier only */
    PATIENT_SEARCH_BY_IDENTIFIER,
    /** Search patients that match both name AND identifier */
    PATIENT_SEARCH_BY_NAME_AND_IDENTIFIER,
    /** Search patients that match either name OR identifier */
    PATIENT_SEARCH_BY_NAME_OR_IDENTIFIER;

    companion object {
        /**
         * Safely parses a string to a PatientSearchMode, returning null if not found.
         *
         * @param value the string value to parse
         * @return the matching PatientSearchMode or null
         */
        @JvmStatic
        fun fromString(value: String?): PatientSearchMode? =
            value?.uppercase()?.let { name ->
                entries.firstOrNull { it.name == name }
            }
    }
}
