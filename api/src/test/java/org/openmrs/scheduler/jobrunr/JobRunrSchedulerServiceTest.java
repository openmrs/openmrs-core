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

import java.util.ArrayList;
import java.util.Collections;
import java.util.UUID;

import org.jobrunr.jobs.Job;
import org.jobrunr.jobs.RecurringJob;
import org.jobrunr.jobs.states.StateName;
import org.jobrunr.scheduling.JobRequestScheduler;
import org.jobrunr.scheduling.JobScheduler;
import org.jobrunr.server.BackgroundJobServer;
import org.jobrunr.storage.ConcurrentJobModificationException;
import org.jobrunr.storage.JobNotFoundException;
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
import static org.mockito.ArgumentMatchers.any;
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

	private final JobRunrSchedulerService schedulerService = new JobRunrSchedulerService(storageProvider,
	        mock(JobRequestScheduler.class), mock(JobScheduler.class), schedulerDAO);

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
