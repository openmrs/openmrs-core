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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.api.context.Context;
import org.openmrs.scheduler.SchedulerService;
import org.openmrs.scheduler.Task;
import org.openmrs.scheduler.TaskDefinition;
import org.openmrs.test.Verifies;
import org.openmrs.web.test.BaseWebContextSensitiveTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.AssertThrows;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Contains tests for the SchedulerFormController
 */
public class SchedulerFormControllerTest extends BaseWebContextSensitiveTest {
	
	private static final Log log = LogFactory.getLog(SchedulerFormControllerTest.class);
	
	private static final String INITIAL_SCHEDULER_TASK_CONFIG_XML = "org/openmrs/web/include/SchedulerServiceTest.xml";
	
	private static final long MAX_WAIT_TIME_IN_MILLISECONDS = 2048;
	
	private MockHttpServletRequest mockRequest;
	
	private SchedulerFormController controller;
	
	private SchedulerService service;
	
	/*
	 * Preparing the test run in this way is not required by all test methods, hence it is not annotated with @Before.
	 */
	private void setUpSchedulerService() throws Exception {
		executeDataSet(INITIAL_SCHEDULER_TASK_CONFIG_XML);
		service = Context.getSchedulerService();
	}
	
	/*
	 * Again, preparing the test run in this way is not required by all test methods, hence it is not annotated with @Before.
	 */
	private void setUpSchedulerController() {
		controller = (SchedulerFormController) applicationContext.getBean("schedulerFormController");
		
		mockRequest = new MockHttpServletRequest();
		mockRequest.setMethod("POST");
		mockRequest.setParameter("action", "");
		mockRequest.setParameter("taskId", "1");
	}
	
	/**
	 * @see {@link SchedulerFormController#onSubmit(HttpServletRequest,HttpServletResponse,Object,BindException)}
	 */
	@Test
	@Verifies(value = "should reschedule a currently scheduled task", method = "onSubmit(HttpServletRequest,HttpServletResponse,Object,BindException)")
	public void onSubmit_shouldRescheduleACurrentlyScheduledTask() throws Exception {
		TaskDefinition task = null;
		
		try {
			setUpSchedulerService();
			setUpSchedulerController();
			
			task = getScheduledTaskDefinition(getTime(Calendar.MINUTE, 5));
			Task oldTaskInstance = task.getTaskInstance();
			
			mockRequest.setParameter("startTime", new SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(getTime(
			    Calendar.MINUTE, 2)));
			
			ModelAndView mav = controller.handleRequest(mockRequest, new MockHttpServletResponse());
			assertNotNull(mav);
			assertTrue(mav.getModel().isEmpty());
			
			//the task SHOULD have been rescheduled to reflect the change in start time
			Assert.assertNotSame(oldTaskInstance, task.getTaskInstance());
			
		}
		catch (Exception e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
			
		}
		finally {
			if (service != null && task != null) {
				service.shutdownTask(task);
				waitUntilTaskHasStopped(task, MAX_WAIT_TIME_IN_MILLISECONDS);
			}
		}
	}
	
	/**
	 * @see {@link SchedulerFormController#onSubmit(HttpServletRequest,HttpServletResponse,Object,BindException)}
	 */
	@Test
	@Verifies(value = "should not reschedule a task that is not currently scheduled", method = "onSubmit(HttpServletRequest,HttpServletResponse,Object,BindException)")
	public void onSubmit_shouldNotRescheduleATaskThatIsNotCurrentlyScheduled() throws Exception {
		TaskDefinition task = null;
		
		setUpSchedulerService();
		setUpSchedulerController();
		
		task = getUnscheduledTaskDefinition(getTime(Calendar.MINUTE, 5));
		Task oldTaskInstance = task.getTaskInstance();
		
		mockRequest.setParameter("startTime", new SimpleDateFormat("MM/dd/yyyy HH:mm:ss")
		        .format(getTime(Calendar.MINUTE, 2)));
		
		ModelAndView mav = controller.handleRequest(mockRequest, new MockHttpServletResponse());
		assertNotNull(mav);
		assertTrue(mav.getModel().isEmpty());
		
		//the task should NOT have been rescheduled
		Assert.assertSame(oldTaskInstance, task.getTaskInstance());
		
	}
	
