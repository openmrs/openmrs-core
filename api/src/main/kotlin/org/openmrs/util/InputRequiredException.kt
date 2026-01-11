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

/**
 * Used by the [DatabaseUpdater] to signal that a given update requires input from the user.
 * 
 * @since 1.5
 */
class InputRequiredException(
    /**
     * The user prompts and datatype for each question that the user has to provide input for
     */
    val requiredInput: Map<String, DATATYPE>
) : Exception("Input is required before being able to update the database") {
    
    /**
     * Required input will be in one of these forms
     */
    enum class DATATYPE {
        STRING,
        INTEGER,
        DOUBLE,
        DATE
    }
    
    companion object {
        const val serialVersionUID = 121994323413L
    }
}
