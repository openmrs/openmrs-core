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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.lang3.time.DateUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.CareSetting;
import org.openmrs.DrugOrder;
import org.openmrs.Encounter;
import org.openmrs.Order;
import org.openmrs.OrderGroup;
import org.openmrs.Patient;
import org.openmrs.TestOrder;
import org.openmrs.api.OrderService;
import org.openmrs.api.builder.OrderBuilder;
import org.openmrs.api.context.Context;
import org.openmrs.order.OrderUtilTest;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;

/**
 * Tests methods on the {@link OrderValidator} class.
 */
public class OrderValidatorTest extends BaseContextSensitiveTest {
	
	private class SomeDrugOrder extends DrugOrder {}
	
	private OrderService orderService;
	
	protected static final String ORDER_SET = "org/openmrs/api/include/OrderSetServiceTest-general.xml";
	
	@BeforeEach
	public void setup() {
		orderService = Context.getOrderService();
	}
	
	/**
	 * @see OrderValidator#validate(Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfOrderIsNull() {
		Errors errors = new BindException(new Order(), "order");
		new OrderValidator().validate(null, errors);
		
		assertTrue(errors.hasErrors());
		assertEquals("error.general", errors.getAllErrors().get(0).getCode());
	}
	
	/**
	 * @see OrderValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfOrderAndEncounterHaveDifferentPatients() {
		Order order = new Order();
		order.setConcept(Context.getConceptService().getConcept(88));
		order.setPatient(Context.getPatientService().getPatient(2));
		order.setEncounter(Context.getEncounterService().getEncounter(3));
		order.setOrderer(Context.getProviderService().getProvider(1));
		Errors errors = new BindException(order, "order");
		new OrderValidator().validate(order, errors);
		
		assertTrue(errors.hasFieldErrors("encounter"));
	}
	
	/**
	 * @see OrderValidator#validate(Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfDateActivatedIsBeforeEncountersEncounterDatetime() {
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
		
		assertTrue(errors.hasFieldErrors("dateActivated"));
	}
	
	/**
	 * @see OrderValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfVoidedIsNull() {
		Order order = new Order();
		order.setVoided(null);
		order.setConcept(Context.getConceptService().getConcept(88));
		order.setPatient(Context.getPatientService().getPatient(2));
		order.setOrderer(Context.getProviderService().getProvider(1));
		
		Errors errors = new BindException(order, "order");
		new OrderValidator().validate(order, errors);
		
		assertFalse(errors.hasFieldErrors("discontinued"));
		assertTrue(errors.hasFieldErrors("voided"));
		assertFalse(errors.hasFieldErrors("concept"));
		assertFalse(errors.hasFieldErrors("patient"));
		assertFalse(errors.hasFieldErrors("orderer"));
	}
	
	/**
	 * @see OrderValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfConceptIsNull() {
		Order order = new Order();
		order.setPatient(Context.getPatientService().getPatient(2));
		order.setOrderer(Context.getProviderService().getProvider(1));
		
		Errors errors = new BindException(order, "order");
		new OrderValidator().validate(order, errors);
		
		assertFalse(errors.hasFieldErrors("discontinued"));
		assertTrue(errors.hasFieldErrors("concept"));
		assertFalse(errors.hasFieldErrors("patient"));
		assertFalse(errors.hasFieldErrors("orderer"));
	}
	
	/**
	 * @see OrderValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfPatientIsNull() {
		Order order = new Order();
		order.setConcept(Context.getConceptService().getConcept(88));
		order.setOrderer(Context.getProviderService().getProvider(1));
		
		Errors errors = new BindException(order, "order");
		new OrderValidator().validate(order, errors);
		
		assertFalse(errors.hasFieldErrors("discontinued"));
		assertFalse(errors.hasFieldErrors("concept"));
		assertTrue(errors.hasFieldErrors("patient"));
		assertFalse(errors.hasFieldErrors("orderer"));
	}
	
	/**
	 * @see OrderValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfOrdererIsNull() {
		Order order = new Order();
		order.setConcept(Context.getConceptService().getConcept(88));
		order.setPatient(Context.getPatientService().getPatient(2));
		
		Errors errors = new BindException(order, "order");
		new OrderValidator().validate(order, errors);
		
		assertFalse(errors.hasFieldErrors("discontinued"));
		assertFalse(errors.hasFieldErrors("concept"));
		assertTrue(errors.hasFieldErrors("orderer"));
		assertFalse(errors.hasFieldErrors("patient"));
	}
	
	/**
	 * @see OrderValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfEncounterIsNull() {
		Order order = new Order();
		order.setConcept(Context.getConceptService().getConcept(88));
		order.setPatient(Context.getPatientService().getPatient(2));
		order.setEncounter(null);
		
		Errors errors = new BindException(order, "order");
		new OrderValidator().validate(order, errors);
		
		assertTrue(errors.hasFieldErrors("encounter"));
	}
	
	/**
	 * @see OrderValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfUrgencyIsNull() {
		Order order = new Order();
		order.setConcept(Context.getConceptService().getConcept(88));
		order.setPatient(Context.getPatientService().getPatient(2));
		order.setUrgency(null);
		
		Errors errors = new BindException(order, "order");
		new OrderValidator().validate(order, errors);
		
		assertTrue(errors.hasFieldErrors("urgency"));
	}
	
	/**
	 * @see OrderValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfActionIsNull() {
		Order order = new Order();
		order.setConcept(Context.getConceptService().getConcept(88));
		order.setPatient(Context.getPatientService().getPatient(2));
		order.setAction(null);
		
		Errors errors = new BindException(order, "order");
		new OrderValidator().validate(order, errors);
		
		assertTrue(errors.hasFieldErrors("action"));
	}
	
	/**
	 * @throws Exception
	 * @see OrderValidator#validate(Object,Errors)
	 */
	@Test
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
		
