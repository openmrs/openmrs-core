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
 * This is the default class for all retire* actions that take place on all services. The
 * {@link RequiredDataAdvice} class uses AOP around each method in every service to check to see if
 * its a retire* method. If it is a retire* method, this class is called to handle setting the
 * {@link Retireable#isRetired()}, {@link Retireable#setRetireReason(String)},
 * {@link Retireable#setRetiredBy(User)}, and {@link Retireable#setDateRetired(Date)}. <br/>
 * <br/>
 * Child collections on this {@link Retireable} that are themselves a {@link Retireable} are looped
 * over and also retired by the {@link RequiredDataAdvice} class.<br/>
 * <br/>
 * This class will only set the retiredBy and dateRetired attributes if retired is set to false. If
 * retired is set to true it is assumed that this object is in a list of things that is getting
 * retired but that it itself was previously retired. The workaround to this is that if the retired
 * bit is true OR the retiredBy is null, the retiredBy, dateRetired, and retireReason will be set.
 * 
 * @see RequiredDataAdvice
 * @since 1.5
 */
@Handler(supports = Retireable.class)
public class BaseRetireHandler implements RetireHandler<Retireable> {
	
	/**
	 * This method sets "retired" to true, the retired reason, and the retiredBy/dateRetired (if
	 * those are null).<br/>
	 * 
	 * @see org.openmrs.api.handler.RequiredDataHandler#handle(org.openmrs.OpenmrsObject,
	 *      org.openmrs.User, java.util.Date, java.lang.String)
	 * @should set the retired bit
	 * @should set the retireReason
	 * @should set retired by
	 * @should not set retired by if non null
	 * @should set dateRetired
	 * @should not set dateRetired if non null
	 * @should not set the retireReason if already voided
	 * @should set retiredBy even if retired bit is set but retiredBy is null
	 */
	public void handle(Retireable retireableObject, User retiringUser, Date retireDate, String retireReason) {
		
		// skip over doing retire stuff if already retired
		if (!retireableObject.isRetired() || retireableObject.getRetiredBy() == null) {
			
			retireableObject.setRetired(true);
			retireableObject.setRetireReason(retireReason);
			
			if (retireableObject.getRetiredBy() == null) {
				retireableObject.setRetiredBy(retiringUser);
			}
			if (retireableObject.getDateRetired() == null) {
				retireableObject.setDateRetired(retireDate);
			}
			
		}
		
	}
	
}
