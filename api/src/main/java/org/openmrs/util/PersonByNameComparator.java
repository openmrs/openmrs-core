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

import org.openmrs.Person;
import org.openmrs.PersonName;

import java.util.Comparator;

/**
 * A simple person comparator for sorting persons by name. Sorts names based on the following
 * precedence: FamilyName, FamilyName2, GivenName, MiddleName, FamilyNamePrefix, FamilyNameSuffix
 * 
 * @since 1.8
 */
public class PersonByNameComparator implements Comparator<Person> {
	
	/**
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	public int compare(Person person1, Person person2) {
		
		return comparePersonsByName(person1, person2);
	}
	
	/**
	 * Compares two person objects by name
	 * 
	 * @should return negative if personName for person1 comes before that of person2
	 * @should return positive if personName for person1 comes after that of person2
	 * @should return zero if the givenName middleName and familyName match
	 * @should be case insensitive
	 * @since 1.8
	 */
	public static int comparePersonsByName(Person person1, Person person2) {
		
		// test for null cases (sorting them to be last in a list)
		if (person1 == null || person1.getPersonName() == null) {
			return 1;
		} else if (person2 == null || person2.getPersonName() == null) {
			return -1;
		}
		
		// if neither are null, do the actual comparison
		PersonName name1 = person1.getPersonName();
		PersonName name2 = person2.getPersonName();
		
		int ret = OpenmrsUtil.compareWithNullAsGreatest(name1.getFamilyName() != null ? name1.getFamilyName().toLowerCase()
		        : null, name2.getFamilyName() != null ? name2.getFamilyName().toLowerCase() : null);
		
		if (ret == 0) {
			ret = OpenmrsUtil.compareWithNullAsGreatest(name1.getFamilyName2() != null ? name1.getFamilyName().toLowerCase()
			        : null, name2.getFamilyName2() != null ? name2.getFamilyName2().toLowerCase() : null);
		}
		
		if (ret == 0) {
			ret = OpenmrsUtil.compareWithNullAsGreatest(name1.getGivenName() != null ? name1.getGivenName().toLowerCase()
			        : null, name2.getGivenName() != null ? name2.getGivenName().toLowerCase() : null);
		}
		
		if (ret == 0) {
			ret = OpenmrsUtil.compareWithNullAsGreatest(name1.getMiddleName() != null ? name1.getMiddleName().toLowerCase()
			        : null, name2.getMiddleName() != null ? name2.getMiddleName().toLowerCase() : null);
		}
		
		if (ret == 0) {
			ret = OpenmrsUtil.compareWithNullAsGreatest(name1.getFamilyNamePrefix() != null ? name1.getFamilyNamePrefix()
			        .toLowerCase() : null, name2.getFamilyNamePrefix() != null ? name2.getFamilyNamePrefix().toLowerCase()
			        : null);
		}
		
		if (ret == 0) {
			ret = OpenmrsUtil.compareWithNullAsGreatest(name1.getFamilyNameSuffix() != null ? name1.getFamilyNameSuffix()
			        .toLowerCase() : null, name2.getFamilyNameSuffix() != null ? name2.getFamilyNameSuffix().toLowerCase()
			        : null);
		}
		
		return ret;
	}
}
