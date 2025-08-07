/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.config;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URL;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.openmrs.api.cache.CacheConfig;
import org.openmrs.obs.ComplexObsHandler;
import org.openmrs.obs.handler.BinaryDataHandler;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;
import org.openmrs.util.HandlerUtil;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Tests {@link org.openmrs.config.OpenmrsAppConfig}
 */
public class OpenmrsAppConfigTest extends BaseContextSensitiveTest {

	@Autowired
	private BinaryDataHandler binaryDataHandler;

	@Autowired
	private CacheConfig cacheConfig;
	
	@Autowired(required = false)
	private HandlerUtil handlerUtil;

	@Test
	public void shouldRegisterBeansViaImport() {
		// Assert BinaryDataHandler is not null and properly loaded
		assertNotNull(binaryDataHandler);
		assertTrue(binaryDataHandler.supportsView(ComplexObsHandler.RAW_VIEW));

		// Assert CacheConfig is not null and properly loaded
		assertNotNull(cacheConfig);
		List<URL> cacheConfigurations = cacheConfig.getCacheConfigurations();
		assertThat(cacheConfigurations.size(), is(2));
		
		//Assert HandlerUtil is null since it's not registered via @Import
		assertNull(handlerUtil);
	}
}
