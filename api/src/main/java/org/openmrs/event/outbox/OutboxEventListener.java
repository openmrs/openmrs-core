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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * It is used for the transactional outbox events.
 * <p>
 * If an event has a listener marked with this interface, the event is automatically put in the
 * outbox table within the same transaction that the event happened. This way it can be persisted
 * and delivered asynchronously with the DB transactional guarantee.
 * <p>
 * An outbox event is asynchronous by its nature. Outbox events are processed by
 * {@link org.openmrs.event.outbox.tasks.OutboxPollingTaskHandler}. By default, the poller runs
 * every 15s, but it may take longer for the event to be processed depending on the outbox events
 * queue length.
 * <p>
 * Outbox events are communicated to listeners in order they were published and according to
 * listeners {@code @Order} ( {@link org.springframework.core.annotation.Order}) annotation.
 * <p>
 * If any listener for the specific event fails, then the event is considered as failed and delivery
 * will be retried for failed listeners.
 * <p>
 * Please note that event listeners should complete as soon as possible (ideally under a second) so
 * that events do not queue up in the system.
 * <p>
 * If you need longer processing, you need to schedule an asynchronous task from your listener. See
 * {@link org.openmrs.scheduler.SchedulerService}.
 *
 * @since 2.9.x
 */
@Target({ ElementType.METHOD, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface OutboxEventListener {

}
