/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.openmrs.test.OpenmrsMatchers.hasId;
import static org.openmrs.test.TestUtil.containsId;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.commons.lang3.time.DateUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.openmrs.CareSetting;
import org.openmrs.Concept;
import org.openmrs.ConceptClass;
import org.openmrs.ConceptName;
import org.openmrs.Drug;
import org.openmrs.DrugOrder;
import org.openmrs.Encounter;
import org.openmrs.FreeTextDosingInstructions;
import org.openmrs.GlobalProperty;
import org.openmrs.Obs;
import org.openmrs.Order;
import org.openmrs.Order.Action;
import org.openmrs.OrderFrequency;
import org.openmrs.OrderType;
import org.openmrs.Patient;
import org.openmrs.Provider;
import org.openmrs.SimpleDosingInstructions;
import org.openmrs.TestOrder;
import org.openmrs.api.context.Context;
import org.openmrs.order.OrderUtil;
import org.openmrs.order.OrderUtilTest;
import org.openmrs.orders.TimestampOrderNumberGenerator;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.test.TestUtil;
import org.openmrs.test.Verifies;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.util.PrivilegeConstants;

/**
 * TODO clean up and test all methods in OrderService
 */
public class OrderServiceTest extends BaseContextSensitiveTest {
	
	private static final String OTHER_ORDER_FREQUENCIES_XML = "org/openmrs/api/include/OrderServiceTest-otherOrderFrequencies.xml";
	
	private ConceptService conceptService;
	
	private OrderService orderService;
	
	private PatientService patientService;
	
	private EncounterService encounterService;
	
	private ProviderService providerService;
	
	private AdministrationService adminService;
	
	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	private class SomeTestOrder extends TestOrder {}
	
	@Before
	public void setup() {
		if (orderService == null) {
			orderService = Context.getOrderService();
		}
		if (patientService == null) {
			patientService = Context.getPatientService();
		}
		
		if (conceptService == null) {
			conceptService = Context.getConceptService();
		}
		
		if (encounterService == null) {
			encounterService = Context.getEncounterService();
		}
		if (providerService == null) {
			providerService = Context.getProviderService();
		}
		if (adminService == null) {
			adminService = Context.getAdministrationService();
		}
	}
	
	/**
	 * @see {@link OrderService#saveOrder(org.openmrs.Order, OrderContext)}
	 */
	@Test
	@Verifies(value = "should not save order if order doesnt validate", method = "saveOrder(Order)")
	public void saveOrder_shouldNotSaveOrderIfOrderDoesntValidate() throws Exception {
		Order order = new Order();
		order.setPatient(null);
		order.setOrderer(null);
		expectedException.expect(APIException.class);
		expectedException.expectMessage("failed to validate with reason:");
		orderService.saveOrder(order, null);
	}
	
	/**
	 * @see {@link OrderService#getOrderByUuid(String)}
	 */
	@Test
	@Verifies(value = "should find object given valid uuid", method = "getOrderByUuid(String)")
	public void getOrderByUuid_shouldFindObjectGivenValidUuid() throws Exception {
		String uuid = "921de0a3-05c4-444a-be03-e01b4c4b9142";
		Order order = orderService.getOrderByUuid(uuid);
		Assert.assertEquals(1, (int) order.getOrderId());
	}
	
	/**
	 * @see {@link OrderService#getOrderByUuid(String)}
	 */
	@Test
	@Verifies(value = "should return null if no object found with given uuid", method = "getOrderByUuid(String)")
	public void getOrderByUuid_shouldReturnNullIfNoObjectFoundWithGivenUuid() throws Exception {
		Assert.assertNull(orderService.getOrderByUuid("some invalid uuid"));
	}
	
	/**
	 * @verifies delete any Obs associated to the order when cascade is true
	 * @see OrderService#purgeOrder(org.openmrs.Order, boolean)
	 */
	@Test
	public void purgeOrder_shouldDeleteAnyObsAssociatedToTheOrderWhenCascadeIsTrue() throws Exception {
		executeDataSet("org/openmrs/api/include/OrderServiceTest-deleteObsThatReference.xml");
		final String ordUuid = "0c96f25c-4949-4f72-9931-d808fbcdb612";
		final String obsUuid = "be3a4d7a-f9ab-47bb-aaad-bc0b452fcda4";
		ObsService os = Context.getObsService();
		
		Obs obs = os.getObsByUuid(obsUuid);
		Assert.assertNotNull(obs);
		
		Order order = orderService.getOrderByUuid(ordUuid);
		Assert.assertNotNull(order);
		
		//sanity check to ensure that the obs and order are actually related
		Assert.assertEquals(order, obs.getOrder());
		
		//Ensure that passing false does not delete the related obs
		orderService.purgeOrder(order, false);
		Assert.assertNotNull(os.getObsByUuid(obsUuid));
		
		orderService.purgeOrder(order, true);
		
		//Ensure that actually the order got purged
		Assert.assertNull(orderService.getOrderByUuid(ordUuid));
		
		//Ensure that the related obs got deleted
		Assert.assertNull(os.getObsByUuid(obsUuid));
	}
	
	/**
	 * @verifies delete order from the database
	 * @see OrderService#purgeOrder(org.openmrs.Order, boolean)
	 */
	@Test
	public void purgeOrder_shouldDeleteOrderFromTheDatabase() throws Exception {
		final String uuid = "9c21e407-697b-11e3-bd76-0800271c1b75";
		Order order = orderService.getOrderByUuid(uuid);
		assertNotNull(order);
		orderService.purgeOrder(order);
		assertNull(orderService.getOrderByUuid(uuid));
	}
	
	/**
	 * @see {@link OrderNumberGenerator#getNewOrderNumber(OrderContext)}
	 */
	@Test
	@Verifies(value = "should always return unique orderNumbers when called multiple times without saving orders", method = "getNewOrderNumber()")
	public void getNewOrderNumber_shouldAlwaysReturnUniqueOrderNumbersWhenCalledMultipleTimesWithoutSavingOrders()
	        throws Exception {
		
		int N = 50;
		final Set<String> uniqueOrderNumbers = new HashSet<String>(50);
		List<Thread> threads = new ArrayList<Thread>();
		for (int i = 0; i < N; i++) {
			threads.add(new Thread(new Runnable() {
				
				@Override
				public void run() {
					try {
						Context.openSession();
						Context.addProxyPrivilege(PrivilegeConstants.ADD_ORDERS);
						uniqueOrderNumbers.add(((OrderNumberGenerator) orderService).getNewOrderNumber(null));
					}
					finally {
						Context.removeProxyPrivilege(PrivilegeConstants.ADD_ORDERS);
						Context.closeSession();
					}
				}
			}));
		}
		for (int i = 0; i < N; ++i) {
			threads.get(i).start();
		}
		for (int i = 0; i < N; ++i) {
			threads.get(i).join();
		}
		//since we used a set we should have the size as N indicating that there were no duplicates
		Assert.assertEquals(N, uniqueOrderNumbers.size());
	}
	
	/**
	 * @see {@link OrderService#getOrderByOrderNumber(String)}
	 */
	@Test
	@Verifies(value = "should find object given valid order number", method = "getOrderByOrderNumber(String)")
	public void getOrderByOrderNumber_shouldFindObjectGivenValidOrderNumber() throws Exception {
		Order order = orderService.getOrderByOrderNumber("1");
		Assert.assertNotNull(order);
		Assert.assertEquals(1, (int) order.getOrderId());
	}
	
	/**
	 * @see {@link OrderService#getOrderByOrderNumber(String)}
	 */
	@Test
	@Verifies(value = "should return null if no object found with given order number", method = "getOrderByOrderNumber(String)")
	public void getOrderByOrderNumber_shouldReturnNullIfNoObjectFoundWithGivenOrderNumber() throws Exception {
		Assert.assertNull(orderService.getOrderByOrderNumber("some invalid order number"));
	}
	
	/**
	 * @see {@link OrderService#getOrderHistoryByConcept(Patient,Concept)}
	 */
	@Test
	@Verifies(value = "should return orders with the given concept", method = "getOrderHistoryByConcept(Patient,Concept)")
	public void getOrderHistoryByConcept_shouldReturnOrdersWithTheGivenConcept() throws Exception {
		//We should have two orders with this concept.
		Concept concept = Context.getConceptService().getConcept(88);
		Patient patient = Context.getPatientService().getPatient(2);
		List<Order> orders = orderService.getOrderHistoryByConcept(patient, concept);
		
		//They must be sorted by dateActivated starting with the latest
		Assert.assertEquals(3, orders.size());
		Assert.assertEquals(444, orders.get(0).getOrderId().intValue());
		Assert.assertEquals(44, orders.get(1).getOrderId().intValue());
		Assert.assertEquals(4, orders.get(2).getOrderId().intValue());
		
		concept = Context.getConceptService().getConcept(792);
		orders = orderService.getOrderHistoryByConcept(patient, concept);
		
		//They must be sorted by dateActivated starting with the latest
		Assert.assertEquals(4, orders.size());
		Assert.assertEquals(3, orders.get(0).getOrderId().intValue());
		Assert.assertEquals(222, orders.get(1).getOrderId().intValue());
		Assert.assertEquals(22, orders.get(2).getOrderId().intValue());
		Assert.assertEquals(2, orders.get(3).getOrderId().intValue());
	}
	
	/**
	 * @see {@link OrderService#getOrderHistoryByConcept(Patient, Concept)}
	 */
	@Test
	@Verifies(value = "should return empty list for concept without orders", method = "getOrderHistoryByConcept(Patient,Concept)")
	public void getOrderHistoryByConcept_shouldReturnEmptyListForConceptWithoutOrders() throws Exception {
		Concept concept = Context.getConceptService().getConcept(21);
		Patient patient = Context.getPatientService().getPatient(2);
		List<Order> orders = orderService.getOrderHistoryByConcept(patient, concept);
		Assert.assertEquals(0, orders.size());
	}
	
	/**
	 * @verifies reject a null concept
	 * @see OrderService#getOrderHistoryByConcept(org.openmrs.Patient, org.openmrs.Concept)
	 */
	@Test
	public void getOrderHistoryByConcept_shouldRejectANullConcept() throws Exception {
		expectedException.expect(IllegalArgumentException.class);
		expectedException.expectMessage("patient and concept are required");
		orderService.getOrderHistoryByConcept(new Patient(), null);
	}
	
	/**
	 * @verifies reject a null patient
	 * @see OrderService#getOrderHistoryByConcept(org.openmrs.Patient, org.openmrs.Concept)
	 */
	@Test
	public void getOrderHistoryByConcept_shouldRejectANullPatient() throws Exception {
		expectedException.expect(IllegalArgumentException.class);
		expectedException.expectMessage("patient and concept are required");
		orderService.getOrderHistoryByConcept(null, new Concept());
	}
	
	/**
	 * @see {@link OrderService#getOrderHistoryByOrderNumber(String)}
	 */
	@Test
	@Verifies(value = "should return all order history for given order number", method = "getOrderHistoryByOrderNumber(String)")
	public void getOrderHistoryByOrderNumber_shouldReturnAllOrderHistoryForGivenOrderNumber() throws Exception {
		List<Order> orders = orderService.getOrderHistoryByOrderNumber("111");
		assertEquals(2, orders.size());
		assertEquals(111, orders.get(0).getOrderId().intValue());
		assertEquals(1, orders.get(1).getOrderId().intValue());
	}
	
	/**
	 * @verifies return the order frequency that matches the specified id
	 * @see OrderService#getOrderFrequency(Integer)
	 */
	@Test
	public void getOrderFrequency_shouldReturnTheOrderFrequencyThatMatchesTheSpecifiedId() throws Exception {
		assertEquals("28090760-7c38-11e3-baa7-0800200c9a66", orderService.getOrderFrequency(1).getUuid());
	}
	
	/**
	 * @verifies return the order frequency that matches the specified uuid
	 * @see OrderService#getOrderFrequencyByUuid(String)
	 */
	@Test
	public void getOrderFrequencyByUuid_shouldReturnTheOrderFrequencyThatMatchesTheSpecifiedUuid() throws Exception {
		assertEquals(1, orderService.getOrderFrequencyByUuid("28090760-7c38-11e3-baa7-0800200c9a66").getOrderFrequencyId()
		        .intValue());
	}
	
	/**
	 * @verifies return the order frequency that matches the specified concept
	 * @see OrderService#getOrderFrequencyByConcept(org.openmrs.Concept)
	 */
	@Test
	public void getOrderFrequencyByConcept_shouldReturnTheOrderFrequencyThatMatchesTheSpecifiedConcept() throws Exception {
		Concept concept = conceptService.getConcept(4);
		assertEquals(3, orderService.getOrderFrequencyByConcept(concept).getOrderFrequencyId().intValue());
	}
	
	/**
	 * @verifies return only non retired order frequencies if includeRetired is set to false
	 * @see OrderService#getOrderFrequencies(boolean)
	 */
	@Test
	public void getOrderFrequencies_shouldReturnOnlyNonRetiredOrderFrequenciesIfIncludeRetiredIsSetToFalse()
	        throws Exception {
		List<OrderFrequency> orderFrequencies = orderService.getOrderFrequencies(false);
		assertEquals(2, orderFrequencies.size());
		assertTrue(containsId(orderFrequencies, 1));
		assertTrue(containsId(orderFrequencies, 2));
	}
	
	/**
	 * @verifies return all the order frequencies if includeRetired is set to true
	 * @see OrderService#getOrderFrequencies(boolean)
	 */
	@Test
	public void getOrderFrequencies_shouldReturnAllTheOrderFrequenciesIfIncludeRetiredIsSetToTrue() throws Exception {
		List<OrderFrequency> orderFrequencies = orderService.getOrderFrequencies(true);
		assertEquals(3, orderFrequencies.size());
		assertTrue(containsId(orderFrequencies, 1));
		assertTrue(containsId(orderFrequencies, 2));
		assertTrue(containsId(orderFrequencies, 3));
	}
	
	/**
	 * @verifies return all active orders for the specified patient
	 * @see OrderService#getActiveOrders(org.openmrs.Patient, org.openmrs.OrderType,
	 *      org.openmrs.CareSetting, java.util.Date)
	 */
	@Test
	public void getActiveOrders_shouldReturnAllActiveOrdersForTheSpecifiedPatient() throws Exception {
		Patient patient = Context.getPatientService().getPatient(2);
		List<Order> orders = orderService.getActiveOrders(patient, null, null, null);
		assertEquals(5, orders.size());
		Order[] expectedOrders = { orderService.getOrder(222), orderService.getOrder(3), orderService.getOrder(444),
		        orderService.getOrder(5), orderService.getOrder(7) };
		assertThat(orders, hasItems(expectedOrders));
		
		assertTrue(OrderUtilTest.isActiveOrder(orders.get(0), null));
		assertTrue(OrderUtilTest.isActiveOrder(orders.get(1), null));
		assertTrue(OrderUtilTest.isActiveOrder(orders.get(2), null));
		assertTrue(OrderUtilTest.isActiveOrder(orders.get(3), null));
		assertTrue(OrderUtilTest.isActiveOrder(orders.get(4), null));
	}
	
	/**
	 * @verifies return all active orders for the specified patient and care setting
	 * @see OrderService#getActiveOrders(org.openmrs.Patient, org.openmrs.OrderType,
	 *      org.openmrs.CareSetting, java.util.Date)
	 */
	@Test
	public void getActiveOrders_shouldReturnAllActiveOrdersForTheSpecifiedPatientAndCareSetting() throws Exception {
		Patient patient = patientService.getPatient(2);
		CareSetting careSetting = orderService.getCareSetting(1);
		List<Order> orders = orderService.getActiveOrders(patient, null, careSetting, null);
		assertEquals(4, orders.size());
		Order[] expectedOrders = { orderService.getOrder(3), orderService.getOrder(444), orderService.getOrder(5),
		        orderService.getOrder(7) };
		assertThat(orders, hasItems(expectedOrders));
	}
	
	/**
	 * @verifies return all active drug orders for the specified patient
	 * @see OrderService#getActiveOrders(org.openmrs.Patient, org.openmrs.OrderType,
	 *      org.openmrs.CareSetting, java.util.Date)
	 */
	@Test
	public void getActiveOrders_shouldReturnAllActiveDrugOrdersForTheSpecifiedPatient() throws Exception {
		Patient patient = patientService.getPatient(2);
		List<Order> orders = orderService.getActiveOrders(patient, orderService.getOrderType(1), null, null);
		assertEquals(4, orders.size());
		Order[] expectedOrders = { orderService.getOrder(222), orderService.getOrder(3), orderService.getOrder(444),
		        orderService.getOrder(5) };
		assertThat(orders, hasItems(expectedOrders));
	}
	
