/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openmrs.aop;

import java.util.LinkedHashSet;
import java.util.Set;

import javax.annotation.Resource;

import org.junit.Assert;
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
		
		Assert.assertArrayEquals("listener 1 get concept", new String[] { "Get Concepts" }, listener1.hasPrivileges
		        .toArray());
		Assert.assertArrayEquals("listener 2 get concept", new String[] { "Get Concepts" }, listener2.hasPrivileges
		        .toArray());
		Assert.assertEquals(0, listener1.lacksPrivileges.size());
		Assert.assertEquals(0, listener2.lacksPrivileges.size());
		
		listener1.hasPrivileges.clear();
		listener2.hasPrivileges.clear();
		
		Context.getConceptService().saveConcept(concept);
		
		Assert.assertArrayEquals("listener 1 save concept: " + listener1.hasPrivileges.toString(), new String[] {
		        "Manage Concepts", "Get Concepts", "Get Observations" }, listener1.hasPrivileges.toArray());
		Assert.assertArrayEquals("listener 2 save concept: " + listener2.hasPrivileges.toString(), new String[] {
		        "Manage Concepts", "Get Concepts", "Get Observations" }, listener2.hasPrivileges.toArray());
		Assert.assertEquals(0, listener1.lacksPrivileges.size());
		Assert.assertEquals(0, listener2.lacksPrivileges.size());
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
