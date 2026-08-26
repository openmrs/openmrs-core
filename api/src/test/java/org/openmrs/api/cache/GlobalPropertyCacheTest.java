/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api.cache;

import org.hibernate.SessionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.GlobalProperty;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Tests the caching of {@link AdministrationService#getGlobalProperty(String)}.
 * <p>
 * "Did this reach the database" is asserted through Hibernate's statement counter rather than by
 * inspecting the cache, so that the tests describe the behaviour that matters rather than the
 * mechanism used to achieve it.
 */
public class GlobalPropertyCacheTest extends BaseContextSensitiveTest {

	private static final String EXISTING_PROPERTY = "concept.defaultConceptMapType";

	private static final String UNSET_PROPERTY = "some.property.that.is.never.set";

	@Autowired
	private AdministrationService administrationService;

	private SessionFactory sessionFactory;

	@BeforeEach
	public void getSessionFactory() {
		sessionFactory = (SessionFactory) applicationContext.getBean("sessionFactory");
	}

	private long statements() {
		return sessionFactory.getStatistics().getPrepareStatementCount();
	}

	/**
	 * Reads the property once to warm every layer, then clears the Hibernate session so that only the
	 * API cache can answer without touching the database.
	 */
	private String warmUp(String propertyName) {
		String value = administrationService.getGlobalProperty(propertyName);
		Context.flushSession();
		Context.clearSession();
		return value;
	}

	@Test
	public void getGlobalProperty_shouldNotHitTheDatabaseOnRepeatedReads() {
		String expected = warmUp(EXISTING_PROPERTY);
		assertNotNull(expected);

		long before = statements();

		assertEquals(expected, administrationService.getGlobalProperty(EXISTING_PROPERTY));
		assertEquals(expected, administrationService.getGlobalProperty(EXISTING_PROPERTY));

		assertEquals(before, statements(), "repeated reads of a cached property should not reach the database");
	}

	@Test
	public void getGlobalProperty_shouldNotHitTheDatabaseOnRepeatedReadsOfAnUnsetProperty() {
		assertNull(warmUp(UNSET_PROPERTY));

		long before = statements();

		assertNull(administrationService.getGlobalProperty(UNSET_PROPERTY));
		assertNull(administrationService.getGlobalProperty(UNSET_PROPERTY));

		assertEquals(before, statements(), "repeated reads of an unset property should not reach the database either");
	}

	@Test
	public void getGlobalProperty_shouldReturnTheDefaultForAnUnsetPropertyWithoutHittingTheDatabase() {
		assertNull(warmUp(UNSET_PROPERTY));

		long before = statements();

		assertEquals("a default", administrationService.getGlobalProperty(UNSET_PROPERTY, "a default"));

		assertEquals(before, statements(), "the defaulting overload should also be served from the cache");
	}

	@Test
	public void getGlobalProperty_shouldReturnTheNewValueAfterSaveGlobalProperty() {
		warmUp(EXISTING_PROPERTY);

		administrationService.saveGlobalProperty(new GlobalProperty(EXISTING_PROPERTY, "a new value"));

		assertEquals("a new value", administrationService.getGlobalProperty(EXISTING_PROPERTY));
	}

	@Test
	public void getGlobalProperty_shouldReturnTheNewValueAfterSetGlobalProperty() {
		warmUp(EXISTING_PROPERTY);

		administrationService.setGlobalProperty(EXISTING_PROPERTY, "a value set through setGlobalProperty");

		assertEquals("a value set through setGlobalProperty", administrationService.getGlobalProperty(EXISTING_PROPERTY));
	}

	@Test
	public void getGlobalProperty_shouldReturnTheNewValueAfterUpdateGlobalProperty() {
		warmUp(EXISTING_PROPERTY);

		administrationService.updateGlobalProperty(EXISTING_PROPERTY, "an updated value");

		assertEquals("an updated value", administrationService.getGlobalProperty(EXISTING_PROPERTY));
	}

	@Test
	public void getGlobalProperty_shouldReturnNullAfterPurgeGlobalProperty() {
		warmUp(EXISTING_PROPERTY);

		administrationService.purgeGlobalProperty(administrationService.getGlobalPropertyObject(EXISTING_PROPERTY));

		assertNull(administrationService.getGlobalProperty(EXISTING_PROPERTY));
	}

	/**
	 * The failure mode this guards against: a property is read while it does not exist, the absence is
	 * cached, the property is then created, and the stale "not set" answer sticks.
	 */
	@Test
	public void getGlobalProperty_shouldSeeAPropertyCreatedAfterItsAbsenceWasCached() {
		assertNull(warmUp(UNSET_PROPERTY));

		administrationService.saveGlobalProperty(new GlobalProperty(UNSET_PROPERTY, "now it exists"));

		assertEquals("now it exists", administrationService.getGlobalProperty(UNSET_PROPERTY));
	}

	/**
	 * Lookups are case insensitive, so a save under one casing has to invalidate every casing.
	 */
	@Test
	public void getGlobalProperty_shouldEvictEveryCasingOfAPropertyName() {
		warmUp(EXISTING_PROPERTY);
		administrationService.getGlobalProperty(EXISTING_PROPERTY.toUpperCase());

		administrationService.saveGlobalProperty(new GlobalProperty(EXISTING_PROPERTY.toLowerCase(), "changed"));

		// Flushing is about Hibernate, not about the cache under test. On a database that compares
		// strings without regard to case, such as the H2 test database created with IGNORECASE=TRUE,
		// a lookup by one casing and a lookup by another produce two different Hibernate entity
		// keys for the same row. Loading by identifier does not auto flush, so without this the
		// second lookup would read the row as it was before the save and report a stale value that
		// has nothing to do with whether the cache entry was evicted.
		Context.flushSession();

		assertEquals("changed", administrationService.getGlobalProperty(EXISTING_PROPERTY.toUpperCase()),
		    "saving one casing must not leave a stale entry under another casing");
		assertEquals("changed", administrationService.getGlobalProperty(EXISTING_PROPERTY));
	}

	@Test
	public void clearEntireCache_shouldEvictCachedGlobalProperties() {
		String expected = warmUp(EXISTING_PROPERTY);

		Context.clearEntireCache();

		long before = statements();
		assertEquals(expected, administrationService.getGlobalProperty(EXISTING_PROPERTY));

		org.junit.jupiter.api.Assertions.assertNotEquals(before, statements(),
		    "after the caches are cleared the property has to be read from the database again");
	}
}
