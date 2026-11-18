/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api;

import java.util.List;

import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for Patient.identifiers batch-fetching behavior. Verifies that loading a page of patients
 * issues a bounded number of queries for identifiers instead of one per patient.
 */
public class PatientIdentifierBatchLoadTest extends BaseContextSensitiveTest {

	private static final String IDENTIFIERS_DATASET = "org/openmrs/api/include/PatientServiceTest-identifiers.xml";

	@Autowired
	private PatientService patientService;

	private SessionFactory sessionFactory;

	@BeforeEach
	public void setup() {
		sessionFactory = (SessionFactory) applicationContext.getBean("sessionFactory");
	}

	/**
	 * Verifies that hydrating a page of N patients issues a bounded number of queries for identifiers,
	 * rather than one per patient. Before the batch-size fix, each patient's identifiers collection was
	 * initialized separately, producing N identifier selects.
	 */
	@Test
	public void getPatients_shouldLoadIdentifiersInBoundedNumberOfQueries() {
		executeDataSet(IDENTIFIERS_DATASET);
		updateSearchIndex();

		Statistics stats = sessionFactory.getStatistics();
		stats.clear();

		// Search for "Batch" patients (7004, 7005, 7006, 7007) - 4 patients
		List<Patient> patients = patientService.getPatients("Batch", null, null, false);
		assertEquals(4, patients.size(), "Should find all 4 Batch patients");

		// hydrate the identifiers for the whole page to trigger collection initialization
		patients.forEach(p -> p.getIdentifiers().size());

		assertEquals(1, stats.getCollectionStatistics("org.openmrs.Patient.identifiers").getFetchCount(),
		    "a page of patients should load its identifiers in one batched select");
	}

	/**
	 * Verifies that a patient with zero identifiers loads correctly and returns an empty set.
	 */
	@Test
	public void getPatient_shouldHandlePatientWithZeroIdentifiers() {
		executeDataSet(IDENTIFIERS_DATASET);
		updateSearchIndex();

		Patient patient = patientService.getPatient(7001);
		assertNotNull(patient, "Patient 7001 should exist");
		assertEquals("NoIdent", patient.getGivenName());
		assertNotNull(patient.getIdentifiers(), "Identifiers collection should not be null");
		assertTrue(patient.getIdentifiers().isEmpty(), "Patient with no identifiers should have empty set");
	}

	/**
	 * Verifies that a patient with exactly one identifier loads correctly.
	 */
	@Test
	public void getPatient_shouldHandlePatientWithOneIdentifier() {
		executeDataSet(IDENTIFIERS_DATASET);
		updateSearchIndex();

		Patient patient = patientService.getPatient(7002);
		assertNotNull(patient, "Patient 7002 should exist");
		assertEquals("Single", patient.getGivenName());
		assertEquals(1, patient.getIdentifiers().size(), "Patient should have exactly 1 identifier");

		PatientIdentifier identifier = patient.getIdentifiers().iterator().next();
		assertEquals("ID-502-A", identifier.getIdentifier());
		assertTrue(identifier.getPreferred(), "The single identifier should be preferred");
	}

