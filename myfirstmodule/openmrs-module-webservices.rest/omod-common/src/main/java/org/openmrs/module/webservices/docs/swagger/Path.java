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

//Holds the relative path to the individual endpoints. The path is appended to the basePath in order to construct the full URL
public class Path {
	
	//Describes the operations available on a single path
	private Map<String, Operation> operations;
	
	public Path() {
		
	}
	
	/**
	 * @return the operation
	 */
	@JsonAnyGetter
	public Map<String, Operation> getOperations() {
		return operations;
	}
	
	/**
	 * @param operation the operation to set
	 */
	public void setOperations(Map<String, Operation> operations) {
		this.operations = operations;
	}
	
}
