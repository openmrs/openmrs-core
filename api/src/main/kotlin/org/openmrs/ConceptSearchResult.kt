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
 * An Object of this class represents a search result returned when searching for concepts, it holds
 * extra metadata about the matched concept(s).
 *
 * @since 1.8
 */
class ConceptSearchResult(
	/**
	 * The single word that will be matched to search terms
	 */
	var word: String? = null,
	
	/**
	 * The concept that is being matched to
	 */
	var concept: Concept? = null,
	
	/**
	 * The specific name that will be matched
	 */
	var conceptName: ConceptName? = null,
	
	/**
	 * The weight for this conceptSearchResult
	 */
	var transientWeight: Double = 0.0
) {
	
	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (other !is ConceptSearchResult) return false
		
		return concept?.equals(other.concept) ?: false
	}
	
	override fun hashCode(): Int {
		return concept?.hashCode() ?: super.hashCode()
	}
	
	companion object {
		private const val serialVersionUID = 6394792520635644989L
	}
}
