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

import org.apache.commons.lang3.StringUtils;
import org.openmrs.Cohort;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.Order;
import org.openmrs.Patient;
import org.openmrs.User;
import org.openmrs.Voidable;
import org.openmrs.annotation.Handler;
import org.openmrs.aop.RequiredDataAdvice;

/**
 * This class ensures that the voidReason is non-null for supported object types
 * 
 * @see RequiredDataAdvice
 * @see UnvoidHandler
 * @since 1.5
 */
@Handler(supports = { Patient.class, Encounter.class, Obs.class, Cohort.class, Order.class }, order = 1 /* low order so this is run first */)
public class RequireVoidReasonVoidHandler implements VoidHandler<Voidable> {
	
	/**
	 * Validates that the voidReason is non-null and non-empty for supported objects
	 * 
	 * <strong>Should</strong> throw IllegalArgumentException if Patient voidReason is null
	 * <strong>Should</strong> throw IllegalArgumentException if Encounter voidReason is empty
	 * <strong>Should</strong> throw IllegalArgumentException if Obs voidReason is blank
	 * <strong>Should</strong> not throw Exception if voidReason is not blank
	 * <strong>Should</strong> not throw Exception if voidReason is null for unsupported types
	 */
	@Override
	public void handle(Voidable voidableObject, User voidingUser, Date voidedDate, String voidReason) {
		
		if (StringUtils.isBlank(voidReason)) {
			throw new IllegalArgumentException("The 'reason' argument is required");
		}
	}
	
}
