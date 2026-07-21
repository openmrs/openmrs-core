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

import java.util.Map;

import org.jobrunr.JobRunrException;
import org.openmrs.api.context.Daemon;
import org.openmrs.scheduler.TaskContext;
import org.openmrs.scheduler.TaskData;
import org.openmrs.scheduler.TaskException;
import org.openmrs.scheduler.TaskHandler;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.GenericTypeResolver;
import org.springframework.stereotype.Component;

/**
 * A generic handler that delegates execution to the appropriate {@link TaskHandler}.
 * 
 * @since 2.9.0
 */
@Component
public class JobRequestHandlerAdapter implements org.jobrunr.jobs.lambdas.JobRequestHandler<JobRequestAdapter>, ApplicationContextAware {
	
	private ApplicationContext applicationContext;

	/**
	 * The capability that proves to {@link Daemon} this class is allowed to act with daemon
     * permissions.
	 */
	private static volatile Daemon.CallerKey daemonCallerKey;

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void run(JobRequestAdapter jobRequestAdapter) throws Exception {
		TaskData request = jobRequestAdapter.getJobRequest();
		TaskContext context = new JobRunrTaskContext(jobContext(), jobRequestAdapter.getUserSystemId());
		Map<String, TaskHandler> handlers = applicationContext.getBeansOfType(TaskHandler.class);

		for (TaskHandler handler : handlers.values()) {
			Class<?> genericType = GenericTypeResolver.resolveTypeArgument(handler.getClass(), TaskHandler.class);
			if (genericType != null && genericType.isAssignableFrom(request.getClass())) {
				Daemon.executeScheduledTaskAsUser(jobRequestAdapter.getUserSystemId(), () -> {
					try {
						handler.execute(request, context);
					} catch (TaskException e) {
						if (e.isDoNotRetry()) {
							throw new JobRunrException(e.getMessage(), e.isDoNotRetry(), e);
						} else {
							throw e;
						}
					}
				}, daemonCallerKey());
				return;
			}
		}
		throw new IllegalStateException("No handler found for " + request.getClass().getName());
	}

	/**
	 * Receives the {@link Daemon} caller key. Called only by {@link Daemon} during its initialization.
	 *
	 * @param callerKey the caller key issued by {@link Daemon}
	 * @since 3.0.0, 2.9.0, 2.8.9
	 */
	public static void setDaemonCallerKey(Daemon.CallerKey callerKey) {
		if (callerKey != null && daemonCallerKey == null) {
			daemonCallerKey = callerKey;
		}
	}

	private static Daemon.CallerKey daemonCallerKey() {
		if (daemonCallerKey == null) {
			// Guarantee Daemon has initialized and therefore handed us the key, regardless of the order in
			// which the two classes were first loaded.
			Daemon.ensureInitialized();
		}
		return daemonCallerKey;
	}
}
