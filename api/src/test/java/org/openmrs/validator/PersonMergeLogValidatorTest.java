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

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.Person;
import org.openmrs.person.PersonMergeLog;
import org.openmrs.person.PersonMergeLogData;
import org.openmrs.test.BaseContextSensitiveTest;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;

public class PersonMergeLogValidatorTest extends BaseContextSensitiveTest {
	
	/**
	 * @see PersonMergeLogValidator#validate(Object,Errors)
	 * @verifies fail validation if personMergeLogData is null
	 */
	@Test
	public void validate_shouldFailValidationIfPersonMergeLogDataIsNull() throws Exception {
		PersonMergeLog personMergeLog = new PersonMergeLog();
		personMergeLog.setWinner(new Person());
		personMergeLog.setLoser(new Person());
		PersonMergeLogValidator validator = new PersonMergeLogValidator();
		Errors errors = new BindException(personMergeLog, "personMergeLog");
		validator.validate(personMergeLog, errors);
		Assert.assertTrue(errors.hasFieldErrors("personMergeLogData"));
	}
	
	/**
	 * @see PersonMergeLogValidator#validate(Object,Errors)
	 * @verifies fail validation if winner is null
	 */
	@Test
	public void validate_shouldFailValidationIfWinnerIsNull() throws Exception {
		PersonMergeLog personMergeLog = new PersonMergeLog();
		personMergeLog.setLoser(new Person());
		personMergeLog.setPersonMergeLogData(new PersonMergeLogData());
		PersonMergeLogValidator validator = new PersonMergeLogValidator();
		Errors errors = new BindException(personMergeLog, "personMergeLog");
		validator.validate(personMergeLog, errors);
		Assert.assertTrue(errors.hasFieldErrors("winner"));
	}
	
	/**
	 * @see PersonMergeLogValidator#validate(Object,Errors)
	 * @verifies fail validation if loser is null
	 */
	@Test
	public void validate_shouldFailValidationIfLoserIsNull() throws Exception {
		PersonMergeLog personMergeLog = new PersonMergeLog();
		personMergeLog.setWinner(new Person());
		personMergeLog.setPersonMergeLogData(new PersonMergeLogData());
		PersonMergeLogValidator validator = new PersonMergeLogValidator();
		Errors errors = new BindException(personMergeLog, "personMergeLog");
		validator.validate(personMergeLog, errors);
		Assert.assertTrue(errors.hasFieldErrors("loser"));
	}
	
	/**
	 * @see PersonMergeLogValidator#validate(Object,Errors)
	 * @verifies pass validation if all fields are correct
	 */
	@Test
	public void validate_shouldPassValidationIfAllFieldsAreCorrect() throws Exception {
		PersonMergeLog personMergeLog = new PersonMergeLog();
		personMergeLog.setWinner(new Person());
		personMergeLog.setLoser(new Person());
		personMergeLog.setPersonMergeLogData(new PersonMergeLogData());
		PersonMergeLogValidator validator = new PersonMergeLogValidator();
		Errors errors = new BindException(personMergeLog, "personMergeLog");
		validator.validate(personMergeLog, errors);
		Assert.assertFalse(errors.hasFieldErrors());
	}
	
	/**
	 * @see PersonMergeLogValidator#validate(Object,Errors)
	 * @verifies pass validation if field lengths are correct
	 */
	@Test
	public void validate_shouldPassValidationIfFieldLengthsAreCorrect() throws Exception {
		PersonMergeLog personMergeLog = new PersonMergeLog();
		personMergeLog.setWinner(new Person());
		personMergeLog.setLoser(new Person());
		personMergeLog.setPersonMergeLogData(new PersonMergeLogData());
		
		personMergeLog.setVoidReason("voidReason");
		
		PersonMergeLogValidator validator = new PersonMergeLogValidator();
		Errors errors = new BindException(personMergeLog, "personMergeLog");
		validator.validate(personMergeLog, errors);
		Assert.assertFalse(errors.hasFieldErrors());
	}
	
	/**
	 * @see PersonMergeLogValidator#validate(Object,Errors)
	 * @verifies fail validation if field lengths are not correct
	 */
	@Test
	public void validate_shouldFailValidationIfFieldLengthsAreNotCorrect() throws Exception {
		PersonMergeLog personMergeLog = new PersonMergeLog();
		personMergeLog.setWinner(new Person());
		personMergeLog.setLoser(new Person());
		personMergeLog.setPersonMergeLogData(new PersonMergeLogData());
		
		personMergeLog
		        .setVoidReason("too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text");
		
		PersonMergeLogValidator validator = new PersonMergeLogValidator();
		Errors errors = new BindException(personMergeLog, "personMergeLog");
		validator.validate(personMergeLog, errors);
		
		Assert.assertTrue(errors.hasFieldErrors("voidReason"));
	}
}
