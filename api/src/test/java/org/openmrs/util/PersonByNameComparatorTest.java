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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.openmrs.Person;
import org.openmrs.PersonName;

/**
 * This test class (should) contain tests for all of the {@link PersonByNameComparator} methods.
 */
public class PersonByNameComparatorTest {
	
	/**
	 * @see PersonByNameComparator#comparePersonsByName(Person,Person)
	 */
	@Test
	public void comparePersonsByName_shouldReturnNegativeIfPersonNameForPerson1ComesBeforeThatOfPerson2() {
		Person person1 = new Person();
		person1.addName(new PersonName("givenName", "middleName", "familyName"));
		Person person2 = new Person();
		person2.addName(new PersonName("givenName", "middleNamf", "familyName"));
		int actualValue = PersonByNameComparator.comparePersonsByName(person1, person2);
		assertTrue(actualValue < 0, "Expected a negative value but it was: " + actualValue);
	}
	
	/**
	 * @see PersonByNameComparator#comparePersonsByName(Person,Person)
	 */
	@Test
	public void comparePersonsByName_shouldReturnPositiveIfPersonNameForPerson1ComesAfterThatOfPerson2() {
		Person person1 = new Person();
		person1.addName(new PersonName("givenName", "middleNamf", "familyName"));
		Person person2 = new Person();
		person2.addName(new PersonName("givenName", "middleName", "familyName"));
		int actualValue = PersonByNameComparator.comparePersonsByName(person1, person2);
		assertTrue(actualValue > 0, "Expected a positive value but it was: " + actualValue);
	}
	
	/**
	 * @see PersonByNameComparator#comparePersonsByName(Person,Person)
	 */
	@Test
	public void comparePersonsByName_shouldReturnZeroIfTheGivenNameMiddleNameAndFamilyNameMatch() {
		Person person1 = new Person();
		person1.addName(new PersonName("givenName", "middleName", "familyName"));
		Person person2 = new Person();
		person2.addName(new PersonName("givenName", "middleName", "familyName"));
		int actualValue = PersonByNameComparator.comparePersonsByName(person1, person2);
		assertEquals(0, actualValue);
	}
	
	@Test
	public void comparePersonsByName_shouldNotBeCaseSensitive() {
		Person person1 = new Person();
		person1.addName(new PersonName("GIVENNAME", "MIDDLENAME", "FAMILYNAME"));
		Person person2 = new Person();
		person2.addName(new PersonName("givenName", "middleName", "familyName"));
		int actualValue = PersonByNameComparator.comparePersonsByName(person1, person2);
		assertEquals(0, actualValue);
	}
}