	/**
	 * @verifies return all active test orders for the specified patient
	 * @see OrderService#getActiveOrders(org.openmrs.Patient, org.openmrs.OrderType,
	 *      org.openmrs.CareSetting, java.util.Date)
	 */
	@Test
	public void getActiveOrders_shouldReturnAllActiveTestOrdersForTheSpecifiedPatient() throws Exception {
		Patient patient = patientService.getPatient(2);
		List<Order> orders = orderService
		        .getActiveOrders(patient, orderService.getOrderTypeByName("Test order"), null, null);
		assertEquals(1, orders.size());
		assertEquals(orders.get(0), orderService.getOrder(7));
	}
	
	/**
	 * @verifies fail if patient is null
	 * @see OrderService#getActiveOrders(org.openmrs.Patient, org.openmrs.OrderType,
	 *      org.openmrs.CareSetting, java.util.Date)
	 */
	@Test
	public void getActiveOrders_shouldFailIfPatientIsNull() throws Exception {
		expectedException.expect(IllegalArgumentException.class);
		expectedException.expectMessage("Patient is required when fetching active orders");
		orderService.getActiveOrders(null, null, orderService.getCareSetting(1), null);
	}
	
	/**
	 * @verifies return active orders as of the specified date
	 * @see OrderService#getActiveOrders(org.openmrs.Patient, org.openmrs.OrderType,
	 *      org.openmrs.CareSetting, java.util.Date)
	 */
	@Test
	public void getActiveOrders_shouldReturnActiveOrdersAsOfTheSpecifiedDate() throws Exception {
		Patient patient = Context.getPatientService().getPatient(2);
		List<Order> orders = orderService.getAllOrdersByPatient(patient);
		assertEquals(12, orders.size());
		
		Date asOfDate = Context.getDateFormat().parse("10/12/2007");
		orders = orderService.getActiveOrders(patient, null, null, asOfDate);
		assertEquals(2, orders.size());
		assertFalse(orders.contains(orderService.getOrder(22)));//DC
		assertFalse(orders.contains(orderService.getOrder(44)));//DC
		assertFalse(orders.contains(orderService.getOrder(8)));//voided
		
		Order[] expectedOrders = { orderService.getOrder(9) };
		
		asOfDate = Context.getDateTimeFormat().parse("10/12/2007 00:01:00");
		orders = orderService.getActiveOrders(patient, null, null, asOfDate);
		assertEquals(1, orders.size());
		assertThat(orders, hasItems(expectedOrders));
		
		Order[] expectedOrders1 = { orderService.getOrder(3), orderService.getOrder(4), orderService.getOrder(222) };
		
		asOfDate = Context.getDateFormat().parse("10/04/2008");
		orders = orderService.getActiveOrders(patient, null, null, asOfDate);
		assertEquals(3, orders.size());
		assertThat(orders, hasItems(expectedOrders1));
		
		asOfDate = Context.getDateTimeFormat().parse("10/04/2008 00:01:00");
		orders = orderService.getActiveOrders(patient, null, null, asOfDate);
		assertEquals(2, orders.size());
		Order[] expectedOrders2 = { orderService.getOrder(222), orderService.getOrder(3) };
		assertThat(orders, hasItems(expectedOrders2));
		
		Order[] expectedOrders3 = { orderService.getOrder(222), orderService.getOrder(3), orderService.getOrder(444),
		        orderService.getOrder(5), orderService.getOrder(6) };
		asOfDate = Context.getDateTimeFormat().parse("26/09/2008 09:24:10");
		orders = orderService.getActiveOrders(patient, null, null, asOfDate);
		assertEquals(5, orders.size());
		assertThat(orders, hasItems(expectedOrders3));
		
		asOfDate = Context.getDateTimeFormat().parse("26/09/2008 09:25:10");
		orders = orderService.getActiveOrders(patient, null, null, asOfDate);
		assertEquals(4, orders.size());
		Order[] expectedOrders4 = { orderService.getOrder(222), orderService.getOrder(3), orderService.getOrder(444),
		        orderService.getOrder(5) };
		assertThat(orders, hasItems(expectedOrders4));
		
		asOfDate = Context.getDateFormat().parse("04/12/2008");
		orders = orderService.getActiveOrders(patient, null, null, asOfDate);
		assertEquals(5, orders.size());
		Order[] expectedOrders5 = { orderService.getOrder(222), orderService.getOrder(3), orderService.getOrder(444),
		        orderService.getOrder(5), orderService.getOrder(7) };
		assertThat(orders, hasItems(expectedOrders5));
		
		asOfDate = Context.getDateFormat().parse("06/12/2008");
		orders = orderService.getActiveOrders(patient, null, null, asOfDate);
		assertEquals(5, orders.size());
		assertThat(orders, hasItems(expectedOrders5));
	}
	
	/**
	 * @verifies return all orders if no orderType is specified
	 * @see OrderService#getActiveOrders(org.openmrs.Patient, org.openmrs.OrderType,
	 *      org.openmrs.CareSetting, java.util.Date)
	 */
	@Test
	public void getActiveOrders_shouldReturnAllOrdersIfNoOrderTypeIsSpecified() throws Exception {
		Patient patient = Context.getPatientService().getPatient(2);
		List<Order> orders = orderService.getActiveOrders(patient, null, null, null);
		assertEquals(5, orders.size());
		Order[] expectedOrders = { orderService.getOrder(222), orderService.getOrder(3), orderService.getOrder(444),
		        orderService.getOrder(5), orderService.getOrder(7) };
		assertThat(orders, hasItems(expectedOrders));
	}
	
	/**
	 * @verifies include orders for sub types if order type is specified
	 * @see OrderService#getActiveOrders(org.openmrs.Patient, org.openmrs.OrderType,
	 *      org.openmrs.CareSetting, java.util.Date)
	 */
	@Test
	public void getActiveOrders_shouldIncludeOrdersForSubTypesIfOrderTypeIsSpecified() throws Exception {
		executeDataSet("org/openmrs/api/include/OrderServiceTest-otherOrders.xml");
		Patient patient = Context.getPatientService().getPatient(2);
		OrderType testOrderType = orderService.getOrderType(2);
		List<Order> orders = orderService.getActiveOrders(patient, testOrderType, null, null);
		assertEquals(5, orders.size());
		Order[] expectedOrder1 = { orderService.getOrder(7), orderService.getOrder(101), orderService.getOrder(102),
		        orderService.getOrder(103), orderService.getOrder(104) };
		assertThat(orders, hasItems(expectedOrder1));
		
		OrderType labTestOrderType = orderService.getOrderType(7);
		orders = orderService.getActiveOrders(patient, labTestOrderType, null, null);
		assertEquals(3, orders.size());
		Order[] expectedOrder2 = { orderService.getOrder(101), orderService.getOrder(103), orderService.getOrder(104) };
		assertThat(orders, hasItems(expectedOrder2));
	}
	
	/**
	 * @verifies populate correct attributes on the discontinue and discontinued orders
	 * @see OrderService#discontinueOrder(org.openmrs.Order, String, java.util.Date,
	 *      org.openmrs.Provider, org.openmrs.Encounter)
	 */
	@Test
	public void discontinueOrder_shouldPopulateCorrectAttributesOnTheDiscontinueAndDiscontinuedOrders() throws Exception {
		Order order = orderService.getOrderByOrderNumber("111");
		Encounter encounter = encounterService.getEncounter(3);
		Provider orderer = providerService.getProvider(1);
		assertTrue(OrderUtilTest.isActiveOrder(order, null));
		Date discontinueDate = new Date();
		String discontinueReasonNonCoded = "Test if I can discontinue this";
		
		Order discontinueOrder = orderService.discontinueOrder(order, discontinueReasonNonCoded, discontinueDate, orderer,
		    encounter);
		
		Assert.assertEquals(order.getDateStopped(), discontinueDate);
		Assert.assertNotNull(discontinueOrder);
		Assert.assertNotNull(discontinueOrder.getId());
		Assert.assertEquals(discontinueOrder.getDateActivated(), discontinueOrder.getAutoExpireDate());
		Assert.assertEquals(discontinueOrder.getAction(), Action.DISCONTINUE);
		Assert.assertEquals(discontinueOrder.getOrderReasonNonCoded(), discontinueReasonNonCoded);
		Assert.assertEquals(discontinueOrder.getPreviousOrder(), order);
	}
	
	/**
	 * @verifies set correct attributes on the discontinue and discontinued orders
	 * @see OrderService#discontinueOrder(org.openmrs.Order, org.openmrs.Concept, java.util.Date,
	 *      org.openmrs.Provider, org.openmrs.Encounter)
	 */
	@Test
	public void discontinueOrder_shouldSetCorrectAttributesOnTheDiscontinueAndDiscontinuedOrders() throws Exception {
		executeDataSet("org/openmrs/api/include/OrderServiceTest-discontinueReason.xml");
		
		Order order = orderService.getOrderByOrderNumber("111");
		Encounter encounter = encounterService.getEncounter(3);
		Provider orderer = providerService.getProvider(1);
		Date discontinueDate = new Date();
		Concept concept = Context.getConceptService().getConcept(1);
		
		Order discontinueOrder = orderService.discontinueOrder(order, concept, discontinueDate, orderer, encounter);
		
		Assert.assertEquals(order.getDateStopped(), discontinueDate);
		Assert.assertNotNull(discontinueOrder);
		Assert.assertNotNull(discontinueOrder.getId());
		Assert.assertEquals(discontinueOrder.getDateActivated(), discontinueOrder.getAutoExpireDate());
		Assert.assertEquals(discontinueOrder.getAction(), Action.DISCONTINUE);
		Assert.assertEquals(discontinueOrder.getOrderReason(), concept);
		Assert.assertEquals(discontinueOrder.getPreviousOrder(), order);
	}
	
	/**
	 * @see {@link OrderService#discontinueOrder(org.openmrs.Order, String, java.util.Date, org.openmrs.Provider, org.openmrs.Encounter)}
	 */
	@Test
	@Verifies(value = "should fail for a discontinuation order", method = "discontinueOrder(Order, String, Date, Provider, Encounter)")
	public void discontinueOrder_shouldFailForADiscontinuationOrder() throws Exception {
		executeDataSet("org/openmrs/api/include/OrderServiceTest-discontinuedOrder.xml");
		Order discontinuationOrder = orderService.getOrder(26);
		assertEquals(Action.DISCONTINUE, discontinuationOrder.getAction());
		Encounter encounter = encounterService.getEncounter(3);
		expectedException.expect(APIException.class);
		expectedException.expectMessage("Order.action.cannot.discontinued");
		orderService.discontinueOrder(discontinuationOrder, "Test if I can discontinue this", null, null, encounter);
	}
	
	/**
	 * @see {@link OrderService#discontinueOrder(org.openmrs.Order, org.openmrs.Concept, java.util.Date, org.openmrs.Provider, org.openmrs.Encounter)}
	 */
	@Test
	@Verifies(value = "should not pass for a discontinuation order", method = "discontinueOrder(Order, Concept, Date, Provider, Encounter)")
	public void discontinueOrder_shouldNotPassForADiscontinuationOrder() throws Exception {
		executeDataSet("org/openmrs/api/include/OrderServiceTest-discontinuedOrder.xml");
		executeDataSet("org/openmrs/api/include/OrderServiceTest-discontinueReason.xml");
		Order discontinuationOrder = orderService.getOrder(26);
		assertEquals(Action.DISCONTINUE, discontinuationOrder.getAction());
		Encounter encounter = encounterService.getEncounter(3);
		expectedException.expect(APIException.class);
		expectedException.expectMessage("Order.action.cannot.discontinued");
		orderService.discontinueOrder(discontinuationOrder, (Concept) null, null, null, encounter);
	}
	
	/**
	 * @verifies fail for a discontinued order
	 * @see OrderService#discontinueOrder(org.openmrs.Order, String, java.util.Date,
	 *      org.openmrs.Provider, org.openmrs.Encounter)
	 */
	@Test
	public void discontinueOrder_shouldFailForADiscontinuedOrder() throws Exception {
		Order discontinuationOrder = orderService.getOrder(2);
		assertFalse(discontinuationOrder.isActive());
		assertNotNull(discontinuationOrder.getDateStopped());
		Encounter encounter = encounterService.getEncounter(3);
		expectedException.expect(APIException.class);
		expectedException.expectMessage("Order.stopped.cannot.discontinued");
		orderService.discontinueOrder(discontinuationOrder, "some reason", null, null, encounter);
	}
	
	/**
	 * @verifies not pass for a discontinued order
	 * @see OrderService#discontinueOrder(org.openmrs.Order, org.openmrs.Concept, java.util.Date,
	 *      org.openmrs.Provider, org.openmrs.Encounter)
	 */
	@Test
	public void discontinueOrder_shouldNotPassForADiscontinuedOrder() throws Exception {
		Order discontinuationOrder = orderService.getOrder(2);
		assertFalse(discontinuationOrder.isActive());
		assertNotNull(discontinuationOrder.getDateStopped());
		Encounter encounter = encounterService.getEncounter(3);
		expectedException.expect(APIException.class);
		expectedException.expectMessage("Order.stopped.cannot.discontinued");
		orderService.discontinueOrder(discontinuationOrder, (Concept) null, null, null, encounter);
	}
	
	/**
	 * @see {@link OrderService#saveOrder(org.openmrs.Order, OrderContext)}
	 */
	@Test
	@Verifies(value = "should discontinue existing active order if new order being saved with action to discontinue", method = "saveOrder(Order)")
	public void saveOrder_shouldDiscontinueExistingActiveOrderIfNewOrderBeingSavedWithActionToDiscontinue() throws Exception {
		DrugOrder order = new DrugOrder();
		order.setAction(Order.Action.DISCONTINUE);
		order.setOrderReasonNonCoded("Discontinue this");
		order.setDrug(conceptService.getDrug(3));
		order.setEncounter(encounterService.getEncounter(5));
		order.setPatient(patientService.getPatient(7));
		order.setOrderer(providerService.getProvider(1));
		order.setCareSetting(orderService.getCareSetting(1));
		order.setEncounter(encounterService.getEncounter(3));
		order.setOrderType(orderService.getOrderType(1));
		order.setDateActivated(new Date());
		order.setDosingType(SimpleDosingInstructions.class);
		order.setDose(500.0);
		order.setDoseUnits(conceptService.getConcept(50));
		order.setFrequency(orderService.getOrderFrequency(1));
		order.setRoute(conceptService.getConcept(22));
		order.setNumRefills(10);
		order.setQuantity(20.0);
		order.setQuantityUnits(conceptService.getConcept(51));
		
		//We are trying to discontinue order id 111 in standardTestDataset.xml
		Order expectedPreviousOrder = orderService.getOrder(111);
		Assert.assertNull(expectedPreviousOrder.getDateStopped());
		
		order = (DrugOrder) orderService.saveOrder(order, null);
		
		Assert.assertNotNull("should populate dateStopped in previous order", expectedPreviousOrder.getDateStopped());
		Assert.assertNotNull("should save discontinue order", order.getId());
		Assert.assertEquals(expectedPreviousOrder, order.getPreviousOrder());
		Assert.assertNotNull(expectedPreviousOrder.getDateStopped());
		Assert.assertEquals(order.getDateActivated(), order.getAutoExpireDate());
	}
	
	/**
	 * @see {@link OrderService#saveOrder(org.openmrs.Order, OrderContext)}
	 */
	@Test
	@Verifies(value = "should discontinue previousOrder if it is not already discontinued", method = "saveOrder(Order)")
	public void saveOrder_shouldDiscontinuePreviousOrderIfItIsNotAlreadyDiscontinued() throws Exception {
		//We are trying to discontinue order id 111 in standardTestDataset.xml
		DrugOrder order = new DrugOrder();
		order.setAction(Order.Action.DISCONTINUE);
		order.setOrderReasonNonCoded("Discontinue this");
		order.setDrug(conceptService.getDrug(3));
		order.setEncounter(encounterService.getEncounter(5));
		order.setPatient(Context.getPatientService().getPatient(7));
		order.setOrderer(Context.getProviderService().getProvider(1));
		order.setCareSetting(orderService.getCareSetting(1));
		order.setEncounter(encounterService.getEncounter(3));
		order.setOrderType(orderService.getOrderType(1));
		order.setDateActivated(new Date());
		order.setDosingType(SimpleDosingInstructions.class);
		order.setDose(500.0);
		order.setDoseUnits(conceptService.getConcept(50));
		order.setFrequency(orderService.getOrderFrequency(1));
		order.setRoute(conceptService.getConcept(22));
		order.setNumRefills(10);
		order.setQuantity(20.0);
		order.setQuantityUnits(conceptService.getConcept(51));
		Order previousOrder = orderService.getOrder(111);
		assertTrue(OrderUtilTest.isActiveOrder(previousOrder, null));
		order.setPreviousOrder(previousOrder);
		
		orderService.saveOrder(order, null);
		Assert.assertEquals(order.getDateActivated(), order.getAutoExpireDate());
		Assert.assertNotNull("previous order should be discontinued", previousOrder.getDateStopped());
	}
	
