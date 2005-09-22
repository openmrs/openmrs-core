package org.openmrs.api.hibernate;

import java.util.Set;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.openmrs.Role;
import org.openmrs.User;

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
	
	public void testRead() {
		Session session = HibernateUtil.currentSession();
		Transaction tx = session.beginTransaction();
		User user = (User) session.get(User.class, 1);
		System.out.println("User = " + user.getFirstName() + " " + user.getLastName());
		Set<Role> roles = user.getRoles();
		for (Role role : roles) {
			System.out.println("  " + role.getRole());			
		}
		System.out.println("middle name (pre) = " + user.getMiddleName());
		user.setMiddleName("William");
//		Role newRole = (Role) session.get(Role.class, "nurse");
//		user.removeRole(newRole);
		User user2 = (User) session.get(User.class, 1);
		System.out.println("middle name (post) = " + user2.getMiddleName());
		tx.commit();
		HibernateUtil.closeSession();
	}
		
	public static Test suite() {
		return new TestSuite(HibernateTest.class, "Basic Hibernate functionality");
	}
	
}
