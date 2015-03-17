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

import static org.mockito.AdditionalMatchers.aryEq;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;

import java.util.Locale;

import junit.framework.Assert;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.openmrs.Location;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.PatientIdentifierType.UniquenessBehavior;
import org.openmrs.api.BlankIdentifierException;
import org.openmrs.api.IdentifierNotUniqueException;
import org.openmrs.api.InvalidCheckDigitException;
import org.openmrs.api.InvalidIdentifierFormatException;
import org.openmrs.api.PatientIdentifierException;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.messagesource.MessageSourceService;
import org.openmrs.patient.IdentifierValidator;
import org.openmrs.patient.impl.LuhnIdentifierValidator;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.test.Verifies;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;

/**
 * Tests methods on the {@link PatientIdentifierValidator} class.
 */
public class PatientIdentifierValidatorTest extends BaseContextSensitiveTest {
	
	@Rule
	public ExpectedException expectedException = ExpectedException.none();
	
	@Mock
	MessageSourceService messageSourceService;
	
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
	 * @see {@link PatientIdentifierValidator#validateIdentifier(PatientIdentifier)}
	 */
	@Test
	@Verifies(value = "should pass validation if field lengths are correct", method = "validateIdentifier(PatientIdentifier)")
	public void validate_shouldPassValidationIfFieldLengthsAreCorrect() throws Exception {
		PatientIdentifier pi = new PatientIdentifier("1TU-8", new PatientIdentifierType(1), null);
		PatientIdentifierType idType = pi.getIdentifierType();
		idType.setLocationBehavior(PatientIdentifierType.LocationBehavior.NOT_USED);
		pi.setVoidReason("voidReason");
		
		Errors errors = new BindException(pi, "pi");
		new PatientIdentifierValidator().validate(pi, errors);
		
		Assert.assertFalse(errors.hasErrors());
	}
	
	/**
	 * @see {@link PatientIdentifierValidator#validateIdentifier(PatientIdentifier)}
	 */
	@Test
	@Verifies(value = "should fail validation if field lengths are not correct", method = "validateIdentifier(PatientIdentifier)")
	public void validate_shouldFailValidationIfFieldLengthsAreNotCorrect() throws Exception {
		PatientIdentifier pi = new PatientIdentifier("too long text too long text too long text too long text",
		        new PatientIdentifierType(1), null);
		PatientIdentifierType idType = pi.getIdentifierType();
		idType.setLocationBehavior(PatientIdentifierType.LocationBehavior.NOT_USED);
		pi
		        .setVoidReason("too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text");
		
		Errors errors = new BindException(pi, "pi");
		new PatientIdentifierValidator().validate(pi, errors);
		
		Assert.assertTrue(errors.hasFieldErrors("identifier"));
		Assert.assertTrue(errors.hasFieldErrors("voidReason"));
	}
	
	/**
	 * @see {@link PatientIdentifierValidator#checkIdentifierAgainstFormat(String,String,String)}
	 */
	@Verifies(value = "include format in error message if no formatDescription is specified", method = "checkIdentifierAgainstFormat(String,String,String)")
	@Test
	public void checkIdentifierAgainstFormat_shouldIncludeFormatInErrorMessageIfNoFormatDescriptionIsSpecified()
	        throws Exception {
		
		String format = "\\d+";
		String formatDescription = null;
		String expectedErrorMessage = "Identifier \"abc\" does not match : \"\\d+\"";
		
		Mockito.when(
		    messageSourceService.getMessage(eq("PatientIdentifier.error.invalidFormat"),
		        aryEq(new String[] { "abc", format }), isA(Locale.class))).thenReturn(expectedErrorMessage);
		
		expectedException.expect(InvalidIdentifierFormatException.class);
		expectedException.expectMessage(expectedErrorMessage);
		
		PatientIdentifierValidator.checkIdentifierAgainstFormat("abc", format, formatDescription);
		
	}
	
	/**
	 * @see {@link PatientIdentifierValidator#checkIdentifierAgainstFormat(String,String,String)}
	 */
	@Verifies(value = "include formatDescription in error message if specified", method = "checkIdentifierAgainstFormat(String,String,String)")
	@Test
	public void checkIdentifierAgainstFormat_shouldIncludeFormatDescriptionInErrorMessageIfSpecified() throws Exception {
		
		String format = "\\d+";
		String formatDescription = "formatDescription";
		String expectedErrorMessage = "Identifier \"abc\" does not match : \"formatDescription\"";
		
		Mockito.when(
		    messageSourceService.getMessage(eq("PatientIdentifier.error.invalidFormat"), aryEq(new String[] { "abc",
		            formatDescription }), isA(Locale.class))).thenReturn(expectedErrorMessage);
		
		expectedException.expect(InvalidIdentifierFormatException.class);
		expectedException.expectMessage(expectedErrorMessage);
		
		PatientIdentifierValidator.checkIdentifierAgainstFormat("abc", format, formatDescription);
		
	}
	
}
