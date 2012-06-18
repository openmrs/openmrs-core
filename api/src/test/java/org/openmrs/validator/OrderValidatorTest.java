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
import org.openmrs.User;
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
		
		Errors errors = new BindException(order, "order");
		new OrderValidator().validate(order, errors);
		
		Assert.assertTrue(errors.hasFieldErrors("discontinued"));
		Assert.assertFalse(errors.hasFieldErrors("concept"));
		Assert.assertFalse(errors.hasFieldErrors("patient"));
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
		
		Errors errors = new BindException(order, "order");
		new OrderValidator().validate(order, errors);
		
		Assert.assertFalse(errors.hasFieldErrors("discontinued"));
		Assert.assertTrue(errors.hasFieldErrors("voided"));
		Assert.assertFalse(errors.hasFieldErrors("concept"));
		Assert.assertFalse(errors.hasFieldErrors("patient"));
	}
	
	/**
	 * @see {@link OrderValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should fail validation if concept is null", method = "validate(Object,Errors)")
	public void validate_shouldFailValidationIfConceptIsNull() throws Exception {
		Order order = new Order();
		order.setPatient(Context.getPatientService().getPatient(2));
		
		Errors errors = new BindException(order, "order");
		new OrderValidator().validate(order, errors);
		
		Assert.assertFalse(errors.hasFieldErrors("discontinued"));
		Assert.assertTrue(errors.hasFieldErrors("concept"));
		Assert.assertFalse(errors.hasFieldErrors("patient"));
	}
	
	/**
	 * @see {@link OrderValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should fail validation if patient is null", method = "validate(Object,Errors)")
	public void validate_shouldFailValidationIfPatientIsNull() throws Exception {
		Order order = new Order();
		order.setConcept(Context.getConceptService().getConcept(88));
		
		Errors errors = new BindException(order, "order");
		new OrderValidator().validate(order, errors);
		
		Assert.assertFalse(errors.hasFieldErrors("discontinued"));
		Assert.assertFalse(errors.hasFieldErrors("concept"));
		Assert.assertTrue(errors.hasFieldErrors("patient"));
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
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH) - 1);
		order.setStartDate(cal.getTime());
		order.setAutoExpireDate(new Date());
		order.setOrderNumber("orderNumber");
		
		Errors errors = new BindException(order, "order");
		new OrderValidator().validate(order, errors);
		
		Assert.assertFalse(errors.hasErrors());
	}
	
	/**
	 * @see {@link OrderValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should fail validation if order is null", method = "validate(Object,Errors)")
	public void validate_shouldFailValidationIfOrderIsNull() throws Exception {
		Errors errors = new BindException(new Order(), "order");
		new OrderValidator().validate(null, errors);
		
		Assert.assertTrue(errors.hasErrors());
	}
	
	/**
	 * @see {@link OrderValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should fail validation if discontinued but date is null", method = "validate(Object,Errors)")
	public void validate_shouldFailValidationIfDiscontinuedButDateIsNull() throws Exception {
		Order order = new Order();
		order.setConcept(Context.getConceptService().getConcept(88));
		order.setPatient(Context.getPatientService().getPatient(2));
		order.setOrderNumber("orderNumber");
		order.setDiscontinued(true);
		order.setDiscontinuedDate(null);
		
		Errors errors = new BindException(order, "order");
		new OrderValidator().validate(order, errors);
		Assert.assertTrue(errors.hasFieldErrors("discontinuedDate"));
	}
	
	/**
	 * @see {@link OrderValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should fail validation if discontinued but by is null", method = "validate(Object,Errors)")
	public void validate_shouldFailValidationIfDiscontinuedButByIsNull() throws Exception {
		Order order = new Order();
		order.setConcept(Context.getConceptService().getConcept(88));
		order.setPatient(Context.getPatientService().getPatient(2));
		order.setOrderNumber("orderNumber");
		order.setDiscontinued(true);
		order.setDiscontinuedBy(null);
		
		Errors errors = new BindException(order, "order");
		new OrderValidator().validate(order, errors);
		Assert.assertTrue(errors.hasFieldErrors("discontinuedBy"));
	}
	
	/**
	 * @see {@link OrderValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should fail validation if discontinued but reason is null", method = "validate(Object,Errors)")
	public void validate_shouldFailValidationIfDiscontinuedButReasonIsNull() throws Exception {
		Order order = new Order();
		order.setConcept(Context.getConceptService().getConcept(88));
		order.setPatient(Context.getPatientService().getPatient(2));
		order.setOrderNumber("orderNumber");
		order.setDiscontinued(true);
		order.setDiscontinuedReason(null);
		
		Errors errors = new BindException(order, "order");
		new OrderValidator().validate(order, errors);
		Assert.assertTrue(errors.hasFieldErrors("discontinuedReason"));
		
		order.setDiscontinuedReason(" ");
		
		errors = new BindException(order, "order");
		new OrderValidator().validate(order, errors);
		Assert.assertTrue(errors.hasFieldErrors("discontinuedReason"));
	}
	
	/**
	 * @see {@link OrderValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should fail validation if not discontinued but date is not null", method = "validate(Object,Errors)")
	public void validate_shouldFailValidationIfNotDiscontinuedButDateIsNotNull() throws Exception {
		Order order = new Order();
		order.setConcept(Context.getConceptService().getConcept(88));
		order.setPatient(Context.getPatientService().getPatient(2));
		order.setOrderNumber("orderNumber");
		order.setDiscontinued(false);
		order.setDiscontinuedDate(new Date());
		
		Errors errors = new BindException(order, "order");
		new OrderValidator().validate(order, errors);
		Assert.assertTrue(errors.hasFieldErrors("discontinuedDate"));
	}
	
	/**
	 * @see {@link OrderValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should fail validation if not discontinued but by is not null", method = "validate(Object,Errors)")
	public void validate_shouldFailValidationIfNotDiscontinuedButByIsNotNull() throws Exception {
		Order order = new Order();
		order.setConcept(Context.getConceptService().getConcept(88));
		order.setPatient(Context.getPatientService().getPatient(2));
		order.setOrderNumber("orderNumber");
		order.setDiscontinued(false);
		order.setDiscontinuedBy(new User());
		
		Errors errors = new BindException(order, "order");
		new OrderValidator().validate(order, errors);
		Assert.assertTrue(errors.hasFieldErrors("discontinuedBy"));
	}
	
	/**
	 * @see {@link OrderValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should fail validation if not discontinued but reason is not null", method = "validate(Object,Errors)")
	public void validate_shouldFailValidationIfNotDiscontinuedButReasonIsNotNull() throws Exception {
		Order order = new Order();
		order.setConcept(Context.getConceptService().getConcept(88));
		order.setPatient(Context.getPatientService().getPatient(2));
		order.setOrderNumber("orderNumber");
		order.setDiscontinued(false);
		order.setDiscontinuedReason("reason");
		
		Errors errors = new BindException(order, "order");
		new OrderValidator().validate(order, errors);
		Assert.assertTrue(errors.hasFieldErrors("discontinuedReason"));
		
		order.setDiscontinuedReason(" ");
		
		errors = new BindException(order, "order");
		new OrderValidator().validate(order, errors);
		Assert.assertTrue(errors.hasFieldErrors("discontinuedReason"));
	}
	
	/**
	 * @see {@link OrderValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should fail validation if discontinuedDate after autoExpireDate", method = "validate(Object,Errors)")
	public void validate_shouldFailValidationIfDiscontinuedDateAfterAutoExpireDate() throws Exception {
		Order order = new Order();
		order.setConcept(Context.getConceptService().getConcept(88));
		order.setPatient(Context.getPatientService().getPatient(2));
		order.setDiscontinued(true);
		order.setDiscontinuedReason("discontinuedReason");
		order.setDiscontinuedBy(new User());
		order.setDiscontinuedDate(new Date());
		
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH) - 1);
		order.setAutoExpireDate(cal.getTime());
		
		Errors errors = new BindException(order, "order");
		new OrderValidator().validate(order, errors);
		
		Assert.assertTrue(errors.hasFieldErrors("discontinuedDate"));
		Assert.assertTrue(errors.hasFieldErrors("autoExpireDate"));
	}
	
	/**
	 * @see {@link OrderValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should fail validation if discontinuedDate in future", method = "validate(Object,Errors)")
	public void validate_shouldFailValidationIfDiscontinuedDateInFuture() throws Exception {
		Order order = new Order();
		order.setConcept(Context.getConceptService().getConcept(88));
		order.setPatient(Context.getPatientService().getPatient(2));
		order.setDiscontinued(true);
		order.setDiscontinuedReason("discontinuedReason");
		order.setDiscontinuedBy(new User());
		
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH) + 1);
		order.setDiscontinuedDate(cal.getTime());
		
		Errors errors = new BindException(order, "order");
		new OrderValidator().validate(order, errors);
		
		Assert.assertTrue(errors.hasFieldErrors("discontinuedDate"));
	}
}
