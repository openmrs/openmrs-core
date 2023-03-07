/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.web;

import org.openmrs.module.webservices.helper.TaskServiceWrapper;
import org.openmrs.scheduler.SchedulerException;
import org.openmrs.scheduler.TaskDefinition;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * This is the Mock TaskServiceWrapper class which will act as a TaskService for the test cases
 */
public class MockTaskServiceWrapper extends TaskServiceWrapper {
	
	public List<TaskDefinition> registeredTasks = new ArrayList<TaskDefinition>();
	
	public List<TaskDefinition> scheduledTasks = new ArrayList<TaskDefinition>();
	
	/**
	 * It will find the respected taskDefinition for the given task Id
	 * 
	 * @param id - used to find the taskDefinition from the registeredTasks
	 * @return - return the taskDefinition if it found, else return the null
	 */
	@Override
	public TaskDefinition getTaskById(Integer id) {
		TaskDefinition taskFound = null;
		for (TaskDefinition taskDef : registeredTasks) {
			if (id == taskDef.getId()) {
				taskFound = taskDef;
				break;
			}
		}
		return taskFound;
	}
	
	/**
	 * It will find the respected taskDefinition for the given task Name
	 * 
	 * @param taskName - used to find the taskDefinition from the registeredTasks
	 * @return - return the taskDefinition if it found, else return the null
	 */
	@Override
	public TaskDefinition getTaskByName(String taskName) {
		TaskDefinition taskFound = null;
		for (TaskDefinition taskDef : registeredTasks) {
			if (taskName.equals(taskDef.getName())) {
				taskFound = taskDef;
				break;
			}
		}
		return taskFound;
	}
	
	@Override
	public Collection<TaskDefinition> getRegisteredTasks() {
		return registeredTasks;
	}
	
	@Override
	public Collection<TaskDefinition> getScheduledTasks() {
		return scheduledTasks;
	}
	
	/**
	 * Mock Function : It will add the taskDefinition to the registeredTasks List
	 * 
	 * @param task will contain the taskDefinition to be saved
	 */
	@Override
	public void saveTaskDefinition(TaskDefinition task) {
		if (!registeredTasks.contains(task)) {
			registeredTasks.add(task);
		}
	}
	
	/**
	 * Mock Function : It will remove the taskDefinition from the registeredTasks List and Scheduled
	 * List
	 * 
	 * @param task will contain the taskDefinition to be deleted
	 * @throws SchedulerException
	 */
	@Override
	public void deleteTask(TaskDefinition task) throws SchedulerException {
		if (scheduledTasks.contains(task)) {
			scheduledTasks.remove(task);
		}
		if (registeredTasks.contains(task)) {
			registeredTasks.remove(task);
		}
	}
	
	/**
	 * Mock Function : It will add the taskDefinition to the scheduledTasks List
	 * 
	 * @param task contains the taskDefinition to be scheduled
	 * @throws SchedulerException
	 */
	@Override
	public void scheduleTask(TaskDefinition task) throws SchedulerException {
		if (!scheduledTasks.contains(task)) {
			scheduledTasks.add(task);
		}
	}
	
	/**
	 * Mock Function : It will remove the taskDefinition from the scheduledTasks List
	 * 
	 * @param task contains the taskDefinition to be shutdown
	 * @throws SchedulerException
	 */
	@Override
	public void shutDownTask(TaskDefinition task) throws SchedulerException {
		if (scheduledTasks.contains(task)) {
			scheduledTasks.remove(task);
		}
		
	}
	
	/**
	 * Mock Function : it will remove the taskDefinition from the scheduledTasks and add it again
	 * 
	 * @param task contains the taskDefinition to be re-scheduled
	 * @throws SchedulerException
	 */
	@Override
	public void reScheduleTask(TaskDefinition task) throws SchedulerException {
		if (scheduledTasks.contains(task)) {
			scheduledTasks.remove(task);
		}
		scheduledTasks.add(task);
	}
	
}
