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
 * Database access for the per-user security state kept in the user_security_state table. Every
 * method reads and writes the database directly; nothing is served from a cached
 * {@link org.openmrs.User}.
 *
 * @see org.openmrs.api.UserSecurityStateService
 * @since 3.0.0, 2.9.0
 */
public interface UserSecurityStateDAO {

	/**
	 * @param userId the id of the user
	 * @param property the key of the entry
	 * @return the stored value, or null when the user has no such entry
	 */
	String getValue(Integer userId, String property);

	/**
	 * Creates or replaces the entry.
	 *
	 * @param userId the id of the user
	 * @param property the key of the entry
	 * @param value the value to store, never null
	 */
	void setValue(Integer userId, String property, String value);

	/**
	 * Replaces the entry's value only if its current value is exactly the expected one. A null
	 * {@code expectedValue} means the entry must not exist yet, so the call creates it.
	 *
	 * @param userId the id of the user
	 * @param property the key of the entry
	 * @param expectedValue the value the entry must currently have, or null for "no entry"
	 * @param newValue the value to store, never null
	 * @return true if the entry was changed, false if its current value was not the expected one
	 */
	boolean compareAndSet(Integer userId, String property, String expectedValue, String newValue);

	/**
	 * Removes the entry only if its current value is exactly the expected one.
	 *
	 * @param userId the id of the user
	 * @param property the key of the entry
	 * @param expectedValue the value the entry must currently have, never null
	 * @return true if an entry was removed
	 */
	boolean deleteIfValueMatches(Integer userId, String property, String expectedValue);

	/**
	 * Removes the entry whatever its value.
	 *
	 * @param userId the id of the user
	 * @param property the key of the entry
	 * @return true if an entry was removed
	 */
	boolean delete(Integer userId, String property);

	/**
	 * Adds one to the entry's numeric value, creating the entry with the value 1 when it does not exist
	 * yet.
	 *
	 * @param userId the id of the user
	 * @param property the key of the entry
	 * @return the value after incrementing
	 * @throws DAOException if the current value is not a number
	 */
	long increment(Integer userId, String property);

	/**
	 * Removes every entry of the user.
	 *
	 * @param userId the id of the user
	 */
	void deleteAll(Integer userId);
}
