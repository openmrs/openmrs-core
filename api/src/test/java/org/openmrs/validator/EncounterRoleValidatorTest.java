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
}
