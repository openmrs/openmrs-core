/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.scheduler.jobrunr;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.jobrunr.jobs.Job;
import org.jobrunr.jobs.JobDetails;
import org.jobrunr.jobs.RecurringJob;
import org.jobrunr.jobs.states.StateName;
import org.jobrunr.scheduling.JobRequestScheduler;
import org.jobrunr.scheduling.JobScheduler;
import org.jobrunr.scheduling.interval.Interval;
import org.jobrunr.server.BackgroundJobServer;
import org.jobrunr.storage.ConcurrentJobModificationException;
import org.jobrunr.storage.JobNotFoundException;
import org.jobrunr.storage.RecurringJobsResult;
import org.jobrunr.storage.StorageProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.openmrs.api.context.Context;
import org.openmrs.scheduler.TaskDefinition;
import org.openmrs.scheduler.db.SchedulerDAO;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class JobRunrSchedulerServiceTest {

	private final BackgroundJobServer server = mock(BackgroundJobServer.class);

	private final JobRunrServerLifecycle lifecycle = new JobRunrServerLifecycle(server);

	private final StorageProvider storageProvider = mock(StorageProvider.class);

	private final SchedulerDAO schedulerDAO = mock(SchedulerDAO.class);

	private final JobRequestScheduler jobRequestScheduler = mock(JobRequestScheduler.class);

	private final JobRunrSchedulerService schedulerService = new JobRunrSchedulerService(storageProvider,
	        jobRequestScheduler, mock(JobScheduler.class), schedulerDAO);

	@BeforeEach
	public void setJobRunrServerLifecycle() {
		schedulerService.setJobRunrServerLifecycle(lifecycle);
	}

	@AfterEach
	public void clearDeferral() {
		new JobRunrServerLifecycle(mock(BackgroundJobServer.class)).openmrsStarted();
	}

	@Test
	public void onStartup_shouldStartAJobServerWhoseStartWasDeferred() {
		JobRunrServerLifecycle.deferStartUntilOpenmrsStarted();
		lifecycle.start();

		schedulerService.onStartup();

		verify(server).start();
	}

	@Test
	public void onShutdown_shouldStopTheJobServer() {
		lifecycle.start();

		schedulerService.onShutdown();

		verify(server).stop();
		assertFalse(lifecycle.isRunning());
	}

	@Test
	public void onStartup_shouldNotReplaceAStartupRunAnotherNodeHasQueued() {
		TaskDefinition task = startupTask();
		Job queued = job(StateName.ENQUEUED, 2);
		when(storageProvider.getJobById(UUID.fromString(task.getUuid()))).thenReturn(queued);

		schedulerService.onStartup();

		verify(storageProvider, never()).save(any(Job.class));
		verify(storageProvider, never()).deletePermanently(any(UUID.class));
	}

	@Test
	public void onStartup_shouldReplaceAFinishedStartupRunWithAVersionedUpdate() {
		TaskDefinition task = startupTask();
		UUID id = UUID.fromString(task.getUuid());
		Job finished = job(StateName.SUCCEEDED, 3);
		when(storageProvider.getJobById(id)).thenReturn(finished);

		schedulerService.onStartup();

		ArgumentCaptor<Job> saved = ArgumentCaptor.forClass(Job.class);
		verify(storageProvider).save(saved.capture());
		assertEquals(id, saved.getValue().getId());
		assertEquals(3, saved.getValue().getVersion());
		assertEquals(StateName.ENQUEUED, saved.getValue().getState());
		verify(storageProvider, never()).deletePermanently(any(UUID.class));
	}

	@Test
	public void onStartup_shouldLeaveTheStartupRunOfANodeThatWonTheRace() {
		TaskDefinition task = startupTask();
		UUID id = UUID.fromString(task.getUuid());
		Job finished = job(StateName.SUCCEEDED, 3);
		Job queuedByOtherNode = job(StateName.ENQUEUED, 4);
		when(queuedByOtherNode.getId()).thenReturn(id);
		ConcurrentJobModificationException conflict = new ConcurrentJobModificationException(queuedByOtherNode);
		when(storageProvider.getJobById(id)).thenReturn(finished, queuedByOtherNode);
		when(storageProvider.save(any(Job.class))).thenThrow(conflict);

		schedulerService.onStartup();

		verify(storageProvider, times(1)).save(any(Job.class));
		verify(storageProvider, times(2)).getJobById(id);
		verify(storageProvider, never()).deletePermanently(any(UUID.class));
	}

	@Test
	public void onStartup_shouldInsertTheStartupRunWhenNoneExists() {
		TaskDefinition task = startupTask();
		UUID id = UUID.fromString(task.getUuid());
		when(storageProvider.getJobById(id)).thenThrow(new JobNotFoundException(id));

		schedulerService.onStartup();

		ArgumentCaptor<Job> saved = ArgumentCaptor.forClass(Job.class);
		verify(storageProvider).save(saved.capture());
		assertEquals(0, saved.getValue().getVersion());
		assertEquals(StateName.ENQUEUED, saved.getValue().getState());
	}

	@Test
	public void scheduleRecurrently_shouldNotRecreateTheRecurringJobOfAStoppedTask() {
		TaskDefinition task = startupTask();
		task.setStartOnStartup(false);
		task.setStarted(false);
		task.setRepeatInterval(60L);
		when(schedulerDAO.getTaskByUuid(task.getUuid())).thenReturn(task);

		try (MockedStatic<Context> context = mockStatic(Context.class)) {
			schedulerService.scheduleRecurrently(task.getUuid());
		}

		verify(storageProvider, never()).saveRecurringJob(any(RecurringJob.class));
	}

	@Test
	public void scheduleRecurrently_shouldRegisterTheConfiguredIntervalOfAStartedTask() {
		TaskDefinition task = startupTask();
		task.setStartOnStartup(false);
		task.setStarted(true);
		task.setRepeatInterval(60L);
		when(schedulerDAO.getTaskByUuid(task.getUuid())).thenReturn(task);

		try (MockedStatic<Context> context = mockStatic(Context.class)) {
			schedulerService.scheduleRecurrently(task.getUuid());
		}

		ArgumentCaptor<RecurringJob> saved = ArgumentCaptor.forClass(RecurringJob.class);
		verify(storageProvider).saveRecurringJob(saved.capture());
		assertEquals("PT1M", saved.getValue().getScheduleExpression());
	}

	@Test
	public void onStartup_shouldReRegisterARecurringJobLeftWithAPlaceholderPeriod() {
		// a started task whose recurring job was written by an earlier 2.9.0 build with a ~1 day placeholder
		TaskDefinition task = recurringTask(60L);
		when(schedulerDAO.getTasks()).thenReturn(Collections.singletonList(task));
		RecurringJob placeholder = new RecurringJob(task.getUuid(),
		        new JobDetails(new JobRequestAdapter(task, "daemon")), new Interval(Duration.ofHours(25)),
		        ZoneId.systemDefault());
		when(storageProvider.getRecurringJobs())
		        .thenReturn(new RecurringJobsResult(Collections.singletonList(placeholder)));

		try (MockedStatic<Context> context = mockStatic(Context.class)) {
			context.when(() -> Context.hasPrivilege(anyString())).thenReturn(true);

			schedulerService.onStartup();
		}

		verify(jobRequestScheduler).delete(task.getUuid());
		ArgumentCaptor<RecurringJob> saved = ArgumentCaptor.forClass(RecurringJob.class);
		verify(storageProvider).saveRecurringJob(saved.capture());
		assertEquals(task.getUuid(), saved.getValue().getId());
		assertEquals("PT1M", saved.getValue().getScheduleExpression());
	}

	@Test
	public void onStartup_shouldLeaveARecurringJobWithTheConfiguredPeriodAlone() {
		TaskDefinition task = recurringTask(60L);
		when(schedulerDAO.getTasks()).thenReturn(Collections.singletonList(task));
		RecurringJob current = new RecurringJob(task.getUuid(),
		        new JobDetails(new JobRequestAdapter(task, "daemon")), new Interval(Duration.ofSeconds(60)),
		        ZoneId.systemDefault());
		when(storageProvider.getRecurringJobs()).thenReturn(new RecurringJobsResult(Collections.singletonList(current)));

		try (MockedStatic<Context> context = mockStatic(Context.class)) {
			// lets a heal that fires by mistake reach delete() instead of failing on the
			// missing user context
			context.when(() -> Context.hasPrivilege(anyString())).thenReturn(true);
			schedulerService.onStartup();
		}
		verify(jobRequestScheduler, never()).delete(anyString());
		verify(storageProvider, never()).saveRecurringJob(any(RecurringJob.class));
	}

	@Test
	public void onStartup_shouldRegisterTasksListedAfterOneThatFailsToRegister() {
		// JobRunr rejects intervals under 5s; the failure must not stop the tasks after it
		TaskDefinition invalid = recurringTask(1L);
		TaskDefinition valid = recurringTask(60L);
		when(schedulerDAO.getTasks()).thenReturn(Arrays.asList(invalid, valid));
		when(storageProvider.getRecurringJobs()).thenReturn(new RecurringJobsResult());

		schedulerService.onStartup();

		ArgumentCaptor<RecurringJob> saved = ArgumentCaptor.forClass(RecurringJob.class);
		verify(storageProvider).saveRecurringJob(saved.capture());
		assertEquals(valid.getUuid(), saved.getValue().getId());
		assertEquals("PT1M", saved.getValue().getScheduleExpression());
	}

	@Test
	public void scheduleTask_shouldFirstRunARecurringTaskAtAFutureStartTime() throws Exception {
		Instant startTime = Instant.now().plus(30, ChronoUnit.MINUTES).truncatedTo(ChronoUnit.SECONDS);
		TaskDefinition task = recurringTask(3600L);
		task.setStartTime(Date.from(startTime));

		schedulerService.scheduleTask(task);

		ArgumentCaptor<RecurringJob> saved = ArgumentCaptor.forClass(RecurringJob.class);
		verify(storageProvider).saveRecurringJob(saved.capture());
		// JobRunr's first interval run is createdAt + interval, so the anchor is one interval before start_time
		assertEquals(startTime.minusSeconds(3600), saved.getValue().getCreatedAt());
		assertEquals(startTime, saved.getValue().getNextRun(Instant.now()));
	}

	@Test
	public void scheduleTask_shouldChangeTheAnchorWhenOnlyTheIntervalChanges() throws Exception {
		Instant startTime = Instant.now().minus(1, ChronoUnit.DAYS).truncatedTo(ChronoUnit.SECONDS);
		TaskDefinition task = recurringTask(3600L);
		task.setStartTime(Date.from(startTime));
		schedulerService.scheduleTask(task);
		task.setRepeatInterval(7200L);
		schedulerService.scheduleTask(task);

		ArgumentCaptor<RecurringJob> saved = ArgumentCaptor.forClass(RecurringJob.class);
		verify(storageProvider, times(2)).saveRecurringJob(saved.capture());
		List<RecurringJob> registrations = saved.getAllValues();
		assertEquals("PT2H", registrations.get(1).getScheduleExpression());
		assertNotEquals(registrations.get(0).getCreatedAt(), registrations.get(1).getCreatedAt());
	}

	private TaskDefinition recurringTask(long repeatIntervalSeconds) {
		TaskDefinition task = new TaskDefinition();
		task.setUuid(UUID.randomUUID().toString());
		task.setName("Recurring task " + repeatIntervalSeconds + "s");
		task.setTaskClass("org.openmrs.scheduler.tasks.HelloWorldTask");
		task.setStartOnStartup(false);
		task.setStarted(true);
		task.setRepeatInterval(repeatIntervalSeconds);
		return task;
	}

	private TaskDefinition startupTask() {
		TaskDefinition task = new TaskDefinition();
		task.setUuid(UUID.randomUUID().toString());
		task.setName("Startup task");
		task.setTaskClass("org.openmrs.scheduler.tasks.HelloWorldTask");
		task.setStartOnStartup(true);
		task.setStarted(false);
		task.setRepeatInterval(0L);
		when(schedulerDAO.getTasks()).thenReturn(Collections.singletonList(task));
		return task;
	}

	private Job job(StateName state, int version) {
		Job job = mock(Job.class);
		when(job.getState()).thenReturn(state);
		when(job.getVersion()).thenReturn(version);
		when(job.getJobStates()).thenReturn(new ArrayList<>());
		return job;
	}
}
