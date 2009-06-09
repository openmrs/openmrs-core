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
package org.openmrs.web.controller.maintenance;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.GlobalProperty;
import org.openmrs.api.context.Context;
import org.openmrs.module.Module;
import org.openmrs.module.ModuleFactory;

/**
 *
 */
public class Namespace {

	/** Logger for this class */
	protected final Log log = LogFactory.getLog(getClass());

	private String name;

	private List<GlobalProperty> globalProperties;
	
	public Namespace(String name) {
		setName(name);
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
    	this.globalProperties = Context.getAdministrationService().getGlobalPropertiesByPrefix(name);
    }

	
    /**
     * @return the globalProperties
     */
    public List<GlobalProperty> getGlobalProperties() {
    	return globalProperties;
    }
	
	
	
}
