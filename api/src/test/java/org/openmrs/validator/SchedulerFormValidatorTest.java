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
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.openmrs.scheduler.TaskDefinition;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;

/**
 * Tests methods on the {@link SchedulerFormValidator} class.
 */
public class SchedulerFormValidatorTest extends BaseContextSensitiveTest {
	
	/**
	 * @see SchedulerFormValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfNameIsNullOrEmptyOrWhitespace() {
		TaskDefinition def = new TaskDefinition();
		def.setName(null);
		def.setRepeatInterval(3600000L);
		def.setTaskClass("org.openmrs.scheduler.tasks.HelloWorldTask");
		
		Errors errors = new BindException(def, "def");
		new SchedulerFormValidator().validate(def, errors);
		assertTrue(errors.hasFieldErrors("name"));
		assertThat(errors.getFieldErrors("name").get(0).getCode(), is("Scheduler.taskForm.required"));
		
		def.setName("");
		errors = new BindException(def, "def");
		new SchedulerFormValidator().validate(def, errors);
		assertTrue(errors.hasFieldErrors("name"));
		assertThat(errors.getFieldErrors("name").get(0).getCode(), is("Scheduler.taskForm.required"));
		
		def.setName(" ");
		errors = new BindException(def, "def");
		new SchedulerFormValidator().validate(def, errors);
		assertTrue(errors.hasFieldErrors("name"));
		assertThat(errors.getFieldErrors("name").get(0).getCode(), is("Scheduler.taskForm.required"));
	}
	
	/**
	 * @see SchedulerFormValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfTaskClassIsEmptyOrWhitespace() {
		TaskDefinition def = new TaskDefinition();
		def.setName("Chores");
		def.setRepeatInterval(3600000L);
		def.setTaskClass("");
		
		Errors errors = new BindException(def, "def");
		new SchedulerFormValidator().validate(def, errors);
		assertTrue(errors.hasFieldErrors("taskClass"));
		assertThat(errors.getFieldErrors("taskClass").get(0).getCode(), is("Scheduler.taskForm.required"));
		
		def.setTaskClass(" ");
		errors = new BindException(def, "def");
		new SchedulerFormValidator().validate(def, errors);
		assertTrue(errors.hasFieldErrors("taskClass"));
		assertThat(errors.getFieldErrors("taskClass").get(0).getCode(), is("Scheduler.taskForm.required"));
	}
	
	/**
	 * @see SchedulerFormValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfRepeatIntervalIsNullOrEmptyOrWhitespace() {
		TaskDefinition def = new TaskDefinition();
		def.setName("Chores");
		def.setTaskClass("org.openmrs.scheduler.tasks.HelloWorldTask");
		
		Errors errors = new BindException(def, "def");
		new SchedulerFormValidator().validate(def, errors);
		assertTrue(errors.hasFieldErrors("repeatInterval"));
		assertThat(errors.getFieldErrors("repeatInterval").get(0).getCode(), is("Scheduler.taskForm.required"));
		
		def.setTaskClass(" ");
		errors = new BindException(def, "def");
		new SchedulerFormValidator().validate(def, errors);
		assertTrue(errors.hasFieldErrors("repeatInterval"));
		assertThat(errors.getFieldErrors("repeatInterval").get(0).getCode(), is("Scheduler.taskForm.required"));
	}
	
	/**
	 * @see SchedulerFormValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfClassIsNotInstanceOfTask() {
		TaskDefinition def = new TaskDefinition();
		def.setName("Chores");
		def.setRepeatInterval(3600000L);
		def.setTaskClass("org.openmrs.Obs");
		
		Errors errors = new BindException(def, "def");
		new SchedulerFormValidator().validate(def, errors);
		
		assertTrue(errors.hasFieldErrors("taskClass"));
		assertEquals("Scheduler.taskForm.classDoesNotImplementTask", errors.getFieldError("taskClass").getCode());
	}
	
	/**
	 * @see SchedulerFormValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfClassIsNotAccessible() {
		TaskDefinition def = new TaskDefinition();
		def.setName("Chores");
		def.setRepeatInterval(3600000L);
		def.setTaskClass("???"); //TODO: Find a way to trigger an IllegalAccessException
		
		Errors errors = new BindException(def, "def");
		new SchedulerFormValidator().validate(def, errors);
		
		assertTrue(errors.hasFieldErrors("taskClass"));
		assertEquals("Scheduler.taskForm.classNotFoundException", errors.getFieldError("taskClass").getCode());
	}
	
	/**
	 * @see SchedulerFormValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfClassCannotBeInstantiated() {
		TaskDefinition def = new TaskDefinition();
		def.setName("Chores");
		def.setRepeatInterval(3600000L);
		def.setTaskClass("org.openmrs.BaseOpenmrsData");
		
		Errors errors = new BindException(def, "def");
		new SchedulerFormValidator().validate(def, errors);
		
		assertTrue(errors.hasFieldErrors("taskClass"));
		assertEquals("Scheduler.taskForm.instantiationException", errors.getFieldError("taskClass").getCode());
	}
	
	/**
	 * @see SchedulerFormValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfClassNotFound() {
		TaskDefinition def = new TaskDefinition();
		def.setName("Chores");
		def.setRepeatInterval(3600000L);
		def.setTaskClass("org.openmrs.ScaryRobot");
		
		Errors errors = new BindException(def, "def");
		new SchedulerFormValidator().validate(def, errors);
		
		assertTrue(errors.hasFieldErrors("taskClass"));
		assertEquals("Scheduler.taskForm.classNotFoundException", errors.getFieldError("taskClass").getCode());
	}
	
	/**
	 * @see SchedulerFormValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldPassValidationIfAllRequiredFieldsHaveProperValues() {
		TaskDefinition def = new TaskDefinition();
		def.setName("Chores");
		def.setRepeatInterval(3600000L);
		def.setTaskClass("org.openmrs.scheduler.tasks.HelloWorldTask");
		
		Errors errors = new BindException(def, "def");
		new SchedulerFormValidator().validate(def, errors);
		
		assertFalse(errors.hasErrors());
	}
	
	/**
	 * @see SchedulerFormValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldPassValidationIfFieldLengthsAreCorrect() {
		TaskDefinition def = new TaskDefinition();
		def.setName("Chores");
		def.setRepeatInterval(3600000L);
		def.setTaskClass("org.openmrs.scheduler.tasks.HelloWorldTask");
		def.setDescription("description");
		def.setStartTimePattern("startTimePattern");
		
		Errors errors = new BindException(def, "def");
		new SchedulerFormValidator().validate(def, errors);
		
		assertFalse(errors.hasErrors());
	}
	
	/**
	 * @see SchedulerFormValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfFieldLengthsAreNotCorrect() {
		TaskDefinition def = new TaskDefinition();
		def
		        .setName("too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text");
		def.setRepeatInterval(3600000L);
		def
		        .setTaskClass("too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text");
		def
		        .setDescription("too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text");
		def
		        .setStartTimePattern("too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text");
		
		Errors errors = new BindException(def, "def");
		new SchedulerFormValidator().validate(def, errors);

		assertTrue(errors.hasFieldErrors("name"));
		assertThat(errors.getFieldErrors("name").get(0).getCode(), is("error.exceededMaxLengthOfField"));

		assertTrue(errors.hasFieldErrors("taskClass"));
		assertThat(errors.getFieldErrors("taskClass").get(0).getCode(), is("error.exceededMaxLengthOfField"));

		assertTrue(errors.hasFieldErrors("description"));
		assertThat(errors.getFieldErrors("description").get(0).getCode(), is("error.exceededMaxLengthOfField"));

		assertTrue(errors.hasFieldErrors("startTimePattern"));
		assertThat(errors.getFieldErrors("startTimePattern").get(0).getCode(), is("error.exceededMaxLengthOfField"));
	}
}
