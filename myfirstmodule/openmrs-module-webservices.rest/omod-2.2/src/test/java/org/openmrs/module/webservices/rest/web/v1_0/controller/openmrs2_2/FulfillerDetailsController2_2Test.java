/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.web.v1_0.controller.openmrs2_2;

import org.junit.Test;
import org.openmrs.Order;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.response.ResourceDoesNotSupportOperationException;
import org.openmrs.module.webservices.rest.web.v1_0.RestTestConstants2_2;
import org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.Assert.assertEquals;

public class FulfillerDetailsController2_2Test extends MainResourceControllerTest {

	@Test
	public void test_shouldSetFulfillerDetailsOfOrderByPost() throws Exception {
		Order.FulfillerStatus fulfillerStatus = Order.FulfillerStatus.RECEIVED;
		String fillerComment = "An example comment from a filler";
		SimpleObject post = new SimpleObject().add("fulfillerStatus", fulfillerStatus)
		        .add("fulfillerComment", fillerComment);
		MockHttpServletRequest request = newPostRequest(getURI(), post);

		handle(request);

		Order order = Context.getOrderService().getOrderByUuid(RestTestConstants2_2.ORDER_UUID);
		assertEquals(order.getFulfillerStatus(), fulfillerStatus);
		assertEquals(order.getFulfillerComment(), fillerComment);
	}

	@Test(expected = ResourceDoesNotSupportOperationException.class)
	public void shouldGetAll() throws Exception {
		super.shouldGetAll();
	}

	@Test(expected = ResourceDoesNotSupportOperationException.class)
	public void shouldGetDefaultByUuid() throws Exception {
		super.shouldGetDefaultByUuid();
	}

	@Test(expected = ResourceDoesNotSupportOperationException.class)
	public void shouldGetFullByUuid() throws Exception {
		super.shouldGetFullByUuid();
	}

	@Test(expected = ResourceDoesNotSupportOperationException.class)
	public void shouldGetRefByUuid() throws Exception {
		super.shouldGetRefByUuid();
	}

	@Override
	public String getURI() {
		return "order/" + RestTestConstants2_2.ORDER_UUID + "/fulfillerdetails";
	}

	@Override
	public String getUuid() {
		return null;
	}

	@Override
	public long getAllCount() {
		return 0;
	}
}
