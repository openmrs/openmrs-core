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
 * This is the super interface for all unvoid* actions that take place on all services. The
 * {@link RequiredDataAdvice} class uses AOP around each method in every service to check to see if
 * its a unvoid* method. If it is a unvoid* method, this class is called to handle setting the
 * {@link Voidable#getVoided()}, {@link Voidable#setVoidReason(String)},
 * {@link Voidable#setVoidedBy(User)}, and {@link Voidable#setDateVoided(Date)} all to null. <br>
 * <br>
 * Child collections on this {@link Voidable} that are themselves a {@link Voidable} are looped over
 * and also unvoided by the {@link RequiredDataAdvice} class.<br>
 * <br>
 * 
 * @see RequiredDataAdvice
 * @see VoidHandler
 * @since 1.5
 */
@Handler(supports = {Voidable.class}, order = 10)
public class BaseUnvoidHandler implements UnvoidHandler<Voidable> {
	
	/**
	 * Called around every unvoid* method to set everything to null.<br>
	 * <br>
	 * 
	 * @see org.openmrs.api.handler.RequiredDataHandler#handle(org.openmrs.OpenmrsObject,
	 *      org.openmrs.User, java.util.Date, java.lang.String)
	 * @should unset the voided bit
	 * @should unset the voider
	 * @should unset the dateVoided
	 * @should unset the voidReason
	 * @should only act on already voided objects
	 * @should not act on objects with a different dateVoided
	 */
	@Override
	public void handle(Voidable voidableObject, User voidingUser, Date origParentVoidedDate, String unused) {
		
		// only operate on voided objects
		if (voidableObject.getVoided()
		        && (origParentVoidedDate == null || origParentVoidedDate.equals(voidableObject.getDateVoided()))) {
			
			// only unvoid objects that were voided at the same time as the parent object
			voidableObject.setVoided(false);
			voidableObject.setVoidedBy(null);
			voidableObject.setDateVoided(null);
			voidableObject.setVoidReason(null);
		}
	}
	
}
