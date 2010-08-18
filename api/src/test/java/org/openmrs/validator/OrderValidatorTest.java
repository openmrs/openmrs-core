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

import java.util.Calendar;
import java.util.Date;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.Order;
import org.openmrs.api.context.Context;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.test.Verifies;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;

/**
 * Tests methods on the {@link OrderValidator} class.
 */
public class OrderValidatorTest extends BaseContextSensitiveTest {
	
	/**
	 * @see {@link OrderValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should fail validation if order and encounter have different patients", method = "validate(Object,Errors)")
	public void validate_shouldFailValidationIfOrderAndEncounterHaveDifferentPatients() throws Exception {
		Order order = new Order();
		order.setConcept(Context.getConceptService().getConcept(88));
		order.setPatient(Context.getPatientService().getPatient(2));
		order.setOrderType(Context.getOrderService().getOrderType(1));
		order.setEncounter(Context.getEncounterService().getEncounter(3));
		
		Errors errors = new BindException(order, "order");
		new OrderValidator().validate(order, errors);
		
		Assert.assertTrue(errors.hasFieldErrors("encounter"));
	}
	
	/**
	 * @see {@link OrderValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should fail validation if discontinued is null", method = "validate(Object,Errors)")
	public void validate_shouldFailValidationIfDiscontinuedIsNull() throws Exception {
		Order order = new Order();
		order.setDiscontinued(null);
		order.setConcept(Context.getConceptService().getConcept(88));
		order.setPatient(Context.getPatientService().getPatient(2));
		order.setOrderType(Context.getOrderService().getOrderType(1));
		
		Errors errors = new BindException(order, "order");
		new OrderValidator().validate(order, errors);
		
		Assert.assertTrue(errors.hasFieldErrors("discontinued"));
		Assert.assertFalse(errors.hasFieldErrors("concept"));
		Assert.assertFalse(errors.hasFieldErrors("patient"));
		Assert.assertFalse(errors.hasFieldErrors("orderType"));
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
		order.setOrderType(Context.getOrderService().getOrderType(1));
		
		Errors errors = new BindException(order, "order");
		new OrderValidator().validate(order, errors);
		
		Assert.assertFalse(errors.hasFieldErrors("discontinued"));
		Assert.assertTrue(errors.hasFieldErrors("voided"));
		Assert.assertFalse(errors.hasFieldErrors("concept"));
		Assert.assertFalse(errors.hasFieldErrors("patient"));
		Assert.assertFalse(errors.hasFieldErrors("orderType"));
	}
	
	/**
	 * @see {@link OrderValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should fail validation if concept is null", method = "validate(Object,Errors)")
	public void validate_shouldFailValidationIfConceptIsNull() throws Exception {
		Order order = new Order();
		order.setPatient(Context.getPatientService().getPatient(2));
		order.setOrderType(Context.getOrderService().getOrderType(1));
		
		Errors errors = new BindException(order, "order");
		new OrderValidator().validate(order, errors);
		
		Assert.assertFalse(errors.hasFieldErrors("discontinued"));
		Assert.assertTrue(errors.hasFieldErrors("concept"));
		Assert.assertFalse(errors.hasFieldErrors("patient"));
		Assert.assertFalse(errors.hasFieldErrors("orderType"));
	}
	
	/**
	 * @see {@link OrderValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should fail validation if patient is null", method = "validate(Object,Errors)")
	public void validate_shouldFailValidationIfPatientIsNull() throws Exception {
		Order order = new Order();
		order.setConcept(Context.getConceptService().getConcept(88));
		order.setOrderType(Context.getOrderService().getOrderType(1));
		
		Errors errors = new BindException(order, "order");
		new OrderValidator().validate(order, errors);
		
		Assert.assertFalse(errors.hasFieldErrors("discontinued"));
		Assert.assertFalse(errors.hasFieldErrors("concept"));
		Assert.assertTrue(errors.hasFieldErrors("patient"));
		Assert.assertFalse(errors.hasFieldErrors("orderType"));
	}
	
	/**
	 * @see {@link OrderValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should fail validation if orderType is null", method = "validate(Object,Errors)")
	public void validate_shouldFailValidationIfOrderTypeIsNull() throws Exception {
		Order order = new Order();
		;
		order.setConcept(Context.getConceptService().getConcept(88));
		order.setPatient(Context.getPatientService().getPatient(2));
		
		Errors errors = new BindException(order, "order");
		new OrderValidator().validate(order, errors);
		
		Assert.assertFalse(errors.hasFieldErrors("discontinued"));
		Assert.assertFalse(errors.hasFieldErrors("concept"));
		Assert.assertFalse(errors.hasFieldErrors("patient"));
		Assert.assertTrue(errors.hasFieldErrors("orderType"));
	}
	
	/**
	 * @see {@link OrderValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should fail validation if startDate after discontinuedDate", method = "validate(Object,Errors)")
	public void validate_shouldFailValidationIfStartDateAfterDiscontinuedDate() throws Exception {
		Order order = new Order();
		;
		order.setConcept(Context.getConceptService().getConcept(88));
		order.setPatient(Context.getPatientService().getPatient(2));
		order.setOrderType(Context.getOrderService().getOrderType(1));
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH) - 1);
		order.setStartDate(new Date());
		order.setDiscontinuedDate(cal.getTime());
		
		Errors errors = new BindException(order, "order");
		new OrderValidator().validate(order, errors);
		
		Assert.assertTrue(errors.hasFieldErrors("startDate"));
		Assert.assertTrue(errors.hasFieldErrors("discontinuedDate"));
	}
	
	/**
	 * @see {@link OrderValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should fail validation if startDate after autoExpireDate", method = "validate(Object,Errors)")
	public void validate_shouldFailValidationIfStartDateAfterAutoExpireDate() throws Exception {
		Order order = new Order();
		order.setConcept(Context.getConceptService().getConcept(88));
		order.setPatient(Context.getPatientService().getPatient(2));
		order.setOrderType(Context.getOrderService().getOrderType(1));
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH) - 1);
		order.setStartDate(new Date());
		order.setAutoExpireDate(cal.getTime());
		
		Errors errors = new BindException(order, "order");
		new OrderValidator().validate(order, errors);
		
		Assert.assertTrue(errors.hasFieldErrors("startDate"));
		Assert.assertTrue(errors.hasFieldErrors("autoExpireDate"));
	}
	
	/**
	 * @see {@link OrderValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should pass validation if all fields are correct", method = "validate(Object,Errors)")
	public void validate_shouldPassValidationIfAllFieldsAreCorrect() throws Exception {
		Order order = new Order();
		order.setConcept(Context.getConceptService().getConcept(88));
		order.setPatient(Context.getPatientService().getPatient(2));
		order.setOrderType(Context.getOrderService().getOrderType(1));
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH) - 1);
		order.setStartDate(cal.getTime());
		order.setDiscontinuedDate(new Date());
		order.setAutoExpireDate(new Date());
		
		Errors errors = new BindException(order, "order");
		new OrderValidator().validate(order, errors);
		
		Assert.assertFalse(errors.hasErrors());
	}
}
