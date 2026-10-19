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
import org.openmrs.Location;
import org.openmrs.api.cache.GlobalPropertyCache;
import org.springframework.beans.factory.ObjectProvider;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

public class GlobalPropertyCacheInterceptorTest {

	private GlobalPropertyCache cache;

	private ObjectProvider<GlobalPropertyCache> provider;

	private GlobalPropertyCacheInterceptor interceptor;

	@BeforeEach
	@SuppressWarnings("unchecked")
	public void setUp() {
		cache = mock(GlobalPropertyCache.class);
		provider = mock(ObjectProvider.class);
		when(provider.getIfAvailable()).thenReturn(cache);
		interceptor = new GlobalPropertyCacheInterceptor(provider);
	}

	@Test
	public void onFlushDirty_shouldEvictTheGlobalProperty() {
		assertFalse(interceptor.onFlushDirty(new GlobalProperty("some.property", "new"), "some.property", new Object[0],
		    new Object[0], new String[0], null));

		verify(cache).evict("some.property");
	}

	@Test
	public void onSave_shouldClearTheCache() {
		assertFalse(interceptor.onSave(new GlobalProperty("some.property", "value"), "some.property", new Object[0],
		    new String[0], null));

		verify(cache).clear();
	}

	@Test
	public void onDelete_shouldEvictTheGlobalProperty() {
		interceptor.onDelete(new GlobalProperty("some.property", "value"), "some.property", new Object[0], new String[0],
		    null);

		verify(cache).evict("some.property");
	}

	@Test
	public void onFlushDirty_shouldIgnoreOtherEntities() {
		interceptor.onFlushDirty(new Location(), 1, new Object[0], new Object[0], new String[0], null);

		verifyNoInteractions(cache);
	}

	@Test
	public void onFlushDirty_shouldDoNothingIfTheCacheIsUnavailable() {
		when(provider.getIfAvailable()).thenReturn(null);

		assertDoesNotThrow(() -> interceptor.onFlushDirty(new GlobalProperty("some.property", "new"), "some.property",
		    new Object[0], new Object[0], new String[0], null));
	}
}
