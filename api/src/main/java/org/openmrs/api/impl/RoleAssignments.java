/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api.impl;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Function;

import org.openmrs.api.APIException;
import org.openmrs.api.cache.RolePrivilegeCache;
import org.openmrs.api.cache.RolePrivileges;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.Daemon;
import org.openmrs.api.db.StoredRole;
import org.openmrs.util.PrivilegeConstants;
import org.openmrs.util.RoleConstants;

/**
 * The rule for handing out roles, whether assigning them to a user or making a role inherit them:
 * each role must be stored under exactly the given name, the authenticated user must hold every
 * privilege the roles grant, including through inheritance, and
 * {@link PrivilegeConstants#ASSIGN_SYSTEM_DEVELOPER_ROLE} if any of them is or inherits
 * {@link RoleConstants#SUPERUSER}. Roles are read as stored, never from a loaded
 * {@link org.openmrs.Role}, which may hold unsaved changes.
 * <p>
 * Superuser status can only be taken away by someone who could hand it out: changing a user or a
 * role that, as stored, is or inherits {@link RoleConstants#SUPERUSER} requires
 * {@link PrivilegeConstants#ASSIGN_SYSTEM_DEVELOPER_ROLE} (see {@link #grantsSuperuser}).
 * <p>
 * Proxy privileges don't count: they let internal calls do work the user can't, but must never let
 * the user hand out more than they hold. Daemon threads hold every privilege.
 *
 * @since 2.8.11
 */
public final class RoleAssignments {

	private RoleAssignments() {
	}

	/**
	 * @param roleNames names of the roles being handed out
	 * @param reader reads a role as stored, returning null if there is no such role
	 * @return the privileges the roles grant that the authenticated user doesn't hold, sorted
	 * @throws APIException if a name isn't exactly the name of a stored role, or if the roles grant
	 *             System Developer and the user can't assign it
	 */
	public static SortedSet<String> missingPrivileges(Collection<String> roleNames, Function<String, StoredRole> reader) {
		SortedSet<String> missing = new TreeSet<>();
		if (roleNames == null || Daemon.isDaemonThread()) {
			return missing;
		}
		for (String roleName : roleNames) {
			if (roleName == null) {
				continue;
			}
			requireStored(roleName, reader);
			RolePrivileges granted = RolePrivilegeCache.resolve(roleName, reader);
			if (granted.grantsSuperuser() && !holds(PrivilegeConstants.ASSIGN_SYSTEM_DEVELOPER_ROLE)) {
				throw new APIException("User.you.must.have.role", new Object[] { RoleConstants.SUPERUSER });
			}
			granted.getPrivilegeNames().stream().filter(p -> !holds(p)).forEach(missing::add);
		}
		return missing;
	}

	/**
	 * Requires a role stored under exactly the given name. A name the database only matches loosely,
	 * for example with different case or trailing spaces, is refused, since privilege checks resolve
	 * roles by their exact name.
	 *
	 * @param roleName a role name
	 * @param reader reads a role as stored, returning null if there is no such role
	 * @return the stored role
	 * @throws APIException if there is no role stored under exactly that name
	 */
	public static StoredRole requireStored(String roleName, Function<String, StoredRole> reader) {
		StoredRole stored = reader.apply(roleName);
		if (stored == null || !stored.getName().equals(roleName)) {
			throw new APIException("Role.unknown", new Object[] { roleName });
		}
		return stored;
	}

	/**
	 * Requires that a role inheriting the given parents would not inherit itself, directly or through
	 * the parents' stored inheritance.
	 *
	 * @param roleName the name of the role, or null for a role not yet named
	 * @param parentNames names of the roles it is to inherit from
	 * @param reader reads a role as stored, returning null if there is no such role
	 * @throws APIException if the role would inherit itself
	 */
	public static void requireNoInheritanceCycle(String roleName, Collection<String> parentNames,
	        Function<String, StoredRole> reader) {
		if (roleName == null || parentNames == null) {
			return;
		}
		Set<String> visited = new HashSet<>();
		Deque<String> pending = new ArrayDeque<>(parentNames);
		while (!pending.isEmpty()) {
			String name = pending.pop();
			if (name == null || !visited.add(name)) {
				continue;
			}
			StoredRole stored = reader.apply(name);
			// case-insensitive, since the database may match a differently cased name to the same row
			if (name.equalsIgnoreCase(roleName) || (stored != null && stored.getName().equalsIgnoreCase(roleName))) {
				throw new APIException("Role.cannot.inherit.descendant", (Object[]) null);
			}
			if (stored != null) {
				pending.addAll(stored.getParentRoles());
			}
		}
	}

	/**
	 * @param roleNames names of roles as stored, such as a user's roles or a single role
	 * @param reader reads a role as stored, returning null if there is no such role; it must read what
	 *            is stored, never pending changes, since the result decides who may change the roles
	 * @return true if any of the roles is or inherits {@link RoleConstants#SUPERUSER}
	 */
	public static boolean grantsSuperuser(Collection<String> roleNames, Function<String, StoredRole> reader) {
		return roleNames != null && roleNames.stream().filter(Objects::nonNull)
		        .anyMatch(name -> RolePrivilegeCache.resolve(name, reader).grantsSuperuser());
	}

	/**
	 * Requires that the authenticated user may change a user or role holding the given roles: if any of
	 * them is or inherits {@link RoleConstants#SUPERUSER}, only a holder of
	 * {@link PrivilegeConstants#ASSIGN_SYSTEM_DEVELOPER_ROLE} may.
	 *
	 * @param messageKey the message for the exception, given the privilege and role names
	 * @param roleNames names of roles as stored, such as a user's roles or a single role
	 * @param reader reads a role as stored, as for {@link #grantsSuperuser}
	 * @throws APIException if the roles grant superuser status and the user can't assign it
	 */
	public static void requireCanChangeSuperuser(String messageKey, Collection<String> roleNames,
	        Function<String, StoredRole> reader) {
		if (!holds(PrivilegeConstants.ASSIGN_SYSTEM_DEVELOPER_ROLE) && grantsSuperuser(roleNames, reader)) {
			throw new APIException(messageKey,
			        new Object[] { PrivilegeConstants.ASSIGN_SYSTEM_DEVELOPER_ROLE, RoleConstants.SUPERUSER });
		}
	}

	/**
	 * @param privileges privilege names being handed out directly, such as a role's own privileges
	 * @return those the authenticated user doesn't hold, sorted
	 */
	public static SortedSet<String> notHeld(Collection<String> privileges) {
		SortedSet<String> missing = new TreeSet<>();
		if (privileges != null) {
			privileges.stream().filter(p -> !holds(p)).forEach(missing::add);
		}
		return missing;
	}

	/**
	 * @param privilege a privilege name
	 * @return true if the authenticated user holds the privilege through their roles, not a proxy
	 */
	public static boolean holds(String privilege) {
		return Context.hasPrivilege(privilege, false);
	}
}
