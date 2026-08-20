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

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.PrivilegeListener;
import org.openmrs.api.context.ServiceContext;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;
import org.openmrs.util.DatabaseUpdateException;
import org.openmrs.util.InputRequiredException;
import org.springframework.context.event.ContextRefreshedEvent;

import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;

public class RegisteredListenerCacheTest extends BaseContextSensitiveTest {

	private ServiceContext serviceContext;

	@BeforeEach
	public void setUp() throws InputRequiredException, DatabaseUpdateException {
		serviceContext = ServiceContext.getInstance();
		serviceContext.clearCachedListeners();
	}

	/**
	 * @see RegisteredListenerCache#onApplicationEvent(ContextRefreshedEvent)
	 */
	@Test
	public void onApplicationEvent_shouldClearCachedListeners() {
		serviceContext.clearCachedListeners();

		List<PrivilegeListener> first = serviceContext.getRegisteredComponents(PrivilegeListener.class);
		List<PrivilegeListener> second = serviceContext.getRegisteredComponents(PrivilegeListener.class);

		assertSame(first, second);

		RegisteredListenerCache cache = applicationContext.getBean(RegisteredListenerCache.class);

		// A context refresh should invalidate listener registrations cached from the previous context.
		cache.onApplicationEvent(new ContextRefreshedEvent(applicationContext));
		List<PrivilegeListener> third = serviceContext.getRegisteredComponents(PrivilegeListener.class);

		assertNotSame(first, third);
	}
}
