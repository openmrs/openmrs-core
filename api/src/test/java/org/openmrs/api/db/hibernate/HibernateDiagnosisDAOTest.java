/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api.db.hibernate;

import java.util.List;

import org.junit.Assert;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Rule;
import org.junit.Test;
import org.junit.Before;
import org.junit.rules.ExpectedException;
import org.openmrs.Diagnosis;
//import org.openmrs.Patient;
import org.openmrs.api.db.DiagnosisDAO;
import org.openmrs.test.BaseContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;

public class HibernateDiagnosisDAOTest extends BaseContextSensitiveTest {

	private static final String DIAGNOSIS_XML = "org/openmrs/api/include/HibernateDiagnosisDAOTestDataset.xml";

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Autowired
	DiagnosisDAO diagnosisDAO;

	@Before
	public void setUp() {
		executeDataSet(DIAGNOSIS_XML);

	}

	@Test
	public void shouldDiagnosisByUuid() {
		String uuid = "4e663d66-6b78-11e0-93c3-18a905e044dc";
		Diagnosis diagnosis = diagnosisDAO.getDiagnosisByUuid(uuid);
		assertEquals(diagnosis.getUuid(), uuid);
	}

	@Test
	public void shouldDiagnosisById() {
		int diagnosisId = 1;
		Diagnosis diagnosis = diagnosisDAO.getDiagnosisById(diagnosisId);
		assertEquals((int) diagnosis.getId(), diagnosisId);
	}

	//	@Test
	//	public void shouldGetActiveDiagnoses() {
	//		Patient patient = new Patient(2);
	//		List<Diagnosis> activeDiagnosis = diagnosisDAO.getActiveDiagnoses(patient);
	//		assertEquals(2, activeDiagnosis.size());
	//	}

	@Test
	public void shouldDeleteCondition() {
		String uuid = "4e663d66-6b78-11e0-93c3-18a905e044dc";
		Diagnosis diagnosis = diagnosisDAO.getDiagnosisByUuid(uuid);

		Assert.assertNotNull(diagnosis);

		diagnosisDAO.deleteDiagnosis(diagnosis);

		Assert.assertNull(diagnosisDAO.getDiagnosisByUuid(uuid));
		}

	}
