/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Date;

import org.junit.jupiter.api.Test;
import org.openmrs.order.OrderUtilTest;

/**
 * Contains tests for ReferralOrder class
 */
public class ReferralOrderTest {
	
	/**
	 * @throws Exception
	 * @see ReferralOrder#copy()
	 */
	@Test
	public void copy_shouldCopyAllReferralOrderFields() throws Exception {
		OrderTest.assertThatAllFieldsAreCopied(new ReferralOrder(), null);
	}
	
	/**
	 * @throws Exception
	 * @see ReferralOrder#cloneForRevision()
	 */
	@Test
	public void cloneForRevision_shouldSetAllTheRelevantFields() throws Exception {
		ReferralOrder newReferralOrder = new ReferralOrder();
		
		OrderGroup orderGroup = new OrderGroup();
		newReferralOrder.setOrderGroup(orderGroup);
		
		ReferralOrder revisedReferralOrder = newReferralOrder.cloneForRevision();
		
		OrderTest.assertThatAllFieldsAreCopied(revisedReferralOrder, "cloneForRevision", "creator", "dateCreated", "action",
		    "changedBy", "dateChanged", "voided", "dateVoided", "voidedBy", "voidReason", "encounter", "orderNumber",
		    "orderer", "previousOrder", "dateActivated", "dateStopped", "accessionNumber");
	}
	
	/**
	 * @see ReferralOrder#cloneForDiscontinuing()
	 */
	@Test
	public void cloneForDiscontinuing_shouldSetAllTheRelevantFields() {
		ReferralOrder anOrder = new ReferralOrder();
		anOrder.setPatient(new Patient());
		anOrder.setCareSetting(new CareSetting());
		anOrder.setConcept(new Concept());
		anOrder.setOrderType(new OrderType());
		
		Order orderThatCanDiscontinueTheOrder = anOrder.cloneForDiscontinuing();
		
		assertEquals(anOrder.getPatient(), orderThatCanDiscontinueTheOrder.getPatient());
		
		assertEquals(anOrder.getConcept(), orderThatCanDiscontinueTheOrder.getConcept());
		
		assertEquals(anOrder, orderThatCanDiscontinueTheOrder.getPreviousOrder(), "should set previous order to anOrder");
		
		assertEquals(orderThatCanDiscontinueTheOrder.getAction(), Order.Action.DISCONTINUE, "should set new order action to new");
		
		assertEquals(anOrder.getCareSetting(), orderThatCanDiscontinueTheOrder.getCareSetting());
		
		assertEquals(anOrder.getOrderType(), orderThatCanDiscontinueTheOrder.getOrderType());
	}
	
	/**
	 * @see ReferralOrder#cloneForRevision()
	 */
	@Test
	public void cloneForRevision_shouldSetTheRelevantFieldsForADCOrder() {
		Order order = new ReferralOrder();
		order.setAction(Order.Action.DISCONTINUE);
		Date date = new Date();
		order.setDateActivated(date);
		order.setAutoExpireDate(date);
		order.setAccessionNumber("some number");
		OrderUtilTest.setDateStopped(order, date);
		order.setPreviousOrder(new Order());
		
		Order clone = order.cloneForRevision();
		assertEquals(Order.Action.DISCONTINUE, clone.getAction());
		assertEquals(order.getDateActivated(), clone.getDateActivated());
		assertEquals(order.getPreviousOrder(), clone.getPreviousOrder());
		assertNull(clone.getAutoExpireDate());
		assertNull(clone.getDateStopped());
		assertNull(clone.getAccessionNumber());
	}
}
