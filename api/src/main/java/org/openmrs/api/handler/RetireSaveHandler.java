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
 * This handler makes sure the when a retired object is saved with the retired bit set to true, the
 * other required values (like dateRetired and retiredBy) are filled in. It also makes sure the
 * retired attributes are cleared out if the retired bit is set to false. <br/>
 * <br/>
 * The {@link RequiredDataAdvice} class uses AOP around each method in every service to check to see
 * if its a save* method. If it is a save* method, and the object being saved implements
 * {@link Retireable}, this class is called to handle setting the
 * {@link Retireable#setRetiredBy(User)}, and {@link Retireable#setDateRetired(Date)} if not set
 * already. <br/>
 * <br/>
 * Note: The {@link RequiredDataAdvice} class will loop over child collections on this
 * {@link Retireable} that are themselves a {@link Retireable} and retiredBy/dateRetired are set,
 * but <b>ONLY IF</b> the retired bit was set on them as well. Using the associated retire* method
 * in the service on the parent instance is preferred so that all child objects are indeed retired.
 * 
 * @see RequiredDataAdvice
 * @see SaveHandler
 * @see RequiredDataAdvice
 * @since 1.5
 */
@Handler(supports = Retireable.class)
public class RetireSaveHandler implements SaveHandler<Retireable> {
	
	/**
	 * This method does not set "retired" to true, but rather only sets the retiredBy/dateRetired if
	 * they are null and retired==true. <br/>
	 * <br/>
	 * If retired is set to false, the retired attributes are cleared nullified.
	 * 
	 * @see org.openmrs.api.handler.RequiredDataHandler#handle(org.openmrs.OpenmrsObject,
	 *      org.openmrs.User, java.util.Date, java.lang.String)
	 * @should not set the retired bit
	 * @should not set the retireReason
	 * @should set retired by
	 * @should not set retired by if non null
	 * @should set dateRetired
	 * @should not set dateRetired if non null
	 * @should not set the dateRetired if retired is false
	 * @should set retireReason to null if retired is true
	 * @should set dateRetired to null if retired is true
	 * @should set retiredBy to null if retired is true
	 */
	public void handle(Retireable retireableObject, User currentUser, Date currentDate, String notUsed) {
		
		// retire reason is not set here, it should be set prior to this method
		
		// only set the values if the user saved this object and set the retired bit
		if (retireableObject.isRetired()) {
			
			if (retireableObject.getRetiredBy() == null) {
				retireableObject.setRetiredBy(currentUser);
			}
			if (retireableObject.getDateRetired() == null) {
				retireableObject.setDateRetired(currentDate);
			}
		} else {
			// retired is set to false
			retireableObject.setRetiredBy(null);
			retireableObject.setDateRetired(null);
			retireableObject.setRetireReason(null);
		}
		
	}
	
}
