/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.web.test;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.openmrs.module.BaseModuleActivatorTest;
import org.openmrs.module.ModuleUtil;
import org.springframework.context.support.AbstractRefreshableApplicationContext;
import org.springframework.test.annotation.NotTransactional;
import org.springframework.test.context.ContextConfiguration;

/**
 * ModuleActivator tests that need refreshing the spring application context. The only reason why i
 * did not put these in the api projects's ModuleActivatorTest is because when the spring
 * application context is refreshed, classes that the module references which are not in the api but
 * web, will lead to ClassNotFoundException s, hence preventing the refresh. If you want to try this
 * out, just put these tests in ModuleActivatorTest
 */
@ContextConfiguration(locations = { "classpath*:webModuleApplicationContext.xml" }, inheritLocations = true, loader = TestContextLoader.class)
public class WebModuleActivatorTest extends BaseModuleActivatorTest {
	
	@Test
	@NotTransactional
	public void shouldCallWillRefreshContextAndContextRefreshedOnRefresh() throws Exception {
		
		ModuleUtil.refreshApplicationContext((AbstractRefreshableApplicationContext) applicationContext, false, null);
		
		assertTrue(moduleTestData.getWillRefreshContextCallCount(MODULE1_ID) == 1);
		assertTrue(moduleTestData.getWillRefreshContextCallCount(MODULE2_ID) == 1);
		assertTrue(moduleTestData.getWillRefreshContextCallCount(MODULE3_ID) == 1);
		
		assertTrue(moduleTestData.getContextRefreshedCallCount(MODULE1_ID) == 1);
		assertTrue(moduleTestData.getContextRefreshedCallCount(MODULE2_ID) == 1);
		assertTrue(moduleTestData.getContextRefreshedCallCount(MODULE3_ID) == 1);
	}
}
