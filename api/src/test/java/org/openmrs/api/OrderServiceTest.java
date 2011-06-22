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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Concept;
import org.openmrs.Drug;
import org.openmrs.GenericDrug;
import org.openmrs.Order;
import org.openmrs.OrderType;
import org.openmrs.Orderable;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.test.Verifies;

/**
 * TODO clean up and test all methods in OrderService
 */
public class OrderServiceTest extends BaseContextSensitiveTest {
	
	private static final String simpleOrderEntryDatasetFilename = "org/openmrs/api/include/OrderServiceTest-simpleOrderEntryTestDataset.xml";
	
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
	
	/**
	 * @see {@link OrderService#getOrderTypeByUuid(String)}
	 */
	@Test
	@Verifies(value = "should find object given valid uuid", method = "getOrderTypeByUuid(String)")
	public void getOrderTypeByUuid_shouldFindObjectGivenValidUuid() throws Exception {
		String uuid = "84ce45a8-5e7c-48f7-a581-ca1d17d63a62";
		OrderType orderType = Context.getOrderService().getOrderTypeByUuid(uuid);
		Assert.assertEquals(1, (int) orderType.getOrderTypeId());
	}
	
	/**
	 * @see {@link OrderService#getOrderTypeByUuid(String)}
	 */
	@Test
	@Verifies(value = "should return null if no object found with given uuid", method = "getOrderTypeByUuid(String)")
	public void getOrderTypeByUuid_shouldReturnNullIfNoObjectFoundWithGivenUuid() throws Exception {
		Assert.assertNull(Context.getOrderService().getOrderTypeByUuid("some invalid uuid"));
	}
	
	/**
	 * @see {@link OrderService#saveOrder(Order)}
	 */
	@Test
	@Verifies(value = "when saving a discontinuedReasonNonCoded parameter the value is correctly stored to the database", method = "saveOrder(Order)")
	public void saveOrder_shouldSaveDiscontinuedReasonNonCoded() throws Exception {
		String uuid = "921de0a3-05c4-444a-be03-e01b4c4b9142";
		Order order = Context.getOrderService().getOrderByUuid(uuid);
		String discontinuedReasonNonCoded = "Non coded discontinued reason";
		
		order.setDiscontinuedReasonNonCoded(discontinuedReasonNonCoded);
		OrderService orderService = Context.getOrderService();
		orderService.saveOrder(order);
		
		order = Context.getOrderService().getOrderByUuid(uuid);
		
		Assert.assertEquals(discontinuedReasonNonCoded, order.getDiscontinuedReasonNonCoded());
	}
	
	/**
	 * @see {@link OrderService#getNewOrderNumber()}
	 */
	@Test
	@Verifies(value = "should return the next unused order id", method = "getNewOrderNumber()")
	public void getNewOrderNumber_shouldReturnTheNextUnusedOrderId() throws Exception {
		Assert.assertEquals("ORDER-11", Context.getOrderService().getNewOrderNumber());
	}
	
	/**
	 * @see {@link OrderService#getOrderHistoryByConcept(Concept)}
	 */
	@Test
	@Verifies(value = "should return orders with the given concept", method = "getOrderHistoryByConcept(Concept)")
	public void getOrderHistoryByConcept_shouldReturnOrdersWithTheGivenConcept() throws Exception {
		//We should have three orders with this concept.
		Concept concept = Context.getConceptService().getConcept(88);
		List<Order> orders = Context.getOrderService().getOrderHistoryByConcept(concept);
		Assert.assertEquals(3, orders.size());
		for (Order order : orders)
			Assert.assertTrue(order.getOrderId() == 1 || order.getOrderId() == 4 || order.getOrderId() == 5);
		
		//We should two orders with this concept.
		concept = Context.getConceptService().getConcept(792);
		orders = Context.getOrderService().getOrderHistoryByConcept(concept);
		Assert.assertEquals(2, orders.size());
		for (Order order : orders)
			Assert.assertTrue(order.getOrderId() == 2 || order.getOrderId() == 3);
	}
	
