package org.openmrs.api.db.hibernate;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.openmrs.BaseTest;
import org.openmrs.User;
import org.openmrs.api.UserService;
import org.openmrs.api.context.Context;

public class HibernateTest extends BaseTest {
		
	public void testGetName() throws Exception {
		
		Context.authenticate("USER-1", "test");
		
		UserService us = Context.getUserService();
		
		User u = us.getUserByUsername("USER-1");
		
		System.out.println("last name: " + u.getPersonName().getFamilyName());
	}
	
	public static Test suite() {
		return new TestSuite(HibernateTest.class, "Basic Hibernate functionality");
	}
	
}
