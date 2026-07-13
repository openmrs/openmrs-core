/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.event.outbox.tasks;

import java.time.Duration;

import org.openmrs.api.context.Context;
import org.openmrs.scheduler.SchedulerService;
import org.openmrs.util.PrivilegeConstants;
import org.springframework.stereotype.Component;

/**
 * @since 2.9.0
 */
@Component
public class OutboxTaskSchedulerInitializer {

	public static final String OUTBOX_POLLER_TASK_NAME = "Transactional Outbox Poller";

	public static final String OUTBOX_POLLER_TASK_UUID = "bc2739ac-5bf3-4ecd-8e11-d383d53695bb";

	public static final String OUTBOX_CLEANUP_TASK_NAME = "Transactional Outbox Cleanup";

	public static final String OUTBOX_CLEANUP_TASK_UUID = "a0c11918-0aba-46bf-a7c6-7b0cc47e7323";

	private final SchedulerService schedulerService;

	public OutboxTaskSchedulerInitializer(SchedulerService schedulerService) {
		this.schedulerService = schedulerService;
	}

	public void schedule() {
		try {
			if (!Context.isSessionOpen()) {
				Context.openSession();
			}
			Context.addProxyPrivilege(PrivilegeConstants.MANAGE_SCHEDULER);

			schedulerService.scheduleRecurrently(OUTBOX_POLLER_TASK_UUID, new OutboxPollingTaskData(),
			    Duration.ofSeconds(15), OUTBOX_POLLER_TASK_NAME);

			schedulerService.scheduleRecurrently(OUTBOX_CLEANUP_TASK_UUID, new OutboxCleanupTaskData(), Duration.ofDays(1),
			    OUTBOX_CLEANUP_TASK_NAME);
		} finally {
			Context.removeProxyPrivilege(PrivilegeConstants.MANAGE_SCHEDULER);
		}
	}
}
