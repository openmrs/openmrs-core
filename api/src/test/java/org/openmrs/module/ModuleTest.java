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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.IdentityHashMap;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests Module Methods
 */

public class ModuleTest {

	private Module testModule;

	@Before
	public void before() {
		testModule = new Module("test");
	}

	/*
	 * @see Module#setStartupErrorMessage(String)
	 */
	@Test(expected = ModuleException.class)
	public void setStartupErrorMessage_shouldThrowExceptionWhenMessageIsNull() {
		testModule.setStartupErrorMessage(null);
	}

	/*
	 * @see Module#setStartupErrorMessage(String, Throwable)
	 */
	@Test(expected = ModuleException.class)
	public void setStartupErrorMessage_shouldThrowExceptionWhenThrowableIsNull() {
		testModule.setStartupErrorMessage("error", null);
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
}
