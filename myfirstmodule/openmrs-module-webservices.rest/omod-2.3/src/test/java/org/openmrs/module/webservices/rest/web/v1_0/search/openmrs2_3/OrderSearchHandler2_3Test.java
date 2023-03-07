/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.web.v1_0.search.openmrs2_3;

import org.apache.commons.beanutils.PropertyUtils;
import org.junit.Assert;
import org.junit.Test;
import org.openmrs.Order;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.v1_0.controller.RestControllerTestUtils;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;

public class OrderSearchHandler2_3Test extends RestControllerTestUtils {

	protected String getURI() {
		return "order";
	}

	/**
	 * @verifies returns orders matching autoExpireOnOrBeforeDate
	 * @see OrderSearchHandler2_3#search(RequestContext)
	 */
	@Test
	public void getSearchConfig_shouldReturnOrdersAutoExpiredBeforeDate() throws Exception {
		MockHttpServletRequest req = request(RequestMethod.GET, getURI());
		req.addParameter("s", "default");
		req.addParameter("autoExpireOnOrBeforeDate", "2008-09-30");

		SimpleObject result = deserialize(handle(req));
		List<Order> orders = result.get("results");
		Assert.assertEquals(4, orders.size());
	}

	/**
	 * @verifies returns orders with dateStopped not null
	 * @see OrderSearchHandler2_3#search(RequestContext)
	 */
	@Test
	public void getSearchConfig_shouldReturnStoppedOrders() throws Exception {
		MockHttpServletRequest req = request(RequestMethod.GET, getURI());
		req.addParameter("s", "default");
		req.addParameter("isStopped", "true");

		SimpleObject result = deserialize(handle(req));
		List<Order> orders = result.get("results");
		Assert.assertEquals(4, orders.size());
	}

	/**
	 * @verifies returns orders matching autoExpireOnOrBeforeDate
	 * @see OrderSearchHandler2_3#search(RequestContext)
	 */
	@Test
	public void getSearchConfig_shouldReturnOnlyCanceledOrAutoExpiredBeforeDate() throws Exception {
		MockHttpServletRequest req = request(RequestMethod.GET, getURI());
		req.addParameter("s", "default");
		req.addParameter("canceledOrExpiredOnOrBeforeDate", "2008-09-30");

		SimpleObject result = deserialize(handle(req));
		List<Order> orders = result.get("results");
		Assert.assertEquals(7, orders.size());
	}

	/**
	 * @verifies returns orders matching fulfillerStatus
	 * @see OrderSearchHandler2_3#search(RequestContext)
	 */
	@Test
	public void getSearchConfig_shouldReturnOrdersWithFulfillerStatusCompleted() throws Exception {
		MockHttpServletRequest req = request(RequestMethod.GET, getURI());
		req.addParameter("s", "default");
		req.addParameter("fulfillerStatus", "COMPLETED");

		SimpleObject result = deserialize(handle(req));
		List<Order> orders = result.get("results");
		Assert.assertEquals(1, orders.size());
	}

	/**
	 * @verifies returns orders matching fulfillerStatus RECEIVED or null
	 * @see OrderSearchHandler2_3#search(RequestContext)
	 */
	@Test
	public void getSearchConfig_shouldReturnOrdersWithFulfillerStatusReceivedOrNull() throws Exception {
		MockHttpServletRequest req = request(RequestMethod.GET, getURI());
		req.addParameter("s", "default");
		req.addParameter("v", "custom:(id,uuid,display,orderNumber,dateActivated,fulfillerStatus)");
		req.addParameter("fulfillerStatus", "RECEIVED");
		req.addParameter("includeNullFulfillerStatus", "true");

		SimpleObject result = deserialize(handle(req));
		List<Object> orders = (List<Object>) result.get("results");
		Assert.assertEquals(12, orders.size());
		for (Object order : orders) {
			Object fulfillerStatus = PropertyUtils.getProperty(order, "fulfillerStatus");
			if (fulfillerStatus != null) {
				Assert.assertEquals("RECEIVED", fulfillerStatus);
			} else {
				Assert.assertNull(fulfillerStatus);
			}
		}
	}

