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
 * Represents different matching strategies for SQL LIKE patterns.
 */
enum class MatchMode {
    /** Match strings starting with the pattern (pattern%) */
    START,
    /** Match strings ending with the pattern (%pattern) */
    END,
    /** Match strings containing the pattern anywhere (%pattern%) */
    ANYWHERE,
    /** Match exact string (no wildcards) */
    EXACT;

    /**
     * Converts the input string to a SQL LIKE pattern, preserving case.
     *
     * @param str the string to convert
     * @return the pattern string with wildcards, or null if input is null
     */
    fun toCaseSensitivePattern(str: String?): String? = toPatternInternal(str, lowercase = false)

    /**
     * Converts the input string to a lowercase SQL LIKE pattern.
     *
     * @param str the string to convert
     * @return the lowercase pattern string with wildcards, or null if input is null
     */
    fun toLowerCasePattern(str: String?): String? = toPatternInternal(str, lowercase = true)

    private fun toPatternInternal(str: String?, lowercase: Boolean): String? {
        if (str == null) return null

        val processedStr = if (lowercase) str.lowercase() else str

        return when (this) {
            START -> "$processedStr%"
            END -> "%$processedStr"
            ANYWHERE -> "%$processedStr%"
            EXACT -> processedStr
        }
    }

    companion object {
        /**
         * Safely parses a string to a MatchMode, returning null if not found.
         *
         * @param value the string value to parse
         * @return the matching MatchMode or null
         */
        @JvmStatic
        fun fromString(value: String?): MatchMode? =
            value?.uppercase()?.let { name ->
                entries.firstOrNull { it.name == name }
            }
    }
}
