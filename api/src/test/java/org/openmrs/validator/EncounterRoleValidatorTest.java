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
import org.openmrs.EncounterType;
import org.openmrs.Patient;
import org.openmrs.Visit;
import org.openmrs.api.context.Context;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.test.Verifies;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;

/**
 * Contains methods for testing {@link org.openmrs.validator.EncounterRoleValidator#validate(Object, org.springframework.validation.Errors)}
 */
public class EncounterRoleValidatorTest extends BaseContextSensitiveTest {
	
	/**
	 * @see {@link org.openmrs.validator.EncounterRoleValidator#validate(Object, org.springframework.validation.Errors)}
	 */
	@Test
	@Verifies(value = "should fail if the name of the encounter role is null empty or whitespace", method = "validate(Object,Errors)")
	public void validate_shouldFailIfTheNameOfTheEncounterRoleIsNullEmptyOrWhitespace() throws Exception {
		
		EncounterRole encounterRoleNo1 = new EncounterRole();
		encounterRoleNo1.setName(null);
		Errors errorsNo1 = new BindException(encounterRoleNo1, "encounterRole");
		new EncounterRoleValidator().validate(encounterRoleNo1, errorsNo1);
		Assert.assertTrue(errorsNo1.hasFieldErrors("name"));
		
		EncounterRole encounterRoleNo2 = new EncounterRole();
		encounterRoleNo2.setName("");
		Errors errorsNo2 = new BindException(encounterRoleNo2, "encounterRole");
		new EncounterRoleValidator().validate(encounterRoleNo2, errorsNo2);
		Assert.assertTrue(errorsNo2.hasFieldErrors("name"));
		
		EncounterRole encounterRoleNo3 = new EncounterRole();
		encounterRoleNo3.setName("  ");
		Errors errorsNo3 = new BindException(encounterRoleNo3, "encounterRole");
		new EncounterRoleValidator().validate(encounterRoleNo3, errorsNo3);
		Assert.assertTrue(errorsNo3.hasFieldErrors("name"));
	}
	
	/**
	 * @see {@link org.openmrs.validator.EncounterRoleValidator#validate(Object, org.springframework.validation.Errors)}
	 */
	@Test
	@Verifies(value = "should fail if encounter role name is duplicate", method = "validate(Object,Errors)")
	public void validate_shouldFailIfEncounterRoleNameIsDuplicate() throws Exception {
		
		Assert.assertNotNull(Context.getEncounterService().getEncounterRoleByName("Unknown"));
		
		EncounterRole newEncounterRole = new EncounterRole();
		newEncounterRole.setName("Unknown");
		Errors errors = new BindException(newEncounterRole, "encounterRole");
		new EncounterRoleValidator().validate(newEncounterRole, errors);
		Assert.assertTrue(errors.hasFieldErrors("name"));
		
	}
	
	/**
	 * {@link org.openmrs.validator.EncounterRoleValidator#validate(Object, org.springframework.validation.Errors)}
	 */
	@Test
	@Verifies(value = "should pass editing encounter role name", method = "validate(Object,Errors)")
	public void validate_shouldPassEditingEncounterRoleName() throws Exception {
		
		EncounterRole encounterRole = Context.getEncounterService().getEncounterRoleByName("Unknown");
		Assert.assertNotNull(encounterRole);
		encounterRole.setName("Lab");
		encounterRole.setDescription("desc");
		Errors errors = new BindException(encounterRole, "encounterRole");
		new EncounterRoleValidator().validate(encounterRole, errors);
		Assert.assertFalse(errors.hasErrors());
		
	}
	
	/**
	 * {@link org.openmrs.validator.EncounterRoleValidator#validate(Object, org.springframework.validation.Errors)}
	 */
	@Test
	@Verifies(value = "should pass validation if field lengths are correct", method = "validate(Object,Errors)")
	public void validate_shouldPassValidationIfFieldLengthsAreCorrect() throws Exception {
		
		EncounterRole encounterRole = Context.getEncounterService().getEncounterRoleByName("Unknown");
		Assert.assertNotNull(encounterRole);
		encounterRole.setName("name");
		encounterRole.setDescription("desc");
		encounterRole.setRetireReason("retireReason");
		Errors errors = new BindException(encounterRole, "encounterRole");
		new EncounterRoleValidator().validate(encounterRole, errors);
		Assert.assertFalse(errors.hasErrors());
	}
	
	/**
	 * {@link org.openmrs.validator.EncounterRoleValidator#validate(Object, org.springframework.validation.Errors)}
	 */
	@Test
	@Verifies(value = "should fail validation if field lengths are not correct", method = "validate(Object,Errors)")
	public void validate_shouldFailValidationIfFieldLengthsAreNotCorrect() throws Exception {
		
		EncounterRole encounterRole = new EncounterRole();
		encounterRole
		        .setName("too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text");
		encounterRole
		        .setDescription("too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text");
		encounterRole
		        .setRetireReason("too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text");
		Errors errors = new BindException(encounterRole, "encounterRole");
		new EncounterRoleValidator().validate(encounterRole, errors);
		Assert.assertTrue(errors.hasFieldErrors("name"));
		Assert.assertTrue(errors.hasFieldErrors("description"));
		Assert.assertTrue(errors.hasFieldErrors("retireReason"));
	}
}
