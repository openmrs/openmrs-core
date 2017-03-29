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

import org.junit.Assert;
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
	 * @see PatientIdentifierValidator#validateIdentifier(PatientIdentifier)
	 */
	@Test(expected = BlankIdentifierException.class)
	public void validateIdentifier_shouldFailValidationIfPatientIdentifierIsNull() {
		PatientIdentifierValidator.validateIdentifier(null);
	}
	
	/**
	 * @see PatientIdentifierValidator#validateIdentifier(PatientIdentifier)
	 */
	@Test
	public void validateIdentifier_shouldPassValidationIfPatientIdentifierIsVoided() {
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
	 * @see PatientIdentifierValidator#validateIdentifier(PatientIdentifier)
	 */
	@Test(expected = IdentifierNotUniqueException.class)
	public void validateIdentifier_shouldFailValidationIfAnotherPatientHasAMatchingIdentifierOfTheSameType()
	{
		PatientIdentifier pi = Context.getPatientService().getPatientIdentifiers("7TU-8", null, null, null, null).get(0);
		pi.setIdentifier("101-6");
		PatientIdentifierValidator.validateIdentifier(pi);
	}
	
	/**
	 * @see PatientIdentifierValidator#validateIdentifier(String,PatientIdentifierType)
	 */
	@Test(expected = BlankIdentifierException.class)
	public void validateIdentifier_shouldFailValidationIfPatientIdentifierTypeIsNull() {
		PatientIdentifierValidator.validateIdentifier("ABC", null);
	}
	
	/**
	 * @see PatientIdentifierValidator#checkIdentifierAgainstFormat(String,String)
	 */
	@Test(expected = BlankIdentifierException.class)
	public void checkIdentifierAgainstFormat_shouldFailValidationIfIdentifierIsBlank() {
		PatientIdentifierValidator.validateIdentifier("", new PatientIdentifierType(1));
	}
	
	/**
	 * @see PatientIdentifierValidator#checkIdentifierAgainstFormat(String,String)
	 */
	@Test(expected = InvalidIdentifierFormatException.class)
	public void checkIdentifierAgainstFormat_shouldFailValidationIfIdentifierDoesNotMatchTheFormat() {
		PatientIdentifierValidator.checkIdentifierAgainstFormat("111-222-333", "[0-9]{3}\\-[0-9]{2}\\-[0-9]{4}", null);
	}
	
	/**
	 * @see PatientIdentifierValidator#checkIdentifierAgainstFormat(String,String)
	 */
	@Test
	public void checkIdentifierAgainstFormat_shouldPassValidationIfIdentifierMatchesTheFormat() {
		PatientIdentifierValidator.checkIdentifierAgainstFormat("111-22-3333", "[0-9]{3}\\-[0-9]{2}\\-[0-9]{4}", null);
	}
	
	/**
	 * @see PatientIdentifierValidator#checkIdentifierAgainstFormat(String,String)
	 */
	@Test
	public void checkIdentifierAgainstFormat_shouldPassValidationIfTheFormatIsBlank() {
		PatientIdentifierValidator.checkIdentifierAgainstFormat("abcdefg", "", null);
	}
	
	/**
	 * @see PatientIdentifierValidator#checkIdentifierAgainstValidator(String,IdentifierValidator)
	 */
	@Test(expected = BlankIdentifierException.class)
	public void checkIdentifierAgainstValidator_shouldFailValidationIfIdentifierIsBlank() {
		PatientIdentifierValidator.checkIdentifierAgainstValidator("", new LuhnIdentifierValidator());
	}
	
	/**
	 * @see PatientIdentifierValidator#checkIdentifierAgainstValidator(String,IdentifierValidator)
	 */
	@Test(expected = InvalidCheckDigitException.class)
	public void checkIdentifierAgainstValidator_shouldFailValidationIfIdentifierIsInvalid() {
		PatientIdentifierValidator.checkIdentifierAgainstValidator("7TU-4", new LuhnIdentifierValidator());
	}
	
	/**
	 * @see PatientIdentifierValidator#checkIdentifierAgainstValidator(String,IdentifierValidator)
	 */
	@Test
	public void checkIdentifierAgainstValidator_shouldPassValidationIfIdentifierIsValid() {
		PatientIdentifierValidator.checkIdentifierAgainstValidator("7TU-8", new LuhnIdentifierValidator());
	}
	
	/**
	 * @see PatientIdentifierValidator#checkIdentifierAgainstValidator(String,IdentifierValidator)
	 */
	@Test
	public void checkIdentifierAgainstValidator_shouldPassValidationIfValidatorIsNull() {
		PatientIdentifierValidator.checkIdentifierAgainstValidator("7TU-4", null);
	}
	
	/**
	 * @see PatientIdentifierValidator#validateIdentifier(String,PatientIdentifierType)
	 */
	@Test(expected = BlankIdentifierException.class)
	public void validateIdentifier_shouldFailValidationIfIdentifierIsBlank() {
		PatientIdentifier identifier = new PatientIdentifier("", new PatientIdentifierType(1), new Location(1));
		PatientIdentifierValidator.validateIdentifier(identifier);
	}
	
	/**
	 * @see PatientIdentifierValidator#validateIdentifier(PatientIdentifier)
	 */
	@Test
	public void validateIdentifier_shouldPassIfInUseAndIdTypeUniquenessIsSetToNonUnique() {
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
	 * @see PatientIdentifierValidator#validateIdentifier(PatientIdentifier)
	 */
	@Test
	public void validateIdentifier_shouldPassIfLocationBehaviorIsNotUsedAndLocationIsNull() {
		PatientIdentifier pi = new PatientIdentifier("1TU-8", new PatientIdentifierType(1), null);
		PatientIdentifierType idType = pi.getIdentifierType();
		idType.setLocationBehavior(PatientIdentifierType.LocationBehavior.NOT_USED);
		PatientIdentifierValidator.validateIdentifier(pi);
	}
	
	/**
	 * @see PatientIdentifierValidator#validateIdentifier(PatientIdentifier)
	 */
	@Test(expected = PatientIdentifierException.class)
	public void validateIdentifier_shouldPassIfLocationBehaviorIsRequiredAndLocationIsNull() {
		PatientIdentifier pi = new PatientIdentifier("1TU-8", new PatientIdentifierType(1), null);
		PatientIdentifierType idType = pi.getIdentifierType();
		idType.setLocationBehavior(PatientIdentifierType.LocationBehavior.REQUIRED);
		PatientIdentifierValidator.validateIdentifier(pi);
	}
	
	/**
	 * @see PatientIdentifierValidator#validateIdentifier(PatientIdentifier)
	 */
	@Test
	public void validate_shouldPassValidationIfFieldLengthsAreCorrect() {
		PatientIdentifier pi = new PatientIdentifier("1TU-8", new PatientIdentifierType(1), null);
		PatientIdentifierType idType = pi.getIdentifierType();
		idType.setLocationBehavior(PatientIdentifierType.LocationBehavior.NOT_USED);
		pi.setVoidReason("voidReason");
		
		Errors errors = new BindException(pi, "pi");
		new PatientIdentifierValidator().validate(pi, errors);
		
		Assert.assertFalse(errors.hasErrors());
	}
	
	/**
	 * @see PatientIdentifierValidator#validateIdentifier(PatientIdentifier)
	 */
	@Test
	public void validate_shouldFailValidationIfFieldLengthsAreNotCorrect() {
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
	 * @see PatientIdentifierValidator#checkIdentifierAgainstFormat(String,String,String)
	 */
	@Test
	public void checkIdentifierAgainstFormat_shouldIncludeFormatInErrorMessageIfNoFormatDescriptionIsSpecified()
	{
		
		String format = "\\d+";
		String formatDescription = null;
		String expectedErrorMessage = "Identifier \"abc\" does not match : \"\\d+\"";
		
		Mockito.when(
		    messageSourceService.getMessage(eq("PatientIdentifier.error.invalidFormat"),
		        aryEq(new String[] { "abc", format }), isA(Locale.class))).thenReturn(expectedErrorMessage);
		
		expectedException.expect(InvalidIdentifierFormatException.class);
		//expectedException.expectMessage(expectedErrorMessage);
		
		PatientIdentifierValidator.checkIdentifierAgainstFormat("abc", format, formatDescription);
		
	}
	
	/**
	 * @see PatientIdentifierValidator#checkIdentifierAgainstFormat(String,String,String)
	 */
	@Test
	public void checkIdentifierAgainstFormat_shouldIncludeFormatDescriptionInErrorMessageIfSpecified() {
		
		String format = "\\d+";
		String formatDescription = "formatDescription";
		String expectedErrorMessage = "Identifier \"abc\" does not match : \"formatDescription\"";
		
		Mockito.when(
		    messageSourceService.getMessage(eq("PatientIdentifier.error.invalidFormat"), aryEq(new String[] { "abc",
		            formatDescription }), isA(Locale.class))).thenReturn(expectedErrorMessage);
		
		expectedException.expect(InvalidIdentifierFormatException.class);
		//expectedException.expectMessage(expectedErrorMessage);
		
		PatientIdentifierValidator.checkIdentifierAgainstFormat("abc", format, formatDescription);
		
	}
	
}
