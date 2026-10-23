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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * A role as stored in the database: its name, the privileges it grants directly, and the names of
 * the roles it directly inherits from. Unlike a loaded {@link org.openmrs.Role}, it can't be
 * changed, so it can't carry in-memory edits into a privilege check.
 *
 * @see UserDAO#getStoredRole(String)
 * @since 2.8.11
 */
public final class StoredRole {

	private final String name;

	private final Set<String> privileges;

	private final Set<String> parentRoles;

	public StoredRole(String name, Set<String> privileges, Set<String> parentRoles) {
		this.name = name;
		this.privileges = Collections.unmodifiableSet(new HashSet<>(privileges));
		this.parentRoles = Collections.unmodifiableSet(new HashSet<>(parentRoles));
	}

	/** @return the role's name as stored */
	public String getName() {
		return name;
	}

	/** @return the names of the privileges the role grants directly */
	public Set<String> getPrivileges() {
		return privileges;
	}

	/** @return the names of the roles the role directly inherits from */
	public Set<String> getParentRoles() {
		return parentRoles;
	}
}
