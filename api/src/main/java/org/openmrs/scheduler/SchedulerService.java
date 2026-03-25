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

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;

import org.openmrs.annotation.Authorized;
import org.openmrs.annotation.Logging;
import org.openmrs.api.OpenmrsService;

/**
 * Defines methods required to schedule a task.
 * <p>
 * Since 2.9.x the default implementation switched to
 * <a href="https://github.com/jobrunr/jobrunr">JobRunr</a>. Please see its documentation
 * <a href="ttps://www.jobrunr.io/en/documentation/">here</a>.
 * <p>
 * The most significant change is that tasks are now run as a user who scheduled the task.
 * <p>
 * <s>{@link TaskDefinition}</s> and <s>{@link Task}</s> are deprecated, but still run on the new
 * implementation via an adapter. New {@link TaskData}/{@link TaskHandler} tasks are not accessible
 * via deprecated methods e.g. <s>{@link #getScheduledTasks()}</s>. Deprecated tasks are accessible
 * when calling new methods e.g. {@link #getTasks(TaskState, Instant)}.
 * <p>
 * Tasks scheduled via SchedulerService are persisted in the database. They can be viewed by a
 * system administrator or presented to users.
 * <p>
 * For more lightweight tasks that are not persisted in the database, please use
 * {@link ScheduledWithLock}.
 */
public interface SchedulerService extends OpenmrsService {

	/**
	 * Checks the status of a scheduled task.
	 *
	 * @param id
	 * @return the <code>String</code> status of the task with the given identifier
	 * @deprecated since 2.9.x use {@link #getTask(String)}
	 */
	@Deprecated
	@Authorized({ "Manage Scheduler" })
	String getStatus(Integer id);

	/**
	 * Cancel a scheduled task.
	 *
	 * @param task the <code>TaskDefinition</code> for the task to cancel
	 * @deprecated since 2.9.x use {@link #deleteTask(String)}
	 */
	@Deprecated
	@Authorized({ "Manage Scheduler" })
	void shutdownTask(TaskDefinition task) throws SchedulerException;

	/**
	 * Start a scheduled task as specified in a TaskDefinition.
	 *
	 * @param task TaskDefinition to start
	 * @return the started <code>Task</code>, or null if there was a problem instantiating or scheduling
	 *         the task
	 * @deprecated since 2.9.x use {@link #schedule(TaskData)}
	 */
	@Deprecated
	@Authorized({ "Manage Scheduler" })
	Task scheduleTask(TaskDefinition task) throws SchedulerException;

	/**
	 * Stop and start a scheduled task.
	 *
	 * @param task the <code>TaskDefinition</code> to reschedule
	 * @deprecated since 2.9.x use {@link #deleteTask(String)} and {@link #schedule(TaskData)}
	 */
	@Deprecated
	@Authorized({ "Manage Scheduler" })
	Task rescheduleTask(TaskDefinition task) throws SchedulerException;

	/**
	 * Loop over all currently started tasks and cycle them. This should be done after the classloader
	 * has been changed (e.g. during module start/stop)
	 *
	 * @deprecated since 2.9.x this method is not needed anymore
	 */
	@Deprecated
	@Authorized({ "Manage Scheduler" })
	void rescheduleAllTasks() throws SchedulerException;

	/**
	 * Get scheduled tasks.
	 *
	 * @return all scheduled tasks
	 * @deprecated since 2.9.x use {@link #getTasks(TaskState, Instant)}
	 */
	@Deprecated
	@Authorized({ "Manage Scheduler" })
	Collection<TaskDefinition> getScheduledTasks();

	/**
	 * Get the list of tasks that are available to be scheduled. Eventually, these should go in the
	 * database.
	 *
	 * @return all available tasks
	 * @deprecated since 2.9.x use {@link #getTasks(TaskState, Instant)}
	 */
	@Deprecated
	@Authorized({ "Manage Scheduler" })
	Collection<TaskDefinition> getRegisteredTasks();

	/**
	 * Get the task with the given identifier.
	 *
	 * @param id the identifier of the task
	 * @deprecated since 2.9.x use {@link #getTask(String)}
	 */
	@Deprecated
	@Authorized({ "Manage Scheduler" })
	TaskDefinition getTask(Integer id);

	/**
	 * @since 2.4.0 Get the task with the given uuid
	 * @param uuid the unique identifier of the task
	 * @deprecated since 2.9.x use {@link #getTask(String)}
	 */
	@Deprecated
	@Authorized({ "Manage Scheduler" })
	TaskDefinition getTaskByUuid(String uuid);

