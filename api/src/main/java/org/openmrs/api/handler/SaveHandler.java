/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
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
