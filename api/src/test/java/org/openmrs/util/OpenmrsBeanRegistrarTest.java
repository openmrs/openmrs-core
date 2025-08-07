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

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.openmrs.annotation.Handler;
import org.openmrs.annotation.OpenmrsComponent1_6To1_7;
import org.openmrs.annotation.OpenmrsComponent1_8;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import java.util.Arrays;
import java.util.List;

/**
 * Tests {@link org.openmrs.util.OpenmrsBeanRegistrar}
 */
public class OpenmrsBeanRegistrarTest extends BaseContextSensitiveTest {

	private static final Logger log = LoggerFactory.getLogger(OpenmrsBeanRegistrarTest.class);

	@Autowired
	private ApplicationContext context;
	
	@Test
	public void shouldRegisterHandlerBeansDynamically() {
		String[] handlerBeans = context.getBeanNamesForAnnotation(Handler.class);

		assertTrue(handlerBeans.length > 0);
		
		Object dummyHandlerOne = context.getBean("dummyHandler");
		assertNotNull(dummyHandlerOne);
	}

	@Test
	public void shouldRegisterBeanForOpenmrs1_8AndLater() {
		OpenmrsComponent1_8 bean = context.getBean(OpenmrsComponent1_8.class);

		assertNotNull(bean);
	}
	
	@Test
	public void shouldNotRegisterBeanForOpenmrs1_6To1_7() {
		assertThrows(NoSuchBeanDefinitionException.class, () -> context.getBean(OpenmrsComponent1_6To1_7.class));
	}

	@Test
	public void shouldNotRegisterBeanForTestTypes() {
		for (String className : superClassTestTypeNames) {
			try {
				Class<?> clazz = Class.forName(className);
				assertThrows(NoSuchBeanDefinitionException.class, () -> context.getBean(clazz));
			} catch (ClassNotFoundException e) {
				// Class not found in current classpath, skip
				log.warn("Skipping class: {}, not found in classpath", className);
			}
		}
	}

	// Test handler class to simulate real @Handler-annotated components
	// This will be picked up by the OpenmrsBeanRegistrar
	@Handler
	public static class DummyHandler {}

	private static final List<String> superClassTestTypeNames = Arrays.asList(
		"org.openmrs.test.BaseContextSensitiveTest",
		"org.openmrs.test.BaseModuleContextSensitiveTest",
		"org.openmrs.web.test.BaseWebContextSensitiveTest",
		"org.openmrs.web.test.BaseModuleWebContextSensitiveTest",
		"org.springframework.test.AbstractTransactionalSpringContextTests",
		"org.openmrs.BaseTest",
		"junit.framework.TestCase"
	);
}
