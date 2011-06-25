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

import java.util.Date;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Concept;
import org.openmrs.Drug;
import org.openmrs.GenericDrug;
import org.openmrs.Order;
import org.openmrs.OrderGroup;
import org.openmrs.OrderSet;
import org.openmrs.Orderable;
import org.openmrs.Patient;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.test.Verifies;

/**
 * TODO clean up and test all methods in OrderService
 */
public class OrderServiceTest extends BaseContextSensitiveTest {
	
	private static final String simpleOrderEntryDatasetFilename = "org/openmrs/api/include/OrderServiceTest-simpleOrderEntryTestDataset.xml";
	
	private static final String orderSetsDatasetFilename = "org/openmrs/api/include/OrderServiceTest-orderSetsTestDataset.xml";
	
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
		Patient patient = Context.getPatientService().getPatient(1);
		List<Order> orders = Context.getOrderService().getOrderHistoryByConcept(patient, concept);
		Assert.assertEquals(3, orders.size());
		for (Order order : orders)
			Assert.assertTrue(order.getOrderId() == 1 || order.getOrderId() == 4 || order.getOrderId() == 5);
		
		//We should two orders with this concept.
		concept = Context.getConceptService().getConcept(792);
		orders = Context.getOrderService().getOrderHistoryByConcept(patient, concept);
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
		Patient patient = Context.getPatientService().getPatient(1);
		List<Order> orders = Context.getOrderService().getOrderHistoryByConcept(patient, concept);
		Assert.assertEquals(0, orders.size());
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
		
		Context.getOrderService().signOrder(order, provider, null);
		
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
		
