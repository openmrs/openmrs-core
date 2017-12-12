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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.openmrs.OrderSet;
import org.openmrs.OrderSetMember;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.test.BaseContextSensitiveTest;

public class OrderSetServiceTest extends BaseContextSensitiveTest {
	
	protected OrderService orderService;
	
	protected OrderSetService orderSetService;
	
	protected ConceptService conceptService;
	
	protected static final String ORDER_SET = "org/openmrs/api/include/OrderSetServiceTest-general.xml";
	
	@Rule
	public ExpectedException expectedException = ExpectedException.none();
	
	/**
	 * Run this before each unit test in this class. The "@Before" method in
	 * {@link BaseContextSensitiveTest} is run right before this method.
	 *
	 * @throws Exception
	 */
	@Before
	public void runBeforeAllTests() {
		if (null == orderSetService) {
			orderSetService = Context.getOrderSetService();
		}
		if (null == orderService) {
			orderService = Context.getOrderService();
		}
		if (null == conceptService) {
			conceptService = Context.getConceptService();
		}
	}
	
	@Test
	public void shouldSaveOrderSet() {
		executeDataSet(ORDER_SET);
		Integer initialNumberOfOrderSets = orderSetService.getOrderSets(false).size();
		
		OrderSet orderSet = orderSetBuilder(false, false);
		OrderSet orderSetObj = orderSetService.saveOrderSet(orderSet);
		Context.flushSession();
		
		List<OrderSet> orderSets = orderSetService.getOrderSets(false);
		
		assertEquals("A new order set was saved to the exisiting list of order sets", initialNumberOfOrderSets + 1,
		    orderSets.size());
		assertNotNull("OrderSet has a order_set_id", orderSetObj.getId());
	}
	
	@Test
	public void shouldSaveAndUpdateOrderSet() {
		executeDataSet(ORDER_SET);
		
		OrderSet orderSet = orderSetBuilder(false, false);
		
		OrderSet orderSetObj = orderSetService.saveOrderSet(orderSet);
		
		orderSetObj.setOperator(OrderSet.Operator.ONE);
		orderSetObj.setDescription("Test Order Set Description Updated");
		
		assertNull("OrderSet is new and is not changed", orderSetObj.getChangedBy());
		assertNull("OrderSet is new and has no change date", orderSetObj.getDateChanged());
		
		orderSetService.saveOrderSet(orderSetObj);
		Context.flushSession();
		
		assertNotNull("OrderSet has a order_set_id", orderSetObj.getId());
		assertEquals("OrderSet has updated description", "Test Order Set Description Updated", orderSetObj.getDescription());
		assertEquals("OrderSet has updated operator", "ONE", orderSetObj.getOperator().toString());
		
		assertNotNull("OrderSet has been changed", orderSetObj.getChangedBy());
		assertNotNull("OrderSet has been changed on some date", orderSetObj.getDateChanged());
		
	}
	
	@Test
	public void shouldRetrieveOrderSetMembersOfAnOrderSet() {
		executeDataSet(ORDER_SET);
		OrderSet orderSet = orderSetService.getOrderSet(2001);
		
		assertEquals("OrderSet should contain orderSetmembers", 2, orderSet.getOrderSetMembers().size());
		
		OrderSet orderSet1 = orderSetService.getOrderSet(2000);
		
		assertEquals("OrderSet should not contain retired orderSetMembers", 2, orderSet1.getOrderSetMembers().size());
	}
	
	@Test
	public void shouldNotAutomaticallyPropagateToSetMembersIfExcludingOrderSet() {
		executeDataSet(ORDER_SET);
		
		List<OrderSet> orderSets = orderSetService.getOrderSets(false);
		Integer numberOfOrderSetMembers = 0;
		for (OrderSet oS : orderSets) {
			numberOfOrderSetMembers = numberOfOrderSetMembers + oS.getOrderSetMembers().size();
		}
		assertEquals(new Integer(4), numberOfOrderSetMembers);
	}
	
	@Test
	public void shouldAddOrderSetMemberAtLastPositionInsideAnExistingOrderSetIfPositionIsNull() {
		executeDataSet(ORDER_SET);
		
		OrderSet orderSet = orderSetService.getOrderSet(2001);
		Integer initialSize = orderSet.getOrderSetMembers().size();
		
		OrderSetMember newOrderSetMember = new OrderSetMember();
		newOrderSetMember.setOrderType(orderService.getOrderType(100));
		newOrderSetMember.setConcept(conceptService.getConcept(1002));
		newOrderSetMember.setCreator(new User(1));
		newOrderSetMember.setDateCreated(new Date());
		newOrderSetMember.setRetired(false);
		
		orderSet.addOrderSetMember(newOrderSetMember, null);
		
		Context.getOrderSetService().saveOrderSet(orderSet);
		
		Context.flushSession();
		
		OrderSet savedOrderSet = Context.getOrderSetService().getOrderSetByUuid(orderSet.getUuid());
		
		assertEquals("Size of the orderSetMembers got updated", initialSize + 1, savedOrderSet.getOrderSetMembers().size());
		assertEquals("New OrderSetMember got added at last position", newOrderSetMember.getUuid(), savedOrderSet
		        .getOrderSetMembers().get(initialSize).getUuid());
	}
	
