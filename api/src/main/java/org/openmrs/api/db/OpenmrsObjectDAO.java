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

import java.io.Serializable;

import org.openmrs.BaseOpenmrsObject;

/**
 * Generic interface for all OpenMrs DAOs
 * @since 1.10
 *
 */
public interface OpenmrsObjectDAO<T extends BaseOpenmrsObject> {
	
	/**
	 * Obtains an object matching a given identifier
	 * 
	 * @param id the metadata identifier
	 * @return the matching metadata object
	 */
	T getById(Serializable id);
	
	/**
	 * Obtains an object matching a given UUID
	 * 
	 * @param uuid
	 * @return the matching metadata object
	 */
	T getByUuid(String uuid);
	
	/**
	 * Completely deletes a persistent from the database
	 * 
	 * @param persistent
	 *            The persistent to delete
	 */
	void delete(T persistent);
	
	/**
	 * Save or update a persistent in the database
	 * 
	 * @param persistent
	 *            The persistent to save or update
	 * @return the persistent that was saved or updated
	 */
	T saveOrUpdate(T newOrPersisted);
	
}
