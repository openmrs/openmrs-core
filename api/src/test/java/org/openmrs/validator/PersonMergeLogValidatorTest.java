package org.openmrs.validator;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.Person;
import org.openmrs.person.PersonMergeLog;
import org.openmrs.person.PersonMergeLogData;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;

public class PersonMergeLogValidatorTest {
	
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
}
