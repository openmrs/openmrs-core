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

import org.openmrs.Provider
import java.io.Serializable

/**
 * Sorts providers by the primary person name associated with the underlying person
 *
 * Note that this ignores any values stored in the provider "name" property and sorts
 * solely on the underlying person name
 *
 * Utilizes the [PersonByNameComparator] comparator to do the underlying sort
 */
class ProviderByPersonNameComparator : Comparator<Provider>, Serializable {
	
	override fun compare(provider1: Provider?, provider2: Provider?): Int {
		// test for null cases (sorting them to be last in a list)
		val provider1IsNull = provider1 == null || provider1.person == null
		val provider2IsNull = provider2 == null || provider2.person == null
		
		return when {
			provider1IsNull && provider2IsNull -> 0
			provider1IsNull -> 1
			provider2IsNull -> -1
			else -> {
				// delegate to the person by name comparator
				PersonByNameComparator().compare(provider1!!.person, provider2!!.person)
			}
		}
	}
	
	companion object {
		private const val serialVersionUID = 1L
	}
}
