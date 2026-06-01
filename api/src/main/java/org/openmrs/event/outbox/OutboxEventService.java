/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.event.outbox;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;

import jakarta.persistence.Query;

import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * It's internal and not for use in modules.
 *
 * @since 2.9.x
 */
@Service
public class OutboxEventService {

	private static final Logger log = LoggerFactory.getLogger(OutboxEventService.class);

	private final SessionFactory sessionFactory;

	private final Duration listenerTimeout;

	public OutboxEventService(SessionFactory sessionFactory,
	    @Value("${outboxevent.listener.timeout:120}") int listenerTimeout) {
		this.sessionFactory = sessionFactory;
		this.listenerTimeout = Duration.ofSeconds(listenerTimeout);
	}

	/**
	 * Revert tasks stuck in PROCESSING for more than {@code outboxevent.listener.timeout} .
	 */
	@Transactional(noRollbackFor = OutboxException.class)
	public void resetStuckEvent() throws OutboxException {
		Date threshold = Date.from(Instant.now().minus(listenerTimeout));
		Query resetStuckQuery = sessionFactory.getCurrentSession().createQuery(
		    "update OutboxEvent set status = 'PENDING', errorCount = coalesce(errorCount, 0) + 1, "
		            + "errorMessage = :errorMessage, dateChanged = :now where status = 'PROCESSING' and dateChanged < :threshold");
		resetStuckQuery.setParameter("now", new Date());
		resetStuckQuery.setParameter("threshold", threshold);
		resetStuckQuery.setParameter("errorMessage",
		    "Stuck in PROCESSING state for more than " + listenerTimeout.getSeconds() + " seconds");
		int resetCount = resetStuckQuery.executeUpdate();
		if (resetCount > 0) {
			// Throw an error to increase visibility of the event
			throw new OutboxException("Reset stuck outbox item from PROCESSING back to PENDING");
		}
	}

	@Transactional(readOnly = true)
	public void warnOnTooManyPendingEvents() {
		Query query = sessionFactory.getCurrentSession()
		        .createQuery("select count(*) from OutboxEvent where status in ('PENDING')");
		long count = query.getSingleResult() == null ? 0 : (long) query.getSingleResult();
		if (count > 10000) {
			log.warn("Too many pending ({}) outbox events to process in the system! "
			        + "Please review your listeners to fix performance issues so outbox events do no pile up!",
			    count);
		}
	}

	@Transactional(readOnly = true)
	public List<OutboxEvent> getProcessingAndPendingEvents() {
		Query query = sessionFactory.getCurrentSession()
		        .createQuery("from OutboxEvent where status in ('PENDING', 'PROCESSING') order by id asc");
		query.setMaxResults(100);

		return query.getResultList();
	}

	@Transactional
	public boolean lockEventForProcessing(OutboxEvent outboxEvent) {
		Query claimQuery = sessionFactory.getCurrentSession().createQuery(
		    "update OutboxEvent set status = 'PROCESSING', dateChanged = :now where id = :id and status = 'PENDING'");

		claimQuery.setParameter("id", outboxEvent.getId());
		claimQuery.setParameter("now", new Date());

		int updatedCount = claimQuery.executeUpdate();
		return updatedCount == 1;
	}

	@Transactional
	public void saveOutboxEvent(OutboxEvent outboxEvent) {
		outboxEvent.setDateChanged(new Date());
		sessionFactory.getCurrentSession().persist(outboxEvent);
	}
}
