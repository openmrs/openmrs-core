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

import org.junit.Test;
import org.junit.Before;

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

	
}
