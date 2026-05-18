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

import java.io.Serializable;

/**
 * A marker interface for OpenMRS task requests.
 * <p>
 * Application code should implement this interface to define job data.
 * <p>
 * {@link TaskData} is serialized to json with Jackson upon scheduling a job and deserialized when
 * the job is executed by the corresponding {@link TaskHandler}.
 * <p>
 * {@link org.openmrs.serialization.JacksonConfig#schedulerObjectMapper()} is used.
 * <p>
 * Please make sure that task data is as small as possible e.g. do not store domain objects rather
 * store their ids or uuids or do not store file content, rather use
 * {@link org.openmrs.api.StorageService} and store paths.
 * <p>
 * This interface hides the underlying JobRunr JobRequest dependency.
 *
 * @since 2.9.x
 */
public interface TaskData extends Serializable {}
