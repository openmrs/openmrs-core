/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.web.v1_0.controller.openmrs2_0;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.hibernate.internal.CacheImpl;
import org.junit.Test;
import org.openmrs.Location;
import org.openmrs.Person;
import org.openmrs.PersonName;
import org.openmrs.api.LocationService;
import org.openmrs.api.PersonService;
import org.openmrs.module.webservices.rest.web.v1_0.controller.RestControllerTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;

public class ClearDbCacheController2_0Test extends RestControllerTestUtils {
	
	private static final String CLEAR_DB_CACHE_URI = "cleardbcache";
	
	@Autowired
	private PersonService personService;
	
	@Autowired
	private LocationService locationService;
	
	@Autowired
	private SessionFactory sessionFactory;
	
	private static final Class PERSON_NAME_CLASS = PersonName.class;
	
	private static final Integer ID_2 = 2;
	
	private static final Integer ID_8 = 8;
	
	private static final String QUERY_REGION = "test";
	
	@Test
	public void clearDbCache_shouldEvictTheEntityFromTheCaches() throws Exception {
		PersonName name = personService.getPersonName(ID_2);
		//Load the person so that the names are also stored  in person names collection region
		personService.getPerson(name.getPerson().getPersonId());
		//Let's have the name in a query cache
		Query query = sessionFactory.getCurrentSession().createQuery("FROM PersonName WHERE personNameId = ?");
		query.setInteger(0, 9351);
		query.setCacheable(true);
		query.setCacheRegion(QUERY_REGION);
		query.list();
		
		assertTrue(sessionFactory.getCache().containsEntity(PERSON_NAME_CLASS, ID_2));
		assertNotNull(sessionFactory.getStatistics().getSecondLevelCacheStatistics(Person.class.getName() + ".names")
		        .getEntries().get(name.getPerson().getPersonId()));
		CacheImpl cache = (CacheImpl) sessionFactory.getCache();
		assertEquals(1, cache.getQueryCache(QUERY_REGION).getRegion().getElementCountInMemory());
		
		final String data = "{\"resource\": \"person\", \"subResource\": \"name\", \"uuid\": \"" + name.getUuid() + "\"}";
		
		MockHttpServletResponse response = handle(newPostRequest(CLEAR_DB_CACHE_URI, data));
		
		assertEquals(HttpStatus.NO_CONTENT.value(), response.getStatus());
		assertFalse(sessionFactory.getCache().containsEntity(PERSON_NAME_CLASS, ID_2));
		//All persistent collections containing the name should have been discarded
		assertNull(sessionFactory.getStatistics().getSecondLevelCacheStatistics(Person.class.getName() + ".names")
		        .getEntries().get(name.getPerson().getPersonId()));
		//All query result caches should have been discarded
		assertEquals(0, cache.getQueryCache(QUERY_REGION).getRegion().getElementCountInMemory());
	}
	
	@Test
	public void clearDbCache_shouldEvictAllEntitiesOfTheSpecifiedTypeFromTheCaches() throws Exception {
		PersonName name1 = personService.getPersonName(ID_2);
		PersonName name2 = personService.getPersonName(ID_8);
		//Load the persons so that the names are also stored in person names collection region
		personService.getPerson(name1.getPerson().getPersonId()).getNames();
		personService.getPerson(name2.getPerson().getPersonId()).getNames();
		Query query = sessionFactory.getCurrentSession().createQuery("FROM PersonName WHERE personNameId IN (?, ?)");
		query.setInteger(0, name1.getPersonNameId());
		query.setInteger(1, name2.getPersonNameId());
		query.setCacheable(true);
		query.setCacheRegion(QUERY_REGION);
		query.list();
		
		assertTrue(sessionFactory.getCache().containsEntity(PERSON_NAME_CLASS, ID_2));
		assertTrue(sessionFactory.getCache().containsEntity(PERSON_NAME_CLASS, ID_8));
		assertNotNull(sessionFactory.getStatistics().getSecondLevelCacheStatistics(Person.class.getName() + ".names")
		        .getEntries().get(name1.getPerson().getPersonId()));
		assertNotNull(sessionFactory.getStatistics().getSecondLevelCacheStatistics(Person.class.getName() + ".names")
		        .getEntries().get(name2.getPerson().getPersonId()));
		CacheImpl cache = (CacheImpl) sessionFactory.getCache();
		assertEquals(1, cache.getQueryCache(QUERY_REGION).getRegion().getElementCountInMemory());
		
		final String data = "{\"resource\": \"person\", \"subResource\": \"name\"}";
		
		MockHttpServletResponse response = handle(newPostRequest(CLEAR_DB_CACHE_URI, data));
		
		assertEquals(HttpStatus.NO_CONTENT.value(), response.getStatus());
		assertFalse(sessionFactory.getCache().containsEntity(PERSON_NAME_CLASS, ID_2));
		assertFalse(sessionFactory.getCache().containsEntity(PERSON_NAME_CLASS, ID_8));
		//All persistent collections containing the names should have been discarded
		assertNull(sessionFactory.getStatistics().getSecondLevelCacheStatistics(Person.class.getName() + ".names")
		        .getEntries().get(name1.getPerson().getPersonId()));
		assertNull(sessionFactory.getStatistics().getSecondLevelCacheStatistics(Person.class.getName() + ".names")
		        .getEntries().get(name2.getPerson().getPersonId()));
		//All query result caches should have been discarded
		assertEquals(0, cache.getQueryCache(QUERY_REGION).getRegion().getElementCountInMemory());
	}
	
