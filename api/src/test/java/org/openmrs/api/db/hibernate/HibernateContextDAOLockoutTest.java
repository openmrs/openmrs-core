/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api.db.hibernate;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

public class HibernateContextDAOLockoutTest {

	private static final long INITIAL_DURATION_MS = TimeUnit.MINUTES.toMillis(10);

	private static final long MAXIMUM_DURATION_MS = TimeUnit.HOURS.toMillis(24);

	@Test
	public void calculateLockoutDurationMs_shouldUseInitialDurationForFirstLockout() {
		assertEquals(INITIAL_DURATION_MS,
			HibernateContextDAO.calculateLockoutDurationMs(0, INITIAL_DURATION_MS, MAXIMUM_DURATION_MS));
		assertEquals(INITIAL_DURATION_MS,
			HibernateContextDAO.calculateLockoutDurationMs(1, INITIAL_DURATION_MS, MAXIMUM_DURATION_MS));
	}

	@Test
	public void calculateLockoutDurationMs_shouldDoubleForEachConsecutiveLockout() {
		assertEquals(TimeUnit.MINUTES.toMillis(20),
			HibernateContextDAO.calculateLockoutDurationMs(2, INITIAL_DURATION_MS, MAXIMUM_DURATION_MS));
		assertEquals(TimeUnit.MINUTES.toMillis(40),
			HibernateContextDAO.calculateLockoutDurationMs(3, INITIAL_DURATION_MS, MAXIMUM_DURATION_MS));
		assertEquals(TimeUnit.MINUTES.toMillis(80),
			HibernateContextDAO.calculateLockoutDurationMs(4, INITIAL_DURATION_MS, MAXIMUM_DURATION_MS));
	}

	@Test
	public void calculateLockoutDurationMs_shouldEnforceMaximumDuration() {
		long maximumDurationMs = TimeUnit.MINUTES.toMillis(60);
		assertEquals(TimeUnit.MINUTES.toMillis(40),
			HibernateContextDAO.calculateLockoutDurationMs(3, INITIAL_DURATION_MS, maximumDurationMs));
		assertEquals(maximumDurationMs,
			HibernateContextDAO.calculateLockoutDurationMs(4, INITIAL_DURATION_MS, maximumDurationMs));
	}

	@Test
	public void calculateLockoutDurationMs_shouldNotOverflowForLargeLockoutCounts() {
		assertEquals(MAXIMUM_DURATION_MS,
			HibernateContextDAO.calculateLockoutDurationMs(Long.MAX_VALUE, INITIAL_DURATION_MS, MAXIMUM_DURATION_MS));
	}

	@Test
	public void calculateLockoutDurationMs_shouldNotExceedMaximumWhenInitialDurationIsLarger() {
		long maximumDurationMs = TimeUnit.MINUTES.toMillis(5);
		assertEquals(maximumDurationMs,
			HibernateContextDAO.calculateLockoutDurationMs(1, INITIAL_DURATION_MS, maximumDurationMs));
	}
}
