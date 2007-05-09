package org.openmrs.api;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.openmrs.BaseTest;
import org.openmrs.PersonName;
import org.openmrs.Privilege;
import org.openmrs.Role;
import org.openmrs.User;
import org.openmrs.api.context.Context;

public class UserServiceTest extends BaseTest {
	
	protected UserService us = Context.getUserService();

	public void testUpdateUser() throws Exception {
		
		Context.authenticate("USER-1", "test");
		
		System.out.println("##start method");
		
		assertTrue(Context.isAuthenticated());
		User u = us.getUserByUsername("USER-1");
		
		if (u == null)
			u = new User();
		u.addName(new PersonName("Benjamin", "A", "Wolfe"));
		u.setUsername("bwolfe");
//		u.setRoles(new HashSet());
		
		System.out.println("##before update");
		
		us.updateUser(u);
		
		System.out.println("##after update, before getbyusername");
		
		User u2 = us.getUserByUsername("bwolfe");
		
		assertTrue(u.equals(u2));
		
		//int len = u.getRoles().size();
		//System.out.println("length: " + len);
		
		Role role1 = new Role();
		role1.setDescription("testing1");
		role1.setRole("test1");
		Privilege p1 = us.getPrivileges().get(0);
		Set<Privilege> privileges1 = new HashSet<Privilege>();
		privileges1.add(p1);
		role1.setPrivileges(privileges1);
		
		Role role2 = new Role();
		role2.setDescription("testing2");
		role2.setRole("test2");
		Privilege p2 = us.getPrivileges().get(1);
		Set privileges2 = new HashSet();
		privileges2.add(p2);
		role2.setPrivileges(privileges2);
		
		us.grantUserRole(u, role1);
		
		System.out.println("##between grants");
		
		us.grantUserRole(u, role2);
		
		System.out.println("##before getting roles");
		
		System.out.println("Roles: " + u.getRoles().toString());
		
		System.out.println("##before getting privileges");
		
		for (Iterator<Role> i = u.getRoles().iterator(); i.hasNext();) {
			Role role = i.next();
			System.out.println("Role: " + role.getRole());
			for (Iterator<Privilege> i2 = role.getPrivileges().iterator(); i2.hasNext();) {
				System.out.println("Priv: " + i2.next().getPrivilege());
			}
		}
		
		shutdown();
	}

	public static Test suite() {
		return new TestSuite(UserServiceTest.class, "Basic UserService functionality");
	}

}
