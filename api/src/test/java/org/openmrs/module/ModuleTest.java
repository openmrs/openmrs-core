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

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests Module Methods
 */

public class ModuleTest {

	private Module testModule;

	@BeforeEach
	public void before() {
		testModule = new Module("test");
	}

	/*
	 * @see Module#setStartupErrorMessage(String)
	 */
	@Test
	public void setStartupErrorMessage_shouldThrowExceptionWhenMessageIsNull() {
		assertThrows(ModuleException.class, () -> testModule.setStartupErrorMessage(null));
	}

	/*
	 * @see Module#setStartupErrorMessage(String, Throwable)
	 */
	@Test
	public void setStartupErrorMessage_shouldThrowExceptionWhenThrowableIsNull() {
		assertThrows(ModuleException.class, () -> testModule.setStartupErrorMessage("error", null));
	}

	/*
	 * @see Module#setStartupErrorMessage(String, Throwable)
	 */
	@Test
	public void setStartupErrorMessage_shouldsetStartupErrorMessageWhenExceptionMessageIsNull () {
		ModuleException modException = new ModuleException("error");

		assertFalse(testModule.hasStartupError());
		testModule.setStartupErrorMessage(null, modException);
		assertTrue(testModule.hasStartupError());

		assertEquals("error\n", testModule.getStartupErrorMessage());
	}

	/*
 	 * @see Module#setStartupErrorMessage(String, Throwable)
	 */
	@Test
	public void setStartupErrorMessage_shouldAppendTheThrowablesMessageToExceptionMessage() {
		ModuleException modException = new ModuleException("error2");

		assertNull(testModule.getStartupErrorMessage());
		testModule.setStartupErrorMessage("error1", modException);

		assertEquals("error1\nerror2\n", testModule.getStartupErrorMessage());
	}

	/*
	 * @see Module#setRequiredModules(List<String>)
	 */
	@Test
	public void setRequiredModules_shouldSetModulesWhenThereIsANullRequiredModulesMap() {
		testModule.setRequiredModulesMap(null);
		assertNull(testModule.getRequiredModulesMap());

		ArrayList<String> first = new ArrayList<>();
		ArrayList<String> second = new ArrayList<>();

		first.add("mod1");
		first.add("mod2");
		second.add("mod2");
		second.add("mod3");

		testModule.setRequiredModules(first);
		testModule.setRequiredModules(second);

		ArrayList<String> ret = new ArrayList<>(testModule.getRequiredModules());
		assertTrue(ret.contains("mod1"));
		assertTrue(ret.contains("mod2"));
		assertTrue(ret.contains("mod3"));
		assertEquals(3, ret.size());
	}

	/*
	 * @see Module#getRequiredModuleVersion(String)
	 */
	@Test
	public void getRequiredModuleVersion_shouldReturnNullIfNoRequiredModulesExist() {
		testModule.setRequiredModulesMap(null);
		assertNull(testModule.getRequiredModules());

		testModule.addRequiredModule("mod1", "1.0");
		assertNull(testModule.getRequiredModuleVersion("mod1"));
	}

	/*
	 * @see Module#getRequiredModuleVersion(String)
	 */
	@Test
	public void getRequiredModuleVersion_shouldReturnNullIfNoRequiredModuleByGivenNameExists () {
		IdentityHashMap<String, String> requiredModules = new IdentityHashMap<>();
		
		requiredModules.put("mod1", "1.0");
		testModule.setRequiredModulesMap(requiredModules);

		assertEquals("1.0", testModule.getRequiredModuleVersion("mod1"));
		assertNull(testModule.getRequiredModuleVersion("mod2"));
	}

	/*
	 * @see Module#addRequiredModule(String, String)
	 */
	@Test
	public void addRequiredModule_shouldAddModuleToRequiredModulesMap () {
		testModule.setRequiredModulesMap(new IdentityHashMap<>());
		testModule.addRequiredModule("mod1", "1.0");
		
		assertEquals("1.0", testModule.getRequiredModuleVersion("mod1"));
	}

