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

import org.openmrs.annotation.Handler;
import org.openmrs.scheduler.Task;
import org.openmrs.scheduler.TaskDefinition;
import org.openmrs.util.OpenmrsClassLoader;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

@Handler(supports = { TaskDefinition.class }, order = 50)
public class SchedulerFormValidator implements Validator {
	
	/**
	 * Determines if the command object being submitted is a valid type
	 * 
	 * @see org.springframework.validation.Validator#supports(java.lang.Class)
	 */
	@Override
	public boolean supports(Class<?> c) {
		return c.equals(TaskDefinition.class);
	}
	
	/**
	 * Checks the form object for any inconsistencies/errors
	 * 
	 * @see org.springframework.validation.Validator#validate(java.lang.Object,
	 *      org.springframework.validation.Errors)
	 * <strong>Should</strong> fail validation if name is null or empty or whitespace
	 * <strong>Should</strong> fail validation if taskClass is empty or whitespace
	 * <strong>Should</strong> fail validation if repeatInterval is null or empty or whitespace
	 * <strong>Should</strong> fail validation if class is not instance of Task
	 * <strong>Should</strong> fail validation if class is not accessible
	 * <strong>Should</strong> fail validation if class cannot be instantiated
	 * <strong>Should</strong> fail validation if class not found
	 * <strong>Should</strong> pass validation if all required fields have proper values
	 * <strong>Should</strong> pass validation if field lengths are correct
	 * <strong>Should</strong> fail validation if field lengths are not correct
	 */
	@Override
	public void validate(Object obj, Errors errors) {
		TaskDefinition taskDefinition = (TaskDefinition) obj;
		
		if (taskDefinition == null) {
			errors.rejectValue("task", "error.general");
		} else {
			//Won't work without name and description properties on Task Definition
			ValidationUtils.rejectIfEmptyOrWhitespace(errors, "name", "Scheduler.taskForm.required", new Object[] {
			        "Task name", taskDefinition.getName() });
			
			ValidationUtils.rejectIfEmptyOrWhitespace(errors, "taskClass", "Scheduler.taskForm.required", new Object[] {
			        "Task class", taskDefinition.getTaskClass() });
			
			ValidationUtils.rejectIfEmptyOrWhitespace(errors, "repeatInterval", "Scheduler.taskForm.required", new Object[] {
			        "Repeat interval", taskDefinition.getRepeatInterval() });
			
			ValidateUtil
			        .validateFieldLengths(errors, obj.getClass(), "name", "description", "taskClass", "startTimePattern");
			
			// Check if the class is valid
			try {
				Class<?> taskClass = OpenmrsClassLoader.getInstance().loadClass(taskDefinition.getTaskClass());
				
				Object o = taskClass.newInstance();
				if (!(o instanceof Task)) {
					errors
					        .rejectValue("taskClass", "Scheduler.taskForm.classDoesNotImplementTask", new Object[] {
					                taskDefinition.getTaskClass(), Task.class.getName() },
					            "Class does not implement Task interface");
				}
				
			}
			catch (IllegalAccessException iae) {
				errors.rejectValue("taskClass", "Scheduler.taskForm.illegalAccessException", new Object[] { taskDefinition
				        .getTaskClass() }, "Illegal access exception.");
			}
			catch (InstantiationException ie) {
				errors.rejectValue("taskClass", "Scheduler.taskForm.instantiationException", new Object[] { taskDefinition
				        .getTaskClass() }, "Error creating new instance of class.");
			}
			catch (ClassNotFoundException cnfe) {
				errors.rejectValue("taskClass", "Scheduler.taskForm.classNotFoundException", new Object[] { taskDefinition
				        .getTaskClass() }, "Class not found error.");
			}
		}
	}
	
}
