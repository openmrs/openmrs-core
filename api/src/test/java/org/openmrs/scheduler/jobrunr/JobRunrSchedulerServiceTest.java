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

import java.util.Collections;
import java.util.UUID;

import org.jobrunr.jobs.Job;
import org.jobrunr.jobs.JobId;
import org.jobrunr.jobs.states.StateName;
import org.jobrunr.scheduling.JobRequestScheduler;
import org.jobrunr.scheduling.JobScheduler;
import org.jobrunr.storage.RecurringJobsResult;
import org.jobrunr.storage.StorageProvider;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.openmrs.scheduler.TaskDefinition;
import org.openmrs.scheduler.db.SchedulerDAO;
import org.openmrs.test.jupiter.BaseContextMockTest;
import org.openmrs.util.PrivilegeConstants;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class JobRunrSchedulerServiceTest extends BaseContextMockTest {

	@Mock
	private StorageProvider storageProvider;

	@Mock
	private JobRequestScheduler jobRequestScheduler;

	@Mock
	private JobScheduler jobScheduler;

	@Mock
	private SchedulerDAO schedulerDAO;

	@Test
	public void onStartup_shouldEnqueueStartOnStartupTaskWithoutCreatorAsDaemon() throws Exception {
		TaskDefinition taskDefinition = new TaskDefinition();
		taskDefinition.setUuid(UUID.randomUUID().toString());
		taskDefinition.setName("Task without creator");
		taskDefinition.setStartOnStartup(true);
		when(schedulerDAO.getTasks()).thenReturn(Collections.singletonList(taskDefinition));

		JobId jobId = new JobId(UUID.fromString(taskDefinition.getUuid()));
		when(jobRequestScheduler.enqueue(eq(jobId.asUUID()), any(JobRequestAdapter.class))).thenReturn(jobId);
		Job job = mock(Job.class);
		when(job.getState()).thenReturn(StateName.ENQUEUED);
		when(storageProvider.getJobById(any(JobId.class))).thenReturn(job);
		when(storageProvider.save(job)).thenReturn(job);
		when(storageProvider.getRecurringJobs()).thenReturn(new RecurringJobsResult());
		when(userContext.hasPrivilege(PrivilegeConstants.MANAGE_SCHEDULER)).thenReturn(true);

		new JobRunrSchedulerService(storageProvider, jobRequestScheduler, jobScheduler, schedulerDAO).onStartup();

		ArgumentCaptor<JobRequestAdapter> jobRequest = ArgumentCaptor.forClass(JobRequestAdapter.class);
		verify(jobRequestScheduler).enqueue(eq(jobId.asUUID()), jobRequest.capture());
		assertEquals("daemon", jobRequest.getValue().getUserSystemId());
	}
}
