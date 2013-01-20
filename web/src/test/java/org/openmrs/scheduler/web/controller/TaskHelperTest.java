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

package org.openmrs.scheduler.web.controller;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.api.context.Context;
import org.openmrs.scheduler.SchedulerException;
import org.openmrs.scheduler.SchedulerService;
import org.openmrs.scheduler.TaskDefinition;
import org.openmrs.web.test.BaseWebContextSensitiveTest;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeoutException;

public class TaskHelperTest extends BaseWebContextSensitiveTest {
	
	private static final String INITIAL_SCHEDULER_TASK_CONFIG_XML = "org/openmrs/web/include/TaskHelperTest.xml";
	
	private static final long MAX_WAIT_TIME_IN_MILLISECONDS = 2048;
	
	private SchedulerService service;
	
	private TaskHelper taskHelper;
	
	@Before
	public void setUp() throws Exception {
		executeDataSet(INITIAL_SCHEDULER_TASK_CONFIG_XML);
		
		service = Context.getSchedulerService();
		taskHelper = new TaskHelper(service);
	}
	
	/**
	 * @verifies get a time in the future
	 * @see TaskHelper#getTime(int, int)
	 */
	@Test
	public void getTime_shouldGetATimeInTheFuture() throws Exception {
		Date then = taskHelper.getTime(Calendar.SECOND, 123);
		Assert.assertTrue(then.after(new Date()));
	}
	
	/**
	 * @verifies get a time in the past
	 * @see TaskHelper#getTime(int, int)
	 */
	@Test
	public void getTime_shouldGetATimeInThePast() throws Exception {
		Date then = taskHelper.getTime(Calendar.SECOND, -123);
		Assert.assertTrue(then.before(new Date()));
	}
	
	/**
	 * @verifies return a task that has been started
	 * @see TaskHelper#getScheduledTaskDefinition(java.util.Date)
	 */
	@Test
	public void getScheduledTaskDefinition_shouldReturnATaskThatHasBeenStarted() throws Exception {
		Date time = taskHelper.getTime(Calendar.SECOND, 1);
		TaskDefinition task = taskHelper.getScheduledTaskDefinition(time);
		Assert.assertTrue(task.getStarted());
	}
	
	/**
	 * @verifies return a task that has not been started
	 * @see TaskHelper#getUnscheduledTaskDefinition(java.util.Date)
	 */
	@Test
	public void getUnscheduledTaskDefinition_shouldReturnATaskThatHasNotBeenStarted() throws Exception {
		Date time = taskHelper.getTime(Calendar.SECOND, 1);
		TaskDefinition task = taskHelper.getUnscheduledTaskDefinition(time);
		Assert.assertFalse(task.getStarted());
	}
	
	/**
	 * @verifies wait until task is executing
	 * @see TaskHelper#waitUntilTaskIsExecuting(org.openmrs.scheduler.TaskDefinition, long)
	 */
	@Test
	public void waitUntilTaskIsExecuting_shouldWaitUntilTaskIsExecuting() throws Exception {
		Date time = taskHelper.getTime(Calendar.SECOND, 1);
		TaskDefinition task = taskHelper.getScheduledTaskDefinition(time);
		taskHelper.waitUntilTaskIsExecuting(task, MAX_WAIT_TIME_IN_MILLISECONDS);
		
		Assert.assertTrue(task.getTaskInstance().isExecuting());
	}
	
	/**
	 * @verifies raise a timeout exception when the timeout is exceeded
	 * @see TaskHelper#waitUntilTaskIsExecuting(org.openmrs.scheduler.TaskDefinition, long)
	 */
	@Test(expected = TimeoutException.class)
	public void waitUntilTaskIsExecuting_shouldRaiseATimeoutExceptionWhenTheTimeoutIsExceeded() throws SchedulerException,
	        TimeoutException, InterruptedException {
		Date time = taskHelper.getTime(Calendar.MINUTE, 1);
		TaskDefinition task = taskHelper.getScheduledTaskDefinition(time);
		taskHelper.waitUntilTaskIsExecuting(task, 10);
	}
}
