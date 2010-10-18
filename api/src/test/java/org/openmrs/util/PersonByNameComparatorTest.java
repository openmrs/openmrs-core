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

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.Person;
import org.openmrs.PersonName;
import org.openmrs.test.Verifies;

/**
 * This test class (should) contain tests for all of the {@link PersonByNameComparator} methods.
 */
public class PersonByNameComparatorTest {
	
	/**
	 * @see {@link PersonByNameComparator#compare(Person,Person)}
	 */
	@Test
	@Verifies(value = "should return negative if personName for person1 comes before that of person2", method = "comparePersons(Person,Person)")
	public void comparePersons_shouldReturnNegativeIfPersonNameForPerson1ComesBeforeThatOfPerson2() throws Exception {
		Person person1 = new Person();
		person1.addName(new PersonName("givenName", "middleName", "familyName"));
		Person person2 = new Person();
		person2.addName(new PersonName("givenName", "middleNamf", "familyName"));
		int actualValue = PersonByNameComparator.comparePersonsByName(person1, person2);
		Assert.assertTrue("Expected a negative value but it was: " + actualValue, actualValue < 0);
	}
	
	/**
	 * @see {@link PersonByNameComparator#compare(Person,Person)}
	 */
	@Test
	@Verifies(value = "should return positive if personName for person1 comes after that of person2", method = "comparePersons(Person,Person)")
	public void comparePersons_shouldReturnPositiveIfPersonNameForPerson1ComesAfterThatOfPerson2() throws Exception {
		Person person1 = new Person();
		person1.addName(new PersonName("givenName", "middleNamf", "familyName"));
		Person person2 = new Person();
		person2.addName(new PersonName("givenName", "middleName", "familyName"));
		int actualValue = PersonByNameComparator.comparePersonsByName(person1, person2);
		Assert.assertTrue("Expected a positive value but it was: " + actualValue, actualValue > 0);
	}
	
	/**
	 * @see {@link PersonByNameComparator#compare(Person,Person)}
	 */
	@Test
	@Verifies(value = "should return zero if the givenName middleName and familyName match", method = "comparePersons(Person,Person)")
	public void comparePersons_shouldReturnZeroIfTheGivenNameMiddleNameAndFamilyNameMatch() throws Exception {
		Person person1 = new Person();
		person1.addName(new PersonName("givenName", "middleName", "familyName"));
		Person person2 = new Person();
		person2.addName(new PersonName("givenName", "middleName", "familyName"));
		int actualValue = PersonByNameComparator.comparePersonsByName(person1, person2);
		Assert.assertTrue("Expected zero but it was: " + actualValue, actualValue == 0);
	}
}
