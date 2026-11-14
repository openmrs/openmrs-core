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

public class HibernateContextDAOBackoffTest {

	@Test
	public void calculateFailedLoginBackoffMillis_shouldFollowTheExponentialSequence() {
		assertEquals(0L, HibernateContextDAO.calculateFailedLoginBackoffMillis(1, 8));
		assertEquals(1000L, HibernateContextDAO.calculateFailedLoginBackoffMillis(2, 8));
		assertEquals(2000L, HibernateContextDAO.calculateFailedLoginBackoffMillis(3, 8));
		assertEquals(4000L, HibernateContextDAO.calculateFailedLoginBackoffMillis(4, 8));
		assertEquals(8000L, HibernateContextDAO.calculateFailedLoginBackoffMillis(5, 8));
	}

	@Test
	public void calculateFailedLoginBackoffMillis_shouldNotDelayTheFirstAttempt() {
		assertEquals(0L, HibernateContextDAO.calculateFailedLoginBackoffMillis(0, 8));
		assertEquals(0L, HibernateContextDAO.calculateFailedLoginBackoffMillis(1, 8));
	}

	@Test
	public void calculateFailedLoginBackoffMillis_shouldCapTheDelayAtTheConfiguredMaximum() {
		assertEquals(8000L, HibernateContextDAO.calculateFailedLoginBackoffMillis(6, 8));
		assertEquals(8000L, HibernateContextDAO.calculateFailedLoginBackoffMillis(20, 8));
		assertEquals(8000L, HibernateContextDAO.calculateFailedLoginBackoffMillis(Integer.MAX_VALUE, 8));
	}

	@Test
	public void calculateFailedLoginBackoffMillis_shouldRespectAMaximumLowerThanTheNextStep() {
		assertEquals(3000L, HibernateContextDAO.calculateFailedLoginBackoffMillis(4, 3));
	}

	@Test
	public void calculateFailedLoginBackoffMillis_shouldReturnZeroWhenTheDelayIsDisabled() {
		assertEquals(0L, HibernateContextDAO.calculateFailedLoginBackoffMillis(5, 0));
		assertEquals(0L, HibernateContextDAO.calculateFailedLoginBackoffMillis(5, -1));
	}

	@Test
	public void calculateFailedLoginBackoffMillis_shouldNotOverflowForVeryLargeAttemptCounts() {
		assertEquals(TimeUnit.SECONDS.toMillis(1L << 30),
		    HibernateContextDAO.calculateFailedLoginBackoffMillis(Integer.MAX_VALUE, Long.MAX_VALUE));
	}
}
