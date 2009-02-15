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
package org.openmrs.scheduler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.openmrs.api.context.Context;
import org.openmrs.scheduler.tasks.AbstractTask;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.util.OpenmrsClassLoader;

/**
 * TODO test all methods in ScheduleService
 */
public class SchedulerServiceTest extends BaseContextSensitiveTest {
	
	private static Log log = LogFactory.getLog(SchedulerServiceTest.class);
	
	@Test
	public void shouldResolveValidTaskClass() throws Exception {
		String className = "org.openmrs.scheduler.tasks.TestTask";
		try {
			Class c = OpenmrsClassLoader.getInstance().loadClass(className);
			Object o = c.newInstance();
			if (o instanceof Task)
				assertTrue("Class " + className + " is a valid Task", true);
			else
				fail("Class " + className + " is not a valid Task");
			
		}
		catch (ClassNotFoundException e) {
			fail(e.getMessage());
		}
	}
	
	@Test
	public void shouldNotResolveInvalidClass() throws Exception {
		String className = "org.openmrs.scheduler.tasks.InvalidTask";
		try {
			Class c = OpenmrsClassLoader.getInstance().loadClass(className);
			Object o = c.newInstance();
			if (o instanceof Task)
				fail("Class " + className + " is not supposed to be a valid Task");
			else
				assertTrue("Class " + className + " is not a valid Task", true);
			
		}
		catch (ClassNotFoundException e) {
			assertTrue("Class " + className + " was not found, as expected", true);
		}
		
	}
	
	@Test
	public void shouldResolveModuleTaskClass() throws Exception {
		
	}
	
	/**
	 * Tests whether the scheduler is handing out the correct execution times for tasks.
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldExecuteOnGivenInterval() throws Exception {
		
		// Represents the start time of the task (right now)
		Calendar startTime = Calendar.getInstance();
		
		// Execute task every 4 minutes
		Long repeatInterval = new Long(4 * 60);
		
		// Create the new task
		TaskDefinition taskDefinition = new TaskDefinition();
		taskDefinition.setStartTime(startTime.getTime());
		taskDefinition.setRepeatInterval(repeatInterval);
		
		// Add four minutes to the start time
		startTime.add(Calendar.MINUTE, 4);
		
		// Get the next scheduled execution time for this task 
		Date nextTime = SchedulerUtil.getNextExecution(taskDefinition);
		
		// Assert that the next execution time is equal to startTime + 4 minutes
		assertEquals(startTime.getTime(), nextTime);
		
	}
	
	private static List<String> outputForConcurrentTasks = new ArrayList<String>();
	
	/**
	 * Longer running class used to demonstrate tasks running concurrently
	 */
	static class SampleTask1 extends AbstractTask {
		
		public void execute() {
			synchronized (outputForConcurrentTasks) {
				outputForConcurrentTasks.add("START-1");
			}
			try {
				Thread.sleep(3000);
			}
			catch (InterruptedException e) {
				log.error("Error generated", e);
			}
			synchronized (outputForConcurrentTasks) {
				outputForConcurrentTasks.add("END-1");
			}
		}
	}
	
	/**
	 * Shorter running class used to demonstrate tasks running concurrently
	 */
	static class SampleTask2 extends AbstractTask {
		
		public void execute() {
			synchronized (outputForConcurrentTasks) {
				outputForConcurrentTasks.add("START-2");
			}
			try {
				Thread.sleep(1000);
			}
			catch (InterruptedException e) {
				log.error("Error generated", e);
			}
			synchronized (outputForConcurrentTasks) {
				outputForConcurrentTasks.add("END-2");
			}
		}
	}
	
