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

import java.util.HashSet;

import org.junit.jupiter.api.Test;
import org.openmrs.Privilege;
import org.openmrs.Role;
import org.openmrs.util.RoleConstants;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for the pure role-flattening logic in {@link RolePrivilegeCache}, which does not
 * require a Spring context.
 */
public class RolePrivilegeCacheTest {

	@Test
	public void computeRolePrivileges_shouldCollectDirectPrivileges() {
		Role role = new Role("Clerk");
		role.addPrivilege(new Privilege("View Patients"));
		role.addPrivilege(new Privilege("Edit Patients"));

		RolePrivileges result = RolePrivilegeCache.computeRolePrivileges(role);

		assertTrue(result.containsPrivilege("View Patients"));
		assertTrue(result.containsPrivilege("Edit Patients"));
		assertFalse(result.containsPrivilege("Delete Patients"));
		assertFalse(result.grantsSuperuser());
	}

	@Test
	public void computeRolePrivileges_shouldMatchPrivilegesCaseInsensitively() {
		Role role = new Role("Clerk");
		role.addPrivilege(new Privilege("View Patients"));

		RolePrivileges result = RolePrivilegeCache.computeRolePrivileges(role);

		assertTrue(result.containsPrivilege("VIEW PATIENTS"));
		assertTrue(result.containsPrivilege("view patients"));
	}

	@Test
	public void computeRolePrivileges_shouldIncludeInheritedPrivileges() {
		Role parent = new Role("Parent");
		parent.addPrivilege(new Privilege("Parent Privilege"));

		Role child = new Role("Child");
		child.addPrivilege(new Privilege("Child Privilege"));
		child.setInheritedRoles(new HashSet<>(java.util.Collections.singletonList(parent)));

		RolePrivileges result = RolePrivilegeCache.computeRolePrivileges(child);

		assertTrue(result.containsPrivilege("Child Privilege"));
		assertTrue(result.containsPrivilege("Parent Privilege"));
	}

	@Test
	public void computeRolePrivileges_shouldFlattenMultipleLevelsOfInheritance() {
		Role grandparent = new Role("Grandparent");
		grandparent.addPrivilege(new Privilege("Grandparent Privilege"));

		Role parent = new Role("Parent");
		parent.addPrivilege(new Privilege("Parent Privilege"));
		parent.setInheritedRoles(new HashSet<>(java.util.Collections.singletonList(grandparent)));

		Role child = new Role("Child");
		child.setInheritedRoles(new HashSet<>(java.util.Collections.singletonList(parent)));

		RolePrivileges result = RolePrivilegeCache.computeRolePrivileges(child);

		assertTrue(result.containsPrivilege("Grandparent Privilege"));
		assertTrue(result.containsPrivilege("Parent Privilege"));
	}

	@Test
	public void computeRolePrivileges_shouldGrantSuperuserWhenRoleIsSuperuser() {
		Role role = new Role(RoleConstants.SUPERUSER);

		RolePrivileges result = RolePrivilegeCache.computeRolePrivileges(role);

		assertTrue(result.grantsSuperuser());
	}

	@Test
	public void computeRolePrivileges_shouldInheritSuperuserStatus() {
		Role superuser = new Role(RoleConstants.SUPERUSER);

		Role role = new Role("Delegated Admin");
		role.setInheritedRoles(new HashSet<>(java.util.Collections.singletonList(superuser)));

		RolePrivileges result = RolePrivilegeCache.computeRolePrivileges(role);

		assertTrue(result.grantsSuperuser());
	}

	@Test
	public void computeRolePrivileges_shouldTerminateOnInheritanceCycle() {
		Role a = new Role("A");
		a.addPrivilege(new Privilege("Privilege A"));
		Role b = new Role("B");
		b.addPrivilege(new Privilege("Privilege B"));

		// A inherits B and B inherits A, forming a cycle
		a.setInheritedRoles(new HashSet<>(java.util.Collections.singletonList(b)));
		b.setInheritedRoles(new HashSet<>(java.util.Collections.singletonList(a)));

		RolePrivileges result = RolePrivilegeCache.computeRolePrivileges(a);

		assertTrue(result.containsPrivilege("Privilege A"));
		assertTrue(result.containsPrivilege("Privilege B"));
		assertFalse(result.grantsSuperuser());
	}

	@Test
	public void computeRolePrivileges_shouldTerminateWhenRoleInheritsItself() {
		Role role = new Role("Self");
		role.addPrivilege(new Privilege("Self Privilege"));
		role.setInheritedRoles(new HashSet<>(java.util.Collections.singletonList(role)));

		RolePrivileges result = RolePrivilegeCache.computeRolePrivileges(role);

		assertTrue(result.containsPrivilege("Self Privilege"));
	}
}
