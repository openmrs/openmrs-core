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

import static org.hamcrest.Matchers.hasItems;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.openmrs.api.APIException;
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
	
	@Rule
	public ExpectedException expectedException = ExpectedException.none();
	
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
	public void shouldPlaceATestOrder() throws Exception {
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
	 * @verifies void an order
	 * @see OrderService#voidOrder(org.openmrs.Order, String)
	 */
	@Test
	public void shouldVoidAnOrderAndFlushSuccessfully() throws Exception {
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
	 * @verifies unvoid an order
	 * @see OrderService#unvoidOrder(org.openmrs.Order)
	 */
	@Test
	public void shouldUnvoidAnOrderAndFlushSuccessfully() throws Exception {
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
	public void shouldDiscontinueAnActiveOrderAndFlushSuccessfully() throws Exception {
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
	public void shouldReviseAnOrderAndFlushSuccessfully() throws Exception {
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
	public void shouldFailIfAnEditedOrderIsFlushed() throws Exception {
		Encounter encounter = encounterService.getEncounter(3);
		assertFalse(encounter.getOrders().isEmpty());
		encounter.getOrders().iterator().next().setInstructions("new");
		expectedException.expect(APIException.class);
		expectedException.expectMessage(Matchers.is("editing.fields.not.allowed"));
		encounterService.saveEncounter(encounter);
		Context.flushSession();
	}
	
	/**
	 * This test ensures that the getter for previous order field returns objects of the actual sub
	 * types for subclasses instead proxies that are instances of Order
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldNotReturnAProxyForPreviousOrder() throws Exception {
		Order dcOrder = orderService.getOrder(22);
		Order previousOrder = dcOrder.getPreviousOrder();
		assertNotNull(previousOrder);
		DrugOrder previousDrugOrder = (DrugOrder) previousOrder;
		
		Order testOrder = orderService.getOrder(7);
		Order dcTestOrder = orderService.discontinueOrder(testOrder, "Testing", null, testOrder.getOrderer(), testOrder
		        .getEncounter());
		Context.flushSession();
		Context.clearSession();
		dcTestOrder = (TestOrder) orderService.getOrder(dcTestOrder.getOrderId()).getPreviousOrder();
	}
	
	@Test
	public void shouldAllowEditingADiscontinuationOrder() throws Exception {
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
		assertTrue(originalDCOrder.isVoided());
		List<Order> newPatientOrders = orderService.getAllOrdersByPatient(originalDCOrder.getPatient());
		assertEquals(originalPatientOrders.size() + 1, newPatientOrders.size());
		Collection<Order> newOrders = CollectionUtils.disjunction(originalPatientOrders, newPatientOrders);
		assertEquals(1, newOrders.size());
		assertEquals(newOrders.iterator().next().getPreviousOrder(), previousOrder);
	}
	
	@Test
	public void shouldAllowRetrospectiveDataEntryOfOrders() throws Exception {
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
		assertEquals(stopDate, order.getDateStopped());
		assertEquals(stopDate, dcOrder.getAutoExpireDate());
	}
}
