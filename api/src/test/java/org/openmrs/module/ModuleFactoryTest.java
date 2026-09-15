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
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;


import javax.annotation.Resource;

public class ModuleFactoryTest extends BaseContextSensitiveTest {
	
	protected static final String MODULE1 = "test1";
	
	protected static final String MODULE1_PATH = "org/openmrs/module/include/test1-1.0-SNAPSHOT.omod";
	protected static final String MODULE1_UPDATE_PATH = "org/openmrs/module/include/test1-2.0-SNAPSHOT.omod";
	
	protected static final String MODULE2 = "test2";
	protected static final String MODULE2_PATH = "org/openmrs/module/include/test2-1.0-SNAPSHOT.omod";
	
	protected static final String MODULE3 = "test3";
	protected static final String MODULE3_PATH = "org/openmrs/module/include/test3-1.0-SNAPSHOT.omod";
	
	@Resource(name = "testingModuleEventListener")
	TestModuleEventListener testModuleEventListener;

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
		assertTrue(ModuleFactory.getLoadedModules().contains(newModule));
		
		// now verify that a rollback to an older version fails and keeps the newer module loaded.
		assertThrows(ModuleException.class, () -> loadModule(MODULE1_PATH, MODULE1, true));
		assertTrue(ModuleFactory.getLoadedModules().contains(newModule));
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
	
	@Test
	public void loadModule_shouldPublishLoadModuleEventWhenNoOldModuleExists() {
		ModuleFactory.unloadModule(ModuleFactory.getModuleById(MODULE1));
		testModuleEventListener.events.clear();

		String moduleLocation = ModuleUtil.class.getClassLoader().getResource(MODULE1_PATH).getPath();
		File moduleToLoad = new File(moduleLocation);

		ModuleFactory.loadModule(moduleToLoad);

		assertEquals(1, testModuleEventListener.events.size());
		assertEquals("test1:Test1 Module:1.0-SNAPSHOT:" + moduleToLoad.getName() + ":MODULE_LOAD:true:null", testModuleEventListener.events.get(0));
	}

	@Test
	public void loadModule_shouldPublishUnloadAndLoadModuleEventIfOldModuleExistsOfSameVersionAndReplacedExistingIsTrue() {
		testModuleEventListener.events.clear();

		String moduleLocation = ModuleUtil.class.getClassLoader().getResource(MODULE1_PATH).getPath();
		File moduleToLoad = new File(moduleLocation);

		ModuleFactory.loadModule(moduleToLoad);

		assertEquals(3, testModuleEventListener.events.size());
		assertEquals("test1:Test1 Module:1.0-SNAPSHOT:MODULE_STOP:true:null", testModuleEventListener.events.get(0));
		assertEquals("test1:Test1 Module:1.0-SNAPSHOT:MODULE_UNLOAD:true:null", testModuleEventListener.events.get(1));
		assertEquals("test1:Test1 Module:1.0-SNAPSHOT:" + moduleToLoad.getName() + ":MODULE_LOAD:true:null", testModuleEventListener.events.get(2));
	}

	@Test
	public void loadModule_shouldPublishFailedLoadModuleEventIfOldModuleExistsOfSameVersionAndReplacedExistingIsFalse() {
		testModuleEventListener.events.clear();

		String moduleLocation = ModuleUtil.class.getClassLoader().getResource(MODULE1_PATH).getPath();
		File moduleToLoad = new File(moduleLocation);

		ModuleException exception = assertThrows(
				ModuleException.class,
				() -> ModuleFactory.loadModule(moduleToLoad, false));
		
		assertEquals(1, testModuleEventListener.events.size());
		assertEquals("test1:Test1 Module:1.0-SNAPSHOT:" + moduleToLoad.getName() + ":MODULE_LOAD:false:" + exception.getMessage(), testModuleEventListener.events.get(0));
	}

	@Test
	public void loadModule_shouldPublishFailedLoadModuleEventIfOldModuleExistsOfNewerVersion() {
		ModuleFactory.unloadModule(ModuleFactory.getModuleById(MODULE1));
		testModuleEventListener.events.clear();

		String oldModuleLocation = ModuleUtil.class.getClassLoader().getResource(MODULE1_UPDATE_PATH).getPath();
		File oldModuleToLoad = new File(oldModuleLocation);
		ModuleFactory.loadModule(oldModuleToLoad);
		testModuleEventListener.events.clear();

		String newModuleLocation = ModuleUtil.class.getClassLoader().getResource(MODULE1_PATH).getPath();
		File newModuleToLoad = new File(newModuleLocation);
		ModuleException exception = assertThrows(ModuleException.class, () -> ModuleFactory.loadModule(newModuleToLoad));

		String failureReason = "A newer version of this module is already loaded";
		assertEquals(1, testModuleEventListener.events.size());
		assertEquals(failureReason, exception.getMessage());
		assertEquals("test1:Test1 Module:1.0-SNAPSHOT:" + newModuleToLoad.getName() + ":MODULE_LOAD:false:" + failureReason, testModuleEventListener.events.get(0));
	}
	
