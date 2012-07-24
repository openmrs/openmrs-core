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

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.api.APIException;

/**
 * Tests methods on the {@link OpenmrsSecurityManager} class
 */
public class OpenmrsSecurityManagerTest {

	/**
	 * @see OpenmrsSecurityManager#getCallerClass(int)
	 * @verifies get the most recently called method
	 */
	@Test
	public void getCallerClass_shouldGetTheMostRecentlyCalledMethod() throws Exception {
		OpenmrsSecurityManager openmrsSecurityManager = new OpenmrsSecurityManager();
		Class<?> callerClass = openmrsSecurityManager.getCallerClass(0);
		Assert.assertTrue("Oops, didn't get a junit type of class: " + callerClass, callerClass.getPackage().getName()
		        .contains("junit"));
	}

	/**
	 * @see OpenmrsSecurityManager#getCallerClass(int)
	 * @verifies throw an error if given a subzero call stack level
	 */
	@Test(expected = APIException.class)
	public void getCallerClass_shouldThrowAnErrorIfGivenASubzeroCallStackLevel()
			throws Exception {
		new OpenmrsSecurityManager().getCallerClass(-1);
	}
}
