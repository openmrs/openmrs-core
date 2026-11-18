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

import org.jobrunr.scheduling.JobRequestScheduler;
import org.jobrunr.scheduling.JobScheduler;
import org.jobrunr.server.BackgroundJobServer;
import org.jobrunr.storage.StorageProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.scheduler.db.SchedulerDAO;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class JobRunrSchedulerServiceTest {

	private final BackgroundJobServer server = mock(BackgroundJobServer.class);

	private final JobRunrServerLifecycle lifecycle = new JobRunrServerLifecycle(server);

	private final JobRunrSchedulerService schedulerService = new JobRunrSchedulerService(mock(StorageProvider.class),
	        mock(JobRequestScheduler.class), mock(JobScheduler.class), mock(SchedulerDAO.class));

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
}
