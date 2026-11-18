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

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.openmrs.annotation.Authorized;
import org.openmrs.security.OpenmrsSecurityConfig;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;
import org.openmrs.util.PrivilegeConstants;
import org.springframework.aop.Advisor;
import org.springframework.aop.framework.Advised;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.interceptor.CacheInterceptor;
import org.springframework.core.Ordered;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authorization.method.AuthorizationInterceptorsOrder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionInterceptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pins where authorization sits in the woven advisor chain relative to caching and transactions.
 * The orders live in three unrelated places - {@link AOPConfig}, the {@code offset} on
 * {@link OpenmrsSecurityConfig}'s {@code @EnableMethodSecurity}, and Spring Security's
 * {@link AuthorizationInterceptorsOrder} - and moving any of them relocates a privilege check
 * without throwing.
 * <p>
 * Authorization inside caching means a cache hit returns before the check runs, serving the first
 * caller's result to everyone (what {@code offset = -700} prevents); moving the transaction advisor
 * between the pre- and post- interceptors would change whether a denied {@code @PostAuthorize}
 * rolls its write back, a trade-off documented on {@code OpenmrsSecurityConfig}. Asserted on a real
 * proxy rather than from the declared orders, since the woven result is what decides behaviour.
 */
public class AdvisorChainOrderTest extends BaseContextSensitiveTest {

	@Autowired
	private FullyAdvisedService service;

	@Test
	public void chain_shouldRunEveryAuthorizationCheckOutsideCachingAndTransactions() {
		List<Advisor> advisors = advisorsOf(service);

		int caching = orderOfAdviceType(advisors, CacheInterceptor.class);
		int transactions = orderOfAdviceType(advisors, TransactionInterceptor.class);
		int legacyAuthorized = orderOfAdviceType(advisors, AuthorizationAdvice.class);

		// a lower order runs further out, i.e. before the method on the way in
		assertTrue(legacyAuthorized < caching,
		    "@Authorized must run outside caching, or a cache hit skips the privilege check");
		assertTrue(legacyAuthorized < transactions, "@Authorized must run outside transactions");

		for (int methodSecurity : methodSecurityOrders()) {
			assertTrue(methodSecurity < caching, "method-security interceptor at order " + methodSecurity
			        + " must run outside caching (" + caching + "), or a cache hit returns before it checks anything");
			assertTrue(methodSecurity < transactions,
			    "method-security interceptor at order " + methodSecurity + " must run outside transactions (" + transactions
			            + "); moving the transaction advisor between the pre- and post- interceptors changes whether a"
			            + " denied @PostAuthorize rolls back - see OpenmrsSecurityConfig");
		}
	}

	@Test
	public void chain_shouldWeaveTheMethodSecurityInterceptorsAtTheConfiguredOffset() {
		// derived from the configuration rather than hard-coded, so this fails on an offset change
		// instead of silently agreeing with whatever is configured
		int offset = OpenmrsSecurityConfig.class.getAnnotation(EnableMethodSecurity.class).offset();
		List<Integer> expected = List.of(AuthorizationInterceptorsOrder.PRE_AUTHORIZE.getOrder() + offset,
		    AuthorizationInterceptorsOrder.POST_AUTHORIZE.getOrder() + offset,
		    AuthorizationInterceptorsOrder.POST_FILTER.getOrder() + offset);

		assertEquals(expected, methodSecurityOrders(),
		    "the woven pre/post interceptors should sit at their AuthorizationInterceptorsOrder value plus the"
		            + " configured offset, in that order");
	}

	/**
	 * Orders of the Spring Security method-security advisors woven onto {@link FullyAdvisedService},
	 * ascending. They cannot be told apart by advice type - all three arrive as the same
	 * {@code DeferringMethodInterceptor} behind an {@code AdvisorWrapper} - so they are identified by
	 * being the advisors that are not one of the OpenMRS or Spring ones.
	 */
	private List<Integer> methodSecurityOrders() {
		List<Integer> orders = new ArrayList<>();
		for (Advisor advisor : advisorsOf(service)) {
			Class<?> advice = advisor.getAdvice().getClass();
			boolean known = AuthorizationAdvice.class.isAssignableFrom(advice)
			        || LoggingAdvice.class.isAssignableFrom(advice) || RequiredDataAdvice.class.isAssignableFrom(advice)
			        || CacheInterceptor.class.isAssignableFrom(advice)
			        || TransactionInterceptor.class.isAssignableFrom(advice);
			if (!known) {
				orders.add(((Ordered) advisor).getOrder());
			}
		}
		return orders;
	}

	private static int orderOfAdviceType(List<Advisor> advisors, Class<?> adviceType) {
		for (Advisor advisor : advisors) {
			if (adviceType.isAssignableFrom(advisor.getAdvice().getClass())) {
				return ((Ordered) advisor).getOrder();
			}
		}
		throw new AssertionError(adviceType.getSimpleName() + " is not woven onto the test service, so this test is"
		        + " no longer checking what it claims - give FullyAdvisedService whatever annotation it needs");
	}

	private static List<Advisor> advisorsOf(Object bean) {
		Advised advised = assertInstanceOf(Advised.class, bean, "the test service should be proxied");
		return List.of(advised.getAdvisors());
	}

	/**
	 * Carries every annotation whose advisor this test asserts on, so all of them are woven onto one
	 * proxy - the method-security interceptors are pointcut-matched to methods actually carrying the
	 * annotation, so they are absent from a bean that has none.
	 */
	@Service
	public static class FullyAdvisedService {

		@Authorized(PrivilegeConstants.GET_CONCEPTS)
		@PreAuthorize("hasPermission(null, '" + PrivilegeConstants.GET_CONCEPTS + "')")
		@PostAuthorize("isAuthenticated()")
		@Transactional
		@Cacheable("testCache")
		public String guardedCachedTransactional(String key) {
			return "ok";
		}

		@PostFilter("isAuthenticated()")
		public List<String> postFiltered() {
			return List.of("a");
		}
	}
}
