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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.openmrs.GlobalProperty;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;

/**
 * Consists of the tests for the methods in the location utility class
 */
public class LocationUtilityTest extends BaseContextSensitiveTest {
	
	/**
	 * @see LocationUtility#getDefaultLocation()
	 */
	@Test
	public void getDefaultLocation_shouldReturnTheUpdatedDefaultLocationWhenTheValueOfTheGlobalPropertyIsChanged()
	{
		//sanity check
		assertEquals("Unknown Location", LocationUtility.getDefaultLocation().getName());
		GlobalProperty gp = new GlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_DEFAULT_LOCATION_NAME, "Xanadu", "Testing");
		Context.getAdministrationService().saveGlobalProperty(gp);
		assertEquals("Xanadu", LocationUtility.getDefaultLocation().getName());
	}
	
	/**
	 * @see LocationUtility#getUserDefaultLocation()
	 */
	@Test
	public void getUserDefaultLocation_shouldReturnTheUserSpecifiedLocationIfAnyIsSet() {
		//sanity check
		assertNull(LocationUtility.getUserDefaultLocation());
		
		User user = Context.getAuthenticatedUser();
		Map<String, String> properties = user.getUserProperties();
		properties.put(OpenmrsConstants.USER_PROPERTY_DEFAULT_LOCATION, "2");
		user.setUserProperties(properties);
		Context.getUserService().saveUser(user);
		
		Context.logout();
		authenticate();
		
		assertEquals("Xanadu", LocationUtility.getUserDefaultLocation().getName());
	}
}
