/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.aop;

import java.util.Arrays;
import java.util.List;

import org.aopalliance.aop.Advice;
import org.junit.jupiter.api.Test;
import org.openmrs.api.APIAuthenticationException;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.ConceptService;
import org.openmrs.api.OrderService;
import org.openmrs.api.context.Context;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.transaction.interceptor.TransactionInterceptor;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Core services must keep their advisors when a module declares a service with a
 * {@link org.springframework.transaction.interceptor.TransactionProxyFactoryBean} whose target
 * references core services.
 */
public class ModuleFactoryBeanServiceAdvisorsTest extends BaseContextSensitiveTest {

	@Autowired
	@Qualifier("conceptService")
	private ConceptService conceptService;

	@Autowired
	@Qualifier("adminService")
	private AdministrationService adminService;

	@Autowired
	@Qualifier("orderService")
	private OrderService orderService;

	@Test
	public void coreServicesReferencedByModuleFactoryBean_shouldKeepCoreAdvisors() {
		for (Object service : List.of(conceptService, adminService, orderService)) {
			List<Advice> advices = Arrays.stream(((Advised) service).getAdvisors()).map(a -> a.getAdvice()).toList();
			assertTrue(advices.stream().anyMatch(AuthorizationAdvice.class::isInstance), "authorization: " + advices);
			assertTrue(advices.stream().anyMatch(RequiredDataAdvice.class::isInstance), "required data: " + advices);
			assertTrue(advices.stream().anyMatch(TransactionInterceptor.class::isInstance), "transaction: " + advices);
		}
	}

	@Test
	public void coreServicesReferencedByModuleFactoryBean_shouldStillBeAuthorized() {
		Context.logout();
		assertThrows(APIAuthenticationException.class, () -> conceptService.getConcept(3));
	}

	@Test
	public void moduleFactoryBeanService_shouldBeMatchedByItsProxiedInterfaceOnly() {
		assertTrue(Arrays.asList(applicationContext.getBeanNamesForType(FactoryBeanModuleService.class))
		        .contains("test.FactoryBeanModuleService"));
		assertArrayEquals(new String[] { "test.FactoryBeanModuleServiceImpl" },
		    applicationContext.getBeanNamesForType(FactoryBeanModuleServiceImpl.class));
		assertTrue(AopUtils.isJdkDynamicProxy(applicationContext.getBean("test.FactoryBeanModuleService")));
		assertTrue(Arrays.asList(applicationContext.getBeanNamesForType(FactoryBeanModuleService.class))
		        .contains("test.FactoryBeanModuleInnerService"));
	}

	public interface FactoryBeanModuleService {}

	public static class FactoryBeanModuleServiceImpl implements FactoryBeanModuleService {

		public void setConceptService(ConceptService conceptService) {
		}

		public void setAdminService(AdministrationService adminService) {
		}
	}

	public static class FactoryBeanModuleInnerServiceImpl implements FactoryBeanModuleService {

		@Autowired
		private OrderService orderService;
	}
}
