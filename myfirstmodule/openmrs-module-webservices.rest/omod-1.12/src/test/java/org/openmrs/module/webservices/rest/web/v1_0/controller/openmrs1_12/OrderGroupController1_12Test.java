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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.beanutils.PropertyUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Encounter;
import org.openmrs.Order;
import org.openmrs.OrderGroup;
import org.openmrs.OrderSet;
import org.openmrs.Patient;
import org.openmrs.api.OrderService;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.test.Util;
import org.openmrs.module.webservices.rest.web.RestTestConstants1_12;
import org.openmrs.module.webservices.rest.web.response.ResourceDoesNotSupportOperationException;
import org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.bind.annotation.RequestMethod;

public class OrderGroupController1_12Test extends MainResourceControllerTest {
	
	private OrderService orderService;
	
	@Before
	public void init() throws Exception {
		orderService = Context.getOrderService();
		executeDataSet(RestTestConstants1_12.ORDER_GROUP_TEST_DATA_SET);
	}
	
	@Override
	public String getURI() {
		return "ordergroup";
	}
	
	@Override
	public String getUuid() {
		return RestTestConstants1_12.ORDER_GROUP_UUID;
	}
	
	@Override
	public long getAllCount() {
		return 0;
	}
	
	@Test
	public void shouldCreateNewOrderGroup() throws Exception {
		
		final String JSON = "{\n" + "  \"patient\": \"" + RestTestConstants1_12.ORDER_GROUP_PATIENT_UUID + "\",\n"
		        + "  \"encounter\": \"" + RestTestConstants1_12.ORDER_GROUP_ENCOUNTER_UUID + "\",\n" + "  \"orders\": [\n"
		        + "    {\n"
		        + "      \"encounter\": \"" + RestTestConstants1_12.ORDER_GROUP_ENCOUNTER_UUID + "\",\n"
		        + "      \"orderType\": \"" + RestTestConstants1_12.ORDER_GROUP_ORDER_TYPE_UUID + "\",\n"
		        + "      \"type\": \"order\",\n"
		        + "      \"action\": \"NEW\",\n" + "      \"accessionNumber\": \"string\",\n"
		        + "      \"dateActivated\": \"2012-09-19\",\n" + "     \"scheduledDate\": \"2012-09-19\",\n"
		        + "      \"patient\": \"" + RestTestConstants1_12.ORDER_GROUP_PATIENT_UUID + "\",\n"
		        + "      \"concept\": \"" + RestTestConstants1_12.ORDER_GROUP_CONCEPT_UUID + "\",\n"
		        + "      \"careSetting\": \"" + RestTestConstants1_12.ORDER_GROUP_CARESETTING_UUID + "\",\n"
		        + "      \"autoExpireDate\": \"2012-09-21\",\n"
		        + "      \"orderer\": \"" + RestTestConstants1_12.ORDER_GROUP_ODERER_UUID + "\",\n"
		        + "      \"previousOrder\": \"\",\n"
		        + "      \"urgency\": \"ROUTINE\",\n" + "      \"orderReason\": \"\",\n"
		        + "      \"orderReasonNonCoded\": \"string\",\n" + "      \"instructions\": \"string\",\n"
		        + "      \"commentToFulfiller\": \"string\"\n" + "    }\n" + "," + "  "
		        + "      {\n"
		        + "      \"encounter\": \"" + RestTestConstants1_12.ORDER_GROUP_ENCOUNTER_UUID + "\",\n"
		        + "      \"orderType\": \"" + RestTestConstants1_12.ORDER_GROUP_ORDER_TYPE_UUID + "\",\n"
		        + "      \"type\": \"order\",\n"
		        + "      \"action\": \"NEW\",\n" + "      \"accessionNumber\": \"string\",\n"
		        + "      \"dateActivated\": \"2012-09-10\",\n" + "      \"scheduledDate\": \"2012-09-10\",\n"
		        + "      \"patient\": \"" + RestTestConstants1_12.ORDER_GROUP_PATIENT_UUID + "\",\n"
		        + "      \"concept\": \"" + RestTestConstants1_12.ORDER_GROUP_CONCEPT_UUID + "\",\n"
		        + "      \"careSetting\": \"" + RestTestConstants1_12.ORDER_GROUP_CARESETTING_UUID + "\",\n"
		        + "      \"autoExpireDate\": \"2012-09-21\",\n"
		        + "      \"orderer\": \"" + RestTestConstants1_12.ORDER_GROUP_ODERER_UUID + "\",\n"
		        + "      \"previousOrder\": \"\",\n"
		        + "      \"urgency\": \"ROUTINE\",\n" + "      \"orderReason\": \"\",\n"
		        + "      \"orderReasonNonCoded\": \"string\",\n" + "      \"instructions\": \"string\",\n"
		        + "      \"commentToFulfiller\": \"string\"\n" + "    }\n" + "  ],\n"
		        + "      \"orderSet\": \"" + RestTestConstants1_12.ORDER_GROUP_ORDERSET_UUID + "\"\n" + "}";
		
		MockHttpServletRequest req = newPostRequest(getURI(), JSON);
		SimpleObject result = deserialize(handle(req));
		Assert.assertEquals(RestTestConstants1_12.ORDER_GROUP_PATIENT_UUID, Util.getByPath(result, "patient/uuid"));
		Assert.assertEquals(RestTestConstants1_12.ORDER_GROUP_ENCOUNTER_UUID, Util.getByPath(result, "encounter/uuid"));
		Assert.assertEquals(RestTestConstants1_12.ORDER_GROUP_ORDERSET_UUID, Util.getByPath(result, "orderSet/uuid"));
		Assert.assertEquals(RestTestConstants1_12.ORDER_GROUP_DISPLAY, Util.getByPath(result, "display"));
		
		String uuid = (String) PropertyUtils.getProperty(result, "uuid");
		Assert.assertEquals(2, orderService.getOrderGroupByUuid(uuid).getOrders().size());
	}
	
