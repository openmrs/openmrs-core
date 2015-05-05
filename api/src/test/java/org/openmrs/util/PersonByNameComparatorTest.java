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
	 * @see {@link PersonByNameComparator#comparePersonsByName(Person,Person)}
	 */
	@Test
	@Verifies(value = "should return negative if personName for person1 comes before that of person2", method = "comparePersonsByName(Person,Person)")
	public void comparePersonsByName_shouldReturnNegativeIfPersonNameForPerson1ComesBeforeThatOfPerson2() throws Exception {
		Person person1 = new Person();
		person1.addName(new PersonName("givenName", "middleName", "familyName"));
		Person person2 = new Person();
		person2.addName(new PersonName("givenName", "middleNamf", "familyName"));
		int actualValue = PersonByNameComparator.comparePersonsByName(person1, person2);
		Assert.assertTrue("Expected a negative value but it was: " + actualValue, actualValue < 0);
	}
	
	/**
	 * @see {@link PersonByNameComparator#comparePersonsByName(Person,Person)}
	 */
	@Test
	@Verifies(value = "should return positive if personName for person1 comes after that of person2", method = "comparePersonsByName(Person,Person)")
	public void comparePersonsByName_shouldReturnPositiveIfPersonNameForPerson1ComesAfterThatOfPerson2() throws Exception {
		Person person1 = new Person();
		person1.addName(new PersonName("givenName", "middleNamf", "familyName"));
		Person person2 = new Person();
		person2.addName(new PersonName("givenName", "middleName", "familyName"));
		int actualValue = PersonByNameComparator.comparePersonsByName(person1, person2);
		Assert.assertTrue("Expected a positive value but it was: " + actualValue, actualValue > 0);
	}
	
	/**
	 * @see {@link PersonByNameComparator#comparePersonsByName(Person,Person)}
	 */
	@Test
	@Verifies(value = "should return zero if the givenName middleName and familyName match", method = "comparePersonsByName(Person,Person)")
	public void comparePersonsByName_shouldReturnZeroIfTheGivenNameMiddleNameAndFamilyNameMatch() throws Exception {
		Person person1 = new Person();
		person1.addName(new PersonName("givenName", "middleName", "familyName"));
		Person person2 = new Person();
		person2.addName(new PersonName("givenName", "middleName", "familyName"));
		int actualValue = PersonByNameComparator.comparePersonsByName(person1, person2);
		Assert.assertTrue("Expected zero but it was: " + actualValue, actualValue == 0);
	}
	
	@Test
	@Verifies(value = "should not be case-sensitive", method = "comparePersonsByName(Person,Person)")
	public void comparePersonsByName_shouldNotBeCaseSensitive() throws Exception {
		Person person1 = new Person();
		person1.addName(new PersonName("GIVENNAME", "MIDDLENAME", "FAMILYNAME"));
		Person person2 = new Person();
		person2.addName(new PersonName("givenName", "middleName", "familyName"));
		int actualValue = PersonByNameComparator.comparePersonsByName(person1, person2);
		Assert.assertTrue("Expected zero but it was: " + actualValue, actualValue == 0);
	}
}
