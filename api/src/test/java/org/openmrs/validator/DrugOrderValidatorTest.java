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
import org.openmrs.Drug;
import org.openmrs.DrugOrder;
import org.openmrs.api.context.Context;
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
	@Verifies(value = "should fail validation if prn is null", method = "validate(Object,Errors)")
	public void validate_shouldFailValidationIfPrnIsNull() throws Exception {
		DrugOrder order = new DrugOrder();
		order.setPrn(null);
		order.setDrug(Context.getConceptService().getDrug(3));
		
		Errors errors = new BindException(order, "order");
		new DrugOrderValidator().validate(order, errors);
		
		Assert.assertTrue(errors.hasFieldErrors("prn"));
	}
	
	/**
	 * @see {@link DrugOrderValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should fail validation if complex is null", method = "validate(Object,Errors)")
	public void validate_shouldFailValidationIfComplexIsNull() throws Exception {
		DrugOrder order = new DrugOrder();
		order.setComplex(null);
		order.setDrug(Context.getConceptService().getDrug(3));
		
		Errors errors = new BindException(order, "order");
		new DrugOrderValidator().validate(order, errors);
		
		Assert.assertTrue(errors.hasFieldErrors("complex"));
	}
	
	/**
	 * @see {@link DrugOrderValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should fail validation if drug is null", method = "validate(Object,Errors)")
	public void validate_shouldFailValidationIfDrugIsNull() throws Exception {
		DrugOrder order = new DrugOrder();
		
		Errors errors = new BindException(order, "order");
		new DrugOrderValidator().validate(order, errors);
		
		Assert.assertTrue(errors.hasFieldErrors("drug"));
	}
	
	/**
	 * @see {@link DrugOrderValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should fail validation if drug concept is not set", method = "validate(Object,Errors)")
	public void validate_shouldFailValidationIfDrugConceptIsNotSet() throws Exception {
		DrugOrder order = new DrugOrder();
		Drug drug = Context.getConceptService().getDrug(3);
		// intentionally set drug's concept to null
		drug.setConcept(null);
		order.setDrug(drug);
		
		Errors errors = new BindException(order, "order");
		new DrugOrderValidator().validate(order, errors);
		
		Assert.assertTrue(errors.hasFieldErrors("drug.concept"));
	}
	
	/**
	 * @see {@link DrugOrderValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should fail validation if drug dose is set and units is not set", method = "validate(Object,Errors)")
	public void validate_shouldFailValidationIfDrugDoseIsSetAndUnitsIsNotSet() throws Exception {
		DrugOrder order = new DrugOrder();
		order.setDose(500d);
		// intentionally set order's units to null
		order.setUnits(null);
		
		Errors errors = new BindException(order, "order");
		new DrugOrderValidator().validate(order, errors);
		
		Assert.assertTrue(errors.hasFieldErrors("units"));
	}
	
	/**
	 * @see {@link DrugOrderValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should fail if daily dose set and units not set", method = "validate(Object,Errors)")
	public void validate_shouldFailIfDailyDoseIsSetAndUnitsIsNotSet() throws Exception {
		DrugOrder order = new DrugOrder();
		order.setEquivalentDailyDose(500d);
		// intentionally set order's units to null
		order.setUnits(null);
		
		Errors errors = new BindException(order, "order");
		new DrugOrderValidator().validate(order, errors);
		
		Assert.assertTrue(errors.hasFieldErrors("units"));
	}
	
	/**
	 * @see {@link DrugOrderValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should fail if quantity set and units not set", method = "validate(Object,Errors)")
	public void validate_shouldFailIfQuantitySetAndUnitsIsNotSet() throws Exception {
		DrugOrder order = new DrugOrder();
		order.setQuantity(5);
		// intentionally set order's units to null
		order.setUnits(null);
		
		Errors errors = new BindException(order, "order");
		new DrugOrderValidator().validate(order, errors);
		
		Assert.assertTrue(errors.hasFieldErrors("units"));
	}
	
	/**
	 * @see {@link DrugOrderValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should pass validation if all fields are correct", method = "validate(Object,Errors)")
	public void validate_shouldPassValidationIfAllFieldsAreCorrect() throws Exception {
		DrugOrder order = new DrugOrder();
		order.setConcept(Context.getConceptService().getConcept(88));
		order.setPatient(Context.getPatientService().getPatient(2));
		order.setOrderType(Context.getOrderService().getOrderType(1));
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH) - 1);
		order.setStartDate(cal.getTime());
		order.setDiscontinuedDate(new Date());
		order.setAutoExpireDate(new Date());
		order.setDrug(Context.getConceptService().getDrug(3));
		
		Errors errors = new BindException(order, "order");
		new DrugOrderValidator().validate(order, errors);
		
		Assert.assertFalse(errors.hasErrors());
	}
	
	/**
	 * @see {@link DrugOrderValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should fail validation if order concept is null", method = "validate(Object,Errors)")
	public void validate_shouldFailValidationIfOrderConceptIsNull() throws Exception {
		DrugOrder order = new DrugOrder();
		order.setConcept(null);
		
		Errors errors = new BindException(order, "order");
		new DrugOrderValidator().validate(order, errors);
		
		Assert.assertTrue(errors.hasFieldErrors("concept"));
	}
	
	/**
	 * @see {@link DrugOrderValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should fail validation if drug concept is different from order concept", method = "validate(Object,Errors)")
	public void validate_shouldFailValidationIfDrugConceptIsDifferentFromOrderConcept() throws Exception {
		DrugOrder order = new DrugOrder();
		order.setDrug(Context.getConceptService().getDrug(3));
		order.setConcept(Context.getConceptService().getConcept(792)); // the actual concept which matches with drug is "88"
		
		Errors errors = new BindException(order, "order");
		new DrugOrderValidator().validate(order, errors);
		
		Assert.assertTrue(errors.hasFieldErrors("concept"));
		Assert.assertTrue(errors.hasFieldErrors("drug"));
	}
}
