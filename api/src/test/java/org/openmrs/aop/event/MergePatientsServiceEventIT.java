/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.aop.event;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.Patient;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MergePatientsServiceEventIT extends BaseContextSensitiveTest {

	private static final String PATIENT_MERGE_XML = "org/openmrs/api/include/PatientServiceTest-mergePatients.xml";

	@Autowired
	private ServiceEventTestListener serviceEventTestListener;

	private PatientService patientService;

	@BeforeEach
	public void setUp() {
		patientService = Context.getPatientService();
		serviceEventTestListener.clearMergePatientsEvents();
	}

	@Test
	public void mergePatients_shouldPublishMergePatientsServiceEvent() throws Exception {
		executeDataSet(PATIENT_MERGE_XML);

		Patient preferred = patientService.getPatient(6);
		Patient notPreferred = patientService.getPatient(7);

		patientService.mergePatients(preferred, notPreferred);

		assertEquals(1, serviceEventTestListener.getMergePatientsEvents().size());
		MergePatientsServiceEvent event = serviceEventTestListener.getMergePatientsEvents().get(0);
		assertEquals(preferred.getPatientId(), event.getPreferred().getPatientId());
		assertEquals(notPreferred.getPatientId(), event.getNotPreferred().getPatientId());
		assertTrue(notPreferred.getVoided());
	}
}
