/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.openmrs.GlobalProperty;
import org.openmrs.api.context.Context;
import org.springframework.test.context.transaction.TestTransaction;

/**
 * Tests methods of the module activator that do not require refreshing of the spring application
 * context. For those that require refreshing, see WebModuleActivatorTest
 */
public class ModuleActivatorTest extends BaseModuleActivatorTest {
	
	@Test
	public void shouldCallWillStartOnStartup() {
		assertTrue(moduleTestData.getWillStartCallCount(MODULE1_ID) == 1);
		assertTrue(moduleTestData.getWillStartCallCount(MODULE2_ID) == 1);
		assertTrue(moduleTestData.getWillStartCallCount(MODULE3_ID) == 1);
	}
	
	@Test
	public void shouldNotCallStartedOnStartup() {
		assertTrue(moduleTestData.getStartedCallCount(MODULE1_ID) == 0);
		assertTrue(moduleTestData.getStartedCallCount(MODULE2_ID) == 0);
		assertTrue(moduleTestData.getStartedCallCount(MODULE3_ID) == 0);
	}
	
	@Test
	public void shouldNotCallWillStopOnStartup() {
		assertTrue(moduleTestData.getWillStopCallCount(MODULE1_ID) == 0);
		assertTrue(moduleTestData.getWillStopCallCount(MODULE2_ID) == 0);
		assertTrue(moduleTestData.getWillStopCallCount(MODULE3_ID) == 0);
	}
	
	@Test
	public void shouldNotCallStoppedOnStartup() {
		assertTrue(moduleTestData.getStoppedCallCount(MODULE1_ID) == 0);
		assertTrue(moduleTestData.getStoppedCallCount(MODULE2_ID) == 0);
		assertTrue(moduleTestData.getStoppedCallCount(MODULE3_ID) == 0);
	}
	
	@Test
	public void shouldStartModulesInOrder() {
		//module2 depends on module1 while module3 depends on module2
		//so startup order should be module1, module2, module3
		assertTrue(moduleTestData.getWillStartCallTime(MODULE1_ID) <= moduleTestData.getWillStartCallTime(MODULE2_ID));
		assertTrue(moduleTestData.getWillStartCallTime(MODULE2_ID) <= moduleTestData.getWillStartCallTime(MODULE3_ID));
		
		assertTrue(moduleTestData.getStartedCallTime(MODULE1_ID) <= moduleTestData.getStartedCallTime(MODULE2_ID));
		assertTrue(moduleTestData.getStartedCallTime(MODULE2_ID) <= moduleTestData.getStartedCallTime(MODULE3_ID));
	}

	@Test
	public void shouldRunSetupHooksOnInitialStartup() {
		TestTransaction.end();
		Module module = ModuleFactory.getModuleById(MODULE6_ID);
		moduleTestData.init(MODULE6_ID);

		ModuleFactory.stopModule(module);
		
		// delete previous module version for test
		GlobalProperty gp = Context.getAdministrationService().getGlobalPropertyObject("module." + MODULE6_ID + ".version");
		if (gp != null) {
			Context.getAdministrationService().purgeGlobalProperty(gp);
		}

		ModuleFactory.startModule(module);
		
		// Hooks should run because previous module version == null 
		assertEquals(1, moduleTestData.getSetupOnVersionChangeBeforeSchemaChangesCallCount(MODULE6_ID));
		assertEquals(1, moduleTestData.getSetupOnVersionChangeCallCount(MODULE6_ID));
	}

	@Test
	public void shouldNotRunSetupHooksWhenNoVersionChanged() {
		Module module = ModuleFactory.getModuleById(MODULE6_ID);
		moduleTestData.init(MODULE6_ID);

		ModuleFactory.stopModule(module);
		ModuleFactory.startModule(module);

		// Hooks should not run because no version change
		assertEquals(0, moduleTestData.getSetupOnVersionChangeBeforeSchemaChangesCallCount(MODULE6_ID));
		assertEquals(0, moduleTestData.getSetupOnVersionChangeCallCount(MODULE6_ID));
	}

	@Test
	public void shouldRunSetupHooksWhenModuleVersionChanged() {
		Module module = ModuleFactory.getModuleById(MODULE6_ID);
		moduleTestData.init(MODULE6_ID);

		ModuleFactory.stopModule(module);
		module.setVersion("1.3.0");
		ModuleFactory.startModule(module);

		// Hooks should run because module version has changed
		assertEquals(1, moduleTestData.getSetupOnVersionChangeBeforeSchemaChangesCallCount(MODULE6_ID));
		assertEquals(1, moduleTestData.getSetupOnVersionChangeCallCount(MODULE6_ID));
	}

