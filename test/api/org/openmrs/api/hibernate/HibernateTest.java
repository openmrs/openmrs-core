package org.openmrs.api.hibernate;

import java.util.Set;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.openmrs.Role;
import org.openmrs.User;
import org.openmrs.api.UserService;
import org.openmrs.context.Context;
import org.openmrs.context.ContextFactory;

public class HibernateTest extends TestCase {

	public void xtestHibernate() {
		Session session = HibernateUtil.currentSession();
		Assert.assertNotNull("obtain session object", session);
		Transaction tx = session.beginTransaction();
		Assert.assertNotNull("begin transaction", tx);
		tx.commit();
		session.close();
		HibernateUtil.session.set(null);
	}
	
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
		
		context.authenticate("admin", "test");
		
		UserService us = context.getUserService();
		
		User u = us.getUserByUsername("admin");
		
		System.out.println("last name: " + u.getLastName());
		
		u.setLastName(u.getLastName() + "1");
		
		us.updateUser(u);
		
		User u2 = us.getUserByUsername("admin");
		
		System.out.println("last name (post): " + u.getLastName());
	}
	
	public static Test suite() {
		return new TestSuite(HibernateTest.class, "Basic Hibernate functionality");
	}
	
}
