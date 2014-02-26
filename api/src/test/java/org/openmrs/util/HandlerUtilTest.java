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
package org.openmrs.util;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.DrugOrder;
import org.openmrs.Order;
import org.openmrs.Patient;
import org.openmrs.api.APIException;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.test.Verifies;
import org.openmrs.validator.DrugOrderValidator;
import org.openmrs.validator.OrderValidator;
import org.openmrs.validator.PatientValidator;
import org.springframework.validation.Validator;

/**
 * Tests the methods in {@link HandlerUtil}
 */
public class HandlerUtilTest extends BaseContextSensitiveTest {
	
	/**
	 * @see {@link HandlerUtil#getHandlerForType(Class, Class)}
	 */
	@Test
	@Verifies(value = "should return a list of all classes that can handle the passed type", method = "getHandlersForType(Class, Class)")
	public void getHandlersForType_shouldReturnAListOfAllClassesThatCanHandleThePassedType() throws Exception {
		List<Validator> l = HandlerUtil.getHandlersForType(Validator.class, Order.class);
		Assert.assertEquals(1, l.size());
		Assert.assertEquals(OrderValidator.class, l.iterator().next().getClass());
		l = HandlerUtil.getHandlersForType(Validator.class, DrugOrder.class);
		Assert.assertEquals(2, l.size());
	}
	
	/**
	 * @see {@link HandlerUtil#getHandlerForType(Class, Class)}
	 */
	@Test
	@Verifies(value = "should return an empty list if no classes can handle the passed type", method = "getHandlersForType(Class, Class)")
	public void getHandlersForType_shouldReturnAnEmptyListIfNoClassesCanHandleThePassedType() throws Exception {
		List<Validator> l = HandlerUtil.getHandlersForType(Validator.class, PatientValidator.class);
		Assert.assertNotNull(l);
		Assert.assertEquals(0, l.size());
	}
	
	/**
	 * @see {@link HandlerUtil#getPreferredHandler(Class, Class)}
	 */
	@Test
	@Verifies(value = "should return the preferred handler for the passed handlerType and type", method = "getPreferredHandler(Class, Class)")
	public void getPreferredHandler_shouldReturnThePreferredHandlerForThePassedHandlerAndType() throws Exception {
		Validator v = HandlerUtil.getPreferredHandler(Validator.class, DrugOrder.class);
		Assert.assertEquals(DrugOrderValidator.class, v.getClass());
	}
	
	/**
	 * @see {@link HandlerUtil#getPreferredHandler(Class, Class)}
	 */
	@Test(expected = APIException.class)
	@Verifies(value = "should throw a APIException if no handler is found", method = "getPreferredHandler(Class, Class)")
	public void getPreferredHandler_shouldThrowAAPIExceptionExceptionIfNoHandlerIsFound() throws Exception {
		HandlerUtil.getPreferredHandler(Validator.class, Patient.class);
	}
}
