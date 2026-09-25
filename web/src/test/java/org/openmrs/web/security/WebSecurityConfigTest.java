/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web.security;

import jakarta.servlet.Filter;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Proves the bean {@code web.xml}'s {@code DelegatingFilterProxy} looks up by name
 * ({@code springSecurityFilterChain}) actually exists and is a {@link Filter} once
 * {@link WebSecurityConfig} is loaded - this is easy to get backwards, since
 * {@code @EnableWebSecurity} auto-registers its own {@code Filter} bean under that exact name,
 * separate from any {@code @Bean SecurityFilterChain} method (see the javadoc on
 * {@link WebSecurityConfig#openmrsSecurityFilterChain(org.springframework.security.config.annotation.web.builders.HttpSecurity)}).
 * A naming collision here would only surface as a servlet-container startup failure, not a compile
 * or unit-test failure of {@link WebSecurityConfig} in isolation.
 */
class WebSecurityConfigTest {

	private AnnotationConfigApplicationContext context;

	@AfterEach
	void closeContext() {
		if (context != null) {
			context.close();
		}
	}

	@Test
	void contextLoads_shouldExposeAFilterBeanNamedSpringSecurityFilterChain() {
		context = new AnnotationConfigApplicationContext(WebSecurityConfig.class);

		Object bean = context.getBean("springSecurityFilterChain");
		assertNotNull(bean);
		org.junit.jupiter.api.Assertions.assertInstanceOf(Filter.class, bean);
	}
}
