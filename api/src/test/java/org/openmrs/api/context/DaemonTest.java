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
package org.openmrs.api.context;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.api.APIAuthenticationException;
import org.openmrs.api.APIException;
import org.openmrs.scheduler.Task;
import org.openmrs.scheduler.tasks.AbstractTask;
import org.openmrs.scheduler.tasks.HelloWorldTask;
import org.openmrs.scheduler.timer.TimerSchedulerTask;
import org.openmrs.test.BaseContextSensitiveTest;

/**
 * Tests the methods on the {@link Daemon} class
 */
public class DaemonTest extends BaseContextSensitiveTest {
	
	/**
	 * @see Daemon#executeScheduledTask(Task)
	 * @verifies not be called from other methods other than TimerSchedulerTask
	 */
	@Test
	public void executeScheduledTask_shouldNotBeCalledFromOtherMethodsOtherThanTimerSchedulerTask() throws Throwable {
		try {
			Daemon.executeScheduledTask(new HelloWorldTask());
			Assert.fail("Should not be here, an exception should have been thrown in the line above");
		}
		catch (APIException e) {
			Assert.assertTrue(e.getMessage().startsWith("This method can only be called from the TimerSchedulerTask class"));
		}
	}
	
	/**
	 * This uses a task that just marks itself as run when its "execute" method is called. This
	 * verifies that the Daemon class is getting past the class check and on to the task running
	 * step
	 * 
	 * @see Daemon#executeScheduledTask(Task)
	 * @verifies not throw error if called from a TimerSchedulerTask class
	 */
	@Test
	public void executeScheduledTask_shouldNotThrowErrorIfCalledFromATimerSchedulerTaskClass() throws Throwable {
		Task task = new PrivateTask();
		Assert.assertTrue(new PrivateSchedulerTask(task).runTheTest());
	}
	
	/**
	 * @see Daemon#runInNewDaemonThread(Runnable)
	 * @verifies throw error if called from a non daemon thread
	 */
	@Test
	public void runInNewDaemonThread_shouldThrowErrorIfCalledFromANonDaemonThread() {
		try {
			Daemon.runInNewDaemonThread(new Runnable() {
				
				@Override
				public void run() {
					// do nothing
				}
			});
			Assert.assertTrue("Should not hit this line, since the previous needed to throw an exception", false);
		}
		catch (APIAuthenticationException ex) {
			Assert.assertEquals("Only daemon threads can spawn new daemon threads", ex.getMessage());
		}
	}
	
	/**
	 * @see Daemon#runInNewDaemonThread(Runnable)
	 * @verifies not throw error if called from a daemon thread
	 */
	@Test
	public void runInNewDaemonThread_shouldNotThrowErrorIfCalledFromADaemonThread() throws Throwable {
		Task taskThatStartsAnotherThread = new TaskThatStartsAnotherThread();
		Assert.assertTrue(new PrivateSchedulerTask(taskThatStartsAnotherThread).runTheTest());
	}
	
	/**
	 * A TimerSchedulerTask that can call the daemon thread
	 * 
	 * @see DaemonTest#executeScheduledTask_shouldNotThrowErrorIfCalledFromATimerSchedulerTaskClass()
	 */
	private class PrivateSchedulerTask extends TimerSchedulerTask {
		
		private Task task;
		
		public PrivateSchedulerTask(Task task) {
			super(task);
			this.task = task;
		}
		
		/**
		 * Returns true/false whether the task was successfully run by the Daemon user
		 * 
		 * @return
		 */
		public boolean runTheTest() throws Throwable {
			Daemon.executeScheduledTask(this.task);
			return ((PrivateTask) task).wasRun;
		}
	}
	
	/**
	 * Small task that just marks itself when it gets run
	 * 
	 *@see DaemonTest#executeScheduledTask_shouldNotThrowErrorIfCalledFromATimerSchedulerTaskClass()
	 */
	private class PrivateTask extends AbstractTask {
		
		public boolean wasRun = false;
		
		@Override
		public void execute() {
			this.wasRun = true;
		}
	}
	
	/**
	 * A task that starts another Daemon thread that marks *this* thread when it gets run. 
	 */
	private class TaskThatStartsAnotherThread extends PrivateTask {
		
		@Override
		public void execute() {
			Thread another = Daemon.runInNewDaemonThread(new Runnable() {
				
				@Override
				public void run() {
					wasRun = true;
				}
			});
			try {
				another.join();
			}
			catch (InterruptedException ex) {}
		}
	}
	
}
