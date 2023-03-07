/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.web.resource.api;

public class SearchParameter {
	
	String name;
	
	String value;
	
	public SearchParameter(String name) {
		this.name = name;
		this.value = null;
	}
	
	public SearchParameter(String name, String value) {
		this.name = name;
		this.value = value;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getValue() {
		return value;
	}
	
	public void setValue(String value) {
		this.value = value;
	}
	
	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || !(o instanceof SearchParameter))
			return false;
		
		SearchParameter that = (SearchParameter) o;
		
		if (!name.equals(that.name))
			return false;
		return value != null ? value.equals(that.value) : that.value == null;
		
	}
	
	@Override
	public int hashCode() {
		int result = name.hashCode();
		result = 31 * result + (value != null ? value.hashCode() : 0);
		return result;
	}
}
