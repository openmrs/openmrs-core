/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
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
