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
