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

/**
 * Data access for custom datatypes
 * 
 * @since 1.9
 */
public interface DatatypeDAO {
	
	/**
	 * Gets a clob storage object by its id
	 * 
	 * @param id
	 * @return
	 */
	ClobDatatypeStorage getClobDatatypeStorage(Integer id);
	
	/**
	 * Gets a clob storage object by its uuid
	 * 
	 * @param uuid
	 * @return
	 */
	ClobDatatypeStorage getClobDatatypeStorageByUuid(String uuid);
	
	/**
	 * Creates or updates a clob storage object
	 * 
	 * @param storage
	 * @return the saved object
	 */
	ClobDatatypeStorage saveClobDatatypeStorage(ClobDatatypeStorage storage);
	
	/**
	 * Deletes a clob storage object from the database
	 * 
	 * @param storage the object to delete
	 */
	void deleteClobDatatypeStorage(ClobDatatypeStorage storage);
	
}
