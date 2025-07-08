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

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.openmrs.Person;
import org.openmrs.PersonName;
import org.openmrs.Role;
import org.openmrs.User;
import org.openmrs.api.APIAuthenticationException;
import org.openmrs.api.APIException;
import org.openmrs.scheduler.Task;
import org.openmrs.scheduler.tasks.AbstractTask;
import org.openmrs.scheduler.tasks.HelloWorldTask;
import org.openmrs.scheduler.timer.TimerSchedulerTask;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;

/**
 * Tests the methods on the {@link Daemon} class
 */
public class DaemonTest extends BaseContextSensitiveTest {
	
	
	/**
	 * @see Daemon#executeScheduledTask(Task)
	 */
	@Test
	public void executeScheduledTask_shouldNotBeCalledFromOtherMethodsOtherThanTimerSchedulerTask() throws Throwable {
		try {
			Daemon.executeScheduledTask(new HelloWorldTask());
			fail("Should not be here, an exception should have been thrown in the line above");
		}
		catch (APIException e) {
			assertThat(e.getMessage(), startsWith(
				Context.getMessageSourceService().getMessage("Scheduler.timer.task.only", new Object[] { this.getClass().getName() }, Locale.ENGLISH)));
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
		assertThat(new PrivateSchedulerTask(task).runTheTest(), is(true));
	}
	
	@Test 
	public void createUser_shouldThrowWhenCalledOutsideContextDAO() throws Throwable {
		// setup
		
		// verify
		
		// replay
		APIException exception = assertThrows(APIException.class, () -> Daemon.createUser(new User(), "password", null));
		assertThat(exception.getMessage(), is(
			Context.getMessageSourceService().getMessage("Context.DAO.only", new Object[] { this.getClass().getName() }, Locale.ENGLISH)));
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
		
		// verify
		assertThat(createdUser.getId(), notNullValue());
		Set<String> roleNames = createdUser.getRoles().stream().map(Role::getName).collect(Collectors.toSet());
		assertThat(roleNames, hasItem("Provider"));
		assertThat(roleNames, not(hasItem("Foobar Role"))); // a bogus role name has just no impact on the created user
	}
	
	@Test 
	public void createUser_shouldThrowWhenUserExists() throws Throwable {
		// setup
		User u = Context.getUserService().getUser(501);
		
		// verify
		
		// replay
		APIException exception = assertThrows(APIException.class, () -> Context.getContextDAO().createUser(u, "P@ssw0rd", null));
		assertThat(exception.getMessage(), is(
			Context.getMessageSourceService().getMessage("User.creating.already.exists", new Object[] { u.getDisplayString() }, Locale.ENGLISH)));
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
			fail("Should not hit this line, since the previous needed to throw an exception");
		}
		catch (APIAuthenticationException ex) {
			assertThat(ex.getMessage(), is("Only daemon threads can spawn new daemon threads"));
		}
	}

	@Test
	public void runInNewDaemonThreadCallable_shouldThrowErrorIfCalledFromANonDaemonThread() {
		try {
			Daemon.runInNewDaemonThread(() -> null);
			fail("Should not hit this line, since the previous needed to throw an exception");
		}
		catch (APIAuthenticationException ex) {
			assertThat(ex.getMessage(), is("Only daemon threads can spawn new daemon threads"));
		}
	}

	@Test
	public void runNewDaemonTask_shouldThrowErrorIfCalledFromANonDaemonThread() {
		try {
			Daemon.runNewDaemonTask(() -> {});
			fail("Should not hit this line, since the previous needed to throw an exception");
		}
		catch (APIAuthenticationException ex) {
			assertThat(ex.getMessage(), is("Only daemon threads can spawn new daemon threads"));
		}
	}
	
	/**
	 * @see Daemon#runInNewDaemonThread(Runnable)
	 */
	@Test
	public void runInNewDaemonThread_shouldNotThrowErrorIfCalledFromADaemonThread() throws Throwable {
		Task taskThatStartsAnotherThread = new TaskThatStartsAnotherThread();
		assertThat(new PrivateSchedulerTask(taskThatStartsAnotherThread).runTheTest(), is(true));
	}

	/**
	 * @see Daemon#runInNewDaemonThread(Runnable)
	 */
	@Test
	public void runInNewDaemonThreadCallable_shouldNotThrowErrorIfCalledFromADaemonThread() throws Throwable {
		Task taskThatStartsAnotherFuture = new TaskThatStartsAnotherFuture();
		assertThat(new PrivateSchedulerTask(taskThatStartsAnotherFuture).runTheTest(), is(true));
	}
	
	/**
	 * @see Daemon#executeScheduledTask(Task)
	 */
	@Test
	public void daemonUser_shouldHaveAssociatedPerson() throws Throwable {
		try {
			TestTask task = new TestTask();
			new PrivateSchedulerTask(task).runTask();
		}
		catch (NullPointerException e) {
			fail("Daemon user should have an associated person");
		}
	}
	
	/**
	 * A TimerSchedulerTask that can call the daemon thread
	 * 
	 * @see DaemonTest#executeScheduledTask_shouldNotThrowErrorIfCalledFromATimerSchedulerTaskClass()
	 */
	private static class PrivateSchedulerTask extends TimerSchedulerTask {
		
		private final Task task;
		
		public PrivateSchedulerTask(Task task) {
			super(task);
			this.task = task;
		}
		
		/**
		 * Returns true/false whether the task was successfully run by the Daemon user
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
	private static class PrivateTask extends AbstractTask {
		
		public volatile boolean wasRun = false;
		
		@Override
		public void execute() throws InterruptedException, ExecutionException {
			this.wasRun = true;
		}
	}
	
	/**
	 * A task that starts another Daemon thread that marks *this* thread when it gets run. 
	 */
	private static class TaskThatStartsAnotherThread extends PrivateTask {
		
		@Override
		public void execute() throws InterruptedException {
			Thread another = Daemon.runInNewDaemonThread(() -> {
				this.wasRun = true;
			});
			
			another.join();
		}
	}

	/**
	 * A task that starts another Daemon thread that marks *this* thread when it gets run. 
	 */
	private static class TaskThatStartsAnotherFuture extends PrivateTask {

		@Override
		public void execute() throws ExecutionException, InterruptedException {
			Future<Boolean> another = Daemon.runInNewDaemonThread(() ->{
				this.wasRun = true; 
				return true;
			});
			
			assertThat(another.get(), is(true));
		}
	}
	
	/**
	 * A task for testing to ensure that a daemon user always has an associated person.
	 */
	private static class TestTask extends AbstractTask {
		
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
		assertThat(Daemon.isDaemonUser(user), is(true));
	}
	
	/**
	 * @see Daemon#isDaemonUser(User user)
	 * 
	 */
	@Test
	public void isDaemonUser_shouldReturnFalseIFTheUserIsNotADaemon() {
		User user = new User();
		user.setUuid("any other value");
		assertThat(Daemon.isDaemonUser(user), is(false));
	}
}
