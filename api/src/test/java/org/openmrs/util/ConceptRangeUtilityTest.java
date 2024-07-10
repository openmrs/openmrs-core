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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.Person;

import java.util.Calendar;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;


class ConceptRangeUtilityTest {
	Calendar calendar;
	ConceptRangeUtility conceptRangeUtility;
	Person person;
		
	@BeforeEach
	public void setUp() {
		calendar = Calendar.getInstance();
		conceptRangeUtility = new ConceptRangeUtility();
		person = new Person();
	}

	@Test
	public void testAgeInRange_shouldReturnTrueIfAgeIsWithinRange() {
		calendar.add(Calendar.YEAR, -5);
		person.setBirthdate(calendar.getTime());
		String criteria = "${fn.getAge(1-10)}";

		assertTrue(conceptRangeUtility.isAgeInRange(criteria, person));
	}

	@Test
	public void testAgeInRange_shouldReturnFalseIfAgeIsOutsideRange() {
		calendar.add(Calendar.YEAR, -11);
		person.setBirthdate(calendar.getTime());
		String criteria = "${fn.getAge(1-10)}";

		assertFalse(conceptRangeUtility.isAgeInRange(criteria, person));
	}

	@Test
	public void testAgeInRange_shouldReturnTrueIfAgeIsOnBoundary() {
		calendar.add(Calendar.YEAR, -10);
		person.setBirthdate(calendar.getTime());
		String criteria = "${fn.getAge(1-10)}";

		assertTrue(conceptRangeUtility.isAgeInRange(criteria, person));
	}

	@Test
	public void testAgeInRange_shouldReturnFalseIfAgeIsNoMatch() {
		calendar.add(Calendar.YEAR, -2);
		person.setBirthdate(calendar.getTime());
		String criteria = "${fn.getAge(15-50)}";

		assertFalse(conceptRangeUtility.isAgeInRange(criteria, person));
	}

	@Test
	public void testAgeInRange_shouldReturnFalseIfCriteriaIsInvalid() {
		calendar.add(Calendar.YEAR, -1);
		person.setBirthdate(calendar.getTime());
		String criteria = "invalidCriteria";

		assertFalse(conceptRangeUtility.isAgeInRange(criteria, person));
	}

	@Test
	public void testAgeInRange_shouldReturnFalseIfCriteriaIsEmpty() {
		calendar.set(2019, Calendar.JUNE, 2);
		person.setBirthdate(calendar.getTime());
		String criteria = "";

		assertFalse(conceptRangeUtility.isAgeInRange(criteria, person));
	}
}
