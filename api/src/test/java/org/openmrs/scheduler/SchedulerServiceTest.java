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
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openmrs.api.context.Context;
import org.openmrs.scheduler.tasks.AbstractTask;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.util.OpenmrsClassLoader;
import org.springframework.util.StringUtils;

/**
 * TODO test all methods in ScheduleService
 */
public class SchedulerServiceTest extends BaseContextSensitiveTest {
	
	private static Log log = LogFactory.getLog(SchedulerServiceTest.class);
	
	// so that we can guarantee tests running accurately instead of tests interfering with the next
	public final Integer SAVE_TASK_LOCK = new Integer(1);
	
	// each task provides a key that will be used in this map.  The value is the output
	private static Map<String, String> output = new HashMap<String, String>();
	
	@Before
	public void setUp() throws Exception {
		Context.flushSession();
		
		Collection<TaskDefinition> tasks = Context.getSchedulerService().getRegisteredTasks();
		for (TaskDefinition task : tasks) {
			Context.getSchedulerService().shutdownTask(task);
			Context.getSchedulerService().deleteTask(task.getId());
		}
		
		Context.flushSession();
	}
	
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
	
	/**
	 * Longer running class used to demonstrate tasks running concurrently
	 */
	public static class ExecutePrintingTask extends AbstractTask {
		
		public void execute() {
			String outputKey = getTaskDefinition().getProperty("outputKey");
			appendOutput(outputKey, getTaskDefinition().getProperty("id"));
			
			try {
				Thread.sleep(Integer.valueOf(getTaskDefinition().getProperty("delay")));
			}
			catch (InterruptedException e) {
				log.error("Error generated", e);
			}
			
			appendOutput(outputKey, getTaskDefinition().getProperty("id"));
			
		}
	}
	
