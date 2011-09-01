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
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.api.context.Context;
import org.openmrs.test.Verifies;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;

/**
 * Tests methods on the {@link PatientValidator} class.
 */
public class PatientValidatorTest extends PersonValidatorTest {
	
	/**
	 * @see {@link PatientValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should fail validation if a preferred patient identifier is not chosen", method = "validate(Object,Errors)")
	public void validate_shouldFailValidationIfAPreferredPatientIdentifierIsNotChosen() throws Exception {
		Patient pa = Context.getPatientService().getPatient(2);
		Assert.assertNotNull(pa.getPatientIdentifier());
		//set all identifiers to be non-preferred
		for (PatientIdentifier id : pa.getIdentifiers())
			id.setPreferred(false);
		
		Errors errors = new BindException(pa, "patient");
		validator.validate(pa, errors);
		Assert.assertTrue(errors.hasErrors());
	}
	
	/**
	 * @see {@link PatientValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should fail validation if a preferred patient identifier is not chosen for voided patients", method = "validate(Object,Errors)")
	public void validate_shouldFailValidationIfAPreferredPatientIdentifierIsNotChosenForVoidedPatients() throws Exception {
		Patient pa = Context.getPatientService().getPatient(999);
		Assert.assertTrue(pa.isVoided());//sanity check
		Assert.assertNotNull(pa.getPatientIdentifier());
		for (PatientIdentifier id : pa.getIdentifiers())
			id.setPreferred(false);
		
		Errors errors = new BindException(pa, "patient");
		validator.validate(pa, errors);
		Assert.assertTrue(errors.hasErrors());
	}
}
