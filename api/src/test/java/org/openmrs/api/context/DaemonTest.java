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
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.openmrs.Person;
import org.openmrs.PersonName;
import org.openmrs.Role;
import org.openmrs.User;
import org.openmrs.api.APIAuthenticationException;
import org.openmrs.api.APIException;
import org.openmrs.scheduler.tasks.AbstractTask;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;
import static org.testcontainers.shaded.org.awaitility.Awaitility.await;

/**
 * Tests the methods on the {@link Daemon} class
 */
public class DaemonTest extends BaseContextSensitiveTest {

	/**
	 * @see Daemon#executeScheduledTaskAsUser(String, Daemon.DaemonTask)
	 */
	@Test
	public void executeScheduledTask_shouldNotBeCalledFromOtherMethodsOtherThanTimerSchedulerTask() throws Throwable {
		try {
			Daemon.executeScheduledTaskAsUser("", () -> {});
			fail("Should not be here, an exception should have been thrown in the line above");
		} catch (APIException e) {
			assertThat(e.getMessage(),
			    startsWith("executeScheduledTaskAsUser can only be called from " + "JobRequestHandlerAdapter"));
		}
	}

	@Test
	public void createUser_shouldThrowWhenCalledOutsideContextDAO() throws Throwable {
		// setup

		// verify

		// replay
		APIException exception = assertThrows(APIException.class, () -> Daemon.createUser(new User(), "password", null));
		assertThat(exception.getMessage(), is(Context.getMessageSourceService().getMessage("Context.DAO.only",
		    new Object[] { this.getClass().getName() }, Locale.ENGLISH)));
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
		APIException exception = assertThrows(APIException.class,
		    () -> Context.getContextDAO().createUser(u, "P@ssw0rd", null));
		assertThat(exception.getMessage(), is(Context.getMessageSourceService().getMessage("User.creating.already.exists",
		    new Object[] { u.getDisplayString() }, Locale.ENGLISH)));
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
		} catch (APIAuthenticationException ex) {
			assertThat(ex.getMessage(), is("Only daemon threads can spawn new daemon threads"));
		}
	}

	@Test
	public void runInNewDaemonThreadCallable_shouldThrowErrorIfCalledFromANonDaemonThread() {
		try {
			Daemon.runInNewDaemonThread(() -> null);
			fail("Should not hit this line, since the previous needed to throw an exception");
		} catch (APIAuthenticationException ex) {
			assertThat(ex.getMessage(), is("Only daemon threads can spawn new daemon threads"));
		}
	}

	@Test
	public void runNewDaemonTask_shouldThrowErrorIfCalledFromANonDaemonThread() {
		try {
			Daemon.runNewDaemonTask(() -> {});
			fail("Should not hit this line, since the previous needed to throw an exception");
		} catch (APIAuthenticationException ex) {
			assertThat(ex.getMessage(), is("Only daemon threads can spawn new daemon threads"));
		}
	}

	/**
	 * Small task that just marks itself when it gets run
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

			// another.join(10000); doesn't actually work as runInNewDaemonThread doesn't use the Thread object rather it
			// only executes the run method. The only way to determine it completed is to wait for wasRun to return true.
			await().atMost(10, TimeUnit.SECONDS).untilTrue(new AtomicBoolean(wasRun));
		}
	}

	/**
	 * A task that starts another Daemon thread that marks *this* thread when it gets run.
	 */
	private static class TaskThatStartsAnotherFuture extends PrivateTask {

		@Override
		public void execute() throws ExecutionException, InterruptedException {
			Future<Boolean> another = Daemon.runInNewDaemonThread(() -> {
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
	 */
	@Test
	public void isDaemonUser_shouldReturnTrueForADaemonUser() {
		User user = new User();
		user.setUuid(Daemon.DAEMON_USER_UUID);
		assertThat(Daemon.isDaemonUser(user), is(true));
	}

	/**
	 * @see Daemon#isDaemonUser(User user)
	 */
	@Test
	public void isDaemonUser_shouldReturnFalseIFTheUserIsNotADaemon() {
		User user = new User();
		user.setUuid("any other value");
		assertThat(Daemon.isDaemonUser(user), is(false));
	}
}
