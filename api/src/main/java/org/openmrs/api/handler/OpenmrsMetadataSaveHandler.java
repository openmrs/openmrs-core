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
package org.openmrs.api.handler;

import java.util.Date;

import org.openmrs.OpenmrsMetadata;

import org.openmrs.User;

/**
 * Is called when any {@link OpenmrsMetadata} object is being saved. Trims out the leading and
 * trailing whitespace around the name and description
 */
public class OpenmrsMetadataSaveHandler implements SaveHandler<OpenmrsMetadata> {
	
	/**
	 * @see org.openmrs.api.handler.SaveHandler#handle(org.openmrs.OpenmrsObject, org.openmrs.User,
	 *      java.util.Date, java.lang.String)
	 * @should trim whitespace from name
	 * @should trim whitespace from description
	 */
	@Override
	public void handle(OpenmrsMetadata object, User creator, Date dateCreated, String other) {
		if (object.getName() != null) {
			object.setName(object.getName().trim());
		}
		
		if (object.getDescription() != null) {
			object.setDescription(object.getDescription().trim());
		}
	}
	
}
