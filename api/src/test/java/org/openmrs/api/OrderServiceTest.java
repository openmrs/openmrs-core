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
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openmrs.Concept;
import org.openmrs.Drug;
import org.openmrs.DrugOrder;
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

/**
 * TODO clean up and test all methods in OrderService
 */
public class OrderServiceTest extends BaseContextSensitiveTest {
	
	private static final String simpleOrderEntryDatasetFilename = "org/openmrs/api/include/OrderServiceTest-simpleOrderEntryTestDataset.xml";
	
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
	 * @see {@link OrderService#getOrderHistoryByConcept(PatientConcept)}
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
	 * @see {@link OrderService#fillOrder(Order, User)}
	 */
	@Test
	@Verifies(value = "should fill order with user", method = "fillOrder(Order, User)")
	public void fillOrder_shouldFillOrderWithUser() throws Exception {
		User provider = Context.getUserService().getUser(501);
		
		Order order = Context.getOrderService().getOrder(10);
		Assert.assertTrue(order.getFiller() == null);
		Assert.assertTrue(order.getDateFilled() == null);
		
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
		Order order = Context.getOrderService().getOrder(10);
		Assert.assertTrue(order.getFiller() == null);
		Assert.assertTrue(order.getDateFilled() == null);
		
		Context.getOrderService().fillOrder(order, "the pharmacist", null);
		
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
	 * @see OrderService#saveOrder(Order)
	 * @verifies not allow you to change the order number of a saved order
	 */
	@Ignore
	// re-enable test when we allow orders to be persisted when not activated and signed
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
	 * @verifies allow you to edit an order before it is activated
	 */
	@Ignore
	// re-enable test when we allow orders to be persisted when not activated and signed
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
	 * @see {@link OrderService#signAndActivateOrder(Order, User, Date))}
	 */
	@Test
	@Verifies(value = "should save sign activate order with unstructured dosing", method = "signAndActivateOrder(Order, User, Date)")
	public void signAndActivateOrder_shouldSaveSignActivateOrderWithUnstructuredDosing() throws Exception {
		
		String unstructuredDosing = "500MG AS DIRECTED";
		
		DrugOrder order = new DrugOrder();
		order.setPatient(Context.getPatientService().getPatient(2));
		order.setConcept(Context.getConceptService().getConcept(23));
		order.setUnstructuredDosing(unstructuredDosing);
		
		order = (DrugOrder) Context.getOrderService().signAndActivateOrder(order);
		
		//Should be saved.
		Assert.assertNotNull(order.getOrderId());
		
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
	public void signAndActivateOrder_shouldSaveSignActivateOrderWithStructuredDosing() throws Exception {
		
		DrugOrder order = new DrugOrder();
		order.setPatient(Context.getPatientService().getPatient(2));
		order.setConcept(Context.getConceptService().getConcept(23));
		
		order.setDose(Double.parseDouble("500"));
		order.setRoute("test");
		order.setFrequency("daily");
		order.setDuration(50);
		order.setDurationUnits("days");
		
		order = (DrugOrder) Context.getOrderService().signAndActivateOrder(order);
		
		//Should be saved.
		Assert.assertNotNull(order.getOrderId());
		
		//Dosing should be set
		Assert.assertNotNull(order.getDose());
		
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
	@Verifies(value = "discontinue previous order", method = "signAndActivateOrder(Order, User, Date)")
	public void signAndActivateOrder_shouldDiscontinuePreviousOrder() throws Exception {
		User provider = Context.getUserService().getUser(501);
		Concept concept = Context.getConceptService().getConcept(23);
		Patient patient = Context.getPatientService().getPatient(6);
		
		Order order1 = new Order();
		order1.setConcept(concept);
		order1.setPatient(patient);
		
		service.signAndActivateOrder(order1, provider, new Date());
		
		Order order2 = new Order();
		order2.setConcept(concept);
		order2.setPatient(patient);
		order2.setPreviousOrderNumber(order1.getOrderNumber());
		
		order2 = service.signAndActivateOrder(order2, provider, new Date());
		
		Assert.assertTrue(order1.getDiscontinued());
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
		Assert.assertNull(order.getDiscontinuedReasonNonCoded());
		Order returnedOrder = service.discontinueOrder(order, "Testing");
		Assert.assertEquals(order, returnedOrder);
		Assert.assertTrue(order.getDiscontinued());
		Assert.assertEquals("Testing", returnedOrder.getDiscontinuedReasonNonCoded());
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
	@Verifies(value = "should fail if the passed in discontinue date is before the date activated", method = "discontinueOrder(Order,String,User,Date)")
	public void discontinueOrder_shouldFailIfThePassedInDiscontinueDateIsBeforeTheDateActivated() throws Exception {
		Order order = service.getOrder(3);
		Assert.assertNotNull(order.getDateActivated());
		Calendar cal = Calendar.getInstance();
		//set the time to before date activated
		cal.setTime(order.getDateActivated());
		cal.add(Calendar.MINUTE, -1);
		service.discontinueOrder(order, "Testing", null, cal.getTime());
	}
	
	/**
	 * @see {@link OrderService#discontinueOrder(Order,String,User,Date)}
	 */
	@Test(expected = APIException.class)
	@Verifies(value = "should fail if the passed in discontinue date is in the future", method = "discontinueOrder(Order,String,User,Date)")
	public void discontinueOrder_shouldFailIfThePassedInDiscontinueDateIsInTheFuture() throws Exception {
		Order order = service.getOrder(3);
		Assert.assertNotNull(order.getDateActivated());
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
		int N = 50;
		final Set<String> uniqueOrderNumbers = new HashSet<String>(50);
		List<Thread> threads = new ArrayList<Thread>();
		for (int i = 0; i < N; i++) {
			threads.add(new Thread(new Runnable() {
				
				@Override
				public void run() {
					try {
						Context.openSession();
						uniqueOrderNumbers.add(service.getNewOrderNumber());
					}
					finally {
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
		
		System.out.println(uniqueOrderNumbers);
	}
}
