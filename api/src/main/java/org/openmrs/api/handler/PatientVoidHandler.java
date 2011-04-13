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
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.User;
import org.openmrs.Voidable;
import org.openmrs.annotation.Handler;
import org.openmrs.aop.RequiredDataAdvice;
import org.openmrs.api.context.Context;
import org.openmrs.util.HandlerUtil;

/**
 * This class deals with {@link Patient} objects when they are voided via a void* method in an
 * Openmrs Service. This handler is automatically called by the {@link RequiredDataAdvice} AOP
 * class. <br/>
 * 
 * @see RequiredDataHandler
 * @see SaveHandler
 * @see Patient
 * @since 1.9
 */
@Handler(supports = Patient.class)
public class PatientVoidHandler implements VoidHandler<Patient> {
	
	/**
	 * @see org.openmrs.api.handler.VoidHandler#handle(org.openmrs.Voidable, org.openmrs.User,
	 *      java.util.Date, java.lang.String)
	 * @should void the encounters and observations for the patient
	 * @should ensure that the patient is voided too
	 */
	@Override
	public void handle(Patient patient, User voidingUser, Date voidedDate, String voidReason) {
		BaseVoidHandler voidHandler = HandlerUtil.getHandlersForType(BaseVoidHandler.class, Voidable.class).get(0);
		List<Encounter> encounters = Context.getEncounterService().getEncountersByPatient(patient);
		if (CollectionUtils.isNotEmpty(encounters)) {
			for (Encounter encounter : encounters) {
				if (!encounter.isVoided()) {
					for (Obs observation : encounter.getObs()) {
						if (!observation.isVoided())
							voidHandler.handle(observation, voidingUser, voidedDate, voidReason);
					}
					voidHandler.handle(encounter, voidingUser, voidedDate, voidReason);
				}
			}
		}
		//ensure that the patient is voided too
		if (!patient.isVoided())
			voidHandler.handle(patient, voidingUser, voidedDate, voidReason);
	}
}
