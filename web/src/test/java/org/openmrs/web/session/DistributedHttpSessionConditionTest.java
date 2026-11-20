/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web.session;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.ContextAuthenticationException;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.StandardAnnotationMetadata;
import org.springframework.mock.env.MockEnvironment;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

/**
 * The {@code session.distributed} flag decides whether the non-serializable
 * {@link org.openmrs.api.context.UserContext} may be replicated into a session store, so it must be
 * off unless an operator explicitly turned it on, and both consumers of it
 * ({@link DistributedHttpSessionCondition#matches} for the Spring config and
 * {@link DistributedHttpSessionCondition#isEnabled()} for the servlet filter) must agree. They
 * share one resolution path, which is what is verified here.
 */
class DistributedHttpSessionConditionTest {

	private final DistributedHttpSessionCondition condition = new DistributedHttpSessionCondition();

	private final StandardAnnotationMetadata metadata = new StandardAnnotationMetadata(
	        DistributedHttpSessionConditionTest.class);

	@AfterEach
	void clearFlag() {
		System.clearProperty(DistributedHttpSessionCondition.PROPERTY);
	}

	/**
	 * A bare {@code ConditionContext}: {@link DistributedHttpSessionCondition#matches} resolves the
	 * flag without touching the context (system property, env var, runtime properties), so it never
	 * needs the Spring {@code Environment}.
	 */
	private ConditionContext conditionContext() {
		return mock(ConditionContext.class);
	}

	@Test
	void shouldBeDisabledWhenTheFlagIsNotSet() {
		assertFalse(condition.matches(conditionContext(), metadata));
		assertFalse(DistributedHttpSessionCondition.isEnabled(), "the backend must be opt-in");
	}

	@Test
	void shouldFollowTheSystemPropertyForBothConsumers() {
		System.setProperty(DistributedHttpSessionCondition.PROPERTY, "true");

		assertTrue(condition.matches(conditionContext(), metadata));
		assertTrue(DistributedHttpSessionCondition.isEnabled());
	}

	/**
	 * Only an exact {@code true} may enable the backend: it decides whether the non-serializable
	 * {@code UserContext} is allowed into a replicated session store, so a typo like {@code yes} or
	 * {@code 1} must not silently switch the replication on. Both entry points are asserted, because a
	 * divergence between them is exactly what must never happen.
	 */
	@Test
	void shouldBeDisabledByAnyNonBooleanValue() {
		System.setProperty(DistributedHttpSessionCondition.PROPERTY, "yes");

		assertFalse(DistributedHttpSessionCondition.isEnabled());
		assertFalse(condition.matches(conditionContext(), metadata));
	}

	/**
	 * This condition runs during context refresh, and a {@code @Conditional} that throws aborts the
	 * whole refresh. An unreadable OpenMRS runtime properties source must therefore resolve to disabled
	 * rather than propagating.
	 */
	@Test
	void shouldFailSafeWhenTheRuntimePropertiesCannotBeRead() {
		try (MockedStatic<Context> contextMock = mockStatic(Context.class)) {
			contextMock.when(Context::getRuntimeProperties)
			        .thenThrow(new ContextAuthenticationException("Context is not initialised"));

			assertFalse(DistributedHttpSessionCondition.isEnabled());
			assertFalse(condition.matches(conditionContext(), metadata));
		}
	}

	@Test
	void shouldNotPropagateAFailureToReadTheFlag() {
		try (MockedStatic<Context> contextMock = mockStatic(Context.class)) {
			contextMock.when(Context::getRuntimeProperties).thenThrow(new IllegalStateException("boom"));

			assertDoesNotThrow(DistributedHttpSessionCondition::isEnabled);
		}
	}

	/**
	 * Regression guard for the two consumers agreeing. A value present only in the Spring
	 * {@link org.springframework.core.env.Environment} - a servlet context-param, a
	 * {@code @PropertySource} - must NOT enable the backend, because the servlet filter's
	 * {@link #isEnabled()} cannot see it. If {@code matches} read the Environment, the config would
	 * register while the filter stayed on the local path and pushed a non-serializable
	 * {@code UserContext} into a replicated session.
	 */
	@Test
	void shouldIgnoreAFlagVisibleOnlyToTheSpringEnvironment() {
		ConditionContext environmentOnly = mock(ConditionContext.class);
		when(environmentOnly.getEnvironment())
		        .thenReturn(new MockEnvironment().withProperty(DistributedHttpSessionCondition.PROPERTY, "true"));

		assertFalse(condition.matches(environmentOnly, metadata),
		    "a value only in the Spring Environment must not enable the config gate");
		assertFalse(DistributedHttpSessionCondition.isEnabled(), "and the servlet filter must agree");
	}
}
