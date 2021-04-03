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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;

public class ModuleFactoryTest extends BaseContextSensitiveTest {
	
	protected static final String MODULE1 = "test1";
	
	protected static final String MODULE1_PATH = "org/openmrs/module/include/test1-1.0-SNAPSHOT.omod";
	protected static final String MODULE1_UPDATE_PATH = "org/openmrs/module/include/test1-2.0-SNAPSHOT.omod";
	
	protected static final String MODULE2 = "test2";
	protected static final String MODULE2_PATH = "org/openmrs/module/include/test2-1.0-SNAPSHOT.omod";
	
	protected static final String MODULE3 = "test3";
	protected static final String MODULE3_PATH = "org/openmrs/module/include/test3-1.0-SNAPSHOT.omod";
	
	@BeforeEach
	public void before() {
		ModuleUtil.shutdown();
		
		String modulesToLoad = MODULE1_PATH + " ";
		       
		runtimeProperties.setProperty(ModuleConstants.RUNTIMEPROPERTY_MODULE_LIST_TO_LOAD, modulesToLoad);
		ModuleUtil.startup(runtimeProperties);
	}
	
	@AfterAll
	public static void cleanUp() {
		//ensure that we do not have any left overs to interfere with other tests
		ModuleUtil.shutdown();
	}
	
	@Test
	public void loadModule_shouldLoadModuleIfItIsCurrentlyNotLoaded() {
		Module test2 = loadModule(MODULE2_PATH, MODULE2, false);
		
		//verify that module test2 is started
		ModuleFactory.startModule(test2);
		assertTrue(ModuleFactory.getLoadedModules().contains(test2));
	}
	
	@Test
	public void loadModule_shouldNotLoadModuleIfAlreadyLoaded() {
		Module test1 = ModuleFactory.getModuleById(MODULE1);
		
		//verify that module test1 is started
		assertNotNull(ModuleFactory.getStartedModuleById(MODULE1));
		assertTrue(test1.isStarted());
		
		//this should throw an exception for trying to load this module again
		assertThrows(ModuleException.class, () -> ModuleFactory.loadModule(test1, false));
	}

	@Test
	public void loadModule_shouldAlwaysLoadModuleIfReplacementIsWanted() {
		Module test1 = ModuleFactory.getModuleById(MODULE1);
		
		Module newModule = loadModule(MODULE1_PATH, MODULE1, true);
		
	    //verify that module test1 is stopped and newModule loaded
		assertNull(ModuleFactory.getStartedModuleById(MODULE1));
		assertFalse(test1.isStarted());
		
		assertTrue(ModuleFactory.getLoadedModules().contains(newModule));
	}
		
	@Test
	public void loadModule_shouldLoadANewerVersionOfTheSameModule() {
		Module test1 = ModuleFactory.getModuleById(MODULE1);

		Module newModule = loadModule(MODULE1_UPDATE_PATH, MODULE1, true);
		
		//verify updated module is loaded and old shut down.
		assertNull(ModuleFactory.getStartedModuleById(MODULE1));
		assertFalse(test1.isStarted());
		
		assertTrue(ModuleFactory.getLoadedModules().contains(newModule));
	}
	
	@Test
	public void loadModule_shouldNotLoadAnOlderVersionOfTheSameModule() {
		Module test1 = ModuleFactory.getModuleById(MODULE1);
		Module newModule = loadModule(MODULE1_UPDATE_PATH, MODULE1, true);
		
		//first upgrade to a newer version so a revert can be tried
		assertNotNull(ModuleFactory.getLoadedModules().contains(newModule));
		
		//now verify that a rollback simply returns the newer version's module.
		Module oldModule = loadModule(MODULE1_PATH, MODULE1, true);
		
		assertEquals(newModule, oldModule);
		assertNotNull(ModuleFactory.getLoadedModules().contains(oldModule));
	}
	
	@Test
	public void startModule_shouldStartAllDependencies() {
		Module test1 = loadModule(MODULE1_PATH, MODULE1, true);
		Module test2 = loadModule(MODULE2_PATH, MODULE2, true);
		
		ModuleFactory.startModule(test2);
		
		assertNotNull(ModuleFactory.getStartedModuleById("test1")); // test1 should have been started, just by starting test2
		assertNotNull(ModuleFactory.getStartedModuleById("test2")); // should be started after starting all dependencies
		assertTrue(test1.isStarted());
		assertTrue(test2.isStarted());
	}
	
	@Test
	public void loadModules_shouldNotCrashWhenFileIsNotFoundOrBroken() {
		ModuleFactory.unloadModule(ModuleFactory.getModuleById(MODULE1));
		String moduleLocation = ModuleUtil.class.getClassLoader().getResource(MODULE1_PATH).getPath();
		moduleLocation += "/i/broke/this/path/module.omod";
		File moduleToLoad = new File(moduleLocation);
		
		List<File> modulesToLoad = new ArrayList<>();
		modulesToLoad.add(moduleToLoad);
		ModuleFactory.loadModules(modulesToLoad);
		
		assertEquals(0, ModuleFactory.getLoadedModules().size());
	}
	
	@Test
	public void loadModules_shouldSetupRequirementMappingsForEveryModule() {
		ModuleFactory.unloadModule(ModuleFactory.getModuleById(MODULE1));
		
		List<File> modulesToLoad = getModuleFiles();
		
		ModuleFactory.loadModules(modulesToLoad);
		assertEquals(3, ModuleFactory.getLoadedModules().size());
		
		Module test1 = ModuleFactory.getModuleById(MODULE1);
		Module test2 = ModuleFactory.getModuleById(MODULE2);
		Module test3 = ModuleFactory.getModuleById(MODULE3);
		
		assertEquals(0, test1.getRequiredModules().size());
		assertEquals(1, test2.getRequiredModules().size());
		assertEquals(1, test3.getRequiredModules().size());
	}
	
	@Test
	public void loadModules_shouldNotStartTheLoadedModules() {
		ModuleFactory.unloadModule(ModuleFactory.getModuleById(MODULE1));
		
		List<File> modulesToLoad = getModuleFiles();
		
		ModuleFactory.loadModules(modulesToLoad);
		assertEquals(3, ModuleFactory.getLoadedModules().size());
		
		Module test1 = ModuleFactory.getModuleById(MODULE1);
		Module test2 = ModuleFactory.getModuleById(MODULE2);
		Module test3 = ModuleFactory.getModuleById(MODULE3);
		
		assertFalse(test1.isStarted());
		assertFalse(test2.isStarted());
		assertFalse(test3.isStarted());
	}
	
	private Module loadModule(String location, String moduleName, boolean replace) {
		String moduleLocation = ModuleUtil.class.getClassLoader().getResource(location).getPath();

		return ModuleFactory.loadModule(new File(moduleLocation), replace);
	}
	
	private List<File> getModuleFiles() {
		List<File> modulesToLoad = new ArrayList<>();
		modulesToLoad.add(new File(ModuleUtil.class.getClassLoader().getResource(MODULE1_PATH).getPath()));
		modulesToLoad.add(new File(ModuleUtil.class.getClassLoader().getResource(MODULE2_PATH).getPath()));
		modulesToLoad.add(new File(ModuleUtil.class.getClassLoader().getResource(MODULE3_PATH).getPath()));
		
		return modulesToLoad;
	}
}
