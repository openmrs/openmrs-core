/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
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
		if (user1 == null)
			return 1;
		else if (user2 == null)
			return -1;
		
		// delegate to the personByNameComparator to sort by person names
		return PersonByNameComparator.comparePersonsByName(user1.getPerson(), user2.getPerson());
	}
}
