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
package org.openmrs.web.dwr;

import org.openmrs.api.context.Context;

/**
 *
 */
public class DWRAdministrationService {
	
	/**
	 * Gets the value of a global property
	 * 
	 * @param name
	 * @return
	 */
	public String getGlobalProperty(String name) {
		return Context.getAdministrationService().getGlobalProperty(name);
	}
	
	/**
	 * Sets the value of a global property
	 * 
	 * @param name
	 * @param newValue
	 */
	public void setGlobalProperty(String name, String newValue) {
		Context.getAdministrationService().setGlobalProperty(name, newValue);
	}
	
}
