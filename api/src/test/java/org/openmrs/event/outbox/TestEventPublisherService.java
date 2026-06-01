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

import org.hibernate.SessionFactory;
import org.openmrs.GlobalProperty;
import org.openmrs.api.AdministrationService;
import org.openmrs.event.EventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * A test service that publishes events.
 */
@Service
public class TestEventPublisherService {

	private final EventPublisher publisher;
	
	private final SessionFactory sessionFactory;
	
	private final AdministrationService adminService;

	public TestEventPublisherService(EventPublisher publisher, AdministrationService adminService, SessionFactory sessionFactory) {
		this.publisher = publisher;
		this.adminService = adminService;
		this.sessionFactory = sessionFactory;
	}
	
	@Transactional
	public void clearOutbox() {
		sessionFactory.getCurrentSession().createQuery("delete from OutboxEvent").executeUpdate();
	}
	
	public void publishEventWithoutTransaction(OutboxableEvent event) {
		adminService.saveGlobalProperty(new GlobalProperty("eventPublished", "true"));
		publisher.publishEvent(event);
	}
	
	@Transactional
	public void publishEventInTransaction(OutboxableEvent event) {
		publishEventWithoutTransaction(event);
	}
}
