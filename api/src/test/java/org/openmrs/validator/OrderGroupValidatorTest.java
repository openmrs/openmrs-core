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

import java.util.Date;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.Order;
import org.openmrs.OrderGroup;
import org.openmrs.Patient;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.test.BaseContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;

/**
 * Tests methods on the {@link OrderGroupValidator} class.
 */
public class OrderGroupValidatorTest extends BaseContextSensitiveTest {
	
	@Autowired
	OrderGroupValidator validator;

	/**
     * @see OrderGroupValidator#validate(Object,Errors)
     * @verifies fail if any member is invalid
     */
    @Test
    public void validate_shouldFailIfAnyMemberIsInvalid() throws Exception {
    	User provider = Context.getUserService().getUser(501);
		Patient patient = Context.getPatientService().getPatient(6);
		
		OrderGroup group = new OrderGroup(null, patient);
		group.setCreator(provider);
		group.setDateCreated(new Date());
		Order order = new Order();
		// we don't setting order's concept so it should be detected
		group.addOrder(order);
		
		Errors errors = new BindException(group, "group");
		validator.validate(group, errors);
		
		Assert.assertTrue(errors.hasErrors());
		Assert.assertFalse(errors.hasFieldErrors("creator"));
		Assert.assertFalse(errors.hasFieldErrors("patient"));
		Assert.assertFalse(errors.hasFieldErrors("dateCreated"));
    }

	/**
     * @see OrderGroupValidator#validate(Object,Errors)
     * @verifies fail if any order has different from group's patient
     */
    @Test
    public void validate_shouldFailIfAnyOrderHasDifferentFromGroupsPatient() throws Exception {
    	User provider = Context.getUserService().getUser(501);
		Patient patient = Context.getPatientService().getPatient(6);
		
		OrderGroup group = new OrderGroup(null, patient);
		group.setCreator(provider);
		group.setDateCreated(new Date());
		Order order = new Order();
		order.setConcept(Context.getConceptService().getConcept(23));
		group.addOrder(order);
		// we are changing patient for order to be different from order gropu's one
		order.setPatient(Context.getPatientService().getPatient(2));
		
		Errors errors = new BindException(group, "group");
		validator.validate(group, errors);
		
		Assert.assertTrue(errors.hasErrors());
		Assert.assertFalse(errors.hasFieldErrors("creator"));
		Assert.assertFalse(errors.hasFieldErrors("patient"));
		Assert.assertFalse(errors.hasFieldErrors("dateCreated"));
    }

	/**
     * @see OrderGroupValidator#validate(Object,Errors)
     * @verifies fail if any required field is null
     */
    @Test
    public void validate_shouldFailIfAnyRequiredFieldIsNull() throws Exception {
		
    	// we are setting all required order to null field to be verified 
		OrderGroup group = new OrderGroup(null, null);
		group.setCreator(null);
		group.setDateCreated(null);
		Order order = new Order();
		order.setConcept(Context.getConceptService().getConcept(23));
		group.addOrder(order);
		
		Errors errors = new BindException(group, "group");
		validator.validate(group, errors);
		
		Assert.assertTrue(errors.hasFieldErrors("creator"));
		Assert.assertTrue(errors.hasFieldErrors("patient"));
		Assert.assertTrue(errors.hasFieldErrors("dateCreated"));
    }

	/**
     * @see OrderGroupValidator#validate(Object,Errors)
     * @verifies fail if group doesn't have any members
     */
    @Test
    public void validate_shouldFailIfGroupDoesntHaveAnyMembers() throws Exception {
    	User provider = Context.getUserService().getUser(501);
		Patient patient = Context.getPatientService().getPatient(6);
		
		OrderGroup group = new OrderGroup(null, patient);
		group.setCreator(provider);
		group.setDateCreated(new Date());
		
		Errors errors = new BindException(group, "group");
		validator.validate(group, errors);
		
		Assert.assertTrue(errors.hasErrors());
		Assert.assertFalse(errors.hasFieldErrors("creator"));
		Assert.assertFalse(errors.hasFieldErrors("patient"));
		Assert.assertFalse(errors.hasFieldErrors("dateCreated"));
    }

	/**
     * @see OrderGroupValidator#validate(Object,Errors)
     * @verifies fail if order group is null
     */
    @Test
    public void validate_shouldFailIfOrderGroupIsNull() throws Exception {
		
    	OrderGroup group = new OrderGroup(null, null);
		
		Errors errors = new BindException(group, "group");
		validator.validate(null, errors);
		
		Assert.assertTrue(errors.hasErrors());
    }

}