	@Test
	void loadModule_shouldPublishFailStopUnloadAndLoadModuleEventIfErrorOccurWhileStoppingExistingModule_AndReplaceExistingIsTrue() {
		ModuleFactory.unloadModule(ModuleFactory.getModuleById(MODULE1));
		testModuleEventListener.events.clear();
		
		String moduleLocation = ModuleUtil.class.getClassLoader().getResource(MODULE1_PATH).getPath();
		File moduleToLoad = new File(moduleLocation);
		ModuleFactory.loadModule(moduleToLoad);
		
		Module test1 = ModuleFactory.getModuleById(MODULE1);
		ModuleFactory.startModule(test1);
		testModuleEventListener.events.clear();
		
		assertTrue(test1.isStarted());
		test1.setMandatory(true);

		MandatoryModuleException exception = assertThrows(MandatoryModuleException.class, () -> ModuleFactory.loadModule(moduleToLoad));
		
		assertEquals(3, testModuleEventListener.events.size());
		assertEquals("test1:Test1 Module:1.0-SNAPSHOT:MODULE_STOP:false:" + exception.getMessage(), testModuleEventListener.events.get(0));
		assertEquals("test1:Test1 Module:1.0-SNAPSHOT:MODULE_UNLOAD:false:" + exception.getMessage(), testModuleEventListener.events.get(1));
		assertEquals("test1:Test1 Module:1.0-SNAPSHOT:" + moduleToLoad.getName() + ":MODULE_LOAD:false:" + exception.getMessage(), testModuleEventListener.events.get(2));
	}
	
	@Test
	void loadModule_shouldPublishFailLoadModuleEventIfModuleFileIsInvalidAndExceptionOccurWhileParsingFile() {
		ModuleFactory.unloadModule(ModuleFactory.getModuleById(MODULE1));
		testModuleEventListener.events.clear();

		File moduleFile = new File("webservices.rest-2.50.0.0.omok");

		ModuleException exception = assertThrows(
			ModuleException.class,
			() -> ModuleFactory.loadModule(moduleFile, false));
		
		assertEquals(1, testModuleEventListener.events.size());
		assertEquals("null:null:null:" + moduleFile.getName() + ":MODULE_LOAD:false:" + exception.getMessage(), testModuleEventListener.events.get(0));
	}
	
	@Test
	void loadModule_shouldPublishFailLoadModuleEventWithNullModuleMetadataIfModuleFileNameIsEmpty() {
		ModuleFactory.unloadModule(ModuleFactory.getModuleById(MODULE1));
		testModuleEventListener.events.clear();
		
		File moduleFile = new File(" ");
		
		ModuleException exception = assertThrows(
			ModuleException.class,
			() -> ModuleFactory.loadModule(moduleFile, false)
		);
		assertEquals(1, testModuleEventListener.events.size());
		assertEquals("null:null:null: :MODULE_LOAD:false:" + exception.getMessage(), testModuleEventListener.events.get(0));
	}

	@Test
	public void startModule_shouldPublishSuccessStartModuleEvent() {

		Module test1 = ModuleFactory.getModuleById(MODULE1);
		ModuleFactory.stopModule(test1);
		testModuleEventListener.events.clear();
		
		ModuleFactory.startModule(test1);

		assertTrue(test1.isStarted());
		assertEquals(1, testModuleEventListener.events.size());
		assertEquals("test1:Test1 Module:1.0-SNAPSHOT:MODULE_START:true:null", testModuleEventListener.events.get(0));
	}

	@Test
	public void startModule_shouldPublishFailedStartModuleEventIfModuleExceptionOccurAndPublishStopModuleEvent() {
		Module test1 = ModuleFactory.getModuleById(MODULE1);
		ModuleFactory.stopModule(test1);
		test1.setModuleActivator(new ThrowingModuleActivator());
		testModuleEventListener.events.clear();

		ModuleFactory.startModule(test1);
		
		assertFalse(test1.isStarted());
		assertEquals(2, testModuleEventListener.events.size());
		assertEquals("test1:Test1 Module:1.0-SNAPSHOT:MODULE_STOP:true:null", testModuleEventListener.events.get(0));
		assertEquals("test1:Test1 Module:1.0-SNAPSHOT:MODULE_START:false:" + test1.getStartupErrorMessage(), testModuleEventListener.events.get(1));
	}
	
