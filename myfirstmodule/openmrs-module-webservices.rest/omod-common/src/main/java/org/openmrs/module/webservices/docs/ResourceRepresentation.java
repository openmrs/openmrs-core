/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.docs;

import java.util.ArrayList;
import java.util.Collection;

/**
 * This class stands for a resource representation.
 */
public class ResourceRepresentation {
	
	private String name;
	
	private Collection<String> properties = new ArrayList<String>();
	
	public ResourceRepresentation(String name, Collection<String> properties) {
		setName(name);
		setProperties(properties);
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public Collection<String> getProperties() {
		return properties;
	}
	
	public void setProperties(Collection<String> properties) {
		this.properties = properties;
	}
	
	@Override
	public String toString() {
				
		String text = null;
		
		for (String property : properties) {
			if (text == null)
				text = "";
			else
				text += System.getProperty("line.separator");
			
			text += property;
		}
		
		return text;
	}
	
}
