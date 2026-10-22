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
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.hibernate.proxy.HibernateProxy;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Covers {@link Authorize}, the presentation-layer side of {@link DomainObjectAuthorizationRule}: a
 * rule's verdict applied to what is about to be serialized, with what it refuses replaced rather
 * than dropped so a reader can tell hidden from absent.
 * <p>
 * The properties worth pinning are the ones a serializer depends on and the one that keeps the
 * whole approach safe: a mask is still the declared type, it discloses only its identifiers, a
 * collection keeps its size, and a mask is refused by the write path instead of reaching Hibernate.
 * <p>
 * {@link MaskingPatientRule} is registered for the whole suite, like the other rules in these
 * tests, and refuses nothing until a test names a patient.
 */
public class AuthorizeTest extends BaseContextSensitiveTest {

	@AfterEach
	public void disarmRule() {
		MaskingPatientRule.deniedPatientId = null;
		MaskingPatientRule.timesAsked = 0;
	}

	private Patient patient(int id) {
		return Context.getPatientService().getPatient(id);
	}

	@Test
	public void maskIfDenied_shouldMaskTheObjectWhenARuleRefusesIt() {
		Patient patient = patient(2);
		MaskingPatientRule.deniedPatientId = 2;

		Patient masked = Authorize.maskIfDenied(patient);

		assertTrue(Authorize.isMasked(masked));
		// still the declared type, so it serializes where the real object would have
		assertNotNull(masked);
		assertEquals(Patient.class, ((MaskedObject) masked).getMaskedType());
	}

	@Test
	public void maskIfDenied_shouldDiscloseNothingAboutTheObjectItMasks() {
		Patient patient = patient(2);
		String uuid = patient.getUuid();
		assertNotNull(patient.getPersonName(), "the premise: this patient has a name to hide");
		MaskingPatientRule.deniedPatientId = 2;

		Patient masked = Authorize.maskIfDenied(patient);

		// a substitute identity rather than the real one: returning the uuid of a record the caller was
		// refused would confirm it exists and hand over a key to try elsewhere
		assertTrue(masked.getUuid().startsWith(Authorize.MASKED_UUID_PREFIX));
		assertNotEquals(uuid, masked.getUuid());
		assertNull(masked.getId());
		assertNull(masked.getPersonName(), "a getter the mask does not recognize must not reach the real object");
		assertNull(masked.getGender());
		// a collection getter comes back empty rather than null: a serializer handed null where it
		// expected a collection fails on the response it is halfway through writing
		assertTrue(masked.getNames().isEmpty());
		assertFalse(masked.getVoided());
	}

	@Test
	public void maskIfDenied_shouldReportOneStableIdentityPerMaskAndNoneAcrossMasks() {
		Patient patient = patient(2);
		MaskingPatientRule.deniedPatientId = 2;

		Patient masked = Authorize.maskIfDenied(patient);
		Patient maskedAgain = Authorize.maskIfDenied(patient);

		// stable while one object is written out, since a representation reads the uuid more than once
		assertEquals(masked.getUuid(), masked.getUuid());
		// and stable for nothing else: two masks of one record must not be correlatable
		assertNotEquals(masked.getUuid(), maskedAgain.getUuid());
	}

	@Test
	public void maskIfDenied_shouldReportAUuidAFhirIdAccepts() {
		Patient patient = patient(2);
		MaskingPatientRule.deniedPatientId = 2;

		String uuid = Authorize.maskIfDenied(patient).getUuid();

		// the reason a substitute is reported at all rather than nothing: fhir2's translators set
		// Resource.id from the uuid, and a resource with no id cannot sit in a searchset bundle, whose
		// entry.fullUrl is built from it. FHIR constrains an id to [A-Za-z0-9-.]{1,64}
		assertTrue(uuid.matches("[A-Za-z0-9\\-.]{1,64}"), uuid);

		// and a well-formed uuid besides, so anything that parses one is not handed something it rejects
		assertEquals(uuid, UUID.fromString(uuid).toString());
		assertEquals(4, UUID.fromString(uuid).version(), "the random groups keep a version 4 uuid's bits");
	}