	/**
	 * @see {@link SchedulerFormController#onSubmit(HttpServletRequest,HttpServletResponse,Object,BindException)}
	 */
	@Test
	@Verifies(value = "should not reschedule a task if the start time has passed", method = "onSubmit(HttpServletRequest,HttpServletResponse,Object,BindException)")
	public void onSubmit_shouldNotRescheduleATaskIfTheStartTimeHasPassed() throws Exception {
		TaskDefinition task = null;
		
		try {
			setUpSchedulerService();
			setUpSchedulerController();
			
			task = getScheduledTaskDefinition(getTime(Calendar.MINUTE, 5));
			
			Task oldTaskInstance = task.getTaskInstance();
			
			mockRequest.setParameter("startTime", new SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(getTime(
			    Calendar.SECOND, -1)));
			
			ModelAndView mav = controller.handleRequest(mockRequest, new MockHttpServletResponse());
			assertNotNull(mav);
			assertTrue(mav.getModel().isEmpty());
			
			//the task should NOT have been rescheduled
			Assert.assertSame(oldTaskInstance, task.getTaskInstance());
			
		}
		catch (Exception e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
			
		}
		finally {
			if (service != null && task != null) {
				service.shutdownTask(task);
				waitUntilTaskHasStopped(task, MAX_WAIT_TIME_IN_MILLISECONDS);
			}
		}
	}
	
	/**
	 * @see {@link SchedulerFormController#onSubmit(HttpServletRequest,HttpServletResponse,Object,BindException)}
	 */
	@Test
	@Verifies(value = "should not reschedule an executing task", method = "onSubmit(HttpServletRequest,HttpServletResponse,Object,BindException)")
	public void onSubmit_shouldNotRescheduleAnExecutingTask() throws Exception {
		TaskDefinition task = null;
		
		try {
			setUpSchedulerService();
			setUpSchedulerController();
			
			Date startTime = getTime(Calendar.SECOND, 1);
			task = getScheduledTaskDefinition(startTime);
			
			waitUntilTaskIsExecuting(task, MAX_WAIT_TIME_IN_MILLISECONDS);
			
			Task oldTaskInstance = task.getTaskInstance();
			
			// use the *same* start time as in the task already running
			mockRequest.setParameter("startTime", new SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(startTime));
			
			ModelAndView mav = controller.handleRequest(mockRequest, new MockHttpServletResponse());
			assertNotNull(mav);
			assertTrue(mav.getModel().isEmpty());
			
			//the task should NOT have been rescheduled
			Assert.assertSame(oldTaskInstance, task.getTaskInstance());
			
		}
		catch (Exception e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
			
		}
		finally {
			if (service != null && task != null) {
				service.shutdownTask(task);
				waitUntilTaskHasStopped(task, MAX_WAIT_TIME_IN_MILLISECONDS);
			}
		}
	}
	
	private Date getTime(int unit, int value) {
		Calendar cal = Calendar.getInstance();
		cal.add(unit, value);
		return cal.getTime();
	}
	
	@Test
	public void shouldGetTimeInTheFuture() {
		Date then = getTime(Calendar.SECOND, 123);
		Assert.assertTrue(then.after(new Date()));
	}
	
	@Test
	public void shouldGetTimeInThePast() {
		Date then = getTime(Calendar.SECOND, -123);
		Assert.assertTrue(then.before(new Date()));
	}
	
	private TaskDefinition getScheduledTaskDefinition(Date startTime) throws Exception {
		return getTaskDefinition(startTime, true);
	}
	
	@Test
	public void shouldGetScheduledTaskDefinition() throws Exception {
		TaskDefinition task = null;
		
		try {
			setUpSchedulerService();
			
			task = getScheduledTaskDefinition(getTime(Calendar.SECOND, 1));
			waitUntilTaskIsExecuting(task, MAX_WAIT_TIME_IN_MILLISECONDS);
			
			Assert.assertTrue(task.getStarted());
			Assert.assertTrue(task.getTaskInstance().isExecuting());
			
		}
		catch (Exception e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
			
		}
		finally {
			if (service != null && task != null) {
				service.shutdownTask(task);
				waitUntilTaskHasStopped(task, MAX_WAIT_TIME_IN_MILLISECONDS);
			}
		}
		
	}
	
	private TaskDefinition getUnscheduledTaskDefinition(Date startTime) throws Exception {
		return getTaskDefinition(startTime, false);
	}
	
	@Test
	public void shouldGetUnscheduledTaskDefinition() throws Exception {
		setUpSchedulerService();
		TaskDefinition task = getUnscheduledTaskDefinition(getTime(Calendar.SECOND, 1));
		Assert.assertFalse(task.getStarted());
		Assert.assertNull(task.getTaskInstance());
	}
	
	private TaskDefinition getTaskDefinition(Date startTime, boolean scheduleTask) throws Exception {
		TaskDefinition task = service.getTaskByName("Hello World Task");
		
		task.setStartTime(startTime);
		service.saveTask(task);
		
		if (scheduleTask) {
			service.scheduleTask(task);
		}
		
		return task;
	}
	
