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

import org.hibernate.SessionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.scheduler.SchedulerService;
import org.openmrs.scheduler.TaskData;
import org.openmrs.test.SkipBaseSetup;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Tests that {@link OutboxTaskSchedulerInitializer} closes the sessions it opens and leaves its
 * caller's session alone. A session it leaves bound would be reused by every later transaction on
 * the thread, so the tests cannot run in a transaction.
 */
@SkipBaseSetup
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class OutboxTaskSchedulerInitializerTest extends BaseContextSensitiveTest {

	@Autowired
	private SessionFactory sessionFactory;

	private SchedulerService schedulerService;

	private OutboxTaskSchedulerInitializer initializer;

	@BeforeEach
	public void setUp() {
		// release any session the base class opened, so each test starts with none
		Context.closeSession();
		assertNull(TransactionSynchronizationManager.getResource(sessionFactory));

		schedulerService = mock(SchedulerService.class);
		initializer = new OutboxTaskSchedulerInitializer(schedulerService);
	}

	@Test
	public void schedule_shouldCloseTheSessionItOpened() {
		initializer.schedule();

		verify(schedulerService).scheduleRecurrently(eq(OutboxTaskSchedulerInitializer.OUTBOX_POLLER_TASK_UUID),
		    any(TaskData.class), any(Duration.class), anyString());
		verify(schedulerService).scheduleRecurrently(eq(OutboxTaskSchedulerInitializer.OUTBOX_CLEANUP_TASK_UUID),
		    any(TaskData.class), any(Duration.class), anyString());
		assertNoSessionOpen();
	}

	@Test
	public void schedule_shouldCloseTheSessionItOpenedWhenSchedulingFails() {
		doThrow(new APIException("scheduling failed")).when(schedulerService).scheduleRecurrently(anyString(),
		    any(TaskData.class), any(Duration.class), anyString());

		assertThrows(APIException.class, () -> initializer.schedule());

		assertNoSessionOpen();
	}

	@Test
	public void schedule_shouldLeaveTheCallersSessionOpen() {
		assertCallersSessionLeftOpen(() -> initializer.schedule());
	}

	@Test
	public void deleteScheduledTasks_shouldCloseTheSessionItOpened() {
		initializer.deleteScheduledTasks();

		verify(schedulerService).deleteRecurringTask(OutboxTaskSchedulerInitializer.OUTBOX_POLLER_TASK_UUID);
		verify(schedulerService).deleteRecurringTask(OutboxTaskSchedulerInitializer.OUTBOX_CLEANUP_TASK_UUID);
		assertNoSessionOpen();
	}

	@Test
	public void deleteScheduledTasks_shouldCloseTheSessionItOpenedWhenDeletionFails() {
		doThrow(new APIException("deletion failed")).when(schedulerService).deleteRecurringTask(anyString());

		assertThrows(APIException.class, () -> initializer.deleteScheduledTasks());

		assertNoSessionOpen();
	}

	@Test
	public void deleteScheduledTasks_shouldLeaveTheCallersSessionOpen() {
		assertCallersSessionLeftOpen(() -> initializer.deleteScheduledTasks());
	}

	private void assertNoSessionOpen() {
		assertFalse(Context.isSessionOpen());
		assertNull(TransactionSynchronizationManager.getResource(sessionFactory));
	}

	private void assertCallersSessionLeftOpen(Runnable call) {
		Context.openSession();
		try {
			Object holder = TransactionSynchronizationManager.getResource(sessionFactory);
			assertNotNull(holder);

			call.run();

			assertTrue(Context.isSessionOpen());
			assertSame(holder, TransactionSynchronizationManager.getResource(sessionFactory));
		} finally {
			Context.closeSession();
		}
		assertNoSessionOpen();
	}
}
