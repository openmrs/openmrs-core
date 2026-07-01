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
import java.time.Instant;
import java.util.Date;

import jakarta.persistence.Query;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.openmrs.scheduler.TaskContext;
import org.openmrs.scheduler.TaskHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * @since 2.9.0
 */
@Component
public class OutboxCleanupTaskHandler implements TaskHandler<OutboxCleanupTaskData> {

	private static final Logger log = LoggerFactory.getLogger(OutboxCleanupTaskHandler.class);

	public final Duration cleanupOlderThan;

	private final SessionFactory sessionFactory;

	public OutboxCleanupTaskHandler(SessionFactory sessionFactory,
	    @Value("${outboxevent.cleanup.olderThan:7}") int cleanupOlderThan) {
		this.sessionFactory = sessionFactory;
		this.cleanupOlderThan = Duration.ofDays(cleanupOlderThan);
	}

	@Override
	@Transactional
	public void execute(OutboxCleanupTaskData taskData, TaskContext taskContext) throws Exception {
		Session session = sessionFactory.getCurrentSession();

		Date threshold = Date.from(Instant.now().minus(cleanupOlderThan));

		Query query = session
		        .createQuery("delete from OutboxEvent where status = 'COMPLETED' and dateChanged <= :threshold");
		query.setParameter("threshold", threshold);

		int deletedCount = query.executeUpdate();
		if (deletedCount > 0) {
			log.debug("Deleted {} completed outbox items older than {} days", deletedCount, cleanupOlderThan.toDays());
		}
	}
}
