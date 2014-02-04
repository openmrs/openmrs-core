/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs;

import static org.hamcrest.Matchers.hasItems;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.util.Date;
import java.util.List;

import org.junit.Test;
import org.openmrs.api.ConceptService;
import org.openmrs.api.OrderService;
import org.openmrs.api.PatientService;
import org.openmrs.test.BaseContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Contains end to end tests for order entry operations i.g placing, discontinuing revising an order
 */
public class OrderEntryIntegrationTest extends BaseContextSensitiveTest {
	
	protected static final String ORDER_ENTRY_DATASET_XML = "org/openmrs/api/include/OrderEntryTest-other.xml";
	
	@Autowired
	private OrderService orderService;
	
	@Autowired
	private PatientService patientService;
	
	@Autowired
	private ConceptService conceptService;
	
	@Test
	public void shouldPlaceATestOrder() throws Exception {
		executeDataSet(ORDER_ENTRY_DATASET_XML);
		Patient patient = patientService.getPatient(7);
		CareSetting careSetting = orderService.getCareSetting(1);
		assertEquals(0, orderService.getActiveOrders(patient, TestOrder.class, careSetting, null).size());
		
		//place test order
		TestOrder order = new TestOrder();
		order.setPatient(patient);
		order.setConcept(conceptService.getConcept(5497));
		order.setCareSetting(careSetting);
		order.setStartDate(new Date());
		order.setClinicalHistory("Patient had a negative reaction to the test in the past");
		order.setFrequency(orderService.getOrderFrequency(1));
		order.setSpecimenSource(conceptService.getConcept(1000));
		order.setNumberOfRepeats(3);
		
		orderService.saveOrder(order);
		List<TestOrder> activeOrders = orderService.getActiveOrders(patient, TestOrder.class, careSetting, null);
		assertEquals(1, activeOrders.size());
		assertThat(activeOrders, hasItems(order));
	}
}
