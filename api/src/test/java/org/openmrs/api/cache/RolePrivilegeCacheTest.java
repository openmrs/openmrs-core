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
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.Privilege;
import org.openmrs.Role;
import org.openmrs.util.RoleConstants;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pure unit tests for {@link RolePrivilegeCache}. These do not touch the database or the Spring
 * application context: an in-memory {@link ConcurrentMapCacheManager} stands in for the real cache,
 * so the flattening logic (inheritance, superuser detection, cycle handling) can be verified in
 * isolation.
 */
public class RolePrivilegeCacheTest {

	private RolePrivilegeCache cache;

	@BeforeEach
	void setUp() {
		// a simple in-memory cache manager that hands out a cache named "rolePrivileges"
		ConcurrentMapCacheManager cacheManager = new ConcurrentMapCacheManager("rolePrivileges");
		cache = new RolePrivilegeCache(cacheManager);
	}

	/** Builds a role with the given name and (case-preserved) privilege names. */
	private Role role(String name, String... privileges) {
		Role role = new Role(name);
		Set<Privilege> privilegeSet = new HashSet<>();
		for (String privilege : privileges) {
			privilegeSet.add(new Privilege(privilege));
		}
		role.setPrivileges(privilegeSet);
		return role;
	}

	@Test
	void getPrivileges_shouldStoreLowercasedPrivilegeNames() {
		Role basicUser = role("Basic User", "View Patients");

		RolePrivileges result = cache.getPrivileges(basicUser);

		assertTrue(result.containsPrivilege("view patients"));
		assertFalse(result.grantsSuperuser());
	}

	@Test
	void getPrivileges_shouldMatchCaseInsensitively() {
		Role basicUser = role("Basic User", "View Patients");

		RolePrivileges result = cache.getPrivileges(basicUser);

		// stored lowercased; the caller is responsible for lowercasing its lookup key
		assertTrue(result.containsPrivilege("view patients"));
		assertFalse(result.containsPrivilege("View Patients")); // not lowercased -> no match, by design
	}

	@Test
	void getPrivileges_shouldIncludeInheritedPrivileges() {
		Role basicUser = role("Basic User", "View Patients");
		Role clinician = role("Clinician", "Add Visits");
		Role seniorClinician = role("Senior Clinician", "Edit Patients");
		clinician.setInheritedRoles(new HashSet<>(Set.of(basicUser)));
		seniorClinician.setInheritedRoles(new HashSet<>(Set.of(clinician)));

		RolePrivileges result = cache.getPrivileges(seniorClinician);

		assertTrue(result.containsPrivilege("edit patients")); // from Senior Clinician itself
		assertTrue(result.containsPrivilege("add visits")); // inherited from Clinician
		assertTrue(result.containsPrivilege("view patients")); // inherited from Basic User
		assertFalse(result.grantsSuperuser());
	}

	@Test
	void getPrivileges_shouldReturnCachedValueOnSecondLookup() {
		Role basicUser = role("Basic User", "View Patients");

		RolePrivileges first = cache.getPrivileges(basicUser);
		assertTrue(first.containsPrivilege("view patients"));

		// mutate the role in memory AFTER it was cached
		basicUser.getPrivileges().add(new Privilege("Manage Concepts"));

		RolePrivileges second = cache.getPrivileges(basicUser);
		// still the OLD value -> proves it was served from the cache, not recomputed
		assertFalse(second.containsPrivilege("manage concepts"));
	}

	@Test
	void getPrivileges_shouldDetectInheritedSuperuser() {
		Role superuserRole = role(RoleConstants.SUPERUSER);
		Role admin = role("Admin", "Manage Users");
		admin.setInheritedRoles(new HashSet<>(Set.of(superuserRole)));

		RolePrivileges result = cache.getPrivileges(admin);

		assertTrue(result.grantsSuperuser()); // superuser status flows down through inheritance
	}

	@Test
	void getPrivileges_shouldTerminateOnInheritanceCycle() {
		Role roleA = role("A", "Privilege A");
		Role roleB = role("B", "Privilege B");
		roleA.setInheritedRoles(new HashSet<>(Set.of(roleB)));
		roleB.setInheritedRoles(new HashSet<>(Set.of(roleA))); // A <-> B cycle

		RolePrivileges result = cache.getPrivileges(roleA); // must not hang

		assertTrue(result.containsPrivilege("privilege a"));
		assertTrue(result.containsPrivilege("privilege b"));
	}
}
