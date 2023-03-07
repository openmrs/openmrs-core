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

import java.util.ArrayList;
import java.util.List;

/*An object that hold data types that can be consumed and produced by operations. These data types can be primitives, arrays or models.*/
public class Definition {
	
	private String type;
	
	private List<String> required;
	
	private Properties properties;
	
	private Xml xml;
	
	public Definition() {
		required = new ArrayList<String>();
		properties = new Properties();
	}
	
	public String getType() {
		return type;
	}
	
	/**
	 * @param type the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}
	
	/**
	 * @return the required
	 */
	public List<String> getRequired() {
		return required;
	}
	
	/**
	 * @param required the required to set
	 */
	public void setRequired(List<String> required) {
		this.required = required;
	}
	
	public void addRequired(String property) {
		if (!required.contains(property)) {
			required.add(property);
		}
	}
	
	/**
	 * @return the properties
	 */
	public Properties getProperties() {
		return properties;
	}
	
	/**
	 * @param properties the properties to set
	 */
	public void setProperties(Properties properties) {
		this.properties = properties;
	}
	
	public Xml getXml() {
		return xml;
	}
	
	public void setXml(Xml xml) {
		this.xml = xml;
	}
}
