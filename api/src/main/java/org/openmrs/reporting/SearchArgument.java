/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.reporting;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

/**
 * This class represents an argument as might be submitted from a web user interface.
 * 
 * @deprecated see reportingcompatibility module
 */
@Root
@Deprecated
public class SearchArgument {
	
	private String name;
	
	private String value;
	
	@SuppressWarnings("unchecked")
	private Class propertyClass;
	
	public SearchArgument() {
	}
	
	@SuppressWarnings("unchecked")
	public SearchArgument(String name, String value, Class propertyClass) {
		super();
		this.name = name;
		this.value = value;
		this.propertyClass = propertyClass;
	}
	
	public String toString() {
		return name + " (" + propertyClass + ") = " + value;
	}
	
	@Attribute(required = true)
	public String getName() {
		return name;
	}
	
	@Attribute(required = true)
	public void setName(String name) {
		this.name = name;
	}
	
	@Attribute(required = true)
	public String getValue() {
		return value;
	}
	
	@Attribute(required = true)
	public void setValue(String value) {
		this.value = value;
	}
	
	@SuppressWarnings("unchecked")
	@Attribute(required = true)
	public Class getPropertyClass() {
		return propertyClass;
	}
	
	@SuppressWarnings("unchecked")
	@Attribute(required = true)
	public void setPropertyClass(Class propertyClass) {
		this.propertyClass = propertyClass;
	}
	
}
