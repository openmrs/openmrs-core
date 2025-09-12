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
	private LoggingAdvice loggingAdvice;
	
	@Autowired
	private RequiredDataAdvice requiredDataAdvice;
	
	@Autowired
	private AuthorizationAdvice authorizationAdvice;
	
	private static Method dummyMethod;

	
	@BeforeAll
	public static void before() throws NoSuchMethodException {
		dummyMethod = Object.class.getDeclaredMethod("toString");
	}
	
	/**
	 * @see org.openmrs.aop.AOPConfig#loggingAdvisor(org.openmrs.aop.LoggingAdvice)
	 */
	@Test
	public void loggingAdvisor_shouldMatchOnlyAnnotatedServiceClasses() {
		assertNotNull(loggingAdvice);
		
		StaticMethodMatcherPointcutAdvisor advisor = (StaticMethodMatcherPointcutAdvisor) new AOPConfig().loggingAdvisor(loggingAdvice);

		// Matches: class implements OpenmrsService AND is annotated with @Service
		assertTrue(advisor.matches(dummyMethod, TestClassAnnotatedExtends.class));

		// Fails: implements OpenmrsService but not annotated with @Service
		assertFalse(advisor.matches(dummyMethod, TestClassNotAnnotatedExtends.class));
		
		// Matches: annotated with @Service but does not implement OpenmrsService
		assertTrue(advisor.matches(dummyMethod, TestClassAnnotatedNotExtends.class));
	}
	
	/**
	 * @see org.openmrs.aop.AOPConfig#authorizationAdvisor(org.openmrs.aop.AuthorizationAdvice)
	 */
	@Test
	public void authorizationAdvisor_shouldMatchOnlyAnnotatedServiceClasses() {
		assertNotNull(authorizationAdvice);
		
		StaticMethodMatcherPointcutAdvisor advisor = (StaticMethodMatcherPointcutAdvisor) new AOPConfig().authorizationAdvisor(authorizationAdvice);

		// Matches: class implements OpenmrsService AND is annotated with @Service
		assertTrue(advisor.matches(dummyMethod, TestClassAnnotatedExtends.class));

		// Fails: implements OpenmrsService but not annotated with @Service
		assertFalse(advisor.matches(dummyMethod, TestClassNotAnnotatedExtends.class));

		// Matches: annotated with @Service but does not implement OpenmrsService
		assertTrue(advisor.matches(dummyMethod, TestClassAnnotatedNotExtends.class));
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

		// Matches: annotated with @Service but does not implement OpenmrsService
		assertTrue(advisor.matches(dummyMethod, TestClassAnnotatedNotExtends.class));
	}
	
	@Service
	public static class TestClassAnnotatedExtends  extends BaseOpenmrsService {}
	
	public static class TestClassNotAnnotatedExtends  extends BaseOpenmrsService {}
	
	@Service
	public static class TestClassAnnotatedNotExtends {}
}
