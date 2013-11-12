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
package org.openmrs.module;

import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.test.BaseContextSensitiveTest;

public class ModuleActivatorTest extends BaseContextSensitiveTest {
	
	private static final String MODULE1_ID = "test1";
	
	private static final String MODULE2_ID = "test2";
	
	private static final String MODULE3_ID = "test3";
	
	private ModuleTestData moduleTestData;
	
	@Before
	public void beforeEachTest() throws Exception {
		moduleTestData = ModuleTestData.getInstance();
		
		ModuleUtil.shutdown();
		
		init();
		
		String modulesToLoad = "org/openmrs/module/include/test3-1.0-SNAPSHOT.omod org/openmrs/module/include/test1-1.0-SNAPSHOT.omod org/openmrs/module/include/test2-1.0-SNAPSHOT.omod";
		runtimeProperties.setProperty(ModuleConstants.RUNTIMEPROPERTY_MODULE_LIST_TO_LOAD, modulesToLoad);
		ModuleUtil.startup(runtimeProperties);
	}
	
	@Test
	public void shouldCallWillStartOnStartup() throws Exception {
		assertTrue(moduleTestData.getWillStartCallCount(MODULE1_ID) == 1);
		assertTrue(moduleTestData.getWillStartCallCount(MODULE2_ID) == 1);
		assertTrue(moduleTestData.getWillStartCallCount(MODULE3_ID) == 1);
	}
	
	@Test
	public void shouldNotCallStartedOnStartup() throws Exception {
		assertTrue(moduleTestData.getStartedCallCount(MODULE1_ID) == 0);
		assertTrue(moduleTestData.getStartedCallCount(MODULE2_ID) == 0);
		assertTrue(moduleTestData.getStartedCallCount(MODULE3_ID) == 0);
	}
	
	@Test
	public void shouldNotCallWillStopOnStartup() throws Exception {
		assertTrue(moduleTestData.getWillStopCallCount(MODULE1_ID) == 0);
		assertTrue(moduleTestData.getWillStopCallCount(MODULE2_ID) == 0);
		assertTrue(moduleTestData.getWillStopCallCount(MODULE3_ID) == 0);
	}
	
	@Test
	public void shouldNotCallStoppedOnStartup() throws Exception {
		assertTrue(moduleTestData.getStoppedCallCount(MODULE1_ID) == 0);
		assertTrue(moduleTestData.getStoppedCallCount(MODULE2_ID) == 0);
		assertTrue(moduleTestData.getStoppedCallCount(MODULE3_ID) == 0);
	}
	
	@Test
	public void shouldNotCallWillRefreshContextOnStartup() throws Exception {
		assertTrue(moduleTestData.getWillRefreshContextCallCount(MODULE1_ID) == 0);
		assertTrue(moduleTestData.getWillRefreshContextCallCount(MODULE2_ID) == 0);
		assertTrue(moduleTestData.getWillRefreshContextCallCount(MODULE3_ID) == 0);
	}
	
	@Test
	public void shouldNotCallContextRefreshedOnStartup() throws Exception {
		assertTrue(moduleTestData.getContextRefreshedCallCount(MODULE1_ID) == 0);
		assertTrue(moduleTestData.getContextRefreshedCallCount(MODULE2_ID) == 0);
		assertTrue(moduleTestData.getContextRefreshedCallCount(MODULE3_ID) == 0);
	}
	
	@Test
	public void shouldStartModulesInOrder() throws Exception {
		//module test2 depends on test1
		//while test3 depends on test2
		assertTrue(moduleTestData.getWillStartCallTime(MODULE1_ID) <= moduleTestData.getWillStartCallTime(MODULE2_ID));
		assertTrue(moduleTestData.getWillStartCallTime(MODULE2_ID) <= moduleTestData.getWillStartCallTime(MODULE3_ID));
	}
	
	@Test
	public void shouldCallWillStopAndStoppedOnlyForStoppedModule() throws Exception {
		ModuleFactory.stopModule(ModuleFactory.getModuleById(MODULE3_ID));
		
		//should have called willStop() for only module test3
		assertTrue(moduleTestData.getWillStopCallCount(MODULE3_ID) == 1);
		assertTrue(moduleTestData.getWillStopCallCount(MODULE1_ID) == 0);
		assertTrue(moduleTestData.getWillStopCallCount(MODULE2_ID) == 0);
		
		//should have called stopped() for only module test3
		assertTrue(moduleTestData.getStoppedCallCount(MODULE3_ID) == 1);
		assertTrue(moduleTestData.getStoppedCallCount(MODULE1_ID) == 0);
		assertTrue(moduleTestData.getStoppedCallCount(MODULE2_ID) == 0);
		
		//it should also have called willRefreshContext and contextRefreshed for the remaining modules
	}
	
