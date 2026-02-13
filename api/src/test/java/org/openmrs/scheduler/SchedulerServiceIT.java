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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Collection;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.GlobalProperty;
import org.openmrs.Person;
import org.openmrs.PersonName;
import org.openmrs.User;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.UserService;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.ContextDAO;
import org.openmrs.scheduler.tasks.AbstractTask;
import org.openmrs.test.jupiter.BaseContextSensitiveNonTransactionalTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.springframework.stereotype.Component;

/**
 * Tests for {@link SchedulerService}.
 */
public class SchedulerServiceIT extends BaseContextSensitiveNonTransactionalTest {

	public static final String EXECUTED_COUNT = "EXECUTED_COUNT";
	
	@Autowired
	SchedulerService schedulerService;
	
	@Autowired
	AdministrationService adminService;
	
	@Autowired
	UserService userService;
	
	TaskDetails taskDetails;
	
	@BeforeEach
	void clearExecutedCount() {
		adminService.saveGlobalProperty(new GlobalProperty(EXECUTED_COUNT, "0"));
	}
	
	@AfterEach
	void after() {
		schedulerService.getRecurringTasks().forEach(r -> schedulerService.deleteRecurringTask(r.getUuid()));
		schedulerService.getTasks(TaskState.SCHEDULED, Instant.now()).forEach(t -> schedulerService.deleteTask(t.getUuid()));
		schedulerService.getTasks(TaskState.ENQUEUED, Instant.now()).forEach(t -> schedulerService.deleteTask(t.getUuid()));
	}
	
	@Test
	void scheduleTask_shouldScheduleTaskDefinition() throws SchedulerException, InterruptedException {
		TaskDefinition task = new TaskDefinition();
		task.setName("scheduleTask_shouldScheduleTaskDefinition");
		task.setTaskClass(LegacyTestTask.class.getName());
		task.setStartOnStartup(false);
		task.setRepeatInterval(5L);
		task.setStartTime(null);

		schedulerService.saveTaskDefinition(task);

		assertNotNull(task.getId());
		TaskDefinition savedTask = schedulerService.getTask(task.getId());
		assertNotNull(savedTask);
		assertEquals("scheduleTask_shouldScheduleTaskDefinition", savedTask.getName());

		schedulerService.scheduleTask(task);
		waitForExecutedCount(2);
	}

	@Test
	void scheduleTask_shouldScheduleTaskDefinitionAsCreator() throws SchedulerException, InterruptedException {
		TaskDefinition task = new TaskDefinition();
		task.setName("scheduleTask_shouldScheduleTaskDefinitionAsCreator");
		task.setTaskClass(LegacyTestTask.class.getName());
		task.setStartOnStartup(false);
		task.setStartTime(null);
		Person person = new Person();
		person.addName(new PersonName("test", "test", "test"));
		person.setGender("M");
		User user = new User();
		user.setPerson(person);
		user.setSystemId("test");
		user.addRole(userService.getRole("System developer"));
		user = userService.createUser(user, "Test12345");
		task.setCreator(user);

		schedulerService.saveTaskDefinition(task);

		assertNotNull(task.getId());
		TaskDefinition savedTask = schedulerService.getTask(task.getId());
		assertNotNull(savedTask);
		assertEquals("scheduleTask_shouldScheduleTaskDefinitionAsCreator", savedTask.getName());

		schedulerService.scheduleTask(task);
		waitForExecutedCount(1);
	}

	@Test
	void scheduleTask_shouldNotFailIfCalledTwiceForTheSameTaskDefinition() throws SchedulerException {
		TaskDefinition task = new TaskDefinition();
		task.setName("scheduleTask_shouldNotFailIfCalledTwice");
		task.setTaskClass(LegacyTestTask.class.getName());
		task.setStartOnStartup(false);
		task.setRepeatInterval(60L);
		task.setStartTime(null);

		schedulerService.saveTaskDefinition(task);
		assertNotNull(task.getId());

		schedulerService.scheduleTask(task);
		schedulerService.scheduleTask(task);
		
		assertThat(schedulerService.getRecurringTasks().count(), equalTo(1L));
	}
	
