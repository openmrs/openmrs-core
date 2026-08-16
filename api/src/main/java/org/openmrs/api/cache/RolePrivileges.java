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
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

/**
 * Immutable, pre-computed view of everything a single role grants: the flattened set of privilege
 * names of the role and all of its transitively inherited roles, plus whether that closure confers
 * superuser status. It is the cached value used by {@link RolePrivilegeCache} to answer privilege
 * checks without re-expanding the role graph on every call.
 * <p>
 * Privilege names are stored case-normalized because privilege comparison in OpenMRS is
 * case-insensitive (see {@link org.openmrs.Role#hasPrivilege(String)}). Lookups normalize the same
 * way, so {@link #containsPrivilege(String)} preserves that behavior.
 *
 * @since 3.0.0, 2.9.0, 2.8.9
 */
public final class RolePrivileges implements Serializable {

	private static final long serialVersionUID = 1L;

	private final Set<String> privilegeNames;

	private final boolean grantsSuperuser;

	/**
	 * @param privilegeNames privilege names granted by the role and its inherited closure;
	 *            case-normalized as copied, null elements ignored
	 * @param grantsSuperuser whether the role or any inherited role confers superuser status
	 */
	public RolePrivileges(Set<String> privilegeNames, boolean grantsSuperuser) {
		Objects.requireNonNull(privilegeNames, "privilegeNames must not be null");
		Set<String> normalized = new HashSet<>();
		for (String privilegeName : privilegeNames) {
			if (privilegeName != null) {
				normalized.add(normalize(privilegeName));
			}
		}
		this.privilegeNames = Collections.unmodifiableSet(normalized);
		this.grantsSuperuser = grantsSuperuser;
	}

	/**
	 * @return true if this role's closure confers superuser status
	 */
	public boolean grantsSuperuser() {
		return grantsSuperuser;
	}

	/**
	 * @param privilege the privilege name to test (compared case-insensitively)
	 * @return true if the role's closure grants the given privilege
	 */
	public boolean containsPrivilege(String privilege) {
		return privilege != null && privilegeNames.contains(normalize(privilege));
	}

	/**
	 * @return an unmodifiable view of the case-normalized privilege names in this closure
	 */
	public Set<String> getPrivilegeNames() {
		return privilegeNames;
	}

	/**
	 * Case-normalizes a privilege name for storage and lookup. Uses {@link Locale#ROOT} so the
	 * normalization is stable across server locales.
	 *
	 * @param privilege the privilege name to normalize; must not be null
	 * @return the lower-cased privilege name
	 * @throws NullPointerException if <code>privilege</code> is null
	 */
	public static String normalize(String privilege) {
		return privilege.toLowerCase(Locale.ROOT);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof RolePrivileges)) {
			return false;
		}
		RolePrivileges other = (RolePrivileges) o;
		return grantsSuperuser == other.grantsSuperuser && privilegeNames.equals(other.privilegeNames);
	}

	@Override
	public int hashCode() {
		return Objects.hash(privilegeNames, grantsSuperuser);
	}
}
