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

import org.jobrunr.jobs.lambdas.JobRequestHandler;
import org.openmrs.scheduler.TaskData;

/**
 * Adapts a {@link TaskData} to a JobRunr {@link org.jobrunr.jobs.lambdas.JobRequest}.
 *
 * @since 2.9.x
 */
public class JobRequestAdapter implements org.jobrunr.jobs.lambdas.JobRequest {

	private TaskData taskRequest;

	private String userSystemId;

	// No-arg constructor required for serialization
	public JobRequestAdapter() {
	}

	public JobRequestAdapter(TaskData taskRequest, String userSystemId) {
		this.taskRequest = taskRequest;
		this.userSystemId = userSystemId;
	}

	@Override
	public Class<? extends JobRequestHandler> getJobRequestHandler() {
		return JobRequestHandlerAdapter.class;
	}

	public TaskData getJobRequest() {
		return taskRequest;
	}

	public String getUserSystemId() {
		return userSystemId;
	}
}
