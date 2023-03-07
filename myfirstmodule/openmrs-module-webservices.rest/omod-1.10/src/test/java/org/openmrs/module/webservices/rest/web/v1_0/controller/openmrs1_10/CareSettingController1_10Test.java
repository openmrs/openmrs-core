/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.web.v1_0.controller.openmrs1_10;

import static org.hamcrest.Matchers.hasItems;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.beanutils.PropertyUtils;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.CareSetting;
import org.openmrs.api.OrderService;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.test.Util;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.RestTestConstants1_10;
import org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Tests CRUD operations for {@link org.openmrs.CareSetting}s via web service calls
 */
public class CareSettingController1_10Test extends MainResourceControllerTest {
	
	private OrderService service;
	
	@Override
	public String getURI() {
		return "caresetting";
	}
	
	@Override
	public String getUuid() {
		return RestTestConstants1_10.CARE_SETTING_UUID;
	}
	
	@Override
	public long getAllCount() {
		return service.getCareSettings(false).size();
	}
	
	@Before
	public void before() {
		this.service = Context.getOrderService();
	}
	
	@Test
	public void shouldGetAnCareSettingByUuid() throws Exception {
		SimpleObject result = deserialize(handle(newGetRequest(getURI() + "/" + getUuid())));
		
		CareSetting expectedCareSetting = service.getCareSettingByUuid(getUuid());
		assertEquals(expectedCareSetting.getUuid(), PropertyUtils.getProperty(result, "uuid"));
		assertEquals(expectedCareSetting.getName(), PropertyUtils.getProperty(result, "name"));
		assertEquals(expectedCareSetting.getCareSettingType().name(), PropertyUtils.getProperty(result, "careSettingType"));
		assertEquals(expectedCareSetting.getDescription(), PropertyUtils.getProperty(result, "description"));
		assertEquals(expectedCareSetting.isRetired(), PropertyUtils.getProperty(result, "retired"));
		assertNull(PropertyUtils.getProperty(result, "auditInfo"));
	}
	
	@Test
	public void shouldGetACareSettingByName() throws Exception {
		final String name = "outpatient";
		SimpleObject result = deserialize(handle(newGetRequest(getURI() + "/" + name)));
		
		CareSetting expectedCareSetting = service.getCareSettingByName(name);
		assertEquals(expectedCareSetting.getUuid(), PropertyUtils.getProperty(result, "uuid"));
		assertEquals(expectedCareSetting.getName(), PropertyUtils.getProperty(result, "name"));
	}
	
	@Test
	public void shouldListAllCareSettings() throws Exception {
		SimpleObject result = deserialize(handle(newGetRequest(getURI())));
		
		assertNotNull(result);
		assertEquals(getAllCount(), Util.getResultsSize(result));
	}
	
	@Test
	public void shouldListAllCareSettingsIncludingRetiredOnesIfIncludeAllIsSetToTrue() throws Exception {
		SimpleObject result = deserialize(handle(newGetRequest(getURI(), new Parameter("includeAll", "true"))));
		
		assertNotNull(result);
		assertEquals(service.getCareSettings(true).size(), Util.getResultsSize(result));
	}
	
	@Test
	public void shouldReturnTheAuditInfoForTheFullRepresentation() throws Exception {
		MockHttpServletRequest req = request(RequestMethod.GET, getURI() + "/" + getUuid());
		req.addParameter(RestConstants.REQUEST_PROPERTY_FOR_REPRESENTATION, RestConstants.REPRESENTATION_FULL);
		SimpleObject result = deserialize(handle(req));
		
		assertNotNull(PropertyUtils.getProperty(result, "auditInfo"));
	}
	
	@Test
	public void shouldSearchAndReturnAListOfCareSettingsMatchingTheQueryString() throws Exception {
		MockHttpServletRequest req = request(RequestMethod.GET, getURI());
		req.addParameter("q", "out");
		SimpleObject result = deserialize(handle(req));
		assertEquals(1, Util.getResultsSize(result));
		assertEquals(getUuid(), PropertyUtils.getProperty(Util.getResultsList(result).get(0), "uuid"));
		
		req.removeAllParameters();
		req.addParameter("q", "pati");
		result = deserialize(handle(req));
		assertEquals(2, Util.getResultsSize(result));
		List<String> uuids = Arrays.asList(new String[] {
		        PropertyUtils.getProperty(Util.getResultsList(result).get(0), "uuid").toString(),
		        PropertyUtils.getProperty(Util.getResultsList(result).get(1), "uuid").toString() });
		assertThat(uuids, hasItems(getUuid(), "c365e560-c3ec-11e3-9c1a-0800200c9a66"));
		
	}
}
