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

import org.openmrs.ConceptMapType
import java.io.Serializable

/**
 * A utility class which sorts a collection of ConceptMapType objects in the following order:
 * 1. Regular
 * 2. Retired
 * 3. Hidden
 * 4. Retired and Hidden
 */
class ConceptMapTypeComparator : Comparator<ConceptMapType>, Serializable {

    /**
     * @see Comparator.compare
     */
    override fun compare(conceptMapType: ConceptMapType, conceptMapType2: ConceptMapType): Int {
        val firstWeight = getConceptMapTypeSortWeight(conceptMapType)
        val secondWeight = getConceptMapTypeSortWeight(conceptMapType2)
        
        return firstWeight.compareTo(secondWeight)
    }
    
    companion object {
        private const val serialVersionUID = 1L
        
        /**
         * This method calculates a weight used to decide the object's order in a collection.
         * @param conceptMapType the ConceptMapType object the weight of which is to be calculated
         * @return
         * 1. Regular: 0
         * 2. Retired: 1
         * 3. Hidden: 2
         * 4. Retired and Hidden: 3
         */
        @JvmStatic
        fun getConceptMapTypeSortWeight(conceptMapType: ConceptMapType): Int {
            return (if (conceptMapType.retired == true) 1 else 0) + (if (conceptMapType.isHidden == true) 2 else 0)
        }
    }
}
