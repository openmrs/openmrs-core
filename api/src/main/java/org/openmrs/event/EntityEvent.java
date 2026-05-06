/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.event;

import org.jspecify.annotations.Nullable;
import org.springframework.core.ResolvableType;
import org.springframework.core.ResolvableTypeProvider;

import java.util.HashSet;
import java.util.Set;

/**
 * It can be implemented to publish application events related to an entity.
 * <p>
 * Entity events may be put in a transactional outbox. If there is a listener annotated with
 * {@link org.openmrs.event.outbox.OutboxEventListener} for an event, the event needs to be 
 * serializable to JSON with Jackson {@link com.fasterxml.jackson.databind.ObjectMapper} or
 * implement {@link org.openmrs.event.outbox.OutboxEventPayload}.
 * <p>
 * Entity events contain autopopulated sessionId to group events happening in the same session.
 * <p>
 * They may also contain tags to use them e.g. as listener conditions 
 * or include additional information for listeners.
 * 
 * @param <T> the related entity
 *     
 * @since 2.9.x
 */
	public abstract class EntityEvent<T> extends BaseEvent implements ResolvableTypeProvider {

	protected T entity;
	
	public EntityEvent() {
	}
	
	public EntityEvent(T entity) {
		this(entity, new HashSet<>());
	}
 	
	public EntityEvent(T entity, Set<String> tags) {
		super(entity, tags);
		this.entity = entity;
	}

	public T getEntity() {
		return entity;
	}
	
	public void setEntity(T entity) {
		this.entity = entity;
		this.source = entity;
	}

	@Override
	public @Nullable ResolvableType getResolvableType() {
		if (entity != null && getClass().getTypeParameters().length == 1) {
			return ResolvableType.forClassWithGenerics(getClass(), ResolvableType.forInstance(entity));
		} else {
			return ResolvableType.forClass(getClass());
		}
	}
	
}
