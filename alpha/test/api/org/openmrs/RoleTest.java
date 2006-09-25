package org.openmrs;

import junit.framework.TestCase;

import org.openmrs.api.context.Context;
import org.openmrs.api.context.ContextFactory;
import org.openmrs.api.db.hibernate.HibernateUtil;

public class RoleTest extends TestCase {

	
	public void testClass() throws Exception {
		
		HibernateUtil.startup();
		Context context = ContextFactory.getContext();
		context.authenticate("ben", "");
		
		Role r = context.getUserService().getRole("Lab Technician");
		System.out.println(r);
		System.out.println("all roles: " + r.getAllParentRoles());
		assertFalse(r.getAllParentRoles().contains(r));
		
		
		HibernateUtil.shutdown();
	}
	
}