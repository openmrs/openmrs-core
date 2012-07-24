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
import org.openmrs.aop.RequiredDataAdvice;

/**
 * This is the super interface for all save*, void*, and retire* actions that take place on all
 * services. The {@link RequiredDataAdvice} uses AOP around each method to call all handlers that a
 * certain interface. Having this superinterface makes it easier to have one method for this.
 * 
 * @see RequiredDataAdvice#recursivelyHandle(Class, OpenmrsObject, User, Date, String)
 * @see RequiredDataAdvice
 * @see SaveHandler
 * @see VoidHandler
 * @see RetireHandler
 * @since 1.5
 */
public interface RequiredDataHandler<O extends OpenmrsObject> {
	
	/**
	 * This method is called to when the required data needs to be set.
	 * 
	 * @param openmrsObject an {@link OpenmrsObject} that needs to have some required data set
	 * @param currentUser the currently authenticated {@link User}
	 * @param currentDate the current {@link Date}
	 * @param other (optional) would be the second argument in the save/void/unvoid/etc method, if
	 *            exists
	 */
	public void handle(O openmrsObject, User currentUser, Date currentDate, String other);
	
}
