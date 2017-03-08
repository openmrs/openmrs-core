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

import org.openmrs.Retireable;
import org.openmrs.User;
import org.openmrs.aop.RequiredDataAdvice;

/**
 * This is the super interface for all unretire* actions that take place on all services. The
 * {@link RequiredDataAdvice} class uses AOP around each method in every service to check to see if
 * its a unretire* method. If it is a unretire* method, this class is called to handle setting the
 * {@link Retireable#isRetired()}, {@link Retireable#setRetireReason(String)},
 * {@link Retireable#setRetiredBy(User)}, and {@link Retireable#setDateRetired(Date)} all to null. <br>
 * <br>
 * Child collections on this {@link Retireable} that are themselves a {@link Retireable} are looped
 * over and also unretired by the {@link RequiredDataAdvice} class. <br>
 * <br>
 * 
 * @see BaseUnretireHandler
 * @see RequiredDataAdvice
 * @see RetireHandler
 * @since 1.5
 */
public interface UnretireHandler<R extends Retireable> extends RequiredDataHandler<R> {
	
	/**
	 * Called around every unretire* method to set {@link Retireable} attributes to null.<br>
	 * <br>
	 * 
	 * @see org.openmrs.api.handler.RequiredDataHandler#handle(org.openmrs.OpenmrsObject,
	 *      org.openmrs.User, java.util.Date, java.lang.String)
	 */
	@Override
	public void handle(R retireableObject, User retiringUser, Date origParentRetiredDate, String unused);
	
}
