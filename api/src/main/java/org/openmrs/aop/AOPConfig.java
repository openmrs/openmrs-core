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

import java.lang.reflect.Method;

import org.aopalliance.aop.Advice;
import org.openmrs.api.OpenmrsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.Advisor;
import org.springframework.aop.support.StaticMethodMatcherPointcutAdvisor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.AnnotationCacheOperationSource;
import org.springframework.cache.interceptor.CacheInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.stereotype.Service;

/**
 * AOPConfig registers AOP advisors used across the OpenMRS service layer. It enables method-level interception using 
 * AspectJ-style pointcuts and integrates multiple aspects such as authorization, logging, required data handling, and caching.
 * <p>
 * The advisors apply to all classes implementing {@link org.openmrs.api.OpenmrsService}
 *
 * <p>
 * The configured advisors include:
 * <ul>
 *   <li><b>AuthorizationAdvisor</b> – Ensures access control based on authorization annotations.</li>
 *   <li><b>LoggingAdvisor</b> – Logs service method invocations and exceptions for auditing and debugging.</li>
 *   <li><b>RequiredDataAdvisor</b> – Automatically sets required metadata like `creator`, `dateCreated`, etc.</li>
 *   <li><b>CacheAdvisor</b> – Enables caching via Spring’s {@code @Cacheable} using a configured CacheInterceptor.</li>
 * </ul>
 *
 * <p>
 * This configuration replaces the older XML-based AOP setup and should be used in conjunction with
 * {@code @EnableAspectJAutoProxy} to ensure proxy-based interception is applied correctly.
 */

@Configuration
@EnableAspectJAutoProxy
public class AOPConfig {

	private static final Logger log = LoggerFactory.getLogger(AOPConfig.class);

	@Bean
	public Advisor authorizationAdvisor(@Qualifier("authorizationAdvice") AuthorizationAdvice advice) {
		return createAdvisor(advice);
	}

	@Bean
	public Advisor loggingAdvisor(@Qualifier("loggingAdvice") LoggingAdvice advice) {
		return createAdvisor(advice);
	}

	@Bean
	public Advisor requiredDataAdvisor(@Qualifier("requiredDataAdvice") RequiredDataAdvice advice) {
		return createAdvisor(advice);
	}

	@Bean
	public Advisor cacheAdvisor(@Qualifier("cacheInterceptor") CacheInterceptor interceptor) {
		return createAdvisor(interceptor);
	}

	@Bean(name = "cacheInterceptor")
	public CacheInterceptor cacheInterceptor(@Qualifier("apiCacheManager") CacheManager apiCacheManager,
		@Qualifier("annotationCacheOperationSource") AnnotationCacheOperationSource annotationCacheOperationSource) {

		CacheInterceptor interceptor = new CacheInterceptor();
		interceptor.setCacheManager(apiCacheManager);
		interceptor.setCacheOperationSources(annotationCacheOperationSource);
		return interceptor;
	}

	private Advisor createAdvisor(Advice advice) {
		return new StaticMethodMatcherPointcutAdvisor(advice) {
			@Override
			public boolean matches(Method method, Class<?> targetClass) {
				return targetClass.isAnnotationPresent(Service.class) 
					&& OpenmrsService.class.isAssignableFrom(targetClass);
			}
		};
	}
}
