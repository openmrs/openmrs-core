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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.openmrs.Patient;
import org.openmrs.security.DomainObjectAuthorizationRule;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Proves that {@code PatientService}'s {@code @PreAuthorize}/{@code @PostFilter} conversions
 * ({@link PatientService#getPatient(Integer)} - pre-checked by id, since the id is already known
 * before the fetch - {@link PatientService#getAllPatients()}, and
 * {@link PatientService#voidPatient(Patient, String)}) actually consult a registered
 * {@code DomainObjectAuthorizationRule} for "Patient", not just the underlying privilege - i.e.
 * that the wiring described on {@link org.openmrs.security.OpenmrsPermissionEvaluator} reaches real
 * service methods, not only the evaluator unit tests.
 * <p>
 * {@link DenyingPatientRule} is registered for the whole suite (picked up by the same
 * component-scan as any other bean), but denies nothing by default so it does not affect unrelated
 * tests; each test here toggles {@link DenyingPatientRule#setDeniedPatientId(Integer)} and restores
 * it afterward.
 */
public class PatientServiceHasPermissionTest extends BaseContextSensitiveTest {

	@Autowired
	private PatientService patientService;

	@Autowired
	private DenyingPatientRule rule;

	@AfterEach
	public void resetRule() {
		rule.setDeniedPatientId(null);
		rule.setDeniedPatientUuid(null);
	}

	@Test
	public void getPatient_shouldAllowWhenNoRuleDeniesTheRequestedPatientId() {
		assertEquals(2, patientService.getPatient(2).getPatientId());
	}

	@Test
	public void getPatient_shouldDenyByIdBeforeFetchingWhenTheRegisteredRuleDeniesIt() {
		rule.setDeniedPatientId(2);

		assertThrows(AccessDeniedException.class, () -> patientService.getPatient(2));
	}

	@Test
	public void getPatient_shouldNotDenyAPatientIdTheRuleDoesNotName() {
		rule.setDeniedPatientId(6);

		assertEquals(2, patientService.getPatient(2).getPatientId());
	}

	@Test
	public void getPatientByUuid_shouldDenyByUuidBeforeFetchingWhenTheRegisteredRuleDeniesIt() {
		String uuid = patientService.getPatient(2).getUuid();
		rule.setDeniedPatientUuid(uuid);

		assertThrows(AccessDeniedException.class, () -> patientService.getPatientByUuid(uuid));
	}

	@Test
	public void getAllPatients_shouldFilterOutOnlyThePatientTheRegisteredRuleDenies() {
		List<Patient> withNoDenial = patientService.getAllPatients();
		assertTrue(withNoDenial.stream().anyMatch(p -> p.getPatientId().equals(2)));

		rule.setDeniedPatientId(2);
		List<Patient> withPatientTwoDenied = patientService.getAllPatients();

		assertFalse(withPatientTwoDenied.stream().anyMatch(p -> p.getPatientId().equals(2)));
		// unaffected patients from the unfiltered call are still present
		assertEquals(withNoDenial.size() - 1, withPatientTwoDenied.size());
	}

	@Test
	public void voidPatient_shouldDenyBeforeVoidingWhenTheRegisteredRuleDeniesThePatient() {
		Patient patient = patientService.getPatient(2);
		rule.setDeniedPatientId(2);

		assertThrows(AccessDeniedException.class, () -> patientService.voidPatient(patient, "test"));

		rule.setDeniedPatientId(null);
		assertFalse(patientService.getPatient(2).getVoided(), "a denied @PreAuthorize call must not run the method body");
	}

	/**
	 * Denies exactly one, test-selected patient - by id or by uuid, matching whichever form
	 * {@link DomainObjectAuthorizationRule}'s id-based {@code isAuthorized} was actually called with
	 * (see its javadoc) - authorizing every other target (including every other domain type -
	 * {@link #getTargetType()} scopes it to {@code Patient} and its subtypes only). Off (denies
	 * nothing) by default so registering it does not change behavior for tests that do not use it.
	 */
	@Component
	public static class DenyingPatientRule implements DomainObjectAuthorizationRule {

		private volatile Integer deniedPatientId;

		private volatile String deniedPatientUuid;

		public void setDeniedPatientId(Integer deniedPatientId) {
			this.deniedPatientId = deniedPatientId;
		}

		public void setDeniedPatientUuid(String deniedPatientUuid) {
			this.deniedPatientUuid = deniedPatientUuid;
		}

		@Override
		public Class<?> getTargetType() {
			return Patient.class;
		}

		@Override
		public boolean isAuthorized(Authentication authentication, Object targetDomainObject, Object permission) {
			if (!(targetDomainObject instanceof Patient patient)) {
				return true;
			}
			// No active denial (both null): authorize regardless of the target's own id/uuid, even a
			// transient (unsaved, id == null) patient - otherwise Objects.equals(null, null) would
			// deny every not-yet-saved patient by accident.
			if (deniedPatientId != null && deniedPatientId.equals(patient.getPatientId())) {
				return false;
			}
			return deniedPatientUuid == null || !deniedPatientUuid.equals(patient.getUuid());
		}

		@Override
		public boolean isAuthorized(Authentication authentication, Serializable targetId, Class<?> targetType,
		        Object permission) {
			if (targetId instanceof Integer id) {
				return deniedPatientId == null || !deniedPatientId.equals(id);
			}
			if (targetId instanceof String uuid) {
				return deniedPatientUuid == null || !deniedPatientUuid.equals(uuid);
			}
			// an id form this rule does not recognize is not evidence of denial
			return true;
		}
	}
}