	/**
	 * Get the task with the given name.
	 *
	 * @param name name of the task
	 * @deprecated since 2.9.x use {@link #getTask(String)}
	 */
	@Deprecated
	@Authorized({ "Manage Scheduler" })
	TaskDefinition getTaskByName(String name);

	/**
	 * Delete the task with the given identifier.
	 *
	 * @param id the identifier of the task
	 * @deprecated since 2.9.x use {@link #deleteTask(String)}
	 */
	@Deprecated
	@Authorized({ "Manage Scheduler" })
	void deleteTask(Integer id);

	/**
	 * Create the given task
	 * <p>
	 * <strong>Should</strong> save task to the database
	 *
	 * @deprecated since 2.9.x use {@link #schedule(TaskData)}
	 */
	@Deprecated
	@Authorized({ "Manage Scheduler" })
	@Logging(ignore = true)
	void saveTaskDefinition(TaskDefinition task);

	/**
	 * Schedules a task for execution if not already running
	 *
	 * @param taskDef
	 * @since 1.10
	 * @deprecated since 2.9.x this method is not needed anymore
	 */
	@Deprecated
	void scheduleIfNotRunning(TaskDefinition taskDef);

	/**
	 * Get TaskDetails of a task with the given uuid.
	 * <p>
	 * User can only get its own tasks unless has Manage Scheduler privilege.
	 *
	 * @param uuid
	 * @return TaskDetails
	 * @since 2.9.x
	 */
	@Authorized
	Optional<TaskDetails> getTask(String uuid);

	/**
	 * Get RecurringTaskDetails of a task with the given uuid.
	 * <p>
	 * User can only get its own tasks unless has Manage Scheduler privilege.
	 *
	 * @param uuid
	 * @return RecurringTaskDetails
	 * @since 2.9.x
	 */
	@Authorized
	Optional<RecurringTaskDetails> getRecurringTask(String uuid);

	/**
	 * Delete task with the given uuid.
	 * <p>
	 * Please note that if a task runs, it won't be stopped, unless it implements the stop logic. The
	 * deletion will prevent the task from running in the future.
	 * <p>
	 * User can only delete its own tasks unless has Manage Scheduler privilege.
	 *
	 * @param uuid
	 * @since 2.9.x
	 */
	@Authorized
	void deleteTask(String uuid);

	/**
	 * Delete recurring task with the given uuid.
	 * <p>
	 * Please note that if a task runs, it won't be stopped. The deletion will prevent the task from
	 * running in the future.
	 * <p>
	 * User can only delete its own tasks unless has Manage Scheduler privilege.
	 *
	 * @param uuid
	 * @since 2.9.x
	 */
	@Authorized
	void deleteRecurringTask(String uuid);

	/**
	 * Returns tasks ordered from the most recently updated.
	 * <p>
	 * It fetches tasks in batches of 100 by default.
	 * <p>
	 * User can only get its own tasks unless has Manage Scheduler privilege.
	 *
	 * @param state the state of the tasks
	 * @param updatedBefore the moment in time (for stable iteration)
	 * @return Stream<TaskDetails>
	 * @since 2.9.x
	 */
	@Authorized
	Stream<TaskDetails> getTasks(TaskState state, Instant updatedBefore);

	/**
	 * Returns recurring tasks ordered from the most recently updated.
	 * <p>
	 * User can only get its own tasks unless has Manage Scheduler privilege.
	 *
	 * @return Stream<RecurringTaskDetails>
	 * @since 2.9.x
	 */
	@Authorized
	Stream<RecurringTaskDetails> getRecurringTasks();

	/**
	 * It schedules a one-off task, that will be put in queue and executed in background.
	 * <p>
	 * In order to schedule a task, you need to implement {@link TaskData} and a corresponding
	 * {@link TaskHandler}.
	 * <p>
	 * {@link TaskData} is persisted and then used by a corresponding {@link TaskHandler} to process the
	 * data.
	 * <p>
	 * Please remember that for performance and consistency it is always better pass simple values in
	 * {@link TaskData} (e.g. DB ids) that can be quickly serialized/deserialized and if needed fetch
	 * objects from DB inside your {@link TaskHandler}.
	 *
	 * @param taskData
	 * @return TaskDetails
	 * @since 2.9.x
	 */
	@Authorized
	TaskDetails schedule(TaskData taskData);