	/**
	 * @verifies returns orders matching fulfillerStatus not null
	 * @see OrderSearchHandler2_3#search(RequestContext)
	 */
	@Test
	public void getSearchConfig_shouldReturnOrdersWithFulfillerStatusNotNull() throws Exception {
		MockHttpServletRequest req = request(RequestMethod.GET, getURI());
		req.addParameter("s", "default");
		req.addParameter("v", "custom:(id,uuid,display,orderNumber,dateActivated,fulfillerStatus)");
		req.addParameter("includeNullFulfillerStatus", "false");

		SimpleObject result = deserialize(handle(req));
		List<Object> orders = (List<Object>) result.get("results");
		Assert.assertEquals(3, orders.size());
		for (Object order : orders) {
			Assert.assertNotNull(PropertyUtils.getProperty(order, "fulfillerStatus"));
		}
	}

	/**
	 * @verifies returns orders matching fulfillerStatus = null
	 * @see OrderSearchHandler2_3#search(RequestContext)
	 */
	@Test
	public void getSearchConfig_shouldReturnOrdersWithFulfillerStatusNull() throws Exception {
		MockHttpServletRequest req = request(RequestMethod.GET, getURI());
		req.addParameter("s", "default");
		req.addParameter("v", "custom:(id,uuid,display,orderNumber,dateActivated,fulfillerStatus)");
		req.addParameter("includeNullFulfillerStatus", "true");

		SimpleObject result = deserialize(handle(req));
		List<Object> orders = (List<Object>) result.get("results");
		Assert.assertEquals(10, orders.size());
		for (Object order : orders) {
			Assert.assertNull(PropertyUtils.getProperty(order, "fulfillerStatus"));
		}
	}

	/**
	 * @verifies returns orders exluding Canceled and Expired
	 * @see OrderSearchHandler2_3#search(RequestContext)
	 */
	@Test
	public void getSearchConfig_shouldNotReturnCanceledOrExpired() throws Exception {
		MockHttpServletRequest req = request(RequestMethod.GET, getURI());
		req.addParameter("s", "default");
		req.addParameter("excludeCanceledAndExpired", "true");

		SimpleObject result = deserialize(handle(req));
		List<Order> orders = result.get("results");
		Assert.assertEquals(6, orders.size());
	}

	/**
	 * @verifies returns orders matching action
	 * @see OrderSearchHandler2_3#search(RequestContext)
	 */
	@Test
	public void getSearchConfig_shouldReturnDiscontinuedOrders() throws Exception {
		MockHttpServletRequest req = request(RequestMethod.GET, getURI());
		req.addParameter("s", "default");
		req.addParameter("action", "DISCONTINUE");

		SimpleObject result = deserialize(handle(req));
		List<Order> orders = result.get("results");
		Assert.assertEquals(2, orders.size());
	}

	/**
	 * @verifies returns orders that are not DISCONTINUE
	 * @see OrderSearchHandler2_3#search(RequestContext)
	 */
	@Test
	public void getSearchConfig_shouldNotReturnDiscontinueOrders() throws Exception {
		MockHttpServletRequest req = request(RequestMethod.GET, getURI());
		req.addParameter("s", "default");
		req.addParameter("v", "custom:(id,uuid,display,orderNumber,dateActivated,fulfillerStatus,action)");
		req.addParameter("excludeDiscontinueOrders", "true");

		SimpleObject result = deserialize(handle(req));
		List<Object> orders = (List<Object>) result.get("results");
		Assert.assertEquals(11, orders.size());
		for (Object order : orders) {
			Assert.assertNotEquals(PropertyUtils.getProperty(order, "action"), "DISCONTINUE");
		}
	}

	@Test
    public void getSearchConfig_shouldReturnOrdersByOrderNumber() throws Exception {
        MockHttpServletRequest req = request(RequestMethod.GET, getURI());
        req.addParameter("orderNumber", "ORD-7");

        SimpleObject result = deserialize(handle(req));
        List<Order> orders = result.get("results");
        Assert.assertEquals(1, orders.size());
        Assert.assertEquals(PropertyUtils.getProperty(orders.get(0), "uuid"), "2c96f25c-4949-4f72-9931-d808fbc226df");
    }

    @Test
    public void getSearchConfig_shouldReturnOrdersByAccessionNumber() throws Exception {
        MockHttpServletRequest req = request(RequestMethod.GET, getURI());
        req.addParameter("accessionNumber", "ACC-123");

        SimpleObject result = deserialize(handle(req));
        List<Order> orders = result.get("results");
        Assert.assertEquals(1, orders.size());
        Assert.assertEquals(PropertyUtils.getProperty(orders.get(0), "uuid"), "e1f95924-697a-11e3-bd76-0800271c1b75");
    }

}