	@Test
	public void shouldAddOrderSetMemberAtSomePositionInsideAnExistingOrderSet() {
		executeDataSet(ORDER_SET);
		
		OrderSet orderSet = orderSetService.getOrderSet(2001);
		Integer initialSize = orderSet.getOrderSetMembers().size();
		
		OrderSetMember newOrderSetMember = new OrderSetMember();
		newOrderSetMember.setOrderType(orderService.getOrderType(100));
		newOrderSetMember.setConcept(conceptService.getConcept(1002));
		newOrderSetMember.setCreator(new User(1));
		newOrderSetMember.setDateCreated(new Date());
		newOrderSetMember.setRetired(false);
		
		Integer position = 2;
		orderSet.addOrderSetMember(newOrderSetMember, position);
		
		Context.getOrderSetService().saveOrderSet(orderSet);
		
		Context.flushSession();
		
		OrderSet savedOrderSet = Context.getOrderSetService().getOrderSetByUuid(orderSet.getUuid());
		
		assertEquals("Size of the orderSetMembers got updated", initialSize + 1, savedOrderSet.getOrderSetMembers().size());
		assertEquals("New OrderSetMember got added at given position", newOrderSetMember.getUuid(), savedOrderSet
		        .getOrderSetMembers().get(position).getUuid());
		
		Integer newPosition = savedOrderSet.getOrderSetMembers().size() + 1;
		OrderSetMember orderSetMemberToBeAddedAtPosition = new OrderSetMember();
		orderSetMemberToBeAddedAtPosition.setOrderType(orderService.getOrderType(100));
		orderSetMemberToBeAddedAtPosition.setConcept(conceptService.getConcept(1002));
		orderSetMemberToBeAddedAtPosition.setCreator(new User(1));
		orderSetMemberToBeAddedAtPosition.setDateCreated(new Date());
		orderSetMemberToBeAddedAtPosition.setRetired(false);
		
		expectedException.expect(APIException.class);
		expectedException.expectMessage("Cannot add a member which is out of range of the list");
		orderSet.addOrderSetMember(orderSetMemberToBeAddedAtPosition, newPosition);
	}
	
	@Test
	public void shouldReturnInTheSameArrangementInWhichTheOrderSetMembersAreSaved() {
		executeDataSet(ORDER_SET);
		
		OrderSet newOrderSet = new OrderSet();
		newOrderSet.setOperator(OrderSet.Operator.ALL);
		newOrderSet.setName("NewOrderSet");
		newOrderSet.setDescription("New Order Set");
		
		OrderSetMember firstOrderSetMember = new OrderSetMember();
		firstOrderSetMember.setOrderType(orderService.getOrderType(100));
		firstOrderSetMember.setConcept(conceptService.getConcept(1000));
		firstOrderSetMember.setCreator(new User(1));
		firstOrderSetMember.setDateCreated(new Date());
		firstOrderSetMember.setRetired(false);
		
		OrderSetMember secondOrderSetMember = new OrderSetMember();
		secondOrderSetMember.setOrderType(orderService.getOrderType(100));
		secondOrderSetMember.setConcept(conceptService.getConcept(1001));
		secondOrderSetMember.setCreator(new User(1));
		secondOrderSetMember.setDateCreated(new Date());
		secondOrderSetMember.setRetired(false);
		
		OrderSetMember thirdOrderSetMember = new OrderSetMember();
		thirdOrderSetMember.setOrderType(orderService.getOrderType(100));
		thirdOrderSetMember.setConcept(conceptService.getConcept(1001));
		thirdOrderSetMember.setCreator(new User(1));
		thirdOrderSetMember.setDateCreated(new Date());
		thirdOrderSetMember.setRetired(false);
		
		List<OrderSetMember> orderSetMembers = new ArrayList<>(Arrays.asList(firstOrderSetMember,
		    thirdOrderSetMember, secondOrderSetMember));
		newOrderSet.setOrderSetMembers(orderSetMembers);
		
		OrderSet savedOrderSet = orderSetService.saveOrderSet(newOrderSet);
		Context.flushSession();
		
		assertEquals(firstOrderSetMember.getUuid(), savedOrderSet.getOrderSetMembers().get(0).getUuid());
		assertEquals(thirdOrderSetMember.getUuid(), savedOrderSet.getOrderSetMembers().get(1).getUuid());
		assertEquals(secondOrderSetMember.getUuid(), savedOrderSet.getOrderSetMembers().get(2).getUuid());
	}
	
