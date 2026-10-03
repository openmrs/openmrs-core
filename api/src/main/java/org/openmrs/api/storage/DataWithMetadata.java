/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api.storage;

import java.io.IOException;
import java.io.InputStream;

/**
 * Holds data and metadata returned together from a single storage read.
 * <p>
 * The data stream must be closed by the caller (or via {@link #close()}) to release the underlying
 * resource.
 *
 * @since 3.0.0
 */
public class DataWithMetadata implements AutoCloseable {

	private final InputStream data;

	private final ObjectMetadata metadata;

	public DataWithMetadata(InputStream data, ObjectMetadata metadata) {
		this.data = data;
		this.metadata = metadata;
	}

	public InputStream data() {
		return data;
	}

	public ObjectMetadata metadata() {
		return metadata;
	}

	@Override
	public void close() throws IOException {
		data.close();
	}
}
