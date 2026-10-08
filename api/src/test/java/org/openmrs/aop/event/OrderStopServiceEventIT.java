/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.aop.event;

import java.util.Date;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.Encounter;
import org.openmrs.Order;
import org.openmrs.api.OrderService;
import org.openmrs.api.context.Context;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class OrderStopServiceEventIT extends BaseContextSensitiveTest {

	@Autowired
	private ServiceEventTestListener serviceEventTestListener;

	private OrderService orderService;

	@BeforeEach
	public void setUp() {
		orderService = Context.getOrderService();
		serviceEventTestListener.clearOrderSaveEvents();
	}

	@Test
	public void discontinueOrder_shouldPublishSaveServiceEventForStoppedOrder() {
		executeDataSet("org/openmrs/api/include/OrderServiceTest-discontinueReason.xml");

		Order order = orderService.getOrder(111);
		Encounter encounter = Context.getEncounterService().getEncounter(3);
		Date discontinueDate = new Date();

		serviceEventTestListener.clearOrderSaveEvents();
		orderService.discontinueOrder(order, "no longer needed", discontinueDate, order.getOrderer(), encounter);

		assertEquals(1, serviceEventTestListener.getOrderSaveEvents().size());
		Order stoppedOrder = serviceEventTestListener.getOrderSaveEvents().get(0).getEntity();
		assertEquals(order.getOrderId(), stoppedOrder.getOrderId());
		assertNotNull(stoppedOrder.getDateStopped());
	}
}
