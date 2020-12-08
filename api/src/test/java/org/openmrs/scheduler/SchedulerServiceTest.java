/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.scheduler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.Collection;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.openmrs.api.context.Context;
import org.openmrs.scheduler.tasks.AbstractTask;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;
import org.openmrs.util.OpenmrsClassLoader;

/**
 * TODO test all methods in SchedulerService
 */
@Disabled("https://issues.openmrs.org/browse/TRUNK-4212")
public class SchedulerServiceTest extends BaseContextSensitiveTest {
	
	// so that we can guarantee tests running accurately instead of tests interfering with the next
	public final Integer TASK_TEST_METHOD_LOCK = 1;
	
	// used to check for concurrent task execution. Only initialized by code protected by TASK_TEST_METHOD_LOCK.
	public static CountDownLatch latch;
	
	public static AtomicBoolean awaitFailed = new AtomicBoolean(false);
	
	public static AtomicBoolean consecutiveInitResult = new AtomicBoolean(false);
	
	// time to wait for concurrent tasks to execute, should only wait this long if there's a test failure
	public static final long CONCURRENT_TASK_WAIT_MS = 30000;
	
	private static final Logger log = LogManager.getLogger(SchedulerServiceTest.class);
	
	@BeforeEach
	public void setUp() throws Exception {
		// Temporary logger level changes to debug TRUNK-4212
		((org.apache.logging.log4j.core.Logger) LogManager.getLogger("org.hibernate.SQL")).setLevel(Level.DEBUG);
		((org.apache.logging.log4j.core.Logger) LogManager.getLogger("org.hibernate.type")).setLevel(Level.TRACE);
		((org.apache.logging.log4j.core.Logger) LogManager.getLogger("org.openmrs.api")).setLevel(Level.DEBUG);
		((org.apache.logging.log4j.core.Logger) LogManager.getLogger("org.openmrs.scheduler")).setLevel(Level.DEBUG);
		log.debug("SchedulerServiceTest setup() start");
		Context.flushSession();
		
		Collection<TaskDefinition> tasks = Context.getSchedulerService().getRegisteredTasks();
		for (TaskDefinition task : tasks) {
			Context.getSchedulerService().shutdownTask(task);
			Context.getSchedulerService().deleteTask(task.getId());
		}
		
		Context.flushSession();
		log.debug("SchedulerServiceTest setup() complete");
	}
	
	@AfterEach
	public void cleanUp() throws Exception {
		// Temporary logger level changes to debug TRUNK-4212
		((org.apache.logging.log4j.core.Logger) LogManager.getLogger("org.hibernate.SQL")).setLevel(Level.WARN);
		((org.apache.logging.log4j.core.Logger) LogManager.getLogger("org.hibernate.type")).setLevel(Level.WARN);
		((org.apache.logging.log4j.core.Logger) LogManager.getLogger("org.openmrs.api")).setLevel(Level.WARN);
		((org.apache.logging.log4j.core.Logger) LogManager.getLogger("org.openmrs.scheduler")).setLevel(Level.WARN);
	}
	
	@Test
	public void shouldResolveValidTaskClass() throws Exception {
		String className = "org.openmrs.scheduler.tasks.TestTask";
		Class<?> c = OpenmrsClassLoader.getInstance().loadClass(className);
		Object o = c.newInstance();
		if (o instanceof Task)
			assertTrue(true, "Class " + className + " is a valid Task");
		else
			fail("Class " + className + " is not a valid Task");
	}
	
	@Test
	public void shouldNotResolveInvalidClass() {
		
		assertThrows(ClassNotFoundException.class,
			() -> OpenmrsClassLoader.getInstance().loadClass("org.openmrs.scheduler.tasks.InvalidTask"));
	}
	
	private TaskDefinition makeRepeatingTaskThatStartsImmediately(String taskClassName) {
		TaskDefinition taskDef = new TaskDefinition();
		taskDef.setTaskClass(taskClassName);
		taskDef.setStartOnStartup(false);
		taskDef.setStartTime(null);
		taskDef.setName("name");
		taskDef.setRepeatInterval(CONCURRENT_TASK_WAIT_MS * 10); // latch should timeout before task ever repeats
		// save task definition to generate a unique ID, otherwise the scheduler thinks they're duplicates and tries to shut one down
		Context.getSchedulerService().saveTaskDefinition(taskDef);
		return taskDef;
	}
	
	/**
	 * Demonstrates concurrent running for tasks
	 */
	@Test
	public void shouldAllowTwoTasksToRunConcurrently() throws Exception {
		TaskDefinition t1 = makeRepeatingTaskThatStartsImmediately(LatchExecuteTask.class.getName());
		TaskDefinition t2 = makeRepeatingTaskThatStartsImmediately(LatchExecuteTask.class.getName());
		
		checkTasksRunConcurrently(t1, t2);
	}
	
