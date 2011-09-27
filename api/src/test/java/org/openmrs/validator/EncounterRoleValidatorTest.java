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
package org.openmrs.validator;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.Encounter;
import org.openmrs.EncounterRole;
import org.openmrs.Patient;
import org.openmrs.Visit;
import org.openmrs.test.Verifies;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;

/**
 * Contains methods for testing {@link org.openmrs.validator.EncounterRoleValidator#validate(Object, org.springframework.validation.Errors)}
 */
public class EncounterRoleValidatorTest {
	
	/**
	 * @see {@link org.openmrs.validator.EncounterRoleValidator#validate(Object, org.springframework.validation.Errors)}
	 */
	@Test
	@Verifies(value = "should fail if the name of the encounter role is not set", method = "validate(Object,Errors)")
	public void validate_shouldFailIfTheNameOfTheEncounterRoleIsNotSet() throws Exception {
		EncounterRole encounterRole = new EncounterRole();
		Errors errors = new BindException(encounterRole, "encounterRole");
		new EncounterRoleValidator().validate(encounterRole, errors);
		Assert.assertTrue(errors.hasFieldErrors("name"));
	}
}