	/**
	 * Demonstrates concurrent running for tasks
	 * 
	 * <pre>
	 *             |
	 * SampleTask2 |    ----
	 * SampleTask1 |------------
	 *             |_____________ time
	 *              ^   ^   ^   ^
	 * Output:     S-1 S-2 E-2 E-1
	 * </pre>
	 */
	@Test
	public void shouldAllowTwoTasksToRunConcurrently() throws Exception {
		SchedulerService schedulerService = Context.getSchedulerService();
		
		TaskDefinition t1 = new TaskDefinition();
		t1.setId(1);
		t1.setStartOnStartup(false);
		t1.setRepeatInterval(10L);
		t1.setTaskClass(SampleTask1.class.getName());
		
		TaskDefinition t2 = new TaskDefinition();
		t2.setId(2);
		t2.setStartOnStartup(false);
		t2.setRepeatInterval(10L);
		t2.setTaskClass(SampleTask2.class.getName());
		
		Calendar startTime1 = Calendar.getInstance();
		startTime1.add(Calendar.SECOND, 1);
		t1.setStartTime(startTime1.getTime());
		
		Calendar startTime2 = Calendar.getInstance();
		startTime2.setTime(startTime1.getTime());
		// Task2 starts one second after Task1
		startTime2.add(Calendar.SECOND, 1);
		t2.setStartTime(startTime2.getTime());
		
		schedulerService.scheduleTask(t1);
		schedulerService.scheduleTask(t2);
		Thread.sleep(5000);
		schedulerService.shutdownTask(t1);
		schedulerService.shutdownTask(t2);
		assertEquals(Arrays.asList("START-1", "START-2", "END-2", "END-1"), outputForConcurrentTasks);
	}
	
	private static List<String> outputForConcurrentInit = new ArrayList<String>();
	
	/**
	 * Longer init'ing class for concurrent init test
	 */
	static class SampleTask3 extends AbstractTask {
		
		public void initialize(TaskDefinition config) {
			synchronized (outputForConcurrentInit) {
				outputForConcurrentInit.add("INIT-START-3");
			}
			super.initialize(config);
			try {
				Thread.sleep(3000);
			}
			catch (InterruptedException e) {
				log.error("Error generated", e);
			}
			synchronized (outputForConcurrentInit) {
				outputForConcurrentInit.add("INIT-END-3");
			}
		}
		
		public void execute() {
		}
	}
	
	/**
	 * Shorter init'ing class for the concurrent init test
	 */
	static class SampleTask4 extends AbstractTask {
		
		public void initialize(TaskDefinition config) {
			synchronized (outputForConcurrentInit) {
				outputForConcurrentInit.add("INIT-START-4");
			}
			super.initialize(config);
			try {
				Thread.sleep(1000);
			}
			catch (InterruptedException e) {
				log.error("Error generated", e);
			}
			synchronized (outputForConcurrentInit) {
				outputForConcurrentInit.add("INIT-END-4");
			}
		}
		
		public void execute() {
		}
	}
	
	/**
	 * Demonstrates concurrent initializing for tasks
	 * 
	 * <pre>
	 *             |
	 * SampleTask4 |    ----
	 * SampleTask3 |------------
	 *             |_____________ time
	 *              ^   ^   ^   ^
	 * Output:     S-3 S-4 E-4 E-3
	 * </pre>
	 */
	@Test
	public void shouldAllowTwoTasksInitMethodsToRunConcurrently() throws Exception {
		SchedulerService schedulerService = Context.getSchedulerService();
		
		TaskDefinition t3 = new TaskDefinition();
		t3.setId(3);
		t3.setStartOnStartup(false);
		t3.setRepeatInterval(10L);
		t3.setTaskClass(SampleTask3.class.getName());
		
		TaskDefinition t4 = new TaskDefinition();
		t4.setId(4);
		t4.setStartOnStartup(false);
		t4.setRepeatInterval(10L);
		t4.setTaskClass(SampleTask4.class.getName());
		
		Calendar startTime3 = Calendar.getInstance();
		startTime3.add(Calendar.SECOND, 1);
		t3.setStartTime(startTime3.getTime());
		
		Calendar startTime4 = Calendar.getInstance();
		startTime4.setTime(startTime3.getTime());
		// Task4 starts one second after Task3
		startTime4.add(Calendar.SECOND, 1);
		t4.setStartTime(startTime4.getTime());
		
		schedulerService.scheduleTask(t3);
		schedulerService.scheduleTask(t4);
		Thread.sleep(4000);
		schedulerService.shutdownTask(t3);
		schedulerService.shutdownTask(t4);
		assertEquals(Arrays.asList("INIT-START-3", "INIT-START-4", "INIT-END-4", "INIT-END-3"), outputForConcurrentInit);
	}
}
