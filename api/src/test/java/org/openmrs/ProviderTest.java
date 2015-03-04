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

import junit.framework.Assert;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.Test;
import org.openmrs.test.Verifies;

/**
 * This tests methods on the provider object.
 */
public class ProviderTest {
	
	/**
	 * @see {@link Provider#setPerson(Person)}
	 */
	@Test
	@Verifies(value = "should blank out name if set to non null person", method = "setPerson(Person)")
	public void setPerson_shouldBlankOutNameIfSetToNonNullPerson() throws Exception {
		final String providerName = "Provider Name";
		final String nameField = "name";
		
		Provider provider = new Provider();
		provider.setName(providerName);
		Assert.assertEquals(providerName, FieldUtils.readField(provider, nameField, true));
		
		Person person = new Person(1);
		person.addName(new PersonName("givenName", "middleName", "familyName"));
		provider.setPerson(person);
		Assert.assertNull(FieldUtils.readField(provider, nameField, true));
	}
	
	/**
	 * @see {@link Provider#getName()}
	 */
	@Test
	@Verifies(value = "return person full name if person is not null", method = "getName()")
	public void getName_shouldReturnPersonFullNameIfPersonIsNotNull() throws Exception {
		final String providerName = "Provider Name";
		
		Provider provider = new Provider();
		provider.setName(providerName);
		Assert.assertEquals(providerName, provider.getName());
		
		Person person = new Person(1);
		person.addName(new PersonName("givenName", "middleName", "familyName"));
		provider.setPerson(person);
		Assert.assertEquals(person.getPersonName().getFullName(), provider.getName());
	}
}
