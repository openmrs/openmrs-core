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
	
	/**
	 * @see {@link Provider#toString()}
	 */
	@Test
	@Verifies(value = "return person all names of person with specific format", method = "toString()")
	public void toString_shouldReturnPersonAllNamesWithSpecificFormat() throws Exception {
		final String providerName = "Provider Name";
		
		Provider provider = new Provider();
		provider.setName(providerName);
		provider.setProviderId(1);
		
		Person person = new Person(1);
		person.addName(new PersonName("givenName", "middleName", "familyName"));
		provider.setPerson(person);
		Assert.assertEquals(provider.toString(), "[Provider: providerId:1 providerName:[givenName middleName familyName] ]");
	}
	
}
