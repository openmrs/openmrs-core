package org.openmrs;

import org.openmrs.api.context.Context;

public class RoleTest extends BaseTest {

	public void testClass() throws Exception {
		
		startup();
		
		Context.authenticate("admin", "test");
		
		Role r = Context.getUserService().getRole("Lab Technician");
		System.out.println(r);
		System.out.println("all roles: " + r.getAllParentRoles());
		assertFalse(r.getAllParentRoles().contains(r));
		
		shutdown();
	}
	
}