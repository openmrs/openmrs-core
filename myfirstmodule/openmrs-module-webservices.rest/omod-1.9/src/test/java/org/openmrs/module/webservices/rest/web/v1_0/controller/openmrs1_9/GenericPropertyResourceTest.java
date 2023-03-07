/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.web.v1_0.controller.openmrs1_9;

import org.apache.commons.beanutils.PropertyUtils;
import org.junit.Assert;
import org.junit.Test;
import org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest;

public class GenericPropertyResourceTest extends MainResourceControllerTest {
	
	private String uuid = "123456_whatevs";
	
	private Integer value = 42;
	
	@Override
	public String getURI() {
		return "genericChild";
	}
	
	@Override
	public String getUuid() {
		return uuid;
	}
	
	@Override
	public long getAllCount() {
		return 0;
	}
	
	@Test
	public void shouldCreateAGenericChild() throws Exception {
		String json = "{ \"value\":\"42\"}";
		
		Object newVisit = deserialize(handle(newPostRequest(getURI(), json)));
		
		Assert.assertNotNull(PropertyUtils.getProperty(newVisit, "uuid"));
		Assert.assertEquals(PropertyUtils.getProperty(newVisit, "value"), value);
	}
	
	// Ignore these tests, we only care about testing that we can create the object
	
	@Override
	public void shouldGetDefaultByUuid() throws Exception {
	}
	
	@Override
	public void shouldGetRefByUuid() throws Exception {
	}
	
	@Override
	public void shouldGetFullByUuid() throws Exception {
	}
	
	@Override
	public void shouldGetAll() throws Exception {
	}
}
