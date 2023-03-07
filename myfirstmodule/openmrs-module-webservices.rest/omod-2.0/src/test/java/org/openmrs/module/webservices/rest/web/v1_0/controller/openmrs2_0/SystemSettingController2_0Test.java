/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.web.v1_0.controller.openmrs2_0;

import static org.junit.Assert.assertEquals;

import org.apache.commons.beanutils.PropertyUtils;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.GlobalProperty;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.RestTestConstants1_9;
import org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Tests CRUD operations for {@link GlobalProperty}s via web service calls
 */
public class SystemSettingController2_0Test extends MainResourceControllerTest {
	
	private AdministrationService service;
	
	/**
	 * @see MainResourceControllerTest#getURI()
	 */
	@Override
	public String getURI() {
		return "systemsetting";
	}
	
	/**
	 * @see MainResourceControllerTest#getUuid()
	 */
	@Override
	public String getUuid() {
		return RestTestConstants1_9.GLOBAL_PROPERTY_UUID;
	}
	
	@Override
	public long getAllCount() {
		return service.getAllGlobalProperties().size();
	}
	
	@Before
	public void before() throws Exception {
		this.service = Context.getAdministrationService();
	}
	
	/**
	 * @see MainResourceControllerTest#shouldGetAll()
	 */
	@Override
	public void shouldGetAll() throws Exception {
		super.shouldGetAll();
	}
	
	@Test
	public void shouldGetASystemSettingWithDotByName() throws Exception {
		final String name = "concept.defaultConceptMapType";
		MockHttpServletRequest req = request(RequestMethod.GET, getURI() + "/" + name);
		SimpleObject result = deserialize(handle(req));
		
		GlobalProperty gp = service.getGlobalPropertyObject(name);
		assertEquals(gp.getUuid(), PropertyUtils.getProperty(result, "uuid"));
		assertEquals(gp.getProperty(), PropertyUtils.getProperty(result, "property"));
		assertEquals(gp.getDescription(), PropertyUtils.getProperty(result, "description"));
		assertEquals(gp.getValue(), PropertyUtils.getProperty(result, "value"));
	}
}
