package org.openmrs.api;

import java.util.HashSet;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.openmrs.User;
import org.openmrs.context.Context;
import org.openmrs.context.ContextAuthenticationException;
import org.openmrs.context.ContextFactory;

public class UserServiceTest extends TestCase {
	
	protected UserService us;
	protected Context context;
	
	public void setUp(){
		context = ContextFactory.getContext();
		
		try {
			context.authenticate("admin", "test");
		} catch (ContextAuthenticationException e) {
			
		}
		
		us = context.getUserService();
	}

	public void testUpdateUser() {
		assertTrue(context.isAuthenticated());
		User u = us.getUserByUsername("bwolfe");
		if (u == null)
			u = new User();
		u.setFirstName("Ben");
		u.setMiddleName("Alexander");
		u.setLastName("Wolfe");
		u.setUsername("bwolfe");
		u.setRoles(new HashSet());
		
		us.updateUser(u);
		
		User u2 = us.getUserByUsername("bwolfe");
		
		assertTrue(u.equals(u2));
		
		
		
	}
	
	public static Test suite() {
		return new TestSuite(UserServiceTest.class, "Basic UserService functionality");
	}

}