	@Test
	public void shouldAddOrderSetMemberFromTheEndOfTheListIfNegativePositionIsGiven() {
		executeDataSet(ORDER_SET);
		
		OrderSet orderSet = orderSetService.getOrderSet(2001);
		Integer initialSize = orderSet.getOrderSetMembers().size();
		
		OrderSetMember newOrderSetMember = new OrderSetMember();
		newOrderSetMember.setOrderType(orderService.getOrderType(100));
		newOrderSetMember.setConcept(conceptService.getConcept(1002));
		newOrderSetMember.setCreator(new User(1));
		newOrderSetMember.setDateCreated(new Date());
		newOrderSetMember.setRetired(false);
		
		Integer position = -2;
		orderSet.addOrderSetMember(newOrderSetMember, position);
		
		Context.getOrderSetService().saveOrderSet(orderSet);
		
		Context.flushSession();
		
		OrderSet savedOrderSet = Context.getOrderSetService().getOrderSetByUuid(orderSet.getUuid());
		
		assertEquals("Size of the orderSetMembers got updated", initialSize + 1, savedOrderSet.getOrderSetMembers().size());
		assertEquals("New OrderSetMember got added at given position", newOrderSetMember.getUuid(), savedOrderSet
		        .getOrderSetMembers().get(position + initialSize + 1).getUuid());
		
	}

	@Test
	public void shouldFetchUnRetiredOrderSetMembers() {
		executeDataSet(ORDER_SET);

		OrderSet orderSet = orderSetService.getOrderSet(2000);
		int initialCountOfMembers = orderSet.getOrderSetMembers().size();
		OrderSetMember orderSetMember = orderSet.getOrderSetMembers().get(0);

		//Retiring an orderSetMember in an existing list of orderSetMembers
		orderSet.retireOrderSetMember(orderSetMember);
		orderSetService.saveOrderSet(orderSet);
		Context.flushSession();

		OrderSet savedOrderSet = orderSetService.getOrderSetByUuid(orderSet.getUuid());
		assertEquals("Count of orderSetMembers are not changed if we get all members", initialCountOfMembers, savedOrderSet.getOrderSetMembers().size());

		//Fetching the unRetired members
		int finalSize = savedOrderSet.getUnRetiredOrderSetMembers().size();
		assertEquals("Count of orderSetMembers gets modified if we filter out the retired members", initialCountOfMembers-1, finalSize);
	}

	@Test
	public void shouldDeleteAnOrderSetMemberInAnOrderSet() {
		executeDataSet(ORDER_SET);

		OrderSet orderSet = orderSetService.getOrderSet(2001);
		int initialCountOfMembers = orderSet.getOrderSetMembers().size();
		OrderSetMember orderSetMember = orderSet.getOrderSetMembers().get(0);

		//Removing an orderSetMember in an existing list of orderSetMembers
		orderSet.removeOrderSetMember(orderSetMember);
		orderSetService.saveOrderSet(orderSet);
		Context.flushSession();

		OrderSet savedOrderSet = orderSetService.getOrderSetByUuid(orderSet.getUuid());
		assertEquals("Count of orderSetMembers changes after removing a member from the orderSet", initialCountOfMembers-1, savedOrderSet.getOrderSetMembers().size());
	}

	@Test
	public void shouldFetchOrderSetMemberByUuid() {
		String orderSetUuid = "2d3fb1d0-ae06-22e3-a5e2-0140211c2002";
		executeDataSet(ORDER_SET);

		OrderSetMember orderSetMember = orderSetService.getOrderSetMemberByUuid(orderSetUuid);
		assertNotNull(orderSetMember.getId());
	}

	@Test
	public void shouldRetireOrderSetAndOrderSetMembersAsWell() {
		executeDataSet(ORDER_SET);

		int initialNumberOfOrderSets = orderSetService.getOrderSets(false).size();

		OrderSet orderSet = orderSetService.getOrderSet(2001);
		orderSetService.retireOrderSet(orderSet, "Testing");
		Context.flushSession();

		int numberOfOrderSetsAfterRetire = orderSetService.getOrderSets(false).size();
		assertEquals(initialNumberOfOrderSets-1,numberOfOrderSetsAfterRetire);

		OrderSet retiredOrderSet = orderSetService.getOrderSet(2001);
		assertEquals(true, retiredOrderSet.getRetired());

		List<OrderSetMember> orderSetMembers = retiredOrderSet.getOrderSetMembers();
		for (OrderSetMember orderSetMember : orderSetMembers) {
			assertTrue(orderSetMember.getRetired());
		}
	}

	private OrderSet orderSetBuilder(boolean orderSetRetired, boolean orderSetMemberRetired) {
		OrderSet orderSet = new OrderSet();
		orderSet.setName("Test Order Set");
		orderSet.setDescription("Test Order Set Description");
		orderSet.setOperator(OrderSet.Operator.ALL);
		
		OrderSetMember orderSetMember = new OrderSetMember();
		orderSetMember.setOrderType(orderService.getOrderType(100));
		orderSetMember.setConcept(conceptService.getConcept(1000));
		orderSetMember.setCreator(new User(1));
		orderSetMember.setDateCreated(new Date());
		orderSetMember.setRetired(orderSetMemberRetired);
		orderSetMember.setOrderSet(orderSet);

		List<OrderSetMember> orderSetMembers = new ArrayList<>(Collections.singletonList(orderSetMember));
		orderSet.setOrderSetMembers(orderSetMembers);
		orderSet.setCreator(new User(1));
		orderSet.setDateCreated(new Date());
		orderSet.setRetired(orderSetRetired);
		return orderSet;
	}
}
