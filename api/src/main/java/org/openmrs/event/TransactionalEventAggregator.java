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

import org.openmrs.api.context.Context;
import org.springframework.context.event.EventListener;
import org.springframework.core.Ordered;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Extend to aggregate events for specific event types that happen in a single transaction.
 * Please note that if transaction is not active, events are not aggregated.
 * <p>
 * It may be used to create a single event for a number of entity changes.
 * <p>
 * It is recommended for each aggregator to produce a custom type extending {@link AggregatedEntityEvent}.
 * <p>
 * The event can be listened to with {@link org.springframework.context.event.EventListener}, 
 * {@link TransactionalEventListener} (<b>except for <i>TransactionPhase.BEFORE_COMMIT</i></b>) 
 * or {@link org.openmrs.event.outbox.OutboxEventListener}.
 * <p>
 * Since this event is emitted in the BEFORE_COMMIT phase, any <i>@EventListener</i> for this event is executed in 
 * the BEFORE_COMMIT phase as well. 
 * <p>
 * <b>It is not supported to use <i>@TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)</i> 
 * to listen for an aggregated event, use <i>@EventListener</i> instead.</b>
 * 
 * @since 2.9.0
 */
public abstract class TransactionalEventAggregator {

	private final EventPublisher eventPublisher;
	
	public TransactionalEventAggregator(EventPublisher eventPublisher) {
		this.eventPublisher = eventPublisher;
	}

	/**
	 * It must be very fast as it is passed all EntityEvents.
	 * 
	 * @param event the event
	 * @return true if event should be aggregated
	 */
	public abstract boolean supportsEvent(EntityEvent<?> event);

	/**
	 * Returns a concrete instance of {@link AggregatedEntityEvent} to be published for listeners.
	 * 
	 * @param events the aggregated events
	 * @return a new aggregated event
	 */
	public abstract AggregatedEntityEvent newAggregatedEntityEvent(List<EntityEvent<?>> events);
	
	/**
	 * Adds event to a group associated with a current transaction.
	 * 
	 * @param event the event
	 */
	@EventListener
	public void aggregateEvent(EntityEvent<?> event) {
		if (!TransactionSynchronizationManager.isSynchronizationActive() || !supportsEvent(event)) {
			return;
		}
		
		// Get or create the list of events for this transaction.
		@SuppressWarnings("unchecked") 
		List<EntityEvent<?>> events = (List<EntityEvent<?>>) TransactionSynchronizationManager.getResource(this);
		if (events == null) {
			events = new ArrayList<>();
			TransactionSynchronizationManager.bindResource(this, events);
			registerTransactionSynchronization();
		}
		events.add(event);
	}

	private void registerTransactionSynchronization() {
		// Register a one-time synchronization to flush the events at the end of the commit phase.
		TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {

			@Override
			public int getOrder() {
				return Ordered.HIGHEST_PRECEDENCE; // Run before any other TransactionalEventListeners
			}

			@Override
			public void beforeCommit(boolean readOnly) {
				Context.flushSession(); // Make sure that all DB events are published before this event.
				@SuppressWarnings("unchecked") 
				List<EntityEvent<?>> events = (List<EntityEvent<?>>) TransactionSynchronizationManager.getResource(TransactionalEventAggregator.this);
				if (events != null && !events.isEmpty()) {
					eventPublisher.publishEvent(newAggregatedEntityEvent(new ArrayList<>(events)));
					events.clear();
				}
			}

			@Override
			public void afterCompletion(int status) {
				// Clean up the resource regardless of commit or rollback.
				TransactionSynchronizationManager.unbindResourceIfPossible(TransactionalEventAggregator.this);
			}
		});
	}
}
