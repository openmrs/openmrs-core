package org.openmrs.api.ibatis;

import java.util.LinkedList;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.openmrs.User;
import org.openmrs.api.UserService;
import org.openmrs.context.Context;
import org.openmrs.context.ContextAuthenticationException;
import org.openmrs.context.ContextFactory;

public class IbatisUserServiceTest extends TestCase {
	
	protected UserService us;
	protected Context context;
	
	public void setUp(){
		context = ContextFactory.getContext();
		
		try {
			context.authenticate("3-4", "test");
		} catch (ContextAuthenticationException e) {
			
		}
		
		us = context.getUserService();
	}

	public void testUpdateUser() {
		assertTrue(context.isAuthenticated());
		User u = us.getUserByUsername("bwolfe");
		u.setFirstName("Ben");
		u.setMiddleName("Alexander");
		u.setLastName("Wolfe");
		u.setUsername("bwolfe");
		u.setRoles(new LinkedList());
		
		us.updateUser(u);
		
		User u2 = us.getUserByUsername("bwolfe");
		
		assertTrue(u.equals(u2));
		
		
		
	}
	
	public static Test suite() {
		return new TestSuite(IbatisUserServiceTest.class, "Basic IbatisUserService functionality");
	}

}
