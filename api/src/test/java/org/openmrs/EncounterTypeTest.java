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

import org.junit.jupiter.api.Test;

/**
 * This class tests the all of the {@link EncounterType} non-trivial object methods.
 * 
 * @see EncounterType
 */
public class EncounterTypeTest {
	
	/**
	 * Make sure the EncounterType(Integer) constructor sets the encounterTypeId
	 * 
	 * @see EncounterType#EncounterType(Integer)
	 */
	@Test
	public void EncounterType_shouldSetEncounterTypeIdWithGivenParameter() {
		EncounterType encounterType = new EncounterType(123);
		assertEquals(123, encounterType.getEncounterTypeId().intValue());
	}
}
