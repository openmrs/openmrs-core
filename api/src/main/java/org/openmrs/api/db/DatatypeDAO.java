/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
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
