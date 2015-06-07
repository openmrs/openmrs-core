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

import org.junit.Assert;
import org.openmrs.GlobalProperty;
import org.openmrs.api.AdministrationService;

public class GlobalPropertiesTestHelper {
	
	private AdministrationService administrationService;
	
	public GlobalPropertiesTestHelper(AdministrationService administrationService) {
		this.administrationService = administrationService;
	}
	
	public String setGlobalProperty(String propertyName, String propertyValue) {
		String oldPropertyValue = administrationService.getGlobalProperty(propertyName);
		
		administrationService.setGlobalProperty(propertyName, propertyValue);
		Assert.assertEquals(propertyValue, administrationService.getGlobalProperty(propertyName));
		
		return oldPropertyValue;
	}
	
	public void purgeGlobalProperty(String propertyName) {
		GlobalProperty globalProperty = administrationService.getGlobalPropertyObject(propertyName);
		
		if (globalProperty != null) {
			administrationService.purgeGlobalProperty(globalProperty);
		}
		Assert.assertNull(administrationService.getGlobalProperty(propertyName));
	}
	
}