	@Test
	public void shouldGetOrderGroupByUuid() throws Exception {
		
		OrderGroup orderGroup = Context.getOrderService().getOrderGroupByUuid(RestTestConstants1_12.ORDER_GROUP_UUID);
		Patient patient = orderGroup.getPatient();
		Encounter encounter = orderGroup.getEncounter();
		OrderSet orderSet = orderGroup.getOrderSet();
		Order order = Context.getOrderService().getOrder(1);
		Order order2 = Context.getOrderService().getOrder(3);
		List<Order> orders = new ArrayList<Order>();
		orders.add(order);
		orders.add(order2);
		
		orderGroup.setOrders(orders);
		order.setOrderGroup(orderGroup);
		order2.setOrderGroup(orderGroup);
		
		MockHttpServletRequest req = request(RequestMethod.GET, getURI() + "/" + RestTestConstants1_12.ORDER_GROUP_UUID);
		SimpleObject response = deserialize(handle(req));
		Assert.assertEquals(patient.getUuid(), Util.getByPath(response, "patient/uuid"));
		Assert.assertEquals(encounter.getUuid(), Util.getByPath(response, "encounter/uuid"));
		Assert.assertEquals(orderSet.getUuid(), Util.getByPath(response, "orderSet/uuid"));
		Assert.assertEquals(RestTestConstants1_12.ORDER_GROUP_DISPLAY, Util.getByPath(response, "display"));
		List<Order> orderList = (List<Order>) PropertyUtils.getProperty(response, "orders");
		Assert.assertEquals(2, orderList.size());
	}
	
	@Test
	public void shouldAddOrdersToAnExistingOrderGroup() throws Exception {
		final String JSON_ORDER = " { \"orders\": [\n" + "    {\n"
		        + "      \"encounter\": \"" + RestTestConstants1_12.ORDER_GROUP_ENCOUNTER_UUID + "\",\n"
		        + "      \"orderType\": \"" + RestTestConstants1_12.ORDER_GROUP_ORDER_TYPE_UUID + "\",\n"
		        + "      \"type\": \"order\",\n"
		        + "      \"action\": \"NEW\",\n" + "      \"accessionNumber\": \"string\",\n"
		        + "      \"dateActivated\": \"2012-09-11\",\n" + "      \"scheduledDate\": \"2012-09-11\",\n"
		        + "      \"patient\": \"" + RestTestConstants1_12.ORDER_GROUP_PATIENT_UUID + "\",\n"
		        + "      \"concept\": \"" + RestTestConstants1_12.ORDER_GROUP_CONCEPT_UUID + "\",\n"
		        + "      \"careSetting\": \"" + RestTestConstants1_12.ORDER_GROUP_CARESETTING_UUID + "\",\n"
		        + "      \"autoExpireDate\": \"2012-09-21\",\n"
		        + "      \"orderer\": \"" + RestTestConstants1_12.ORDER_GROUP_ODERER_UUID + "\",\n"
		        + "      \"previousOrder\": \"\",\n"
		        + "      \"urgency\": \"ROUTINE\",\n" + "      \"orderReason\": \"\",\n"
		        + "      \"orderReasonNonCoded\": \"for Test\",\n" + "      \"instructions\": \"string\",\n"
		        + "      \"commentToFulfiller\": \"string\"\n" + "    }\n" + "  ]\n" + "}";
		Integer ordersBefore = orderService.getOrderGroupByUuid(RestTestConstants1_12.ORDER_GROUP_UUID).getOrders().size();
		
		MockHttpServletRequest req = newPostRequest(getURI() + "/" + RestTestConstants1_12.ORDER_GROUP_UUID, JSON_ORDER);
		handle(req);
		Integer ordersAfter = orderService.getOrderGroupByUuid(RestTestConstants1_12.ORDER_GROUP_UUID).getOrders().size();
		Assert.assertEquals(++ordersBefore, ordersAfter);
	}
	
	@Override
	@Test(expected = ResourceDoesNotSupportOperationException.class)
	public void shouldGetAll() throws Exception {
		super.shouldGetAll();
	}
}
