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
import org.openmrs.PersonName;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.test.Verifies;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;

/**
 * Tests methods on the {@link PersonNameValidator} class.
 */
public class PersonNameValidatorTest extends BaseContextSensitiveTest {
	
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
		personName.setPrefix("123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789");  // 100 characters long
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
		personName.setPrefix("1234567890123456789012345678901234567890123456789");  // exactly 50 characters long
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
		personName.setGivenName("123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789");  // 100 characters long
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
		personName.setGivenName("1234567890123456789012345678901234567890123456789");  // exactly 50 characters long
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
		personName.setGivenName("1234567890");
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
		personName.setMiddleName("123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789");  // 100 characters long
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
		personName.setMiddleName("1234567890123456789012345678901234567890123456789");  // exactly 50 characters long
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
		personName.setMiddleName("1234567890");
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
		personName.setFamilyNamePrefix("123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789");  // 100 characters long
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
		personName.setFamilyNamePrefix("1234567890123456789012345678901234567890123456789");  // exactly 50 characters long
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
		personName.setFamilyName("123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789");  // 100 characters long
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
		personName.setFamilyName("1234567890123456789012345678901234567890123456789");  // exactly 50 characters long
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
		personName.setFamilyName("1234567890");
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
		personName.setFamilyName2("123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789");  // 100 characters long
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
		personName.setFamilyName2("1234567890123456789012345678901234567890123456789");  // exactly 50 characters long
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
		personName.setFamilyName2("1234567890");
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
		personName.setFamilyNameSuffix("123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789");  // 100 characters long
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
		personName.setFamilyNameSuffix("1234567890123456789012345678901234567890123456789");  // exactly 50 characters long
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
		personName.setDegree("123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789");  // 100 characters long
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
		personName.setDegree("1234567890123456789012345678901234567890123456789");  // exactly 50 characters long
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

}
