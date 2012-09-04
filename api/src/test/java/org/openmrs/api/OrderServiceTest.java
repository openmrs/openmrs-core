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

import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Concept;
import org.openmrs.Drug;
import org.openmrs.DrugOrder;
import org.openmrs.Encounter;
import org.openmrs.GenericDrug;
import org.openmrs.Order;
import org.openmrs.Order.OrderAction;
import org.openmrs.Orderable;
import org.openmrs.Patient;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.test.Verifies;
import org.openmrs.util.OpenmrsUtil;

import static java.util.Collections.synchronizedSet;

/**
 * TODO clean up and test all methods in OrderService
 */
public class OrderServiceTest extends BaseContextSensitiveTest {
	
	private static final String simpleOrderEntryDatasetFilename = "org/openmrs/api/include/OrderServiceTest-simpleOrderEntryTestDataset.xml";
	
	protected static final String DRUG_ORDERS_DATASET_XML = "org/openmrs/api/include/OrderServiceTest-drugOrdersList.xml";
	
	protected static final String ORDERS_DATASET_XML = "org/openmrs/api/include/OrderServiceTest-ordersList.xml";
	
	private OrderService service;
	
	@Before
	public void before() {
		this.service = Context.getOrderService();
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
	 * @see {@link OrderService#saveOrder(Order)}
	 */
	@Test
	@Verifies(value = "should save new version of an existing order", method = "saveOrder(Order)")
	public void saveOrder_shouldSaveNewVersionOfAnExistingOrder() throws Exception {
		OrderService orderService = Context.getOrderService();
		Order order = orderService.getOrder(3);
		Order newOrder = orderService.saveOrder(order);
		Assert.assertTrue(order.getOrderId() != newOrder.getOrderId());
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
	 * @see {@link OrderService#getOrder(Integer)}
	 */
	@Test
	@Verifies(value = "should find object given valid order id", method = "getOrder(Integer)")
	public void getOrder_shouldFindObjectGivenValidOrderId() throws Exception {
		Order order = Context.getOrderService().getOrder(1);
		Assert.assertNotNull(order);
	}
	
	/**
	 * @see {@link OrderService#getOrder(Integer)}
	 */
	@Test
	@Verifies(value = "should return null if no object found with given order id", method = "getOrder(Integer)")
	public void getOrder_shouldReturnNullIfNoObjectFoundWithGivenOrderId() throws Exception {
		Assert.assertNull(Context.getOrderService().getOrder(999));
	}
	
	/**
	 * @see {@link OrderService#purgeOrder(Order)}
	 */
	@Test
	@Verifies(value = "should delete order", method = "purgeOrder(Order)")
	public void purgeOrder_shouldDeleteOrder() throws Exception {
		Order order = Context.getOrderService().getOrder(1);
		Assert.assertNotNull(order);
		Context.getOrderService().purgeOrder(order);
		order = Context.getOrderService().getOrder(1);
		Assert.assertNull(order);
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
		Assert.assertEquals(2, orders.size());
		for (Order order : orders)
			Assert.assertTrue(order.getOrderId() == 4 || order.getOrderId() == 5);
		
		//We should two different orders with this concept
		concept = Context.getConceptService().getConcept(792);
		orders = Context.getOrderService().getOrderHistoryByConcept(patient, concept);
		Assert.assertEquals(2, orders.size());
		for (Order order : orders)
			Assert.assertTrue(order.getOrderId() == 2 || order.getOrderId() == 3);
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
	 * @see {@link OrderService#getOrderables(String)}
	 */
	@Test
	@Verifies(value = "should get orderable concepts by name and drug class", method = "getOrderables(String)")
	public void getOrderables_shouldGetOrderableConceptsByNameAndDrugClass() throws Exception {
		executeDataSet(simpleOrderEntryDatasetFilename);
		
		String query = "Ampi";
		
		List<Orderable<?>> result = Context.getOrderService().getOrderables(query);
		
		Assert.assertNotNull(result);
		
		Assert.assertEquals(3, result.size());
		
		Boolean isExpected = result.get(0).getClass().equals(GenericDrug.class);
		Assert.assertTrue(isExpected);
		isExpected = result.get(1).getClass().equals(GenericDrug.class);
		Assert.assertTrue(isExpected);
		isExpected = result.get(2).getClass().equals(Drug.class);
		Assert.assertTrue(isExpected);
	}
	
	/**
	 * @see {@link OrderService#getOrderables(String)}
	 */
	@Test(expected = IllegalArgumentException.class)
	@Verifies(value = "should fail if null passed in", method = "getOrderables(String)")
	public void getOrderables_shouldFailIfNullPassedIn() throws Exception {
		executeDataSet(simpleOrderEntryDatasetFilename);
		
		String query = null;
		Context.getOrderService().getOrderables(query);
	}
	
	/**
	 * @see {@link OrderService#getOrderable(String)}
	 */
	@Test(expected = IllegalArgumentException.class)
	@Verifies(value = "should fail if null passed in", method = "getOrderable(String)")
	public void getOrderable_shouldFailIfNullPassedIn() throws Exception {
		Context.getOrderService().getOrderable(null);
	}
	
	/**
	 * @see {@link OrderService#getOrderable(String)}
	 */
	@Test
	@Verifies(value = "should fetch an orderable with given identifier", method = "getOrderable(String)")
	public void getOrderable_shouldFetchAnOrderableWithGivenIdentifier() throws Exception {
		Orderable orderable = Context.getOrderService().getOrderable("org.openmrs.GenericDrug:concept=3");
		Assert.assertNotNull(orderable);
		Assert.assertTrue(orderable.getClass().equals(GenericDrug.class));
		
		orderable = Context.getOrderService().getOrderable("org.openmrs.Drug:2");
		Assert.assertNotNull(orderable);
		Assert.assertTrue(orderable.getClass().equals(Drug.class));
	}
	
	/**
	 * @see OrderService#saveOrder(Order)
	 * @verifies not allow you to change the order number of a saved order
	 */
	@Test
	public void saveOrder_shouldNotAllowYouToChangeTheOrderNumberOfASavedOrder() throws Exception {
		Order existing = service.getOrder(1);
		existing.setOrderNumber("New Number");
		try {
			service.saveOrder(existing);
			Assert.fail("the previous line should have thrown an exception");
		}
		catch (APIException ex) {
			// test this way rather than @Test(expected...) so we can verify it's the right APIException
			Assert.assertTrue(ex.getMessage().contains("orderNumber"));
		}
	}
	
	/**
	 * @see {@link OrderService#discontinueOrder(Order,String,User,Date)}
	 */
	@Test
	@Verifies(value = "should discontinue and return the old order", method = "discontinueOrder(Order,String,User,Date)")
	public void discontinueOrder_shouldDiscontinueAndReturnTheOldOrder() throws Exception {
		int originalCount = service.getOrders(Order.class, null, null, null, null, null, null, null).size();
		Order order = service.getOrder(3);
		
		Assert.assertFalse(order.getDiscontinued());
		Assert.assertNull(order.getDiscontinuedDate());
		Assert.assertNull(order.getDiscontinuedBy());
		Assert.assertNull(order.getDiscontinuedReason());
		Order returnedOrder = service.discontinueOrder(order, "Testing");
		Assert.assertEquals(order, returnedOrder);
		Assert.assertTrue(order.getDiscontinued());
		Assert.assertEquals("Testing", returnedOrder.getDiscontinuedReason());
		Assert.assertNotNull(returnedOrder.getDiscontinuedDate());
		Assert.assertNotNull(returnedOrder.getDiscontinuedBy());
		//should have created a discontinue order
		Assert.assertEquals(originalCount + 1, service.getOrders(Order.class, null, null, null, null, null, null, null)
		        .size());
		//find the newly created order and make ensure that its action is DISCONTINUE
		Order discontinueOrder = null;
		for (Order o : service.getOrders(Order.class, null, null, null, null, null, null, null)) {
			if (OpenmrsUtil.nullSafeEquals(o.getPreviousOrderNumber(), order.getOrderNumber()))
				discontinueOrder = o;
		}
		
		Assert.assertNotNull(discontinueOrder);
		Assert.assertEquals(OrderAction.DISCONTINUE, discontinueOrder.getOrderAction());
	}
	
	/**
	 * @see {@link OrderService#discontinueOrder(Order,String,User,Date)}
	 */
	@Test(expected = APIException.class)
	@Verifies(value = "should fail if the discontinue date is after the auto expire date", method = "discontinueOrder(Order,String,User,Date)")
	public void discontinueOrder_shouldFailIfTheDiscontinueDateIsAfterTheAutoExpireDate() throws Exception {
		Order order = service.getOrder(12);
		Assert.assertNotNull(order.getAutoExpireDate());
		Calendar cal = Calendar.getInstance();
		//set the time to after auto expire date
		cal.setTime(order.getAutoExpireDate());
		cal.add(Calendar.MINUTE, 1);
		service.discontinueOrder(order, "Testing", null, cal.getTime());
	}
	
	/**
	 * @see {@link OrderService#discontinueOrder(Order,String,User,Date)}
	 */
	@Test(expected = APIException.class)
	@Verifies(value = "should fail if the order is already discontinued", method = "discontinueOrder(Order,String,User,Date)")
	public void discontinueOrder_shouldFailIfTheOrderIsAlreadyDiscontinued() throws Exception {
		Order order = service.getOrder(3);
		Assert.assertFalse(order.getDiscontinued());
		service.discontinueOrder(order, "Testing");
		Assert.assertTrue(order.getDiscontinued());
		//re discontinue
		service.discontinueOrder(order, "Testing2");
	}
	
	/**
	 * @see {@link OrderService#discontinueOrder(Order,String,User,Date)}
	 */
	@Test(expected = APIException.class)
	@Verifies(value = "should fail if the passed in discontinue date is in the future", method = "discontinueOrder(Order,String,User,Date)")
	public void discontinueOrder_shouldFailIfThePassedInDiscontinueDateIsInTheFuture() throws Exception {
		Order order = service.getOrder(3);
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MINUTE, 1);
		service.discontinueOrder(order, "Testing", null, cal.getTime());
	}
	
	/**
	 * @see OrderService#getNewOrderNumber()
	 * @verifies always return unique orderNumbers when called multiple times without saving orders
	 */
	@Test
	public void getNewOrderNumber_shouldAlwaysReturnUniqueOrderNumbersWhenCalledMultipleTimesWithoutSavingOrders()
	        throws Exception {
		final int numberOfConcurrentOrderNumberRequests = 16;
		final Set<String> uniqueOrderNumbers = synchronizedSet(new HashSet<String>());
		final CountDownLatch beginRequestingOrderNumbers = new CountDownLatch(1);
		final CountDownLatch newOrderNumbersObtained = new CountDownLatch(numberOfConcurrentOrderNumberRequests);
		Runnable orderNumberRequest = new Runnable() {
			
			@Override
			public void run() {
				try {
					beginRequestingOrderNumbers.await();
					Context.openSession();
					String orderNumber = service.getNewOrderNumber();
					uniqueOrderNumbers.add(orderNumber);
				}
				catch (InterruptedException e) {}
				finally {
					Context.closeSession();
					newOrderNumbersObtained.countDown();
				}
			}
		};
		for (int i = 0; i < numberOfConcurrentOrderNumberRequests; i++) {
			new Thread(orderNumberRequest).start();
		}
		
		beginRequestingOrderNumbers.countDown();
		newOrderNumbersObtained.await();
		
		Assert.assertEquals("Should receive a unique order number for each concurrent request",
		    numberOfConcurrentOrderNumberRequests, uniqueOrderNumbers.size());
	}
	
	/**
	 * @see OrderService#getDrugOrdersByPatientAndIngredient(Patient,Concept)
	 * @verifies return drug orders matched by patient and intermediate concept
	 */
	@Test
	public void getDrugOrdersByPatientAndIngredient_shouldReturnDrugOrdersMatchedByPatientAndIntermediateConcept()
	        throws Exception {
		OrderService orderService = Context.getOrderService();
		List<DrugOrder> drugOrders = orderService.getDrugOrdersByPatientAndIngredient(new Patient(2), new Concept(88));
		Assert.assertEquals(4, drugOrders.size());
	}
	
	/**
	 * @see OrderService#getDrugOrdersByPatientAndIngredient(Patient,Concept)
	 * @verifies return drug orders matched by patient and drug concept
	 */
	@Test
	public void getDrugOrdersByPatientAndIngredient_shouldReturnDrugOrdersMatchedByPatientAndDrugConcept() throws Exception {
		OrderService orderService = Context.getOrderService();
		List<DrugOrder> drugOrders = orderService.getDrugOrdersByPatientAndIngredient(new Patient(2), new Concept(792));
		Assert.assertEquals(2, drugOrders.size());
	}
	
	/**
	 * @see OrderService#getDrugOrdersByPatientAndIngredient(Patient,Concept)
	 * @verifies return empty list if no concept matched
	 */
	@Test
	public void getDrugOrdersByPatientAndIngredient_shouldReturnEmptyListIfNoConceptMatched() throws Exception {
		OrderService orderService = Context.getOrderService();
		List<DrugOrder> drugOrders = orderService.getDrugOrdersByPatientAndIngredient(new Patient(2), new Concept(80));
		Assert.assertEquals(0, drugOrders.size());
	}
	
	/**
	 * @see OrderService#getDrugOrdersByPatientAndIngredient(Patient,Concept)
	 * @verifies return empty list if no patient matched
	 */
	@Test
	public void getDrugOrdersByPatientAndIngredient_shouldReturnEmptyListIfNoPatientMatched() throws Exception {
		OrderService orderService = Context.getOrderService();
		List<DrugOrder> drugOrders = orderService.getDrugOrdersByPatientAndIngredient(new Patient(10), new Concept(88));
		Assert.assertEquals(0, drugOrders.size());
	}
	
	/**
	 * @see {@link OrderService#getOrdersByPatient(Patient, boolean)}
	 */
	@Test
	@Verifies(value = "return list of orders for patient with respect to the include voided flag", method = "getOrdersByPatient(Patient, boolean)")
	public void getOrdersByPatient_shouldReturnListOfOrdersForPatientWithRespectToTheIncludeVoidedFlag() throws Exception {
		executeDataSet(ORDERS_DATASET_XML);
		Patient p = Context.getPatientService().getPatient(2);
		List<Order> orders = Context.getOrderService().getOrdersByPatient(p, true);
		Assert.assertEquals(12, orders.size());
		
		orders = Context.getOrderService().getOrdersByPatient(p, false);
		Assert.assertEquals(8, orders.size());
	}
	
	/**
	 * @see {@link OrderService#getOrdersByPatient(Patient)}
	 */
	@Test
	@Verifies(value = "return list of non voided orders for patient", method = "getOrdersByPatient(Patient)")
	public void getOrdersByPatient_shouldReturnListOfNonVoidedOrdersForPatient() throws Exception {
		Patient p = Context.getPatientService().getPatient(2);
		List<Order> orders = Context.getOrderService().getOrdersByPatient(p);
		Assert.assertEquals(8, orders.size());
	}
	
	/**
	 * @see OrderService#voidOrder(Order,String)
	 */
	@Test(expected = IllegalArgumentException.class)
	@Verifies(value = "should fail if reason is empty", method = "voidOrder(Order,String)")
	public void voidOrder_shouldFailIfReasonIsEmpty() throws Exception {
		OrderService orderService = Context.getOrderService();
		
		Order order = orderService.getOrder(2);
		Assert.assertNotNull(order);
		
		String voidReason = "";
		orderService.voidOrder(order, "");
	}
	
	/**
	 * @see {@link OrderService#voidOrder(Order,String)}
	 */
	@Test(expected = IllegalArgumentException.class)
	@Verifies(value = "should fail if reason is null", method = "voidOrder(Order,String)")
	public void voidOrder_shouldFailIfReasonIsNull() throws Exception {
		OrderService orderService = Context.getOrderService();
		
		Order order = orderService.getOrder(2);
		Assert.assertNotNull(order);
		
		String voidReason = null;
		orderService.voidOrder(order, voidReason);
	}
	
	/**
	 * @see {@link OrderService#voidOrder(Order,String)}
	 */
	@Test
	@Verifies(value = "should void given order", method = "voidOrder(Order,String)")
	public void voidOrder_shouldVoidGivenOrder() throws Exception {
		OrderService orderService = Context.getOrderService();
		
		Order order = orderService.getOrder(2);
		Assert.assertNotNull(order);
		
		String voidReason = "test reason";
		orderService.voidOrder(order, voidReason);
		
		// assert that order is voided and void reason is set
		Assert.assertTrue(order.isVoided());
		Assert.assertEquals(voidReason, order.getVoidReason());
	}
	
	/**
	 * @see {@link OrderService#voidOrder(Order,String)}
	 */
	@Test
	@Verifies(value = "should not change an already voided order", method = "voidOrder(Order,String)")
	public void voidOrder_shouldNotChangeAnAlreadyVoidedOrder() throws Exception {
		executeDataSet(DRUG_ORDERS_DATASET_XML);
		OrderService orderService = Context.getOrderService();
		
		Order order = orderService.getOrder(8);
		Assert.assertNotNull(order);
		// assert that order has been already voided
		Assert.assertTrue(order.isVoided());
		String expectedVoidReason = order.getVoidReason();
		
		String voidReason = "test reason";
		orderService.voidOrder(order, voidReason);
		
		// assert that voiding does not make an affect
		Assert.assertTrue(order.isVoided());
		Assert.assertEquals(expectedVoidReason, order.getVoidReason());
	}
	
	/**
	 * @see {@link OrderService#unvoidOrder(Order)}
	 */
	@Test
	@Verifies(value = "should unvoid given order", method = "voidOrder(Order)")
	public void unvoidOrder_shouldUnvoidGivenOrder() throws Exception {
		executeDataSet(DRUG_ORDERS_DATASET_XML);
		OrderService orderService = Context.getOrderService();
		
		Order order = orderService.getOrder(8);
		Assert.assertNotNull(order);
		// assert that order has been already voided
		Assert.assertTrue(order.isVoided());
		
		orderService.unvoidOrder(order);
		
		Assert.assertFalse(order.isVoided());
	}
	
	/**
	 * @see {@link OrderService#getOrdersByEncounter(Encounter)}
	 */
	@Test
	@Verifies(value = "return list of non voided orders by encounter", method = "getOrdersByEncounter(Encounter)")
	public void getOrdersByEncounter_shouldReturnListOfNonVoidedOrdersByEncounter() throws Exception {
		executeDataSet(ORDERS_DATASET_XML);
		Encounter encounter = new Encounter(3);
		List<Order> orders = Context.getOrderService().getOrdersByEncounter(encounter);
		Assert.assertEquals(3, orders.size());
	}
	
	/**
	 * @see {@link OrderService#getOrdersByOrderer(User)}
	 */
	@Test
	@Verifies(value = "return list of non voided orders by orderer", method = "getOrdersByOrderer(User)")
	public void getOrdersByOrderer_shouldReturnListOfNonVoidedOrdersByOrderer() throws Exception {
		User user = Context.getUserService().getUser(1);
		List<Order> orders = Context.getOrderService().getOrdersByOrderer(user);
		Assert.assertEquals(10, orders.size());
	}
	
	/**
	 * @see {@link OrderService#saveOrder(Order)}
	 */
	@Test
	@Verifies(value = "should asign order number for new order", method = "saveOrder(Order)")
	public void saveOrder_shouldAssignOrderNumberForNewOrder() throws Exception {
		Order order = new Order();
		order.setConcept(Context.getConceptService().getConcept(23));
		order.setPatient(Context.getPatientService().getPatient(6));
		order.setStartDate(new Date());
		service.saveOrder(order);
		Assert.assertNotNull(order.getOrderId());
		Assert.assertNotNull(order.getOrderNumber());
	}
}
