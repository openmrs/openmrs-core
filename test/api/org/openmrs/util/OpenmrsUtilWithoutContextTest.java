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
package org.openmrs.util;

import org.junit.Test;
import org.openmrs.test.Verifies;

/**
 * Tests the methods in {@link OpenmrsUtil} TODO: finish adding tests for all methods
 */
public class OpenmrsUtilWithoutContextTest {
	
	/**
	 * @see {@link OpenmrsUtil#validatePassword(String,String,String)}
	 */
	@Test
	@Verifies(value = "should still work without an open session", method = "validatePassword(String,String,String)")
	public void validatePassword_shouldStillWorkWithoutAnOpenSession() throws Exception {
		OpenmrsUtil.validatePassword("admin", "1234Password", "systemId");
	}
}
