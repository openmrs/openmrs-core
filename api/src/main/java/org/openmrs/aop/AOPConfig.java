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
import java.util.ArrayList;
import java.util.List;

import org.aopalliance.aop.Advice;
import org.springframework.aop.Advisor;
import org.springframework.aop.support.StaticMethodMatcherPointcutAdvisor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.AnnotationTransactionAttributeSource;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAttributeSource;

/**
 * AOPConfig registers AOP advisors used across the OpenMRS service layer. It enables method-level interception using 
 * AspectJ-style pointcuts and integrates multiple aspects such as authorization, logging, required data handling, and caching.
 * <p>
 * The advisors apply to all classes annotated with {@link Service}.
 *
 * <p>
 * The configured advisors include:
 * <ul>
 *   <li><b>AuthorizationAdvisor</b> – Ensures access control based on authorization annotations.</li>
 *   <li><b>LoggingAdvisor</b> – Logs service method invocations and exceptions for auditing and debugging.</li>
 *   <li><b>RequiredDataAdvisor</b> – Automatically sets required metadata like `creator`, `dateCreated`, etc.</li>
 *   <li><b>CacheInterceptor</b> - Supports caching for methods annotated with 
 *   {@link Cacheable}</li>
 *   <li><b>TransactionalInterceptor</b> - Supports transactions for methods annotated with 
 *   {@link Transactional}</li>
 * </ul>
 * <p>
 * In order to add advisors from a module create beans wrapped with {@link #createAdvisor(Advice, Integer)}, see e.g.
 * {@link #authorizationAdvisor(AuthorizationAdvice)}. Please note the order should be null or higher than 100 in order 
 * not to interfere with core advisors.
 * 
 * <p>
 * This configuration replaces the older XML-based AOP setup.
 * 
 * @since 3.0.0
 */
@Configuration
@EnableTransactionManagement(order = 5, proxyTargetClass = true)
@EnableCaching(order = 4, proxyTargetClass = true)
@EnableAspectJAutoProxy(proxyTargetClass = true)
public class AOPConfig {

	/**
	 * Added for backwards compatibility with services defined in xml with TransactionProxyFactoryBean
	 * @param authorizationAdvice
	 * @param loggingAdvice
	 * @param requiredDataAdvice
	 * @param cacheInterceptor
	 * @return serviceInterceptors
	 * @deprecated since 3.0.0 use {@link Service} annotation instead
	 */
	@Bean 
	public List<Advice> serviceInterceptors(AuthorizationAdvice authorizationAdvice,
											LoggingAdvice loggingAdvice, RequiredDataAdvice requiredDataAdvice,
											CacheInterceptor cacheInterceptor) {
		List<Advice> interceptors = new ArrayList<>();
		interceptors.add(authorizationAdvice);
		interceptors.add(loggingAdvice);
		interceptors.add(requiredDataAdvice);
		interceptors.add(cacheInterceptor);
		return interceptors;
	}

	/**
	 * Added for backwards compatibility with services defined in xml with TransactionProxyFactoryBean
	 * 
	 * @return transactionAttributeSource
	 * @deprecated since 3.0.0 use {@link Service} annotation instead
	 */
	@Bean
	public TransactionAttributeSource transactionAttributeSource() {
		return new AnnotationTransactionAttributeSource();
	}
	
	@Bean
	public Advisor authorizationAdvisor(AuthorizationAdvice advice) {
		return createAdvisor(advice, 1);
	}

	@Bean
	public Advisor loggingAdvisor(LoggingAdvice advice) {
		return createAdvisor(advice, 2);
	}

	@Bean
	public Advisor requiredDataAdvisor(RequiredDataAdvice advice) {
		return createAdvisor(advice,3);
	}

	public Advisor createAdvisor(Advice advice, Integer order) {
		StaticMethodMatcherPointcutAdvisor advisor = new StaticMethodMatcherPointcutAdvisor(advice) {
			@Override
			public boolean matches(Method method, Class<?> targetClass) {
				return targetClass.isAnnotationPresent(Service.class);
			}
		};
		if (order != null) {
			advisor.setOrder(order);
		}
		return advisor;
	}
}
