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
package org.openmrs;


/**
 *
 */
public class GlobalPropertyType {
	
	private Integer globalPropertyTypeId = 0;
	private String name = "";
	
	public GlobalPropertyType() {
	}
	
	public GlobalPropertyType(String name) {
		this.name = name;
	}
	
	public GlobalPropertyType(String name, Integer id) {
		this.name = name;
		this.globalPropertyTypeId = id;
	}
	
    /**
     * @return the globalPropertyTypeId
     */
    public Integer getGlobalPropertyTypeId() {
    	return globalPropertyTypeId;
    }
	
    /**
     * @param globalPropertyTypeId the globalPropertyTypeId to set
     */
    public void setGlobalPropertyTypeId(Integer globalPropertyTypeId) {
    	this.globalPropertyTypeId = globalPropertyTypeId;
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
	
	
}
