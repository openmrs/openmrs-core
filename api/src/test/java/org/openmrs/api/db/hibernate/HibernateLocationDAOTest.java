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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.LocationTag;
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
	
}
