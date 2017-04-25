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
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.openmrs.Encounter;
import org.openmrs.Patient;
import org.openmrs.User;
import org.openmrs.annotation.Handler;
import org.openmrs.aop.RequiredDataAdvice;
import org.openmrs.api.EncounterService;
import org.openmrs.api.context.Context;
import org.openmrs.util.PrivilegeConstants;

/**
 * This class deals with {@link Patient} objects when they are voided via a void* method in an
 * Openmrs Service. This handler is automatically called by the {@link RequiredDataAdvice} AOP
 * class. <br>
 * The handler voids all the encounters(including their associated observations) and orders
 * associated with the specified patient object
 * 
 * @see RequiredDataHandler
 * @see VoidHandler
 * @see Patient
 * @since 1.9
 */
@Handler(supports = Patient.class)
public class PatientDataVoidHandler implements VoidHandler<Patient> {
	
	/**
	 * @see org.openmrs.api.handler.VoidHandler#handle(org.openmrs.Voidable, org.openmrs.User,
	 *      java.util.Date, java.lang.String)
	 * @should void the orders encounters and observations associated with the patient
	 */
	@Override
	public void handle(Patient patient, User voidingUser, Date voidedDate, String voidReason) {
		//void all the encounters associated with this patient
		EncounterService es = Context.getEncounterService();
		List<Encounter> encounters = es.getEncountersByPatient(patient);
		if (CollectionUtils.isNotEmpty(encounters)) {
			for (Encounter encounter : encounters) {
				if (!encounter.getVoided()) {
					// EncounterServiceImpl.voidEncounter and the requiredDataAdvice will set dateVoided to current date 
					//if it is null, we need to set it now to match the patient's date voided so that the unvoid 
					//handler's logic doesn't fail when comparing dates while unvoiding encounters that were voided 
					//with the patient
					encounter.setDateVoided(patient.getDateVoided());
					es.voidEncounter(encounter, voidReason);
				}
			}
		}

		Context.addProxyPrivilege(PrivilegeConstants.EDIT_COHORTS);
		try {
			Context.getCohortService().notifyPatientVoided(patient);
		}
		finally {
			Context.removeProxyPrivilege(PrivilegeConstants.EDIT_COHORTS);
		}
	}
}
