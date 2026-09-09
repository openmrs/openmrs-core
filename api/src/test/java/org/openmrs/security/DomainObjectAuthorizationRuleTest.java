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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.proxy.HibernateProxy;
import org.hibernate.proxy.LazyInitializer;
import org.junit.jupiter.api.Test;
import org.openmrs.Patient;
import org.openmrs.Person;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.security.core.Authentication;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Verifies how {@link OpenmrsPermissionEvaluator} consults registered
 * {@link DomainObjectAuthorizationRule} beans: a target with no rule registered for its type is
 * authorized by default, a supporting rule can deny it, several supporting rules combine with
 * most-restrictive- wins, and a rule registered for an unrelated type is never consulted. Runs as a
 * {@link BaseContextSensitiveTest} only so {@code Context.hasPrivilege(...)} - which the underlying
 * privilege check still requires - has an authenticated (superuser) context to resolve against; the
 * evaluator itself is constructed directly with hand-built rules rather than autowired, so each
 * test controls exactly which rules are in play.
 */
public class DomainObjectAuthorizationRuleTest extends BaseContextSensitiveTest {

	private static final String PRIVILEGE = "Some Privilege";

	// public, with a visible constructor, so CGLIB can subclass it the way
	// @AuthorizeReturnObject's proxy factory would
	public static class TestTarget {}

	public static class TestTargetSubtype extends TestTarget {}

	private static class AnotherTestTarget {}

	/**
	 * Records whether it was consulted, so a test can assert a rule for an unrelated type - or one that
	 * should never be reached because the base privilege check already failed - was skipped rather than
	 * merely happening to agree.
	 */
	private static class FakeRule implements DomainObjectAuthorizationRule {

		private final Class<?> targetType;

		private final boolean authorized;

		private boolean called = false;

		FakeRule(Class<?> targetType, boolean authorized) {
			this.targetType = targetType;
			this.authorized = authorized;
		}

		@Override
		public Class<?> getTargetType() {
			return targetType;
		}

		@Override
		public boolean isAuthorized(Authentication authentication, Object targetDomainObject, Object permission) {
			called = true;
			return authorized;
		}

		@Override
		public boolean isAuthorized(Authentication authentication, Serializable targetId, Class<?> targetType,
		        Object permission) {
			called = true;
			return authorized;
		}

		boolean wasCalled() {
			return called;
		}
	}

	@Test
	public void hasPermission_shouldAuthorizeATargetWithNoRuleRegisteredForItsType() {
		OpenmrsPermissionEvaluator evaluator = new OpenmrsPermissionEvaluator(List.of());

		assertTrue(evaluator.hasPermission(null, new TestTarget(), PRIVILEGE));
		assertTrue(evaluator.hasPermission(null, 1, TestTarget.class.getName(), PRIVILEGE));
	}

	@Test
	public void hasPermission_shouldDenyWhenTheSupportingRuleDenies() {
		FakeRule denies = new FakeRule(TestTarget.class, false);
		OpenmrsPermissionEvaluator evaluator = new OpenmrsPermissionEvaluator(List.of(denies));

		assertFalse(evaluator.hasPermission(null, new TestTarget(), PRIVILEGE));
		assertTrue(denies.wasCalled());
	}

	@Test
	public void hasPermission_shouldDenyWhenTheSupportingRuleDeniesByIdAndType() {
		FakeRule denies = new FakeRule(TestTarget.class, false);
		OpenmrsPermissionEvaluator evaluator = new OpenmrsPermissionEvaluator(List.of(denies));

		assertFalse(evaluator.hasPermission(null, 1, TestTarget.class.getName(), PRIVILEGE));
		assertTrue(denies.wasCalled());
	}

