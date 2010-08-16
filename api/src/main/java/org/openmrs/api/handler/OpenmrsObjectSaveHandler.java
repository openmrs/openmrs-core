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
import java.util.UUID;

import org.openmrs.OpenmrsObject;
import org.openmrs.User;
import org.openmrs.annotation.Handler;
import org.openmrs.aop.RequiredDataAdvice;

/**
 * This class deals with any object that implements {@link OpenmrsObject}. When an
 * {@link OpenmrsObject} is saved (via a save* method in a service), this handler is automatically
 * called by the {@link RequiredDataAdvice} AOP class. <br/>
 * <br/>
 * This class sets the uuid property on the given OpenmrsObject to a randomly generated <a
 * href="http://wikipedia.org/wiki/UUID">UUID</a> if it is non-null.
 * 
 * @see RequiredDataHandler
 * @see SaveHandler
 * @since 1.5
 */
@Handler(supports = OpenmrsObject.class)
public class OpenmrsObjectSaveHandler implements SaveHandler<OpenmrsObject> {
	
	/**
	 * This sets the uuid property on the given OpenmrsObject if it is non-null.
	 * 
	 * @see org.openmrs.api.handler.RequiredDataHandler#handle(org.openmrs.OpenmrsObject,
	 *      org.openmrs.User, java.util.Date, java.lang.String)
	 */
	public void handle(OpenmrsObject openmrsObject, User creator, Date dateCreated, String reason) {
		if (openmrsObject.getUuid() == null)
			openmrsObject.setUuid(UUID.randomUUID().toString());
	}
	
}
