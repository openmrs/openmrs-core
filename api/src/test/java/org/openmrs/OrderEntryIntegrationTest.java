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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.List;

import org.junit.Test;
import org.openmrs.api.ConceptService;
import org.openmrs.api.OrderService;
import org.openmrs.api.PatientService;
import org.openmrs.api.ProviderService;
import org.openmrs.api.context.Context;
import org.openmrs.order.OrderUtil;
import org.openmrs.test.BaseContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Contains end to end tests for order entry operations i.g placing, discontinuing revising an order
 */
public class OrderEntryIntegrationTest extends BaseContextSensitiveTest {
	
	protected static final String ORDER_ENTRY_DATASET_XML = "org/openmrs/api/include/OrderEntryIntegrationTest-other.xml";
	
	@Autowired
	private OrderService orderService;
	
	@Autowired
	private PatientService patientService;
	
	@Autowired
	private ConceptService conceptService;
	
	@Autowired
	private ProviderService providerService;
	
	@Test
	public void shouldGetTheActiveOrdersForAPatient() {
		Patient patient = patientService.getPatient(2);
		List<Order> activeOrders = orderService.getActiveOrders(patient, null, null, null);
		assertEquals(5, activeOrders.size());
		Order[] expectedOrders = { orderService.getOrder(3), orderService.getOrder(5), orderService.getOrder(7),
		        orderService.getOrder(222), orderService.getOrder(444) };
		assertThat(activeOrders, hasItems(expectedOrders));
	}
	
	@Test
	public void shouldGetTheActiveDrugOrdersForAPatient() {
		Patient patient = patientService.getPatient(2);
		List<DrugOrder> activeDrugOrders = orderService.getActiveOrders(patient, DrugOrder.class, null, null);
		assertEquals(2, activeDrugOrders.size());
		DrugOrder[] expectedDrugOrders = { (DrugOrder) orderService.getOrder(3), (DrugOrder) orderService.getOrder(5) };
		assertThat(activeDrugOrders, hasItems(expectedDrugOrders));
	}
	
	@Test
	public void shouldPlaceADrugOrder() throws Exception {
		executeDataSet(ORDER_ENTRY_DATASET_XML);
		Patient patient = patientService.getPatient(7);
		CareSetting careSetting = orderService.getCareSetting(1);
		int activeDrugOrderCount = orderService.getActiveOrders(patient, DrugOrder.class, careSetting, null).size();
		
		//place drug order
		DrugOrder order = new DrugOrder();
		order.setPatient(patient);
		order.setConcept(conceptService.getConcept(5497));
		order.setCareSetting(careSetting);
		order.setOrderer(Context.getProviderService().getProvider(1));
		order.setStartDate(new Date());
		order.setDrug(conceptService.getDrug(1));
		order.setDosingType(DrugOrder.DosingType.SIMPLE);
		order.setDose(300.0);
		Concept mgs = conceptService.getConcept(50);
		order.setDoseUnits(mgs);
		order.setQuantity(20.0);
		Concept tabs = conceptService.getConcept(51);
		order.setQuantityUnits(tabs);
		order.setDuration(20.0);
		Concept days = conceptService.getConcept(1002);
		order.setDurationUnits(days);
		OrderFrequency onceDaily = orderService.getOrderFrequency(3000);
		order.setFrequency(onceDaily);
		
		orderService.saveOrder(order);
		List<DrugOrder> activeOrders = orderService.getActiveOrders(patient, DrugOrder.class, careSetting, null);
		assertEquals(++activeDrugOrderCount, activeOrders.size());
		assertThat(activeOrders, hasItems(order));
	}
	
