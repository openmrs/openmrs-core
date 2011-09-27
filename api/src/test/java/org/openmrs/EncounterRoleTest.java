package org.openmrs;

import org.junit.Test;
import org.openmrs.test.Verifies;

/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 * <p/>
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 * <p/>
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
public class EncounterRoleTest {
	
	/**
	 * @see {@link Encounter#toString()}
	 */
	@Test
	@Verifies(value = "should not fail with empty object", method = "toString()")
	public void toString_shouldNotFailWithEmptyObject() throws Exception {
		EncounterRole encounterRole = new EncounterRole();
		@SuppressWarnings("unused")
		String toStringOutput = encounterRole.toString();
	}
	
}
