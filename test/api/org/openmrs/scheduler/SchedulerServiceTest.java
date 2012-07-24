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
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;
import org.junit.Test;
import org.openmrs.api.context.Context;
import org.openmrs.scheduler.tasks.AbstractTask;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.util.OpenmrsClassLoader;
import org.openmrs.util.OpenmrsConstants;

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
	
	private static List<String> outputForInitExecSync = new ArrayList<String>();
	
	static class SampleTask5 extends AbstractTask {
		
		public void initialize(TaskDefinition config) {
			synchronized (outputForInitExecSync) {
				outputForInitExecSync.add("INIT-START-5");
			}
			super.initialize(config);
			try {
				Thread.sleep(2000);
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
				outputForInitExecSync.add("START-5");
			}
			try {
				Thread.sleep(2000);
			}
			catch (InterruptedException e) {
				log.error("Error generated", e);
			}
			synchronized (outputForInitExecSync) {
				outputForInitExecSync.add("END-5");
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
		t5.setRepeatInterval(10L);
		t5.setTaskClass(SampleTask5.class.getName());
		
		Calendar startTime5 = Calendar.getInstance();
		startTime5.add(Calendar.SECOND, 1);
		t5.setStartTime(startTime5.getTime());
		schedulerService.scheduleTask(t5);
		Thread.sleep(5000);
		schedulerService.shutdownTask(t5);
		assertEquals(Arrays.asList("INIT-START-5", "INIT-END-5", "START-5", "END-5"), outputForInitExecSync);
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
		def.setTaskClass(SampleTask1.class.getName());
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

        public void shutdown() {}
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
		td.setRepeatInterval(1L);
		td.setTaskClass(BareTask.class.getName());

		Calendar startTime = Calendar.getInstance();
		startTime.add(Calendar.SECOND, 1);
		td.setStartTime(startTime.getTime());
		schedulerService.scheduleTask(td);
		Thread.sleep(2000);
		schedulerService.shutdownTask(td);
		
		assertTrue(BareTask.outputList.contains("TEST"));
        System.out.println(BareTask.outputList);
	}

	/**
	 * Task opens a session and stores the execution time.
	 */
	static class SessionTask extends AbstractTask {
		public void execute() {
            try {
                // Do something
                Context.openSession();
                Context.addProxyPrivilege(OpenmrsConstants.PRIV_MANAGE_IMPLEMENTATION_ID);
                Context.getAdministrationService().getImplementationId();
                actualExecutionTime = System.currentTimeMillis();

            } finally {
                Context.removeProxyPrivilege(OpenmrsConstants.PRIV_MANAGE_IMPLEMENTATION_ID);
                Context.closeSession();
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
		td.setRepeatInterval(1L);
		td.setTaskClass(SessionTask.class.getName());
        Calendar startTime = Calendar.getInstance();
        startTime.add(Calendar.SECOND, 1);
        td.setStartTime(startTime.getTime());
    	service.saveTask(td);

		service.scheduleTask(td);
		Thread.sleep(2000);
		service.shutdownTask(td);

        td = service.getTaskByName(NAME);
		assertEquals("Last execution time in seconds is wrong", actualExecutionTime.longValue() / 1000, td.getLastExecutionTime().getTime() / 1000, 1);
	}
}
