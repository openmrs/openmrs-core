/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api.db;

import java.util.List;

import org.openmrs.BaseOpenmrsMetadata;

/**
 * In OpenMRS, we distinguish between data and metadata within our data model. Data (as opposed to
 * metadata) generally represent person- or patient-specific data. This provides an interface for
 * standard DAO operations on metadata.
 * 
 * @since 1.10
 */
public interface OpenmrsMetadataDAO<T extends BaseOpenmrsMetadata> extends OpenmrsObjectDAO<T> {
	
	/**
	 * Return a list of metadata objects (optionally retired)
	 * 
	 * @param includeRetired if true retired metadata is also returned
	 * @return a list of all metadata objects of the given class
	 */
	List<T> getAll(boolean includeRetired);
	
	/**
	 * Returns total number of persistents (optionally retired)
	 * @param includeRetired
	 * @return total number of persistens
	 */
	int getAllCount(boolean includeRetired);
	
	/**
	 * Return a lists of metadata objects optionally retired, with paging
	 * @param includeRetired
	 * @param firstResult
	 * @param maxResults
	 * @return list of metadata object
	 */
	List<T> getAll(boolean includeRetired, Integer firstResult, Integer maxResults);
	
}
