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

/**
 * Handles execution of a {@link TaskData}. Application code should implement this interface to
 * define job logic.
 * <p>
 * Do not store any state in the handler instance as it is shared between all task requests. It may
 * also run on different replicas so that each {@link TaskData} may be handled by a different
 * instance.
 * <p>
 * You may use {@link TaskContext#getMetadata()} to keep any state related to this task request.
 *
 * @param <T> the type of the request this handler processes
 * @since 2.9.x
 */
public interface TaskHandler<T extends TaskData> {

	/**
	 * It is called when a corresponding {@link TaskData} is to be processed.
	 * <p>
	 * If an error occurs, it is logged and the task is retried automatically at most 3 times with
	 * exponential backoff by default.
	 * <p>
	 * In order to prevent retries, the task must throw
	 * <code>throw new TaskException("message", true)</code>
	 *
	 * @param taskData task data
	 * @param taskContext task context
	 * @throws Exception if an error occurs
	 */
	void execute(T taskData, TaskContext taskContext) throws Exception;
}
