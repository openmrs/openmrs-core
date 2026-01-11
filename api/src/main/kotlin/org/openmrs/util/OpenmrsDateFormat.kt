/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.util

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.regex.Pattern

/**
 * An extension of SimpleDateFormat that defaults to setLenient(false) and for date patterns
 * that end in yyyy rejects any dates that don't end in 4 digits (to prevent against
 * 2-digit years being interpreted incorrectly); for date patterns that don't end in yyyy, it verifies
 * that the date string is the same length as the pattern string
 */
class OpenmrsDateFormat(sdf: SimpleDateFormat, locale: Locale) : SimpleDateFormat(sdf.toPattern(), locale) {
    
    init {
        isLenient = false
    }
    
    override fun parse(text: String): Date {
        // first test to see if the pattern ends in "{non-alphanumeric-character}yyyy"
        val patternMatch = Pattern.compile("\\Wyyyy$").matcher(toPattern())
        if (patternMatch.find()) {
            // if it does, make sure that the string to parse ends in "{non-alphanumeric-character}{digit}{digit}{digit}{digit}"
            val dateMatch = Pattern.compile("\\W\\d{4}$").matcher(text)
            if (!dateMatch.find()) {
                throw ParseException("Unparseable date \"$text\" - year must have 4 digits", 0)
            }
        } else {
            // otherwise, verify that the pattern and the string are the same length
            if (toPattern().length != text.length) {
                throw ParseException(
                    "Unparseable date \"$text\" - length of date string doesn't match length of date pattern",
                    0
                )
            }
        }
        
        // if we've passed this validation, just call the SimpleDateFormat.parse() method
        return super.parse(text)
    }
    
    companion object {
        private const val serialVersionUID = 1L
    }
}