	/**
	 * Helper method to append the given text to the "output" static variable map
	 * <br/>
	 * Map will contain string like "text, text1, text2"
	 * 
	 * @param outputKey the key for the "output" map
	 * @param the text to append to the value in the output map with the given key
	 */
	public synchronized static void appendOutput(String outputKey, String appendText) {
		if (StringUtils.hasLength(output.get(outputKey)))
			output.put(outputKey, output.get(outputKey) + ", " + appendText);
		else
			output.put(outputKey, appendText);
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
	@Ignore("TRUNK-4212 SchedulerServiceTest fails depending on thread scheduling")
	public void shouldAllowTwoTasksToRunConcurrently() throws Exception {
		SchedulerService schedulerService = Context.getSchedulerService();
		
		TaskDefinition t1 = new TaskDefinition();
		t1.setId(1);
		t1.setStartOnStartup(false);
		t1.setStartTime(null);
		t1.setTaskClass(ExecutePrintingTask.class.getName());
		t1.setProperty("id", "TASK-1");
		t1.setProperty("delay", "400"); // must be longer than t2's delay
		t1.setProperty("outputKey", "shouldAllowTwoTasksToRunConcurrently");
		t1.setName("name");
		t1.setRepeatInterval(5000l);
		
		TaskDefinition t2 = new TaskDefinition();
		t2.setId(2);
		t2.setStartOnStartup(false);
		t2.setStartTime(null);
		t2.setTaskClass(ExecutePrintingTask.class.getName());
		t2.setProperty("id", "TASK-2");
		t2.setProperty("delay", "100"); // must be shorter than t1's delay
		t2.setProperty("outputKey", "shouldAllowTwoTasksToRunConcurrently");
		t2.setName("name");
		t2.setRepeatInterval(5000l);
		
		synchronized (SAVE_TASK_LOCK) {
			schedulerService.scheduleTask(t1);
			Thread.sleep(50); // so t2 doesn't start before t1 due to random millisecond offsets
			schedulerService.scheduleTask(t2);
			Thread.sleep(2500); // must be longer than t2's delay
			assertEquals("TASK-1, TASK-2, TASK-2, TASK-1", output.get("shouldAllowTwoTasksToRunConcurrently"));
		}
	}
	
	/**
	 * Longer init'ing class for concurrent init test
	 */
	public static class SimpleTask extends AbstractTask {
		
		public void initialize(TaskDefinition config) {
			String outputKey = config.getProperty("outputKey");
			appendOutput(outputKey, config.getProperty("id"));
			
			super.initialize(config);
			try {
				// must be less than delay before printing
				Thread.sleep(Integer.valueOf(config.getProperty("delay")));
			}
			catch (InterruptedException e) {
				log.error("Error generated", e);
			}
			
			appendOutput(outputKey, config.getProperty("id"));
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
	@Ignore("TRUNK-4212 SchedulerServiceTest fails depending on thread scheduling")
	public void shouldAllowTwoTasksInitMethodsToRunConcurrently() throws Exception {
		SchedulerService schedulerService = Context.getSchedulerService();
		
		TaskDefinition t3 = new TaskDefinition();
		t3.setStartOnStartup(false);
		t3.setStartTime(null); // so it starts immediately
		t3.setTaskClass(SimpleTask.class.getName());
		t3.setProperty("id", "TASK-3");
		t3.setProperty("delay", "300"); // must be longer than t4's delay
		t3.setProperty("outputKey", "shouldAllowTwoTasksInitMethodsToRunConcurrently");
		t3.setName("name");
		t3.setRepeatInterval(5000l);
		
		TaskDefinition t4 = new TaskDefinition();
		t4.setStartOnStartup(false);
		t4.setStartTime(null); // so it starts immediately
		t4.setTaskClass(SimpleTask.class.getName());
		t4.setProperty("id", "TASK-4");
		t4.setProperty("delay", "100");
		t4.setProperty("outputKey", "shouldAllowTwoTasksInitMethodsToRunConcurrently");
		t4.setName("name");
		t4.setRepeatInterval(5000l);
		
		// both of these tasks start immediately
		synchronized (SAVE_TASK_LOCK) {
			schedulerService.scheduleTask(t3); // starts first, ends last
			schedulerService.scheduleTask(t4); // starts last, ends first
		}
		Thread.sleep(500); // must be greater than task3 delay so that it prints out its end
		assertEquals("TASK-3, TASK-4, TASK-4, TASK-3", output.get("shouldAllowTwoTasksInitMethodsToRunConcurrently"));
		
		// cleanup
		schedulerService.shutdownTask(t3);
		schedulerService.shutdownTask(t4);
	}
	
	public static class SampleTask5 extends AbstractTask {
		
		public void initialize(TaskDefinition config) {
			
			String outputKey = config.getProperty("outputKey");
			appendOutput(outputKey, "INIT-START-5");
			
			super.initialize(config);
			try {
				Thread.sleep(700);
			}
			catch (InterruptedException e) {
				log.error("Error generated", e);
			}
			
			appendOutput(outputKey, "INIT-END-5");
		}
		
		public void execute() {
			String outputKey = getTaskDefinition().getProperty("outputKey");
			appendOutput(outputKey, "IN EXECUTE");
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
		t5.setProperty("outputKey", "shouldNotAllowTaskExecuteToRunBeforeInitializationIsComplete");
		t5.setName("name");
		t5.setRepeatInterval(5000l);
		
		synchronized (SAVE_TASK_LOCK) {
			schedulerService.scheduleTask(t5);
			Thread.sleep(2500);
			assertEquals("INIT-START-5, INIT-END-5, IN EXECUTE", output
			        .get("shouldNotAllowTaskExecuteToRunBeforeInitializationIsComplete"));
		}
	}
	
	@Test
	public void saveTask_shouldSaveTaskToTheDatabase() throws Exception {
		SchedulerService service = Context.getSchedulerService();
		
		TaskDefinition def = new TaskDefinition();
		final String TASK_NAME = "This is my test! 123459876";
		def.setName(TASK_NAME);
		def.setStartOnStartup(false);
		def.setRepeatInterval(10L);
		def.setTaskClass(ExecutePrintingTask.class.getName());
		
		synchronized (SAVE_TASK_LOCK) {
			int size = service.getRegisteredTasks().size();
			service.saveTask(def);
			Assert.assertEquals(size + 1, service.getRegisteredTasks().size());
		}
		
		def = service.getTaskByName(TASK_NAME);
		Assert.assertEquals(Context.getAuthenticatedUser().getUserId(), def.getCreator().getUserId());
	}
	
	/**
	 * Sample task that does not extend AbstractTask
	 */
	public static class BareTask implements Task {
		
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
	 * Task which does not return TaskDefinition in getTaskDefinition should run without throwing
	 * exceptions.
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
		td.setName("name");
		td.setRepeatInterval(5000l);
		
		synchronized (SAVE_TASK_LOCK) {
			schedulerService.scheduleTask(td);
		}
		Thread.sleep(500);
		
		assertTrue(BareTask.outputList.contains("TEST"));
	}
	
	/**
	 * Task opens a session and stores the execution time.
	 */
	public static class SessionTask extends AbstractTask {
		
		public void execute() {
			try {
				// something would happen here...
				
				actualExecutionTime = System.currentTimeMillis();
			}
			finally {}
			
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
		td.setRepeatInterval(new Long(0));//0 indicates single execution
		synchronized (SAVE_TASK_LOCK) {
			service.saveTask(td);
			service.scheduleTask(td);
		}
		
		// refetch the task
		td = service.getTaskByName(NAME);
		
		// sleep a while until the task has executed, up to 30 times
		for (int x = 0; x < 30 && (actualExecutionTime == null || td.getLastExecutionTime() == null); x++)
			Thread.sleep(200);
		
		Assert
		        .assertNotNull(
		            "The actualExecutionTime variable is null, so either the SessionTask.execute method hasn't finished or didn't get run",
		            actualExecutionTime);
		assertEquals("Last execution time in seconds is wrong", actualExecutionTime.longValue() / 1000, td
		        .getLastExecutionTime().getTime() / 1000, 1);
	}
}
