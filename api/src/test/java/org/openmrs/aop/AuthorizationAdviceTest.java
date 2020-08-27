/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openmrs.aop;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.junit.jupiter.api.Assertions.assertThrows;

import javax.annotation.Resource;
import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.openmrs.Concept;
import org.openmrs.PrivilegeListener;
import org.openmrs.User;
import org.openmrs.api.APIAuthenticationException;
import org.openmrs.api.context.Context;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;
import org.openmrs.util.PrivilegeConstants;
import org.springframework.stereotype.Component;

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
		        PrivilegeConstants.GET_CONCEPT_ATTRIBUTE_TYPES };
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
	
}
