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

import junit.framework.Assert;

import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.GlobalProperty;
import org.openmrs.PersonName;
import org.openmrs.api.context.Context;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.test.Verifies;
import org.openmrs.util.OpenmrsConstants;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;

/**
 * Tests methods on the {@link PersonNameValidator} class.
 */
public class PersonNameValidatorTest extends BaseContextSensitiveTest {
	
	/**
	 * Set the GP to a regex used during validation
	 */
	@Before
	public void createNameRegex() {
		Context.getAdministrationService().saveGlobalProperty(
		    new GlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_PATIENT_NAME_REGEX, "^[a-zA-Z \\-]+$"));
	}
	
	/**
	 * @see {@link PatientNameValidator#validate(java.lang.Object, org.springframework.validation.Errors)}
	 */
	@Test
	@Verifies(value = "should fail validation if PersonName object is null", method = "validate(java.lang.Object, org.springframework.validation.Errors)")
	public void validate_shouldFailValidationIfPersonNameObjectIsNull() throws Exception {
		PersonName personName = new PersonName();
		Errors errors = new BindException(personName, "personName");
		new PersonNameValidator().validate(null, errors);
		Assert.assertTrue(errors.hasErrors());
	}
	
	/**
	 * @see {@link PatientNameValidator#validatePersonName(java.lang.Object, org.springframework.validation.Errors, boolean, boolean)}
	 */
	@Test
	@Verifies(value = "should fail validation if PersonName.givenName is null", method = "validate(java.lang.Object, org.springframework.validation.Errors, boolean, boolean)")
	public void validate_shouldFailValidationIfPersonNameGivenNameIsNull() throws Exception {
		PersonName personName = new PersonName();
		Errors errors = new BindException(personName, "givenName");
		new PersonNameValidator().validatePersonName(personName, errors, false, true);
		Assert.assertTrue(errors.hasFieldErrors("givenName"));
	}
	
	/**
	 * @see {@link PatientNameValidator#validatePersonName(java.lang.Object, org.springframework.validation.Errors, boolean, boolean)}
	 */
	@Test
	@Verifies(value = "should fail validation if PersonName.givenName is empty", method = "validate(java.lang.Object, org.springframework.validation.Errors, boolean, boolean)")
	public void validate_shouldFailValidationIfPersonNameGivenNameIsEmpty() throws Exception {
		PersonName personName = new PersonName();
		personName.setGivenName("");
		Errors errors = new BindException(personName, "givenName");
		new PersonNameValidator().validatePersonName(personName, errors, false, true);
		Assert.assertTrue(errors.hasFieldErrors("givenName"));
	}
	
	/**
	 * @see {@link PatientNameValidator#validatePersonName(java.lang.Object, org.springframework.validation.Errors, boolean, boolean)}
	 */
	@Test
	@Verifies(value = "should fail validation if PersonName.givenName is just spaces", method = "validate(java.lang.Object, org.springframework.validation.Errors, boolean, boolean)")
	public void validate_shouldFailValidationIfPersonNameGivenNameIsJustSpaces() throws Exception {
		PersonName personName = new PersonName();
		personName.setGivenName("    ");
		Errors errors = new BindException(personName, "givenName");
		new PersonNameValidator().validatePersonName(personName, errors, false, true);
		Assert.assertTrue(errors.hasFieldErrors("givenName"));
	}
	
	/**
	 * @see {@link PatientNameValidator#validatePersonName(java.lang.Object, org.springframework.validation.Errors, boolean, boolean)}
	 */
	@Test
	@Verifies(value = "should fail validation if PersonName.givenName is spaces surrounded by quotation marks", method = "validate(java.lang.Object, org.springframework.validation.Errors, boolean, boolean)")
	public void validate_shouldFailValidationIfPersonNameGivenNameIsSpacesSurroundedByQuotationMarks() throws Exception {
		PersonName personName = new PersonName();
		personName.setGivenName("\"   \"");
		Errors errors = new BindException(personName, "givenName");
		new PersonNameValidator().validatePersonName(personName, errors, false, true);
		Assert.assertTrue(errors.hasFieldErrors("givenName"));
	}
	
	/**
	 * @see {@link PatientNameValidator#validatePersonName(java.lang.Object, org.springframework.validation.Errors, boolean, boolean)}
	 */
	@Test
	@Verifies(value = "should pass validation if PersonName.givenName is not blank", method = "validate(java.lang.Object, org.springframework.validation.Errors, boolean, boolean)")
	public void validate_shouldPassValidationIfPersonNameGivenNameIsNotBlank() throws Exception {
		PersonName personName = new PersonName();
		personName.setGivenName("Fred");
		Errors errors = new BindException(personName, "givenName");
		new PersonNameValidator().validatePersonName(personName, errors, false, true);
		Assert.assertFalse(errors.hasFieldErrors("givenName"));
		
	}
	
	/**
	 * @see {@link PatientNameValidator#validatePersonName(java.lang.Object, org.springframework.validation.Errors, boolean, boolean)}
	 */
	@Test
	@Verifies(value = "should fail validation if PersonName.familyName is null", method = "validate(java.lang.Object, org.springframework.validation.Errors, boolean, boolean)")
	public void validate_shouldFailValidationIfPersonNameFamilyNameIsNull() throws Exception {
		PersonName personName = new PersonName();
		Errors errors = new BindException(personName, "familyName");
		new PersonNameValidator().validatePersonName(personName, errors, false, true);
		Assert.assertTrue(errors.hasFieldErrors("familyName"));
	}
	
	/**
	 * @see {@link PatientNameValidator#validatePersonName(java.lang.Object, org.springframework.validation.Errors, boolean, boolean)}
	 */
	@Test
	@Verifies(value = "should fail validation if PersonName.familyName is empty", method = "validate(java.lang.Object, org.springframework.validation.Errors, boolean, boolean)")
	public void validate_shouldFailValidationIfPersonNameFamilyNameIsEmpty() throws Exception {
		PersonName personName = new PersonName();
		personName.setFamilyName("");
		Errors errors = new BindException(personName, "familyName");
		new PersonNameValidator().validatePersonName(personName, errors, false, true);
		Assert.assertTrue(errors.hasFieldErrors("familyName"));
	}
	
	/**
	 * @see {@link PatientNameValidator#validatePersonName(java.lang.Object, org.springframework.validation.Errors, boolean, boolean)}
	 */
	@Test
	@Verifies(value = "should fail validation if PersonName.familyName is just spaces", method = "validate(java.lang.Object, org.springframework.validation.Errors, boolean, boolean)")
	public void validate_shouldFailValidationIfPersonNameFamilyNameIsJustSpaces() throws Exception {
		PersonName personName = new PersonName();
		personName.setFamilyName("    ");
		Errors errors = new BindException(personName, "familyName");
		new PersonNameValidator().validatePersonName(personName, errors, false, true);
		Assert.assertTrue(errors.hasFieldErrors("familyName"));
	}
	
	/**
	 * @see {@link PatientNameValidator#validatePersonName(java.lang.Object, org.springframework.validation.Errors, boolean, boolean)}
	 */
	@Test
	@Verifies(value = "should fail validation if PersonName.familyName is spaces surrounded by quotation marks", method = "validate(java.lang.Object, org.springframework.validation.Errors, boolean, boolean)")
	public void validate_shouldFailValidationIfPersonNameFamilyNameIsSpacesSurroundedByQuotationMarks() throws Exception {
		PersonName personName = new PersonName();
		personName.setFamilyName("\"   \"");
		Errors errors = new BindException(personName, "familyName");
		new PersonNameValidator().validatePersonName(personName, errors, false, true);
		Assert.assertTrue(errors.hasFieldErrors("familyName"));
	}
	
	/**
	 * @see {@link PatientNameValidator#validatePersonName(java.lang.Object, org.springframework.validation.Errors, boolean, boolean)}
	 */
	@Test
	@Verifies(value = "should pass validation if PersonName.familyName is not blank", method = "validate(java.lang.Object, org.springframework.validation.Errors, boolean, boolean)")
	public void validate_shouldPassValidationIfPersonNameFamilyNameIsNotBlank() throws Exception {
		PersonName personName = new PersonName();
		personName.setFamilyName("Rogers");
		Errors errors = new BindException(personName, "familyName");
		new PersonNameValidator().validatePersonName(personName, errors, false, true);
		Assert.assertFalse(errors.hasFieldErrors("familyName"));
	}
	
	/**
	 * @see {@link PatientNameValidator#validatePersonName(java.lang.Object, org.springframework.validation.Errors, boolean, boolean)}
	 */
	@Test
	@Verifies(value = "should fail validation if PersonName.prefix is too long", method = "validate(java.lang.Object, org.springframework.validation.Errors, boolean, boolean)")
	public void validate_shouldFailValidationIfPersonNamePrefixIsTooLong() throws Exception {
		PersonName personName = new PersonName();
		personName.setGivenName("givenName");
		personName.setFamilyName("familyName");
		personName
		        .setPrefix("123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789"); // 100 characters long
		Errors errors = new BindException(personName, "prefix");
		new PersonNameValidator().validatePersonName(personName, errors, false, true);
		Assert.assertTrue(errors.hasFieldErrors("prefix"));
	}
	
	/**
	 * @see {@link PatientNameValidator#validatePersonName(java.lang.Object, org.springframework.validation.Errors, boolean, boolean)}
	 */
	@Test
	@Verifies(value = "should pass validation if PersonName.prefix is exactly max length", method = "validate(java.lang.Object, org.springframework.validation.Errors, boolean, boolean)")
	public void validate_shouldPassValidationIfPersonNamePrefixIsExactlyMaxLength() throws Exception {
		PersonName personName = new PersonName();
		personName.setPrefix("12345678901234567890123456789012345678901234567890"); // exactly 50 characters long
		Errors errors = new BindException(personName, "prefix");
		new PersonNameValidator().validatePersonName(personName, errors, false, true);
		Assert.assertFalse(errors.hasFieldErrors("prefix"));
	}
	
	/**
	 * @see {@link PatientNameValidator#validatePersonName(java.lang.Object, org.springframework.validation.Errors, boolean, boolean)}
	 */
	@Test
	@Verifies(value = "should pass validation if PersonName.prefix is less than maximum field length", method = "validate(java.lang.Object, org.springframework.validation.Errors, boolean, boolean)")
	public void validate_shouldPassValidationIfPersonNamePrefixIsLessThanMaxFieldLength() throws Exception {
		PersonName personName = new PersonName();
		personName.setPrefix("1234567890");
		Errors errors = new BindException(personName, "prefix");
		new PersonNameValidator().validatePersonName(personName, errors, false, true);
		Assert.assertFalse(errors.hasFieldErrors("prefix"));
	}
	
	/**
	 * @see {@link PatientNameValidator#validatePersonName(java.lang.Object, org.springframework.validation.Errors, boolean, boolean)}
	 */
	@Test
	@Verifies(value = "should fail validation if PersonName.givenName is too long", method = "validate(java.lang.Object, org.springframework.validation.Errors, boolean, boolean)")
	public void validate_shouldFailValidationIfPersonNameGivenNameIsTooLong() throws Exception {
		PersonName personName = new PersonName();
		personName
		        .setGivenName("123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789"); // 100 characters long
		Errors errors = new BindException(personName, "givenName");
		new PersonNameValidator().validatePersonName(personName, errors, false, true);
		Assert.assertTrue(errors.hasFieldErrors("givenName"));
	}
	
	/**
	 * @see {@link PatientNameValidator#validatePersonName(java.lang.Object, org.springframework.validation.Errors, boolean, boolean)}
	 */
	@Test
	@Verifies(value = "should pass validation if PersonName.givenName is exactly max length", method = "validate(java.lang.Object, org.springframework.validation.Errors, boolean, boolean)")
	public void validate_shouldPassValidationIfPersonNameGivenNameIsExactlyMaxLength() throws Exception {
		PersonName personName = new PersonName();
		personName.setGivenName("abcdefghijabcdefghijabcdefghijabcdefghijabcdefghij"); // exactly 50 characters long
		Errors errors = new BindException(personName, "givenName");
		new PersonNameValidator().validatePersonName(personName, errors, false, true);
		Assert.assertFalse(errors.hasFieldErrors("givenName"));
	}
	
	/**
	 * @see {@link PatientNameValidator#validatePersonName(java.lang.Object, org.springframework.validation.Errors, boolean, boolean)}
	 */
	@Test
	@Verifies(value = "should pass validation if PersonName.givenName is less than maximum field length", method = "validate(java.lang.Object, org.springframework.validation.Errors, boolean, boolean)")
	public void validate_shouldPassValidationIfPersonNameGivenNameIsLessThanMaxFieldLength() throws Exception {
		PersonName personName = new PersonName();
		personName.setGivenName("abcdefghij");
		Errors errors = new BindException(personName, "givenName");
		new PersonNameValidator().validatePersonName(personName, errors, false, true);
		Assert.assertFalse(errors.hasFieldErrors("givenName"));
	}
	
	/**
	 * @see {@link PatientNameValidator#validatePersonName(java.lang.Object, org.springframework.validation.Errors, boolean, boolean)}
	 */
	@Test
	@Verifies(value = "should fail validation if PersonName.middleName is too long", method = "validate(java.lang.Object, org.springframework.validation.Errors, boolean, boolean)")
	public void validate_shouldFailValidationIfPersonNameMiddleNameIsTooLong() throws Exception {
		PersonName personName = new PersonName();
		personName
		        .setMiddleName("123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789"); // 100 characters long
		Errors errors = new BindException(personName, "middleName");
		new PersonNameValidator().validatePersonName(personName, errors, false, true);
		Assert.assertTrue(errors.hasFieldErrors("middleName"));
	}
	
	/**
	 * @see {@link PatientNameValidator#validatePersonName(java.lang.Object, org.springframework.validation.Errors, boolean, boolean)}
	 */
	@Test
	@Verifies(value = "should pass validation if PersonName.middleName is exactly max length", method = "validate(java.lang.Object, org.springframework.validation.Errors, boolean, boolean)")
	public void validate_shouldPassValidationIfPersonNameMiddleNameIsExactlyMaxLength() throws Exception {
		PersonName personName = new PersonName();
		personName.setMiddleName("abcdefghijabcdefghijabcdefghijabcdefghijabcdefghij"); // exactly 50 characters long
		Errors errors = new BindException(personName, "middleName");
		new PersonNameValidator().validatePersonName(personName, errors, false, true);
		Assert.assertFalse(errors.hasFieldErrors("middleName"));
	}
	
	/**
	 * @see {@link PatientNameValidator#validatePersonName(java.lang.Object, org.springframework.validation.Errors, boolean, boolean)}
	 */
	@Test
	@Verifies(value = "should pass validation if PersonName.middleName is less than maximum field length", method = "validate(java.lang.Object, org.springframework.validation.Errors, boolean, boolean)")
	public void validate_shouldPassValidationIfPersonNameMiddleNameIsLessThanMaxFieldLength() throws Exception {
		PersonName personName = new PersonName();
		personName.setMiddleName("abcdefghij");
		Errors errors = new BindException(personName, "middleName");
		new PersonNameValidator().validatePersonName(personName, errors, false, true);
		Assert.assertFalse(errors.hasFieldErrors("middleName"));
	}
	
	/**
	 * @see {@link PatientNameValidator#validatePersonName(java.lang.Object, org.springframework.validation.Errors, boolean, boolean)}
	 */
	@Test
	@Verifies(value = "should fail validation if PersonName.familyNamePrefix is too long", method = "validate(java.lang.Object, org.springframework.validation.Errors, boolean, boolean)")
	public void validate_shouldFailValidationIfPersonNameFamilyNamePrefixIsTooLong() throws Exception {
		PersonName personName = new PersonName();
		personName.setGivenName("givenName");
		personName.setFamilyName("familyName");
		personName
		        .setFamilyNamePrefix("123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789"); // 100 characters long
		Errors errors = new BindException(personName, "familyNamePrefix");
		new PersonNameValidator().validatePersonName(personName, errors, false, true);
		Assert.assertTrue(errors.hasFieldErrors("familyNamePrefix"));
	}
	
	/**
	 * @see {@link PatientNameValidator#validatePersonName(java.lang.Object, org.springframework.validation.Errors, boolean, boolean)}
	 */
	@Test
	@Verifies(value = "should pass validation if PersonName.familyNamePrefix is exactly max length", method = "validate(java.lang.Object, org.springframework.validation.Errors, boolean, boolean)")
	public void validate_shouldPassValidationIfPersonNameFamilyNamePrefixIsExactlyMaxLength() throws Exception {
		PersonName personName = new PersonName();
		personName.setFamilyNamePrefix("12345678901234567890123456789012345678901234567890"); // exactly 50 characters long
		Errors errors = new BindException(personName, "familyNamePrefix");
		new PersonNameValidator().validatePersonName(personName, errors, false, true);
		Assert.assertFalse(errors.hasFieldErrors("familyNamePrefix"));
	}
	
	/**
	 * @see {@link PatientNameValidator#validatePersonName(java.lang.Object, org.springframework.validation.Errors, boolean, boolean)}
	 */
	@Test
	@Verifies(value = "should pass validation if PersonName.familyNamePrefix is less than maximum field length", method = "validate(java.lang.Object, org.springframework.validation.Errors, boolean, boolean)")
	public void validate_shouldPassValidationIfPersonNameFamilyNamePrefixIsLessThanMaxFieldLength() throws Exception {
		PersonName personName = new PersonName();
		personName.setFamilyNamePrefix("1234567890");
		Errors errors = new BindException(personName, "familyNamePrefix");
		new PersonNameValidator().validatePersonName(personName, errors, false, true);
		Assert.assertFalse(errors.hasFieldErrors("familyNamePrefix"));
	}
	
	/**
	 * @see {@link PatientNameValidator#validatePersonName(java.lang.Object, org.springframework.validation.Errors, boolean, boolean)}
	 */
	@Test
	@Verifies(value = "should fail validation if PersonName.familyName is too long", method = "validate(java.lang.Object, org.springframework.validation.Errors, boolean, boolean)")
	public void validate_shouldFailValidationIfPersonNameFamilyNameIsTooLong() throws Exception {
		PersonName personName = new PersonName();
		personName
		        .setFamilyName("123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789"); // 100 characters long
		Errors errors = new BindException(personName, "familyName");
		new PersonNameValidator().validatePersonName(personName, errors, false, true);
		Assert.assertTrue(errors.hasFieldErrors("familyName"));
	}
	
	/**
	 * @see {@link PatientNameValidator#validatePersonName(java.lang.Object, org.springframework.validation.Errors, boolean, boolean)}
	 */
	@Test
	@Verifies(value = "should pass validation if PersonName.familyName is exactly max length", method = "validate(java.lang.Object, org.springframework.validation.Errors, boolean, boolean)")
	public void validate_shouldPassValidationIfPersonNameFamilyNameIsExactlyMaxLength() throws Exception {
		PersonName personName = new PersonName();
		personName.setFamilyName("abcdefghijabcdefghijabcdefghijabcdefghijabcdefghij"); // exactly 50 characters long
		Errors errors = new BindException(personName, "familyName");
		new PersonNameValidator().validatePersonName(personName, errors, false, true);
		Assert.assertFalse(errors.hasFieldErrors("familyName"));
	}
	
	/**
	 * @see {@link PatientNameValidator#validatePersonName(java.lang.Object, org.springframework.validation.Errors, boolean, boolean)}
	 */
	@Test
	@Verifies(value = "should pass validation if PersonName.familyName is less than maximum field length", method = "validate(java.lang.Object, org.springframework.validation.Errors, boolean, boolean)")
	public void validate_shouldPassValidationIfPersonNameFamilyNameIsLessThanMaxFieldLength() throws Exception {
		PersonName personName = new PersonName();
		personName.setFamilyName("abcdefghij");
		Errors errors = new BindException(personName, "familyName");
		new PersonNameValidator().validatePersonName(personName, errors, false, true);
		Assert.assertFalse(errors.hasFieldErrors("familyName"));
	}
	
	/**
	 * @see {@link PatientNameValidator#validatePersonName(java.lang.Object, org.springframework.validation.Errors, boolean, boolean)}
	 */
	@Test
	@Verifies(value = "should fail validation if PersonName.familyName2 is too long", method = "validate(java.lang.Object, org.springframework.validation.Errors, boolean, boolean)")
	public void validate_shouldFailValidationIfPersonNameFamilyName2IsTooLong() throws Exception {
		PersonName personName = new PersonName();
		personName
		        .setFamilyName2("123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789"); // 100 characters long
		Errors errors = new BindException(personName, "familyName2");
		new PersonNameValidator().validatePersonName(personName, errors, false, true);
		Assert.assertTrue(errors.hasFieldErrors("familyName2"));
	}
	
	/**
	 * @see {@link PatientNameValidator#validatePersonName(java.lang.Object, org.springframework.validation.Errors, boolean, boolean)}
	 */
	@Test
	@Verifies(value = "should pass validation if PersonName.familyName2 is exactly max length", method = "validate(java.lang.Object, org.springframework.validation.Errors, boolean, boolean)")
	public void validate_shouldPassValidationIfPersonNameFamilyName2IsExactlyMaxLength() throws Exception {
		PersonName personName = new PersonName();
		personName.setFamilyName2("abcdefghijabcdefghijabcdefghijabcdefghijabcdefghij"); // exactly 50 characters long
		Errors errors = new BindException(personName, "familyName2");
		new PersonNameValidator().validatePersonName(personName, errors, false, true);
		Assert.assertFalse(errors.hasFieldErrors("familyName2"));
	}
	
	/**
	 * @see {@link PatientNameValidator#validatePersonName(java.lang.Object, org.springframework.validation.Errors, boolean, boolean)}
	 */
	@Test
	@Verifies(value = "should pass validation if PersonName.familyName2 is less than maximum field length", method = "validate(java.lang.Object, org.springframework.validation.Errors, boolean, boolean)")
	public void validate_shouldPassValidationIfPersonNameFamilyName2IsLessThanMaxFieldLength() throws Exception {
		PersonName personName = new PersonName();
		personName.setFamilyName2("abcdefghij");
		Errors errors = new BindException(personName, "familyName2");
		new PersonNameValidator().validatePersonName(personName, errors, false, true);
		Assert.assertFalse(errors.hasFieldErrors("familyName2"));
	}
	
	/**
	 * @see {@link PatientNameValidator#validatePersonName(java.lang.Object, org.springframework.validation.Errors, boolean, boolean)}
	 */
	@Test
	@Verifies(value = "should fail validation if PersonName.familyNameSuffix is too long", method = "validate(java.lang.Object, org.springframework.validation.Errors, boolean, boolean)")
	public void validate_shouldFailValidationIfPersonNameFamilyNameSuffixIsTooLong() throws Exception {
		PersonName personName = new PersonName();
		personName.setGivenName("givenName");
		personName.setFamilyName("familyName");
		personName
		        .setFamilyNameSuffix("123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789"); // 100 characters long
		Errors errors = new BindException(personName, "familyNameSuffix");
		new PersonNameValidator().validatePersonName(personName, errors, false, true);
		Assert.assertTrue(errors.hasFieldErrors("familyNameSuffix"));
	}
	
	/**
	 * @see {@link PatientNameValidator#validatePersonName(java.lang.Object, org.springframework.validation.Errors, boolean, boolean)}
	 */
	@Test
	@Verifies(value = "should pass validation if PersonName.familyNameSuffix is exactly max length", method = "validate(java.lang.Object, org.springframework.validation.Errors, boolean, boolean)")
	public void validate_shouldPassValidationIfPersonNameFamilyNameSuffixIsExactlyMaxLength() throws Exception {
		PersonName personName = new PersonName();
		personName.setFamilyNameSuffix("12345678901234567890123456789012345678901234567890"); // exactly 50 characters long
		Errors errors = new BindException(personName, "familyNameSuffix");
		new PersonNameValidator().validatePersonName(personName, errors, false, true);
		Assert.assertFalse(errors.hasFieldErrors("familyNameSuffix"));
	}
	
	/**
	 * @see {@link PatientNameValidator#validatePersonName(java.lang.Object, org.springframework.validation.Errors, boolean, boolean)}
	 */
	@Test
	@Verifies(value = "should pass validation if PersonName.familyNameSuffix is less than maximum field length", method = "validate(java.lang.Object, org.springframework.validation.Errors, boolean, boolean)")
	public void validate_shouldPassValidationIfPersonNameFamilyNameSuffixIsLessThanMaxFieldLength() throws Exception {
		PersonName personName = new PersonName();
		personName.setFamilyNameSuffix("1234567890");
		Errors errors = new BindException(personName, "familyNameSuffix");
		new PersonNameValidator().validatePersonName(personName, errors, false, true);
		Assert.assertFalse(errors.hasFieldErrors("familyNameSuffix"));
	}
	
	/**
	 * @see {@link PatientNameValidator#validatePersonName(java.lang.Object, org.springframework.validation.Errors, boolean, boolean)}
	 */
	@Test
	@Verifies(value = "should fail validation if PersonName.degree is too long", method = "validate(java.lang.Object, org.springframework.validation.Errors, boolean, boolean)")
	public void validate_shouldFailValidationIfPersonNameDegreeIsTooLong() throws Exception {
		PersonName personName = new PersonName();
		personName.setGivenName("givenName");
		personName.setFamilyName("familyName");
		personName
		        .setDegree("123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789"); // 100 characters long
		Errors errors = new BindException(personName, "degree");
		new PersonNameValidator().validatePersonName(personName, errors, false, true);
		Assert.assertTrue(errors.hasFieldErrors("degree"));
	}
	
	/**
	 * @see {@link PatientNameValidator#validatePersonName(java.lang.Object, org.springframework.validation.Errors, boolean, boolean)}
	 */
	@Test
	@Verifies(value = "should pass validation if PersonName.degree is exactly max length", method = "validate(java.lang.Object, org.springframework.validation.Errors, boolean, boolean)")
	public void validate_shouldPassValidationIfPersonNameDegreeIsExactlyMaxLength() throws Exception {
		PersonName personName = new PersonName();
		personName.setDegree("12345678901234567890123456789012345678901234567890"); // exactly 50 characters long
		Errors errors = new BindException(personName, "degree");
		new PersonNameValidator().validatePersonName(personName, errors, false, true);
		Assert.assertFalse(errors.hasFieldErrors("degree"));
	}
	
	/**
	 * @see {@link PatientNameValidator#validatePersonName(java.lang.Object, org.springframework.validation.Errors, boolean, boolean)}
	 */
	@Test
	@Verifies(value = "should pass validation if PersonName.degree is less than maximum field length", method = "validate(java.lang.Object, org.springframework.validation.Errors, boolean, boolean)")
	public void validate_shouldPassValidationIfPersonNameDegreeIsLessThanMaxFieldLength() throws Exception {
		PersonName personName = new PersonName();
		personName.setDegree("1234567890");
		Errors errors = new BindException(personName, "degree");
		new PersonNameValidator().validatePersonName(personName, errors, false, true);
		Assert.assertFalse(errors.hasFieldErrors("degree"));
	}
	
	/**
	 * @see {@link PatientNameValidator#validatePersonName(java.lang.Object, org.springframework.validation.Errors, boolean, boolean)}
	 */
	@Test
	@Verifies(value = "should fail validation if PersonName.givenName is invalid", method = "validate(java.lang.Object, org.springframework.validation.Errors, boolean, boolean)")
	public void validate_shouldFailValidationIfPersonNameGivenNameIsInvalid() throws Exception {
		PersonName personName = new PersonName();
		personName.setGivenName("34dfgd");
		Errors errors = new BindException(personName, "givenName");
		new PersonNameValidator().validatePersonName(personName, errors, false, true);
		Assert.assertTrue(errors.hasFieldErrors("givenName"));
	}
	
	/**
	 * @see {@link PatientNameValidator#validatePersonName(java.lang.Object, org.springframework.validation.Errors, boolean, boolean)}
	 */
	@Test
	@Verifies(value = "should pass validation if PersonName.givenName is valid", method = "validate(java.lang.Object, org.springframework.validation.Errors, boolean, boolean)")
	public void validate_shouldPassValidationIfPersonNameGivenNameIsValid() throws Exception {
		PersonName personName = new PersonName();
		personName.setGivenName("alex");
		Errors errors = new BindException(personName, "givenName");
		new PersonNameValidator().validatePersonName(personName, errors, false, true);
		Assert.assertFalse(errors.hasFieldErrors("givenName"));
	}
	
	/**
	 * @see {@link PatientNameValidator#validatePersonName(java.lang.Object, org.springframework.validation.Errors, boolean, boolean)}
	 */
	@Test
	@Verifies(value = "should fail validation if PersonName.middleName is invalid", method = "validate(java.lang.Object, org.springframework.validation.Errors, boolean, boolean)")
	public void validate_shouldFailValidationIfPersonNameMiddleNameIsInvalid() throws Exception {
		PersonName personName = new PersonName();
		personName.setMiddleName("34dfgd");
		Errors errors = new BindException(personName, "middleName");
		new PersonNameValidator().validatePersonName(personName, errors, false, true);
		Assert.assertTrue(errors.hasFieldErrors("middleName"));
	}
	
	/**
	 * @see {@link PatientNameValidator#validatePersonName(java.lang.Object, org.springframework.validation.Errors, boolean, boolean)}
	 */
	@Test
	@Verifies(value = "should pass validation if PersonName.middleName is valid", method = "validate(java.lang.Object, org.springframework.validation.Errors, boolean, boolean)")
	public void validate_shouldPassValidationIfPersonNameMiddleNameIsValid() throws Exception {
		PersonName personName = new PersonName();
		personName.setMiddleName("de");
		Errors errors = new BindException(personName, "middleName");
		new PersonNameValidator().validatePersonName(personName, errors, false, true);
		Assert.assertFalse(errors.hasFieldErrors("middleName"));
	}
	
	/**
	 * @see {@link PatientNameValidator#validatePersonName(java.lang.Object, org.springframework.validation.Errors, boolean, boolean)}
	 */
	@Test
	@Verifies(value = "should fail validation if PersonName.familyName is invalid", method = "validate(java.lang.Object, org.springframework.validation.Errors, boolean, boolean)")
	public void validate_shouldFailValidationIfPersonNameFamilyNameIsInvalid() throws Exception {
		PersonName personName = new PersonName();
		personName.setFamilyName("34dfgd");
		Errors errors = new BindException(personName, "familyName");
		new PersonNameValidator().validatePersonName(personName, errors, false, true);
		Assert.assertTrue(errors.hasFieldErrors("familyName"));
	}
	
	/**
	 * @see {@link PatientNameValidator#validatePersonName(java.lang.Object, org.springframework.validation.Errors, boolean, boolean)}
	 */
	@Test
	@Verifies(value = "should pass validation if PersonName.familyName is valid", method = "validate(java.lang.Object, org.springframework.validation.Errors, boolean, boolean)")
	public void validate_shouldPassValidationIfPersonNameFamilyNameIsValid() throws Exception {
		PersonName personName = new PersonName();
		personName.setFamilyName("souza");
		Errors errors = new BindException(personName, "familyName");
		new PersonNameValidator().validatePersonName(personName, errors, false, true);
		Assert.assertFalse(errors.hasFieldErrors("familyName"));
	}
	
	/**
	 * @see {@link PatientNameValidator#validatePersonName(java.lang.Object, org.springframework.validation.Errors, boolean, boolean)}
	 */
	@Test
	@Verifies(value = "should fail validation if PersonName.familyName2 is invalid", method = "validate(java.lang.Object, org.springframework.validation.Errors, boolean, boolean)")
	public void validate_shouldFailValidationIfPersonNameFamilyName2IsInvalid() throws Exception {
		PersonName personName = new PersonName();
		personName.setFamilyName2("34dfgd");
		Errors errors = new BindException(personName, "familyName2");
		new PersonNameValidator().validatePersonName(personName, errors, false, true);
		Assert.assertTrue(errors.hasFieldErrors("familyName2"));
	}
	
	/**
	 * @see {@link PatientNameValidator#validatePersonName(java.lang.Object, org.springframework.validation.Errors, boolean, boolean)}
	 */
	@Test
	@Verifies(value = "should pass validation if PersonName.familyName2 is valid", method = "validate(java.lang.Object, org.springframework.validation.Errors, boolean, boolean)")
	public void validate_shouldPassValidationIfPersonNameFamilyName2IsValid() throws Exception {
		PersonName personName = new PersonName();
		personName.setFamilyName2("souza-");
		Errors errors = new BindException(personName, "familyName2");
		new PersonNameValidator().validatePersonName(personName, errors, false, true);
		Assert.assertFalse(errors.hasFieldErrors("familyName2"));
	}
	
	/**
	 * @see {@link PatientNameValidator#validatePersonName(java.lang.Object, org.springframework.validation.Errors, boolean, boolean)}
	 */
	@Test
	@Verifies(value = "should pass validation if regex string is null", method = "validate(java.lang.Object, org.springframework.validation.Errors, boolean, boolean)")
	public void validate_shouldSkipRegexValidationIfValidationStringIsNull() throws Exception {
		Context.getAdministrationService().saveGlobalProperty(
		    new GlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_PATIENT_NAME_REGEX, null));
		PersonName personName = new PersonName();
		personName.setFamilyName("asd123");
		Errors errors = new BindException(personName, "familyName");
		new PersonNameValidator().validatePersonName(personName, errors, false, true);
		Assert.assertFalse(errors.hasFieldErrors("familyName"));
	}
	
	/**
	 * @see {@link PatientNameValidator#validatePersonName(java.lang.Object, org.springframework.validation.Errors, boolean, boolean)}
	 */
	@Test
	@Verifies(value = "should pass validation if regex string is empty", method = "validate(java.lang.Object, org.springframework.validation.Errors, boolean, boolean)")
	public void validate_shouldSkipRegexValidationIfValidationStringIsEmpty() throws Exception {
		Context.getAdministrationService().saveGlobalProperty(
		    new GlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_PATIENT_NAME_REGEX, ""));
		PersonName personName = new PersonName();
		personName.setGivenName("123asd");
		Errors errors = new BindException(personName, "givenName");
		new PersonNameValidator().validatePersonName(personName, errors, false, true);
		Assert.assertFalse(errors.hasFieldErrors("givenName"));
	}
	
	/**
	 * @see {@link PersonNameValidator#validatePersonName(PersonName,Errors,null,null)}
	 */
	@Test
	@Verifies(value = "should not validate against regex for blank names", method = "validatePersonName(PersonName,Errors,null,null)")
	public void validatePersonName_shouldNotValidateAgainstRegexForBlankNames() throws Exception {
		String regex = Context.getAdministrationService().getGlobalProperty(
		    OpenmrsConstants.GLOBAL_PROPERTY_PATIENT_NAME_REGEX);
		//the regex string should be set for the test to be valid
		Assert.assertFalse(StringUtils.isBlank(regex));
		
		PersonName personName = new PersonName();
		personName.setGivenName("given");
		personName.setFamilyName("family");
		personName.setMiddleName("");
		personName.setFamilyName2("");
		Errors errors = new BindException(personName, "personName");
		new PersonNameValidator().validatePersonName(personName, errors, false, true);
		Assert.assertFalse(errors.hasErrors());
	}
	
	/**
	 * @see PersonNameValidator#validate(Object,Errors)
	 * @verifies pass validation if name is invalid but voided
	 */
	@Test
	public void validate_shouldPassValidationIfNameIsInvalidButVoided() throws Exception {
		PersonName personName = new PersonName();
		personName.setVoided(true);
		personName.setFamilyName2("34dfgd"); //invalid familyName2
		
		Errors errors = new BindException(personName, "familyName2");
		new PersonNameValidator().validate(personName, errors);
		
		Assert.assertFalse(errors.hasErrors());
	}
	
	/**
	 * @see {@link PersonNameValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should pass validation if field lengths are correct", method = "validate(java.lang.Object, org.springframework.validation.Errors, boolean, boolean)")
	public void validate_shouldPassValidationIfFieldLengthsAreCorrect() throws Exception {
		PersonName personName = new PersonName();
		personName.setPrefix("prefix");
		personName.setGivenName("givenName");
		personName.setMiddleName("middleName");
		personName.setFamilyNamePrefix("familyNamePrefix");
		personName.setFamilyName("familyName");
		personName.setFamilyName2("familyName");
		personName.setFamilyNameSuffix("familyNameSuffix");
		personName.setDegree("degree");
		personName.setVoidReason("voidReason");
		
		Errors errors = new BindException(personName, "personName");
		new PersonNameValidator().validate(personName, errors);
		Assert.assertFalse(errors.hasErrors());
	}
	
	/**
	 * @see {@link PersonNameValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should fail validation if field lengths are not correct", method = "validate(java.lang.Object, org.springframework.validation.Errors, boolean, boolean)")
	public void validate_shouldFailValidationIfFieldLengthsAreNotCorrect() throws Exception {
		PersonName personName = new PersonName();
		personName.setPrefix("too long text too long text too long text too long text");
		personName.setGivenName("too long text too long text too long text too long text");
		personName.setMiddleName("too long text too long text too long text too long text");
		personName.setFamilyName("too long text too long text too long text too long text");
		personName.setFamilyNamePrefix("too long text too long text too long text too long text");
		personName.setFamilyName("too long text too long text too long text too long text");
		personName.setFamilyName2("too long text too long text too long text too long text");
		personName.setFamilyNameSuffix("too long text too long text too long text too long text");
		personName.setDegree("too long text too long text too long text too long text");
		personName
		        .setVoidReason("too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text");
		
		Errors errors = new BindException(personName, "personName");
		new PersonNameValidator().validate(personName, errors);
		
		Assert.assertTrue(errors.hasFieldErrors("prefix"));
		Assert.assertTrue(errors.hasFieldErrors("givenName"));
		Assert.assertTrue(errors.hasFieldErrors("familyNamePrefix"));
		Assert.assertTrue(errors.hasFieldErrors("familyName"));
		Assert.assertTrue(errors.hasFieldErrors("familyName2"));
		Assert.assertTrue(errors.hasFieldErrors("familyNameSuffix"));
		Assert.assertTrue(errors.hasFieldErrors("degree"));
		Assert.assertTrue(errors.hasFieldErrors("middleName"));
		Assert.assertTrue(errors.hasFieldErrors("voidReason"));
	}
}
