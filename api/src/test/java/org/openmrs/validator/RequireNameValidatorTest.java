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
import org.openmrs.EncounterRole;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;

/**
 * Tests methods on the {@link RequireNameValidator} class.
 */
public class RequireNameValidatorTest {
	
	/**
	 * @see RequireNameValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfNameIsNullOrEmptyOrWhitespace() {
		EncounterRole role = new EncounterRole();
		role.setName(null);
		role.setDescription(":(");
		
		Errors errors = new BindException(role, "type");
		new RequireNameValidator().validate(role, errors);
		Assert.assertTrue(errors.hasFieldErrors("name"));
		
		role.setName("");
		errors = new BindException(role, "type");
		new RequireNameValidator().validate(role, errors);
		Assert.assertTrue(errors.hasFieldErrors("name"));
		
		role.setName(" ");
		errors = new BindException(role, "type");
		new RequireNameValidator().validate(role, errors);
		Assert.assertTrue(errors.hasFieldErrors("name"));
	}
	
	/**
	 * @see RequireNameValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldPassValidationIfNameHasProperValue() {
		EncounterRole role = new EncounterRole();
		role.setName("restraining");
		
		Errors errors = new BindException(role, "type");
		new RequireNameValidator().validate(role, errors);
		
		Assert.assertFalse(errors.hasErrors());
	}
}
