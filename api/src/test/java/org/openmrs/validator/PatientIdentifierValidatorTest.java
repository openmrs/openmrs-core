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

import junit.framework.Assert;

import org.junit.Test;
import org.openmrs.Location;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.PatientIdentifierType.UniquenessBehavior;
import org.openmrs.api.BlankIdentifierException;
import org.openmrs.api.IdentifierNotUniqueException;
import org.openmrs.api.InvalidCheckDigitException;
import org.openmrs.api.InvalidIdentifierFormatException;
import org.openmrs.api.PatientService;
import org.openmrs.api.PatientIdentifierException;
import org.openmrs.api.context.Context;
import org.openmrs.patient.IdentifierValidator;
import org.openmrs.patient.impl.LuhnIdentifierValidator;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.test.Verifies;

/**
 * Tests methods on the {@link PatientIdentifierValidator} class.
 */
public class PatientIdentifierValidatorTest extends BaseContextSensitiveTest {
	
	/**
	 * @see {@link PatientIdentifierValidator#validateIdentifier(PatientIdentifier)}
	 */
	@Test(expected = BlankIdentifierException.class)
	@Verifies(value = "should fail validation if PatientIdentifier is null", method = "validateIdentifier(PatientIdentifier)")
	public void validateIdentifier_shouldFailValidationIfPatientIdentifierIsNull() throws Exception {
		PatientIdentifierValidator.validateIdentifier(null);
	}
	
