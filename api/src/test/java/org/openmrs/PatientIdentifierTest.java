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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PatientIdentifierTest {

	@Test
	public void defaultComparator_shouldHandleNullIdentifierType() {
		PatientIdentifier pi1 = new PatientIdentifier();
		PatientIdentifier pi2 = new PatientIdentifier();

		PatientIdentifierType identifierType = new PatientIdentifierType();
		identifierType.setPatientIdentifierTypeId(1);

		pi1.setIdentifierType(identifierType);
		pi2.setIdentifierType(null);

		PatientIdentifier.DefaultComparator comparator = new PatientIdentifier.DefaultComparator();

		assertEquals(-1, comparator.compare(pi1, pi2));
		assertEquals(1, comparator.compare(pi2, pi1));
	}
}
