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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A utility class that evaluates the concept ranges 
 */
public class ConceptRangeUtility {
	
	private static final String AGE_CRITERIA_EXPRESSION = "\\$\\{fn\\.getAge\\((\\d+)-(\\d+)\\)}";
	
	/**
	 * This method evaluates if person attributes fits a concept range.
	 * 
	 * @param criteria ConceptReferenceRange criteria
	 * @param person person to evaluate
	 * @return true if the person's age fits the criteria and false otherwise
	 */
	public boolean isAgeInRange(String criteria, Person person) {
		int age = person.getAge();
		boolean ageMatch = false;
		
		Pattern agePattern = Pattern.compile(AGE_CRITERIA_EXPRESSION);
		Matcher ageMatcher = agePattern.matcher(criteria);
		
		while (ageMatcher.find()) {
			if (ageMatcher.group(1) != null && ageMatcher.group(2) != null) {
				// Age range
				int minAge = Integer.parseInt(ageMatcher.group(1));
				int maxAge = Integer.parseInt(ageMatcher.group(2));
				ageMatch = age >= minAge && age <= maxAge;
			}
		}
		
		return ageMatch;
	}
}
