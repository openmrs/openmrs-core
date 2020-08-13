/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.postgres;

import org.openmrs.postgres.db.PostgresDAO;

/**
 * Database specific methods related to PostgreSQL
 * <ul>
 * <li>Unlike MySQL which uses identifier strategy, PostgreSQL follows sequence strategy</li>
 * <li>So as to bridge the gap between these two strategies, this service has been created.</li>
 * <li>It will perform tasks like updating the sequence values after insertions are done from core
 * data or concepts are inserted (present in Reference Metadata Module)</li>
 * </ul>
 */
public interface PostgresService {
	
	/**
	 * Used by Spring to set the specific/chosen database access implementation
	 * 
	 * @param dao The dao implementation to use
	 */
	public void setPostgresDAO(PostgresDAO dao);
	
	/**
	 * Updates the PostgreSQL sequences to latest values. On other databases like MySQL it does
	 * nothing.
	 * 
	 * @since 2.4.0
	 */
	public void updateAllSequence();
	
}
