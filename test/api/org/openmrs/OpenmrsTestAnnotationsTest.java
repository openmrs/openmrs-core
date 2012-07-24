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
package org.openmrs;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.api.context.Context;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.test.SkipBaseSetup;

/**
 * Runs tests on the "@SkipBaseSetup" annotation that OpenMRS unit tests use.
 */
public class OpenmrsTestAnnotationsTest extends BaseContextSensitiveTest {
	
	/**
	 * Make sure the "@Before" method on {@link BaseContextSensitiveTest} is authenticating the user
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldCallBaseSetupMethod() throws Exception {
		// make sure we're authenticated
		Assert.assertTrue(Context.isAuthenticated());
		// make sure we have some data from the EXAMPLE_XML_DATASET_PACKAGE_PATH
		Assert.assertTrue(Context.getEncounterService().getAllEncounterTypes().size() > 0);
		// make sure we have the data from the INITIAL_DATA_SET_XML_FILENAME
		Context.authenticate("admin", "test");
		
		// this is put here for the next test method to check that authentication is
		// not happening when told not to
		Context.logout();
	}
	
	/**
	 * Make sure the "@Before" method on {@link BaseContextSensitiveTest} is not authenticating the
	 * user when told to skip
	 * 
	 * @throws Exception
	 */
	@Test
	@SkipBaseSetup
	public void shouldSkipAuthentication() throws Exception {
		
		// this depends on Context.logout() being in the previous test method
		
		Assert.assertFalse(Context.isAuthenticated());
	}
	
}
