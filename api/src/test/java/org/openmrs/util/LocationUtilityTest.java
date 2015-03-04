/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.util;

import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.GlobalProperty;
import org.openmrs.User;
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
	
	/**
	 * @see {@link LocationUtility#getUserDefaultLocation()}
	 */
	@Test
	@Verifies(value = "should return the user specified location if any is set", method = "getUserDefaultLocation()")
	public void getUserDefaultLocation_shouldReturnTheUserSpecifiedLocationIfAnyIsSet() throws Exception {
		//sanity check
		Assert.assertNull(LocationUtility.getUserDefaultLocation());
		User user = Context.getAuthenticatedUser();
		Map<String, String> properties = user.getUserProperties();
		properties.put(OpenmrsConstants.USER_PROPERTY_DEFAULT_LOCATION, "2");
		user.setUserProperties(properties);
		Context.getUserService().saveUser(user, null);
		Context.refreshAuthenticatedUser();
		Assert.assertEquals("Xanadu", LocationUtility.getUserDefaultLocation().getName());
	}
}
