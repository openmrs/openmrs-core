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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.hibernate.SessionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.Location;
import org.openmrs.LocationTag;
import org.openmrs.api.context.Context;
import org.openmrs.parameter.LocationSearchCriteria;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;

public class HibernateLocationDAOTest extends BaseContextSensitiveTest {
	
	@Autowired
	private HibernateLocationDAO dao;
	
	private static final String LOC_INITIAL_DATA_XML = "org/openmrs/api/include/LocationServiceTest-initialData.xml";
	
	@BeforeEach
	public void runBeforeEachTest() {
		executeDataSet(LOC_INITIAL_DATA_XML);
	}
	
	@Test
	public void getLocationsHavingAllTags_shouldGetLocationsHavingAllTags() {
		List<LocationTag> list1 = new ArrayList<>();
		list1.add(dao.getLocationTag(1));
		list1.add(dao.getLocationTag(2));
		
		List<LocationTag> list2 = new ArrayList<>();
		list2.add(dao.getLocationTag(3));
		list2.add(dao.getLocationTag(4));
		
		List<LocationTag> list3 = new ArrayList<>();
		list3.add(dao.getLocationTag(1));
		list3.add(dao.getLocationTag(2));
		list3.add(dao.getLocationTag(3));
		list3.add(dao.getLocationTag(4));
		
		List<LocationTag> list4 = new ArrayList<>();
		list4.add(dao.getLocationTag(4));
		
		assertEquals(1, dao.getLocationsHavingAllTags(list1).size());
		assertEquals(2, dao.getLocationsHavingAllTags(list2).size());
		assertEquals(0, dao.getLocationsHavingAllTags(list3).size());
		assertEquals(4, dao.getLocationsHavingAllTags(list4).size());
	}
	
	@Test
	public void getLocationsHavingAllTags_shouldReturnEmptyListWhenNoLocationHasTheGivenTags() {
		assertEquals(0, dao.getLocationsHavingAllTags(
		    Collections.singletonList(dao.getLocationTagByName("Nobody got this tag"))).size());
	}
	
	@Test
	public void getLocationsHavingAllTags_shouldIgnoreNullValuesInLocationTagList() {
		List<LocationTag> list1 = new ArrayList<>();
		list1.add(dao.getLocationTag(1));
		list1.add(dao.getLocationTag(2));
		list1.add(null);
		
		assertEquals(1, dao.getLocationsHavingAllTags(list1).size());
	}

	@Test
	public void getLocations_shouldNotLoadAllLocationsWhenFilteringBySelectiveNameFragment() {
		for (int i = 0; i < 40; i++) {
			Location noise = new Location();
			noise.setName("Noise Location " + i);
			dao.saveLocation(noise);
		}

		Location match1 = new Location();
		match1.setName("Target Prefix One");
		dao.saveLocation(match1);

		Location match2 = new Location();
		match2.setName("Target Prefix Two");
		dao.saveLocation(match2);

		Context.flushSession();
		Context.clearSession();

		SessionFactory sessionFactory = (SessionFactory) applicationContext.getBean("sessionFactory");
		sessionFactory.getStatistics().clear();

		LocationSearchCriteria criteria = new LocationSearchCriteria();
		criteria.setIncludeRetired(true);
		criteria.setNameFragment("Target Prefix");

		List<Location> result = dao.getLocations(criteria);

		assertEquals(2, result.size());
		assertTrue(sessionFactory.getStatistics().getEntityLoadCount() <= 10,
		    "Expected selective filtering to avoid loading the full location table");
	}

}
