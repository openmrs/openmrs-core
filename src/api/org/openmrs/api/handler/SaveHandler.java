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

import org.openmrs.OpenmrsObject;
import org.openmrs.User;
import org.openmrs.annotation.Handler;
import org.openmrs.aop.RequiredDataAdvice;

/**
 * This is the super interface for all save* actions that take place on all services. AOP is used
 * around each method to see if its a save* method. If it is, then all handlers that implement this
 * class (that also {@link Handler#supports() support} the current object being saved) are called.
 * 
 * @see RequiredDataAdvice
 * @see OpenmrsObjectSaveHandler
 * @see AuditableSaveHandler
 * @see ConceptNameSaveHandler
 * @since 1.5
 */
public interface SaveHandler<O extends OpenmrsObject> extends RequiredDataHandler<O> {
	
	/**
	 * This method is used by the implementing classes to set any required data that it needs to.
	 * 
	 * @param object an OpenmrsObject that needs to have some required data set
	 * @param creator the user who is saving this object
	 * @param dateCreated the datetime this object is being saved
	 * @param other (optional) would be the second argument in the save method, if exists
	 * @see org.openmrs.api.handler.RequiredDataHandler#handle(org.openmrs.OpenmrsObject,
	 *      org.openmrs.User, java.util.Date, java.lang.String)
	 */
	public void handle(O object, User creator, Date dateCreated, String other);
	
}
