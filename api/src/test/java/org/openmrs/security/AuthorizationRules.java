/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.security;

/**
 * Registers a {@link DomainObjectAuthorizationRule} with the evaluator for the duration of one
 * test, reaching {@code OpenmrsPermissionEvaluator}'s package-private hook on behalf of tests in
 * other packages.
 * <p>
 * Test rules are not beans on purpose. A {@code @Component} rule is in every context that scans
 * {@code org.openmrs} - the rest of core's suite, and every module's test context, since this test
 * jar is also where {@code BaseModuleContextSensitiveTest} lives and {@code TestTypeFilter} only
 * excludes a class whose direct superclass is a test base class, which a nested class's is not. A
 * registered rule changes which code runs even while it refuses nothing: the evaluator reports that
 * rules exist, and {@link Authorize} then copies a collection instead of returning a view of it,
 * initializing a lazy association on the way. Registered per test, every other test - core's and a
 * module's alike - runs the no-rule path a server without a rule module takes.
 * <p>
 * Register in {@code @BeforeEach} and unregister in {@code @AfterEach}: the evaluator is a
 * singleton of the one application context the suite shares, so a rule left behind is a rule every
 * later test in the JVM has.
 */
public final class AuthorizationRules {

	private AuthorizationRules() {
	}

	public static void register(OpenmrsPermissionEvaluator evaluator, DomainObjectAuthorizationRule... rules) {
		for (DomainObjectAuthorizationRule rule : rules) {
			evaluator.addRule(rule);
		}
	}

	public static void unregister(OpenmrsPermissionEvaluator evaluator, DomainObjectAuthorizationRule... rules) {
		for (DomainObjectAuthorizationRule rule : rules) {
			evaluator.removeRule(rule);
		}
	}
}