	@Test
	public void clearDbCache_shouldEvictAllEntitiesFromTheCaches() throws Exception {
		PersonName name1 = personService.getPersonName(ID_2);
		PersonName name2 = personService.getPersonName(ID_8);
		//Load the location an persons so that the names also stored in person names collection region
		personService.getPerson(name1.getPerson().getPersonId()).getNames();
		personService.getPerson(name2.getPerson().getPersonId()).getNames();
		locationService.getLocation(ID_2);
		Query query = sessionFactory.getCurrentSession().createQuery("FROM PersonName WHERE personNameId IN (?, ?)");
		query.setInteger(0, name1.getPersonNameId());
		query.setInteger(1, name2.getPersonNameId());
		query.setCacheable(true);
		query.setCacheRegion(QUERY_REGION);
		query.list();
		
		assertTrue(sessionFactory.getCache().containsEntity(PERSON_NAME_CLASS, ID_2));
		assertTrue(sessionFactory.getCache().containsEntity(PERSON_NAME_CLASS, ID_8));
		assertTrue(sessionFactory.getCache().containsEntity(Location.class, ID_2));
		assertNotNull(sessionFactory.getStatistics().getSecondLevelCacheStatistics(Person.class.getName() + ".names")
		        .getEntries().get(name1.getPerson().getPersonId()));
		assertNotNull(sessionFactory.getStatistics().getSecondLevelCacheStatistics(Person.class.getName() + ".names")
		        .getEntries().get(name2.getPerson().getPersonId()));
		CacheImpl cache = (CacheImpl) sessionFactory.getCache();
		assertEquals(1, cache.getQueryCache(QUERY_REGION).getRegion().getElementCountInMemory());
		
		MockHttpServletResponse response = handle(newPostRequest(CLEAR_DB_CACHE_URI, "{}"));
		
		assertEquals(HttpStatus.NO_CONTENT.value(), response.getStatus());
		assertFalse(sessionFactory.getCache().containsEntity(PERSON_NAME_CLASS, ID_2));
		assertFalse(sessionFactory.getCache().containsEntity(PERSON_NAME_CLASS, ID_8));
		assertFalse(sessionFactory.getCache().containsEntity(Location.class, ID_2));
		//All persistent collections containing the names should have been discarded
		assertNull(sessionFactory.getStatistics().getSecondLevelCacheStatistics(Person.class.getName() + ".names")
		        .getEntries().get(name1.getPerson().getPersonId()));
		assertNull(sessionFactory.getStatistics().getSecondLevelCacheStatistics(Person.class.getName() + ".names")
		        .getEntries().get(name2.getPerson().getPersonId()));
		//All query result caches should have been discarded
		assertEquals(0, cache.getQueryCache(QUERY_REGION).getRegion().getElementCountInMemory());
	}
	
	@Test
	public void clearDbCache_shouldNotFailIfNoEntityIsFoundMatchingTheSpecifiedUuid() throws Exception {
		final String uuid = "some-uuid";
		assertNull(personService.getPersonNameByUuid(uuid));
		final String data = "{\"resource\": \"person\", \"subResource\": \"name\", \"uuid\": \"" + uuid + "\"}";
		
		MockHttpServletResponse response = handle(newPostRequest(CLEAR_DB_CACHE_URI, data));
		
		assertEquals(HttpStatus.NO_CONTENT.value(), response.getStatus());
	}
	
}
