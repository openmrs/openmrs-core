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

import java.util.Set;

import org.hamcrest.Description;
import org.junit.internal.matchers.TypeSafeMatcher;
import org.openmrs.PersonName;

public class NameMatcher extends TypeSafeMatcher<Set<PersonName>> {
	
	private String fullName;
	
	public NameMatcher(String fullName) {
		this.fullName = fullName;
	}
	
	@Override
	public boolean matchesSafely(Set<PersonName> personNames) {
		for (PersonName personName : personNames) {
			if (personName.getFullName().equals(fullName)) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public void describeTo(Description description) {
		description.appendText(fullName);
	}
	
	public static NameMatcher containsFullName(String fullName) {
		return new NameMatcher(fullName);
	}
}
