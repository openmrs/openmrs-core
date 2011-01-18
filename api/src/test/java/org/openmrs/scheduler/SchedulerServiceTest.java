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
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;
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
		Class c = OpenmrsClassLoader.getInstance().loadClass(className);
		Object o = c.newInstance();
		if (o instanceof Task)
			assertTrue("Class " + className + " is a valid Task", true);
		else
			fail("Class " + className + " is not a valid Task");
	}
	
	@Test(expected = ClassNotFoundException.class)
	public void shouldNotResolveInvalidClass() throws Exception {
		String className = "org.openmrs.scheduler.tasks.InvalidTask";
		Class c = OpenmrsClassLoader.getInstance().loadClass(className);
		Object o = c.newInstance();
		if (o instanceof Task)
			fail("Class " + className + " is not supposed to be a valid Task");
		else
			assertTrue("Class " + className + " is not a valid Task", true);
	}
	
	private static List<String> outputForConcurrentTasks = new ArrayList<String>();
	
	/**
	 * Longer running class used to demonstrate tasks running concurrently
	 */
	static class ExecutePrintingTask extends AbstractTask {
		
		public void execute() {
			synchronized (outputForConcurrentTasks) {
				outputForConcurrentTasks.add(getTaskDefinition().getProperty("id"));
			}
			try {
				Thread.sleep(Integer.valueOf(getTaskDefinition().getProperty("delay")));
			}
			catch (InterruptedException e) {
				log.error("Error generated", e);
			}
			synchronized (outputForConcurrentTasks) {
				outputForConcurrentTasks.add(getTaskDefinition().getProperty("id"));
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
		t1.setStartTime(null);
		t1.setTaskClass(ExecutePrintingTask.class.getName());
		t1.setProperty("id", "TASK-1");
		t1.setProperty("delay", "400"); // must be longer than t2's delay
		
		TaskDefinition t2 = new TaskDefinition();
		t2.setId(2);
		t2.setStartOnStartup(false);
		t2.setStartTime(null);
		t2.setTaskClass(ExecutePrintingTask.class.getName());
		t2.setProperty("id", "TASK-2");
		t2.setProperty("delay", "100"); // must be shorter than t1's delay
		
		schedulerService.scheduleTask(t1);
		Thread.sleep(50); // so t2 doesn't start before t1 due to random millisecond offsets
		schedulerService.scheduleTask(t2);
		Thread.sleep(600); // must be longer than t2's delay
		assertEquals(Arrays.asList("TASK-1", "TASK-2", "TASK-2", "TASK-1"), outputForConcurrentTasks);
	}
	
	private static List<String> outputForConcurrentInit = new ArrayList<String>();
	
	/**
	 * Longer init'ing class for concurrent init test
	 */
	static class SimpleTask extends AbstractTask {
		
		public void initialize(TaskDefinition config) {
			synchronized (outputForConcurrentInit) {
				outputForConcurrentInit.add(config.getProperty("id"));
			}
			super.initialize(config);
			try {
				// must be less than delay before printing
				Thread.sleep(Integer.valueOf(config.getProperty("delay")));
			}
			catch (InterruptedException e) {
				log.error("Error generated", e);
			}
			synchronized (outputForConcurrentInit) {
				outputForConcurrentInit.add(config.getProperty("id"));
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
		t3.setStartTime(null); // so it starts immediately
		t3.setTaskClass(SimpleTask.class.getName());
		t3.setProperty("id", "TASK-3");
		t3.setProperty("delay", "300"); // must be longer than t4's delay
		
		TaskDefinition t4 = new TaskDefinition();
		t4.setId(4);
		t4.setStartOnStartup(false);
		t4.setStartTime(null); // so it starts immediately
		t4.setTaskClass(SimpleTask.class.getName());
		t4.setProperty("id", "TASK-4");
		t4.setProperty("delay", "100");
		
		// both of these tasks start immediately
		schedulerService.scheduleTask(t3); // starts first, ends last
		schedulerService.scheduleTask(t4); // starts last, ends first
		Thread.sleep(500); // must be greater than task3 delay so that it prints out its end
		assertEquals(Arrays.asList("TASK-3", "TASK-4", "TASK-4", "TASK-3"), outputForConcurrentInit);
		
		// cleanup
		schedulerService.shutdownTask(t3);
		schedulerService.shutdownTask(t4);
	}
	
	private static List<String> outputForInitExecSync = new ArrayList<String>();
	
	static class SampleTask5 extends AbstractTask {
		
		public void initialize(TaskDefinition config) {
			synchronized (outputForInitExecSync) {
				outputForInitExecSync.add("INIT-START-5");
			}
			super.initialize(config);
			try {
				Thread.sleep(700);
			}
			catch (InterruptedException e) {
				log.error("Error generated", e);
			}
			synchronized (outputForInitExecSync) {
				outputForInitExecSync.add("INIT-END-5");
			}
		}
		
		public void execute() {
			synchronized (outputForInitExecSync) {
				outputForInitExecSync.add("IN EXECUTE");
			}
		}
	}
	
	/**
	 * Demonstrates that initialization of a task is accomplished before its execution without
	 * interleaving, which is a non-trivial behavior in the presence of a threaded initialization
	 * method (as implemented in TaskThreadedInitializationWrapper)
	 * 
	 * <pre>
	 *             |
	 * SampleTask5 |------------
	 *             |_____________ time
	 *              ^   ^   ^   ^
	 * Output:     IS  IE   S   E
	 * </pre>
	 */
	@Test
	public void shouldNotAllowTaskExecuteToRunBeforeInitializationIsComplete() throws Exception {
		SchedulerService schedulerService = Context.getSchedulerService();
		
		TaskDefinition t5 = new TaskDefinition();
		t5.setId(5);
		t5.setStartOnStartup(false);
		t5.setStartTime(null); // immediate start
		t5.setTaskClass(SampleTask5.class.getName());
		
		schedulerService.scheduleTask(t5);
		Thread.sleep(1200);
		assertEquals(Arrays.asList("INIT-START-5", "INIT-END-5", "IN EXECUTE"), outputForInitExecSync);
	}
	
	@Test
	public void saveTask_shouldSaveTaskToTheDatabase() throws Exception {
		SchedulerService service = Context.getSchedulerService();
		Assert.assertEquals(0, service.getRegisteredTasks().size());
		
		TaskDefinition def = new TaskDefinition();
		final String TASK_NAME = "This is my test! 123459876";
		def.setName(TASK_NAME);
		def.setStartOnStartup(false);
		def.setRepeatInterval(10L);
		def.setTaskClass(ExecutePrintingTask.class.getName());
		service.saveTask(def);
		Assert.assertEquals(1, service.getRegisteredTasks().size());
		
		def = service.getTaskByName(TASK_NAME);
		Assert.assertEquals(Context.getAuthenticatedUser().getUserId(), def.getCreator().getUserId());
	}
	
	/**
	 * Sample task that does not extend AbstractTask
	 */
	static class BareTask implements Task {
		
		public static ArrayList outputList = new ArrayList();
		
		public void execute() {
			synchronized (outputList) {
				outputList.add("TEST");
			}
		}
		
		public TaskDefinition getTaskDefinition() {
			return null;
		}
		
		public void initialize(TaskDefinition definition) {
		}
		
		public boolean isExecuting() {
			return false;
		}
		
		public void shutdown() {
		}
	}
	
	/**
	 * Task which does not return TaskDefinition in getTaskDefinition should run without throwing exceptions.
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldNotThrowExceptionWhenTaskDefinitionIsNull() throws Exception {
		SchedulerService schedulerService = Context.getSchedulerService();
		
		TaskDefinition td = new TaskDefinition();
		td.setId(10);
		td.setName("Task");
		td.setStartOnStartup(false);
		td.setTaskClass(BareTask.class.getName());
		td.setStartTime(null);
		
		schedulerService.scheduleTask(td);
		Thread.sleep(500);
		
		assertTrue(BareTask.outputList.contains("TEST"));
	}
	
	/**
	 * Task opens a session and stores the execution time.
	 */
	static class SessionTask extends AbstractTask {
		
		public void execute() {
			try {
				// something would happen here...
				
				actualExecutionTime = System.currentTimeMillis();
				
			}
			finally {
			}
			
		}
	}
	
	public static Long actualExecutionTime;
	
	/**
	 * Check saved last execution time.
	 */
	@Test
	public void shouldSaveLastExecutionTime() throws Exception {
		final String NAME = "Session Task";
		SchedulerService service = Context.getSchedulerService();
		
		TaskDefinition td = new TaskDefinition();
		td.setName(NAME);
		td.setStartOnStartup(false);
		td.setTaskClass(SessionTask.class.getName());
		td.setStartTime(null);
		service.saveTask(td);
		
		service.scheduleTask(td);
		
		// refetch the task
		td = service.getTaskByName(NAME);
		
		// sleep a while until the task has executed, up to 5 times
		for (int x = 0; x < 30 && (actualExecutionTime == null || td.getLastExecutionTime() == null); x++)
			Thread.sleep(200);
		
		Assert.assertNotNull(actualExecutionTime);
		assertEquals("Last execution time in seconds is wrong", actualExecutionTime.longValue() / 1000, td
		        .getLastExecutionTime().getTime() / 1000, 1);
	}
}
