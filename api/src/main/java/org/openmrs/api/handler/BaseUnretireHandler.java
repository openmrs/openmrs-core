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
import org.openmrs.annotation.Handler;
import org.openmrs.aop.RequiredDataAdvice;

/**
 * This is the super interface for all unretire* actions that take place on all services. The
 * {@link RequiredDataAdvice} class uses AOP around each method in every service to check to see if
 * its a unretire* method. If it is a unretire* method, this class is called to handle setting the
 * {@link Retireable#getRetired()}, {@link Retireable#setRetireReason(String)},
 * {@link Retireable#setRetiredBy(User)}, and {@link Retireable#setDateRetired(Date)} all to null.
 * <br>
 * <br>
 * Child collections on this {@link Retireable} that are themselves a {@link Retireable} are looped
 * over and also unretired by the {@link RequiredDataAdvice} class. <br>
 * <br>
 * 
 * @see RequiredDataAdvice
 * @see RetireHandler
 * @since 1.5
 */
@Handler(supports = Retireable.class)
public class BaseUnretireHandler implements UnretireHandler<Retireable> {
	
	/**
	 * Called around every unretire* method to set {@link Retireable} attributes to null.<br>
	 * <br>
	 * 
	 * @see org.openmrs.api.handler.RequiredDataHandler#handle(org.openmrs.OpenmrsObject,
	 *      org.openmrs.User, java.util.Date, java.lang.String)
	 * <strong>Should</strong> unset the retired bit
	 * <strong>Should</strong> unset the retirer
	 * <strong>Should</strong> unset the date retired
	 * <strong>Should</strong> unset the retire reason
	 * <strong>Should</strong> not act on already unretired objects
	 * <strong>Should</strong> not act on retired objects with a different dateRetired
	 */
	@Override
	public void handle(Retireable retireableObject, User retiringUser, Date origParentRetiredDate, String unused) {
		
		// only act on retired objects
		if (retireableObject.getRetired()
		        && (origParentRetiredDate == null || origParentRetiredDate.equals(retireableObject.getDateRetired()))) {
			// only act on retired objects that match the same date retired as the parent
			retireableObject.setRetired(false);
			retireableObject.setRetiredBy(null);
			retireableObject.setDateRetired(null);
			retireableObject.setRetireReason(null);
		}
	}
	
}
