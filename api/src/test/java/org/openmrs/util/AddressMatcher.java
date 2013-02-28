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
