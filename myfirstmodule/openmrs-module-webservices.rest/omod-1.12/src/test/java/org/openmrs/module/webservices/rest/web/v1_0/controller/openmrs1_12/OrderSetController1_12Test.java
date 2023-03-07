/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.web.v1_0.controller.openmrs1_12;

import org.apache.commons.beanutils.PropertyUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.OrderSet;
import org.openmrs.api.OrderSetService;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.test.Util;
import org.openmrs.module.webservices.rest.web.RestTestConstants1_12;
import org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.bind.annotation.RequestMethod;
import static org.junit.Assert.assertEquals;

/**
 * Tests functionality of OrderSet CRUD by MainResourceController
 */
public class OrderSetController1_12Test extends MainResourceControllerTest {
	
	private OrderSetService orderSetService;
	
	@Before
	public void init() throws Exception {
		orderSetService = Context.getOrderSetService();
		executeDataSet(RestTestConstants1_12.TEST_DATA_SET);
	}
	
	/**
	 * @see MainResourceControllerTest#getURI()
	 */
	@Override
	public String getURI() {
		return "orderset";
	}
	
	/**
	 * @see MainResourceControllerTest#getAllCount()
	 */
	@Override
	public long getAllCount() {
		return orderSetService.getOrderSets(false).size();
	}
	
	@Override
	public String getUuid() {
		return RestTestConstants1_12.ORDER_SET_UUID;
	}
	
	public String getName() {
		return "orderSet1";
	}
	
	@Test
	public void shouldListAllUnRetiredOrderSets() throws Exception {
		
		MockHttpServletRequest req = request(RequestMethod.GET, getURI());
		SimpleObject result = deserialize(handle(req));
		
		Assert.assertNotNull(result);
		Assert.assertEquals(getAllCount(), Util.getResultsSize(result));
	}
	
	@Test
	public void shouldGetAnOrderSetByUuid() throws Exception {
		
		MockHttpServletRequest req = request(RequestMethod.GET, getURI() + "/" + getUuid());
		SimpleObject result = deserialize(handle(req));
		
		OrderSet orderSet = orderSetService.getOrderSetByUuid(getUuid());
		
		Assert.assertNotNull(result);
		Assert.assertEquals(orderSet.getUuid(), PropertyUtils.getProperty(result, "uuid"));
		Assert.assertEquals(orderSet.getName(), PropertyUtils.getProperty(result, "name"));
		Assert.assertEquals(orderSet.getDescription(), PropertyUtils.getProperty(result, "description"));
		Assert.assertEquals(orderSet.getRetired(), PropertyUtils.getProperty(result, "retired"));
	}
	
	@Test
	public void shouldCreateAnOrderSet() throws Exception {
		long originalCount = getAllCount();
		
		SimpleObject orderSet = new SimpleObject();
		orderSet.add("name", "New OrderSet");
		orderSet.add("description", "OrderSet description");
		orderSet.add("operator", "ALL");
		
		String json = new ObjectMapper().writeValueAsString(orderSet);
		
		MockHttpServletRequest req = request(RequestMethod.POST, getURI());
		req.setContent(json.getBytes());
		
		SimpleObject newOrderSet = deserialize(handle(req));
		
		Assert.assertNotNull(PropertyUtils.getProperty(newOrderSet, "uuid"));
		Assert.assertEquals(originalCount + 1, getAllCount());
	}
	
	@Test
	public void shouldCreateAnOrderSetWithSomeOrderSetMembers() throws Exception {
		long originalCount = getAllCount();
		
		String json = "{\n" + "  \"name\": \"A\",\n" + "  \"description\": \"OSA\",\n" + "  \"operator\": \"ALL\",\n"
		        + "  \"orderSetMembers\": [\n" + "      {\n" + "      \"orderType\": {\n"
		        + "        \"uuid\": \"131168f4-15f5-102d-96e4-000c29c2a5d7\"\n" + "      },\n" + "      \"concept\": {\n"
		        + "        \"name\": \"Amoxicillin\",\n" + "        \"uuid\": \"b055abd8-a420-4a11-8b98-02ee170a7b54\"\n"
		        + "      }\n" + "    }\n" + "    ]\n" + "}";
		
		MockHttpServletRequest req = request(RequestMethod.POST, getURI());
		req.setContent(json.getBytes());
		
		SimpleObject newOrderSet = deserialize(handle(req));
		
		Assert.assertNotNull(PropertyUtils.getProperty(newOrderSet, "uuid"));
		Assert.assertEquals(originalCount + 1, getAllCount());
	}
	
	@Test
	public void shouldEditAnOrderSet() throws Exception {
		
		final String editedName = "OrderSet Edited";
		String json = "{ \"name\":\"" + editedName + "\" }";
		MockHttpServletRequest req = request(RequestMethod.POST, getURI() + "/" + getUuid());
		req.setContent(json.getBytes());
		handle(req);
		
		OrderSet editedOrderSet = orderSetService.getOrderSetByUuid(getUuid());
		
		Assert.assertNotNull(editedOrderSet);
		Assert.assertEquals(editedName, editedOrderSet.getName());
	}
	
	@Test
	public void shouldRetireAnOrderSet() throws Exception {
		assertEquals(false, orderSetService.getOrderSetByUuid(getUuid()).isRetired());
		
		MockHttpServletRequest req = request(RequestMethod.DELETE, getURI() + "/" + getUuid());
		req.addParameter("!purge", "");
		req.addParameter("reason", "random reason");
		handle(req);
		
		OrderSet retiredOrderSet = orderSetService.getOrderSetByUuid(getUuid());
		
		Assert.assertTrue(retiredOrderSet.isRetired());
		Assert.assertEquals("random reason", retiredOrderSet.getRetireReason());
	}
	
}
