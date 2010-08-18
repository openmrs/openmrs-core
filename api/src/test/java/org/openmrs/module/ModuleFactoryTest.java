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

import java.io.File;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.test.Verifies;

/**
 * Tests for the {@link ModuleFactory} for correct functioning of the queuing of modules.
 */
public class ModuleFactoryTest extends BaseContextSensitiveTest {
	
	/**
	 * Constructor that initializes the ModuleFactor loaded modules
	 */
	public ModuleFactoryTest() {
		String[] moduleIds = { "formimportexport", "restmodule", "gmapsviewer", "dssmodule" };
		for (String moduleId : moduleIds) {
			Module testModule = new Module("For JUnit Test");
			testModule.setModuleId(moduleId);
			ModuleFactory.getLoadedModulesMap().put(moduleId, testModule);
		}
	}
	
	/**
	 * Tests whether a loaded module is allowed to be queued for starting
	 */
	@Test
	@Verifies(value = "should return true if tried to start a loaded module", method = "queueModuleAction(String, ModuleAction)")
	public void queueModuleAction_shouldReturnTrueIfTriedToStartALoadedModule() {
		final boolean EXPECTED = true;
		boolean actual = ModuleFactory.queueModuleAction("formimportexport", ModuleAction.PENDING_START);
		Assert.assertEquals(EXPECTED, actual);
	}
	
	/**
	 * Tests whether a loaded and started module is allowed to be queued for stop
	 */
	@Test
	@Verifies(value = "should return true if tried to stop a loaded and started module", method = "queueModuleAction(String, ModuleAction)")
	public void queueModuleAction_shouldReturnTrueIfTriedToStopALoadedAndStartedModule() {
		final boolean EXPECTED = true;
		final String MODULE_ID = "restmodule";
		ModuleFactory.getStartedModulesMap().put(MODULE_ID, ModuleFactory.getModuleById(MODULE_ID));
		boolean actual = ModuleFactory.queueModuleAction(MODULE_ID, ModuleAction.PENDING_STOP);
		Assert.assertEquals(EXPECTED, actual);
	}
	
	/**
	 * Tests whether a loaded module is allowed to be queued for unloading
	 */
	@Test
	@Verifies(value = "should return true if tried to unload a loaded module", method = "queueModuleAction(String, ModuleAction)")
	public void queueModuleAction_shouldReturnTrueIfTriedToUnloadALoadedModule() {
		final boolean EXPECTED = true;
		boolean actual = ModuleFactory.queueModuleAction("gmapsviewer", ModuleAction.PENDING_UNLOAD);
		Assert.assertEquals(EXPECTED, actual);
	}

	/**
	 * Tests whether a loaded module with a updateFile is allowed to be queued for upgrade
	 */
	@Test
	@Verifies(value = "should return true if tried to upgrade a loaded module with a new update file", method="queueModuleAction(String, ModuleAction)")
	public void queueModuleAction_shouldReturnTrueIfTriedToUpgradeALoadedModule() {
		final boolean EXPECTED = true;
		final String MODULE_ID = "dssmodule";
		Module upgradeModule = ModuleFactory.getModuleById(MODULE_ID);
		upgradeModule.setUpdateFile(new File("org/openmrs/module/include/dssmodule-1.44.omod"));
		boolean actual = ModuleFactory.queueModuleAction(MODULE_ID, ModuleAction.PENDING_UPGRADE);
		Assert.assertEquals(EXPECTED, actual);
	}
	
	/**
	 * Tests whether a ModuleException is thrown if tried to queue non loaded module
	 * 
	 * @throws Exception
	 */
	@Test(expected = ModuleException.class)
	@Verifies(value = "should throw ModuleException if moduleId supplied is of a non existent module", method="queueModuleAction(String, ModuleAction)")
	public void queueModuleAction_shouldThrowModuleExceptionIfModuleIdSuppliedIsOfANonExistentModule() throws Exception {
		final String NON_EXISTING_MODULE_ID = "ui.springmvc";
		ModuleFactory.queueModuleAction(NON_EXISTING_MODULE_ID, ModuleAction.PENDING_START);
	}
	
