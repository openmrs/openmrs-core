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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.openmrs.test.matchers.HasFieldErrors.hasFieldErrors;
import static org.openmrs.test.matchers.HasGlobalErrors.hasGlobalErrors;

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
	 * @see PersonNameValidator#validate(java.lang.Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfPersonNameObjectIsNull() {

		validator.validate(null, errors);

		assertThat(errors, hasGlobalErrors("error.name"));
	}

	/**
	 * @see PersonNameValidator#validate(java.lang.Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfPersonNameGivenNameIsNull() {

		validator.validate(personName, errors);

		assertThat(errors, hasFieldErrors("givenName", "Patient.names.required.given.family"));
	}

	/**
	 * @see PersonNameValidator#validate(java.lang.Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfPersonNameGivenNameIsEmpty() {

		personName.setGivenName("");

		validator.validate(personName, errors);

		assertThat(errors, hasFieldErrors("givenName", "Patient.names.required.given.family"));
	}

	/**
	 * @see PersonNameValidator#validate(java.lang.Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfPersonNameGivenNameIsJustSpaces() {

		personName.setGivenName("    ");

		validator.validate(personName, errors);

		assertThat(errors, hasFieldErrors("givenName", "Patient.names.required.given.family"));
	}

	/**
	 * @see PersonNameValidator#validate(java.lang.Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfPersonNameGivenNameIsSpacesSurroundedByQuotationMarks() {

		personName.setGivenName("\"   \"");

		validator.validate(personName, errors);

		assertThat(errors, hasFieldErrors("givenName", "Patient.names.required.given.family"));
	}

	/**
	 * @see PersonNameValidator#validate(java.lang.Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldPassValidationIfPersonNameGivenNameIsNotBlank() {

		personName.setGivenName("Fred");

		validator.validate(personName, errors);

		assertThat(errors, not(hasFieldErrors("givenName")));
	}

	/**
	 * @see PersonNameValidator#validate(java.lang.Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldPassValidationIfPersonNameFamilyNameIsNull() {

		personName.setGivenName("Fred");

		validator.validate(personName, errors);

		assertThat(errors, not(hasFieldErrors("familyName")));
	}

	/**
	 * @see PersonNameValidator#validate(java.lang.Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldPassValidationIfPersonNameFamilyNameIsEmpty() {

		personName.setGivenName("Fred");
		personName.setFamilyName("");

		validator.validate(personName, errors);

		assertThat(errors, not(hasFieldErrors("familyName")));
	}

	/**
	 * @see PersonNameValidator#validate(java.lang.Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldPassValidationIfPersonNameFamilyNameIsJustSpaces() {

		personName.setGivenName("Fred");
		personName.setFamilyName("    ");

		validator.validate(personName, errors);

		assertThat(errors, not(hasFieldErrors("familyName")));
	}

	/**
	 * @see PersonNameValidator#validate(java.lang.Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfPersonNameFamilyNameIsSpacesSurroundedByQuotationMarks() {

		personName.setGivenName("Fred");
		personName.setFamilyName("\"   \"");

		validator.validate(personName, errors);

		assertThat(errors, hasFieldErrors("familyName", "FamilyName.invalid"));
	}

	/**
	 * @see PersonNameValidator#validate(java.lang.Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldPassValidationIfPersonNameFamilyNameIsNotBlank() {

		personName.setGivenName("Fred");
		personName.setFamilyName("Rogers");

		validator.validate(personName, errors);

		assertThat(errors, not(hasFieldErrors("familyName")));
	}

	/**
	 * @see PersonNameValidator#validate(java.lang.Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfPersonNamePrefixIsTooLong() {

		personName.setGivenName("givenName");
		personName.setFamilyName("familyName");
		personName.setPrefix(STRING_OF_51);

		validator.validate(personName, errors);

		assertThat(errors, hasFieldErrors("prefix", "error.exceededMaxLengthOfField"));
	}

	/**
	 * @see PersonNameValidator#validate(java.lang.Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldPassValidationIfPersonNamePrefixIsExactlyMaxLength() {

		personName.setGivenName("givenName");
		personName.setPrefix("12345678901234567890123456789012345678901234567890");

		validator.validate(personName, errors);

		assertThat(errors, not(hasFieldErrors("prefix")));
	}

	/**
	 * @see PersonNameValidator#validate(java.lang.Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldPassValidationIfPersonNamePrefixIsLessThanMaxFieldLength() {

		personName.setGivenName("givenName");
		personName.setPrefix("1234567890");

		validator.validate(personName, errors);

		assertThat(errors, not(hasFieldErrors("prefix")));
	}

	/**
	 * @see PersonNameValidator#validate(java.lang.Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfPersonNameGivenNameIsTooLong() {

		personName.setGivenName(STRING_OF_51);

		validator.validate(personName, errors);

		assertThat(errors, hasFieldErrors("givenName", "error.exceededMaxLengthOfField"));
	}

	/**
	 * @see PersonNameValidator#validate(java.lang.Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldPassValidationIfPersonNameGivenNameIsExactlyMaxLength() {

		personName.setGivenName(STRING_OF_50);

		validator.validate(personName, errors);

		assertThat(errors, not(hasFieldErrors("givenName")));
	}

	/**
	 * @see PersonNameValidator#validate(java.lang.Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldPassValidationIfPersonNameGivenNameIsLessThanMaxFieldLength() {

		personName.setGivenName("abcdefghij");

		validator.validate(personName, errors);

		assertThat(errors, not(hasFieldErrors("givenName")));
	}

	/**
	 * @see PersonNameValidator#validate(java.lang.Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfPersonNameMiddleNameIsTooLong() {

		personName.setGivenName("givenName");
		personName.setMiddleName(STRING_OF_51);

		validator.validate(personName, errors);

		assertThat(errors, hasFieldErrors("middleName", "error.exceededMaxLengthOfField"));
	}

	/**
	 * @see PersonNameValidator#validate(java.lang.Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldPassValidationIfPersonNameMiddleNameIsExactlyMaxLength() {

		personName.setGivenName("givenName");
		personName.setMiddleName("abcdefghijabcdefghijabcdefghijabcdefghijabcdefghij");

		validator.validate(personName, errors);

		assertThat(errors, not(hasFieldErrors("middleName")));
	}

	/**
	 * @see PersonNameValidator#validate(java.lang.Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldPassValidationIfPersonNameMiddleNameIsLessThanMaxFieldLength() {

		personName.setGivenName("givenName");
		personName.setMiddleName("abcdefghij");

		validator.validate(personName, errors);

		assertThat(errors, not(hasFieldErrors("middleName")));
	}

	/**
	 * @see PersonNameValidator#validate(java.lang.Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfPersonNameFamilyNamePrefixIsTooLong() {

		personName.setGivenName("givenName");
		personName.setFamilyName("familyName");
		personName.setFamilyNamePrefix(STRING_OF_51);

		validator.validate(personName, errors);

		assertThat(errors, hasFieldErrors("familyNamePrefix", "error.exceededMaxLengthOfField"));
	}

	/**
	 * @see PersonNameValidator#validate(java.lang.Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldPassValidationIfPersonNameFamilyNamePrefixIsExactlyMaxLength() {

		personName.setGivenName("givenName");
		personName.setFamilyNamePrefix("12345678901234567890123456789012345678901234567890");

		validator.validate(personName, errors);

		assertThat(errors, not(hasFieldErrors("familyNamePrefix")));
	}

	/**
	 * @see PersonNameValidator#validate(java.lang.Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldPassValidationIfPersonNameFamilyNamePrefixIsLessThanMaxFieldLength() {

		personName.setGivenName("givenName");
		personName.setFamilyNamePrefix("1234567890");

		validator.validate(personName, errors);

		assertThat(errors, not(hasFieldErrors("familyNamePrefix")));
	}

	/**
	 * @see PersonNameValidator#validate(java.lang.Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfPersonNameFamilyNameIsTooLong() {

		personName.setGivenName("givenName");
		personName.setFamilyName(STRING_OF_51);

		validator.validate(personName, errors);

		assertThat(errors, hasFieldErrors("familyName", "error.exceededMaxLengthOfField"));
	}

	/**
	 * @see PersonNameValidator#validate(java.lang.Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldPassValidationIfPersonNameFamilyNameIsExactlyMaxLength() {

		personName.setGivenName("givenName");
		personName.setFamilyName("abcdefghijabcdefghijabcdefghijabcdefghijabcdefghij");

		validator.validate(personName, errors);

		assertThat(errors, not(hasFieldErrors("familyName")));
	}

	/**
	 * @see PersonNameValidator#validate(java.lang.Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldPassValidationIfPersonNameFamilyNameIsLessThanMaxFieldLength() {

		personName.setGivenName("givenName");
		personName.setFamilyName("abcdefghij");

		validator.validate(personName, errors);

		assertThat(errors, not(hasFieldErrors("familyName")));
	}

	/**
	 * @see PersonNameValidator#validate(java.lang.Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfPersonNameFamilyName2IsTooLong() {

		personName.setGivenName("givenName");
		personName.setFamilyName2(STRING_OF_51);

		validator.validate(personName, errors);

		assertThat(errors, hasFieldErrors("familyName2", "error.exceededMaxLengthOfField"));
	}

	/**
	 * @see PersonNameValidator#validate(java.lang.Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldPassValidationIfPersonNameFamilyName2IsExactlyMaxLength() {

		personName.setGivenName("givenName");
		personName.setFamilyName2("abcdefghijabcdefghijabcdefghijabcdefghijabcdefghij");

		validator.validate(personName, errors);

		assertThat(errors, not(hasFieldErrors("familyName2")));
	}

	/**
	 * @see PersonNameValidator#validate(java.lang.Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldPassValidationIfPersonNameFamilyName2IsLessThanMaxFieldLength() {

		personName.setGivenName("givenName");
		personName.setFamilyName2("abcdefghij");

		validator.validate(personName, errors);

		assertThat(errors, not(hasFieldErrors("familyName2")));
	}

	/**
	 * @see PersonNameValidator#validate(java.lang.Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfPersonNameFamilyNameSuffixIsTooLong() {

		personName.setGivenName("givenName");
		personName.setFamilyName("familyName");
		personName.setFamilyNameSuffix(STRING_OF_51);

		validator.validate(personName, errors);

		assertThat(errors, hasFieldErrors("familyNameSuffix", "error.exceededMaxLengthOfField"));
	}

	/**
	 * @see PersonNameValidator#validate(java.lang.Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldPassValidationIfPersonNameFamilyNameSuffixIsExactlyMaxLength() {

		personName.setGivenName("givenName");
		personName.setFamilyNameSuffix(STRING_OF_50);

		validator.validate(personName, errors);

		assertThat(errors, not(hasFieldErrors("familyNameSuffix")));
	}

	/**
	 * @see PersonNameValidator#validate(java.lang.Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldPassValidationIfPersonNameFamilyNameSuffixIsLessThanMaxFieldLength() {

		personName.setGivenName("givenName");
		personName.setFamilyNameSuffix("1234567890");

		validator.validate(personName, errors);

		assertThat(errors, not(hasFieldErrors("familyNameSuffix")));
	}

	/**
	 * @see PersonNameValidator#validate(java.lang.Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfPersonNameDegreeIsTooLong() {

		personName.setGivenName("givenName");
		personName.setFamilyName("familyName");
		personName.setDegree(STRING_OF_51);

		validator.validate(personName, errors);

		assertThat(errors, hasFieldErrors("degree", "error.exceededMaxLengthOfField"));
	}

	/**
	 * @see PersonNameValidator#validate(java.lang.Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldPassValidationIfPersonNameDegreeIsExactlyMaxLength() {

		personName.setGivenName("givenName");
		personName.setDegree(STRING_OF_50);

		validator.validate(personName, errors);

		assertThat(errors, not(hasFieldErrors("degree")));
	}

	/**
	 * @see PersonNameValidator#validate(java.lang.Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldPassValidationIfPersonNameDegreeIsLessThanMaxFieldLength() {

		personName.setGivenName("givenName");
		personName.setDegree("1234567890");

		validator.validate(personName, errors);

		assertThat(errors, not(hasFieldErrors("degree")));
	}

	/**
	 * @see PersonNameValidator#validate(java.lang.Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfPersonNameGivenNameIsInvalid() {

		personName.setGivenName("34dfgd");

		validator.validate(personName, errors);

		assertThat(errors, hasFieldErrors("givenName", "GivenName.invalid"));
	}

	/**
	 * @see PersonNameValidator#validate(java.lang.Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldPassValidationIfPersonNameGivenNameIsValid() {

		personName.setGivenName("alex");

		validator.validate(personName, errors);

		assertThat(errors, not(hasFieldErrors("givenName")));
	}

	/**
	 * @see PersonNameValidator#validate(java.lang.Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfPersonNameMiddleNameIsInvalid() {

		personName.setGivenName("givenName");
		personName.setMiddleName("34dfgd");

		validator.validate(personName, errors);

		assertThat(errors, hasFieldErrors("middleName", "MiddleName.invalid"));
	}

	/**
	 * @see PersonNameValidator#validate(java.lang.Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldPassValidationIfPersonNameMiddleNameIsValid() {

		personName.setGivenName("givenName");
		personName.setMiddleName("de");

		validator.validate(personName, errors);

		assertThat(errors, not(hasFieldErrors("middleName")));
	}

	/**
	 * @see PersonNameValidator#validate(java.lang.Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfPersonNameFamilyNameIsInvalid() {

		personName.setGivenName("givenName");
		personName.setFamilyName("34dfgd");

		validator.validate(personName, errors);

		assertThat(errors, hasFieldErrors("familyName", "FamilyName.invalid"));
	}

	/**
	 * @see PersonNameValidator#validate(java.lang.Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldPassValidationIfPersonNameFamilyNameIsValid() {

		personName.setGivenName("givenName");
		personName.setFamilyName("souza");

		validator.validate(personName, errors);

		assertThat(errors, not(hasFieldErrors("familyName")));
	}

	/**
	 * @see PersonNameValidator#validate(java.lang.Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfPersonNameFamilyName2IsInvalid() {

		personName.setGivenName("givenName");
		personName.setFamilyName2("34dfgd");

		validator.validate(personName, errors);

		assertThat(errors, hasFieldErrors("familyName2", "FamilyName2.invalid"));
	}

	/**
	 * @see PersonNameValidator#validate(java.lang.Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldPassValidationIfPersonNameFamilyName2IsValid() {

		personName.setGivenName("givenName");
		personName.setFamilyName2("souza-");

		validator.validate(personName, errors);

		assertThat(errors, not(hasFieldErrors("familyName2")));
	}

	/**
	 * @see PersonNameValidator#validate(java.lang.Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldSkipRegexValidationIfValidationStringIsNull() {

		Context.getAdministrationService()
		        .saveGlobalProperty(new GlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_PATIENT_NAME_REGEX, null));
		personName.setGivenName("givenName");
		personName.setFamilyName("asd123");

		validator.validate(personName, errors);

		assertThat(errors, not(hasFieldErrors("familyName")));
	}

	/**
	 * @see PersonNameValidator#validate(java.lang.Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldSkipRegexValidationIfValidationStringIsEmpty() {

		Context.getAdministrationService()
		        .saveGlobalProperty(new GlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_PATIENT_NAME_REGEX, ""));
		personName.setGivenName("123asd");

		validator.validate(personName, errors);

		assertThat(errors, not(hasFieldErrors("givenName")));
	}

	/**
	 * @see PersonNameValidator#validate(java.lang.Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldNotValidateAgainstRegexForBlankNames() {

		personName.setGivenName("given");
		personName.setFamilyName("family");
		personName.setMiddleName("");
		personName.setFamilyName2("");

		validator.validate(personName, errors);

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
	 * @see PersonNameValidator#validate(java.lang.Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldReportErrorsOnCorrectFieldNames() {

		PersonName personName = new PersonName("", "reb", "feb");
		MapBindingResult errors = new MapBindingResult(new HashMap<String, Object>(), "personName");

		validator.validate(personName, errors);

		assertThat(errors, hasFieldErrors("givenName"));
	}

	/**
	 * @see PersonNameValidator#validate(java.lang.Object, org.springframework.validation.Errors)
	 */
	@Test
	@Disabled("Unignore after investigating and fixing - RA-543")
	public void validate_shouldFailValidationIfPersonNameGivenNameHasLeadingSpaces() {

		personName.setGivenName(" alex");

		validator.validate(personName, errors);

		assertThat(errors, hasFieldErrors("givenName"));
	}

	/**
	 * @see PersonNameValidator#validate(java.lang.Object, org.springframework.validation.Errors)
	 */
	@Test
	@Disabled("Unignore after investigating and fixing - RA-543")
	public void validate_shouldFailValidationIfPersonNameGivenNameHasTrailingSpaces() {

		personName.setGivenName("alex ");

		validator.validate(personName, errors);

		assertThat(errors, hasFieldErrors("givenName"));
	}

	/**
	 * @see PersonNameValidator#validate(java.lang.Object, org.springframework.validation.Errors)
	 */
	@Test
	@Disabled("Unignore after investigating and fixing - RA-543")
	public void validate_shouldFailValidationIfPersonNameMiddleNameHasLeadingSpaces() {

		personName.setGivenName("givenName");
		personName.setMiddleName(" de");

		validator.validate(personName, errors);

		assertThat(errors, hasFieldErrors("middleName"));
	}

	/**
	 * @see PersonNameValidator#validate(java.lang.Object, org.springframework.validation.Errors)
	 */
	@Test
	@Disabled("Unignore after investigating and fixing - RA-543")
	public void validate_shouldFailValidationIfPersonNameMiddleNameHasTrailingSpaces() {

		personName.setGivenName("givenName");
		personName.setMiddleName("de ");

		validator.validate(personName, errors);

		assertThat(errors, hasFieldErrors("middleName"));
	}

	/**
	 * @see PersonNameValidator#validate(java.lang.Object, org.springframework.validation.Errors)
	 */
	@Test
	@Disabled("Unignore after investigating and fixing - RA-543")
	public void validate_shouldFailValidationIfPersonNameFamilyNameHasLeadingSpaces() {

		personName.setGivenName("givenName");
		personName.setFamilyName(" souza");

		validator.validate(personName, errors);

		assertThat(errors, hasFieldErrors("familyName"));
	}

	/**
	 * @see PersonNameValidator#validate(java.lang.Object, org.springframework.validation.Errors)
	 */
	@Test
	@Disabled("Unignore after investigating and fixing - RA-543")
	public void validate_shouldFailValidationIfPersonNameFamilyNameHasTrailingSpaces() {

		personName.setGivenName("givenName");
		personName.setFamilyName("souza ");

		validator.validate(personName, errors);

		assertThat(errors, hasFieldErrors("familyName"));
	}

	/**
	 * @see PersonNameValidator#validate(java.lang.Object, org.springframework.validation.Errors)
	 */
	@Test
	@Disabled("Unignore after investigating and fixing - RA-543")
	public void validate_shouldFailValidationIfPersonNameFamilyName2HasLeadingSpaces() {

		personName.setGivenName("givenName");
		personName.setFamilyName2(" souza-");

		validator.validate(personName, errors);

		assertThat(errors, hasFieldErrors("familyName2"));
	}

	/**
	 * @see PersonNameValidator#validate(java.lang.Object, org.springframework.validation.Errors)
	 */
	@Test
	@Disabled("Unignore after investigating and fixing - RA-543")
	public void validate_shouldFailValidationIfPersonNameFamilyName2HasTrailingSpaces() {

		personName.setGivenName("givenName");
		personName.setFamilyName2("souza- ");

		validator.validate(personName, errors);

		assertThat(errors, hasFieldErrors("familyName2"));
	}
}