	@Test
	public void shouldStopDependantModulesOnStopModule() throws Exception {
		//since module test2 depends on test1 and test3 depends on test2
		//stopping test1 should also stop both modules test2 and test3
		ModuleFactory.stopModule(ModuleFactory.getModuleById(MODULE1_ID));
		
		//should have called willStop() for only modules test1, test2 and test3
		assertTrue(moduleTestData.getWillStopCallCount(MODULE1_ID) == 1);
		assertTrue(moduleTestData.getWillStopCallCount(MODULE2_ID) == 1);
		assertTrue(moduleTestData.getWillStopCallCount(MODULE3_ID) == 1);
		
		//should have called stopped() for only modules test1, test2 and test3
		assertTrue(moduleTestData.getStoppedCallCount(MODULE1_ID) == 1);
		assertTrue(moduleTestData.getStoppedCallCount(MODULE2_ID) == 1);
		assertTrue(moduleTestData.getStoppedCallCount(MODULE3_ID) == 1);
	}
	
	@Test
	public void shouldCallWillStopAndStoppedOnShutdown() throws Exception {
		ModuleUtil.shutdown();
		
		//should have called willStop()
		assertTrue(moduleTestData.getWillStopCallCount(MODULE1_ID) == 1);
		assertTrue(moduleTestData.getWillStopCallCount(MODULE2_ID) == 1);
		assertTrue(moduleTestData.getWillStopCallCount(MODULE3_ID) == 1);
		
		//should have called stopped()
		assertTrue(moduleTestData.getStoppedCallCount(MODULE1_ID) == 1);
		assertTrue(moduleTestData.getStoppedCallCount(MODULE2_ID) == 1);
		assertTrue(moduleTestData.getStoppedCallCount(MODULE3_ID) == 1);
		
		//willStop() should have been called before stopped()
		assertTrue(moduleTestData.getWillStopCallTime(MODULE1_ID) <= moduleTestData.getStoppedCallTime(MODULE1_ID));
		assertTrue(moduleTestData.getWillStopCallTime(MODULE2_ID) <= moduleTestData.getStoppedCallTime(MODULE2_ID));
		assertTrue(moduleTestData.getWillStopCallTime(MODULE3_ID) <= moduleTestData.getStoppedCallTime(MODULE3_ID));
	}
	
	@Test
	public void shouldExcludePrevouslyStoppedModules() {
		//since module test2 depends on test1 and test3 depends on test2
		//stopping test1 should also stop both modules test2 and test3
		ModuleFactory.stopModule(ModuleFactory.getModuleById(MODULE3_ID));
		
		//should have called willStop() for only modules test1, test2 and test3
		assertTrue(moduleTestData.getWillStopCallCount(MODULE1_ID) == 0);
		assertTrue(moduleTestData.getWillStopCallCount(MODULE2_ID) == 0);
		assertTrue(moduleTestData.getWillStopCallCount(MODULE3_ID) == 1);
		
		assertTrue(moduleTestData.getStoppedCallCount(MODULE1_ID) == 0);
		assertTrue(moduleTestData.getStoppedCallCount(MODULE2_ID) == 0);
		assertTrue(moduleTestData.getStoppedCallCount(MODULE3_ID) == 1);
		
		ModuleUtil.shutdown();
		
		//should have called stopped() for only modules test1, test2 and test3
		assertTrue(moduleTestData.getStoppedCallCount(MODULE1_ID) == 1);
		assertTrue(moduleTestData.getStoppedCallCount(MODULE2_ID) == 1);
		assertTrue(moduleTestData.getStoppedCallCount(MODULE3_ID) == 1);
		
		assertTrue(moduleTestData.getStoppedCallCount(MODULE1_ID) == 1);
		assertTrue(moduleTestData.getStoppedCallCount(MODULE2_ID) == 1);
		assertTrue(moduleTestData.getStoppedCallCount(MODULE3_ID) == 1);
	}
	
	public void init() {
		moduleTestData.init(MODULE1_ID);
		moduleTestData.init(MODULE2_ID);
		moduleTestData.init(MODULE3_ID);
	}
}