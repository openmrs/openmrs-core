package org.openmrs.api.db.hibernate;

import org.openmrs.BaseTest;
import org.openmrs.User;
import org.openmrs.api.UserService;
import org.openmrs.api.context.Context;

public class HibernateTest extends BaseTest {
	
	@Override
	protected void onSetUpBeforeTransaction() throws Exception {
		super.onSetUpBeforeTransaction();
		authenticate();
	}

	public void testGetName() throws Exception {
		
		Context.openSession();
		
		UserService us = Context.getUserService();
		
		User u = us.getUserByUsername("admin");
		
		System.out.println("last name: " + u.getPersonName().getFamilyName());
		
		Context.closeSession();
	}
	
}
