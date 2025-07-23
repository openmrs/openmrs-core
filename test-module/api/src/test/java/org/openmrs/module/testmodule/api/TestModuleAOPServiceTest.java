/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.testmodule.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.openmrs.test.jupiter.BaseModuleContextSensitiveTest;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;


/**
 * Tests {@link org.openmrs.module.testmodule.api.TestModuleAOPService}.
 */
public class TestModuleAOPServiceTest extends BaseModuleContextSensitiveTest {
	
	@Autowired
	@Qualifier("testModuleAOPService")
	private TestModuleAOPService aopService;


	/**
	 * @see org.openmrs.module.testmodule.api.TestModuleAOPService#aopHello()
	 */
	@Test
	public void testAopHello() {
		assertEquals("AOP Hello", aopService.aopHello());
	}

	@Test
	public void xmlServiceDefined_shouldHaveInterceptorsApplied() {
		assertNotNull(aopService);
		
		Advised advised = (Advised) aopService;
		List<String> expectedAdvices = Arrays.asList(
			"AuthorizationAdvice", "LoggingAdvice", "RequiredDataAdvice", "CacheInterceptor");

		boolean hasAllExpectedAdvice = expectedAdvices.stream()
			.allMatch(expected -> hasAdvice(advised, expected));
		assertTrue(hasAllExpectedAdvice);
	}

	@Test
	public void xmlServiceDefined_shouldNotHaveDuplicateAdvices() {
		assertNotNull(aopService);

		List<String> expectedAdvices = Arrays.asList(
			"AuthorizationAdvice", "LoggingAdvice", "RequiredDataAdvice", "CacheInterceptor");

		// Collect all advice class simple names applied to the proxy
		Advised advised = (Advised) aopService;
		List<String> appliedAdviceNames = Arrays.stream(advised.getAdvisors())
			.map(advisor -> advisor.getAdvice().getClass().getSimpleName())
			.collect(Collectors.toList());

		// Count occurrences of each expected advice
		for (String expected : expectedAdvices) {
			long count = appliedAdviceNames.stream()
				.filter(name -> name.equals(expected))
				.count();

			assertEquals(1, count);
		}
	}
	
	private boolean hasAdvice(Advised advised, String adviceName) {
		return Arrays.stream(advised.getAdvisors())
			.anyMatch(advisor -> advisor.getAdvice().getClass().getSimpleName().contains(adviceName));
	}
}