	@Test
	public void stopModule_shouldPublishSuccessStopModuleEvent() {
		testModuleEventListener.events.clear();
		
		Module test1 = ModuleFactory.getModuleById(MODULE1);
		assertTrue(test1.isStarted());
		
		ModuleFactory.stopModule(test1, false, false);
		
		assertFalse(test1.isStarted());
		assertEquals(1, testModuleEventListener.events.size());
		assertEquals("test1:Test1 Module:1.0-SNAPSHOT:MODULE_STOP:true:null", testModuleEventListener.events.get(0));
	}

	@Test
	public void stopModule_shouldPublishFailStopModuleEventIfModuleIsMandatory() {
		testModuleEventListener.events.clear();
		Module test1 = ModuleFactory.getModuleById(MODULE1);
		
		assertTrue(test1.isStarted());
		test1.setMandatory(true);

		ModuleMustStartException exception = assertThrows(ModuleMustStartException.class, () -> ModuleFactory.stopModule(test1, false, false));

		assertTrue(test1.isStarted());
		assertEquals(1, testModuleEventListener.events.size());
		assertEquals("test1:Test1 Module:1.0-SNAPSHOT:MODULE_STOP:false:" + exception.getMessage(), testModuleEventListener.events.get(0));
	}

	@Test
	public void stopModule_shouldNotPublishModuleEventIfModuleNotEvenStarted() {
		testModuleEventListener.events.clear();

		Module test1 = ModuleFactory.getModuleById(MODULE1);
		assertTrue(test1.isStarted());
		ModuleFactory.stopModule(test1, false, false);
		testModuleEventListener.events.clear();

		assertFalse(test1.isStarted());

		ModuleFactory.stopModule(test1, false, false);

		assertFalse(test1.isStarted());
		assertEquals(0, testModuleEventListener.events.size());
	}

	@Test
	public void unloadModule_shouldPublishSuccessStopAndUnloadModuleEvent() {
		testModuleEventListener.events.clear();

		Module test1 = ModuleFactory.getModuleById(MODULE1);
		assertTrue(test1.isStarted());
		
		ModuleFactory.unloadModule(ModuleFactory.getModuleById(MODULE1));
		
		assertEquals(2, testModuleEventListener.events.size());
		assertEquals("test1:Test1 Module:1.0-SNAPSHOT:MODULE_STOP:true:null", testModuleEventListener.events.get(0));
		assertEquals("test1:Test1 Module:1.0-SNAPSHOT:MODULE_UNLOAD:true:null", testModuleEventListener.events.get(1));

	}

	@Test
	public void unloadModule_shouldPublishFailUnloadModuleEventIfUnloadingMandatoryModule() {
		testModuleEventListener.events.clear();

		Module test1 = ModuleFactory.getModuleById(MODULE1);
		assertTrue(test1.isStarted());
		test1.setMandatory(true);

		ModuleMustStartException exception = assertThrows(ModuleMustStartException.class, () -> ModuleFactory.unloadModule(test1));

		assertEquals(2, testModuleEventListener.events.size());
		assertEquals("test1:Test1 Module:1.0-SNAPSHOT:MODULE_STOP:false:" + exception.getMessage(), testModuleEventListener.events.get(0));
		assertEquals("test1:Test1 Module:1.0-SNAPSHOT:MODULE_UNLOAD:false:" + exception.getMessage(), testModuleEventListener.events.get(1));
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

	private static class ThrowingModuleActivator extends BaseModuleActivator {
		@Override
		public void willStart() {
			throw new ModuleException("Unable to start the module");
		}
	}

	@Component("testingModuleEventListener")
	public static class TestModuleEventListener {

		public List<String> events = new ArrayList<>();

		@EventListener
		public void onModuleEvent(AbstractModuleEvent moduleEvent) {
			String entry = moduleEvent.getModuleId() + ":" + moduleEvent.getModuleName() + ":" + moduleEvent.getModuleVersion();
			if (moduleEvent instanceof ModuleLoadEvent) {
				entry += ":" + ((ModuleLoadEvent) moduleEvent).getModuleFileName();
			}
			entry += ":" + getEventTypeLabel(moduleEvent) + ":" + moduleEvent.isSuccess() + ":" + moduleEvent.getFailureReason();
			events.add(entry);
		}

		private String getEventTypeLabel(AbstractModuleEvent moduleEvent) {
			if (moduleEvent instanceof ModuleLoadEvent) {
				return "MODULE_LOAD";
			}
			if (moduleEvent instanceof ModuleStartEvent) {
				return "MODULE_START";
			}
			if (moduleEvent instanceof ModuleStopEvent) {
				return "MODULE_STOP";
			}
			if (moduleEvent instanceof ModuleUnloadEvent) {
				return "MODULE_UNLOAD";
			}
			return moduleEvent.getClass().getSimpleName();
		}

		public void clear() {
			events.clear();
		}
	}
}
