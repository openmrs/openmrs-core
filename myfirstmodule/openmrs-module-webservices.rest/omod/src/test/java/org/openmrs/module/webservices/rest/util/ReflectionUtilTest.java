/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.util;

import org.junit.Test;
import org.openmrs.DrugOrder;
import org.openmrs.Order;
import org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResource;
import org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingSubclassHandler;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceHandler;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingSubclassHandler;
import org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs1_10.DrugOrderSubclassHandler1_10;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class ReflectionUtilTest {
	
	/**
	 * @verifies find genericInterface on a superclass if clazz does not directly implement it
	 * @see ReflectionUtil#getParameterizedTypeFromInterface(Class, Class, int)
	 */
	@Test
	public void getParameterizedTypeFromInterface_shouldFindGenericInterfaceOnASuperclassIfClazzDoesNotDirectlyImplementIt()
	        throws Exception {
		Class<?> expectedClass = ReflectionUtil.getParameterizedTypeFromInterface(DrugOrderSubclassHandler1_10.class,
		    DelegatingSubclassHandler.class, 0);
		assertEquals(Order.class, expectedClass);
		
		expectedClass = ReflectionUtil.getParameterizedTypeFromInterface(DrugOrderSubclassHandler1_10.class,
		    DelegatingSubclassHandler.class, 1);
		assertEquals(DrugOrder.class, expectedClass);
	}
	
	/**
	 * @verifies ignore type variables on the declaring interface
	 * @see ReflectionUtil#getParameterizedTypeFromInterface(Class, Class, int)
	 */
	@Test
	public void getParameterizedTypeFromInterface_shouldIgnoreTypeVariablesOnTheDeclaringInterface() throws Exception {
		//DelegatingResourceHandler<T> is one of the generic interfaces implemented by 
		//BaseDelegatingResource, once the logic reaches it, the type parameter T has to be ignored
		Class<?> clazz = ReflectionUtil.getParameterizedTypeFromInterface(BaseDelegatingResource.class,
		    DelegatingResourceHandler.class, 0);
		assertNull(clazz);
	}
	
	/**
	 * @verifies not inspect superclasses of the specified genericInterface
	 * @see ReflectionUtil#getParameterizedTypeFromInterface(Class, Class, int)
	 */
	@Test
	public void getParameterizedTypeFromInterface_shouldNotInspectSuperclassesOfTheSpecifiedGenericInterface()
	        throws Exception {
		Class<?> clazz = ReflectionUtil.getParameterizedTypeFromInterface(BaseDelegatingSubclassHandler.class,
		    DrugOrderSubclassHandler1_10.class, 1);
		assertNull(clazz);
	}
}
