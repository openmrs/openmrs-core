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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.Test;

public class CohortMembershipTest {

	@Test
	public void isMembershipActive_shouldReturnTrueIfAsOfDateIsAfterStartDate() throws Exception {
		CohortMembership newMember = new CohortMembership(4);
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date startDate = dateFormat.parse("2017-01-01 00:00:00");
		newMember.setStartDate(startDate);

		Date dateToTest = dateFormat.parse("2017-01-01 12:00:00");
		assertTrue(newMember.isActive(dateToTest));
		assertTrue(newMember.isActive(new Date()));
		assertTrue(newMember.isActive(startDate));
	}

	@Test
	public void isActive_shouldReturnFalseIfStartDateIsAfterAsOfDate() throws Exception {
		CohortMembership newMember = new CohortMembership(4);
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date startDate = dateFormat.parse("2017-01-01 00:00:00");
		newMember.setStartDate(startDate);

		Date dateToTest = dateFormat.parse("2016-12-01 00:00:00");
		assertFalse(newMember.isActive(dateToTest));
	}

	@Test
	public void isActive_shouldBeInactiveAsOfDateForVoidedMembership() throws Exception {
		CohortMembership newMember = new CohortMembership(4);
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date startDate = dateFormat.parse("2017-01-01 00:00:00");
		Date endDate = dateFormat.parse("2017-01-31 00:00:00");
		newMember.setStartDate(startDate);
		newMember.setEndDate(endDate);
		newMember.setVoided(true);

		Date dateToTest = dateFormat.parse("2017-02-01 00:00:00");
		assertFalse(newMember.isActive(dateToTest));
	}

	@Test
	public void compareTo_shouldCompareMembershipsForEquality() {
		CohortMembership firstMembership = new CohortMembership(4);
		CohortMembership secondMembership = new CohortMembership(4);

		Cohort cohort = new Cohort(2);
		firstMembership.setCohort(cohort);
		secondMembership.setCohort(cohort);

		Date date = new Date();
		firstMembership.setStartDate(date);
		secondMembership.setStartDate(date);

		assertEquals(firstMembership.compareTo(secondMembership), 0);
		assertEquals(secondMembership.compareTo(firstMembership), 0);
	}

	@Test
	public void compareTo_shouldFailIfPatientOrCohortIdsDoNotMatch() {
		CohortMembership firstMembership = new CohortMembership(4);
		CohortMembership secondMembership = new CohortMembership(4);

		Cohort cohort = new Cohort(1);
		Cohort anotherCohort = new Cohort(2);

		firstMembership.setCohort(cohort);
		secondMembership.setCohort(anotherCohort);

		assertEquals(firstMembership.compareTo(secondMembership), -1);

		secondMembership.setCohort(cohort);
		secondMembership.setPatientId(7);

		assertEquals(firstMembership.compareTo(secondMembership), -1);
	}

	/**
	 * @verifies fail if start date or end date do not match
	 * @see CohortMembership#compareTo(CohortMembership)
	 */
	@Test
	public void compareTo_shouldFailIfStartOrEndDateDoNotMatch() throws Exception {
		CohortMembership firstMembership = new CohortMembership(4);
		CohortMembership secondMembership = new CohortMembership(4);

		Cohort cohort = new Cohort(1);

		firstMembership.setCohort(cohort);
		secondMembership.setCohort(cohort);

		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date oneDate = dateFormat.parse("2017-01-01 00:00:00");
		Date twoDate = dateFormat.parse("2017-01-31 00:00:00");

		firstMembership.setStartDate(oneDate);
		secondMembership.setStartDate(twoDate);

		assertEquals(firstMembership.compareTo(secondMembership), -1);

		secondMembership.setStartDate(oneDate);
		secondMembership.setEndDate(twoDate);

		assertEquals(firstMembership.compareTo(secondMembership), -1);
	}

	/**
	 * @verifies fails if one of the memberships is not active
	 * @see CohortMembership#compareTo(CohortMembership)
	 */
	@Test
	public void compareTo_shouldFailIfOneOfTheMembershipIsNotActive() throws Exception {
		CohortMembership firstMembership = new CohortMembership(4);
		CohortMembership secondMembership = new CohortMembership(4);

		Cohort cohort = new Cohort(1);

		firstMembership.setCohort(cohort);
		secondMembership.setCohort(cohort);

		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date oneDate = dateFormat.parse("2017-01-01 00:00:00");
		Date twoDate = dateFormat.parse("2017-01-31 00:00:00");

		firstMembership.setStartDate(oneDate);
		secondMembership.setEndDate(twoDate);

		assertEquals(firstMembership.compareTo(secondMembership), -1);
	}
}
