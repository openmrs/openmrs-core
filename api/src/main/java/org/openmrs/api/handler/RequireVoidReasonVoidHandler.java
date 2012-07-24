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
import org.openmrs.Cohort;
import org.openmrs.Encounter;
import org.openmrs.Obs;
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
@Handler(supports = { Patient.class, Encounter.class, Obs.class, Cohort.class }, order = 1 /* low order so this is run first */)
public class RequireVoidReasonVoidHandler implements VoidHandler<Voidable> {
	
	/**
	 * Validates that the voidReason is non-null and non-empty for supported objects
	 * 
	 * @should throw IllegalArgumentException if Patient voidReason is null
	 * @should throw IllegalArgumentException if Encounter voidReason is empty
	 * @should throw IllegalArgumentException if Obs voidReason is blank
	 * @should not throw Exception if voidReason is not blank
	 * @should not throw Exception if voidReason is null for unsupported types
	 */
	public void handle(Voidable voidableObject, User voidingUser, Date voidedDate, String voidReason) {
		
		if (StringUtils.isBlank(voidReason)) {
			throw new IllegalArgumentException("The 'reason' argument is required");
		}
	}
	
}
