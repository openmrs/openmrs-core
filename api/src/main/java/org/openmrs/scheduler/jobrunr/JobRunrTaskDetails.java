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

import org.jobrunr.jobs.Job;
import org.jobrunr.jobs.states.ScheduledState;
import org.jobrunr.jobs.states.StateName;
import org.openmrs.scheduler.TaskDetails;
import org.openmrs.scheduler.TaskState;

/**
 * @since 2.9.x
 */
public class JobRunrTaskDetails implements TaskDetails {

	private final Job job;

	public JobRunrTaskDetails(Job job) {
		this.job = job;
	}

	@Override
	public String getUuid() {
		return job.getId().toString();
	}

	@Override
	public Optional<String> getRecurringTaskUuid() {
		return job.getRecurringJobId();
	}

	@Override
	public String getName() {
		return job.getJobName();
	}

	@Override
	public TaskState getState() {
		return TaskState.valueOf(job.getJobState().getName().name());
	}

	@Override
	public Optional<Instant> getScheduledAt() {
		if (job.getState() == StateName.SCHEDULED) {
			ScheduledState scheduledState = job.getJobState();
			return Optional.of(scheduledState.getScheduledAt());
		}
		return Optional.empty();
	}

	@Override
	public Instant getCreatedAt() {
		return job.getCreatedAt();
	}

	@Override
	public Instant getUpdatedAt() {
		return job.getUpdatedAt();
	}

	@Override
	public String getSignature() {
		return job.getJobSignature();
	}

	@Override
	public Map<String, Object> getMetadata() {
		return job.getMetadata();
	}
}
