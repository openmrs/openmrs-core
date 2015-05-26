/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.validator;

import static org.hamcrest.Matchers.isIn;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Calendar;
import java.util.Date;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.CareSetting;
import org.openmrs.Concept;
import org.openmrs.CustomDosingInstructions;
import org.openmrs.Drug;
import org.openmrs.DrugOrder;
import org.openmrs.Encounter;
import org.openmrs.FreeTextDosingInstructions;
import org.openmrs.GlobalProperty;
import org.openmrs.Order;
import org.openmrs.OrderType;
import org.openmrs.Patient;
import org.openmrs.SimpleDosingInstructions;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.openmrs.order.OrderUtilTest;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.test.Verifies;
import org.openmrs.util.OpenmrsConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;

/**
 * Tests methods on the {@link DrugOrderValidator} class.
 */
public class DrugOrderValidatorTest extends BaseContextSensitiveTest {
	
	@Autowired
	@Qualifier("adminService")
	AdministrationService adminService;
	
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
		order.setDosingType(FreeTextDosingInstructions.class);
		order.setInstructions("Instructions");
		order.setDosingInstructions("Test Instruction");
		order.setPatient(patient);
		encounter.setPatient(patient);
		order.setEncounter(encounter);
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH) - 1);
		order.setDateActivated(cal.getTime());
		order.setAutoExpireDate(new Date());
		order.setOrderType(Context.getOrderService().getOrderTypeByName("Drug order"));
		order.setDrug(Context.getConceptService().getDrug(3));
		order.setCareSetting(Context.getOrderService().getCareSetting(1));
		order.setQuantity(2.00);
		order.setQuantityUnits(Context.getConceptService().getConcept(51));
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
	 * @verifies fail validation if dose is null for SimpleDosingInstructions dosingType
	 * @see DrugOrderValidator#validate(Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfDoseIsNullForSimpleDosingInstructionsDosingType() throws Exception {
		DrugOrder order = new DrugOrder();
		order.setDosingType(SimpleDosingInstructions.class);
		order.setDose(null);
		Errors errors = new BindException(order, "order");
		new DrugOrderValidator().validate(order, errors);
		Assert.assertTrue(errors.hasFieldErrors("dose"));
	}
	
	/**
	 * @verifies fail validation if doseUnits is null for SimpleDosingInstructions dosingType
	 * @see DrugOrderValidator#validate(Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfDoseUnitsIsNullForSimpleDosingInstructionsDosingType() throws Exception {
		DrugOrder order = new DrugOrder();
		order.setDosingType(SimpleDosingInstructions.class);
		order.setDoseUnits(null);
		Errors errors = new BindException(order, "order");
		new DrugOrderValidator().validate(order, errors);
		Assert.assertTrue(errors.hasFieldErrors("doseUnits"));
	}
	
	/**
	 * @verifies fail validation if route is null for SimpleDosingInstructions dosingType
	 * @see DrugOrderValidator#validate(Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfRouteIsNullForSimpleDosingInstructionsDosingType() throws Exception {
		DrugOrder order = new DrugOrder();
		order.setDosingType(SimpleDosingInstructions.class);
		order.setRoute(null);
		Errors errors = new BindException(order, "order");
		new DrugOrderValidator().validate(order, errors);
		Assert.assertTrue(errors.hasFieldErrors("route"));
	}
	
	/**
	 * @verifies fail validation if frequency is null for SimpleDosingInstructions dosingType
	 * @see DrugOrderValidator#validate(Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfFrequencyIsNullForSimpleDosingInstructionsDosingType() throws Exception {
		DrugOrder order = new DrugOrder();
		order.setDosingType(SimpleDosingInstructions.class);
		order.setFrequency(null);
		Errors errors = new BindException(order, "order");
		new DrugOrderValidator().validate(order, errors);
		Assert.assertTrue(errors.hasFieldErrors("frequency"));
	}
	
	/**
	 * @verifies fail validation if dosingInstructions is null for FreeTextDosingInstructions
	 *           dosingType
	 * @see DrugOrderValidator#validate(Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfDosingInstructionsIsNullForFreeTextDosingInstructionsDosingType()
	        throws Exception {
		DrugOrder order = new DrugOrder();
		order.setDosingType(FreeTextDosingInstructions.class);
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
		order.setDosingType(FreeTextDosingInstructions.class);
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
		order.setDosingType(FreeTextDosingInstructions.class);
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
		order.setDosingType(FreeTextDosingInstructions.class);
		order.setDuration(20);
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
	 * @verifies not require all fields for a discontinuation order
	 * @see DrugOrderValidator#validate(Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldNotRequireAllFieldsForADiscontinuationOrder() throws Exception {
		DrugOrder orderToDiscontinue = (DrugOrder) Context.getOrderService().getOrder(111);
		assertTrue(OrderUtilTest.isActiveOrder(orderToDiscontinue, null));
		DrugOrder discontinuationOrder = new DrugOrder();
		discontinuationOrder.setDosingType(null);
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
	
	/**
	 * @see DrugOrderValidator#validate(Object,Errors)
	 * @verifies fail validation if doseUnits is not a dose unit concept
	 */
	@Test
	public void validate_shouldFailValidationIfDoseUnitsIsNotADoseUnitConcept() throws Exception {
		Concept concept = Context.getConceptService().getConcept(3);
		assertThat(concept, not(isIn(Context.getOrderService().getDrugDosingUnits())));
		
		DrugOrder order = new DrugOrder();
		order.setDosingType(FreeTextDosingInstructions.class);
		order.setDuration(5);
		order.setDurationUnits(concept);
		order.setDose(1.0);
		order.setDoseUnits(concept);
		order.setQuantity(30.0);
		order.setQuantityUnits(concept);
		
		Errors errors = new BindException(order, "order");
		new DrugOrderValidator().validate(order, errors);
		Assert.assertTrue(errors.hasFieldErrors("doseUnits"));
	}
	
	/**
	 * @see DrugOrderValidator#validate(Object,Errors)
	 * @verifies fail validation if quantityUnits it not a quantity unit concept
	 */
	@Test
	public void validate_shouldFailValidationIfQuantityUnitsItNotAQuantityUnitConcept() throws Exception {
		Concept concept = Context.getConceptService().getConcept(3);
		assertThat(concept, not(isIn(Context.getOrderService().getDrugDispensingUnits())));
		
		DrugOrder order = new DrugOrder();
		order.setDosingType(FreeTextDosingInstructions.class);
		order.setDuration(5);
		order.setDurationUnits(concept);
		order.setDose(1.0);
		order.setDoseUnits(concept);
		order.setQuantity(30.0);
		order.setQuantityUnits(concept);
		
		Errors errors = new BindException(order, "order");
		new DrugOrderValidator().validate(order, errors);
		Assert.assertTrue(errors.hasFieldErrors("quantityUnits"));
	}
	
	/**
	 * @see DrugOrderValidator#validate(Object,Errors)
	 * @verifies fail validation if durationUnits is not a duration unit concept
	 */
	@Test
	public void validate_shouldFailValidationIfDurationUnitsIsNotADurationUnitConcept() throws Exception {
		Concept concept = Context.getConceptService().getConcept(3);
		assertThat(concept, not(isIn(Context.getOrderService().getDurationUnits())));
		
		DrugOrder order = new DrugOrder();
		order.setDosingType(FreeTextDosingInstructions.class);
		order.setDuration(5);
		order.setDurationUnits(concept);
		order.setDose(1.0);
		order.setDoseUnits(concept);
		order.setQuantity(30.0);
		order.setQuantityUnits(concept);
		
		Errors errors = new BindException(order, "order");
		new DrugOrderValidator().validate(order, errors);
		Assert.assertTrue(errors.hasFieldErrors("durationUnits"));
	}
	
	/**
	 * @verifies fail if route is not a valid concept
	 * @see DrugOrderValidator#validate(Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldFailIfRouteIsNotAValidConcept() throws Exception {
		Concept concept = Context.getConceptService().getConcept(3);
		assertThat(concept, not(isIn(Context.getOrderService().getDrugRoutes())));
		
		DrugOrder order = new DrugOrder();
		order.setDosingType(FreeTextDosingInstructions.class);
		order.setDuration(5);
		order.setDurationUnits(concept);
		order.setDose(1.0);
		order.setDoseUnits(concept);
		order.setQuantity(30.0);
		order.setQuantityUnits(concept);
		order.setRoute(concept);
		
		Errors errors = new BindException(order, "order");
		new DrugOrderValidator().validate(order, errors);
		Assert.assertTrue(errors.hasFieldErrors("route"));
		Assert.assertEquals("DrugOrder.error.routeNotAmongAllowedConcepts", errors.getFieldError("route").getCode());
	}
	
	/**
	 * @verifies fail if concept is null and drug is not specified
	 * @see DrugOrderValidator#validate(Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldFailIfConceptIsNullAndDrugIsNotSpecified() throws Exception {
		DrugOrder order = new DrugOrder();
		
		Errors errors = new BindException(order, "order");
		new DrugOrderValidator().validate(order, errors);
		
		Assert.assertTrue(errors.hasFieldErrors("concept"));
	}
	
	/**
	 * @verifies fail if concept is null and cannot infer it from drug
	 * @see DrugOrderValidator#validate(Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldFailIfConceptIsNullAndCannotInferItFromDrug() throws Exception {
		DrugOrder order = new DrugOrder();
		Drug drug = Context.getConceptService().getDrug(3);
		drug.setConcept(null);
		order.setDrug(drug);
		
		Errors errors = new BindException(order, "order");
		adminService.validate(order, errors);
		Assert.assertTrue(errors.hasFieldErrors("concept"));
	}
	
	/**
	 * @verifies pass if concept is null and drug is set
	 * @see DrugOrderValidator#validate(Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldPassIfConceptIsNullAndDrugIsSet() throws Exception {
		DrugOrder order = new DrugOrder();
		order.setPatient(Context.getPatientService().getPatient(7));
		order.setCareSetting(Context.getOrderService().getCareSetting(2));
		order.setEncounter(Context.getEncounterService().getEncounter(3));
		order.setOrderer(Context.getProviderService().getProvider(1));
		Drug drug = Context.getConceptService().getDrug(3);
		order.setDrug(drug);
		order.setConcept(null);
		FreeTextDosingInstructions di = new FreeTextDosingInstructions();
		di.setInstructions("testing");
		di.setDosingInstructions(order);
		
		Errors errors = new BindException(order, "order");
		new DrugOrderValidator().validate(order, errors);
		
		Assert.assertFalse(errors.hasFieldErrors());
	}
	
	/**
	 * @verifies apply validation for a custom dosing type
	 * @see DrugOrderValidator#validate(Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldApplyValidationForACustomDosingType() throws Exception {
		DrugOrder order = new DrugOrder();
		Encounter encounter = new Encounter();
		Patient patient = Context.getPatientService().getPatient(2);
		order.setConcept(Context.getConceptService().getConcept(88));
		order.setOrderer(Context.getProviderService().getProvider(1));
		order.setDosingType(CustomDosingInstructions.class);
		order.setInstructions("Instructions");
		order.setPatient(patient);
		encounter.setPatient(patient);
		order.setEncounter(encounter);
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH) - 1);
		order.setDateActivated(cal.getTime());
		order.setAutoExpireDate(new Date());
		order.setOrderType(Context.getOrderService().getOrderTypeByName("Drug order"));
		order.setDrug(Context.getConceptService().getDrug(3));
		order.setCareSetting(Context.getOrderService().getCareSetting(1));
		order.setQuantity(2.00);
		order.setQuantityUnits(Context.getConceptService().getConcept(51));
		order.setNumRefills(10);
		
		Errors errors = new BindException(order, "order");
		new DrugOrderValidator().validate(order, errors);
		Assert.assertTrue(errors.hasFieldErrors("brandName"));
	}
	
	/**
	 * @verifies not validate a custom dosing type against any other dosing type validation
	 * @see DrugOrderValidator#validate(Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldNotValidateACustomDosingTypeAgainstAnyOtherDosingTypeValidation() throws Exception {
		DrugOrder order = new DrugOrder();
		order.setDosingType(CustomDosingInstructions.class);
		order.setDosingInstructions(null);
		Errors errors = new BindException(order, "order");
		new DrugOrderValidator().validate(order, errors);
		Assert.assertFalse(errors.hasFieldErrors("dosingInstructions"));
	}
	
	/**
	 * @verifies fail validation if GLOBAL_PROPERTY_DRUG_ORDER_REQUIRE_DRUG is true
	 * @see org.openmrs.api.OrderService#saveOrder(org.openmrs.Order, org.openmrs.api.OrderContext)
	 */
	@Test
	public void saveOrder_shouldFailDrugOrderWithoutADrugWhenDrugOrderRequireDrugGBIsTrue() throws Exception {
		GlobalProperty gp = new GlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_DRUG_ORDER_REQUIRE_DRUG, "true");
		Context.getAdministrationService().saveGlobalProperty(gp);
		
		Patient patient = Context.getPatientService().getPatient(7);
		CareSetting careSetting = Context.getOrderService().getCareSetting(2);
		OrderType orderType = Context.getOrderService().getOrderTypeByName("Drug order");
		
		//place drug order
		DrugOrder order = new DrugOrder();
		Encounter encounter = Context.getEncounterService().getEncounter(3);
		order.setEncounter(encounter);
		order.setConcept(Context.getConceptService().getConcept(5497));
		order.setPatient(patient);
		order.setCareSetting(careSetting);
		order.setOrderer(Context.getProviderService().getProvider(1));
		order.setDateActivated(encounter.getEncounterDatetime());
		order.setOrderType(orderType);
		order.setDosingType(FreeTextDosingInstructions.class);
		order.setInstructions("None");
		order.setDosingInstructions("Test Instruction");
		Errors errors = new BindException(order, "order");
		new DrugOrderValidator().validate(order, errors);
		Assert.assertTrue(errors.hasFieldErrors("drug"));
	}
	
	/**
	 * @verifies pass validation of Drug Order if formulation(Drug) not specified i.e
	 *           GLOBAL_PROPERTY_DRUG_ORDER_REQUIRE_DRUG is false
	 * @see org.openmrs.api.OrderService#saveOrder(org.openmrs.Order, org.openmrs.api.OrderContext)
	 */
	@Test
	public void saveOrder_shouldPassDrugOrderWithoutADrugWhenDrugOrderRequireDrugGBIsFalse() throws Exception {
		GlobalProperty gp = new GlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_DRUG_ORDER_REQUIRE_DRUG, "false");
		Context.getAdministrationService().saveGlobalProperty(gp);
		Patient patient = Context.getPatientService().getPatient(7);
		CareSetting careSetting = Context.getOrderService().getCareSetting(2);
		OrderType orderType = Context.getOrderService().getOrderTypeByName("Drug order");
		
		//place drug order
		DrugOrder order = new DrugOrder();
		Encounter encounter = Context.getEncounterService().getEncounter(3);
		order.setEncounter(encounter);
		order.setConcept(Context.getConceptService().getConcept(5497));
		order.setPatient(patient);
		order.setCareSetting(careSetting);
		order.setOrderer(Context.getProviderService().getProvider(1));
		order.setDateActivated(encounter.getEncounterDatetime());
		order.setOrderType(orderType);
		order.setDosingType(FreeTextDosingInstructions.class);
		order.setInstructions("None");
		order.setDosingInstructions("Test Instruction");
		Errors errors = new BindException(order, "order");
		new DrugOrderValidator().validate(order, errors);
		Assert.assertFalse(errors.hasFieldErrors());
	}
	
	/**
	 * @verifies default case pass saving Drug Order without a Drug if formulation(drug) not
	 *           specified i.e GLOBAL_PROPERTY_DRUG_ORDER_REQUIRE_DRUG is not set
	 * @see org.openmrs.api.OrderService#saveOrder(org.openmrs.Order, org.openmrs.api.OrderContext)
	 */
	@Test
	public void saveOrder_shouldPassDrugOrderWithoutADrugWhenDrugOrderRequireDrugGBIsNotSet() throws Exception {
		deleteAllData();
		baseSetupWithStandardDataAndAuthentication();
		Patient patient = Context.getPatientService().getPatient(7);
		CareSetting careSetting = Context.getOrderService().getCareSetting(2);
		OrderType orderType = Context.getOrderService().getOrderTypeByName("Drug order");
		assertNull(Context.getAdministrationService().getGlobalPropertyObject(
		    OpenmrsConstants.GLOBAL_PROPERTY_DRUG_ORDER_REQUIRE_DRUG));
		//place drug order
		DrugOrder order = new DrugOrder();
		Encounter encounter = Context.getEncounterService().getEncounter(3);
		order.setEncounter(encounter);
		order.setConcept(Context.getConceptService().getConcept(5497));
		order.setPatient(patient);
		order.setCareSetting(careSetting);
		order.setOrderer(Context.getProviderService().getProvider(1));
		order.setDateActivated(encounter.getEncounterDatetime());
		order.setOrderType(orderType);
		order.setDosingType(FreeTextDosingInstructions.class);
		order.setInstructions("None");
		order.setDosingInstructions("Test Instruction");
		Errors errors = new BindException(order, "order");
		new DrugOrderValidator().validate(order, errors);
		Assert.assertFalse(errors.hasFieldErrors());
	}
	
	/**
	 * @verifies fail if durationUnits has no mapping to SNOMEDCT source
	 * @see DrugOrderValidator#validate(Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldFailIfDurationUnitsHasNoMappingToSNOMEDCTSource() throws Exception {
		Patient patient = Context.getPatientService().getPatient(7);
		CareSetting careSetting = Context.getOrderService().getCareSetting(2);
		OrderType orderType = Context.getOrderService().getOrderTypeByName("Drug order");
		
		//place drug order
		DrugOrder order = new DrugOrder();
		Encounter encounter = Context.getEncounterService().getEncounter(3);
		order.setEncounter(encounter);
		ConceptService cs = Context.getConceptService();
		order.setConcept(cs.getConcept(5497));
		order.setPatient(patient);
		order.setCareSetting(careSetting);
		order.setOrderer(Context.getProviderService().getProvider(1));
		order.setDateActivated(encounter.getEncounterDatetime());
		order.setOrderType(orderType);
		order.setDosingType(FreeTextDosingInstructions.class);
		order.setInstructions("None");
		order.setDosingInstructions("Test Instruction");
		order.setDuration(20);
		order.setDurationUnits(cs.getConcept(28));
		Errors errors = new BindException(order, "order");
		new DrugOrderValidator().validate(order, errors);
		assertEquals("DrugOrder.error.durationUnitsNotMappedToSnomedCtDurationCode", errors.getFieldError("durationUnits")
		        .getCode());
	}
	
	/**
	 * @see {@link DrugOrderValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should pass validation if field lengths are correct", method = "validate(Object,Errors)")
	public void validate_shouldPassValidationIfFieldLengthsAreCorrect() throws Exception {
		DrugOrder order = new DrugOrder();
		Encounter encounter = new Encounter();
		Patient patient = Context.getPatientService().getPatient(2);
		order.setConcept(Context.getConceptService().getConcept(88));
		order.setOrderer(Context.getProviderService().getProvider(1));
		order.setDosingType(FreeTextDosingInstructions.class);
		order.setInstructions("Instructions");
		order.setDosingInstructions("Test Instruction");
		order.setPatient(patient);
		encounter.setPatient(patient);
		order.setEncounter(encounter);
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH) - 1);
		order.setDateActivated(cal.getTime());
		order.setAutoExpireDate(new Date());
		order.setOrderType(Context.getOrderService().getOrderTypeByName("Drug order"));
		order.setDrug(Context.getConceptService().getDrug(3));
		order.setCareSetting(Context.getOrderService().getCareSetting(1));
		order.setQuantity(2.00);
		order.setQuantityUnits(Context.getConceptService().getConcept(51));
		order.setNumRefills(10);
		
		order.setAsNeededCondition("asNeededCondition");
		order.setBrandName("brandName");
		
		Errors errors = new BindException(order, "order");
		new DrugOrderValidator().validate(order, errors);
		Assert.assertFalse(errors.hasErrors());
	}
	
	/**
	 * @see {@link DrugOrderValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should fail validation if field lengths are not correct", method = "validate(Object,Errors)")
	public void validate_shouldFailValidationIfFieldLengthsAreNotCorrect() throws Exception {
		DrugOrder order = new DrugOrder();
		Encounter encounter = new Encounter();
		Patient patient = Context.getPatientService().getPatient(2);
		order.setConcept(Context.getConceptService().getConcept(88));
		order.setOrderer(Context.getProviderService().getProvider(1));
		order.setDosingType(FreeTextDosingInstructions.class);
		order.setInstructions("Instructions");
		order.setDosingInstructions("Test Instruction");
		order.setPatient(patient);
		encounter.setPatient(patient);
		order.setEncounter(encounter);
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH) - 1);
		order.setDateActivated(cal.getTime());
		order.setAutoExpireDate(new Date());
		order.setOrderType(Context.getOrderService().getOrderTypeByName("Drug order"));
		order.setDrug(Context.getConceptService().getDrug(3));
		order.setCareSetting(Context.getOrderService().getCareSetting(1));
		order.setQuantity(2.00);
		order.setQuantityUnits(Context.getConceptService().getConcept(51));
		order.setNumRefills(10);
		
		order
		        .setAsNeededCondition("too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text");
		order
		        .setBrandName("too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text");
		
		Errors errors = new BindException(order, "order");
		new DrugOrderValidator().validate(order, errors);
		Assert.assertTrue(errors.hasFieldErrors("asNeededCondition"));
		Assert.assertTrue(errors.hasFieldErrors("brandName"));
	}
}
