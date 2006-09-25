package org.openmrs.api.db.hibernate;

import java.util.Set;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.openmrs.Role;
import org.openmrs.User;
import org.openmrs.api.UserService;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.ContextFactory;

public class HibernateTest extends TestCase {

	public void xtestRead() {
		Session session = HibernateUtil.currentSession();
		Transaction tx = session.beginTransaction();
		User user = (User) session.get(User.class, 1);
		System.out.println("User = " + user.getFirstName() + " " + user.getLastName());
		Set<Role> roles = user.getRoles();
		for (Role role : roles) {
			System.out.println("  " + role.getRole());			
		}
		System.out.println("middle name (pre) = " + user.getMiddleName());
		user.setMiddleName("Williams");
//		Role newRole = (Role) session.get(Role.class, "nurse");
//		user.removeRole(newRole);
		User user2 = (User) session.get(User.class, 1);
		System.out.println("middle name (post) = " + user2.getMiddleName());
		tx.commit();
		HibernateUtil.closeSession();
	}
		
	public void testGetName() throws Exception {
		
		Context context = ContextFactory.getContext();
		
		context.authenticate("USER-1", "test");
		
		UserService us = context.getUserService();
		
		User u = us.getUserByUsername("USER-1");
		
		System.out.println("last name: " + u.getLastName());
		
		u.setLastName(u.getLastName() + "1");
		
		us.updateUser(u);
		
		User u2 = us.getUserByUsername("USER-1");
		
		System.out.println("last name (post): " + u.getLastName());
	}
	
	public static Test suite() {
		return new TestSuite(HibernateTest.class, "Basic Hibernate functionality");
	}
	
}
