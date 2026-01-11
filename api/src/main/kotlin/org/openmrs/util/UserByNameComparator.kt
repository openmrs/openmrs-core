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

import org.openmrs.User
import java.io.Serializable

/**
 * A simple user comparator for sorting users by personName. Sorts names based on the following
 * precedence: FamilyName, FamilyName2, GivenName, MiddleName, FamilyNamePrefix, FamilyNameSuffix
 *
 * @since 1.8
 */
class UserByNameComparator : Comparator<User>, Serializable {
	
	/**
	 * @see Comparator.compare
	 * <strong>Should</strong> sort users by personNames
	 */
	override fun compare(user1: User?, user2: User?): Int {
		// test for null cases (sorting them to be last in a list)
		return when {
			user1 == null -> 1
			user2 == null -> -1
			else -> {
				// delegate to the personByNameComparator to sort by person names
				PersonByNameComparator.comparePersonsByName(user1.person, user2.person)
			}
		}
	}
	
	companion object {
		private const val serialVersionUID = 1L
	}
}
