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

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

import org.jobrunr.jobs.context.JobContext;
import org.openmrs.User;
import org.openmrs.scheduler.TaskContext;
import org.openmrs.scheduler.TaskProgress;
import org.openmrs.scheduler.TaskState;

/**
 * @since 2.9.x
 */
public class JobRunrTaskContext implements TaskContext {

	private final JobContext jobContext;

	private final String userSystemId;

	public JobRunrTaskContext(JobContext jobContext, User scheduledBy) {
		this.jobContext = jobContext;
		this.userSystemId = scheduledBy.getUuid();
	}

	public JobRunrTaskContext(JobContext jobContext, String userSystemId) {
		this.jobContext = jobContext;
		this.userSystemId = userSystemId;
	}

	@Override
	public String getUuid() {
		return jobContext.getJobId().toString();
	}

	@Override
	public Optional<String> getRecurringTaskUuid() {
		throw new UnsupportedOperationException("Please use SchedulerService#getTask() instead until JobRunr is upgraded.");
	}

	@Override
	public String getName() {
		return jobContext.getJobName();
	}

	@Override
	public Optional<Instant> getScheduledAt() {
		throw new UnsupportedOperationException("Please use SchedulerService#getTask() instead until JobRunr is upgraded.");
	}

	@Override
	public String getUserSystemId() {
		return userSystemId;
	}

	@Override
	public Instant getCreatedAt() {
		return jobContext.getCreatedAt();
	}

	@Override
	public Instant getUpdatedAt() {
		return jobContext.getUpdatedAt();
	}

	@Override
	public String getSignature() {
		return jobContext.getJobSignature();
	}

	@Override
	public TaskState getState() {
		return TaskState.valueOf(jobContext.getJobState().name());
	}

	@Override
	public void saveMetadata(String key, Object value) {
		jobContext.saveMetadata(key, value);
	}

	@Override
	public void saveMetadataIfAbsent(String key, Object value) {
		jobContext.saveMetadataIfAbsent(key, value);
	}

	@Override
	public Map<String, Object> getMetadata() {
		return jobContext.getMetadata();
	}

	@Override
	public TaskProgress setProgressBar(int totalProgress) {
		return new JobRunrTaskProgress(jobContext.progressBar(totalProgress));
	}

}
