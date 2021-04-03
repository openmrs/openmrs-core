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
 * This handler makes sure the when a voided object is saved with the voided bit set to true, the
 * other required values (like dateVoided and voidedBy) are filled in. It also makes sure the voided
 * attributes are cleared out if the voided bit is set to false. <br>
 * <br>
 * The {@link RequiredDataAdvice} class uses AOP around each method in every service to check to see
 * if its a save* method. If it is a save* method, and the object being saved implements
 * {@link Voidable}, this class is called to handle setting the {@link Voidable#setVoidedBy(User)},
 * and {@link Voidable#setDateVoided(Date)} if not set already. <br>
 * <br>
 * Note: The {@link RequiredDataAdvice} class will loop over child collections on this
 * {@link Voidable} that are themselves a {@link Voidable} and voidedBy/dateVoided are set, but
 * <b>ONLY IF</b> the voided bit was set on them as well. Using the associated void* method in the
 * service on the parent instance is preferred so that all child objects are indeed voided.
 * 
 * @see RequiredDataAdvice
 * @see SaveHandler
 * @see RequiredDataAdvice
 * @since 1.5
 */
@Handler(supports = Voidable.class)
public class VoidSaveHandler implements SaveHandler<Voidable> {
	
	/**
	 * This method does not set "voided" to true, but rather only sets the voidedBy/dateVoided if
	 * they are null and voided==true. <br>
	 * <br>
	 * If voided is set to false, the voided attributes are cleared nullified.
	 * 
	 * @see org.openmrs.api.handler.RequiredDataHandler#handle(org.openmrs.OpenmrsObject,
	 *      org.openmrs.User, java.util.Date, java.lang.String)
	 * <strong>Should</strong> not set the voided bit
	 * <strong>Should</strong> not set the voidReason
	 * <strong>Should</strong> set voided by
	 * <strong>Should</strong> not set voided by if non null
	 * <strong>Should</strong> set dateVoided
	 * <strong>Should</strong> not set dateVoided if non null
	 * <strong>Should</strong> not set the dateVoided if voided is false
	 * <strong>Should</strong> set voidReason to null if voided is true
	 * <strong>Should</strong> set dateVoided to null if voided is true
	 * <strong>Should</strong> set voidedBy to null if voided is true
	 */
	@Override
	public void handle(Voidable voidableObject, User currentUser, Date currentDate, String notUsed) {
		
		// void reason is not set here, it should be set prior to this method
		
		// only set the values if the user saved this object and set the voided bit
		if (voidableObject.getVoided()) {
			
			if (voidableObject.getVoidedBy() == null) {
				voidableObject.setVoidedBy(currentUser);
			}
			if (voidableObject.getDateVoided() == null) {
				voidableObject.setDateVoided(currentDate);
			}
		} else {
			// voided is set to false
			voidableObject.setVoidedBy(null);
			voidableObject.setDateVoided(null);
			voidableObject.setVoidReason(null);
		}
		
	}
	
}
