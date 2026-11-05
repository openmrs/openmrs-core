/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api.db.hibernate;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.Location;
import org.openmrs.api.LocationService;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.event.DeleteDbEvent;
import org.openmrs.api.db.event.SaveDbEvent;
import org.openmrs.event.EntityEvent;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.PayloadApplicationEvent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Checks that {@link EventInterceptor} publishes database events when it runs inside the
 * {@link ChainingInterceptor} that Hibernate is configured with.
 */
public class EventInterceptorTest extends BaseContextSensitiveTest {

	@Autowired
	private ConfigurableApplicationContext context;

	private final List<EntityEvent<?>> events = new CopyOnWriteArrayList<>();

	private final ApplicationListener<PayloadApplicationEvent<?>> listener = event -> {
		if (event.getPayload() instanceof EntityEvent) {
			events.add((EntityEvent<?>) event.getPayload());
		}
	};

	private LocationService locationService;

	@BeforeEach
	public void setUp() {
		locationService = Context.getLocationService();
		context.addApplicationListener(listener);
	}

	@AfterEach
	public void tearDown() {
		context.removeApplicationListener(listener);
	}

	@Test
	public void shouldPublishSaveDbEventWhenANewEntityIsPersisted() {
		Location location = new Location();
		location.setName("A brand new location");

		locationService.saveLocation(location);
		Context.flushSession();

		List<SaveDbEvent<?>> saveEvents = saveEventsFor(location);
		assertEquals(1, saveEvents.size());
		assertNull(saveEvents.get(0).getPreviousState());
	}

	@Test
	public void shouldPublishSaveDbEventWhenAnExistingEntityIsUpdated() {
		Location location = locationService.getLocation(1);
		location.setDescription("changed description");

		locationService.saveLocation(location);
		Context.flushSession();

		assertEquals(1, saveEventsFor(location).size());
	}

	@Test
	public void shouldPublishDeleteDbEventWhenAnEntityIsRemoved() {
		Location location = new Location();
		location.setName("A location to purge");
		locationService.saveLocation(location);
		Context.flushSession();
		Integer id = location.getId();

		locationService.purgeLocation(location);
		Context.flushSession();

		List<DeleteDbEvent<?>> deleteEvents = events.stream().filter(e -> e instanceof DeleteDbEvent)
		        .map(e -> (DeleteDbEvent<?>) e).filter(e -> e.getEntity() == location && e.getPropertyNames().length > 1)
		        .collect(Collectors.toList());
		assertEquals(1, deleteEvents.size());
		assertEquals(id, deleteEvents.get(0).getId());
	}

	private List<SaveDbEvent<?>> saveEventsFor(Object entity) {
		return events.stream().filter(e -> e instanceof SaveDbEvent).map(e -> (SaveDbEvent<?>) e)
		        .filter(e -> e.getEntity() == entity && e.getPropertyNames().length > 1).collect(Collectors.toList());
	}
}
