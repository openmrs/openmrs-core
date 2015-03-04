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

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.test.Verifies;

/**
 * This class tests the all of the {@link EncounterType} non-trivial object methods.
 * 
 * @see EncounterType
 */
public class EncounterTypeTest {
	
	/**
	 * Make sure the EncounterType(Integer) constructor sets the encounterTypeId
	 * 
	 * @see {@link EncounterType#EncounterType(Integer)}
	 */
	@Test
	@Verifies(value = "should set encounter type id with given parameter", method = "EncounterType(Integer)")
	public void EncounterType_shouldSetEncounterTypeIdWithGivenParameter() throws Exception {
		EncounterType encounterType = new EncounterType(123);
		Assert.assertEquals(123, encounterType.getEncounterTypeId().intValue());
	}
}
