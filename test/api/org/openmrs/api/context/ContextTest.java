package org.openmrs.api.context;

import junit.framework.Test;
import org.openmrs.BaseTest;
import junit.framework.TestSuite;

import org.openmrs.User;
import org.openmrs.api.UserService;

public class ContextTest extends BaseTest {

	public void testAuthentication() throws Exception {
		Context.authenticate("USER-1", "test");
	}

	public void testUserService() {
		UserService us = Context.getUserService();
		String username = "USER-1";
		User user = us.getUserByUsername(username);
		assertNotNull("user " + username, user);
		System.out.println("Successfully found user: " + user.getPersonName() + " (" + username + ")");
	}

	public static Test suite() {
		return new TestSuite(ContextTest.class, "OpenMRS context");
	}

}