	@Test
	public void hasPermission_shouldAuthorizeWhenEverySupportingRuleAgrees() {
		FakeRule first = new FakeRule(TestTarget.class, true);
		FakeRule second = new FakeRule(TestTarget.class, true);
		OpenmrsPermissionEvaluator evaluator = new OpenmrsPermissionEvaluator(List.of(first, second));

		assertTrue(evaluator.hasPermission(null, new TestTarget(), PRIVILEGE));
		assertTrue(first.wasCalled());
		assertTrue(second.wasCalled());
	}

	@Test
	public void hasPermission_shouldBeMostRestrictiveWinsWhenSupportingRulesDisagree() {
		FakeRule allows = new FakeRule(TestTarget.class, true);
		FakeRule denies = new FakeRule(TestTarget.class, false);
		OpenmrsPermissionEvaluator evaluator = new OpenmrsPermissionEvaluator(List.of(allows, denies));

		assertFalse(evaluator.hasPermission(null, new TestTarget(), PRIVILEGE));
	}

	@Test
	public void hasPermission_shouldConsultARuleRegisteredForASupertypeOfTheTarget() {
		FakeRule deniesTheSupertype = new FakeRule(TestTarget.class, false);
		OpenmrsPermissionEvaluator evaluator = new OpenmrsPermissionEvaluator(List.of(deniesTheSupertype));

		assertFalse(evaluator.hasPermission(null, new TestTargetSubtype(), PRIVILEGE));
		assertFalse(evaluator.hasPermission(null, 1, TestTargetSubtype.class.getName(), PRIVILEGE));
	}

	@Test
	public void hasPermission_shouldNotConsultARuleRegisteredForASubtypeOfTheTarget() {
		// the one direction assignability cannot cover: the target is only known to be a TestTarget
		FakeRule deniesTheSubtype = new FakeRule(TestTargetSubtype.class, false);
		OpenmrsPermissionEvaluator evaluator = new OpenmrsPermissionEvaluator(List.of(deniesTheSubtype));

		assertTrue(evaluator.hasPermission(null, new TestTarget(), PRIVILEGE));
		assertFalse(deniesTheSubtype.wasCalled());
	}

	@Test
	public void hasPermission_shouldResolveASimpleTargetTypeNameAgainstTheOpenmrsPackage() {
		FakeRule deniesPersons = new FakeRule(Person.class, false);
		OpenmrsPermissionEvaluator evaluator = new OpenmrsPermissionEvaluator(List.of(deniesPersons));

		assertFalse(evaluator.hasPermission(null, 1, "Person", PRIVILEGE), "'Person' should mean org.openmrs.Person");
		assertFalse(evaluator.hasPermission(null, 1, "org.openmrs.Person", PRIVILEGE));
		// and a simple name reaches subtypes of the rule's type too
		assertFalse(evaluator.hasPermission(null, 1, "Patient", PRIVILEGE));
	}

	@Test
	public void hasPermission_shouldHandARuleTheResolvedClassRatherThanTheName() {
		List<Class<?>> seen = new ArrayList<>();
		OpenmrsPermissionEvaluator evaluator = new OpenmrsPermissionEvaluator(List.of(new FakeRule(Person.class, true) {

			@Override
			public boolean isAuthorized(Authentication authentication, Serializable targetId, Class<?> targetType,
			        Object permission) {
				seen.add(targetType);
				return true;
			}
		}));

		assertTrue(evaluator.hasPermission(null, 1, "Patient", PRIVILEGE));
		assertEquals(List.of(Patient.class), seen);
	}

	@Test
	public void hasPermission_shouldAuthorizeOnThePrivilegeAloneWhenTheTargetTypeNamesNoClass() {
		FakeRule wouldDeny = new FakeRule(TestTarget.class, false);
		OpenmrsPermissionEvaluator evaluator = new OpenmrsPermissionEvaluator(List.of(wouldDeny));

		// a predicate that must not throw: an unresolvable name cannot be matched to a rule, and a rule
		// can only ever restrict, so the privilege check stands alone
		assertTrue(evaluator.hasPermission(null, 1, "NoSuchDomainType", PRIVILEGE));
		assertFalse(wouldDeny.wasCalled());
	}

