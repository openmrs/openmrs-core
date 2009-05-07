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

import org.openmrs.User;
import org.openmrs.Voidable;
import org.openmrs.aop.RequiredDataAdvice;

/**
 * This is the super interface for all void* actions that take place on all services. The
 * {@link RequiredDataAdvice} class uses AOP around each method in every service to check to see if
 * its a void* method. If it is a void* method, this class is called to handle setting the
 * {@link Voidable#isVoided()}, {@link Voidable#setVoidReason(String)},
 * {@link Voidable#setVoiddBy(User)}, and {@link Voidable#setDateVoidd(Date)}. <br/>
 * <br/>
 * Child collections on this {@link Voidable} that are themselves a {@link Voidable} are looped over
 * and also voided by the {@link RequiredDataAdvice} class.
 * 
 * @see RequiredDataAdvice
 * @see UnvoidHandler
 * @since 1.5
 */
public interface VoidHandler<V extends Voidable> extends RequiredDataHandler<V> {
	
	/**
	 * Implementing classes should set all void attributes to the given parameters.
	 * 
	 * @see org.openmrs.api.handler.RequiredDataHandler#handle(org.openmrs.OpenmrsObject,
	 *      org.openmrs.User, java.util.Date, java.lang.String)
	 */
	public void handle(V voidableObject, User voidingUser, Date voidedDate, String voidReason);
}
