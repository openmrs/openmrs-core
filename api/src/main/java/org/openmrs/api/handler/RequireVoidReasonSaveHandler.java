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

import org.apache.commons.lang.StringUtils;
import org.openmrs.Encounter;
import org.openmrs.Patient;
import org.openmrs.User;
import org.openmrs.Voidable;
import org.openmrs.annotation.Handler;
import org.openmrs.aop.RequiredDataAdvice;
import org.openmrs.api.APIException;

/**
 * This handler makes sure the when a voided object is saved, that the reason field is entered for
 * supported objects Note: Obs is not included in the supported types because the ObsService handles
 * this explicitly
 * 
 * @see RequiredDataAdvice
 * @see SaveHandler
 * @see RequiredDataAdvice
 * @since 1.5
 */
@Handler(supports = { Patient.class, Encounter.class })
public class RequireVoidReasonSaveHandler implements SaveHandler<Voidable> {
	
	/**
	 * Validates that the voidReason is non-null and non-empty for supported objects
	 * 
	 * @should throw APIException if Patient voidReason is null
	 * @should throw APIException if Encounter voidReason is empty
	 * @should throw APIException if Encounter voidReason is blank
	 * @should not throw Exception if voidReason is not blank
	 * @should not throw Exception if voidReason is null for unsupported types
	 */
	public void handle(Voidable voidableObject, User currentUser, Date currentDate, String notUsed) {
		
		if (voidableObject.isVoided() && StringUtils.isBlank(voidableObject.getVoidReason())) {
			throw new APIException("voided.bit.was.set.true", new Object[] { voidableObject, voidableObject.getClass() });
		}
	}
	
}
