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

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnyGetter;

//List of Available Paths. 
public class Paths {
	
	private Map<String, Path> paths;
	
	public Paths() {
		
	}
	
	/**
	 * @return the paths
	 */
	@JsonAnyGetter
	public Map<String, Path> getPaths() {
		return paths;
	}
	
	/**
	 * @param paths the paths to set
	 */
	public void setPaths(Map<String, Path> paths) {
		this.paths = paths;
	}
	
}
