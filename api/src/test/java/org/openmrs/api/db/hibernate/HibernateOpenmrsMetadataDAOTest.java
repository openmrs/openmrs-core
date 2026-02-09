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
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.hibernate.SessionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.Location;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;

/**
 * Tests for {@link HibernateOpenmrsMetadataDAO}
 */
public class HibernateOpenmrsMetadataDAOTest extends BaseContextSensitiveTest {

	private HibernateOpenmrsMetadataDAO<Location> dao;

	@BeforeEach
	public void setUp() {
		dao = new HibernateOpenmrsMetadataDAO<>(Location.class);
		dao.setSessionFactory((SessionFactory) applicationContext.getBean("sessionFactory"));
	}

	@Test
	public void getAllCount_shouldReturnCountOfAllRecordsIncludingRetired() {
		int count = dao.getAllCount(true);
		assertEquals(dao.getAll(true).size(), count);
	}

	@Test
	public void getAllCount_shouldReturnCountExcludingRetiredWhenIncludeRetiredIsFalse() {
		int count = dao.getAllCount(false);
		assertEquals(dao.getAll(false).size(), count);
	}

	@Test
	public void getAllCount_shouldReturnMoreWhenIncludingRetired() {
		int countWithRetired = dao.getAllCount(true);
		int countWithoutRetired = dao.getAllCount(false);
		assertTrue(countWithRetired > countWithoutRetired,
			"Count including retired should be greater than count excluding retired");
	}
}
