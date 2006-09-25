package org.openmrs.api.context;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.openmrs.User;
import org.openmrs.api.UserService;

public class ContextTest extends TestCase {

	public void testAuthentication() throws Exception {
		ContextFactory.getContext().authenticate("USER-1", "test");
	}

	public void testUserService() {
		UserService us = ContextFactory.getContext().getUserService();
		String username = "USER-1";
		User user = us.getUserByUsername(username);
		assertNotNull("user " + username, user);
		System.out.println("Successfully found user: " + user.getFirstName()
				+ " " + user.getLastName() + " (" + username + ")");
	}

	public static Test suite() {
		return new TestSuite(ContextTest.class, "OpenMRS context");
	}

}
