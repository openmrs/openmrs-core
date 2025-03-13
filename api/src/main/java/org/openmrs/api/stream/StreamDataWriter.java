/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api.stream;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Used by {@link StreamDataService} to convert OutputStream to InputStream without
 * loading all data in memory. 
 * <p>
 * The write operation may be executed in a separate thread and is typically implemented as a lambda. 
 * Please take extra care and avoid modifying references used in the write operation as they are accessed by 
 * different threads.
 * 
 * @since 2.8.0, 2.7.4, 2.6.16, 2.5.15
 */
@FunctionalInterface
public interface StreamDataWriter {
	void write(OutputStream outputStream) throws IOException;
}
