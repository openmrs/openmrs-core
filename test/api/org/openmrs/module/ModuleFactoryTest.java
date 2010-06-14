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
	
	public ModuleFactoryTest() {
		String[] moduleIds = { "formimportexport", "restmodule", "gmapsviewer", "dssmodule" };
		for (String moduleId : moduleIds) {
			Module testModule = new Module("For JUnit Test");
			testModule.setModuleId(moduleId);
			ModuleFactory.getLoadedModulesMap().put(moduleId, testModule);
		}
	}

	@Test
	@Verifies(value = "should return true if tried to start a loaded module", method = "queueModuleAction(String, ModuleAction)")
	public void queueModuleAction_shouldReturnTrueIfTriedToStartALoadedModule() {
		final boolean expected = true;
		boolean actual = ModuleFactory.queueModuleAction("formimportexport", ModuleAction.PENDING_START);
		Assert.assertEquals(expected, actual);
	}

	@Test
	@Verifies(value = "should return true if tried to stop a loaded and started module", method = "queueModuleAction(String, ModuleAction)")
	public void queueModuleAction_shouldReturnTrueIfTriedToStopALoadedAndStartedModule() {
		final boolean expected = true;
		final String moduleId = "restmodule";
		ModuleFactory.getStartedModulesMap().put(moduleId, ModuleFactory.getModuleById(moduleId));
		boolean actual = ModuleFactory.queueModuleAction(moduleId, ModuleAction.PENDING_STOP);
		Assert.assertEquals(expected, actual);
	}

	@Test
	@Verifies(value = "should return true if tried to unload a loaded module", method = "queueModuleAction(String, ModuleAction)")
	public void queueModuleAction_shouldReturnTrueIfTriedToUnloadALoadedModule() {
		final boolean expected = true;
		boolean actual = ModuleFactory.queueModuleAction("gmapsviewer", ModuleAction.PENDING_UNLOAD);
		Assert.assertEquals(expected, actual);
	}

	@Test
	@Verifies(value = "should return true if tried to upgrade a loaded module with a new update file", method="queueModuleAction(String, ModuleAction)")
	public void queueModuleAction_shouldReturnTrueIfTriedToUpgradeALoadedModule() {
		final boolean expected = true;
		final String moduleId = "dssmodule";
		Module upgradeModule = ModuleFactory.getModuleById(moduleId);
		upgradeModule.setUpdateFile(new File("org/openmrs/module/include/dssmodule-1.44.omod"));
		boolean actual = ModuleFactory.queueModuleAction(moduleId, ModuleAction.PENDING_UPGRADE);
		Assert.assertEquals(expected, actual);
	}

	@Test(expected = ModuleException.class)
	@Verifies(value = "should throw ModuleException if moduleId supplied is of a non existent module", method="queueModuleAction(String, ModuleAction)")
	public void queueModuleAction_shouldThrowModuleExceptionIfModuleIdSuppliedIsOfANonExistentModule() throws Exception {
		final String nonExistingModuleId = "ui.springmvc";
		ModuleFactory.queueModuleAction(nonExistingModuleId, ModuleAction.PENDING_START);
	}
	
	@Test
	@Verifies(value = "should return false if tried to start a started module", method = "queueModuleAction(String, ModuleAction)")
	public void queueModuleAction_shouldReturnFalseIfTriedToStartAStartedModule() {
		final boolean expected = false;
		final String moduleId = "restmodule";
		Module module = ModuleFactory.getModuleById(moduleId);
		ModuleFactory.getStartedModulesMap().put(moduleId, module);
		boolean actual = ModuleFactory.queueModuleAction(moduleId, ModuleAction.PENDING_START);
		Assert.assertEquals(expected, actual);
	}
	
	@Test
	@Verifies(value = "should return false if tried to stop a not started module", method = "queueModuleAction(String, ModuleAction)")
	public void queueModuleAction_shouldReturnFalseIfTriedToStopANotStartedModule() {
		final boolean expected = false;
		final String moduleId = "formimportexport";
		boolean actual = ModuleFactory.queueModuleAction(moduleId, ModuleAction.PENDING_STOP);
		Assert.assertEquals(expected, actual);
	}

	@Test
	@Verifies(value = "should return false if tried to upgrade a loaded module without a new update file", method = "queueModuleAction(String, ModuleAction)")
	public void queueModuleAction_shouldReturnFalseIfTriedToUpgradeALoadedModuleWithoutANewUpdateFile() {
		final boolean expected = false;
		final String moduleId = "formimportexport";
		Module updateModule = ModuleFactory.getModuleById(moduleId);
		//No update file is being set
		updateModule.setUpdateFile(null);
		boolean actual = ModuleFactory.queueModuleAction(moduleId, ModuleAction.PENDING_UPGRADE);
		Assert.assertEquals(expected, actual);
	}
	
	@Test
	@Verifies(value = "should return an iterator with atleast one element if a module action was queued", method = "getModulesWithPendingAction()")
	public void getModulesWithPendingAction_shouldReturnAnIteratorWithAtleastOneElementIfAModuleActionWasQueued() {
		final boolean expected = true;
		boolean actual = ModuleFactory.getModulesWithPendingAction().hasNext();
		Assert.assertEquals(expected, actual);
	}
	
	@Test
	@Verifies(value = "should return true if there are modules with pending actions", method = "hasPendingModuleActions")
	public void hasPendingModuleActions_shouldReturnTrueIfThereAreModulesWithPendingActions() {
		final boolean expected = true;
		boolean actual = ModuleFactory.hasPendingModuleActions();
		Assert.assertEquals(expected, actual);
	}
	
	@Test
	@Verifies(value = "should return true if there is a pending action for the given moduleId", method = "hasPendingModuleActionForModuleId(String)")
	public void hasPendingModuleActionForModuleId_shouldReturnTrueIfThereIsAPendingActionForTheGivenModuleId() {
		final boolean expected = true;
		final String moduleId = "restmodule";
		boolean actual = ModuleFactory.hasPendingModuleActionForModuleId(moduleId);
		Assert.assertEquals(expected, actual);
	}

	@Test
	@Verifies(value = "should clear all the pending module actions", method = "clearAllPendingActions()")
	public void clearAllPendingActions_shouldClearAllThePendingModuleActions() {
		final boolean expected = false;
		ModuleFactory.clearAllPendingActions();
		boolean actual = ModuleFactory.hasPendingModuleActions();
		Assert.assertEquals(expected, actual);
	}
		
	

}
