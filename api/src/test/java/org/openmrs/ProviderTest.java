/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * This tests methods on the provider object.
 */
public class ProviderTest {
	
	/**
	 * @see Provider#getName()
	 */
	@Test
	public void getName_shouldReturnPersonFullNameIfPersonIsNotNullOrNullOtherwise() {
		Provider provider = new Provider();
		
		Person person = new Person(1);
		person.addName(new PersonName("givenName", "middleName", "familyName"));
		provider.setPerson(person);
		assertEquals(person.getPersonName().getFullName(), provider.getName());
	}
	
	/**
	 * @see Provider#toString()
	 */
	@Test
	public void toString_shouldReturnPersonAllNamesWithSpecificFormat() {
		
		Provider provider = new Provider();
		provider.setProviderId(1);
		
		Person person = new Person(1);
		person.addName(new PersonName("givenName", "middleName", "familyName"));
		provider.setPerson(person);
		assertEquals(provider.toString(), "[Provider: providerId:1 providerName:[givenName middleName familyName] ]");
	}
	
}
