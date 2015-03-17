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

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.time.DateUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.CareSetting;
import org.openmrs.DrugOrder;
import org.openmrs.Encounter;
import org.openmrs.Order;
import org.openmrs.Patient;
import org.openmrs.TestOrder;
import org.openmrs.api.OrderService;
import org.openmrs.api.context.Context;
import org.openmrs.order.OrderUtilTest;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.test.Verifies;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.validation.ObjectError;

/**
 * Tests methods on the {@link OrderValidator} class.
 */
public class OrderValidatorTest extends BaseContextSensitiveTest {
	
	private class SomeDrugOrder extends DrugOrder {}
	
	private OrderService orderService;
	
	@Before
	public void setup() {
		orderService = Context.getOrderService();
	}
	
	/**
	 * @verifies fail validation if order is null
	 * @see OrderValidator#validate(Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfOrderIsNull() throws Exception {
		Errors errors = new BindException(new Order(), "order");
		new OrderValidator().validate(null, errors);
		
		Assert.assertTrue(errors.hasErrors());
		Assert.assertEquals("error.general", ((List<ObjectError>) errors.getAllErrors()).get(0).getCode());
	}
	
	/**
	 * @see {@link OrderValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should fail validation if order and encounter have different patients", method = "validate(Object,Errors)")
	public void validate_shouldFailValidationIfOrderAndEncounterHaveDifferentPatients() throws Exception {
		Order order = new Order();
		order.setConcept(Context.getConceptService().getConcept(88));
		order.setPatient(Context.getPatientService().getPatient(2));
		order.setEncounter(Context.getEncounterService().getEncounter(3));
		order.setOrderer(Context.getProviderService().getProvider(1));
		Errors errors = new BindException(order, "order");
		new OrderValidator().validate(order, errors);
		
		Assert.assertTrue(errors.hasFieldErrors("encounter"));
	}
	
	/**
	 * @verifies fail validation if dateActivated is before encounter's encounterDatetime
	 * @see OrderValidator#validate(Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfDateActivatedIsBeforeEncountersEncounterDatetime() throws Exception {
		Date encounterDate = new Date();
		Date orderDate = DateUtils.addDays(encounterDate, -1);
		Encounter encounter = Context.getEncounterService().getEncounter(3);
		encounter.setEncounterDatetime(encounterDate);
		Order order = new Order();
		order.setDateActivated(orderDate);
		order.setConcept(Context.getConceptService().getConcept(88));
		order.setPatient(Context.getPatientService().getPatient(2));
		order.setEncounter(encounter);
		order.setOrderer(Context.getProviderService().getProvider(1));
		Errors errors = new BindException(order, "order");
		new OrderValidator().validate(order, errors);
		
		Assert.assertTrue(errors.hasFieldErrors("dateActivated"));
	}
	
	/**
	 * @see {@link OrderValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should fail validation if voided is null", method = "validate(Object,Errors)")
	public void validate_shouldFailValidationIfVoidedIsNull() throws Exception {
		Order order = new Order();
		order.setVoided(null);
		order.setConcept(Context.getConceptService().getConcept(88));
		order.setPatient(Context.getPatientService().getPatient(2));
		order.setOrderer(Context.getProviderService().getProvider(1));
		
		Errors errors = new BindException(order, "order");
		new OrderValidator().validate(order, errors);
		
		Assert.assertFalse(errors.hasFieldErrors("discontinued"));
		Assert.assertTrue(errors.hasFieldErrors("voided"));
		Assert.assertFalse(errors.hasFieldErrors("concept"));
		Assert.assertFalse(errors.hasFieldErrors("patient"));
		Assert.assertFalse(errors.hasFieldErrors("orderer"));
	}
	
	/**
	 * @see {@link OrderValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should fail validation if concept is null", method = "validate(Object,Errors)")
	public void validate_shouldFailValidationIfConceptIsNull() throws Exception {
		Order order = new Order();
		order.setPatient(Context.getPatientService().getPatient(2));
		order.setOrderer(Context.getProviderService().getProvider(1));
		
		Errors errors = new BindException(order, "order");
		new OrderValidator().validate(order, errors);
		
		Assert.assertFalse(errors.hasFieldErrors("discontinued"));
		Assert.assertTrue(errors.hasFieldErrors("concept"));
		Assert.assertFalse(errors.hasFieldErrors("patient"));
		Assert.assertFalse(errors.hasFieldErrors("orderer"));
	}
	
	/**
	 * @see {@link OrderValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should fail validation if patient is null", method = "validate(Object,Errors)")
	public void validate_shouldFailValidationIfPatientIsNull() throws Exception {
		Order order = new Order();
		order.setConcept(Context.getConceptService().getConcept(88));
		order.setOrderer(Context.getProviderService().getProvider(1));
		
		Errors errors = new BindException(order, "order");
		new OrderValidator().validate(order, errors);
		
		Assert.assertFalse(errors.hasFieldErrors("discontinued"));
		Assert.assertFalse(errors.hasFieldErrors("concept"));
		Assert.assertTrue(errors.hasFieldErrors("patient"));
		Assert.assertFalse(errors.hasFieldErrors("orderer"));
	}
	
	/**
	 * @see {@link OrderValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should fail validation if orderer is null", method = "validate(Object,Errors)")
	public void validate_shouldFailValidationIfOrdererIsNull() throws Exception {
		Order order = new Order();
		order.setConcept(Context.getConceptService().getConcept(88));
		order.setPatient(Context.getPatientService().getPatient(2));
		
		Errors errors = new BindException(order, "order");
		new OrderValidator().validate(order, errors);
		
		Assert.assertFalse(errors.hasFieldErrors("discontinued"));
		Assert.assertFalse(errors.hasFieldErrors("concept"));
		Assert.assertTrue(errors.hasFieldErrors("orderer"));
		Assert.assertFalse(errors.hasFieldErrors("patient"));
	}
	
	/**
	 * @see {@link OrderValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should fail validation if encounter is null", method = "validate(Object,Errors)")
	public void validate_shouldFailValidationIfEncounterIsNull() throws Exception {
		Order order = new Order();
		order.setConcept(Context.getConceptService().getConcept(88));
		order.setPatient(Context.getPatientService().getPatient(2));
		order.setEncounter(null);
		
		Errors errors = new BindException(order, "order");
		new OrderValidator().validate(order, errors);
		
		Assert.assertTrue(errors.hasFieldErrors("encounter"));
	}
	
	/**
	 * @see {@link OrderValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should fail validation if urgency is null", method = "validate(Object,Errors)")
	public void validate_shouldFailValidationIfUrgencyIsNull() throws Exception {
		Order order = new Order();
		order.setConcept(Context.getConceptService().getConcept(88));
		order.setPatient(Context.getPatientService().getPatient(2));
		order.setUrgency(null);
		
		Errors errors = new BindException(order, "order");
		new OrderValidator().validate(order, errors);
		
		Assert.assertTrue(errors.hasFieldErrors("urgency"));
	}
	
	/**
	 * @see {@link OrderValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should fail validation if action is null", method = "validate(Object,Errors)")
	public void validate_shouldFailValidationIfActionIsNull() throws Exception {
		Order order = new Order();
		order.setConcept(Context.getConceptService().getConcept(88));
		order.setPatient(Context.getPatientService().getPatient(2));
		order.setAction(null);
		
		Errors errors = new BindException(order, "order");
		new OrderValidator().validate(order, errors);
		
		Assert.assertTrue(errors.hasFieldErrors("action"));
	}
	
	/**
	 * @see {@link OrderValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should fail validation if dateActivated after dateStopped", method = "validate(Object,Errors)")
	public void validate_shouldFailValidationIfDateActivatedAfterDateStopped() throws Exception {
		Order order = new Order();
		order.setConcept(Context.getConceptService().getConcept(88));
		order.setPatient(Context.getPatientService().getPatient(2));
		order.setOrderer(Context.getProviderService().getProvider(1));
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH) - 1);
		order.setDateActivated(new Date());
		OrderUtilTest.setDateStopped(order, cal.getTime());
		
		Errors errors = new BindException(order, "order");
		new OrderValidator().validate(order, errors);
		
		Assert.assertTrue(errors.hasFieldErrors("dateActivated"));
		Assert.assertTrue(errors.hasFieldErrors("dateStopped"));
	}
	
	/**
	 * @see {@link OrderValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should fail validation if dateActivated after autoExpireDate", method = "validate(Object,Errors)")
	public void validate_shouldFailValidationIfDateActivatedAfterAutoExpireDate() throws Exception {
		Order order = new Order();
		order.setConcept(Context.getConceptService().getConcept(88));
		order.setPatient(Context.getPatientService().getPatient(2));
		order.setOrderer(Context.getProviderService().getProvider(1));
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH) - 1);
		order.setDateActivated(new Date());
		order.setAutoExpireDate(cal.getTime());
		
		Errors errors = new BindException(order, "order");
		new OrderValidator().validate(order, errors);
		
		Assert.assertTrue(errors.hasFieldErrors("dateActivated"));
		Assert.assertTrue(errors.hasFieldErrors("autoExpireDate"));
	}
	
	/**
	 * @verifies fail validation if scheduledDate is null when urgency is ON_SCHEDULED_DATE
	 * @see OrderValidator#validate(Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfScheduledDateIsNullWhenUrgencyIsON_SCHEDULED_DATE() throws Exception {
		Order order = new Order();
		order.setConcept(Context.getConceptService().getConcept(88));
		order.setPatient(Context.getPatientService().getPatient(2));
		order.setOrderer(Context.getProviderService().getProvider(1));
		
		order.setUrgency(Order.Urgency.ON_SCHEDULED_DATE);
		order.setScheduledDate(null);
		Errors errors = new BindException(order, "order");
		new OrderValidator().validate(order, errors);
		Assert.assertTrue(errors.hasFieldErrors("scheduledDate"));
		
		order.setScheduledDate(new Date());
		order.setUrgency(Order.Urgency.STAT);
		errors = new BindException(order, "order");
		new OrderValidator().validate(order, errors);
		Assert.assertFalse(errors.hasFieldErrors("scheduledDate"));
	}
	
	/**
	 * @verifies fail validation if scheduledDate is set and urgency is not set as ON_SCHEDULED_DATE
	 * @see OrderValidator#validate(Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfScheduledDateIsSetAndUrgencyIsNotSetAsON_SCHEDULED_DATE() throws Exception {
		Order order = new Order();
		order.setConcept(Context.getConceptService().getConcept(88));
		order.setPatient(Context.getPatientService().getPatient(2));
		order.setOrderer(Context.getProviderService().getProvider(1));
		
		order.setScheduledDate(new Date());
		order.setUrgency(Order.Urgency.ROUTINE);
		Errors errors = new BindException(order, "order");
		new OrderValidator().validate(order, errors);
		Assert.assertTrue(errors.hasFieldErrors("urgency"));
		
		order.setScheduledDate(new Date());
		order.setUrgency(Order.Urgency.ON_SCHEDULED_DATE);
		errors = new BindException(order, "order");
		new OrderValidator().validate(order, errors);
		Assert.assertFalse(errors.hasFieldErrors("urgency"));
	}
	
	/**
	 * @verifies fail validation if orderType.javaClass does not match order.class
	 * @see OrderValidator#validate(Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfOrderTypejavaClassDoesNotMatchOrderclass() throws Exception {
		Order order = new DrugOrder();
		order.setConcept(Context.getConceptService().getConcept(88));
		order.setPatient(Context.getPatientService().getPatient(2));
		order.setOrderer(Context.getProviderService().getProvider(1));
		order.setOrderType(Context.getOrderService().getOrderTypeByName("Test order"));
		
		Errors errors = new BindException(order, "order");
		new OrderValidator().validate(order, errors);
		Assert.assertTrue(errors.hasFieldErrors("orderType"));
		Assert.assertTrue(Arrays.asList(errors.getFieldError("orderType").getCodes()).contains(
		    "Order.error.orderTypeClassMismatchesOrderClass"));
	}
	
	/**
	 * @verifies pass validation if the class of the order is a subclass of orderType.javaClass
	 * @see OrderValidator#validate(Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldPassValidationIfTheClassOfTheOrderIsASubclassOfOrderTypejavaClass() throws Exception {
		SomeDrugOrder order = new SomeDrugOrder();
		order.setConcept(Context.getConceptService().getConcept(88));
		order.setPatient(Context.getPatientService().getPatient(2));
		order.setOrderer(Context.getProviderService().getProvider(1));
		order.setOrderType(Context.getOrderService().getOrderTypeByName("Drug order"));
		
		Errors errors = new BindException(order, "order");
		new OrderValidator().validate(order, errors);
		Assert.assertFalse(errors.hasFieldErrors("orderType"));
	}
	
	/**
	 * @see {@link OrderValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should pass validation if all fields are correct", method = "validate(Object,Errors)")
	public void validate_shouldPassValidationIfAllFieldsAreCorrect() throws Exception {
		Order order = new DrugOrder();
		Encounter encounter = new Encounter();
		order.setConcept(Context.getConceptService().getConcept(88));
		order.setOrderer(Context.getProviderService().getProvider(1));
		Patient patient = Context.getPatientService().getPatient(2);
		encounter.setPatient(patient);
		order.setPatient(patient);
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH) - 1);
		order.setDateActivated(cal.getTime());
		order.setAutoExpireDate(new Date());
		order.setCareSetting(new CareSetting());
		order.setEncounter(encounter);
		order.setUrgency(Order.Urgency.ROUTINE);
		order.setAction(Order.Action.NEW);
		order.setOrderType(Context.getOrderService().getOrderTypeByName("Drug order"));
		
		Errors errors = new BindException(order, "order");
		new OrderValidator().validate(order, errors);
		
		Assert.assertFalse(errors.hasErrors());
	}
	
	/**
	 * @verifies not allow a future dateActivated
	 * @see OrderValidator#validate(Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldNotAllowAFutureDateActivated() throws Exception {
		Patient patient = Context.getPatientService().getPatient(7);
		TestOrder order = new TestOrder();
		order.setPatient(patient);
		order.setOrderType(orderService.getOrderTypeByName("Test order"));
		order.setEncounter(Context.getEncounterService().getEncounter(3));
		order.setConcept(Context.getConceptService().getConcept(5497));
		order.setOrderer(Context.getProviderService().getProvider(1));
		order.setCareSetting(orderService.getCareSetting(1));
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.HOUR_OF_DAY, 1);
		order.setDateActivated(cal.getTime());
		
		Errors errors = new BindException(order, "order");
		new OrderValidator().validate(order, errors);
		
		Assert.assertTrue(errors.hasFieldErrors("dateActivated"));
		Assert.assertEquals("Order.error.dateActivatedInFuture", errors.getFieldError("dateActivated").getCode());
	}
	
	/**
	 * @see {@link OrderValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should pass validation if field lengths are correct", method = "validate(Object,Errors)")
	public void validate_shouldPassValidationIfFieldLengthsAreCorrect() throws Exception {
		Order order = new Order();
		Encounter encounter = new Encounter();
		order.setConcept(Context.getConceptService().getConcept(88));
		order.setOrderer(Context.getProviderService().getProvider(1));
		Patient patient = Context.getPatientService().getPatient(2);
		encounter.setPatient(patient);
		order.setPatient(patient);
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH) - 1);
		order.setDateActivated(cal.getTime());
		order.setAutoExpireDate(new Date());
		order.setCareSetting(new CareSetting());
		order.setEncounter(encounter);
		order.setUrgency(Order.Urgency.ROUTINE);
		order.setAction(Order.Action.NEW);
		
		order.setOrderReasonNonCoded("orderReasonNonCoded");
		order.setAccessionNumber("accessionNumber");
		order.setCommentToFulfiller("commentToFulfiller");
		order.setVoidReason("voidReason");
		
		Errors errors = new BindException(order, "order");
		new OrderValidator().validate(order, errors);
		
		Assert.assertFalse(errors.hasErrors());
	}
	
	/**
	 * @see {@link OrderValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should fail validation if field lengths are not correct", method = "validate(Object,Errors)")
	public void validate_shouldFailValidationIfFieldLengthsAreNotCorrect() throws Exception {
		Order order = new Order();
		Encounter encounter = new Encounter();
		order.setConcept(Context.getConceptService().getConcept(88));
		order.setOrderer(Context.getProviderService().getProvider(1));
		Patient patient = Context.getPatientService().getPatient(2);
		encounter.setPatient(patient);
		order.setPatient(patient);
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH) - 1);
		order.setDateActivated(cal.getTime());
		order.setAutoExpireDate(new Date());
		order.setCareSetting(new CareSetting());
		order.setEncounter(encounter);
		order.setUrgency(Order.Urgency.ROUTINE);
		order.setAction(Order.Action.NEW);
		
		order
		        .setOrderReasonNonCoded("too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text");
		order
		        .setAccessionNumber("too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text");
		order
		        .setCommentToFulfiller("too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text");
		order
		        .setVoidReason("too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text");
		
		Errors errors = new BindException(order, "order");
		new OrderValidator().validate(order, errors);
		
		Assert.assertTrue(errors.hasFieldErrors("accessionNumber"));
		Assert.assertTrue(errors.hasFieldErrors("orderReasonNonCoded"));
		Assert.assertTrue(errors.hasFieldErrors("commentToFulfiller"));
		Assert.assertTrue(errors.hasFieldErrors("voidReason"));
	}
}