	@Test
	void scheduleTask_shouldHandleZeroRepeatInterval() throws SchedulerException, InterruptedException {
		Calendar startTime = Calendar.getInstance();
		
		TaskDefinition task = new TaskDefinition();
		task.setName("scheduleTask_shouldHandleZeroRepeatInterval");
		task.setTaskClass(LegacyTestTask.class.getName());
		task.setStartOnStartup(false);
		task.setRepeatInterval(0L);
		task.setStartTime(startTime.getTime());

		schedulerService.saveTaskDefinition(task);
		assertNotNull(task.getId());

		schedulerService.scheduleTask(task);
		
		waitForExecutedCount(1);
	}

	@Test
	void deleteTask_shouldDeleteTask() {
		TaskDefinition task = new TaskDefinition();
		task.setName("Task to Delete");
		task.setTaskClass(LegacyTestTask.class.getName());
		task.setStartOnStartup(false);
		task.setRepeatInterval(100L);
		schedulerService.saveTaskDefinition(task);
		Integer id = task.getId();

		schedulerService.deleteTask(id);

		assertThrows(ObjectRetrievalFailureException.class, () -> schedulerService.getTask(id));
	}

	@Test
	void getTaskByName_shouldReturnTask() {
		TaskDefinition task = new TaskDefinition();
		task.setName("Unique Task Name");
		task.setTaskClass(LegacyTestTask.class.getName());
		task.setStartOnStartup(false);
		task.setRepeatInterval(100L);
		schedulerService.saveTaskDefinition(task);

		TaskDefinition fetched = schedulerService.getTaskByName("Unique Task Name");
		assertNotNull(fetched);
		assertEquals(task.getId(), fetched.getId());
	}

	@Test
	void getRegisteredTasks_shouldReturnAllTasks() {
		TaskDefinition task = new TaskDefinition();
		task.setName("Registered Task");
		task.setTaskClass(LegacyTestTask.class.getName());
		task.setStartOnStartup(false);
		task.setRepeatInterval(100L);
		schedulerService.saveTaskDefinition(task);

		Collection<TaskDefinition> tasks = schedulerService.getRegisteredTasks();
		assertNotNull(tasks);
		assertTrue(tasks.stream().anyMatch(t -> t.getName().equals("Registered Task")));
	}

	@Test
	void schedule_shouldScheduleTaskData() throws InterruptedException {
		taskDetails = schedulerService.schedule(new TestTaskData());
		assertNotNull(taskDetails);
		waitForExecutedCount(1);
	}

	@Test
	void schedule_shouldScheduleNamedTaskData() throws InterruptedException {
		taskDetails = schedulerService.schedule("Named Task Data", new TestTaskData());
		assertNotNull(taskDetails);
		waitForExecutedCount(1);
	}

	@Test
	void schedule_shouldScheduleTaskDataWithInstant() throws InterruptedException {
		Instant runAt = Instant.now().plus(5, ChronoUnit.SECONDS);
		taskDetails = schedulerService.schedule("schedule_shouldScheduleTaskDataWithInstant", new TestTaskData(), runAt);
		assertNotNull(taskDetails);
		waitForExecutedCount(1);
	}

	@Test
	void scheduleRecurrently_shouldScheduleTaskDataWithCron() throws InterruptedException {
		String cron = "*/5 * * * * *";
		taskDetails = schedulerService.scheduleRecurrently("scheduleRecurrently_shouldScheduleTaskDataWithCron", new TestTaskData(), cron);
		assertNotNull(taskDetails);
		waitForExecutedCount(2);
	}

	@Test
	void scheduleRecurrently_shouldScheduleTaskDataWithDuration() throws InterruptedException {
		Duration interval = Duration.ofSeconds(5);
		taskDetails = schedulerService.scheduleRecurrently("scheduleRecurrently_shouldScheduleTaskDataWithDuration", new TestTaskData(), interval);
		assertNotNull(taskDetails);
		waitForExecutedCount(2);
	}

