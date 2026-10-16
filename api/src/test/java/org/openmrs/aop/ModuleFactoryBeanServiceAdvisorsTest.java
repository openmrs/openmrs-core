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
import java.util.Set;

import org.aopalliance.aop.Advice;
import org.junit.jupiter.api.Test;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.ConceptService;
import org.openmrs.api.OrderService;
import org.openmrs.api.context.Context;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;
import org.springframework.aop.framework.Advised;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.transaction.annotation.AnnotationTransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionInterceptor;
import org.springframework.transaction.interceptor.TransactionProxyFactoryBean;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
		assertThrows(AccessDeniedException.class, () -> conceptService.getConcept(3));
	}

	@Test
	public void moduleFactoryBeanService_shouldBeMatchedByItsProxyTypeBeforeItIsCreated() {
		DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
		beanFactory.registerBeanDefinition("impl", new RootBeanDefinition(FactoryBeanModuleServiceImpl.class));
		registerFactoryBean(beanFactory, "service", new RuntimeBeanReference("impl"));
		registerFactoryBean(beanFactory, "innerService",
		    new BeanDefinitionHolder(new RootBeanDefinition(FactoryBeanModuleInnerServiceImpl.class), "inner"));
		registerFactoryBean(beanFactory, "optimizedService", new RuntimeBeanReference("impl")).getPropertyValues()
		        .add("optimize", "true");

		new ProxyFactoryBeanObjectTypePostProcessor().postProcessBeanFactory(beanFactory);
		// the application context does the same after running its post processors
		beanFactory.clearMetadataCache();

		Set<String> byInterface = Set.of(beanFactory.getBeanNamesForType(FactoryBeanModuleService.class));
		Set<String> byClass = Set.of(beanFactory.getBeanNamesForType(FactoryBeanModuleServiceImpl.class));
		assertEquals(0, beanFactory.getSingletonCount());
		assertEquals(Set.of("impl", "service", "innerService", "optimizedService"), byInterface);
		// an optimized proxy subclasses its target, the others only implement its interfaces
		assertEquals(Set.of("impl", "optimizedService"), byClass);

		beanFactory.preInstantiateSingletons();
		assertEquals(byInterface, Set.of(beanFactory.getBeanNamesForType(FactoryBeanModuleService.class)));
		assertEquals(byClass, Set.of(beanFactory.getBeanNamesForType(FactoryBeanModuleServiceImpl.class)));
	}

	@Test
	public void moduleFactoryBeanServiceWithFactoryBeanTarget_shouldStillBeMatchedByItsInterface() {
		DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
		beanFactory.registerBeanDefinition("impl", new RootBeanDefinition(FactoryBeanModuleServiceImpl.class));
		registerFactoryBean(beanFactory, "service", new RuntimeBeanReference("impl"));
		registerFactoryBean(beanFactory, "wrappingService", new RuntimeBeanReference("service"));

		new ProxyFactoryBeanObjectTypePostProcessor().postProcessBeanFactory(beanFactory);
		beanFactory.clearMetadataCache();

		// its type is left to Spring, which creates it to find out
		assertEquals(Set.of("impl", "service", "wrappingService"),
		    Set.of(beanFactory.getBeanNamesForType(FactoryBeanModuleService.class)));
	}

	private static RootBeanDefinition registerFactoryBean(DefaultListableBeanFactory beanFactory, String name,
	        Object target) {
		RootBeanDefinition definition = new RootBeanDefinition(TransactionProxyFactoryBean.class);
		definition.getPropertyValues().add("target", target).add("transactionAttributeSource",
		    new AnnotationTransactionAttributeSource());
		beanFactory.registerBeanDefinition(name, definition);
		return definition;
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
