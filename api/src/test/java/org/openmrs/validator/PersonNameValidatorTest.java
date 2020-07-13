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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.openmrs.test.matchers.HasFieldErrors.hasFieldErrors;
import static org.openmrs.test.matchers.HasGlobalErrors.hasGlobalErrors;

import java.util.HashMap;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.openmrs.GlobalProperty;
import org.openmrs.PersonName;
import org.openmrs.api.context.Context;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;
import org.openmrs.util.OpenmrsConstants;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.validation.MapBindingResult;

/**
 * Tests methods on the {@link PersonNameValidator} class.
 */
public class PersonNameValidatorTest extends BaseContextSensitiveTest {
	
	private static String STRING_OF_50 = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";
	
	private static String STRING_OF_51 = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";
	
	private static String STRING_OF_256 = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";
	
	private PersonNameValidator validator;
	
	private PersonName personName;
	
	private Errors errors;
	
	@BeforeEach
	public void setUp() {
		validator = new PersonNameValidator();
		
		personName = new PersonName();
		
		errors = new BindException(personName, "personName");
		
		Context.getAdministrationService().saveGlobalProperty(
		    new GlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_PATIENT_NAME_REGEX, "^[a-zA-Z \\-]+$"));
	}
	
	/**
	 * @see PatientNameValidator#validate(java.lang.Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfPersonNameObjectIsNull() {
		
		validator.validate(null, errors);
		
		assertThat(errors, hasGlobalErrors("error.name"));
	}
	
	/**
	 * @see PatientNameValidator#validatePersonName(java.lang.Object, org.springframework.validation.Errors, boolean, boolean)
	 */
	@Test
	public void validate_shouldFailValidationIfPersonNameGivenNameIsNull() {
		
		validator.validatePersonName(personName, errors, false, true);
		
		assertThat(errors, hasFieldErrors("givenName", "Patient.names.required.given.family"));
	}
	
	/**
	 * @see PatientNameValidator#validatePersonName(java.lang.Object, org.springframework.validation.Errors, boolean, boolean)
	 */
	@Test
	public void validate_shouldFailValidationIfPersonNameGivenNameIsEmpty() {
		
		personName.setGivenName("");
		
		validator.validatePersonName(personName, errors, false, true);
		
		assertThat(errors, hasFieldErrors("givenName", "Patient.names.required.given.family"));
	}
	
	/**
	 * @see PatientNameValidator#validatePersonName(java.lang.Object, org.springframework.validation.Errors, boolean, boolean)
	 */
	@Test
	public void validate_shouldFailValidationIfPersonNameGivenNameIsJustSpaces() {
		
		personName.setGivenName("    ");
		
		validator.validatePersonName(personName, errors, false, true);
		
		assertThat(errors, hasFieldErrors("givenName", "Patient.names.required.given.family"));
	}
	
	/**
	 * @see PatientNameValidator#validatePersonName(java.lang.Object, org.springframework.validation.Errors, boolean, boolean)
	 */
	@Test
	public void validate_shouldFailValidationIfPersonNameGivenNameIsSpacesSurroundedByQuotationMarks() {
		
		personName.setGivenName("\"   \"");
		
		validator.validatePersonName(personName, errors, false, true);
		
		assertThat(errors, hasFieldErrors("givenName", "Patient.names.required.given.family"));
	}
	
	/**
	 * @see PatientNameValidator#validatePersonName(java.lang.Object, org.springframework.validation.Errors, boolean, boolean)
	 */
	@Test
	public void validate_shouldPassValidationIfPersonNameGivenNameIsNotBlank() {
		
		personName.setGivenName("Fred");
		
		validator.validatePersonName(personName, errors, false, true);
		
		assertThat(errors, not(hasFieldErrors("givenName")));
	}
	
	/**
	 * @see PatientNameValidator#validatePersonName(java.lang.Object, org.springframework.validation.Errors, boolean, boolean)
	 */
	@Test
	public void validate_shouldPassValidationIfPersonNameFamilyNameIsNull() {
		
		validator.validatePersonName(personName, errors, false, true);
		
		assertThat(errors, not(hasFieldErrors("familyName")));
	}
	
	/**
	 * @see PatientNameValidator#validatePersonName(java.lang.Object, org.springframework.validation.Errors, boolean, boolean)
	 */
	@Test
	public void validate_shouldPassValidationIfPersonNameFamilyNameIsEmpty() {
		
		personName.setFamilyName("");
		
		validator.validatePersonName(personName, errors, false, true);
		
		assertThat(errors, not(hasFieldErrors("familyName")));
	}
	
	/**
	 * @see PatientNameValidator#validatePersonName(java.lang.Object, org.springframework.validation.Errors, boolean, boolean)
	 */
	@Test
	public void validate_shouldPassValidationIfPersonNameFamilyNameIsJustSpaces() {
		
		personName.setFamilyName("    ");
		
		validator.validatePersonName(personName, errors, false, true);
		
		assertThat(errors, not(hasFieldErrors("familyName")));
	}
	
	/**
	 * @see PatientNameValidator#validatePersonName(java.lang.Object, org.springframework.validation.Errors, boolean, boolean)
	 */
	@Test
	public void validate_shouldFailValidationIfPersonNameFamilyNameIsSpacesSurroundedByQuotationMarks() {
		
		personName.setFamilyName("\"   \"");
		
		validator.validatePersonName(personName, errors, false, true);
		
		assertThat(errors, hasFieldErrors("familyName", "FamilyName.invalid"));
	}
	
	/**
	 * @see PatientNameValidator#validatePersonName(java.lang.Object, org.springframework.validation.Errors, boolean, boolean)
	 */
	@Test
	public void validate_shouldPassValidationIfPersonNameFamilyNameIsNotBlank() {
		
		personName.setFamilyName("Rogers");
		
		validator.validatePersonName(personName, errors, false, true);
		
		assertThat(errors, not(hasFieldErrors("familyName")));
	}
	
	/**
	 * @see PatientNameValidator#validatePersonName(java.lang.Object, org.springframework.validation.Errors, boolean, boolean)
	 */
	@Test
	public void validate_shouldFailValidationIfPersonNamePrefixIsTooLong() {
		
		personName.setGivenName("givenName");
		personName.setFamilyName("familyName");
		personName.setPrefix(STRING_OF_51);
		
		validator.validatePersonName(personName, errors, false, true);
		
		assertThat(errors, hasFieldErrors("prefix", "error.exceededMaxLengthOfField"));
	}
	
	/**
	 * @see PatientNameValidator#validatePersonName(java.lang.Object, org.springframework.validation.Errors, boolean, boolean)
	 */
	@Test
	public void validate_shouldPassValidationIfPersonNamePrefixIsExactlyMaxLength() {
		
		personName.setPrefix("12345678901234567890123456789012345678901234567890"); // exactly 50 characters long
		
		validator.validatePersonName(personName, errors, false, true);
		
		assertThat(errors, not(hasFieldErrors("prefix")));
	}
	
	/**
	 * @see PatientNameValidator#validatePersonName(java.lang.Object, org.springframework.validation.Errors, boolean, boolean)
	 */
	@Test
	public void validate_shouldPassValidationIfPersonNamePrefixIsLessThanMaxFieldLength() {
		
		personName.setPrefix("1234567890");
		
		validator.validatePersonName(personName, errors, false, true);
		
		assertThat(errors, not(hasFieldErrors("prefix")));
	}
	
	/**
	 * @see PatientNameValidator#validatePersonName(java.lang.Object, org.springframework.validation.Errors, boolean, boolean)
	 */
	@Test
	public void validate_shouldFailValidationIfPersonNameGivenNameIsTooLong() {
		
		personName.setGivenName(STRING_OF_51);
		
		validator.validatePersonName(personName, errors, false, true);
		
		assertThat(errors, hasFieldErrors("givenName", "error.exceededMaxLengthOfField"));
	}
	
	/**
	 * @see PatientNameValidator#validatePersonName(java.lang.Object, org.springframework.validation.Errors, boolean, boolean)
	 */
	@Test
	public void validate_shouldPassValidationIfPersonNameGivenNameIsExactlyMaxLength() {
		
		personName.setGivenName(STRING_OF_50);
		
		validator.validatePersonName(personName, errors, false, true);
		
		assertThat(errors, not(hasFieldErrors("givenName")));
	}
	
	/**
	 * @see PatientNameValidator#validatePersonName(java.lang.Object, org.springframework.validation.Errors, boolean, boolean)
	 */
	@Test
	public void validate_shouldPassValidationIfPersonNameGivenNameIsLessThanMaxFieldLength() {
		
		personName.setGivenName("abcdefghij");
		
		validator.validatePersonName(personName, errors, false, true);
		
		assertThat(errors, not(hasFieldErrors("givenName")));
	}
	
	/**
	 * @see PatientNameValidator#validatePersonName(java.lang.Object, org.springframework.validation.Errors, boolean, boolean)
	 */
	@Test
	public void validate_shouldFailValidationIfPersonNameMiddleNameIsTooLong() {
		
		personName.setMiddleName(STRING_OF_51);
		
		validator.validatePersonName(personName, errors, false, true);
		
		assertThat(errors, hasFieldErrors("middleName", "error.exceededMaxLengthOfField"));
	}
	
	/**
	 * @see PatientNameValidator#validatePersonName(java.lang.Object, org.springframework.validation.Errors, boolean, boolean)
	 */
	@Test
	public void validate_shouldPassValidationIfPersonNameMiddleNameIsExactlyMaxLength() {
		
		personName.setMiddleName("abcdefghijabcdefghijabcdefghijabcdefghijabcdefghij"); // exactly 50 characters long
		
		validator.validatePersonName(personName, errors, false, true);
		
		assertThat(errors, not(hasFieldErrors("middleName")));
	}
	
	/**
	 * @see PatientNameValidator#validatePersonName(java.lang.Object, org.springframework.validation.Errors, boolean, boolean)
	 */
	@Test
	public void validate_shouldPassValidationIfPersonNameMiddleNameIsLessThanMaxFieldLength() {
		
		personName.setMiddleName("abcdefghij");
		
		validator.validatePersonName(personName, errors, false, true);
		
		assertThat(errors, not(hasFieldErrors("middleName")));
	}
	
	/**
	 * @see PatientNameValidator#validatePersonName(java.lang.Object, org.springframework.validation.Errors, boolean, boolean)
	 */
	@Test
	public void validate_shouldFailValidationIfPersonNameFamilyNamePrefixIsTooLong() {
		
		personName.setGivenName("givenName");
		personName.setFamilyName("familyName");
		personName.setFamilyNamePrefix(STRING_OF_51);
		
		validator.validatePersonName(personName, errors, false, true);
		
		assertThat(errors, hasFieldErrors("familyNamePrefix", "error.exceededMaxLengthOfField"));
	}
	
	/**
	 * @see PatientNameValidator#validatePersonName(java.lang.Object, org.springframework.validation.Errors, boolean, boolean)
	 */
	@Test
	public void validate_shouldPassValidationIfPersonNameFamilyNamePrefixIsExactlyMaxLength() {
		
		personName.setFamilyNamePrefix("12345678901234567890123456789012345678901234567890"); // exactly 50 characters long
		
		validator.validatePersonName(personName, errors, false, true);
		
		assertThat(errors, not(hasFieldErrors("familyNamePrefix")));
	}
	
	/**
	 * @see PatientNameValidator#validatePersonName(java.lang.Object, org.springframework.validation.Errors, boolean, boolean)
	 */
	@Test
	public void validate_shouldPassValidationIfPersonNameFamilyNamePrefixIsLessThanMaxFieldLength() {
		
		personName.setFamilyNamePrefix("1234567890");
		
		validator.validatePersonName(personName, errors, false, true);
		
		assertThat(errors, not(hasFieldErrors("familyNamePrefix")));
	}
	
	/**
	 * @see PatientNameValidator#validatePersonName(java.lang.Object, org.springframework.validation.Errors, boolean, boolean)
	 */
	@Test
	public void validate_shouldFailValidationIfPersonNameFamilyNameIsTooLong() {
		
		personName.setFamilyName(STRING_OF_51);
		
		validator.validatePersonName(personName, errors, false, true);
		
		assertThat(errors, hasFieldErrors("familyName", "error.exceededMaxLengthOfField"));
	}
	
	/**
	 * @see PatientNameValidator#validatePersonName(java.lang.Object, org.springframework.validation.Errors, boolean, boolean)
	 */
	@Test
	public void validate_shouldPassValidationIfPersonNameFamilyNameIsExactlyMaxLength() {
		
		personName.setFamilyName("abcdefghijabcdefghijabcdefghijabcdefghijabcdefghij"); // exactly 50 characters long
		
		validator.validatePersonName(personName, errors, false, true);
		
		assertThat(errors, not(hasFieldErrors("familyName")));
	}
	
	/**
	 * @see PatientNameValidator#validatePersonName(java.lang.Object, org.springframework.validation.Errors, boolean, boolean)
	 */
	@Test
	public void validate_shouldPassValidationIfPersonNameFamilyNameIsLessThanMaxFieldLength() {
		
		personName.setFamilyName("abcdefghij");
		
		validator.validatePersonName(personName, errors, false, true);
		
		assertThat(errors, not(hasFieldErrors("familyName")));
	}
	
	/**
	 * @see PatientNameValidator#validatePersonName(java.lang.Object, org.springframework.validation.Errors, boolean, boolean)
	 */
	@Test
	public void validate_shouldFailValidationIfPersonNameFamilyName2IsTooLong() {
		
		personName.setFamilyName2(STRING_OF_51);
		
		validator.validatePersonName(personName, errors, false, true);
		
		assertThat(errors, hasFieldErrors("familyName2", "error.exceededMaxLengthOfField"));
	}
	
	/**
	 * @see PatientNameValidator#validatePersonName(java.lang.Object, org.springframework.validation.Errors, boolean, boolean)
	 */
	@Test
	public void validate_shouldPassValidationIfPersonNameFamilyName2IsExactlyMaxLength() {
		
		personName.setFamilyName2("abcdefghijabcdefghijabcdefghijabcdefghijabcdefghij"); // exactly 50 characters long
		
		validator.validatePersonName(personName, errors, false, true);
		
		assertThat(errors, not(hasFieldErrors("familyName2")));
	}
	
	/**
	 * @see PatientNameValidator#validatePersonName(java.lang.Object, org.springframework.validation.Errors, boolean, boolean)
	 */
	@Test
	public void validate_shouldPassValidationIfPersonNameFamilyName2IsLessThanMaxFieldLength() {
		
		personName.setFamilyName2("abcdefghij");
		
		validator.validatePersonName(personName, errors, false, true);
		
		assertThat(errors, not(hasFieldErrors("familyName2")));
	}
	
	/**
	 * @see PatientNameValidator#validatePersonName(java.lang.Object, org.springframework.validation.Errors, boolean, boolean)
	 */
	@Test
	public void validate_shouldFailValidationIfPersonNameFamilyNameSuffixIsTooLong() {
		
		personName.setGivenName("givenName");
		personName.setFamilyName("familyName");
		personName.setFamilyNameSuffix(STRING_OF_51);
		
		validator.validatePersonName(personName, errors, false, true);
		
		assertThat(errors, hasFieldErrors("familyNameSuffix", "error.exceededMaxLengthOfField"));
	}
	
	/**
	 * @see PatientNameValidator#validatePersonName(java.lang.Object, org.springframework.validation.Errors, boolean, boolean)
	 */
	@Test
	public void validate_shouldPassValidationIfPersonNameFamilyNameSuffixIsExactlyMaxLength() {
		
		personName.setFamilyNameSuffix(STRING_OF_50);
		
		validator.validatePersonName(personName, errors, false, true);
		
		assertThat(errors, not(hasFieldErrors("familyNameSuffix")));
	}
	
	/**
	 * @see PatientNameValidator#validatePersonName(java.lang.Object, org.springframework.validation.Errors, boolean, boolean)
	 */
	@Test
	public void validate_shouldPassValidationIfPersonNameFamilyNameSuffixIsLessThanMaxFieldLength() {
		
		personName.setFamilyNameSuffix("1234567890");
		
		validator.validatePersonName(personName, errors, false, true);
		
		assertThat(errors, not(hasFieldErrors("familyNameSuffix")));
	}
	
	/**
	 * @see PatientNameValidator#validatePersonName(java.lang.Object, org.springframework.validation.Errors, boolean, boolean)
	 */
	@Test
	public void validate_shouldFailValidationIfPersonNameDegreeIsTooLong() {
		
		personName.setGivenName("givenName");
		personName.setFamilyName("familyName");
		personName.setDegree(STRING_OF_51);
		
		validator.validatePersonName(personName, errors, false, true);
		
		assertThat(errors, hasFieldErrors("degree", "error.exceededMaxLengthOfField"));
	}
	
	/**
	 * @see PatientNameValidator#validatePersonName(java.lang.Object, org.springframework.validation.Errors, boolean, boolean)
	 */
	@Test
	public void validate_shouldPassValidationIfPersonNameDegreeIsExactlyMaxLength() {
		
		personName.setDegree(STRING_OF_50);
		
		validator.validatePersonName(personName, errors, false, true);
		
		assertThat(errors, not(hasFieldErrors("degree")));
	}
	
	/**
	 * @see PatientNameValidator#validatePersonName(java.lang.Object, org.springframework.validation.Errors, boolean, boolean)
	 */
	@Test
	public void validate_shouldPassValidationIfPersonNameDegreeIsLessThanMaxFieldLength() {
		
		personName.setDegree("1234567890");
		
		validator.validatePersonName(personName, errors, false, true);
		
		assertThat(errors, not(hasFieldErrors("degree")));
	}
	
	/**
	 * @see PatientNameValidator#validatePersonName(java.lang.Object, org.springframework.validation.Errors, boolean, boolean)
	 */
	@Test
	public void validate_shouldFailValidationIfPersonNameGivenNameIsInvalid() {
		
		personName.setGivenName("34dfgd");
		
		validator.validatePersonName(personName, errors, false, true);
		
		assertThat(errors, hasFieldErrors("givenName", "GivenName.invalid"));
	}
	
	/**
	 * @see PatientNameValidator#validatePersonName(java.lang.Object, org.springframework.validation.Errors, boolean, boolean)
	 */
	@Test
	public void validate_shouldPassValidationIfPersonNameGivenNameIsValid() {
		
		personName.setGivenName("alex");
		
		validator.validatePersonName(personName, errors, false, true);
		
		assertThat(errors, not(hasFieldErrors("givenName")));
	}
	
	/**
	 * @see PatientNameValidator#validatePersonName(java.lang.Object, org.springframework.validation.Errors, boolean, boolean)
	 */
	@Test
	public void validate_shouldFailValidationIfPersonNameMiddleNameIsInvalid() {
		
		personName.setMiddleName("34dfgd");
		
		validator.validatePersonName(personName, errors, false, true);
		
		assertThat(errors, hasFieldErrors("middleName", "MiddleName.invalid"));
	}
	
	/**
	 * @see PatientNameValidator#validatePersonName(java.lang.Object, org.springframework.validation.Errors, boolean, boolean)
	 */
	@Test
	public void validate_shouldPassValidationIfPersonNameMiddleNameIsValid() {
		
		personName.setMiddleName("de");
		
		validator.validatePersonName(personName, errors, false, true);
		
		assertThat(errors, not(hasFieldErrors("middleName")));
	}
	
	/**
	 * @see PatientNameValidator#validatePersonName(java.lang.Object, org.springframework.validation.Errors, boolean, boolean)
	 */
	@Test
	public void validate_shouldFailValidationIfPersonNameFamilyNameIsInvalid() {
		
		personName.setFamilyName("34dfgd");
		
		validator.validatePersonName(personName, errors, false, true);
		
		assertThat(errors, hasFieldErrors("familyName", "FamilyName.invalid"));
	}
	
	/**
	 * @see PatientNameValidator#validatePersonName(java.lang.Object, org.springframework.validation.Errors, boolean, boolean)
	 */
	@Test
	public void validate_shouldPassValidationIfPersonNameFamilyNameIsValid() {
		
		personName.setFamilyName("souza");
		
		validator.validatePersonName(personName, errors, false, true);
		
		assertThat(errors, not(hasFieldErrors("familyName")));
	}
	
	/**
	 * @see PatientNameValidator#validatePersonName(java.lang.Object, org.springframework.validation.Errors, boolean, boolean)
	 */
	@Test
	public void validate_shouldFailValidationIfPersonNameFamilyName2IsInvalid() {
		
		personName.setFamilyName2("34dfgd");
		
		validator.validatePersonName(personName, errors, false, true);
		
		assertThat(errors, hasFieldErrors("familyName2", "FamilyName2.invalid"));
	}
	
	/**
	 * @see PatientNameValidator#validatePersonName(java.lang.Object, org.springframework.validation.Errors, boolean, boolean)
	 */
	@Test
	public void validate_shouldPassValidationIfPersonNameFamilyName2IsValid() {
		
		personName.setFamilyName2("souza-");
		
		validator.validatePersonName(personName, errors, false, true);
		
		assertThat(errors, not(hasFieldErrors("familyName2")));
	}
	
	/**
	 * @see PatientNameValidator#validatePersonName(java.lang.Object, org.springframework.validation.Errors, boolean, boolean)
	 */
	@Test
	public void validate_shouldSkipRegexValidationIfValidationStringIsNull() {
		
		Context.getAdministrationService().saveGlobalProperty(
		    new GlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_PATIENT_NAME_REGEX, null));
		personName.setFamilyName("asd123");
		
		validator.validatePersonName(personName, errors, false, true);
		
		assertThat(errors, not(hasFieldErrors("familyName")));
	}
	
	/**
	 * @see PatientNameValidator#validatePersonName(java.lang.Object, org.springframework.validation.Errors, boolean, boolean)
	 */
	@Test
	public void validate_shouldSkipRegexValidationIfValidationStringIsEmpty() {
		
		Context.getAdministrationService().saveGlobalProperty(
		    new GlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_PATIENT_NAME_REGEX, ""));
		personName.setGivenName("123asd");
		
		validator.validatePersonName(personName, errors, false, true);
		
		assertThat(errors, not(hasFieldErrors("givenName")));
	}
	
	/**
	 * @see PersonNameValidator#validatePersonName(PersonName,Errors,null,null)
	 */
	@Test
	public void validatePersonName_shouldNotValidateAgainstRegexForBlankNames() {
		
		personName.setGivenName("given");
		personName.setFamilyName("family");
		personName.setMiddleName("");
		personName.setFamilyName2("");
		
		validator.validatePersonName(personName, errors, false, true);
		
		assertFalse(errors.hasErrors());
	}
	
	/**
	 * @see PersonNameValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldPassValidationIfNameIsInvalidButVoided() {
		
		personName.setVoided(true);
		personName.setFamilyName2("34dfgd"); // invalid
		
		validator.validate(personName, errors);
		
		assertFalse(errors.hasErrors());
	}
	
	/**
	 * @see PersonNameValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldPassValidationIfFieldLengthsAreCorrect() {
		
		personName.setPrefix("prefix");
		personName.setGivenName("givenName");
		personName.setMiddleName("middleName");
		personName.setFamilyNamePrefix("familyNamePrefix");
		personName.setFamilyName("familyName");
		personName.setFamilyName2("familyName");
		personName.setFamilyNameSuffix("familyNameSuffix");
		personName.setDegree("degree");
		personName.setVoidReason("voidReason");
		
		validator.validate(personName, errors);
		
		assertFalse(errors.hasErrors());
	}
	
	/**
	 * @see PersonNameValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfFieldLengthsAreNotCorrect() {
		
		personName.setPrefix(STRING_OF_51);
		personName.setGivenName(STRING_OF_51);
		personName.setMiddleName(STRING_OF_51);
		personName.setFamilyName(STRING_OF_51);
		personName.setFamilyNamePrefix(STRING_OF_51);
		personName.setFamilyName(STRING_OF_51);
		personName.setFamilyName2(STRING_OF_51);
		personName.setFamilyNameSuffix(STRING_OF_51);
		personName.setDegree(STRING_OF_51);
		personName.setVoidReason(STRING_OF_256);
		
		validator.validate(personName, errors);
		
		Stream.of("prefix", "givenName", "familyNamePrefix", "familyName", "familyName2", "familyNameSuffix", "degree",
		    "middleName", "voidReason")
		        .forEach(f -> assertThat(errors, hasFieldErrors(f, "error.exceededMaxLengthOfField")));
	}

    /**
     * @see PersonNameValidator#validatePersonName(PersonName, Errors, Boolean, Boolean)
     */
    @Test
	public void validatePersonName_shouldReportErrorsWithNonStandardPrefixWhenCalledInHistoricWay() {
		
		PersonName personName = new PersonName("", "reb", "feb");
		MapBindingResult errors = new MapBindingResult(new HashMap<String, Object>(), "personName");
		
		validator.validatePersonName(personName, errors, true, false);
		
		assertThat(errors, hasFieldErrors("names[0]." + "givenName"));
	}

	/**
	 * @see PersonNameValidator#validate(Object, Errors)
	 */
	@Test
	public void validate_shouldReportErrorsOnCorrectFieldNames() {
		
		PersonName personName = new PersonName("", "reb", "feb");
		MapBindingResult errors = new MapBindingResult(new HashMap<String, Object>(), "personName");
		
		validator.validate(personName, errors);
		
		assertThat(errors, hasFieldErrors("givenName"));
	}
	
	/**
	 * @see PatientNameValidator#validatePersonName(java.lang.Object, org.springframework.validation.Errors, boolean, boolean)
	 */
	@Test
	@Disabled("Unignore after investigating and fixing - RA-543")
	public void validate_shouldFailValidationIfPersonNameGivenNameHasLeadingSpaces() {
		
		personName.setGivenName(" alex");
		
		validator.validatePersonName(personName, errors, false, true);
		
		assertThat(errors, hasFieldErrors("givenName"));
	}

	/**
	 * @see PatientNameValidator#validatePersonName(java.lang.Object, org.springframework.validation.Errors, boolean, boolean)
	 */
	@Test
	@Disabled("Unignore after investigating and fixing - RA-543")
	public void validate_shouldFailValidationIfPersonNameGivenNameHasTrailingSpaces() {
		
		personName.setGivenName("alex ");
		
		validator.validatePersonName(personName, errors, false, true);
		
		assertThat(errors, hasFieldErrors("givenName"));
	}
	
	/**
	 * @see PatientNameValidator#validatePersonName(java.lang.Object, org.springframework.validation.Errors, boolean, boolean)
	 */
	@Test
	@Disabled("Unignore after investigating and fixing - RA-543")
	public void validate_shouldFailValidationIfPersonNameMiddleNameHasLeadingSpaces() {
		
		personName.setMiddleName(" de");
		
		validator.validatePersonName(personName, errors, false, true);
		
		assertThat(errors, hasFieldErrors("middleName"));
	}
	
	/**
	 * @see PatientNameValidator#validatePersonName(java.lang.Object, org.springframework.validation.Errors, boolean, boolean)
	 */
	@Test
	@Disabled("Unignore after investigating and fixing - RA-543")
	public void validate_shouldFailValidationIfPersonNameMiddleNameHasTrailingSpaces() {
		
		personName.setMiddleName("de ");
		
		validator.validatePersonName(personName, errors, false, true);
		
		assertThat(errors, hasFieldErrors("middleName"));
	}

	/**
	 * @see PatientNameValidator#validatePersonName(java.lang.Object, org.springframework.validation.Errors, boolean, boolean)
	 */
	@Test
	@Disabled("Unignore after investigating and fixing - RA-543")
	public void validate_shouldFailValidationIfPersonNameFamilyNameHasLeadingSpaces() {
		
		personName.setFamilyName(" souza");
		
		validator.validatePersonName(personName, errors, false, true);
		
		assertThat(errors, hasFieldErrors("familyName"));
	}

	/**
	 * @see PatientNameValidator#validatePersonName(java.lang.Object, org.springframework.validation.Errors, boolean, boolean)
	 */
	@Test
	@Disabled("Unignore after investigating and fixing - RA-543")
	public void validate_shouldFailValidationIfPersonNameFamilyNameHasTrailingSpaces() {
		
		personName.setFamilyName("souza ");
		
		validator.validatePersonName(personName, errors, false, true);
		
		assertThat(errors, hasFieldErrors("familyName"));
	}
	
	/**
	 * @see PatientNameValidator#validatePersonName(java.lang.Object, org.springframework.validation.Errors, boolean, boolean)
	 */
	@Test
	@Disabled("Unignore after investigating and fixing - RA-543")
	public void validate_shouldFailValidationIfPersonNameFamilyName2HasLeadingSpaces() {
		
		personName.setFamilyName2(" souza-");
		
		validator.validatePersonName(personName, errors, false, true);
		
		assertThat(errors, hasFieldErrors("familyName2"));
	}

	/**
	 * @see PatientNameValidator#validatePersonName(java.lang.Object, org.springframework.validation.Errors, boolean, boolean)
	 */
	@Test
	@Disabled("Unignore after investigating and fixing - RA-543")
	public void validate_shouldFailValidationIfPersonNameFamilyName2HasTrailingSpaces() {
		
		personName.setFamilyName2("souza- ");
		
		validator.validatePersonName(personName, errors, false, true);
		
		assertThat(errors, hasFieldErrors("familyName2"));
	}
}
