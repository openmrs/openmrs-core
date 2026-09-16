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

import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.openmrs.PrivilegeListener;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.TestUsernameAuthenticationScheme;
import org.openmrs.api.context.UserContext;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;
import org.springframework.context.event.ContextRefreshedEvent;

import static org.mockito.Mockito.times;

public class RegisteredListenerCacheTest extends BaseContextSensitiveTest {

	/**
	 * @see RegisteredListenerCache#onApplicationEvent(ContextRefreshedEvent)
	 */
	@Test
	public void onApplicationEvent_shouldClearCachedListeners() {
		try (MockedStatic<Context> contextMock = Mockito.mockStatic(Context.class)) {
			UserContext.clearCachedListeners();
			contextMock.when(() -> Context.getRegisteredComponents(PrivilegeListener.class))
			        .thenReturn(Collections.emptyList());

			UserContext userContext = new UserContext(new TestUsernameAuthenticationScheme());
			userContext.addProxyPrivilege("Some Privilege");

			// The first check populates the listener cache.
			userContext.hasPrivilege("Some Privilege");

			RegisteredListenerCache cache = applicationContext.getBean(RegisteredListenerCache.class);

			// A context refresh should invalidate the cached listener registrations.
			cache.onApplicationEvent(new ContextRefreshedEvent(applicationContext));

			// The next check should perform a fresh listener lookup.
			userContext.hasPrivilege("Some Privilege");

			contextMock.verify(() -> Context.getRegisteredComponents(PrivilegeListener.class), times(2));
		} finally {
			UserContext.clearCachedListeners();
		}
	}
}
