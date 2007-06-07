package org.openmrs;

import org.openmrs.api.context.Context;

public class RoleTest extends BaseTest {

	@Override
	protected void onSetUpBeforeTransaction() throws Exception {
		super.onSetUpBeforeTransaction();
		authenticate();
	}

	public void testClass() throws Exception {
		
		Role r = Context.getUserService().getRole("Anonymous");
		System.out.println(r);
		
		assertNotNull(r);
		
		System.out.println("all roles: " + r.getAllParentRoles());
		
		assertFalse(r.getAllParentRoles().contains(r));
		
	}
	
}