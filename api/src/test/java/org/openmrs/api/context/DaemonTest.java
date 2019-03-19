/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api.context;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.openmrs.Person;
import org.openmrs.PersonName;
import org.openmrs.User;
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
	
	@Rule
	public ExpectedException expectedException = ExpectedException.none();
	
	/**
	 * @see Daemon#executeScheduledTask(Task)
	 */
	@Test
	public void executeScheduledTask_shouldNotBeCalledFromOtherMethodsOtherThanTimerSchedulerTask() throws Throwable {
		try {
			Daemon.executeScheduledTask(new HelloWorldTask());
			Assert.fail("Should not be here, an exception should have been thrown in the line above");
		}
		catch (APIException e) {
			Assert.assertTrue(e.getMessage().startsWith(Context.getMessageSourceService().getMessage("Scheduler.timer.task.only", new Object[] { this.getClass().getName() }, null)));
		}
	}
	
	/**
	 * This uses a task that just marks itself as run when its "execute" method is called. This
	 * verifies that the Daemon class is getting past the class check and on to the task running
	 * step
	 * 
	 * @see Daemon#executeScheduledTask(Task)
	 */
	@Test
	public void executeScheduledTask_shouldNotThrowErrorIfCalledFromATimerSchedulerTaskClass() throws Throwable {
		Task task = new PrivateTask();
		Assert.assertTrue(new PrivateSchedulerTask(task).runTheTest());
	}
	
	@Test 
	public void createUser_shouldThrowWhenCalledOutsideContextDAO() throws Throwable {
		// verif
		expectedException.expect(APIException.class);
		expectedException.expectMessage(Context.getMessageSourceService().getMessage("Context.DAO.only", new Object[] { this.getClass().getName() }, null));
		
		// replay
		Daemon.createUser(new User(), "password", null);
	}
	
	@Test
	public void createUser_shouldCreateUserWithRolesInContextDAO() throws Throwable {
		// setup
		User u = new User();
		u.setPerson(new Person());
		u.addName(new PersonName("Jane", "X", "Doe"));
		u.setUsername("jdoe");
		u.getPerson().setGender("F");
		
		// replay
		User createdUser = Context.getContextDAO().createUser(u, "P@ssw0rd", Arrays.asList("Provider", "Foobar Role"));
		
		// verif
		Assert.assertNotNull(createdUser.getId());
		Set<String> roleNames = createdUser.getRoles().stream().map(r -> r.getName()).collect(Collectors.toSet());
		Assert.assertTrue(roleNames.contains("Provider"));
		Assert.assertFalse(roleNames.contains("Foobar Role")); // a bogus role name has just no impact on the created user
	}
	
	@Test 
	public void createUser_shouldThrowWhenUserExists() throws Throwable {
		// setup
		User u = Context.getUserService().getUser(501);
		
		// verif
		expectedException.expect(APIException.class);
		expectedException.expectMessage(Context.getMessageSourceService().getMessage("User.creating.already.exists", new Object[] { u.getDisplayString() }, null));
		
		// replay
		Context.getContextDAO().createUser(u, "P@ssw0rd", null);
	}
	
	/**
	 * @see Daemon#runInNewDaemonThread(Runnable)
	 */
	@Test
	public void runInNewDaemonThread_shouldThrowErrorIfCalledFromANonDaemonThread() {
		try {
			Daemon.runInNewDaemonThread(() -> {
				// do nothing
			});
			Assert.assertTrue("Should not hit this line, since the previous needed to throw an exception", false);
		}
		catch (APIAuthenticationException ex) {
			Assert.assertEquals("Only daemon threads can spawn new daemon threads", ex.getMessage());
		}
	}
	
	/**
	 * @see Daemon#runInNewDaemonThread(Runnable)
	 */
	@Test
	public void runInNewDaemonThread_shouldNotThrowErrorIfCalledFromADaemonThread() throws Throwable {
		Task taskThatStartsAnotherThread = new TaskThatStartsAnotherThread();
		Assert.assertTrue(new PrivateSchedulerTask(taskThatStartsAnotherThread).runTheTest());
	}
	
	/**
	 * @see Daemon#executeScheduledTask(Task)
	 */
	@Test
	public void daemonUser_shouldHaveAssociatedPerson() throws Throwable {
		try {
			TestTask task = new TestTask();
			new PrivateSchedulerTask(task).runTask();
			Assert.assertTrue(true);
		}
		catch (NullPointerException e) {
			Assert.fail("Daemon user should have an associated person");
		}
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
		
		public void runTask() throws Throwable {
			Daemon.executeScheduledTask(this.task);
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
			Thread another = Daemon.runInNewDaemonThread(() -> wasRun = true);
			try {
				another.join();
			}
			catch (InterruptedException ex) {}
		}
	}
	
	/**
	 * A task for testing to ensure that a daemon user always has an associated person.
	 */
	private class TestTask extends AbstractTask {
		
		@Override
		public void execute() {
			Context.getAuthenticatedUser().getPersonName().getFullName();
		}
	}
	
	/**
	 * @see Daemon#isDaemonUser(User user)
	 * 
	 */
	@Test
	public void isDaemonUser_shouldReturnTrueForADaemonUser() {
		User user = new User();
		user.setUuid(Daemon.DAEMON_USER_UUID);
		Assert.assertTrue(Daemon.isDaemonUser(user));
	}
	
	/**
	 * @see Daemon#isDaemonUser(User user)
	 * 
	 */
	@Test
	public void isDaemonUser_shouldReturnFalseIFTheUserIsNotADaemon() {
		User user = new User();
		user.setUuid("any other value");
		Assert.assertFalse(Daemon.isDaemonUser(user));
	}
}
