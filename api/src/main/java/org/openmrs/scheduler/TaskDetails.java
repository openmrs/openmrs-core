/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.scheduler;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

/**
 * Defines task details with its metadata.
 * <p>
 * Hides the underlying JobRunr Job dependency.
 * <p>
 * It is a one-off task details. One-off tasks are also created from {@link RecurringTaskDetails}
 * when the time comes.
 *
 * @since 2.9.x
 */
public interface TaskDetails {

	String getUuid();

	Optional<String> getRecurringTaskUuid();

	String getName();

	TaskState getState();

	Optional<Instant> getScheduledAt();

	Instant getCreatedAt();

	Instant getUpdatedAt();

	String getSignature();

	Map<String, Object> getMetadata();
}