	/**
	 * @see {@link OrderService#getOrderHistoryByConcept(Concept)}
	 */
	@Test
	@Verifies(value = "should return empty list for concept without orders", method = "getOrderHistoryByConcept(Concept)")
	public void getOrderHistoryByConcept_shouldReturnEmptyListForConceptWithoutOrders() throws Exception {
		Concept concept = Context.getConceptService().getConcept(21);
		List<Order> orders = Context.getOrderService().getOrderHistoryByConcept(concept);
		Assert.assertEquals(0, orders.size());
	}
	
	/**
	 * @see {@link OrderService#discontinueOrder(Concept, Concept, Date)}
	 */
	@Test
	@Verifies(value = "should discontinue orders with the given concept", method = "discontinueOrder(Concept, Concept, Date)")
	public void discontinueOrder_shouldDiscontinueOrdersWithTheGivenConcept() throws Exception {
		Date discontinueDate = new Date();
		Concept discontinueReason = Context.getConceptService().getConcept(10);
		
		List<String> orderNumbers = new ArrayList<String>();
		
		Concept concept = Context.getConceptService().getConcept(23);
		List<Order> orders = Context.getOrderService().getOrderHistoryByConcept(concept);
		Assert.assertTrue(orders.size() == 2);
		for (Order order : orders) {
			orderNumbers.add(order.getOrderNumber());
			Assert.assertFalse(order.isDiscontinued(discontinueDate));
		}
		
		Context.getOrderService().discontinueOrderByConcept(concept, discontinueReason, discontinueDate);
		
		orders = Context.getOrderService().getOrderHistoryByConcept(concept);
		//Each discontinue creates a new order.
		Assert.assertTrue(orders.size() == 4);
		for (Order order : orders) {
			if (orderNumbers.contains(order.getOrderNumber())) {
				Assert.assertTrue(order.isDiscontinued(discontinueDate));
			}
		}
	}
	
	/**
	 * @see {@link OrderService#getOrderHistoryByOrderNumber(String)}
	 */
	@Test
	@Verifies(value = "should get orders with given number", method = "getOrderHistoryByOrderNumber(String)")
	public void getOrderHistoryByOrderNumber_shouldGetOrdersWithGivenNumber() throws Exception {
		List<Order> orders = Context.getOrderService().getOrderHistoryByOrderNumber("8");
		Assert.assertTrue(orders.size() == 3);
		for (Order order : orders) {
			Assert.assertTrue(order.getOrderId() == 8 || order.getOrderId() == 9 || order.getOrderId() == 10);
		}
	}
	
	/**
	 * @see {@link OrderService#getOrderHistoryByOrderNumber(String)}
	 */
	@Test
	@Verifies(value = "should get empty list for non existing order number", method = "getOrderHistoryByOrderNumber(String)")
	public void getOrderHistoryByOrderNumber_shouldGetEmptyListForNonExistingOrderNumber() throws Exception {
		List<Order> orders = Context.getOrderService().getOrderHistoryByOrderNumber("NON EXISTING");
		Assert.assertTrue(orders.size() == 0);
	}
	
	/**
	 * @see {@link OrderService#getOrderByOrderNumber(String)}
	 */
	@Test
	@Verifies(value = "should get latest order with given number", method = "getOrderByOrderNumber(String)")
	public void getOrderByOrderNumber_shouldGetLatestOrderWithGivenNumber() throws Exception {
		Order order = Context.getOrderService().getOrderByOrderNumber("8");
		Assert.assertTrue(order.getOrderId() == 10);
	}
	
	/**
	 * @see {@link OrderService#getOrderByOrderNumber(String)}
	 */
	@Test
	@Verifies(value = "should get null for non existing order number", method = "getOrderByOrderNumber(String)")
	public void getOrderByOrderNumber_shouldGetNullForNonExistingOrderNumber() throws Exception {
		Order order = Context.getOrderService().getOrderByOrderNumber("NON EXISTING");
		Assert.assertNull(order);
	}
	
