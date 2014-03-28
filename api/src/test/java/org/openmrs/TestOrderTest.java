package org.openmrs;

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

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Contains tests for TestOrder class
 */
public class TestOrderTest {
	
	/**
	 * @verifies copy all test order fields
	 * @see TestOrder#copy()
	 */
	@Test
	public void copy_shouldCopyAllTestOrderFields() throws Exception {
		OrderTest.assertThatAllFieldsAreCopied(new TestOrder(), null);
	}
	
	/**
	 * @verifies set all the relevant fields
	 * @see TestOrder#cloneForRevision()
	 */
	@Test
	public void cloneForRevision_shouldSetAllTheRelevantFields() throws Exception {
		OrderTest.assertThatAllFieldsAreCopied(new TestOrder(), "cloneForRevision", "creator", "dateCreated", "action",
		    "changedBy", "dateChanged", "voided", "dateVoided", "voidedBy", "voidReason", "encounter", "orderNumber",
		    "orderer", "previousOrder", "startDate", "dateStopped");
	}
	
	/**
	 * @verifies set all the relevant fields
	 * @see TestOrder#cloneForDiscontinuing()
	 */
	@Test
	public void cloneForDiscontinuing_shouldSetAllTheRelevantFields() throws Exception {
		TestOrder anOrder = new TestOrder();
		anOrder.setPatient(new Patient());
		anOrder.setCareSetting(new CareSetting());
		anOrder.setConcept(new Concept());
		anOrder.setOrderType(new OrderType());
		
		Order orderThatCanDiscontinueTheOrder = anOrder.cloneForDiscontinuing();
		
		assertEquals(anOrder.getPatient(), orderThatCanDiscontinueTheOrder.getPatient());
		
		assertEquals(anOrder.getConcept(), orderThatCanDiscontinueTheOrder.getConcept());
		
		assertEquals("should set previous order to anOrder", anOrder, orderThatCanDiscontinueTheOrder.getPreviousOrder());
		
		assertEquals("should set new order action to new", orderThatCanDiscontinueTheOrder.getAction(),
		    Order.Action.DISCONTINUE);
		
		assertEquals(anOrder.getCareSetting(), orderThatCanDiscontinueTheOrder.getCareSetting());
		
		assertEquals(anOrder.getOrderType(), orderThatCanDiscontinueTheOrder.getOrderType());
	}
}