	@Test
	public void shouldRunSetupHooksWhenCoreVersionChanged() {
		TestTransaction.end();
		Module module = ModuleFactory.getModuleById(MODULE6_ID);
		moduleTestData.init(MODULE6_ID);

		ModuleFactory.stopModule(module);

		// manually change previous core version for test
		String propertyName = "core.version";
		String value = "2.0.0";
		GlobalProperty gp = Context.getAdministrationService().getGlobalPropertyObject(propertyName);

		if (gp == null) {
			gp = new GlobalProperty(propertyName, value);
		} else {
			gp.setPropertyValue(value);
		}
		Context.getAdministrationService().saveGlobalProperty(gp);

		ModuleFactory.startModule(module);

		// Hooks should run because core version has changed
		assertEquals(1, moduleTestData.getSetupOnVersionChangeBeforeSchemaChangesCallCount(MODULE6_ID));
		assertEquals(1, moduleTestData.getSetupOnVersionChangeCallCount(MODULE6_ID));

		// clean up
		Context.getAdministrationService().purgeGlobalProperty(gp);
	}

	@Test
	public void shouldRunSetupHooksWhenForceSetupGPIsTrue() {
		TestTransaction.end();
		Module module = ModuleFactory.getModuleById(MODULE6_ID);
		moduleTestData.init(MODULE6_ID);

		ModuleFactory.stopModule(module);

		// change force setup GP
		String propertyName = "force.setup";
		String value = "true";
		GlobalProperty gp = Context.getAdministrationService().getGlobalPropertyObject(propertyName);

		if (gp == null) {
			gp = new GlobalProperty(propertyName, value);
		} else {
			gp.setPropertyValue(value);
		}
		Context.getAdministrationService().saveGlobalProperty(gp);

		ModuleFactory.startModule(module);

		// Hooks should run because force setup GP is true
		assertEquals(1, moduleTestData.getSetupOnVersionChangeBeforeSchemaChangesCallCount(MODULE6_ID));
		assertEquals(1, moduleTestData.getSetupOnVersionChangeCallCount(MODULE6_ID));

		// clean up
		Context.getAdministrationService().purgeGlobalProperty(gp);
	}
	
	@Test
	public void shouldCallWillStopAndStoppedOnlyForStoppedModule() {
		ModuleFactory.stopModule(ModuleFactory.getModuleById(MODULE3_ID));
		
		//should have called willStop() for only module3
		assertTrue(moduleTestData.getWillStopCallCount(MODULE3_ID) == 1);
		assertTrue(moduleTestData.getWillStopCallCount(MODULE1_ID) == 0);
		assertTrue(moduleTestData.getWillStopCallCount(MODULE2_ID) == 0);
		
		//should have called stopped() for only module3
		assertTrue(moduleTestData.getStoppedCallCount(MODULE3_ID) == 1);
		assertTrue(moduleTestData.getStoppedCallCount(MODULE1_ID) == 0);
		assertTrue(moduleTestData.getStoppedCallCount(MODULE2_ID) == 0);
	}
	
	@Test
	public void shouldStopDependantModulesOnStopModule() {
		//since module2 depends on module1, and module3 depends on module2
		//stopping module1 should also stop both module2 and module3
		ModuleFactory.stopModule(ModuleFactory.getModuleById(MODULE1_ID));
		
		//should have called willStop() for all module1, module2 and module3
		assertTrue(moduleTestData.getWillStopCallCount(MODULE1_ID) == 1);
		assertTrue(moduleTestData.getWillStopCallCount(MODULE2_ID) == 1);
		assertTrue(moduleTestData.getWillStopCallCount(MODULE3_ID) == 1);
		
		//should have called stopped() for all module1, module2 and module3
		assertTrue(moduleTestData.getStoppedCallCount(MODULE1_ID) == 1);
		assertTrue(moduleTestData.getStoppedCallCount(MODULE2_ID) == 1);
		assertTrue(moduleTestData.getStoppedCallCount(MODULE3_ID) == 1);

		//willStop() should have been called in the order module1, module2, module3
		assertThat(moduleTestData.getWillStopCallTime(MODULE1_ID), lessThanOrEqualTo(moduleTestData
		        .getWillStopCallTime(MODULE2_ID)));
		assertThat(moduleTestData.getWillStopCallTime(MODULE2_ID), lessThanOrEqualTo(moduleTestData
		        .getWillStopCallTime(MODULE3_ID)));

		//stopped() should have been called in the order module3, module2, module1
		assertThat(moduleTestData.getStoppedCallTime(MODULE3_ID), lessThanOrEqualTo(moduleTestData
		        .getStoppedCallTime(MODULE2_ID)));
		assertThat(moduleTestData.getStoppedCallTime(MODULE2_ID), lessThanOrEqualTo(moduleTestData
		        .getStoppedCallTime(MODULE1_ID)));
	}
	