	/*
	 * @see Module#disposeAdvicePointsClassInstance()
	 */
	@Test
	public void disposeAdvicePointsClassInstance_shouldDisposeAllClassInstancesNotAdvicePoints() {
		ArrayList<AdvicePoint> points = new ArrayList<>();
		String obj1 = "string";
		ArrayList<String> obj2 = new ArrayList<>();
		AdvicePoint point1 = new AdvicePoint("point1", obj1.getClass());
		AdvicePoint point2 = new AdvicePoint("point2", obj2.getClass());

		points.add(point1);
		points.add(point2);

		testModule.setAdvicePoints(null);
		testModule.disposeAdvicePointsClassInstance();

		assertEquals(obj1.getClass(), point1.getClassInstance().getClass());
		assertEquals(obj2.getClass(), point2.getClassInstance().getClass());

		testModule.setAdvicePoints(points);
		testModule.disposeAdvicePointsClassInstance();

		assertNotNull(point1);
		assertNotNull(point2);
		assertNull(point1.getClassInstance());
		assertNull(point2.getClassInstance());
	}
	//Xin: Test equals and hashCode Methods
	@Test
	public void equals_shouldReturnTrueForSameModuleId() {
		Module module1 = new Module("Module1", "org.openmrs.module1", "org.openmrs.module1", "Author1", "Description1", "1.0", "1.0");
		Module module2 = new Module("Module2", "org.openmrs.module1", "org.openmrs.module2", "Author2", "Description2", "2.0", "2.0");
		assertTrue(module1.equals(module2));
	}
	@Test
	public void equals_shouldReturnFalseForDifferentModuleId() {
		Module module1 = new Module("Module1", "org.openmrs.module1", "org.openmrs.module1", "Author1", "Description1", "1.0", "1.0");
		Module module2 = new Module("Module2", "org.openmrs.module2", "org.openmrs.module2", "Author2", "Description2", "2.0", "2.0");
		assertFalse(module1.equals(module2));
	}

	@Test
	public void hashCode_shouldReturnSameValueForSameModuleId() {
		Module module1 = new Module("Module1", "org.openmrs.module1", "org.openmrs.module1", "Author1", "Description1", "1.0", "1.0");
		Module module2 = new Module("Module2", "org.openmrs.module1", "org.openmrs.module2", "Author2", "Description2", "2.0", "2.0");
		assertEquals(module1.hashCode(), module2.hashCode());
	}
	//Xin: Test getModuleActivator Method
//	@Test
//	public void getModuleActivator_shouldReturnModuleActivatorInstance() {
//		Module module1 = new Module("Module1", "org.openmrs.module1", "org.openmrs.module1", "Author1", "Description1", "1.0", "1.0");
//		module1.setActivatorName("org.openmrs.module1.TestModuleActivator");
//		ModuleActivator activator = module1.getModuleActivator();
//		assertNotNull(activator);
//	}

	@Test
	public void getModuleActivator_shouldThrowExceptionWhenActivatorClassNotFound() {
		Module module = new Module("TestModule", "org.openmrs.testmodule", "org.openmrs.testmodule", "Author", "Description", "1.0", "1.0");
		module.setActivatorName("org.openmrs.module.NonExistentActivator");
		assertThrows(ModuleException.class, module::getModuleActivator);
	}
	//Xin: Test getExtensions Method
	@Test
	public void getExtensions_shouldReturnEmptyListWhenExtensionNamesIsNull() {
		Module module = new Module("TestModule");
		module.setExtensionNames(null);
		assertTrue(module.getExtensions().isEmpty());
	}