	/**
	 * Demonstrates concurrent initializing for tasks
	 */
	@Test
	public void shouldAllowTwoTasksInitMethodsToRunConcurrently() throws Exception {
		TaskDefinition t3 = makeRepeatingTaskThatStartsImmediately(LatchInitializeTask.class.getName());
		TaskDefinition t4 = makeRepeatingTaskThatStartsImmediately(LatchInitializeTask.class.getName());
		
		checkTasksRunConcurrently(t3, t4);
	}
	
	private void checkTasksRunConcurrently(TaskDefinition t1, TaskDefinition t2) throws SchedulerException,
	        InterruptedException {
		
		SchedulerService schedulerService = Context.getSchedulerService();
		
		// synchronized on a class level object in case a test runner is running test methods concurrently
		synchronized (TASK_TEST_METHOD_LOCK) {
			latch = new CountDownLatch(2);
			awaitFailed.set(false);
			
			schedulerService.scheduleTask(t1);
			schedulerService.scheduleTask(t2);
			
			// wait for the tasks to call countDown()
			assertTrue(latch.await(CONCURRENT_TASK_WAIT_MS, TimeUnit.MILLISECONDS), "methods ran consecutively or not at all");
			// the main await() didn't fail so both tasks ran and called countDown(), 
			// but if the first await() failed and the latch still reached 0 then the tasks must have been running consecutively 
			assertTrue(!awaitFailed.get(), "methods ran consecutively");
		}
		schedulerService.shutdownTask(t1);
		schedulerService.shutdownTask(t2);
	}
	
	public abstract static class LatchTask extends AbstractTask {
		
		protected void waitForLatch() {
			try {
				latch.countDown();
				// wait here until the other task thread(s) also countDown the latch
				// if they do then they must be executing concurrently with this task
				if (!latch.await(CONCURRENT_TASK_WAIT_MS, TimeUnit.MILLISECONDS)) {
					// this wait timed out, record it as otherwise the next
					// task(s) could execute consecutively rather than concurrently 
					awaitFailed.set(true);
				}
			}
			catch (InterruptedException ignored) {}
		}
	}
	
	/**
	 * task that waits in its initialize method until all other tasks on the same latch have called
	 * initialize()
	 */
	public static class LatchInitializeTask extends LatchTask {
		
		@Override
		public void initialize(TaskDefinition config) {
			super.initialize(config);
			waitForLatch();
		}
		
		@Override
		public void execute() {
		}
	}
	
	/**
	 * task that waits in its execute method until all other tasks on the same latch have called
	 * execute()
	 */
	public static class LatchExecuteTask extends LatchTask {
		
		@Override
		public void initialize(TaskDefinition config) {
			super.initialize(config);
		}
		
		@Override
		public void execute() {
			waitForLatch();
		}
	}
	
	/**
	 * task that checks for its execute method running at the same time as its initialize method
	 */
	public static class InitSequenceTestTask extends AbstractTask {
		
		@Override
		public void initialize(TaskDefinition config) {
			
			super.initialize(config);
			
			// wait for any other thread to run the execute method
			try {
				Thread.sleep(700);
			}
			catch (InterruptedException ignored) {}
			
			// set to false if execute() method was running concurrently and has cleared the latch
			consecutiveInitResult.set(latch.getCount() != 0);
		}
		
		@Override
		public void execute() {
			// clear the latch to signal the main thread
			latch.countDown();
		}
	}
	
	/**
	 * Demonstrates that initialization of a task is accomplished before its execution without
	 * interleaving, which is a non-trivial behavior in the presence of a threaded initialization
	 * method (as implemented in TaskThreadedInitializationWrapper)
	 */
	@Test
	public void shouldNotAllowTaskExecuteToRunBeforeInitializationIsComplete() throws Exception {
		SchedulerService schedulerService = Context.getSchedulerService();
		
		TaskDefinition t5 = new TaskDefinition();
		t5.setStartOnStartup(false);
		t5.setStartTime(null); // immediate start
		t5.setTaskClass(InitSequenceTestTask.class.getName());
		t5.setName("name");
		t5.setRepeatInterval(CONCURRENT_TASK_WAIT_MS * 4);
		
		synchronized (TASK_TEST_METHOD_LOCK) {
			// wait for the task to complete
			latch = new CountDownLatch(1);
			consecutiveInitResult.set(false);
			schedulerService.saveTaskDefinition(t5);
			schedulerService.scheduleTask(t5);
			assertTrue(latch.await(CONCURRENT_TASK_WAIT_MS, TimeUnit.MILLISECONDS) && consecutiveInitResult.get(), "Init and execute methods should run consecutively");
		}
		schedulerService.shutdownTask(t5);
	}
	