	/**
	 * It schedules a one-off task with a user-friendly name, that will be put in queue and executed in
	 * background.
	 * <p>
	 * See {@link #schedule(TaskData)} for more details.
	 *
	 * @param name
	 * @param taskData
	 * @return TaskDetails
	 * @since 2.9.x
	 */
	@Authorized
	TaskDetails schedule(String name, TaskData taskData);

	/**
	 * It schedules a task to run once at the given time.
	 * <p>
	 * See {@link #schedule(TaskData)} for more details.
	 *
	 * @param taskData
	 * @param runAt
	 * @return TaskDetails
	 * @since 2.9.x
	 */
	@Authorized
	TaskDetails schedule(TaskData taskData, Instant runAt);

	/**
	 * It schedules a task with a user-friendly name to run once at the given time.
	 * <p>
	 * See {@link #schedule(TaskData)} for more details.
	 *
	 * @param taskData
	 * @param runAt
	 * @return TaskDetails
	 * @since 2.9.x
	 */
	@Authorized
	TaskDetails schedule(String name, TaskData taskData, Instant runAt);

	/**
	 * It schedules a task to run once at the given time respecting time zone.
	 * <p>
	 * See {@link #schedule(TaskData)} for more details.
	 *
	 * @param taskData
	 * @param runAt
	 * @return TaskDetails
	 * @since 2.9.x
	 */
	@Authorized
	TaskDetails schedule(TaskData taskData, ZonedDateTime runAt);

	/**
	 * It schedules a task with a user-friendly name to run once at the given time respecting time zone.
	 * <p>
	 * See {@link #schedule(TaskData)} for more details.
	 *
	 * @param taskData
	 * @param runAt
	 * @return TaskDetails
	 * @since 2.9.x
	 */
	@Authorized
	TaskDetails schedule(String name, TaskData taskData, ZonedDateTime runAt);

	/**
	 * It schedules a task to run recurrently at the given cron schedule.
	 * <p>
	 * See {@link #schedule(TaskData)} for more details.
	 *
	 * @param taskData
	 * @param cron
	 * @return TaskDetails
	 * @since 2.9.x
	 */
	@Authorized
	RecurringTaskDetails scheduleRecurrently(TaskData taskData, String cron);

	/**
	 * It schedules a task with a user-friendly name to run recurrently at the given cron schedule.
	 * <p>
	 * See {@link #schedule(TaskData)} for more details.
	 *
	 * @param taskData
	 * @param cron
	 * @return TaskDetails
	 * @since 2.9.x
	 */
	@Authorized
	RecurringTaskDetails scheduleRecurrently(String name, TaskData taskData, String cron);

	/**
	 * It schedules a task to run recurrently at the given cron schedule respecting the given time zone.
	 * <p>
	 * See {@link #schedule(TaskData)} for more details.
	 *
	 * @param taskData
	 * @param cron
	 * @param zoneId
	 * @return TaskDetails
	 * @since 2.9.x
	 */
	@Authorized
	RecurringTaskDetails scheduleRecurrently(TaskData taskData, String cron, ZoneId zoneId);

	/**
	 * It schedules a task with a user-friendly name to run recurrently at the given cron schedule
	 * respecting the given time zone.
	 * <p>
	 * See {@link #schedule(TaskData)} for more details.
	 *
	 * @param taskData
	 * @param cron
	 * @param zoneId
	 * @return TaskDetails
	 * @since 2.9.x
	 */
	@Authorized
	RecurringTaskDetails scheduleRecurrently(String name, TaskData taskData, String cron, ZoneId zoneId);

	/**
	 * It schedules a task to run recurrently at the given interval.
	 * <p>
	 * See {@link #schedule(TaskData)} for more details.
	 *
	 * @param taskData
	 * @param interval
	 * @return TaskDetails
	 * @since 2.9.x
	 */
	@Authorized
	RecurringTaskDetails scheduleRecurrently(TaskData taskData, Duration interval);

	/**
	 * It schedules a task with a user-friendly name to run recurrently at the given interval.
	 * <p>
	 * See {@link #schedule(TaskData)} for more details.
	 *
	 * @param taskData
	 * @param interval
	 * @return TaskDetails
	 * @since 2.9.x
	 */
	@Authorized
	RecurringTaskDetails scheduleRecurrently(String name, TaskData taskData, Duration interval);

}