	/**
	 * @see {@link OrderService#saveOrder(org.openmrs.Order, OrderContext)}
	 */
	@Test
	@Verifies(value = "should fail if concept in previous order does not match this concept", method = "saveOrder(Order)")
	public void saveOrder_shouldFailIfConceptInPreviousOrderDoesNotMatchThisConcept() throws Exception {
		Order previousOrder = orderService.getOrder(7);
		assertTrue(OrderUtilTest.isActiveOrder(previousOrder, null));
		Order order = previousOrder.cloneForDiscontinuing();
		order.setDateActivated(new Date());
		order.setOrderReasonNonCoded("Discontinue this");
		order.setEncounter(encounterService.getEncounter(6));
		order.setOrderer(providerService.getProvider(1));
		Concept newConcept = conceptService.getConcept(5089);
		assertFalse(previousOrder.getConcept().equals(newConcept));
		order.setConcept(newConcept);
		
		expectedException.expect(APIException.class);
		expectedException.expectMessage("Order.previous.concept");
		orderService.saveOrder(order, null);
	}
	
	/**
	 * @verifies reject a future discontinueDate
	 * @see OrderService#discontinueOrder(org.openmrs.Order, org.openmrs.Concept, java.util.Date,
	 *      org.openmrs.Provider, org.openmrs.Encounter)
	 */
	@Test
	public void discontinueOrder_shouldRejectAFutureDiscontinueDate() throws Exception {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.HOUR_OF_DAY, 1);
		Patient patient = Context.getPatientService().getPatient(2);
		CareSetting careSetting = orderService.getCareSetting(1);
		Order orderToDiscontinue = orderService.getActiveOrders(patient, null, careSetting, null).get(0);
		Encounter encounter = encounterService.getEncounter(3);
		expectedException.expect(IllegalArgumentException.class);
		expectedException.expectMessage("Discontinue date cannot be in the future");
		orderService.discontinueOrder(orderToDiscontinue, new Concept(), cal.getTime(), null, encounter);
	}
	
	/**
	 * @verifies fail if discontinueDate is in the future
	 * @see OrderService#discontinueOrder(org.openmrs.Order, String, java.util.Date,
	 *      org.openmrs.Provider, org.openmrs.Encounter)
	 */
	@Test
	public void discontinueOrder_shouldFailIfDiscontinueDateIsInTheFuture() throws Exception {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.HOUR_OF_DAY, 1);
		Order orderToDiscontinue = orderService.getActiveOrders(Context.getPatientService().getPatient(2), null,
		    orderService.getCareSetting(1), null).get(0);
		Encounter encounter = encounterService.getEncounter(3);
		expectedException.expect(IllegalArgumentException.class);
		expectedException.expectMessage("Discontinue date cannot be in the future");
		orderService.discontinueOrder(orderToDiscontinue, "Testing", cal.getTime(), null, encounter);
	}
	
	/**
	 * @verifies pass if the existing drug order matches the concept and drug of the DC order
	 * @see OrderService#saveOrder(org.openmrs.Order, OrderContext)
	 */
	@Test
	public void saveOrder_shouldPassIfTheExistingDrugOrderMatchesTheConceptAndDrugOfTheDCOrder() throws Exception {
		final DrugOrder orderToDiscontinue = (DrugOrder) orderService.getOrder(444);
		assertTrue(OrderUtilTest.isActiveOrder(orderToDiscontinue, null));
		
		DrugOrder order = new DrugOrder();
		order.setDrug(orderToDiscontinue.getDrug());
		order.setOrderType(orderService.getOrderTypeByName("Drug order"));
		order.setAction(Order.Action.DISCONTINUE);
		order.setOrderReasonNonCoded("Discontinue this");
		order.setPatient(orderToDiscontinue.getPatient());
		order.setConcept(orderToDiscontinue.getConcept());
		order.setOrderer(orderToDiscontinue.getOrderer());
		order.setCareSetting(orderToDiscontinue.getCareSetting());
		order.setEncounter(encounterService.getEncounter(6));
		order.setDateActivated(new Date());
		order.setDosingType(SimpleDosingInstructions.class);
		order.setDose(orderToDiscontinue.getDose());
		order.setDoseUnits(orderToDiscontinue.getDoseUnits());
		order.setRoute(orderToDiscontinue.getRoute());
		order.setFrequency(orderToDiscontinue.getFrequency());
		order.setQuantity(orderToDiscontinue.getQuantity());
		order.setQuantityUnits(orderToDiscontinue.getQuantityUnits());
		order.setNumRefills(orderToDiscontinue.getNumRefills());
		
		orderService.saveOrder(order, null);
		
		Assert.assertNotNull("previous order should be discontinued", orderToDiscontinue.getDateStopped());
	}
	
	/**
	 * @verifies fail if the existing drug order matches the concept and not drug of the DC order
	 * @see OrderService#saveOrder(org.openmrs.Order, OrderContext)
	 */
	@Test
	public void saveOrder_shouldFailIfTheExistingDrugOrderMatchesTheConceptAndNotDrugOfTheDCOrder() throws Exception {
		final DrugOrder orderToDiscontinue = (DrugOrder) orderService.getOrder(5);
		assertTrue(OrderUtilTest.isActiveOrder(orderToDiscontinue, null));
		
		//create a different test drug
		Drug discontinuationOrderDrug = new Drug();
		discontinuationOrderDrug.setConcept(orderToDiscontinue.getConcept());
		discontinuationOrderDrug = conceptService.saveDrug(discontinuationOrderDrug);
		assertNotEquals(discontinuationOrderDrug, orderToDiscontinue.getDrug());
		assertNotNull(orderToDiscontinue.getDrug());
		
		DrugOrder order = orderToDiscontinue.cloneForRevision();
		order.setDateActivated(new Date());
		order.setOrderer(providerService.getProvider(1));
		order.setEncounter(encounterService.getEncounter(6));
		order.setDrug(discontinuationOrderDrug);
		order.setOrderReasonNonCoded("Discontinue this");
		
		expectedException.expect(APIException.class);
		expectedException.expectMessage("Order.previous.drug");
		orderService.saveOrder(order, null);
	}
	
	/**
	 * @verifies pass if the existing drug order matches the concept and there is no drug on the previous order
	 * @see OrderService#saveOrder(org.openmrs.Order, OrderContext)
	 */
	@Test
	public void saveOrder_shouldPassIfTheExistingDrugOrderMatchesTheConceptAndThereIsNoDrugOnThePreviousOrder()
	        throws Exception {
		DrugOrder orderToDiscontinue = new DrugOrder();
		orderToDiscontinue.setAction(Action.NEW);
		orderToDiscontinue.setPatient(Context.getPatientService().getPatient(7));
		orderToDiscontinue.setConcept(Context.getConceptService().getConcept(5497));
		orderToDiscontinue.setCareSetting(orderService.getCareSetting(1));
		orderToDiscontinue.setOrderer(orderService.getOrder(1).getOrderer());
		orderToDiscontinue.setEncounter(encounterService.getEncounter(3));
		orderToDiscontinue.setDateActivated(new Date());
		orderToDiscontinue.setScheduledDate(new Date());
		orderToDiscontinue.setUrgency(Order.Urgency.ON_SCHEDULED_DATE);
		orderToDiscontinue.setEncounter(encounterService.getEncounter(3));
		orderToDiscontinue.setOrderType(orderService.getOrderType(17));
		
		orderToDiscontinue.setDrug(null);
		orderToDiscontinue.setDosingType(FreeTextDosingInstructions.class);
		orderToDiscontinue.setDosingInstructions("instructions");
		orderToDiscontinue.setOrderer(providerService.getProvider(1));
		orderToDiscontinue.setDosingInstructions("2 for 5 days");
		orderToDiscontinue.setQuantity(10.0);
		orderToDiscontinue.setQuantityUnits(conceptService.getConcept(51));
		orderToDiscontinue.setNumRefills(2);
		
		orderService.saveOrder(orderToDiscontinue, null);
		assertTrue(OrderUtilTest.isActiveOrder(orderToDiscontinue, null));
		
		DrugOrder order = orderToDiscontinue.cloneForDiscontinuing();
		order.setDateActivated(new Date());
		order.setOrderer(providerService.getProvider(1));
		order.setEncounter(encounterService.getEncounter(3));
		order.setOrderReasonNonCoded("Discontinue this");
		
		orderService.saveOrder(order, null);
		
		Assert.assertNotNull("previous order should be discontinued", orderToDiscontinue.getDateStopped());
	}
	
	/**
	 * @verifies fail for a stopped order
	 * @see OrderService#discontinueOrder(org.openmrs.Order, org.openmrs.Concept, java.util.Date,
	 *      org.openmrs.Provider, org.openmrs.Encounter)
	 */
	@Test
	public void discontinueOrder_shouldFailForAStoppedOrder() throws Exception {
		Order orderToDiscontinue = orderService.getOrder(1);
		Encounter encounter = encounterService.getEncounter(3);
		assertNotNull(orderToDiscontinue.getDateStopped());
		expectedException.expect(APIException.class);
		expectedException.expectMessage("Order.stopped.cannot.discontinued");
		orderService.discontinueOrder(orderToDiscontinue, Context.getConceptService().getConcept(1), null, null, encounter);
	}
	
	/**
	 * @verifies fail for a voided order
	 * @see OrderService#discontinueOrder(org.openmrs.Order, String, java.util.Date,
	 *      org.openmrs.Provider, org.openmrs.Encounter)
	 */
	@Test
	public void discontinueOrder_shouldFailForAVoidedOrder() throws Exception {
		Order orderToDiscontinue = orderService.getOrder(8);
		Encounter encounter = encounterService.getEncounter(3);
		assertTrue(orderToDiscontinue.isVoided());
		expectedException.expect(APIException.class);
		expectedException.expectMessage("Order.stopped.cannot.discontinued");
		orderService.discontinueOrder(orderToDiscontinue, "testing", null, null, encounter);
	}
	
	/**
	 * @verifies fail for an expired order
	 * @see OrderService#discontinueOrder(org.openmrs.Order, org.openmrs.Concept, java.util.Date,
	 *      org.openmrs.Provider, org.openmrs.Encounter)
	 */
	@Test
	public void discontinueOrder_shouldFailForAnExpiredOrder() throws Exception {
		Order orderToDiscontinue = orderService.getOrder(6);
		Encounter encounter = encounterService.getEncounter(3);
		assertNotNull(orderToDiscontinue.getAutoExpireDate());
		assertTrue(orderToDiscontinue.getAutoExpireDate().before(new Date()));
		expectedException.expect(APIException.class);
		expectedException.expectMessage("Order.stopped.cannot.discontinued");
		orderService.discontinueOrder(orderToDiscontinue, Context.getConceptService().getConcept(1), null, null, encounter);
	}
	
	/**
	 * @verifies not allow editing an existing order
	 * @see OrderService#saveOrder(org.openmrs.Order, OrderContext)
	 */
	@Test
	public void saveOrder_shouldNotAllowEditingAnExistingOrder() throws Exception {
		final DrugOrder order = (DrugOrder) orderService.getOrder(5);
		expectedException.expect(APIException.class);
		expectedException.expectMessage("Order.cannot.edit.existing");
		orderService.saveOrder(order, null);
	}
	
	/**
	 * @verifies return the care setting with the specified uuid
	 * @see OrderService#getCareSettingByUuid(String)
	 */
	@Test
	public void getCareSettingByUuid_shouldReturnTheCareSettingWithTheSpecifiedUuid() throws Exception {
		CareSetting cs = orderService.getCareSettingByUuid("6f0c9a92-6f24-11e3-af88-005056821db0");
		assertEquals(1, cs.getId().intValue());
	}
	
	/**
	 * @verifies return the care setting with the specified name
	 * @see OrderService#getCareSettingByName(String)
	 */
	@Test
	public void getCareSettingByName_shouldReturnTheCareSettingWithTheSpecifiedName() throws Exception {
		CareSetting cs = orderService.getCareSettingByName("INPATIENT");
		assertEquals(2, cs.getId().intValue());
		
		//should also be case insensitive
		cs = orderService.getCareSettingByName("inpatient");
		assertEquals(2, cs.getId().intValue());
	}
	
	/**
	 * @verifies return only un retired care settings if includeRetired is set to false
	 * @see OrderService#getCareSettings(boolean)
	 */
	@Test
	public void getCareSettings_shouldReturnOnlyUnRetiredCareSettingsIfIncludeRetiredIsSetToFalse() throws Exception {
		List<CareSetting> careSettings = orderService.getCareSettings(false);
		assertEquals(2, careSettings.size());
		assertTrue(containsId(careSettings, 1));
		assertTrue(containsId(careSettings, 2));
	}
	
	/**
	 * @verifies return retired care settings if includeRetired is set to true
	 * @see OrderService#getCareSettings(boolean)
	 */
	@Test
	public void getCareSettings_shouldReturnRetiredCareSettingsIfIncludeRetiredIsSetToTrue() throws Exception {
		CareSetting retiredCareSetting = orderService.getCareSetting(3);
		assertTrue(retiredCareSetting.isRetired());
		List<CareSetting> careSettings = orderService.getCareSettings(true);
		assertEquals(3, careSettings.size());
		assertTrue(containsId(careSettings, retiredCareSetting.getCareSettingId()));
	}
	
	/**
	 * @verifies not allow revising a stopped order
	 * @see OrderService#saveOrder(org.openmrs.Order, OrderContext)
	 */
	@Test
	public void saveOrder_shouldNotAllowRevisingAStoppedOrder() throws Exception {
		Order originalOrder = orderService.getOrder(1);
		assertNotNull(originalOrder.getDateStopped());
		Order revisedOrder = originalOrder.cloneForRevision();
		revisedOrder.setEncounter(encounterService.getEncounter(4));
		revisedOrder.setInstructions("Take after a meal");
		revisedOrder.setOrderer(providerService.getProvider(1));
		revisedOrder.setDateActivated(new Date());
		expectedException.expect(APIException.class);
		expectedException.expectMessage("Order.stopped.cannot.discontinued");
		orderService.saveOrder(revisedOrder, null);
	}
	
	/**
	 * @verifies not allow revising a voided order
	 * @see OrderService#saveOrder(org.openmrs.Order, OrderContext)
	 */
	@Test
	public void saveOrder_shouldNotAllowRevisingAVoidedOrder() throws Exception {
		Order originalOrder = orderService.getOrder(8);
		assertTrue(originalOrder.isVoided());
		Order revisedOrder = originalOrder.cloneForRevision();
		revisedOrder.setEncounter(encounterService.getEncounter(6));
		revisedOrder.setInstructions("Take after a meal");
		revisedOrder.setOrderer(providerService.getProvider(1));
		revisedOrder.setDateActivated(new Date());
		expectedException.expect(APIException.class);
		expectedException.expectMessage("Order.stopped.cannot.discontinued");
		orderService.saveOrder(revisedOrder, null);
	}
	
	/**
	 * @verifies not allow revising an expired order
	 * @see OrderService#saveOrder(org.openmrs.Order, OrderContext)
	 */
	@Test
	public void saveOrder_shouldNotAllowRevisingAnExpiredOrder() throws Exception {
		Order originalOrder = orderService.getOrder(6);
		assertNotNull(originalOrder.getAutoExpireDate());
		assertTrue(originalOrder.getAutoExpireDate().before(new Date()));
		Order revisedOrder = originalOrder.cloneForRevision();
		revisedOrder.setEncounter(encounterService.getEncounter(6));
		revisedOrder.setInstructions("Take after a meal");
		revisedOrder.setOrderer(providerService.getProvider(1));
		revisedOrder.setDateActivated(new Date());
		revisedOrder.setAutoExpireDate(new Date());
		expectedException.expect(APIException.class);
		expectedException.expectMessage("Order.stopped.cannot.discontinued");
		orderService.saveOrder(revisedOrder, null);
	}
	
	/**
	 * @verifies not allow revising an order with no previous order
	 * @see OrderService#saveOrder(org.openmrs.Order, OrderContext)
	 */
	@Test
	public void saveOrder_shouldNotAllowRevisingAnOrderWithNoPreviousOrder() throws Exception {
		Order originalOrder = orderService.getOrder(111);
		assertTrue(originalOrder.isActive());
		Order revisedOrder = originalOrder.cloneForRevision();
		revisedOrder.setEncounter(encounterService.getEncounter(5));
		revisedOrder.setInstructions("Take after a meal");
		revisedOrder.setPreviousOrder(null);
		revisedOrder.setOrderer(providerService.getProvider(1));
		revisedOrder.setDateActivated(new Date());
		
		expectedException.expect(APIException.class);
		expectedException.expectMessage("Order.previous.required");
		orderService.saveOrder(revisedOrder, null);
	}
	
	/**
	 * @verifies save a revised order
	 * @see OrderService#saveOrder(org.openmrs.Order, OrderContext)
	 */
	@Test
	public void saveOrder_shouldSaveARevisedOrder() throws Exception {
		Order originalOrder = orderService.getOrder(111);
		assertTrue(originalOrder.isActive());
		final Patient patient = originalOrder.getPatient();
		List<Order> originalActiveOrders = orderService.getActiveOrders(patient, null, null, null);
		final int originalOrderCount = originalActiveOrders.size();
		assertTrue(originalActiveOrders.contains(originalOrder));
		Order revisedOrder = originalOrder.cloneForRevision();
		revisedOrder.setEncounter(encounterService.getEncounter(5));
		revisedOrder.setInstructions("Take after a meal");
		revisedOrder.setDateActivated(new Date());
		revisedOrder.setOrderer(providerService.getProvider(1));
		revisedOrder.setEncounter(encounterService.getEncounter(3));
		orderService.saveOrder(revisedOrder, null);
		
		List<Order> activeOrders = orderService.getActiveOrders(patient, null, null, null);
		assertEquals(originalOrderCount, activeOrders.size());
		assertEquals(revisedOrder.getDateActivated(), DateUtils.addSeconds(originalOrder.getDateStopped(), 1));
		assertFalse(originalOrder.isActive());
	}
	
	/**
	 * @verifies get non retired frequencies with names matching the phrase if includeRetired is
	 *           false
	 * @see OrderService#getOrderFrequencies(String, java.util.Locale, boolean, boolean)
	 */
	@Test
	public void getOrderFrequencies_shouldGetNonRetiredFrequenciesWithNamesMatchingThePhraseIfIncludeRetiredIsFalse()
	        throws Exception {
		executeDataSet("org/openmrs/api/include/OrderServiceTest-otherOrderFrequencies.xml");
		List<OrderFrequency> orderFrequencies = orderService.getOrderFrequencies("once", Locale.US, false, false);
		assertEquals(2, orderFrequencies.size());
		assertTrue(containsId(orderFrequencies, 100));
		assertTrue(containsId(orderFrequencies, 102));
		
		//should match anywhere in the concept name
		orderFrequencies = orderService.getOrderFrequencies("nce", Locale.US, false, false);
		assertEquals(2, orderFrequencies.size());
		assertTrue(containsId(orderFrequencies, 100));
		assertTrue(containsId(orderFrequencies, 102));
	}
	
	/**
	 * @verifies include retired frequencies if includeRetired is set to true
	 * @see OrderService#getOrderFrequencies(String, java.util.Locale, boolean, boolean)
	 */
	@Test
	public void getOrderFrequencies_shouldIncludeRetiredFrequenciesIfIncludeRetiredIsSetToTrue() throws Exception {
		executeDataSet("org/openmrs/api/include/OrderServiceTest-otherOrderFrequencies.xml");
		List<OrderFrequency> orderFrequencies = orderService.getOrderFrequencies("ce", Locale.US, false, true);
		assertEquals(4, orderFrequencies.size());
		assertTrue(containsId(orderFrequencies, 100));
		assertTrue(containsId(orderFrequencies, 101));
		assertTrue(containsId(orderFrequencies, 102));
		assertTrue(containsId(orderFrequencies, 103));
	}
	
	/**
	 * @verifies get frequencies with names that match the phrase and locales if exact locale is
	 *           false
	 * @see OrderService#getOrderFrequencies(String, java.util.Locale, boolean, boolean)
	 */
	@Test
	public void getOrderFrequencies_shouldGetFrequenciesWithNamesThatMatchThePhraseAndLocalesIfExactLocaleIsFalse()
	        throws Exception {
		executeDataSet("org/openmrs/api/include/OrderServiceTest-otherOrderFrequencies.xml");
		List<OrderFrequency> orderFrequencies = orderService.getOrderFrequencies("ce", Locale.US, false, false);
		assertEquals(3, orderFrequencies.size());
		assertTrue(containsId(orderFrequencies, 100));
		assertTrue(containsId(orderFrequencies, 101));
		assertTrue(containsId(orderFrequencies, 102));
	}
	
	/**
	 * @verifies get frequencies with names that match the phrase and locale if exact locale is true
	 * @see OrderService#getOrderFrequencies(String, java.util.Locale, boolean, boolean)
	 */
	@Test
	public void getOrderFrequencies_shouldGetFrequenciesWithNamesThatMatchThePhraseAndLocaleIfExactLocaleIsTrue()
	        throws Exception {
		executeDataSet("org/openmrs/api/include/OrderServiceTest-otherOrderFrequencies.xml");
		List<OrderFrequency> orderFrequencies = orderService.getOrderFrequencies("ce", Locale.US, true, false);
		assertEquals(1, orderFrequencies.size());
		assertEquals(102, orderFrequencies.get(0).getOrderFrequencyId().intValue());
		
		orderFrequencies = orderService.getOrderFrequencies("ce", Locale.ENGLISH, true, false);
		assertEquals(2, orderFrequencies.size());
		assertTrue(containsId(orderFrequencies, 100));
		assertTrue(containsId(orderFrequencies, 101));
	}
	
	/**
	 * @verifies return unique frequencies
	 * @see OrderService#getOrderFrequencies(String, java.util.Locale, boolean, boolean)
	 */
	@Test
	public void getOrderFrequencies_shouldReturnUniqueFrequencies() throws Exception {
		executeDataSet("org/openmrs/api/include/OrderServiceTest-otherOrderFrequencies.xml");
		final String searchPhrase = "once";
		final Locale locale = Locale.ENGLISH;
		List<OrderFrequency> orderFrequencies = orderService.getOrderFrequencies(searchPhrase, locale, true, false);
		assertEquals(1, orderFrequencies.size());
		final OrderFrequency expectedOrderFrequency = orderService.getOrderFrequency(100);
		assertEquals(expectedOrderFrequency, orderFrequencies.get(0));
		
		//Add a new name to the frequency concept so that our search phrase matches on 2
		//concept names for the same frequency concept
		Concept frequencyConcept = expectedOrderFrequency.getConcept();
		final String newConceptName = searchPhrase + " A Day";
		frequencyConcept.addName(new ConceptName(newConceptName, locale));
		conceptService.saveConcept(frequencyConcept);
		
		orderFrequencies = orderService.getOrderFrequencies(searchPhrase, locale, true, false);
		assertEquals(1, orderFrequencies.size());
		assertEquals(expectedOrderFrequency, orderFrequencies.get(0));
	}
	
	/**
	 * @verifies reject a null search phrase
	 * @see OrderService#getOrderFrequencies(String, java.util.Locale, boolean, boolean)
	 */
	@Test
	public void getOrderFrequencies_shouldRejectANullSearchPhrase() throws Exception {
		expectedException.expect(IllegalArgumentException.class);
		expectedException.expectMessage("searchPhrase is required");
		orderService.getOrderFrequencies(null, Locale.ENGLISH, false, false);
	}
	
	@Test
	@Verifies(value = "should retire given order frequency", method = "retireOrderFrequency(orderFrequency, String)")
	public void retireOrderFrequency_shouldRetireGivenOrderFrequency() throws Exception {
		OrderFrequency orderFrequency = orderService.getOrderFrequency(1);
		assertNotNull(orderFrequency);
		Assert.assertFalse(orderFrequency.isRetired());
		Assert.assertNull(orderFrequency.getRetireReason());
		Assert.assertNull(orderFrequency.getDateRetired());
		
		orderService.retireOrderFrequency(orderFrequency, "retire reason");
		
		orderFrequency = orderService.getOrderFrequency(1);
		assertNotNull(orderFrequency);
		assertTrue(orderFrequency.isRetired());
		assertEquals("retire reason", orderFrequency.getRetireReason());
		assertNotNull(orderFrequency.getDateRetired());
		
		//Should not change the number of order frequencies.
		assertEquals(3, orderService.getOrderFrequencies(true).size());
	}
	
	@Test
	@Verifies(value = "should unretire given order frequency", method = "unretireOrderFrequency(OrderFrequency)")
	public void unretireOrderFrequency_shouldUnretireGivenOrderFrequency() throws Exception {
		OrderFrequency orderFrequency = orderService.getOrderFrequency(3);
		assertNotNull(orderFrequency);
		assertTrue(orderFrequency.isRetired());
		assertEquals("Some Retire Reason", orderFrequency.getRetireReason());
		assertNotNull(orderFrequency.getDateRetired());
		
		orderService.unretireOrderFrequency(orderFrequency);
		
		orderFrequency = orderService.getOrderFrequency(3);
		assertNotNull(orderFrequency);
		assertFalse(orderFrequency.isRetired());
		assertNull(orderFrequency.getRetireReason());
		assertNull(orderFrequency.getDateRetired());
		
		//Should not change the number of order frequencies.
		assertEquals(3, orderService.getOrderFrequencies(true).size());
	}
	
	@Test
	@Verifies(value = "should delete given order frequency", method = "purgeOrderFrequency(OrderFrequency)")
	public void purgeOrderFrequency_shouldDeleteGivenOrderFrequency() throws Exception {
		OrderFrequency orderFrequency = orderService.getOrderFrequency(3);
		assertNotNull(orderFrequency);
		
		orderService.purgeOrderFrequency(orderFrequency);
		
		orderFrequency = orderService.getOrderFrequency(3);
		Assert.assertNull(orderFrequency);
		
		//Should reduce the existing number of order frequencies.
		assertEquals(2, orderService.getOrderFrequencies(true).size());
	}
	
	/**
	 * @see {@link OrderService#saveOrderFrequency(OrderFrequency)}
	 */
	@Test
	@Verifies(value = "should add a new order frequency to the database", method = "saveOrderFrequency(OrderFrequency)")
	public void saveOrderFrequency_shouldAddANewOrderFrequencyToTheDatabase() throws Exception {
		Concept concept = new Concept();
		concept.addName(new ConceptName("new name", Context.getLocale()));
		concept.setConceptClass(conceptService.getConceptClassByName("Frequency"));
		concept = conceptService.saveConcept(concept);
		Integer originalSize = orderService.getOrderFrequencies(true).size();
		OrderFrequency orderFrequency = new OrderFrequency();
		orderFrequency.setConcept(concept);
		orderFrequency.setFrequencyPerDay(2d);
		
		orderFrequency = orderService.saveOrderFrequency(orderFrequency);
		
		assertNotNull(orderFrequency.getId());
		assertNotNull(orderFrequency.getUuid());
		assertNotNull(orderFrequency.getCreator());
		assertNotNull(orderFrequency.getDateCreated());
		assertEquals(originalSize + 1, orderService.getOrderFrequencies(true).size());
	}
	
	/**
	 * @see {@link OrderService#saveOrderFrequency(OrderFrequency)}
	 */
	@Test
	@Verifies(value = "should edit an existing order frequency that is not in use", method = "saveOrderFrequency(OrderFrequency)")
	public void saveOrderFrequency_shouldEditAnExistingOrderFrequencyThatIsNotInUse() throws Exception {
		executeDataSet(OTHER_ORDER_FREQUENCIES_XML);
		OrderFrequency orderFrequency = orderService.getOrderFrequency(100);
		assertNotNull(orderFrequency);
		
		orderFrequency.setFrequencyPerDay(4d);
		orderService.saveOrderFrequency(orderFrequency);
	}
	
	/**
	 * @see {@link OrderService#saveOrderFrequency(OrderFrequency)}
	 */
	@Test
	@Verifies(value = "should not allow editing an existing order frequency that is in use", method = "saveOrderFrequency(OrderFrequency)")
	public void saveOrderFrequency_shouldNotAllowEditingAnExistingOrderFrequencyThatIsInUse() throws Exception {
		OrderFrequency orderFrequency = orderService.getOrderFrequency(1);
		assertNotNull(orderFrequency);
		
		orderFrequency.setFrequencyPerDay(4d);
		expectedException.expect(APIException.class);
		expectedException.expectMessage("Order.frequency.cannot.edit");
		orderService.saveOrderFrequency(orderFrequency);
	}
	
	/**
	 * @see {@link OrderService#purgeOrderFrequency(OrderFrequency)}
	 */
	@Test
	@Verifies(value = "should not allow deleting an order frequency that is in use", method = "purgeOrderFrequency(OrderFrequency)")
	public void purgeOrderFrequency_shouldNotAllowDeletingAnOrderFrequencyThatIsInUse() throws Exception {
		OrderFrequency orderFrequency = orderService.getOrderFrequency(1);
		assertNotNull(orderFrequency);
		
		expectedException.expect(APIException.class);
		expectedException.expectMessage("Order.frequency.cannot.delete");
		orderService.purgeOrderFrequency(orderFrequency);
	}
	
	@Test
	public void saveOrderWithScheduledDate_shouldAddANewOrderWithScheduledDateToTheDatabase() {
		Date scheduledDate = new Date();
		Order order = new Order();
		order.setAction(Action.NEW);
		order.setPatient(Context.getPatientService().getPatient(7));
		order.setConcept(Context.getConceptService().getConcept(5497));
		order.setCareSetting(orderService.getCareSetting(1));
		order.setOrderer(orderService.getOrder(1).getOrderer());
		order.setEncounter(encounterService.getEncounter(3));
		order.setDateActivated(new Date());
		order.setScheduledDate(scheduledDate);
		order.setUrgency(Order.Urgency.ON_SCHEDULED_DATE);
		order.setEncounter(encounterService.getEncounter(3));
		order.setOrderType(orderService.getOrderType(17));
		order = orderService.saveOrder(order, null);
		Order newOrder = orderService.getOrder(order.getOrderId());
		assertNotNull(order);
		assertEquals(scheduledDate, order.getScheduledDate());
		assertNotNull(newOrder);
		assertEquals(scheduledDate, newOrder.getScheduledDate());
		
	}
	
	/**
	 * @verifies set order number specified in the context if specified
	 * @see OrderService#saveOrder(org.openmrs.Order, OrderContext)
	 */
	@Test
	public void saveOrder_shouldSetOrderNumberSpecifiedInTheContextIfSpecified() throws Exception {
		GlobalProperty gp = new GlobalProperty(OpenmrsConstants.GP_ORDER_NUMBER_GENERATOR_BEAN_ID,
		        "orderEntry.OrderNumberGenerator");
		Context.getAdministrationService().saveGlobalProperty(gp);
		Order order = new TestOrder();
		order.setEncounter(encounterService.getEncounter(6));
		order.setPatient(patientService.getPatient(7));
		order.setConcept(conceptService.getConcept(5497));
		order.setOrderer(providerService.getProvider(1));
		order.setCareSetting(orderService.getCareSetting(1));
		order.setOrderType(orderService.getOrderType(2));
		order.setEncounter(encounterService.getEncounter(3));
		order.setDateActivated(new Date());
		OrderContext orderCtxt = new OrderContext();
		final String expectedOrderNumber = "Testing";
		orderCtxt.setAttribute(TimestampOrderNumberGenerator.NEXT_ORDER_NUMBER, expectedOrderNumber);
		order = orderService.saveOrder(order, orderCtxt);
		assertEquals(expectedOrderNumber, order.getOrderNumber());
	}
	
	/**
	 * @verifies set the order number returned by the configured generator
	 * @see OrderService#saveOrder(org.openmrs.Order, OrderContext)
	 */
	@Test
	public void saveOrder_shouldSetTheOrderNumberReturnedByTheConfiguredGenerator() throws Exception {
		GlobalProperty gp = new GlobalProperty(OpenmrsConstants.GP_ORDER_NUMBER_GENERATOR_BEAN_ID,
		        "orderEntry.OrderNumberGenerator");
		Context.getAdministrationService().saveGlobalProperty(gp);
		Order order = new TestOrder();
		order.setPatient(patientService.getPatient(7));
		order.setConcept(conceptService.getConcept(5497));
		order.setOrderer(providerService.getProvider(1));
		order.setCareSetting(orderService.getCareSetting(1));
		order.setOrderType(orderService.getOrderType(2));
		order.setEncounter(encounterService.getEncounter(3));
		order.setDateActivated(new Date());
		order = orderService.saveOrder(order, null);
		assertTrue(order.getOrderNumber().startsWith(TimestampOrderNumberGenerator.ORDER_NUMBER_PREFIX));
	}

	/**
	 * @verifies fail for revision order if an active drug order for the same concept and care settings exists
	 * @see OrderService#saveOrder(org.openmrs.Order, OrderContext)
	 */
	@Test
	public void saveOrder_shouldFailForRevisionOrderIfAnActiveDrugOrderForTheSameConceptAndCareSettingsExists() throws Exception {
		final Patient patient = patientService.getPatient(2);
		final Concept aspirin = conceptService.getConcept(88);
		DrugOrder firstOrder = new DrugOrder();
		firstOrder.setPatient(patient);
		firstOrder.setConcept(aspirin);
		firstOrder.setEncounter(encounterService.getEncounter(6));
		firstOrder.setOrderer(providerService.getProvider(1));
		firstOrder.setCareSetting(orderService.getCareSetting(2));
		firstOrder.setDrug(conceptService.getDrug(3));
		firstOrder.setDateActivated(new Date());
		firstOrder.setAutoExpireDate(DateUtils.addDays(new Date(), 10));
		firstOrder.setDosingType(FreeTextDosingInstructions.class);
		firstOrder.setDosingInstructions("2 for 5 days");
		firstOrder.setQuantity(10.0);
		firstOrder.setQuantityUnits(conceptService.getConcept(51));
		firstOrder.setNumRefills(0);
		orderService.saveOrder(firstOrder, null);

		//New order in future for same concept and care setting
		DrugOrder secondOrder = new DrugOrder();
		secondOrder.setPatient(firstOrder.getPatient());
		secondOrder.setConcept(firstOrder.getConcept());
		secondOrder.setEncounter(encounterService.getEncounter(6));
		secondOrder.setOrderer(providerService.getProvider(1));
		secondOrder.setCareSetting(firstOrder.getCareSetting());
		secondOrder.setDrug(conceptService.getDrug(3));
		secondOrder.setDateActivated(new Date());
		secondOrder.setScheduledDate(DateUtils.addDays(firstOrder.getEffectiveStopDate(), 1));
		secondOrder.setUrgency(Order.Urgency.ON_SCHEDULED_DATE);
		secondOrder.setDosingType(FreeTextDosingInstructions.class);
		secondOrder.setDosingInstructions("2 for 5 days");
		secondOrder.setQuantity(10.0);
		secondOrder.setQuantityUnits(conceptService.getConcept(51));
		secondOrder.setNumRefills(0);
		orderService.saveOrder(secondOrder, null);

		//Revise second order to have scheduled date overlapping with active order
		DrugOrder revision = secondOrder.cloneForRevision();
		revision.setScheduledDate(DateUtils.addDays(firstOrder.getEffectiveStartDate(), 2));
		revision.setEncounter(encounterService.getEncounter(6));
		revision.setOrderer(providerService.getProvider(1));

		expectedException.expect(APIException.class);
		expectedException.expectMessage("Order.cannot.have.more.than.one");
		orderService.saveOrder(revision, null);
	}

	/**
	 * @verifies pass for revision order if an active test order for the same concept and care settings exists
	 * @see OrderService#saveOrder(Order, OrderContext)
	 */
	@Test
	public void saveOrder_shouldPassForRevisionOrderIfAnActiveTestOrderForTheSameConceptAndCareSettingsExists() throws Exception {
		final Patient patient = patientService.getPatient(2);
		final Concept cd4Count = conceptService.getConcept(5497);
		TestOrder activeOrder = new TestOrder();
		activeOrder.setPatient(patient);
		activeOrder.setConcept(cd4Count);
		activeOrder.setEncounter(encounterService.getEncounter(6));
		activeOrder.setOrderer(providerService.getProvider(1));
		activeOrder.setCareSetting(orderService.getCareSetting(2));
		activeOrder.setDateActivated(new Date());
		activeOrder.setAutoExpireDate(DateUtils.addDays(new Date(), 10));
		orderService.saveOrder(activeOrder, null);

		//New order in future for same concept
		TestOrder secondOrder = new TestOrder();
		secondOrder.setPatient(activeOrder.getPatient());
		secondOrder.setConcept(activeOrder.getConcept());
		secondOrder.setEncounter(encounterService.getEncounter(6));
		secondOrder.setOrderer(providerService.getProvider(1));
		secondOrder.setCareSetting(activeOrder.getCareSetting());
		secondOrder.setDateActivated(new Date());
		secondOrder.setScheduledDate(DateUtils.addDays(activeOrder.getEffectiveStopDate(), 1));
		secondOrder.setUrgency(Order.Urgency.ON_SCHEDULED_DATE);
		orderService.saveOrder(secondOrder, null);

		//Revise second order to have scheduled date overlapping with active order
		TestOrder revision = secondOrder.cloneForRevision();
		revision.setScheduledDate(DateUtils.addDays(activeOrder.getEffectiveStartDate(), 2));
		revision.setEncounter(encounterService.getEncounter(6));
		revision.setOrderer(providerService.getProvider(1));

		Order savedSecondOrder = orderService.saveOrder(revision, null);

		assertNotNull(orderService.getOrder(savedSecondOrder.getOrderId()));
	}

	/**
	 * @verifies fail if an active drug order for the same concept and care setting exists
	 * @see OrderService#saveOrder(Order, OrderContext)
	 */
	@Test
	public void saveOrder_shouldFailIfAnActiveDrugOrderForTheSameConceptAndCareSettingExists() throws Exception {
		final Patient patient = patientService.getPatient(2);
		final Concept triomuneThirty = conceptService.getConcept(792);
		//sanity check that we have an active order for the same concept
		DrugOrder duplicateOrder = (DrugOrder) orderService.getOrder(3);
		assertTrue(duplicateOrder.isActive());
		assertEquals(triomuneThirty, duplicateOrder.getConcept());

		DrugOrder drugOrder = new DrugOrder();
		drugOrder.setPatient(patient);
		drugOrder.setCareSetting(orderService.getCareSetting(1));
		drugOrder.setConcept(triomuneThirty);
		drugOrder.setEncounter(encounterService.getEncounter(6));
		drugOrder.setOrderer(providerService.getProvider(1));
		drugOrder.setCareSetting(duplicateOrder.getCareSetting());
		drugOrder.setDrug(duplicateOrder.getDrug());
		drugOrder.setDose(duplicateOrder.getDose());
		drugOrder.setDoseUnits(duplicateOrder.getDoseUnits());
		drugOrder.setRoute(duplicateOrder.getRoute());
		drugOrder.setFrequency(duplicateOrder.getFrequency());
		drugOrder.setQuantity(duplicateOrder.getQuantity());
		drugOrder.setQuantityUnits(duplicateOrder.getQuantityUnits());
		drugOrder.setNumRefills(duplicateOrder.getNumRefills());

		expectedException.expect(APIException.class);
		expectedException.expectMessage("Order.cannot.have.more.than.one");
		orderService.saveOrder(drugOrder, null);
	}
	
	/**
	 * @verifies fail if an active order for the same concept and care setting exists
	 * @see OrderService#saveOrder(org.openmrs.Order, OrderContext)
	 */
	@Test
	public void saveOrder_shouldPassIfAnActiveTestOrderForTheSameConceptAndCareSettingExists() throws Exception {
		final Patient patient = patientService.getPatient(2);
		final Concept cd4Count = conceptService.getConcept(5497);
		//sanity check that we have an active order for the same concept
		TestOrder duplicateOrder = (TestOrder) orderService.getOrder(7);
		assertTrue(duplicateOrder.isActive());
		assertEquals(cd4Count, duplicateOrder.getConcept());
		
		Order order = new TestOrder();
		order.setPatient(patient);
		order.setCareSetting(orderService.getCareSetting(2));
		order.setConcept(cd4Count);
		order.setEncounter(encounterService.getEncounter(6));
		order.setOrderer(providerService.getProvider(1));
		order.setCareSetting(duplicateOrder.getCareSetting());

		Order savedOrder = orderService.saveOrder(order, null);

		assertNotNull(orderService.getOrder(savedOrder.getOrderId()));
	}
	
	/**
	 * @see OrderService#saveOrder(org.openmrs.Order, OrderContext)
	 */
	@Test
	public void saveOrder_shouldSaveRevisionOrderScheduledOnDateNotOverlappingWithAnActiveOrderForTheSameConceptAndCareSetting()
	        throws Exception {
		//sanity check that we have an active order
		final Patient patient = patientService.getPatient(2);
		final Concept cd4Count = conceptService.getConcept(5497);
		TestOrder activeOrder = new TestOrder();
		activeOrder.setPatient(patient);
		activeOrder.setConcept(cd4Count);
		activeOrder.setEncounter(encounterService.getEncounter(6));
		activeOrder.setOrderer(providerService.getProvider(1));
		activeOrder.setCareSetting(orderService.getCareSetting(2));
		activeOrder.setDateActivated(new Date());
		activeOrder.setAutoExpireDate(DateUtils.addDays(new Date(), 10));
		orderService.saveOrder(activeOrder, null);
		
		//New Drug order in future for same concept
		TestOrder secondOrder = new TestOrder();
		secondOrder.setPatient(activeOrder.getPatient());
		secondOrder.setConcept(activeOrder.getConcept());
		secondOrder.setEncounter(encounterService.getEncounter(6));
		secondOrder.setOrderer(providerService.getProvider(1));
		secondOrder.setCareSetting(activeOrder.getCareSetting());
		secondOrder.setDateActivated(new Date());
		secondOrder.setScheduledDate(DateUtils.addDays(activeOrder.getEffectiveStopDate(), 1));
		secondOrder.setUrgency(Order.Urgency.ON_SCHEDULED_DATE);
		orderService.saveOrder(secondOrder, null);
		
		//Revise Second Order to have scheduled date not overlapping with active order
		TestOrder revision = secondOrder.cloneForRevision();
		revision.setScheduledDate(DateUtils.addDays(activeOrder.getEffectiveStopDate(), 2));
		revision.setEncounter(encounterService.getEncounter(6));
		revision.setOrderer(providerService.getProvider(1));
		
		Order savedRevisionOrder = orderService.saveOrder(revision, null);
		
		assertNotNull(orderService.getOrder(savedRevisionOrder.getOrderId()));
	}
	
	/**
	 * @verifies pass if an active drug order for the same concept and care setting but different
	 *           formulation exists
	 * @see OrderService#saveOrder(org.openmrs.Order, OrderContext)
	 */
	@Test
	public void saveOrder_shouldPassIfAnActiveDrugOrderForTheSameConceptAndCareSettingButDifferentFormulationExists()
	        throws Exception {
		executeDataSet("org/openmrs/api/include/OrderServiceTest-drugOrdersWithSameConceptAndDifferentFormAndStrength.xml");
		final Patient patient = patientService.getPatient(2);
		//sanity check that we have an active order
		DrugOrder existingOrder = (DrugOrder) orderService.getOrder(1000);
		assertTrue(existingOrder.isActive());
		//New Drug order
		DrugOrder order = new DrugOrder();
		order.setPatient(patient);
		order.setConcept(existingOrder.getConcept());
		order.setEncounter(encounterService.getEncounter(6));
		order.setOrderer(providerService.getProvider(1));
		order.setCareSetting(existingOrder.getCareSetting());
		order.setDrug(conceptService.getDrug(3001));
		order.setDosingType(FreeTextDosingInstructions.class);
		order.setDosingInstructions("2 for 5 days");
		order.setQuantity(10.0);
		order.setQuantityUnits(conceptService.getConcept(51));
		order.setNumRefills(2);
		
		Order savedDrugOrder = orderService.saveOrder(order, null);
		
		assertNotNull(orderService.getOrder(savedDrugOrder.getOrderId()));
	}
	
	/**
	 * @verifies fail if an active drug order for the same drug formulation exists
	 * @see OrderService#saveOrder(org.openmrs.Order, OrderContext)
	 */
	@Test
	public void saveOrder_shouldFailIfAnActiveDrugOrderForTheSameDrugFormulationExists() throws Exception {
		executeDataSet("org/openmrs/api/include/OrderServiceTest-drugOrdersWithSameConceptAndDifferentFormAndStrength.xml");
		final Patient patient = patientService.getPatient(2);
		//sanity check that we have an active order for the same concept
		DrugOrder existingOrder = (DrugOrder) orderService.getOrder(1000);
		assertTrue(existingOrder.isActive());
		
		//New Drug order
		DrugOrder order = new DrugOrder();
		order.setPatient(patient);
		order.setDrug(existingOrder.getDrug());
		order.setEncounter(encounterService.getEncounter(6));
		order.setOrderer(providerService.getProvider(1));
		order.setCareSetting(existingOrder.getCareSetting());
		order.setDosingType(FreeTextDosingInstructions.class);
		order.setDosingInstructions("2 for 5 days");
		order.setQuantity(10.0);
		order.setQuantityUnits(conceptService.getConcept(51));
		order.setNumRefills(2);
		
		expectedException.expect(APIException.class);
		expectedException.expectMessage("Order.cannot.have.more.than.one");
		orderService.saveOrder(order, null);
	}
	
	/**
	 * @verifies pass if an active order for the same concept exists in a different care setting
	 * @see OrderService#saveOrder(org.openmrs.Order, OrderContext)
	 */
	@Test
	public void saveOrder_shouldPassIfAnActiveOrderForTheSameConceptExistsInADifferentCareSetting() throws Exception {
		final Patient patient = patientService.getPatient(2);
		final Concept cd4Count = conceptService.getConcept(5497);
		TestOrder duplicateOrder = (TestOrder) orderService.getOrder(7);
		final CareSetting inpatient = orderService.getCareSetting(2);
		assertNotEquals(inpatient, duplicateOrder.getCareSetting());
		assertTrue(duplicateOrder.isActive());
		assertEquals(cd4Count, duplicateOrder.getConcept());
		int initialActiveOrderCount = orderService.getActiveOrders(patient, null, null, null).size();
		
		TestOrder order = new TestOrder();
		order.setPatient(patient);
		order.setCareSetting(orderService.getCareSetting(2));
		order.setConcept(cd4Count);
		order.setEncounter(encounterService.getEncounter(6));
		order.setOrderer(providerService.getProvider(1));
		order.setCareSetting(inpatient);
		
		orderService.saveOrder(order, null);
		List<Order> activeOrders = orderService.getActiveOrders(patient, null, null, null);
		assertEquals(++initialActiveOrderCount, activeOrders.size());
	}
	
	/**
	 * @verifies roll the autoExpireDate to the end of the day if it has no time component
	 * @see OrderService#saveOrder(org.openmrs.Order, OrderContext)
	 */
	@Test
	public void saveOrder_shouldRollTheAutoExpireDateToTheEndOfTheDayIfItHasNoTimeComponent() throws Exception {
		Order order = new TestOrder();
		order.setPatient(patientService.getPatient(2));
		order.setCareSetting(orderService.getCareSetting(2));
		order.setConcept(conceptService.getConcept(5089));
		order.setEncounter(encounterService.getEncounter(6));
		order.setOrderer(providerService.getProvider(1));
		DateFormat dateformat = new SimpleDateFormat("dd/MM/yyyy");
		order.setDateActivated(dateformat.parse("14/08/2014"));
		order.setAutoExpireDate(dateformat.parse("18/08/2014"));
		
		orderService.saveOrder(order, null);
		dateformat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.S");
		assertEquals(dateformat.parse("18/08/2014 23:59:59.999"), order.getAutoExpireDate());
	}
	
	/**
	 * @verifies not change the autoExpireDate if it has a time component
	 * @see OrderService#saveOrder(org.openmrs.Order, OrderContext)
	 */
	@Test
	public void saveOrder_shouldNotChangeTheAutoExpireDateIfItHasATimeComponent() throws Exception {
		Order order = new TestOrder();
		order.setPatient(patientService.getPatient(2));
		order.setCareSetting(orderService.getCareSetting(2));
		order.setConcept(conceptService.getConcept(5089));
		order.setEncounter(encounterService.getEncounter(6));
		order.setOrderer(providerService.getProvider(1));
		order.setDateActivated(new Date());
		DateFormat dateformat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		order.setDateActivated(dateformat.parse("14/08/2014 10:00:00"));
		Date autoExpireDate = dateformat.parse("18/08/2014 10:00:00");
		order.setAutoExpireDate(autoExpireDate);
		
		orderService.saveOrder(order, null);
		assertEquals(autoExpireDate, order.getAutoExpireDate());
	}
	
	/**
	 * @verifies pass if an active drug order for the same drug formulation exists beyond new
	 *           order's schedule
	 * @see OrderService#saveOrder(org.openmrs.Order, OrderContext)
	 */
	@Test
	public void saveOrder_shouldPassIfAnActiveDrugOrderForTheSameDrugFormulationExistsBeyondSchedule() throws Exception {
		executeDataSet("org/openmrs/api/include/OrderServiceTest-DrugOrders.xml");
		final Patient patient = patientService.getPatient(2);
		
		DrugOrder existingOrder = (DrugOrder) orderService.getOrder(2000);
		int initialActiveOrderCount = orderService.getActiveOrders(patient, null, null, null).size();
		
		//New Drug order
		DrugOrder order = new DrugOrder();
		order.setPatient(patient);
		order.setDrug(existingOrder.getDrug());
		order.setEncounter(encounterService.getEncounter(6));
		order.setOrderer(providerService.getProvider(1));
		order.setCareSetting(existingOrder.getCareSetting());
		order.setDosingType(FreeTextDosingInstructions.class);
		order.setDosingInstructions("2 for 10 days");
		order.setQuantity(10.0);
		order.setQuantityUnits(conceptService.getConcept(51));
		order.setNumRefills(2);
		order.setUrgency(Order.Urgency.ON_SCHEDULED_DATE);
		
		order.setScheduledDate(DateUtils.addDays(existingOrder.getDateStopped(), 1));
		
		orderService.saveOrder(order, null);
		List<Order> activeOrders = orderService.getActiveOrders(patient, null, null, null);
		assertEquals(++initialActiveOrderCount, activeOrders.size());
	}
	
	/**
	 * @verifies find order type object given valid id
	 * @see OrderService#getOrderType(Integer)
	 */
	@Test
	public void getOrderType_shouldFindOrderTypeObjectGivenValidId() throws Exception {
		assertEquals("Drug order", orderService.getOrderType(1).getName());
	}
	
	/**
	 * @verifies return null if no order type object found with given id
	 * @see OrderService#getOrderType(Integer)
	 */
	@Test
	public void getOrderType_shouldReturnNullIfNoOrderTypeObjectFoundWithGivenId() throws Exception {
		OrderType orderType = orderService.getOrderType(1000);
		assertNull(orderType);
	}
	
	/**
	 * @verifies find order type object given valid uuid
	 * @see OrderService#getOrderTypeByUuid(String)
	 */
	@Test
	public void getOrderTypeByUuid_shouldFindOrderTypeObjectGivenValidUuid() throws Exception {
		OrderType orderType = orderService.getOrderTypeByUuid("131168f4-15f5-102d-96e4-000c29c2a5d7");
		assertEquals("Drug order", orderType.getName());
	}
	
	/**
	 * @verifies return null if no order type object found with given uuid
	 * @see OrderService#getOrderTypeByUuid(String)
	 */
	@Test
	public void getOrderTypeByUuid_shouldReturnNullIfNoOrderTypeObjectFoundWithGivenUuid() throws Exception {
		assertNull(orderService.getOrderTypeByUuid("some random uuid"));
	}
	
	/**
	 * @verifies get all order types if includeRetired is set to true
	 * @see OrderService#getOrderTypes(boolean)
	 */
	@Test
	public void getOrderTypes_shouldGetAllOrderTypesIfIncludeRetiredIsSetToTrue() throws Exception {
		assertEquals(14, orderService.getOrderTypes(true).size());
	}
	
	/**
	 * @verifies get all non retired order types if includeRetired is set to false
	 * @see OrderService#getOrderTypes(boolean)
	 */
	@Test
	public void getOrderTypes_shouldGetAllNonRetiredOrderTypesIfIncludeRetiredIsSetToFalse() throws Exception {
		assertEquals(11, orderService.getOrderTypes(false).size());
	}
	
	/**
	 * @verifies return the order type that matches the specified name
	 * @see OrderService#getOrderTypeByName(String)
	 */
	@Test
	public void getOrderTypeByName_shouldReturnTheOrderTypeThatMatchesTheSpecifiedName() throws Exception {
		OrderType orderType = orderService.getOrderTypeByName("Drug order");
		assertEquals("131168f4-15f5-102d-96e4-000c29c2a5d7", orderType.getUuid());
	}
	
	/**
	 * @verifies fail if patient is null
	 * @see OrderService#getOrders(org.openmrs.Patient, org.openmrs.CareSetting,
	 *      org.openmrs.OrderType, boolean)
	 */
	@Test
	public void getOrders_shouldFailIfPatientIsNull() throws Exception {
		expectedException.expect(IllegalArgumentException.class);
		expectedException.expectMessage("Patient is required");
		orderService.getOrders(null, null, null, false);
	}
	
	/**
	 * @verifies fail if careSetting is null
	 * @see OrderService#getOrders(org.openmrs.Patient, org.openmrs.CareSetting,
	 *      org.openmrs.OrderType, boolean)
	 */
	@Test
	public void getOrders_shouldFailIfCareSettingIsNull() throws Exception {
		expectedException.expect(IllegalArgumentException.class);
		expectedException.expectMessage("CareSetting is required");
		orderService.getOrders(new Patient(), null, null, false);
	}
	
	/**
	 * @verifies get the orders that match all the arguments
	 * @see OrderService#getOrders(org.openmrs.Patient, org.openmrs.CareSetting,
	 *      org.openmrs.OrderType, boolean)
	 */
	@Test
	public void getOrders_shouldGetTheOrdersThatMatchAllTheArguments() throws Exception {
		Patient patient = patientService.getPatient(2);
		CareSetting outPatient = orderService.getCareSetting(1);
		OrderType testOrderType = orderService.getOrderType(2);
		List<Order> testOrders = orderService.getOrders(patient, outPatient, testOrderType, false);
		assertEquals(3, testOrders.size());
		TestUtil.containsId(testOrders, 6);
		TestUtil.containsId(testOrders, 7);
		TestUtil.containsId(testOrders, 9);
		
		OrderType drugOrderType = orderService.getOrderType(1);
		List<Order> drugOrders = orderService.getOrders(patient, outPatient, drugOrderType, false);
		assertEquals(5, drugOrders.size());
		TestUtil.containsId(drugOrders, 2);
		TestUtil.containsId(drugOrders, 3);
		TestUtil.containsId(drugOrders, 44);
		TestUtil.containsId(drugOrders, 444);
		TestUtil.containsId(drugOrders, 5);
		
		CareSetting inPatient = orderService.getCareSetting(2);
		List<Order> inPatientDrugOrders = orderService.getOrders(patient, inPatient, drugOrderType, false);
		assertEquals(222, inPatientDrugOrders.get(0).getOrderId().intValue());
	}
	
	/**
	 * @verifies get all unvoided matches if includeVoided is set to false
	 * @see OrderService#getOrders(org.openmrs.Patient, org.openmrs.CareSetting,
	 *      org.openmrs.OrderType, boolean)
	 */
	@Test
	public void getOrders_shouldGetAllUnvoidedMatchesIfIncludeVoidedIsSetToFalse() throws Exception {
		Patient patient = patientService.getPatient(2);
		CareSetting outPatient = orderService.getCareSetting(1);
		OrderType testOrderType = orderService.getOrderType(2);
		assertEquals(3, orderService.getOrders(patient, outPatient, testOrderType, false).size());
	}
	
	/**
	 * @verifies include voided matches if includeVoided is set to true
	 * @see OrderService#getOrders(org.openmrs.Patient, org.openmrs.CareSetting,
	 *      org.openmrs.OrderType, boolean)
	 */
	@Test
	public void getOrders_shouldIncludeVoidedMatchesIfIncludeVoidedIsSetToTrue() throws Exception {
		Patient patient = patientService.getPatient(2);
		CareSetting outPatient = orderService.getCareSetting(1);
		OrderType testOrderType = orderService.getOrderType(2);
		assertEquals(4, orderService.getOrders(patient, outPatient, testOrderType, true).size());
	}
	
	/**
	 * @verifies include orders for sub types if order type is specified
	 * @see OrderService#getOrders(org.openmrs.Patient, org.openmrs.CareSetting,
	 *      org.openmrs.OrderType, boolean)
	 */
	@Test
	public void getOrders_shouldIncludeOrdersForSubTypesIfOrderTypeIsSpecified() throws Exception {
		executeDataSet("org/openmrs/api/include/OrderServiceTest-otherOrders.xml");
		Patient patient = patientService.getPatient(2);
		OrderType testOrderType = orderService.getOrderType(2);
		CareSetting outPatient = orderService.getCareSetting(1);
		List<Order> orders = orderService.getOrders(patient, outPatient, testOrderType, false);
		assertEquals(7, orders.size());
		Order[] expectedOrder1 = { orderService.getOrder(6), orderService.getOrder(7), orderService.getOrder(9),
		        orderService.getOrder(101), orderService.getOrder(102), orderService.getOrder(103),
		        orderService.getOrder(104) };
		assertThat(orders, hasItems(expectedOrder1));
		
		OrderType labTestOrderType = orderService.getOrderType(7);
		orders = orderService.getOrders(patient, outPatient, labTestOrderType, false);
		assertEquals(3, orderService.getOrders(patient, outPatient, labTestOrderType, false).size());
		Order[] expectedOrder2 = { orderService.getOrder(101), orderService.getOrder(103), orderService.getOrder(104) };
		assertThat(orders, hasItems(expectedOrder2));
	}
	
	/**
	 * @verifies fail if patient is null
	 * @see OrderService#getAllOrdersByPatient(org.openmrs.Patient)
	 */
	@Test
	public void getAllOrdersByPatient_shouldFailIfPatientIsNull() throws Exception {
		expectedException.expect(IllegalArgumentException.class);
		expectedException.expectMessage("Patient is required");
		orderService.getAllOrdersByPatient(null);
	}
	
	/**
	 * @verifies get all the orders for the specified patient
	 * @see OrderService#getAllOrdersByPatient(org.openmrs.Patient)
	 */
	@Test
	public void getAllOrdersByPatient_shouldGetAllTheOrdersForTheSpecifiedPatient() throws Exception {
		assertEquals(12, orderService.getAllOrdersByPatient(patientService.getPatient(2)).size());
		assertEquals(2, orderService.getAllOrdersByPatient(patientService.getPatient(7)).size());
	}
	
	/**
	 * @verifies set order type if null but mapped to the concept class
	 * @see OrderService#saveOrder(org.openmrs.Order, OrderContext)
	 */
	@Test
	public void saveOrder_shouldSetOrderTypeIfNullButMappedToTheConceptClass() throws Exception {
		TestOrder order = new TestOrder();
		order.setPatient(patientService.getPatient(7));
		order.setConcept(conceptService.getConcept(5497));
		order.setOrderer(providerService.getProvider(1));
		order.setCareSetting(orderService.getCareSetting(1));
		order.setEncounter(encounterService.getEncounter(3));
		order.setDateActivated(new Date());
		orderService.saveOrder(order, null);
		assertEquals(2, order.getOrderType().getOrderTypeId().intValue());
	}
	
	/**
	 * @verifies fail if order type is null and not mapped to the concept class
	 * @see OrderService#saveOrder(org.openmrs.Order, OrderContext)
	 */
	@Test
	public void saveOrder_shouldFailIfOrderTypeIsNullAndNotMappedToTheConceptClass() throws Exception {
		Order order = new Order();
		order.setPatient(patientService.getPatient(7));
		order.setConcept(conceptService.getConcept(9));
		order.setOrderer(providerService.getProvider(1));
		order.setCareSetting(orderService.getCareSetting(1));
		order.setEncounter(encounterService.getEncounter(3));
		order.setDateActivated(new Date());
		expectedException.expect(APIException.class);
		expectedException.expectMessage("Order.type.cannot.determine");
		orderService.saveOrder(order, null);
	}
	
	/**
	 * @see {@link OrderService#saveOrderType(org.openmrs.OrderType)}
	 */
	@Test
	@Verifies(value = "should add a new order type to the database", method = "saveOrderType(org.openmrs.OrderType)")
	public void saveOrderType_shouldAddANewOrderTypeToTheDatabase() {
		int orderTypeCount = orderService.getOrderTypes(true).size();
		OrderType orderType = new OrderType();
		orderType.setName("New Order");
		orderType.setJavaClassName("org.openmrs.NewTestOrder");
		orderType.setDescription("New order type for testing");
		orderType.setRetired(false);
		orderType = orderService.saveOrderType(orderType);
		assertNotNull(orderType);
		assertEquals("New Order", orderType.getName());
		assertNotNull(orderType.getId());
		assertEquals((orderTypeCount + 1), orderService.getOrderTypes(true).size());
	}
	
	/**
	 * @verifies edit an existing order type
	 * @see OrderService#saveOrderType(org.openmrs.OrderType)
	 */
	@Test
	public void saveOrderType_shouldEditAnExistingOrderType() throws Exception {
		OrderType orderType = orderService.getOrderType(1);
		assertNull(orderType.getDateChanged());
		assertNull(orderType.getChangedBy());
		final String newDescription = "new";
		orderType.setDescription(newDescription);
		
		orderService.saveOrderType(orderType);
		Context.flushSession();
		assertNotNull(orderType.getDateChanged());
		assertNotNull(orderType.getChangedBy());
	}
	
	/**
	 * @see {@link OrderService#purgeOrderType(org.openmrs.OrderType)}
	 */
	@Test
	@Verifies(value = "should delete order type if not in use", method = "purgeOrderType(org.openmrs.OrderType)")
	public void purgeOrderType_shouldDeleteOrderTypeIfNotInUse() {
		final Integer id = 13;
		OrderType orderType = orderService.getOrderType(id);
		assertNotNull(orderType);
		orderService.purgeOrderType(orderType);
		assertNull(orderService.getOrderType(id));
	}
	
	/**
	 * @see {@link OrderService#purgeOrderType(org.openmrs.OrderType)}
	 */
	@Test
	@Verifies(value = "should not allow deleting an order type that is in use", method = "purgeOrderType(org.openmrs.OrderType)")
	public void purgeOrderType_shouldNotAllowDeletingAnOrderTypeThatIsInUse() {
		OrderType orderType = orderService.getOrderType(1);
		assertNotNull(orderType);
		expectedException.expect(APIException.class);
		expectedException.expectMessage("Order.type.cannot.delete");
		orderService.purgeOrderType(orderType);
	}
	
	/**
	 * @see {@link OrderService#retireOrderType(org.openmrs.OrderType, String)}
	 */
	@Test
	@Verifies(value = "should retire order type", method = "retireOrderType(org.openmrs.OrderType, String)")
	public void retireOrderType_shouldRetireOrderType() {
		OrderType orderType = orderService.getOrderType(15);
		assertFalse(orderType.getRetired());
		assertNull(orderType.getRetiredBy());
		assertNull(orderType.getRetireReason());
		assertNull(orderType.getDateRetired());
		orderService.retireOrderType(orderType, "Retire for testing purposes");
		orderType = orderService.getOrderType(15);
		assertTrue(orderType.getRetired());
		assertNotNull(orderType.getRetiredBy());
		assertNotNull(orderType.getRetireReason());
		assertNotNull(orderType.getDateRetired());
	}
	
	/**
	 * @see {@link OrderService#unretireOrderType(org.openmrs.OrderType)}
	 */
	@Test
	@Verifies(value = "should unretire order type", method = "unretireOrderType(org.openmrs.OrderType)")
	public void unretireOrderType_shouldUnretireOrderType() {
		OrderType orderType = orderService.getOrderType(16);
		assertTrue(orderType.getRetired());
		assertNotNull(orderType.getRetiredBy());
		assertNotNull(orderType.getRetireReason());
		assertNotNull(orderType.getDateRetired());
		orderService.unretireOrderType(orderType);
		orderType = orderService.getOrderType(16);
		assertFalse(orderType.getRetired());
		assertNull(orderType.getRetiredBy());
		assertNull(orderType.getRetireReason());
		assertNull(orderType.getDateRetired());
	}
	
	/**
	 * @see {@link OrderService#getSubtypes(org.openmrs.OrderType, boolean)}
	 */
	@Test
	@Verifies(value = "should return all order subtypes of given order type", method = "getOrderSubtypes(org.openmrs.OrderType, boolean)")
	public void getOrderSubTypes_shouldGetAllSubOrderTypesWithRetiredOrderTypes() {
		List<OrderType> orderTypeList = orderService.getSubtypes(orderService.getOrderType(2), true);
		assertEquals(7, orderTypeList.size());
	}
	
	/**
	 * @see {@link OrderService#getSubtypes(org.openmrs.OrderType, boolean)}
	 */
	@Test
	@Verifies(value = "should return unretired order subtypes of given order type", method = "getOrderSubtypes(org.openmrs.OrderType, boolean)")
	public void getOrderSubTypes_shouldGetAllSubOrderTypesWithoutRetiredOrderTypes() {
		List<OrderType> orderTypeList = orderService.getSubtypes(orderService.getOrderType(2), false);
		assertEquals(6, orderTypeList.size());
	}
	
	/**
	 * @verifies default to care setting and order type defined in the order context if null
	 * @see OrderService#saveOrder(org.openmrs.Order, OrderContext)
	 */
	@Test
	public void saveOrder_shouldDefaultToCareSettingAndOrderTypeDefinedInTheOrderContextIfNull() throws Exception {
		Order order = new TestOrder();
		order.setPatient(patientService.getPatient(7));
		Concept trimune30 = conceptService.getConcept(792);
		order.setConcept(trimune30);
		order.setOrderer(providerService.getProvider(1));
		order.setEncounter(encounterService.getEncounter(3));
		order.setDateActivated(new Date());
		OrderType expectedOrderType = orderService.getOrderType(2);
		CareSetting expectedCareSetting = orderService.getCareSetting(1);
		OrderContext orderContext = new OrderContext();
		orderContext.setOrderType(expectedOrderType);
		orderContext.setCareSetting(expectedCareSetting);
		order = orderService.saveOrder(order, orderContext);
		assertFalse(expectedOrderType.getConceptClasses().contains(trimune30.getConceptClass()));
		assertEquals(expectedOrderType, order.getOrderType());
		assertEquals(expectedCareSetting, order.getCareSetting());
	}
	
	/**
	 * @see OrderService#getDiscontinuationOrder(Order)
	 * @verifies return discontinuation order if order has been discontinued
	 */
	@Test
	public void getDiscontinuationOrder_shouldReturnDiscontinuationOrderIfOrderHasBeenDiscontinued() throws Exception {
		Order order = orderService.getOrder(111);
		Order discontinuationOrder = orderService.discontinueOrder(order, "no reason", new Date(), providerService
		        .getProvider(1), order.getEncounter());
		
		Order foundDiscontinuationOrder = orderService.getDiscontinuationOrder(order);
		
		assertThat(foundDiscontinuationOrder, is(discontinuationOrder));
	}
	
	/**
	 * @see OrderService#getDiscontinuationOrder(Order)
	 * @verifies return null if order has not been discontinued
	 */
	@Test
	public void getDiscontinuationOrder_shouldReturnNullIfOrderHasNotBeenDiscontinued() throws Exception {
		Order order = orderService.getOrder(111);
		Order discontinuationOrder = orderService.getDiscontinuationOrder(order);
		
		assertThat(discontinuationOrder, is(nullValue()));
	}
	
	/**
	 * @see OrderService#getOrderTypeByConceptClass(ConceptClass)
	 * @verifies get order type mapped to the given concept class
	 */
	@Test
	public void getOrderTypeByConceptClass_shouldGetOrderTypeMappedToTheGivenConceptClass() throws Exception {
		OrderType orderType = orderService.getOrderTypeByConceptClass(Context.getConceptService().getConceptClass(1));
		
		Assert.assertNotNull(orderType);
		Assert.assertEquals(2, orderType.getOrderTypeId().intValue());
	}
	
	/**
	 * @see OrderService#getOrderTypeByConcept(Concept)
	 * @verifies get order type mapped to the given concept
	 */
	@Test
	public void getOrderTypeByConcept_shouldGetOrderTypeMappedToTheGivenConcept() throws Exception {
		OrderType orderType = orderService.getOrderTypeByConcept(Context.getConceptService().getConcept(5089));
		
		Assert.assertNotNull(orderType);
		Assert.assertEquals(2, orderType.getOrderTypeId().intValue());
	}
	
	/**
	 * @verifies not allow changing the patient of the previous order when revising an order
	 * @see OrderService#saveOrder(org.openmrs.Order, OrderContext)
	 */
	@Test
	public void saveOrder_shouldNotAllowChangingThePatientOfThePreviousOrderWhenRevisingAnOrder() throws Exception {
		Order orderToRevise = orderService.getOrder(7);
		Patient newPatient = patientService.getPatient(7);
		assertFalse(orderToRevise.getPatient().equals(newPatient));
		orderToRevise.setPatient(newPatient);
		Order order = orderToRevise.cloneForRevision();
		order.setDateActivated(new Date());
		order.setEncounter(encounterService.getEncounter(3));
		order.setOrderer(providerService.getProvider(1));
		
		expectedException.expect(APIException.class);
		expectedException.expectMessage("Order.cannot.change.patient");
		orderService.saveOrder(order, null);
	}
	
	/**
	 * @verifies not allow changing the careSetting of the previous order when revising an order
	 * @see OrderService#saveOrder(org.openmrs.Order, OrderContext)
	 */
	@Test
	public void saveOrder_shouldNotAllowChangingTheCareSettingOfThePreviousOrderWhenRevisingAnOrder() throws Exception {
		Order order = orderService.getOrder(7).cloneForRevision();
		order.setDateActivated(new Date());
		order.setEncounter(encounterService.getEncounter(6));
		order.setOrderer(providerService.getProvider(1));
		CareSetting newCareSetting = orderService.getCareSetting(2);
		assertFalse(order.getPreviousOrder().getCareSetting().equals(newCareSetting));
		order.getPreviousOrder().setCareSetting(newCareSetting);
		
		expectedException.expect(APIException.class);
		expectedException.expectMessage("Order.cannot.change.careSetting");
		orderService.saveOrder(order, null);
	}
	
	/**
	 * @verifies not allow changing the concept of the previous order when revising an order
	 * @see OrderService#saveOrder(org.openmrs.Order, OrderContext)
	 */
	@Test
	public void saveOrder_shouldNotAllowChangingTheConceptOfThePreviousOrderWhenRevisingAnOrder() throws Exception {
		Order order = orderService.getOrder(7).cloneForRevision();
		order.setDateActivated(new Date());
		order.setEncounter(encounterService.getEncounter(6));
		order.setOrderer(providerService.getProvider(1));
		Concept newConcept = conceptService.getConcept(5089);
		assertFalse(order.getPreviousOrder().getConcept().equals(newConcept));
		
		order.getPreviousOrder().setConcept(newConcept);
		
		expectedException.expect(APIException.class);
		expectedException.expectMessage("Order.cannot.change.concept");
		orderService.saveOrder(order, null);
	}
	
	/**
	 * @verifies not allow changing the drug of the previous drug order when revising an order
	 * @see OrderService#saveOrder(org.openmrs.Order, OrderContext)
	 */
	@Test
	public void saveOrder_shouldNotAllowChangingTheDrugOfThePreviousDrugOrderWhenRevisingAnOrder() throws Exception {
		DrugOrder order = (DrugOrder) orderService.getOrder(111).cloneForRevision();
		order.setDateActivated(new Date());
		order.setEncounter(encounterService.getEncounter(3));
		order.setOrderer(providerService.getProvider(1));
		Drug newDrug = conceptService.getDrug(2);
		DrugOrder previousOrder = (DrugOrder) order.getPreviousOrder();
		assertFalse(previousOrder.getDrug().equals(newDrug));
		previousOrder.setDrug(newDrug);
		
		expectedException.expect(APIException.class);
		expectedException.expectMessage("Order.cannot.change.drug");
		orderService.saveOrder(order, null);
	}
	
	/**
	 * @verifies fail if concept in previous order does not match that of the revised order
	 * @see OrderService#saveOrder(org.openmrs.Order, OrderContext)
	 */
	@Test
	public void saveOrder_shouldFailIfConceptInPreviousOrderDoesNotMatchThatOfTheRevisedOrder() throws Exception {
		Order previousOrder = orderService.getOrder(7);
		Order order = previousOrder.cloneForRevision();
		order.setDateActivated(new Date());
		order.setOrderer(providerService.getProvider(1));
		order.setEncounter(encounterService.getEncounter(6));
		Concept newConcept = conceptService.getConcept(5089);
		assertFalse(previousOrder.getConcept().equals(newConcept));
		order.setConcept(newConcept);
		
		expectedException.expect(APIException.class);
		expectedException.expectMessage("Order.previous.concept");
		orderService.saveOrder(order, null);
	}
	
	/**
	 * @verifies fail if the existing drug order matches the concept and not drug of the revised
	 *           order
	 * @see OrderService#saveOrder(org.openmrs.Order, OrderContext)
	 */
	@Test
	public void saveOrder_shouldFailIfTheExistingDrugOrderMatchesTheConceptAndNotDrugOfTheRevisedOrder() throws Exception {
		final DrugOrder orderToDiscontinue = (DrugOrder) orderService.getOrder(5);
		
		//create a different test drug
		Drug discontinuationOrderDrug = new Drug();
		discontinuationOrderDrug.setConcept(orderToDiscontinue.getConcept());
		discontinuationOrderDrug = conceptService.saveDrug(discontinuationOrderDrug);
		assertNotEquals(discontinuationOrderDrug, orderToDiscontinue.getDrug());
		assertNotNull(orderToDiscontinue.getDrug());
		
		DrugOrder order = orderToDiscontinue.cloneForRevision();
		order.setDateActivated(new Date());
		order.setOrderer(providerService.getProvider(1));
		order.setEncounter(encounterService.getEncounter(6));
		order.setDrug(discontinuationOrderDrug);
		
		expectedException.expect(APIException.class);
		expectedException.expectMessage("Order.previous.drug");
		orderService.saveOrder(order, null);
	}
	
	/**
	 * @verifies fail if the order type of the previous order does not match
	 * @see OrderService#saveOrder(org.openmrs.Order, OrderContext)
	 */
	@Test
	public void saveOrder_shouldFailIfTheOrderTypeOfThePreviousOrderDoesNotMatch() throws Exception {
		Order order = orderService.getOrder(7);
		assertTrue(OrderUtilTest.isActiveOrder(order, null));
		Order discontinuationOrder = order.cloneForDiscontinuing();
		OrderType orderType = orderService.getOrderType(7);
		assertNotEquals(discontinuationOrder.getOrderType(), orderType);
		assertTrue(OrderUtil.isType(discontinuationOrder.getOrderType(), orderType));
		discontinuationOrder.setOrderType(orderType);
		discontinuationOrder.setOrderer(Context.getProviderService().getProvider(1));
		discontinuationOrder.setEncounter(Context.getEncounterService().getEncounter(6));
		
		expectedException.expect(APIException.class);
		expectedException.expectMessage("Order.type.does.not.match");
		orderService.saveOrder(discontinuationOrder, null);
	}
	
	/**
	 * @verifies fail if the java type of the previous order does not match
	 * @see OrderService#saveOrder(org.openmrs.Order, OrderContext)
	 */
	@Test
	public void saveOrder_shouldFailIfTheJavaTypeOfThePreviousOrderDoesNotMatch() throws Exception {
		Order order = orderService.getOrder(7);
		assertTrue(OrderUtilTest.isActiveOrder(order, null));
		Order discontinuationOrder = new SomeTestOrder();
		discontinuationOrder.setCareSetting(order.getCareSetting());
		discontinuationOrder.setConcept(order.getConcept());
		discontinuationOrder.setAction(Order.Action.DISCONTINUE.DISCONTINUE);
		discontinuationOrder.setPreviousOrder(order);
		discontinuationOrder.setPatient(order.getPatient());
		assertTrue(order.getOrderType().getJavaClass().isAssignableFrom(discontinuationOrder.getClass()));
		discontinuationOrder.setOrderType(order.getOrderType());
		discontinuationOrder.setOrderer(Context.getProviderService().getProvider(1));
		discontinuationOrder.setEncounter(Context.getEncounterService().getEncounter(6));
		
		expectedException.expect(APIException.class);
		expectedException.expectMessage("Order.class.does.not.match");
		orderService.saveOrder(discontinuationOrder, null);
	}
	
	/**
	 * @verifies fail if the careSetting of the previous order does not match
	 * @see OrderService#saveOrder(org.openmrs.Order, OrderContext)
	 */
	@Test
	public void saveOrder_shouldFailIfTheCareSettingOfThePreviousOrderDoesNotMatch() throws Exception {
		Order order = orderService.getOrder(7);
		assertTrue(OrderUtilTest.isActiveOrder(order, null));
		Order discontinuationOrder = order.cloneForDiscontinuing();
		CareSetting careSetting = orderService.getCareSetting(2);
		assertNotEquals(discontinuationOrder.getCareSetting(), careSetting);
		discontinuationOrder.setCareSetting(careSetting);
		discontinuationOrder.setOrderer(Context.getProviderService().getProvider(1));
		discontinuationOrder.setEncounter(Context.getEncounterService().getEncounter(6));
		
		expectedException.expect(APIException.class);
		expectedException.expectMessage("Order.care.setting.does.not.match");
		orderService.saveOrder(discontinuationOrder, null);
	}
	
	/**
	 * @verifies set concept for drug orders if null
	 * @see OrderService#saveOrder(org.openmrs.Order, OrderContext)
	 */
	@Test
	public void saveOrder_shouldSetConceptForDrugOrdersIfNull() throws Exception {
		Patient patient = patientService.getPatient(7);
		CareSetting careSetting = orderService.getCareSetting(2);
		OrderType orderType = orderService.getOrderTypeByName("Drug order");
		
		//place drug order
		DrugOrder order = new DrugOrder();
		Encounter encounter = encounterService.getEncounter(3);
		order.setEncounter(encounter);
		order.setPatient(patient);
		order.setDrug(conceptService.getDrug(2));
		order.setCareSetting(careSetting);
		order.setOrderer(Context.getProviderService().getProvider(1));
		order.setDateActivated(encounter.getEncounterDatetime());
		order.setOrderType(orderType);
		order.setDosingType(FreeTextDosingInstructions.class);
		order.setInstructions("None");
		order.setDosingInstructions("Test Instruction");
		orderService.saveOrder(order, null);
		assertNotNull(order.getOrderId());
	}
	
	/**
	 * @see org.openmrs.api.OrderService#getDrugRoutes()
	 * @verifies get drug routes associated with concept uuid provided in global properties
	 */
	@Test
	public void getDrugRoutes_shouldGetDrugRoutesAssociatedConceptPrividedInGlobalProperties() throws Exception {
		List<Concept> drugRoutesList = orderService.getDrugRoutes();
		assertEquals(1, drugRoutesList.size());
		assertEquals(22, drugRoutesList.get(0).getConceptId().intValue());
	}
	
	/**
	 * @verifies void an order
	 * @see OrderService#voidOrder(org.openmrs.Order, String)
	 */
	@Test
	public void voidOrder_shouldVoidAnOrder() throws Exception {
		Order order = orderService.getOrder(1);
		assertFalse(order.getVoided());
		assertNull(order.getDateVoided());
		assertNull(order.getVoidedBy());
		assertNull(order.getVoidReason());
		
		orderService.voidOrder(order, "None");
		assertTrue(order.getVoided());
		assertNotNull(order.getDateVoided());
		assertNotNull(order.getVoidedBy());
		assertNotNull(order.getVoidReason());
	}
	
	/**
	 * @verifies unset dateStopped of the previous order if the specified order is a discontinuation
	 * @see OrderService#voidOrder(org.openmrs.Order, String)
	 */
	@Test
	public void voidOrder_shouldUnsetDateStoppedOfThePreviousOrderIfTheSpecifiedOrderIsADiscontinuation() throws Exception {
		Order order = orderService.getOrder(22);
		assertEquals(Action.DISCONTINUE, order.getAction());
		Order previousOrder = order.getPreviousOrder();
		assertNotNull(previousOrder.getDateStopped());
		assertFalse(order.getVoided());
		
		orderService.voidOrder(order, "None");
		//Ensures order interceptor is okay with all the changes
		Context.flushSession();
		assertTrue(order.getVoided());
		assertNull(previousOrder.getDateStopped());
	}
	
	/**
	 * @verifies unset dateStopped of the previous order if the specified order is a revision
	 * @see OrderService#voidOrder(org.openmrs.Order, String)
	 */
	@Test
	public void voidOrder_shouldUnsetDateStoppedOfThePreviousOrderIfTheSpecifiedOrderIsARevision() throws Exception {
		Order order = orderService.getOrder(111);
		assertEquals(Action.REVISE, order.getAction());
		Order previousOrder = order.getPreviousOrder();
		assertNotNull(previousOrder.getDateStopped());
		assertFalse(order.getVoided());
		
		orderService.voidOrder(order, "None");
		Context.flushSession();
		assertTrue(order.getVoided());
		assertNull(previousOrder.getDateStopped());
	}
	
	/**
	 * @verifies unvoid an order
	 * @see OrderService#unvoidOrder(org.openmrs.Order)
	 */
	@Test
	public void unvoidOrder_shouldUnvoidAnOrder() throws Exception {
		Order order = orderService.getOrder(8);
		assertTrue(order.getVoided());
		assertNotNull(order.getDateVoided());
		assertNotNull(order.getVoidedBy());
		assertNotNull(order.getVoidReason());
		
		orderService.unvoidOrder(order);
		assertFalse(order.getVoided());
		assertNull(order.getDateVoided());
		assertNull(order.getVoidedBy());
		assertNull(order.getVoidReason());
	}
	
	/**
	 * @verifies stop the previous order if the specified order is a discontinuation
	 * @see OrderService#unvoidOrder(org.openmrs.Order)
	 */
	@Test
	public void unvoidOrder_shouldStopThePreviousOrderIfTheSpecifiedOrderIsADiscontinuation() throws Exception {
		Order order = orderService.getOrder(22);
		assertEquals(Action.DISCONTINUE, order.getAction());
		Order previousOrder = order.getPreviousOrder();
		assertNotNull(previousOrder.getDateStopped());
		assertFalse(order.getVoided());
		
		//void the DC order for testing purposes so we can unvoid it later
		orderService.voidOrder(order, "None");
		Context.flushSession();
		assertTrue(order.getVoided());
		assertNull(previousOrder.getDateStopped());
		
		orderService.unvoidOrder(order);
		Context.flushSession();
		assertFalse(order.getVoided());
		assertNotNull(previousOrder.getDateStopped());
	}
	
	/**
	 * @verifies stop the previous order if the specified order is a revision
	 * @see OrderService#unvoidOrder(org.openmrs.Order)
	 */
	@Test
	public void unvoidOrder_shouldStopThePreviousOrderIfTheSpecifiedOrderIsARevision() throws Exception {
		Order order = orderService.getOrder(111);
		assertEquals(Action.REVISE, order.getAction());
		Order previousOrder = order.getPreviousOrder();
		assertNotNull(previousOrder.getDateStopped());
		assertFalse(order.getVoided());
		
		//void the revise order for testing purposes so we can unvoid it later
		orderService.voidOrder(order, "None");
		Context.flushSession();
		assertTrue(order.getVoided());
		assertNull(previousOrder.getDateStopped());
		
		orderService.unvoidOrder(order);
		Context.flushSession();
		assertFalse(order.getVoided());
		assertNotNull(previousOrder.getDateStopped());
	}
	
	/**
	 * @verifies fail for a discontinuation order if the previousOrder is inactive
	 * @see OrderService#unvoidOrder(org.openmrs.Order)
	 */
	@Test
	public void unvoidOrder_shouldFailForADiscontinuationOrderIfThePreviousOrderIsInactive() throws Exception {
		Order order = orderService.getOrder(22);
		assertEquals(Action.DISCONTINUE, order.getAction());
		Order previousOrder = order.getPreviousOrder();
		assertNotNull(previousOrder.getDateStopped());
		assertFalse(order.getVoided());
		
		//void the DC order for testing purposes so we can unvoid it later
		orderService.voidOrder(order, "None");
		assertTrue(order.getVoided());
		assertNull(previousOrder.getDateStopped());
		
		//stop the order with a different DC order
		orderService.discontinueOrder(previousOrder, "Testing", null, previousOrder.getOrderer(), previousOrder
		        .getEncounter());
		Thread.sleep(10);
		
		expectedException.expect(APIException.class);
		expectedException.expectMessage("Order.action.cannot.unvoid");
		orderService.unvoidOrder(order);
	}
	
	/**
	 * @verifies fail for a revise order if the previousOrder is inactive
	 * @see OrderService#unvoidOrder(org.openmrs.Order)
	 */
	@Test
	public void unvoidOrder_shouldFailForAReviseOrderIfThePreviousOrderIsInactive() throws Exception {
		Order order = orderService.getOrder(111);
		assertEquals(Action.REVISE, order.getAction());
		Order previousOrder = order.getPreviousOrder();
		assertNotNull(previousOrder.getDateStopped());
		assertFalse(order.getVoided());
		
		//void the DC order for testing purposes so we can unvoid it later
		orderService.voidOrder(order, "None");
		assertTrue(order.getVoided());
		assertNull(previousOrder.getDateStopped());
		
		//stop the order with a different REVISE order
		Order revise = previousOrder.cloneForRevision();
		revise.setOrderer(order.getOrderer());
		revise.setEncounter(order.getEncounter());
		orderService.saveOrder(revise, null);
		Thread.sleep(10);
		
		expectedException.expect(APIException.class);
		expectedException.expectMessage("Order.action.cannot.unvoid");
		orderService.unvoidOrder(order);
	}
	
	/**
	 * @verifies return revision order if order has been revised
	 * @see OrderService#getRevisionOrder(org.openmrs.Order)
	 */
	@Test
	public void getRevisionOrder_shouldReturnRevisionOrderIfOrderHasBeenRevised() throws Exception {
		assertEquals(orderService.getOrder(111), orderService.getRevisionOrder(orderService.getOrder(1)));
	}
	
	/**
	 * @verifies return null if order has not been revised
	 * @see OrderService#getRevisionOrder(org.openmrs.Order)
	 */
	@Test
	public void getRevisionOrder_shouldReturnNullIfOrderHasNotBeenRevised() throws Exception {
		assertNull(orderService.getRevisionOrder(orderService.getOrder(444)));
	}
	
	/**
	 * @see OrderService#getDiscontinuationOrder(Order)
	 * @verifies return null if dc order is voided
	 */
	@Test
	public void getDiscontinuationOrder_shouldReturnNullIfDcOrderIsVoided() throws Exception {
		Order order = orderService.getOrder(7);
		Order discontinueOrder = orderService.discontinueOrder(order, "Some reason", new Date(), providerService
		        .getProvider(1), encounterService.getEncounter(3));
		orderService.voidOrder(discontinueOrder, "Invalid reason");
		
		Order discontinuationOrder = orderService.getDiscontinuationOrder(order);
		assertThat(discontinuationOrder, is(nullValue()));
	}
	
	/**
	 * @verifies return the union of the dosing and dispensing units
	 * @see OrderService#getDrugDispensingUnits()
	 */
	@Test
	public void getDrugDispensingUnits_shouldReturnTheUnionOfTheDosingAndDispensingUnits() throws Exception {
		List<Concept> dispensingUnits = orderService.getDrugDispensingUnits();
		assertEquals(2, dispensingUnits.size());
		assertThat(dispensingUnits, containsInAnyOrder(hasId(50), hasId(51)));
	}
	
	/**
	 * @see OrderService#getDrugDispensingUnits()
	 * @verifies return an empty list if nothing is configured
	 */
	@Test
	public void getDrugDispensingUnits_shouldReturnAnEmptyListIfNothingIsConfigured() throws Exception {
		adminService.saveGlobalProperty(new GlobalProperty(OpenmrsConstants.GP_DRUG_DISPENSING_UNITS_CONCEPT_UUID, ""));
		adminService.saveGlobalProperty(new GlobalProperty(OpenmrsConstants.GP_DRUG_DOSING_UNITS_CONCEPT_UUID, ""));
		assertThat(orderService.getDrugDispensingUnits(), is(empty()));
	}
	
	/**
	 * @see OrderService#getDrugDosingUnits()
	 * @verifies return a list if GP is set
	 */
	@Test
	public void getDrugDosingUnits_shouldReturnAListIfGPIsSet() throws Exception {
		List<Concept> dosingUnits = orderService.getDrugDosingUnits();
		assertEquals(2, dosingUnits.size());
		assertThat(dosingUnits, containsInAnyOrder(hasId(50), hasId(51)));
	}
	
	/**
	 * @see OrderService#getDrugDosingUnits()
	 * @verifies return an empty list if nothing is configured
	 */
	@Test
	public void getDrugDosingUnits_shouldReturnAnEmptyListIfNothingIsConfigured() throws Exception {
		adminService.saveGlobalProperty(new GlobalProperty(OpenmrsConstants.GP_DRUG_DOSING_UNITS_CONCEPT_UUID, ""));
		assertThat(orderService.getDrugDosingUnits(), is(empty()));
	}
	
	/**
	 * @see OrderService#getDurationUnits()
	 * @verifies return a list if GP is set
	 */
	@Test
	public void getDurationUnits_shouldReturnAListIfGPIsSet() throws Exception {
		List<Concept> durationConcepts = orderService.getDurationUnits();
		assertEquals(1, durationConcepts.size());
		assertEquals(28, durationConcepts.get(0).getConceptId().intValue());
	}
	
	/**
	 * @see OrderService#getDurationUnits()
	 * @verifies return an empty list if nothing is configured
	 */
	@Test
	public void getDurationUnits_shouldReturnAnEmptyListIfNothingIsConfigured() throws Exception {
		adminService.saveGlobalProperty(new GlobalProperty(OpenmrsConstants.GP_DURATION_UNITS_CONCEPT_UUID, ""));
		assertThat(orderService.getDurationUnits(), is(empty()));
	}
	
	/**
	 * @verifies not return a voided revision order
	 * @see OrderService#getRevisionOrder(org.openmrs.Order)
	 */
	@Test
	public void getRevisionOrder_shouldNotReturnAVoidedRevisionOrder() throws Exception {
		Order order = orderService.getOrder(7);
		Order revision1 = order.cloneForRevision();
		revision1.setEncounter(order.getEncounter());
		revision1.setOrderer(order.getOrderer());
		orderService.saveOrder(revision1, null);
		assertEquals(revision1, orderService.getRevisionOrder(order));
		orderService.voidOrder(revision1, "Testing");
		assertThat(orderService.getRevisionOrder(order), is(nullValue()));
		
		//should return the new unvoided revision
		Order revision2 = order.cloneForRevision();
		revision2.setEncounter(order.getEncounter());
		revision2.setOrderer(order.getOrderer());
		orderService.saveOrder(revision2, null);
		assertEquals(revision2, orderService.getRevisionOrder(order));
	}
	
	/**
	 * @verifies pass for a discontinuation order with no previous order
	 * @see OrderService#saveOrder(org.openmrs.Order, OrderContext)
	 */
	@Test
	public void saveOrder_shouldPassForADiscontinuationOrderWithNoPreviousOrder() throws Exception {
		TestOrder dcOrder = new TestOrder();
		dcOrder.setAction(Action.DISCONTINUE);
		dcOrder.setPatient(patientService.getPatient(2));
		dcOrder.setCareSetting(orderService.getCareSetting(2));
		dcOrder.setConcept(conceptService.getConcept(5089));
		dcOrder.setEncounter(encounterService.getEncounter(6));
		dcOrder.setOrderer(providerService.getProvider(1));
		orderService.saveOrder(dcOrder, null);
	}
	
	/**
	 * @verifies return a list if GP is set
	 * @see OrderService#getTestSpecimenSources()
	 */
	@Test
	public void getTestSpecimenSources_shouldReturnAListIfGPIsSet() throws Exception {
		List<Concept> specimenSourceList = orderService.getTestSpecimenSources();
		assertEquals(1, specimenSourceList.size());
		assertEquals(22, specimenSourceList.get(0).getConceptId().intValue());
	}
	
	/**
	 * @verifies return an empty list if nothing is configured
	 * @see OrderService#getTestSpecimenSources()
	 */
	@Test
	public void getTestSpecimenSources_shouldReturnAnEmptyListIfNothingIsConfigured() throws Exception {
		adminService.saveGlobalProperty(new GlobalProperty(OpenmrsConstants.GP_TEST_SPECIMEN_SOURCES_CONCEPT_UUID, ""));
		assertThat(orderService.getTestSpecimenSources(), is(empty()));
	}
	
	/**
	 * @see OrderService#retireOrderType(org.openmrs.OrderType, String)
	 */
	@Test
	@Verifies(value = "should not retire concept class", method = "retireOrderType(OrderType orderType, String reason)")
	public void retireOrderType_shouldNotRetireIndependentField() throws Exception {
		OrderType orderType = orderService.getOrderType(2);
		ConceptClass conceptClass = conceptService.getConceptClass(1);
		Assert.assertFalse(conceptClass.isRetired());
		orderType.addConceptClass(conceptClass);
		orderService.retireOrderType(orderType, "test retire reason");
		Assert.assertFalse(conceptClass.isRetired());
	}
	
	/**
	 * @verifies set Order type to drug order if not set
	 * @see OrderService#saveOrder(org.openmrs.Order, OrderContext)
	 */
	@Test
	public void saveOrder_shouldSetOrderTypeOfDrugOrderToDrugOrderIfNotSetAndConceptNotMapped() throws Exception {
		Drug drug = conceptService.getDrug(2);
		Concept unmappedConcept = conceptService.getConcept(113);
		
		Assert.assertNull(orderService.getOrderTypeByConcept(unmappedConcept));
		drug.setConcept(unmappedConcept);
		
		DrugOrder drugOrder = new DrugOrder();
		Encounter encounter = encounterService.getEncounter(3);
		drugOrder.setEncounter(encounter);
		drugOrder.setPatient(patientService.getPatient(7));
		drugOrder.setCareSetting(orderService.getCareSetting(1));
		drugOrder.setOrderer(Context.getProviderService().getProvider(1));
		drugOrder.setDateActivated(encounter.getEncounterDatetime());
		drugOrder.setDrug(drug);
		drugOrder.setDosingType(SimpleDosingInstructions.class);
		drugOrder.setDose(300.0);
		drugOrder.setDoseUnits(conceptService.getConcept(50));
		drugOrder.setQuantity(20.0);
		drugOrder.setQuantityUnits(conceptService.getConcept(51));
		drugOrder.setFrequency(orderService.getOrderFrequency(3));
		drugOrder.setRoute(conceptService.getConcept(22));
		drugOrder.setNumRefills(10);
		drugOrder.setOrderType(null);
		
		orderService.saveOrder(drugOrder, null);
		Assert.assertNotNull(drugOrder.getOrderType());
		Assert.assertEquals(orderService.getOrderTypeByUuid(OrderType.DRUG_ORDER_TYPE_UUID), drugOrder.getOrderType());
	}
	
	/**
	 * @verifies set Order type of Test Order to test order if not set and concept not mapped
	 * @see OrderService#saveOrder(org.openmrs.Order, OrderContext)
	 */
	@Test
	public void saveOrder_shouldSetOrderTypeOfTestOrderToTestOrderIfNotSetAndConceptNotMapped() throws Exception {
		TestOrder testOrder = new TestOrder();
		testOrder.setPatient(patientService.getPatient(7));
		Concept unmappedConcept = conceptService.getConcept(113);
		
		Assert.assertNull(orderService.getOrderTypeByConcept(unmappedConcept));
		testOrder.setConcept(unmappedConcept);
		testOrder.setOrderer(providerService.getProvider(1));
		testOrder.setCareSetting(orderService.getCareSetting(1));
		Encounter encounter = encounterService.getEncounter(3);
		testOrder.setEncounter(encounter);
		testOrder.setDateActivated(encounter.getEncounterDatetime());
		testOrder.setClinicalHistory("Patient had a negative reaction to the test in the past");
		testOrder.setFrequency(orderService.getOrderFrequency(3));
		testOrder.setSpecimenSource(conceptService.getConcept(22));
		testOrder.setNumberOfRepeats(3);
		
		orderService.saveOrder(testOrder, null);
		Assert.assertNotNull(testOrder.getOrderType());
		Assert.assertEquals(orderService.getOrderTypeByUuid(OrderType.TEST_ORDER_TYPE_UUID), testOrder.getOrderType());
	}
	
	@Test
	public void saveOrder_shouldSetAutoExpireDateOfDrugOrderIfAutoExpireDateIsNotSet() throws Exception {
		executeDataSet("org/openmrs/api/include/OrderServiceTest-drugOrderAutoExpireDate.xml");
		Drug drug = conceptService.getDrug(3000);
		DrugOrder drugOrder = new DrugOrder();
		Encounter encounter = encounterService.getEncounter(3);
		drugOrder.setEncounter(encounter);
		drugOrder.setPatient(patientService.getPatient(7));
		drugOrder.setCareSetting(orderService.getCareSetting(1));
		drugOrder.setOrderer(Context.getProviderService().getProvider(1));
		drugOrder.setDrug(drug);
		drugOrder.setDosingType(SimpleDosingInstructions.class);
		drugOrder.setDose(300.0);
		drugOrder.setDoseUnits(conceptService.getConcept(50));
		drugOrder.setQuantity(20.0);
		drugOrder.setQuantityUnits(conceptService.getConcept(51));
		drugOrder.setFrequency(orderService.getOrderFrequency(3));
		drugOrder.setRoute(conceptService.getConcept(22));
		drugOrder.setNumRefills(0);
		drugOrder.setOrderType(null);
		drugOrder.setDateActivated(TestUtil.createDateTime("2014-08-03"));
		drugOrder.setDuration(20);// 20 days
		drugOrder.setDurationUnits(conceptService.getConcept(1001));
		
		Order savedOrder = orderService.saveOrder(drugOrder, null);
		
		Order loadedOrder = orderService.getOrder(savedOrder.getId());
		Assert.assertEquals(TestUtil.createDateTime("2014-08-22 23:59:59"), loadedOrder.getAutoExpireDate());
	}
	
	@Test
	public void saveOrder_shouldSetAutoExpireDateForReviseOrderWithSimpleDosingInstructions() throws Exception {
		executeDataSet("org/openmrs/api/include/OrderServiceTest-drugOrderAutoExpireDate.xml");
		DrugOrder originalOrder = (DrugOrder) orderService.getOrder(111);
		assertTrue(originalOrder.isActive());
		DrugOrder revisedOrder = originalOrder.cloneForRevision();
		revisedOrder.setOrderer(originalOrder.getOrderer());
		revisedOrder.setEncounter(originalOrder.getEncounter());
		
		revisedOrder.setNumRefills(0);
		revisedOrder.setAutoExpireDate(null);
		revisedOrder.setDuration(10);
		revisedOrder.setDurationUnits(conceptService.getConcept(1001));
		
		orderService.saveOrder(revisedOrder, null);
		
		assertNotNull(revisedOrder.getAutoExpireDate());
	}
}
