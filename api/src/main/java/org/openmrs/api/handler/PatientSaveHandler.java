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

import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.User;
import org.openmrs.annotation.Handler;
import org.openmrs.aop.RequiredDataAdvice;

/**
 * This class deals with {@link Patient} objects when they are saved via a save* method in an
 * Openmrs Service. This handler is automatically called by the {@link RequiredDataAdvice} AOP
 * class. <br>
 *
 * @see RequiredDataHandler
 * @see SaveHandler
 * @see Patient
 * @since 1.5
 */
@Handler(supports = Patient.class)
public class PatientSaveHandler implements SaveHandler<Patient> {
	
	/**
	 * @see org.openmrs.api.handler.SaveHandler#handle(org.openmrs.OpenmrsObject, org.openmrs.User,
	 *      java.util.Date, java.lang.String)
	 */
	@Override
	public void handle(Patient patient, User creator, Date dateCreated, String other) {
		if (patient.getIdentifiers() != null) {
			for (PatientIdentifier pIdentifier : patient.getIdentifiers()) {
				
				// make sure the identifier is associated with the current patient
				if (pIdentifier.getPatient() == null) {
					pIdentifier.setPatient(patient);
				}
			}
		}
	}
}
