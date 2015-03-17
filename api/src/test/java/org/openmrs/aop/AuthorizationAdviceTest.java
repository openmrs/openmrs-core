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

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.junit.Assert.assertThat;

import java.util.LinkedHashSet;
import java.util.Set;

import javax.annotation.Resource;

import org.junit.Test;
import org.openmrs.Concept;
import org.openmrs.PrivilegeListener;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.test.Verifies;
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
	@Verifies(value = "notify listeners about checked privileges", method = "before(Method, Object[], Object)")
	public void before_shouldNotifyListenersAboutCheckedPrivileges() {
		listener1.hasPrivileges.clear();
		listener1.lacksPrivileges.clear();
		
		listener2.hasPrivileges.clear();
		listener2.lacksPrivileges.clear();
		
		Concept concept = Context.getConceptService().getConcept(3);
		
		assertThat("listener1", listener1.hasPrivileges, containsInAnyOrder("Get Concepts"));
		assertThat("listener2", listener2.hasPrivileges, containsInAnyOrder("Get Concepts"));
		assertThat(listener1.lacksPrivileges, empty());
		assertThat(listener2.lacksPrivileges, empty());
		
		listener1.hasPrivileges.clear();
		listener2.hasPrivileges.clear();
		
		Context.getConceptService().saveConcept(concept);
		
		assertThat("listener1", listener1.hasPrivileges, containsInAnyOrder("Manage Concepts", "Get Observations"));
		assertThat("listener2", listener2.hasPrivileges, containsInAnyOrder("Manage Concepts", "Get Observations"));
		assertThat(listener1.lacksPrivileges, empty());
		assertThat(listener2.lacksPrivileges, empty());
	}
	
	@Component("listener1")
	public static class Listener1 implements PrivilegeListener {
		
		//We need to preserve order due to the semantics of Assert.assertArrayEquals
		public Set<String> hasPrivileges = new LinkedHashSet<String>();
		
		public Set<String> lacksPrivileges = new LinkedHashSet<String>();
		
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
}