	@Test
	public void shouldCallWillStopAndStoppedOnShutdown() {
		ModuleUtil.shutdown();
		
		//should have called willStop() for module1, module2, and module3
		assertTrue(moduleTestData.getWillStopCallCount(MODULE1_ID) == 1);
		assertTrue(moduleTestData.getWillStopCallCount(MODULE2_ID) == 1);
		assertTrue(moduleTestData.getWillStopCallCount(MODULE3_ID) == 1);
		
		//should have called stopped() for module1, module2, and module3
		assertTrue(moduleTestData.getStoppedCallCount(MODULE1_ID) == 1);
		assertTrue(moduleTestData.getStoppedCallCount(MODULE2_ID) == 1);
		assertTrue(moduleTestData.getStoppedCallCount(MODULE3_ID) == 1);
		
		//willStop() should have been called before stopped() for module1, module2, and module3
		assertTrue(moduleTestData.getWillStopCallTime(MODULE1_ID) <= moduleTestData.getStoppedCallTime(MODULE1_ID));
		assertTrue(moduleTestData.getWillStopCallTime(MODULE2_ID) <= moduleTestData.getStoppedCallTime(MODULE2_ID));
		assertTrue(moduleTestData.getWillStopCallTime(MODULE3_ID) <= moduleTestData.getStoppedCallTime(MODULE3_ID));
	}
	
	@Test
	public void shouldExcludePreviouslyStoppedModulesOnShutdown() {
		//At OpenMRS shutdown, willStop() and stopped() methods get called for all 
		//started module's activator EXCLUDING any module(s) that were previously stopped.
		
		//now let us make module3 be the previously stopped module
		ModuleFactory.stopModule(ModuleFactory.getModuleById(MODULE3_ID));
		
		//should have called willStop() and stopped() for only module3
		assertTrue(moduleTestData.getWillStopCallCount(MODULE1_ID) == 0);
		assertTrue(moduleTestData.getWillStopCallCount(MODULE2_ID) == 0);
		assertTrue(moduleTestData.getWillStopCallCount(MODULE3_ID) == 1);
		
		assertTrue(moduleTestData.getStoppedCallCount(MODULE1_ID) == 0);
		assertTrue(moduleTestData.getStoppedCallCount(MODULE2_ID) == 0);
		assertTrue(moduleTestData.getStoppedCallCount(MODULE3_ID) == 1);
		
		//now shutdown
		ModuleUtil.shutdown();
		
		//should have called willStop() and stopped() for module1 and module2
		//while willStop() and stopped() should not be called again for module3
		assertTrue(moduleTestData.getWillStopCallCount(MODULE1_ID) == 1);
		assertTrue(moduleTestData.getWillStopCallCount(MODULE2_ID) == 1);
		assertTrue(moduleTestData.getWillStopCallCount(MODULE3_ID) == 1);
		
		assertTrue(moduleTestData.getStoppedCallCount(MODULE1_ID) == 1);
		assertTrue(moduleTestData.getStoppedCallCount(MODULE2_ID) == 1);
		assertTrue(moduleTestData.getStoppedCallCount(MODULE3_ID) == 1);
	}
	
	@Test
	public void shouldCallWillStopAndStoppedOnUnloadModule() {
		
		ModuleFactory.unloadModule(ModuleFactory.getModuleById(MODULE3_ID));
		
		//should have called willStop() and stopped() for module3
		assertTrue(moduleTestData.getWillStopCallCount(MODULE3_ID) == 1);
		assertTrue(moduleTestData.getStoppedCallCount(MODULE3_ID) == 1);
		
		//should not call willStop() and stopped() for module1 and module2
		assertTrue(moduleTestData.getWillStopCallCount(MODULE1_ID) == 0);
		assertTrue(moduleTestData.getWillStopCallCount(MODULE2_ID) == 0);
		assertTrue(moduleTestData.getStoppedCallCount(MODULE1_ID) == 0);
		assertTrue(moduleTestData.getStoppedCallCount(MODULE2_ID) == 0);
	}
	
	@Test
	public void shouldStartBeforeAnotherModule() {
		//module 5 should start before module 4
		assertTrue(moduleTestData.getWillStartCallTime(MODULE5_ID) <= moduleTestData.getWillStartCallTime(MODULE4_ID));
		assertTrue(moduleTestData.getStartedCallTime(MODULE5_ID) <= moduleTestData.getStartedCallTime(MODULE4_ID));
	}
	
	@AfterAll
	public static void cleanUp() {
		//ensure that we do not have any left overs to interfere with other tests
		ModuleUtil.shutdown();
	}
}
