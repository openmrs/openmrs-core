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

import org.openmrs.BaseOpenmrsData;

/**
 * In OpenMRS, we distinguish between data and metadata within our data model. Data (as opposed to
 * metadata) generally represent person- or patient-specific data. This provides an interface for
 * standard DAO operations on data.
 * 
 * @since 1.10
 */
public interface OpenmrsDataDAO<T extends BaseOpenmrsData> extends OpenmrsObjectDAO<T> {
	
	/**
	 * Return a list of persistents (optionally voided)
	 * 
	 * @param includeVoided if true voided persistents are also returned
	 * @return a list of persistents of the given class
	 */
	List<T> getAll(boolean includeVoided);
	
	/**
	 * Returns total number of persistents (optionally voided)
	 * @param includeVoided
	 * @return
	 */
	int getAllCount(boolean includeVoided);
	
	/**
	 * Return a lists of persistents optionally voided, with paging
	 * @param includeVoided
	 * @param firstResult
	 * @param maxResults
	 * @return
	 */
	List<T> getAll(boolean includeVoided, Integer firstResult, Integer maxResults);
	
}
