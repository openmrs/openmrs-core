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

import java.util.Collection;
import java.util.Set;

import org.hibernate.Hibernate;
import org.junit.jupiter.api.Test;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.api.context.Context;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Covers {@link Authorize} on the installation nearly every one is: no
 * {@link DomainObjectAuthorizationRule} registered, because no module contributed one. Separate
 * from {@link AuthorizeTest}, which registers a rule and so can never reach this path.
 * <p>
 * What is worth pinning is that nothing is touched. A rule-free {@code maskIfDenied} hands back the
 * object itself and a view of the collection rather than a copy, and copying would call
 * {@code toArray()} on an association that arrives from Hibernate uninitialized - turning a
 * collection nobody asked for into a select, or a {@code LazyInitializationException} on a closed
 * session.
 */
public class AuthorizeWithoutRulesTest extends BaseContextSensitiveTest {

	@Autowired
	private OpenmrsPermissionEvaluator evaluator;

	@Test
	public void componentScan_shouldFindNoRuleInTheTestSources() {
		// this test jar is on every module's test classpath and applicationContext-service.xml scans
		// org.openmrs, so a @Component rule written for a test here is a rule in every module's test
		// context too - where it changes which path a read takes while refusing nothing
		assertTrue(applicationContext.getBeansOfType(DomainObjectAuthorizationRule.class).isEmpty(),
		    "a test rule has to be registered per test, not as a bean: "
		            + applicationContext.getBeansOfType(DomainObjectAuthorizationRule.class).keySet());
	}

	@Test
	public void maskIfDenied_shouldHandBackTheObjectItself() {
		assertFalse(evaluator.hasRules(), "the premise: a suite that registers no rule of its own");
		Patient patient = Context.getPatientService().getPatient(2);

		assertSame(patient, Authorize.maskIfDenied(patient));
	}

	@Test
	public void maskIfDenied_shouldNotInitializeALazyCollectionItIsGiven() {
		Context.flushSession();
		Context.clearSession();
		Set<PatientIdentifier> identifiers = Context.getPatientService().getPatient(2).getIdentifiers();
		assertFalse(Hibernate.isInitialized(identifiers), "the premise: an association no one has read yet");

		Collection<PatientIdentifier> returned = Authorize.maskIfDenied(identifiers);

		assertFalse(Hibernate.isInitialized(identifiers), "a view of the association, not a copy of it");
		// a view of the service's own collection, so it has to refuse mutation: a caller that trimmed it
		// would be scheduling deletes at flush
		assertThrows(UnsupportedOperationException.class, returned::clear);
	}
}
