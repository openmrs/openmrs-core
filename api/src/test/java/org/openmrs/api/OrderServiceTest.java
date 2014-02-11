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
package org.openmrs.api;

import static org.hamcrest.Matchers.hasItems;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.openmrs.test.TestUtil.containsId;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.openmrs.CareSetting;
import org.openmrs.Concept;
import org.openmrs.Drug;
import org.openmrs.DrugOrder;
import org.openmrs.Obs;
import org.openmrs.Order;
import org.openmrs.Order.Action;
import org.openmrs.OrderFrequency;
import org.openmrs.Patient;
import org.openmrs.TestOrder;
import org.openmrs.api.context.Context;
import org.openmrs.order.OrderUtil;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.test.Verifies;
import org.openmrs.util.PrivilegeConstants;

/**
 * TODO clean up and test all methods in OrderService
 */
public class OrderServiceTest extends BaseContextSensitiveTest {
	
	private ConceptService conceptService;
	
	private OrderService orderService;
	
	private PatientService patientService;
	
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
	}
	
	/**
	 * @see {@link OrderService#saveOrder(Order)}
	 */
	@Test(expected = APIException.class)
	@Verifies(value = "should not save order if order doesnt validate", method = "saveOrder(Order)")
	public void saveOrder_shouldNotSaveOrderIfOrderDoesntValidate() throws Exception {
		OrderService orderService = Context.getOrderService();
		Order order = new Order();
		order.setPatient(null);
		orderService.saveOrder(order);
	}
	
	/**
	 * @see {@link OrderService#getOrderByUuid(String)}
	 */
	@Test
	@Verifies(value = "should find object given valid uuid", method = "getOrderByUuid(String)")
	public void getOrderByUuid_shouldFindObjectGivenValidUuid() throws Exception {
		String uuid = "921de0a3-05c4-444a-be03-e01b4c4b9142";
		Order order = Context.getOrderService().getOrderByUuid(uuid);
		Assert.assertEquals(1, (int) order.getOrderId());
	}
	
	/**
	 * @see {@link OrderService#getOrderByUuid(String)}
	 */
	@Test
	@Verifies(value = "should return null if no object found with given uuid", method = "getOrderByUuid(String)")
	public void getOrderByUuid_shouldReturnNullIfNoObjectFoundWithGivenUuid() throws Exception {
		Assert.assertNull(Context.getOrderService().getOrderByUuid("some invalid uuid"));
	}
	
	@Test
	public void purgeOrder_shouldDeleteObsThatReference() throws Exception {
		executeDataSet("org/openmrs/api/include/OrderServiceTest-deleteObsThatReference.xml");
		final String ordUuid = "0c96f25c-4949-4f72-9931-d808fbcdb612";
		final String obsUuid = "be3a4d7a-f9ab-47bb-aaad-bc0b452fcda4";
		ObsService os = Context.getObsService();
		OrderService service = Context.getOrderService();
		
		Obs obs = os.getObsByUuid(obsUuid);
		Assert.assertNotNull(obs);
		
		Order order = service.getOrderByUuid(ordUuid);
		Assert.assertNotNull(order);
		
		//sanity check to ensure that the obs and order are actually related
		Assert.assertEquals(order, obs.getOrder());
		
		//Ensure that passing false does not delete the related obs
		service.purgeOrder(order, false);
		Assert.assertNotNull(os.getObsByUuid(obsUuid));
		
		service.purgeOrder(order, true);
		
		//Ensure that actually the order got purged
		Assert.assertNull(service.getOrderByUuid(ordUuid));
		
		//Ensure that the related obs got deleted
		Assert.assertNull(os.getObsByUuid(obsUuid));
		
	}
	
	/**
	 * @see {@link OrderNumberGenerator#getNewOrderNumber()}
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
						uniqueOrderNumbers.add(((OrderNumberGenerator) Context.getOrderService()).getNewOrderNumber());
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
		Order order = Context.getOrderService().getOrderByOrderNumber("1");
		Assert.assertNotNull(order);
		Assert.assertEquals(1, (int) order.getOrderId());
	}
	
	/**
	 * @see {@link OrderService#getOrderByOrderNumber(String)}
	 */
	@Test
	@Verifies(value = "should return null if no object found with given order number", method = "getOrderByOrderNumber(String)")
	public void getOrderByOrderNumber_shouldReturnNullIfNoObjectFoundWithGivenOrderNumber() throws Exception {
		Assert.assertNull(Context.getOrderService().getOrderByOrderNumber("some invalid order number"));
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
		List<Order> orders = Context.getOrderService().getOrderHistoryByConcept(patient, concept);
		
		//They must be sorted by startDate starting with the latest
		Assert.assertEquals(4, orders.size());
		Assert.assertEquals(5, orders.get(0).getOrderId().intValue());
		Assert.assertEquals(444, orders.get(1).getOrderId().intValue());
		Assert.assertEquals(44, orders.get(2).getOrderId().intValue());
		Assert.assertEquals(4, orders.get(3).getOrderId().intValue());
		
		concept = Context.getConceptService().getConcept(792);
		orders = Context.getOrderService().getOrderHistoryByConcept(patient, concept);
		
		//They must be sorted by startDate starting with the latest
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
		List<Order> orders = Context.getOrderService().getOrderHistoryByConcept(patient, concept);
		Assert.assertEquals(0, orders.size());
	}
	
	/**
	 * @verifies reject a null concept
	 * @see OrderService#getOrderHistoryByConcept(org.openmrs.Patient, org.openmrs.Concept)
	 */
	@Test(expected = IllegalArgumentException.class)
	public void getOrderHistoryByConcept_shouldRejectANullConcept() throws Exception {
		orderService.getOrderHistoryByConcept(new Patient(), null);
	}
	
	/**
	 * @verifies reject a null patient
	 * @see OrderService#getOrderHistoryByConcept(org.openmrs.Patient, org.openmrs.Concept)
	 */
	@Test(expected = IllegalArgumentException.class)
	public void getOrderHistoryByConcept_shouldRejectANullPatient() throws Exception {
		orderService.getOrderHistoryByConcept(null, new Concept());
	}
	
	/**
	 * @see {@link OrderService#getOrderHistoryByOrderNumber(String)}
	 */
	@Test
	@Verifies(value = "should return all order history for given order number", method = "getOrderHistoryByOrderNumber(String)")
	public void getOrderHistoryByOrderNumber_shouldReturnAllOrderHistoryForGivenOrderNumber() throws Exception {
		List<Order> orders = Context.getOrderService().getOrderHistoryByOrderNumber("111");
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
		assertEquals("28090760-7c38-11e3-baa7-0800200c9a66", Context.getOrderService().getOrderFrequency(1)
		                .getUuid());
	}
	
	/**
	 * @verifies return the order frequency that matches the specified uuid
	 * @see OrderService#getOrderFrequencyByUuid(String)
	 */
	@Test
	public void getOrderFrequencyByUuid_shouldReturnTheOrderFrequencyThatMatchesTheSpecifiedUuid() throws Exception {
		assertEquals(1, Context.getOrderService().getOrderFrequencyByUuid("28090760-7c38-11e3-baa7-0800200c9a66")
		        .getOrderFrequencyId().intValue());
	}
	
	/**
	 * @verifies return only non retired order frequencies if includeRetired is set to false
	 * @see OrderService#getOrderFrequencies(boolean)
	 */
	@Test
	public void getOrderFrequencies_shouldReturnOnlyNonRetiredOrderFrequenciesIfIncludeRetiredIsSetToFalse()
	        throws Exception {
		List<OrderFrequency> orderFrequencies = Context.getOrderService().getOrderFrequencies(false);
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
		List<OrderFrequency> orderFrequencies = Context.getOrderService().getOrderFrequencies(true);
		assertEquals(3, orderFrequencies.size());
		assertTrue(containsId(orderFrequencies, 1));
		assertTrue(containsId(orderFrequencies, 2));
		assertTrue(containsId(orderFrequencies, 3));
	}
	
	/**
	 * @verifies return all active orders for the specified patient
	 * @see OrderService#getActiveOrders(org.openmrs.Patient, Class, org.openmrs.CareSetting,
	 *      java.util.Date)
	 */
	@Test
	public void getActiveOrders_shouldReturnAllActiveOrdersForTheSpecifiedPatient() throws Exception {
		Patient patient = Context.getPatientService().getPatient(2);
		List<Order> orders = orderService.getActiveOrders(patient, Order.class, null, null);
		assertEquals(5, orders.size());
		Order[] expectedOrders = { orderService.getOrder(222), orderService.getOrder(3), orderService.getOrder(444),
		        orderService.getOrder(5), orderService.getOrder(7) };
		assertThat(orders, hasItems(expectedOrders));
		
		assertTrue(OrderUtil.isOrderActive(orders.get(0), null));
		assertTrue(OrderUtil.isOrderActive(orders.get(1), null));
		assertTrue(OrderUtil.isOrderActive(orders.get(2), null));
		assertTrue(OrderUtil.isOrderActive(orders.get(3), null));
		assertTrue(OrderUtil.isOrderActive(orders.get(4), null));
	}
	
	/**
	 * @verifies return all active orders for the specified patient and care setting
	 * @see OrderService#getActiveOrders(org.openmrs.Patient, Class, org.openmrs.CareSetting,
	 *      java.util.Date)
	 */
	@Test
	public void getActiveOrders_shouldReturnAllActiveOrdersForTheSpecifiedPatientAndCareSetting() throws Exception {
		Patient patient = patientService.getPatient(2);
		CareSetting careSetting = orderService.getCareSetting(1);
		List<Order> orders = orderService.getActiveOrders(patient, Order.class, careSetting, null);
		assertEquals(4, orders.size());
		Order[] expectedOrders = { orderService.getOrder(3), orderService.getOrder(444), orderService.getOrder(5),
		        orderService.getOrder(7) };
		assertThat(orders, hasItems(expectedOrders));
	}
	
	/**
	 * @verifies return all active drug orders for the specified patient
	 * @see OrderService#getActiveOrders(org.openmrs.Patient, Class, org.openmrs.CareSetting,
	 *      java.util.Date)
	 */
	@Test
	public void getActiveOrders_shouldReturnAllActiveDrugOrdersForTheSpecifiedPatient() throws Exception {
		Patient patient = patientService.getPatient(2);
		List<DrugOrder> orders = orderService.getActiveOrders(patient, DrugOrder.class, null, null);
		assertEquals(2, orders.size());
		DrugOrder[] expectedOrders = { (DrugOrder) orderService.getOrder(3), (DrugOrder) orderService.getOrder(5) };
		assertThat(orders, hasItems(expectedOrders));
	}
	
	/**
	 * @verifies return all active test orders for the specified patient
	 * @see OrderService#getActiveOrders(org.openmrs.Patient, Class, org.openmrs.CareSetting,
	 *      java.util.Date)
	 */
	@Test
	public void getActiveOrders_shouldReturnAllActiveTestOrdersForTheSpecifiedPatient() throws Exception {
		Patient patient = patientService.getPatient(2);
		List<TestOrder> orders = orderService.getActiveOrders(patient, TestOrder.class, null, null);
		assertEquals(1, orders.size());
		assertEquals(orders.get(0), orderService.getOrder(7));
	}
	
	/**
	 * @verifies fail if patient is null
	 * @see OrderService#getActiveOrders(org.openmrs.Patient, Class, org.openmrs.CareSetting,
	 *      java.util.Date)
	 */
	@Test(expected = IllegalArgumentException.class)
	public void getActiveOrders_shouldFailIfPatientIsNull() throws Exception {
		orderService.getActiveOrders(null, Order.class, orderService.getCareSetting(1), null);
	}
	
	/**
	 * @verifies return active orders as of the specified date
	 * @see OrderService#getActiveOrders(org.openmrs.Patient, Class, org.openmrs.CareSetting,
	 *      java.util.Date)
	 */
	@Test
	public void getActiveOrders_shouldReturnActiveOrdersAsOfTheSpecifiedDate() throws Exception {
		Patient patient = Context.getPatientService().getPatient(2);
		Date asOfDate = Context.getDateTimeFormat().parse("02/12/2007 23:59:59");
		List<Order> orders = orderService.getActiveOrders(patient, Order.class, null, asOfDate);
		assertEquals(0, orders.size());
		
		asOfDate = Context.getDateFormat().parse("03/12/2007");
		orders = orderService.getActiveOrders(patient, Order.class, null, asOfDate);
		assertEquals(1, orders.size());
		assertEquals(orderService.getOrder(2), orders.get(0));
		
		asOfDate = Context.getDateFormat().parse("10/12/2007");
		orders = orderService.getActiveOrders(patient, Order.class, null, asOfDate);
		assertEquals(1, orders.size());
		assertEquals(orderService.getOrder(2), orders.get(0));
		
		asOfDate = Context.getDateTimeFormat().parse("10/12/2007 00:01:00");
		orders = orderService.getActiveOrders(patient, Order.class, null, asOfDate);
		assertEquals(0, orders.size());
		
		asOfDate = Context.getDateFormat().parse("09/04/2008");
		orders = orderService.getActiveOrders(patient, Order.class, null, asOfDate);
		assertEquals(3, orders.size());
		Order[] expectedOrders = { orderService.getOrder(222), orderService.getOrder(3), orderService.getOrder(4) };
		assertThat(orders, hasItems(expectedOrders));
		
		asOfDate = Context.getDateFormat().parse("25/09/2008");
		orders = orderService.getActiveOrders(patient, Order.class, null, asOfDate);
		assertEquals(5, orders.size());
		Order[] expectedOrders1 = { orderService.getOrder(222), orderService.getOrder(3), orderService.getOrder(444),
		        orderService.getOrder(5), orderService.getOrder(6) };
		assertThat(orders, hasItems(expectedOrders1));
		
		asOfDate = Context.getDateTimeFormat().parse("26/09/2008 10:24:10");
		orders = orderService.getActiveOrders(patient, Order.class, null, asOfDate);
		assertEquals(4, orders.size());
		Order[] expectedOrders2 = { orderService.getOrder(222), orderService.getOrder(3), orderService.getOrder(444),
		        orderService.getOrder(5) };
		assertThat(orders, hasItems(expectedOrders2));
		
		asOfDate = Context.getDateFormat().parse("20/11/2008");
		orders = orderService.getActiveOrders(patient, Order.class, null, asOfDate);
		assertEquals(5, orders.size());
		Order[] expectedOrders3 = { orderService.getOrder(222), orderService.getOrder(3), orderService.getOrder(444),
		        orderService.getOrder(5), orderService.getOrder(7) };
		assertThat(orders, hasItems(expectedOrders3));
		
		asOfDate = Context.getDateFormat().parse("02/12/2008");
		orders = orderService.getActiveOrders(patient, Order.class, null, asOfDate);
		assertEquals(6, orders.size());
		Order[] expectedOrders4 = { orderService.getOrder(222), orderService.getOrder(3), orderService.getOrder(444),
		        orderService.getOrder(5), orderService.getOrder(7), orderService.getOrder(9) };
		assertThat(orders, hasItems(expectedOrders4));
		
		asOfDate = Context.getDateFormat().parse("04/12/2008");
		orders = orderService.getActiveOrders(patient, Order.class, null, asOfDate);
		assertEquals(5, orders.size());
		Order[] expectedOrders5 = { orderService.getOrder(222), orderService.getOrder(3), orderService.getOrder(444),
		        orderService.getOrder(5), orderService.getOrder(7) };
		assertThat(orders, hasItems(expectedOrders5));
	}
	
	/**
	 * @verifies default to Order class if no orderClass is specified
	 * @see OrderService#getActiveOrders(org.openmrs.Patient, Class, org.openmrs.CareSetting,
	 *      java.util.Date)
	 */
	@Test
	public void getActiveOrders_shouldDefaultToOrderClassIfNoOrderClassIsSpecified() throws Exception {
		Patient patient = Context.getPatientService().getPatient(2);
		List<Order> orders = orderService.getActiveOrders(patient, null, null, null);
		assertEquals(5, orders.size());
		Order[] expectedOrders = { orderService.getOrder(222), orderService.getOrder(3), orderService.getOrder(444),
		        orderService.getOrder(5), orderService.getOrder(7) };
		assertThat(orders, hasItems(expectedOrders));
	}
	
	/**
	 * @see {@link OrderService#discontinueOrder(org.openmrs.Order, String, java.util.Date)}
	 */
	@Test
	@Verifies(value = "populate correct attributes on the discontinue and discontinued orders", method = "discontinueOrder(Order, String, Date)")
	public void discontinueOrderWithNonCodedReason_shouldPopulateCorrectAttributesOnBothOrders() throws Exception {
		Order order = orderService.getOrderByOrderNumber("111");
		assertTrue(OrderUtil.isOrderActive(order, null));
		Date discontinueDate = new Date();
		String discontinueReasonNonCoded = "Test if I can discontinue this";
		
		Order discontinueOrder = orderService.discontinueOrder(order, discontinueReasonNonCoded, discontinueDate);
		
		Assert.assertEquals(order.getDateStopped(), discontinueDate);
		Assert.assertNotNull(discontinueOrder);
		Assert.assertNotNull(discontinueOrder.getId());
		Assert.assertEquals(discontinueOrder.getAction(), Action.DISCONTINUE);
		Assert.assertEquals(discontinueOrder.getOrderReasonNonCoded(), discontinueReasonNonCoded);
		Assert.assertEquals(discontinueOrder.getPreviousOrder(), order);
	}
	
	/**
	 * @see {@link OrderService#discontinueOrder(org.openmrs.Order, org.openmrs.Concept, java.util.Date)}
	 */
	@Test
	@Verifies(value = "populate correct attributes on the discontinue and discontinued orders", method = "discontinueOrder(Order, Concept, Date)")
	public void discontinueOrderWithConcept_shouldPopulateCorrectAttributesOnBothOrders() throws Exception {
		executeDataSet("org/openmrs/api/include/OrderServiceTest-discontinueReason.xml");
		
		Order order = orderService.getOrderByOrderNumber("111");
		Date discontinueDate = new Date();
		Concept concept = Context.getConceptService().getConcept(1);
		
		Order discontinueOrder = orderService.discontinueOrder(order, concept, discontinueDate);
		
		Assert.assertEquals(order.getDateStopped(), discontinueDate);
		Assert.assertNotNull(discontinueOrder);
		Assert.assertNotNull(discontinueOrder.getId());
		Assert.assertEquals(discontinueOrder.getAction(), Action.DISCONTINUE);
		Assert.assertEquals(discontinueOrder.getOrderReason(), concept);
		Assert.assertEquals(discontinueOrder.getPreviousOrder(), order);
	}
	
	/**
	 * @see {@link OrderService#discontinueOrder(org.openmrs.Order, String, java.util.Date)}
	 */
	@Test(expected = APIException.class)
	@Verifies(value = "fail for a discontinue order", method = "discontinueOrder(Order, String, Date)")
	public void discontinueOrderWithNonCodedReason_shouldFailForADiscontinueOrder() throws Exception {
		executeDataSet("org/openmrs/api/include/OrderServiceTest-discontinuedOrder.xml");
		OrderService orderService = Context.getOrderService();
		Order discontinueOrder = orderService.getOrder(26);
		
		orderService.discontinueOrder(discontinueOrder, "Test if I can discontinue this", null);
	}
	
	/**
	 * @see {@link OrderService#discontinueOrder(org.openmrs.Order, org.openmrs.Concept, java.util.Date)}
	 */
	@Test(expected = APIException.class)
	@Verifies(value = "fail for a discontinue order", method = "discontinueOrder(Order, Concept, Date)")
	public void discontinueOrderWithConcept_shouldFailForADiscontinueOrder() throws Exception {
		executeDataSet("org/openmrs/api/include/OrderServiceTest-discontinuedOrder.xml");
		executeDataSet("org/openmrs/api/include/OrderServiceTest-discontinueReason.xml");
		OrderService orderService = Context.getOrderService();
		Order discontinueOrder = orderService.getOrder(26);
		
		orderService.discontinueOrder(discontinueOrder, (Concept) null, null);
	}
	
	/**
	 * @see {@link OrderService#saveOrder(org.openmrs.Order)}
	 */
	@Test
	@Verifies(value = "discontinue existing active order if new order being saved with action to discontinue", method = "saveOrder(Order)")
	public void saveOrder_shouldDiscontinueExistingActiveOrderIfNewOrderBeingSavedWithActionToDiscontinue() throws Exception {
		Order order = new Order();
		order.setAction(Order.Action.DISCONTINUE);
		order.setOrderReasonNonCoded("Discontinue this");
		order.setPatient(Context.getPatientService().getPatient(7));
		order.setConcept(Context.getConceptService().getConcept(88));
		order.setCareSetting(orderService.getCareSetting(1));
		order.setStartDate(new Date());
		
		//We are trying to discontinue order id 111 in standardTestDataset.xml
		Order expectedPreviousOrder = orderService.getOrder(111);
		Assert.assertNull(expectedPreviousOrder.getDateStopped());
		
		order = orderService.saveOrder(order);
		
		Assert.assertNotNull("should populate dateStopped in previous order", expectedPreviousOrder.getDateStopped());
		Assert.assertNotNull("should save discontinue order", order.getId());
		Assert.assertEquals(expectedPreviousOrder, order.getPreviousOrder());
		Assert.assertNotNull(expectedPreviousOrder.getDateStopped());
	}
	
	/**
	 * @see {@link OrderService#saveOrder(org.openmrs.Order)}
	 */
	@Test
	@Verifies(value = "discontinue previousOrder if it is not already discontinued", method = "saveOrder(Order)")
	public void saveOrder_shouldDiscontinuePreviousOrderIfItIsNotAlreadyDiscontinued() throws Exception {
		//We are trying to discontinue order id 111 in standardTestDataset.xml
		Order order = new Order();
		order.setAction(Order.Action.DISCONTINUE);
		order.setOrderReasonNonCoded("Discontinue this");
		order.setPatient(Context.getPatientService().getPatient(7));
		order.setConcept(Context.getConceptService().getConcept(88));
		order.setCareSetting(orderService.getCareSetting(1));
		order.setStartDate(new Date());
		Order previousOrder = orderService.getOrder(111);
		assertTrue(OrderUtil.isOrderActive(previousOrder, null));
		order.setPreviousOrder(previousOrder);
		
		orderService.saveOrder(order);
		
		Assert.assertNotNull("previous order should be discontinued", previousOrder.getDateStopped());
	}
	
	/**
	 * @see {@link OrderService#saveOrder(org.openmrs.Order)}
	 */
	@Test(expected = APIException.class)
	@Verifies(value = "fail if concept in previous order does not match this concept", method = "saveOrder(Order)")
	public void saveOrder_shouldFailIfConceptInPreviousOrderDoesNotMatchThisConcept() throws Exception {
		OrderService orderService = Context.getOrderService();
		//We are trying to discontinue order id 111 in standardTestDataset.xml
		Order order = new Order();
		order.setAction(Order.Action.DISCONTINUE);
		order.setOrderReasonNonCoded("Discontinue this");
		order.setPatient(Context.getPatientService().getPatient(7));
		order.setConcept(Context.getConceptService().getConcept(3));
		order.setCareSetting(orderService.getCareSetting(1));
		order.setStartDate(new Date());
		Order previousOrder = orderService.getOrder(111);
		order.setPreviousOrder(previousOrder);
		
		orderService.saveOrder(order);
	}
	
	/**
	 * @verifies reject a future discontinueDate
	 * @see OrderService#discontinueOrder(org.openmrs.Order, org.openmrs.Concept, java.util.Date)
	 */
	@Test(expected = IllegalArgumentException.class)
	public void discontinueOrder_shouldRejectAFutureDiscontinueDate() throws Exception {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.HOUR_OF_DAY, 1);
		Patient patient = Context.getPatientService().getPatient(2);
		CareSetting careSetting = orderService.getCareSetting(1);
		Order orderToDiscontinue = orderService.getActiveOrders(patient, Order.class, careSetting, null).get(0);
		orderService.discontinueOrder(orderToDiscontinue, new Concept(), cal.getTime());
	}
	
	/**
	 * @verifies fail if discontinueDate is in the future
	 * @see OrderService#discontinueOrder(org.openmrs.Order, String, java.util.Date)
	 */
	@Test(expected = IllegalArgumentException.class)
	public void discontinueOrder_shouldFailIfDiscontinueDateIsInTheFuture() throws Exception {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.HOUR_OF_DAY, 1);
		Order orderToDiscontinue = orderService.getActiveOrders(Context.getPatientService().getPatient(2), Order.class,
		    orderService.getCareSetting(1), null).get(0);
		orderService.discontinueOrder(orderToDiscontinue, "Testing", cal.getTime());
	}
	
	/**
	 * @verifies pass if the existing drug order matches the concept and drug of the DC order
	 * @see OrderService#saveOrder(org.openmrs.Order)
	 */
	@Test
	public void saveOrder_shouldPassIfTheExistingDrugOrderMatchesTheConceptAndDrugOfTheDCOrder() throws Exception {
		final DrugOrder orderToDiscontinue = (DrugOrder) orderService.getOrder(5);
		assertTrue(OrderUtil.isOrderActive(orderToDiscontinue, null));
		
		DrugOrder order = new DrugOrder();
		order.setDrug(orderToDiscontinue.getDrug());
		order.setAction(Order.Action.DISCONTINUE);
		order.setOrderReasonNonCoded("Discontinue this");
		order.setPatient(orderToDiscontinue.getPatient());
		order.setConcept(orderToDiscontinue.getConcept());
		order.setCareSetting(orderToDiscontinue.getCareSetting());
		order.setStartDate(new Date());
		
		orderService.saveOrder(order);
		
		Assert.assertNotNull("previous order should be discontinued", orderToDiscontinue.getDateStopped());
	}
	
	/**
	 * @verifies fail if the existing drug order matches the concept and not drug of the DC order
	 * @see OrderService#saveOrder(org.openmrs.Order)
	 */
	@Test(expected = APIException.class)
	public void saveOrder_shouldFailIfTheExistingDrugOrderMatchesTheConceptAndNotDrugOfTheDCOrder() throws Exception {
		final DrugOrder orderToDiscontinue = (DrugOrder) orderService.getOrder(5);
		assertTrue(OrderUtil.isOrderActive(orderToDiscontinue, null));
		
		//create a different test drug
		Drug discontinuationOrderDrug = new Drug();
		discontinuationOrderDrug.setConcept(orderToDiscontinue.getConcept());
		discontinuationOrderDrug = Context.getConceptService().saveDrug(discontinuationOrderDrug);
		assertNotEquals(discontinuationOrderDrug, orderToDiscontinue.getDrug());
		assertNotNull(orderToDiscontinue.getDrug());
		
		DrugOrder order = new DrugOrder();
		order.setDrug(discontinuationOrderDrug);
		order.setAction(Order.Action.DISCONTINUE);
		order.setOrderReasonNonCoded("Discontinue this");
		order.setPatient(orderToDiscontinue.getPatient());
		order.setConcept(orderToDiscontinue.getConcept());
		order.setCareSetting(orderToDiscontinue.getCareSetting());
		order.setStartDate(new Date());
		
		orderService.saveOrder(order);
		
		Assert.assertNotNull("previous order should be discontinued", orderToDiscontinue.getDateStopped());
	}
	
	/**
	 * @verifies fail for a stopped order
	 * @see OrderService#discontinueOrder(org.openmrs.Order, org.openmrs.Concept, java.util.Date)
	 */
	@Test(expected = APIException.class)
	public void discontinueOrder_shouldFailForAStoppedOrder() throws Exception {
		Order orderToDiscontinue = orderService.getOrder(1);
		assertNotNull(orderToDiscontinue.getDateStopped());
		orderService.discontinueOrder(orderToDiscontinue, Context.getConceptService().getConcept(1), null);
	}
	
	/**
	 * @verifies fail for a voided order
	 * @see OrderService#discontinueOrder(org.openmrs.Order, String, java.util.Date)
	 */
	@Test(expected = APIException.class)
	public void discontinueOrder_shouldFailForAVoidedOrder() throws Exception {
		Order orderToDiscontinue = orderService.getOrder(8);
		assertTrue(orderToDiscontinue.isVoided());
		orderService.discontinueOrder(orderToDiscontinue, "testing", null);
	}
	
	/**
	 * @verifies fail for an expired order
	 * @see OrderService#discontinueOrder(org.openmrs.Order, org.openmrs.Concept, java.util.Date)
	 */
	@Test(expected = APIException.class)
	public void discontinueOrder_shouldFailForAnExpiredOrder() throws Exception {
		Order orderToDiscontinue = orderService.getOrder(6);
		assertNotNull(orderToDiscontinue.getAutoExpireDate());
		assertTrue(orderToDiscontinue.getAutoExpireDate().before(new Date()));
		orderService.discontinueOrder(orderToDiscontinue, Context.getConceptService().getConcept(1), null);
	}
	
	/**
	 * @verifies not allow editing an existing order
	 * @see OrderService#saveOrder(org.openmrs.Order)
	 */
	@Test(expected = APIException.class)
	public void saveOrder_shouldNotAllowEditingAnExistingOrder() throws Exception {
		final DrugOrder order = (DrugOrder) orderService.getOrder(5);
		orderService.saveOrder(order);
	}
	
	/**
	 * @verifies return the care setting with the specified uuid
	 * @see OrderService#getCareSettingByUuid(String)
	 */
	@Test
	public void getCareSettingByUuid_shouldReturnTheCareSettingWithTheSpecifiedUuid() throws Exception {
		CareSetting cs = orderService.getCareSettingByUuid("2ed1e57d-9f18-41d3-b067-2eeaf4b30fb1");
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
	 * @see OrderService#saveRevisedOrder(org.openmrs.Order) (org.openmrs.Order)
	 */
	@Test
	public void saveRevisedOrder_shouldReviseDrugOrder() throws Exception {
		executeDataSet("org/openmrs/api/include/OrderServiceTest-reviseActiveOrders.xml");
		Order order = orderService.getOrder(16);
		
		DrugOrder reviseOrder = (DrugOrder) order.cloneForRevision();
		reviseOrder.setDose((double) 200);
		reviseOrder.setDoseUnits(conceptService.getConcept(51));
		reviseOrder.setFrequency(orderService.getOrderFrequency(2));
		reviseOrder.setAsNeeded(false);
		reviseOrder.setDosingType(DrugOrder.DosingType.FREE_TEXT);
		reviseOrder.setDateCreated(new Date());
		reviseOrder.setDuration((double) 10);
		reviseOrder.setAutoExpireDate(dateAfterDays(10));
		
		Integer revisedOrderId = orderService.saveRevisedOrder(reviseOrder).getOrderId();
		DrugOrder savedRevisedOrder = (DrugOrder) orderService.getOrder(revisedOrderId);
		
		assertEquals(new Integer(16), savedRevisedOrder.getPreviousOrder().getOrderId());
		assertEquals(new Date().toString(), savedRevisedOrder.getPreviousOrder().getDateStopped().toString());
		assertEquals(Action.REVISE, savedRevisedOrder.getAction());
		assertEquals(new Double(200), savedRevisedOrder.getDose());
		assertEquals(new Integer(51), savedRevisedOrder.getDoseUnits().getConceptId());
		assertFalse(savedRevisedOrder.getAsNeeded());
		assertEquals(DrugOrder.DosingType.FREE_TEXT, savedRevisedOrder.getDosingType());
		
		assertNotEquals(order.getOrderNumber(), savedRevisedOrder.getOrderNumber());
	}
	
	@Rule
	public ExpectedException expectedEx = ExpectedException.none();
	
	/**
	 * @verifies not allow revisions for an inactive order
	 * @see OrderService#saveRevisedOrder(org.openmrs.Order)
	 */
	@Test
	public void saveRevisedOrder_shouldNotReviseAnInActiveOrder() throws Exception {
		executeDataSet("org/openmrs/api/include/OrderServiceTest-reviseInActiveOrders.xml");
		DrugOrder order = (DrugOrder) orderService.getOrder(6);
		
		DrugOrder orderWithRevision = (DrugOrder) order.cloneForRevision();
		orderWithRevision.setDose((double) 200);
		orderWithRevision.setDoseUnits(conceptService.getConcept(51));
		orderWithRevision.setFrequency(orderService.getOrderFrequency(2));
		orderWithRevision.setAsNeeded(false);
		orderWithRevision.setDosingType(DrugOrder.DosingType.FREE_TEXT);
		
		orderWithRevision.setDateCreated(new Date());
		orderWithRevision.setDuration((double) 10);
		orderWithRevision.setAutoExpireDate(dateAfterDays(10));
		
		expectedEx.expect(APIException.class);
		expectedEx.expectMessage("Cannot revise an inactive order.");
		orderService.saveRevisedOrder(orderWithRevision);
	}
	
	/**
	 * @verifies not allow revisions for an order with no previous order
	 * @see OrderService#saveRevisedOrder(org.openmrs.Order)
	 */
	@Test
	public void saveRevisedOrder_shouldNotReviseOrderWithNoPreviousOrder() throws Exception {
		executeDataSet("org/openmrs/api/include/OrderServiceTest-reviseActiveOrders.xml");
		
		DrugOrder order = new DrugOrder();
		order.setAction(Action.REVISE);
		order.setDose((double) 200);
		order.setDateCreated(new Date());
		order.setDuration((double) 10);
		order.setAutoExpireDate(dateAfterDays(10));
		order.setConcept(conceptService.getConcept(88));
		order.setPatient(patientService.getPatient(2));
		
		expectedEx.expect(APIException.class);
		expectedEx.expectMessage("Previous order cannot be null");
		orderService.saveRevisedOrder(order);
	}
	
	/**
	 * @verifies not allow revisions for a discontinued order
	 * @see OrderService#saveRevisedOrder(org.openmrs.Order)
	 */
	@Test
	public void saveRevisedOrder_shouldNotReviseAPreviouslyDiscontinuedOrder() throws Exception {
		executeDataSet("org/openmrs/api/include/OrderServiceTest-reviseDiscontinuedOrders.xml");
		
		Order previousOrder = orderService.getOrder(16);
		
		DrugOrder order = (DrugOrder) previousOrder.cloneForRevision();
		order.setDose((double) 200);
		order.setDateCreated(new Date());
		order.setAutoExpireDate(dateAfterDays(10));
		order.setPreviousOrder(previousOrder);
		
		expectedEx.expect(APIException.class);
		expectedEx.expectMessage("Cannot revise a discontinued order");
		orderService.saveRevisedOrder(order);
	}
	
	/**
	 * @verifies not allow revisions only for a REVISE Order
	 * @see OrderService#saveRevisedOrder(org.openmrs.Order)
	 */
	@Test
	public void saveRevisedOrder_shouldReviseOnlyWhenActionIsRevise() throws Exception {
		executeDataSet("org/openmrs/api/include/OrderServiceTest-reviseActiveOrders.xml");
		Order order = orderService.getOrder(16);
		
		DrugOrder revisionOrder = (DrugOrder) order.cloneForRevision();
		revisionOrder.setAction(Action.DISCONTINUE);
		
		expectedEx.expect(APIException.class);
		expectedEx.expectMessage("Action has to be 'REVISE'");
		orderService.saveRevisedOrder(revisionOrder).getOrderId();
	}
	
	private Date dateAfterDays(int days) {
		Calendar c = Calendar.getInstance();
		c.setTime(new Date());
		c.add(Calendar.DATE, days);
		return c.getTime();
	}
	
}
