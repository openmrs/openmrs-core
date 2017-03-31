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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.Date;

import org.junit.Test;
import org.openmrs.order.OrderUtilTest;

/**
 * Contains tests for DrugOrder
 */
public class DrugOrderTest {
	
	/**
	 * @see DrugOrder#cloneForDiscontinuing()
	 */
	@Test
	public void cloneForDiscontinuing_shouldSetAllTheRelevantFields() {
		DrugOrder order = new DrugOrder();
		order.setPatient(new Patient());
		order.setCareSetting(new CareSetting());
		Drug drug = new Drug();
		drug.setConcept(new Concept());
		order.setDrug(drug);
		order.setOrderType(new OrderType());
		
		DrugOrder dcOrder = order.cloneForDiscontinuing();
		
		assertEquals(order.getDrug(), dcOrder.getDrug());
		
		assertEquals(order.getPatient(), dcOrder.getPatient());
		
		assertEquals(order.getConcept(), dcOrder.getConcept());
		
		assertEquals("should set previous order to anOrder", order, dcOrder.getPreviousOrder());
		
		assertEquals("should set new order action to new", dcOrder.getAction(), Order.Action.DISCONTINUE);
		
		assertEquals(order.getCareSetting(), dcOrder.getCareSetting());
		
		assertEquals(order.getOrderType(), dcOrder.getOrderType());
	}
	
