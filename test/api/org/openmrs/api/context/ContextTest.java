package org.openmrs.api.context;

import org.openmrs.BaseTest;
import org.openmrs.User;
import org.openmrs.api.UserService;

public class ContextTest extends BaseTest {
	
	@Override
	protected void onSetUpBeforeTransaction() throws Exception {
		super.onSetUpBeforeTransaction();
		authenticate();
	}

	public void testGettingUser() {
		Context.openSession();
		
		UserService us = Context.getUserService();
		String username = "admin";
		User user = us.getUserByUsername(username);
		assertNotNull("user " + username, user);
		System.out.println("Successfully found user: " + user.getPersonName() + " (" + username + ")");
		
		Context.closeSession();
	}
	
	public void testProxyPrivilege() {
		
		Context.openSession();
		
		//create a bum user
		
		// make sure they can't do High Level Task X
		
		// give them privileges to do High Level Task X
		
		// delete the user
		
		Context.closeSession();
	}

}
