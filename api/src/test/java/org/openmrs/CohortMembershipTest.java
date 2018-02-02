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

import static org.hamcrest.collection.IsIterableContainingInOrder.contains;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.number.OrderingComparison.greaterThan;
import static org.hamcrest.number.OrderingComparison.lessThan;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.time.DateUtils;
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
	public void isActive_shouldBeInclusiveOfStartAndEndDates() throws Exception {
		CohortMembership membership = new CohortMembership(4, DateUtils.parseDate("2014-01-02", "yyyy-MM-dd"));
		membership.setEndDate(new Date());
		assertTrue(membership.isActive(membership.getStartDate()));
		assertTrue(membership.isActive(membership.getEndDate()));
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
		
		firstMembership.setUuid("same-uuid");
		secondMembership.setUuid("same-uuid");
		
		assertEquals(0, firstMembership.compareTo(secondMembership));
		assertEquals(0, secondMembership.compareTo(firstMembership));
	}
	
	@Test
	public void compareTo_shouldCompareBasedOnPatientId() {
		Date date = new Date();
		CohortMembership firstMembership = new CohortMembership(4, date);
		CohortMembership secondMembership = new CohortMembership(7, date);
		
		assertThat(firstMembership.compareTo(secondMembership), lessThan(0));
		assertThat(secondMembership.compareTo(firstMembership), greaterThan(0));
	}
	
	/**
	 * @see CohortMembership#compareTo(CohortMembership)
	 */
	@Test
	public void compareTo_shouldSortByStartDate() throws Exception {
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
		
		assertThat(firstMembership.compareTo(secondMembership), greaterThan(0));
		assertThat(secondMembership.compareTo(firstMembership), lessThan(0));
	}
	
	/**
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
		secondMembership.setStartDate(oneDate);
		secondMembership.setEndDate(twoDate);
		
		assertThat(firstMembership.compareTo(secondMembership), not(0));
		assertThat(secondMembership.compareTo(firstMembership), not(0));
	}
	
	/**
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
		
		assertThat(firstMembership.compareTo(secondMembership), lessThan(0));
	}
	
	@Test
	public void shouldReturnNegativeValueIfTheMembershipIsNotVoidedAndTheOtherIsAndTheyAreBothEnded() {
		Cohort cohort = new Cohort();
		CohortMembership cm1 = new CohortMembership(1);
		cm1.setCohort(cohort);
		cm1.setEndDate(new Date());
		CohortMembership cm2 = new CohortMembership(1);
		cm2.setCohort(cohort);
		cm2.setEndDate(new Date());
		cm2.setVoided(true);
		assertTrue(cm1.compareTo(cm2) < 0);
	}
	
	@Test
	public void shouldReturnPositiveValueIfTheMembershipIsVoidedAndTheOtherIsNotAndTheyAreBothEnded() {
		Cohort cohort = new Cohort();
		CohortMembership cm1 = new CohortMembership(1);
		cm1.setCohort(cohort);
		cm1.setEndDate(new Date());
		cm1.setVoided(true);
		CohortMembership cm2 = new CohortMembership(1);
		cm2.setCohort(cohort);
		cm2.setEndDate(new Date());
		assertTrue(cm1.compareTo(cm2) > 0);
	}
	
	@Test
	public void compareTo_shouldSort() throws Exception {
		SimpleDateFormat ymd = new SimpleDateFormat("yyyy-MM-dd");
		
		Cohort c1 = new Cohort();
		c1.setCohortId(1);
		Cohort c2 = new Cohort();
		c2.setCohortId(2);
		
		// ensure that being in another cohort is ignored for sorting
		// "no start or end" means it's treated as having started forever ago
		CohortMembership noStartOrEnd = new CohortMembership(1);
		noStartOrEnd.setStartDate(null);
		noStartOrEnd.setCohort(c2);
		noStartOrEnd.setUuid("no start or end date");
		
		// this is voided so it should sort to the end of the list
		CohortMembership voided = new CohortMembership(1);
		voided.setCohort(c1);
		voided.setVoided(true);
		voided.setUuid("voided");
		
		// ended goes towards the end, and longer ago goes further at the end
		CohortMembership endedLongAgo = new CohortMembership(1);
		endedLongAgo.setCohort(c1);
		endedLongAgo.setEndDate(ymd.parse("2015-01-01"));
		endedLongAgo.setUuid("ended 2015");
		CohortMembership endedRecently = new CohortMembership(2);
		endedRecently.setCohort(c1);
		endedRecently.setStartDate(ymd.parse("2016-01-01"));
		endedRecently.setEndDate(ymd.parse("2017-02-01"));
		endedRecently.setUuid("ended 2017");
		
		// active goes towards the front, started more recently goes further to the front
		CohortMembership startedLongAgo = new CohortMembership(3);
		startedLongAgo.setCohort(c1);
		startedLongAgo.setStartDate(ymd.parse("2015-01-01"));
		startedLongAgo.setUuid("started 2015");
		CohortMembership startedRecently = new CohortMembership(3);
		startedRecently.setCohort(c1);
		startedRecently.setStartDate(ymd.parse("2016-01-01"));
		startedRecently.setUuid("started 2016");
		
		List<CohortMembership> list = Arrays.asList(noStartOrEnd, voided, endedLongAgo, endedRecently, startedLongAgo,
				startedRecently);
		Collections.sort(list);
		
		assertThat(list, contains(startedRecently, startedLongAgo, noStartOrEnd, endedRecently, endedLongAgo, voided));
	}
}
