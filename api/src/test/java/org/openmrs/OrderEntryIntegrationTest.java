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
import org.openmrs.api.EncounterService;
import org.openmrs.api.OrderService;
import org.openmrs.api.PatientService;
import org.openmrs.api.ProviderService;
import org.openmrs.api.context.Context;
import org.openmrs.order.OrderUtilTest;
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
	
	@Autowired
	private EncounterService encounterService;
	
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
		List<Order> activeDrugOrders = orderService.getActiveOrders(patient, orderService.getOrderTypeByName("Drug order"),
		    null, null);
		assertEquals(4, activeDrugOrders.size());
		Order[] expectedDrugOrders = { orderService.getOrder(222), orderService.getOrder(3), orderService.getOrder(444),
		        orderService.getOrder(5) };
		assertThat(activeDrugOrders, hasItems(expectedDrugOrders));
	}
	
	@Test
	public void shouldPlaceADrugOrder() throws Exception {
		executeDataSet(ORDER_ENTRY_DATASET_XML);
		Patient patient = patientService.getPatient(7);
		CareSetting careSetting = orderService.getCareSetting(1);
		int activeDrugOrderCount = orderService.getActiveOrders(patient, orderService.getOrderTypeByName("Drug order"),
		    careSetting, null).size();
		
		//place drug order
		DrugOrder order = new DrugOrder();
		Encounter encounter = encounterService.getEncounter(3);
		order.setEncounter(encounter);
		order.setPatient(patient);
		order.setConcept(conceptService.getConcept(88));
		order.setCareSetting(careSetting);
		order.setOrderer(Context.getProviderService().getProvider(1));
		order.setStartDate(encounter.getEncounterDatetime());
		order.setOrderType(orderService.getOrderTypeByName("Drug order"));
		order.setDrug(conceptService.getDrug(3));
		order.setDosingType(DrugOrder.DosingType.SIMPLE);
		order.setDose(300.0);
		order.setDoseUnits(conceptService.getConcept(50));
		order.setQuantity(20.0);
		order.setQuantityUnits(conceptService.getConcept(51));
		order.setDuration(20.0);
		order.setDurationUnits(conceptService.getConcept(1002));
		order.setFrequency(orderService.getOrderFrequency(3000));
		order.setRoute(conceptService.getConcept(22));
		order.setNumRefills(10);
		
		orderService.saveOrder(order, null);
		List<Order> activeOrders = orderService.getActiveOrders(patient, orderService.getOrderTypeByName("Drug order"),
		    careSetting, null);
		assertEquals(++activeDrugOrderCount, activeOrders.size());
		assertTrue(activeOrders.contains(order));
	}
	
	@Test
	public void shouldPlaceATestOrder() throws Exception {
		executeDataSet(ORDER_ENTRY_DATASET_XML);
		Patient patient = patientService.getPatient(7);
		CareSetting careSetting = orderService.getCareSetting(1);
		int activeTestOrderCount = orderService.getActiveOrders(patient, orderService.getOrderTypeByName("Test order"),
		    careSetting, null).size();
		
		//place test order
		TestOrder order = new TestOrder();
		order.setPatient(patient);
		order.setOrderType(orderService.getOrderTypeByName("Test order"));
		order.setEncounter(encounterService.getEncounter(3));
		order.setConcept(conceptService.getConcept(5497));
		order.setOrderer(providerService.getProvider(1));
		order.setCareSetting(careSetting);
		order.setEncounter(encounterService.getEncounter(3));
		order.setStartDate(new Date());
		order.setClinicalHistory("Patient had a negative reaction to the test in the past");
		order.setFrequency(orderService.getOrderFrequency(3000));
		order.setSpecimenSource(conceptService.getConcept(1000));
		order.setNumberOfRepeats(3);
		
		orderService.saveOrder(order, null);
		List<Order> activeOrders = orderService.getActiveOrders(patient, orderService.getOrderTypeByName("Test order"),
		    careSetting, null);
		assertEquals(++activeTestOrderCount, activeOrders.size());
		assertTrue(activeOrders.contains(order));
	}
	
	@Test
	public void shouldDiscontinueAnActiveOrder() throws Exception {
		executeDataSet(ORDER_ENTRY_DATASET_XML);
		executeDataSet("org/openmrs/api/include/OrderServiceTest-discontinueReason.xml");
		
		Order firstOrderToDiscontinue = orderService.getOrder(3);
		Encounter encounter = encounterService.getEncounter(3);
		assertTrue(OrderUtilTest.isActiveOrder(firstOrderToDiscontinue, null));
		Patient patient = firstOrderToDiscontinue.getPatient();
		int ordersCount = orderService.getActiveOrders(patient, null, null, null).size();
		
		Concept discontinueReason = Context.getConceptService().getConcept(1);
		Provider orderer = providerService.getProvider(1);
		Order discontinuationOrder1 = orderService.discontinueOrder(firstOrderToDiscontinue, discontinueReason, null,
		    orderer, encounter);
		assertEquals(firstOrderToDiscontinue, discontinuationOrder1.getPreviousOrder());
		
		//Lets discontinue another order with reason being a string instead of concept
		Order secondOrderToDiscontinue = orderService.getOrder(5);
		assertEquals(patient, secondOrderToDiscontinue.getPatient());
		assertTrue(OrderUtilTest.isActiveOrder(secondOrderToDiscontinue, null));
		Order discontinuationOrder2 = orderService.discontinueOrder(secondOrderToDiscontinue, "Testing", null, orderer,
		    encounter);
		assertEquals(secondOrderToDiscontinue, discontinuationOrder2.getPreviousOrder());
		
		//Lets discontinue another order by saving a DC order
		Order thirdOrderToDiscontinue = orderService.getOrder(7);
		assertTrue(OrderUtilTest.isActiveOrder(thirdOrderToDiscontinue, null));
		Order discontinuationOrder = thirdOrderToDiscontinue.cloneForDiscontinuing();
		discontinuationOrder.setOrderer(orderer);
		discontinuationOrder.setEncounter(encounterService.getEncounter(6));
		orderService.saveOrder(discontinuationOrder, null);
		
		Thread.sleep(1000);
		List<Order> activeOrders = orderService.getActiveOrders(patient, null, null, null);
		assertEquals(ordersCount - 3, activeOrders.size());
		assertFalse(activeOrders.contains(firstOrderToDiscontinue));
		assertFalse(activeOrders.contains(secondOrderToDiscontinue));
		assertFalse(activeOrders.contains(thirdOrderToDiscontinue));
	}
	
	@Test
	public void shouldReviseAnOrder() throws Exception {
		Order originalOrder = orderService.getOrder(111);
		assertTrue(OrderUtilTest.isActiveOrder(originalOrder, null));
		final Patient patient = originalOrder.getPatient();
		List<Order> originalActiveOrders = orderService.getActiveOrders(patient, null, null, null);
		final int originalOrderCount = originalActiveOrders.size();
		assertTrue(originalActiveOrders.contains(originalOrder));
		
		Order revisedOrder = originalOrder.cloneForRevision();
		revisedOrder.setInstructions("Take after a meal");
		revisedOrder.setStartDate(new Date());
		revisedOrder.setOrderer(providerService.getProvider(1));
		revisedOrder.setEncounter(encounterService.getEncounter(3));
		orderService.saveOrder(revisedOrder, null);
		
		//If the time is too close, the original order may be returned because it
		//dateStopped will be exactly the same as the asOfDate(now) to the millisecond
		Thread.sleep(1);
		List<Order> activeOrders = orderService.getActiveOrders(patient, null, null, null);
		assertEquals(originalOrderCount, activeOrders.size());
		assertFalse(OrderUtilTest.isActiveOrder(originalOrder, null));
	}
}
