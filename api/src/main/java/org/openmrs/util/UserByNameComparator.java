/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.util;

import java.util.Comparator;

import org.openmrs.User;

/**
 * A simple user comparator for sorting users by personName. Sorts names based on the following
 * precedence: FamilyName, FamilyName2, GivenName, MiddleName, FamilyNamePrefix, FamilyNameSuffix
 *
 * @since 1.8
 */
public class UserByNameComparator implements Comparator<User> {
	
	/**
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 * @should sort users by personNames
	 */
	public int compare(User user1, User user2) {
		
		// test for null cases (sorting them to be last in a list)
		if (user1 == null) {
			return 1;
		} else if (user2 == null) {
			return -1;
		}
		
		// delegate to the personByNameComparator to sort by person names
		return PersonByNameComparator.comparePersonsByName(user1.getPerson(), user2.getPerson());
	}
}