	/**
	 * Tests whether a module already started not allowed to be queue for start again
	 */
	@Test
	@Verifies(value = "should return false if tried to start a started module", method = "queueModuleAction(String, ModuleAction)")
	public void queueModuleAction_shouldReturnFalseIfTriedToStartAStartedModule() {
		final boolean EXPECTED = false;
		final String MODULE_ID = "restmodule";
		Module module = ModuleFactory.getModuleById(MODULE_ID);
		ModuleFactory.getStartedModulesMap().put(MODULE_ID, module);
		boolean actual = ModuleFactory.queueModuleAction(MODULE_ID, ModuleAction.PENDING_START);
		Assert.assertEquals(EXPECTED, actual);
	}
	
	/**
	 * Tests whether a module stopped not allowed to be queued for stop again
	 */
	@Test
	@Verifies(value = "should return false if tried to stop a not started module", method = "queueModuleAction(String, ModuleAction)")
	public void queueModuleAction_shouldReturnFalseIfTriedToStopANotStartedModule() {
		final boolean EXPECTED = false;
		final String MODULE_ID = "formimportexport";
		boolean actual = ModuleFactory.queueModuleAction(MODULE_ID, ModuleAction.PENDING_STOP);
		Assert.assertEquals(EXPECTED, actual);
	}
	
	/**
	 * Tests whether a module is not allowed to be queued for upgrade with a new update file
	 */
	@Test
	@Verifies(value = "should return false if tried to upgrade a loaded module without a new update file", method = "queueModuleAction(String, ModuleAction)")
	public void queueModuleAction_shouldReturnFalseIfTriedToUpgradeALoadedModuleWithoutANewUpdateFile() {
		final boolean EXPECTED = false;
		final String MODULE_ID = "formimportexport";
		Module updateModule = ModuleFactory.getModuleById(MODULE_ID);
		//No update file is being set
		updateModule.setUpdateFile(null);
		boolean actual = ModuleFactory.queueModuleAction(MODULE_ID, ModuleAction.PENDING_UPGRADE);
		Assert.assertEquals(EXPECTED, actual);
	}
	
	/**
	 * Tests whether modules with pending actions iterator available after queuing of a module
	 */
	@Test
	@Verifies(value = "should return an iterator with atleast one element if a module action was queued", method = "getModulesWithPendingAction()")
	public void getModulesWithPendingAction_shouldReturnAnIteratorWithAtleastOneElementIfAModuleActionWasQueued() {
		final boolean EXPECTED = true;
		boolean actual = ModuleFactory.getModulesWithPendingAction().hasNext();
		Assert.assertEquals(EXPECTED, actual);
	}
	
	/**
	 * Tests whether modules with pending actions are available after queuing of a module
	 */
	@Test
	@Verifies(value = "should return true if there are modules with pending actions", method = "hasPendingModuleActions")
	public void hasPendingModuleActions_shouldReturnTrueIfThereAreModulesWithPendingActions() {
		final boolean EXPECTED = true;
		boolean actual = ModuleFactory.hasPendingModuleActions();
		Assert.assertEquals(EXPECTED, actual);
	}
	
	/**
	 * Tests whether it is possible to check whether there is a pending action for a particular
	 * module.
	 */
	@Test
	@Verifies(value = "should return true if there is a pending action for the given moduleId", method = "hasPendingModuleActionForModuleId(String)")
	public void hasPendingModuleActionForModuleId_shouldReturnTrueIfThereIsAPendingActionForTheGivenModuleId() {
		final boolean EXPECTED = true;
		final String moduleId = "restmodule";
		boolean actual = ModuleFactory.hasPendingModuleActionForModuleId(moduleId);
		Assert.assertEquals(EXPECTED, actual);
	}
	
	/**
	 * Tests whether a pending action of a particular module can be cleared
	 */
	@Test
	@Verifies(value = "should clear the pending module action for a module", method = "clearPendingActionOfModuleId(String)")
	public void clearPendingActionOfModuleId_shouldClearThePendingModuleActionForAModule() {
		final boolean EXPECTED = false;
		ModuleFactory.clearPendingActionOfModuleId("restmodule");
		boolean actual = ModuleFactory.hasPendingModuleActionForModuleId("restmodule");
		Assert.assertEquals(EXPECTED, actual);
	}

	/**
	 * Tests whether the clearing of pending actions work
	 */
	@Test
	@Verifies(value = "should clear all the pending module actions", method = "clearAllPendingActions()")
	public void clearAllPendingActions_shouldClearAllThePendingModuleActions() {
		final boolean EXPECTED = false;
		ModuleFactory.clearAllPendingActions();
		boolean actual = ModuleFactory.hasPendingModuleActions();
		Assert.assertEquals(EXPECTED, actual);
	}

}
