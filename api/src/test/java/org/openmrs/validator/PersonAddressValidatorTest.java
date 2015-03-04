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
	
}
