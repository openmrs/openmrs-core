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
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.OrderSet;
import org.openmrs.OrderSetMember;
import org.openmrs.api.OrderSetService;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.test.Util;
import org.openmrs.module.webservices.rest.web.RestTestConstants1_12;
import org.openmrs.module.webservices.rest.web.api.RestHelperService;
import org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest;

import java.util.Arrays;
import java.util.List;

public class OrderSetMemberController1_12Test extends MainResourceControllerTest {
	
	String orderSetUuid = RestTestConstants1_12.ORDER_SET_UUID;
	
	String orderSetMemberUuid = RestTestConstants1_12.ORDER_SET_MEMBER_UUID;
	
	private OrderSetService orderSetService;
	
	@Before
	public void init() throws Exception {
		orderSetService = Context.getOrderSetService();
		executeDataSet(RestTestConstants1_12.TEST_DATA_SET);
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest#getURI()
	 */
	@Override
	public String getURI() {
		return "orderset/" + orderSetUuid + "/ordersetmember";
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest#getUuid()
	 */
	@Override
	public String getUuid() {
		return orderSetMemberUuid;
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest#getAllCount()
	 */
	@Override
	public long getAllCount() {
		return orderSetService.getOrderSetByUuid(orderSetUuid).getOrderSetMembers().size();
	}
	
	@Test
	public void shouldAddAnOrderSetMemberToAnOrderSet() throws Exception {
		int before = (int) getAllCount();
		String json = "{\n" + "      \"orderType\": {\n" + "        \"uuid\": \"131168f4-15f5-102d-96e4-000c29c2a5d7\"\n"
		        + "      },\n" + "      \"retired\": false\n" + "    }";
		
		handle(newPostRequest(getURI(), json));
		
		int after = orderSetService.getOrderSetByUuid(orderSetUuid).getOrderSetMembers().size();
		Assert.assertEquals(before + 1, after);
	}
	
	@Test
	public void shouldShowAllOrderSetMembersForAnOrderSet() throws Exception {
		
		OrderSet testOrderSet = Context.getOrderSetService().getOrderSetByUuid(orderSetUuid);
		OrderSetMember testOrderSetMember = new OrderSetMember();
		testOrderSet.addOrderSetMember(testOrderSetMember);
		Context.getOrderSetService().saveOrderSet(testOrderSet);
		
		Context.flushSession();
		Context.clearSession();
		
		List<OrderSetMember> orderSetMembers = Context.getOrderSetService().getOrderSetByUuid(orderSetUuid)
		        .getOrderSetMembers();
		
		for (OrderSetMember orderSetMember : orderSetMembers) {
			Assert.assertNotNull(orderSetMember.getOrderSetMemberId());
		}
		
		Assert.assertEquals(3, testOrderSet.getOrderSetMembers().size());
		
		SimpleObject response = deserialize(handle(newGetRequest(getURI())));
		
		List<Object> resultsList = Util.getResultsList(response);
		Assert.assertEquals(3, resultsList.size());
		
		List<Object> descriptions = Arrays.asList(PropertyUtils.getProperty(resultsList.get(0), "uuid"),
		    PropertyUtils.getProperty(resultsList.get(1), "uuid"));
		
		Assert.assertTrue(descriptions.contains("order_set_member_uuid1"));
		Assert.assertTrue(descriptions.contains("order_set_member_uuid2"));
	}
	
	@Test
	public void shouldEditAnOrderSetMember() throws Exception {
		OrderSetMember orderSetMember = Context.getService(RestHelperService.class).getObjectByUuid(OrderSetMember.class,
		    orderSetMemberUuid);
		Assert.assertEquals(null, orderSetMember.getOrderTemplate());
		
		String json = "{\n" + "\"orderTemplate\": \"NEW TEST TEMPLATE\"\n" + "    }";
		
		deserialize(handle(newPostRequest(getURI() + "/" + getUuid(), json)));
		
		Assert.assertTrue(PropertyUtils.getProperty(orderSetMember, "orderTemplate").equals("NEW TEST TEMPLATE"));
	}
	
	@Test
	public void shouldRetireAnOrderSetMember() throws Exception {
		int before = (int) getAllCount();
		
		handle(newDeleteRequest(getURI() + "/" + getUuid(), new Parameter("!purge", ""), new Parameter("reason",
		        "testing delete")));
		
		int after = orderSetService.getOrderSetByUuid(orderSetUuid).getUnRetiredOrderSetMembers().size();
		Assert.assertEquals(before - 1, after);
	}
	
	@Test
	public void shouldPurgeAnOrderSetMember() throws Exception {
		int before = (int) getAllCount();
		
		handle(newDeleteRequest(getURI() + "/" + getUuid(), new Parameter("purge", "")));
		
		int after = orderSetService.getOrderSetByUuid(orderSetUuid).getOrderSetMembers().size();
		Assert.assertEquals(before - 1, after);
	}
	
}
