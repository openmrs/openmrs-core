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
 * DB level event emitted upon creation or modification of an entity.
 * 
 * @param <T> type of entity
 */
public class SaveDbEvent<T> extends EntityEvent<T> {
	
	private Serializable id;
	private Object[] state;
	private Object[] previousState;
	private String[] propertyNames;
	private boolean modified = false;
	
	public SaveDbEvent() {
	}
	
	public SaveDbEvent(T entity, Serializable id, Object[] state, String[] propertyNames) {
		this(entity, id, state, propertyNames, null, new HashSet<>());
	}

	public SaveDbEvent(T entity, Serializable id, Object[] state, Object[] previousState, String[] propertyNames) {
		this(entity, id, state, previousState, propertyNames, new HashSet<>());
	}

	public SaveDbEvent(T entity, Serializable id, Object[] state, Object[] previousState, String[] propertyNames, Set<String> tags) {
		super(entity, tags);
		this.id = id;
		this.state = state;
		this.previousState = previousState;
		this.propertyNames = propertyNames;
	}

	public Serializable getId() {
		return id;
	}

	public Object[] getState() {
		return state;
	}

	public Object[] getPreviousState() {
		return previousState;
	}

	public String[] getPropertyNames() {
		return propertyNames;
	}

	public boolean isModified() {
		return modified;
	}

	/**
	 * The setting is only respected if called from a synchronous listener
	 * and before commit. Changes will be applied to the object by Hibernate before persisting.
	 * Please note that modifications to collection properties are not propagated and are read-only.
	 * 
	 * @param modified false by default
	 */
	public void setModified(boolean modified) {
		this.modified = modified;
	}
}
