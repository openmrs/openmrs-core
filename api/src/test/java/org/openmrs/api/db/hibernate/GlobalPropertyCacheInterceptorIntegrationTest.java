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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.GlobalProperty;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.cache.GlobalPropertyCacheTestUtil;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.AdministrationDAO;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;
import org.openmrs.util.OpenmrsConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.transaction.TestTransaction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Checks that global properties written without {@link AdministrationService} are evicted from the
 * global property cache when Hibernate flushes them.
 */
public class GlobalPropertyCacheInterceptorIntegrationTest extends BaseContextSensitiveTest {

	@Autowired
	private AdministrationDAO adminDAO;

	private AdministrationService adminService;

	@BeforeEach
	public void setUp() {
		adminService = Context.getAdministrationService();
	}

	@Test
	public void shouldEvictTheOrderNumberSeedWhenTheOrderDaoIncrementsIt() {
		long seed = Long.parseLong(fill(OpenmrsConstants.GP_NEXT_ORDER_NUMBER_SEED));

		// increments the seed and commits in a transaction of its own
		Context.getOrderService().getNextOrderNumberSeedSequenceValue();

		assertFalse(GlobalPropertyCacheTestUtil.isCached(OpenmrsConstants.GP_NEXT_ORDER_NUMBER_SEED));
		// this test's own session still holds the old value, but the fill reads the committed one
		adminService.getGlobalProperty(OpenmrsConstants.GP_NEXT_ORDER_NUMBER_SEED);
		GlobalPropertyCacheTestUtil.awaitFills();
		assertEquals(String.valueOf(seed + 1),
		    GlobalPropertyCacheTestUtil.getIfCached(OpenmrsConstants.GP_NEXT_ORDER_NUMBER_SEED).getValue());
	}

	@Test
	public void shouldEvictAPropertyWhoseLoadedEntityIsChangedAndFlushedWhenTheTransactionCompletes() {
		fill("concept.defaultConceptMapType");

		GlobalProperty property = adminDAO.getGlobalPropertyObject("concept.defaultConceptMapType");
		property.setPropertyValue("changed");
		Context.flushSession();

		assertNull(GlobalPropertyCacheTestUtil.getIfCached("concept.defaultConceptMapType"));
		assertEquals("changed", adminService.getGlobalProperty("concept.defaultConceptMapType"));
		TestTransaction.end();
		assertFalse(GlobalPropertyCacheTestUtil.isCached("concept.defaultConceptMapType"));
	}

	@Test
	public void shouldEvictAPropertyDeletedThroughTheDaoWhenTheTransactionCompletes() {
		fill("concept.defaultConceptMapType");

		adminDAO.deleteGlobalProperty(adminDAO.getGlobalPropertyObject("concept.defaultConceptMapType"));
		Context.flushSession();

		assertNull(GlobalPropertyCacheTestUtil.getIfCached("concept.defaultConceptMapType"));
		assertNull(adminService.getGlobalProperty("concept.defaultConceptMapType"));
		TestTransaction.end();
		assertFalse(GlobalPropertyCacheTestUtil.isCached("concept.defaultConceptMapType"));
	}

	@Test
	public void shouldClearTheCacheWhenAPropertyIsInsertedThroughTheDao() {
		fill("concept.defaultConceptMapType");

		adminDAO.saveGlobalProperty(new GlobalProperty("inserted.property", "value"));
		Context.flushSession();

		assertNull(GlobalPropertyCacheTestUtil.getIfCached("concept.defaultConceptMapType"));
		TestTransaction.end();
		assertFalse(GlobalPropertyCacheTestUtil.isCached("concept.defaultConceptMapType"));
	}

	/** Reads the property, missing the cache, and waits for the fill, checking that it cached it. */
	private String fill(String propertyName) {
		String value = adminService.getGlobalProperty(propertyName);
		GlobalPropertyCacheTestUtil.awaitFills();
		assertTrue(GlobalPropertyCacheTestUtil.isCached(propertyName));
		return value;
	}
}
