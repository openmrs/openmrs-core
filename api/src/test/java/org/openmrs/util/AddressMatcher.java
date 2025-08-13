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
import org.hamcrest.TypeSafeDiagnosingMatcher;
import org.openmrs.PersonAddress;

public class AddressMatcher extends TypeSafeDiagnosingMatcher<Set<PersonAddress>> {

	private final String expectedAddress;

	public AddressMatcher(String expectedAddress) {
		this.expectedAddress = expectedAddress;
	}

	@Override
	protected boolean matchesSafely(Set<PersonAddress> personAddresses, Description mismatchDescription) {
		for (PersonAddress personAddress : personAddresses) {
			if (personAddress != null && personAddress.toString().equals(expectedAddress)) {
				return true;
			}
		}

		mismatchDescription.appendText("no address matched ").appendValue(expectedAddress);
		return false;
	}

	@Override
	public void describeTo(Description description) {
		description.appendValue(expectedAddress);
	}

	public static AddressMatcher containsAddress(String expectedAddress) {
		return new AddressMatcher(expectedAddress);
	}
}
