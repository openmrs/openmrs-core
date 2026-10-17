/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.openmrs.Cohort;
import org.openmrs.Encounter;
import org.openmrs.api.PatientServiceHasPermissionTest.DenyingPatientRule;
import org.openmrs.security.DomainObjectAuthorizationRule;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Guards the rules reaching {@link EncounterService#getAllEncounters(Cohort)}, whose
 * {@code @PreAuthorize} can only ever hand them the {@link Cohort} itself - no {@code Patient} rule
 * is consulted for a cohort, and none sees the encounters either, since the result is a
 * {@link Map}. Without the {@code @PostFilter} a module restricting patients by rule still serves a
 * denied patient's encounters through here, silently, while {@code getPatient} and
 * {@code getEncountersByPatientId} deny.
 * <p>
 * Each half of the filter is pinned separately, since the key and the value reach a rule by
 * different routes: the key is a patient id, so it needs its type named to resolve a
 * {@code Patient} rule at all, while the value is judged per encounter on its own runtime type.
 * <p>
 * {@link DenyingPatientRule} is reused rather than duplicated; both it and
 * {@link DenyingEncounterRule} deny nothing until a test arms them.
 */
public class EncounterServiceHasPermissionTest extends BaseContextSensitiveTest {

	private static final int PATIENT_WITH_THREE_ENCOUNTERS = 7;

	private static final int PATIENT_WITH_ONE_ENCOUNTER = 2;

	@Autowired
	private EncounterService encounterService;

	@Autowired
	private DenyingPatientRule patientRule;

	@Autowired
	private DenyingEncounterRule encounterRule;

	@AfterEach
	public void resetRules() {
		patientRule.setDeniedPatientId(null);
		patientRule.setDeniedPatientUuid(null);
		encounterRule.setDeniedEncounterId(null);
	}

	private Map<Integer, List<Encounter>> encountersOfBothPatients() {
		return encounterService
		        .getAllEncounters(new Cohort(List.of(PATIENT_WITH_ONE_ENCOUNTER, PATIENT_WITH_THREE_ENCOUNTERS)));
	}

	@Test
	public void getAllEncounters_shouldDropTheEntryOfAPatientARuleDenies() {
		assertTrue(encountersOfBothPatients().containsKey(PATIENT_WITH_THREE_ENCOUNTERS),
		    "the premise: the cohort query returns this patient undenied");

		patientRule.setDeniedPatientId(PATIENT_WITH_THREE_ENCOUNTERS);
		Map<Integer, List<Encounter>> encounters = encountersOfBothPatients();

		assertFalse(encounters.containsKey(PATIENT_WITH_THREE_ENCOUNTERS),
		    "a Patient rule must reach the patient id each entry is keyed by");
		assertTrue(encounters.containsKey(PATIENT_WITH_ONE_ENCOUNTER), "the other patient is unaffected");
	}

	@Test
	public void getAllEncounters_shouldDropAPatientsEntryWhenARuleDeniesOneOfTheirEncounters() {
		Encounter denied = encountersOfBothPatients().get(PATIENT_WITH_THREE_ENCOUNTERS).get(0);
		assertEquals(3, encountersOfBothPatients().get(PATIENT_WITH_THREE_ENCOUNTERS).size());

		encounterRule.setDeniedEncounterId(denied.getEncounterId());

		// the whole entry goes, not just the denied encounter: a @PostFilter over a map can only drop
		// entries, which is the trade-off getRelationshipMap already makes
		assertFalse(encountersOfBothPatients().containsKey(PATIENT_WITH_THREE_ENCOUNTERS),
		    "an Encounter rule must reach the encounters each entry holds");
	}

	/**
	 * Denies exactly one, test-selected encounter, authorizing every other target. Off by default so
	 * registering it does not change behavior for tests that do not use it.
	 */
	@Component
	public static class DenyingEncounterRule implements DomainObjectAuthorizationRule {

		private volatile Integer deniedEncounterId;

		public void setDeniedEncounterId(Integer deniedEncounterId) {
			this.deniedEncounterId = deniedEncounterId;
		}

		@Override
		public Class<?> getTargetType() {
			return Encounter.class;
		}

		@Override
		public boolean isAuthorized(Authentication authentication, Object targetDomainObject, Object permission) {
			if (deniedEncounterId == null || !(targetDomainObject instanceof Encounter encounter)) {
				return true;
			}
			return !deniedEncounterId.equals(encounter.getEncounterId());
		}

		@Override
		public boolean isAuthorized(Authentication authentication, Serializable targetId, Class<?> targetType,
		        Object permission) {
			if (deniedEncounterId == null) {
				return true;
			}
			// an id form this rule cannot interpret denies, as DomainObjectAuthorizationRule requires
			return targetId instanceof Integer id && !deniedEncounterId.equals(id);
		}
	}
}
