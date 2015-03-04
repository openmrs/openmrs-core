/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.scheduler.web.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.scheduler.SchedulerException;
import org.openmrs.scheduler.SchedulerService;
import org.openmrs.scheduler.TaskDefinition;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeoutException;

/**
 * Provides helper methods for creating scheduled and unscheduled tasks. Also provides methods for waiting until
 * a task has reached a given state, such as executing or having been stopped.
 */
public class TaskHelper {
	
	private static final Log log = LogFactory.getLog(TaskHelper.class);
	
	private SchedulerService service;
	
	public TaskHelper(SchedulerService service) {
		this.service = service;
	}
	
	/**
	 * @param unit defines the unit of the offset
	 * @param value defines the value of the offset
	 *
	 * @return a date object based on an offset relative to the current date and time
	 *
	 * @should get a time in the future
	 * @should get a time in the past
	 */
	public Date getTime(int unit, int value) {
		Calendar cal = Calendar.getInstance();
		cal.add(unit, value);
		return cal.getTime();
	}
	
	/**
	 *
	 * @param startTime defines the start time for a scheduled task
	 * @return a task that has been scheduled and started
	 * @throws SchedulerException if the task cannot be scheduled
	 *
	 * @should return a task that has been started
	 */
	public TaskDefinition getScheduledTaskDefinition(Date startTime) throws SchedulerException {
		TaskDefinition taskDefinition = getTaskDefinition(startTime);
		service.scheduleTask(taskDefinition);
		return taskDefinition;
	}
	
	/**
	 * @param startTime defines the start time for a scheduled task
	 * @return a task that has not been scheduled and has not started
	 *
	 * @should return a task that has not been started
	 */
	public TaskDefinition getUnscheduledTaskDefinition(Date startTime) {
		return getTaskDefinition(startTime);
	}
	
	private TaskDefinition getTaskDefinition(Date startTime) {
		TaskDefinition task = service.getTaskByName("Hello World Task");
		
		task.setStartTime(startTime);
		service.saveTask(task);
		
		return task;
	}
	
	/**
	 * Waits until a task is executing or until a timeout occurs.
	 *
	 * @param task the task that is expected to be executing
	 * @param timeoutInMilliseconds defines how long to wait before raising a timeout exception
	 *
	 * @throws InterruptedException if an interrupt occurs while waiting
	 * @throws TimeoutException if the task is not executing after the specified timeout
	 *
	 * @should wait until task is executing
	 * @should raise a timeout exception when the timeout is exceeded
	 */
	public void waitUntilTaskIsExecuting(TaskDefinition task, long timeoutInMilliseconds) throws InterruptedException,
	        TimeoutException {
		long scheduledBefore = System.currentTimeMillis();
		
		log.debug("waiting for test task to start executing");
		
		while (!task.getTaskInstance().isExecuting()) {
			if (System.currentTimeMillis() - scheduledBefore > timeoutInMilliseconds) {
				throw new TimeoutException("A timeout has occurred while starting a test task. The task has been scheduled "
				        + timeoutInMilliseconds + " milliseconds ago and is not yet executing.");
			}
			Thread.sleep(10);
		}
		
		log.debug("test task has started executing " + (System.currentTimeMillis() - scheduledBefore)
		        + " milliseconds after having been scheduled");
	}
	
}