	@Test
	public void hasPermission_shouldAuthorizeOnThePrivilegeAloneWhenTheTargetTypeIsNull() {
		FakeRule wouldDeny = new FakeRule(TestTarget.class, false);
		OpenmrsPermissionEvaluator evaluator = new OpenmrsPermissionEvaluator(List.of(wouldDeny));

		assertTrue(evaluator.hasPermission(null, 1, null, PRIVILEGE));
		assertFalse(wouldDeny.wasCalled());
	}

	@Test
	public void hasPermission_shouldNeverConsultARuleRegisteredForAnUnrelatedType() {
		FakeRule deniesEverythingForAnotherType = new FakeRule(AnotherTestTarget.class, false);
		OpenmrsPermissionEvaluator evaluator = new OpenmrsPermissionEvaluator(List.of(deniesEverythingForAnotherType));

		assertTrue(evaluator.hasPermission(null, new TestTarget(), PRIVILEGE));
		assertFalse(deniesEverythingForAnotherType.wasCalled());
	}

	@Test
	public void hasPermission_shouldNotConsultRulesWhenTheBasePrivilegeCheckAlreadyFails() {
		FakeRule wouldAuthorize = new FakeRule(TestTarget.class, true);
		OpenmrsPermissionEvaluator evaluator = new OpenmrsPermissionEvaluator(List.of(wouldAuthorize));

		assertFalse(evaluator.hasPermission(null, new TestTarget(), null));
		assertFalse(wouldAuthorize.wasCalled());
	}

	@Test
	public void hasPermission_shouldMatchARuleThroughASpringProxy() {
		// what @AuthorizeReturnObject hands back: a CGLIB subclass whose getSimpleName() is
		// TestTarget$$SpringCGLIB$$0, so matching on it directly would consult no rule at all
		FakeRule rule = new FakeRule(TestTarget.class, false);
		OpenmrsPermissionEvaluator evaluator = new OpenmrsPermissionEvaluator(List.of(rule));
		ProxyFactory factory = new ProxyFactory(new TestTarget());
		factory.setProxyTargetClass(true);

		assertFalse(evaluator.hasPermission(null, factory.getProxy(), PRIVILEGE));
		assertTrue(rule.called);
	}

	@Test
	public void hasPermission_shouldMatchARuleThroughAHibernateProxyWithoutInitializingIt() {
		// a lazily loaded association arrives as a Hibernate proxy; the type has to come from the
		// persistent class, since initializing it here would hit the database once per checked element
		LazyInitializer initializer = mock(LazyInitializer.class);
		when(initializer.getPersistentClass()).thenReturn((Class) TestTarget.class);
		HibernateProxy proxy = mock(HibernateProxy.class);
		when(proxy.getHibernateLazyInitializer()).thenReturn(initializer);
		FakeRule rule = new FakeRule(TestTarget.class, false);
		OpenmrsPermissionEvaluator evaluator = new OpenmrsPermissionEvaluator(List.of(rule));

		assertFalse(evaluator.hasPermission(null, proxy, PRIVILEGE));
		assertTrue(rule.called);
		verify(initializer, never()).getImplementation();
	}

	@Test
	public void hasPermission_shouldAuthorizeANullTargetWithoutConsultingAnyRule() {
		FakeRule wouldDeny = new FakeRule(TestTarget.class, false);
		OpenmrsPermissionEvaluator evaluator = new OpenmrsPermissionEvaluator(List.of(wouldDeny));

		assertTrue(evaluator.hasPermission(null, (Object) null, PRIVILEGE));
		assertTrue(evaluator.hasPermission(null, null, TestTarget.class.getName(), PRIVILEGE));
		assertFalse(wouldDeny.wasCalled());
	}
}
