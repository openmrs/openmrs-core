/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs1_9;

import java.util.UUID;

public class CustomDatatypeHandlerRepresentation {
	
	private String uuid;
	
	private String handlerClassname;
	
	private CustomDatatypeRepresentation parent;
	
	public CustomDatatypeHandlerRepresentation() {
		
	}
	
	public CustomDatatypeHandlerRepresentation(CustomDatatypeRepresentation parent, String handlerClassname) {
		this.parent = parent;
		this.handlerClassname = handlerClassname;
		this.uuid = UUID.nameUUIDFromBytes((parent.getDatatypeClassname() + handlerClassname).getBytes()).toString();
	}
	
	public String getUuid() {
		return uuid;
	}
	
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
	
	public String getHandlerClassname() {
		return handlerClassname;
	}
	
	public void setHandlerClassname(String handlerClassname) {
		this.handlerClassname = handlerClassname;
		this.uuid = UUID.nameUUIDFromBytes(handlerClassname.getBytes()).toString();
	}
	
	public CustomDatatypeRepresentation getParent() {
		return parent;
	}
	
	public void setParent(CustomDatatypeRepresentation parent) {
		this.parent = parent;
	}
	
	public String getTextToDisplay() {
		return CustomDatatypeRepresentation.prettifyClassname(handlerClassname);
	}
}
