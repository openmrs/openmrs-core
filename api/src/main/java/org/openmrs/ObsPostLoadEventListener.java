/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs;

import java.lang.reflect.Field;

import javax.annotation.PostConstruct;

import org.hibernate.SessionFactory;
import org.hibernate.event.service.spi.EventListenerRegistry;
import org.hibernate.event.spi.EventType;
import org.hibernate.event.spi.PostLoadEvent;
import org.hibernate.event.spi.PostLoadEventListener;
import org.hibernate.internal.SessionFactoryImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Hibernate by default calls setters when initializing a persistent entity from the database
 * meaning an Obs would be marked dirty the first time it's loaded by hibernate, therefore we need
 * to use an instance of this PostLoadEventListener to mark an Obs as not dirty when it gets loaded.
 * 
 * <pre>
 * Note that in hibernate 4, event listeners are now registered via the new integrator and service
 * capabilities which leverage Java's standard java.util.ServiceLoader mechanism to discover then
 * but unfortunately spring hasn't caught up with these integrator capabilities therefore we need to
 * manually 'springfy' the registration of our EventListener
 * </pre>
 */
@Component
public class ObsPostLoadEventListener implements PostLoadEventListener {
	
	private static final Logger log = LoggerFactory.getLogger(ObsPostLoadEventListener.class);
	
	@Autowired
	private SessionFactory sessionFactory;
	
	@PostConstruct
	public void registerListener() {
		EventListenerRegistry registry = ((SessionFactoryImpl) sessionFactory).getServiceRegistry().getService(
		    EventListenerRegistry.class);
		registry.getEventListenerGroup(EventType.POST_LOAD).appendListener(this);
	}
	
	@Override
	public void onPostLoad(PostLoadEvent event) {
		if (Obs.class.isAssignableFrom(event.getEntity().getClass())) {
			Field field = null;
			try {
				field = Obs.class.getDeclaredField("dirty");
				field.setAccessible(true);
				field.set(event.getEntity(), false);
			}
			catch (ReflectiveOperationException e) {
				log.error("Failed to unset an Obs as dirty after being loaded from the database", e);
			}
			finally {
				if (field != null) {
					field.setAccessible(false);
				}
			}
		}
	}
}
