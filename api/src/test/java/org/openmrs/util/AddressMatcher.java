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

import org.hamcrest.Description;
import org.junit.internal.matchers.TypeSafeMatcher;
import org.openmrs.PersonAddress;
import org.openmrs.PersonName;

import java.util.Set;

public class AddressMatcher extends TypeSafeMatcher<Set<PersonAddress>> {
	
	private String address;
	
	public AddressMatcher(String address) {
		this.address = address;
	}
	
	@Override
	public boolean matchesSafely(Set<PersonAddress> personAddresses) {
		for (PersonAddress personAddress : personAddresses) {
			if (personAddress.toString().equals(address)) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public void describeTo(Description description) {
		description.appendText(address);
	}
	
	public static AddressMatcher containsAddress(String address) {
		return new AddressMatcher(address);
	}
}
