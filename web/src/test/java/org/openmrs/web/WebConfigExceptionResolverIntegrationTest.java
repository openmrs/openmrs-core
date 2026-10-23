/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.context.annotation.Bean;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.handler.HandlerExceptionResolverComposite;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Proves {@link WebConfig#removeRethrowingAccessDeniedExceptionResolver(ObjectProvider)} actually
 * takes effect in a built context, which {@code WebConfigTest} cannot show: there the composite is
 * hand-assembled, so nothing checks that the removal runs late enough. Here Spring Security
 * contributes its resolver through the real {@code WebMvcConfigurer} machinery and the removal runs
 * as the {@link SmartInitializingSingleton} callback it is - the two orderings that have to line up
 * for an under-privileged user to reach {@code uncaughtException} rather than a bare 403.
 * <p>
 * Neither nested class is a {@code @Configuration}: this package is inside the {@code org.openmrs}
 * tree that {@code applicationContext-service.xml} component-scans, so a stereotype here would
 * register these beans into every context-sensitive test in the module. {@code @Bean} methods and
 * {@code @Enable...} imports are still honoured on an explicitly registered class.
 */
class WebConfigExceptionResolverIntegrationTest {

	private AnnotationConfigWebApplicationContext context;

	@AfterEach
	void closeContext() {
		if (context != null) {
			context.close();
		}
	}

	@Test
	void builtComposite_shouldNotContainTheRethrowingResolver() {
		HandlerExceptionResolverComposite composite = compositeFrom(WithRemoval.class);

		assertTrue(
		    composite.getExceptionResolvers().stream()
		            .noneMatch(resolver -> resolver.getClass().getName().contains("AccessDeniedExceptionResolver")),
		    composite.getExceptionResolvers().toString());
	}

	@Test
	void builtComposite_shouldLeaveADenialUnresolvedSoItFallsThroughToSimpleMappingExceptionResolver() {
		HandlerExceptionResolverComposite composite = compositeFrom(WithRemoval.class);

		assertNull(composite.resolveException(new MockHttpServletRequest(), new MockHttpServletResponse(), null,
		    new AccessDeniedException("denied")));
	}

	/**
	 * The same context without the removal bean, so the test above cannot pass for the wrong reason -
	 * if Spring Security ever stops contributing the resolver, this fails and the removal is moot.
	 */
	@Test
	void builtComposite_shouldContainTheRethrowingResolverWithoutTheRemovalBean() {
		HandlerExceptionResolverComposite composite = compositeFrom(WithoutRemoval.class);

		assertTrue(
		    composite.getExceptionResolvers().stream()
		            .anyMatch(resolver -> resolver.getClass().getName().contains("AccessDeniedExceptionResolver")),
		    composite.getExceptionResolvers().toString());
	}

	private HandlerExceptionResolverComposite compositeFrom(Class<?> configuration) {
		context = new AnnotationConfigWebApplicationContext();
		context.setServletContext(new MockServletContext());
		context.register(configuration);
		context.refresh();

		return context.getBean("handlerExceptionResolver", HandlerExceptionResolverComposite.class);
	}

	@EnableWebMvc
	@EnableMethodSecurity
	static class WithoutRemoval {}

	@EnableWebMvc
	@EnableMethodSecurity
	static class WithRemoval {

		@Bean
		SmartInitializingSingleton removeRethrowingAccessDeniedExceptionResolver(
		        ObjectProvider<HandlerExceptionResolverComposite> compositeProvider) {
			return new WebConfig().removeRethrowingAccessDeniedExceptionResolver(compositeProvider);
		}
	}
}