	@Test
	void rescheduleTask_shouldRescheduleTask() throws SchedulerException, InterruptedException {
		TaskDefinition task = new TaskDefinition();
		task.setName("rescheduleTask_shouldRescheduleTask");
		task.setTaskClass(LegacyTestTask.class.getName());
		task.setStartOnStartup(false);
		task.setRepeatInterval(5L);
		task.setStartTime(null);

		schedulerService.saveTaskDefinition(task);
		schedulerService.scheduleTask(task);
		schedulerService.rescheduleTask(task);
		clearExecutedCount();

		waitForExecutedCount(1);

		TaskDefinition rescheduledTask = schedulerService.getTask(task.getId());
		assertTrue(rescheduledTask.getStarted());
	}

	@Test
	void onStartup_shouldStartTasks() throws InterruptedException {
		TaskDefinition task = new TaskDefinition();
		task.setName("onStartup_shouldStartTasks");
		task.setTaskClass(LegacyTestTask.class.getName());
		task.setStartOnStartup(true);
		task.setStartTime(null);
		schedulerService.saveTaskDefinition(task);

		schedulerService.onStartup();

		waitForExecutedCount(1);
		schedulerService.deleteTask(task.getId());
	}

	@Test
	void onStartup_shouldNotScheduleTaskTwice() throws InterruptedException {
		TaskDefinition task = new TaskDefinition();
		task.setName("onStartup_shouldNotScheduleTaskTwice");
		task.setTaskClass(LegacyTestTask.class.getName());
		task.setStartOnStartup(true);
		task.setRepeatInterval(100L);
		task.setStartTime(null);
		schedulerService.saveTaskDefinition(task);
		
		schedulerService.onStartup();
		
		waitForExecutedCount(1);
		
		schedulerService.onStartup(); // should not fail

		assertThat(schedulerService.getRecurringTasks().count(), equalTo(1L));
		schedulerService.deleteTask(task.getId());
	}
	
	private void waitForExecutedCount(int moreThan) throws InterruptedException {
		long start = System.currentTimeMillis();
		while (System.currentTimeMillis() - start < 30000) {
			Context.clearSession(); // Needed so that GP is always fetched from DB and not cache
			Context.clearEntireCache();
			int executedCount = Integer.parseInt(adminService.getGlobalProperty(EXECUTED_COUNT));
			if (executedCount >= moreThan) {
				return;
			}
			Thread.sleep(200);
		}
		throw new RuntimeException("Task did not execute within 30s");
	}

	public static class LegacyTestTask extends AbstractTask {
		@Override
		public void execute() {
			// Assert authenticated as task creator
			assertThat(Context.getAuthenticatedUser().getSystemId(), equalTo(getTaskDefinition().getCreatorSystemId()));
			
			// Assert user authenticated with person assigned
			assertThat(Context.getAuthenticatedUser().getPerson(), notNullValue());
			
			// Using SQL to do atomic counter updates
			try (PreparedStatement statement = Context.getDatabaseConnection().prepareStatement(
				"UPDATE global_property SET property_value = property_value + 1 WHERE property = ?")) {
				statement.setString(1, EXECUTED_COUNT);
				statement.executeUpdate();
			} catch (SQLException e) {
				throw new RuntimeException(e);
			}
		}
	}

	public static class TestTaskData implements TaskData, Serializable {
		private static final long serialVersionUID = 1L;
	}

	@Component
	public static class TestTaskHandler implements TaskHandler<TestTaskData> {
		
		private ContextDAO contextDAO;
		
		public TestTaskHandler(ContextDAO contextDAO) {
			this.contextDAO = contextDAO;
		}
		
		@Override
		public void execute(TestTaskData taskData, TaskContext taskContext) throws Exception {
			// Assert authenticated as task creator
			assertThat(Context.getAuthenticatedUser().getSystemId(), is(taskContext.getUserSystemId()));

			// Using SQL to do atomic counter updates
			try (PreparedStatement statement = contextDAO.getDatabaseConnection().prepareStatement(
				"UPDATE global_property SET property_value = property_value + 1 WHERE property = ?")) {
				statement.setString(1, EXECUTED_COUNT);
				statement.executeUpdate();
			} catch (SQLException e) {
				throw new RuntimeException(e);
			}
		}
	}
}
