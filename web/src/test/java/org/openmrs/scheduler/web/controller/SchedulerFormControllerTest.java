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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.openmrs.api.context.Context;
import org.openmrs.scheduler.SchedulerService;
import org.openmrs.scheduler.Task;
import org.openmrs.scheduler.TaskDefinition;
import org.openmrs.test.Verifies;
import org.openmrs.web.test.BaseWebContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

/**
 * Contains tests for the SchedulerFormController
 */
public class SchedulerFormControllerTest extends BaseWebContextSensitiveTest {
	
	private static final String INITIAL_SCHEDULER_TASK_CONFIG_XML = "org/openmrs/web/include/SchedulerServiceTest.xml";
	
	@Autowired
	private SchedulerFormController controller;
	
	/**
	 * @see {@link SchedulerFormController#onSubmit(HttpServletRequest,HttpServletResponse,Object,BindException)}
	 */
	@Test
	@Verifies(value = "should reschedule a currently scheduled task", method = "onSubmit(HttpServletRequest,HttpServletResponse,Object,BindException)")
	public void onSubmit_shouldRescheduleACurrentlyScheduledTask() throws Exception {
		executeDataSet(INITIAL_SCHEDULER_TASK_CONFIG_XML);
		
		SchedulerService service = Context.getSchedulerService();
		TaskDefinition task = service.getTaskByName("Hello World Task");
		//sanity check
		Assert.assertNull(task.getTaskInstance());
		
		Calendar cal = Calendar.getInstance();
		//schedule the task to run in the next 5 min for testing
		cal.add(Calendar.MINUTE, 5);
		task.setStartTime(cal.getTime());
		service.saveTask(task);
		try {
			//start the task
			service.scheduleTask(task);
			//the task should have been started
			Assert.assertTrue(task.getStarted());
			Assert.assertNotNull(task.getTaskInstance());
			Task oldTaskInstance = task.getTaskInstance();
			
			SchedulerFormController controller = (SchedulerFormController) applicationContext
			        .getBean("schedulerFormController");
			MockHttpServletRequest mockRequest = new MockHttpServletRequest();
			mockRequest.setMethod("POST");
			mockRequest.setParameter("action", "");
			mockRequest.setParameter("taskId", "1");
			
			//postpone the start time by 2 min and submit the form
			cal.add(Calendar.MINUTE, 2);
			mockRequest.setParameter("startTime", new SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(cal.getTime()));
			ModelAndView mav = controller.handleRequest(mockRequest, new MockHttpServletResponse());
			assertNotNull(mav);
			assertTrue(mav.getModel().isEmpty());
			
			//the task should have been rescheduled to reflect the change in start time
			Assert.assertNotSame(oldTaskInstance, task.getTaskInstance());
		}
		finally {
			service.shutdownTask(task);
			//Ensure that the task was stopped
			Assert.assertFalse(task.getTaskInstance().isExecuting());
			Assert.assertFalse(task.getStarted());
		}
	}
	
	/**
	 * @see {@link SchedulerFormController#onSubmit(HttpServletRequest,HttpServletResponse,Object,BindException)}
	 */
	@Test
	@Verifies(value = "should not reschedule a task that is not currently scheduled", method = "onSubmit(HttpServletRequest,HttpServletResponse,Object,BindException)")
	public void onSubmit_shouldNotRescheduleATaskThatIsNotCurrentlyScheduled() throws Exception {
		executeDataSet(INITIAL_SCHEDULER_TASK_CONFIG_XML);
		
		SchedulerService service = Context.getSchedulerService();
		TaskDefinition task = service.getTaskByName("Hello World Task");
		//sanity check
		Assert.assertNull(task.getTaskInstance());
		
		Calendar cal = Calendar.getInstance();
		//schedule the task to run in the next 5 min for testing
		cal.add(Calendar.MINUTE, 5);
		task.setStartTime(cal.getTime());
		service.saveTask(task);
		Task oldTaskInstance = task.getTaskInstance();
		
		SchedulerFormController controller = (SchedulerFormController) applicationContext.getBean("schedulerFormController");
		MockHttpServletRequest mockRequest = new MockHttpServletRequest();
		mockRequest.setMethod("POST");
		mockRequest.setParameter("action", "");
		mockRequest.setParameter("taskId", "1");
		
		//postpone the start time by 2 min and submit the form
		cal.add(Calendar.MINUTE, 2);
		mockRequest.setParameter("startTime", new SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(cal.getTime()));
		ModelAndView mav = controller.handleRequest(mockRequest, new MockHttpServletResponse());
		assertNotNull(mav);
		assertTrue(mav.getModel().isEmpty());
		
		//the task shouldn't have been rescheduled
		Assert.assertEquals(oldTaskInstance, task.getTaskInstance());
	}
	
	/**
	 * @see {@link SchedulerFormController#onSubmit(HttpServletRequest,HttpServletResponse,Object,BindException)}
	 */
	@Test
	@Verifies(value = "should not reschedule a task if the start time has passed", method = "onSubmit(HttpServletRequest,HttpServletResponse,Object,BindException)")
	public void onSubmit_shouldNotRescheduleATaskIfTheStartTimeHasPassed() throws Exception {
		executeDataSet(INITIAL_SCHEDULER_TASK_CONFIG_XML);
		
		SchedulerService service = Context.getSchedulerService();
		TaskDefinition task = service.getTaskByName("Hello World Task");
		//sanity check
		Assert.assertNull(task.getTaskInstance());
		
		Calendar cal = Calendar.getInstance();
		//schedule the task to run in the next 5 min for testing
		cal.add(Calendar.MINUTE, 5);
		task.setStartTime(cal.getTime());
		service.saveTask(task);
		try {
			//start the task
			service.scheduleTask(task);
			//the task should have been started
			Assert.assertTrue(task.getStarted());
			Assert.assertNotNull(task.getTaskInstance());
			Task oldTaskInstance = task.getTaskInstance();
			
			SchedulerFormController controller = (SchedulerFormController) applicationContext
			        .getBean("schedulerFormController");
			MockHttpServletRequest mockRequest = new MockHttpServletRequest();
			mockRequest.setMethod("POST");
			mockRequest.setParameter("action", "");
			mockRequest.setParameter("taskId", "1");
			
			//set the start time to be in the past
			Calendar cal2 = Calendar.getInstance();
			cal2.add(Calendar.SECOND, -1);
			mockRequest.setParameter("startTime", new SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(cal2.getTime()));
			ModelAndView mav = controller.handleRequest(mockRequest, new MockHttpServletResponse());
			assertNotNull(mav);
			assertTrue(mav.getModel().isEmpty());
			
			//the task shouldn't have been rescheduled
			Assert.assertSame(oldTaskInstance, task.getTaskInstance());
		}
		finally {
			service.shutdownTask(task);
			//Ensure that the task was stopped
			Assert.assertFalse(task.getTaskInstance().isExecuting());
			Assert.assertFalse(task.getStarted());
			deleteAllData();
		}
	}
}
