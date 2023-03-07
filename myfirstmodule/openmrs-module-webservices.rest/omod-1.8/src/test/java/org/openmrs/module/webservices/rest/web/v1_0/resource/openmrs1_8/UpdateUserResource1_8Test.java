/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs1_8;

import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.api.RestService;
import org.openmrs.module.webservices.rest.web.v1_0.wrapper.openmrs1_8.UserAndPassword1_8;
import org.openmrs.web.test.BaseModuleWebContextSensitiveTest;

import java.util.HashMap;
import java.util.Map;

/**
 * Integration tests for the framework that lets a resource handle an entire class hierarchy
 */
public class UpdateUserResource1_8Test extends BaseModuleWebContextSensitiveTest {
	
	private UserResource1_8 resource;
	
	@Before
	public void beforeEachTests() throws Exception {
		resource = (UserResource1_8) Context.getService(RestService.class).getResourceBySupportedClass(
		    UserAndPassword1_8.class);
	}
	
	@Test
	public void shouldUpdateUser() throws Exception {
		SimpleObject userSimpleObject = new SimpleObject();
		
		userSimpleObject.putAll(new ObjectMapper().readValue(
		    getClass().getClassLoader().getResourceAsStream("update_user.json"), HashMap.class));
		SimpleObject updated = (SimpleObject) resource.update("c98a1558-e131-11de-babe-001e378eb67e", userSimpleObject,
		    new RequestContext());
		
		Map<String, String> userProperties = (Map<String, String>) updated.get("userProperties");
		Assert.assertEquals(2, userProperties.size());
		Assert.assertNotNull(userProperties.get("favouriteObsTemplates"));
		Assert.assertEquals("Gynaecology", userProperties.get("favouriteObsTemplates"));
		
		userSimpleObject.putAll(new ObjectMapper().readValue(
		    getClass().getClassLoader().getResourceAsStream("update_user.json"), HashMap.class));
		SimpleObject updatedAgain = (SimpleObject) resource.update("c98a1558-e131-11de-babe-001e378eb67e", userSimpleObject,
		    new RequestContext());
	}
	
}
