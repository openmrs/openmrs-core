/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.serialization;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.GlobalProperty;
import org.openmrs.Person;
import org.openmrs.PersonName;
import org.openmrs.Privilege;
import org.openmrs.Role;
import org.openmrs.User;
import org.openmrs.api.APIException;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.AdministrationDAO;
import org.openmrs.module.Module;
import org.openmrs.module.ModuleFactory;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;
import org.openmrs.test.serialization.ProbeTarget;
import org.openmrs.util.PrivilegeConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Runs the serializer type configuration through the real service as a user who holds only the
 * configuration privilege and the read privilege the calls need: entries that violate the rules are
 * refused at save time, values stored by other means are still applied with the same rules by a
 * fresh serializer, and a module-owned entry round-trips through the real serializer.
 */
public class SimpleXStreamSerializerLimitedUserTest extends BaseContextSensitiveTest {

	private static final String GP_USER = "gponlyuser";

	private static final String TEST_MODULE_ID = "testmodule";

	@Autowired
	private CacheManager cacheManager;

	@BeforeEach
	public void authenticateAsGlobalPropertiesOnlyUser() throws Exception {
		Module testModule = new Module("Test Module", TEST_MODULE_ID, "org.openmrs.module." + TEST_MODULE_ID, "OpenMRS",
		        "module used by tests", "1.0", "1");
		ModuleFactory.loadModule(testModule, true);

		Context.getUserContext().logout();
		Context.authenticate("admin", "test");

		Role role = new Role("gponlyrole");
		role.setDescription("holds only the serializer configuration privileges");
		grantPrivilege(role, PrivilegeConstants.MANAGE_GLOBAL_PROPERTIES);
		grantPrivilege(role, PrivilegeConstants.GET_GLOBAL_PROPERTIES);
		Context.getUserService().saveRole(role);

		User user = new User();
		user.setPerson(new Person());
		user.getPerson().setGender("M");
		user.getPerson().addName(new PersonName("Gp", "Only", "User"));
		user.setUsername(GP_USER);
		user.addRole(role);
		Context.getUserService().createUser(user, "Openmr5xy");

		Context.getUserContext().logout();
		Context.authenticate(GP_USER, "Openmr5xy");
		assertFalse(Context.getAuthenticatedUser().isSuperUser(), "the scenario user must not be a superuser");
	}

	@AfterEach
	public void unloadFixtureModule() {
		ModuleFactory.getLoadedModulesMap().remove(TEST_MODULE_ID);
	}

	private AdministrationDAO administrationDao() {
		return Context.getRegisteredComponent("adminDAO", AdministrationDAO.class);
	}

	private void grantPrivilege(Role role, String privilegeName) throws Exception {
		Privilege privilege = Context.getUserService().getPrivilege(privilegeName);
		if (privilege == null) {
			privilege = Context.getUserService().savePrivilege(new Privilege(privilegeName, "granted for the scenario"));
		}
		role.addPrivilege(privilege);
	}

	/**
	 * Returns the serializer configuration service as a plain object with only the data access wiring
	 * set, so reads of the type list do not go through the cache.
	 */
	private AdministrationService rawAdministrationService() {
		org.openmrs.api.impl.AdministrationServiceImpl impl = new org.openmrs.api.impl.AdministrationServiceImpl();
		impl.setAdministrationDAO(administrationDao());
		return impl;
	}

	private void clearSerializerCaches() {
		if (cacheManager.getCacheNames().contains("serializerWhiteListTypes")) {
			cacheManager.getCache("serializerWhiteListTypes").clear();
		}
	}

	@Test
	public void shouldRejectSavingBroadConfigurationEntriesThroughTheService() {
		AdministrationService adminService = Context.getAdministrationService();
		assertThrows(APIException.class,
		    () -> adminService.saveGlobalProperty(new GlobalProperty("x.serializer.whitelist.types", "**")));
		assertThrows(APIException.class,
		    () -> adminService.saveGlobalProperty(new GlobalProperty(TEST_MODULE_ID + ".serializer.whitelist.types", "**")));
	}

	@Test
	public void shouldRefuseOutsideTypesStoredByOtherMeansAfterAFreshSerializerBuild() {
		AdministrationDAO administrationDao = administrationDao();
		administrationDao.saveGlobalProperty(new GlobalProperty("x.serializer.whitelist.types", "**"));
		administrationDao.saveGlobalProperty(new GlobalProperty(TEST_MODULE_ID + ".serializer.whitelist.types", "**"));
		clearSerializerCaches();

		SimpleXStreamSerializer serializer = new SimpleXStreamSerializer(rawAdministrationService());
		assertThrows(SerializationException.class, () -> serializer.deserialize(
		    "<org.openmrs.test.serialization.ProbeTarget><name>probe</name></org.openmrs.test.serialization.ProbeTarget>",
		    ProbeTarget.class));
	}

	@Test
	public void shouldSerializeAndDeserializeRoundTripForAnAllowedModuleEntry() throws SerializationException {
		AdministrationDAO administrationDao = administrationDao();
		administrationDao.saveGlobalProperty(new GlobalProperty(TEST_MODULE_ID + ".serializer.whitelist.types",
		        "org.openmrs.module." + TEST_MODULE_ID + ".ProbeTarget"));
		clearSerializerCaches();

		SimpleXStreamSerializer serializer = new SimpleXStreamSerializer(rawAdministrationService());
		org.openmrs.module.testmodule.ProbeTarget target = new org.openmrs.module.testmodule.ProbeTarget();
		target.setName("roundtrip");
		String xml = serializer.serialize(target);
		assertEquals("roundtrip", serializer.deserialize(xml, org.openmrs.module.testmodule.ProbeTarget.class).getName());
	}

}