	@Test
	public void maskIfDenied_shouldSerializeAMaskedObject() {
		// fetched before the rule is armed: getPatient is itself guarded by it
		Patient patient = patient(2);
		MaskingPatientRule.deniedPatientId = 2;
		Patient masked = Authorize.maskIfDenied(patient);

		// the whole object through a serializer, not just a string off it: the point of masking rather
		// than nulling is that whatever is about to write the response still can, and a mask has to
		// survive being walked getter by getter the way a serializer walks it
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
		String json = assertDoesNotThrow(() -> mapper.writeValueAsString(masked));

		assertTrue(json.contains(masked.getUuid()), json);
		assertFalse(json.contains("Horatio"), "nothing of the real patient may appear: " + json);
	}

	@Test
	public void maskIfDenied_shouldPrintNothingIdentifyingAboutAMask() {
		Patient patient = patient(2);
		MaskingPatientRule.deniedPatientId = 2;

		String printed = Authorize.maskIfDenied(patient).toString();

		// a JSP that prints an object prints this, so it must not carry the type or the substitute uuid
		assertEquals("[hidden]", printed);
	}

	@Test
	public void maskIfDenied_shouldAnswerEveryGetterOfAMaskWithoutThrowing() {
		Patient patient = patient(2);
		MaskingPatientRule.deniedPatientId = 2;
		Patient masked = Authorize.maskIfDenied(patient);

		// a consumer walks whatever getters it likes; none of them may blow up on the stand-in
		for (Method method : Patient.class.getMethods()) {
			if (method.getParameterCount() > 0 || void.class.equals(method.getReturnType())
			        || method.getName().startsWith("set")) {
				continue;
			}
			assertDoesNotThrow(() -> method.invoke(masked), method.getName());
		}
	}

	@Test
	public void maskIfDenied_shouldReturnAnUnmodifiableCollection() {
		List<Patient> given = new ArrayList<>(List.of(patient(2)));

		Collection<Patient> returned = Authorize.maskIfDenied(given);

		// the result may be a view of the argument when no rule is registered, so it has to refuse
		// mutation: a caller that trims it would otherwise be trimming the service's own collection,
		// which on a Hibernate association becomes deletes at flush
		assertThrows(UnsupportedOperationException.class, returned::clear);
		assertEquals(1, given.size());
	}

	@Test
	public void maskIfDenied_shouldRefuseAWriteToAMaskedObject() {
		Patient patient = patient(2);
		MaskingPatientRule.deniedPatientId = 2;
		Patient masked = Authorize.maskIfDenied(patient);

		assertThrows(UnsupportedOperationException.class, () -> masked.setGender("F"));
	}

	@Test
	public void maskIfDenied_shouldLeaveANullObjectAlone() {
		assertNull(Authorize.maskIfDenied((Patient) null));
	}

	@Test
	public void maskIfDenied_shouldKeepTheSizeAndReplaceOnlyWhatARuleRefuses() {
		List<Patient> patients = new ArrayList<>(List.of(patient(2), patient(6)));
		MaskingPatientRule.deniedPatientId = 2;

		Collection<Patient> filtered = Authorize.maskIfDenied(patients);

		assertEquals(2, filtered.size(), "a replaced entry still counts, so six with two hidden is not four");
		List<Patient> asList = new ArrayList<>(filtered);
		assertTrue(Authorize.isMasked(asList.get(0)));
		assertFalse(Authorize.isMasked(asList.get(1)));
		assertEquals(6, asList.get(1).getId(), "order is preserved");
	}

	@Test
	public void maskIfDenied_shouldNotMutateTheCollectionItWasGiven() {
		List<Patient> patients = new ArrayList<>(List.of(patient(2)));
		MaskingPatientRule.deniedPatientId = 2;

		Authorize.maskIfDenied(patients);

		// unlike @PostFilter, which filters in place - the reason a @PostFilter'ed @Cacheable method
		// has to key its cache by the caller
		assertEquals(1, patients.size());
		assertFalse(Authorize.isMasked(patients.get(0)));
	}

	@Test
	public void maskIfDenied_shouldLeaveANullCollectionAlone() {
		assertNull(Authorize.maskIfDenied((Collection<Patient>) null));
	}

