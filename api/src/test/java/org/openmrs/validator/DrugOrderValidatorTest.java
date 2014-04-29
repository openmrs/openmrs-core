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
package org.openmrs.validator;

import static org.junit.Assert.assertTrue;

import java.util.Calendar;
import java.util.Date;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.Concept;
import org.openmrs.ConceptClass;
import org.openmrs.Drug;
import org.openmrs.DrugOrder;
import org.openmrs.Encounter;
import org.openmrs.Order;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.order.OrderUtilTest;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.test.Verifies;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;

/**
 * Tests methods on the {@link DrugOrderValidator} class.
 */
public class DrugOrderValidatorTest extends BaseContextSensitiveTest {
	
	/**
	 * @see {@link DrugOrderValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should fail validation if asNeeded is null", method = "validate(Object,Errors)")
	public void validate_shouldFailValidationIfAsNeededIsNull() throws Exception {
		DrugOrder order = new DrugOrder();
		order.setAsNeeded(null);
		order.setDrug(Context.getConceptService().getDrug(3));
		
		Errors errors = new BindException(order, "order");
		new DrugOrderValidator().validate(order, errors);
		
		Assert.assertTrue(errors.hasFieldErrors("asNeeded"));
	}
	
	/**
	 * @see {@link DrugOrderValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should fail validation if dosingType is null", method = "validate(Object,Errors)")
	public void validate_shouldFailValidationIfDosingTypeIsNull() throws Exception {
		DrugOrder order = new DrugOrder();
		order.setDosingType(null);
		order.setDrug(Context.getConceptService().getDrug(3));
		
		Errors errors = new BindException(order, "order");
		new DrugOrderValidator().validate(order, errors);
		
		Assert.assertTrue(errors.hasFieldErrors("dosingType"));
	}
	
	/**
	 * @see {@link DrugOrderValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should not fail validation if drug is null", method = "validate(Object,Errors)")
	public void validate_shouldNotFailValidationIfDrugIsNull() throws Exception {
		DrugOrder order = new DrugOrder();
		order.setDrug(null);
		
		Errors errors = new BindException(order, "order");
		new DrugOrderValidator().validate(order, errors);
		
		Assert.assertFalse(errors.hasFieldErrors("drug"));
	}
	
	/**
	 * @see {@link DrugOrderValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should pass validation if all fields are correct", method = "validate(Object,Errors)")
	public void validate_shouldPassValidationIfAllFieldsAreCorrect() throws Exception {
		DrugOrder order = new DrugOrder();
		Encounter encounter = new Encounter();
		Patient patient = Context.getPatientService().getPatient(2);
		order.setConcept(Context.getConceptService().getConcept(88));
		order.setOrderer(Context.getProviderService().getProvider(1));
		order.setDosingType(DrugOrder.DosingType.FREE_TEXT);
		order.setInstructions("Instructions");
		order.setDosingInstructions("Test Instruction");
		order.setPatient(patient);
		encounter.setPatient(patient);
		order.setEncounter(encounter);
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH) - 1);
		order.setStartDate(cal.getTime());
		order.setAutoExpireDate(new Date());
		order.setOrderType(Context.getOrderService().getOrderTypeByName("Drug order"));
		order.setDrug(Context.getConceptService().getDrug(3));
		order.setCareSetting(Context.getOrderService().getCareSetting(1));
		order.setQuantity(2.00);
		order.setQuantityUnits(Context.getConceptService().getConcept(50));
		order.setNumRefills(10);
		
		Errors errors = new BindException(order, "order");
		new DrugOrderValidator().validate(order, errors);
		Assert.assertFalse(errors.hasErrors());
	}
	
	/**
	 * @verifies fail validation if quantity is null for outpatient careSetting
	 * @see DrugOrderValidator#validate(Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfQuantityIsNullForOutpatientCareSetting() throws Exception {
		DrugOrder OutpatientOrder = new DrugOrder();
		OutpatientOrder.setCareSetting(Context.getOrderService().getCareSetting(1));
		OutpatientOrder.setQuantity(null);
		Errors OutpatientOrderErrors = new BindException(OutpatientOrder, "order");
		new DrugOrderValidator().validate(OutpatientOrder, OutpatientOrderErrors);
		Assert.assertTrue(OutpatientOrderErrors.hasFieldErrors("quantity"));
		
		DrugOrder inPatientOrder = new DrugOrder();
		inPatientOrder.setCareSetting(Context.getOrderService().getCareSetting(2));
		inPatientOrder.setQuantity(null);
		Errors InpatientOrderErrors = new BindException(inPatientOrder, "order");
		new DrugOrderValidator().validate(inPatientOrder, InpatientOrderErrors);
		Assert.assertFalse(InpatientOrderErrors.hasFieldErrors("quantity"));
	}
	
	/**
	 * @verifies fail validation if numberOfRefills is null for outpatient careSetting
	 * @see DrugOrderValidator#validate(Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfNumberOfRefillsIsNullForOutpatientCareSetting() throws Exception {
		DrugOrder OutpatientOrder = new DrugOrder();
		OutpatientOrder.setCareSetting(Context.getOrderService().getCareSetting(1));
		OutpatientOrder.setNumRefills(null);
		Errors OutpatientOrderErrors = new BindException(OutpatientOrder, "order");
		new DrugOrderValidator().validate(OutpatientOrder, OutpatientOrderErrors);
		Assert.assertTrue(OutpatientOrderErrors.hasFieldErrors("numRefills"));
		
		DrugOrder inPatientOrder = new DrugOrder();
		inPatientOrder.setCareSetting(Context.getOrderService().getCareSetting(2));
		inPatientOrder.setNumRefills(null);
		Errors InpatientOrderErrors = new BindException(inPatientOrder, "order");
		new DrugOrderValidator().validate(inPatientOrder, InpatientOrderErrors);
		Assert.assertFalse(InpatientOrderErrors.hasFieldErrors("numRefills"));
	}
	
	/**
	 * @verifies fail validation if dose is null for SIMPLE dosingType
	 * @see DrugOrderValidator#validate(Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfDoseIsNullForSIMPLEDosingType() throws Exception {
		DrugOrder order = new DrugOrder();
		order.setDosingType(DrugOrder.DosingType.SIMPLE);
		order.setDose(null);
		Errors errors = new BindException(order, "order");
		new DrugOrderValidator().validate(order, errors);
		Assert.assertTrue(errors.hasFieldErrors("dose"));
	}
	
	/**
	 * @verifies fail validation if doseUnits is null for SIMPLE dosingType
	 * @see DrugOrderValidator#validate(Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfDoseUnitsIsNullForSIMPLEDosingType() throws Exception {
		DrugOrder order = new DrugOrder();
		order.setDosingType(DrugOrder.DosingType.SIMPLE);
		order.setDoseUnits(null);
		Errors errors = new BindException(order, "order");
		new DrugOrderValidator().validate(order, errors);
		Assert.assertTrue(errors.hasFieldErrors("doseUnits"));
	}
	
	/**
	 * @verifies fail validation if route is null for SIMPLE dosingType
	 * @see DrugOrderValidator#validate(Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfRouteIsNullForSIMPLEDosingType() throws Exception {
		DrugOrder order = new DrugOrder();
		order.setDosingType(DrugOrder.DosingType.SIMPLE);
		order.setRoute(null);
		Errors errors = new BindException(order, "order");
		new DrugOrderValidator().validate(order, errors);
		Assert.assertTrue(errors.hasFieldErrors("route"));
	}
	
	/**
	 * @verifies fail validation if frequency is null for SIMPLE dosingType
	 * @see DrugOrderValidator#validate(Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfFrequencyIsNullForSIMPLEDosingType() throws Exception {
		DrugOrder order = new DrugOrder();
		order.setDosingType(DrugOrder.DosingType.SIMPLE);
		order.setFrequency(null);
		Errors errors = new BindException(order, "order");
		new DrugOrderValidator().validate(order, errors);
		Assert.assertTrue(errors.hasFieldErrors("frequency"));
	}
	
	/**
	 * @verifies fail validation if instruction is null for FREE_TEXT dosingType
	 * @see DrugOrderValidator#validate(Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfDosingInstructionIsNullForFREE_TEXTDosingType() throws Exception {
		DrugOrder order = new DrugOrder();
		order.setDosingType(DrugOrder.DosingType.FREE_TEXT);
		order.setDosingInstructions(null);
		Errors errors = new BindException(order, "order");
		new DrugOrderValidator().validate(order, errors);
		Assert.assertTrue(errors.hasFieldErrors("dosingInstructions"));
	}
	
	/**
	 * @verifies fail validation if doseUnits is null when dose is present
	 * @see DrugOrderValidator#validate(Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfDoseUnitsIsNullWhenDoseIsPresent() throws Exception {
		DrugOrder order = new DrugOrder();
		order.setDosingType(DrugOrder.DosingType.FREE_TEXT);
		order.setDose(20.0);
		order.setDoseUnits(null);
		Errors errors = new BindException(order, "order");
		new DrugOrderValidator().validate(order, errors);
		Assert.assertTrue(errors.hasFieldErrors("doseUnits"));
	}
	
	/**
	 * @verifies fail validation if quantityUnits is null when quantity is present
	 * @see DrugOrderValidator#validate(Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfQuantityUnitsIsNullWhenQuantityIsPresent() throws Exception {
		DrugOrder order = new DrugOrder();
		order.setDosingType(DrugOrder.DosingType.FREE_TEXT);
		order.setQuantity(20.0);
		order.setQuantityUnits(null);
		Errors errors = new BindException(order, "order");
		new DrugOrderValidator().validate(order, errors);
		Assert.assertTrue(errors.hasFieldErrors("quantityUnits"));
	}
	
	/**
	 * @verifies fail validation if durationUnits is null when duration is present
	 * @see DrugOrderValidator#validate(Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfDurationUnitsIsNullWhenDurationIsPresent() throws Exception {
		DrugOrder order = new DrugOrder();
		order.setDosingType(DrugOrder.DosingType.FREE_TEXT);
		order.setDuration(20.0);
		order.setDurationUnits(null);
		Errors errors = new BindException(order, "order");
		new DrugOrderValidator().validate(order, errors);
		Assert.assertTrue(errors.hasFieldErrors("durationUnits"));
	}
	
	/**
	 * @see {@link DrugOrderValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should fail validation if drug concept is different from order concept", method = "validate(Object,Errors)")
	public void validate_shouldFailValidationIfDrugConceptIsDifferentFromOrderConcept() throws Exception {
		DrugOrder order = new DrugOrder();
		Drug drug = Context.getConceptService().getDrug(3);
		Concept concept = Context.getConceptService().getConcept(792);
		order.setDrug(drug);
		order.setConcept(concept); // the actual concept which matches with drug is "88"
		Assert.assertNotEquals(drug.getConcept(), concept);
		
		Errors errors = new BindException(order, "order");
		new DrugOrderValidator().validate(order, errors);
		
		Assert.assertTrue(errors.hasFieldErrors("concept"));
		Assert.assertTrue(errors.hasFieldErrors("drug"));
	}
	
	/**
	 * @verifies fail validation if class of quantityUnits,doseUnits or durationUnits is not Units
	 *           of Measure Concept class
	 * @see DrugOrderValidator#validate(Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfClassOfQuantityUnitsdoseUnitsOrDurationUnitsIsNotUnitsOfMeasureConceptClass()
	        throws Exception {
		Concept concept = Context.getConceptService().getConcept(3);
		Assert.assertFalse(concept.getConceptClass().getUuid().equals(ConceptClass.UNIT_OF_MEASUREMENT_UUID));
		
		DrugOrder order = new DrugOrder();
		order.setDosingType(DrugOrder.DosingType.FREE_TEXT);
		order.setDuration(5.0);
		order.setDurationUnits(concept);
		order.setDose(1.0);
		order.setDoseUnits(concept);
		order.setQuantity(30.0);
		order.setQuantityUnits(concept);
		
		Errors errors = new BindException(order, "order");
		new DrugOrderValidator().validate(order, errors);
		Assert.assertTrue(errors.hasFieldErrors("doseUnits"));
		Assert.assertTrue(errors.hasFieldErrors("quantityUnits"));
		Assert.assertTrue(errors.hasFieldErrors("durationUnits"));
	}
	
	/**
	 * @verifies not require all fields for a discontinuation order
	 * @see DrugOrderValidator#validate(Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldNotRequireAllFieldsForADiscontinuationOrder() throws Exception {
		DrugOrder orderToDiscontinue = (DrugOrder) Context.getOrderService().getOrder(111);
		assertTrue(OrderUtilTest.isActiveOrder(orderToDiscontinue, null));
		DrugOrder discontinuationOrder = new DrugOrder();
		discontinuationOrder.setCareSetting(orderToDiscontinue.getCareSetting());
		discontinuationOrder.setConcept(orderToDiscontinue.getConcept());
		discontinuationOrder.setAction(Order.Action.DISCONTINUE);
		discontinuationOrder.setPreviousOrder(orderToDiscontinue);
		discontinuationOrder.setPatient(orderToDiscontinue.getPatient());
		discontinuationOrder.setDrug(orderToDiscontinue.getDrug());
		discontinuationOrder.setOrderType(orderToDiscontinue.getOrderType());
		discontinuationOrder.setOrderer(Context.getProviderService().getProvider(1));
		discontinuationOrder.setEncounter(Context.getEncounterService().getEncounter(3));
		
		Errors errors = new BindException(discontinuationOrder, "order");
		new DrugOrderValidator().validate(discontinuationOrder, errors);
		Assert.assertFalse(errors.hasErrors());
	}
}