	/**
	 * @see {@link PatientIdentifierValidator#validateIdentifier(PatientIdentifier)}
	 */
	@Test
	@Verifies(value = "should pass validation if PatientIdentifier is voided", method = "validateIdentifier(PatientIdentifier)")
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
	 * @see {@link PatientIdentifierValidator#validateIdentifier(PatientIdentifier)}
	 */
	@Test(expected = IdentifierNotUniqueException.class)
	@Verifies(value = "should fail validation if another patient has a matching identifier of the same type", method = "validateIdentifier(PatientIdentifier)")
	public void validateIdentifier_shouldFailValidationIfAnotherPatientHasAMatchingIdentifierOfTheSameType()
	        throws Exception {
		PatientIdentifier pi = Context.getPatientService().getPatientIdentifiers("7TU-8", null, null, null, null).get(0);
		pi.setIdentifier("101-6");
		PatientIdentifierValidator.validateIdentifier(pi);
	}
	
	/**
	 * @see {@link PatientIdentifierValidator#validateIdentifier(String,PatientIdentifierType)}
	 */
	@Test(expected = BlankIdentifierException.class)
	@Verifies(value = "should fail validation if PatientIdentifierType is null", method = "validateIdentifier(String,PatientIdentifierType)")
	public void validateIdentifier_shouldFailValidationIfPatientIdentifierTypeIsNull() throws Exception {
		PatientIdentifierValidator.validateIdentifier("ABC", null);
	}
	
	/**
	 * @see {@link PatientIdentifierValidator#checkIdentifierAgainstFormat(String,String)}
	 */
	@Test(expected = BlankIdentifierException.class)
	@Verifies(value = "should fail validation if identifier is blank", method = "checkIdentifierAgainstFormat(String,String)")
	public void checkIdentifierAgainstFormat_shouldFailValidationIfIdentifierIsBlank() throws Exception {
		PatientIdentifierValidator.validateIdentifier("", new PatientIdentifierType(1));
	}
	
	/**
	 * @see {@link PatientIdentifierValidator#checkIdentifierAgainstFormat(String,String)}
	 */
	@Test(expected = InvalidIdentifierFormatException.class)
	@Verifies(value = "should fail validation if identifier does not match the format", method = "checkIdentifierAgainstFormat(String,String)")
	public void checkIdentifierAgainstFormat_shouldFailValidationIfIdentifierDoesNotMatchTheFormat() throws Exception {
		PatientIdentifierValidator.checkIdentifierAgainstFormat("111-222-333", "[0-9]{3}\\-[0-9]{2}\\-[0-9]{4}", null);
	}
	
	/**
	 * @see {@link PatientIdentifierValidator#checkIdentifierAgainstFormat(String,String)}
	 */
	@Test
	@Verifies(value = "should pass validation if identifier matches the format", method = "checkIdentifierAgainstFormat(String,String)")
	public void checkIdentifierAgainstFormat_shouldPassValidationIfIdentifierMatchesTheFormat() throws Exception {
		PatientIdentifierValidator.checkIdentifierAgainstFormat("111-22-3333", "[0-9]{3}\\-[0-9]{2}\\-[0-9]{4}", null);
	}
	
	/**
	 * @see {@link PatientIdentifierValidator#checkIdentifierAgainstFormat(String,String)}
	 */
	@Test
	@Verifies(value = "should pass validation if the format is blank", method = "checkIdentifierAgainstFormat(String,String)")
	public void checkIdentifierAgainstFormat_shouldPassValidationIfTheFormatIsBlank() throws Exception {
		PatientIdentifierValidator.checkIdentifierAgainstFormat("abcdefg", "", null);
	}
	
	/**
	 * @see {@link PatientIdentifierValidator#checkIdentifierAgainstValidator(String,IdentifierValidator)}
	 */
	@Test(expected = BlankIdentifierException.class)
	@Verifies(value = "should fail validation if identifier is blank", method = "checkIdentifierAgainstValidator(String,IdentifierValidator)")
	public void checkIdentifierAgainstValidator_shouldFailValidationIfIdentifierIsBlank() throws Exception {
		PatientIdentifierValidator.checkIdentifierAgainstValidator("", new LuhnIdentifierValidator());
	}
	
	/**
	 * @see {@link PatientIdentifierValidator#checkIdentifierAgainstValidator(String,IdentifierValidator)}
	 */
	@Test(expected = InvalidCheckDigitException.class)
	@Verifies(value = "should fail validation if identifier is invalid", method = "checkIdentifierAgainstValidator(String,IdentifierValidator)")
	public void checkIdentifierAgainstValidator_shouldFailValidationIfIdentifierIsInvalid() throws Exception {
		PatientIdentifierValidator.checkIdentifierAgainstValidator("7TU-4", new LuhnIdentifierValidator());
	}
	
	/**
	 * @see {@link PatientIdentifierValidator#checkIdentifierAgainstValidator(String,IdentifierValidator)}
	 */
	@Test
	@Verifies(value = "should pass validation if identifier is valid", method = "checkIdentifierAgainstValidator(String,IdentifierValidator)")
	public void checkIdentifierAgainstValidator_shouldPassValidationIfIdentifierIsValid() throws Exception {
		PatientIdentifierValidator.checkIdentifierAgainstValidator("7TU-8", new LuhnIdentifierValidator());
	}
	
	/**
	 * @see {@link PatientIdentifierValidator#checkIdentifierAgainstValidator(String,IdentifierValidator)}
	 */
	@Test
	@Verifies(value = "should pass validation if validator is null", method = "checkIdentifierAgainstValidator(String,IdentifierValidator)")
	public void checkIdentifierAgainstValidator_shouldPassValidationIfValidatorIsNull() throws Exception {
		PatientIdentifierValidator.checkIdentifierAgainstValidator("7TU-4", null);
	}
	
	/**
	 * @see {@link PatientIdentifierValidator#validateIdentifier(String,PatientIdentifierType)}
	 */
	@Test(expected = BlankIdentifierException.class)
	@Verifies(value = "should fail validation if identifier is blank", method = "validateIdentifier(String,PatientIdentifierType)")
	public void validateIdentifier_shouldFailValidationIfIdentifierIsBlank() throws Exception {
		PatientIdentifier identifier = new PatientIdentifier("", new PatientIdentifierType(1), new Location(1));
		PatientIdentifierValidator.validateIdentifier(identifier);
	}
	
	/**
	 * @see {@link PatientIdentifierValidator#validateIdentifier(PatientIdentifier)}
	 */
	@Test
	@Verifies(value = "should pass if in use and id type uniqueness is set to non unique", method = "validateIdentifier(PatientIdentifier)")
	public void validateIdentifier_shouldPassIfInUseAndIdTypeUniquenessIsSetToNonUnique() throws Exception {
		PatientService patientService = Context.getPatientService();
		PatientIdentifier duplicateId = patientService.getPatientIdentifier(1);
		Assert.assertNotNull(duplicateId.getLocation());
		
		PatientIdentifierType idType = duplicateId.getIdentifierType();
		idType.setUniquenessBehavior(UniquenessBehavior.NON_UNIQUE);
		patientService.savePatientIdentifierType(idType);
		
		PatientIdentifier pi = new PatientIdentifier(duplicateId.getIdentifier(), idType, duplicateId.getLocation());
		PatientIdentifierValidator.validateIdentifier(pi);
	}
	
	/**
	 * @see {@link PatientIdentifierValidator#validateIdentifier(PatientIdentifier)}
	 */
	@Test
	@Verifies(value = "should pass if locationBehavior is NOT_USED and location is null", method = "validateIdentifier(PatientIdentifier)")
	public void validateIdentifier_shouldPassIfLocationBehaviorIsNotUsedAndLocationIsNull() throws Exception {
		PatientIdentifier pi = new PatientIdentifier("1TU-8", new PatientIdentifierType(1), null);
		PatientIdentifierType idType = pi.getIdentifierType();
		idType.setLocationBehavior(PatientIdentifierType.LocationBehavior.NOT_USED);
		PatientIdentifierValidator.validateIdentifier(pi);
	}
	
	/**
	 * @see {@link PatientIdentifierValidator#validateIdentifier(PatientIdentifier)}
	 */
	@Test(expected = PatientIdentifierException.class)
	@Verifies(value = "should fail validation if locationBehavior is REQUIRED and location is null", method = "validateIdentifier(PatientIdentifier)")
	public void validateIdentifier_shouldPassIfLocationBehaviorIsRequiredAndLocationIsNull() throws Exception {
		PatientIdentifier pi = new PatientIdentifier("1TU-8", new PatientIdentifierType(1), null);
		PatientIdentifierType idType = pi.getIdentifierType();
		idType.setLocationBehavior(PatientIdentifierType.LocationBehavior.REQUIRED);
		PatientIdentifierValidator.validateIdentifier(pi);
	}
	
	/**
	 * @see PatientIdentifierValidator#checkIdentifierAgainstFormat(String,String,String)
	 * @verifies include format in error message if no formatDescription is specified
	 */
	 @Test
	 public void checkIdentifierAgainstFormat_shouldIncludeFormatInErrorMessageIfNoFormatDescriptionIsSpecified() throws Exception {
	  	String pattern = "\\d+";
	   	String patternDescription = null;
	    	
	    try {
	    	PatientIdentifierValidator.checkIdentifierAgainstFormat("not digits", pattern, patternDescription);
	    	Assert.fail();
	    } catch (InvalidIdentifierFormatException ex) {
	    	Assert.assertTrue(ex.getMessage().indexOf(pattern) > 0);
	    }
	 }
	
	/**
	 * @see PatientIdentifierValidator#checkIdentifierAgainstFormat(String,String,String)
	 * @verifies include formatDescription in error message if specified
	 */
	 @Test
	 public void checkIdentifierAgainstFormat_shouldIncludeFormatDescriptionInErrorMessageIfSpecified() throws Exception {
	   	String pattern = "\\d+";
	   	String patternDescription = "Should be digits";
	   	try {
	   		PatientIdentifierValidator.checkIdentifierAgainstFormat("not digits", pattern, patternDescription);
	   		Assert.fail();
	   	} catch (InvalidIdentifierFormatException ex) {
	   		Assert.assertFalse(ex.getMessage().indexOf(pattern) > 0);
	   		Assert.assertTrue(ex.getMessage().indexOf(patternDescription) > 0);
	   	}
	 }
}
