/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
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
