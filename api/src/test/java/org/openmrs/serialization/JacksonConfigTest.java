/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.serialization;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;

import com.fasterxml.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class JacksonConfigTest {

	private ObjectMapper objectMapper;

	@BeforeEach
	public void setup() {
		JacksonConfig config = new JacksonConfig();
		objectMapper = config.objectMapper();
	}

	@Test
	public void objectMapper_shouldSerializeAndDeserializePatientCorrectly() throws Exception {
		// Given
		Patient patient = new Patient(1);
		patient.setUuid(UUID.randomUUID().toString());
		patient.setAllergyStatus("Unknown");

		PatientIdentifierType identifierType = new PatientIdentifierType();
		identifierType.setUuid(UUID.randomUUID().toString());
		identifierType.setName("OpenMRS ID");

		PatientIdentifier identifier = new PatientIdentifier();
		identifier.setIdentifier("12345");
		identifier.setIdentifierType(identifierType);
		identifier.setUuid(UUID.randomUUID().toString());
		identifier.setPreferred(true);

		// This will set the patient reference inside the PatientIdentifier to maintain bidirectionality
		patient.addIdentifier(identifier);

		// When
		String json = objectMapper.writeValueAsString(patient);
		Patient deserializedPatient = objectMapper.readValue(json, Patient.class);

		// Then
		assertNotNull(deserializedPatient);
		assertEquals(patient.getUuid(), deserializedPatient.getUuid());
		assertEquals(patient.getPatientId(), deserializedPatient.getPatientId());
		assertEquals(patient.getAllergyStatus(), deserializedPatient.getAllergyStatus());

		assertNotNull(deserializedPatient.getIdentifiers());
		assertEquals(1, deserializedPatient.getIdentifiers().size());

		PatientIdentifier deserializedIdentifier = deserializedPatient.getIdentifiers().iterator().next();
		assertEquals(identifier.getIdentifier(), deserializedIdentifier.getIdentifier());
		assertEquals(identifier.getUuid(), deserializedIdentifier.getUuid());
		assertTrue(deserializedIdentifier.getPreferred());

		assertNotNull(deserializedIdentifier.getIdentifierType());
		assertEquals(identifierType.getUuid(), deserializedIdentifier.getIdentifierType().getUuid());

		// Verify bidirectional relationship is maintained without infinite recursion
		assertNotNull(deserializedIdentifier.getPatient());
		assertEquals(patient.getUuid(), deserializedIdentifier.getPatient().getUuid());
	}
}
