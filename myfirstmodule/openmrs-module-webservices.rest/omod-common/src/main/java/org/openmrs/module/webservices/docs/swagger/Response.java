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

/* An object to hold responses to be reused across operations */
public class Response {
	
	private String description;
	
	private Schema schema;
	
	public Response() {
		
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
	 * @return the schema
	 */
	public Schema getSchema() {
		return schema;
	}
	
	/**
	 * @param schema the schema to set
	 */
	public void setSchema(Schema schema) {
		this.schema = schema;
	}
}
