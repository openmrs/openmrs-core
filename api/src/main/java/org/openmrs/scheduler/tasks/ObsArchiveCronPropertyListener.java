/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.scheduler.tasks;

import java.util.Optional;

import org.openmrs.GlobalProperty;
import org.openmrs.api.GlobalPropertyListener;
import org.openmrs.api.context.Context;
import org.openmrs.scheduler.RecurringTaskDetails;
import org.openmrs.scheduler.SchedulerService;

public class ObsArchiveCronPropertyListener implements GlobalPropertyListener {

	private static final String CRON_PROPERTY = "obs.archive.cron";

	private static final String TASK_NAME = "Observation Archiving Job";

	private static final String DEFAULT_CRON = "0 0 * * *";

	@Override
	public boolean supportsPropertyName(String propertyName) {
		return CRON_PROPERTY.equals(propertyName);
	}

	@Override
	public void globalPropertyChanged(GlobalProperty newValue) {
		String newCron = newValue.getPropertyValue();
		if (newCron == null || newCron.isBlank()) {
			newCron = DEFAULT_CRON;
		}

		SchedulerService schedulerService = Context.getSchedulerService();

		// Find existing recurring task by name and delete it
		Optional<RecurringTaskDetails> existingTask = schedulerService.getRecurringTasks()
		        .filter(t -> t.getName() != null && t.getName().equals(TASK_NAME)).findFirst();
		existingTask.ifPresent(t -> schedulerService.deleteRecurringTask(t.getUuid()));

		// Reschedule with the new cron expression
		schedulerService.scheduleRecurrently(TASK_NAME, new ObsArchivingTaskData(), newCron);
	}

	@Override
	public void globalPropertyDeleted(String propertyName) {
		// Reschedule with the default cron when the property is deleted
		globalPropertyChanged(new GlobalProperty(CRON_PROPERTY, DEFAULT_CRON));
	}
}
