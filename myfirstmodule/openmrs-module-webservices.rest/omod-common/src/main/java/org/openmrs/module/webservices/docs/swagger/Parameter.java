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
import com.fasterxml.jackson.annotation.JsonSetter;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * The contents of this file are subject to the OpenMRS Public License Version 1.0 (the "License");
 * you may not use this file except in compliance with the License. You may obtain a copy of the
 * License at http://license.openmrs.org Software distributed under the License is distributed on an
 * "AS IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for the
 * specific language governing rights and limitations under the License. Copyright (C) OpenMRS, LLC.
 * All Rights Reserved.
 */

/*Describes a single operation parameter.*/
public class Parameter {
	
	/* The name of the parameter. Parameter names are case sensitive.*/
	private String name;
	
	/* he location of the parameter. Possible values are "query", "header", "path", "formData" or "body".*/
	private String in;
	
	/*A brief description of the parameter. This could contain examples of use.*/
	private String description;
	
	/*Determines whether this parameter is mandatory*/
	private Boolean required;
	
	private String type;
	
	@JsonProperty("enum")
	private List<String> enumeration;
	
	private Schema schema;
	
	public Parameter() {
		type = "string"; // default to string
	}
	
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * @return the in
	 */
	public String getIn() {
		return in;
	}
	
	/**
	 * @param in the in to set
	 */
	public void setIn(String in) {
		this.in = in;
	}
	
	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}
	
	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}
	
	/**
	 * @return the required
	 */
	public Boolean getRequired() {
		return required;
	}
	
	/**
	 * @param required the required to set
	 */
	public void setRequired(Boolean required) {
		this.required = required;
	}
	
	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}
	
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
	
	public Schema getSchema() {
		return schema;
	}
	
	public void setSchema(Schema schema) {
		this.schema = schema;
	}
}
