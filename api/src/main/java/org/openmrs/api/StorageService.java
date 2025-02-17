/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api;

import java.io.IOException;
import java.io.InputStream;
import java.util.stream.Stream;

/**
 * Storage service to persist data that does not fit well in DB.
 * <p>
 * Data objects are stored under generated or provided unique keys. Data and keys are immutable.
 * 
 * @since 2.8.0, 2.7.2, 2.6.16, 2.5.15
 */
public interface StorageService extends OpenmrsService {
	
	/**
	 * Get InputStream to read data for the given key.
	 *
	 * @param key unique key
	 * @return data
	 * @throws IOException wrong key or IO error
	 */
	InputStream getData(String key) throws IOException;
	
	/**
	 * Returns keys starting with the given prefix (the order depends on the implementation).
	 * <p>
	 * Remember to close the stream when done iterating.
	 *
	 * @param moduleId null for global or module id
	 * @param prefix key prefix
	 * @return stream of keys
	 * @throws IOException IO error
	 */
	Stream<String> getKeys(String moduleId, String prefix) throws IOException;
	
	/**
	 * Saves the given InputStream.
	 *
	 * @param inputStream data
	 * @param moduleId null for global or module id
	 * @return unique key
	 * @throws IOException IO error
	 */
	String saveData(InputStream inputStream, String moduleId) throws IOException;
	
	/**
	 * Saves the given InputStream as a temporary file. Temporary files are kept, if possible, in local
	 * storage for fast IO. They are available only for the duration of a request and for the single
	 * replica.
	 *
	 * @param inputStream data
	 * @return unique key
	 * @throws IOException IO error
	 */
	String saveTempData(InputStream inputStream) throws IOException;
	
	/**
	 * Saves the given InputStream under the given key suffix.
	 * 
	 * @param inputStream data
	 * @param moduleId null for global or module id
	 * @param keySuffix for the unique key
	 * @return unique key
	 * @throws IOException if key exists or IO error
	 */
	String saveData(InputStream inputStream, String moduleId, String keySuffix) throws IOException;
	
	/**
	 * Marks data for deletion. The key may be freed up after some period.
	 *
	 * @param key unique key
	 * @return true if marked for deletion
	 * @throws IOException wrong key or IO error
	 */
	boolean purgeData(String key) throws IOException;
	
	/**
	 * Returns true if object with the given key exists.
	 * 
	 * @param key unique key
	 * @return true if exists
	 */
	boolean exists(String key);
}
