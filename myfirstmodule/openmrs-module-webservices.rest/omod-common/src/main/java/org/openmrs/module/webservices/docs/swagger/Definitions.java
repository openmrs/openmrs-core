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

//List of Definitions
public class Definitions {
	
	private Map<String, Definition> definitions;
	
	public Definitions() {
		
	}
	
	/**
	 * @return the definitions
	 */
	@JsonAnyGetter
	public Map<String, Definition> getDefinitions() {
		return definitions;
	}
	
	/**
	 * @param definitions the definitions to set
	 */
	public void setDefinitions(Map<String, Definition> definitions) {
		this.definitions = definitions;
	}
	
}
