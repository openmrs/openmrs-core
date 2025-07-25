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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Method;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;
import org.springframework.aop.support.StaticMethodMatcherPointcutAdvisor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.interceptor.CacheInterceptor;
import org.springframework.stereotype.Service;

/**
 * Tests the {@link org.openmrs.aop.AOPConfig} class.
 */
public class AOPConfigTest extends BaseContextSensitiveTest {
	
	@Autowired
	@Qualifier("loggingAdvice")
	private LoggingAdvice loggingAdvice;
	
	@Autowired
	@Qualifier("requiredDataAdvice")
	private RequiredDataAdvice requiredDataAdvice;
	
	@Autowired
	@Qualifier("authorizationAdvice")
	private AuthorizationAdvice authorizationAdvice;
	
	@Autowired
	@Qualifier("cacheInterceptor")
	private CacheInterceptor cacheInterceptor;
	
	private static Method dummyMethod;

	
	@BeforeAll
	public static void before() throws NoSuchMethodException {
		dummyMethod = Object.class.getDeclaredMethod("toString");
	}
	
	/**
	 * @see org.openmrs.aop.AOPConfig#loggingAdvisor(org.openmrs.aop.LoggingAdvice)
	 */
	@Test
	public void loggingAdvisor_shouldMatchOnlyAnnotatedOpenmrsServiceClasses() {
		assertNotNull(loggingAdvice);
		
		StaticMethodMatcherPointcutAdvisor advisor = (StaticMethodMatcherPointcutAdvisor) new AOPConfig().loggingAdvisor(loggingAdvice);

		// Matches: class implements OpenmrsService AND is annotated with @Service
		assertTrue(advisor.matches(dummyMethod, TestClassAnnotatedExtends.class));

		// Fails: implements OpenmrsService but not annotated with @Service
		assertFalse(advisor.matches(dummyMethod, TestClassNotAnnotatedExtends.class));
		
		// Fails: annotated with @Service but does not implement OpenmrsService
		assertFalse(advisor.matches(dummyMethod, TestClassAnnotatedNotExtends.class));
	}
	
	/**
	 * @see org.openmrs.aop.AOPConfig#authorizationAdvisor(org.openmrs.aop.AuthorizationAdvice)
	 */
	@Test
	public void authorizationAdvisor_shouldMatchOnlyAnnotatedOpenmrsServiceClasses() {
		assertNotNull(authorizationAdvice);
		
		StaticMethodMatcherPointcutAdvisor advisor = (StaticMethodMatcherPointcutAdvisor) new AOPConfig().authorizationAdvisor(authorizationAdvice);

		// Matches: class implements OpenmrsService AND is annotated with @Service
		assertTrue(advisor.matches(dummyMethod, TestClassAnnotatedExtends.class));

		// Fails: implements OpenmrsService but not annotated with @Service
		assertFalse(advisor.matches(dummyMethod, TestClassNotAnnotatedExtends.class));

		// Fails: annotated with @Service but does not implement OpenmrsService
		assertFalse(advisor.matches(dummyMethod, TestClassAnnotatedNotExtends.class));
	}
	
	/**
	 * @see org.openmrs.aop.AOPConfig#requiredDataAdvisor(org.openmrs.aop.RequiredDataAdvice)
	 */
	@Test
	public void requiredDataAdvisor_shouldMatchOnlyAnnotatedOpenmrsServiceClasses() {
		assertNotNull(requiredDataAdvice);
		
		StaticMethodMatcherPointcutAdvisor advisor = (StaticMethodMatcherPointcutAdvisor) new AOPConfig().requiredDataAdvisor(requiredDataAdvice);

		// Matches: class implements OpenmrsService AND is annotated with @Service
		assertTrue(advisor.matches(dummyMethod, TestClassAnnotatedExtends.class));

		// Fails: implements OpenmrsService but not annotated with @Service
		assertFalse(advisor.matches(dummyMethod, TestClassNotAnnotatedExtends.class));

		// Fails: annotated with @Service but does not implement OpenmrsService
		assertFalse(advisor.matches(dummyMethod, TestClassAnnotatedNotExtends.class));
	}
	
	/**
	 * @see org.openmrs.aop.AOPConfig#cacheAdvisor(org.springframework.cache.interceptor.CacheInterceptor)
	 */
	@Test
	public void cacheAdvisor_shouldMatchOnlyAnnotatedOpenmrsServiceClasses() {
		assertNotNull(cacheInterceptor);
		
		StaticMethodMatcherPointcutAdvisor advisor = (StaticMethodMatcherPointcutAdvisor) new AOPConfig().cacheAdvisor(cacheInterceptor);

		// Matches: class implements OpenmrsService AND is annotated with @Service
		assertTrue(advisor.matches(dummyMethod, TestClassAnnotatedExtends.class));

		// Fails: implements OpenmrsService but not annotated with @Service
		assertFalse(advisor.matches(dummyMethod, TestClassNotAnnotatedExtends.class));

		// Fails: annotated with @Service but does not implement OpenmrsService
		assertFalse(advisor.matches(dummyMethod, TestClassAnnotatedNotExtends.class));
	}
	
	@Service
	private static class TestClassAnnotatedExtends  extends BaseOpenmrsService {}
	
	private static class TestClassNotAnnotatedExtends  extends BaseOpenmrsService {}
	
	@Service
	private static class TestClassAnnotatedNotExtends {}
}
