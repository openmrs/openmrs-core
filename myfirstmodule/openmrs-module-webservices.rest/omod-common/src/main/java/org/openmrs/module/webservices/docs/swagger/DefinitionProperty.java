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

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;

import java.util.ArrayList;
import java.util.List;

/*Defines the type of the property */
public class DefinitionProperty {
	
	private String type;
	
	@JsonProperty("enum")
	private List<String> enumeration;
	
	public DefinitionProperty() {
		
	}
	
	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}
	
	/**
	 * @param type the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}
	
	@JsonGetter("enum")
	public List<String> getEnumeration() {
		return this.enumeration;
	}
	
	@JsonSetter("enum")
	public void setEnumeration(List<String> enumeration) {
		this.enumeration = enumeration;
	}
	
	public void addEnumerationItem(String enumerationItem) {
		if (enumeration == null)
			enumeration = new ArrayList<String>();
		
		if (!this.enumeration.contains(enumerationItem)) {
			this.enumeration.add(enumerationItem);
		}
	}
}
