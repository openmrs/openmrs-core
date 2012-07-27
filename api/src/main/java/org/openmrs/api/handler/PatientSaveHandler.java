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

import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.User;
import org.openmrs.annotation.Handler;
import org.openmrs.aop.RequiredDataAdvice;

/**
 * This class deals with {@link Patient} objects when they are saved via a save* method in an
 * Openmrs Service. This handler is automatically called by the {@link RequiredDataAdvice} AOP
 * class. <br/>
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
	public void handle(Patient patient, User creator, Date dateCreated, String other) {
		if (patient.getIdentifiers() != null) {
			for (PatientIdentifier pIdentifier : patient.getIdentifiers()) {
				
				// make sure the identifier is associated with the current patient
				if (pIdentifier.getPatient() == null)
					pIdentifier.setPatient(patient);
			}
		}
	}
}