	@Test
	public void saveTask_shouldSaveTaskToTheDatabase() throws Exception {
		log.debug("saveTask_shouldSaveTaskToTheDatabase start");
		SchedulerService service = Context.getSchedulerService();
		
		TaskDefinition def = new TaskDefinition();
		final String TASK_NAME = "This is my test! 123459876";
		def.setName(TASK_NAME);
		def.setStartOnStartup(false);
		def.setRepeatInterval(10000000L);
		def.setTaskClass(LatchExecuteTask.class.getName());
		
		synchronized (TASK_TEST_METHOD_LOCK) {
			Collection<TaskDefinition> tasks = service.getRegisteredTasks();
			for (TaskDefinition task : tasks) {
				log.debug("Task dump 1: " + task);
			}
			int size = tasks.size();
			service.saveTaskDefinition(def);
			tasks = service.getRegisteredTasks();
			for (TaskDefinition task : tasks) {
				log.debug("Task dump 2:" + task);
			}
			assertEquals(size + 1, tasks.size());
		}
		
		def = service.getTaskByName(TASK_NAME);
		assertEquals(Context.getAuthenticatedUser().getUserId(), def.getCreator().getUserId());
		log.debug("saveTask_shouldSaveTaskToTheDatabase end");
	}
	
	/**
	 * Sample task that does not extend AbstractTask
	 */
	public static class BareTask implements Task {
		
		@Override
		public void execute() {
			latch.countDown();
		}
		
		@Override
		public TaskDefinition getTaskDefinition() {
			return null;
		}
		
		@Override
		public void initialize(TaskDefinition definition) {
		}
		
		@Override
		public boolean isExecuting() {
			return false;
		}
		
		@Override
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
		td.setName("Task");
		td.setStartOnStartup(false);
		td.setTaskClass(BareTask.class.getName());
		td.setStartTime(null);
		td.setName("name");
		td.setRepeatInterval(5000L);
		
		synchronized (TASK_TEST_METHOD_LOCK) {
			latch = new CountDownLatch(1);
			schedulerService.saveTaskDefinition(td);
			schedulerService.scheduleTask(td);
			assertTrue(latch.await(CONCURRENT_TASK_WAIT_MS, TimeUnit.MILLISECONDS));
		}
	}
	
	/**
	 * Just stores the execution time.
	 */
	public static class StoreExecutionTimeTask extends AbstractTask {
		
		@Override
		public void execute() {
			actualExecutionTime = System.currentTimeMillis();
			// signal the test method that the task has executed
			latch.countDown();
		}
	}
	
	public static Long actualExecutionTime;
	
	/**
	 * Check saved last execution time.
	 */
	@Test
	public void shouldSaveLastExecutionTime() throws Exception {
		log.debug("shouldSaveLastExecutionTime start");
		final String NAME = "StoreExecutionTime Task";
		SchedulerService service = Context.getSchedulerService();
		
		TaskDefinition td = new TaskDefinition();
		td.setName(NAME);
		td.setStartOnStartup(false);
		td.setTaskClass(StoreExecutionTimeTask.class.getName());
		td.setStartTime(null);
		td.setRepeatInterval(0L);//0 indicates single execution
		synchronized (TASK_TEST_METHOD_LOCK) {
			latch = new CountDownLatch(1);
			service.saveTaskDefinition(td);
			service.scheduleTask(td);
			
			// wait for the task to execute
			assertTrue(latch.await(CONCURRENT_TASK_WAIT_MS, TimeUnit.MILLISECONDS), "task didn't execute");
		}
		
		log.debug("shouldSaveLastExecutionTime task done");
		
		// wait for the SchedulerService to update the execution time
		for (int x = 0; x < 100; x++) {
			// refetch the task
			td = service.getTaskByName(NAME);
			if (td.getLastExecutionTime() != null) {
				log.debug("shouldSaveLastExecutionTime wait done");
				break;
			}
			Thread.sleep(200);
		}
		assertNotNull(actualExecutionTime, "actualExecutionTime is null, so either the SessionTask.execute method hasn't finished or didn't get run");
		assertNotNull(td.getLastExecutionTime(), "lastExecutionTime is null, so the SchedulerService didn't save it");
		assertEquals(1, td.getLastExecutionTime().getTime() / 1000, actualExecutionTime / 1000, "Last execution time in seconds is wrong");
	}

	/**
	 * @see org.openmrs.scheduler.SchedulerService#getTaskByUuid(java.lang.String)
	 */
	@Test
	public void getTaskByUuid_shouldGetTaskByUuid() throws Exception {
		TaskDefinition td = Context.getSchedulerService().getTaskByUuid("1365a6da-6493-4e9b-b950-5af1b392aaa3");
		assertNotNull(td);
	}

	/**
	 * @see org.openmrs.scheduler.SchedulerService#getTaskByUuid(java.lang.String)
	 */
	@Test
	public void getTaskByUuid_shouldReturnNullWhenUuidDoesNotExist() throws Exception {
		TaskDefinition td = Context.getSchedulerService().getTaskByUuid("kncsjvcjvbevismcvbsnksndcsjbvjhvbn");
		assertNull(td);
	}
}