		assertTrue(errors.hasFieldErrors("dateActivated"));
		assertTrue(errors.hasFieldErrors("dateStopped"));
	}
	
	/**
	 * @see OrderValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfDateActivatedAfterAutoExpireDate() {
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
		
		assertTrue(errors.hasFieldErrors("dateActivated"));
		assertTrue(errors.hasFieldErrors("autoExpireDate"));
	}
	
	/**
	 * @see OrderValidator#validate(Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfScheduledDateIsNullWhenUrgencyIsON_SCHEDULED_DATE() {
		Order order = new Order();
		order.setConcept(Context.getConceptService().getConcept(88));
		order.setPatient(Context.getPatientService().getPatient(2));
		order.setOrderer(Context.getProviderService().getProvider(1));
		
		order.setUrgency(Order.Urgency.ON_SCHEDULED_DATE);
		order.setScheduledDate(null);
		Errors errors = new BindException(order, "order");
		new OrderValidator().validate(order, errors);
		assertTrue(errors.hasFieldErrors("scheduledDate"));
		
		order.setScheduledDate(new Date());
		order.setUrgency(Order.Urgency.STAT);
		errors = new BindException(order, "order");
		new OrderValidator().validate(order, errors);
		assertFalse(errors.hasFieldErrors("scheduledDate"));
	}
	
	/**
	 * @see OrderValidator#validate(Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfScheduledDateIsSetAndUrgencyIsNotSetAsON_SCHEDULED_DATE() {
		Order order = new Order();
		order.setConcept(Context.getConceptService().getConcept(88));
		order.setPatient(Context.getPatientService().getPatient(2));
		order.setOrderer(Context.getProviderService().getProvider(1));
		
		order.setScheduledDate(new Date());
		order.setUrgency(Order.Urgency.ROUTINE);
		Errors errors = new BindException(order, "order");
		new OrderValidator().validate(order, errors);
		assertTrue(errors.hasFieldErrors("urgency"));
		
		order.setScheduledDate(new Date());
		order.setUrgency(Order.Urgency.ON_SCHEDULED_DATE);
		errors = new BindException(order, "order");
		new OrderValidator().validate(order, errors);
		assertFalse(errors.hasFieldErrors("urgency"));
	}
	
	/**
	 * @see OrderValidator#validate(Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfOrderTypejavaClassDoesNotMatchOrderclass() {
		Order order = new DrugOrder();
		order.setConcept(Context.getConceptService().getConcept(88));
		order.setPatient(Context.getPatientService().getPatient(2));
		order.setOrderer(Context.getProviderService().getProvider(1));
		order.setOrderType(Context.getOrderService().getOrderTypeByName("Test order"));
		
		Errors errors = new BindException(order, "order");
		new OrderValidator().validate(order, errors);
		assertTrue(errors.hasFieldErrors("orderType"));
		assertTrue(Arrays.asList(errors.getFieldError("orderType").getCodes()).contains(
		    "Order.error.orderTypeClassMismatchesOrderClass"));
	}
	
	/**
	 * @see OrderValidator#validate(Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldPassValidationIfTheClassOfTheOrderIsASubclassOfOrderTypejavaClass() {
		SomeDrugOrder order = new SomeDrugOrder();
		order.setConcept(Context.getConceptService().getConcept(88));
		order.setPatient(Context.getPatientService().getPatient(2));
		order.setOrderer(Context.getProviderService().getProvider(1));
		order.setOrderType(Context.getOrderService().getOrderTypeByName("Drug order"));
		
		Errors errors = new BindException(order, "order");
		new OrderValidator().validate(order, errors);
		assertFalse(errors.hasFieldErrors("orderType"));
	}
	
	/**
	 * @see OrderValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldPassValidationIfAllFieldsAreCorrect() {
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
		
		assertFalse(errors.hasErrors());
	}
	
	/**
	 * @see OrderValidator#validate(Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldNotAllowAFutureDateActivated() {
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
		
		assertTrue(errors.hasFieldErrors("dateActivated"));
		assertEquals("Order.error.dateActivatedInFuture", errors.getFieldError("dateActivated").getCode());
	}
	
	/**
	 * @see OrderValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldPassValidationIfFieldLengthsAreCorrect() {
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

		assertFalse(errors.hasErrors());
	}

	@Test
	public void saveOrder_shouldNotSaveOrderIfInvalidOrderGroupEncounter() {
		executeDataSet(ORDER_SET);
		OrderGroup orderGroup = new OrderGroup();
		
		orderGroup.setEncounter(Context.getEncounterService().getEncounter(5));
		
		Order order = new OrderBuilder().withAction(Order.Action.NEW).withPatient(7).withConcept(1000).withCareSetting(1)
		        .withOrderer(1).withEncounter(3).withDateActivated(new Date()).withOrderType(17).withUrgency(
		            Order.Urgency.ON_SCHEDULED_DATE).withScheduledDate(new Date()).withOrderGroup(orderGroup).build();

		Errors errors = new BindException(order, "order");
		new OrderValidator().validate(order, errors);
		
		assertTrue(errors.hasErrors());
	}
	
	/**
	 * @see OrderValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfFieldLengthsAreNotCorrect() {
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

		assertTrue(errors.hasFieldErrors("accessionNumber"));
		assertTrue(errors.hasFieldErrors("orderReasonNonCoded"));
		assertTrue(errors.hasFieldErrors("commentToFulfiller"));
		assertTrue(errors.hasFieldErrors("voidReason"));
	}
	
	@Test
	public void saveOrder_shouldNotSaveOrderIfInvalidOrderGroupPatient() {
		executeDataSet(ORDER_SET);
		OrderGroup orderGroup = new OrderGroup();
		
		orderGroup.setEncounter(Context.getEncounterService().getEncounter(5));
		orderGroup.setPatient(Context.getPatientService().getPatient(2));
		
		Order order = new OrderBuilder().withAction(Order.Action.NEW).withPatient(7).withConcept(1000).withCareSetting(1)
		        .withOrderer(1).withEncounter(3).withDateActivated(new Date()).withOrderType(17).withUrgency(
		            Order.Urgency.ON_SCHEDULED_DATE).withScheduledDate(new Date()).withOrderGroup(orderGroup).build();

		Errors errors = new BindException(order, "order");
		new OrderValidator().validate(order, errors);

		assertTrue(errors.hasFieldErrors("patient"));
		assertEquals("Order.error.orderPatientAndOrderGroupPatientMismatch", errors.getFieldError("patient")
		        .getCode());
	}
}
