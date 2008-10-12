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
package org.openmrs.test.validator;

import junit.framework.Assert;

import org.junit.Test;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.api.BlankIdentifierException;
import org.openmrs.api.IdentifierNotUniqueException;
import org.openmrs.api.InvalidCheckDigitException;
import org.openmrs.api.InvalidIdentifierFormatException;
import org.openmrs.api.context.Context;
import org.openmrs.patient.IdentifierValidator;
import org.openmrs.patient.impl.LuhnIdentifierValidator;
import org.openmrs.test.testutil.BaseContextSensitiveTest;
import org.openmrs.validator.PatientIdentifierValidator;

/**
 * 
 */
public class PatientIdentifierValidatorTest extends BaseContextSensitiveTest {
		
	/**
	 * @verifies {@link PatientIdentifierValidator#validate(PatientIdentifier)}
	 * 		tests that validation fails if PatientIdentifier is null
	 */
	@Test(expected=BlankIdentifierException.class)
	public void validateIdentifier_shouldFailValidationIfPatientIdentifierIsNull() throws Exception {
		PatientIdentifierValidator.validateIdentifier(null);
	}
	
	/**
	 * @verifies {@link PatientIdentifierValidator#validateIdentifier(PatientIdentifier)}
	 * 		tests that validation passes if PatientIdentifier is voided
	 */
	@Test
	public void validateIdentifier_shouldPassValidationIfPatientIdentifierIsVoided() throws Exception {
		PatientIdentifier pi = Context.getPatientService().getPatientIdentifiers("7TU-8", null, null, null, null).get(0);
		pi.setIdentifier("7TU-4");
		// First, make sure this fails
		try {
			PatientIdentifierValidator.validateIdentifier(pi);
			Assert.fail("The patient identifier should be invalid prior to voiding");
		}
		catch (Exception e) {}
		pi.setVoided(true);
		pi.setVoidedBy(Context.getAuthenticatedUser());
		pi.setVoidReason("Testing");
		// Now, make sure this passes
		PatientIdentifierValidator.validateIdentifier(pi);
	}
	
	/**
	 * @verifies {@link PatientIdentifierValidator#validateIdentifier(PatientIdentifier)}
	 * 		tests that validation fails if another patient has a matching identifier of the same type
	 */
	@Test(expected=IdentifierNotUniqueException.class)
	public void validateIdentifier_shouldFailValidationIfAnotherPatientHasAMatchingIdentifierOfTheSameType() throws Exception {
		PatientIdentifier pi = Context.getPatientService().getPatientIdentifiers("7TU-8", null, null, null, null).get(0);
		pi.setIdentifier("101-6");
		PatientIdentifierValidator.validateIdentifier(pi);
	}
	
	/**
	 * @verifies {@link PatientIdentifierValidator#validateIdentifier(String, PatientIdentifierType)}
	 * 		tests that validation fails if the PatientIdentifierType is null
	 */
	@Test(expected=BlankIdentifierException.class)
	public void validateIdentifier_shouldFailValidationIfPatientIdentifierTypeIsNull() throws Exception {
		PatientIdentifierValidator.validateIdentifier("ABC", null);
	}
	
	/**
	 * @verifies {@link PatientIdentifierValidator#validateIdentifier(String, PatientIdentifierType)}
	 * 		tests that validation fails if the identifier is blank
	 */
	@Test(expected=BlankIdentifierException.class)
	public void validateIdentifier_shouldFailValidationIfIdentifierIsBlank() throws Exception {
		PatientIdentifierValidator.validateIdentifier("", new PatientIdentifierType(1));
	}
	
	/**
	 * @verifies {@link PatientIdentifierValidator#checkIdentifierAgainstFormat(String, String)}
	 * 		tests that validation fails if an identifier does not match the expected regex format
	 */
	@Test(expected=InvalidIdentifierFormatException.class)
	public void checkIdentifierAgainstFormat_shouldFailValidationIfIdentifierDoesNotMatchTheFormat() throws Exception {
		PatientIdentifierValidator.checkIdentifierAgainstFormat("111-222-333", "[0-9]{3}\\-[0-9]{2}\\-[0-9]{4}");
	}
	
	/**
	 * @verifies {@link PatientIdentifierValidator#checkIdentifierAgainstFormat(String, String)}
	 * 		tests that validation passes if the identifier string matches the expected regex format
	 */
	@Test
	public void checkIdentifierAgainstFormat_shouldPassValidationIfIdentifierMatchesTheFormat() throws Exception {
		PatientIdentifierValidator.checkIdentifierAgainstFormat("111-22-3333", "[0-9]{3}\\-[0-9]{2}\\-[0-9]{4}");	
	}
	
	/**
	 * @verifies {@link PatientIdentifierValidator#checkIdentifierAgainstFormat(String, String)}
	 * 		tests that validation passes if the regex format is blank
	 */
	@Test
	public void checkIdentifierAgainstFormat_shouldPassValidationIfTheFormatIsBlank() throws Exception {
		PatientIdentifierValidator.checkIdentifierAgainstFormat("abcdefg", "");	
	}
	
	/**
	 * @verifies {@link PatientIdentifierValidator#checkIdentifierAgainstValidator(String, IdentifierValidator)}
	 * 		tests that validation fails if the identifier is blank
	 */
	@Test(expected=BlankIdentifierException.class)
	public void checkIdentifierAgainstValidator_shouldFailValidationIfIdentifierIsBlank() throws Exception {
		PatientIdentifierValidator.checkIdentifierAgainstValidator("", new LuhnIdentifierValidator());
	}
	
	/**
	 * @verifies {@link PatientIdentifierValidator#checkIdentifierAgainstValidator(String, IdentifierValidator)}
	 * 		tests that validation fails if the identifier is not valid according to the IdentifierValidator
	 */
	@Test(expected=InvalidCheckDigitException.class)
	public void checkIdentifierAgainstValidator_shouldFailValidationIfIdentifierIsInvalid() throws Exception {
		PatientIdentifierValidator.checkIdentifierAgainstValidator("7TU-4", new LuhnIdentifierValidator());
	}
	
	/**
	 * @verifies {@link PatientIdentifierValidator#checkIdentifierAgainstValidator(String, IdentifierValidator)}
	 * 		tests that validation passes if the identifier is valid according to the IdentifierValidator
	 */
	@Test
	public void checkIdentifierAgainstValidator_shouldPassValidationIfIdentifierIsValid() throws Exception {
		PatientIdentifierValidator.checkIdentifierAgainstValidator("7TU-8", new LuhnIdentifierValidator());
	}
	
	/**
	 * @verifies {@link PatientIdentifierValidator#checkIdentifierAgainstValidator(String, IdentifierValidator)}
	 * 		tests that validation passes if the IdentifierValidator is null
	 */	@Test
	public void checkIdentifierAgainstValidator_shouldPassValidationIfValidatorIsNull() throws Exception {
		PatientIdentifierValidator.checkIdentifierAgainstValidator("7TU-4", null);
	}

}
