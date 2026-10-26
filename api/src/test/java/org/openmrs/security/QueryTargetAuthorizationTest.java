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
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.Encounter;
import org.openmrs.Patient;
import org.openmrs.Person;
import org.openmrs.api.context.Context;
import org.openmrs.parameter.ObsSearchCriteria;
import org.openmrs.parameter.ObsSearchCriteriaBuilder;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;
import org.openmrs.util.PrivilegeConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Covers the two ways a search method hands a {@link DomainObjectAuthorizationRule} the objects a
 * query is restricted to, rather than only the privilege: a collection parameter
 * ({@code hasPermission(#whom, 'Get Observations')}) and a criteria object's property
 * ({@code hasPermission(#obsSearchCriteria?.whom, ...)}). The criteria overloads matter because
 * they are a second route to the same query - left on a bare {@code hasAuthority} a rule would be
 * bypassed by calling the newer overload.
 * <p>
 * Two things here are deliberate and easy to regress. A collection target is judged per element, so
 * the caller's collection never has to be {@link Serializable} - which the
 * {@code hasPermission(targetId, targetType, permission)} overload casts to and a
 * {@code List.subList(...)} view is not. And navigation into a criteria object is null-safe, so a
 * null criteria still reaches the service's own diagnostic instead of failing in SpEL.
 */
public class QueryTargetAuthorizationTest extends BaseContextSensitiveTest {

	@Autowired
	private OpenmrsPermissionEvaluator evaluator;

	private final RuleRecorder recorder = new RuleRecorder();

	private final PersonRule personRule = new PersonRule(recorder);

	private final PatientRule patientRule = new PatientRule(recorder);

	private final EncounterRule encounterRule = new EncounterRule(recorder);

	@BeforeEach
	public void registerRules() {
		AuthorizationRules.register(evaluator, personRule, patientRule, encounterRule);
	}

	@AfterEach
	public void unregisterRules() {
		AuthorizationRules.unregister(evaluator, personRule, patientRule, encounterRule);
	}

	@Test
	public void shouldConsultARuleForEachPersonACriteriaNames() {
		ObsSearchCriteria criteria = new ObsSearchCriteriaBuilder().setWhom(List.of(Context.getPersonService().getPerson(2)))
		        .createObsSearchCriteria();
		recorder.reset();

		Context.getObsService().getObservations(criteria);

		// one target per element, never the collection itself
		assertFalse(recorder.targets.isEmpty(), "a rule should have been consulted for the criteria's whom");
		for (Object target : recorder.targets) {
			assertInstanceOf(Person.class, target);
		}
	}

	@Test
	public void shouldDenyTheCriteriaOverloadWhenARuleRefusesAPersonTheQueryNames() {
		// built before the rules are armed: getPerson is itself @PostAuthorize'd on a Person, so an
		// already-denying rule would stop the fixture rather than the call under test
		ObsSearchCriteria criteria = new ObsSearchCriteriaBuilder().setWhom(List.of(Context.getPersonService().getPerson(2)))
		        .createObsSearchCriteria();
		recorder.deny = true;

		assertThrows(AccessDeniedException.class, () -> Context.getObsService().getObservations(criteria));
	}

	@Test
	public void shouldConsultNoRuleWhenTheCriteriaNamesNobody() {
		Context.getObsService().getObservations(new ObsSearchCriteriaBuilder().createObsSearchCriteria());

		// a null target is authorized on the privilege alone
		assertTrue(recorder.targets.isEmpty());
	}

	@Test
	public void shouldReportANullCriteriaRatherThanFailingInSpel() {
		IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
		    () -> Context.getObsService().getObservations((ObsSearchCriteria) null));

		assertEquals("obsSearchCriteria cannot be null", thrown.getMessage());
	}

	@Test
	public void shouldAcceptACollectionTargetThatIsNotSerializable() {
		List<Encounter> page = new ArrayList<>(List.of(new Encounter(1), new Encounter(2), new Encounter(3))).subList(0, 2);
		// the premise: paging hands this method a view, and the 3-arg hasPermission overload would fail
		// its cast to Serializable on one
		assertFalse(page instanceof Serializable);

		assertDoesNotThrow(() -> Context.getEncounterService().filterEncountersByViewPermissions(page, null));
	}

	@Test
	public void shouldDenyWhenARuleRefusesOneElementOfACollectionTarget() {
		List<Encounter> encounters = new ArrayList<>(List.of(new Encounter(1), new Encounter(2)));
		recorder.deny = true;

		assertThrows(AccessDeniedException.class,
		    () -> Context.getEncounterService().filterEncountersByViewPermissions(encounters, null));
	}

	@Test
	public void shouldConsultARuleForEachIdWhenTheNamedTargetIsACollectionOfIds() {
		Context.getProgramWorkflowService().getPatientProgramAttributeByAttributeName(List.of(2, 6), "any");

		// one target per id, never the collection itself - and judged as the named type, since a
		// List<Integer> carries nothing to infer "Patient" from
		assertFalse(recorder.targets.isEmpty(), "a rule should have been consulted for each patient id");
		for (Object target : recorder.targets) {
			assertInstanceOf(Integer.class, target);
		}
		assertTrue(recorder.targets.containsAll(List.of(2, 6)));
	}

	@Test
	public void shouldDenyWhenARuleRefusesOneIdOfACollectionOfIds() {
		recorder.deny = true;

		assertThrows(AccessDeniedException.class,
		    () -> Context.getProgramWorkflowService().getPatientProgramAttributeByAttributeName(List.of(2, 6), "any"));
	}

	@Test
	public void shouldAuthorizeOnThePrivilegeAloneWhenAnIdCannotBeHandedToARule() {
		// not Serializable, so it cannot reach the rule's id-and-type method at all; authorizing is the
		// same choice an unresolvable targetType makes, since a rule can only restrict and never grant
		Object unserializable = new Object();
		recorder.deny = true;

		assertTrue(evaluator.hasPermission(null, new ArrayList<>(List.of(unserializable)), "Patient",
		    PrivilegeConstants.GET_PATIENT_PROGRAMS));
		assertTrue(recorder.targets.isEmpty(), "no rule can be consulted for an id it cannot be given");
	}

	/**
	 * What the rules record, shared between them so each stays a trivial type-specific rule. One per
	 * test, like the rules themselves, so nothing a previous test recorded can be read here.
	 */
	static final class RuleRecorder {

		final Collection<Object> targets = new CopyOnWriteArrayList<>();

		volatile boolean deny = false;

		boolean record(Object target) {
			targets.add(target);
			return !deny;
		}

		void reset() {
			targets.clear();
			deny = false;
		}
	}

	/**
	 * Authorizes everything unless a test arms it. Registered per type because a rule is selected by
	 * the target's runtime type - {@code List<Person>} of patients is judged as {@code "Patient"}.
	 */
	abstract static class RecordingRule implements DomainObjectAuthorizationRule {

		private final RuleRecorder recorder;

		RecordingRule(RuleRecorder recorder) {
			this.recorder = recorder;
		}

		@Override
		public boolean isAuthorized(Authentication authentication, Object targetDomainObject, Object permission) {
			return recorder.record(targetDomainObject);
		}

		@Override
		public boolean isAuthorized(Authentication authentication, Serializable targetId, Class<?> targetType,
		        Object permission) {
			return recorder.record(targetId);
		}
	}

	public static class PersonRule extends RecordingRule {

		PersonRule(RuleRecorder recorder) {
			super(recorder);
		}

		@Override
		public Class<?> getTargetType() {
			return Person.class;
		}
	}

	public static class PatientRule extends RecordingRule {

		PatientRule(RuleRecorder recorder) {
			super(recorder);
		}

		@Override
		public Class<?> getTargetType() {
			return Patient.class;
		}
	}

	public static class EncounterRule extends RecordingRule {

		EncounterRule(RuleRecorder recorder) {
			super(recorder);
		}

		@Override
		public Class<?> getTargetType() {
			return Encounter.class;
		}
	}
}