	@Test
	public void save_shouldRejectAMaskedObjectRatherThanPersistIt() {
		Patient patient = patient(2);
		MaskingPatientRule.deniedPatientId = 2;
		Patient masked = Authorize.maskIfDenied(patient);
		MaskingPatientRule.deniedPatientId = null;

		// the constraint the whole approach rests on: a mask carries no data, so saving one would blank
		// the record it stands for, and as a proxy it would be inserted as new
		APIException thrown = assertThrows(APIException.class, () -> Context.getPatientService().savePatient(masked));
		assertTrue(thrown.getMessage().contains("cannot be written"), thrown.getMessage());
	}

	@Test
	public void save_shouldRejectAMaskedObjectInsideACollectionArgument() {
		Patient patient = patient(2);
		MaskingPatientRule.deniedPatientId = 2;
		Patient masked = Authorize.maskIfDenied(patient);
		MaskingPatientRule.deniedPatientId = null;

		assertThrows(APIException.class, () -> Context.getPatientService().mergePatients(patient(6), List.of(masked)));
	}

	@Test
	public void maskIfDenied_shouldConsultNoRuleForATypeNoneCovers() {
		Obs obs = new Obs();
		obs.setUuid("an-obs-no-rule-covers");

		// no Obs rule is registered, so this is the additive case: nothing refuses it
		assertSame(obs, Authorize.maskIfDenied(obs));
	}

	@Test
	public void maskIfDenied_shouldNotWrapAMaskAgain() {
		Patient patient = patient(2);
		MaskingPatientRule.deniedPatientId = 2;

		Patient masked = Authorize.maskIfDenied(patient);
		int askedToProduceTheMask = MaskingPatientRule.timesAsked;

		assertSame(masked, Authorize.maskIfDenied(masked), "a mask is not wrapped again");
		// not merely "came back unchanged": a rule asked about a mask is asked about a null id, which an
		// allow-list rule reads as refused or throws on
		assertEquals(askedToProduceTheMask, MaskingPatientRule.timesAsked, "a mask is not judged again");
	}

	@Test
	public void maskIfDenied_shouldNotJudgeACollectionElementThatIsAlreadyAMask() {
		Patient patient = patient(2);
		MaskingPatientRule.deniedPatientId = 2;
		Patient mask = Authorize.maskIfDenied(patient);
		int askedToProduceTheMask = MaskingPatientRule.timesAsked;

		Collection<Patient> judged = Authorize.maskIfDenied(List.of(mask));

		assertSame(mask, judged.iterator().next(), "the mask is passed through");
		assertEquals(askedToProduceTheMask, MaskingPatientRule.timesAsked, "and not judged again");
	}

	@Test
	public void maskIfDenied_shouldAskAPatientRuleAboutALazyPersonProxy() {
		Context.flushSession();
		Context.clearSession();
		Obs obs = Context.getObsService().getObs(7);
		assertInstanceOf(HibernateProxy.class, obs.getPerson(), "the premise: a lazy proxy typed Person");
		MaskingPatientRule.deniedPatientId = obs.getPersonId();

		assertTrue(Authorize.isMasked(Authorize.maskIfDenied(obs.getPerson())),
		    "a Patient rule has to reach a Person-typed proxy of a patient it refuses");
	}

	/**
	 * Refuses exactly one, test-selected patient, authorizing every other target. Off by default so
	 * registering it does not change behavior for tests that do not use it.
	 */
	@Component
	public static class MaskingPatientRule implements DomainObjectAuthorizationRule {

		static volatile Integer deniedPatientId;

		/** How many times the rule has been consulted, so a test can pin that it was not. */
		static volatile int timesAsked;

		@Override
		public Class<?> getTargetType() {
			return Patient.class;
		}

		@Override
		public boolean isAuthorized(Authentication authentication, Object targetDomainObject, Object permission) {
			timesAsked++;
			if (deniedPatientId == null || !(targetDomainObject instanceof Patient patient)) {
				return true;
			}
			return !deniedPatientId.equals(patient.getPatientId());
		}

		@Override
		public boolean isAuthorized(Authentication authentication, Serializable targetId, Class<?> targetType,
		        Object permission) {
			if (deniedPatientId == null) {
				return true;
			}
			// an id form this rule cannot interpret denies, as DomainObjectAuthorizationRule requires
			return targetId instanceof Integer id && !deniedPatientId.equals(id);
		}
	}
}
