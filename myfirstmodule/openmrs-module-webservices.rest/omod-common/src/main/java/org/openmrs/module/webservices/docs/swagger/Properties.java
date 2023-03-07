/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.docs.swagger;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnyGetter;

// Describes properties of an object
public class Properties {
	
	private Map<String, DefinitionProperty> properties;
	
	public Properties() {
		properties = new HashMap<String, DefinitionProperty>();
	}
	
	/**
	 * @return the properties
	 */
	@JsonAnyGetter
	public Map<String, DefinitionProperty> getProperties() {
		return properties;
	}
	
	/**
	 * @param properties the properties to set
	 */
	public void setProperties(Map<String, DefinitionProperty> properties) {
		this.properties = properties;
	}
	
	public void addProperty(String propertyKey, DefinitionProperty property) {
		properties.put(propertyKey, property);
	}
	
}
