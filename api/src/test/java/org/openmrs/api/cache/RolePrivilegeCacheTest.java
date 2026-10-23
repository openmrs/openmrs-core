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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.openmrs.api.db.StoredRole;
import org.openmrs.util.RoleConstants;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for the pure role-flattening logic in {@link RolePrivilegeCache}, which does not
 * require a Spring context.
 */
public class RolePrivilegeCacheTest {

	private final Map<String, StoredRole> stored = new HashMap<>();

	private void store(String name, Set<String> privileges, Set<String> parents) {
		stored.put(name, new StoredRole(name, privileges, parents));
	}

	private RolePrivileges resolve(String roleName) {
		return RolePrivilegeCache.resolve(roleName, stored::get);
	}

	@Test
	public void resolve_shouldCollectDirectPrivileges() {
		store("Clerk", Set.of("View Patients", "Edit Patients"), Set.of());

		RolePrivileges result = resolve("Clerk");

		assertTrue(result.containsPrivilege("View Patients"));
		assertTrue(result.containsPrivilege("Edit Patients"));
		assertFalse(result.containsPrivilege("Delete Patients"));
		assertFalse(result.grantsSuperuser());
	}

	@Test
	public void resolve_shouldMatchPrivilegesCaseInsensitively() {
		store("Clerk", Set.of("View Patients"), Set.of());

		RolePrivileges result = resolve("Clerk");

		assertTrue(result.containsPrivilege("VIEW PATIENTS"));
		assertTrue(result.containsPrivilege("view patients"));
	}

	@Test
	public void resolve_shouldIncludeInheritedPrivileges() {
		store("Parent", Set.of("Parent Privilege"), Set.of());
		store("Child", Set.of("Child Privilege"), Set.of("Parent"));

		RolePrivileges result = resolve("Child");

		assertTrue(result.containsPrivilege("Child Privilege"));
		assertTrue(result.containsPrivilege("Parent Privilege"));
	}

	@Test
	public void resolve_shouldFlattenMultipleLevelsOfInheritance() {
		store("Grandparent", Set.of("Grandparent Privilege"), Set.of());
		store("Parent", Set.of("Parent Privilege"), Set.of("Grandparent"));
		store("Child", Set.of(), Set.of("Parent"));

		RolePrivileges result = resolve("Child");

		assertTrue(result.containsPrivilege("Grandparent Privilege"));
		assertTrue(result.containsPrivilege("Parent Privilege"));
		assertTrue(result.includesRole("Grandparent"));
	}

	@Test
	public void resolve_shouldGrantSuperuserWhenRoleIsSuperuser() {
		store(RoleConstants.SUPERUSER, Set.of(), Set.of());

		assertTrue(resolve(RoleConstants.SUPERUSER).grantsSuperuser());
	}

	@Test
	public void resolve_shouldInheritSuperuserStatus() {
		store(RoleConstants.SUPERUSER, Set.of(), Set.of());
		store("Delegated Admin", Set.of(), Set.of(RoleConstants.SUPERUSER));

		assertTrue(resolve("Delegated Admin").grantsSuperuser());
	}

	@Test
	public void resolve_shouldTerminateOnInheritanceCycle() {
		// A inherits B and B inherits A, forming a cycle
		store("A", Set.of("Privilege A"), Set.of("B"));
		store("B", Set.of("Privilege B"), Set.of("A"));

		RolePrivileges result = resolve("A");

		assertTrue(result.containsPrivilege("Privilege A"));
		assertTrue(result.containsPrivilege("Privilege B"));
		assertFalse(result.grantsSuperuser());
	}

	@Test
	public void resolve_shouldTerminateWhenRoleInheritsItself() {
		store("Self", Set.of("Self Privilege"), Set.of("Self"));

		assertTrue(resolve("Self").containsPrivilege("Self Privilege"));
	}

	@Test
	public void resolve_shouldGrantNothingForARoleThatIsNotStored() {
		RolePrivileges result = resolve("Missing");

		assertFalse(result.containsPrivilege("View Patients"));
		assertFalse(result.grantsSuperuser());
		assertFalse(result.includesRole("Missing"));
	}
}
