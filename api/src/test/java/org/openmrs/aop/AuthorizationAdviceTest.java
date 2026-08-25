/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.aop;

import java.lang.reflect.Method;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;

import jakarta.annotation.Resource;

import org.junit.jupiter.api.Test;
import org.openmrs.Concept;
import org.openmrs.Patient;
import org.openmrs.PrivilegeListener;
import org.openmrs.User;
import org.openmrs.api.APIAuthenticationException;
import org.openmrs.api.ConceptService;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;
import org.openmrs.util.PrivilegeConstants;
import org.springframework.stereotype.Component;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests {@link AuthorizationAdvice}.
 */
public class AuthorizationAdviceTest extends BaseContextSensitiveTest {

	@Resource(name = "listener1")
	Listener1 listener1;

	@Resource(name = "listener2")
	Listener2 listener2;

	@Test
	public void before_shouldNotifyListenersAboutCheckedPrivileges() {

		listener1.hasPrivileges.clear();
		listener1.lacksPrivileges.clear();

		listener2.hasPrivileges.clear();
		listener2.lacksPrivileges.clear();

		Concept concept = Context.getConceptService().getConcept(3);

		assertThat("listener1", listener1.hasPrivileges, containsInAnyOrder(PrivilegeConstants.GET_CONCEPTS));
		assertThat("listener2", listener2.hasPrivileges, containsInAnyOrder(PrivilegeConstants.GET_CONCEPTS));
		assertThat(listener1.lacksPrivileges, empty());
		assertThat(listener2.lacksPrivileges, empty());

		listener1.hasPrivileges.clear();
		listener2.hasPrivileges.clear();

		Context.getConceptService().saveConcept(concept);

		String[] privileges = { PrivilegeConstants.MANAGE_CONCEPTS, PrivilegeConstants.GET_OBS,
		        PrivilegeConstants.GET_CONCEPT_ATTRIBUTE_TYPES, PrivilegeConstants.GET_GLOBAL_PROPERTIES,
		        PrivilegeConstants.GET_CONCEPTS };
		assertThat("listener1", listener1.hasPrivileges, containsInAnyOrder(privileges));
		assertThat("listener2", listener2.hasPrivileges, containsInAnyOrder(privileges));
		assertThat(listener1.lacksPrivileges, empty());
		assertThat(listener2.lacksPrivileges, empty());
	}

	@Component("listener1")
	public static class Listener1 implements PrivilegeListener {

		public Set<String> hasPrivileges = new LinkedHashSet<>();

		public Set<String> lacksPrivileges = new LinkedHashSet<>();

		@Override
		public void privilegeChecked(User user, String privilege, boolean hasPrivilege) {
			if (hasPrivilege) {
				hasPrivileges.add(privilege);
			} else {
				lacksPrivileges.add(privilege);
			}
		}
	}

	@Component("listener2")
	public static class Listener2 extends Listener1 {}

	@Test
	public void before_shouldThrowAPIAuthenticationException() {
		Context.getUserContext().logout();
		assertThrows(APIAuthenticationException.class, () -> Context.getConceptService().getConcept(3));
	}

	@Test
	public void before_shouldUseCachedMetadataOnRepeatedCalls() throws Throwable {
		Method getConceptMethod = ConceptService.class.getMethod("getConcept", Integer.class);
		AuthorizationAdvice advice = new AuthorizationAdvice();
		assertDoesNotThrow(() -> advice.before(getConceptMethod, new Object[] { 3 }, null));
		assertDoesNotThrow(() -> advice.before(getConceptMethod, new Object[] { 3 }, null));
	}

	@Test
	public void before_shouldCorrectlyAuthorizeWithRequireAll() throws Throwable {
		Method saveCauseOfDeathMethod = PatientService.class.getMethod("saveCauseOfDeathObs", Patient.class, Date.class,
		    Concept.class, String.class);
		AuthorizationAdvice advice = new AuthorizationAdvice();
		assertDoesNotThrow(() -> advice.before(saveCauseOfDeathMethod, new Object[] { null, null, null, null }, null));
	}

	@Test
	public void before_shouldRejectUnauthenticatedUserForAnnotatedMethod() {
		Context.getUserContext().logout();
		AuthorizationAdvice advice = new AuthorizationAdvice();

		try {
			Method getConceptMethod = ConceptService.class.getMethod("getConcept", Integer.class);
			assertThrows(APIAuthenticationException.class, () -> advice.before(getConceptMethod, new Object[] { 3 }, null));
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		}
	}

	@Test
	public void before_shouldCacheMetadataForDifferentMethodsIndependently() throws Throwable {
		AuthorizationAdvice advice = new AuthorizationAdvice();

		Method getConceptMethod = ConceptService.class.getMethod("getConcept", Integer.class);
		Method saveConceptMethod = ConceptService.class.getMethod("saveConcept", Concept.class);
		assertDoesNotThrow(() -> advice.before(getConceptMethod, new Object[] { 3 }, null));
		assertDoesNotThrow(() -> advice.before(saveConceptMethod, new Object[] { new Concept() }, null));
	}

}
