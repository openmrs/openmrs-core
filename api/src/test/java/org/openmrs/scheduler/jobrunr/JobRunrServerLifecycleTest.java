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

import org.jobrunr.server.BackgroundJobServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class JobRunrServerLifecycleTest {

	private final BackgroundJobServer server = mock(BackgroundJobServer.class);

	private final JobRunrServerLifecycle lifecycle = new JobRunrServerLifecycle(server);

	@AfterEach
	public void clearDeferral() {
		new JobRunrServerLifecycle(mock(BackgroundJobServer.class)).openmrsStarted();
	}

	@Test
	public void start_shouldStartTheServerWhenNotDeferred() {
		lifecycle.start();

		verify(server).start();
		assertTrue(lifecycle.isRunning());
	}

	@Test
	public void start_shouldNotStartTheServerUntilOpenmrsHasStarted() {
		JobRunrServerLifecycle.deferStartUntilOpenmrsStarted();

		lifecycle.start();

		verify(server, never()).start();
		assertFalse(lifecycle.isRunning());

		lifecycle.openmrsStarted();

		verify(server).start();
		assertTrue(lifecycle.isRunning());
	}

	@Test
	public void openmrsStarted_shouldNotStartARunningServerAgain() {
		lifecycle.start();

		lifecycle.openmrsStarted();

		verify(server, times(1)).start();
	}

	@Test
	public void start_shouldStartServersOfLaterContextsOnceOpenmrsHasStarted() {
		JobRunrServerLifecycle.deferStartUntilOpenmrsStarted();
		lifecycle.openmrsStarted();
		BackgroundJobServer refreshedServer = mock(BackgroundJobServer.class);

		new JobRunrServerLifecycle(refreshedServer).start();

		verify(refreshedServer).start();
	}

	@Test
	public void stop_shouldStopTheServer() {
		lifecycle.start();

		lifecycle.stop();

		verify(server).stop();
		assertFalse(lifecycle.isRunning());
	}
}
