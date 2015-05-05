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
