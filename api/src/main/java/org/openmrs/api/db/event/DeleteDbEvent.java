/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api.db.event;

import org.openmrs.event.EntityEvent;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * DB level event emitted upon deletion of an entity.
 *
 * @param <T> type of entity
 */
public class DeleteDbEvent<T> extends EntityEvent<T> {
	
	private Serializable id;
	private Object[] state;
	private String[] propertyNames;
	
	public DeleteDbEvent() {
	}
	
	public DeleteDbEvent(T entity, Serializable id, Object[] state, String[] propertyNames) {
		this(entity, id, state, propertyNames, new HashSet<>());
	}

	public DeleteDbEvent(T entity, Serializable id, Object[] state, String[] propertyNames, Set<String> tags) {
		super(entity, tags);
		this.state = state;
		this.propertyNames = propertyNames;
		this.id = id;
	}

	public Serializable getId() {
		return id;
	}

	public Object[] getState() {
		return state;
	}

	public String[] getPropertyNames() {
		return propertyNames;
	}
}
