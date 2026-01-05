/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api

/**
 * The concept name type enumeration.
 *
 * **FULLY_SPECIFIED** - Indicates that the name is marked as the fully specified name, which is
 * returned by default for a locale in case there is no preferred name set. A concept can have only
 * one fully specified name per locale. It will also be set as the preferred name in its locale if
 * no name is explicitly set as preferred for the same locale and concept.
 *
 * **SHORT** - Indicates the name is marked as the short name for the concept. Only one name can
 * be marked short per locale for a concept.
 *
 * **INDEX_TERM** - Indicates that the name is marked as a search term for the concept. It could
 * be a common misspelled version of any of the names for the concept. Typically this name will be
 * used for searching purposes.
 *
 * NOTE: Any name with a null Concept name type is deemed a synonym. ONLY a fully specified name or
 * synonym can be marked as preferred.
 *
 * @see org.openmrs.ConceptName
 * @since 1.7
 */
enum class ConceptNameType {
    /** The fully specified name for a concept in a locale */
    FULLY_SPECIFIED,
    /** A short name/abbreviation for a concept */
    SHORT,
    /** A search/index term (may include common misspellings) */
    INDEX_TERM;

    companion object {
        /**
         * Safely parses a string to a ConceptNameType, returning null if not found.
         *
         * @param value the string value to parse
         * @return the matching ConceptNameType or null
         */
        @JvmStatic
        fun fromString(value: String?): ConceptNameType? =
            value?.uppercase()?.let { name ->
                entries.firstOrNull { it.name == name }
            }
    }
}