		Context.getOrderService().activateOrder(order, provider, null);
		
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
		Context.getOrderService().fillOrder(order, provider, null);
	}
	
	/**
	 * @see {@link OrderService#fillOrder(Order, String)}
	 */
	@Test(expected = APIException.class)
	@Verifies(value = "should not fill order with non user if not signed", method = "fillOrder(Order, String)")
	public void fillOrder_shouldNotFillOrderWithNonUserIfNotSigned() throws Exception {
		Order order = Context.getOrderService().getOrder(10);
		Context.getOrderService().fillOrder(order, "url", null);
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
		
		Context.getOrderService().signOrder(order, provider, null);
		Context.getOrderService().fillOrder(order, provider, null);
		
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
		
		Context.getOrderService().signOrder(order, provider, null);
		Context.getOrderService().fillOrder(order, "url", null);
		
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
		Context.getOrderService().signAndActivateOrder(order, provider, null);
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
		order.setConcept(Context.getConceptService().getConcept(23));
		order.setPatient(Context.getPatientService().getPatient(6));
		
		Context.getOrderService().signAndActivateOrder(order, provider, null);
		
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
		order.setConcept(Context.getConceptService().getConcept(23));
		order.setPatient(Context.getPatientService().getPatient(6));
		
		String nextAvaliableOrderNumber = Context.getOrderService().getNewOrderNumber();
		
		Context.getOrderService().saveOrder(order);
		
		Assert.assertNotNull(order.getOrderId());
		Assert.assertEquals(nextAvaliableOrderNumber, order.getOrderNumber());
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
	
	/**
	 * @see {@link OrderService#signAndActivateOrderGroup(org.openmrs.OrderGroup, User, Date)}
	 */
	@Test
	@Verifies(value = "sign and activate orders group", method = "signAndActivateOrderGroup(OrderGroup, User, Date)")
	public void getOrderables_shouldSignAndActivateOrdersGroup() throws Exception {
		
		User provider = Context.getUserService().getUser(501);
		Patient patient = Context.getPatientService().getPatient(6);
		
		OrderGroup group = new OrderGroup(null, patient);
		group.setCreator(provider);
		group.setDateCreated(new Date());
		Order order = new Order();
		order.setOrderNumber("1");
		order.setDateCreated(new Date());
		order.setConcept(Context.getConceptService().getConcept(23));
		order.setPatient(patient);
		group.addOrder(order);
		
		group = Context.getOrderService().signAndActivateOrderGroup(group, provider, null);
		order = (Order) group.getMembers().toArray()[0];
		//Should be saved.
		Assert.assertNotNull(group);
		
		//Should be signed.
		Assert.assertTrue(order.isSigned());
		Assert.assertTrue(order.getDateSigned() != null);
		
		//Should be activated.
		Assert.assertTrue(order.getActivatedBy() != null);
		Assert.assertTrue(order.getDateActivated() != null);
		
	}
	
	/**
	 * @see OrderService#getOrderGroup(Integer)
	 * @verifies return order group entity by id
	 */
	@Test
	public void getOrderGroup_shouldReturnOrderGroupEntityById() throws Exception {
		OrderGroup group = Context.getOrderService().getOrderGroup(1);
		Assert.assertNotNull(group);
		Assert.assertEquals((Integer) 1, group.getOrderGroupId());
	}
	
	/**
	 * @see OrderService#getOrderGroup(Integer)
	 * @verifies return null if order group doesn't exist
	 */
	@Test
	public void getOrderGroup_shouldReturnNullIfOrderGroupDoesntExist() throws Exception {
		OrderGroup group = Context.getOrderService().getOrderGroup(100);
		Assert.assertNull(group);
	}
	
	/**
	 * @see OrderService#getOrderGroupByUuid(String)
	 * @verifies get order group by uuid
	 */
	@Test
	public void getOrderGroupByUuid_shouldGetOrderGroupByUuid() throws Exception {
		String uuid = "ab7cc118-c97b-4d5a-a63e-d4bb4be010ed";
		OrderGroup group = Context.getOrderService().getOrderGroupByUuid(uuid);
		Assert.assertNotNull(group);
		Assert.assertEquals(uuid, group.getUuid());
	}
	
	/**
	 * @see OrderService#getOrderGroupsByPatient(Patient)
	 * @verifies return not empty list of order groups
	 */
	@Test
	public void getOrderGroupsByPatient_shouldReturnNotEmptyListOfOrderGroups() throws Exception {
		Patient patient = Context.getPatientService().getPatient(6);
		List<OrderGroup> groups = Context.getOrderService().getOrderGroupsByPatient(patient);
		Assert.assertNotNull(groups);
		Assert.assertEquals(2, groups.size());
	}
	
	/**
	 * @see OrderService#saveOrderGroup(OrderGroup)
	 * @verifies save new order group
	 */
	@Test
	public void saveOrderGroup_shouldSaveNewOrderGroup() throws Exception {
		User provider = Context.getUserService().getUser(501);
		Patient patient = Context.getPatientService().getPatient(6);
		
		OrderGroup group = new OrderGroup(null, patient);
		group.setCreator(provider);
		group.setDateCreated(new Date());
		Order order = new Order();
		order.setOrderNumber("1");
		order.setDateCreated(new Date());
		order.setConcept(Context.getConceptService().getConcept(23));
		order.setPatient(patient);
		group.addOrder(order);
		
		group = Context.getOrderService().saveOrderGroup(group);
		
		Assert.assertNotNull(group);
		
	}
	
	/**
	 * @see OrderService#saveOrderGroup(OrderGroup)
	 * @verifies update existing order group
	 */
	@Test
	public void saveOrderGroup_shouldUpdateExistingOrderGroup() throws Exception {
		OrderGroup group = Context.getOrderService().getOrderGroup(2);
		group.setDateChanged(new Date());
		
		group = Context.getOrderService().saveOrderGroup(group);
		
		Assert.assertNotNull(group);
	}
	
	/**
	 * @see OrderService#voidOrderGroup(OrderGroup)
	 * @verifies void order group
	 */
	@Test
	public void voidOrderGroup_shouldVoidOrderGroup() throws Exception {
		OrderGroup group = Context.getOrderService().getOrderGroup(2);
		String reason = "because";
		
		group = Context.getOrderService().voidOrderGroup(group, reason);
		
		Assert.assertNotNull(group);
		Assert.assertTrue(group.isVoided());
	}
	
	/**
	 * @see OrderService#getPublishedOrderSet(Concept)
	 * @verifies get a published order set by concept
	 */
	@Test
	public void getPublishedOrderSet_shouldGetAPublishedOrderSetByConcept() throws Exception {
		executeDataSet(orderSetsDatasetFilename);
		Concept publishedAs = Context.getConceptService().getConcept(100);
		Assert.assertNotNull(publishedAs);
		Assert.assertEquals("Aspirin and Triomune", service.getPublishedOrderSet(publishedAs).getName());
	}
	
	/**
	 * @see OrderService#getPublishedOrderSets(String)
	 * @verifies get all published order sets by query
	 */
	@Test
	public void getPublishedOrderSets_shouldGetAllPublishedOrderSetsByQuery() throws Exception {
		executeDataSet(orderSetsDatasetFilename);
		Assert.assertEquals(1, service.getPublishedOrderSets("Aspirin and Triomune").size());
		Assert.assertEquals(1, service.getPublishedOrderSets("Aspirin").size());
		Assert.assertEquals(0, service.getPublishedOrderSets("CureYouFast(TM)").size());
	}
	
	/**
	 * @see OrderService#publishOrderSet(Concept,OrderSet)
	 * @verifies publish an order set as a concept
	 */
	@Test
	public void publishOrderSet_shouldPublishAnOrderSetAsAConcept() throws Exception {
		executeDataSet(orderSetsDatasetFilename);
		int before = service.getPublishedOrderSets("Unusual").size();
		
		OrderSet os = new OrderSet();
		os.setName("Unusual name unlikely to be found elsewhere");
		service.saveOrderSet(os);
		
		service.publishOrderSet(Context.getConceptService().getConcept(18), os);
		int after = service.getPublishedOrderSets("Unusual").size();
		Assert.assertEquals(before + 1, after);
	}
	
	/**
	 * @see OrderService#publishOrderSet(Concept,OrderSet)
	 * @verifies publish an order set as a concept overwriting the previous entity
	 */
	@Test
	public void publishOrderSet_shouldPublishAnOrderSetAsAConceptOverwritingThePreviousEntity() throws Exception {
		executeDataSet(orderSetsDatasetFilename);
		Assert.assertNotNull(service.getPublishedOrderSet(Context.getConceptService().getConcept(100)));
		Assert.assertNull(service.getPublishedOrderSet(Context.getConceptService().getConcept(18)));
		
		Concept foodConcept = Context.getConceptService().getConcept(18);
		OrderSet os = service.getOrderSet(1);
		service.publishOrderSet(foodConcept, os);
		
		Assert.assertNotNull(service.getPublishedOrderSet(Context.getConceptService().getConcept(18)));
		Assert.assertNull(service.getPublishedOrderSet(Context.getConceptService().getConcept(100)));
	}
	
}
