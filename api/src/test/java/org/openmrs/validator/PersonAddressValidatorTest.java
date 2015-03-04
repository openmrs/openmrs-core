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

import java.util.Calendar;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.PersonAddress;
import org.openmrs.api.PersonService;
import org.openmrs.api.context.Context;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.test.Verifies;
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
	@Before
	public void runBeforeAllTests() throws Exception {
		validator = new PersonAddressValidator();
		ps = Context.getPersonService();
	}
	
	/**
	 * @see {@link PersonAddressValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should fail if the startDate is in the future", method = "validate(Object,Errors)")
	public void validate_shouldFailIfTheStartDateIsInTheFuture() throws Exception {
		PersonAddress personAddress = new PersonAddress();
		Calendar c = Calendar.getInstance();
		// put the time into the future by a minute
		c.add(Calendar.MINUTE, 1);
		personAddress.setStartDate(c.getTime());
		Errors errors = new BindException(personAddress, "personAddress");
		validator.validate(personAddress, errors);
		Assert.assertEquals(true, errors.hasFieldErrors());
	}
	
	/**
	 * @see {@link PersonAddressValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should fail if the endDate is before the startDate", method = "validate(Object,Errors)")
	public void validate_shouldFailIfTheEndDateIsBeforeTheStartDate() throws Exception {
		PersonAddress personAddress = new PersonAddress();
		Calendar c = Calendar.getInstance();
		personAddress.setStartDate(c.getTime());
		c.set(2010, 3, 15);//set to an older date
		personAddress.setEndDate(c.getTime());
		Errors errors = new BindException(personAddress, "personAddress");
		validator.validate(personAddress, errors);
		Assert.assertEquals(true, errors.hasFieldErrors());
	}
	
	/**
	 * @see {@link PersonAddressValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should pass if all the dates are valid", method = "validate(Object,Errors)")
	public void validate_shouldPassIfAllTheDatesAreValid() throws Exception {
		PersonAddress personAddress = new PersonAddress();
		Calendar c = Calendar.getInstance();
		personAddress.setStartDate(c.getTime());
		personAddress.setEndDate(c.getTime());
		Errors errors = new BindException(personAddress, "personAddress");
		validator.validate(personAddress, errors);
		Assert.assertEquals(false, errors.hasFieldErrors());
	}
	
	/**
	 * @see {@link PersonAddressValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should pass if startDate and endDate are both null", method = "validate(Object,Errors)")
	public void validate_shouldPassIfStartDateAndEndDateAreBothNull() throws Exception {
		PersonAddress personAddress = new PersonAddress();
		personAddress.setStartDate(null);
		personAddress.setEndDate(null);
		Errors errors = new BindException(personAddress, "personAddress");
		validator.validate(personAddress, errors);
		Assert.assertEquals(false, errors.hasFieldErrors());
	}
	
	/**
	 * @see {@link PersonAddressValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should pass if startDate is null", method = "validate(Object,Errors)")
	public void validate_shouldPassIfStartDateIsNull() throws Exception {
		PersonAddress personAddress = new PersonAddress();
		Calendar c = Calendar.getInstance();
		personAddress.setStartDate(null);
		personAddress.setEndDate(c.getTime());
		Errors errors = new BindException(personAddress, "personAddress");
		validator.validate(personAddress, errors);
		Assert.assertEquals(false, errors.hasFieldErrors());
	}
	
	/**
	 * @see {@link PersonAddressValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should pass if endDate is null", method = "validate(Object,Errors)")
	public void validate_shouldPassIfEndDateIsNull() throws Exception {
		PersonAddress personAddress = new PersonAddress();
		Calendar c = Calendar.getInstance();
		personAddress.setStartDate(c.getTime());
		personAddress.setEndDate(null);
		Errors errors = new BindException(personAddress, "personAddress");
		validator.validate(personAddress, errors);
		Assert.assertEquals(false, errors.hasFieldErrors());
	}
	
	/**
	 * @see PersonAddressValidator#validate(Object, org.springframework.validation.Errors)
	 */
	@Test
	@Verifies(value = "should fail if required fields are empty", method = "validate(Object,Errors)")
	public void validate_shouldFailIfRequiredFieldsAreEmpty() throws Exception {
		executeDataSet(PERSON_ADDRESS_VALIDATOR_DATASET_PACKAGE_PATH);
		PersonAddress personAddress = new PersonAddress();
		
		Errors errors = new BindException(personAddress, "personAddress");
		validator.validate(personAddress, errors);
		Assert.assertEquals(true, errors.hasErrors());
	}
	
	/**
	 * @see PersonAddressValidator#validate(Object, org.springframework.validation.Errors)
	 */
	@Test
	@Verifies(value = "should pass if required fields are not empty", method = "validate(Object,Errors)")
	public void validate_shouldPassIfRequiredFieldsAreNotEmpty() throws Exception {
		executeDataSet(PERSON_ADDRESS_VALIDATOR_DATASET_PACKAGE_PATH);
		PersonAddress personAddress = new PersonAddress();
		personAddress.setAddress1("Address1");
		
		Errors errors = new BindException(personAddress, "personAddress");
		validator.validate(personAddress, errors);
		Assert.assertEquals(false, errors.hasErrors());
	}
	
	/**
	 * @see {@link PersonAddressValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should pass validation if field lengths are correct", method = "validate(Object,Errors)")
	public void validate_shouldPassValidationIfFieldLengthsAreCorrect() throws Exception {
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
		Assert.assertEquals(false, errors.hasErrors());
	}
	
	/**
	 * @see {@link PersonAddressValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should fail validation if field lengths are not correct", method = "validate(Object,Errors)")
	public void validate_shouldFailValidationIfFieldLengthsAreNotCorrect() throws Exception {
		PersonAddress personAddress = new PersonAddress();
		personAddress.setStartDate(null);
		personAddress.setEndDate(null);
		personAddress
		        .setAddress1("too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text");
		personAddress
		        .setAddress2("too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text");
		personAddress
		        .setCityVillage("too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text");
		personAddress
		        .setStateProvince("too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text");
		personAddress
		        .setPostalCode("too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text");
		personAddress
		        .setCountry("too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text");
		personAddress
		        .setLatitude("too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text");
		personAddress
		        .setLongitude("too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text");
		personAddress
		        .setVoidReason("too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text");
		personAddress
		        .setCountyDistrict("too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text");
		personAddress
		        .setAddress3("too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text");
		personAddress
		        .setAddress4("too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text");
		personAddress
		        .setAddress5("too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text");
		Errors errors = new BindException(personAddress, "personAddress");
		validator.validate(personAddress, errors);
		Assert.assertEquals(true, errors.hasFieldErrors("address1"));
		Assert.assertEquals(true, errors.hasFieldErrors("address2"));
		Assert.assertEquals(true, errors.hasFieldErrors("cityVillage"));
		Assert.assertEquals(true, errors.hasFieldErrors("stateProvince"));
		Assert.assertEquals(true, errors.hasFieldErrors("postalCode"));
		Assert.assertEquals(true, errors.hasFieldErrors("country"));
		Assert.assertEquals(true, errors.hasFieldErrors("latitude"));
		Assert.assertEquals(true, errors.hasFieldErrors("longitude"));
		Assert.assertEquals(true, errors.hasFieldErrors("voidReason"));
		Assert.assertEquals(true, errors.hasFieldErrors("countyDistrict"));
		Assert.assertEquals(true, errors.hasFieldErrors("address3"));
		Assert.assertEquals(true, errors.hasFieldErrors("address4"));
		Assert.assertEquals(true, errors.hasFieldErrors("address5"));
	}
}
