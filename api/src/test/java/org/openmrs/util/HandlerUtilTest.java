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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.openmrs.DrugOrder;
import org.openmrs.Order;
import org.openmrs.Patient;
import org.openmrs.Person;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;
import org.openmrs.validator.DrugOrderValidator;
import org.openmrs.validator.OrderValidator;
import org.openmrs.validator.PatientValidator;
import org.openmrs.validator.PersonValidator;
import org.springframework.validation.Validator;

/**
 * Tests the methods in {@link HandlerUtil}
 */
public class HandlerUtilTest extends BaseContextSensitiveTest {
	
	
	/**
	 * @see HandlerUtil#getHandlerForType(Class, Class)
	 */
	@Test
	public void getHandlersForType_shouldReturnAListOfAllClassesThatCanHandleThePassedType() {
		List<Validator> l = HandlerUtil.getHandlersForType(Validator.class, Order.class);
		assertEquals(1, l.size());
		assertEquals(OrderValidator.class, l.iterator().next().getClass());
		l = HandlerUtil.getHandlersForType(Validator.class, DrugOrder.class);
		assertEquals(2, l.size());
	}

	/**
	 * @see HandlerUtil#getHandlerForType(Class, Class)
	 */
	@Test
	public void getHandlersForType_shouldReturnAnEmptyListIfNoClassesCanHandleThePassedType() {
		List<Validator> l = HandlerUtil.getHandlersForType(Validator.class, PatientValidator.class);
		assertNotNull(l);
		assertEquals(0, l.size());
	}
	
	/**
	 * @see HandlerUtil#getPreferredHandler(Class, Class)
	 */
	@Test
	public void getPreferredHandler_shouldReturnThePreferredHandlerForThePassedHandlerAndType() {
		Validator v = HandlerUtil.getPreferredHandler(Validator.class, DrugOrder.class);
		assertEquals(DrugOrderValidator.class, v.getClass());
	}
	
	/**
	 * @see HandlerUtil#getPreferredHandler(Class, Class)
	 */
	@Test
	public void getPreferredHandler_shouldThrowAAPIExceptionExceptionIfNoHandlerIsFound() { 
		
		APIException exception = assertThrows(APIException.class, () -> HandlerUtil.getPreferredHandler(Validator.class, Integer.class));
		assertThat(exception.getMessage(), is(Context.getMessageSourceService().getMessage("handler.type.not.found", new Object[] { Validator.class.toString(), Integer.class }, null)));
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
