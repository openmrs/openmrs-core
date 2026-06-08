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

import org.codehaus.jackson.annotate.JsonIgnore;
import org.jspecify.annotations.Nullable;
import org.springframework.core.ResolvableType;
import org.springframework.core.ResolvableTypeProvider;

import java.util.Map;
import java.util.Set;

/**
 * Issued by the CDC engine (if available, see e.g. https://github.com/openmrs/openmrs-module-debezium)
 * <p>
 * Please note that CDC events are usually not happening inside an application transaction rather
 * they are fetched from DB by a separate process e.g. Debezium. For this reason it does not make sense
 * to use {@link org.springframework.transaction.event.TransactionalEventListener} to process them.
 * <p>
 * If you want to guarantee delivery, please use {@link org.springframework.context.event.EventListener} 
 * so that an event is processed in the same thread as the publisher and if the processing fails, it will be
 * re-tried by the publisher. It is recommended only for fast enough listeners so that publisher is not
 * blocked for too long, and it keeps up with the events queue.
 * <p>
 * If you combine {@link org.springframework.context.event.EventListener} with {@link org.springframework.scheduling.annotation.Async}
 * you may run longer processing, but you lose the ability to re-try if anything fails, and in worst case you may lose the event.
 * <p>
 * For longer running processing with guaranteed delivery you may use {@link org.openmrs.event.outbox.OutboxEventListener}
 * to persist events to the database and have them processed asynchronously or publish events with 
 * {@link org.springframework.context.event.EventListener} to a message broker for asynchronous processing.
 * The latter is more performant, and it can also be used to exchange messages with external systems.
 * 
 * @param <T> entityType associated with this event
 * 
 * @since 2.9.0
 */
public class CDCEvent<T> extends BaseEvent implements ResolvableTypeProvider {
	private static final long serialVersionUID = 1L;
	
	private String snapshot;
	
	private Class<T> entityType;
	
	private String transactionId;

	private Operation operation;
	
	private String tableName;
	
	private Map<String, Object> primaryKey;

	private Map<String, Object> newState;
	
	private Map<String, Object> previousState;
	
	/**
	 * Default constructor for deserialization.
	 */
	public CDCEvent() {
	}
	
	public CDCEvent(Class<T> entityType) {
		this.entityType = entityType;
	}

	public CDCEvent(Class<T> entityType, Set<String> tags) {
		super(tags);
		this.entityType = entityType;
	}

	public void setPrimaryKey(Map<String, Object> primaryKey) {
		this.primaryKey = primaryKey;
	}

	public Map<String, Object> getPrimaryKey() {
		return primaryKey;
	}
	
	public boolean isCompositePrimaryKey() {
		return primaryKey != null && primaryKey.size() > 1;
	}

	public void setEntityType(Class<T> entityType) {
		this.entityType = entityType;
	}

	public Class<T> getEntityType() {
		return entityType;
	}

	public void setTransactionId(String transactionId) {
		this.transactionId = transactionId;
	}

	public String getTransactionId() {
		return transactionId;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public String getTableName() {
		return tableName;
	}

	public void setOperation(Operation operation) {
		this.operation = operation;
	}

	public Operation getOperation() {
		return operation;
	}

	/**
	 * Indicates that the event is an initial data synchronization.
	 * 
	 * @return true, first or last for snapshot event, false or null otherwise
	 */
	public String getSnapshot() {
		return snapshot;
	}

	public void setSnapshot(String snapshot) {
		this.snapshot = snapshot;
	}

	public Map<String, Object> getNewState() {
		return newState;
	}

	public void setNewState(Map<String, Object> newState) {
		this.newState = newState;
	}

	public Map<String, Object> getPreviousState() {
		return previousState;
	}

	public void setPreviousState(Map<String, Object> previousState) {
		this.previousState = previousState;
	}
	
	public enum Operation {
		READ, CREATE, UPDATE, DELETE, TRUNCATE 
	}

	@JsonIgnore
	@Override
	public @Nullable ResolvableType getResolvableType() {
		if (entityType != null && getClass().getTypeParameters().length == 1) {
			return ResolvableType.forClassWithGenerics(getClass(), ResolvableType.forClass(entityType));
		} else {
			return ResolvableType.forClass(getClass());
		}
	}
}
