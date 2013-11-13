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
import org.openmrs.module.Module;
import org.openmrs.module.ModuleFactory;
import org.openmrs.module.ModuleUtil;
import org.openmrs.module.web.WebModuleUtil;
import org.springframework.context.support.AbstractRefreshableApplicationContext;
import org.springframework.test.annotation.NotTransactional;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.context.support.XmlWebApplicationContext;

/**
 * ModuleActivator tests that need refreshing the spring application context. The only reason why i
 * did not put these in the api projects's ModuleActivatorTest is because when the spring
 * application context is refreshed, classes that the module references which are not in the api but
 * web, will lead to ClassNotFoundException s, hence preventing the refresh. If you want to try this
 * out, just put these tests in ModuleActivatorTest NOTE: The way we start, stop, unload, etc,
 * modules is copied from ModuleListController
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
	
	@Test
	@NotTransactional
	public void shouldRefreshOtherModulesOnStoppingModule() {
		
		//When OpenMRS is running and you stop a module:
		//    willRefreshContext() and contextRefreshed() methods get called for ONLY the started modules' activators EXCLUDING the stopped module
		//    willStop() and stopped() methods get called for ONLY the stopped module's activator
		
		Module module = ModuleFactory.getModuleById(MODULE3_ID);
		ModuleFactory.stopModule(module);
		WebModuleUtil.stopModule(module, ((XmlWebApplicationContext) applicationContext).getServletContext());
		
		//module3 should have stopped
		assertTrue(moduleTestData.getWillStopCallCount(MODULE3_ID) == 1);
		assertTrue(moduleTestData.getStoppedCallCount(MODULE3_ID) == 1);
		
		//module1 and module2 should not stop
		assertTrue(moduleTestData.getWillStopCallCount(MODULE1_ID) == 0);
		assertTrue(moduleTestData.getStoppedCallCount(MODULE1_ID) == 0);
		assertTrue(moduleTestData.getWillStopCallCount(MODULE2_ID) == 0);
		assertTrue(moduleTestData.getStoppedCallCount(MODULE2_ID) == 0);
		
		//module3 should not refresh
		assertTrue(moduleTestData.getWillRefreshContextCallCount(MODULE3_ID) == 0);
		assertTrue(moduleTestData.getContextRefreshedCallCount(MODULE3_ID) == 0);
		
		//module1 and module2 should refresh
		assertTrue(moduleTestData.getWillRefreshContextCallCount(MODULE1_ID) == 1);
		assertTrue(moduleTestData.getWillRefreshContextCallCount(MODULE2_ID) == 1);
		assertTrue(moduleTestData.getContextRefreshedCallCount(MODULE1_ID) == 1);
		assertTrue(moduleTestData.getContextRefreshedCallCount(MODULE2_ID) == 1);
	}
	
	@Test
	@NotTransactional
	public void shouldRefreshOtherModulesOnStartingStoppedModule() {
		Module module = ModuleFactory.getModuleById(MODULE3_ID);
		ModuleFactory.stopModule(module);
		
		init(); //to initialize for the condition below:
		
		//When OpenMRS is running and you start a stopped module:
		//	willRefreshContext() and contextRefreshed() methods get called for all started modules' activators (including the newly started module)
		//  started() method gets called for ONLY the newly started module's activator
		
		//start module3 which was previously stopped
		ModuleFactory.startModule(module);
		WebModuleUtil.startModule(module, ((XmlWebApplicationContext) applicationContext).getServletContext(), false);
		
		assertTrue(module.isStarted());
		assertTrue(ModuleFactory.isModuleStarted(module));
		
		//module1, module2 and module3 should refresh
		assertTrue(moduleTestData.getWillRefreshContextCallCount(MODULE1_ID) == 1);
		assertTrue(moduleTestData.getWillRefreshContextCallCount(MODULE2_ID) == 1);
		assertTrue(moduleTestData.getWillRefreshContextCallCount(MODULE3_ID) == 1);
		assertTrue(moduleTestData.getContextRefreshedCallCount(MODULE1_ID) == 1);
		assertTrue(moduleTestData.getContextRefreshedCallCount(MODULE2_ID) == 1);
		assertTrue(moduleTestData.getContextRefreshedCallCount(MODULE3_ID) == 1);
		
		//started() method gets called for ONLY the newly started module's activator
		assertTrue(moduleTestData.getStartedCallCount(MODULE1_ID) == 0);
		assertTrue(moduleTestData.getStartedCallCount(MODULE2_ID) == 0);
		assertTrue(moduleTestData.getStartedCallCount(MODULE3_ID) == 1);
	}
}
