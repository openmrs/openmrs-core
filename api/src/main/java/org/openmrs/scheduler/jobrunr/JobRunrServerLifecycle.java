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
import org.springframework.context.SmartLifecycle;

/**
 * Ties the JobRunr {@link BackgroundJobServer} to the lifecycle of the application context that
 * created it, so jobs are only processed while that context is fully refreshed and not yet closing.
 * <p>
 * While OpenMRS starts up, the application context is refreshed before modules are started, and
 * again once they are, so a job processed at the end of the first refresh fails on a module's task
 * class that is not loaded yet. Startup code that refreshes contexts before starting modules calls
 * {@link #deferStartUntilOpenmrsStarted()} first, and the server then starts only once
 * {@link #openmrsStarted()} reports that OpenMRS has finished starting.
 *
 * @since 2.9.0
 */
public class JobRunrServerLifecycle implements SmartLifecycle {

	private static volatile boolean startDeferred = false;

	private final BackgroundJobServer backgroundJobServer;

	private volatile boolean running;

	public JobRunrServerLifecycle(BackgroundJobServer backgroundJobServer) {
		this.backgroundJobServer = backgroundJobServer;
	}

	/**
	 * Keeps background job servers from starting with their application context until
	 * {@link #openmrsStarted()} is called.
	 */
	public static void deferStartUntilOpenmrsStarted() {
		startDeferred = true;
	}

	/**
	 * Starts the background job server if its start was deferred until OpenMRS has started.
	 */
	public synchronized void openmrsStarted() {
		startDeferred = false;
		if (!running) {
			start();
		}
	}

	@Override
	public synchronized void start() {
		if (startDeferred) {
			return;
		}
		backgroundJobServer.start();
		running = true;
	}

	@Override
	public synchronized void stop() {
		running = false;
		backgroundJobServer.stop();
	}

	@Override
	public boolean isRunning() {
		return running;
	}
}