	@Test
	public void getExtensions_shouldReturnEmptyListWhenExtensionNamesIsEmpty() {
		Module module = new Module("TestModule");
		module.setExtensionNames(new IdentityHashMap<>());
		assertTrue(module.getExtensions().isEmpty());
	}

//	@Test
//	public void getExtensions_shouldExpandExtensionNamesIntoExtensions() {
//		Module module = new Module("TestModule");
//		IdentityHashMap<String, String> extensionNames = new IdentityHashMap<>();
//		extensionNames.put("point1", "org.openmrs.module.TestExtension");
//		module.setExtensionNames(extensionNames);
//		List<Extension> extensions = module.getExtensions();
//		assertTrue(extensions.isEmpty());
//		assertEquals("point1", extensions.get(0).getPointId());
//	}
// Xin: Test isCoreModule Method
//	@Test
//	public void isCoreModule_shouldReturnTrueForCoreModule() {
//		Module module = new Module("CoreModule", ModuleConstants.CORE_MODULES.keySet().iterator().next(), "org.openmrs.core", "Author", "Description", "1.0", "1.0");
//		assertTrue(module.isCoreModule());
//	}

	@Test
	public void isCoreModule_shouldReturnFalseForNonCoreModule() {
		Module module = new Module("NonCoreModule", "org.openmrs.noncore", "org.openmrs.noncore", "Author", "Description", "1.0", "1.0");
		assertFalse(module.isCoreModule());
	}
	//Xin: Test isStarted Method
//	@Test
//	public void isStarted_shouldReturnTrueWhenModuleIsStarted() {
//		Module module = new Module("StartedModule");
//		ModuleFactory.startModule(module);
//		assertTrue(module.isStarted());
//	}

	@Test
	public void isStarted_shouldReturnFalseWhenModuleIsNotStarted() {
		Module module = new Module("NotStartedModule");
		assertFalse(module.isStarted());
	}
	//Xin: Test clearStartupError Method
	@Test
	public void clearStartupError_shouldClearStartupErrorMessage() {
		Module module = new Module("TestModule");
		module.setStartupErrorMessage("Error occurred");
		assertTrue(module.hasStartupError());
		module.clearStartupError();
		assertFalse(module.hasStartupError());
		assertNull(module.getStartupErrorMessage());
	}
	//Xin: Test getModuleIdAsPath Method
	@Test
	public void getModuleIdAsPath_shouldReplaceDotsWithSlashes() {
		Module module = new Module("TestModule", "org.openmrs.testmodule", "org.openmrs.testmodule", "Author", "Description", "1.0", "1.0");
		assertEquals("org/openmrs/testmodule", module.getModuleIdAsPath());
	}
	//Xin: Test setMandatory and isMandatory Methods
	@Test
	public void isMandatory_shouldReturnTrueWhenModuleIsMandatory() {
		Module module = new Module("MandatoryModule");
		module.setMandatory(true);
		assertTrue(module.isMandatory());
	}

	@Test
	public void isMandatory_shouldReturnFalseWhenModuleIsNotMandatory() {
		Module module = new Module("NonMandatoryModule");
		module.setMandatory(false);
		assertFalse(module.isMandatory());
	}
	//Xin: Test getConditionalResources and setConditionalResources Methods
	@Test
	public void getConditionalResources_shouldReturnSetConditionalResources() {
		Module module = new Module("TestModule");
		List<ModuleConditionalResource> conditionalResources = new ArrayList<>();
		conditionalResources.add(new ModuleConditionalResource());
		module.setConditionalResources(conditionalResources);
		assertEquals(conditionalResources, module.getConditionalResources());
	}
	//Xin: Test toString Method
	@Test
	public void toString_shouldReturnModuleIdWhenModuleIdIsNotNull() {
		Module module = new Module("TestModule", "org.openmrs.testmodule", "org.openmrs.testmodule", "Author", "Description", "1.0", "1.0");
		assertEquals("org.openmrs.testmodule", module.toString());
	}

	@Test
	public void toString_shouldReturnSuperToStringWhenModuleIdIsNull() {
		Module module = new Module("TestModule");
		module.setModuleId(null);
		assertNotNull(module.toString());
	}
}
