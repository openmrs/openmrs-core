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

import org.openmrs.Retireable;
import org.openmrs.User;
import org.openmrs.aop.RequiredDataAdvice;

/**
 * This is the super interface for all unretire* actions that take place on all services. The
 * {@link RequiredDataAdvice} class uses AOP around each method in every service to check to see if
 * its a unretire* method. If it is a unretire* method, this class is called to handle setting the
 * {@link Retireable#isRetired()}, {@link Retireable#setRetireReason(String)},
 * {@link Retireable#setRetiredBy(User)}, and {@link Retireable#setDateRetired(Date)} all to null. <br/>
 * <br/>
 * Child collections on this {@link Retireable} that are themselves a {@link Retireable} are looped
 * over and also unretired by the {@link RequiredDataAdvice} class. <br/>
 * <br/>
 * 
 * @see BaseUnretireHandler
 * @see RequiredDataAdvice
 * @see RetireHandler
 * @since 1.5
 */
public interface UnretireHandler<R extends Retireable> extends RequiredDataHandler<R> {
	
	/**
	 * Called around every unretire* method to set {@link Retireable} attributes to null.<br/>
	 * <br/>
	 * 
	 * @see org.openmrs.api.handler.RequiredDataHandler#handle(org.openmrs.OpenmrsObject,
	 *      org.openmrs.User, java.util.Date, java.lang.String)
	 */
	public void handle(R retireableObject, User retiringUser, Date origParentRetiredDate, String unused);
	
}
