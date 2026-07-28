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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;

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
	 * Verifies that hydrating a page of N patients issues a bounded number of queries for
	 * identifiers, rather than one per patient. Before the batch-size fix, each patient's
	 * identifiers collection was initialized separately, producing N identifier selects.
	 */
	@Test
	public void getPatients_shouldLoadIdentifiersInBoundedNumberOfQueries() {
		executeDataSet(IDENTIFIERS_DATASET);
		updateSearchIndex();

		Statistics stats = sessionFactory.getStatistics();
		stats.setStatisticsEnabled(true);
		stats.clear();

		// Search for "Batch" patients (504, 505, 506, 507) - 4 patients
		List<Patient> patients = patientService.getPatients("Batch", null, null, false);
		assertEquals(4, patients.size(), "Should find all 4 Batch patients");

		// Count queries to patient_identifier table
		long identifierQueries = 0;
		String[] queries = stats.getQueries();
		for (String query : queries) {
			if (query.contains("patient_identifier")) {
				identifierQueries += stats.getQueryStatistics(query).getExecutionCount();
			}
		}

		// With batch-size="1000", all 4 patients' identifiers should load in 1 query
		// (or 0 if cached). Without the fix, this would be 4 queries.
		assertTrue(identifierQueries <= 1,
		    "Expected at most 1 batched identifier query for 4 patients, but got " + identifierQueries);

		stats.setStatisticsEnabled(false);
	}

	/**
	 * Verifies that a patient with zero identifiers loads correctly and returns an empty set.
	 */
	@Test
	public void getPatient_shouldHandlePatientWithZeroIdentifiers() {
		executeDataSet(IDENTIFIERS_DATASET);
		updateSearchIndex();

		Patient patient = patientService.getPatient(501);
		assertNotNull(patient, "Patient 501 should exist");
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

		Patient patient = patientService.getPatient(502);
		assertNotNull(patient, "Patient 502 should exist");
		assertEquals("Single", patient.getGivenName());
		assertEquals(1, patient.getIdentifiers().size(), "Patient should have exactly 1 identifier");

		PatientIdentifier identifier = patient.getIdentifiers().iterator().next();
		assertEquals("ID-502-A", identifier.getIdentifier());
		assertTrue(identifier.getPreferred(), "The single identifier should be preferred");
	}

	/**
	 * Verifies that a patient with several identifiers loads all identifiers.
	 * Note: getIdentifiers() returns both voided and non-voided; getActiveIdentifiers() returns
	 * only non-voided.
	 */
	@Test
	public void getPatient_shouldHandlePatientWithSeveralIdentifiers() {
		executeDataSet(IDENTIFIERS_DATASET);
		updateSearchIndex();

		Patient patient = patientService.getPatient(503);
		assertNotNull(patient, "Patient 503 should exist");
		assertEquals("Multi", patient.getGivenName());

		// getIdentifiers() returns all (3 total: 2 non-voided + 1 voided)
		assertEquals(3, patient.getIdentifiers().size(),
		    "Patient should have 3 total identifiers (including voided)");

		// getActiveIdentifiers() returns only non-voided (2)
		assertEquals(2, patient.getActiveIdentifiers().size(),
		    "Patient should have 2 active (non-voided) identifiers");

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
	 * Verifies that identifier ordering follows natural ordering (PatientIdentifier's compareTo).
	 * Uses getActiveIdentifiers() to get only non-voided identifiers in natural order.
	 * Natural order: voided (false first), then preferred (true first), then dateCreated, then type, then identifier.
	 */
	@Test
	public void getPatient_shouldReturnIdentifiersInNaturalOrder() {
		executeDataSet(IDENTIFIERS_DATASET);
		updateSearchIndex();

		Patient patient = patientService.getPatient(503);
		assertNotNull(patient);

		// getActiveIdentifiers() returns non-voided identifiers in natural order
		PatientIdentifier[] identifiers = patient.getActiveIdentifiers().toArray(new PatientIdentifier[0]);
		assertEquals(2, identifiers.length);

		// Natural order: preferred=true comes before preferred=false
		// ID-503-B (preferred=true) comes before ID-503-A (preferred=false)
		assertEquals("ID-503-B", identifiers[0].getIdentifier(),
		    "First identifier should be ID-503-B (preferred=true comes first)");
		assertEquals("ID-503-A", identifiers[1].getIdentifier(),
		    "Second identifier should be ID-503-A (preferred=false comes second)");
	}

	/**
	 * Verifies that Patient.getPatientIdentifier() returns the preferred identifier.
	 */
	@Test
	public void getPatientIdentifier_shouldReturnPreferredIdentifier() {
		executeDataSet(IDENTIFIERS_DATASET);
		updateSearchIndex();

		// Patient with multiple identifiers, preferred is ID-503-B
		Patient patient = patientService.getPatient(503);
		assertNotNull(patient);
		PatientIdentifier preferred = patient.getPatientIdentifier();
		assertNotNull(preferred, "Preferred identifier should not be null");
		assertEquals("ID-503-B", preferred.getIdentifier(),
		    "getPatientIdentifier() should return the preferred identifier");

		// Patient with single identifier
		Patient singlePatient = patientService.getPatient(502);
		assertNotNull(singlePatient);
		PatientIdentifier singlePreferred = singlePatient.getPatientIdentifier();
		assertNotNull(singlePreferred, "Single identifier patient should have a preferred identifier");
		assertEquals("ID-502-A", singlePreferred.getIdentifier());

		// Patient with no identifiers
		Patient noIdPatient = patientService.getPatient(501);
		assertNotNull(noIdPatient);
		assertNull(noIdPatient.getPatientIdentifier(),
		    "Patient with no identifiers should return null from getPatientIdentifier()");
	}

	/**
	 * Verifies that batch loading works correctly when fetching multiple patients at once,
	 * confirming the identifiers for all patients are loaded.
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

		// Patient 504: 1 identifier, Patient 505: 2 identifiers, Patient 506: 0, Patient 507: 1
		assertEquals(4, totalIdentifiers,
		    "Total non-voided identifiers across all Batch patients should be 4");
	}
}
