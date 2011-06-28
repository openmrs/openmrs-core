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
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openmrs.Concept;
import org.openmrs.Drug;
import org.openmrs.DrugOrder;
import org.openmrs.GenericDrug;
import org.openmrs.Order;
import org.openmrs.OrderGroup;
import org.openmrs.OrderSet;
import org.openmrs.Orderable;
import org.openmrs.Patient;
import org.openmrs.PublishedOrderSet;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.test.Verifies;
import org.openmrs.util.OpenmrsUtil;

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
	 * @see {@link OrderService#getNewOrderNumber()}
	 */
	@Ignore
	@Test
	@Verifies(value = "should return the next unused order id", method = "getNewOrderNumber()")
	public void getNewOrderNumber_shouldReturnTheNextUnusedOrderId() throws Exception {
		Assert.assertEquals("ORDER-11", Context.getOrderService().getNewOrderNumber());
	}
	
	/**
	 * @see {@link OrderService#getOrderHistoryByConcept(Concept)}
	 */
	@Ignore
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
	@Ignore
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
	@Ignore
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
	@Ignore
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
	@Ignore
	@Test(expected = APIException.class)
	@Verifies(value = "should not fill order with non user if not signed", method = "fillOrder(Order, String)")
	public void fillOrder_shouldNotFillOrderWithNonUserIfNotSigned() throws Exception {
		Order order = Context.getOrderService().getOrder(10);
		Context.getOrderService().fillOrder(order, "url", null);
	}
	
	/**
	 * @see {@link OrderService#fillOrder(Order, User)}
	 */
	@Ignore
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
	@Ignore
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
		User provider = Context.getUserService().getUser(501);
		
		Order order = new Order();
		order.setConcept(Context.getConceptService().getConcept(23));
		order.setPatient(Context.getPatientService().getPatient(6));
		
		order = Context.getOrderService().signAndActivateOrder(order, provider, null);
		
		Assert.assertNotNull(order.getOrderId());
		Assert.assertNotNull(order.getOrderNumber());
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
	 * @see {@link OrderService#signAndActivateOrdersInGroup(org.openmrs.OrderGroup, User, Date)}
	 */
	@Ignore
	@Test
	@Verifies(value = "sign and activate orders group", method = "signAndActivateOrderGroup(OrderGroup, User, Date)")
	public void getOrderables_shouldSignAndActivateOrdersGroup() throws Exception {
		
		User provider = Context.getUserService().getUser(501);
		Patient patient = Context.getPatientService().getPatient(6);
		
		OrderGroup group = new OrderGroup(null, patient);
		group.setCreator(provider);
		group.setDateCreated(new Date());
		Order order = new Order();
		
		order.setConcept(Context.getConceptService().getConcept(23));
		order.setPatient(patient);
		group.addOrder(order);
		
		group = Context.getOrderService().signAndActivateOrdersInGroup(group, provider, null);
		order = (Order) group.getMembers().toArray()[0];
		//Should be saved.
		Assert.assertNotNull(group);
		
		//Should be signed.
		Assert.assertTrue(order.isSigned());
		Assert.assertNotNull(order.getDateSigned());
		
		//Should be activated.
		Assert.assertNotNull(order.getActivatedBy());
		Assert.assertNotNull(order.getDateActivated());
		
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
		Assert.assertEquals(3, groups.size());
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
		Order order = new Order();
		order.setActivatedBy(provider);
		order.setDateActivated(new Date());
		order.setSignedBy(provider);
		order.setDateSigned(new Date());
		order.setOrderNumber("TEST");
		order.setConcept(Context.getConceptService().getConcept(23));
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
	 * @verifies void orders in group
	 */
	@Test
	public void voidOrderGroup_shouldVoidOrdersInGroup() throws Exception {
		OrderGroup group = Context.getOrderService().getOrderGroup(2);
		String reason = "because";
		
		group = Context.getOrderService().voidOrderGroup(group, reason);
		Order order = (Order) group.getMembers().toArray()[0];
		
		// check if group is voided
		Assert.assertNotNull(group);
		Assert.assertTrue(group.isVoided());
		
		// check if group members are voided
		Assert.assertNotNull(order);
		Assert.assertTrue(order.isVoided());
	}
	
	/**
	 * @see OrderService#unvoidOrderGroup(OrderGroup)
	 * @verifies unvoid orders group
	 */
	@Test
	public void unvoidOrderGroup_shouldUnvoidOrdersGroup() throws Exception {
		OrderGroup group = Context.getOrderService().getOrderGroup(3);
		
		group = Context.getOrderService().unvoidOrderGroup(group);
		
		// check if group is unvoided
		Assert.assertNotNull(group);
		Assert.assertFalse(group.isVoided());
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
	
	/**
	 * @see OrderService#getOrderables(String)
	 * @verifies get order sets
	 */
	@Test
	public void getOrderables_shouldGetOrderSets() throws Exception {
		executeDataSet(orderSetsDatasetFilename);
		
		List<Orderable<?>> result = Context.getOrderService().getOrderables("Aspir");
		Assert.assertNotNull(result);
		boolean foundOrderSet = false;
		for (Orderable<?> o : result) {
			if (o instanceof PublishedOrderSet) {
				if (o.getName().equals("Aspirin and Triomune"))
					foundOrderSet = true;
			}
		}
		Assert.assertTrue(foundOrderSet);
	}
	
	/**
	 * @see OrderService#saveOrder(Order)
	 * @verifies not allow you to change the order number of a saved order
	 */
	@Ignore
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
	 * @see OrderService#saveOrder(Order)
	 * @verifies not allow you to edit an order after it has been activated
	 */
	@Test
	public void saveOrder_shouldNotAllowYouToEditAnOrderAfterItHasBeenActivated() throws Exception {
		DrugOrder existing = service.getOrder(1, DrugOrder.class);
		//service.activateOrder(existing, null, null);
		//Context.flushSession();
		existing = service.getOrder(1, DrugOrder.class);
		existing.setDose(999d);
		try {
			service.saveOrder(existing);
			Assert.fail("the previous line should have thrown an exception");
		}
		catch (APIException ex) {
			// test this way rather than @Test(expected...) so we can verify it's the right APIException
			Assert.assertTrue(ex.getMessage().contains("activated"));
		}
	}
	
	/**
	 * @see OrderService#saveOrder(Order)
	 * @verifies allow you to edit an order before it is activated
	 */
	@Ignore
	@Test
	public void saveOrder_shouldAllowYouToEditAnOrderBeforeItIsActivated() throws Exception {
		DrugOrder existing = service.getOrder(5, DrugOrder.class);
		Assert.assertNotSame(999d, existing.getDose());
		existing.setDose(999d);
		service.saveOrder(existing);
		// if we got here, that means success
		
		// I originally tried doing these two lines before and after the save, but Order doesn't have a real dateChanged property.
		// TODO determine whether we want to add dateChanged, and either remove this comment, or add the property and uncomment it 
		// Assert.assertNull(existing.getDateChanged());
		// Assert.assertNotNull(existing.getDateChanged());
	}
	
	/**
	 * @see OrderService#saveOrder(Order)
	 * @verifies not allow you to save an order that is not activated and signed
	 */
	@Test
	public void saveOrder_shouldNotAllowYouToSaveAnOrderThatIsNotActivatedAndSigned() throws Exception {
		DrugOrder order = new DrugOrder();
		order.setPatient(Context.getPatientService().getPatient(2));
		order.setConcept(Context.getConceptService().getConcept(88));
		// not signed or activated
		try {
			service.saveOrder(order);
			Assert.fail("previous line should have failed");
		}
		catch (APIException ex) {
			// exception is expected
		}
		
		order = new DrugOrder();
		order.setPatient(Context.getPatientService().getPatient(2));
		order.setConcept(Context.getConceptService().getConcept(88));
		// signed but not activated
		try {
			service.signOrder(order, null, null);
			Assert.fail("previous line should have failed");
		}
		catch (APIException ex) {
			// exception is expected
		}
		
		order = new DrugOrder();
		order.setPatient(Context.getPatientService().getPatient(2));
		order.setConcept(Context.getConceptService().getConcept(88));
		// activated, but not signed
		try {
			service.activateOrder(order, null, null);
			Assert.fail("previous line should have failed");
		}
		catch (APIException ex) {
			// exception is expected
		}
		
		order = new DrugOrder();
		order.setPatient(Context.getPatientService().getPatient(2));
		order.setConcept(Context.getConceptService().getConcept(88));
		// signed and activated
		service.signAndActivateOrder(order, null, null);
	}
	
	/**
	 * @see {@link OrderService#discontinueOrder(Order,String,User,Date)}
	 */
	@Ignore
	@Test
	@Verifies(value = "should discontinue and return the old order", method = "discontinueOrder(Order,String,User,Date)")
	public void discontinueOrder_shouldDiscontinueAndReturnTheOldOrder() throws Exception {
		int originalCount = service.getOrders(Order.class, null, null, null, null, null).size();
		Order order = service.getOrder(3);
		Assert.assertFalse(order.getDiscontinued());
		Assert.assertNull(order.getDiscontinuedDate());
		Assert.assertNull(order.getDiscontinuedBy());
		Assert.assertNull(order.getDiscontinuedReasonNonCoded());
		Order returnedOrder = service.discontinueOrder(order, "Testing");
		Assert.assertEquals(order, returnedOrder);
		Assert.assertTrue(order.getDiscontinued());
		Assert.assertEquals("Testing", returnedOrder.getDiscontinuedReasonNonCoded());
		Assert.assertNotNull(returnedOrder.getDiscontinuedDate());
		Assert.assertNotNull(returnedOrder.getDiscontinuedBy());
		//should have created a discontinue order
		Assert.assertEquals(originalCount + 1, service.getOrders(Order.class, null, null, null, null, null).size());
	}
	
	/**
	 * @see {@link OrderService#discontinueOrder(Order,String,User,Date)}
	 */
	@Test(expected = APIException.class)
	@Verifies(value = "should fail if the passed in discontinue date is in the past for an actived order", method = "discontinueOrder(Order,String,User,Date)")
	public void discontinueOrder_shouldFailIfThePassedInDiscontinueDateIsInThePastForAnActivedOrder() throws Exception {
		Order order = service.getOrder(3);
		Assert.assertNotNull(order.getDateActivated());
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MINUTE, -1);
		service.discontinueOrder(order, "Testing", null, cal.getTime());
	}
	
	/**
	 * @see {@link OrderService#discontinueOrder(Order,String,User,Date)}
	 */
	@Ignore
	@Test
	@Verifies(value = "should re discontinue an order whose discontinued date has not yet passed", method = "discontinueOrder(Order,String,User,Date)")
	public void discontinueOrder_shouldReDiscontinueAnOrderWhoseDiscontinuedDateHasNotYetPassed() throws Exception {
		//discontinue the same order twice with different dates
		Order order = service.getOrder(5);
		Assert.assertNull(order.getDateActivated());
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.HOUR_OF_DAY, 1);
		service.discontinueOrder(order, "Testing1", null, cal.getTime());
		Assert.assertTrue(order.getDiscontinued());
		
		//re discontinue to future time
		Calendar cal1 = Calendar.getInstance();
		cal1.add(Calendar.HOUR_OF_DAY, 2);
		Date date2 = cal1.getTime();
		service.discontinueOrder(order, "Testing2", null, date2);
		Assert.assertEquals(date2, order.getDiscontinuedDate());
		Assert.assertEquals("Testing2", order.getDiscontinuedReasonNonCoded());
		
		//re-discontinue back to an earlier time
		Calendar cal2 = Calendar.getInstance();
		cal2.add(Calendar.MINUTE, 2);
		Date date3 = cal2.getTime();
		service.discontinueOrder(order, "Testing3", null, date3);
		Assert.assertEquals(date3, order.getDiscontinuedDate());
		Assert.assertEquals("Testing3", order.getDiscontinuedReasonNonCoded());
	}
	
	/**
	 * @see {@link OrderService#discontinueOrder(Order,String,User,Date)}
	 */
	@Ignore
	@Test
	@Verifies(value = "should use the passed in future discontinue date if the order is not yet activated", method = "discontinueOrder(Order,String,User,Date)")
	public void discontinueOrder_shouldUseThePassedInFutureDiscontinueDateIfTheOrderIsNotYetActivated() throws Exception {
		Order order = service.getOrder(5);
		Assert.assertNull(order.getDateActivated());
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.HOUR_OF_DAY, 1);
		Date date = cal.getTime();
		service.discontinueOrder(order, "Testing", null, date);
		Assert.assertTrue(order.getDiscontinued());
		Assert.assertEquals(date, order.getDiscontinuedDate());
	}
	
	/**
	 * @see {@link OrderService#discontinueOrder(Order,String,User,Date)}
	 */
	@Ignore
	@Test
	@Verifies(value = "should default to current date for an activated order and discontinue date is in the past", method = "discontinueOrder(Order,String,User,Date)")
	public void discontinueOrder_shouldDefaultToCurrentDateForAnActivatedOrderAndDiscontinueDateIsInThePast()
	        throws Exception {
		Order order = service.getOrder(5);
		Assert.assertNull(order.getDateActivated());
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.HOUR_OF_DAY, -1);
		Date date = cal.getTime();
		service.discontinueOrder(order, "Testing", null, date);
		Assert.assertTrue(order.getDiscontinued());
		Assert.assertNotNull(order.getDiscontinuedDate());
		Assert.assertTrue(OpenmrsUtil.compareWithNullAsEarliest(order.getDiscontinuedDate(), date) > 0);
	}
	
	/**
	 * @see {@link OrderService#signAndActivateOrder(Order, User, Date))}
	 */
	@Test
	@Verifies(value = "should save sign activate order with unstructured dosing", method = "signAndActivateOrder(Order, User, Date)")
	public void saveActivatedOrder_shouldSaveSignActivateOrderWithUnstructuredDosing() throws Exception {
		
		User provider = Context.getUserService().getUser(501);
		String unstructuredDosing = "500MG AS DIRECTED";
		
		DrugOrder order = new DrugOrder();
		order.setPatient(Context.getPatientService().getPatient(2));
		order.setConcept(Context.getConceptService().getConcept(23));
		order.setUnstructuredDosing(unstructuredDosing);
		order.setDateCreated(new Date());
		
		order = (DrugOrder) Context.getOrderService().signAndActivateOrder(order, provider, null);
		
		//Should be saved.
		Assert.assertNotNull(order);
		
		//Dosing should be set
		Assert.assertEquals(unstructuredDosing, order.getUnstructuredDosing());
		
		//Should be signed.
		Assert.assertTrue(order.isSigned());
		Assert.assertNotNull(order.getDateSigned());
		
		//Should be activated.
		Assert.assertNotNull(order.getActivatedBy());
		Assert.assertNotNull(order.getDateActivated());
	}
	
	/**
	 * @see {@link OrderService#signAndActivateOrder(Order, User, Date))}
	 */
	@Test
	@Verifies(value = "should save sign activate order with structured dosing", method = "signAndActivateOrder(Order, User, Date)")
	public void saveActivatedOrder_shouldSaveSignActivateOrderWithStructuredDosing() throws Exception {
		
		User provider = Context.getUserService().getUser(501);
		
		DrugOrder order = new DrugOrder();
		order.setPatient(Context.getPatientService().getPatient(2));
		order.setConcept(Context.getConceptService().getConcept(23));
		
		order.setDose(Double.parseDouble("500"));
		order.setRoute("test");
		order.setFrequency("daily");
		order.setDuration(50);
		order.setDateCreated(new Date());
		
		order = (DrugOrder) Context.getOrderService().signAndActivateOrder(order, provider, null);
		
		//Should be saved.
		Assert.assertNotNull(order);
		
		//Dosing should be set
		Assert.assertNotNull(order.getDose());
		
		//Should be signed.
		Assert.assertTrue(order.isSigned());
		Assert.assertNotNull(order.getDateSigned());
		
		//Should be activated.
		Assert.assertNotNull(order.getActivatedBy());
		Assert.assertNotNull(order.getDateActivated());
	}
}
