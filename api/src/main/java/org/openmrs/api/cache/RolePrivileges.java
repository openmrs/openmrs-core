/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api.cache;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Immutable, cacheable snapshot of the fully-flattened privileges granted by a single role.
 * <p>
 * One instance represents "the answer for one role": every privilege that role grants (including
 * everything it inherits), plus whether the role grants superuser status. It is computed once by
 * {@link RolePrivilegeCache} and stored in the {@code rolePrivileges} cache, so privilege checks
 * can read a pre-computed result instead of re-walking the role inheritance tree on every call.
 * <p>
 * Privilege names are stored already lowercased (see {@link RolePrivilegeCache}) so that
 * case-insensitive matching becomes a fast {@link Set} lookup. The object is immutable and
 * {@link Serializable} so it is safe to share across threads and move between cluster nodes.
 *
 * @since 2.8.0
 */
public final class RolePrivileges implements Serializable {

	private static final long serialVersionUID = 1L;

	private final Set<String> privilegeNames;

	private final boolean grantsSuperuser;

	/**
	 * @param privilegeNames the role's flattened privilege names, already lowercased
	 * @param grantsSuperuser whether this role grants superuser status (directly or via inheritance)
	 */
	public RolePrivileges(Set<String> privilegeNames, boolean grantsSuperuser) {
		this.privilegeNames = Collections.unmodifiableSet(new HashSet<>(privilegeNames));
		this.grantsSuperuser = grantsSuperuser;
	}

	/**
	 * @return true if this role grants superuser status, in which case every privilege check passes
	 */
	public boolean grantsSuperuser() {
		return grantsSuperuser;
	}

	/**
	 * @param alreadyLowercased the privilege name to look for, already lowercased by the caller
	 * @return true if this role's flattened privilege set contains the given privilege
	 */
	public boolean containsPrivilege(String alreadyLowercased) {
		return privilegeNames.contains(alreadyLowercased);
	}
}
