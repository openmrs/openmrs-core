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
import org.junit.Test;
import org.openmrs.Concept;
import org.openmrs.DrugOrder;
import org.openmrs.Obs;
import org.openmrs.Order;
import org.openmrs.OrderType;
import org.openmrs.Patient;
import org.openmrs.api.OrderService.ORDER_STATUS;
import org.openmrs.api.context.Context;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.test.Verifies;

/**
 * TODO clean up and test all methods in OrderService
 */
public class OrderServiceTest extends BaseContextSensitiveTest {
	
	protected static final String DRUG_ORDERS_DATASET_XML = "org/openmrs/api/include/OrderServiceTest-drugOrdersList.xml";
	
	protected static final String ORDERS_DATASET_XML = "org/openmrs/api/include/OrderServiceTest-ordersList.xml";
	
	protected static final String OBS_THAT_REFERENCE_DATASET_XML = "org/openmrs/api/include/OrderServiceTest-deleteObsThatReference.xml";
	
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
	 * 
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
	 * 
	 */
	@Test
	@Verifies(value = "should return null if no object found with given uuid", method = "getOrderByUuid(String)")
	public void getOrderByUuid_shouldReturnNullIfNoObjectFoundWithGivenUuid() throws Exception {
		Assert.assertNull(Context.getOrderService().getOrderByUuid("some invalid uuid"));
	}
	
	/**
	 * @see {@link OrderService#getOrderTypeByUuid(String)}
	 * 
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
	 * 
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
	 * @see {@link OrderService#discontinueOrder(Order, Concept, Date)}
	 */
	@Test
	@Verifies(value = "should set discontinuedDate if the discontinue date is not in future", method = "discontinueOrder(Order order, Concept discontinueReason, Date discontinueDate)")
	public void discontinueOrder_shouldSetDiscontinuedDateIfTheDiscontinueDateIsNotInFuture() throws Exception {
		String uuid = "921de0a3-05c4-444a-be03-e01b4c4b9142";
		Order order = Context.getOrderService().getOrderByUuid(uuid);
		Concept discontinudReason = Context.getConceptService().getConcept(1107);
		Date discontinueDate = new Date();
		
		Order updatedOrder = Context.getOrderService().discontinueOrder(order, discontinudReason, discontinueDate);
		
		Assert.assertEquals(discontinueDate, updatedOrder.getDiscontinuedDate());
		
	}
	
	/**
	 * @see {@link OrderService#discontinueOrder(Order, Concept, Date)}
	 */
	@Test
	@Verifies(value = "should set autoExpireDate if the discontinue date is in future", method = "discontinueOrder(Order order, Concept discontinueReason, Date discontinueDate)")
	public void discontinueOrder_shouldSetAutoExpireDateIfTheDiscontinueDateIsInFuture() throws Exception {
		String uuid = "921de0a3-05c4-444a-be03-e01b4c4b9142";
		Order order = Context.getOrderService().getOrderByUuid(uuid);
		Concept discontinudReason = Context.getConceptService().getConcept(1107);
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, 10);
		Date autoExpireDate = cal.getTime();
		Order updatedOrder = Context.getOrderService().discontinueOrder(order, discontinudReason, autoExpireDate);
		
		Assert.assertEquals(autoExpireDate, updatedOrder.getAutoExpireDate());
		
	}
	
	@Test
	public void voidDrugSet_shouldNotVoidThePatient() throws Exception {
		Patient p = Context.getPatientService().getPatient(2);
		Assert.assertFalse(p.isVoided());
		Context.getOrderService().voidDrugSet(p, "1", "Reason", OrderService.SHOW_ALL);
		Assert.assertFalse(p.isVoided());
	}
	
	/**
	 * @see {@link OrderService#getDrugOrdersByPatient(Patient, ORDER_STATUS, boolean)}
	 */
	@Test
	@Verifies(value = "return list of drug orders with given status", method = "getDrugOrdersByPatient(Patient, ORDER_STATUS, boolean)")
	public void getDrugOrdersByPatient_shouldReturnListOfDrugOrdersWithGivenStatus() throws Exception {
		executeDataSet(DRUG_ORDERS_DATASET_XML);
		Patient p = Context.getPatientService().getPatient(2);
		List<DrugOrder> drugOrders = Context.getOrderService().getDrugOrdersByPatient(p, ORDER_STATUS.CURRENT_AND_FUTURE,
		    Boolean.FALSE);
		Assert.assertEquals(4, drugOrders.size());
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
		Assert.assertEquals(8, orders.size());
		
		orders = Context.getOrderService().getOrdersByPatient(p, false);
		Assert.assertEquals(4, orders.size());
	}
	
	/**
	 * @see {@link OrderService#getOrdersByPatient(Patient)}
	 */
	@Test
	@Verifies(value = "return list of non voided orders for patient", method = "getOrdersByPatient(Patient)")
	public void getOrdersByPatient_shouldReturnListOfNonVoidedOrdersForPatient() throws Exception {
		executeDataSet(ORDERS_DATASET_XML);
		Patient p = Context.getPatientService().getPatient(2);
		List<Order> orders = Context.getOrderService().getOrdersByPatient(p);
		Assert.assertEquals(4, orders.size());
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
	
	@Test
	public void purgeOrder_shouldDeleteObsThatReference() throws Exception {
		executeDataSet(OBS_THAT_REFERENCE_DATASET_XML);
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
	
}
