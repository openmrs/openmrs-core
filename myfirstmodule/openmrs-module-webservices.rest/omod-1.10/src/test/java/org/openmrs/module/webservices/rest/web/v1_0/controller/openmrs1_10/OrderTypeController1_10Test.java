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
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.OrderType;
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
 * Tests CRUD operations for {@link org.openmrs.OrderType}s via web service calls
 */
public class OrderTypeController1_10Test extends MainResourceControllerTest {
	
	private OrderService service;
	
	@Override
	public String getURI() {
		return "ordertype";
	}
	
	@Override
	public String getUuid() {
		return RestTestConstants1_10.ORDER_TYPE_UUID;
	}
	
	@Override
	public long getAllCount() {
		return service.getOrderTypes(false).size();
	}
	
	@Before
	public void before() {
		this.service = Context.getOrderService();
	}
	
	@Test
	public void shouldGetAnOrderTypeByUuid() throws Exception {
		SimpleObject result = deserialize(handle(newGetRequest(getURI() + "/" + getUuid())));
		
		OrderType expectedOrderType = service.getOrderTypeByUuid(getUuid());
		assertEquals(expectedOrderType.getUuid(), PropertyUtils.getProperty(result, "uuid"));
		assertEquals(expectedOrderType.getName(), PropertyUtils.getProperty(result, "name"));
		assertEquals(expectedOrderType.getJavaClassName(), PropertyUtils.getProperty(result, "javaClassName"));
		assertEquals(expectedOrderType.getDescription(), PropertyUtils.getProperty(result, "description"));
		assertEquals(expectedOrderType.isRetired(), PropertyUtils.getProperty(result, "retired"));
		assertNull(PropertyUtils.getProperty(result, "auditInfo"));
	}
	
	@Test
	public void shouldGetAnOrderTypeByName() throws Exception {
		final String name = "Test order";
		SimpleObject result = deserialize(handle(newGetRequest(getURI() + "/" + name)));
		
		OrderType expectedOrderType = service.getOrderTypeByName(name);
		assertEquals(expectedOrderType.getUuid(), PropertyUtils.getProperty(result, "uuid"));
		assertEquals(expectedOrderType.getName(), PropertyUtils.getProperty(result, "name"));
	}
	
	@Test
	public void shouldListAllOrderTypes() throws Exception {
		SimpleObject result = deserialize(handle(newGetRequest(getURI())));
		
		assertNotNull(result);
		assertEquals(getAllCount(), Util.getResultsSize(result));
	}
	
	@Test
	public void shouldListAllOrderTypesIncludingRetiredOnesIfIncludeAllIsSetToTrue() throws Exception {
		SimpleObject result = deserialize(handle(newGetRequest(getURI(), new Parameter("includeAll", "true"))));
		
		assertNotNull(result);
		assertEquals(service.getOrderTypes(true).size(), Util.getResultsSize(result));
	}
	
	@Test
	public void shouldReturnTheAuditInfoForTheFullRepresentation() throws Exception {
		MockHttpServletRequest req = request(RequestMethod.GET, getURI() + "/" + getUuid());
		req.addParameter(RestConstants.REQUEST_PROPERTY_FOR_REPRESENTATION, RestConstants.REPRESENTATION_FULL);
		SimpleObject result = deserialize(handle(req));
		
		assertNotNull(PropertyUtils.getProperty(result, "auditInfo"));
	}
	
	@Test
	public void shouldSearchAndReturnAListOfOrderTypeMatchingTheQueryString() throws Exception {
		MockHttpServletRequest req = request(RequestMethod.GET, getURI());
		req.addParameter("q", "Test");
		SimpleObject result = deserialize(handle(req));
		assertEquals(3, Util.getResultsSize(result));
		assertEquals(getUuid(), PropertyUtils.getProperty(Util.getResultsList(result).get(0), "uuid"));
		
		req.removeAllParameters();
		req.addParameter("q", "logy");
		result = deserialize(handle(req));
		assertEquals(3, Util.getResultsSize(result));
		List<String> uuids = Arrays.asList(new String[] {
		        PropertyUtils.getProperty(Util.getResultsList(result).get(0), "uuid").toString(),
		        PropertyUtils.getProperty(Util.getResultsList(result).get(1), "uuid").toString(),
		        PropertyUtils.getProperty(Util.getResultsList(result).get(2), "uuid").toString() });
		assertThat(
		    uuids,
		    hasItems("90a1e5b0-ac05-11e3-a5e2-0800200c9a66", "9b6cf570-ac05-11e3-a5e2-0800200c9a66",
		        "a4ebaf10-ac05-11e3-a5e2-0800200c9a66"));
		
	}
	
	@Test
	public void shouldPurgeAnOrderType() throws Exception {
		String uuid = "00e17510-aa09-11e3-a5e2-0800200c9a66";
		assertNotNull(service.getOrderTypeByUuid(uuid));
		MockHttpServletRequest req = request(RequestMethod.DELETE, getURI() + "/" + uuid);
		req.addParameter("purge", "true");
		handle(req);
		assertNull(service.getOrderTypeByUuid(uuid));
	}
	
	@Test
	public void shouldCreateANewOrderType() throws Exception {
		OrderService orderService = Context.getOrderService();
		int orderTypeCount = orderService.getOrderTypes(true).size();
		SimpleObject orderType = new SimpleObject();
		orderType.add("name", "New Order");
		orderType.add("javaClassName", "org.openmrs.NewTestOrder");
		orderType.add("description", "New order type for testing");
		MockHttpServletRequest req = newPostRequest(getURI(), orderType);
		SimpleObject newOrder = deserialize(handle(req));
		
		assertNotNull(newOrder);
		
		List<OrderType> orderTypes = service.getOrderTypes(true);
		assertEquals(++orderTypeCount, orderTypes.size());
		assertNotNull(PropertyUtils.getProperty(newOrder, "javaClassName"));
		assertEquals(orderType.get("name"), Util.getByPath(newOrder, "name"));
		assertEquals(orderType.get("javaClassName"), Util.getByPath(newOrder, "javaClassName"));
		assertEquals(orderType.get("description"), Util.getByPath(newOrder, "description"));
	}
	
	@Test
	public void shouldEditAnOrderType() throws Exception {
		final String newName = "Updated name";
		SimpleObject conceptMapTypeType = new SimpleObject();
		conceptMapTypeType.add("name", newName);
		
		String json = new ObjectMapper().writeValueAsString(conceptMapTypeType);
		
		MockHttpServletRequest req = request(RequestMethod.POST, getURI() + "/" + getUuid());
		req.setContent(json.getBytes());
		handle(req);
		assertEquals(newName, service.getOrderTypeByUuid(getUuid()).getName());
	}
	
	@Test
	public void shouldRetireAnOrderType() throws Exception {
		assertEquals(false, service.getOrderTypeByUuid(getUuid()).isRetired());
		MockHttpServletRequest req = request(RequestMethod.DELETE, getURI() + "/" + getUuid());
		req.addParameter("!purge", "");
		final String reason = "none";
		req.addParameter("reason", reason);
		handle(req);
		assertEquals(true, service.getOrderTypeByUuid(getUuid()).isRetired());
		assertEquals(reason, service.getOrderTypeByUuid(getUuid()).getRetireReason());
	}
}
