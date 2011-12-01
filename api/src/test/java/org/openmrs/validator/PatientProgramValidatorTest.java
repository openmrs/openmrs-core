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

import java.util.Calendar;
import java.util.Date;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.PatientProgram;
import org.openmrs.PatientState;
import org.openmrs.api.context.Context;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.test.Verifies;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;

/**
 * Contains tests for the {@link PatientProgramValidator}
 */
public class PatientProgramValidatorTest extends BaseContextSensitiveTest {
	
	/**
	 * @see {@link PatientProgramValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should fail if the patient field is blank", method = "validate(Object,Errors)")
	public void validate_shouldFailIfThePatientFieldIsBlank() throws Exception {
		PatientProgram program = new PatientProgram();
		BindException errors = new BindException(program, "program");
		new PatientProgramValidator().validate(program, errors);
		Assert.assertEquals(true, errors.hasFieldErrors("patient"));
	}
	
	/**
	 * @see {@link PatientProgramValidator#validate(Object,Errors)}
	 */
	@Test(expected = IllegalArgumentException.class)
	@Verifies(value = "should fail validation if obj is null", method = "validate(Object,Errors)")
	public void validate_shouldFailValidationIfObjIsNull() throws Exception {
		new PatientProgramValidator().validate(null, new BindException(new Object(), ""));
	}
	
	/**
	 * @see {@link PatientProgramValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should pass if the start and end dates for any patient state are both null", method = "validate(Object,Errors)")
	public void validate_shouldPassIfTheStartAndEndDatesForAnyPatientStateAreBothNull() throws Exception {
		PatientProgram program = Context.getProgramWorkflowService().getPatientProgram(1);
		PatientState patientSate = program.getStates().iterator().next();
		Assert.assertNotNull(patientSate);
		patientSate.setStartDate(null);
		Assert.assertNull(patientSate.getEndDate());
		ValidateUtil.validate(program);
	}
	
	/**
	 * @see {@link PatientProgramValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should pass if the start and end dates for any patient state are equal", method = "validate(Object,Errors)")
	public void validate_shouldPassIfTheStartAndEndDatesForAnyPatientStateAreEqual() throws Exception {
		PatientProgram program = Context.getProgramWorkflowService().getPatientProgram(1);
		PatientState patientSate = program.getStates().iterator().next();
		Assert.assertNotNull(patientSate);
		Date date = new Date();
		patientSate.setStartDate(date);
		patientSate.setEndDate(date);
		ValidateUtil.validate(program);
	}
	
	/**
	 * @see {@link PatientProgramValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should fail if any patient state has an end date before its start date", method = "validate(Object,Errors)")
	public void validate_shouldFailIfAnyPatientStateHasAnEndDateBeforeItsStartDate() throws Exception {
		PatientProgram program = Context.getProgramWorkflowService().getPatientProgram(1);
		PatientState patientSate = program.getStates().iterator().next();
		Assert.assertNotNull(patientSate);
		Calendar c = Calendar.getInstance();
		patientSate.setStartDate(c.getTime());
		c.set(1970, 2, 1);//set to an old date
		patientSate.setEndDate(c.getTime());
		
		BindException errors = new BindException(program, "");
		new PatientProgramValidator().validate(program, errors);
		Assert.assertTrue(errors.hasErrors());
	}
	
	/**
	 * @see {@link PatientProgramValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should fail if there is more than one patientState with the same states and startDates", method = "validate(Object,Errors)")
	public void validate_shouldFailIfThereIsMoreThanOnePatientStateWithTheSameStatesAndStartDates() throws Exception {
		PatientProgram program = Context.getProgramWorkflowService().getPatientProgram(1);
		Set<PatientState> states = program.getStates();
		Assert.assertNotNull(states);
		PatientState patientSate = states.iterator().next();
		Assert.assertNotNull(patientSate);
		PatientState duplicate = patientSate.copy();
		states.add(duplicate);
		
		BindException errors = new BindException(program, "");
		new PatientProgramValidator().validate(program, errors);
		Assert.assertTrue(errors.hasErrors());
	}
}
