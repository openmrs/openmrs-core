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

import org.infinispan.manager.DefaultCacheManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Verifies that a purge drops a stored session without touching its value, so an entry whose
 * attribute map fails to deserialize (an allow-listed-out class) is recoverable by removal.
 */
public class DistributedSessionPurgerTest {

	private DefaultCacheManager cacheManager;

	private DistributedSessionPurger purger;

	@BeforeEach
	public void setUp() throws Exception {
		cacheManager = new DefaultCacheManager("infinispan-api-local.xml");
		purger = new DistributedSessionPurger(cacheManager);
	}

	@AfterEach
	public void tearDown() {
		if (cacheManager != null) {
			cacheManager.stop();
		}
	}

	@Test
	public void purge_shouldRemoveTheStoredSession() {
		cacheManager.getCache("sessions").put("session-1", "any value");
		assertEquals("any value", cacheManager.getCache("sessions").get("session-1"));

		purger.purge("session-1");

		assertNull(cacheManager.getCache("sessions").get("session-1"), "the poisoned session must be dropped");
	}

	@Test
	public void purge_shouldTolerateAnUnknownSessionId() {
		assertDoesNotThrow(() -> purger.purge("no-such-session"));
		assertNull(cacheManager.getCache("sessions").get("no-such-session"));
	}
}
