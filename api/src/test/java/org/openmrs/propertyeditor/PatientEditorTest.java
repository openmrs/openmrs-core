/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.propertyeditor;

import org.openmrs.Patient;
import org.openmrs.api.PatientService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Tests the {@link PatientEditor}
 */
public class PatientEditorTest extends BasePropertyEditorTest<Patient, PatientEditor> {
	
	private static final Integer EXISTING_ID = 2;
	
	@Autowired
	private PatientService patientService;
	
	@Override
	protected PatientEditor getNewEditor() {
		return new PatientEditor();
	}
	
	@Override
	protected Patient getExistingObject() {
		return patientService.getPatient(EXISTING_ID);
	}
}
