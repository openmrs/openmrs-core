/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;

class PatientIdentifierTest {

	@Test
	void copy_shouldReturnNewInstance() {
		PatientIdentifier original = new PatientIdentifier();
		original.setIdentifier("12345");

		PatientIdentifier copy = original.copy();

		assertNotSame(original, copy);
	}

	@Test
	void copy_shouldCopyAllFields() {
		PatientIdentifier original = new PatientIdentifier();
		original.setIdentifier("12345");
		original.setIdentifierType(new PatientIdentifierType(1));
		original.setLocation(new Location(1));
		original.setPreferred(true);
		original.setVoided(false);

		PatientIdentifier copy = original.copy();

		assertEquals(original.getIdentifier(), copy.getIdentifier());
		assertEquals(original.getIdentifierType(), copy.getIdentifierType());
		assertEquals(original.getLocation(), copy.getLocation());
		assertEquals(original.getPreferred(), copy.getPreferred());
		assertEquals(original.getVoided(), copy.getVoided());
	}

	@Test
	void copy_shouldSharePatientReference() {
		Patient patient = new Patient();
		PatientIdentifier original = new PatientIdentifier();
		original.setPatient(patient);

		PatientIdentifier copy = original.copy();

		assertSame(original.getPatient(), copy.getPatient());
	}

}
