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
import org.openmrs.GlobalProperty;
import org.openmrs.api.context.Context;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.test.Verifies;

/**
 * Consists of the tests for the methods in the location utility class
 */
public class LocationUtilityTest extends BaseContextSensitiveTest {
	
	/**
	 * @see {@link LocationUtility#getDefaultLocation()}
	 */
	@Test
	@Verifies(value = "should return the updated defaultLocation when the value of the global property is changed", method = "getDefaultLocation()")
	public void getDefaultLocation_shouldReturnTheUpdatedDefaultLocationWhenTheValueOfTheGlobalPropertyIsChanged()
	        throws Exception {
		//sanity check
		Assert.assertEquals("Unknown Location", LocationUtility.getDefaultLocation().getName());
		GlobalProperty gp = new GlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_DEFAULT_LOCATION_NAME, "Xanadu", "Testing");
		Context.getAdministrationService().saveGlobalProperty(gp);
		Assert.assertEquals("Xanadu", LocationUtility.getDefaultLocation().getName());
	}
}
