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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for the {@link RolePrivileges} value type. Its {@code equals}/{@code hashCode} are
 * load-bearing: the clustered cache may store and return an equal-but-not-identical deserialized
 * value, and callers compare returned values rather than instances.
 */
public class RolePrivilegesTest {

	@Test
	public void containsPrivilege_shouldMatchCaseInsensitively() {
		RolePrivileges privileges = new RolePrivileges(Collections.singleton("View Patients"), false);

		assertTrue(privileges.containsPrivilege("View Patients"));
		assertTrue(privileges.containsPrivilege("VIEW PATIENTS"));
		assertTrue(privileges.containsPrivilege("view patients"));
		assertFalse(privileges.containsPrivilege("Edit Patients"));
	}

	@Test
	public void containsPrivilege_shouldReturnFalseForNull() {
		RolePrivileges privileges = new RolePrivileges(Collections.singleton("View Patients"), false);

		assertFalse(privileges.containsPrivilege(null));
	}

	@Test
	public void constructor_shouldIgnoreNullElements() {
		RolePrivileges privileges = new RolePrivileges(new HashSet<>(Arrays.asList("View Patients", null)), false);

		assertTrue(privileges.containsPrivilege("View Patients"));
		assertEquals(1, privileges.getPrivilegeNames().size());
	}

	@Test
	public void constructor_shouldRejectANullSet() {
		assertThrows(NullPointerException.class, () -> new RolePrivileges(null, false));
	}

	@Test
	public void constructor_shouldAcceptAnEmptySet() {
		RolePrivileges privileges = new RolePrivileges(Collections.emptySet(), false);

		assertTrue(privileges.getPrivilegeNames().isEmpty());
		assertFalse(privileges.grantsSuperuser());
	}

	@Test
	public void getPrivilegeNames_shouldBeUnmodifiable() {
		RolePrivileges privileges = new RolePrivileges(Collections.singleton("View Patients"), false);

		assertThrows(UnsupportedOperationException.class, () -> privileges.getPrivilegeNames().add("Edit Patients"));
	}

	@Test
	public void equals_shouldBeInsensitiveToInsertionOrderAndCase() {
		RolePrivileges a = new RolePrivileges(new LinkedHashSet<>(Arrays.asList("View Patients", "Edit Patients")), false);
		RolePrivileges b = new RolePrivileges(new LinkedHashSet<>(Arrays.asList("EDIT PATIENTS", "VIEW PATIENTS")), false);

		assertEquals(a, b);
		assertEquals(b, a);
		assertEquals(a.hashCode(), b.hashCode());
	}

	@Test
	public void equals_shouldDistinguishBySuperuserFlag() {
		RolePrivileges granting = new RolePrivileges(Collections.emptySet(), true);
		RolePrivileges notGranting = new RolePrivileges(Collections.emptySet(), false);

		assertNotEquals(granting, notGranting);
	}

	@Test
	public void equals_shouldDistinguishByPrivilegeSet() {
		RolePrivileges a = new RolePrivileges(Collections.singleton("View Patients"), false);
		RolePrivileges b = new RolePrivileges(Collections.singleton("Edit Patients"), false);

		assertNotEquals(a, b);
	}
}
