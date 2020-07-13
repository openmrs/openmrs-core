/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.junit.jupiter.api.Test;
import org.openmrs.api.APIException;
import org.openmrs.api.ConceptService;
import org.openmrs.api.EncounterService;
import org.openmrs.api.OrderService;
import org.openmrs.api.PatientService;
import org.openmrs.api.ProviderService;
import org.openmrs.api.context.Context;
import org.openmrs.order.OrderUtilTest;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;
import org.openmrs.util.DateUtil;
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
	public void shouldPlaceADrugOrder() {
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
		order.setCareSetting(careSetting);
		order.setOrderer(Context.getProviderService().getProvider(1));
		order.setDateActivated(encounter.getEncounterDatetime());
		order.setDrug(conceptService.getDrug(2));
		order.setDosingType(SimpleDosingInstructions.class);
		order.setDose(300.0);
		order.setDoseUnits(conceptService.getConcept(50));
		order.setQuantity(20.0);
		order.setQuantityUnits(conceptService.getConcept(51));
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
	public void shouldPlaceATestOrder() {
		executeDataSet(ORDER_ENTRY_DATASET_XML);
		Patient patient = patientService.getPatient(7);
		CareSetting careSetting = orderService.getCareSetting(1);
		int activeTestOrderCount = orderService.getActiveOrders(patient, orderService.getOrderTypeByName("Test order"),
		    careSetting, null).size();
		
		//place test order
		TestOrder order = new TestOrder();
		order.setPatient(patient);
		order.setConcept(conceptService.getConcept(5497));
		order.setOrderer(providerService.getProvider(1));
		order.setCareSetting(careSetting);
		Encounter encounter = encounterService.getEncounter(3);
		order.setEncounter(encounter);
		order.setDateActivated(encounter.getEncounterDatetime());
		order.setClinicalHistory("Patient had a negative reaction to the test in the past");
		order.setFrequency(orderService.getOrderFrequency(3000));
		order.setSpecimenSource(conceptService.getConcept(22));
		order.setNumberOfRepeats(3);
		order.setFulfillerStatus(Order.FulfillerStatus.RECEIVED);
		order.setFulfillerComment("A comment from the filler");
		
		orderService.saveOrder(order, null);
		List<Order> activeOrders = orderService.getActiveOrders(patient, orderService.getOrderTypeByName("Test order"),
		    careSetting, null);
		assertEquals(++activeTestOrderCount, activeOrders.size());
		assertTrue(activeOrders.contains(order));
	}
	
	@Test
	public void shouldDiscontinueAnActiveOrder() {
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
		
		List<Order> activeOrders = orderService.getActiveOrders(patient, null, null, null);
		assertEquals(ordersCount - 3, activeOrders.size());
		assertFalse(activeOrders.contains(firstOrderToDiscontinue));
		assertFalse(activeOrders.contains(secondOrderToDiscontinue));
		assertFalse(activeOrders.contains(thirdOrderToDiscontinue));
	}
	
	@Test
	public void shouldReviseAnOrder() {
		Order originalOrder = orderService.getOrder(111);
		assertTrue(OrderUtilTest.isActiveOrder(originalOrder, null));
		final Patient patient = originalOrder.getPatient();
		List<Order> originalActiveOrders = orderService.getActiveOrders(patient, null, null, null);
		final int originalOrderCount = originalActiveOrders.size();
		assertTrue(originalActiveOrders.contains(originalOrder));
		
		Order revisedOrder = originalOrder.cloneForRevision();
		revisedOrder.setInstructions("Take after a meal");
		revisedOrder.setDateActivated(new Date());
		revisedOrder.setOrderer(providerService.getProvider(1));
		revisedOrder.setEncounter(encounterService.getEncounter(3));
		orderService.saveOrder(revisedOrder, null);
		
		List<Order> activeOrders = orderService.getActiveOrders(patient, null, null, null);
		assertEquals(originalOrderCount, activeOrders.size());
		assertEquals(revisedOrder.getDateActivated(), DateUtils.addSeconds(originalOrder.getDateStopped(), 1));
		assertFalse(OrderUtilTest.isActiveOrder(originalOrder, null));
	}
	
	/**
	 * @see OrderService#voidOrder(org.openmrs.Order, String)
	 */
	@Test
	public void shouldVoidAnOrderAndFlushSuccessfully() {
		Order order = orderService.getOrder(1);
		assertFalse(order.getVoided());
		assertNull(order.getDateVoided());
		assertNull(order.getVoidedBy());
		assertNull(order.getVoidReason());
		
		orderService.voidOrder(order, "None");
		//forces hibernate interceptors to get invoked
		Context.flushSession();
		assertTrue(order.getVoided());
		assertNotNull(order.getDateVoided());
		assertNotNull(order.getVoidedBy());
		assertNotNull(order.getVoidReason());
	}
	
	/**
	 * @see OrderService#unvoidOrder(org.openmrs.Order)
	 */
	@Test
	public void shouldUnvoidAnOrderAndFlushSuccessfully() {
		Order order = orderService.getOrder(8);
		assertTrue(order.getVoided());
		assertNotNull(order.getDateVoided());
		assertNotNull(order.getVoidedBy());
		assertNotNull(order.getVoidReason());
		
		orderService.unvoidOrder(order);
		Context.flushSession();
		assertFalse(order.getVoided());
		assertNull(order.getDateVoided());
		assertNull(order.getVoidedBy());
		assertNull(order.getVoidReason());
	}
	
	@Test
	public void shouldDiscontinueAnActiveOrderAndFlushSuccessfully() {
		executeDataSet(ORDER_ENTRY_DATASET_XML);
		executeDataSet("org/openmrs/api/include/OrderServiceTest-discontinueReason.xml");
		
		Order firstOrderToDiscontinue = orderService.getOrder(3);
		Encounter encounter = encounterService.getEncounter(3);
		assertTrue(OrderUtilTest.isActiveOrder(firstOrderToDiscontinue, null));
		
		Concept discontinueReason = Context.getConceptService().getConcept(1);
		Provider orderer = providerService.getProvider(1);
		Order discontinuationOrder1 = orderService.discontinueOrder(firstOrderToDiscontinue, discontinueReason, null,
		    orderer, encounter);
		Context.flushSession();
		assertEquals(firstOrderToDiscontinue, discontinuationOrder1.getPreviousOrder());
	}
	
	@Test
	public void shouldReviseAnOrderAndFlushSuccessfully() {
		Order originalOrder = orderService.getOrder(111);
		assertTrue(OrderUtilTest.isActiveOrder(originalOrder, null));
		final Patient patient = originalOrder.getPatient();
		List<Order> originalActiveOrders = orderService.getActiveOrders(patient, null, null, null);
		final int originalOrderCount = originalActiveOrders.size();
		assertTrue(originalActiveOrders.contains(originalOrder));
		
		Order revisedOrder = originalOrder.cloneForRevision();
		revisedOrder.setInstructions("Take after a meal");
		revisedOrder.setDateActivated(new Date());
		revisedOrder.setOrderer(providerService.getProvider(1));
		revisedOrder.setEncounter(encounterService.getEncounter(3));
		orderService.saveOrder(revisedOrder, null);
		Context.flushSession();
		
		List<Order> activeOrders = orderService.getActiveOrders(patient, null, null, null);
		assertEquals(originalOrderCount, activeOrders.size());
		assertFalse(OrderUtilTest.isActiveOrder(originalOrder, null));
	}
	
	@Test
	public void shouldFailIfAnEditedOrderIsFlushed() {
		Encounter encounter = encounterService.getEncounter(3);
		assertFalse(encounter.getOrders().isEmpty());
		encounter.getOrders().iterator().next().setInstructions("new");
		encounterService.saveEncounter(encounter);
		APIException exception = assertThrows(APIException.class, () -> Context.flushSession());
		assertThat(exception.getMessage(), is(Context.getMessageSourceService().getMessage("editing.fields.not.allowed", new Object[] { "[instructions]", Order.class.getSimpleName() }, null)));
	}
	
	/**
	 * This test ensures that the getter for previous order field returns objects of the actual sub
	 * types for subclasses instead proxies that are instances of Order
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldNotReturnAProxyForPreviousOrder() {
		Order dcOrder = orderService.getOrder(22);
		Order previousOrder = dcOrder.getPreviousOrder();
		assertNotNull(previousOrder);
		
		Order testOrder = orderService.getOrder(7);
		Order dcTestOrder = orderService.discontinueOrder(testOrder, "Testing", null, testOrder.getOrderer(), testOrder
		        .getEncounter());
		Context.flushSession();
		Context.clearSession();
		dcTestOrder = orderService.getOrder(dcTestOrder.getOrderId()).getPreviousOrder();
	}
	
	@Test
	public void shouldAllowEditingADiscontinuationOrder() {
		Order originalDCOrder = orderService.getOrder(22);
		assertEquals(Order.Action.DISCONTINUE, originalDCOrder.getAction());
		List<Order> originalPatientOrders = orderService.getAllOrdersByPatient(originalDCOrder.getPatient());
		final Order previousOrder = originalDCOrder.getPreviousOrder();
		assertNotNull(previousOrder);
		final Date newStartDate = originalDCOrder.getEncounter().getEncounterDatetime();
		
		Order newDcOrder = originalDCOrder.cloneForRevision();
		newDcOrder.setEncounter(originalDCOrder.getEncounter());
		newDcOrder.setOrderer(originalDCOrder.getOrderer());
		newDcOrder.setDateActivated(newStartDate);
		orderService.voidOrder(originalDCOrder, "To be replace with a new one");
		assertNull(originalDCOrder.getDateStopped());
		orderService.saveOrder(newDcOrder, null);
		
		//We need to flush so that we ensure the interceptor is okay with all this
		Context.flushSession();
		assertTrue(originalDCOrder.getVoided());
		List<Order> newPatientOrders = orderService.getAllOrdersByPatient(originalDCOrder.getPatient());
		assertEquals(originalPatientOrders.size() + 1, newPatientOrders.size());
		Collection<Order> newOrders = CollectionUtils.disjunction(originalPatientOrders, newPatientOrders);
		assertEquals(1, newOrders.size());
		assertEquals(newOrders.iterator().next().getPreviousOrder(), previousOrder);
	}
	
	@Test
	public void shouldAllowRetrospectiveDataEntryOfOrders() {
		Order order = new TestOrder();
		order.setPatient(patientService.getPatient(2));
		order.setCareSetting(orderService.getCareSetting(2));
		order.setConcept(conceptService.getConcept(5089));
		order.setEncounter(encounterService.getEncounter(6));
		order.setOrderer(providerService.getProvider(1));
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DAY_OF_WEEK, -1);
		order.setDateActivated(cal.getTime());
		orderService.saveOrder(order, null);
		
		cal.add(Calendar.HOUR_OF_DAY, -1);
		Date stopDate = cal.getTime();
		Order dcOrder = orderService.discontinueOrder(order, "Testing", stopDate, order.getOrderer(), order.getEncounter());
		Context.flushSession(); // ensures that order is flushed and that the drop milliseconds interceptor is called
		assertEquals(DateUtil.truncateToSeconds(stopDate), order.getDateStopped());
		assertEquals(DateUtil.truncateToSeconds(stopDate), dcOrder.getAutoExpireDate());
	}

	@Test
	public void shouldAllowRevisionOfOrdersInRetrospectiveDataEntry() {
		executeDataSet("org/openmrs/api/include/OrderServiceTest-drugOrderAutoExpireDate.xml");
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DAY_OF_WEEK, -20);

		Encounter encounter = new Encounter();
		encounter.setEncounterDatetime(cal.getTime());
		Patient patient = patientService.getPatient(6);
		encounter.setPatient(patient);
		encounter.setEncounterType(encounterService.getEncounterType(1));
		encounterService.saveEncounter(encounter);

		DrugOrder order = new DrugOrder();
		order.setEncounter(encounter);
		order.setPatient(patient);
		order.setCareSetting(orderService.getCareSetting(2));
		order.setOrderer(Context.getProviderService().getProvider(1));
		order.setDateActivated(encounter.getEncounterDatetime());
		order.setDrug(conceptService.getDrug(2));
		order.setDosingType(SimpleDosingInstructions.class);
		order.setDose(300.0);
		order.setDoseUnits(conceptService.getConcept(50));
		order.setQuantity(20.0);
		order.setOrderType(orderService.getOrderType(1));
		order.setQuantityUnits(conceptService.getConcept(51));
		order.setFrequency(orderService.getOrderFrequency(1));
		order.setRoute(conceptService.getConcept(22));
		order.setDuration(10);
		order.setDurationUnits(conceptService.getConcept(1001));
		orderService.saveOrder(order, null);

		cal.add(Calendar.DAY_OF_WEEK, 4);
		Encounter encounter2 = new Encounter();
		encounter2.setEncounterDatetime(cal.getTime());
		encounter2.setPatient(patient);
		encounter2.setEncounterType(encounterService.getEncounterType(1));
		encounterService.saveEncounter(encounter2);

		DrugOrder order2 = new DrugOrder();
		order2.setEncounter(encounter2);
		order2.setPatient(patient);
		order2.setCareSetting(orderService.getCareSetting(2));
		order2.setOrderer(Context.getProviderService().getProvider(1));
		order2.setDateActivated(encounter2.getEncounterDatetime());
		order2.setDrug(conceptService.getDrug(2));
		order2.setDosingType(SimpleDosingInstructions.class);
		order2.setDose(300.0);
		order2.setDoseUnits(conceptService.getConcept(50));
		order2.setQuantity(20.0);
		order2.setOrderType(orderService.getOrderType(1));
		order2.setQuantityUnits(conceptService.getConcept(51));
		order2.setFrequency(orderService.getOrderFrequency(1));
		order2.setRoute(conceptService.getConcept(22));
		order2.setDuration(20);
		order2.setDurationUnits(conceptService.getConcept(1001));
		order2.setAction(Order.Action.REVISE);
		order2.setPreviousOrder(order);
		orderService.saveRetrospectiveOrder(order2, null);

		assertEquals(DateUtils.addSeconds(order2.getDateActivated(), -1), order.getDateStopped());
	}
}
