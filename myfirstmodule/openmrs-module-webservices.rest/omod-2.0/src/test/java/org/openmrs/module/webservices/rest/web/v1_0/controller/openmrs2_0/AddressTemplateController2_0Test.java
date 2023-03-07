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

import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest;
import org.openmrs.util.OpenmrsConstants;
import org.springframework.mock.web.MockHttpServletRequest;

public class AddressTemplateController2_0Test extends MainResourceControllerTest {

	@Override
	public String getURI() {
		return "addresstemplate";
	}
	
	@Test
	public void shouldGetAddressTemplate() throws Exception {
		String xml;
		try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("addressTemplate.xml")) {
			xml = IOUtils.toString(inputStream, "UTF-8");
		}
		Context.getAdministrationService().setGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_ADDRESS_TEMPLATE, xml);
		
		MockHttpServletRequest req = newGetRequest(getURI());
		
		SimpleObject result = deserialize(handle(req));
		
		String json;
		try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("addressTemplate.json")) {
			json = IOUtils.toString(inputStream, "UTF-8");
		}
		Assert.assertThat(result, Matchers.is(SimpleObject.parseJson(json)));
	}
	
	@Override
	public String getUuid() {
		return null;
	}

	@Override
	public long getAllCount() {
		return 0;
	}
	
	@Override
	public void shouldGetAll() throws Exception {
		
	}
	
	@Override
	public void shouldGetRefByUuid() throws Exception {
		
	}
	
	@Override
	public void shouldGetDefaultByUuid() throws Exception {
		
	}
	
	@Override
	public void shouldGetFullByUuid() throws Exception {
		
	}
}
