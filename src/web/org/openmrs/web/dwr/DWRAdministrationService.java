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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;
import org.openmrs.GlobalProperty;
import org.openmrs.api.context.Context;

/**
 *
 */
public class DWRAdministrationService {
	
	private static final Logger log = Logger.getLogger(DWRAdministrationService.class);
	
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
	 * Sets the type and the value of a global property
	 * 
	 * @param namespace
	 * @param name
	 * @param type
	 * @param value
	 * @param description
	 */
	public void setGlobalProperty(String namespace, String name, String type, String value, String description) {

		GlobalProperty globalProperty = new GlobalProperty();

		if (namespace != "") {
			globalProperty.setProperty(namespace + "." + name);
		} else {
			globalProperty.setProperty(name);
		}

		globalProperty.setDefaultPropertyType(type);
		globalProperty.setPropertyType(type);

		log.debug("Given value: " + value);
		
		globalProperty.setDefaultPropertyValue(value);
		globalProperty.setPropertyValue(value);

		globalProperty.setDescription(description);
		globalProperty.setCreator(Context.getAuthenticatedUser());
		globalProperty.setDateCreated(new Date());

		Context.getAdministrationService().saveGlobalProperty(globalProperty);
	}
}
