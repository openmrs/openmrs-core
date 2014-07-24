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
package org.openmrs;

import java.util.Date;

import org.junit.Test;
import org.openmrs.order.OrderUtilTest;

import static org.junit.Assert.*;

/**
 * Contains tests for DrugOrder
 */
public class DrugOrderTest {
	
	/**
	 * @verifies set all the relevant fields
	 * @see DrugOrder#cloneForDiscontinuing()
	 */
	@Test
	public void cloneForDiscontinuing_shouldSetAllTheRelevantFields() throws Exception {
		DrugOrder order = new DrugOrder();
		order.setPatient(new Patient());
		order.setCareSetting(new CareSetting());
		Drug drug = new Drug();
		drug.setConcept(new Concept());
		order.setDrug(drug);
		order.setOrderType(new OrderType());
		
		DrugOrder dcOrder = (DrugOrder) order.cloneForDiscontinuing();
		
		assertEquals(order.getDrug(), dcOrder.getDrug());
		
		assertEquals(order.getPatient(), dcOrder.getPatient());
		
		assertEquals(order.getConcept(), dcOrder.getConcept());
		
		assertEquals("should set previous order to anOrder", order, dcOrder.getPreviousOrder());
		
		assertEquals("should set new order action to new", dcOrder.getAction(), Order.Action.DISCONTINUE);
		
		assertEquals(order.getCareSetting(), dcOrder.getCareSetting());
		
		assertEquals(order.getOrderType(), dcOrder.getOrderType());
	}
	
	/**
	 * @verifies copy all drug order fields
	 * @see DrugOrder#copy()
	 */
	@Test
	public void copy_shouldCopyAllDrugOrderFields() throws Exception {
		DrugOrder drugOrder = new DrugOrder();
		Drug drug = new Drug();
		drug.setConcept(new Concept());
		drugOrder.setDrug(drug);
		
		OrderTest.assertThatAllFieldsAreCopied(drugOrder, null);
		
	}
	
	/**
	 * @verifies set all the relevant fields
	 * @see DrugOrder#cloneForRevision()
	 */
	@Test
	public void cloneForRevision_shouldSetAllTheRelevantFields() throws Exception {
		DrugOrder drugOrder = new DrugOrder();
		Drug drug = new Drug();
		drug.setConcept(new Concept());
		drugOrder.setDrug(drug);
		OrderTest.assertThatAllFieldsAreCopied(drugOrder, "cloneForRevision", "creator", "dateCreated", "action",
		    "changedBy", "dateChanged", "voided", "dateVoided", "voidedBy", "voidReason", "encounter", "orderNumber",
		    "orderer", "previousOrder", "startDate", "dateStopped", "accessionNumber");
	}
	
	/**
	 * @verifies set the relevant fields for a DC order
	 * @see DrugOrder#cloneForRevision()
	 */
	@Test
	public void cloneForRevision_shouldSetTheRelevantFieldsForADCOrder() throws Exception {
		Order order = new DrugOrder();
		order.setAction(Order.Action.DISCONTINUE);
		Date date = new Date();
		order.setStartDate(date);
		order.setAutoExpireDate(date);
		order.setAccessionNumber("some number");
		OrderUtilTest.setDateStopped(order, date);
		order.setPreviousOrder(new Order());
		
		Order clone = order.cloneForRevision();
		assertEquals(Order.Action.DISCONTINUE, clone.getAction());
		assertEquals(order.getStartDate(), clone.getStartDate());
		assertEquals(order.getPreviousOrder(), clone.getPreviousOrder());
		assertNull(clone.getAutoExpireDate());
		assertNull(clone.getDateStopped());
		assertNull(clone.getAccessionNumber());
	}


    /**
     * @verifies false if the concept of the orders do not match
     * @see DrugOrder#hasSameOrderableAs(Order)
     */
	@Test
	public void hasSameOrderableAs_shouldBeFalseIfConceptsDoNotMatch() {
		DrugOrder order = new DrugOrder();
		Drug drug1 = new Drug();
		drug1.setConcept(new Concept(123));
		order.setDrug(drug1);

		DrugOrder otherOrder = new DrugOrder();
		Drug drug2 = new Drug();
		drug2.setConcept(new Concept(456));
		otherOrder.setDrug(drug2);

		assertFalse(order.hasSameOrderableAs(otherOrder));
	}

    /**
     * @verifies false if other order is not a drug order
     * @see DrugOrder#hasSameOrderableAs(Order)
     */
	@Test
	public void hasSameOrderableAs_shouldBeFalseIfOtherOrderIsNotADrugOrder() {
		DrugOrder order = new DrugOrder();
		Drug drug1 = new Drug();
		Concept concept = new Concept(123);
		drug1.setConcept(concept);
		order.setDrug(drug1);

		Order otherOrder = new Order();
		otherOrder.setConcept(concept);

		assertFalse(order.hasSameOrderableAs(otherOrder));
	}

    /**
     * @verifies false if other order is null
     * @see DrugOrder#hasSameOrderableAs(Order)
     */
	@Test
	public void hasSameOrderableAs_shouldBeFalseIfOtherOrderIsNull() {
		DrugOrder order = new DrugOrder();
		order.setConcept(new Concept(123));

		assertFalse(order.hasSameOrderableAs(null));
	}

    /**
     * @verifies false if drugs are different but have same concept
     * @see DrugOrder#hasSameOrderableAs(Order)
     */
	@Test
	public void hasSameOrderableAs_shouldBeFalseIfDrugsAreDifferentButConceptsMatch() {
		DrugOrder order = new DrugOrder();
		Concept concept = new Concept();
		Drug drug1 = new Drug();
		drug1.setConcept(concept);
		order.setDrug(drug1);

		DrugOrder otherOrder = new DrugOrder();
		Drug drug2 = new Drug();
		drug2.setConcept(concept);
		otherOrder.setDrug(drug2);

		assertFalse(order.hasSameOrderableAs(otherOrder));
	}

    /**
     * @verifies true if the drug of the orders match
     * @see DrugOrder#hasSameOrderableAs(Order)
     */
	@Test
	public void hasSameOrderableAs_shouldBeTrueIfDrugsMatch() {
		DrugOrder order = new DrugOrder();
		Concept concept = new Concept();
		Drug drug1 = new Drug();
		drug1.setConcept(concept);
		order.setDrug(drug1);

		DrugOrder otherOrder = new DrugOrder();
		otherOrder.setDrug(drug1);

		assertTrue(order.hasSameOrderableAs(otherOrder));
	}
}
