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

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.hibernate.Interceptor;
import org.hibernate.collection.spi.PersistentCollection;
import org.hibernate.type.Type;
import org.openmrs.api.db.event.DeleteDbEvent;
import org.openmrs.api.db.event.SaveDbEvent;
import org.openmrs.event.EventPublisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * Publishes {@link SaveDbEvent} and {@link DeleteDbEvent}.
 *
 * @since 2.9.x
 */
@Component
public class EventInterceptor implements Interceptor {

	private EventPublisher eventPublisher;

	@Autowired
	@Lazy
	public void setEventPublisher(EventPublisher eventPublisher) {
		this.eventPublisher = eventPublisher;
	}

	@Override
	public boolean onPersist(Object entity, Object id, Object[] state, String[] propertyNames, Type[] types) {
		return onFlushDirty(entity, id, state, null, propertyNames, types);
	}

	@Override
	public boolean onFlushDirty(Object entity, Object id, Object[] currentState, Object[] previousState,
	        String[] propertyNames, Type[] types) {
		SaveDbEvent<Object> dbEvent = new SaveDbEvent<>(entity, (Serializable) id, currentState, previousState,
		        propertyNames);
		eventPublisher.publishEvent(dbEvent);
		return dbEvent.isModified();
	}

	public void onRemove(Object entity, Object id, Object[] state, String[] propertyNames, Type[] types) {
		eventPublisher.publishEvent(new DeleteDbEvent<>(entity, (Serializable) id, state, propertyNames));
	}

	@Override
	public void onCollectionUpdate(Object collection, Object key) {
		publishCollectionEvent(collection, key, false);
	}

	@Override
	public void onCollectionRemove(Object collection, Object key) {
		publishCollectionEvent(collection, key, true);
	}

	@Override
	public void onCollectionRecreate(Object collection, Object key) {
		publishCollectionEvent(collection, key, false);
	}

	private void publishCollectionEvent(Object collection, Object key, boolean isRemove) {
		if (collection instanceof PersistentCollection) {
			PersistentCollection pc = (PersistentCollection) collection;
			Object owner = pc.getOwner();
			if (owner != null) {

				// 1. Get the original snapshot from Hibernate
				Serializable snapshot = pc.getStoredSnapshot();
				Collection<?> originalElements = Collections.emptyList();

				// Hibernate stores snapshots differently depending on the collection type
				if (snapshot instanceof Map) {
					originalElements = ((Map<?, ?>) snapshot).values();
				} else if (snapshot instanceof Collection) {
					// PersistentList and PersistentBag snapshots are stored as Collections
					originalElements = (Collection<?>) snapshot;
				}

				Collection<?> currentElements;
				if (pc instanceof Map) {
					currentElements = ((Map<?, ?>) pc).values();
				} else {
					currentElements = (Collection<?>) pc;
				}

				// Extract the property name from the collection's role (e.g., "org.openmrs.Patient.identifiers" -> "identifiers")
				String role = pc.getRole();
				String propertyName = (role != null && role.contains(".")) ? role.substring(role.lastIndexOf('.') + 1) : "";

				// Pass the property name and the current collection state in the event arrays
				String[] propertyNames = propertyName.isEmpty() ? new String[0] : new String[] { propertyName };
				Object[] state = propertyName.isEmpty() ? new Object[0] : new Object[] { currentElements };
				Object[] previousState = propertyName.isEmpty() ? new Object[0] : new Object[] { originalElements };

				if (!isRemove) {
					eventPublisher
					        .publishEvent(new SaveDbEvent<>(owner, (Serializable) key, state, previousState, propertyNames));
				} else {
					eventPublisher.publishEvent(new DeleteDbEvent<>(owner, (Serializable) key, state, propertyNames));
				}
			}
		}
	}
}