	/**
	 * @see {@link OrderService#signOrder(Order, User)}
	 */
	@Test
	@Verifies(value = "should sign given order", method = "signOrder(Order, User)")
	public void signOrder_shouldSignGivenOrder() throws Exception {
		User provider = Context.getUserService().getUser(501);
		
		Order order = Context.getOrderService().getOrder(10);
		Assert.assertTrue(!order.isSigned());
		Assert.assertTrue(order.getDateSigned() == null);
		
		Context.getOrderService().signOrder(order, provider);
		
		order = Context.getOrderService().getOrder(10);
		Assert.assertTrue(order.isSigned());
		Assert.assertTrue(order.getDateSigned() != null);
	}
	
	/**
	 * @see {@link OrderService#signOrder(Order, User)}
	 */
	@Test
	@Verifies(value = "should activate given order", method = "activateOrder(Order, User)")
	public void activateOrder_shouldActivateGivenOrder() throws Exception {
		User provider = Context.getUserService().getUser(501);
		
		Order order = Context.getOrderService().getOrder(10);
		Assert.assertTrue(order.getActivatedBy() == null);
		Assert.assertTrue(order.getDateActivated() == null);
		
		Context.getOrderService().activateOrder(order, provider);
		
		order = Context.getOrderService().getOrder(10);
		Assert.assertTrue(order.getActivatedBy() != null);
		Assert.assertTrue(order.getDateActivated() != null);
	}
	
	/**
	 * @see {@link OrderService#fillOrder(Order, User)}
	 */
	@Test(expected = APIException.class)
	@Verifies(value = "should not fill order with user if not signed", method = "fillOrder(Order, User)")
	public void fillOrder_shouldNotFillOrderWithUserIfNotSigned() throws Exception {
		Order order = Context.getOrderService().getOrder(10);
		User provider = Context.getUserService().getUser(501);
		Context.getOrderService().fillOrder(order, provider);
	}
	
	/**
	 * @see {@link OrderService#fillOrder(Order, String)}
	 */
	@Test(expected = APIException.class)
	@Verifies(value = "should not fill order with non user if not signed", method = "fillOrder(Order, String)")
	public void fillOrder_shouldNotFillOrderWithNonUserIfNotSigned() throws Exception {
		Order order = Context.getOrderService().getOrder(10);
		Context.getOrderService().fillOrder(order, "url");
	}
	
	/**
	 * @see {@link OrderService#fillOrder(Order, User)}
	 */
	@Test
	@Verifies(value = "should fill order with user", method = "fillOrder(Order, User)")
	public void fillOrder_shouldFillOrderWithUser() throws Exception {
		User provider = Context.getUserService().getUser(501);
		
		Order order = Context.getOrderService().getOrder(10);
		Assert.assertTrue(order.getFiller() == null);
		Assert.assertTrue(order.getDateFilled() == null);
		
		Context.getOrderService().signOrder(order, provider);
		Context.getOrderService().fillOrder(order, provider);
		
		order = Context.getOrderService().getOrder(10);
		Assert.assertTrue(order.getFiller() != null);
		Assert.assertTrue(order.getDateFilled() != null);
	}
	
	/**
	 * @see {@link OrderService#fillOrder(Order, User)}
	 */
	@Test
	@Verifies(value = "should fill order with non user", method = "fillOrder(Order, User)")
	public void fillOrder_shouldFillGivenOrderWithNonUser() throws Exception {
		User provider = Context.getUserService().getUser(501);
		
		Order order = Context.getOrderService().getOrder(10);
		Assert.assertTrue(order.getFiller() == null);
		Assert.assertTrue(order.getDateFilled() == null);
		
		Context.getOrderService().signOrder(order, provider);
		Context.getOrderService().fillOrder(order, "url");
		
		order = Context.getOrderService().getOrder(10);
		Assert.assertTrue(order.getFiller() != null);
		Assert.assertTrue(order.getDateFilled() != null);
	}
	
	/**
	 * @see {@link OrderService#saveActivatedOrder(Order, User)}
	 */
	@Test(expected = APIException.class)
	@Verifies(value = "should not be called for existing order", method = "saveActivatedOrder(Order, User)")
	public void saveActivatedOrder_shouldNotBeCalledForExistingOrder() throws Exception {
		Order order = Context.getOrderService().getOrder(10);
		User provider = Context.getUserService().getUser(501);
		Context.getOrderService().signAndActivateOrder(order, provider);
	}
	
