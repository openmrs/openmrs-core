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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.ProviderAttributeType;
import org.openmrs.ProviderRole;
import org.openmrs.api.context.Context;
import org.openmrs.test.jupiter.BaseModuleContextSensitiveTest;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ProviderRoleServiceTest extends BaseModuleContextSensitiveTest {

	protected static final String XML_DATASET_PATH = "org/openmrs/api/include/ProviderRoleServiceTest-dataset.xml";

	private ProviderRoleService providerRoleService;

	@BeforeEach
	public void init() throws Exception {
		executeDataSet(XML_DATASET_PATH);
		providerRoleService = Context.getService(ProviderRoleService.class);
	}
	
	@Test
	public void getAllProviderRoles_shouldGetAllProviderRoles() {
		List<ProviderRole> roles = providerRoleService.getAllProviderRoles(true);
		int roleCount = roles.size();
		assertEquals(12, roleCount);

		roles = providerRoleService.getAllProviderRoles(true);
		roleCount = roles.size();
		assertEquals(12, roleCount);
	}

	@Test
	public void getAllProviderRoles_shouldGetAllProviderRolesExcludingRetired() {
		List<ProviderRole> roles = providerRoleService.getAllProviderRoles(false);
		int roleCount = roles.size();
		assertEquals(11, roleCount);
	}

	@Test
	public void getProviderRole_shouldGetProviderRole() {
		ProviderRole role = providerRoleService.getProviderRole(1002);
		assertEquals(new Integer(1002), role.getId());
		assertEquals("Binome supervisor", role.getName());
	}

	@Test
	public void getProviderRole_shouldReturnNullIfNoProviderForId() {
		assertNull(providerRoleService.getProviderRole(200));
	}

	@Test
	public void getProviderRoleByUuid_shouldGetProviderRoleByUuid() {
		ProviderRole role = providerRoleService.getProviderRoleByUuid("db7f523f-27ce-4bb2-86d6-6d1d05312bd5");
		assertEquals(new Integer(1003), role.getId());
		assertEquals("Cell supervisor", role.getName());
	}

	@Test
	public void getProviderRoleByUuid_shouldReturnNUllIfNoProviderForUuid() {
		ProviderRole role = providerRoleService.getProviderRoleByUuid("zzz");
	}

	@Test
	public void saveProviderRole_shouldSaveBasicProviderRole() {
		ProviderRole role = new ProviderRole();
		role.setName("Some provider role");
		Context.getService(ProviderRoleService.class).saveProviderRole(role);
		assertEquals(13, providerRoleService.getAllProviderRoles(true).size());
	}

	@Test
	public void saveProviderRole_shouldSaveProviderRoleWithProviderAttributeTypes() {
		ProviderRole role = new ProviderRole();
		role.setName("Some provider role");

		Set<ProviderAttributeType> attributeTypes = new HashSet<ProviderAttributeType>();
		attributeTypes.add(Context.getProviderService().getProviderAttributeType(1001));
		attributeTypes.add(Context.getProviderService().getProviderAttributeType(1002));

		Context.getService(ProviderRoleService.class).saveProviderRole(role);
		assertEquals(13, providerRoleService.getAllProviderRoles(true).size());
	}

	@Test
	public void deleteProviderRole_shouldDeleteProviderRole() throws Exception {
		ProviderRole role = providerRoleService.getProviderRole(1012);
		providerRoleService.purgeProviderRole(role);
		assertEquals(11, providerRoleService.getAllProviderRoles(true).size());
		assertNull(providerRoleService.getProviderRole(1012));
	}

	@Test
	public void deleteProviderRole_shouldFailIfForeignKeyConstraintExists() throws Exception {
		assertThrows(ProviderRoleInUseException.class, () -> {
			ProviderRole role = providerRoleService.getProviderRole(1002);
			providerRoleService.purgeProviderRole(role);
		});
	}

	@Test
	public void retireProviderRole_shouldRetireProviderRole() {
		ProviderRole role = providerRoleService.getProviderRole(1002);
		providerRoleService.retireProviderRole(role, "test");
		assertEquals(10, providerRoleService.getAllProviderRoles(false).size());

		role = providerRoleService.getProviderRole(1002);
		assertTrue(role.getRetired());
		assertEquals("test", role.getRetireReason());

	}

	@Test
	public void unretireProviderRole_shouldUnretireProviderRole() {
		ProviderRole role = providerRoleService.getProviderRole(1002);
		providerRoleService.retireProviderRole(role, "test");
		assertEquals(10, providerRoleService.getAllProviderRoles(false).size());

		role = providerRoleService.getProviderRole(1002);
		providerRoleService.unretireProviderRole(role);
		assertFalse(role.getRetired());
	}
}
