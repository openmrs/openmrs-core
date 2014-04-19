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