	/**
	 * @see {@link OrderService#saveActivatedOrder(Order, User)}
	 */
	@Test
	@Verifies(value = "should save sign activate order", method = "saveActivatedOrder(Order, User)")
	public void saveActivatedOrder_shouldSaveSignActivateOrder() throws Exception {
		User provider = Context.getUserService().getUser(501);
		
		Order order = new Order();
		order.setDateCreated(new Date());
		order.setOrderType(Context.getOrderService().getOrderType(2));
		order.setConcept(Context.getConceptService().getConcept(23));
		order.setPatient(Context.getPatientService().getPatient(6));
		
		Context.getOrderService().signAndActivateOrder(order, provider);
		
		order = Context.getOrderService().getOrder(order.getOrderId());
		
		//Should be saved.
		Assert.assertNotNull(order);
		
		//Should be signed.
		Assert.assertTrue(order.isSigned());
		Assert.assertTrue(order.getDateSigned() != null);
		
		//Should be activated.
		Assert.assertTrue(order.getActivatedBy() != null);
		Assert.assertTrue(order.getDateActivated() != null);
	}
	
	/**
	 * @see {@link OrderService#saveOrder(Order)}
	 */
	@Test
	@Verifies(value = "should asign order number for new order", method = "saveOrder(Order)")
	public void saveOrder_shouldAssignOrderNumberForNewOrder() throws Exception {
		Order order = new Order();
		order.setOrderType(Context.getOrderService().getOrderType(2));
		order.setConcept(Context.getConceptService().getConcept(23));
		order.setPatient(Context.getPatientService().getPatient(6));
		
		String nextAvaliableOrderNumber = Context.getOrderService().getNewOrderNumber();
		
		Context.getOrderService().saveOrder(order);
		
		Assert.assertNotNull(order.getOrderId());
		Assert.assertEquals(nextAvaliableOrderNumber, order.getOrderNumber());
		Assert.assertTrue(order.isLatestVersion());
		Assert.assertTrue(order.getOrderVersion() == 1);
	}
	
	/**
	 * @see {@link OrderService#saveOrder(Order)}
	 */
	@Test
	@Verifies(value = "should create new order for existing order", method = "saveOrder(Order)")
	public void saveOrder_shouldCreateNewOrderForExistingOrder() throws Exception {
		
		Order order = Context.getOrderService().getOrder(10);
		Context.getOrderService().saveOrder(order);
		
		Order newOrder = Context.getOrderService().getOrderByOrderNumber(order.getOrderNumber());
		
		Assert.assertNotNull(newOrder);
		Assert.assertNull(newOrder.getPreviousOrderNumber());
		Assert.assertTrue(newOrder.isLatestVersion());
		Assert.assertTrue(newOrder.getOrderVersion() == order.getOrderVersion() + 1);
	}
	
	/**
	 * @see {@link OrderService#saveOrder(Order)}
	 */
	@Test
	@Verifies(value = "should set latest version for existing order to false", method = "saveOrder(Order)")
	public void saveOrder_shouldSetLatestVersionForExistingOrderToFalse() throws Exception {
		
		Order order = Context.getOrderService().getOrder(10);
		Assert.assertTrue(order.isLatestVersion());
		
		Context.getOrderService().saveOrder(order);
		
		order = Context.getOrderService().getOrder(10);
		Assert.assertFalse(order.isLatestVersion());
	}
	
	/**
	 * @see {@link OrderService#getOrderables(String)}
	 */
	@Test
	@Verifies(value = "get orderable concepts by name and drug class", method = "getOrderables(String)")
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
	@Verifies(value = "fail if null passed in", method = "getOrderables(String)")
	public void getOrderables_shouldFailIfNullPassedIn() throws Exception {
		executeDataSet(simpleOrderEntryDatasetFilename);
		
		String query = null;
		Context.getOrderService().getOrderables(query);
	}
}