	private void waitUntilTaskIsExecuting(TaskDefinition task, long timeoutInMilliseconds) throws InterruptedException,
	        TimeoutException {
		long scheduledBefore = System.currentTimeMillis();
		
		log.debug("waiting for test task to start executing");
		
		// wait up to the timeout for the task to be executing
		//
		// if the TimeoutException is thrown, consider adjusting MAX_WAIT_TIME_IN_MILLISECONDS
		//
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
	
	@Test
	public void shouldWaitUntilTaskIsExecuting() throws Exception {
		TaskDefinition task = null;
		
		try {
			setUpSchedulerService();
			task = getScheduledTaskDefinition(getTime(Calendar.SECOND, 1));
			
			waitUntilTaskIsExecuting(task, MAX_WAIT_TIME_IN_MILLISECONDS);
			
			Assert.assertTrue(task.getStarted());
			Assert.assertTrue(task.getTaskInstance().isExecuting());
			
		}
		catch (Exception e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
			
		}
		finally {
			if (service != null && task != null) {
				service.shutdownTask(task);
				waitUntilTaskHasStopped(task, MAX_WAIT_TIME_IN_MILLISECONDS);
			}
		}
		
	}
	
	@Test(expected = TimeoutException.class)
	public void shouldRunIntoTimeoutWhileWaitingForTaskToStartExecuting() throws Exception {
		TaskDefinition task = null;
		try {
			setUpSchedulerService();
			task = getScheduledTaskDefinition(getTime(Calendar.MINUTE, 1));
			
			waitUntilTaskIsExecuting(task, 10);
			
		}
		catch (TimeoutException te) {
			throw te;
			
		}
		catch (Exception e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
			
		}
		finally {
			if (service != null && task != null) {
				service.shutdownTask(task);
				waitUntilTaskHasStopped(task, MAX_WAIT_TIME_IN_MILLISECONDS);
			}
		}
	}
	
	private void waitUntilTaskHasStopped(TaskDefinition task, long timeoutInMilliseconds) throws InterruptedException,
	        TimeoutException {
		long shutdownBefore = System.currentTimeMillis();
		
		log.debug("waiting for test task to stop executing");
		
		// wait up to the timeout for the task to stop executing
		//
		// if the TimeoutException is thrown, consider adjusting MAX_WAIT_TIME_IN_MILLISECONDS
		//
		while (task.getTaskInstance().isExecuting()) {
			if (System.currentTimeMillis() - shutdownBefore > timeoutInMilliseconds) {
				throw new TimeoutException("A timeout has occurred while stopping a test task. The task has been shut down "
				        + timeoutInMilliseconds + " milliseconds ago and has not yet stopped executing.");
			}
			Thread.sleep(10);
		}
		
		log.debug("test task has stopped executing " + (System.currentTimeMillis() - shutdownBefore)
		        + " milliseconds after having been shut down");
	}
	
	@Test
	public void shouldWaitUntilTaskHasStopped() throws Exception {
		TaskDefinition task = null;
		
		try {
			setUpSchedulerService();
			task = getScheduledTaskDefinition(getTime(Calendar.SECOND, 1));
			
			waitUntilTaskIsExecuting(task, MAX_WAIT_TIME_IN_MILLISECONDS);
			service.shutdownTask(task);
			
			waitUntilTaskHasStopped(task, MAX_WAIT_TIME_IN_MILLISECONDS);
			
			Assert.assertFalse(task.getStarted());
			Assert.assertFalse(task.getTaskInstance().isExecuting());
			
		}
		catch (Exception e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
			
		}
		
	}
	
	@Test(expected = TimeoutException.class)
	public void shouldRunIntoTimeoutWhileWaitingForTaskToStopExecuting() throws Exception {
		TaskDefinition task = null;
		try {
			setUpSchedulerService();
			task = getScheduledTaskDefinition(getTime(Calendar.SECOND, 1));
			
			waitUntilTaskIsExecuting(task, MAX_WAIT_TIME_IN_MILLISECONDS);
			
			// the task is NOT stopped to trigger the timeout while waiting
			
			waitUntilTaskHasStopped(task, 10);
			
		}
		catch (TimeoutException te) {
			throw te;
			
		}
		catch (Exception e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
			
		}
		finally {
			if (service != null && task != null) {
				service.shutdownTask(task);
				waitUntilTaskHasStopped(task, MAX_WAIT_TIME_IN_MILLISECONDS);
			}
		}
	}
	
}