	/**
	 * Verifies that a patient with several identifiers loads all identifiers. Note: getIdentifiers()
	 * returns both voided and non-voided; getActiveIdentifiers() returns only non-voided.
	 */
	@Test
	public void getPatient_shouldHandlePatientWithSeveralIdentifiers() {
		executeDataSet(IDENTIFIERS_DATASET);
		updateSearchIndex();

		Patient patient = patientService.getPatient(7003);
		assertNotNull(patient, "Patient 7003 should exist");
		assertEquals("Multi", patient.getGivenName());

		// getIdentifiers() returns all (3 total: 2 non-voided + 1 voided)
		assertEquals(3, patient.getIdentifiers().size(), "Patient should have 3 total identifiers (including voided)");

		// getActiveIdentifiers() returns only non-voided (2)
		assertEquals(2, patient.getActiveIdentifiers().size(), "Patient should have 2 active (non-voided) identifiers");

		boolean foundA = false;
		boolean foundB = false;
		boolean foundVoided = false;
		for (PatientIdentifier pi : patient.getIdentifiers()) {
			if ("ID-503-A".equals(pi.getIdentifier())) {
				foundA = true;
				assertFalse(pi.getPreferred(), "Identifier A should not be preferred");
			} else if ("ID-503-B".equals(pi.getIdentifier())) {
				foundB = true;
				assertTrue(pi.getPreferred(), "Identifier B should be preferred");
			} else if ("ID-503-C-VOIDED".equals(pi.getIdentifier())) {
				foundVoided = true;
				assertTrue(pi.getVoided(), "Identifier C should be voided");
			}
		}
		assertTrue(foundA, "Should contain identifier ID-503-A");
		assertTrue(foundB, "Should contain identifier ID-503-B");
		assertTrue(foundVoided, "Should contain voided identifier ID-503-C-VOIDED");
	}

	/**
	 * Verifies that the mapped collection comes back in PatientIdentifier's natural order: non-voided
	 * first, then preferred ascending (Boolean.compareTo puts false before true), then dateCreated,
	 * then type, then identifier.
	 */
	@Test
	public void getPatient_shouldReturnIdentifiersInNaturalOrder() {
		executeDataSet(IDENTIFIERS_DATASET);
		updateSearchIndex();

		Patient patient = patientService.getPatient(7003);
		assertNotNull(patient);

		PatientIdentifier[] identifiers = patient.getIdentifiers().toArray(new PatientIdentifier[0]);
		assertEquals(3, identifiers.length);

		assertEquals("ID-503-A", identifiers[0].getIdentifier());
		assertEquals("ID-503-B", identifiers[1].getIdentifier());
		assertEquals("ID-503-C-VOIDED", identifiers[2].getIdentifier());
	}

	/**
	 * Verifies that Patient.getPatientIdentifier() returns the preferred identifier.
	 */
	@Test
	public void getPatientIdentifier_shouldReturnPreferredIdentifier() {
		executeDataSet(IDENTIFIERS_DATASET);
		updateSearchIndex();

		// Patient with multiple identifiers, preferred is ID-503-B
		Patient patient = patientService.getPatient(7003);
		assertNotNull(patient);
		PatientIdentifier preferred = patient.getPatientIdentifier();
		assertNotNull(preferred, "Preferred identifier should not be null");
		assertEquals("ID-503-B", preferred.getIdentifier(), "getPatientIdentifier() should return the preferred identifier");

		// Patient with single identifier
		Patient singlePatient = patientService.getPatient(7002);
		assertNotNull(singlePatient);
		PatientIdentifier singlePreferred = singlePatient.getPatientIdentifier();
		assertNotNull(singlePreferred, "Single identifier patient should have a preferred identifier");
		assertEquals("ID-502-A", singlePreferred.getIdentifier());

		// Patient with no identifiers
		Patient noIdPatient = patientService.getPatient(7001);
		assertNotNull(noIdPatient);
		assertNull(noIdPatient.getPatientIdentifier(),
		    "Patient with no identifiers should return null from getPatientIdentifier()");
	}

	/**
	 * Verifies that batch loading works correctly when fetching multiple patients at once, confirming
	 * the identifiers for all patients are loaded.
	 */
	@Test
	public void getPatients_shouldLoadAllIdentifiersForBatchOfPatients() {
		executeDataSet(IDENTIFIERS_DATASET);
		updateSearchIndex();

		List<Patient> patients = patientService.getPatients("Batch", null, null, false);
		assertEquals(4, patients.size());

		int totalIdentifiers = 0;
		for (Patient p : patients) {
			totalIdentifiers += p.getIdentifiers().size();
		}

		// Patient 7004: 1 identifier, Patient 7005: 2 identifiers, Patient 7006: 0, Patient 7007: 1
		assertEquals(4, totalIdentifiers, "Total non-voided identifiers across all Batch patients should be 4");
	}
}
