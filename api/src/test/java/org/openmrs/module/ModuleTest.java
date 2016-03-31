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

import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.junit.Before;

import java.util.ArrayList;
import java.util.IdentityHashMap;

/**
 * Tests Module methods
 */
public class ModuleTest {
	
	private Module mod;

	@Before
	public void setTestMod() {
		mod = new Module("test");
	}

	/**
	 * @verifies setting start up message with string only
	 * @see Module#setStartupErrorMessage(String)
	 */
	@Test
	public void setStartupErrorMessageString_shouldSucceed() throws Exception {
		String message = "test error message";

		assertFalse(mod.hasStartupError());
		mod.setStartupErrorMessage(message);
		assertTrue(mod.hasStartupError());
		
		assertEquals("test error message", mod.getStartupErrorMessage());
	}

        /**
         * @verifies setting start up message with string only throws exception when null
         * @see Module#setStartupErrorMessage(String)
         */
        @Test(expected = ModuleException.class)
        public void setStartupErrorMessageString_shouldThrowModuleException() throws Exception {
		mod.setStartupErrorMessage(null);
        }

        /**
         * @verifies setting start up message with throwable
         * @see Module#setStartupErrorMessage(String, Throwable)
         */
        @Test
        public void setStartupErrorMessageThrowable_shouldSucceed() throws Exception {
		ModuleException modException = new ModuleException("test exception error message");

		assertFalse(mod.hasStartupError());
		mod.setStartupErrorMessage(null, modException);
		assertTrue(mod.hasStartupError());

		assertEquals("test exception error message\n", mod.getStartupErrorMessage());
        }

        /**
         * @verifies setting start up message with throwable throws exception when null
         * @see Module#setStartupErrorMessage(String, Throwable)
         */
        @Test(expected = ModuleException.class)
        public void setStartupErrorMessageThrowable_shouldThrowModuleExcpetion() throws Exception {
		mod.setStartupErrorMessage(null, null);
        }

        /**
         * @verifies setting start up message with throwable and string
         * @see Module#setStartupErrorMessage(String, Throwable)
         */
        @Test
        public void setStartupErrorMessageThrowableAndString_shouldSucceedAndAppend() throws Exception {
		ModuleException modException = new ModuleException("end");
		String message = "begin";
		mod.setStartupErrorMessage(message, modException);
		assertEquals("begin\nend\n", mod.getStartupErrorMessage());
        }


	//Possible bug in setRequiredModules(List<String>), it does not handle null values

	/**
	 * @verifies setting required modules from a List with null versions
	 * @see Module#setRequiredModules(List<String>)
	 */
	@Test
	public void setRequiredModules_shouldAddRequiredModulesFromList() {
		assertNull(mod.getRequiredModules());

		ArrayList<String> first = new ArrayList<String>();
		ArrayList<String> second = new ArrayList<String>();

		first.add("mod1");
		first.add("mod2");
		second.add("mod2");
		second.add("mod3");

		mod.setRequiredModules(first);
		mod.setRequiredModules(second);

		ArrayList<String> ret = new ArrayList<String>(mod.getRequiredModules());
		assertTrue(ret.contains("mod1"));
		assertTrue(ret.contains("mod2"));
		assertTrue(ret.contains("mod3"));
		assertEquals(3, ret.size());
	}

	/*
	 * @verifies adding a required module to a null requiredModuleMap does not fail
	 * @see Module#addRequiredModule(String, String)
	 */
	@Test
	public void addRequiredModule_shouldNotFail() {
		mod.addRequiredModule("testModule", "1");
		assertNull(mod.getRequiredModuleVersion("testModule"));
	}

	//possible bug in expandExtensionsNames and getExtensions, extensions won't be 
	//expanded if they are the extensions and extensionNames are the same size, even if 
	//their contents are not related.

	//they also do not handle null values

	/*
	 * @verifies The behavior outlined above with getExtensions
	 * @see Module#getExtensions()
	 */
	@Test
	public void getExtensions_shouldNotExpand() {
		ArrayList<Extension> extensions = new ArrayList<Extension>();
		IdentityHashMap<String, String> extensionNames = new IdentityHashMap<String, String>();

		extensions.add(new TestExtension());
		extensionNames.put("point", "className");

		mod.setExtensions(extensions);
		mod.setExtensionNames(extensionNames);
		ArrayList<Extension> ret = new ArrayList<Extension>(mod.getExtensions());

		assertEquals(extensions.get(0), ret.get(0));
		assertEquals(1, ret.size());
	}

	/*
	 * @verifies disposeAdvicePointsClassInstance disposes all classInstances, but not the Advice point
	 * @see Module#disposeAdvicePointsClassInstance()
	 */
	@Test
	public void disposeAdvicePointsClassInstance_shouldDisposeClassInstancesNotAdvicePoint() {
		ArrayList<AdvicePoint> points = new ArrayList<AdvicePoint>();
		String obj1 = "string";
		ArrayList<String> obj2 = new ArrayList<String>();
		AdvicePoint point1 = new AdvicePoint("point1", obj1.getClass());
		AdvicePoint point2 = new AdvicePoint("point2", obj2.getClass());

		points.add(point1);
		points.add(point2);

		mod.setAdvicePoints(null);
		mod.disposeAdvicePointsClassInstance();

		assertEquals(obj1.getClass(), point1.getClassInstance().getClass());
		assertEquals(obj2.getClass(), point2.getClassInstance().getClass());

		mod.setAdvicePoints(points);
		mod.disposeAdvicePointsClassInstance();

		assertNotNull(point1);
		assertNotNull(point2);
		assertNull(point1.getClassInstance());
		assertNull(point2.getClassInstance());
	}

	private class TestExtension extends Extension {
		public Extension.MEDIA_TYPE getMediaType() {
			return null;
		}
	}
}
