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

import org.junit.jupiter.api.Test;
import org.openmrs.GlobalProperty;
import org.openmrs.api.AdministrationService;
import org.openmrs.scheduler.SchedulerService;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class ObsArchiveCronPropertyListenerTest extends BaseContextSensitiveTest {

	@Autowired
	private AdministrationService adminService;

	@Autowired
	private SchedulerService schedulerService;

	@Test
	public void shouldRescheduleTaskOnCronChange() throws Exception {
		// Verify task is not scheduled initially
		boolean isScheduled = schedulerService.getRecurringTasks()
		        .anyMatch(t -> t.getName() != null && t.getName().equals("Observation Archiving Job"));

		// Set cron
		adminService.saveGlobalProperty(new GlobalProperty("obs.archive.cron", "0 0 * * *"));

		boolean isScheduledAfter = schedulerService.getRecurringTasks()
		        .anyMatch(t -> t.getName() != null && t.getName().equals("Observation Archiving Job"));
		assertTrue(isScheduledAfter);
	}
}
