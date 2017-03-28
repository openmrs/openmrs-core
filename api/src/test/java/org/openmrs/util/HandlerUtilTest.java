/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.util;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.openmrs.DrugOrder;
import org.openmrs.Order;
import org.openmrs.Patient;
import org.openmrs.Person;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.validator.DrugOrderValidator;
import org.openmrs.validator.OrderValidator;
import org.openmrs.validator.PatientValidator;
import org.openmrs.validator.PersonValidator;
import org.springframework.validation.Validator;

/**
 * Tests the methods in {@link HandlerUtil}
 */
public class HandlerUtilTest extends BaseContextSensitiveTest {
	
	@Rule
	public ExpectedException thrown = ExpectedException.none();
	
	/**
	 * @see HandlerUtil#getHandlerForType(Class, Class)
	 */
	@Test
	public void getHandlersForType_shouldReturnAListOfAllClassesThatCanHandleThePassedType() {
		List<Validator> l = HandlerUtil.getHandlersForType(Validator.class, Order.class);
		Assert.assertEquals(1, l.size());
		Assert.assertEquals(OrderValidator.class, l.iterator().next().getClass());
		l = HandlerUtil.getHandlersForType(Validator.class, DrugOrder.class);
		Assert.assertEquals(2, l.size());
	}

	/**
	 * @see HandlerUtil#getHandlerForType(Class, Class)
	 */
	@Test
	public void getHandlersForType_shouldReturnAnEmptyListIfNoClassesCanHandleThePassedType() {
		List<Validator> l = HandlerUtil.getHandlersForType(Validator.class, PatientValidator.class);
		Assert.assertNotNull(l);
		Assert.assertEquals(0, l.size());
	}
	
	/**
	 * @see HandlerUtil#getPreferredHandler(Class, Class)
	 */
	@Test
	public void getPreferredHandler_shouldReturnThePreferredHandlerForThePassedHandlerAndType() {
		Validator v = HandlerUtil.getPreferredHandler(Validator.class, DrugOrder.class);
		Assert.assertEquals(DrugOrderValidator.class, v.getClass());
	}
	
	/**
	 * @see HandlerUtil#getPreferredHandler(Class, Class)
	 */
	@Test
	public void getPreferredHandler_shouldThrowAAPIExceptionExceptionIfNoHandlerIsFound() {
		thrown.expect(APIException.class);
		thrown.expectMessage(Context.getMessageSourceService().getMessage("handler.type.not.found", new Object[] { Validator.class.toString(), Integer.class }, null));
		
		HandlerUtil.getPreferredHandler(Validator.class, Integer.class);
	}
	
	@Test
	public void getPreferredHandler_shouldReturnPatientValidatorForPatient() {
		Validator handler = HandlerUtil.getPreferredHandler(Validator.class, Patient.class);
		
		assertThat(handler, is(instanceOf(PatientValidator.class)));
	}
	
	@Test
	public void getPreferredHandler_shouldReturnPersonValidatorForPerson() {
		Validator handler = HandlerUtil.getPreferredHandler(Validator.class, Person.class);
		
		assertThat(handler, is(instanceOf(PersonValidator.class)));
	}
}
