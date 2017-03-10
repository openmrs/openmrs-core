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

import org.openmrs.User;
import org.openmrs.Voidable;
import org.openmrs.annotation.Handler;
import org.openmrs.aop.RequiredDataAdvice;

/**
 * This is the super interface for all void* actions that take place on all services. The
 * {@link RequiredDataAdvice} class uses AOP around each method in every service to check to see if
 * its a void* method. If it is a void* method, this class is called to handle setting the
 * {@link Voidable#getVoided()}, {@link Voidable#setVoidReason(String)},
 * {@link Voidable#setVoidedBy(User)}, and {@link Voidable#setDateVoided(Date)}. <br>
 * <br>
 * Child collections on this {@link Voidable} that are themselves a {@link Voidable} are looped over
 * and also voided by the {@link RequiredDataAdvice} class.<br>
 * <br>
 * This class will only set the voidedBy and dateVoided attributes if voided is set to false. If
 * voided is set to true it is assumed that this object is in a list of things that is getting
 * voided but that it itself was previously voided. The workaround to this is that if the voided bit
 * is true OR the voidedBy is null, the voidedBy, dateVoided, and voidReason will be set.
 * 
 * @see RequiredDataAdvice
 * @see UnvoidHandler
 * @since 1.5
 */
@Handler(supports = Voidable.class)
public class BaseVoidHandler implements VoidHandler<Voidable> {
	
	/**
	 * Sets all void attributes to the given parameters.
	 * 
	 * @see org.openmrs.api.handler.RequiredDataHandler#handle(org.openmrs.OpenmrsObject,
	 *      org.openmrs.User, java.util.Date, java.lang.String)
	 * @should set the voided bit
	 * @should set the voidReason
	 * @should set voidedBy
	 * @should not set voidedBy if non null
	 * @should set dateVoided
	 * @should not set dateVoided if non null
	 * @should not set the voidReason if already voided
	 * @should set voidedBy even if voided bit is set but voidedBy is null
	 */
	@Override
	public void handle(Voidable voidableObject, User voidingUser, Date voidedDate, String voidReason) {
		
		// skip over all work if the object is already voided
		if (!voidableObject.getVoided() || voidableObject.getVoidedBy() == null) {
			
			voidableObject.setVoided(true);
			voidableObject.setVoidReason(voidReason);
			
			if (voidableObject.getVoidedBy() == null) {
				voidableObject.setVoidedBy(voidingUser);
			}
			if (voidableObject.getDateVoided() == null) {
				voidableObject.setDateVoided(voidedDate);
			}
		}
	}
	
}
