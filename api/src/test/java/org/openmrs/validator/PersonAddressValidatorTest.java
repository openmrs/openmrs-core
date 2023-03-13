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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Calendar;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.Address;
import org.openmrs.PersonAddress;
import org.openmrs.api.PersonService;
import org.openmrs.api.context.Context;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;

/**
 * Consists of tests for the PersonAddressValidator
 */
public class PersonAddressValidatorTest extends BaseContextSensitiveTest {
	
	protected static final String PERSON_ADDRESS_VALIDATOR_DATASET_PACKAGE_PATH = "org/openmrs/include/personAddressValidatorTestDataset.xml";
	
	PersonAddressValidator validator = null;
	
	PersonService ps = null;
	
	/**
	 * Run this before each unit test in this class.
	 * 
	 * @throws Exception
	 */
	@BeforeEach
	public void runBeforeAllTests() {
		validator = new PersonAddressValidator();
		ps = Context.getPersonService();
	}
	
	/**
	 * @see PersonAddressValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldFailIfTheStartDateIsInTheFuture() {
		PersonAddress personAddress = new PersonAddress();
		Calendar c = Calendar.getInstance();
		// put the time into the future by a minute
		c.add(Calendar.MINUTE, 1);
		personAddress.setStartDate(c.getTime());
		Errors errors = new BindException(personAddress, "personAddress");
		validator.validate(personAddress, errors);
		assertTrue(errors.hasFieldErrors());
	}
	
	/**
	 * @see PersonAddressValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldFailIfTheEndDateIsBeforeTheStartDate() {
		PersonAddress personAddress = new PersonAddress();
		Calendar c = Calendar.getInstance();
		personAddress.setStartDate(c.getTime());
		c.set(2010, 3, 15);//set to an older date
		personAddress.setEndDate(c.getTime());
		Errors errors = new BindException(personAddress, "personAddress");
		validator.validate(personAddress, errors);
		assertTrue(errors.hasFieldErrors());
	}
	
	/**
	 * @see PersonAddressValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldPassIfAllTheDatesAreValid() {
		PersonAddress personAddress = new PersonAddress();
		Calendar c = Calendar.getInstance();
		personAddress.setStartDate(c.getTime());
		personAddress.setEndDate(c.getTime());
		Errors errors = new BindException(personAddress, "personAddress");
		validator.validate(personAddress, errors);
		assertFalse(errors.hasFieldErrors());
	}
	
	/**
	 * @see PersonAddressValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldPassIfStartDateAndEndDateAreBothNull() {
		PersonAddress personAddress = new PersonAddress();
		personAddress.setStartDate(null);
		personAddress.setEndDate(null);
		Errors errors = new BindException(personAddress, "personAddress");
		validator.validate(personAddress, errors);
		assertFalse(errors.hasFieldErrors());
	}
	
	/**
	 * @see PersonAddressValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldPassIfStartDateIsNull() {
		PersonAddress personAddress = new PersonAddress();
		Calendar c = Calendar.getInstance();
		personAddress.setStartDate(null);
		personAddress.setEndDate(c.getTime());
		Errors errors = new BindException(personAddress, "personAddress");
		validator.validate(personAddress, errors);
		assertFalse(errors.hasFieldErrors());
	}
	
	/**
	 * @see PersonAddressValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldPassIfEndDateIsNull() {
		PersonAddress personAddress = new PersonAddress();
		Calendar c = Calendar.getInstance();
		personAddress.setStartDate(c.getTime());
		personAddress.setEndDate(null);
		Errors errors = new BindException(personAddress, "personAddress");
		validator.validate(personAddress, errors);
		assertFalse(errors.hasFieldErrors());
	}
	
	/**
	 * @see PersonAddressValidator#validate(Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldFailIfRequiredFieldsAreEmpty() {
		executeDataSet(PERSON_ADDRESS_VALIDATOR_DATASET_PACKAGE_PATH);
		Address personAddress = new PersonAddress();
		
		Errors errors = new BindException(personAddress, "personAddress");
		validator.validate(personAddress, errors);
		assertTrue(errors.hasErrors());
	}
	
	/**
	 * @see PersonAddressValidator#validate(Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldPassIfRequiredFieldsAreNotEmpty() {
		executeDataSet(PERSON_ADDRESS_VALIDATOR_DATASET_PACKAGE_PATH);
		Address personAddress = new PersonAddress();
		personAddress.setAddress1("Address1");
		
		Errors errors = new BindException(personAddress, "personAddress");
		validator.validate(personAddress, errors);
		assertFalse(errors.hasErrors());
	}
	
	/**
	 * @see PersonAddressValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldPassValidationIfFieldLengthsAreCorrect() {
		PersonAddress personAddress = new PersonAddress();
		personAddress.setStartDate(null);
		personAddress.setEndDate(null);
		personAddress.setAddress1("address1");
		personAddress.setAddress2("address2");
		personAddress.setCityVillage("cityVillage");
		personAddress.setStateProvince("stateProvince");
		personAddress.setPostalCode("postalCode");
		personAddress.setCountry("country");
		personAddress.setLatitude("latitude");
		personAddress.setLongitude("longitude");
		personAddress.setVoidReason("voidReason");
		personAddress.setCountyDistrict("countyDistrict");
		personAddress.setAddress3("address3");
		personAddress.setAddress4("address4");
		personAddress.setAddress5("address5");
		Errors errors = new BindException(personAddress, "personAddress");
		validator.validate(personAddress, errors);
		assertFalse(errors.hasErrors());
	}
	
	/**
	 * @see PersonAddressValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfFieldLengthsAreNotCorrect() {
		PersonAddress personAddress = new PersonAddress();
		personAddress.setStartDate(null);
		personAddress.setEndDate(null);
		String longString = "too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text";
		personAddress.setAddress1(longString);
		personAddress.setAddress2(longString);
		personAddress.setCityVillage(longString);
		personAddress.setStateProvince(longString);
		personAddress.setPostalCode(longString);
		personAddress.setCountry(longString);
		personAddress.setLatitude(longString);
		personAddress.setLongitude(longString);
		personAddress.setVoidReason(longString);
		personAddress.setCountyDistrict(longString);
		personAddress.setAddress3(longString);
		personAddress.setAddress4(longString);
		personAddress.setAddress5(longString);
		personAddress.setAddress6(longString);
		personAddress.setAddress7(longString);
		personAddress.setAddress8(longString);
		personAddress.setAddress9(longString);
		personAddress.setAddress10(longString);
		personAddress.setAddress11(longString);
		personAddress.setAddress12(longString);
		personAddress.setAddress13(longString);
		personAddress.setAddress14(longString);
		personAddress.setAddress15(longString);
		Errors errors = new BindException(personAddress, "personAddress");
		validator.validate(personAddress, errors);
		assertTrue(errors.hasFieldErrors("address1"));
		assertTrue(errors.hasFieldErrors("address2"));
		assertTrue(errors.hasFieldErrors("cityVillage"));
		assertTrue(errors.hasFieldErrors("stateProvince"));
		assertTrue(errors.hasFieldErrors("postalCode"));
		assertTrue(errors.hasFieldErrors("country"));
		assertTrue(errors.hasFieldErrors("latitude"));
		assertTrue(errors.hasFieldErrors("longitude"));
		assertTrue(errors.hasFieldErrors("voidReason"));
		assertTrue(errors.hasFieldErrors("countyDistrict"));
		assertTrue(errors.hasFieldErrors("address3"));
		assertTrue(errors.hasFieldErrors("address4"));
		assertTrue(errors.hasFieldErrors("address5"));
		assertTrue(errors.hasFieldErrors("address6"), "address6 missing in errors");
		assertTrue(errors.hasFieldErrors("address7"), "address7 missing in errors");
		assertTrue(errors.hasFieldErrors("address8"), "address8 missing in errors");
		assertTrue(errors.hasFieldErrors("address9"), "address9 missing in errors");
		assertTrue(errors.hasFieldErrors("address10"), "address10 missing in errors");
		assertTrue(errors.hasFieldErrors("address11"), "address11 missing in errors");
		assertTrue(errors.hasFieldErrors("address12"), "address12 missing in errors");
		assertTrue(errors.hasFieldErrors("address13"), "address13 missing in errors");
		assertTrue(errors.hasFieldErrors("address14"), "address14 missing in errors");
		assertTrue(errors.hasFieldErrors("address15"), "address15 missing in errors");
	}
}