	/**
	 * @throws Exception
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
	 * @throws Exception
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
		    "orderer", "previousOrder", "dateActivated", "dateStopped", "accessionNumber");
	}
	
	/**
	 * @throws Exception
	 * @see DrugOrder#cloneForRevision()
	 */
	@Test
	public void cloneForRevision_shouldSetTheRelevantFieldsForADCOrder() {
		Order order = new DrugOrder();
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
	
	/**
	 * @see DrugOrder#hasSameOrderableAs(Order)
	 */
	@Test
	public void hasSameOrderableAs_shouldReturnFalseIfTheOtherOrderIsNull() {
		DrugOrder order = new DrugOrder();
		order.setConcept(new Concept());
		
		assertFalse(order.hasSameOrderableAs(null));
	}
	
	/**
	 * @see DrugOrder#hasSameOrderableAs(Order)
	 */
	@Test
	public void hasSameOrderableAs_shouldReturnFalseIfTheOtherOrderIsNotADrugOrder() {
		DrugOrder order = new DrugOrder();
		Drug drug1 = new Drug();
		Concept concept = new Concept();
		drug1.setConcept(concept);
		order.setDrug(drug1);
		
		Order otherOrder = new Order();
		otherOrder.setConcept(concept);
		
		assertFalse(order.hasSameOrderableAs(otherOrder));
	}
	
	/**
	 * @see DrugOrder#hasSameOrderableAs(Order)
	 */
	@Test
	public void hasSameOrderableAs_shouldReturnFalseIfBothDrugsAreNullAndTheConceptsAreDifferent() {
		DrugOrder order = new DrugOrder();
		order.setConcept(new Concept());
		
		DrugOrder otherOrder = new DrugOrder();
		otherOrder.setConcept(new Concept());
		
		assertFalse(order.hasSameOrderableAs(otherOrder));
	}
	
	/**
	 * @see DrugOrder#hasSameOrderableAs(Order)
	 */
	@Test
	public void hasSameOrderableAs_shouldReturnFalseIfTheConceptsMatchAndOnlyThisHasADrug() {
		DrugOrder order = new DrugOrder();
		Concept concept = new Concept();
		Drug drug1 = new Drug();
		drug1.setConcept(concept);
		order.setDrug(drug1);
		
		DrugOrder otherOrder = new DrugOrder();
		otherOrder.setConcept(concept);
		assertEquals(order.getConcept(), otherOrder.getConcept());//sanity check
		
		assertFalse(order.hasSameOrderableAs(otherOrder));
	}
	
	/**
	 * @see DrugOrder#hasSameOrderableAs(Order)
	 */
	@Test
	public void hasSameOrderableAs_shouldReturnFalseIfTheConceptsMatchAndOnlyTheOtherHasADrug() {
		DrugOrder order = new DrugOrder();
		Concept concept = new Concept();
		order.setConcept(concept);
		
		DrugOrder otherOrder = new DrugOrder();
		Drug drug1 = new Drug();
		drug1.setConcept(concept);
		otherOrder.setDrug(drug1); //should set the concept
		assertEquals(order.getConcept(), otherOrder.getConcept());//sanity check
		
		assertFalse(order.hasSameOrderableAs(otherOrder));
	}
	
	/**
	 * @see DrugOrder#hasSameOrderableAs(Order)
	 */
	@Test
	public void hasSameOrderableAs_shouldReturnFalseIfTheConceptsMatchAndDrugsAreDifferentAndNotNull() {
		DrugOrder order = new DrugOrder();
		Concept concept = new Concept();
		Drug drug1 = new Drug();
		drug1.setConcept(concept);
		order.setDrug(drug1); //should set concept
		
		DrugOrder otherOrder = new DrugOrder();
		Drug drug2 = new Drug();
		drug2.setConcept(concept);
		otherOrder.setDrug(drug2);
		//sanity check
		assertTrue(order.getConcept() != null && otherOrder.getConcept() != null);
		
		assertFalse(order.hasSameOrderableAs(otherOrder));
	}
	
	/**
	 * @see DrugOrder#hasSameOrderableAs(Order)
	 */
	@Test
	public void hasSameOrderableAs_shouldReturnTrueIfBothDrugsAreNullAndTheConceptsMatch() {
		DrugOrder order = new DrugOrder();
		Concept concept = new Concept();
		order.setConcept(concept);
		
		DrugOrder otherOrder = new DrugOrder();
		otherOrder.setConcept(concept);
		
		assertTrue(order.hasSameOrderableAs(otherOrder));
	}
	
	/**
	 * @see DrugOrder#hasSameOrderableAs(Order)
	 */
	@Test
	public void hasSameOrderableAs_shouldReturnTrueIfTheDrugsMatch() {
		DrugOrder order = new DrugOrder();
		Concept concept = new Concept();
		Drug drug1 = new Drug();
		drug1.setConcept(concept);
		order.setDrug(drug1);
		
		DrugOrder otherOrder = new DrugOrder();
		otherOrder.setDrug(drug1);
		
		assertTrue(order.hasSameOrderableAs(otherOrder));
	}
	
	@Test
	public void shouldSetDefaultDosingTypeToFreeText() {
		DrugOrder drugOrder = new DrugOrder();
		assertEquals(SimpleDosingInstructions.class, drugOrder.getDosingType());
	}
	
	@Test
	public void shouldAllowToSetCustomDosingTypes() {
		DrugOrder drugOrder = new DrugOrder();
		assertEquals(SimpleDosingInstructions.class, drugOrder.getDosingType());
		CustomDosingInstructions customDosingInstructions = new CustomDosingInstructions();
		drugOrder.setDosingType(customDosingInstructions.getClass());
		DosingInstructions dosingInstructionsObject = drugOrder.getDosingInstructionsInstance();
		assertEquals(customDosingInstructions.getClass(), dosingInstructionsObject.getClass());
		assertEquals(customDosingInstructions.getClass(), drugOrder.getDosingType());
	}
	
	/**
	 * @see DrugOrder#setAutoExpireDateBasedOnDuration()
	 */
	@Test
	public void setAutoExpireDateBasedOnDuration_shouldDelegateCalculationToDosingInstructions() {
		DrugOrder drugOrder = spy(new DrugOrder());
		drugOrder.setAutoExpireDate(null);
		DosingInstructions dosingInstructions = mock(DosingInstructions.class);
		when(drugOrder.getDosingInstructionsInstance()).thenReturn(dosingInstructions);
		Date expectedAutoExpireDate = new Date();
		when(dosingInstructions.getAutoExpireDate(drugOrder)).thenReturn(expectedAutoExpireDate);
		
		drugOrder.setAutoExpireDateBasedOnDuration();
		
		assertEquals(expectedAutoExpireDate, drugOrder.getAutoExpireDate());
	}
	
	/**
	 * @see DrugOrder#setAutoExpireDateBasedOnDuration()
	 */
	@Test
	public void setAutoExpireDateBasedOnDuration_shouldNotCalculateForDiscontinueAction() {
		DrugOrder drugOrder = new DrugOrder();
		drugOrder.setAction(Order.Action.DISCONTINUE);
		Date expectedAutoExpireDate = new Date();
		drugOrder.setAutoExpireDate(expectedAutoExpireDate);
		
		drugOrder.setAutoExpireDateBasedOnDuration();
		
		assertEquals(expectedAutoExpireDate, drugOrder.getAutoExpireDate());
	}
	
	/**
	 * @see DrugOrder#setAutoExpireDateBasedOnDuration()
	 */
	@Test
	public void setAutoExpireDateBasedOnDuration_shouldNotCalculateIfAutoExpireDateAlreadySet() {
		DrugOrder drugOrder = new DrugOrder();
		Date expectedAutoExpireDate = new Date();
		drugOrder.setAutoExpireDate(expectedAutoExpireDate);
		
		drugOrder.setAutoExpireDateBasedOnDuration();
		
		assertEquals(expectedAutoExpireDate, drugOrder.getAutoExpireDate());
	}

	/**
	 * @see DrugOrder#hasSameOrderableAs(Order)
	 */
	@Test
	public void hasSameOrderableAs_shouldReturnTrueIfTheConceptsMatchAndHaveSameDrugNonCoded() {
		DrugOrder order = new DrugOrder();
		Concept concept = new Concept();
		order.setConcept(concept);
		order.setDrugNonCoded("Chrocine");

		DrugOrder otherOrder = new DrugOrder();
		otherOrder.setConcept(concept);
		otherOrder.setDrugNonCoded("chrocine");

		assertTrue(order.hasSameOrderableAs(otherOrder));
	}

	/**
	 * @see DrugOrder#hasSameOrderableAs(Order)
	 */
	@Test
	public void hasSameOrderableAs_shouldReturnFalseIfTheConceptsMatchAndHaveDifferentDrugNonCoded() {
		DrugOrder order = new DrugOrder();
		Concept concept = new Concept();
		order.setConcept(concept);
		order.setDrugNonCoded("Chrocine");

		DrugOrder otherOrder = new DrugOrder();
		otherOrder.setConcept(concept);
		otherOrder.setDrugNonCoded("paracetemol");

		assertFalse(order.hasSameOrderableAs(otherOrder));
	}

	/**
	 * @see DrugOrder#hasSameOrderableAs(Order)
	 */
	@Test
	public void hasSameOrderableAs_shouldReturnTrueIfTheConceptsMatchAndHaveSameDrugNonCodedTrimmingSpaces() {
		DrugOrder order = new DrugOrder();
		Concept concept = new Concept();
		order.setConcept(concept);
		order.setDrugNonCoded("Chrocine");

		DrugOrder otherOrder = new DrugOrder();
		otherOrder.setConcept(concept);
		otherOrder.setDrugNonCoded(" Chrocine ");

		assertTrue(order.hasSameOrderableAs(otherOrder));
	}

	/**
	 * @see DrugOrder#hasSameOrderableAs(Order)
	 */
	@Test
	public void hasSameOrderableAs_shouldReturnFalseIfTheConceptsMatchAndEitherOfDrugNonCodedIsNull() {
		DrugOrder order = new DrugOrder();
		Concept concept = new Concept();
		order.setConcept(concept);
		order.setDrugNonCoded("Chrocine");

		DrugOrder otherOrder = new DrugOrder();
		otherOrder.setConcept(concept);

		assertFalse(order.hasSameOrderableAs(otherOrder));
	}

	/**
	 * @see DrugOrder#hasSameOrderableAs(Order)
	 */
	@Test
	public void hasSameOrderableAs_shouldReturnTrueIfTheConceptsMatchAndBothDrugNonCodedIsNull() {
		DrugOrder order = new DrugOrder();
		Concept concept = new Concept();
		order.setConcept(concept);

		DrugOrder otherOrder = new DrugOrder();
		otherOrder.setConcept(concept);

		assertTrue(order.hasSameOrderableAs(otherOrder));
	}
}
