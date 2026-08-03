/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.annotation;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Verifies that {@link OpenmrsProfile} is self-enforcing via {@link OpenmrsProfileCondition}:
 * profile gating must work even inside a component-scan that does NOT register the OpenMRS
 * TypeFilters (OpenmrsProfileIncludeFilter / OpenmrsProfileExcludeFilter) — i.e. the module-scan
 * escape scenario from mekomsolutions/openmrs-module-initializer#325.
 *
 * @since 3.0.0
 */
public class OpenmrsProfileConditionTest {

	private AnnotationConfigApplicationContext ctx;

	@BeforeEach
	public void setUp() {
		ctx = new AnnotationConfigApplicationContext();
		// Simulate a module's own component-scan: default filters only,
		// no OpenmrsProfileIncludeFilter / OpenmrsProfileExcludeFilter registered
		ctx.scan("org.openmrs.annotation");
		ctx.refresh();
	}

	@AfterEach
	public void tearDown() {
		if (ctx != null) {
			ctx.close();
		}
	}

	/**
	 * Regression test for mekomsolutions/openmrs-module-initializer#325. A bean with a
	 * non-matching @OpenmrsProfile must NOT be registered even when discovered by a scan that does not
	 * have the OpenMRS TypeFilters.
	 */
	@Test
	public void moduleScan_shouldNotRegisterBeanWhenProfileDoesNotMatch() {
		assertThrows(NoSuchBeanDefinitionException.class, () -> ctx.getBean(OpenmrsProfileModuleScanNonMatchingBean.class));
	}

	/**
	 * A bare @OpenmrsProfile bean (no version/module constraint) must be registered when discovered by
	 * a scan that does not have the OpenMRS TypeFilters.
	 */
	@Test
	public void moduleScan_shouldRegisterBeanWhenProfileMatches() {
		OpenmrsProfileModuleScanMatchingBean bean = ctx.getBean(OpenmrsProfileModuleScanMatchingBean.class);

		assertThat(bean, is(notNullValue()));
	}
}
