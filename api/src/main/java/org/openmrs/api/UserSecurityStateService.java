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

import org.openmrs.User;
import org.openmrs.annotation.Logging;
import org.openmrs.util.PrivilegeConstants;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 * Per-user store for security state: failed login counts, lockout timestamps, second-factor
 * secrets, remember-me tokens and the like. Unlike user properties, entries are not part of
 * {@link User}, are not returned with it (through REST or otherwise), are always read from the
 * database rather than from a cached user, and can be changed atomically. Every method requires the
 * {@link PrivilegeConstants#MANAGE_USER_SECURITY_STATE} privilege, including for the caller's own
 * entries; modules call it under a proxy privilege or from a daemon thread.
 * <p>
 * Keys are namespaced by their owner, e.g. {@code core.loginAttempts} or
 * {@code authentication.totpSecret}, and are at most 255 characters long. Values are at most 4000
 * characters long and never blank: the absence of an entry is the only representation of "no
 * state". Every method throws {@link IllegalArgumentException} for a user without an id, a blank or
 * over-long key, or a blank or over-long value.
 *
 * @since 3.0.0
 */
public interface UserSecurityStateService extends OpenmrsService {

	/**
	 * @param user the user whose entry to read
	 * @param property the namespaced key of the entry
	 * @return the stored value, or null when the user has no such entry <strong>Should</strong> read
	 *         the value from the database <strong>Should</strong> return null when the user has no such
	 *         entry <strong>Should</strong> read the callers own entry under a proxy privilege
	 *         <strong>Should</strong> fail without the manage user security state privilege
	 */
	@PreAuthorize("hasAuthority('" + PrivilegeConstants.MANAGE_USER_SECURITY_STATE + "')")
	String getValue(User user, String property);

	/**
	 * Creates or replaces an entry.
	 *
	 * @param user the user whose entry to set
	 * @param property the namespaced key of the entry
	 * @param value the value to store <strong>Should</strong> create the entry when it does not exist
	 *            <strong>Should</strong> replace the value of an existing entry <strong>Should</strong>
	 *            not store the entry as a user property <strong>Should</strong> fail for a blank value
	 *            <strong>Should</strong> fail for a value longer than 4000 characters
	 *            <strong>Should</strong> fail for a blank property <strong>Should</strong> fail for an
	 *            unsaved user <strong>Should</strong> fail without the manage user security state
	 *            privilege
	 */
	@PreAuthorize("hasAuthority('" + PrivilegeConstants.MANAGE_USER_SECURITY_STATE + "')")
	@Logging(ignoredArgumentIndexes = { 2 })
	void setValue(User user, String property, String value);

	/**
	 * Replaces an entry's value only if its current value is exactly {@code expectedValue}. Passing
	 * null as {@code expectedValue} means the entry must not exist yet, so the call creates it. The
	 * check and the change happen atomically: of several concurrent calls with the same expected value
	 * and a different new value, exactly one succeeds.
	 *
	 * @param user the user whose entry to change
	 * @param property the namespaced key of the entry
	 * @param expectedValue the value the entry must currently have, or null for "no entry"
	 * @param newValue the value to store
	 * @return true if the entry was changed, false if its current value was not the expected one
	 *         <strong>Should</strong> create the entry when expected value is null and the entry does
	 *         not exist <strong>Should</strong> not create the entry when expected value is null and
	 *         the entry exists <strong>Should</strong> replace the value when the current value matches
	 *         <strong>Should</strong> not replace the value when the current value does not match
	 *         <strong>Should</strong> fail without the manage user security state privilege
	 */
	@PreAuthorize("hasAuthority('" + PrivilegeConstants.MANAGE_USER_SECURITY_STATE + "')")
	@Logging(ignoredArgumentIndexes = { 2, 3 })
	boolean compareAndSet(User user, String property, String expectedValue, String newValue);

	/**
	 * Removes an entry only if its current value is exactly {@code expectedValue}. The check and the
	 * removal happen atomically: of several concurrent calls for the same entry, exactly one returns
	 * true.
	 *
	 * @param user the user whose entry to remove
	 * @param property the namespaced key of the entry
	 * @param expectedValue the value the entry must currently have, never blank
	 * @return true if an entry was removed <strong>Should</strong> remove the entry when the current
	 *         value matches <strong>Should</strong> not remove the entry when the current value does
	 *         not match <strong>Should</strong> fail for a blank expected value <strong>Should</strong>
	 *         fail without the manage user security state privilege
	 */
	@PreAuthorize("hasAuthority('" + PrivilegeConstants.MANAGE_USER_SECURITY_STATE + "')")
	@Logging(ignoredArgumentIndexes = { 2 })
	boolean deleteIfValueMatches(User user, String property, String expectedValue);

	/**
	 * Removes an entry whatever its value.
	 *
	 * @param user the user whose entry to remove
	 * @param property the namespaced key of the entry
	 * @return true if an entry was removed <strong>Should</strong> remove the entry
	 *         <strong>Should</strong> return false when the user has no such entry
	 *         <strong>Should</strong> fail without the manage user security state privilege
	 */
	@PreAuthorize("hasAuthority('" + PrivilegeConstants.MANAGE_USER_SECURITY_STATE + "')")
	boolean delete(User user, String property);

	/**
	 * Adds one to an entry's numeric value, creating the entry with the value 1 when it does not exist
	 * yet. Concurrent increments of the same entry are never lost.
	 *
	 * @param user the user whose entry to increment
	 * @param property the namespaced key of the entry
	 * @return the value after incrementing
	 * @throws org.openmrs.api.db.DAOException if the current value is not a number
	 *             <strong>Should</strong> create the entry with the value one <strong>Should</strong>
	 *             add one to the current value <strong>Should</strong> fail when the current value is
	 *             not a number <strong>Should</strong> fail without the manage user security state
	 *             privilege
	 */
	@PreAuthorize("hasAuthority('" + PrivilegeConstants.MANAGE_USER_SECURITY_STATE + "')")
	long increment(User user, String property);
}
