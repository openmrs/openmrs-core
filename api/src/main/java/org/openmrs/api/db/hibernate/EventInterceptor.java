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

import org.hibernate.CallbackException;
import org.hibernate.EmptyInterceptor;
import org.hibernate.Transaction;
import org.hibernate.collection.spi.PersistentCollection;
import org.hibernate.type.Type;
import org.openmrs.api.db.event.DeleteDbEvent;
import org.openmrs.api.db.event.SaveDbEvent;
import org.openmrs.event.EventPublisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * Publishes {@link SaveDbEvent} and {@link DeleteDbEvent}.
 * 
 * @since 2.9.x
 */
@Component
public class EventInterceptor extends EmptyInterceptor {

	private EventPublisher eventPublisher;
	
	@Autowired
	@Lazy
	public void setEventPublisher(EventPublisher eventPublisher) {
		this.eventPublisher = eventPublisher;
	}
	
	@Override
	public boolean onSave(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types) {
		return onFlushDirty(entity, id, state, null, propertyNames, types);
	}

	@Override
	public boolean onFlushDirty(Object entity, Serializable id, Object[] newState, Object[] previousState, String[] propertyNames, Type[] types) {
		SaveDbEvent<Object> dbEvent = new SaveDbEvent<>(entity, id, newState, previousState, propertyNames);
		eventPublisher.publishEvent(dbEvent);
		return dbEvent.isModified();
	}

	@Override
	public void onDelete(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types) {
		eventPublisher.publishEvent(new DeleteDbEvent<>(entity, id, state, propertyNames));
	}

	@Override
	public void onCollectionUpdate(Object collection, Serializable key) {
		publishCollectionEvent(collection, key);
	}

	@Override
	public void onCollectionRemove(Object collection, Serializable key) throws CallbackException {
		super.onCollectionRemove(collection, key);
	}

	@Override
	public void onCollectionRecreate(Object collection, Serializable key) {
		publishCollectionEvent(collection, key);
	}

	private void publishCollectionEvent(Object collection, Serializable key) {
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
				
				eventPublisher.publishEvent(new SaveDbEvent<>(owner, key, state, previousState, propertyNames));
			}
		}
	}
}
