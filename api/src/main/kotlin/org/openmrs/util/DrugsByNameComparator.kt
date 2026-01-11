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

import org.openmrs.Drug
import java.io.Serializable

/**
 * The Class DrugsByNameComparator. An Util class which sorts drug names while ignoring any
 * numericals or other characters contained in the string. It will ignore all except letters a-z and
 * A-Z.
 */
class DrugsByNameComparator : Comparator<Drug>, Serializable {

    /**
     * @see Comparator.compare
     */
    override fun compare(d1: Drug, d2: Drug): Int {
        return compareDrugNamesIgnoringNumericals(d1, d2)
    }
    
    /**
     * Compare drug names ignoring numericals and other characters. Using compareToIgnoreCase()
     * method to prevent a capital letter getting precedence over a simple letter which comes before
     * it in the alphabet.
     * 
     * @param d1 the first Drug to be compared
     * @param d2 the second Drug to be compared
     * @return the int
     * Should return negative if name for drug1 comes before that of drug2
     * Should return zero if name for drug1 comes before that of drug2
     * Should return positive if name for drug1 comes before that of drug2 ignoring dashes
     * Should return positive if name for drug1 comes before that of drug2 ignoring numerics
     */
    private fun compareDrugNamesIgnoringNumericals(d1: Drug, d2: Drug): Int {
        val firstDrugName = remove(d1.name)
        val secondDrugName = remove(d2.name)
        
        return firstDrugName.compareTo(secondDrugName, ignoreCase = true)
    }
    
    /**
     * Private method which will remove all characters expect a-z and A to Z from text strings
     * 
     * @param drugName the drug name
     * @return the string
     */
    private fun remove(drugName: String?): String {
        return drugName?.replace("[^a-zA-Z]".toRegex(), "") ?: ""
    }
    
    companion object {
        private const val serialVersionUID = 1L
    }
}