	@Test
	public void shouldPlaceATestOrder() throws Exception {
		executeDataSet(ORDER_ENTRY_DATASET_XML);
		Patient patient = patientService.getPatient(7);
		CareSetting careSetting = orderService.getCareSetting(1);
		int activeTestOrderCount = orderService.getActiveOrders(patient, TestOrder.class, careSetting, null).size();
		
		//place test order
		TestOrder order = new TestOrder();
		order.setPatient(patient);
		order.setConcept(conceptService.getConcept(5497));
		order.setOrderer(providerService.getProvider(1));
		order.setCareSetting(careSetting);
		order.setStartDate(new Date());
		order.setClinicalHistory("Patient had a negative reaction to the test in the past");
		order.setFrequency(orderService.getOrderFrequency(3000));
		order.setSpecimenSource(conceptService.getConcept(1000));
		order.setNumberOfRepeats(3);
		
		orderService.saveOrder(order);
		List<TestOrder> activeOrders = orderService.getActiveOrders(patient, TestOrder.class, careSetting, null);
		assertEquals(++activeTestOrderCount, activeOrders.size());
		assertThat(activeOrders, hasItems(order));
	}
	
	@Test
	public void shouldDiscontinueAnActiveOrder() throws Exception {
		executeDataSet(ORDER_ENTRY_DATASET_XML);
		executeDataSet("org/openmrs/api/include/OrderServiceTest-discontinueReason.xml");
		
		Order firstOrderToDiscontinue = orderService.getOrder(3);
		Encounter encounter = Context.getEncounterService().getEncounter(3);
		assertTrue(OrderUtil.isOrderActive(firstOrderToDiscontinue, null));
		Patient patient = firstOrderToDiscontinue.getPatient();
		int ordersCount = orderService.getActiveOrders(patient, null, null, null).size();
		
		Concept discontinueReason = Context.getConceptService().getConcept(1);
		Order discontinuationOrder1 = orderService.discontinueOrder(firstOrderToDiscontinue, discontinueReason, null, null,
		    encounter);
		assertEquals(firstOrderToDiscontinue, discontinuationOrder1.getPreviousOrder());
		
		//Lets discontinue another order with reason being a string instead of concept
		Order secondOrderToDiscontinue = orderService.getOrder(5);
		assertEquals(patient, secondOrderToDiscontinue.getPatient());
		assertTrue(OrderUtil.isOrderActive(secondOrderToDiscontinue, null));
		Order discontinuationOrder2 = orderService.discontinueOrder(secondOrderToDiscontinue, "Testing", null, null,
		    encounter);
		assertEquals(secondOrderToDiscontinue, discontinuationOrder2.getPreviousOrder());
		
		//Lets discontinue another order by saving a DC order
		Order thirdOrderToDiscontinue = orderService.getOrder(7);
		assertTrue(OrderUtil.isOrderActive(thirdOrderToDiscontinue, null));
		Order discontinuationOrder = thirdOrderToDiscontinue.cloneForDiscontinuing();
		discontinuationOrder.setOrderer(providerService.getProvider(1));
		orderService.saveOrder(discontinuationOrder);
		
		List<Order> activeOrders = orderService.getActiveOrders(patient, null, null, null);
		assertEquals(ordersCount - 3, activeOrders.size());
		assertFalse(activeOrders.contains(firstOrderToDiscontinue));
		assertFalse(activeOrders.contains(secondOrderToDiscontinue));
		assertFalse(activeOrders.contains(thirdOrderToDiscontinue));
	}
	
	@Test
	public void shouldReviseAnOrder() throws Exception {
		Order originalOrder = orderService.getOrder(111);
		assertTrue(OrderUtil.isOrderActive(originalOrder, null));
		final Patient patient = originalOrder.getPatient();
		List<Order> originalActiveOrders = orderService.getActiveOrders(patient, null, null, null);
		final int originalOrderCount = originalActiveOrders.size();
		assertTrue(originalActiveOrders.contains(originalOrder));
		
		Order revisedOrder = originalOrder.cloneForRevision();
		revisedOrder.setInstructions("Take after a meal");
		revisedOrder.setStartDate(new Date());
		revisedOrder.setOrderer(providerService.getProvider(1));
		orderService.saveOrder(revisedOrder);
		
		List<Order> activeOrders = orderService.getActiveOrders(patient, null, null, null);
		assertEquals(originalOrderCount, activeOrders.size());
		assertFalse(OrderUtil.isOrderActive(originalOrder, null));
	}
}
