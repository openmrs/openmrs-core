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

import org.apache.commons.lang.StringUtils;
import org.openmrs.Encounter;
import org.openmrs.Patient;
import org.openmrs.User;
import org.openmrs.Voidable;
import org.openmrs.annotation.Handler;
import org.openmrs.aop.RequiredDataAdvice;
import org.openmrs.api.APIException;

/**
 * This handler makes sure the when a voided object is saved, that the reason
 * field is entered for supported objects
 * Note: Obs is not included in the supported types because the ObsService handles this explicitly
 * @see RequiredDataAdvice
 * @see SaveHandler
 * @see RequiredDataAdvice
 * @since 1.5
 */
@Handler(supports = {Patient.class, Encounter.class})
public class RequireVoidReasonSaveHandler implements SaveHandler<Voidable> {
	
	/**
	 * Validates that the voidReason is non-null and non-empty for supported objects
	 * @should throw APIException if Patient voidReason is null
	 * @should throw APIException if Encounter voidReason is empty
	 * @should throw APIException if Encounter voidReason is blank
	 * @should not throw Exception if voidReason is not blank
	 * @should not throw Exception if voidReason is null for unsupported types
	 */
	public void handle(Voidable voidableObject, User currentUser, Date currentDate, String notUsed) {
		
		if (voidableObject.isVoided() && StringUtils.isBlank(voidableObject.getVoidReason())) {
			throw new APIException(
				"The voided bit was set to true, so a void reason is required at save time for object: "
                + voidableObject + " of class: " + voidableObject.getClass());
		}
	}
	
}
